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

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventPublisher;

import uk.gov.gchq.palisade.client.java.*;
import uk.gov.gchq.palisade.client.java.download.*;
import uk.gov.gchq.palisade.client.java.request.*;

import javax.inject.Singleton;

/**
 * This is the factory for any services that are injected via the DI framework.
 * This configuration is used when a new {@code ApplicationContext} is created.
 *
 * @author dbell
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
    public PalisadeClient createPalisadeClient(ClientConfig clientConfig, PalisadeServiceClient prc) {
        return new PalisadeClient() {
            @Override
            public PalisadeResponse submit(PalisadeRequest request) {
                var httpResponse = prc.registerDataRequestSync(request);
                try {
                    var opt = httpResponse.getBody();
                    if (!opt.isPresent()) {
                        var url = clientConfig.getClient().getUrl() + PalisadeServiceClient.REGISTER_DATA_REQUEST;
                        var code = httpResponse.code();
                        throw new ClientException(String.format("Request to %s failed with status %s", url, code));
                    }
                    var response = opt.get();
                    return response;
                } catch (Exception e) {
                    String msg = "Request to palisade failed";
                    throw new ClientException(msg, e);
                }
            }
        };
    }

    /**
     * Returns a {@code ClientContext} which wraps Micronaut's
     * {@code ApplicationContext} and methods which are not needed.
     *
     * @param applicationContext The context to be wrapped
     * @return a {@code ClientContext} which wraps Micronaut's
     *         {@code ApplicationContext} and methods which are not needed.
     */
    @Singleton
    public ClientContext createClientContext(ApplicationContext applicationContext) {
        return new ClientContext() {
            @Override
            public <T> T get(Class<T> type) {
                return applicationContext.getBean(type);
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
    public DownloadTracker createDownloadTracker(DownloadManager downloadManager) {
        return downloadManager.getDownloadTracker();
    }

    /**
     * Returns a {@code Bus} instance which wraps Micronaut's
     * {@code ApplicationEventPublisher}
     *
     * @param applicationEventPublisher the publisher being wrapped
     * @return a {@code Bus} instance which wraps Micronaut's
     *         {@code ApplicationEventPublisher}
     */
    @Singleton
    public Bus createEventBus(ApplicationEventPublisher applicationEventPublisher) {
        return (event) -> applicationEventPublisher.publishEventAsync(event);
    }

}
