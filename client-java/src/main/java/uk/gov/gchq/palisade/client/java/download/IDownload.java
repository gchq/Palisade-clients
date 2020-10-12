package uk.gov.gchq.palisade.client.java.download;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.io.InputStream;
import java.util.Optional;

/**
 * <p>
 * A Download represents a successful request to the Data Service. Instances of
 * this class are emitted via a stream returned upon subscription to a job.
 * </p>
 * <p>
 * If there is no {@code inputStream} this signals that there are no more
 * downloads
 * </p>
 * <p>
 * Note that the {@code DataRequest} class is created at compile time. The way
 * in which the class is created is determined by the {@link ImmutableStyle}.
 * </p>
 *
 * @author dbell
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@ImmutableStyle
public interface IDownload {

    /**
     * Returns the token
     *
     * @return the token
     */
    String getToken();

    /**
     * Returns the resource for this download or empty if no more downloads
     *
     * @return the resource for this download or empty if no more downloads
     */
    Optional<Resource> getResource();

    /**
     * Returns the {@code InputStream} for this download
     *
     * @return the {@code InputStream} for this download
     */
    Optional<InputStream> getStream();

}
