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

public interface ContentLengthRepository extends ReactiveCrudRepository<ContentLengthEntity, String> {
    int PARALLELISM = 1;

    Mono<Boolean> existsById(String id);

    Mono<ContentLengthEntity> getByResourceId(String id);

    default CompletableFuture<Boolean> futureExistsById(String id) {
        return existsById(id).toFuture();
    }

    default CompletableFuture<Optional<ContentLengthEntity>> futureGetByResourceId(String id) {
        return this.getByResourceId(id)
                .toFuture()
                .thenApply(Optional::ofNullable);
    }

    default CompletableFuture<ContentLengthEntity> futureSave(ContentLengthEntity entity) {
        return this.save(entity)
                .toFuture();
    }
}
