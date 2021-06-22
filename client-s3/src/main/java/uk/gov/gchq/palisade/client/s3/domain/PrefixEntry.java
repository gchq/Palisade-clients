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

import uk.gov.gchq.palisade.Generated;

import java.util.StringJoiner;

public class PrefixEntry {

    @JsonProperty(value = "Prefix", required = true)
    protected String prefix;

    @Generated
    public String getPrefix() {
        return prefix;
    }

    @Generated
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", PrefixEntry.class.getSimpleName() + "[", "]")
                .add("prefix='" + prefix + "'")
                .toString();
    }
}
