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

public interface ResourceRepository extends ReactiveCrudRepository<ResourceEntity, String> {
    int PARALLELISM = 1;

    Mono<Boolean> existsById(String id);

    Mono<ResourceEntity> getByResourceId(String id);

    Flux<ResourceEntity> findAllByResourceIdStartingWith(String prefix);


    default CompletableFuture<Boolean> futureExistsById(String id) {
        return existsById(id).toFuture();
    }

    default Source<ResourceEntity, NotUsed> streamGetByResourceId(String id) {
        return Source.fromPublisher(this.getByResourceId(id));
    }

    default Source<ResourceEntity, NotUsed> streamFindAll() {
        return Source.fromPublisher(this.findAll());
    }

    default Source<ResourceEntity, NotUsed> streamFindAllByResourceIdStartingWith(String prefix) {
        return Source.fromPublisher(this.findAllByResourceIdStartingWith(prefix));
    }

    default Sink<ResourceEntity, CompletionStage<Done>> streamSaveAll() {
        return Sink.foreachAsync(PARALLELISM, entity -> this.save(entity)
                .toFuture()
                // Downcast to Void
                .thenApply(x -> null));
    }
}
