/*
 * Copyright 2018-2021 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.client.s3.web;

import akka.Done;
import akka.NotUsed;
import akka.http.javadsl.model.DateTime;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.AccessControlAllowMethods;
import akka.http.javadsl.model.headers.LastModified;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.Route;
import akka.stream.Graph;
import akka.stream.Materializer;
import akka.stream.SourceShape;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.akka.AkkaClient;
import uk.gov.gchq.palisade.client.s3.config.JacksonXmlSupport;
import uk.gov.gchq.palisade.client.s3.domain.CanonicalUser;
import uk.gov.gchq.palisade.client.s3.domain.ListBucketResult;
import uk.gov.gchq.palisade.client.s3.domain.ListEntry;
import uk.gov.gchq.palisade.client.s3.domain.StorageClass;
import uk.gov.gchq.palisade.client.s3.repository.PersistenceLayer;
import uk.gov.gchq.palisade.client.s3.web.S3ServerApi.AwaitingStatus;
import uk.gov.gchq.palisade.resource.LeafResource;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicBucketApi implements RouteSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicBucketApi.class);
    private static final int PARALLELISM = 1;

    protected final Date bucketCreationTime = Date.from(Instant.now());

    protected AkkaClient client;
    protected Materializer materialiser;
    protected PersistenceLayer persistenceLayer;

    // Initial request
    protected String userId;
    protected String resourceId;
    protected Map<String, String> context;

    // Internal client request state
    protected CompletableFuture<String> token;
    protected CompletableFuture<Source<LeafResource, CompletionStage<NotUsed>>> resources;
    protected CompletableFuture<Done> persistence;

    public DynamicBucketApi(final AkkaClient client, final Materializer materialiser, final PersistenceLayer persistenceLayer,
                            final String userId, final String resourceId, final Map<String, String> context) {
        this.client = client;
        this.materialiser = materialiser;
        this.persistenceLayer = persistenceLayer;

        this.userId = userId;
        this.resourceId = resourceId;
        this.context = context;

        this.token = this.client.register(userId, resourceId, context).toCompletableFuture();
        this.resources = this.token.thenApply(this.client::fetchSource);
        this.persistence = this.resources.thenCompose(stream -> stream.runWith(this.persistenceLayer.putResources(), this.materialiser));
    }

    public AwaitingStatus getStatus() {
        if (!this.token.isDone()) {
            return AwaitingStatus.TOKEN;
        } else if (!this.resources.isDone()) {
            return AwaitingStatus.RESOURCES;
        } else if (!this.persistence.isDone()) {
            return AwaitingStatus.PERSISTENCE;
        } else {
            return AwaitingStatus.DONE;
        }
    }

    @Override
    public Route route() {
        Stream<RouteSupplier> routers = Stream.of(new ListObjectsV2(), new GetObject(), new HeadObject(), new HeadBucket());

        return routers
                .map(RouteSupplier::route)
                .reduce(Directives::concat)
                .orElseThrow(() -> new IllegalArgumentException("No route suppliers found to create API server bindings."));
    }

    public class ListObjectsV2 implements RouteSupplier {
        public Route route() {
            Route listObjects = Directives.extract(ctx -> ctx.getRequest().getUri().query().get("continuation-token"), continuationToken ->
                    Directives.extract(ctx -> ctx.getRequest().getUri().query().get("delimiter").orElse("/"), delimiter ->
                            Directives.extract(ctx -> ctx.getRequest().getUri().query().get("max-keys").map(Integer::valueOf).orElse(1_000), maxKeys ->
                                    Directives.extract(ctx -> ctx.getRequest().getUri().query().get("prefix"), prefix -> {
                                        var pathPrefix = prefix.map(p -> resourceId + p);
                                        LOGGER.info("ListBucketV2 for continuationToken={} maxKeys={}, pathPrefix={}", continuationToken, maxKeys, pathPrefix);

                                        // Get stream of resources, using continuation or prefix
                                        var resourceStream = pathPrefix.map(persistenceLayer::getResourcesByPrefix)
                                                .orElseGet(persistenceLayer::getResources)
                                                // Drop resources until we've dropped the last-returned continuation-token
                                                .dropWhile(resource -> continuationToken.map(last -> !resource.getId().equals(last)).orElse(false));

                                        // Split into 'this page' and 'next pages'
                                        var takenKeys = resourceStream.take(maxKeys);

                                        // Construct result container object
                                        var result = new ListBucketResult();
                                        result.setName(token.join());
                                        result.setDelimiter(delimiter);
                                        result.setMaxKeys(maxKeys);

                                        // Add all elements for this page
                                        CompletionStage<Done> done = takenKeys.runWith(Sink.foreachAsync(PARALLELISM,
                                                (LeafResource resource) -> getListEntryForResource(resource)
                                                        .thenAccept(entry -> result.getContents().add(entry))),
                                                materialiser);

                                        // Wait until all elements have been added
                                        done.toCompletableFuture().join();

                                        // Update for next continuation token
                                        if (result.getContents().size() == maxKeys) {
                                            result.setIsTruncated(true);
                                            // Use the last resource-id as next continuation-token (so we can just dropWhile)
                                            result.setNextContinuationToken(result.getContents().get(maxKeys - 1).getKey());
                                            LOGGER.info("Result required truncation, returning {} elements and set continuation-token {}", maxKeys, result.getNextContinuationToken());
                                        } else {
                                            result.setIsTruncated(false);
                                        }

                                        // Populate other fields where relevant
                                        prefix.ifPresent(result::setPrefix);
                                        continuationToken.ifPresent(result::setContinuationToken);
                                        result.setKeyCount(result.getContents().size());

                                        LOGGER.info("Returning result {}", result);
                                        return Directives.<ListBucketResult>complete(StatusCodes.OK, result, JacksonXmlSupport.<ListBucketResult>marshaller());
                                    }))));
            return Directives.get(() -> Directives.pathEndOrSingleSlash(() -> listObjects));
        }
    }

    public class GetObject implements RouteSupplier {
        public Route route() {
            Function<String, Route> getObject = key ->
                    Directives.extract(ctx -> ctx.getRequest().getUri().query().get("partNumber").map(Integer::parseInt), partNumber -> {
                        // Get the object (leafResource) if it exists
                        var pathKey = resourceId + key;
                        LOGGER.info("GetObject for pathKey={}", pathKey);
                        Source<LeafResource, NotUsed> resourceStream = persistenceLayer.getById(pathKey);

                        // If there was an object (leafResource) for this request, construct a response
                        Source<HttpResponse, NotUsed> responseStream = resourceStream
                                .mapAsync(PARALLELISM, leafResource -> upsertAndGetContentLength(leafResource)
                                        .thenApply(contentLength -> HttpResponse.create()
                                                .addHeaders(getDefaultHeadersForFoundResource(leafResource))
                                                .addHeaders(extractUserMetadataHeaders(leafResource))
                                                .withStatus(StatusCodes.OK)
                                                // Return the object data as a chunked stream instead of strict
                                                .withEntity(HttpEntities.create(
                                                        LeafResourceContentType.create(leafResource),
                                                        contentLength,
                                                        client.readSource(token.join(), leafResource)))));

                        // Concat a default, which after taking the head, will be used if there was no object (leafResource) found for the request
                        Source<HttpResponse, NotUsed> resourceOrMissing = responseStream
                                .<NotUsed>concat((Graph<SourceShape<HttpResponse>, NotUsed>) Source.single(HttpResponse.create()
                                        .addHeaders(getDefaultHeadersForMissingResource())
                                        .withStatus(StatusCodes.NOT_FOUND)));

                        // Take (preferably) the response for an object that exists, else the 'object not found' default
                        CompletableFuture<HttpResponse> futureResponse = resourceOrMissing.runWith(Sink.head(), materialiser)
                                .toCompletableFuture();
                        return Directives.completeWithFuture(futureResponse);
                    });
            return Directives.get(() -> restOfPath(getObject));
        }
    }

    public class HeadBucket implements RouteSupplier {
        public Route route() {
            Supplier<Route> headBucket = () -> {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("HeadBucket for bucket={}", token.join());
                }

                return Directives.complete(HttpResponse.create()
                        .addHeaders(List.of(
                                LastModified.create(DateTime.create(bucketCreationTime.toInstant().toEpochMilli())),
                                AccessControlAllowMethods.create(HttpMethods.HEAD, HttpMethods.GET)
                        ))
                        .withStatus(StatusCodes.OK));
            };

            return Directives.head(() -> Directives.pathEndOrSingleSlash(headBucket));
        }
    }

    public class HeadObject implements RouteSupplier {
        public Route route() {
            Function<String, Route> headObject = key -> {
                // Get the object (leafResource) if it exists
                var pathKey = resourceId + key;
                LOGGER.info("HeadObject for pathKey={}", pathKey);
                Source<LeafResource, NotUsed> resourceStream = persistenceLayer.getById(pathKey);

                // If there was an object (leafResource) for this request, construct a response
                Source<HttpResponse, NotUsed> responseStream = resourceStream
                        .mapAsync(PARALLELISM, leafResource -> {
                            LOGGER.info("Found resource {}", leafResource);
                            return upsertAndGetContentLength(leafResource)
                                    .thenApply(contentLength -> HttpResponse.create()
                                            .addHeaders(getDefaultHeadersForFoundResource(leafResource))
                                            .addHeaders(extractUserMetadataHeaders(leafResource))
                                            .withStatus(StatusCodes.OK)
                                            .withEntity(HttpEntities.create(
                                                    LeafResourceContentType.create(leafResource),
                                                    contentLength,
                                                    Source.empty())));
                        })

                        // Concat a default, which after taking the head, will be used if there was no object (leafResource) found for the request
                        .<NotUsed>concat((Graph<SourceShape<HttpResponse>, NotUsed>) Source.lazySingle(() -> {
                            LOGGER.info("Did not find resource");
                            return HttpResponse.create()
                                    .addHeaders(getDefaultHeadersForMissingResource())
                                    .withStatus(StatusCodes.NOT_FOUND);
                        }));

                // Take (preferably) the response for an object that exists, else the 'object not found' default
                CompletableFuture<HttpResponse> futureResponse = responseStream.runWith(Sink.head(), materialiser)
                        .toCompletableFuture();
                return Directives.completeWithFuture(futureResponse);
            };
            return Directives.head(() -> restOfPath(headObject));
        }
    }

    private CompletableFuture<Long> upsertAndGetContentLength(final LeafResource leafResource) {
        return persistenceLayer.getContentLength(leafResource)
                .thenCompose(maybeLength -> maybeLength
                        .map(length -> {
                            LOGGER.info("Got content-length {} for resource {}", length, leafResource);
                            return CompletableFuture.completedFuture(length);
                        })
                        .orElseGet(() -> {
                            CompletableFuture<Long> contentLengthFuture = client.readSource(token.join(), leafResource)
                                    .map(ByteString::length)
                                    .map(Long::valueOf)
                                    .runWith(Sink.fold(0L, Long::sum), materialiser)
                                    .toCompletableFuture();
                            return contentLengthFuture
                                    .thenApply((Long length) -> {
                                        LOGGER.info("Calculated and upserting content-length {} for resource {}", length, leafResource);
                                        return length;
                                    })
                                    .thenCompose(length -> persistenceLayer.putContentLength(leafResource, length));
                        }));
    }

    protected CompletableFuture<ListEntry> getListEntryForResource(LeafResource resource) {
        return upsertAndGetContentLength(resource).thenApply(contentLength -> {
            var entry = new ListEntry();

            // Set the key to the resourceId
            entry.setKey(resource.getId().replaceFirst(resourceId, ""));
            // Set the lastModified time to now (or whenever?)
            entry.setLastModified(bucketCreationTime);
            // The actual size is unknown because of rule application
            // We have to read the resource to populate the field
            entry.setSize(contentLength);
            // We don't have a concept of storageClasses
            entry.setStorageClass(StorageClass.UNKNOWN);
            entry.seteTag(resource.getId());
            // Set the owner to the user who registered the request
            CanonicalUser owner = new CanonicalUser();
            owner.setId(userId);
            owner.setDisplayName(userId);
            entry.setOwner(owner);

            return entry;
        });
    }

    private List<HttpHeader> getDefaultHeadersForFoundResource(final LeafResource leafResource) {
        return List.of(
                LastModified.create(DateTime.create(bucketCreationTime.toInstant().toEpochMilli())),
                AccessControlAllowMethods.create(HttpMethods.HEAD, HttpMethods.GET),
                RawHeader.create("etag", leafResource.getId())
        );
    }

    private List<HttpHeader> getDefaultHeadersForMissingResource() {
        return List.of(
                LastModified.create(DateTime.create(bucketCreationTime.toInstant().toEpochMilli())),
                AccessControlAllowMethods.create(HttpMethods.HEAD, HttpMethods.GET)
        );
    }

    private static List<HttpHeader> extractUserMetadataHeaders(final LeafResource leafResource) {
        return leafResource.getAttributes().entrySet().stream()
                .map(entry -> RawHeader.create("x-amz-meta-" + entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private static Route restOfPath(Function<String, Route> inner) {
        return restOfPath0(inner, new LinkedList<>());
    }

    private static Route restOfPath0(Function<String, Route> inner, LinkedList<String> segments) {
        return Directives.concat(Directives.pathEnd(() -> inner.apply(String.join("/", segments))),
                Directives.pathPrefix(match -> {
                    segments.addLast(match);
                    return restOfPath0(inner, segments);
                }));
    }
}
