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
package uk.gov.gchq.palisade.clients.simpleclient.web;

import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;

import uk.gov.gchq.palisade.clients.simpleclient.config.ClientConfiguration;

import java.util.Map;

/**
 * The type Url data client for when the profile is not Eureka.
 */
public class UrlDataClient implements DataClientFactory {

    private final FeignClientBuilder feignClientBuilder;
    private final ClientConfiguration clientConfiguration;

    /**
     * Instantiates a new Url data client with a Feign Builder of url serviceId i.e localhost:8085.
     *
     * @param appContext          the app context
     * @param clientConfiguration the clientConfiguration
     */
    public UrlDataClient(final ApplicationContext appContext, final ClientConfiguration clientConfiguration) {
        this.feignClientBuilder = new FeignClientBuilder(appContext);
        this.clientConfiguration = clientConfiguration;
    }

    public DataClient build(final String serviceId) {
        Map<String, String> dataServices = clientConfiguration.getClient();
        return feignClientBuilder
                .forType(DataClient.class, serviceId)
                .url(dataServices.getOrDefault(serviceId, "http://" + serviceId))
                .build();
    }
}
