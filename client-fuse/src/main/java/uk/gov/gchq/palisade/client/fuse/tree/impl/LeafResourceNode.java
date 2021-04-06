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
import uk.gov.gchq.palisade.client.fuse.tree.TreeNode;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

@SuppressWarnings({"NullableProblems", "unchecked", "rawtypes"})
public class LeafResourceNode implements ChildNode<Resource> {
    final String id;
    final ParentNode<ParentResource> parent;
    final LeafResource resource;

    public LeafResourceNode(final String id, final ParentNode<ParentResource> parent, final LeafResource resource) {
        this.id = id;
        this.parent = parent;
        this.resource = resource;
    }

    @Override
    public ParentNode<Resource> getParent() {
        return (ParentNode) parent;
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
    public Stream<Resource> traverse() {
        return Stream.of(this.get());
    }

    // Collection

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(final Object o) {
        return false;
    }

    @Override
    public Iterator<TreeNode<Resource>> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(final T[] ts) {
        return (T[]) new Object[0];
    }

    @Override
    public boolean add(final TreeNode<Resource> resourceTreeNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(final Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends TreeNode<Resource>> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.prettyprint(sb::append, 0);
        return sb.toString();
    }
}
