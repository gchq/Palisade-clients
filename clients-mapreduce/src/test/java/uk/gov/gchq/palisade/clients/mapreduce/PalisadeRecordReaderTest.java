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
package uk.gov.gchq.palisade.clients.mapreduce;

import feign.Response;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.clients.simpleclient.request.ReadRequest;
import uk.gov.gchq.palisade.clients.simpleclient.web.DataClientFactory.DataClient;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.serialise.SimpleStringSerialiser;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.TreeMap;
//import java.util.concurrent.CompletionException;

public class PalisadeRecordReaderTest {
    private static Configuration conf;
    private static TaskAttemptContext con;
    private static Serialiser<String> serialiser;

    @BeforeClass
    public static void setup() {
        conf = new Configuration();
        //make sure this is available for the tests
        serialiser = new SimpleStringSerialiser();
        PalisadeInputFormat.setSerialiser(conf, serialiser);
        con = new TaskAttemptContextImpl(conf, new TaskAttemptID());
    }

    @Test(expected = ClassCastException.class)
    public void shouldThrowOnNonPalisadeInputSplit() throws IOException {
        //Given
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        InputSplit is = mock(InputSplit.class);
        //When
        prr.initialize(is, con);
        //Then
        fail("expected exception");
    }

    @Test(expected = IOException.class)
    public void shouldThrowOnNoResourceInSplit() throws IOException {
        //Given
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        PalisadeInputSplit is = new PalisadeInputSplit("test", new HashSet<>(), new RequestId().id("test"));
        //When
        prr.initialize(is, con);
        //Then
        fail("expected exception");
    }

    @Test
    public void shouldNotHaveNextAfterClosed() throws IOException {
        //Given
//        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
//        Collection<String> resData = Arrays.asList("s1", "s2", "s3", "s4");
//        DataRequestResponse response = new DataRequestResponse().token("request1")
//                .resource(new StubResource("type_a", "id1", "format1"), new StubConnectionDetail("con1")
//                        .setServiceToCreate(createMockDS(resData, false)))
//                .resource(new StubResource("type_b", "id2", "format2"), new StubConnectionDetail("con2")
//                        .setServiceToCreate(createMockDS(resData, false)));
//        response.originalRequestId(new RequestId().id("test 1"));
//
//        //When
//        PalisadeInputSplit split = new PalisadeInputSplit(response);
//        prr.initialize(split, con);
//        //start the stream reading
//        prr.nextKeyValue();
//        prr.nextKeyValue();
//        prr.close();
//        //Then
//        assertFalse(prr.nextKeyValue());
    }

    /**
     * Validate that a record reader is returning the expected results in order and that {@link
     * PalisadeRecordReader#nextKeyValue()} is responding correctly.
     *
     * @param expected   the list of items expected
     * @param testReader the reader under test
     * @param <T>        value type of reader
     * @throws IOException that shouldn't happen
     */
    private static <T> void readValuesAndValidate(final Stream<T> expected, final PalisadeRecordReader<T> testReader) {
        expected.forEach(item -> {
            assertTrue(testReader.nextKeyValue());
            assertEquals(item, testReader.getCurrentValue());
        });
        assertFalse(testReader.nextKeyValue());
    }

    /**
     * Validates that the read method for each mock data service was called once. This method assumes that each resource
     * in the DataResponseRequest has its own unique mock DataClient.
     *
     * @param response the collection of all responses that will have been read
     */
    private static void verifyMocksCalled(final DataRequestResponse response) {
        // Must be reworked
    }

    private static DataClient createMockDS(final Collection<String> dataToReturn, final boolean shouldFail) throws IOException {
        //create the simulated response
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialiser.serialise(dataToReturn.stream(), baos);
        Response readResponse = mock(Response.class);
        when(readResponse.body().asInputStream()).thenReturn(new ByteArrayInputStream(baos.toByteArray()));
        //mock a data service to return it
        DataClient mock = mock(DataClient.class);
        if (shouldFail) {
            when(mock.readChunked(any(ReadRequest.class))).thenThrow(new RuntimeException("test exception"));
        } else {
            when(mock.readChunked(any(ReadRequest.class))).thenReturn(readResponse);
        }
        return mock;
    }

