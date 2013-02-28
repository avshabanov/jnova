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

package com.truward.jnova.util.diagnostics.formatter.support;

import com.truward.jnova.util.diagnostics.formatter.DiagnosticsFormatter;
import com.truward.jnova.util.diagnostics.formatter.DiagnosticsSeverity;
import com.truward.jnova.util.diagnostics.formatter.DiagnosticsTemplate;
import com.truward.jnova.util.diagnostics.parameter.DiagnosticsParameter;
import com.truward.jnova.util.diagnostics.parameter.Offset;
import com.truward.jnova.util.diagnostics.source.SourcePosition;

import java.io.PrintWriter;
import java.text.MessageFormat;

/*
            (2)
Test.java:7: warning:
[unchecked] unchecked generic array creation
for varargs parameter of type List<String>[]
    Arrays.asList(Arrays.asList("January",
    ^
1 warning



            (3)
$fullpath/DefaultDiagnosticsLog.java:[37,4] annotation type not applicable to this kind of declaration
 */

/**
 * Represents basic formatting capabilities.
 *
 * $source:[row+1,column+1]: $severity
 * message
 * $line
 * $pointer
 *
 */
public final class BasicDiagnosticsFormatter implements DiagnosticsFormatter {
    private static String getSeverityString(DiagnosticsSeverity severity) {
        switch (severity) {
            case ERROR:
                return "error";

            case WARNING:
                return "warning";

            case INFO:
                return "info";

            default:
                throw new AssertionError("Unexpected severity: " + severity);
        }
    }

    private static Offset getOffset(DiagnosticsParameter[] parameters) {
        for (final DiagnosticsParameter parameter : parameters) {
            if (parameter instanceof Offset) {
                return (Offset) parameter;
            }
        }

        return Offset.INVALID;
    }

    @Override
    public void format(DiagnosticsTemplate template) {
        final PrintWriter writer = template.getWriter();

        // source
        writer.print(template.getSourceName());
        writer.print(":");

        // position
        SourcePosition position = null;
        final Offset offset = getOffset(template.getParameters());
        if (offset.isValid() && template.isSourceAvailable()) {
            position = template.translate(offset.getOffset());
            if (position != null) {
                writer.print("[");
                writer.print(position.getRow() + 1);
                writer.print(",");
                writer.print(position.getColumn() + 1);
                writer.print("]:");
            }
        }

        // severity
        writer.print(" ");
        writer.print(getSeverityString(template.getSeverity()));
        writer.println(":");


        // error message
        writer.println(MessageFormat.format(template.getMessage(), (Object[]) template.getParameters()));

        // line and pointer
        if (position != null) {
            final String line = template.getLine(position.getRow());
            writer.println(line);

            for (int i = 0; i < position.getColumn(); ++i) {
                final char ch = line.charAt(i);
                if (ch <= ' ') {
                    writer.write(ch);
                } else {
                    writer.write(' ');
                }
            }
            writer.write('^');
        }

        // print line feed
        writer.println();
        writer.flush();
    }
}
