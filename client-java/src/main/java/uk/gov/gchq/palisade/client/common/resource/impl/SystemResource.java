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

package uk.gov.gchq.palisade.client.common.resource.impl;

import uk.gov.gchq.palisade.client.common.resource.AbstractResource;
import uk.gov.gchq.palisade.client.common.resource.ParentResource;

/**
 * A SystemResource is the Palisade representation of a root directory inside a system
 * {@code eg. "file:/dev/Palisade/pom.xml" = System "/" -> Directory "/dev/" -> Directory "/dev/Palisade/" -> File "/dev/Palisade/pom.xml" }
 */
public class SystemResource extends AbstractResource implements ParentResource {
    private static final long serialVersionUID = 1L;

    public SystemResource() {
        //no-args constructor needed for serialization only
    }

    @Override
    public SystemResource id(final String id) {
        if (id.endsWith("/")) {
            return (SystemResource) super.id(id);
        } else {
            return (SystemResource) super.id(id + "/");
        }
    }
}
