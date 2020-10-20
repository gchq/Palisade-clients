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
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ByteBufferInputStreamTest {

    private ByteBufferInputStream is;

    @BeforeEach
    void setUp() {
        ByteBuffer a = ByteBuffer.wrap("abcd".getBytes());
        ByteBuffer b = ByteBuffer.wrap("efgh".getBytes());
        ByteBuffer c = ByteBuffer.wrap("ijkl".getBytes());
        ByteBuffer d = ByteBuffer.wrap("mnop".getBytes());
        ByteBuffer e = ByteBuffer.wrap("qrst".getBytes());
        ByteBuffer f = ByteBuffer.wrap("uvwx".getBytes());
        ByteBuffer g = ByteBuffer.wrap("yz".getBytes());

        Flowable<ByteBuffer> flowable = Flowable
                .fromIterable(List.of(a, b, c, d, e, f, g))
                .observeOn(Schedulers.io());

        is = new ByteBufferInputStream(flowable);
    }

    @Test
    void testReadByteArray4Chars() throws Exception {
        byte[] ba = new byte[4];
        int read = is.read(ba);
        assertThat(read).isEqualTo(4);
        assertThat(new String(ba)).isEqualTo("abcd");
    }

    @Test
    void testReadByteArray8Chars() throws Exception {
        byte[] ba = new byte[8];
        int read = is.read(ba);
        assertThat(read).isEqualTo(8);
        assertThat(new String(ba)).isEqualTo("abcdefgh");
    }

    @Test
    void testReadByteArray30Chars() throws Exception {
        byte[] ba = new byte[30];
        int read = is.read(ba);
        assertThat(read).isEqualTo(26);
        assertThat(new String(ba, 0, read)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    @Test
    void testReadByteArray2Chars() throws Exception {
        byte[] ba = new byte[30];
        int read = is.read(ba);
        assertThat(read).isEqualTo(26);
        assertThat(new String(ba, 0, read)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    @Test
    void testReadByteArrayWithOffsetAndLengthOf4() throws Exception {
        byte[] ba = "11111111".getBytes();
        int read = is.read(ba, 4, 4);
        assertThat(read).isEqualTo(4);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("1111abcd");

    }

    @Test
    void testReadByteArrayWithOffsetAndLengthOf30() throws Exception {
        byte[] ba = "111111111111111111111111111111".getBytes();
        int read = is.read(ba, 0, 30);
        assertThat(read).isEqualTo(26);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklmnopqrstuvwxyz1111");

    }

    @Test
    void testSkipSingleBuffer() throws Exception {
        long skipped = is.skip(2);
        assertThat(skipped).isEqualTo(2);
        byte[] ba = "11".getBytes();
        int read = is.read(ba, 0, 2);
        assertThat(read).isEqualTo(2);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("cd");
    }

    @Test
    void testSkipAcrossBuffers() throws Exception {
        long skipped = is.skip(2);
        assertThat(skipped).isEqualTo(2);
        byte[] ba = "1111".getBytes();
        int read = is.read(ba, 0, 4);
        assertThat(read).isEqualTo(4);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("cdef");
    }

    @Test
    void testReadAll() throws Exception {
        byte[] ba = is.readAllBytes();
        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    @Test
    void testReadNBytes() throws Exception {
        byte[] ba = is.readNBytes(13);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklm");
    }

    @Test
    void testReadNBytesAll() throws Exception {
        byte[] ba = is.readNBytes(26);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    @Test
    void testReadNBytesAllExtra() throws Exception {
        byte[] ba = is.readNBytes(27);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    @Test
    void testAvailable() throws Exception {
        assertThat(is.available()).isEqualTo(4);
    }

    @Test
    void testClose() throws Exception {
        is.close();
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.read());
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.read(new byte[8]));
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.read(new byte[8], 0, 4));
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.read(new byte[8], 0, 4));
    }

    @Test
    void testReset() {
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.reset());
    }

    @Test
    void testMarkSupported() {
        assertThat(is.markSupported()).isFalse();
    }

}
