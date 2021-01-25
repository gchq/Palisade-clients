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
package uk.gov.gchq.palisade.clients.mapreduce;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.util.StringUtils;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * The input split for {@link PalisadeInputFormat}. This class contains all the information for describing the resources
 * for one split and the necessary connection methods for finding the data services responsible for finding those
 * resources.
 */
public class PalisadeInputSplit extends InputSplit implements Writable {
    /**
     * The response object that contains all the details
     */
    private DataRequestResponse requestResponse;

    /**
     * No-arg constructor required by Hadoop to de-serialise.
     */
    public PalisadeInputSplit() {
        requestResponse = new DataRequestResponse();
    }

    /**
     * Create a new input split. The given details are wrapped inside a new {@link DataRequestResponse} object.
     *
     * @param token             the token to validate that the request for data has been registered
     * @param resources         the resources to be processed by this split
     * @param originalRequestId this Id is unique per data access request from a user
     * @throws NullPointerException if anything is null
     */
    public PalisadeInputSplit(final String token, final Set<LeafResource> resources, final RequestId originalRequestId) {
        DataRequestResponse temp = new DataRequestResponse().token(requireNonNull(token)).resources(requireNonNull(resources));
        temp.originalRequestId(requireNonNull(originalRequestId));
        this.setRequestResponse(temp);
    }

    /**
     * Create a new input split. The request response is stored inside this input split.
     *
     * @param requestResponse the response object
     * @throws NullPointerException if {@code requestResponse} is null
     */
    public PalisadeInputSplit(final DataRequestResponse requestResponse) {
        this.setRequestResponse(requestResponse);
    }

    /**
     * Get the response for this input split. This response object may contain many resources for a single request
     *
     * @return the response object
     */
    @Generated
    public DataRequestResponse getRequestResponse() {
        return requestResponse;
    }

    /**
     * Sets request response.
     *
     * @param requestResponse the request response
     */
    @Generated
    public void setRequestResponse(final DataRequestResponse requestResponse) {
        requireNonNull(requestResponse);
        this.requestResponse = requestResponse;
    }

    /**
     * {@inheritDoc}
     * <p>
     * We measure length according to the number of resources that this split will process.
     *
     * @return the number of resources contained in this split
     */
    @Override
    @Generated
    public long getLength() {
        return getRequestResponse().getResources().size();
    }

    /**
     * {@inheritDoc}
     * <p>
     * We don't implement about data locality, so this always returns an empty array.
     *
     * @return always returns an empty string array
     */
    @Override
    @Generated
    public String[] getLocations() {
        return StringUtils.emptyStringArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final DataOutput dataOutput) throws IOException {
        requireNonNull(dataOutput, "dataOutput");
        //serialise this class to JSON and write out
        byte[] serial = JSONSerialiser.serialise(requestResponse);

        dataOutput.writeInt(serial.length);
        dataOutput.write(serial);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFields(final DataInput dataInput) throws IOException {
        requireNonNull(dataInput, "dataInput");
        int length = dataInput.readInt();
        //validate length
        if (length < 0) {
            throw new IOException("illegal negative length on deserialisation");
        }
        //make buffer and read
        byte[] buffer = new byte[length];
        dataInput.readFully(buffer);
        //deserialise
        DataRequestResponse deserialisedResponse = JSONSerialiser.deserialise(buffer, DataRequestResponse.class);
        requireNonNull(deserialisedResponse, "deserialised request response was null");
        //all clear
        this.requestResponse = deserialisedResponse;
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PalisadeInputSplit)) {
            return false;
        }
        final PalisadeInputSplit that = (PalisadeInputSplit) o;
        return Objects.equals(requestResponse, that.requestResponse);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(requestResponse);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", PalisadeInputSplit.class.getSimpleName() + "[", "]")
                .add("requestResponse=" + requestResponse)
                .toString();
    }
}