    @Test
    public void shouldContinueOnResourceError() throws IOException {
        resourceFailure(ReaderFailureMode.CONTINUE_ON_READ_FAILURE);
    }

//    @Test(expected = CompletionException.class)
//    public void shouldFailOnResourceError() throws IOException {
//        resourceFailure(ReaderFailureMode.FAIL_ON_READ_FAILURE);
//    }

    /**
     * Runs a test with one resource that will fail and a second one that should succeed.
     *
     * @param mode the failure mode for resources
     * @throws IOException IOException
     */
    private void resourceFailure(final ReaderFailureMode mode) throws IOException {
//        //Given - multiple resources
//        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
//        //add more data which should succeed
//        List<String> returnResources2 = Arrays.asList("s5", "s6", "s7", "s8");
//        DataRequestResponse response = new DataRequestResponse().token("test")
//                //set up some data which should return an error
//                .resource(new StubResource("type_a", "id1", "format1"),
//                        new StubConnectionDetail("con1").setServiceToCreate(createMockDS(Collections.emptyList(), true)))
//                .resource(new StubResource("type_b", "id2", "format2"),
//                        new StubConnectionDetail("con2").setServiceToCreate(createMockDS(returnResources2, false)));
//        response.originalRequestId(new RequestId().id("test"));
//        //When
//        PalisadeInputSplit split = new PalisadeInputSplit(response);
//        PalisadeInputFormat.setResourceErrorBehaviour(con, mode);
//        prr.initialize(split, con);
//        //Then
//        readValuesAndValidate(returnResources2.stream(), prr);
//        verifyMocksCalled(response);
    }

    @Test
    public void shouldReadbackResourcesFromOneResource() throws IOException {
//        //Given
//        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
//        //set up some data
//        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
//        DataRequestResponse response = new DataRequestResponse().token("request1")
//                //set up the mock data service
//                .resource(new StubResource("type_a", "id1", "format1"),
//                        new StubConnectionDetail("con1").setServiceToCreate(createMockDS(returnResources, false)));
//        response.originalRequestId(new RequestId().id("test"));
//        //When
//        PalisadeInputSplit split = new PalisadeInputSplit(response);
//        prr.initialize(split, con);
//        //Then
//        readValuesAndValidate(returnResources.stream(), prr);
//        verifyMocksCalled(response);
    }

    @Test
    public void shouldReadbackResultsFromOneResourcePlusOneEmpty() throws IOException {
//        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
//        //set up some data
//        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
//        //inject a treemap to ensure iteration order
//        DataRequestResponse response = new DataRequestResponse().token("test")
//                .resource(new StubResource("type_a", "id1", "format1"),
//                        new StubConnectionDetail("con1").setServiceToCreate(createMockDS(returnResources, false)))
//                //make an empty resource response
//                .resource(new StubResource("type_b", "id2", "format2"),
//                        new StubConnectionDetail("con2").setServiceToCreate(createMockDS(Collections.emptyList(), false)));
//        response.originalRequestId(new RequestId().id("test"));
//        //When
//        PalisadeInputSplit split = new PalisadeInputSplit(response);
//        prr.initialize(split, con);
//        //Then
//        readValuesAndValidate(Stream.concat(returnResources.stream(), Stream.empty()), prr);
//        verifyMocksCalled(response);
    }

    @Test
    public void shouldReadbackNothingFromEmptyResource() throws IOException {
//        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
//        DataRequestResponse response = new DataRequestResponse().token("request1")
//                //make an empty resource response
//                .resource(new StubResource("type_a", "id1", "format1"),
//                        new StubConnectionDetail("con1").setServiceToCreate(createMockDS(Collections.emptyList(), false)));
//        response.originalRequestId(new RequestId().id("test"));
//        //When
//        PalisadeInputSplit split = new PalisadeInputSplit(response);
//        prr.initialize(split, con);
//        //Then
//        readValuesAndValidate(Stream.empty(), prr);
//        verifyMocksCalled(response);
    }

