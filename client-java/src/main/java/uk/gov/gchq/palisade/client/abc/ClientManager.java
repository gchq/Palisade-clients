package uk.gov.gchq.palisade.client.abc;

import java.util.Map;

/**
 * Note that a urls would be:
 * <ul>
 * <li>pal://host:port - standard client (which using http for PS and FRS</li>
 * <li>pal:alt://host:port - alternative client</li>
 * </ul>
 *
 * @author dbell
 */
public class ClientManager {

    public ClientManager() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Returns a client for the given palisade url. The ClientManager attempts to
     * select an appropriate client from the set of registered Clients.
     *
     * @param url a palisade url of the form pal:subname://host:port/context
     * @return a client for the provided URL
     */
    public static Client getClient(final String url) {
        return null;
    }

    /**
     * Returns a client for the given palisade url. The ClientManager attempts to
     * select an appropriate client from the set of registered Clients.
     * <p>
     * Note: If a property is specified as part of the url and is also specified in
     * the Properties object, it is implementation-defined as to which value will
     * take precedence. For maximum portability, an application should only specify
     * a property once.
     *
     * @param url  a palisade url of the form pal:subname://host:port/context
     * @param info a list of arbitrary string tag/value pairs as connection
     *             arguments; normally at least a "user" and "password" property
     *             should be included
     * @return a client for the provided URL
     */
    public static Client getClient(final String url, final Map<String, String> info) {
        return null;
    }

}
