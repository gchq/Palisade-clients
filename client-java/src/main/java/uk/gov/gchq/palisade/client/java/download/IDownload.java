package uk.gov.gchq.palisade.client.java.download;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.io.InputStream;
import java.util.Optional;

@Value.Immutable
@ImmutableStyle
public interface IDownload {
    String getToken();

    Optional<Resource> getResource();

    Optional<InputStream> getStream();
}
