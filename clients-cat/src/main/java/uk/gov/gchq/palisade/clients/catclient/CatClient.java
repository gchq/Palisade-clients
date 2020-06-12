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

//TODO REQUIRES REFACTORING TO DEPEND ON COMMON ELEMENTS
//TODO THE CLIENT SHOULD NOT DEPEND ON THE SERVICES SUCH AS PALISADE AND DATA SERVICE
//TODO INSTEAD THE RESTFUL INTERFACE SHOULD BE USED
//TODO we should be using feign annotation to do this

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.clients.simpleclient.request.ReadRequest;
import uk.gov.gchq.palisade.clients.simpleclient.request.RegisterDataRequest;
import uk.gov.gchq.palisade.clients.simpleclient.web.DataClientFactory;
import uk.gov.gchq.palisade.clients.simpleclient.web.DataClientFactory.DataClient;
import uk.gov.gchq.palisade.clients.simpleclient.web.PalisadeClient;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class CatClient {

    @Autowired
    private PalisadeClient palisadeClient;
    @Autowired
    private DataClientFactory dataClientFactory;

    public CatClient(final PalisadeClient palisadeClient, final DataClientFactory dataClientFactory) {
        this.palisadeClient = palisadeClient;
        this.dataClientFactory = dataClientFactory;
    }

    public CatClient() {
    }

    public static void main(final String[] args) throws IOException {
        if (args.length == 3) {
            String userId = args[0];
            String resource = args[1];
            String purpose = args[2];

            new CatClient().cat(userId, resource, purpose);
        } else {
            System.out.printf("Usage: %s userId resource purpose%n%n", CatClient.class.getSimpleName());
            System.out.println("userId\t\t the unique id of the user making this query");
            System.out.println("resource\t the name of the resource being requested");
            System.out.println("purpose\t\t purpose for accessing the resource");
        }
    }

    public void cat(final String userId, final String resource, final String purpose) throws IOException {
        final RegisterDataRequest dataRequest = new RegisterDataRequest().resourceId(resource).userId(new UserId().id(userId)).context(new Context().purpose(purpose));
        final DataRequestResponse dataRequestResponse = palisadeClient.registerDataRequestSync(dataRequest);
        for (final LeafResource leafResource : dataRequestResponse.getResources()) {
            final ConnectionDetail connectionDetail = leafResource.getConnectionDetail();
            final DataClient dataClient = dataClientFactory.build(connectionDetail.createConnection());
            final ReadRequest readRequest = new ReadRequest()
                    .token(dataRequestResponse.getToken())
                    .resource(leafResource);
            readRequest.setOriginalRequestId(dataRequestResponse.getOriginalRequestId());

            final Response response = dataClient.readChunked(readRequest);
            new BufferedReader(new InputStreamReader(response.body().asInputStream())).lines().forEachOrdered(System.out::println);
        }
    }
}
