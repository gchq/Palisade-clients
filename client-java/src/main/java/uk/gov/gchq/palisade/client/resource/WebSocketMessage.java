package uk.gov.gchq.palisade.client.resource;

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.Serializable;
import java.util.Map;
import java.util.function.UnaryOperator;

@ImmutableStyle
public interface WebSocketMessage extends Serializable {

    /**
     * Helper method to create a {@link CompleteMessage} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    static CompleteMessage createComplete(final UnaryOperator<CompleteMessage.Builder> func) {
        return func.apply(new CompleteMessage.Builder()).build();
    }

    /**
     * Helper method to create a {@link ErrorMessage} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    static ErrorMessage createError(final UnaryOperator<ErrorMessage.Builder> func) {
        return func.apply(new ErrorMessage.Builder()).build();
    }

    /**
     * Helper method to create a {@link ResourceMessage} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    static ResourceMessage createResource(final UnaryOperator<ResourceMessage.Builder> func) {
        return func.apply(new ResourceMessage.Builder()).build();
    }

    /**
     * Returns the token to which this error belongs
     *
     * @return the token to which this error belongs
     */
    String getToken();

    /**
     * Returns any extra properties for this message or an empty map if there are
     * none
     *
     * @return any extra properties for this message or an empty map if there are
     *         none
     */
    Map<String, String> getProperties();

}
