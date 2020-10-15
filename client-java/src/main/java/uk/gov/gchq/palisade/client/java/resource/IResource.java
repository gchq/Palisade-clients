/*
 * Copyright 2020 Crown Copyright
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
package uk.gov.gchq.palisade.client.java.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.request.UserId;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.io.Serializable;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * <p>
 * A {@code Resource} object is received in a {@code Message} of type
 * {@link MessageType#RESOURCE} after it has sent a message of type
 * {@link MessageType#CTS}.
 * </p>
 * <p>
 * Note that the {@link UserId} class is created at compile time. The way in
 * which the class is created is determined by the {@link ImmutableStyle}. This
 * class is also compatible with Jackson.
 * </p>
 *
 * @since 0.5.0
 * @see ResourceClient
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = Resource.class)
@JsonSerialize(as = Resource.class)
public interface IResource extends Serializable {

    /**
     * Helper method to create a {@link Resource} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static Resource create(final UnaryOperator<Resource.Builder> func) {
        return func.apply(Resource.builder()).build();
    }

    /**
     * Returns the token to which this resource is associated with
     *
     * @return the token to which this resource is associated with
     */
    String getToken();

    /**
     * Returns the leaf resource id which is to be downloaded
     *
     * @return the leaf resource id which is to be downloaded
     */
    String getLeafResourceId();

    /**
     * Return the url of the download service
     *
     * @return the url of the download service
     */
    String getUrl();

    /**
     * Returns any extra properties for this resource or an empty map if there are
     * none
     *
     * @return any extra properties for this resource or an empty map if there are
     *         none
     */
    Map<String, String> getProperties();

}
