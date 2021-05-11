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
package uk.gov.gchq.palisade.contract.java;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.java.ClientManager;
import uk.gov.gchq.palisade.client.java.Download;
import uk.gov.gchq.palisade.client.java.QueryItem.ItemType;
import uk.gov.gchq.palisade.client.java.QueryResponse;

import javax.inject.Inject;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.java.testing.ClientTestData.FILE_NAME_0;

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

        var session = ClientManager.openSession(String.format("pal://localhost:%d/cluster?userid=alice", port));
        var query = session.createQuery("resource_id");
        var publisher = query
            .execute()
            .thenApply(QueryResponse::stream)
            .get();

        var resources = Flowable.fromPublisher(FlowAdapters.toPublisher(publisher))
            .filter(m -> m.getType().equals(ItemType.RESOURCE))
            .collect(Collectors.toList())
            .timeout(10, TimeUnit.SECONDS)
            .blockingGet();

        assertThat(resources).as("check resource count").hasSizeGreaterThan(0);

        var resource = resources.get(0);
        assertThat(resource.asResource().getId())
            .as("check leaf resource id")
            .isEqualTo(FILE_NAME_0.asString());

        var download = session.fetch(resource);
        assertThat(download).as("check download exists").isNotNull();

        try (var actual = download.getInputStream();
             var expected = FILE_NAME_0.createStream();
        ) {
            assertThat(actual).as("check stream download").hasSameContentAs(expected);
        }

    }

    @Test
    void testWithDownloadInsideStream() throws Exception {

        var session = ClientManager.openSession(String.format("pal://localhost:%d/cluster?userid=alice", embeddedServer.getPort()));
        var query = session.createQuery("resource_id");
        var publisher = query
            .execute()
            .thenApply(QueryResponse::stream)
            .get();

        Flowable.fromPublisher(FlowAdapters.toPublisher(publisher))
            .filter(m -> m.getType().equals(ItemType.RESOURCE))
            .map(session::fetch)
            .timeout(10, TimeUnit.SECONDS)
            .subscribe(new FlowableSubscriber<>() {

                @Override
                public void onNext(final Download t) {
                    LOGGER.debug("## Got message: {}", t);
                    try (var is = t.getInputStream()) {
                        LOGGER.debug("## reading bytes");
                        var ba = is.readAllBytes();
                        LOGGER.debug("## read {} bytes", ba.length);
                        LOGGER.debug(new String(ba));
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
