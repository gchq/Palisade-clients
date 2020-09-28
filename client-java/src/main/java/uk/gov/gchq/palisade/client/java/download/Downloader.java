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
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import uk.gov.gchq.palisade.client.java.data.*;
import uk.gov.gchq.palisade.client.java.job.Deserializer;
import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.util.function.UnaryOperator;

/**
 * A downloader is responsible for downloading from
 *
 * @author dbell
 *
 */
public class Downloader implements Runnable {

    @Value.Immutable
    @ImmutableStyle
    public interface IDownloadConfig {
        public static DownloadConfig create(UnaryOperator<DownloadConfig.Builder> func) {
            return func.apply(DownloadConfig.builder()).build();
        }
        String getToken();
        Resource getResource();

        Deserializer getDeserialiser();
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

        var url = config.getUrl();
        var token = config.getToken();
        var resource = config.getResource();
        var deserializer = config.getDeserialiser();

        try {

            log.debug("Downloading {}...", resource);


            // create a connection to data service
            DataClient dataSerice = new Retrofit.Builder()
                    .addConverterFactory(JacksonConverterFactory.create())
                    .baseUrl(url)
                    .build()
                    .create(DataClient.class);

            IDataRequest.create(b -> b.originalRequestId(resource)

            );

            ResponseBody body = dataSerice.readChunked(request);

            var byteStream = body.byteStream();

            var object = deserializer.deserialize(byteStream);

            // TODO: we need to close the stream
            Thread.sleep(2000);

            log.debug("Downloaded {}...", resource);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}