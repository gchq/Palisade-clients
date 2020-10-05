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
package uk.gov.gchq.palisade.client.java.request;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;

@Client("${palisade.client.url}")
public interface PalisadeServiceClient {

    public static final String REGISTER_DATA_REQUEST = "/registerDataRequest";

    @Post(REGISTER_DATA_REQUEST)
    @Header("Content-Type: application/json; charset=utf-8")
    public HttpResponse<PalisadeResponse> registerDataRequestSync(@Body final PalisadeRequest request);

}
