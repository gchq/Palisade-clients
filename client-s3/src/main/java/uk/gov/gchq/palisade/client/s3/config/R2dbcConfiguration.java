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

package uk.gov.gchq.palisade.client.s3.config;

import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.lang.NonNull;

import uk.gov.gchq.palisade.client.s3.repository.ResourceConverter;

import java.util.List;

/**
 * Configuration for the reactive R2DBC repositories
 */
@Configuration
@EnableR2dbcRepositories(basePackages = {"uk.gov.gchq.palisade.client.s3.repository"})
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(R2dbcConfiguration.class);
    private final ConnectionFactory defaultConnectionFactory;

    /**
     * Public constructor for the R2DBC configuration
     *
     * @param defaultConnectionFactory the connection factory
     */
    public R2dbcConfiguration(final ConnectionFactory defaultConnectionFactory) {
        this.defaultConnectionFactory = defaultConnectionFactory;
        LOGGER.debug("Initialised R2DBC repositories");
    }

    @Override
    @NonNull
    public ConnectionFactory connectionFactory() {
        return this.defaultConnectionFactory;
    }

    @Override
    @Bean
    @NonNull
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Converter<?, ?>> converterList = List.of(
                new ResourceConverter.Reading(), new ResourceConverter.Writing()
        );
        return new R2dbcCustomConversions(getStoreConversions(), converterList);
    }

    /**
     * Prepopulate the database with initial SQL commands (create the tables used by the entities and repositories)
     *
     * @return an initializer for the factory (executes some initial commands on the database once created)
     */
    @Bean
    ConnectionFactoryInitializer initializer() {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(defaultConnectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        return initializer;
    }

}
