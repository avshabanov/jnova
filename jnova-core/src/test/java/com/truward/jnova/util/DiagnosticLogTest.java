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

package com.truward.jnova.util;

import com.truward.jnova.util.diagnostics.DiagnosticsLog;
import com.truward.jnova.util.diagnostics.formatter.support.BasicDiagnosticsFormatter;
import com.truward.jnova.util.diagnostics.support.DefaultDiagnosticsLog;
import com.truward.jnova.util.source.support.DefaultSource;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static com.truward.jnova.util.diagnostics.parameter.Offset.at;
import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;


public final class DiagnosticLogTest {

    private DiagnosticsLog log;

    private StringWriter writer;

    @Before
    public void initLog() {
        writer = new StringWriter();

        log = new DefaultDiagnosticsLog(writer, new BasicDiagnosticsFormatter());
    }

    private void setSource(String source) {
        log.setSource(new DefaultSource(new StringReader(source), source.length(), "/path/to/myfile.ext"));
    }

    @Test
    public void test1() {
        setSource("asd\ndef asd  987 asd sfg\n\t\tdef");
        log.error(format("{0} is defined twice", "asd"), at(0));
        assertEquals("/path/to/myfile.ext:[1,1]: error:\n" +
                "asd is defined twice\n" +
                "asd\n" +
                "^\n",
                writer.toString());
    }

    @Test
    public void test2() {
        setSource("asd\ndef asd  987 asd sfg\n\t\tdef");
        log.warning(format("second declaration of {0} occurs here", "asd"), at(8));
        assertEquals("/path/to/myfile.ext:[2,5]: warning:\n" +
                "second declaration of asd occurs here\n" +
                "def asd  987 asd sfg\n" +
                "    ^\n",
                writer.toString());
    }

    @Test
    public void test3() {
        setSource("asd\ndef asd  987 asd sfg\n\t\tdef");
        log.error("third declaration occurs here", at(27));
        assertEquals("/path/to/myfile.ext:[3,3]: error:\n" +
                "third declaration occurs here\n" +
                "\t\tdef\n" +
                "\t\t^\n",
                writer.toString());
    }

    @Test
    public void test4() {
        setSource("a\nd ef\nu");
        log.info("myinfo", at(2));
        assertEquals("/path/to/myfile.ext:[2,1]: info:\n" +
                "myinfo\n" +
                "d ef\n" +
                "^\n",
                writer.toString());
    }

    @Test
    public void testErrorAndWarningCount() {
        assertEquals(0, log.getTotalErrors());
        assertEquals(0, log.getTotalWarnings());

        setSource("asdf");
        log.error("error1", at(1));
        log.error("error2");
        log.warning("warning1", at(3));
        log.info("info");

        assertEquals(2, log.getTotalErrors());
        assertEquals(1, log.getTotalWarnings());
    }

    @Test
    public void testStdErrLogging() {
        final PrintStream prevErr = System.err;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(bos));

        try {
            final DefaultDiagnosticsLog log = new DefaultDiagnosticsLog();
            log.error("Error!");

            final String str = bos.toString();
            assertEquals("<unspecified>: error:\n" +
                    "Error!\n" +
                    "\n", str);
        } finally {
            System.setErr(prevErr);
        }
    }
}
