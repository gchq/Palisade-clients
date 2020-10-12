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
package uk.gov.gchq.palisade.client.java.download;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;

/**
 * @author dbell
 *
 */
class ByteBufferInputStreamTest {

    private ByteBufferInputStream is;
    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {

        var a = ByteBuffer.wrap("abcd".getBytes());
        var b = ByteBuffer.wrap("efgh".getBytes());
        var c = ByteBuffer.wrap("ijkl".getBytes());
        var d = ByteBuffer.wrap("mnop".getBytes());
        var e = ByteBuffer.wrap("qrst".getBytes());
        var f = ByteBuffer.wrap("uvwx".getBytes());
        var g = ByteBuffer.wrap("yz".getBytes());

        var flowable = Flowable
            .fromIterable(List.of(a, b, c, d, e, f, g))
            .observeOn(Schedulers.io());

        is = new ByteBufferInputStream(flowable);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    void tearDown() throws Exception {
    }

    /**
     * Test method for {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#read()}.
     */
    @Test
    void test_read_bytearray_4_chars() throws Exception {
        var ba = new byte[4];
        int read = is.read(ba);
        assertThat(read).isEqualTo(4);
        assertThat(new String(ba)).isEqualTo("abcd");
    }

    /**
     * Test method for
     * {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#read()}.
     */
    @Test
    void test_read_bytearray_8_chars() throws Exception {
        var ba = new byte[8];
        int read = is.read(ba);
        assertThat(read).isEqualTo(8);
        assertThat(new String(ba)).isEqualTo("abcdefgh");
    }

    /**
     * Test method for
     * {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#read()}.
     */
    @Test
    void test_read_bytearray_30_chars() throws Exception {
        var ba = new byte[30];
        int read = is.read(ba);
        assertThat(read).isEqualTo(26);
        assertThat(new String(ba, 0, read)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    /**
     * Test method for
     * {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#read()}.
     */
    @Test
    void test_read_bytearray_26_chars() throws Exception {
        var ba = new byte[30];
        int read = is.read(ba);
        assertThat(read).isEqualTo(26);
        assertThat(new String(ba, 0, read)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }


    /**
     * Test method for {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#read(byte[], int, int)}.
     */
    @Test
    void test_read_bytearray_with_offset_and_length_of_4() throws Exception {
        var ba = "11111111".getBytes();
        int read = is.read(ba, 4, 4);
        assertThat(read).isEqualTo(4);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("1111abcd");

    }

    /**
     * Test method for
     * {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#read(byte[], int, int)}.
     */
    @Test
    void test_read_bytearray_with_offset_and_length_of_30() throws Exception {
        var ba = "111111111111111111111111111111".getBytes();
        int read = is.read(ba, 0, 30);
        assertThat(read).isEqualTo(26);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklmnopqrstuvwxyz1111");

    }

    /**
     * Test method for
     * {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#skip(long)}.
     */
    @Test
    void test_skip_single_buffer() throws Exception {
        long skipped = is.skip(2);
        assertThat(skipped).isEqualTo(2);
        var ba = "11".getBytes();
        int read = is.read(ba, 0, 2);
        assertThat(read).isEqualTo(2);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("cd");
    }

    /**
     * Test method for
     * {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#skip(long)}.
     */
    @Test
    void test_skip_across_buffers() throws Exception {
        long skipped = is.skip(2);
        assertThat(skipped).isEqualTo(2);
        var ba = "1111".getBytes();
        int read = is.read(ba, 0, 4);
        assertThat(read).isEqualTo(4);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("cdef");
    }

    /**
     * Test method for
     */
    @Test
    void test_read_all() throws Exception {
//        var ba = is.readAllBytes();
//        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    /**
     * Test method for
     */
    @Test
    void test_read_n_bytes() throws Exception {
        var ba = is.readNBytes(13);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklm");
    }

    /**
     * Test method for
     */
    @Test
    void test_read_n_bytes_all() throws Exception {
        var ba = is.readNBytes(26);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    /**
     * Test method for
     */
    @Test
    void test_read_n_bytes_all_extra() throws Exception {
        var ba = is.readNBytes(27);
        assertThat(new String(ba, 0, ba.length)).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    /**
     * Test method for
     * {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#available()}.
     */
    @Test
    void testAvailable() throws Exception {
        assertThat(is.available()).isEqualTo(4);
    }

    /**
     * Test method for {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#close()}.
     */
    @Test
    void testClose() throws Exception {
        is.close();
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.read());
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.read(new byte[8]));
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.read(new byte[8], 0, 4));
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.read(new byte[8], 0, 4));
    }


    /**
     * Test method for {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#reset()}.
     */
    @Test
    void testReset() {
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> is.reset());
    }

    /**
     * Test method for {@link uk.gov.gchq.palisade.client.java.download.ByteBufferInputStream#markSupported()}.
     */
    @Test
    void testMarkSupported() {
        assertThat(is.markSupported()).isEqualTo(false);
    }

    public String consume(InputStream i) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(i, Charset.forName(UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }
}
