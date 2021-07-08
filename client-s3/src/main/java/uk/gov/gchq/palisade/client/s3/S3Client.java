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

package uk.gov.gchq.palisade.client.s3;

import akka.actor.ActorSystem;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import uk.gov.gchq.palisade.client.s3.web.AkkaHttpServer;

/**
 * Implementation of the client interface that also exposes some akka-specific data-types such as {@link Source}s.
 */
@SpringBootApplication
public class S3Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Client.class);

    private final AkkaHttpServer server;
    private final ActorSystem system;

    /**
     * Autowire Akka objects in constructor for application ready event
     *
     * @param system the default akka actor system
     * @param server the http server to start (in replacement of spring-boot-starter-web)
     */
    public S3Client(
            final AkkaHttpServer server,
            final ActorSystem system) {
        this.server = server;
        this.system = system;
    }

    /**
     * Application entrypoint, creates and runs a spring application, passing in the given command-line args
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(final String[] args) {
        LOGGER.debug("{} started with: {}", S3Client.class.getSimpleName(), args);
        new SpringApplicationBuilder(S3Client.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    /**
     * Runs all available Akka {@link RunnableGraph}s until completion.
     * The 'main' threads of the application during runtime are the completable futures spawned here.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void serveForever() {
        this.server.serveForever(this.system);
    }
}
