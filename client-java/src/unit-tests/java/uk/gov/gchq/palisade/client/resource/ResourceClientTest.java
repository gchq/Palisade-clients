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
package uk.gov.gchq.palisade.client.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Note that this class must be public for the subscriptions on the event bus to
 * work. SonarQube complains about this though.
 *
 * @since 0.5.0
 */
@MicronautTest
public class ResourceClientTest {

    private static final String TOKEN = "abcd-1";

    @Inject
    EmbeddedServer embeddedServer;

    private ObjectMapper objectMapper;
    private WebSocketClient resourceClient;

    private int port;

    @BeforeEach
    void setup() {
        this.port = embeddedServer.getPort();
        this.objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        this.resourceClient = WebSocketClient
            .createResourceClient(b -> b
                .token(TOKEN)
                .baseUri("ws://localhost:" + port + "/cluster/filteredResource/name/%t")
                .objectMapper(objectMapper))
            .connect();
    }

    @Test
    void testMessageFlow() throws Exception {

        // There are two resources so we should have 3 events (2 resource and 1
        // complete)

        var messages = new ArrayList<WebSocketMessage>();
        var message = (WebSocketMessage) null;
        do {
            message = resourceClient.poll(5, TimeUnit.SECONDS);
            messages.add(message);
        } while (!(message instanceof CompleteMessage));

        assertThat(messages).hasSize(12);

        var event0 = getIfInstanceOf(messages.get(0), ResourceMessage.class);
        var event1 = getIfInstanceOf(messages.get(1), ResourceMessage.class);
        var event10 = getIfInstanceOf(messages.get(10), ErrorMessage.class);
        var event11 = getIfInstanceOf(messages.get(11), CompleteMessage.class);

        assertThat(event0)
            .extracting("leafResourceId", "token", "url")
            .containsExactly("resources/pi0.txt", TOKEN, "http://localhost:" + embeddedServer.getPort());

        assertThat(event1)
            .extracting("leafResourceId", "token", "url")
            .containsExactly("resources/pi1.txt", TOKEN, "http://localhost:" + embeddedServer.getPort());

        assertThat(event10)
            .extracting("text")
            .isEqualTo("test error");

        assertThat(event11)
            .extracting("token")
            .isEqualTo(TOKEN);

    }

    @SuppressWarnings("unchecked")
    private <T> T getIfInstanceOf(final Object o, final Class<T> c) {
        assertThat(o).isInstanceOf(c);
        return (T) o;
    }

}
