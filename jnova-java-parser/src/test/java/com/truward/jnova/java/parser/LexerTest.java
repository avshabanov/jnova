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

import com.truward.jnova.util.ImmList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for lexer.
 */
public final class LexerTest extends ParserTestSupport {

    private Token[] scanAll() {
        ImmList<Token> result = ImmList.nil();

        for (;;) {
            lexer.nextToken();
            if (lexer.token() == Token.EOF) {
                break;
            }

            result = result.prepend(lexer.token());
        }

        return result.toArray(new Token[result.size()]);
    }

    private void assertScanEquals(Object ... expected) {
        for (int i = 0;; ++i) {
            lexer.nextToken();
            if (lexer.token() == Token.EOF) {
                assertEquals(i, expected.length);
                return;
            }

            final Token token = lexer.token();
            final String description;
            if (token.getName() == null) {
                description = ", value: " + lexer.name();
            } else {
                description = "";
            }

            assertTrue("Excessive unexpected token: " + token + description, i < expected.length);

            final Object currentExpected = expected[i];
            if (currentExpected instanceof String) {
                assertEquals(Token.IDENTIFIER, token);
                assertEquals(currentExpected.toString(), lexer.name().toString());
            } else if (currentExpected instanceof Token) {
                assertEquals(currentExpected, token);
            }
        }
    }

    @Test
    public void testBaseLexer() {
        setSource("import abc.def;");
        final Token[] tokens = scanAll();
        assertTrue(tokens.length > 0);

        setSource("import abc.def;");
        assertScanEquals(Token.IMPORT, "abc", Token.DOT, "def", Token.SEMI);
    }
}
