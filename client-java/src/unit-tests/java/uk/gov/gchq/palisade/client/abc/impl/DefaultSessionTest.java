/*
 * Copyright 2020-2021 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.client.abc.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.abc.QueryInfoImpl;
import uk.gov.gchq.palisade.client.internal.dft.DefaultSession;
import uk.gov.gchq.palisade.client.internal.impl.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

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
        var query = session.createQuery(QueryInfoImpl.create(b -> b.resourceId("resource_id")));
        assertThat(query).isNotNull();
    }

}
