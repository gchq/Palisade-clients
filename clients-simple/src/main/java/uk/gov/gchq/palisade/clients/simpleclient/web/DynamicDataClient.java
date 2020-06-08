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
package uk.gov.gchq.palisade.clients.simpleclient.web;

import feign.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import uk.gov.gchq.palisade.clients.simpleclient.request.AddSerialiserRequest;
import uk.gov.gchq.palisade.clients.simpleclient.request.ReadRequest;

public interface DynamicDataClient {
    interface DataClient {

        @PostMapping(value = "/read/chunked", consumes = "application/json", produces = "application/octet-stream")
        Response readChunked(@RequestBody final ReadRequest request);

        @PostMapping(value = "/addSerialiser", consumes = "application/json", produces = "application/json")
        Boolean addSerialiser(@RequestBody final AddSerialiserRequest request);

    }

    DataClient clientFor(final String serviceId);
}



