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

import com.truward.jnova.java.parser.Token;
import com.truward.jnova.util.naming.Symbol;
import com.truward.jnova.util.naming.SymbolTable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Map from Name to Token and Token to String.
 */
public final class Keywords {
    /**
     * Keyword array. Maps name indices to Token.
     */
    private Token[] key;

    /**
     * The number of the last entered keyword.
     */
    private int maxKey = 0;

    /** The names of all tokens.
     */
    private Symbol[] tokenName = new Symbol[Token.values().length];


    @Resource
    private SymbolTable nameTable;

    private void enterKeyword(String keyword, Token token) {
        final Symbol n = nameTable.fromSequence(keyword);
        tokenName[token.ordinal()] = n;
        if (n.getIndex() > maxKey) {
            maxKey = n.getIndex();
        }
    }

    @PostConstruct
    public void postConstruct() {
        assert key == null;

        for (Token token : Token.values()) {
            if (token.getName() != null) {
                enterKeyword(token.getName(), token);
            } else {
                tokenName[token.ordinal()] = null;
            }
        }

        key = new Token[maxKey + 1];

        for (int i = 0; i <= maxKey; i++) {
            key[i] = Token.IDENTIFIER;
        }

        for (Token t : Token.values()) {
            if (t.getName() != null) {
                key[tokenName[t.ordinal()].getIndex()] = t;
            }
        }
    }

    /**
     * Gets associated token with the name given.
     * @param name Keyword name.
     * @return Token
     */
    public Token key(Symbol name) {
        return (name.getIndex() > maxKey) ? Token.IDENTIFIER : key[name.getIndex()];
    }
}
