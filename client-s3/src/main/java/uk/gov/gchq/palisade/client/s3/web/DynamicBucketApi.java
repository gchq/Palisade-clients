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
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.Route;
import akka.stream.Materializer;
import akka.stream.javadsl.BroadcastHub;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.akka.AkkaClient;
import uk.gov.gchq.palisade.client.s3.config.JacksonXmlSupport;
import uk.gov.gchq.palisade.client.s3.domain.ListBucketResponse;
import uk.gov.gchq.palisade.client.s3.domain.ListBucketResult;
import uk.gov.gchq.palisade.client.s3.domain.ListEntry;
import uk.gov.gchq.palisade.client.s3.repository.PersistenceLayer;
import uk.gov.gchq.palisade.client.s3.web.S3ServerApi.AwaitingStatus;
import uk.gov.gchq.palisade.resource.LeafResource;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

public class DynamicBucketApi implements RouteSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicBucketApi.class);

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
        Stream<RouteSupplier> routers = Stream.of(new ListObjectsV2(), new GetObject(), new HeadBucket(), new HeadObject());

        return routers
                .map(RouteSupplier::route)
                .reduce(Directives::concat)
                .orElseThrow(() -> new IllegalArgumentException("No route suppliers found to create API server bindings."));
    }

    public class ListObjectsV2 implements RouteSupplier {
        protected Map<String, Source<LeafResource, NotUsed>> inFlightContinuations = new ConcurrentHashMap<>();

        /**
         * <pre>
         * Request Syntax
         * GET /?list-type=2
         *     &continuation-token=ContinuationToken
         *     &delimiter=Delimiter
         *     &encoding-type=EncodingType
         *     &fetch-owner=FetchOwner
         *     &max-keys=MaxKeys
         *     &prefix=Prefix
         *     &start-after=StartAfter
         * HTTP/1.1
         * Host: Bucket.s3.amazonaws.com
         * <p>
         * Response Syntax
         * HTTP/1.1 200 OK
         * Content-Length: ContentLength
         * Content-Type: application/xml
         * Connection: close
         * Body: <ListBucketResult/>
         * </pre>
         *
         * @return
         */
        public Route route() {
            Route listObjects = Directives.extract(ctx -> ctx.getRequest().getUri().query().get("continuation-token"), continuationToken ->
                    Directives.extract(ctx -> ctx.getRequest().getUri().query().get("max-keys").map(Integer::valueOf).orElse(1_000), maxKeys ->
                            Directives.extract(ctx -> ctx.getRequest().getUri().query().get("prefix"), prefix -> {
                                LOGGER.info("ListBucketV2 for continuationToken={} maxKeys={}, prefix={}", continuationToken, maxKeys, prefix);

                                // Get stream of resources, using continuation or prefix
                                var resourceStream = continuationToken.map(inFlightContinuations::get)
                                        .orElse(prefix.map(persistenceLayer::getResourcesByPrefix)
                                                .orElseGet(persistenceLayer::getResources))
                                        .toMat(BroadcastHub.of(LeafResource.class), Keep.right());

                                // Split into 'this page' and 'next pages'
                                var takenKeys = resourceStream.run(materialiser).take(maxKeys);
                                var nextContinuation = resourceStream.run(materialiser).drop(maxKeys);

                                // Construct result container object
                                var result = new ListBucketResult();

                                // Add all elements for this page
                                var done = takenKeys.runWith(Sink.foreach(resource -> {
                                    var entry = new ListEntry();
                                    entry.setKey(resource.getId());
                                    result.getContents().add(entry);
                                }), materialiser);

                                done.toCompletableFuture().join();
                                // Update for next continuation token
                                continuationToken.ifPresent(inFlightContinuations::remove);
                                if (result.getContents().size() == maxKeys) {
                                    var nextContinuationToken = UUID.randomUUID().toString();
                                    inFlightContinuations.put(nextContinuationToken, nextContinuation);
                                    result.setIsTruncated(true);
                                    result.setNextMarker(nextContinuationToken);
                                } else {
                                    result.setIsTruncated(false);
                                }

                                // Populate other fields where relevant
                                prefix.ifPresent(result::setPrefix);
                                continuationToken.ifPresent(result::setMarker);
                                result.setMaxKeys(maxKeys);

                                // Return object
                                ListBucketResponse response = new ListBucketResponse();
                                response.setListBucketResponse(result);
                                return Directives.<ListBucketResponse>complete(StatusCodes.OK, response, JacksonXmlSupport.<ListBucketResponse>marshaller());
                            })));
            return Directives.get(() -> Directives.pathEndOrSingleSlash(() -> listObjects));
        }
    }

    public class GetObject implements RouteSupplier {
        /**
         * <pre>
         * Request Syntax
         * GET /Key+?partNumber=PartNumber
         *     &response-cache-control=ResponseCacheControl
         *     &response-content-disposition=ResponseContentDisposition
         *     &sesponse-content-encoding=ResponseContentEncoding
         *     &response-content-language=ResponseContentLanguage
         *     &response-content-type=ResponseContentType
         *     &response-expires=ResponseExpires
         *     &versionId=VersionId
         * HTTP/1.1
         * Host: Bucket.s3.amazonaws.com
         * <p>
         * Response Syntax
         * HTTP/1.1 200
         * accept-ranges: AcceptRanges
         * Cache-Control: CacheControl
         * Content-Disposition: ContentDisposition
         * Content-Encoding: ContentEncoding
         * Content-Language: ContentLanguage
         * Content-Length: ContentLength
         * Content-Range: ContentRange
         * Content-Type: ContentType
         * Body: data
         * </pre>
         *
         * @return
         */
        public Route route() {
            Function<String, Route> getObject = key -> {
                LOGGER.info("GetObject for key={}", key);
                String resourceKey = resourceId + "/" + key;
                CompletableFuture<Optional<LeafResource>> leafResource = persistenceLayer.getById(resourceKey)
                        .runWith(Sink.headOption(), materialiser)
                        .toCompletableFuture();
                Source<HttpResponse, CompletionStage<NotUsed>> responseSource = Source.completionStageSource(token.thenCompose(tk -> leafResource.thenApply(foundLeaf -> foundLeaf
                        .map(leaf -> client.readSource(tk, leaf).mapMaterializedValue(ignored -> NotUsed.notUsed()))
                        .map(bss -> bss.map(bs -> HttpResponse.create().withStatus(StatusCodes.OK).withEntity(bs)))
                        .orElse(Source.single(HttpResponse.create().withStatus(StatusCodes.NOT_FOUND))))));
                return Directives.completeWithFuture(responseSource.runWith(Sink.head(), materialiser));
            };
            return Directives.get(() -> Directives.path(getObject));
        }
    }

    public class HeadBucket implements RouteSupplier {
        /**
         * <pre>
         * Request Syntax
         * HEAD /
         * HTTP/1.1
         * Host: Bucket.s3.amazonaws.com
         * <p>
         * Response Syntax
         * HTTP/1.1 200
         * </pre>
         *
         * @return
         */
        public Route route() {
            Route headBucket = Directives.complete(StatusCodes.OK);
            return Directives.head(() -> Directives.pathEndOrSingleSlash(() -> headBucket));
        }
    }

    public class HeadObject implements RouteSupplier {
        /**
         * <pre>
         * Request Syntax
         * HEAD /Key+?partNumber=PartNumber
         *     &versionId=VersionId
         * HTTP/1.1
         * Host: Bucket.s3.amazonaws.com
         * <p>
         * Response Syntax
         * HTTP/1.1 200
         * accept-ranges: AcceptRanges
         * Cache-Control: CacheControl
         * Content-Disposition: ContentDisposition
         * Content-Encoding: ContentEncoding
         * Content-Language: ContentLanguage
         * Content-Length: ContentLength
         * Content-Type: application/xml
         * </pre>
         *
         * @return an Akka route completing the request
         */
        public Route route() {
            Function<String, Route> headObject = key -> {
                LOGGER.info("HeadObject for key={}", key);
                CompletableFuture<HttpResponse> status = persistenceLayer.existsById(key)
                        .thenApply(exists -> {
                            if (exists) {
                                return StatusCodes.OK;
                            } else {
                                return StatusCodes.NOT_FOUND;
                            }
                        })
                        .thenApply(HttpResponse.create()::withStatus);
                return Directives.completeWithFuture(status);
            };
            return Directives.head(() -> Directives.path(headObject));
        }
    }
}
