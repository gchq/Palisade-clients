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
package uk.gov.gchq.palisade.client.test.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.internal.resource.CompleteMessage;
import uk.gov.gchq.palisade.client.internal.resource.ErrorMessage;
import uk.gov.gchq.palisade.client.internal.resource.ResourceMessage;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketClient;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketMessage;

import javax.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.FILE_PATH_0;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.FILE_PATH_1;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.TOKEN;

/**
 * Note that this class must be public for the subscriptions on the event bus to
 * work. SonarQube complains about this though.
 *
 * @since 0.5.0
 */
@MicronautTest
public class ResourceClientTest {

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
                .httpClient(HttpClient.newHttpClient())
                .token(TOKEN)
                .uri(URI.create("ws://localhost:" + port + "/cluster/resource/%25t"))
                .objectMapper(objectMapper))
            .connect();
    }

    @Test
    void testMessageFlow() {

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
            .as("check resource event0")
            .extracting("id", "token", "url")
            .containsExactly(FILE_PATH_0, TOKEN, "http://localhost:" + embeddedServer.getPort());

        assertThat(event1)
            .as("check resource event1")
            .extracting("id", "token", "url")
            .containsExactly(FILE_PATH_1, TOKEN, "http://localhost:" + embeddedServer.getPort());

        assertThat(event10)
            .as("check event10 (error)")
            .extracting("text")
            .isEqualTo("test error");

        assertThat(event11)
            .as("check event101 (complete)")
            .extracting("token")
            .isEqualTo(TOKEN);

    }

    @SuppressWarnings("unchecked")
    private <T> T getIfInstanceOf(final Object o, final Class<T> c) {
        assertThat(o)
            .as("check instance type of (%s)", o.getClass())
            .isInstanceOf(c);
        return (T) o;
    }

}
