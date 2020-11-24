package uk.gov.gchq.palisade.client.job.state;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.receiver.FileReceiver;
import uk.gov.gchq.palisade.client.request.IPalisadeResponse;
import uk.gov.gchq.palisade.client.util.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.gchq.palisade.client.job.state.IJobRequest.createJobRequest;

class JobStateServiceTest {

    private JobStateService service;

    @BeforeEach
    void setUp() throws Exception {

        var objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(Include.NON_NULL)
            .setSerializationInclusion(Include.NON_ABSENT)
            .setSerializationInclusion(Include.NON_EMPTY)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        this.service = new JobStateService(
            objectMapper,
            Path.of("/tmp/palisade/testing/pal-state-" + UUID.randomUUID() + ".json"));

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
    void testCreateFrom() throws Exception {

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
        assertThat(state.getPalisadeResponse().get()).isEqualTo(palisadeResponse);
        assertThat(state.getExecutions()).hasSize(2);
        assertThat(state.getSequence()).isEqualTo(0);
        assertThat(state.getStatus()).isEqualTo(JobStatus.DOWNLOADS_IN_PROGRESS);
        assertThat(state.getJobConfig()).isEqualTo(jobConfig);
        assertThat(state.getCurrentExecution()).isNotNull();
        assertThat(state.getExecutions().get(state.getCurrentExecution().getId()))
            .isEqualTo(state.getCurrentExecution());

    }

    @Test
    @Disabled
    void testSave() {
        fail("Not yet implemented");
    }

}
