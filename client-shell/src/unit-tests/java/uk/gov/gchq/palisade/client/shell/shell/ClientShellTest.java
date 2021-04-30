/*
 * Copyright 2018-2021 Crown Copyright
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

package uk.gov.gchq.palisade.client.shell.shell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.client.java.internal.dft.DefaultClient;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultQuery;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultQueryItem;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultQueryResponse;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultSession;
import uk.gov.gchq.palisade.client.java.internal.download.DownloadImpl;
import uk.gov.gchq.palisade.client.java.internal.model.PalisadeResponse;
import uk.gov.gchq.palisade.resource.LeafResource;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Subscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ClientShellTest {

    DefaultClient client;
    ClientShell shell;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(DefaultClient.class);
        shell = new ClientShell(client);
    }

    @Test
    void testGetSession() {
        // Given
        var mockSession = Mockito.mock(DefaultSession.class);
        shell.sessionState.set(mockSession);
        // When
        var currentSession = shell.getSessionState();
        // Then
        assertThat(currentSession)
            .as("Get session state should return set session state")
            .isEqualTo(mockSession);
    }

    @Test
    void testGetSelectedToken() {
        // Given
        var mockToken = "mockToken";
        shell.tokenState.set(mockToken);
        // When
        var currentToken = shell.getTokenState();
        // Then
        assertThat(currentToken)
            .as("Get token state should return set token state")
            .isEqualTo(mockToken);
    }

    @Test
    void testOpenSession() {
        // Given
        var mockSession = Mockito.mock(DefaultSession.class);
        shell.sessionState.set(mockSession);
        // When
        var availability = shell.openSession();
        // Then
        assertThat(availability.isAvailable())
            .as("Session should be available if there is a set session state")
            .isTrue();

        // Given
        shell.sessionState.set(null);
        // When
        availability = shell.openSession();
        // Then
        assertThat(availability.isAvailable())
            .as("Session should not be available if there is not a set session state")
            .isFalse();
    }

    @Test
    void testConnect() {
        // Given
        var mockSession = Mockito.mock(DefaultSession.class);
        var mockUrl = "mock://url";
        var unacceptableUrl = "unacceptable://url";
        Mockito.when(client.acceptsURL(unacceptableUrl))
            .thenReturn(false);
        Mockito.when(client.acceptsURL(mockUrl))
            .thenReturn(true);
        Mockito.when(client.connect(Mockito.anyString()))
            .thenReturn(mockSession);

        // When
        var response = shell.connect(mockUrl);
        // Then
        assertThat(response)
            .as("Response from connect should match url used to connect if successful")
            .isEqualTo("Connected to " + mockUrl);

        // When
        var currentSession = shell.getSessionState();
        // Then
        assertThat(currentSession)
            .as("Session state should equal the session returned by the client on connect")
            .isEqualTo(mockSession);

        // When/Then
        assertThatIllegalArgumentException()
            .as("Illegal client URIs should throw an appropriate exception")
            .isThrownBy(() -> shell.connect(unacceptableUrl))
            .withMessage("Client does not accept url: " + unacceptableUrl);
    }

    @Test
    void testRegister() {
        // Given
        var mockSession = Mockito.mock(DefaultSession.class);
        var mockQuery = Mockito.mock(DefaultQuery.class);
        var mockQueryResponse = Mockito.mock(DefaultQueryResponse.class);
        var mockPalResponse = Mockito.mock(PalisadeResponse.class);
        var mockToken = "mockToken";
        Mockito.when(client.acceptsURL(Mockito.anyString()))
            .thenReturn(true);
        Mockito.when(client.connect(Mockito.anyString()))
            .thenReturn(mockSession);
        Mockito.when(mockSession.createQuery(Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(mockQuery);
        Mockito.when(mockQuery.execute())
            .thenReturn(CompletableFuture.completedFuture(mockQueryResponse));
        Mockito.when(mockQueryResponse.getPalisadeResponse())
            .thenReturn(mockPalResponse);
        Mockito.when(mockPalResponse.getToken())
            .thenReturn(mockToken);
        shell.sessionState.set(mockSession);

        // When
        var response = shell.register(new String[]{"ctxKey=val"}, "resourceId");
        // Then
        assertThat(response)
            .as("Registering a request should return the token for that request")
            .isEqualTo(mockToken);
        // Then
        assertThat(shell.registeredQueries)
            .as("Registering a request should add the response to the query map")
            .contains(Map.entry(mockToken, mockQueryResponse));
    }

    @Test
    void testList() {
        // Given
        var mockToken = "mockToken";
        var mockQueryResponse = Mockito.mock(DefaultQueryResponse.class);
        var mockQueryItem = Mockito.mock(DefaultQueryItem.class);
        var mockResource = Mockito.mock(LeafResource.class);
        var mockResourceId = "mockResourceId";
        Mockito.when(mockQueryItem.asResource())
            .thenReturn(mockResource);
        Mockito.when(mockResource.getId())
            .thenReturn(mockResourceId);
        shell = new ClientShell(client, Map.of(mockToken, mockQueryResponse), Map.of(mockToken, List.of(mockQueryItem)));

        // When
        var response = shell.list(mockToken);
        // Then
        assertThat(response)
            .as("Listing on a token should return all resources available for that request")
            .isEqualTo(mockResourceId);

        // When
        shell.tokenState.set(mockToken);
        response = shell.list(null);
        // Then
        assertThat(response)
            .as("Listing with a token state should return all resources available for that request")
            .isEqualTo(mockResourceId);

        // When
        shell.tokenState.set(null);
        response = shell.list(null);
        // Then
        assertThat(response)
            .as("Listing with neither a token arg or token state should return all registered tokens")
            .isEqualTo(mockToken);
    }

    @Test
    void testSelect() {
        // Given
        var mockToken = "mockToken";
        var nonExistentToken = "NON_EXISTENT_TOKEN";
        var mockQueryResponse = Mockito.mock(DefaultQueryResponse.class);
        shell = new ClientShell(client, Map.of(mockToken, mockQueryResponse), Map.of(mockToken, List.of()));

        // When
        var response = shell.select(mockToken);
        // Then
        assertThat(response)
            .as("Selecting a token should report that the requested token was selected")
            .isEqualTo("Selected " + mockToken);
        // Then
        assertThat(shell.tokenState.get())
            .as("Selecting a token should set the token state to the selected token")
            .isEqualTo(mockToken);

        // When
        response = shell.select("..");
        // Then
        assertThat(response)
            .as("Deselecting a token should report that the requested token was deselected")
            .isEqualTo("Deselected token");
        // Then
        assertThat(shell.tokenState.get())
            .as("Deselecting a token should set the token state to null")
            .isNull();

        // When/Then
        assertThatIllegalArgumentException()
            .as("Selecting a token that doesn't exist should throw an error")
            .isThrownBy(() -> shell.select(nonExistentToken))
            .withMessage("No such registered token: " + nonExistentToken);
    }

    @Test
    void testRead() {
        // Given
        var mockToken = "mockToken";
        var mockSession = Mockito.mock(DefaultSession.class);
        var mockQueryResponse = Mockito.mock(DefaultQueryResponse.class);
        var mockQueryItem = Mockito.mock(DefaultQueryItem.class);
        var mockResource = Mockito.mock(LeafResource.class);
        var mockDownload = Mockito.mock(DownloadImpl.class);
        var mockResourceId = "mockResourceId";
        var nonExistentResourceId = "NON_EXISTENT_RESOURCE";
        var mockData = "mockData";
        Mockito.when(mockQueryResponse.stream())
            .thenReturn(Subscriber::onComplete);
        Mockito.when(mockQueryItem.asResource())
            .thenReturn(mockResource);
        Mockito.when(mockResource.getId())
            .thenReturn(mockResourceId);
        Mockito.when(mockSession.fetch(mockQueryItem))
            .thenReturn(mockDownload);
        Mockito.when(mockDownload.getInputStream()).thenAnswer(invocation -> new ByteArrayInputStream(mockData.getBytes()));
        shell = new ClientShell(client, Map.of(mockToken, mockQueryResponse), Map.of(mockToken, List.of(mockQueryItem)));
        shell.sessionState.set(mockSession);

        // When
        var response = shell.read(mockResourceId, mockToken);
        // Then
        assertThat(response)
            .isEqualTo(mockData);

        // When
        shell.tokenState.set(mockToken);
        response = shell.read(mockResourceId, null);
        // Then
        assertThat(response)
            .as("Reading a resource should return its data")
            .isEqualTo(mockData);

        // When/Then
        shell.tokenState.set(null);
        assertThatIllegalArgumentException()
            .as("Resources can only be read with a selected token (otherwise no token to send to the Data Service)")
            .isThrownBy(() -> shell.read(mockResourceId, null))
            .withMessage("No token selected");

        // When/Then
        assertThatIllegalArgumentException()
            .as("Reading a resource that doesn't exist should throw an exception")
            .isThrownBy(() -> shell.read(nonExistentResourceId, mockToken))
            .withMessage("No such received resource: " + nonExistentResourceId);
    }
}
