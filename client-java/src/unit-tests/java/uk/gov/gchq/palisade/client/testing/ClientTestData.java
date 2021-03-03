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
package uk.gov.gchq.palisade.client.testing;

import org.immutables.value.Value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import static uk.gov.gchq.palisade.client.util.Checks.checkArgument;
import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

@SuppressWarnings("javadoc")
public abstract class ClientTestData {

    /**
     * This class represents a "name" of a resource. Of course it's not really a
     * name, but a scheme on how to generate a resource for testing. The name of the
     * resource follows this format:
     * <pre>{@code <resource_name>_<seed>_<numberOfBytes>}</pre>. To create a name
     * from a string, simply use {@code #from(String)}. Once the object is created
     * an InputStream can then be retrieved which will provide the correct random
     * content
     */
    @Value.Immutable
    @Value.Style(allParameters = true, typeImmutable = "*Tuple", defaults = @Value.Immutable(builder = false))
    public interface Name {

        String getName();

        long getSeed();

        int getBytes();

        default String asString() {
            return getName() + "_" + getSeed() + "_" + getBytes();
        }

        static Name from(final String nameString) {
            checkNotNull(nameString, "name string cannot be null");
            var split = nameString.split("_");
            checkArgument(split.length == 3, "Should conform to <name>_<seed>_<bytes>. e.g. alice-eve_0_1024");
            var name = split[0];
            long seed = -1;
            try {
                seed = Long.valueOf(split[1]);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Expected seed to be a long value, but was: " + split[1]);
            }
            int bytes = -1;
            try {
                bytes = Integer.valueOf(split[2]);
                checkArgument(seed >= 0, "#bytes must be >0");
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Expected bytes to be an int value > 0, but was: " + split[2]);
            }
            return NameTuple.of(name, seed, bytes);
        }

        /**
         * Returns an input stream containing bytes generated from a {@code Random}
         * initialised with the provided seed.
         *
         * @return an input stream containing {@code bytes} generated from a
         *         {@code Random} initialised with the provided {@code seed}
         */
        default InputStream createStream() {
            var bytea = new byte[getBytes()];
            new Random(getSeed()).nextBytes(bytea);
            return new ByteArrayInputStream(bytea);
        }

    }

    public static final String FILE_PREFIX = "test-data_";
    public static final Name FILE_NAME_0 = NameTuple.of("test-data", 0, 1024);
    public static final Name FILE_NAME_1 = NameTuple.of("test-data", 1, 1024);
    public static final List<String> FILE_NAMES = List.of(FILE_NAME_0.asString(), FILE_NAME_1.asString());
    public static final String TOKEN = "abcd-1";

    private ClientTestData() {
    }

}
