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

import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;

import uk.gov.gchq.palisade.client.java.QueryResponse;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultSession;
import uk.gov.gchq.palisade.client.java.internal.impl.Configuration;
import uk.gov.gchq.palisade.client.java.internal.model.MessageType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @since 0.5.0
 */

class ManualTest {
    static class TestConfiguration extends Configuration {
        // Expose protected constructor
        TestConfiguration(final Map<String, Object> properties) {
            super(properties);
        }
    }

    @Test
    @Disabled
    void testWithDownloadOutsideStream() throws Exception {
        // Manual configuration for a local 'helm install' with 'kubectl expose deployment xxx's and 'kubectl expose pod xxx's
        // Minikube users need to also use 'minikube service xxx'
        var session = new DefaultSession(new TestConfiguration(Map.of(
            Configuration.USER_ID, "Alice",
            Configuration.PALISADE_URI, URI.create("http://192.168.49.2:32586/api/registerDataRequest"),
            Configuration.FILTERED_RESOURCE_URI, URI.create("ws://192.168.49.2:30598/resource/%25t"),
            Configuration.DATA_SERVICE_MAP, Map.of("data-service", URI.create("http://192.168.49.2:32405"))
        )));

        var query = session.createQuery("file:/data/local-data-store/", Map.of("purpose", "SALARY"));
        var publisher = query
            .execute()
            .thenApply(QueryResponse::stream)
            .get();

        var resources = Flowable.fromPublisher(FlowAdapters.toPublisher(publisher))
            .filter(m -> m.getType().equals(MessageType.RESOURCE))
            .collect(Collectors.toList())
            .blockingGet();

        assertThat(resources).as("check resource count").hasSizeGreaterThan(0);

        assertThat(List.of(resources.get(0), resources.get(1)))
            .extracting(item -> item.asResource().getId())
            .as("check leaf resource id")
            .containsExactly("file:/data/local-data-store/employee_file0.avro", "file:/data/local-data-store/employee_file1.avro");

        var download = session.fetch(resources.get(0));
        assertThat(download).as("check download exists").isNotNull();

        try (var inputStream = download.getInputStream()) {
            System.out.println(new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n")));
        }
    }
}
