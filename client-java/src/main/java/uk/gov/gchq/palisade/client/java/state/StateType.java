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
package uk.gov.gchq.palisade.client.java.state;

/**
 * The state type
 *
 * @author dbell
 * @since 0.5.0
 */
public enum StateType {

    /**
     * A subscribing messae has been sent to the server and no response has been
     * reived yet. The job should not be in this state for long.
     */
    SUBSCRIBING,

    /**
     * The job has been successfully subscribed.
     */
    SUBSCRIBED,

    /**
     * This is the default state
     */
    WAITING,

    /**
     * The job is complete and no more resources are available
     */
    COMPLETE,

    /**
     * The job has just signifuied that it is ready to receive more reources
     */
    CTS,

    /**
     * The job is receiving a reource
     */
    RESOURCE;

}