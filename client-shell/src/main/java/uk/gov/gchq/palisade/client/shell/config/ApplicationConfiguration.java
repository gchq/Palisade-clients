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

package uk.gov.gchq.palisade.client.shell.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.shell.jline.PromptProvider;

import uk.gov.gchq.palisade.client.java.internal.dft.DefaultClient;
import uk.gov.gchq.palisade.client.shell.shell.ClientShell;
import uk.gov.gchq.palisade.client.shell.shell.CustomPromptProvider;
import uk.gov.gchq.palisade.client.shell.shell.ExceptionHandlingAspect;

/**
 * Spring bean dependency injection graph
 */
@Configuration
@EnableAspectJAutoProxy
public class ApplicationConfiguration {
    @Bean
    ExceptionHandlingAspect exceptionHandlingAspect() {
        return new ExceptionHandlingAspect();
    }

    @Bean
    PromptProvider customPromptProvider(final ClientShell shell) {
        return new CustomPromptProvider(shell);
    }

    @Bean
    DefaultClient defaultClient() {
        return new DefaultClient();
    }
}
