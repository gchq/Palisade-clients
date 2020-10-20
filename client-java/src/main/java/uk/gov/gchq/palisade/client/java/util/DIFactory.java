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
package uk.gov.gchq.palisade.client.java.util;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.http.HttpResponse;

import uk.gov.gchq.palisade.client.java.ClientConfig;
import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.ClientException;
import uk.gov.gchq.palisade.client.java.download.DownloadManager;
import uk.gov.gchq.palisade.client.java.download.DownloadTracker;
import uk.gov.gchq.palisade.client.java.request.PalisadeClient;
import uk.gov.gchq.palisade.client.java.request.PalisadeRequest;
import uk.gov.gchq.palisade.client.java.request.PalisadeResponse;
import uk.gov.gchq.palisade.client.java.request.PalisadeServiceClient;

import javax.inject.Singleton;

import java.util.Optional;

/**
 * This is the factory for any services that are injected via the DI framework.
 * This configuration is used when a new {@link io.micronaut.context.ApplicationContext} is created.
 *
 * @since 0.5.0
 */
@Factory
public class DIFactory {

    /**
     * Creates a new factory
     */
    public DIFactory() { // cannot be instantiated
    }

    /**
     * Returns the one and only {@link PalisadeClient} instance.
     *
     * @param clientConfig The client configuration (needed for service url)
     * @param prc          The {@link PalisadeServiceClient} to be wrapped
     * @return the one and only {@link PalisadeClient} instance.
     */
    @Singleton
    public PalisadeClient createPalisadeClient(final ClientConfig clientConfig, final PalisadeServiceClient prc) {
        return (final PalisadeRequest request) -> {
            HttpResponse<PalisadeResponse> httpResponse = prc.registerDataRequestSync(request);
            Optional<PalisadeResponse> opt = httpResponse.getBody();
            if (opt.isEmpty()) {
                String url = clientConfig.getClient().getUrl() + PalisadeServiceClient.REGISTER_DATA_REQUEST;
                int code = httpResponse.code();
                throw new ClientException(String.format("Request to %s failed with status %s", url, code));
            }
            return opt.get();
        };
    }

    /**
     * Returns a {@code ClientContext} which wraps Micronaut's
     * {@code ApplicationContext} and methods which are not needed.
     *
     * @param applicationContext The context to be wrapped
     * @return a {@code ClientContext} which wraps Micronaut's
     * {@code ApplicationContext} and methods which are not needed.
     */
    @Singleton
    public ClientContext createClientContext(final BeanLocator applicationContext) {
        return new ClientContext() {

            @Override
            public <T> T get(final Class<T> type) {
                try {
                    return applicationContext.getBean(type);
                } catch (NoSuchBeanException nsbe) {
                    throw new ClientException("Could not find type in underlying context", nsbe);
                }
            }

            @Override
            public <T> Optional<T> find(final Class<T> type) {
                return applicationContext.findBean(type);
            }
        };
    }

    /**
     * Returns a download tracker used to provide availability of download slots
     *
     * @param downloadManager The download manager being tracked
     * @return a download tracker used to provide availability of download slots
     */
    @Singleton
    public DownloadTracker createDownloadTracker(final DownloadManager downloadManager) {
        return downloadManager.getDownloadTracker();
    }

    /**
     * Returns a {@code Bus} instance which wraps Micronaut's
     * {@code ApplicationEventPublisher}
     *
     * @param applicationEventPublisher the publisher being wrapped
     * @return a {@code Bus} instance which wraps Micronaut's
     * {@code ApplicationEventPublisher}
     */
    @Singleton
    public Bus createEventBus(final ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEventAsync;
    }

}
