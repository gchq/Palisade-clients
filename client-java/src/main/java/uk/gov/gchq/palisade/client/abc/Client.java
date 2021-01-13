package uk.gov.gchq.palisade.client.abc;

import uk.gov.gchq.palisade.client.ClientException;

import java.util.Map;

public interface Client {

    /**
     * Retrieves whether the driver thinks that it can open a session to the given
     * URL. Typically drivers will return <code>true</code> if they understand the
     * sub-protocol specified in the URL and <code>false</code> if they do not.
     *
     * @param url the URL of the database
     * @return <code>true</code> if this driver understands the given URL;
     *         <code>false</code> otherwise
     * @exception ClientException if a database access error occurs or the url is
     *                            {@code null}
     */
    boolean acceptsURL(String url);

    /**
     * Attempts to make a palisade connection to the given URL. The client should
     * return "null" if it realizes it is the wrong kind of driver to connect to the
     * given URL. This will be common, as when the JDBC driver manager is asked to
     * connect to a given URL it passes the URL to each loaded driver in turn.
     * <P>
     * The driver should throw an <code>ClientException</code> if it is the right
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
     * @param url  the URL of the database to which to connect
     * @param info a list of arbitrary string tag/value pairs as connection
     *             arguments. Normally at least a "user" property should be
     *             included.
     * @return a <code>Session</code> object that represents a connection to the URL
     * @exception ClientException if a cluster error occurs or the url is
     *                            {@code null}
     */
    Session connect(String url, Map<String, String> info);

}
