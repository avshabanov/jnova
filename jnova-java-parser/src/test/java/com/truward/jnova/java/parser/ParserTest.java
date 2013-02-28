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

import com.truward.jnova.java.ast.Ast;
import com.truward.jnova.java.ast.DefaultAstFactory;
import com.truward.jnova.java.code.Flags;
import com.truward.jnova.java.code.TypeTags;
import com.truward.jnova.java.parser.impl.ParserImpl;
import com.truward.jnova.util.ImmList;
import com.truward.jnova.util.naming.Symbol;
import com.truward.jnova.util.naming.SymbolTable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests parser.
 */
public final class ParserTest extends ParserTestSupport {

    private Parser parser;

    private SymbolTable table;

    private final Ast.Factory f = new DefaultAstFactory();

    @Before
    public void initTable() {
        table = context.getBean(SymbolTable.class);
    }


    @Before
    public void initParser() {
        context.registerBean(ParserImpl.class);

        parser = context.getBean(Parser.class);
    }

    private Symbol nm(String value) {
        return table.fromSequence(value);
    }

    @Test
    public void testBaseParse() {
        setSource("import asd.def;");

        final Ast.Node node = parser.parseTest();
        assertNotNull(node);

        // test tree
        {
            assertTrue(node instanceof Ast.Import);
            final Ast.Import importNode = (Ast.Import) node;
            assertTrue(!importNode.isStaticImport());

            assertTrue(importNode.getQualifier() instanceof Ast.FieldAccess);
            final Ast.FieldAccess fieldAccess = (Ast.FieldAccess) importNode.getQualifier();
            assertEquals("def", fieldAccess.getIdentifier().toString());

            assertTrue(fieldAccess.getExpression() instanceof Ast.Ident);
            final Ast.Ident ident = (Ast.Ident) fieldAccess.getExpression();
            assertEquals("asd", ident.getName().toString());
        }
    }

    @Test
    public void testSimpleAnnotation() {
        setSource("@Resource()");
        Ast.Node node = parser.parseTest();
        assertNotNull(node);

        StructMatcher.assertStructMatches(
                Ast.Node.class,
                f.astAnnotation(
                        f.astIdent(nm("Resource")),
                        ImmList.<Ast.Expression>nil()),
                node);
    }

    @Test
    public void testComplexAnnotation() {
        setSource("@Spoon(field1 = 12, field2 = \"Literal2\")");

        StructMatcher.assertStructMatches(
                Ast.Node.class,
                f.astAnnotation(
                        f.astIdent(nm("Spoon")),
                        ImmList.of(
                                f.astAssignment(
                                        f.astIdent(nm("field1")),
                                        f.astLiteral(TypeTags.INT, 12)),
                                f.astAssignment(
                                        f.astIdent(nm("field2")),
                                        f.astLiteral(TypeTags.CLASS, "Literal2"))
                        )),
                parser.parseTest());
    }

    @Test
    public void testPackageCompilationUnit() {
        setSource("@SomeAnnotation()\n" +
                "package com.mysite.checkme;");

        StructMatcher.assertStructMatches(
                Ast.Node.class,
                f.astCompilationUnit(
                        ImmList.of(f.astAnnotation(f.astIdent(nm("SomeAnnotation")), ImmList.<Ast.Expression>nil())),
                        f.astFieldAccess(f.astFieldAccess(f.astIdent(nm("com")), nm("mysite")), nm("checkme")),
                        ImmList.<Ast.Node>nil()
                ),
                parser.parseCompilationUnit());
    }

    @Test
    public void testSimpleCompilationUnit() {
        setSource("package com.mysite;\n" +
                "import some.Place;\n" +
                "public final class Clazz extends Place implements Serializable, Lockable {\n" +
                "public void foo() {}" +
                "}");

        StructMatcher.assertStructMatches(
                Ast.Node.class,
                f.astCompilationUnit(
                        ImmList.<Ast.Annotation>nil(),
                        f.astFieldAccess(f.astIdent(nm("com")), nm("mysite")),
                        ImmList.of(
                                f.astImport(f.astFieldAccess(f.astIdent(nm("some")), nm("Place")) ,false),
                                f.astClassDecl(
                                        f.astModifiers(Flags.PUBLIC | Flags.FINAL),
                                        nm("Clazz"),
                                        ImmList.<Ast.TypeParameter>nil(),
                                        f.astIdent(nm("Place")),
                                        ImmList.of(f.astIdent(nm("Serializable")), f.astIdent(nm("Lockable"))),
                                        ImmList.of(
                                                f.astMethodDecl(
                                                        f.astModifiers(Flags.PUBLIC),
                                                        nm("foo"),
                                                        f.astPrimitiveType(TypeTags.VOID),
                                                        ImmList.<Ast.TypeParameter>nil(),
                                                        ImmList.<Ast.VariableDecl>nil(),
                                                        ImmList.<Ast.Expression>nil(),
                                                        f.astBlock(ImmList.<Ast.Statement>nil()),
                                                        null
                                                )
                                        )
                                )
                        )
                ),
                parser.parseCompilationUnit()
        );
    }

