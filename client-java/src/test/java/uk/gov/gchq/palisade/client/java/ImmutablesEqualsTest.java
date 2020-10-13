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
package uk.gov.gchq.palisade.client.java;

import nl.jqno.equalsverifier.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.*;

import uk.gov.gchq.palisade.client.java.download.DataRequest;
import uk.gov.gchq.palisade.client.java.job.JobContext;
import uk.gov.gchq.palisade.client.java.resource.ServerState;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.stream.Stream;

class ImmutablesEqualsTest {

    @ParameterizedTest(name = "Test equals contract")
    @MethodSource("provideClassesForEqualsTest")
    void testEqualsContract(Class<?> cls) {
        EqualsVerifier
            .forClass(cls)
            .suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT)
            .verify();
    }

    private static Stream<Arguments> provideClassesForEqualsTest() {

        // These have to be ignored due to some warnings generated by equalsverifier.
        // It looks like something to do with internal json object generated to
        // support Jackson

        var ignored = List.of(
                ServerState.class,
                JobContext.class,
                DataRequest.class);

        // find all generated classes within the provided package that have
        // been annotated with "javax.annotation.concurrent.Immutable".
        // The immutables processor nicely adds this annotation

        var reflections = new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("uk.gov.gchq.palisade.client.java"))
                .setScanners(
                        new TypeAnnotationsScanner(),
                        new SubTypesScanner())
                .useParallelExecutor());

        var classes = reflections.getTypesAnnotatedWith(Immutable.class);

        return classes
            .stream()
            .filter(c -> !ignored.contains(c)) // exclude if on ignored list
            .map(Arguments::of);

    }

}