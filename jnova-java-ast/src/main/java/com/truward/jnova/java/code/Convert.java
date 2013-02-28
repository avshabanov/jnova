/*
 * Copyright 2013 Alexander Shabanov - http://alexshabanov.com.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truward.jnova.java.code;

/**
 * Utility class for static conversion methods between numbers
 * and strings in various formats.
 */
public final class Convert {
    private Convert() {} // Prohibit instantiation


    /**
     * Escape all unicode characters in string.
     * @param unicodeStr Source string with unicode characters.
     * @return Escaped string.
     */
    public static String escapeUnicode(String unicodeStr) {
        final int len = unicodeStr.length();
        int i = 0;
        while (i < len) {
            char ch = unicodeStr.charAt(i);
            if (ch > 255) {
                final StringBuilder builder = new StringBuilder();
                builder.append(unicodeStr.substring(0, i));
                while (i < len) {
                    ch = unicodeStr.charAt(i);
                    if (ch > 255) {
                        builder.append("\\u");
                        builder.append(Character.forDigit((ch >> 12) % 16, 16));
                        builder.append(Character.forDigit((ch >>  8) % 16, 16));
                        builder.append(Character.forDigit((ch >>  4) % 16, 16));
                        builder.append(Character.forDigit((ch      ) % 16, 16));
                    } else {
                        builder.append(ch);
                    }
                    i++;
                }
                unicodeStr = builder.toString();
            } else {
                i++;
            }
        }
        return unicodeStr;
    }


    // Convert string to integer.
    // TODO: move to another class - ParserImpl? (remove altogether)
    public static int string2int(String s, int radix)
        throws NumberFormatException {
        if (radix == 10) {
            return Integer.parseInt(s, radix);
        } else {
            char[] cs = s.toCharArray();
            int limit = Integer.MAX_VALUE / (radix/2);
            int n = 0;
            for (int i = 0; i < cs.length; i++) {
                int d = Character.digit(cs[i], radix);
                if (n < 0 ||
                    n > limit ||
                    n * radix > Integer.MAX_VALUE - d)
                    throw new NumberFormatException();
                n = n * radix + d;
            }
            return n;
        }
    }

    // Convert string to long integer.
    // TODO: move to another class - ParserImpl? (remove altogether)
    public static long string2long(String s, int radix)
        throws NumberFormatException {
        if (radix == 10) {
            return Long.parseLong(s, radix);
        } else {
            char[] cs = s.toCharArray();
            long limit = Long.MAX_VALUE / (radix/2);
            long n = 0;
            for (int i = 0; i < cs.length; i++) {
                int d = Character.digit(cs[i], radix);
                if (n < 0 ||
                    n > limit ||
                    n * radix > Long.MAX_VALUE - d)
                    throw new NumberFormatException();
                n = n * radix + d;
            }
            return n;
        }
    }


    /**
     * Escapes each character in a string that has an escape sequence or
     * is non-printable ASCII.  Leaves non-ASCII characters alone.
     * @param s String to be quoted.
     * @return Quoted string.
     */
    public static String quote(String s) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            buf.append(quote(s.charAt(i)));
        }
        return buf.toString();
    }

    /**
     * Escapes a character if it has an escape sequence or is
     * non-printable ASCII.  Leaves non-ASCII characters alone.
     * @param ch Character to be quoted.
     * @return Quoted character.
     */
    public static String quote(char ch) {
        switch (ch) {
        case '\b':  return "\\b";
        case '\f':  return "\\f";
        case '\n':  return "\\n";
        case '\r':  return "\\r";
        case '\t':  return "\\t";
        case '\'':  return "\\'";
        case '\"':  return "\\\"";
        case '\\':  return "\\\\";
        default:
            return (isPrintableAscii(ch))
                ? String.valueOf(ch)
                : String.format("\\u%04x", (int) ch);
        }
    }

    /**
     * Tests whether a character is a printable ASCII symbol.
     * @param ch Character to be tested.
     * @return True, if character is a printable ASCII, false otherwise.
     */
    private static boolean isPrintableAscii(char ch) {
        return ch >= ' ' && ch <= '~';
    }
}
