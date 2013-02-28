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
import com.truward.jnova.java.code.PredefinedNames;
import com.truward.jnova.java.parser.impl.LexerImpl;
import com.truward.jnova.java.parser.impl.ParserBundle;
import com.truward.jnova.java.source.Keywords;
import com.truward.jnova.java.source.Source;
import com.truward.jnova.util.diagnostics.DiagnosticsLog;
import com.truward.jnova.util.diagnostics.support.DefaultDiagnosticsLog;
import com.truward.jnova.util.naming.support.HashSymbolTable;
import com.truward.jnova.util.source.support.DefaultSource;
import org.junit.Before;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * Supportive class for JUNIT parser tests.
 */
public abstract class ParserTestSupport {
    protected InjectionContext context;

    protected Lexer lexer;

    protected StringWriter logWriter;

    @Before
    public final void initLexer() {
        context = new DefaultInjectionContext();

        context.registerBean(new HashSymbolTable());
        context.registerBean(PredefinedNames.class);
        context.registerBean(Keywords.class);
        context.registerBean(Source.DEFAULT);
        context.registerBean(ParserBundle.class);

        logWriter = new StringWriter();
        context.registerBean(new DefaultDiagnosticsLog(logWriter));
        context.registerBean(LexerImpl.class);

        lexer = context.getBean(Lexer.class);
    }

    protected final void setSource(String sourceStr) {
        final DefaultSource source = new DefaultSource(new StringReader(sourceStr), sourceStr.length(), "<unnamed>");

        lexer.setSource(source.getBuffer(), source.length());
        context.getBean(DiagnosticsLog.class).setSource(source);
    }
}
