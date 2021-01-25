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
package uk.gov.gchq.palisade.client.job.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.util.Configuration;
import uk.gov.gchq.palisade.client.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The job state service provides loading and saving services for job states
 *
 * @since 0.5.0
 */
public class JobStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStateService.class);

    private final ObjectMapper objectMapper;

    /**
     * Returns a newly created instance
     *
     * @param objectMapper The object mapper to use when (de)serialising the state.
     */
    public JobStateService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a newly created {@code JobState} with the provided job request and
     * configuration properties
     *
     * @param jobConfig     The job configuration
     * @param configuration The client configuration
     * @return a newly created {@code JobState}
     */
    public JobState createNew(final IJobRequest jobConfig, final Configuration configuration) {
        return new JobState(this, jobConfig, configuration);
    }


    /**
     * Returns newly created {@code JobState} from the provided path
     *
     * @param fromPath The path to load state from
     * @return newly created {@code JobState}
     */
    public JobState createFrom(final Path fromPath) {
        return createFrom(fromPath, Map.of());
    }

    /**
     * Returns newly created {@code JobState} from the provided path overridden with
     * the provided configuration properties.
     *
     * @param fromPath      The path to load state from
     * @param configuration The override configuration
     * @return newly created {@code JobState}
     */
    public JobState createFrom(final Path fromPath, final Map<String, Object> configuration) {
        try {
            var json = Files.readString(fromPath);
            var state = fromJson(json);
            return new JobState(this, state, configuration);
        } catch (IOException e) {
            throw new ClientException("Failed to load state", e);
        }
    }

    final void save(final ISavedJobState state, final String path) {

        LOGGER.debug("Saving state to: {}", path);

        var replacementMap = Map.<String, Supplier<String>>of(
            "%t", () -> state.getPalisadeResponse().getToken(),
            "%s", () -> Util.timeStampFormat(Instant.now()));

        var s = Util.replaceTokens(path, replacementMap);
        var p = Path.of(s);

        try {
            var json = toJson(state);
            Files.createDirectories(p.getParent());
            Files.writeString(p, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ClientException("Failed to save state", e);
        }
    }


    private String toJson(final ISavedJobState state) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new ClientException("Failed to serialise state", e);
        }

    }

    private ISavedJobState fromJson(final String json) {
        try {
            return objectMapper.readValue(json, SavedJobState.class);
        } catch (JsonProcessingException e) {
            throw new ClientException("Failed to deserialise state", e);
        }
    }

}
