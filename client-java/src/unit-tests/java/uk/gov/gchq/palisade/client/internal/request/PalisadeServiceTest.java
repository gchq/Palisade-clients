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
import org.mockito.Mockito;

import uk.gov.gchq.palisade.client.ClientException;

import javax.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
class PalisadeServiceTest {

    @Inject ObjectMapper objectMapper;
    @Inject EmbeddedServer embeddedServer;

    @Test
    void testSubmit() throws Exception {

        var port = embeddedServer.getPort();
        var uri = new URI("http://localhost:" + port + "/cluster/palisade/registerDataRequest");
        var palisadeRequest = PalisadeRequest.createPalisadeRequest(b -> b
            .resourceId("resource_id")
            .userId("user_id")
            .putContext("key", "value"));

        var service = PalisadeService.createPalisadeService(b -> b
            .httpClient(HttpClient.newHttpClient())
            .objectMapper(objectMapper)
            .uri(uri));

        var palisadeResponse = service.submit(palisadeRequest);

        assertThat(palisadeResponse).isNotNull();
        assertThat(palisadeResponse.getToken()).isEqualTo("abcd-1");

    }

    @Test
    void testCheckStatusOK202() {
        var response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.statusCode()).thenReturn(202);
        assertThat(PalisadeService.checkStatusOK(response)).isEqualTo(response);
    }

    @Test
    void testCheckStatusOK404() {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        assertThatExceptionOfType(ClientException.class).isThrownBy(() -> PalisadeService.checkStatusOK(response))
            .withMessage("Request to palisade service failed (404) with no body");
        when(response.body()).thenReturn("body");
        assertThatExceptionOfType(ClientException.class).isThrownBy(() -> PalisadeService.checkStatusOK(response))
            .withMessage("Request to palisade service failed (404) with body:\nbody");

    }

}
