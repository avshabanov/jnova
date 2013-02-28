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
 * Represents symbol table.
 */
public interface SymbolTable {

    /**
     * Create a name from the characters from the given buffer in the specified range.
     *
     * @param src       Source char buffer.
     * @param start     Index in source chars buffer.
     * @param length    Length of the converted sequence.
     * @return Non-null symbol instance.
     */
    Symbol fromChars(char[] src, int start, int length);

    /**
     * Create a name from the characters in the given sequence.
     *
     * @param src       Source char sequence.
     * @return Non-null symbol instance.
     */
    Symbol fromSequence(CharSequence src);

    /**
     * Gets symbol by the associated index.
     * @see com.truward.jnova.util.naming.Symbol#getIndex()
     * Throws out of bounds exception if symbolIndex does not belong to any symbol registered in the symbol table.
     * NOTE: this function is not expected to be efficient and shall be used only for debugging purposes.
     *
     * @param symbolIndex Symbol index.
     * @return Non-null symbol instance.
     */
    Symbol fromIndex(int symbolIndex);
}
