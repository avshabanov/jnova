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

package com.truward.jnova.java.parser;

import com.truward.jnova.util.naming.Symbol;

/**
 * Represents lexer interface.
 */
public interface Lexer {
    /**
     * Sets source to parse.
     *
     * @param input Input char array.
     * @param inputLength Length of input.
     */
    void setSource(char[] input, int inputLength);

    /**
     * The value of a literal token, recorded as a string.
     * For integers, leading 0x and 'l' suffixes are suppressed.
     *
     * @return Value of a literal token.
     */
    String stringVal();

    /**
     * Reads token.
     */
    void nextToken();

    /**
     * Gets current token name.
     *
     * @return The name, enclosed in the current token.
     */
    Symbol name();

    /**
     * Gets previous end position.
     *
     * @return The last character position of the previous token.
     */
    int prevEndPos();

    /**
     * Gets current token postion.
     *
     * @return A 0-based offset from beginning of the raw input stream (before unicode translation).
     */
    int pos();

    /**
     * Gets error position.
     *
     * @return The position where a lexical error occurred.
     */
    int errPos();

    /**
     * Set the position where a lexical error occurred.
     *
     * @param pos The position where a lexical error occurred.
     */
    void setErrPos(int pos);

    /**
     * Gets current token.
     *
     * @return The current token, set by nextToken().
     */
    Token token();

    /**
     * Sets the current token.
     * @param token Token to be set.
     */
    void setToken(Token token);

    /**
     * Gets radix, set when scanning number.
     *
     * @return Radix base.
     */
    int radix();

    /**
     * Returns the documentation string of the current token.
     * @return Documentation string or null.
     */
    String docComment();

    /**
     * Tests whether the lexer has been encountered deprecated in last doc comment.
     * This needs to be reset by client with resetDeprecatedFlag.
     * @see #resetDeprecatedFlag()
     *
     * @return True, if deprecated flag has been encountered.
     */
    boolean deprecatedFlag();

    /**
     * Resets deprecated flag.
     * @see #deprecatedFlag()
     */
    void resetDeprecatedFlag();
}
