package uk.gov.gchq.palisade.client.java.download;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.receiver.Receiver;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.util.function.Supplier;

import com.google.common.eventbus.EventBus;

/**
 * <p>
 * An instance of {@code DownloadManagerConfig} is passed to a download manager
 * during creation.
 * </p>
 * <p>
 * Note that the {@code DownloadManagerConfig} class is created at compile time.
 * The way in which the class is created is determined by the
 * {@link ImmutableStyle}.
 * </p>
 *
 * @author dbell
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@ImmutableStyle
public interface IDownloadManagerConfig {

    /**
     * Returns the id (name) of the configured download manager
     *
     * @return the id (name) of the configured download manager
     */
    String getId();

    /**
     * Returns the event bus to be used when posting events
     *
     * @return the event bus to be used when posting events
     */
    EventBus getEventBus();

    /**
     * Returns the application (DI) context from which configuration and services
     * can be retrieved
     *
     * @return the application (DI) context from which configuration and services
     *         can be retrieved
     */
    ClientContext getClientContext();

    /**
     * Returns the supplier that will create and return {@link Receiver} instances
     * that will actually consume the input stream supplied by a downloader.
     *
     * @return the supplier that will create and return {@link Receiver} instances
     *         that will actually consume the input stream supplied by a downloader
     */
    Supplier<Receiver> getReceiverSupplier();

}
