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

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Repository for storing/caching resources as they are returned from the Filtered-Resource Service.
 */
public interface ResourceRepository extends ReactiveCrudRepository<ResourceEntity, String> {
    int PARALLELISM = 1;

    /**
     * Is there a resource matching the requested resourceId.
     *
     * @param id the resourceId to search for
     * @return true if the resource exists, false otherwise
     */
    Mono<Boolean> existsById(String id);

    /**
     * Get a resource matching the requested resourceId.
     *
     * @param id the resourceId to search for
     * @return a {@link ResourceEntity} containing the resource if the resource exists
     */
    Mono<ResourceEntity> getByResourceId(String id);

    /**
     * Find all resources stored where the id begins with the given prefix.
     *
     * @param prefix the prefix for resourceIds to search for
     * @return all resources that matched the prefix
     */
    Flux<ResourceEntity> findAllByResourceIdStartingWith(String prefix);

    /**
     * Is there a resource matching the requested resourceId.
     *
     * @param id the resourceId to search for
     * @return true if the resource exists, false otherwise
     */
    default CompletableFuture<Boolean> futureExistsById(String id) {
        return existsById(id).toFuture();
    }

    /**
     * Get a resource matching the requested resourceId.
     *
     * @param id the resourceId to search for
     * @return a {@link ResourceEntity} containing the resource if the resource exists, {@link Source#empty()} otherwise
     */
    default Source<ResourceEntity, NotUsed> streamGetByResourceId(String id) {
        return Source.fromPublisher(this.getByResourceId(id));
    }

    /**
     * Find all resources stored.
     *
     * @return all resources stored in persistence
     */
    default Source<ResourceEntity, NotUsed> streamFindAll() {
        return Source.fromPublisher(this.findAll());
    }

    /**
     * Find all resources stored where the id begins with the given prefix.
     *
     * @param prefix the prefix for resourceIds to search for
     * @return all resources that matched the prefix
     */
    default Source<ResourceEntity, NotUsed> streamFindAllByResourceIdStartingWith(String prefix) {
        return Source.fromPublisher(this.findAllByResourceIdStartingWith(prefix));
    }

    /**
     * Get a {@link Sink} for writing resources to persistence.
     *
     * @return a {@link Sink} to be piped into to save resources to persistence
     */
    default Sink<ResourceEntity, CompletionStage<Done>> streamSaveAll() {
        return Sink.foreachAsync(PARALLELISM, entity -> this.save(entity)
                .toFuture()
                // Downcast to Void
                .thenApply(x -> null));
    }
}
