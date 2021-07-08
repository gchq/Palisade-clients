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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import uk.gov.gchq.palisade.Generated;

import java.util.StringJoiner;


/**
 * S3 model for the 'CanonicalUser' XML schema.
 */
public class CanonicalUser {

    @JsonProperty(value = "ID", required = true)
    @JacksonXmlProperty(namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
    protected String id;
    @JsonProperty(value = "DisplayName")
    @JacksonXmlProperty(namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
    protected String displayName;

    @Generated
    public String getId() {
        return id;
    }

    @Generated
    public void setId(final String id) {
        this.id = id;
    }

    @Generated
    public String getDisplayName() {
        return displayName;
    }

    @Generated
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", CanonicalUser.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("displayName='" + displayName + "'")
                .toString();
    }
}
