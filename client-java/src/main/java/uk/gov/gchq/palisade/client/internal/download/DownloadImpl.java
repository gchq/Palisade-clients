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

import uk.gov.gchq.palisade.client.Download;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A download is returned after a request is received from data service. This
 * object contains access to the input stream and extra information such as size
 * in bytes and the name.
 *
 * @since 0.5.0
 */
public class DownloadImpl implements Download {

    private static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HTTP_HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final Pattern FILENAME_PATTERN = Pattern.compile("attachment;\\s*filename\\s*=\\s*\"([^\"]*)\"");

    private final HttpResponse<InputStream> response;

    /**
     * Create and returns a new {@code DownloadImpl} with the provided
     * {@code HttpResponse} returned from the data service.
     *
     * @param response from the data service
     */
    public DownloadImpl(final HttpResponse<InputStream> response) {
        this.response = response;
    }

    @Override
    public int getLength() {
        return response
            .headers()
            .firstValue(HTTP_HEADER_CONTENT_LENGTH)
            .map(Integer::valueOf)
            .orElse(Download.super.getLength());
    }

    @SuppressWarnings("java:S1774")
    @Override
    public Optional<String> getFilename() {
        return response
            .headers()
            .firstValue(HTTP_HEADER_CONTENT_DISPOSITION)
            .map((final String dis) -> {
                var matcher = FILENAME_PATTERN.matcher(dis);
                return matcher.find() ? matcher.group(1) : null;
            })
            .or(Download.super::getFilename);
    }

    @Override
    public InputStream getInputStream() {
        return response.body();
    }

}
