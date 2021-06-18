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

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.gchq.palisade.client.akka.AkkaClient;
import uk.gov.gchq.palisade.client.akka.AkkaClient.SSLMode;
import uk.gov.gchq.palisade.client.s3.repository.ContentLengthRepository;
import uk.gov.gchq.palisade.client.s3.repository.PersistenceLayer;
import uk.gov.gchq.palisade.client.s3.repository.ResourceRepository;
import uk.gov.gchq.palisade.client.s3.web.AkkaHttpServer;
import uk.gov.gchq.palisade.client.s3.web.RouteSupplier;
import uk.gov.gchq.palisade.client.s3.web.S3ServerApi;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Configuration
public class EndpointConfiguration {

    /**
     * The HTTP server will serve forever on the supplied {@code server.host} and {@code server.port}
     * config values.
     *
     * @param properties     spring internal {@code server.xxx} config file object
     * @param routeSuppliers collection of routes to bind for this server (see below)
     * @return the http server
     */
    @Bean
    AkkaHttpServer akkaHttpServer(final ServerProperties properties, final Collection<RouteSupplier> routeSuppliers) {
        String hostname = Optional.ofNullable(properties.getAddress())
                .map(InetAddress::getHostAddress)
                .orElse("0.0.0.0");
        return new AkkaHttpServer(hostname, properties.getPort(), routeSuppliers);
    }

    @Bean
    PersistenceLayer persistenceLayer(final ResourceRepository resourceRepository, final ContentLengthRepository contentLengthRepository) {
        return new PersistenceLayer(resourceRepository, contentLengthRepository);
    }

    @Bean
    AkkaClient akkaClient(final ActorSystem actorSystem) {
        return new AkkaClient("192.168.49.2:31097/palisade", "192.168.49.2:31097/filteredResource",
                Map.of("data-service", "192.168.49.2:31097/data"), actorSystem, SSLMode.NONE);
    }

    @Bean
    RouteSupplier s3ServerApi(final AkkaClient akkaClient, final Materializer materializer, final PersistenceLayer persistenceLayer) {
        return new S3ServerApi(akkaClient, materializer, persistenceLayer);
    }

    @Bean
    ActorSystem actorSystem() {
        return ActorSystem.create("SpringAkkaActorSystem");
    }

    @Bean
    Materializer materialiser(final ActorSystem actorSystem) {
        return Materializer.createMaterializer(actorSystem);
    }

    @Bean
    @ConfigurationProperties("server")
    ServerProperties serverProperties() {
        return new ServerProperties();
    }
}
