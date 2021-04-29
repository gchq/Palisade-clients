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
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A node in a tree which is only a parent.
 * Therefore it is the 'root' of the tree, represented by a {@link ParentResource}.
 * See the {@link uk.gov.gchq.palisade.resource.impl.SystemResource} implementation
 * for an analogous {@link Resource} class.
 */
@SuppressWarnings({"NullableProblems", "unchecked", "rawtypes"})
public class RootResourceNode implements ParentNode<Resource> {
    private final String id;
    private final Set<ChildNode<ChildResource>> children;
    private final ParentResource resource;

    /**
     * Create a new branch node, given its id, parent and the resource it represents
     *
     * @param id       the {@link TreeNode#getId()} identifier for this node
     * @param resource the {@link TreeNode#get()} collection item stored at this point, which
     *                 should implement {@link ParentResource}
     */
    public RootResourceNode(final String id, final ParentResource resource) {
        this.id = id;
        this.resource = resource;
        this.children = new HashSet<>();
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

    // Collection

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return children.contains(o);
    }

    @Override
    public Iterator<TreeNode<Resource>> iterator() {
        return (Iterator) children.iterator();
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(final T[] ts) {
        return (T[]) toArray();
    }

    @Override
    public boolean add(final TreeNode<Resource> resourceTreeNode) {
        if (resourceTreeNode instanceof ChildNode && resourceTreeNode.get() instanceof ChildResource) {
            return children.add((ChildNode) resourceTreeNode);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean remove(final Object o) {
        return children.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> collection) {
        return children.containsAll(collection);
    }

    @Override
    public boolean addAll(final Collection<? extends TreeNode<Resource>> collection) {
        return collection.stream()
            .map(this::add)
            .reduce(Boolean::logicalOr)
            .orElse(false);
    }

    @Override
    public boolean removeAll(final Collection<?> collection) {
        return collection.stream()
            .map(this::remove)
            .reduce(Boolean::logicalOr)
            .orElse(false);
    }

    @Override
    public boolean retainAll(final Collection<?> collection) {
        return children.retainAll(collection);
    }

    @Override
    public void clear() {
        children.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.prettyprint(sb::append, 0);
        return sb.toString();
    }
}
