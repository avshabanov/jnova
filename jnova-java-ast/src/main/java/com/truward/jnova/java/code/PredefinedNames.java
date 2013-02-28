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

package com.truward.jnova.java.code;

import com.truward.jnova.util.naming.Symbol;
import com.truward.jnova.util.naming.SymbolTable;


/**
 * Contains the most used names.
 */
public final class PredefinedNames {
    public PredefinedNames(SymbolTable symbolTable) {
        empty = symbolTable.fromSequence("");

        asterisk = symbolTable.fromSequence(ASTERISK);
        slash = symbolTable.fromSequence("/");
        slashequals = symbolTable.fromSequence("/=");
        hyphen = symbolTable.fromSequence("-");

        error = symbolTable.fromSequence("<error>");
        init = symbolTable.fromSequence(INIT);

        _this = symbolTable.fromSequence("this");
        _super = symbolTable.fromSequence("super");
        _default = symbolTable.fromSequence("default");
        _class = symbolTable.fromSequence("class");
    }

    public static final String ASTERISK = "*";

    public static final String INIT = "<init>";

    public final Symbol empty;

    public final Symbol asterisk;
    public final Symbol slash;
    public final Symbol slashequals;
    public final Symbol hyphen;

    public final Symbol error;
    public final Symbol init;

    public final Symbol _this;
    public final Symbol _super;
    public final Symbol _default;
    public final Symbol _class;
}
