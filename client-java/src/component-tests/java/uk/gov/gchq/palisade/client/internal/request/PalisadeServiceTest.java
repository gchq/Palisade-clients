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
package uk.gov.gchq.palisade.client.internal.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.ClientException;

import javax.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.TOKEN;

@MicronautTest
class PalisadeServiceTest {

    @Inject ObjectMapper objectMapper;
    @Inject EmbeddedServer embeddedServer;

    @Test
    void testSubmit() throws Exception {

        var port = embeddedServer.getPort();
        var uri = new URI("http://localhost:" + port + "/cluster/palisade/api/registerDataRequest");
        var palisadeRequest = PalisadeRequest.createPalisadeRequest(b -> b
            .resourceId("resource_id")
            .userId("user_id")
            .putContext("key", "value"));

        var service = PalisadeService.createPalisadeService(b -> b
            .httpClient(HttpClient.newHttpClient())
            .objectMapper(objectMapper)
            .uri(uri));

        var palisadeResponse = service.submit(palisadeRequest);

        assertThat(palisadeResponse)
            .as("A valid response has been returned")
            .isNotNull();

        var expectedToken = TOKEN;

        assertThat(palisadeResponse.getToken())
            .as("Response has token %s", expectedToken)
            .isEqualTo(expectedToken);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testCheckStatusOK202() {

        var expectedResponse = mock(HttpResponse.class);

        when(expectedResponse.statusCode()).thenReturn(202);

        assertThat(PalisadeService.checkStatusOK(expectedResponse))
            .as("A valid response has correct status of ")
            .isEqualTo(expectedResponse);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testCheckStatusOK404() {

        var response = mock(HttpResponse.class);
        var expectedException = ClientException.class;

        when(response.statusCode()).thenReturn(404);

        assertThatExceptionOfType(expectedException)
            .as("%s thrown when checking invalid response with no body", expectedException)
            .isThrownBy(() -> PalisadeService.checkStatusOK(response))
            .withMessage("Request to palisade service failed (404) with no body");

        when(response.body()).thenReturn("body");

        assertThatExceptionOfType(expectedException)
            .as("%s thrown when checking invalid response with a body", expectedException)
            .isThrownBy(() -> PalisadeService.checkStatusOK(response))
            .withMessage("Request to palisade service failed (404) with body:\nbody");

    }

}
