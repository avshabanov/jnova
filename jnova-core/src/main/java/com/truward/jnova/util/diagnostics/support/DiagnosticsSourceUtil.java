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

package com.truward.jnova.util.diagnostics.support;

import com.truward.jnova.util.diagnostics.source.SourcePosition;
import com.truward.jnova.util.source.Source;

/**
 * Internally used utility class for working with diagnostic source.
 */
public final class DiagnosticsSourceUtil {
    private DiagnosticsSourceUtil() {} // Hidden ctor


    public static SourcePosition translateOffset(Source source, int offset) {
        final char[] buf = source.getBuffer();
        final int length = source.length();

        int currentColumn = 0;
        int currentRow = 0;
        int currentOffset = 0;

        for (int i = 0; i < length; ++i) {
            if (currentOffset == offset) {
                return new SourcePosition(currentRow, currentColumn);
            }
            ++currentOffset;

            if (buf[i] == '\n') {
                currentColumn = 0;
                ++currentRow;
            } else {
                ++currentColumn;
            }
        }

        return null;
    }

    public static String getLine(Source source, int row) {
        int currentRow = 0;
        final char[] buf = source.getBuffer();
        final int length = source.length();

        for (int i = 0; i < length; ++i) {
            final char ch = buf[i];
            if (ch == '\n') {
                ++currentRow;
                continue;
            }

            if (currentRow == row) {
                int startOffset = i;
                int endOffset = i + 1;

                // advance ahead till end of the buffer or new line
                for (; endOffset < length && buf[endOffset] != '\n'; ++endOffset) {}

                return new String(buf, startOffset, endOffset - startOffset);
            }
        }

        return null;
    }
}
