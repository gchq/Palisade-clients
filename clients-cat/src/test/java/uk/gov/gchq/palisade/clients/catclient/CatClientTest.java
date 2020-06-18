/*
 * Copyright 2018 Crown Copyright
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
package uk.gov.gchq.palisade.clients.catclient;

import feign.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.clients.simpleclient.request.ReadRequest;
import uk.gov.gchq.palisade.clients.simpleclient.request.RegisterDataRequest;
import uk.gov.gchq.palisade.clients.simpleclient.web.DataClientFactory.DataClient;
import uk.gov.gchq.palisade.clients.simpleclient.web.PalisadeClient;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

//import static org.junit.Assert.assertEquals;

//TODO REQUIRES REFACTORING TO DEPEND ON COMMON ELEMENTS
//TODO THE CLIENT SHOULD NOT DEPEND ON THE SERVICES SUCH AS PALISADE AND DATA SERVICE
//TODO INSTEAD THE RESTFUL INTERFACE SHOULD BE USED
//TODO we should be using feign annotation to do this

public class CatClientTest {

    // mock creation
    private static final PalisadeClient MOCK_PALISADE_CLIENT = Mockito.mock(PalisadeClient.class);
    private static final DataClient MOCK_DATA_CLIENT = Mockito.mock(DataClient.class);
    private static final ConnectionDetail MOCK_CONNECTION_DETAIL = new SimpleConnectionDetail().serviceName("mock-data-client");

    private static RegisterDataRequest registerDataRequest; // Client to Palisade service
    private static DataRequestResponse reqResponse; // Palisade to Client response
    private static ReadRequest readRequest1; // Client to Data Service request 1
    private static ReadRequest readRequest2; // Client to Data Service request 2
    private static Response readResponse1; // Data Service to Client response 1
    private static Response readResponse2; // Data Service to Client response 2

    private static FileResource resource1 = new FileResource().id("resource 1");
    private static FileResource resource2 = new FileResource().id("resource 2");

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    public void testClientToPalisade() throws IOException {
        //Given
        String dir = "test directory";
        String userId = "Alice";
        String purpose = "test purpose";

        String token = "Test token";
        RequestId reqId = new RequestId().id("testId");

        registerDataRequest = new RegisterDataRequest().userId(new UserId().id(userId)).resourceId(dir).context(new Context().purpose(purpose));

        readRequest1 = new ReadRequest().token(token).resource(resource1);
        readRequest1.originalRequestId(reqId);
        readRequest2 = new ReadRequest().token(token).resource(resource2);
        readRequest2.originalRequestId(reqId);
        reqResponse = new DataRequestResponse()
                .token(token)
                .resource(resource1.connectionDetail(MOCK_CONNECTION_DETAIL))
                .resource(resource2.connectionDetail(MOCK_CONNECTION_DETAIL));
        reqResponse.originalRequestId(new RequestId().id("Test ID"));

        // readResponse1
        // readResponse2

        Mockito.when(MOCK_PALISADE_CLIENT.registerDataRequestSync(Mockito.refEq(registerDataRequest, "id"))).thenReturn(reqResponse);
        Mockito.when(MOCK_DATA_CLIENT.readChunked(Mockito.refEq(readRequest1, "id", "originalRequestId"))).thenReturn(readResponse1);
        Mockito.when(MOCK_DATA_CLIENT.readChunked(Mockito.refEq(readRequest2, "id", "originalRequestId"))).thenReturn(readResponse2);

        //When
//        CatClient catClient = new CatClient();
//        catClient.cat(userId, dir, purpose);

        //Verify
//        Mockito.verify(mockPalisadeClient, Mockito.times(1)).registerDataRequestSync(Mockito.refEq(registerDataRequest, "id"));
//        Mockito.verify(mockDataClient, Mockito.times(1)).readChunked(Mockito.refEq(readRequest1, "id", "originalRequestId"));
//        Mockito.verify(mockDataClient, Mockito.times(1)).readChunked(Mockito.refEq(readRequest2, "id", "originalRequestId"));
//
//        //Then
//        assertEquals(String.format("Test data 1%nTest data 2%n"), outContent.toString());
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}
