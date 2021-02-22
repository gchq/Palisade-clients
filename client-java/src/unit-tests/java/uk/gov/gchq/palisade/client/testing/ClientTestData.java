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

import java.util.List;

@SuppressWarnings("javadoc")
public abstract class ClientTestData {

    public static final String FILE_PREFIX = "test-data-";
    public static final String FILE_SUFFIX = ".txt";
    public static final String BASE_PATH = "resources/";

    public static final String FILE_NAME_0 = FILE_PREFIX + "0" + FILE_SUFFIX;
    public static final String FILE_NAME_1 = FILE_PREFIX + "1" + FILE_SUFFIX;
    public static final String FILE_NAME_2 = FILE_PREFIX + "2" + FILE_SUFFIX;
    public static final String FILE_NAME_3 = FILE_PREFIX + "3" + FILE_SUFFIX;
    public static final String FILE_NAME_4 = FILE_PREFIX + "4" + FILE_SUFFIX;
    public static final String FILE_NAME_5 = FILE_PREFIX + "5" + FILE_SUFFIX;
    public static final String FILE_NAME_6 = FILE_PREFIX + "6" + FILE_SUFFIX;
    public static final String FILE_NAME_7 = FILE_PREFIX + "7" + FILE_SUFFIX;
    public static final String FILE_NAME_8 = FILE_PREFIX + "8" + FILE_SUFFIX;
    public static final String FILE_NAME_9 = FILE_PREFIX + "9" + FILE_SUFFIX;

    public static final String FILE_PATH_0 = BASE_PATH + FILE_NAME_0;
    public static final String FILE_PATH_1 = BASE_PATH + FILE_NAME_1;
    public static final String FILE_PATH_2 = BASE_PATH + FILE_NAME_2;
    public static final String FILE_PATH_3 = BASE_PATH + FILE_NAME_3;
    public static final String FILE_PATH_4 = BASE_PATH + FILE_NAME_4;
    public static final String FILE_PATH_5 = BASE_PATH + FILE_NAME_5;
    public static final String FILE_PATH_6 = BASE_PATH + FILE_NAME_6;
    public static final String FILE_PATH_7 = BASE_PATH + FILE_NAME_7;
    public static final String FILE_PATH_8 = BASE_PATH + FILE_NAME_8;
    public static final String FILE_PATH_9 = BASE_PATH + FILE_NAME_9;

    public static final List<String> FILE_NAMES = List.of(
        FILE_PATH_0,
        FILE_PATH_1,
        FILE_PATH_2,
        FILE_PATH_3,
        FILE_PATH_4,
        FILE_PATH_5,
        FILE_PATH_6,
        FILE_PATH_7,
        FILE_PATH_8,
        FILE_PATH_9);

    public static final String TOKEN = "abcd-1";

    private ClientTestData() {
    }

}
