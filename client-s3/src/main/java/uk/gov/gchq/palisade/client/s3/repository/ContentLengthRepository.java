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

package uk.gov.gchq.palisade.client.s3.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Repository for storing/caching content-lengths of resources after rules are applied.
 * Used to inform a client of the S3 server on HTTP Content-Length (e.g. Spark is quite picky about it).
 */
public interface ContentLengthRepository extends ReactiveCrudRepository<ContentLengthEntity, String> {
    /**
     * Is there a length stored for a given resourceId.
     *
     * @param id the resourceId to check if there is a length stored
     * @return true if there is a length stored, false otherwise
     */
    Mono<Boolean> existsById(String id);

    /**
     * Get a Content-Length for a resourceId.
     *
     * @param id the resourceId to get a length for
     * @return a {@link ContentLengthEntity} containing the Content-Length
     */
    Mono<ContentLengthEntity> getByResourceId(String id);

    /**
     * Is there a length stored for a given resourceId.
     *
     * @param id the resourceId to check if there is a length stored
     * @return true if there is a length stored, false otherwise
     */
    default CompletableFuture<Boolean> futureExistsById(String id) {
        return existsById(id).toFuture();
    }

    /**
     * Get a Content-Length for a resourceId.
     *
     * @param id the resourceId to get a length for
     * @return a {@link ContentLengthEntity} containing the Content-Length
     */
    default CompletableFuture<Optional<ContentLengthEntity>> futureGetByResourceId(String id) {
        return this.getByResourceId(id)
                .toFuture()
                .thenApply(Optional::ofNullable);
    }

    /**
     * Set a Content-Length for a given resourceId.
     *
     * @param entity a {@link ContentLengthEntity} that will be stored, keyed by its id
     * @return the saved {@link ContentLengthEntity}, once it has been saved
     */
    default CompletableFuture<ContentLengthEntity> futureSave(ContentLengthEntity entity) {
        return this.save(entity)
                .toFuture();
    }
}
