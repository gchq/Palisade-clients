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

import jnr.ffi.Pointer;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseContext;
import ru.serce.jnrfuse.struct.FuseFileInfo;
import ru.serce.jnrfuse.struct.Statvfs;

import uk.gov.gchq.palisade.client.fuse.tree.ParentNode;
import uk.gov.gchq.palisade.client.fuse.tree.ResourceTree;
import uk.gov.gchq.palisade.client.fuse.tree.TreeNode;
import uk.gov.gchq.palisade.client.fuse.tree.impl.LeafResourceNode;
import uk.gov.gchq.palisade.resource.AbstractLeafResource;
import uk.gov.gchq.palisade.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of a FuseFS mountable using a {@link ResourceTree} to populate directories.
 * This uses the stubbed methods from {@link FuseStubFS} where specialisation is not required.
 * This is where resources (returned from the Filtered-Resource Service) are mounted and presented
 * to the user.
 * The API was very much created with C in mind, so there's a lot of setting fields on mutable
 * objects and returning {@link ErrorCodes} integers from methods.
 *
 * @see <a href="https://man7.org/linux/man-pages/man8/mount.fuse3.8.html">mount.fuse3</a>
 */
// Unchecked casts with instanceof
// Raw types with instanceof
// Duplicated code for getting tree node
@SuppressWarnings({"unchecked", "rawtypes", "DuplicatedCode"})
public class ResourceTreeFS extends FuseStubFS {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTreeFS.class);

    private ResourceTree resourceTree;
    private Function<LeafResourceNode, InputStream> reader;

    /**
     * Construct an instance of the FuseFS implementation, given a mutable tree which will be
     * updated with new resources, and a function to read the data from nodes of the tree.
     *
     * @param resourceTree a tree collection that will be used for directory listings
     * @param reader       a function for acquiring an {@link InputStream} from a tree node,
     *                     used for reading files
     */
    // We actively want the resourceTree collection to be mutable
    // The structure should allow for creation of resources asynchronously to the fs mount
    @SuppressWarnings("java:S2384")
    public ResourceTreeFS(final ResourceTree resourceTree, final Function<LeafResourceNode, InputStream> reader) {
        this.resourceTree = resourceTree;
        this.reader = reader;
    }

    private static int getattr(final TreeNode<Resource> node, final FuseContext ctx, final FileStat stat) {
        final int readOnly = FileStat.S_IRUSR | FileStat.S_IRGRP; // ug+r
        if (node instanceof ParentNode) {
            // Directory, read-only
            stat.st_mode.set(FileStat.S_IFDIR | readOnly);
            // Set user-id and group-id from fuse context (whoever mounted)
            stat.st_uid.set(ctx.uid.get());
            stat.st_gid.set(ctx.gid.get());
            return 0;
        } else if (node instanceof LeafResourceNode) {
            // Regular file, read-only
            stat.st_mode.set(FileStat.S_IFREG | readOnly);
            // unknown file-size so just go for something big
            stat.st_size.set(Integer.MAX_VALUE);
            // Set user-id and group-id from fuse context (whoever mounted)
            stat.st_uid.set(ctx.uid.get());
            stat.st_gid.set(ctx.gid.get());
            return 0;
        } else {
            return -ErrorCodes.ENOENT();
        }
    }

    private static int getxattr(final AbstractLeafResource resource, final String name, final Pointer value) {
        Object attribute = resource.getAttribute(name);
        if (attribute != null) {
            byte[] buf = attribute.toString().getBytes();
            value.put(0L, buf, 0, buf.length);
            return 0;
        } else {
            // Attribute does not exist on object
            return -ErrorCodes.ENODATA();
        }
    }

    private static int listxattr(final AbstractLeafResource resource, final Pointer list, final long size) {
        // Fill buffer with null-terminated string from map key-set, if there's space
        byte[] buf = String.join("\0", resource.getAttributes().keySet()).getBytes();
        if (buf.length <= size) {
            list.put(0L, buf, 0, buf.length);
            return 0;
        } else {
            return -ErrorCodes.E2BIG();
        }
    }

    private static int read(final InputStream is, final Pointer buffer, final long size, final long offset) {
        // Fill buffer with bytes from input stream
        try {
            byte[] buf = new byte[(int) size];
            int bytesRead = is.readNBytes(buf, (int) offset, (int) size);
            buffer.put(0, buf, 0, bytesRead);
            return bytesRead;
        } catch (IOException ex) {
            LOGGER.warn("Failed to read (remote) input-stream", ex);
            return -ErrorCodes.EREMOTEIO();
        }
    }

    private static int readdir(final ParentNode<Resource> node, final Pointer buf, final FuseFillDir filler, final long offset) {
        Stream<String> nodeNames = Stream.concat(Stream.of(".", ".."), node.getChildren().stream().map(TreeNode::getId));
        // Fill buffer with child resource id's using filter function
        for (String childName : nodeNames.skip(offset).collect(Collectors.toList())) {
            // Put the childName into the buffer, returns 1 if the buffer is full
            if (filler.apply(buf, childName, null, 0) == 1) {
                // Stop if the buffer is full (~32k entities)
                return 1;
            }
        }
        return 0;
    }

    /**
     * Get the UNIX FS node attributes for a {@link TreeNode}.
     *
     * @param path the path to a node in the tree to generate filesystem attributes for
     * @param stat mutable object to assign attributes into
     * @return an {@link ErrorCodes} error-code
     * @see <a href="https://man7.org/linux/man-pages/man2/stat.2.html">stat</a>
     */
    @Override
    public int getattr(final String path, final FileStat stat) {
        return resourceTree.getNode(path)
            .map(node -> getattr(node, this.getContext(), stat))
            .orElse(-ErrorCodes.ENOENT());
    }

    /**
     * Read a filesystem node by copying bytes from the {@link InputStream} into the buffer.
     *
     * @param path   the path to a (file) node in the tree to read
     * @param buf    the native buffer to write bytes into
     * @param size   the requested number of bytes to write into the buffer
     * @param offset the offset in the file to start from
     * @return an {@link ErrorCodes} error-code
     * @see <a href="https://man7.org/linux/man-pages/man2/read.2.html">read</a>
     */
    @Override
    public int read(final String path, final Pointer buf, @size_t final long size, @off_t final long offset, final FuseFileInfo fi) {
        TreeNode<Resource> node = resourceTree.getNode(path).orElse(null);
        if (node == null) {
            // No entity (tree node) exists for the given path
            return -ErrorCodes.EBADF();
        } else if (node instanceof LeafResourceNode) {
            // Read the file if it's a leaf
            return read(reader.apply((LeafResourceNode) node), buf, size, offset);
        } else {
            // Can't read directories
            return -ErrorCodes.EISDIR();
        }
    }

    /**
     * Read a filesystem node by copying bytes from the {@link InputStream} into the buffer.
     *
     * @param path   the path to a (file) node in the tree to read
     * @param buf    the native buffer to write bytes into
     * @param offset the offset in the file to start from
     * @return an {@link ErrorCodes} error-code
     * @see <a href="https://man7.org/linux/man-pages/man2/readdir.2.html">readdir</a>
     */
    @Override
    public int readdir(final String path, final Pointer buf, final FuseFillDir filter, @off_t final long offset, final FuseFileInfo fi) {
        TreeNode<Resource> node = resourceTree.getNode(path).orElse(null);
        if (node == null) {
            // No entity (tree node) exists for the given path
            return -ErrorCodes.EBADF();
        } else if (node instanceof ParentNode) {
            // Read the directory if it's a parent
            return readdir((ParentNode) node, buf, filter, offset);
        } else {
            // Can't readdir files
            return -ErrorCodes.ENOTDIR();
        }
    }

    /**
     * Stat the mounted filesystem, report that it is read-only.
     *
     * @param path  the path to a (file) node in the tree to read
     * @param stbuf the native object to set flags etc. on
     * @return an {@link ErrorCodes} error-code
     * @see <a href="https://man7.org/linux/man-pages/man2/statsf.2.html">statsf</a>
     */
    @Override
    public int statfs(final String path, final Statvfs stbuf) {
        // Mount read-only filesystem
        stbuf.f_flag.set(Statvfs.ST_RDONLY);
        return 0;
    }

    /**
     * Get an extended attribute by name on a file.
     * Only file nodes (leaf resources) have attributes
     *
     * @param path  the path to a node in the tree to read extended attributes for
     * @param name  the name of a single attribute on the node
     * @param value the native buffer to fill with the attribute's value
     * @param size  space available in the buffer
     * @return an {@link ErrorCodes} error-code
     * @see <a href="https://man7.org/linux/man-pages/man2/getxattr.2.html">getxattr</a>
     */
    @Override
    public int getxattr(final String path, final String name, final Pointer value, final long size) {
        TreeNode<Resource> node = resourceTree.getNode(path).orElse(null);
        if (node == null) {
            // No entity (tree node) exists for the given path
            return -ErrorCodes.ENOENT();
        } else if (node.get() instanceof AbstractLeafResource) {
            // Get an attribute on a file
            return getxattr((AbstractLeafResource) node.get(), name, value);
        } else {
            // At the moment, Palisade only has attributes on leaf resources
            return 0;
        }
    }

    /**
     * Get all extended attribute names on a file.
     * Only file nodes (leaf resources) have attributes
     *
     * @param path the path to a node in the tree to read extended attributes for
     * @param list the native buffer to fill with all attributes' values, formatted
     *             as concat'ed null-terminated strings
     * @param size space available in the buffer
     * @return an {@link ErrorCodes} error-code
     * @see <a href="https://man7.org/linux/man-pages/man2/listxattr.2.html">listxattr</a>
     */
    @Override
    public int listxattr(final String path, final Pointer list, final long size) {
        TreeNode<Resource> node = resourceTree.getNode(path).orElse(null);
        if (node == null) {
            // No entity (tree node) exists for the given path
            return -ErrorCodes.ENOENT();
        } else if (node.get() instanceof AbstractLeafResource) {
            return listxattr((AbstractLeafResource) node.get(), list, size);
        } else {
            // At the moment, Palisade only has attributes on leaf resources
            return 0;
        }
    }
}
