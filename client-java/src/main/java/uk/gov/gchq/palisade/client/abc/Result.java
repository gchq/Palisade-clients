package uk.gov.gchq.palisade.client.abc;

import org.reactivestreams.Publisher;

public interface Result {

    Publisher<ResourceInfo> stream();

}
