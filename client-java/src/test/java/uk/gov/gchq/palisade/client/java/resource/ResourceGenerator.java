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
package uk.gov.gchq.palisade.client.java.resource;

import java.util.Iterator;

public class ResourceGenerator implements Iterator<Resource> {

    private final int size;
    private final String token;

    private int count = 0;

    public ResourceGenerator(String token, int size) {
        this.size = size;
        this.token = token;
    }

    @Override
    public Resource next() {
        return IResource.create(b -> b
            .token(token)
                .leafResourceId("leaf_resource_" + ++count)
                .url("http://localhost:8081"));
    }

    @Override
    public boolean hasNext() {
        return size == 0 || count < size;
    }

}