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
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Node of a {@link ResourceTree}, where each node is a collection of its children.
 *
 * @param <T> the type of an object contained within the node
 */
public interface TreeNode<T> extends Collection<TreeNode<T>> {
    String getId();

    T get();

    Stream<T> traverse();

    default void prettyprint(final Consumer<String> printer, final int indent) {
        printer.accept(String.join("", Collections.nCopies(indent, "\t")) + getId() + "\n");
    }
}
