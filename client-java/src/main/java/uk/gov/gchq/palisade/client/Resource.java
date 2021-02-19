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
package uk.gov.gchq.palisade.client;

import java.util.Optional;

/**
 * A resource message
 *
 * @since 0.5.0
 */
public interface Resource extends Message {

    /**
     * Returns the leaf resource id. The leaf resource id represents a child
     * resource at some point in the hierarchy of the requested parent.
     * <p>
     * For example if a request was made to an AWS S3 bucket, then a leaf resource
     * id would represent a file within the bucket.
     *
     * @return the leaf resource id
     */
    String getLeafResourceId();

    /**
     * Returns the URL location of the resource
     *
     * @return the URL location of the resource
     */
    String getUrl();

    /**
     * Returns the type. The type describes the data. Whereas
     * {@code #getSerialisedFormat()} describes the format of the data, e.g. json,
     * the type describes the context of the data. This could be the structural
     * format, for example "Employee".
     *
     * @return the type
     */
    Optional<String> getType();

    /**
     * Returns the serialised format. The serialised format is the encoding of the
     * byte stream which is provided via a {@code Download} once a resource is
     * fetched. The format would determine what type of deserialiser to be used when
     * processing the data. Examples of this might be avro, json, xml, yaml, etc.
     *
     * @return the serialised format
     */
    Optional<String> getSerialisedFormat();

}
