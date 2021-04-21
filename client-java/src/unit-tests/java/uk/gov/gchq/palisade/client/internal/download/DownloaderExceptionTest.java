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
package uk.gov.gchq.palisade.client.internal.download;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DownloaderExceptionTest {

    /**
     * Test method for {@link uk.gov.gchq.palisade.client.internal.download.DownloaderException#getStatusCode()}.
     */
    @Test
    void testGetStatusCode() {
        var expectedCode = 400;
        var exception = new DownloaderException("oops", expectedCode);
        assertThat(exception.getStatusCode())
            .as("check %s's status code", DownloaderException.class.getSimpleName())
            .isEqualTo(400);
    }

}
