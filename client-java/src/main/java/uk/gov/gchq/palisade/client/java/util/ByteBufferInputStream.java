/*
 * Copyright 2020 Crown Copyright
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
package uk.gov.gchq.palisade.client.java.util;

import io.reactivex.Flowable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Objects;

import static java.lang.System.arraycopy;

/**
 * A simple InputStream which uses ByteBuffers as it's backing store.
 * <P>
 * The only IOException should come if the InputStream has been closed. All
 * other IOException should not occur because all the data is local. Data reads
 * on an exhausted ByteBuffer returns a -1.
 */
public class ByteBufferInputStream extends InputStream {

    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private static final String CLOSED_STREAM_MSG = "tried to access to closed stream";

    private final Flowable<ByteBuffer> flowable;
    private Iterable<ByteBuffer> iterable;
    private Iterator<ByteBuffer> iterator;

    private ByteBuffer buff = EMPTY_BUFFER;
    private boolean closed = false;

    /**
     * Returns a newly created {@code ByteBufferInputStream} with the provided
     * {@code Flowable}
     *
     * @param flowable The {@code Flowable} of bytebuffers to be consumed by this
     *                 {@code InputStream}
     */
    public ByteBufferInputStream(final Flowable<ByteBuffer> flowable) {
        this.flowable = flowable;
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public byte[] readNBytes(final int len) throws IOException {
        var bytes1 = new byte[len];
        var actual = read(bytes1);
        if (actual < len) {
            var bytes2 = new byte[actual];
            arraycopy(bytes1, 0, bytes2, 0, actual);
            return bytes2;
        }
        return bytes1;
    }

    @Override
    public int readNBytes(final byte[] b, final int off, final int len) throws IOException {
        return read(b, off, len);
    }


    @Override
    public int read() throws IOException {
        if (closed) {
            throw new IOException(CLOSED_STREAM_MSG);
        }
        var bb = buffer();
        if (bb.remaining() == 0) {
            return -1;
        }
        return bb.get() & 0xff;
    }

    @Override
    public int read(final byte[] ba) throws IOException {
        return read(ba, 0, ba.length);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {

        if (closed) {
            throw new IOException(CLOSED_STREAM_MSG);
        }

        Objects.requireNonNull(b);

        if (b.length == 0 || len == 0) {
            return 0;
        }

        var bb = buffer();

        if (!bb.hasRemaining()) {
            return -1;
        }

        int l = len; // bytes left
        int o = off; // relative offset
        int c = 0; // current byte count

        while (l > 0) {
            var rem = bb.remaining();

            if (rem == 0) {
                assert c + l == len : "should have no bytes left, but have " + l;
                return c;
            }

            if (rem > l) {
                bb.get(b, o, l);
                return c + l;
            }

            bb.get(b, o, rem);
            assert bb.remaining() == 0 : "must have no bytes left in current bytebuffer";
            o += rem;
            l -= rem;
            c += rem;

            bb = buffer();
        }

        return c;

    }

    @Override
    public long skip(final long n) throws IOException {

        if (closed) {
            throw new IOException("tried to access to closed stream");
        }

        if (n <= 0) {
            return 0;
        }

        var bb = buffer();

        if (!bb.hasRemaining()) {
            return 0;
        }

        int l = (int) n; // remaining
        int c = 0; // skipped

        while (l > 0) {
            var rem = bb.remaining();
            if (rem > l) {
                bb.position(bb.position() + l);
                return c + l;
            }
            this.buff = ByteBuffer.allocate(0);
            assert bb.remaining() == 0 : "must have no bytes left in current bytebuffer";
            c += rem;
            bb = buffer();
        }

        return c;
    }

    @Override
    public int available() throws IOException {
        if (closed) {
            return 0;
        }
        return buffer().remaining();
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    /**
     * Marking is not supported, so this method is a no-op
     */
    @Override
    public synchronized void mark(final int readlimit) {
        /* noop */ }

    /**
     * Throws {@code IOException} as marking is not supported
     */
    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    /**
     * Returns false as this implementation does not support marking
     *
     * @return false
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    private ByteBuffer buffer() {
        if (iterator == null) {
            this.iterable = flowable.blockingIterable();
            this.iterator = iterable.iterator();
        }
        if (buff == null || buff.remaining() == 0) {
            if (buff != null) {
                // TODO: This is where we should be releasing the buffers memory allocation, but
                // there does not seem to be a way to do so.
            }
            if (iterator.hasNext()) {
                buff = iterator.next();
            }
        }
        return buff;
    }

}