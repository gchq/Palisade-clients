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
package uk.gov.gchq.palisade.client.java.download;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxStreamingHttpClient;
import org.immutables.value.Value;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.ClientException;
import uk.gov.gchq.palisade.client.java.data.IDataRequest;
import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.io.*;
import java.net.*;
import java.util.function.UnaryOperator;

import com.google.common.eventbus.EventBus;

public class Downloader implements Runnable {

    @Value.Immutable
    @ImmutableStyle
    public interface IDownloadConfig {
        public static DownloadConfig create(UnaryOperator<DownloadConfig.Builder> func) {
            return func.apply(DownloadConfig.builder()).build();
        }
        EventBus getEventBus();
        Resource getResource();
    }

    static Downloader create(UnaryOperator<DownloadConfig.Builder> func) {
        return new Downloader(func.apply(DownloadConfig.builder()).build());
    }

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);
    private final DownloadConfig config;

    private Downloader(DownloadConfig config) {
        this.config = config;
    }

    @Override
    public void run() {

        log.debug("Downloader Started");

        var resource = config.getResource();
        var token = resource.getToken();

        URL url = null;
        try {
            url = new URL("http://localhost:8081/name");
        } catch (MalformedURLException e1) {
            throw new RuntimeException(e1.getMessage(), e1);
        }

        String str = null;

        try (var client = RxStreamingHttpClient.create(url)) {

            var dataRequest = IDataRequest.create(b -> b
                    .token(token)
                    .leafResourceId(resource.getLeafResourceId()));

            var request = HttpRequest
                    .POST("/read/chunked", dataRequest)
                    .header("Content-type", "application/json; charset=utf-8");

            var flow = client.retrieve(request);
            str = flow.blockingFirst();

            log.debug("### GOT: {}", str);

        }

        try (var is = new ByteArrayInputStream(str.getBytes())) {

            log.debug("Got bytestream");

            // here we should publish a downkload started event. This event will have all
            // the information needed for a deserialiser.

            try {
                config.getEventBus().post(DownloadReadyEvent.of(token, resource, is));
            } catch (Exception e) {
                // TODO: add some error handling here
                // wrap the this as the code receiving the evtn will consume the input stream
            }

        } catch (IOException ioe) {
            throw new ClientException("failed to download");
        }

        log.debug("Downloader ended");

    }


}