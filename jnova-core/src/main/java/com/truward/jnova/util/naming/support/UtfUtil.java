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

package com.truward.jnova.util.naming.support;

/**
 * UTF-8 converter utility class.
 */
public final class UtfUtil {
    private UtfUtil() {} // Prevent instantiation


    /**
     * Copy characters in source array to bytes in target array, converting them to Utf8 representation.
     * The target array must be large enough to hold the result.
     * @param src       The array holding the characters to convert.
     * @param srcIndex  The start index from which characters are converted.
     * @param dst       The array holding the converted characters..
     * @param dstIndex  The start index from which converted bytes are written.
     * @param len       The maximum number of characters to convert.
     * @return First index in `dst' past the last copied byte.
     */
    public static int charsToUtf(char[] src, int srcIndex, byte[] dst, int dstIndex, int len) {
        int j = dstIndex;
        int limit = srcIndex + len;
        for (int i = srcIndex; i < limit; i++) {
            char ch = src[i];
            if (1 <= ch && ch <= 0x7F) {
                dst[j++] = (byte)ch;
            } else if (ch <= 0x7FF) {
                dst[j++] = (byte)(0xC0 | (ch >> 6));
                dst[j++] = (byte)(0x80 | (ch & 0x3F));
            } else {
                dst[j++] = (byte)(0xE0 | (ch >> 12));
                dst[j++] = (byte)(0x80 | ((ch >> 6) & 0x3F));
                dst[j++] = (byte)(0x80 | (ch & 0x3F));
            }
        }
        return j;
    }

    /**
     * Convert `len' bytes from utf8 to characters.
     * Parameters are as in {@see System#arraycopy}
     * @param src       The array holding the bytes to convert.
     * @param srcIndex  The start index from which bytes are converted.
     * @param dst       The array holding the converted characters.
     * @param dstIndex  The start index from which converted characters are written.
     * @param len       The maximum number of bytes to convert.
     * @return First index in `dst' past the last copied char.
     */
    public static int utfToChars(byte[] src, int srcIndex, char[] dst, int dstIndex, int len) {
        int i = srcIndex;
        int j = dstIndex;
        int limit = srcIndex + len;
        while (i < limit) {
            int b = src[i++] & 0xFF;
            if (b >= 0xE0) {
                b = (b & 0x0F) << 12;
                b = b | (src[i++] & 0x3F) << 6;
                b = b | (src[i++] & 0x3F);
            } else if (b >= 0xC0) {
                b = (b & 0x1F) << 6;
                b = b | (src[i++] & 0x3F);
            }
            dst[j++] = (char)b;
        }
        return j;
    }

    // TODO: remove if not needed
//    /**
//     * Return bytes in Utf8 representation as an array of characters.
//     * @param src       The array holding the bytes.
//     * @param srcIndex  The start index from which bytes are converted.
//     * @param len       The maximum number of bytes to convert.
//     * @return Converted char array.
//     */
//    public static char[] utfToChars(byte[] src, int srcIndex, int len) {
//        char[] dst = new char[len];
//        int len1 = utfToChars(src, srcIndex, dst, 0, len);
//        char[] result = new char[len1];
//        System.arraycopy(dst, 0, result, 0, len1);
//        return result;
//    }
//
//    /**
//     * Return all bytes of a given array in Utf8 representation as an array of characters.
//     * @param src       The array holding the bytes.
//     * @return Converted char array.
//     */
//    public static char[] utfToChars(byte[] src) {
//        return utfToChars(src, 0, src.length);
//    }

    /**
     * Return bytes in Utf8 representation as a string.
     * @param src       The array holding the bytes.
     * @param srcIndex  The start index from which bytes are converted.
     * @param len       The maximum number of bytes to convert.
     * @return String representation.
     */
    public static String utfToString(byte[] src, int srcIndex, int len) {
        char dst[] = new char[len];
        int len1 = utfToChars(src, srcIndex, dst, 0, len);
        return new String(dst, 0, len1);
    }
}
