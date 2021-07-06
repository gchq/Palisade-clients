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

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.Route;
import akka.stream.Materializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.akka.AkkaClient;
import uk.gov.gchq.palisade.client.s3.repository.PersistenceLayer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main interface from REST into the s3-client server, allowing registering requests - /register.
 * These requests return a token, which is used as the bucket-name - /request/{bucket-name}.
 */
public class DynamicS3ServerApi implements RouteSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicS3ServerApi.class);
    protected AkkaClient client;
    protected Materializer materialiser;
    protected PersistenceLayer persistenceLayer;

    protected Map<String, S3BucketApi> dynamicBuckets = new ConcurrentHashMap<>();

    /**
     * The state of a registered bucket:
     * {@link AwaitingStatus#TOKEN} if waiting for the token from the Palisade Service
     * {@link AwaitingStatus#RESOURCES} if waiting for more resources from the Filtered-Resource Service
     * {@link AwaitingStatus#PERSISTENCE} if waiting for the stream of returned resources to be saved to persistence
     * {@link AwaitingStatus#DONE} once everything is done
     * {@link AwaitingStatus#UNKNOWN} otherwise (the status could not be deduced, likely because there was an error in processing)
     */
    public enum AwaitingStatus {
        UNKNOWN,
        TOKEN,
        RESOURCES,
        PERSISTENCE,
        DONE
    }

    /**
     * Construct a new instance of the server.
     *
     * @param client           the configured AkkaClient to connect to Palisade
     * @param materialiser     an Akka materialiser for running connected Sources and Sinks
     * @param persistenceLayer persistence for the returned resource metadata from the Filtered-Resource Service (as well as Content-Length hints)
     */
    public DynamicS3ServerApi(final AkkaClient client, final Materializer materialiser, final PersistenceLayer persistenceLayer) {
        this.client = client;
        this.materialiser = materialiser;
        this.persistenceLayer = persistenceLayer;
    }

    @Override
    public Route route() {
        return Directives.concat(serverRequest(), bucketRequest());
    }

    private Route serverRequest() {
        return Directives.post(() ->
                Directives.pathPrefix("register", () ->
                        Directives.extract(ctx -> ctx.getRequest().getUri().query().toMap(), context ->
                                Directives.completeWithFuture(registerDynamicBucket(context.get("userId"), context.get("resourceId"), context)
                                        .thenApply(token -> HttpResponse.create().withStatus(StatusCodes.CREATED).withEntity(token))))));
    }

    private Route bucketRequest() {
        return Directives.pathPrefix("request",
                () -> Directives.pathPrefix(bucket -> Optional.ofNullable(dynamicBuckets.get(bucket))
                        .map(RouteSupplier::route)
                        .orElse(Directives.complete(StatusCodes.NOT_FOUND, dynamicBucketStatus(bucket), Jackson.marshaller()))));
    }

    private CompletableFuture<String> registerDynamicBucket(final String userId, final String resourceId, final Map<String, String> context) {
        LOGGER.info("Register dynamic bucket for {} {} {}", userId, resourceId, context);
        var bucket = new S3BucketApi(client, materialiser, persistenceLayer, userId, resourceId, context);
        return bucket.token.thenApply((String token) -> {
            LOGGER.info("Registered bucket for token {}", token);
            dynamicBuckets.put(token, bucket);
            return token;
        });
    }

    private AwaitingStatus dynamicBucketStatus(final String token) {
        return Optional.ofNullable(dynamicBuckets.get(token)).map(S3BucketApi::getStatus).orElse(AwaitingStatus.UNKNOWN);
    }

}
