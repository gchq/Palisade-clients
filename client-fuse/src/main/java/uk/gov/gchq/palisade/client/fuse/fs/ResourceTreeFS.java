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

package uk.gov.gchq.palisade.client.fuse.fs;

import jnr.ffi.Platform;
import jnr.ffi.Pointer;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;
import ru.serce.jnrfuse.struct.Statvfs;

import uk.gov.gchq.palisade.client.fuse.tree.ParentNode;
import uk.gov.gchq.palisade.client.fuse.tree.ResourceTree;
import uk.gov.gchq.palisade.client.fuse.tree.TreeNode;
import uk.gov.gchq.palisade.client.fuse.tree.impl.LeafResourceNode;
import uk.gov.gchq.palisade.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static jnr.ffi.Platform.OS.WINDOWS;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ResourceTreeFS extends FuseStubFS {

    private int getattr(final TreeNode<Resource> node, final FileStat stat) {
        final int readOnly = FileStat.S_IRUSR | FileStat.S_IRGRP; // ug+r
        if (node instanceof ParentNode) {
            stat.st_mode.set(FileStat.S_IFDIR | readOnly);
            stat.st_uid.set(getContext().uid.get());
            stat.st_gid.set(getContext().gid.get());
            return 0;
        } else if (node instanceof LeafResourceNode) {
            stat.st_mode.set(FileStat.S_IFREG | readOnly);
            stat.st_size.set(Integer.MAX_VALUE); // unknown
            stat.st_uid.set(getContext().uid.get());
            stat.st_gid.set(getContext().gid.get());
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }

    private int read(final LeafResourceNode node, final Pointer buffer, final long size, final long offset) {
        InputStream is = reader.apply(node);
        byte[] buf = new byte[(int) size];
        try {
            int bytesRead = is.readNBytes(buf, (int) offset, (int) size);
            buffer.put(0, buf, 0, bytesRead);
            return bytesRead;
        } catch (IOException ex) {
            return -ErrorCodes.EREMOTEIO();
        }
    }

    private void readdir(final ParentNode<Resource> node, final Pointer buf, final FuseFillDir filler) {
        node.getChildren()
            .forEach(child ->
                filler.apply(buf, child.getId(), null, 0));
    }

    private ResourceTree resourceTree;
    private Function<LeafResourceNode, InputStream> reader;

    public ResourceTreeFS(final ResourceTree resourceTree, final Function<LeafResourceNode, InputStream> reader) {
        this.resourceTree = resourceTree;
        this.reader = reader;
    }

    @Override
    public int getattr(final String path, final FileStat stat) {
        return resourceTree.getNode(path)
            .map(node -> getattr(node, stat))
            .orElse(-ErrorCodes.ENOENT());
    }

    @Override
    public int read(final String path, final Pointer buf, @size_t final long size, @off_t final long offset, final FuseFileInfo fi) {
        TreeNode<Resource> node = resourceTree.getNode(path).orElse(null);
        if (node == null) {
            return -ErrorCodes.ENOENT();
        } else if (!(node instanceof LeafResourceNode)) {
            return -ErrorCodes.EISDIR();
        } else {
            return read((LeafResourceNode) node, buf, size, offset);
        }
    }

    @Override
    public int readdir(final String path, final Pointer buf, final FuseFillDir filter, @off_t final long offset, final FuseFileInfo fi) {
        TreeNode<Resource> node = resourceTree.getNode(path).orElse(null);
        if (node == null) {
            return -ErrorCodes.ENOENT();
        } else if (!(node instanceof ParentNode)) {
            return -ErrorCodes.ENOTDIR();
        } else {
            filter.apply(buf, ".", null, 0);
            filter.apply(buf, "..", null, 0);
            readdir((ParentNode) node, buf, filter);
            return 0;
        }
    }


    @Override
    public int statfs(final String path, final Statvfs stbuf) {
        if (Platform.getNativePlatform().getOS() == WINDOWS && "/".equals(path)) {
            // statfs needs to be implemented on Windows in order to allow for copying
            // data from other devices because winfsp calculates the volume size based
            // on the statvfs call.
            stbuf.f_blocks.set(1024 * 1024); // total data blocks in file system
            stbuf.f_frsize.set(1024);        // fs block size
            stbuf.f_bfree.set(1024 * 1024);  // free blocks in fs
        }
        return super.statfs(path, stbuf);
    }

}

