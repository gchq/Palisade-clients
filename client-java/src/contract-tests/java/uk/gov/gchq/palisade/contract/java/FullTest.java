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
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.internal.schedulers.IoScheduler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.java.ClientManager;
import uk.gov.gchq.palisade.client.java.QueryItem.ItemType;
import uk.gov.gchq.palisade.client.java.QueryResponse;

import javax.inject.Inject;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.java.testing.ClientTestData.FILE_NAME_0;
import static uk.gov.gchq.palisade.client.java.testing.ClientTestData.FILE_NAME_1;

/**
 * @since 0.5.0
 */
@MicronautTest
class FullTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FullTest.class);

    @Inject
    EmbeddedServer embeddedServer;

    /**
     * Register a request with the Palisade Service, fetch resources from the Filtered-Resource Service, and download from the Data Service.
     * This test runs in a flatter, non-streaming, non-async manner, where the download is performed on the main thread.
     *
     * @throws Exception if no resources are returned, or the download fails
     */
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

        var expectedCollection = Map.of(
                FILE_NAME_0.asString(), FILE_NAME_0.createStream(),
                FILE_NAME_1.asString(), FILE_NAME_1.createStream()
        );

        for (var resource : resources) {
            assertThat(resource.asResource().getId())
                    .as("check leaf resource id")
                    .isIn(expectedCollection.keySet());

            var download = session.fetch(resource);
            assertThat(download).as("check download exists").isNotNull();

            try (var actual = download.getInputStream();
                 var expected = expectedCollection.get(resource.asResource().getId());
            ) {
                assertThat(actual).as("check stream download").hasSameContentAs(expected);
            }
        }

    }

    /**
     * Register a request with the Palisade Service, fetch resources from the Filtered-Resource Service, and download from the Data Service.
     * This test runs in a nested, streaming, async manner, where the download is performed asynchronously on some reactor thread.
     * <p>
     * <b>There appears to be some bug between Java's Http client and RxJava's {@link Subscriber#onComplete()} where the test blocks forever</b>.
     * Until this can be resolved, the test is marked as {@link Disabled}
     *
     * @throws Exception if no resources are returned, or the download fails
     */
    @Test
    void testWithDownloadInsideStream() throws Exception {

        var session = ClientManager.openSession(String.format("pal://localhost:%d/cluster?userid=alice", embeddedServer.getPort()));
        var query = session.createQuery("resource_id");
        var publisher = query
                .execute()
                .thenApply(QueryResponse::stream)
                .get();

        var expectedCollection = Map.of(
                FILE_NAME_0.asString(), FILE_NAME_0.createStream(),
                FILE_NAME_1.asString(), FILE_NAME_1.createStream()
        );

        var disposable = Flowable.fromPublisher(FlowAdapters.toPublisher(publisher))
                .filter(m -> m.getType().equals(ItemType.RESOURCE))
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(new IoScheduler())
                .subscribe((var resource) -> {
                    assertThat(resource.asResource().getId())
                            .as("check leaf resource id")
                            .isIn(expectedCollection.keySet());

                    var download = session.fetch(resource);
                    assertThat(download).as("check download exists").isNotNull();

                    try (var actual = download.getInputStream();
                         var expected = expectedCollection.get(resource.asResource().getId());
                    ) {
                        assertThat(actual).as("check stream download").hasSameContentAs(expected);
                    }
                });

        disposable.dispose();
    }
}
