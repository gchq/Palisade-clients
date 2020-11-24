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
package uk.gov.gchq.palisade.client.job.state;

/**
 * The status of a download
 *
 * @since 0.5.0
 */
public enum JobStatus {

    /**
     * Completed successfully
     */
    OPEN(10),

    /**
     * A request has been sent, but not received
     */
    REQUEST_SENT(15),

    /**
     * Download failed
     */
    RESPONSE_RECEIVED(20),

    /**
     * Downloads are in progress
     */
    DOWNLOADS_IN_PROGRESS(30),

    /**
     * The job is complete
     */
    COMPLETE(40);

    private final int sequence;

    JobStatus(final int sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns the sequence of this status
     *
     * @return the sequence of this status
     */
    public int getSequence() {
        return this.sequence;
    }

}

