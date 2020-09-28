/**
 *
 */
package uk.gov.gchq.palisade.client.java;

/**
 * Root class of client exceptions
 *
 * @author dbell
 *
 */
public class ClientException extends RuntimeException {

    /**
     * {@inheritDoc}
     */
    public ClientException() {
    }

    /**
     * {@inheritDoc}
     */
    public ClientException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public ClientException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public ClientException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
