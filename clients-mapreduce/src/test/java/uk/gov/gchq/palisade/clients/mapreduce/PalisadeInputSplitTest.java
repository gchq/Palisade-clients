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

import org.junit.Assert;
import org.junit.Test;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PalisadeInputSplitTest {

    @Test(expected = IOException.class)
    public void shouldThrowOnNegativeLength() throws IOException {
        //Given
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(buffer);
        dos.writeInt(-1);
        ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toByteArray());
        DataInputStream dis = new DataInputStream(bis);
        PalisadeInputSplit test = new PalisadeInputSplit();
        //When
        test.readFields(dis);
        //Then - throws IOException
        Assert.fail("exception expected");
    }

    @Test
    public void shouldSerialiseToEqualObject() throws IOException {
        //Given
        ConnectionDetail connectionDetail = new SimpleConnectionDetail().serviceName("data-service");
        FileResource fileResource = new FileResource().id("test id").type("test type").serialisedFormat("test format").connectionDetail(connectionDetail);
        DataRequestResponse drr = new DataRequestResponse()
                .token("test string")
                .resource(fileResource);
        drr.originalRequestId(new RequestId().id("test id"));
        PalisadeInputSplit test = new PalisadeInputSplit(drr);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(buffer);
        //When
        test.write(dos);
        ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toByteArray());
        DataInputStream dis = new DataInputStream(bis);
        PalisadeInputSplit readBack = new PalisadeInputSplit();
        readBack.readFields(dis);
        //Then
        Assert.assertEquals(test, readBack);
    }
}
