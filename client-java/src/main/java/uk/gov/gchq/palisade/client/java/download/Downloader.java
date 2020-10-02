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

import okhttp3.ResponseBody;
import org.immutables.value.Value;
import org.slf4j.*;
import retrofit2.Response;

import uk.gov.gchq.palisade.client.java.ClientException;
import uk.gov.gchq.palisade.client.java.data.*;
import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.io.*;
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
        DataClient getDataClient();
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

        var eventBus = config.getEventBus();
        var resource = config.getResource();
        var dataClient = config.getDataClient();

        var token = resource.getToken();

        var dataRequest = IDataRequest.create(b -> b
                .token(token)
                .leafResourceId(resource.getLeafResourceId()));

        Response<ResponseBody> response;
        try {
            response = dataClient.readChunked(dataRequest).execute();
        } catch (IOException e) {
            throw new ClientException("Failed to read body", e);
        }
        var responseBody = response.body();

        try (InputStream is = responseBody.byteStream()) {

            log.debug("Got bytestream");

            // here we should publish a downkload started event. This event will have all
            // the information needed for a deserialiser.

            var event = DownloadReadyEvent.of(token, is);
            try {
                eventBus.post(event);
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