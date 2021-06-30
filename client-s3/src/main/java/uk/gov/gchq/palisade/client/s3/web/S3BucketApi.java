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
import uk.gov.gchq.palisade.client.s3.web.DynamicS3ServerApi.AwaitingStatus;
import uk.gov.gchq.palisade.resource.LeafResource;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link RouteSupplier} for the server which dynamically creates  a single S3-compliant bucket endpoint.
 * These are created fom a userId, resourceId and context, which will be used later by the class
 * for registering a request with Palisade.
 */
public class S3BucketApi implements RouteSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3BucketApi.class);
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

    /**
     * Construct a new instance of the S3BucketAPI endpoint.
     *
     * @param client           the configured AkkaClient to connect to Palisade
     * @param materialiser     an Akka materialiser for running connected Sources and Sinks
     * @param persistenceLayer persistence for the returned resource metadata from the Filtered-Resource Service (as well as Content-Length hints)
     * @param userId           the userId to register the request with
     * @param resourceId       the resourceId that will be used as the root directory of the bucket, used for registering the request
     * @param context          the context map of key-value pairs to register the request with
     */
    public S3BucketApi(final AkkaClient client, final Materializer materialiser, final PersistenceLayer persistenceLayer,
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

    /**
     * Get the state of this bucket, indicating what part of Palisade is being waited on before the result set is complete.
     *
     * @return an {@link AwaitingStatus}, indicating what part of Palisade is being waited on:
     * {@link AwaitingStatus#TOKEN} if waiting for the token from the Palisade Service
     * {@link AwaitingStatus#RESOURCES} if waiting for more resources from the Filtered-Resource Service
     * {@link AwaitingStatus#PERSISTENCE} if waiting for the stream of returned resources to be saved to persistence
     * {@link AwaitingStatus#DONE} once everything is done
     */
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

    /**
     * Implementation of the S3 'ListObjectsV2' request.
     */
    public class ListObjectsV2 implements RouteSupplier {
        @Override
        public Route route() {
            Route listObjects = Directives.extract(ctx -> ctx.getRequest().getUri().query().get("continuation-token"), (Optional<String> continuationToken) ->
                    Directives.extract(ctx -> ctx.getRequest().getUri().query().get("delimiter").orElse("/"), (String delimiter) ->
                            Directives.extract(ctx -> ctx.getRequest().getUri().query().get("max-keys").map(Integer::valueOf).orElse(1_000), (Integer maxKeys) ->
                                    Directives.extract(ctx -> ctx.getRequest().getUri().query().get("prefix"), (Optional<String> prefix) -> {
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
                                        return Directives.complete(StatusCodes.OK, result, JacksonXmlSupport.marshaller());
                                    }))));
            return Directives.get(() -> Directives.pathEndOrSingleSlash(() -> listObjects));
        }
    }

    /**
     * Implementation of the S3 'GetObject' request.
     */
    public class GetObject implements RouteSupplier {
        @Override
        public Route route() {
            Function<String, Route> getObject = key ->
                    Directives.withRangeSupport(() -> {
                        // Get the object (leafResource) if it exists
                        var pathKey = resourceId + key;
                        LOGGER.info("GetObject for pathKey={}", pathKey);
                        Source<LeafResource, NotUsed> resourceStream = persistenceLayer.getById(pathKey);

                        // If there was an object (leafResource) for this request, construct a response
                        CompletionStage<Optional<HttpResponse>> responseStream = resourceStream
                                .mapAsync(PARALLELISM, (LeafResource leafResource) -> {
                                    var data = client.readSource(token.join(), leafResource);
                                    return insertAndGetContentLength(leafResource)
                                            .thenApply(contentLength -> HttpResponse.create()
                                                    .addHeaders(getDefaultHeadersForFoundResource(leafResource))
                                                    .addHeaders(extractUserMetadataHeaders(leafResource))
                                                    .withStatus(StatusCodes.OK)
                                                    // Return the object data as a chunked stream instead of strict
                                                    .withEntity(HttpEntities.create(
                                                            LeafResourceContentType.create(leafResource),
                                                            contentLength,
                                                            data)));
                                })
                                .runWith(Sink.headOption(), materialiser);

                        // Take the response for an object that exists, else create an 'object not found' default
                        CompletableFuture<HttpResponse> futureResponse = responseStream.toCompletableFuture()
                                .thenApply(maybeResponse -> maybeResponse.orElseGet(() -> HttpResponse.create()
                                        .addHeaders(getDefaultHeadersForMissingResource())
                                        .withStatus(StatusCodes.NOT_FOUND)));
                        return Directives.completeWithFuture(futureResponse);
                    });
            return Directives.get(() -> restOfPath(getObject));
        }
    }

    /**
     * Implementation of the S3 'HeadBucket' request.
     */
    public class HeadBucket implements RouteSupplier {
        @Override
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

    /**
     * Implementation of the S3 'HeadObject' request.
     */
    public class HeadObject implements RouteSupplier {
        @Override
        public Route route() {
            Function<String, Route> headObject = key ->
                    Directives.withRangeSupport(() -> {
                        // Get the object (leafResource) if it exists
                        var pathKey = resourceId + key;
                        LOGGER.info("HeadObject for pathKey={}", pathKey);
                        Source<LeafResource, NotUsed> resourceStream = persistenceLayer.getById(pathKey);

                        // If there was an object (leafResource) for this request, construct a response
                        Source<HttpResponse, NotUsed> responseStream = resourceStream
                                .mapAsync(PARALLELISM, (LeafResource leafResource) -> {
                                    LOGGER.info("Found resource {}", leafResource);
                                    return insertAndGetContentLength(leafResource)
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
                                .concat(Source.lazySingle(() -> {
                                    LOGGER.info("Did not find resource");
                                    return HttpResponse.create()
                                            .addHeaders(getDefaultHeadersForMissingResource())
                                            .withStatus(StatusCodes.NOT_FOUND);
                                }));

                        // Take (preferably) the response for an object that exists, else the 'object not found' default
                        CompletableFuture<HttpResponse> futureResponse = responseStream.runWith(Sink.head(), materialiser)
                                .toCompletableFuture();
                        return Directives.completeWithFuture(futureResponse);
                    });
            return Directives.head(() -> restOfPath(headObject));
        }
    }

    /**
     * Try to get the content-length from persistence, otherwise calculate it and store it.
     *
     * @param leafResource the resource to get the length of
     * @return the content-length of the resource when requested from the data-service
     */
    private CompletableFuture<Long> insertAndGetContentLength(final LeafResource leafResource) {
        return persistenceLayer.getContentLength(leafResource)
                .thenCompose(maybeLength -> maybeLength
                        .map((Long length) -> {
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

    protected CompletableFuture<ListEntry> getListEntryForResource(final LeafResource resource) {
        return insertAndGetContentLength(resource).thenApply((Long contentLength) -> {
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
            entry.setETag(resource.getId());
            // Set the owner to the user who registered the request
            CanonicalUser owner = new CanonicalUser();
            owner.setId(userId);
            owner.setDisplayName(userId);
            entry.setOwner(owner);

            return entry;
        });
    }

    private List<HttpHeader> getDefaultHeadersForFoundResource(final LeafResource leafResource) {
        String eTag;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            sha.update(token.join().getBytes(Charset.defaultCharset()));
            sha.update(bucketCreationTime.toString().getBytes(Charset.defaultCharset()));
            sha.update(leafResource.getId().getBytes(Charset.defaultCharset()));
            eTag = new String(Base64.getEncoder().encode(sha.digest()), Charset.defaultCharset());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("Could not find SHA-256 digest instance", e);
            eTag = token.join() + bucketCreationTime.toString() + leafResource.getId();
        }

        String id2;
        Instant headerTime = Instant.now();
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            sha.update(token.join().getBytes(Charset.defaultCharset()));
            sha.update(bucketCreationTime.toString().getBytes(Charset.defaultCharset()));
            sha.update(headerTime.toString().getBytes(Charset.defaultCharset()));
            id2 = new String(Base64.getEncoder().encode(sha.digest()), Charset.defaultCharset());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("Could not find SHA-256 digest instance", e);
            id2 = token.join() + bucketCreationTime.toString() + headerTime.toString();
        }

        return List.of(
                // AccessControlAllowMethods.create(HttpMethods.HEAD, HttpMethods.GET),
                LastModified.create(DateTime.create(bucketCreationTime.toInstant().toEpochMilli())),
                RawHeader.create("ETag", '"' + eTag + '"'),
                RawHeader.create("x-amz-id-2", id2)
        );
    }

    private List<HttpHeader> getDefaultHeadersForMissingResource() {
        return List.of(
                LastModified.create(DateTime.create(bucketCreationTime.toInstant().toEpochMilli()))
        );
    }

    private static List<HttpHeader> extractUserMetadataHeaders(final LeafResource leafResource) {
        return leafResource.getAttributes().entrySet().stream()
                .map(entry -> RawHeader.create("x-amz-meta-" + entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private static Route restOfPath(final Function<String, Route> inner) {
        return restOfPath0(inner, new LinkedList<>());
    }

    private static Route restOfPath0(final Function<String, Route> inner, final LinkedList<String> segments) {
        return Directives.concat(Directives.pathEnd(() -> inner.apply(String.join("/", segments))),
                Directives.pathPrefix((String match) -> {
                    segments.addLast(match);
                    return restOfPath0(inner, segments);
                }));
    }
}
