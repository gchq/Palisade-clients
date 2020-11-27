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
package uk.gov.gchq.palisade.client.job.state;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.receiver.FileReceiver;
import uk.gov.gchq.palisade.client.request.IPalisadeResponse;
import uk.gov.gchq.palisade.client.util.Configuration;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.job.state.IJobRequest.createJobRequest;

class JobStateServiceTest {

    private JobStateService service;

    @BeforeEach
    void setUp() {

        var objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(Include.NON_NULL)
            .setSerializationInclusion(Include.NON_ABSENT)
            .setSerializationInclusion(Include.NON_EMPTY)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        this.service = new JobStateService(objectMapper);

    }

    @Test
    void testCreateNew() {

        var jobConfig = createJobRequest(b -> b
            .userId("user_id")
            .resourceId("resource_id")
            .purpose("purpose")
            .receiverClass(FileReceiver.class)
            .putProperty("key", "value"));

        var state = service.createNew(jobConfig, Configuration.fromDefaults());

        assertThat(state).isNotNull();
        assertThat(state.getCreated()).isNotNull();
        assertThat(state.getDownloads()).isEmpty();
        assertThat(state.getPalisadeResponse()).isEmpty();
        assertThat(state.getExecutions()).hasSize(1);
        assertThat(state.getSequence()).isEqualTo(0);
        assertThat(state.getStatus()).isEqualTo(JobStatus.OPEN);
        assertThat(state.getJobConfig()).isEqualTo(jobConfig);
        assertThat(state.getCurrentExecution()).isNotNull();
        assertThat(state.getExecutions().get(state.getCurrentExecution().getId()))
            .isEqualTo(state.getCurrentExecution());

    }

    @Test
    void testCreateFromNoDownloads() throws Exception {

        var jobConfig = createJobRequest(b -> b
            .userId("user_id")
            .resourceId("resource_id")
            .purpose("purpose")
            .receiverClass(FileReceiver.class)
            .putProperty("key", "value"));

        var palisadeResponse = IPalisadeResponse.createPalisadeResponse(b -> b
            .token("abcd-1")
        );

        var url = Thread.currentThread().getContextClassLoader()
            .getResource("resume/palisade-state_1_no-downloads.json");
        var path = Paths.get(url.toURI());

        var state = service.createFrom(path);

        assertThat(state).isNotNull();
        assertThat(state.getCreated()).isNotNull();
        assertThat(state.getDownloads()).isEmpty();
        assertThat(state.getPalisadeResponse()).hasValue(palisadeResponse);
        assertThat(state.getExecutions()).hasSize(2);
        assertThat(state.getSequence()).isEqualTo(0);
        assertThat(state.getStatus()).isEqualTo(JobStatus.DOWNLOADS_IN_PROGRESS);
        assertThat(state.getJobConfig()).isEqualTo(jobConfig);
        assertThat(state.getCurrentExecution()).isNotNull();
        assertThat(state.getExecutions().get(state.getCurrentExecution().getId()))
            .isEqualTo(state.getCurrentExecution());

    }

    @Test
    void testCreateFromWithDownloads() throws Exception {

        var jobConfig = createJobRequest(b -> b
            .userId("user_id")
            .resourceId("resource_id")
            .purpose("purpose")
            .receiverClass(FileReceiver.class)
            .putProperty("key", "value"));

        var palisadeResponse = IPalisadeResponse.createPalisadeResponse(b -> b.token("abcd-1"));

        var url = Thread.currentThread().getContextClassLoader()
            .getResource("resume/palisade-state_1_with-downloads.json");
        var path = Paths.get(url.toURI());

        var state = service.createFrom(path);

        assertThat(state).isNotNull();
        assertThat(state.getCreated()).isNotNull();
        assertThat(state.getDownloads()).isNotEmpty().hasSize(2);
        assertThat(state.getPalisadeResponse()).hasValue(palisadeResponse);
        assertThat(state.getExecutions()).hasSize(2);
        assertThat(state.getSequence()).isEqualTo(0);
        assertThat(state.getStatus()).isEqualTo(JobStatus.DOWNLOADS_IN_PROGRESS);
        assertThat(state.getJobConfig()).isEqualTo(jobConfig);
        assertThat(state.getCurrentExecution()).isNotNull();
        assertThat(state.getExecutions().get(state.getCurrentExecution().getId()))
            .isEqualTo(state.getCurrentExecution());

    }

}
