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
package uk.gov.gchq.palisade.client.java.job;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.request.PalisadeResponse;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.util.function.UnaryOperator;

import com.google.common.eventbus.EventBus;

/**
 * <p>
 * An instance of {@code JobConfig} is passed during the creation of a new Job.
 * </p>
 * <p>
 * Note that the {@code JobConfig} class is created at compile time. The way in
 * which the class is created is determined by the {@link ImmutableStyle}.
 * </p>
 *
 * @author dbell
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@ImmutableStyle
public interface IJobContext {

    /**
     * Helper method to create a {@code JobConfig} using a builder function which
     * negates the need to use {@code builder()} and {@code build()} methods:
     *
     * <pre>
     * {@code
     * var jobctx =
     *     createJobContext(b -> b
     *         .applicationContext(appctx)
     *         .jobConfig(jobConfig)
     *         .eventBus(eventBus)
     *         .response(response));
     * }
     * </pre>
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    public static JobContext createJobContext(UnaryOperator<JobContext.Builder> func) {
        return func.apply(JobContext.builder()).build();
    }

    /**
     * Returns the client context
     *
     * @return the client context
     */
    ClientContext getClientContext();

    /**
     * Returns the job configuration that the user provided
     *
     * @return the job configuration that the user provided
     */
    JobConfig getJobConfig();

    /**
     * Returns the event bus to be used for registration and posting of events
     *
     * @return the event bus to be used for registration and posting of events
     */
    EventBus getEventBus();

    /**
     * Returns the response that was returned from the Palisade Service
     *
     * @return the response that was returned from the Palisade Service
     */
    PalisadeResponse getResponse();

}
