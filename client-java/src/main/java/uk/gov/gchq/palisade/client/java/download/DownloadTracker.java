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
package uk.gov.gchq.palisade.client.java.download;

/**
 * An instance of this class is provided by a DownloadManager in order to track
 * its status. This is especially important when replying to an RTS request from
 * the server. If there are no more slots available, then the client will wait
 * until there is before send a CTS.
 *
 * @since 0.5.0
 */
public interface DownloadTracker {

    /**
     * The current status of the download manager
     *
     * @since 0.5.0
     */
    enum ManagerStatus {
        /**
         * Currently downloading and/or has slots available
         */
        ACTIVE,

        /**
         * Download manager is in the process of shutting down
         */
        SHUTTING_DOWN,

        /**
         * Download manager has shuit down and all resources released (threads).
         */
        SHUT_DOWN
    }

    /**
     * Returns the number of slots available for downloads to initiated
     *
     * @return the number of slots available for downloads to initiated
     */
    int getAvaliableSlots();

    /**
     * Returns true if there are download slots available
     *
     * @return true if there are download slots available
     */
    boolean hasAvailableSlots();

    /**
     * Returns the current status of this download manager
     *
     * @return the current status of this download manager
     */
    ManagerStatus getStatus();

}
