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
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.internal.model.PalisadeRequest;
import uk.gov.gchq.palisade.client.internal.request.PalisadeService;

import javax.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.TOKEN;

@MicronautTest
class PalisadeServiceTest {

    @Inject
    ObjectMapper objectMapper;
    @Inject
    EmbeddedServer embeddedServer;

    @Test
    void testSubmit() throws Exception {

        var port = embeddedServer.getPort();
        var uri = new URI("http://localhost:" + port + "/cluster/palisade/api/registerDataRequest");
        var palisadeRequest = PalisadeRequest.Builder.create()
            .withUserId("user_id")
            .withResourceId("resource_id")
            .withContext(Map.of("key", "value"));

        var service = PalisadeService.createPalisadeService(b -> b
            .httpClient(HttpClient.newHttpClient())
            .objectMapper(objectMapper)
            .uri(uri));

        var palisadeResponse = service.submit(palisadeRequest);

        assertThat(palisadeResponse)
            .as("check valid response")
            .isNotNull()
            .extracting("token")
            .isEqualTo(TOKEN);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testCheckStatusOK202() {

        var expectedResponse = mock(HttpResponse.class);

        when(expectedResponse.statusCode()).thenReturn(202);

        assertThat(PalisadeService.checkStatusOK(expectedResponse))
            .as("check response is valid")
            .isEqualTo(expectedResponse);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testCheckStatusOK404() {

        var response = mock(HttpResponse.class);
        var expectedException = ClientException.class;

        when(response.statusCode()).thenReturn(404);

        assertThatExceptionOfType(expectedException)
            .as("check valid exception thrown for response with no body")
            .isThrownBy(() -> PalisadeService.checkStatusOK(response))
            .withMessage("Request to Palisade Service failed (404) with no body");

        when(response.body()).thenReturn("body");

        assertThatExceptionOfType(expectedException)
            .as("check valid exception thrown response with a body")
            .isThrownBy(() -> PalisadeService.checkStatusOK(response))
            .withMessage("Request to Palisade Service failed (404) with body:\nbody");

    }

}
