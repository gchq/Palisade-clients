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
package uk.gov.gchq.palisade.client.java;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * <p>
 * A configuration to be used to load the initial property map to be loaded
 * into. A map of properties is passed to the {@link Client} during creation.
 * Micronaut will take this map and overlay it onto this instance. The
 * properties will override any propertis set via java parameters, environment
 * variables and those set in a yaml file.
 * </p>
 * <p>
 * The current recognised parameters are:
 * </p>
 * <ul>
 * <li><b>palisade.client.url</b> - The initial url which will be used to
 * connect to the Palisade service in order to submit the request</li>
 * <li><b>palisade.client.download.threads</b> - The number of download threads
 * </ul>
 *
 * @since 0.5.0
 */
@ConfigurationProperties("palisade")
public class ClientConfig {


    private Client client = new Client();
    private Download download = new Download();

    /**
     * Returns the client configuration section
     *
     * @return the client configuration section
     */
    public Client getClient() {
        return this.client;
    }

    /**
     * Sets the client configuration section
     *
     * @param client the client configuration section to set
     */
    public void setClient(final Client client) {
        this.client = client;
    }

    /**
     * Returns the download section
     *
     * @return the download section
     */
    public Download getDownload() {
        return this.download;
    }

    /**
     * Sets the download section
     *
     * @param download the download section to set
     */
    public void setDownload(final Download download) {
        this.download = download;
    }

    /**
     * Properties effecting downloads
     *
     * @since 0.5.0
     */
    @ConfigurationProperties("download")
    public static class Download {

        /**
         * Threads property
         */
        public static final String THREADS_PROPERTY = "palisade.download.threads";

        private int threads = 1;
        private String path = "/tmp";

        /**
         * Returns the number of downloads that will be used when downloading from the
         * data service.
         *
         * @return the URL that will be used to contact the Palisade service.
         * @see #THREADS_PROPERTY
         */
        public int getThreads() {
            return this.threads;
        }

        /**
         * Sets the number of downloads that will be used when downloading from the data
         * service.
         *
         * @param threads The number of download threads
         * @see #THREADS_PROPERTY
         */
        public void setThreads(final int threads) {
            this.threads = threads;
        }

        /**
         * Returns the path where downloads should be created
         *
         * @return the path where downloads should be created
         */
        public String getPath() {
            return this.path;
        }

        /**
         * Sets the path where downloads should be created
         *
         * @param path The path where downloads should be created
         */
        public void setPath(final String path) {
            this.path = path;
        }
    }

    /**
     * Properties effecting access to the Palisade service
     *
     * @since 0.5.0
     */
    @ConfigurationProperties("client")
    public static class Client {

        /**
         * The base url for the palisade service
         */
        public static final String URL_PROPERTY = "palisade.client.url";

        private String url = "http://localhost:8081";

        /**
         * Returns the URL that will be used to contact the Palisade service.
         *
         * @return the URL that will be used to contact the Palisade service.
         * @see #URL_PROPERTY
         */
        public String getUrl() {
            return this.url;
        }

        /**
         * Sets the URL that will be used to contact the Palisade service.
         *
         * @param url The base url
         * @see #URL_PROPERTY
         */
        public void setUrl(final String url) {
            this.url = url;
        }

    }
}