    @Test
    public void testAnnotationModifier() {
        setSource("@Generated public final class MyDaoImpl {}");

        final Ast.CompilationUnit compilationUnit = parser.parseCompilationUnit();

        StructMatcher.assertStructMatches(
                Ast.Node.class,
                f.astCompilationUnit(
                        ImmList.<Ast.Annotation>nil(),
                        null,
                        ImmList.of(f.astClassDecl(
                                f.astModifiers(
                                        Flags.PUBLIC | Flags.FINAL,
                                        ImmList.of(f.astAnnotation(f.astIdent(nm("Generated")), ImmList.<Ast.Expression>nil()))
                                ),
                                nm("MyDaoImpl"),
                                ImmList.<Ast.TypeParameter>nil(),
                                null,
                                ImmList.<Ast.Expression>nil(),
                                ImmList.<Ast.Node>nil()
                        ))),
                compilationUnit);
    }

    @Test
    public void testMethodInvocation() {
        setSource("package com.mysite;\n" +
                "public class MyDaoImpl extends MyAbstractDao {\n" +
                "   public void save(String name) {\n" +
                "       getJdbcTemplate().update(\"INSERT INTO table (name) VALUES (?)\", name);" +
                "   }" +
                "}");

        final Ast.CompilationUnit compilationUnit = parser.parseCompilationUnit();
        StructMatcher.assertStructMatches(
                Ast.Node.class,
                f.astCompilationUnit(
                        ImmList.<Ast.Annotation>nil(),
                        f.astFieldAccess(f.astIdent(nm("com")), nm("mysite")),
                        ImmList.of(
                                f.astClassDecl(
                                        f.astModifiers(Flags.PUBLIC),
                                        nm("MyDaoImpl"),
                                        ImmList.<Ast.TypeParameter>nil(),
                                        f.astIdent(nm("MyAbstractDao")),
                                        ImmList.<Ast.Expression>nil(),
                                        ImmList.of(
                                                f.astMethodDecl(
                                                        f.astModifiers(Flags.PUBLIC),
                                                        nm("save"),
                                                        f.astPrimitiveType(TypeTags.VOID),
                                                        ImmList.<Ast.TypeParameter>nil(),
                                                        ImmList.of(f.astVariableDecl(
                                                                f.astModifiers(Flags.PARAMETER),
                                                                nm("name"),
                                                                f.astIdent(nm("String")),
                                                                null)),
                                                        ImmList.<Ast.Expression>nil(),
                                                        f.astBlock(ImmList.of(
                                                                f.astExpressionStatement(f.astMethodInvocation(
                                                                        f.astFieldAccess(
                                                                                f.astMethodInvocation(
                                                                                        f.astIdent(nm("getJdbcTemplate")),
                                                                                        ImmList.<Ast.Expression>nil()
                                                                                ),
                                                                                nm("update")),
                                                                        ImmList.of(
                                                                                f.astLiteral(TypeTags.CLASS, "INSERT INTO table (name) VALUES (?)"),
                                                                                f.astIdent(nm("name"))
                                                                        )
                                                                ))
                                                        )),
                                                        null
                                                )
                                        )
                                )
                        )
                ), compilationUnit);
    }

    @Test
    public void testErrorTooLargeNumber() {
        setSource("class MyDaoImpl {\n" +
                "void foo() {\n" +
                "int a = 1263546546574987987;\n" +
                "}\n" +
                "}");

        parser.parseCompilationUnit();

        final String logContent = logWriter.toString();

        assertEquals("<unnamed>:[3,9]: error:\n" +
                "Integer number 1263546546574987987 is too large\n" +
                "int a = 1263546546574987987;\n" +
                "        ^\n", logContent);
    }
}
