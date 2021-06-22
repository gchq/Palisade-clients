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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.LeafResource;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PersistenceLayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceLayer.class);
    private final ResourceRepository resourceRepository;
    private final ContentLengthRepository contentLengthRepository;

    public PersistenceLayer(final ResourceRepository resourceRepository, final ContentLengthRepository contentLengthRepository) {
        this.resourceRepository = resourceRepository;
        this.contentLengthRepository = contentLengthRepository;
    }

    public CompletableFuture<Boolean> existsById(final String resourceId) {
        return resourceRepository.futureExistsById(resourceId);
    }

    public Source<LeafResource, NotUsed> getById(final String resourceId) {
        return resourceRepository.streamGetByResourceId(resourceId)
                .map(ResourceEntity::getResource)
                .map(LeafResource.class::cast);
    }

    public Source<LeafResource, NotUsed> getResources() {
        return resourceRepository.streamFindAll()
                .map(ResourceEntity::getResource)
                .map(LeafResource.class::cast);
    }

    public Source<LeafResource, NotUsed> getResourcesByPrefix(final String resourcePrefix) {
        return resourceRepository.streamFindAllByResourceIdStartingWith(resourcePrefix)
                .map(ResourceEntity::getResource)
                .map(LeafResource.class::cast);
    }

    public Sink<LeafResource, CompletionStage<Done>> putResources() {
        return Flow.<LeafResource>create()
                .map((LeafResource leafResource) -> {
                    ResourceEntity entity = new ResourceEntity(leafResource);
                    LOGGER.info("Persisting resource entity {}", entity);
                    return entity;
                })
                .toMat(resourceRepository.streamSaveAll(), Keep.right());
    }

    public CompletableFuture<Optional<Long>> getContentLength(final LeafResource resource) {
        return contentLengthRepository.futureGetByResourceId(resource.getId())
                .thenApply(entity -> entity.map(ContentLengthEntity::getContentLength));
    }

    public CompletableFuture<Long> putContentLength(final LeafResource leafResource, final Long contentLength) {
        return contentLengthRepository.futureSave(new ContentLengthEntity(leafResource, contentLength))
                .thenApply(entity -> {
                    LOGGER.info("Persisting content-length entity {}", entity);
                    return entity.getContentLength();
                });
    }
}
