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
package uk.gov.gchq.palisade.client;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @since 0.5.0
 */
@MicronautTest
class FullTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FullTest.class);

    @Inject
    EmbeddedServer embeddedServer;

    @Test
    void testWithDownloadOutsideStream() throws Exception {

        var port = embeddedServer.getPort();

        var properties = Map.of(
            "service.palisade.port", "" + port,
            "service.filteredResource.port", "" + port);

        var session = ClientManager.openSession("pal://alice@localhost/cluster", properties);
        var query = session.createQuery("resource_id");
        var publisher = query
            .execute()
            .thenApply(QueryResponse::stream)
            .get();

        var resources = Flowable.fromPublisher(FlowAdapters.toPublisher(publisher))
            .filter(m -> m.getType() == MessageType.RESOURCE)
            .map(Resource.class::cast)
            .collect(Collectors.toList())
            .blockingGet();

        assertThat(resources).hasSizeGreaterThan(0);

        var resource = resources.get(0);
        assertThat(resource.getLeafResourceId()).isEqualTo("resources/pi0.txt");

        var download = session.fetch(resource);
        assertThat(download).isNotNull();

        try (var actual = download.getInputStream();
             var expected = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/pi0.txt");
        ) {
            assertThat(actual).hasSameContentAs(expected);
        }

    }

    /*
     * This tests that the download inputstream can be consumed within the same
     * Flowable. But, there is a problem with Java 11:
     * https://bugs.openjdk.java.net/browse/JDK-8228970 You can view the test here :
     * https://hg.openjdk.java.net/jdk/jdk/rev/e4cc5231ce2d
     */
    @Test
    @Disabled
    @SuppressWarnings("java:S1607")
    void testWithDownloadInsideStream() throws Exception {

        var properties = Map.of(
            "service.palisade.port", "" + embeddedServer.getPort(),
            "service.filteredResource.port", "" + embeddedServer.getPort());

        var session = ClientManager.openSession("pal://alice@localhost/cluster", properties);
        var query = session.createQuery("resource_id");
        var publisher = query
            .execute()
            .thenApply(QueryResponse::stream)
            .get();

        Flowable.fromPublisher(FlowAdapters.toPublisher(publisher))
            .filter(m -> m.getType() == MessageType.RESOURCE)
            .map(Resource.class::cast)
            .map(session::fetch)
            .subscribe(new FlowableSubscriber<>() {

                @Override
                public void onNext(final Download t) {
                    LOGGER.debug("## Got message: {}", t);
                    try (var is = t.getInputStream()) {
                        LOGGER.debug("## reading bytes");
                        var ba = is.readAllBytes();
                        LOGGER.debug("## read {} bytes", ba.length);
                    } catch (Throwable e) {
                        LOGGER.error("Got error reading input stream into byte array", e);
                        throw new IllegalStateException("Got error reading input stream into byte array", e);
                    }
                }

                @Override
                public void onError(final Throwable t) {
                    LOGGER.error("## Error: {}", t.getMessage());
                    Assertions.fail("Failed due to:" + t.getMessage());
                }

                @Override
                public void onComplete() {
                    LOGGER.debug("## complete");

                }

                @Override
                public void onSubscribe(final org.reactivestreams.@NonNull Subscription s) {
                    s.request(Long.MAX_VALUE);
                    LOGGER.debug("## Subscribed");
                }
            });

    }

}
