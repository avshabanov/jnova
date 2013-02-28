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

package com.truward.jnova.util.source.support;

import com.truward.jnova.util.source.Source;
import com.truward.jnova.util.source.SourceAccessException;

import java.io.*;

/**
 * Default implementation of the Source interface.
 */
public final class DefaultSource implements Source {

    public static final String STRING_SOURCE_NAME = "<string>";


    private char[] buffer;

    private final int length;

    private Reader reader;

    private final String sourceName;

    private int overflowSize;

    private int lastOverflowSize;



    public DefaultSource(Reader reader, int length, String sourceName) {
        assert sourceName != null && length >= 0;

        this.length = length;
        this.sourceName = sourceName;
        this.reader = reader;
    }

    public DefaultSource(String content) {
        this(new StringReader(content), content.length(), STRING_SOURCE_NAME);
    }

    public DefaultSource(InputStream inputStream, String sourceName, String encoding) {
        assert inputStream != null && sourceName != null && encoding != null;

        this.sourceName = sourceName;

        try {
            final int estimatedOverflow = 1;
            final int excessLength = inputStream.available() + estimatedOverflow;
            if (excessLength < 0) {
                throw new SourceAccessException("Too few bytes available: " + excessLength + " in source " + sourceName);
            }

            final InputStreamReader reader = new InputStreamReader(inputStream, encoding);
            try {
                buffer = new char[excessLength];
                length = reader.read(buffer);
                assert length >= 0 && length < excessLength;

                this.overflowSize = excessLength - length;
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new SourceAccessException(e);
        }
    }

    @Override
    public void setOverflowSize(int size) {
        overflowSize = size;
    }

    @Override
    public char[] getBuffer() {
        if (lastOverflowSize == overflowSize && buffer != null) {
            return buffer;
        }

        if (buffer == null) {
            if (reader == null) {
                throw new SourceAccessException("Reader is not specified, source: " + sourceName);
            }

            // read buffer and close it
            final char[] tmpBuffer = new char[length + overflowSize];

            try {
                final int charsRead;
                if (length == 0) {
                    // don't attempt to read from the buffer, since certain implementation may throw an error here.
                    charsRead = 0;
                } else {
                    charsRead = reader.read(tmpBuffer);
                }

                if (charsRead != length) {
                    throw new SourceAccessException("Expected to read " + length + " chars from the given source " +
                            sourceName + ", but read only " + charsRead);
                }

                // OK, close reader and remove reference to it.
                reader.close();
                reader = null;
            } catch (IOException e) {
                throw new SourceAccessException(e);
            }

            // update state
            buffer = tmpBuffer;
            lastOverflowSize = overflowSize;
        }

        // expand buffer to meet overflow size bounds
        if (overflowSize > lastOverflowSize) {
            final char[] tmpBuffer = new char[length + overflowSize];
            System.arraycopy(this.buffer, 0, tmpBuffer, 0, length);
            this.buffer = tmpBuffer;
            this.lastOverflowSize = overflowSize;
        }

        return buffer;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }
}
