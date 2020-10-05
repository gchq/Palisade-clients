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
package uk.gov.gchq.palisade.client.java.data;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;

public interface DataServiceClient {

    public static final String ENDPOINT_READ_CHUNKED = "/read/chunked";

    /**
     * TODO: At the moment this has one major drawback in that it will only allow
     * downloading all the data at once that will fit in memory. Need to find a way
     * to get access to the underlying BytBuffer from Netty.
     *
     * It looks like this may be possible:
     *
     * <pre>
     * <{@code
     * Flowable<ByteBuffer<?>> responseFlowable = myClient.getQueryResult("job1", "foo")
     * int sum = 0
     * responseFlowable.blockingForEach { ByteBuffer byteBuffer ->
     *   sum += byteBuffer.toByteArray().count('!')
     *   ((ReferenceCounted)byteBuffer).release() // Let Netty do its thing!
     * }
     * }
     * </pre>
     *
     * Have to release those netty buffers though otherwise chaos will ensue :)
     *
     * @param request
     * @return
     */
    @Post(ENDPOINT_READ_CHUNKED)
    @Header("Content-Type: application/json; charset=utf-8")
    HttpResponse<byte[]> readChunked(@Body final DataRequest request);

}
