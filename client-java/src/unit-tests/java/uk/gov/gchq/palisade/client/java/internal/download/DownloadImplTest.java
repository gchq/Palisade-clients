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
package uk.gov.gchq.palisade.client.java.internal.download;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DownloadImplTest {

    private static final String FILENAME = "cool.html";
    private static final String CONTENT_DISPOSITION = String.format("attachment; filename=\"%s\"", FILENAME);
    private static final InputStream BODY = new ByteArrayInputStream(new byte[]{'a', 'b', 'c'});

    private DownloadImpl download;

    @SuppressWarnings("resource") // suppress potential resource leak warning
    @BeforeEach
    void setUp(
            @Mock final HttpResponse<InputStream> response,
            @Mock final HttpHeaders headers) throws Exception {

        lenient().when(response.headers()).thenReturn(headers);
        lenient().when(response.body()).thenReturn(BODY);
        lenient().when(headers.firstValue("Content-Disposition")).thenReturn(Optional.of(CONTENT_DISPOSITION));

        this.download = new DownloadImpl(response);

    }

    @Test
    void testGetInputStream() throws Exception {
        try (var is = download.getInputStream()) {
            assertThat(is)
                    .as("check input stream")
                    .isEqualTo(BODY);
        }
    }

}
