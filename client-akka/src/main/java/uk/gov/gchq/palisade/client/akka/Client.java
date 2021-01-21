/*
 * Copyright 2021 Crown Copyright
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

package uk.gov.gchq.palisade.client.akka;

import uk.gov.gchq.palisade.resource.LeafResource;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow.Publisher;

public interface Client {

    CompletionStage<String> register(final String userId, final String resourceId, final Map<String, String> context);

    Publisher<LeafResource> fetch(final String token);

    InputStream read(final String token, final LeafResource resource);

}
