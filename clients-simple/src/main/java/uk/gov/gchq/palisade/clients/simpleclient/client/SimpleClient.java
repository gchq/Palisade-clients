/*
 * Copyright 2020 Crown Copyright
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

package uk.gov.gchq.palisade.clients.simpleclient.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.clients.simpleclient.request.ReadRequest;
import uk.gov.gchq.palisade.clients.simpleclient.request.RegisterDataRequest;
import uk.gov.gchq.palisade.clients.simpleclient.web.DataClientFactory;
import uk.gov.gchq.palisade.clients.simpleclient.web.PalisadeClient;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * The type Simple client.
 *
 * @param <T> the type parameter
 */
public class SimpleClient<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClient.class);
    private final Serialiser<T> serialiser;

    private final PalisadeClient palisadeClient;
    private final DataClientFactory dataClientFactory;

    /**
     * Instantiates a new Simple client.
     *
     * @param serialiser        the serialiser
     * @param palisadeClient    the palisade client
     * @param dataClientFactory the data client factory
     */
    public SimpleClient(final Serialiser<T> serialiser, final PalisadeClient palisadeClient, final DataClientFactory dataClientFactory) {
        this.serialiser = serialiser;
        this.palisadeClient = palisadeClient;
        this.dataClientFactory = dataClientFactory;
    }

    /**
     * Read stream.
     *
     * @param filename the filename
     * @param userId   the user id
     * @param purpose  the purpose
     * @return a stream of getObjectStream
     * @throws IOException the io exception
     */
    public Stream<T> read(final String filename, final String userId, final String purpose) throws IOException {
        DataRequestResponse dataRequestResponse = makeRequest(filename, userId, purpose);
        return getObjectStreams(dataRequestResponse);
    }

    private DataRequestResponse makeRequest(final String fileName, final String userId, final String purpose) {
        RegisterDataRequest dataRequest = new RegisterDataRequest().resourceId(fileName).userId(new UserId().id(userId)).context(new Context().purpose(purpose));

        // While there may be many palisade services, just use one
        return palisadeClient.registerDataRequestSync(dataRequest);
    }

    /**
     * For a given DataRequestResponse returns a stream of deserialised objects from the dataClient
     *
     * @param response the response
     * @return the object streams
     * @throws IOException the io exception
     */
    public Stream<T> getObjectStreams(final DataRequestResponse response) throws IOException {
        requireNonNull(response, "response");

        final List<Stream<T>> dataStreams = new ArrayList<>(response.getResources().size());
        for (final LeafResource resource : response.getResources()) {
            final ConnectionDetail connectionDetail = resource.getConnectionDetail();
            final RequestId uuid = response.getOriginalRequestId();

            final ReadRequest readRequest = new ReadRequest()
                    .token(response.getToken())
                    .resource(resource);
            readRequest.setOriginalRequestId(uuid);

            LOGGER.info("Resource {} has DATA-SERVICE connection detail {}", resource.getId(), connectionDetail);
            DataClientFactory.DataClient dataClient = dataClientFactory.build(connectionDetail.createConnection());
            InputStream responseStream = dataClient.readChunked(readRequest).body().asInputStream();
            Stream<T> dataStream = getSerialiser().deserialise(responseStream);
            dataStreams.add(dataStream);
        }
        return dataStreams.stream().flatMap(Function.identity());
    }

    /**
     * Gets serialiser.
     *
     * @return the serialiser
     */
    public Serialiser<T> getSerialiser() {
        return serialiser;
    }
}
