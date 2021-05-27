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
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import uk.gov.gchq.palisade.resource.LeafResource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PersistenceLayer {
    private final ResourceRepository resourceRepository;

    public PersistenceLayer(final ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public CompletableFuture<Boolean> existsById(final String resourceId) {
        return resourceRepository.futureExistsById(resourceId);
    }

    public Source<LeafResource, NotUsed> getById(final String resourceId) {
        return resourceRepository.streamGetById(resourceId)
                .map(ResourceEntity::getResource)
                .map(LeafResource.class::cast);
    }

    public Source<LeafResource, NotUsed> getResources() {
        return resourceRepository.streamFindAll()
                .map(ResourceEntity::getResource)
                .map(LeafResource.class::cast);
    }

    public Source<LeafResource, NotUsed> getResourcesByPrefix(final String resourcePrefix) {
        return resourceRepository.streamFindAllByIdStartingWith(resourcePrefix)
                .map(ResourceEntity::getResource)
                .map(LeafResource.class::cast);
    }

    public Sink<LeafResource, CompletionStage<Done>> putResources() {
        return Flow.<LeafResource>create()
                .map(ResourceEntity::new)
                .toMat(resourceRepository.streamSaveAll(), Keep.right());
    }
}