    @Test
    public void shouldReadbackResultsFromEmptyResourceThenOneResourceWithData() throws IOException {
//        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
//        //set up some data
//        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
//        DataRequestResponse response = new DataRequestResponse().token("test")
//                .resource(new StubResource("type_a", "id1", "format1"),
//                        new StubConnectionDetail("con1").setServiceToCreate(createMockDS(Collections.emptyList(), false)))
//                .resource(new StubResource("type_b", "id2", "format2"),
//                        new StubConnectionDetail("con2").setServiceToCreate(createMockDS(returnResources, false)));
//        response.originalRequestId(new RequestId().id("test"));
//        //When
//        PalisadeInputSplit split = new PalisadeInputSplit(response);
//        prr.initialize(split, con);
//        //Then
//        readValuesAndValidate(Stream.concat(Stream.empty(), returnResources.stream()), prr);
//        verifyMocksCalled(response);
    }

    @Test
    public void shouldReturnResultsFromTwoResources() throws IOException {
//        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
//        //set up some data
//        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
//        //add more data
//        List<String> returnResources2 = Arrays.asList("s5", "s6", "s7", "s8");
//        DataRequestResponse response = new DataRequestResponse().token("test")
//                .resource(new StubResource("type_a", "id1", "format1"),
//                        new StubConnectionDetail("con1").setServiceToCreate(createMockDS(returnResources, false)))
//                .resource(new StubResource("type_b", "id2", "format2"),
//                        new StubConnectionDetail("con2").setServiceToCreate(createMockDS(returnResources2, false)));
//        response.originalRequestId(new RequestId().id("test"));
//        //When
//        PalisadeInputSplit split = new PalisadeInputSplit(response);
//        prr.initialize(split, con);
//        //Then
//        readValuesAndValidate(Stream.concat(returnResources.stream(), returnResources2.stream()), prr);
//        verifyMocksCalled(response);
    }

    @Test
    public void shouldReturnResultsFromTwoResourcesWithEmptyInBetween() throws IOException {
//        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
//        DataRequestResponse response = new DataRequestResponse().token("test")
//                .resources(new TreeMap<>());
//        response.originalRequestId(new RequestId().id("test"));
//        //set up some data
//        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
//        response.getResources().put(new StubResource("type_a", "id1", "format1"), new StubConnectionDetail("con1").setServiceToCreate(createMockDS(returnResources, false)));
//        //add an empty
//        response.getResources().put(new StubResource("type_b", "id2", "format2"), new StubConnectionDetail("con2").setServiceToCreate(createMockDS(Collections.emptyList(), false)));
//        //add more data
//        List<String> returnResources2 = Arrays.asList("s5", "s6", "s7", "s8");
//        response.getResources().put(new StubResource("type_c", "id3", "format3"), new StubConnectionDetail("con3").setServiceToCreate(createMockDS(returnResources2, false)));
//        //When
//        PalisadeInputSplit split = new PalisadeInputSplit(response);
//        prr.initialize(split, con);
//        //Then
//        readValuesAndValidate(Stream.concat(returnResources.stream(), returnResources2.stream()), prr);
//        verifyMocksCalled(response);
    }

    @Test
    public void shouldReturnNothingFromEmpties() throws IOException {
//        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
//        DataRequestResponse response = new DataRequestResponse().token("test")
//                .resources(new HashMap<>());
//        response.originalRequestId(new RequestId().id("test"));
//        //add empty resources
//        response.getResources().put(new StubResource("type_a", "id1", "format1"), new StubConnectionDetail("con1").setServiceToCreate(createMockDS(Collections.emptyList(), false)));
//        response.getResources().put(new StubResource("type_b", "id2", "format2"), new StubConnectionDetail("con2").setServiceToCreate(createMockDS(Collections.emptyList(), false)));
//        response.getResources().put(new StubResource("type_c", "id3", "format3"), new StubConnectionDetail("con3").setServiceToCreate(createMockDS(Collections.emptyList(), false)));
//        //When
//        PalisadeInputSplit split = new PalisadeInputSplit(response);
//        prr.initialize(split, con);
//        //Then
//        readValuesAndValidate(Stream.empty(), prr);
//        verifyMocksCalled(response);
    }
}
