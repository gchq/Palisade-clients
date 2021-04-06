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

@SuppressWarnings({"NullableProblems", "unchecked", "rawtypes", "SimplifyStreamApiCallChains"})
public class ResourceTree implements Collection<Resource> {
    RootResourceNode root;

    public static TreeNode<Resource> create(final ParentNode<Resource> parent, final Resource resource) {
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

    public static List<String> getPath(final String path) {
        String strippedPath = path
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
        return "".equals(strippedPath)
                ? List.of()
                : List.of(strippedPath.split("/"));
    }

    public static List<String> getPath(final Resource resource) {
        return getPath(resource.getId());
    }

    public Optional<TreeNode<Resource>> getNode(final List<String> idPath) {
        return getNode(root, idPath);
    }

    public Optional<TreeNode<Resource>> getNode(final String path) {
        return getNode(getPath(path));
    }

    public Optional<TreeNode<Resource>> getNode(final Resource resource) {
        return getNode(getPath(resource));
    }

    private <T> List<T> dropFirst(final List<T> list) {
        return list.size() > 1
                ? list.subList(1, list.size())
                : List.of();
    }

    private Optional<TreeNode<Resource>> getNode(final TreeNode<Resource> node, final List<String> path) {
        if (node == null) {
            return Optional.empty();
        } else if (path.isEmpty()) {
            return Optional.of(node);
        } else if (node instanceof ParentNode) {
            return ((ParentNode<Resource>) node).getChildren()
                    .stream()
                    .filter(child -> child.getId().equals(path.get(0)))
                    .map(child -> getNode(child, dropFirst(path)))
                    .flatMap(Optional::stream)
                    .findAny();
        } else {
            // Cannot subpath a non-parent (ie. file)
            return Optional.empty();
        }
    }

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

    @Override
    public boolean add(final Resource resource) {
        if (getNode(resource).isEmpty()) {
            if (resource instanceof ChildResource) {
                ParentResource parent = ((ChildResource) resource).getParent();
                add(parent);
                getNode(parent)
                        .ifPresent(node -> node
                                .add(create((ParentNode) node, resource)));
            } else if (resource instanceof ParentResource && root == null) {
                    root = (RootResourceNode) create(null, resource);
            }
            return true;
        }
        return false;
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
