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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings({"NullableProblems", "unchecked", "rawtypes"})
public interface ParentNode<T> extends TreeNode<T> {
    Collection<ChildNode<T>> getChildren();

    @Override
    default Stream<T> traverse() {
        return Stream.concat(
                Stream.of(this.get()),
                this.getChildren().stream().flatMap(TreeNode::traverse)
        );
    }

    @Override
    default int size() {
        return getChildren().size();
    }

    @Override
    default boolean isEmpty() {
        return getChildren().isEmpty();
    }

    @Override
    default boolean contains(final Object o) {
        return getChildren().contains(o);
    }

    @Override
    default Iterator<TreeNode<T>> iterator() {
        return (Iterator) getChildren().iterator();
    }

    @Override
    default Object[] toArray() {
        return new Object[0];
    }

    @Override
    default <T> T[] toArray(final T[] ts) {
        return (T[]) toArray();
    }

    @Override
    default boolean add(final TreeNode<T> resourceTreeNode) {
        if (resourceTreeNode instanceof ChildNode) {
            return getChildren().add((ChildNode) resourceTreeNode);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    default boolean remove(final Object o) {
        return getChildren().remove(o);
    }

    @Override
    default boolean containsAll(final Collection<?> collection) {
        return getChildren().containsAll(collection);
    }

    @Override
    default boolean addAll(final Collection<? extends TreeNode<T>> collection) {
        return collection.stream()
                .map(this::add)
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    @Override
    default boolean removeAll(final Collection<?> collection) {
        return collection.stream()
                .map(this::remove)
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    @Override
    default boolean retainAll(final Collection<?> collection) {
        return getChildren().retainAll(collection);
    }

    @Override
    default void clear() {
        getChildren().clear();
    }

    @Override
    default void prettyprint(final Consumer<String> printer, final int indent) {
        printer.accept(String.join("", Collections.nCopies(indent, "\t")) + getId() + "\n");
        getChildren().forEach(child -> child.prettyprint(printer, indent + 1));
    }
}
