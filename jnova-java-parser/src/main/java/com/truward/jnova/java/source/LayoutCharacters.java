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

package com.truward.jnova.java.source;

/**
 * Contains layout character constants used in Java programs.
 */
public final class LayoutCharacters {
    private LayoutCharacters() {}

    /**
     * Tabulator column increment.
     */
    public final static int TAB_INC     = 8;

    /**
     * Tabulator character.
     */
    public final static byte TAB        = 0x8;

    /**
     * Line feed character.
     */
    public final static byte LF         = 0xA;

    /**
     * Form feed character.
     */
    public final static byte FF         = 0xC;

    /**
     * Carriage return character.
     */
    public final static byte CR         = 0xD;

    /**
     * End of input character.  Used as a sentinel to denote the character one
     * beyond the last defined character in a source file.
     */
    public final static byte EOI        = 0x1A;
}