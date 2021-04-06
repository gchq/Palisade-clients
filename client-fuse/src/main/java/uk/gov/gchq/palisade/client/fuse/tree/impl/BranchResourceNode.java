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

package uk.gov.gchq.palisade.client.fuse.tree.impl;

import uk.gov.gchq.palisade.client.fuse.tree.ChildNode;
import uk.gov.gchq.palisade.client.fuse.tree.ParentNode;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BranchResourceNode implements ParentNode<Resource>, ChildNode<Resource> {
    final String id;
    final ParentNode<ParentResource> parent;
    final Set<ChildNode<ChildResource>> children;
    final ChildResource resource;

    public BranchResourceNode(final String id, final ParentNode<ParentResource> parent, final ChildResource resource) {
        this.id = id;
        this.parent = parent;
        if (!(resource instanceof ParentResource)) {
            throw new IllegalArgumentException("Resource must be both parent and child");
        }
        this.resource = resource;
        this.children = new HashSet<>();
    }

    @Override
    public ParentNode<Resource> getParent() {
        return (ParentNode) parent;
    }

    @Override
    public Collection<ChildNode<Resource>> getChildren() {
        return (Collection) children;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Resource get() {
        return resource;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.prettyprint(sb::append, 0);
        return sb.toString();
    }
}
