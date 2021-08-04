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

import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.resource.impl.SystemResource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceTreeTest {
    @SuppressWarnings({"SimplifyStreamApiCallChains"})
    @Test
    void testTreeBuildsCorrectly() {
        ResourceTree tree = new ResourceTree();

        ParentResource root = new SystemResource()
            .id("file:/");
        ParentResource some = new DirectoryResource()
            .id("file:/some/");
        LeafResource file1 = new FileResource()
            .id("file:/some/file1")
            .type("type")
            .serialisedFormat("format")
            .connectionDetail(new SimpleConnectionDetail().serviceName("data-service"));
        LeafResource file2 = new FileResource()
            .id("file:/some/file2")
            .type("type")
            .serialisedFormat("format")
            .connectionDetail(new SimpleConnectionDetail().serviceName("data-service"));

        tree.add(file1);

        assertThat(tree.stream().collect(Collectors.toSet()))
            .as("Tree stream should contain all (three) elements added to the tree")
            .isEqualTo(Set.of(root, some, file1));

        tree.add(file2);

        assertThat(tree.stream().collect(Collectors.toSet()))
            .as("Tree stream should contain all (four) elements added to the tree")
            .isEqualTo(Set.of(root, some, file1, file2));
    }
}
