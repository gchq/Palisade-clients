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
    /**
     * Get the identifier for this node. This might be eg. a file name.
     * This is similar to a Map of Strings to Ts.
     *
     * @return the node's identifier
     */
    String getId();

    /**
     * Get the content of this node, an element of the collection
     *
     * @return the object at this node
     */
    T get();

    /**
     * Get all objects at this node and below.
     * Order does not matter.
     *
     * @return a stream of objects as the tree is traversed
     */
    Stream<T> traverse();

    /**
     * Try to print this node and its children in a human-readable manner.
     *
     * @param printer the logger for the string values
     * @param indent  the depth of indent, proportional to the depth of the node printed in the tree
     */
    default void prettyprint(final Consumer<String> printer, final int indent) {
        printer.accept(String.join("", Collections.nCopies(indent, "\t")) + getId() + "\n");
    }
}
