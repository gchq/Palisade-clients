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

public class S3ServerApi implements RouteSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3ServerApi.class);
    protected AkkaClient client;
    protected Materializer materialiser;
    protected PersistenceLayer persistenceLayer;

    protected Map<String, DynamicBucketApi> dynamicBuckets = new ConcurrentHashMap<>();

    public enum AwaitingStatus {
        TOKEN,
        RESOURCES,
        PERSISTENCE,
        DONE
    }

    public S3ServerApi(final AkkaClient client, final Materializer materialiser, final PersistenceLayer persistenceLayer) {
        this.client = client;
        this.materialiser = materialiser;
        this.persistenceLayer = persistenceLayer;
    }

    @Override
    public Route route() {
        return Directives.concat(serverRequest(), bucketRequest());
    }

    public Route serverRequest() {
        return Directives.post(() ->
                Directives.pathPrefix("register", () ->
                        Directives.extract(ctx -> ctx.getRequest().getUri().query().toMap(), context ->
                                Directives.completeWithFuture(registerDynamicBucket(context.get("userId"), context.get("resourceId"), context)
                                        .thenApply(token -> HttpResponse.create().withStatus(StatusCodes.CREATED).withEntity(token))))));
    }

    public Route bucketRequest() {
        return Directives.pathPrefix("request",
                () -> Directives.pathPrefix(bucket -> Optional.ofNullable(dynamicBuckets.get(bucket))
                        .map(RouteSupplier::route)
                        .orElse(Directives.complete(StatusCodes.NOT_FOUND))));
    }

    public CompletableFuture<String> registerDynamicBucket(final String userId, final String resourceId, final Map<String, String> context) {
        LOGGER.info("Register dynamic bucket for {} {} {}", userId, resourceId, context);
        var bucket = new DynamicBucketApi(client, materialiser, persistenceLayer, userId, resourceId, context);
        return bucket.token.thenApply(token -> {
            LOGGER.info("Registered bucket for token {}", token);
            dynamicBuckets.put(token, bucket);
            return token;
        });
    }

    public AwaitingStatus dynamicBucketStatus(final String token) {
        return dynamicBuckets.get(token).getStatus();
    }

}
