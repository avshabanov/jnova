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

package com.truward.jnova.util.diagnostics.formatter;

import com.truward.jnova.util.diagnostics.parameter.DiagnosticsParameter;
import com.truward.jnova.util.diagnostics.source.SourcePosition;

import java.io.PrintWriter;

/**
 * Callback to provide certain formatting-related information.
 */
public interface DiagnosticsTemplate {
    // print method

    PrintWriter getWriter();

    //
    // source navigation
    //

    boolean isSourceAvailable();

    SourcePosition translate(int offset);

    String getLine(int row);

    //
    // arguments
    //

    String getSourceName();

    DiagnosticsSeverity getSeverity();

    String getMessage();

    DiagnosticsParameter[] getParameters();
}
