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
package uk.gov.gchq.palisade.client.java.util;

import uk.gov.gchq.palisade.client.java.job.JobConfig;
import uk.gov.gchq.palisade.client.java.request.*;
import uk.gov.gchq.palisade.client.java.state.StateManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public abstract class ClientUtil {

    /**
     * This "singleton" relies on the initialization phase of execution within the
     * Java Virtual Machine (JVM) as specified by the Java Language Specification
     * (JLS). This implementation is an efficient thread-safe "singleton" cache
     * without synchronization overhead, and better performing than uncontended
     * synchronization.
     *
     * @author dbell
     *
     */
    public static class Single {

        private final ObjectMapper objectMapper;
        private final StateManager stateManager;

        private Single() {
            this.objectMapper = new ObjectMapper()
                    .registerModule(new GuavaModule())
                    .registerModule(new Jdk8Module());
            this.stateManager = new StateManager();
        }

        private static class LazyHolder {
            static final Single INSTANCE = new Single();
        }

        public static Single getInstance() {
            return LazyHolder.INSTANCE;
        }
    }

    private ClientUtil() { // cannot be instantiated
    }

    public static final ObjectMapper getObjectMapper() {
        return Single.getInstance().objectMapper;
    }

    public static final StateManager getStateManager() {
        return Single.getInstance().stateManager;
    }

    public static <E> PalisadeRequest createPalisadeRequest(JobConfig<E> jobConfig) {

        assert jobConfig != null : "Need to have a job config in order to create a request";

        var userId = jobConfig.getUserId();
        var purpose = jobConfig.getPurpose();
        var className = jobConfig.getClassname();
        var requestId = jobConfig.getRequestId();
        var resourceId = jobConfig.getResourceId();
        var properties = jobConfig.getProperties();

        return PalisadeRequest.builder()
                .resourceId(resourceId)
                .userId(UserId.builder()
                        .id(userId)
                        .build())
                .requestId(RequestId.builder()
                        .id(requestId)
                        .build())
                .context(Context.builder()
                        .className(className)
                        .purpose(purpose)
                        .contents(properties)
                        .build())
                .build();

    }

}
