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

package uk.gov.gchq.palisade.client.fuse.tree;

import uk.gov.gchq.palisade.client.fuse.tree.impl.BranchResourceNode;
import uk.gov.gchq.palisade.client.fuse.tree.impl.LeafResourceNode;
import uk.gov.gchq.palisade.client.fuse.tree.impl.RootResourceNode;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link ResourceTree} is a {@link Collection} of {@link Resource}s, structured as a tree.
 * The tree points to a {@link RootResourceNode}, and traverses the tree to implement the collection
 * methods.
 * Each node of the tree is analogous to the type of the resource it holds - eg. {@link LeafResourceNode}s
 * hold {@link LeafResource}s.
 */
// Non-null collections api
// Unchecked typecasts and raw types associated with .toArray()
// Override .stream() and use it for implementing other methods
@SuppressWarnings({"NullableProblems", "unchecked", "rawtypes", "SimplifyStreamApiCallChains"})
public class ResourceTree implements Collection<Resource> {
    protected RootResourceNode root;

    /**
     * Create a new node of the tree and link it to a (grand-)parent.
     * This is equivalent to adding a resource to the collection.
     *
     * @param parent   the (grand-)parent node to attach this resource to
     * @param resource the resource to add
     * @return a new {@link TreeNode} for the added resource
     */
    private static TreeNode<Resource> createNode(final ParentNode<Resource> parent, final Resource resource) {
        List<String> pathComponents = getPath(resource.getId());
        String id = pathComponents.isEmpty()
            ? ""
            : pathComponents.get(pathComponents.size() - 1);

        TreeNode<Resource> node;
        if (resource instanceof LeafResource) {
            node = new LeafResourceNode(id, (ParentNode) parent, (LeafResource) resource);
        } else if (resource instanceof ParentResource && resource instanceof ChildResource) {
            node = new BranchResourceNode(id, (ParentNode) parent, (ChildResource) resource);
        } else if (resource instanceof ParentResource) {
            node = new RootResourceNode(id, (ParentResource) resource);
        } else {
            throw new IllegalArgumentException(resource.getClass().getName() + " is not a valid type");
        }

        if (parent != null) {
            parent.add(node);
        }
        return node;
    }

    /**
     * Format a String-based path into a list of path components.
     * These are used to select children by name while traversing the tree.
     *
     * @param path a path for a tree node, using forward-slash path separators
     * @return a list of names to select for (grand-)children to reach the node
     * in the tree
     */
    private static List<String> getPath(final String path) {
        String strippedPath = path
            .replaceAll("^/+", "")
            .replaceAll("/+$", "");
        return "".equals(strippedPath)
            ? List.of()
            : List.of(strippedPath.split("/"));
    }

    private static List<String> getPath(final Resource resource) {
        return getPath(resource.getId());
    }

    private Optional<TreeNode<Resource>> getNode(final List<String> idPath) {
        return getNode(root, idPath);
    }

    /**
     * Get a node in the tree by path to the node.
     *
     * @param path the path to the node, using forward-slash separators
     * @return the node if it was found in the tree, {@link Optional#empty()} otherwise
     */
    public Optional<TreeNode<Resource>> getNode(final String path) {
        return getNode(getPath(path));
    }

    private Optional<TreeNode<Resource>> getNode(final Resource resource) {
        return getNode(getPath(resource));
    }

    // Remove the first item of a list
    private <T> List<T> dropFirst(final List<T> list) {
        return list.size() > 1
            ? list.subList(1, list.size())
            : List.of();
    }

    // Given a current node and list of child traversals, recursively get the next child in the list
    private Optional<TreeNode<Resource>> getNode(final TreeNode<Resource> node, final List<String> path) {
        if (node == null) {
            // If we've hit a null node, we're not going to find anything
            return Optional.empty();
        } else if (path.isEmpty()) {
            // Non-null node and no more path to traverse
            return Optional.of(node);
        } else if (node instanceof ParentNode) {
            // Not the node we're looking for, but it has children and we have more path values to traverse
            return ((ParentNode<Resource>) node).getChildren()
                .stream()
                // Get children with the correct name for the next segment of the path
                .filter(child -> child.getId().equals(path.get(0)))
                // Drop the now-unnecessary first item off the path list when recursing
                .map(child -> getNode(child, dropFirst(path)))
                .flatMap(Optional::stream)
                // Convert stream to optional
                .findAny();
        } else {
            // We've got to something that has no children, but there's still path values to traverse
            // Cannot subpath a non-parent (ie. file)
            return Optional.empty();
        }
    }

    /* Collections API */

    @Override
    public int size() {
        return (int) root.traverse().count();
    }

    @Override
    public boolean isEmpty() {
        return root.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return root.traverse().anyMatch(resource -> resource.equals((o)));
    }

    @Override
    public Stream<Resource> stream() {
        return root.traverse();
    }

    @Override
    public Iterator<Resource> iterator() {
        return stream().iterator();
    }

    @Override
    public Object[] toArray() {
        return stream().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] ts) {
        return (T[]) toArray();
    }

    /**
     * In order to successfully add a resource, we also have to add any missing (grand-)parents
     * for that resource too. We also have to traverse the tree to find out where to add the
     * new (child) resource, or consider the special case where the tree is empty and we're
     * having to add a new root.
     *
     * @param resource the resource to add to the tree
     * @return true if the tree was modified, false if it was left unchanged
     */
    @Override
    public boolean add(final Resource resource) {
        if (getNode(resource).isEmpty()) {
            // If this resource does not exist in the tree, add it
            if (resource instanceof ChildResource) {
                // Resource is not a a new root node
                ParentResource parent = ((ChildResource) resource).getParent();
                // Add any missing parents (recursively)
                this.add(parent);
                // Add this node as a child of its parent
                return this.getNode(parent)
                    .map(node -> node.add(ResourceTree.createNode((ParentNode) node, resource)))
                    .orElseThrow(() -> new IllegalStateException("Failed to find parent node after adding it to the tree"));
            } else if (resource instanceof ParentResource && root == null) {
                // Resource is a new root node
                root = (RootResourceNode) createNode(null, resource);
                return true;
            } else {
                // Resource requires a new root node, but one already exists
                throw new IllegalStateException("Adding resource required a new root node, but one already exists");
            }
        } else {
            // If the resource already exists, we're done
            return false;
        }
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(final Collection<?> collection) {
        return collection.stream().anyMatch(this::contains);
    }

    @Override
    public boolean addAll(final Collection<? extends Resource> collection) {
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
        throw new UnsupportedOperationException();
    }
}
