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
package uk.gov.gchq.palisade.client.internal.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.internal.resource.WebSocketListener.Item;

/**
 * A {@code Resource} object is received in a {@link Item} of type
 * {@code MessageType#RESOURCE} after it has sent a message of type
 * {@code MessageType#CTS}.
 * <p>
 * Note that the {@code UserId} class is created at compile time. The way in
 * which the class is created is determined by the {@code ImmutableStyle}. This
 * class is also compatible with Jackson.
 *
 * @see WebSocketClient
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableResourceMessage.class)
@JsonSerialize(as = ImmutableResourceMessage.class)
public interface ResourceMessage extends WebSocketMessage {

    /**
     * Exposes the generated builder outside this package
     * <p>
     * While the generated implementation (and consequently its builder) is not
     * visible outside of this package. This builder inherits and exposes all public
     * methods defined on the generated implementation's Builder class.
     */
    class Builder extends ImmutableResourceMessage.Builder { // empty
    }

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

}
