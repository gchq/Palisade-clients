package uk.gov.gchq.palisade.client.abc;

import java.util.concurrent.Flow.Publisher;

public interface QueryResponse {

    Publisher<Message> stream();

}
