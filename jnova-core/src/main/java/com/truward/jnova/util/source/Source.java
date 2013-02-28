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

package com.truward.jnova.util.source;

/**
 * Represents source being read.
 */
public interface Source {
    /**
     * Sets overflow size when reading the buffer.
     * The read-with-overflow is required for lexer.
     *
     * @param size Overflow size, can not be negative.
     */
    void setOverflowSize(int size);

    /**
     * Returns buffer available for read.
     * The only writable bytes are in the "overflow" bounds.
     *
     * @return Readable character buffer.
     */
    char[] getBuffer();

    /**
     * Returns length of the buffer without overflow.
     *
     * @return Readable buffer length.
     */
    int length();

    /**
     * Gets the associated source name.
     *
     * @return Associated name of the source.
     */
    String getSourceName();
}
