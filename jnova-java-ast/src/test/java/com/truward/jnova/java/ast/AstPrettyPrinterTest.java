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

package com.truward.jnova.java.ast;

import com.truward.jnova.java.code.Flags;
import com.truward.jnova.java.code.TypeTags;
import com.truward.jnova.util.ImmList;
import com.truward.jnova.util.naming.Symbol;
import com.truward.jnova.util.naming.SymbolTable;
import com.truward.jnova.util.naming.support.HashSymbolTable;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests AST pretty printer.
 */
public class AstPrettyPrinterTest {
    private SymbolTable table = new HashSymbolTable();

    private final Ast.Factory f = new DefaultAstFactory();



    private Symbol nm(String str) {
        return table.fromSequence(str);
    }


    private static class StringChunkMatcher {
        final String source;
        int startPos;
        int endPos;

        private StringChunkMatcher(String source) {
            this.source = source;
            this.startPos = 0;
            this.endPos = 0;
        }

        boolean scanNext() {
            // start from previous end pos
            startPos = endPos;

            // find out start pos starting at non-whitespace chunk
            for (;startPos < source.length(); ++startPos) {
                if (source.charAt(startPos) > ' ') {
                    break;
                }
            }

            // stop at subsequent whitespace (if any)
            for (endPos = startPos; endPos <= source.length(); ++endPos) {
                if (source.length() == endPos || source.charAt(endPos) <= ' ') {
                    break;
                }
            }

            // no further progress made
            return (endPos > startPos);
        }

        @Override
        public int hashCode() {
            int result = source != null ? source.hashCode() : 0;
            result = 31 * result + startPos;
            result = 31 * result + endPos;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof StringChunkMatcher)) {
                return false;
            }

            final StringChunkMatcher other = (StringChunkMatcher) obj;

            // match sequences against each other
            final int delta = endPos - startPos;
            if (delta != (other.endPos - other.startPos)) {
                return false;
            }

            // compare sequences
            for (int i = 0; i < delta; ++i) {
                if (source.charAt(startPos + i) != other.source.charAt(other.startPos + i)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public String toString() {
            if (endPos == startPos) {
                return ((startPos == 0 && source.length() > 0) ? "<BOF>" : "<EOF>");
            }

            return source.substring(startPos, endPos);
        }
    }

    private static void assertTokenMatch(String expected, Ast.Node actualNode) {
        final StringWriter stringWriter = new StringWriter();
        new AstPrettyPrinter(stringWriter, true).printExpr(actualNode);
        // always append whitespace at the end
        final String actual = stringWriter.toString();

        final StringChunkMatcher expectedMatcher = new StringChunkMatcher(expected);
        final StringChunkMatcher actualMatcher = new StringChunkMatcher(actual);

        for (;;) {
            final boolean hasNextExpected = expectedMatcher.scanNext();
            final boolean hasNextActual = actualMatcher.scanNext();

            assertTrue("Chunks must match: " + expectedMatcher + " vs " + actualMatcher,
                    hasNextActual == hasNextExpected);

            if (!hasNextExpected) {
                break;
            }

            assertEquals(expectedMatcher, actualMatcher);
        }
    }

    @Test
    public void testImport() {
        assertTokenMatch("import java.util.List;",
                f.astImport(
                        f.astFieldAccess(f.astFieldAccess(f.astIdent(nm("java")), nm("util")), nm("List")),
                        false));
    }

    @Test
    public void testExprStatement() {
        assertTokenMatch("{\n" +
                "getJdbcTemplate().update(\"INSERT\", name);\n" +
                "}",
                f.astBlock(ImmList.of(
                        f.astExpressionStatement(f.astMethodInvocation(
                                f.astFieldAccess(
                                        f.astMethodInvocation(
                                                f.astIdent(nm("getJdbcTemplate")),
                                                ImmList.<Ast.Expression>nil()
                                        ),
                                        nm("update")),
                                ImmList.of(
                                        f.astLiteral(TypeTags.CLASS, "INSERT"),
                                        f.astIdent(nm("name"))
                                )
                        ))
                )));
    }

    @Test
    public void testSimpleCompilationUnit() {
        assertTokenMatch("package com.mycompany.myapp;\n" +
                "import java.util.List;\n" +
                "public class MyApp {\n" +
                "}",
                f.astCompilationUnit(ImmList.<Ast.Annotation>nil(),
                        f.astFieldAccess(f.astFieldAccess(f.astIdent(nm("com")), nm("mycompany")), nm("myapp")),
                        ImmList.of(
                                f.astImport(
                                        f.astFieldAccess(f.astFieldAccess(f.astIdent(nm("java")), nm("util")), nm("List")), false),
                                f.astClassDecl(
                                        f.astModifiers(Flags.PUBLIC, ImmList.<Ast.Annotation>nil()),
                                        nm("MyApp"),
                                        ImmList.<Ast.TypeParameter>nil(),
                                        null,
                                        ImmList.<Ast.Expression>nil(),
                                        ImmList.<Ast.Node>nil()
                                )
                        )));
    }

    @Test
    public void testCompilationUnitWithMethod() {
        assertTokenMatch("package com.mycompany.myapp;\n" +
                "import java.util.List;\n" +
                "@Repository()\n" +
                "public final class MyDaoImpl extends JdbcDaoSupport implements MyDao, InitializingBean {\n" +
                "public void savePerson(String personName, int age) {\n" +
                "}\n" +
                "}",
                f.astCompilationUnit(ImmList.<Ast.Annotation>nil(),
                        f.astFieldAccess(f.astFieldAccess(f.astIdent(nm("com")), nm("mycompany")), nm("myapp")),
                        ImmList.of(
                                f.astImport(
                                        f.astFieldAccess(f.astFieldAccess(f.astIdent(nm("java")), nm("util")), nm("List")), false),
                                f.astClassDecl(
                                        f.astModifiers(Flags.PUBLIC | Flags.FINAL,
                                                ImmList.of(f.astAnnotation(f.astIdent(nm("Repository")), ImmList.<Ast.Expression>nil()))),
                                        nm("MyDaoImpl"),
                                        ImmList.<Ast.TypeParameter>nil(),
                                        f.astIdent(nm("JdbcDaoSupport")),
                                        ImmList.of(f.astIdent(nm("MyDao")), f.astIdent(nm("InitializingBean"))),
                                        ImmList.of(
                                                // savePerson method
                                                f.astMethodDecl(
                                                        f.astModifiers(Flags.PUBLIC, ImmList.<Ast.Annotation>nil()),
                                                        nm("savePerson"),
                                                        f.astPrimitiveType(TypeTags.VOID),
                                                        ImmList.<Ast.TypeParameter>nil(),
                                                        ImmList.of(
                                                                f.astVariableDecl(
                                                                        f.astModifiers(0),
                                                                        nm("personName"), f.astIdent(nm("String")), null),
                                                                f.astVariableDecl(
                                                                        f.astModifiers(0),
                                                                        nm("age"), f.astPrimitiveType(TypeTags.INT), null)),
                                                        ImmList.<Ast.Expression>nil(),
                                                        f.astBlock(0, ImmList.<Ast.Statement>nil()),
                                                        null
                                                )
                                        )
                                )
                        )));
    }
}
