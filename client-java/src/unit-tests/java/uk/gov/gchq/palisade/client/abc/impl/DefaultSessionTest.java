package uk.gov.gchq.palisade.client.abc.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class DefaultSessionTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testCreateQuery() {
        var session = new DefaultSession(Configuration.fromDefaults());
        var query = session.createQuery("resource_id");
        assertThat(query).isNotNull();
    }

    @Test
    void testCreateQueryWithProperties() {
        fail("Not yet implemented");
    }

}
