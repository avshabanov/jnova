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

import com.truward.di.InjectionContext;
import com.truward.di.support.DefaultInjectionContext;
import com.truward.jnova.java.source.Keywords;
import com.truward.jnova.util.naming.SymbolTable;
import com.truward.jnova.util.naming.support.HashSymbolTable;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

/**
 * Tests base parser naming utilities.
 */
public final class NamingTest {
    private final EnumSet<Token> TOKENS_WITHOUT_NAME = EnumSet.of(
            Token.IDENTIFIER,
            Token.EOF,
            Token.ERROR,
            Token.CUSTOM,
            Token.INTLITERAL,
            Token.LONGLITERAL,
            Token.FLOATLITERAL,
            Token.DOUBLELITERAL,
            Token.CHARLITERAL,
            Token.STRINGLITERAL
    );

    @Test
    public void testKeywords() {
        final InjectionContext context = new DefaultInjectionContext();

        context.registerBean(new HashSymbolTable());
        context.registerBean(Keywords.class);

        final SymbolTable table = context.getBean(SymbolTable.class);
        final Keywords keywords = context.getBean(Keywords.class);

        // For named tokens
        for (final Token token : Token.values()) {
            if (!TOKENS_WITHOUT_NAME.contains(token)) {
                assertEquals(token, keywords.key(table.fromSequence(token.getName())));
            }
        }

        // For identifiers
        assertEquals(Token.IDENTIFIER, keywords.key(table.fromSequence("asd")));
        assertEquals(Token.IDENTIFIER, keywords.key(table.fromSequence("123")));
    }
}
