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

package uk.gov.gchq.palisade.client.s3.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.gchq.palisade.Generated;

import java.util.Date;
import java.util.StringJoiner;

public class ListEntry {

    @JsonProperty(value = "Key", required = true)
    protected String key;
    @JsonProperty(value = "LastModified", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    protected Date lastModified;
    @JsonProperty(value = "ETag", required = true)
    protected String eTag;
    @JsonProperty(value = "Size")
    protected long size;
    @JsonProperty(value = "Owner")
    protected CanonicalUser owner;
    @JsonProperty(value = "StorageClass", required = true)
    protected StorageClass storageClass;

    @Generated
    public String getKey() {
        return key;
    }

    @Generated
    public void setKey(final String key) {
        this.key = key;
    }

    @Generated
    public Date getLastModified() {
        return lastModified;
    }

    @Generated
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    @Generated
    public String geteTag() {
        return eTag;
    }

    @Generated
    public void seteTag(final String eTag) {
        this.eTag = eTag;
    }

    @Generated
    public long getSize() {
        return size;
    }

    @Generated
    public void setSize(final long size) {
        this.size = size;
    }

    @Generated
    public CanonicalUser getOwner() {
        return owner;
    }

    @Generated
    public void setOwner(final CanonicalUser owner) {
        this.owner = owner;
    }

    @Generated
    public StorageClass getStorageClass() {
        return storageClass;
    }

    @Generated
    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", ListEntry.class.getSimpleName() + "[", "]")
                .add("key='" + key + "'")
                .add("lastModified=" + lastModified)
                .add("eTag='" + eTag + "'")
                .add("size=" + size)
                .add("owner=" + owner)
                .add("storageClass=" + storageClass)
                .toString();
    }
}
