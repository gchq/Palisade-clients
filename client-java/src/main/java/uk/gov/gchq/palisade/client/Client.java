/*
 * Copyright 2020-2021 Crown Copyright
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
package uk.gov.gchq.palisade.client;

import java.util.Map;

/**
 * The interface that a client class must implement
 *
 * @since 0.5.0
 */
public interface Client {

    /**
     * Retrieves whether the client thinks that it can open a session to the given
     * URL. Typically clients will return <code>true</code> if they understand the
     * sub-protocol specified in the URL and <code>false</code> if they do not.
     *
     * @param url the URL of the server cluster
     * @return <code>true</code> if this client understands the given URL;
     *         <code>false</code> otherwise
     * @exception ClientException if a client error occurs or the url is
     *                            {@code null}
     */
    boolean acceptsURL(String url);

    /**
     * Attempts to make a palisade connection to the given URL. The client should
     * return "null" if it realizes it is the wrong kind of client to connect to the
     * given URL. This will be common, as when the Palisade client manager is asked
     * to connect to a given URL it passes the URL to each loaded client in turn.
     * <P>
     * The client should throw an <code>ClientException</code> if it is the right
     * client to connect to the given URL but has trouble connecting to the cluster.
     * <P>
     * The {@code Properties} argument can be used to pass arbitrary string
     * tag/value pairs as connection arguments. Normally at least "user" property
     * should be included in the {@code Map} object.
     * <p>
     * <B>Note:</B> If a property is specified as part of the {@code url} and is
     * also specified in the {@code Properties} object, it is implementation-defined
     * as to which value will take precedence. For maximum portability, an
     * application should only specify a property once.
     *
     * @param url  the URL of the palisade cluster to which to connect
     * @param info a list of arbitrary string tag/value pairs as session arguments.
     *             Normally at least a "user" property should be included.
     * @return a <code>Session</code> object that represents a connection to the URL
     * @exception ClientException if a cluster error occurs or the url is
     *                            {@code null}
     */
    Session connect(String url, Map<String, String> info);

}
