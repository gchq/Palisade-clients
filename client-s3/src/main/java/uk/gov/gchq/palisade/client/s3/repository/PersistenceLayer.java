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

/**
 * Handles both the {@link ResourceRepository} and {@link ContentLengthRepository}, providing metadata on available resources
 */
public class PersistenceLayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceLayer.class);
    private final ResourceRepository resourceRepository;
    private final ContentLengthRepository contentLengthRepository;

    /**
     * Construct a new {@link PersistenceLayer} to manage the multiple repositories.
     *
     * @param resourceRepository      the repository holding the resource metadata
     * @param contentLengthRepository the repository storing resource content-lengths only
     */
    public PersistenceLayer(final ResourceRepository resourceRepository, final ContentLengthRepository contentLengthRepository) {
        this.resourceRepository = resourceRepository;
        this.contentLengthRepository = contentLengthRepository;
    }

    /**
     * Get whether a resource exists for the given id.
     *
     * @param resourceId the resourceId to search for
     * @return true if it exists, false otherwise.
     * This may on future calls return true if/when the resource is returned by the Filtered-Resource Service
     */
    public CompletableFuture<Boolean> existsById(final String resourceId) {
        return resourceRepository.futureExistsById(resourceId);
    }

    /**
     * Get a resource by its id.
     *
     * @param resourceId the resourceId to search for
     * @return the {@link LeafResource}, or {@link Source#empty()} if it did not exist
     */
    public Source<LeafResource, NotUsed> getById(final String resourceId) {
        return resourceRepository.streamGetByResourceId(resourceId)
                .map(ResourceEntity::getResource)
                .map(LeafResource.class::cast);
    }

    /**
     * Get all resources stored.
     *
     * @return a {@link Source} of all resources stored
     */
    public Source<LeafResource, NotUsed> getResources() {
        return resourceRepository.streamFindAll()
                .map(ResourceEntity::getResource)
                .map(LeafResource.class::cast);
    }

    /**
     * Get all resources stored where the id matches some prefix.
     *
     * @param resourcePrefix the porefix for the id to search for
     * @return a {@link Source} of all resources stored matching the prefix
     */
    public Source<LeafResource, NotUsed> getResourcesByPrefix(final String resourcePrefix) {
        return resourceRepository.streamFindAllByResourceIdStartingWith(resourcePrefix)
                .map(ResourceEntity::getResource)
                .map(LeafResource.class::cast);
    }

    /**
     * Get a {@link Sink} for writing resources to persistence.
     *
     * @return a {@link Sink} to be piped into to save resources to persistence
     */
    public Sink<LeafResource, CompletionStage<Done>> putResources() {
        return Flow.<LeafResource>create()
                .map((LeafResource leafResource) -> {
                    ResourceEntity entity = new ResourceEntity(leafResource);
                    LOGGER.info("Persisting resource entity {}", entity);
                    return entity;
                })
                .toMat(resourceRepository.streamSaveAll(), Keep.right());
    }

    /**
     * Get the content-length for a resource, if it can be found.
     *
     * @param resource the resource to get the content-length of
     * @return the length, if it was found
     */
    // Only LeafResources have content, so only they have a content-length, do not use a Resource here
    @SuppressWarnings("java:S3242")
    public CompletableFuture<Optional<Long>> getContentLength(final LeafResource resource) {
        return contentLengthRepository.futureGetByResourceId(resource.getId())
                .thenApply(entity -> entity.map(ContentLengthEntity::getContentLength));
    }

    /**
     * Set the content-length for a resource.
     *
     * @param leafResource  the resource to set the length for
     * @param contentLength the length of the resource
     * @return the stored length, once it has been saved successfully
     */
    public CompletableFuture<Long> putContentLength(final LeafResource leafResource, final Long contentLength) {
        return contentLengthRepository.futureSave(new ContentLengthEntity(leafResource, contentLength))
                .thenApply((ContentLengthEntity entity) -> {
                    LOGGER.info("Persisting content-length entity {}", entity);
                    return entity.getContentLength();
                });
    }
}
