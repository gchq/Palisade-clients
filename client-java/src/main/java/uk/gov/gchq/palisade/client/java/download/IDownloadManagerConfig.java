package uk.gov.gchq.palisade.client.java.download;

import io.micronaut.context.ApplicationContext;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import com.google.common.eventbus.EventBus;

@Value.Immutable
@ImmutableStyle
public interface IDownloadManagerConfig {
    String getId();
    EventBus getEventBus();
    ApplicationContext getApplicationContext();
}
