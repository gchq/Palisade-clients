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

/**
 * Node of a tree which has a parent.
 * This is analogous to a {@link uk.gov.gchq.palisade.resource.ChildResource}
 *
 * @param <T> the type of elements in the tree
 */
public interface ChildNode<T> extends TreeNode<T> {
    ParentNode<T> getParent();
}
