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

package uk.gov.gchq.palisade.clients.simpleclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.gov.gchq.palisade.clients.simpleclient.web.NamedDataClient;
import uk.gov.gchq.palisade.clients.simpleclient.web.UrlDataClient;

/**
 * The type Application configuration.
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * Client configuration bean
     *
     * @return a new clientConfiguration
     */
    @Bean
    @ConfigurationProperties(prefix = "web")
    public ClientConfiguration clientConfiguration() {
        return new ClientConfiguration();
    }

    @Bean
    @Profile("!eureka")
    public UrlDataClient urlDataClient(final ApplicationContext applicationContext, final ClientConfiguration clientConfiguration) {
        return new UrlDataClient(applicationContext, clientConfiguration);
    }

    @Bean
    @Profile("eureka")
    public NamedDataClient namedDataClient(final ApplicationContext applicationContext) {
        return new NamedDataClient(applicationContext);
    }

}
