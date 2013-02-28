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

package com.truward.jnova.util.naming;

/**
 * An abstraction for internal strings allocated in symbol table.
 * The equal symbols can be compared with each other by using the `==' operator.
 * Internal representation is expected to be UTF-8 - based.
 */
public interface Symbol extends Comparable<Symbol> {

    /**
     * The index where the bytes of this name are stored in the global name buffer `nameArray'.
     * This method is introduced in order to provide a very fast Symbol-to-anything mapping -
     * e.g. create an array of values that corresponds to certain symbols and get associations by
     * accessing its array using the symbol's index.
     *
     * @return Zero based index.
     */
    public int getIndex();

    /**
     * Returns length of the UTF-8 byte representation of the symbol.
     * The returned length will be identical to the length of the corresponding String representation
     * when all the characters are 7-bit ASCII.
     *
     * @return Non-negative integer.
     */
    int getUtfLength();

    /**
     * Writes chars to the given buffer.
     * When preparing the buffer for certain string it is sufficient to allocate buffer of #getUtfLength length.
     *
     * @param buffer    Buffer the stored symbol should be written to.
     * @param startPos  Start position in the buffer from where the stored symbol should be written.
     * @return Number of chars written to the buffer.
     */
    int writeChars(char[] buffer, int startPos);
}
