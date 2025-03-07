/*
 * Copyright (C) 2019 Sun.Hao(https://www.crazy-coder.cn/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lodsve.core.ansi;

import org.springframework.util.Assert;

import java.util.Locale;

/**
 * Generates ANSI encoded output, automatically attempting to detect if the terminal
 * supports ANSI.
 *
 * @author Phillip Webb
 */
public abstract class AnsiOutput {

    private static final String ENCODE_JOIN = ";";

    private static Enabled enabled = Enabled.DETECT;

    private static Boolean consoleAvailable;

    private static Boolean ansiCapable;

    private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name")
            .toLowerCase(Locale.ENGLISH);

    private static final String ENCODE_START = "\033[";

    private static final String ENCODE_END = "m";

    private static final String RESET = "0;" + AnsiColor.DEFAULT;

    /**
     * Sets if ANSI output is enabled.
     *
     * @param enabled if ANSI is enabled, disabled or detected
     */
    public static void setEnabled(Enabled enabled) {
        Assert.notNull(enabled, "Enabled must not be null");
        AnsiOutput.enabled = enabled;
    }

    /**
     * Sets if the System.console() is known to be available.
     *
     * @param consoleAvailable if the console is known to be available or {@code null} to
     *                         use standard detection logic.
     */
    public static void setConsoleAvailable(Boolean consoleAvailable) {
        AnsiOutput.consoleAvailable = consoleAvailable;
    }

    static Enabled getEnabled() {
        return AnsiOutput.enabled;
    }

    /**
     * Encode a single {@link AnsiElement} if output is enabled.
     *
     * @param element the element to encode
     * @return the encoded element or an empty string
     */
    public static String encode(AnsiElement element) {
        if (isEnabled()) {
            return ENCODE_START + element + ENCODE_END;
        }
        return "";
    }

    /**
     * Create a new ANSI string from the specified elements. Any {@link AnsiElement}s will
     * be encoded as required.
     *
     * @param elements the elements to encode
     * @return a string of the encoded elements
     */
    public static String toString(Object... elements) {
        StringBuilder sb = new StringBuilder();
        if (isEnabled()) {
            buildEnabled(sb, elements);
        } else {
            buildDisabled(sb, elements);
        }
        return sb.toString();
    }

    private static void buildEnabled(StringBuilder sb, Object[] elements) {
        boolean writingAnsi = false;
        boolean containsEncoding = false;
        for (Object element : elements) {
            if (element instanceof AnsiElement) {
                containsEncoding = true;
                if (!writingAnsi) {
                    sb.append(ENCODE_START);
                    writingAnsi = true;
                } else {
                    sb.append(ENCODE_JOIN);
                }
            } else {
                if (writingAnsi) {
                    sb.append(ENCODE_END);
                    writingAnsi = false;
                }
            }
            sb.append(element);
        }
        if (containsEncoding) {
            sb.append(writingAnsi ? ENCODE_JOIN : ENCODE_START);
            sb.append(RESET);
            sb.append(ENCODE_END);
        }
    }

    private static void buildDisabled(StringBuilder sb, Object[] elements) {
        for (Object element : elements) {
            if (!(element instanceof AnsiElement) && element != null) {
                sb.append(element);
            }
        }
    }

    private static boolean isEnabled() {
        if (enabled == Enabled.DETECT) {
            if (ansiCapable == null) {
                ansiCapable = detectIfAnsiCapable();
            }
            return ansiCapable;
        }
        return enabled == Enabled.ALWAYS;
    }

    private static boolean detectIfAnsiCapable() {
        try {
            if (Boolean.FALSE.equals(consoleAvailable)) {
                return false;
            }
            if ((consoleAvailable == null) && (System.console() == null)) {
                return false;
            }
            return !(OPERATING_SYSTEM_NAME.contains("win"));
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * Possible values to pass to {@link AnsiOutput#setEnabled}. Determines when to output
     * ANSI escape sequences for coloring application output.
     */
    public enum Enabled {

        /**
         * Try to detect whether ANSI coloring capabilities are available. The default
         * value for {@link AnsiOutput}.
         */
        DETECT,

        /**
         * Enable ANSI-colored output.
         */
        ALWAYS,

        /**
         * Disable ANSI-colored output.
         */
        NEVER

    }

}
