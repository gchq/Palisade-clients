package uk.gov.gchq.palisade.client.abc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.abc.impl.DefaultClient;

import java.util.ArrayList;
import java.util.List;
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
public abstract class ClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientManager.class);
    private static final List<Client> clients = new ArrayList<>();

    static {
        clients.add(new DefaultClient());
    }

    private ClientManager() {
        // prevent instantiation
    }

    /**
     * Returns a client for the given palisade url. The ClientManager attempts to
     * select an appropriate client from the set of registered Clients.
     *
     * @param url a palisade url of the form pal:subname://host:port/context
     * @return a client for the provided URL
     */
    public static Client getClient(final String url) {
        for (Client client : clients) {
            if (client.acceptsURL(url)) {
                // Success!
                LOGGER.debug("getClient returning {}", client.getClass().getName());
                return (client);
            }
        }
        throw new ClientException("No suitable driver");
    }

    public static final Session openSession(final String url) {
        var client = getClient(url);
        return client.connect(url, Map.of());
    }

    public static final Session openSession(final String url, final Map<String, String> info) {
        var client = getClient(url);
        return client.connect(url, info);
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
        return getClient(url);
    }

}
