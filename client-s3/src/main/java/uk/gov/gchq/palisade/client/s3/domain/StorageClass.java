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

package uk.gov.gchq.palisade.client.s3.domain;

/**
 * S3 model for the 'StorageClass' XML schema.
 */
public enum StorageClass {

    STANDARD,
    REDUCED_REDUNDANCY,
    GLACIER,
    UNKNOWN;

    /**
     * Convert from Enum to String.
     *
     * @return a {@link String} representation of the {@link StorageClass}
     */
    public String value() {
        return name();
    }

    /**
     * Convert from String to Enum.
     *
     * @return a {@link StorageClass}
     */
    public static StorageClass fromValue(final String v) {
        return valueOf(v);
    }

}
