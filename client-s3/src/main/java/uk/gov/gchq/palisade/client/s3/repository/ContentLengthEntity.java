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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.resource.Resource;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * The Database uses this as the object that will be stored in the backing store linked by an ID
 * In this case the ResourceID and ResourceEntity make up the key
 * This contains all objects that will be go into the database, including how they are serialised and indexed
 */
@Table("content_lengths")
public class ContentLengthEntity implements Serializable, Persistable<String> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column("resource_id")
    private final String resourceId;

    @Column("content_length")
    private final Long contentLength;

    @Transient
    private final boolean isNew;

    @PersistenceConstructor
    @JsonCreator
    private ContentLengthEntity(final @JsonProperty("resourceId") String resourceId,
                                final @JsonProperty("parentId") Long contentLength) {
        this(resourceId, contentLength, false);
    }

    private ContentLengthEntity(final String resourceId, final Long contentLength, final boolean isNew) {
        this.resourceId = resourceId;
        this.contentLength = contentLength;
        this.isNew = isNew;
    }

    /**
     * Constructor used for the Database that takes a Resource and extracts the values
     * Used for inserting objects into the backing store
     *
     * @param resource specified to insert into the backing store
     */
    public ContentLengthEntity(final Resource resource, final Long contentLength) {
        this(
                resource.getId(),
                contentLength,
                true
        );
    }

    @Override
    @JsonIgnore
    public String getId() {
        return resourceId;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

    @Generated
    public String getResourceId() {
        return getId();
    }

    public Long getContentLength() {
        return contentLength;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", ContentLengthEntity.class.getSimpleName() + "[", "]")
                .add("resourceId='" + resourceId + "'")
                .add("contentLength=" + contentLength)
                .add("isNew=" + isNew)
                .toString();
    }
}
