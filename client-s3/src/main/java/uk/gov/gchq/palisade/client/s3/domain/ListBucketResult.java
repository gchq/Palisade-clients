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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import uk.gov.gchq.palisade.Generated;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * S3 model for the 'ListBucketResult' XML schema.
 */
// Schema specifies field 'isTruncated', not 'truncated'
@SuppressWarnings("java:S2047")
@JacksonXmlRootElement(localName = "ListBucketResult", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
public class ListBucketResult {

    @JsonProperty(value = "Metadata")
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<MetadataEntry> metadata;
    @JsonProperty(value = "Name")
    protected String name;
    @JsonProperty(value = "Prefix", required = true)
    protected String prefix;
    @JsonProperty(value = "ContinuationToken", required = true)
    protected String continuationToken;
    @JsonProperty(value = "NextContinuationToken")
    protected String nextContinuationToken;
    @JsonProperty(value = "MaxKeys")
    protected int maxKeys;
    @JsonProperty(value = "KeyCount")
    protected int keyCount;
    @JsonProperty(value = "Delimiter")
    protected String delimiter;
    @JsonProperty(value = "IsTruncated")
    protected boolean isTruncated;
    @JsonProperty(value = "Contents")
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<ListEntry> contents;
    @JsonProperty(value = "CommonPrefixes")
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<PrefixEntry> commonPrefixes;

    @Generated
    public List<MetadataEntry> getMetadata() {
        if (metadata == null) {
            metadata = new ArrayList<>();
        }
        return this.metadata;
    }

    @Generated
    public List<ListEntry> getContents() {
        if (contents == null) {
            contents = new ArrayList<>();
        }
        return this.contents;
    }

    @Generated
    public List<PrefixEntry> getCommonPrefixes() {
        if (commonPrefixes == null) {
            commonPrefixes = new ArrayList<>();
        }
        return this.commonPrefixes;
    }

    @Generated
    public String getName() {
        return name;
    }

    @Generated
    public void setName(final String name) {
        this.name = name;
    }

    @Generated
    public String getPrefix() {
        return prefix;
    }

    @Generated
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Generated
    public String getContinuationToken() {
        return continuationToken;
    }

    @Generated
    public void setContinuationToken(final String continuationToken) {
        this.continuationToken = continuationToken;
    }

    @Generated
    public String getNextContinuationToken() {
        return nextContinuationToken;
    }

    @Generated
    public void setNextContinuationToken(final String nextContinuationToken) {
        this.nextContinuationToken = nextContinuationToken;
    }

    @Generated
    public int getMaxKeys() {
        return maxKeys;
    }

    @Generated
    public void setMaxKeys(final int maxKeys) {
        this.maxKeys = maxKeys;
    }

    @Generated
    public int getKeyCount() {
        return keyCount;
    }

    @Generated
    public void setKeyCount(final int keyCount) {
        this.keyCount = keyCount;
    }

    @Generated
    public String getDelimiter() {
        return delimiter;
    }

    @Generated
    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    @Generated
    public boolean getIsTruncated() {
        return isTruncated;
    }

    @Generated
    public void setIsTruncated(final boolean truncated) {
        isTruncated = truncated;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", ListBucketResult.class.getSimpleName() + "[", "]")
                .add("metadata=" + metadata)
                .add("name='" + name + "'")
                .add("prefix='" + prefix + "'")
                .add("continuationToken='" + continuationToken + "'")
                .add("nextContinuationToken='" + nextContinuationToken + "'")
                .add("maxKeys=" + maxKeys)
                .add("keyCount=" + keyCount)
                .add("delimiter='" + delimiter + "'")
                .add("isTruncated=" + isTruncated)
                .add("contents=" + contents)
                .add("commonPrefixes=" + commonPrefixes)
                .toString();
    }
}
