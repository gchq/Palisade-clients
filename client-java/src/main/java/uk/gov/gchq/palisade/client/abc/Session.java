package uk.gov.gchq.palisade.client.abc;

import java.util.Map;

public interface Session {

    Query createQuery(String resourceId);

    Query createQuery(String resourceId, Map<String, String> context);

}
