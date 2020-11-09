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
package uk.gov.gchq.palisade.client.util;

import java.util.IllegalFormatException;

/**
 * @author dbell
 */
public abstract class Checks {

    private Checks() {
        // should never be instantiated or subclassed
    }

    /**
     * Ensures that the provided parameter argument is not null.
     *
     * @param <T>      The type of argument
     * @param argument a boolean expression
     * @return the argument
     * @throws IllegalArgumentException if {@code argument} is null
     */
    public static <T> T checkArgument(final T argument) {
        if (argument == null) {
            throw new IllegalArgumentException("Null argument");
        }
        return argument;
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     *
     * @param expression a boolean expression
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(final boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     *
     * @param expression   a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be
     *                     converted to a string using
     *                     {@link String#valueOf(Object)}
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(final boolean expression, final Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     *
     * @param expression a boolean expression
     * @param template   a template for the exception message should the check fail.
     *                   The message is formed by passing the template and argument
     *                   to {@code String.format}. The difference is that if an
     *                   formatting error occurs, then the unmodified template will
     *                   be returned.
     * @param args       the arguments to be substituted into the message template.
     *                   Arguments are converted to strings using
     *                   {@link String#valueOf(Object)}.
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(
        final boolean expression,
        final String template,
        final Object... args) {
        if (!expression) {
            throw new IllegalArgumentException(format(template, args));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(final boolean b, final String template, final char p1) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(final boolean b, final String template, final int p1) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(final boolean b, final String template, final long p1) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final Object p1) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final char p1, final char p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final char p1, final int p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final char p1, final long p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final char p1, final Object p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final int p1, final char p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final int p1, final int p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final int p1, final long p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final int p1, final Object p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final long p1, final char p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final long p1, final int p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final long p1, final long p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final long p1, final Object p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final Object p1, final char p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final Object p1, final int p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final Object p1, final long p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b, final String template, final Object p1, final Object p2) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @param p3       the error template parameter 3
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b,
        final String template,
        final Object p1,
        final Object p2,
        final Object p3) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2, p3));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     * <p>
     *
     * @param b        the expression
     * @param template the error template
     * @param p1       the error template parameter 1
     * @param p2       the error template parameter 2
     * @param p3       the error template parameter 3
     * @param p4       the error template parameter 4
     * @see #checkArgument(boolean, String, Object...) for details.
     */
    public static void checkArgument(
        final boolean b,
        final String template,
        final Object p1,
        final Object p2,
        final Object p3,
        final Object p4) {
        if (!b) {
            throw new IllegalArgumentException(format(template, p1, p2, p3, p4));
        }
    }

    private static String format(final String template, final Object... objects) {
        try {
            return String.format(template, objects);
        } catch (IllegalFormatException ife) {
            // we catch this here as we do not want to throw an error, we'll just return the
            // template
            return template;
        }
    }
}
