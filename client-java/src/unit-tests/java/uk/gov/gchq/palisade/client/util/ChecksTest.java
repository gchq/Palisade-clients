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
package uk.gov.gchq.palisade.client.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ChecksTest {

    private static final Class<IllegalArgumentException> CLASS = IllegalArgumentException.class;

    @Test
    void testCheckArgumentT() {
        assertThatExceptionOfType(CLASS).isThrownBy(() -> Checks.checkNotNull(null));
    }

    @Test
    void testCheckArgumentTObject() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkNotNull(null, "oops"))
            .withMessage("oops");
        assertThat(Checks.checkNotNull("boo", "oops")).isEqualTo("boo");
    }

    @Test
    void testCheckArgumentTStringObjectArray() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkNotNull(null, "oops %s %s", "one", "two"))
            .withMessage("oops one two");
        assertThat(Checks.checkNotNull("boo", "oops %s %s", "one", "two")).isEqualTo("boo");
    }

    @Test
    void testCheckArgumentBoolean() {
        assertThatExceptionOfType(CLASS).isThrownBy(() -> Checks.checkArgument(false));
        assertThatNoException().isThrownBy(() -> Checks.checkArgument(true));
    }

    @Test
    void testCheckArgumentBooleanObject() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops"))
            .withMessage("oops");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops"));
    }

    @Test
    void testCheckArgumentBooleanStringObjectArray() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", "one", "two"))
            .withMessage("oops one two");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", "one", "two"));
    }

    @Test
    void testCheckArgumentBooleanStringChar() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s", 'x'))
            .withMessage("oops x");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s", 'x'));
    }

    @Test
    void testCheckArgumentBooleanStringInt() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s", 2))
            .withMessage("oops 2");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s", 2));
    }

    @Test
    void testCheckArgumentBooleanStringLong() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s", 2L))
            .withMessage("oops 2");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s", 2L));
    }

    @Test
    void testCheckArgumentBooleanStringObject() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s", new BigDecimal("12")))
            .withMessage("oops 12");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s", new BigDecimal("12")));
    }

    @Test
    void testCheckArgumentBooleanStringCharChar() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 'x', 'y'))
            .withMessage("oops x y");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 'x', 'y'));
    }

    @Test
    void testCheckArgumentBooleanStringCharInt() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 'x', 2))
            .withMessage("oops x 2");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 'x', 2));
    }

    @Test
    void testCheckArgumentBooleanStringCharLong() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 'x', 2L))
            .withMessage("oops x 2");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 'x', 2L));
    }

    @Test
    void testCheckArgumentBooleanStringCharObject() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 'x', new BigDecimal("12")))
            .withMessage("oops x 12");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 'x', new BigDecimal("12")));
    }

    @Test
    void testCheckArgumentBooleanStringIntChar() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 2, 'x'))
            .withMessage("oops 2 x");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 2, 'x'));
    }

    @Test
    void testCheckArgumentBooleanStringIntInt() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 2, 4))
            .withMessage("oops 2 4");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 2, 4));
    }

    @Test
    void testCheckArgumentBooleanStringIntLong() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 2, 4L))
            .withMessage("oops 2 4");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 2, 4L));
    }

    @Test
    void testCheckArgumentBooleanStringIntObject() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 2, new BigDecimal("12")))
            .withMessage("oops 2 12");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 2, new BigDecimal("12")));
    }

    @Test
    void testCheckArgumentBooleanStringLongChar() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 2L, 'x'))
            .withMessage("oops 2 x");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 2L, 'x'));
    }

    @Test
    void testCheckArgumentBooleanStringLongInt() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 2L, 4))
            .withMessage("oops 2 4");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 2L, 4));
    }

    @Test
    void testCheckArgumentBooleanStringLongLong() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 2L, 4L))
            .withMessage("oops 2 4");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 2L, 4L));
    }

    @Test
    void testCheckArgumentBooleanStringLongObject() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", 2L, new BigDecimal("12")))
            .withMessage("oops 2 12");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", 2L, new BigDecimal("12")));
    }

    @Test
    void testCheckArgumentBooleanStringObjectChar() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", new BigDecimal("12"), 'x'))
            .withMessage("oops 12 x");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", new BigDecimal("12"), 'x'));
    }

    @Test
    void testCheckArgumentBooleanStringObjectInt() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", new BigDecimal("12"), 2))
            .withMessage("oops 12 2");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", new BigDecimal("12"), 2));
    }

    @Test
    void testCheckArgumentBooleanStringObjectLong() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", new BigDecimal("12"), 2L))
            .withMessage("oops 12 2");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", new BigDecimal("12"), 2L));
    }

    @Test
    void testCheckArgumentBooleanStringObjectObject() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s", new BigDecimal("12"), new BigDecimal("24")))
            .withMessage("oops 12 24");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s", new BigDecimal("12"), new BigDecimal("24")));
    }

    @Test
    void testCheckArgumentBooleanStringObjectObjectObject() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(() -> Checks.checkArgument(false, "oops %s %s %s", new BigDecimal("12"), new BigDecimal("24"),
                new BigDecimal("36")))
            .withMessage("oops 12 24 36");
        assertThatNoException()
            .isThrownBy(() -> Checks.checkArgument(true, "oops %s %s %s", new BigDecimal("12"), new BigDecimal("24"),
                new BigDecimal("36")));
    }

    @Test
    void testCheckArgumentBooleanStringObjectObjectObjectObject() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(
                () -> Checks.checkArgument(false, "oops %s %s %s %s", new BigDecimal("12"), new BigDecimal("24"),
                    new BigDecimal("36"), new BigDecimal("48")))
            .withMessage("oops 12 24 36 48");
        assertThatNoException()
            .isThrownBy(
                () -> Checks.checkArgument(true, "oops %s %s %s %s", new BigDecimal("12"), new BigDecimal("24"),
                    new BigDecimal("36"), new BigDecimal("48")));
    }

    @Test
    void testCheckArgumentExpressionTemplateObjectArray() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(
                () -> Checks.checkArgument((false), "oops %s %s %s %s %s", new BigDecimal("12"), new BigDecimal("24"),
                    new BigDecimal("36"), new BigDecimal("48"), new BigDecimal("52")))
            .withMessage("oops 12 24 36 48 52");
        assertThatNoException()
            .isThrownBy(
                () -> Checks.checkArgument((true), "oops %s %s %s %s %s", new BigDecimal("12"), new BigDecimal("24"),
                    new BigDecimal("36"), new BigDecimal("48"), new BigDecimal("52")));

    }

    @Test
    void testCheckArgumentObjectTemplateObjectArray() {
        assertThatExceptionOfType(CLASS)
            .isThrownBy(
                () -> Checks.checkArgument(false, "oops %s %s %s %s %s", new BigDecimal("12"), new BigDecimal("24"),
                    new BigDecimal("36"), new BigDecimal("48"), new BigDecimal("52")))
            .withMessage("oops 12 24 36 48 52");
        assertThatNoException()
            .isThrownBy(
                () -> Checks.checkArgument(true, "oops %s %s %s %s %s", new BigDecimal("12"), new BigDecimal("24"),
                    new BigDecimal("36"), new BigDecimal("48"), new BigDecimal("52")));

        var str1 = "boo";
        var str2 = Checks.checkNotNull(str1, "oops %s %s %s %s %s", new BigDecimal("12"), new BigDecimal("24"),
            new BigDecimal("36"), new BigDecimal("48"), new BigDecimal("52"));

        assertThat(str2).isSameAs(str1);

    }

}
