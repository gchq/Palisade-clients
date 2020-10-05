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

import io.micronaut.context.ApplicationContext;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.request.PalisadeResponse;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.util.function.UnaryOperator;

import com.google.common.eventbus.EventBus;

@Value.Immutable
@ImmutableStyle
public interface IJobContext<E> {

    public static <E> JobContext<E> createJobContext(UnaryOperator<JobContext.Builder<E>> func) {
        return func.apply(JobContext.<E>builder()).build();
    }

    ApplicationContext getApplicationContext();

    JobConfig<E> getJobConfig();

    EventBus getEventBus();

    PalisadeResponse getResponse();

}
