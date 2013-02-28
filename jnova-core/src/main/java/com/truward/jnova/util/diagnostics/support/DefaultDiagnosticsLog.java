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

package com.truward.jnova.util.diagnostics.support;

import com.truward.jnova.util.diagnostics.*;
import com.truward.jnova.util.diagnostics.formatter.DiagnosticsFormatter;
import com.truward.jnova.util.diagnostics.formatter.DiagnosticsSeverity;
import com.truward.jnova.util.diagnostics.formatter.DiagnosticsTemplate;
import com.truward.jnova.util.diagnostics.formatter.support.BasicDiagnosticsFormatter;
import com.truward.jnova.util.diagnostics.parameter.DiagnosticsParameter;
import com.truward.jnova.util.diagnostics.source.SourcePosition;
import com.truward.jnova.util.source.Source;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Implementation of the diagnostic log.
 */
public final class DefaultDiagnosticsLog implements DiagnosticsLog {

    private static final String UNSPECIFIED_SOURCE_NAME = "<unspecified>";

    private Source source;

    private final PrintWriter logWriter;

    private final DiagnosticsFormatter formatter;

    private int totalErrors = 0;

    private int totalWarnings = 0;

    public DefaultDiagnosticsLog() {
        this(new PrintWriter(System.err));
    }

    public DefaultDiagnosticsLog(Writer logWriter) {
        this(logWriter, new BasicDiagnosticsFormatter());
    }

    public DefaultDiagnosticsLog(Writer logWriter, DiagnosticsFormatter formatter) {
        this.logWriter = new PrintWriter(logWriter);
        this.formatter = formatter;
    }




    private void print(final DiagnosticsSeverity severity,
                       final String message,
                       final DiagnosticsParameter[] parameters) {
        formatter.format(new DiagnosticsTemplate() {
            @Override
            public PrintWriter getWriter() {
                return logWriter;
            }

            @Override
            public boolean isSourceAvailable() {
                return (source != null);
            }

            @Override
            public SourcePosition translate(int offset) {
                if (source == null) {
                    return null;
                }

                return DiagnosticsSourceUtil.translateOffset(source, offset);
            }

            @Override
            public String getLine(int row) {
                if (source == null) {
                    throw new IllegalStateException("Can't get content with unknown diagnostic source");
                }

                return DiagnosticsSourceUtil.getLine(source, row);
            }

            @Override
            public String getSourceName() {
                if (source != null) {
                    return source.getSourceName();
                }

                return UNSPECIFIED_SOURCE_NAME;
            }

            @Override
            public DiagnosticsSeverity getSeverity() {
                return severity;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public DiagnosticsParameter[] getParameters() {
                return parameters;
            }
        });
    }


    @Override
    public void error(String message, DiagnosticsParameter... parameters) {
        print(DiagnosticsSeverity.ERROR, message, parameters);
        ++totalErrors;
    }

    @Override
    public void warning(String message, DiagnosticsParameter... parameters) {
        print(DiagnosticsSeverity.WARNING, message, parameters);
        ++totalWarnings;
    }

    @Override
    public void info(String message, DiagnosticsParameter... parameters) {
        print(DiagnosticsSeverity.INFO, message, parameters);
    }

    @Override
    public int getTotalErrors() {
        return totalErrors;
    }

    @Override
    public int getTotalWarnings() {
        return totalWarnings;
    }

    @Override
    public void setSource(Source source) {
        this.source = source;
    }
}
