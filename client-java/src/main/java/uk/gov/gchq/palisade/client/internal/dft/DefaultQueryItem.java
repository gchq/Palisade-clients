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

package uk.gov.gchq.palisade.client.internal.dft;

import uk.gov.gchq.palisade.client.QueryItem;
import uk.gov.gchq.palisade.client.internal.model.MessageType;
import uk.gov.gchq.palisade.client.internal.model.Token;
import uk.gov.gchq.palisade.client.internal.model.WebSocketMessage;
import uk.gov.gchq.palisade.resource.LeafResource;

import java.util.Optional;

public class DefaultQueryItem implements QueryItem {

    private final WebSocketMessage message;

    public DefaultQueryItem(final WebSocketMessage message) {
        assert message.getType() == MessageType.RESOURCE || message.getType() == MessageType.ERROR;
        this.message = message;
    }

    @Override
    public ItemType getType() {
        switch (message.getType()) {
            case RESOURCE:
                return ItemType.RESOURCE;
            case ERROR:
                return ItemType.ERROR;
            default:
                return null;
        }
    }

    @Override
    public String getToken() {
        return message.getHeaders().get(Token.HEADER);
    }

    @Override
    public String asError() {
        return Optional.of(getType())
            .filter(ItemType.ERROR::equals)
            .map(isError -> message.getBodyObject(String.class))
            .orElse(null);
    }

    @Override
    public LeafResource asResource() {
        return Optional.of(getType())
            .filter(ItemType.RESOURCE::equals)
            .map(isResource -> message.getBodyObject(LeafResource.class))
            .orElse(null);
    }
}
