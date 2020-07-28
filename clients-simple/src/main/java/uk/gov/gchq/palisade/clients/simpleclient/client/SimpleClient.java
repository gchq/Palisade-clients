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
import uk.gov.gchq.palisade.clients.simpleclient.exception.StreamingIOException;
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
import java.util.Set;
import java.util.stream.Stream;

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
     * @param serialiser        the serialiser to use for deserialising all inbound data
     * @param palisadeClient    the client to use for connecting to the palisade-service
     * @param dataClientFactory a factory for clients for many data-services, depending on the returned connectionDetail
     */
    public SimpleClient(final Serialiser<T> serialiser, final PalisadeClient palisadeClient, final DataClientFactory dataClientFactory) {
        this.serialiser = serialiser;
        this.palisadeClient = palisadeClient;
        this.dataClientFactory = dataClientFactory;
    }

    /**
     * Does a request to the palisade-service for the given resource id using the user id and purpose for a context.
     * Then does a read of the data-service for all returned resources, and deserialises using the configured serialiser.
     *
     * @param resourceId the resource id
     * @param userId     the user id
     * @param purpose    the purpose
     * @return a stream of objects of type T from the data-service
     * @throws IOException if deserialisation of the data-service response failed
     */
    public Stream<Stream<T>> read(final String resourceId, final String userId, final String purpose) throws IOException {
        DataRequestResponse dataRequestResponse = registerRequest(resourceId, userId, purpose);
        return readResponse(dataRequestResponse);
    }

    /**
     * Does a request to the palisade-service for the given resource id using the user id and purpose for a context.
     *
     * @param resourceId the resource id
     * @param userId     the user id
     * @param purpose    the purpose
     * @return the {@link DataRequestResponse} from the palisade-service
     */
    public DataRequestResponse registerRequest(final String resourceId, final String userId, final String purpose) {
        RegisterDataRequest dataRequest = new RegisterDataRequest().resourceId(resourceId).userId(new UserId().id(userId)).context(new Context().purpose(purpose));
        return registerRequest(dataRequest);
    }

    /**
     * Does a request to the palisade-service for the given {@link RegisterDataRequest} request.
     * Provides more control over the context object than providing just the purpose.
     *
     * @param request the request
     * @return the {@link DataRequestResponse} from the palisade-service
     */
    public DataRequestResponse registerRequest(final RegisterDataRequest request) {
        return palisadeClient.registerDataRequestSync(request);
    }

    /**
     * Does a read of the data-service for the specified resources, and deserialises using the configured serialiser.
     *
     * @param token       the token returned from a previous {@link DataRequestResponse}
     * @param resourceSet the set of resources to read (if permitted)
     * @return a stream of objects of type T from the data-service
     * @throws IOException if deserialisation of the data-service response failed
     */
    public Stream<Stream<T>> readResponse(final String token, final Set<LeafResource> resourceSet) throws IOException {
        DataRequestResponse requestResponse = new DataRequestResponse().resources(resourceSet).token(token);
        return readResponse(requestResponse);
    }

    /**
     * Does a read of the data-service for all returned resources, and deserialises using the configured serialiser.
     *
     * @param response the {@link DataRequestResponse} from a previous call to the palisade-service
     * @return a stream of objects of type T from the data-service
     * @throws IOException if deserialisation of the data-service response failed
     */
    public Stream<Stream<T>> readResponse(final DataRequestResponse response) throws IOException {
        // Lazily evaluate as a stream so each element represents a new connection for a single resource to the data-service
        return response.getResources().stream()
                .map(resource -> {
                    final ConnectionDetail connectionDetail = resource.getConnectionDetail();
                    final RequestId uuid = response.getOriginalRequestId();
                    LOGGER.debug("Resource {} has connection detail {}", resource.getId(), connectionDetail);
                    DataClientFactory.DataClient dataClient = dataClientFactory.build(connectionDetail.createConnection());

                    final ReadRequest readRequest = new ReadRequest()
                            .token(response.getToken())
                            .resource(resource);
                    readRequest.setOriginalRequestId(uuid);
                    try {
                        // Creates and keeps alive a connection to the data-service
                        InputStream responseStream = dataClient.readChunked(readRequest)
                                .body()
                                .asInputStream();
                        // Lazily evaluate as a stream so each element represents a record from the parent resource stream
                        return getSerialiser()
                                .deserialise(responseStream)
                                // This inputStream needs to be closed as the stream closes, register that action here
                                .onClose(() -> {
                                    try {
                                        responseStream.close();
                                    } catch (IOException e) {
                                        LOGGER.warn("InputStream was already closed", e);
                                    }
                                });
                    } catch (IOException ex) {
                        throw new StreamingIOException(ex);
                    }
                });
    }

    /**
     * Gets the serialiser used by this client for deserialising all inbound data.
     *
     * @return the serialiser
     */
    public Serialiser<T> getSerialiser() {
        return serialiser;
    }
}
