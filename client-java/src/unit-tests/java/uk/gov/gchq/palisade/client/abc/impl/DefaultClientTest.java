package uk.gov.gchq.palisade.client.abc.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultClientTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testAcceptsURL() {
        var client = new DefaultClient();
        assertThat(client.acceptsURL("pal://localhost")).isTrue();
        assertThat(client.acceptsURL("jdbc://localhost")).isFalse();
    }

    @Test
    void testConnect() {
        var client = new DefaultClient();
        var session = client.connect("pal://localhost", Map.of());
        assertThat(session).isInstanceOf(DefaultSession.class);
    }

}
