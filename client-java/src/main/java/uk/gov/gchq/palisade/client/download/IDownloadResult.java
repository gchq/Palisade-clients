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
package uk.gov.gchq.palisade.client.download;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * An instance of {@link DownloadResult} is returned from a downloader after the
 * successful completion of a download
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
public interface IDownloadResult {

    /**
     * Helper method to create a {@code DownloadResult} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static DownloadResult createDownloadResult(final UnaryOperator<DownloadResult.Builder> func) {
        return func.apply(DownloadResult.builder()).build();
    }

    /**
     * Returns the unique id of the download
     *
     * @return the unique id of the download
     */
    UUID getId();

    // TODO: maybe add some stuff in here

}