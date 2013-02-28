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

import com.truward.jnova.java.code.*;
import com.truward.jnova.util.ImmList;
import com.truward.jnova.util.naming.Symbol;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Prints out a tree as an indented Java source program.
 */
public final class AstPrettyPrinter extends AstVisitor {
    public static final int DEFAULT_INDENTATION_WIDTH = 4;

    /**
     * The output stream on which trees are printed.
     */
    private final Writer out;

    /**
     * Set when we are producing source output.  If we're not producing source output, we can sometimes
     * give more detail in the output even though that detail would not be valid java source.
     */
    private final boolean sourceOutput;

    /**
     * Indentation width.
     */
    private int indentationWidth = DEFAULT_INDENTATION_WIDTH;

    /**
     * The current left margin.
     */
    private int leftMargin = 0;

    /**
     * The enclosing class name.
     */
    private Symbol enclClassName;

    /**
     * A hashtable mapping trees to their documentation comments (can be null)
     */
    private Map<Ast.Node, String> docComments = null;

    /**
     * Visitor argument: the current precedence level.
     */
    private int prec = AstInfo.notExpression;

    /**
     * Cached - current line separator character.
     */
    private final String lineSeparator = System.getProperty("line.separator");



    //
    // utility methods
    //

    private static boolean isAsterisk(Symbol name) {
        return name.toString().equals(PredefinedNames.ASTERISK);
    }

    private static boolean isInit(Symbol name) {
        return name.toString().equals(PredefinedNames.INIT);
    }


    /**
     * Public constructor.
     *
     * @param out Destination writer.
     * @param sourceOutput Detects whether pure java source should be printed out.
     */
    public AstPrettyPrinter(Writer out, boolean sourceOutput) {
        this.out = out;
        this.sourceOutput = sourceOutput;
    }

    
    /**
     * Exception to propagate IOException through visitXXX methods.
     */
    private static class UncheckedIOException extends RuntimeException {
        UncheckedIOException(IOException e) {
            super(e.getMessage(), e);
        }
    }

    /**
     * Align code to be indented to left margin.
     */
    private void align() {
        try {
            for (int i = 0; i < leftMargin; i++) {
                out.write(" ");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Increase left margin by indentation width.
     */
    private void indent() {
        leftMargin += indentationWidth;
    }

    /**
     * Decrease left margin by indentation width.
     */
    private void undent() {
        leftMargin -= indentationWidth;
    }

    /**
     * Enter a new precedence level. Emit a `(' if new precedence level is less than precedence level so far.
     *
     * @param contextPrec The precedence level in force so far.
     * @param ownPrec The new precedence level.
     */
    private void open(int contextPrec, int ownPrec) {
        if (ownPrec < contextPrec) {
            try {
                out.write("(");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Leave precedence level. Emit a `(' if inner precedence level is less than precedence level we revert to.
     *
     * @param contextPrec The precedence level we revert to.
     * @param ownPrec The inner precedence level.
     */
    private void close(int contextPrec, int ownPrec) {
        if (ownPrec < contextPrec) {
            try {
                out.write(")");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Print string, replacing all non-ascii character with unicode escapes.
     *
     * @param str String to be printed.
     */
    private void print(String str) {
        try {
            out.write(Convert.escapeUnicode(str));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void println() {
        print(lineSeparator);
    }
    
    
    
    //
    // Traversal methods
    //
    
    private void printExpr(Ast.Node expr, int prec) {
        int oldPrec = this.prec;
        try {
            this.prec = prec;
            if (expr == null) {
                // TODO: ensure that this never happens
                print("/* missing */");
            } else {
                expr.accept(this);
            }
        } finally {
            // restore prec
            this.prec = oldPrec;
        }
    }

    /**
     * Publicly visible AST node printer.
     * @param expr Expression to be printed out.
     */
    public void printExpr(Ast.Node expr) {
        printExpr(expr, AstInfo.noPrec);
    }
    
    /**
     * Derived visitor method: print statement tree.
     * @param node Ast statement node.
     */
    private void printStat(Ast.Node node) {
        printExpr(node, AstInfo.notExpression);
    }

    /**
     * Derived visitor method: print list of expression trees, separated by given string.
     * @param nodes Ast expression nodes.
     * @param sep the separator string
     */
    private <T extends Ast.Node> void printExprs(ImmList<T> nodes, String sep) {
        if (!nodes.isEmpty()) {
            printExpr(nodes.getHead());
            for (ImmList<T> l = nodes.getTail(); !l.isEmpty(); l = l.getTail()) {
                print(sep);
                printExpr(l.getHead());
            }
        }
    }

    /**
     * Derived visitor method: print list of expression trees, separated by commas.
     * @param nodes Ast expression nodes.
     */
    private <T extends Ast.Node> void printExprs(ImmList<T> nodes) {
        printExprs(nodes, ", ");
    }

    // Prints the inner element type of a nested array
    private void printBaseElementType(Ast.ArrayType node) {
        Ast.Node elem = node.getElementType();

        while (elem instanceof Ast.Wildcard) {
            elem = ((Ast.Wildcard) elem).getBound();
        }

        if (elem instanceof Ast.ArrayType) {
            printBaseElementType((Ast.ArrayType) elem);
        } else {
            printExpr(elem);
        }
    }

    // prints the brackets of a nested array in reverse order
    private void printBrackets(Ast.ArrayType node) {
        Ast.Node elem;
        while (true) {
            elem = node.getElementType();
            print("[]");
            if (!(elem instanceof Ast.ArrayType)) {
                break;
            }
            node = (Ast.ArrayType) elem;
        }
    }

    /** Derived visitor method: print list of statements, each on a separate line.
     * @param nodes Ast statement nodes.
     */
    private void printStats(ImmList<? extends Ast.Node> nodes) {
        for (ImmList<? extends Ast.Node> l = nodes; !l.isEmpty(); l = l.getTail()) {
            align();
            printStat(l.getHead());
            println();
        }
    }

    /**
     * Print a set of modifiers.
     * @param flags Modifier flags.
     */
    private void printFlags(long flags) {
        if ((flags & Flags.SYNTHETIC) != 0) {
            print("/*synthetic*/ ");
        }

        print(AstInfo.flagNames(flags));

        if ((flags & Flags.StandardFlags) != 0) {
            print(" ");
        }

        if ((flags & Flags.ANNOTATION) != 0) {
            print("@");
        }
    }

    private void printAnnotations(ImmList<Ast.Annotation> nodes) {
        for (ImmList<Ast.Annotation> l = nodes; l.nonEmpty(); l = l.getTail()) {
            printStat(l.getHead());
            println();
            align();
        }
    }

    /**
     * Print documentation comment, if it exists
     * @param node The ast node for which a documentation comment should be printed.
     */
    private void printDocComment(Ast.Node node) {
        if (docComments != null) {
            String dc = docComments.get(node);
            if (dc != null) {
                print("/**"); println();
                int pos = 0;
                int endpos = lineEndPos(dc, pos);
                while (pos < dc.length()) {
                    align();
                    print(" *");
                    if (pos < dc.length() && dc.charAt(pos) > ' ') print(" ");
                    print(dc.substring(pos, endpos)); println();
                    pos = endpos + 1;
                    endpos = lineEndPos(dc, pos);
                }
                align(); print(" */"); println();
                align();
            }
        }
    }
    // helper for printDocComment
    private static int lineEndPos(String s, int start) {
        int pos = s.indexOf('\n', start);
        if (pos < 0) {
            pos = s.length();
        }
        return pos;
    }

    /**
     * If type parameter list is non-empty, print it enclosed in "<...>" brackets.
     * @param nodes Ast type parameters nodes.
     */
    public void printTypeParameters(ImmList<Ast.TypeParameter> nodes) {
        if (nodes.nonEmpty()) {
            print("<");
            printExprs(nodes);
            print(">");
        }
    }

    /**
     * Prints a statements block.
     * @param stats Ast statement nodes.
     */
    public void printBlock(ImmList<? extends Ast.Node> stats) {
        print("{");
        println();
        indent();
        printStats(stats);
        undent();
        align();
        print("}");
    }

    /**
     * Prints enum body block.
     * @param stats Enum body Ast statements.
     */
    public void printEnumBody(ImmList<? extends Ast.Node> stats) {
        print("{");
        println();
        indent();
        boolean first = true;
        for (ImmList<? extends Ast.Node> l = stats; l.nonEmpty(); l = l.getTail()) {
            if (isEnumerator(l.getHead())) {
                if (!first) {
                    print(",");
                    println();
                }
                align();
                printStat(l.getHead());
                first = false;
            }
        }
        print(";");
        println();
        for (ImmList<? extends Ast.Node> l = stats; l.nonEmpty(); l = l.getTail()) {
            if (!isEnumerator(l.getHead())) {
                align();
                printStat(l.getHead());
                println();
            }
        }
        undent();
        align();
        print("}");
    }

    private static boolean isEnumerator(Ast.Node t) {
        return t.getKind() == AstNodeKind.VARIABLE_DECL &&
                (((Ast.VariableDecl) t).getModifiers().getFlags() & Flags.ENUM) != 0;
    }
    
    /** 
     * Print unit consisting of package clause and import statements in toplevel,
     * followed by class definition. if class definition == null, print all definitions in toplevel.
     * @param tree     The toplevel tree
     * @param cdef     The class definition, which is assumed to be part of the toplevel tree.
     */
    public void printUnit(Ast.CompilationUnit tree, Ast.ClassDecl cdef) {
        docComments = AstInfo.getDocComments(tree);
        printDocComment(tree);

        if (!tree.getPackageAnnotations().isEmpty()) {
            for (final Ast.Annotation annotation : tree.getPackageAnnotations()) {
                visitAnnotation(annotation);
                println();
            }
        }

        if (tree.getPackageId() != null) {
            print("package ");
            printExpr(tree.getPackageId());
            print(";");
            println();
        }
        boolean firstImport = true;
        for (ImmList<? extends Ast.Node> l = tree.getDefinitions();
        !l.isEmpty() && (cdef == null || l.getHead().getKind() == AstNodeKind.IMPORT);
        l = l.getTail()) {
            if (l.getHead().getKind() == AstNodeKind.IMPORT) {
                final Ast.Import imp = (Ast.Import) l.getHead();
                final Symbol name = AstInfo.name(imp.getQualifier());
                if (isAsterisk(name) ||
                        cdef == null ||
                        isUsed(imp.getQualifier(), cdef)) {
                    if (firstImport) {
                        firstImport = false;
                        println();
                    }
                    printStat(imp);
                }
            } else {
                printStat(l.getHead());
            }
        }
        if (cdef != null) {
            printStat(cdef);
            println();
        }
    }

    // where
    boolean isUsed(Ast.Node nodeSym, Ast.Node cdef) {
        // TODO: properly implement
        // cheating code inspector
        return nodeSym == cdef;
        //        final Symbol t = TreeInfo.symbol(nodeSym);
//        class UsedVisitor extends AstScanner {
//            public void scan(Ast.Node node) {
//                if (node != null && !result) {
//                    node.accept(this);
//                }
//            }
//
//            boolean result = false;
//
//            public void visitIdent(Ast.Ident node) {
//                if (node.sym == t) {
//                    result = true;
//                }
//            }
//        }
//
//        UsedVisitor v = new UsedVisitor();
//        v.scan(cdef);
//        return v.result;
    }
    

    //
    // visitor methods
    //

    @Override
    public void visitNode(Ast.Node node) {
        print("/* UNKNOWN NODE: " + node.getClass() + "#" + node.hashCode() + " */");
    }


    @Override
    public void visitCompilationUnit(Ast.CompilationUnit node) {
        printUnit(node, null);
    }

    @Override
    public void visitImport(Ast.Import node) {
        print("import ");
        if (node.isStaticImport()) {
            print("static ");
        }
        printExpr(node.getQualifier());
        print(";");
        println();
    }

    @Override
    public void visitClass(Ast.ClassDecl node) {
        println();
        align();
        printDocComment(node);
        printAnnotations(node.getModifiers().getAnnotations());
        printFlags(node.getModifiers().getFlags() & ~Flags.INTERFACE);

        final Symbol enclClassNamePrev = enclClassName;
        enclClassName = node.getName();

        if ((node.getModifiers().getFlags() & Flags.INTERFACE) != 0) {
            print("interface " + node.getName());
            printTypeParameters(node.getTypeParameters());

            if (node.getImplementing().nonEmpty()) {
                print(" extends ");
                printExprs(node.getImplementing());
            }
        } else {
            if ((node.getModifiers().getFlags() & Flags.ENUM) != 0) {
                print("enum " + node.getName());
            } else {
                print("class " + node.getName());
            }

            printTypeParameters(node.getTypeParameters());

            if (node.getExtending() != null) {
                print(" extends ");
                printExpr(node.getExtending());
            }

            if (node.getImplementing().nonEmpty()) {
                print(" implements ");
                printExprs(node.getImplementing());
            }
        }

        print(" ");

        if ((node.getModifiers().getFlags() & Flags.ENUM) != 0) {
            printEnumBody(node.getDefinitions());
        } else {
            printBlock(node.getDefinitions());
        }

        enclClassName = enclClassNamePrev;
    }

    @Override
    public void visitMethod(Ast.MethodDecl node) {
        // when producing source output, omit anonymous constructors
        if (isInit(node.getName()) && enclClassName == null && sourceOutput) {
            return;
        }

        println(); align();
        printDocComment(node);
        printExpr(node.getModifiers());
        printTypeParameters(node.getTypeParameters());

        if (isInit(node.getName())) {
            print((enclClassName != null ? enclClassName : node.getName()).toString());
        } else {
            printExpr(node.getReturnType());
            print(" " + node.getName());
        }

        print("(");
        printExprs(node.getParameters());
        print(")");

        if (node.getThrown().nonEmpty()) {
            print(" throws ");
            printExprs(node.getThrown());
        }

        if (node.getBody() != null) {
            print(" ");
            printStat(node.getBody());
        } else {
            print(";");
        }
    }

    @Override
    public void visitVariable(Ast.VariableDecl node) {
        if (docComments != null && docComments.get(node) != null) {
            println(); align();
        }

        printDocComment(node);

        if ((node.getModifiers().getFlags() & Flags.ENUM) != 0) {
            print("/*public static final*/ ");
            print(node.getName().toString());

            if (node.getInitializer() != null) {
                print(" /* = ");
                printExpr(node.getInitializer());
                print(" */");
            }
        } else {
            printExpr(node.getModifiers());

            if ((node.getModifiers().getFlags() & Flags.VARARGS) != 0) {
                printExpr(((Ast.ArrayType) node.getVariableType()).getElementType());
                print("... " + node.getName());
            } else {
                printExpr(node.getVariableType());
                print(" " + node.getName());
            }

            if (node.getInitializer() != null) {
                print(" = ");
                printExpr(node.getInitializer());
            }

            if (prec == AstInfo.notExpression) {
                print(";");
            }
        }
    }

    @Override
    public void visitEmptyStatement(Ast.EmptyStatement node) {
        print(";");
    }

    @Override
    public void visitBlock(Ast.Block node) {
        printFlags(node.getFlags());
        printBlock(node.getStatements());
    }

    @Override
    public void visitDoWhileLoop(Ast.DoWhileLoop node) {
        print("do ");
        printStat(node.getBody());
        align();
        print(" while ");

        if (node.getCondition().getKind() == AstNodeKind.PARENS) {
            printExpr(node.getCondition());
        } else {
            print("(");
            printExpr(node.getCondition());
            print(")");
        }

        print(";");
    }

    @Override
    public void visitWhileLoop(Ast.WhileLoop node) {
        print("while ");

        if (node.getCondition().getKind() == AstNodeKind.PARENS) {
            printExpr(node.getCondition());
        } else {
            print("(");
            printExpr(node.getCondition());
            print(")");
        }

        print(" ");
        printStat(node.getBody());
    }

    @Override
    public void visitForLoop(Ast.ForLoop node) {
        print("for (");
        if (node.getInitializers().nonEmpty()) {
            if (node.getInitializers().getHead().getKind() == AstNodeKind.VARIABLE_DECL) {
                printExpr(node.getInitializers().getHead());
                for (ImmList<? extends Ast.Statement> l = node.getInitializers().getTail(); l.nonEmpty(); l = l.getTail()) {
                    Ast.VariableDecl vdef = (Ast.VariableDecl) l.getHead();
                    print(", " + vdef.getName() + " = ");
                    printExpr(vdef.getInitializer());
                }
            } else {
                printExprs(node.getInitializers());
            }
        }

        print("; ");

        if (node.getCondition() != null) {
            printExpr(node.getCondition());
        }

        print("; ");
        printExprs(node.getStep());
        print(") ");
        printStat(node.getBody());
    }

    @Override
    public void visitForEachLoop(Ast.ForEachLoop node) {
        print("for (");
        printExpr(node.getVariable());
        print(" : ");
        printExpr(node.getExpression());
        print(") ");
        printStat(node.getBody());
    }

    @Override
    public void visitLabeledStatement(Ast.LabeledStatement node) {
        print(node.getLabel() + ": ");
        printStat(node.getBody());
    }

    @Override
    public void visitSwitch(Ast.Switch node) {
        print("switch ");

        if (node.getSelector().getKind() == AstNodeKind.PARENS) {
            printExpr(node.getSelector());
        } else {
            print("(");
            printExpr(node.getSelector());
            print(")");
        }

        print(" {");
        println();
        printStats(node.getCases());
        align();
        print("}");
    }

    @Override
    public void visitCase(Ast.Case node) {
        if (node.getExpression() == null) {
            print("default");
        } else {
            print("case ");
            printExpr(node.getExpression());
        }

        print(": ");
        println();
        indent();
        printStats(node.getStatements());
        undent();
        align();
    }

    @Override
    public void visitSynchronized(Ast.Synchronized node) {
        print("synchronized ");
        if (node.getLock().getKind() == AstNodeKind.PARENS) {
            printExpr(node.getLock());
        } else {
            print("(");
            printExpr(node.getLock());
            print(")");
        }
        print(" ");
        printStat(node.getBody());
    }

    @Override
    public void visitTry(Ast.Try node) {
        print("try ");
        printStat(node.getBody());

        for (ImmList<Ast.Catch> l = node.getCatchers(); l.nonEmpty(); l = l.getTail()) {
            printStat(l.getHead());
        }

        if (node.getFinalizer() != null) {
            print(" finally ");
            printStat(node.getFinalizer());
        }
    }

    @Override
    public void visitCatch(Ast.Catch node) {
        print(" catch (");
        printExpr(node.getParameter());
        print(") ");
        printStat(node.getBody());
    }

    @Override
    public void visitConditionalExpression(Ast.Conditional node) {
        open(prec, AstInfo.condPrec);
        printExpr(node.getCondition(), AstInfo.condPrec);
        print(" ? ");
        printExpr(node.getTruePart(), AstInfo.condPrec);
        print(" : ");
        printExpr(node.getFalsePart(), AstInfo.condPrec);
        close(prec, AstInfo.condPrec);
    }

    @Override
    public void visitIf(Ast.If node) {
        print("if ");
        if (node.getCondition().getKind() == AstNodeKind.PARENS) {
            printExpr(node.getCondition());
        } else {
            print("(");
            printExpr(node.getCondition());
            print(")");
        }
        print(" ");
        printStat(node.getThenPart());
        if (node.getElsePart() != null) {
            print(" else ");
            printStat(node.getElsePart());
        }
    }

    @Override
    public void visitExpressionStatement(Ast.ExpressionStatement node) {
        printExpr(node.getExpression());
        if (prec == AstInfo.notExpression) {
            print(";");
        }
    }

    @Override
    public void visitBreak(Ast.Break node) {
        print("break");
        if (node.getLabel() != null) {
            print(" " + node.getLabel());
        }
        print(";");
    }

    @Override
    public void visitContinue(Ast.Continue node) {
        print("continue");
        if (node.getLabel() != null) {
            print(" " + node.getLabel());
        }
        print(";");
    }

    @Override
    public void visitReturn(Ast.Return node) {
        print("return");
        if (node.getExpression() != null) {
            print(" ");
            printExpr(node.getExpression());
        }
        print(";");
    }

    @Override
    public void visitThrow(Ast.Throw node) {
        print("throw ");
        printExpr(node.getExpression());
        print(";");
    }

    @Override
    public void visitAssert(Ast.Assert node) {
        print("assert ");
        printExpr(node.getCondition());
        if (node.getDetail() != null) {
            print(" : ");
            printExpr(node.getDetail());
        }
        print(";");
    }

    @Override
    public void visitMethodInvocation(Ast.MethodInvocation node) {
        // TODO: assume node.getTypeArguments() is never equals to null!
        if (node.getTypeArguments() != null && node.getTypeArguments().nonEmpty()) {
            if (node.getMethodSelect().getKind() == AstNodeKind.SELECT) {
                final Ast.FieldAccess left = (Ast.FieldAccess) node.getMethodSelect();
                printExpr(left.getExpression());
                print(".<");
                printExprs(node.getTypeArguments());
                print(">" + left.getIdentifier().toString());
            } else {
                print("<");
                printExprs(node.getTypeArguments());
                print(">");
                printExpr(node.getMethodSelect());
            }
        } else {
            printExpr(node.getMethodSelect());
        }
        print("(");
        printExprs(node.getArguments());
        print(")");
    }

    @Override
    public void visitNewClass(Ast.NewClass node) {
        if (node.getEnclosingExpression() != null) {
            printExpr(node.getEnclosingExpression());
            print(".");
        }

        print("new ");

        if (!node.getTypeArguments().isEmpty()) {
            print("<");
            printExprs(node.getTypeArguments());
            print(">");
        }

        printExpr(node.getClassIdentifier());
        print("(");
        printExprs(node.getArguments());
        print(")");

        if (node.getClassBody() != null) {
            Symbol enclClassNamePrev = enclClassName;
            enclClassName = AstInfo.getEnclosingClassName(node);

            if ((node.getClassBody().getModifiers().getFlags() & Flags.ENUM) != 0) {
                print("/*enum*/");
            }

            printBlock(node.getClassBody().getDefinitions());

            enclClassName = enclClassNamePrev;
        }
    }

    @Override
    public void visitNewArray(Ast.NewArray node) {
        if (node.getElementType() != null) {
            print("new ");
            Ast.Node elem = node.getElementType();
            if (elem instanceof Ast.ArrayType) {
                printBaseElementType((Ast.ArrayType) elem);
            } else {
                printExpr(elem);
            }

            for (ImmList<Ast.Expression> l = node.getDimensions(); l.nonEmpty(); l = l.getTail()) {
                print("[");
                printExpr(l.getHead());
                print("]");
            }
            if (elem instanceof Ast.ArrayType) {
                printBrackets((Ast.ArrayType) elem);
            }
        }
        if (node.getInitializers() != null) {
            if (node.getElementType() != null) {
                print("[]");
            }
            print("{");
            printExprs(node.getInitializers());
            print("}");
        }
    }

    @Override
    public void visitParens(Ast.Parens node) {
        print("(");
        printExpr(node.getExpression());
        print(")");
    }

    @Override
    public void visitAssignment(Ast.Assignment node) {
        open(prec, AstInfo.assignPrec);
        printExpr(node.getVariable(), AstInfo.assignPrec + 1);
        print(" = ");
        printExpr(node.getExpression(), AstInfo.assignPrec);
        close(prec, AstInfo.assignPrec);
    }
    
    private String operatorName(int tag) {
        switch(tag) {
            case AstNodeKind.POS:       return "+";
            case AstNodeKind.NEG:       return "-";
            case AstNodeKind.NOT:       return "!";
            case AstNodeKind.COMPL:     return "~";
            case AstNodeKind.PREINC:    return "++";
            case AstNodeKind.PREDEC:    return "--";
            case AstNodeKind.POSTINC:   return "++";
            case AstNodeKind.POSTDEC:   return "--";
            case AstNodeKind.NULLCHK:   return "<*nullchk*>";
            case AstNodeKind.OR:        return "||";
            case AstNodeKind.AND:       return "&&";
            case AstNodeKind.EQ:        return "==";
            case AstNodeKind.NE:        return "!=";
            case AstNodeKind.LT:        return "<";
            case AstNodeKind.GT:        return ">";
            case AstNodeKind.LE:        return "<=";
            case AstNodeKind.GE:        return ">=";
            case AstNodeKind.BITOR:     return "|";
            case AstNodeKind.BITXOR:    return "^";
            case AstNodeKind.BITAND:    return "&";
            case AstNodeKind.SL:        return "<<";
            case AstNodeKind.SR:        return ">>";
            case AstNodeKind.USR:       return ">>>";
            case AstNodeKind.PLUS:      return "+";
            case AstNodeKind.MINUS:     return "-";
            case AstNodeKind.MUL:       return "*";
            case AstNodeKind.DIV:       return "/";
            case AstNodeKind.MOD:       return "%";
            default:
                throw new AssertionError("Unexpected tag: " + tag);
        }
    }

    @Override
    public void visitCompoundAssignment(Ast.CompoundAssignment node) {
        open(prec, AstInfo.assignopPrec);
        printExpr(node.getVariable(), AstInfo.assignopPrec + 1);
        print(" " + operatorName(node.getKind() - AstNodeKind.ASG_OFFSET) + "= ");
        printExpr(node.getExpression(), AstInfo.assignopPrec);
        close(prec, AstInfo.assignopPrec);
    }

    @Override
    public void visitUnary(Ast.Unary node) {
        final int ownprec = AstInfo.opPrec(node.getKind());
        final String opname = operatorName(node.getKind());

        open(prec, ownprec);

        if (node.getKind() <= AstNodeKind.PREDEC) {
            print(opname);
            printExpr(node.getExpression(), ownprec);
        } else {
            printExpr(node.getExpression(), ownprec);
            print(opname);
        }

        close(prec, ownprec);
    }

    @Override
    public void visitBinary(Ast.Binary node) {
        final int ownprec = AstInfo.opPrec(node.getKind());
        final String opname = operatorName(node.getKind());

        open(prec, ownprec);
        printExpr(node.getLeftOperand(), ownprec);
        print(" " + opname + " ");
        printExpr(node.getRightOperand(), ownprec + 1);
        close(prec, ownprec);
    }

    @Override
    public void visitTypeCast(Ast.TypeCast node) {
        open(prec, AstInfo.prefixPrec);
        print("(");
        printExpr(node.getType());
        print(")");
        printExpr(node.getExpression(), AstInfo.prefixPrec);
        close(prec, AstInfo.prefixPrec);
    }

    @Override
    public void visitInstanceOf(Ast.InstanceOf node) {
        open(prec, AstInfo.ordPrec);
        printExpr(node.getExpression(), AstInfo.ordPrec);
        print(" instanceof ");
        printExpr(node.getTestedClass(), AstInfo.ordPrec + 1);
        close(prec, AstInfo.ordPrec);
    }

    @Override
    public void visitArrayAccess(Ast.ArrayAccess node) {
        printExpr(node.getExpression(), AstInfo.postfixPrec);
        print("[");
        printExpr(node.getIndex());
        print("]");
    }

    @Override
    public void visitFieldAccess(Ast.FieldAccess node) {
        printExpr(node.getExpression(), AstInfo.postfixPrec);
        print(".");
        print(node.getIdentifier().toString());
    }

    @Override
    public void visitIdent(Ast.Ident node) {
        print(node.getName().toString());
    }

    @Override
    public void visitLiteral(Ast.Literal node) {
        switch (node.getTypeTag()) {
            case TypeTags.INT:
                print(node.getValue().toString());
                break;
            case TypeTags.LONG:
                print(node.getValue() + "L");
                break;
            case TypeTags.FLOAT:
                print(node.getValue() + "F");
                break;
            case TypeTags.DOUBLE:
                print(node.getValue().toString());
                break;
            case TypeTags.CHAR:
                print("\'" +
                        Convert.quote(String.valueOf((char)((Number)node.getValue()).intValue())) +
                        "\'");
                break;
            case TypeTags.BOOLEAN:
                print(((Number) node.getValue()).intValue() == 1 ? "true" : "false");
                break;
            case TypeTags.BOT:
                print("null");
                break;
            default:
                print("\"" + Convert.quote(node.getValue().toString()) + "\"");
                break;
        }
    }

    @Override
    public void visitPrimitiveType(Ast.PrimitiveType node) {
        switch(node.getTypeTag()) {
            case TypeTags.BYTE:
                print("byte");
                break;
            case TypeTags.CHAR:
                print("char");
                break;
            case TypeTags.SHORT:
                print("short");
                break;
            case TypeTags.INT:
                print("int");
                break;
            case TypeTags.LONG:
                print("long");
                break;
            case TypeTags.FLOAT:
                print("float");
                break;
            case TypeTags.DOUBLE:
                print("double");
                break;
            case TypeTags.BOOLEAN:
                print("boolean");
                break;
            case TypeTags.VOID:
                print("void");
                break;
            default:
                print("error");
                break;
        }
    }

    @Override
    public void visitArrayType(Ast.ArrayType node) {
        printBaseElementType(node);
        printBrackets(node);
    }

    @Override
    public void visitParameterizedType(Ast.ParameterizedType node) {
        printExpr(node.getParameterizedClass());
        print("<");
        printExprs(node.getArguments());
        print(">");
    }

    @Override
    public void visitTypeParameter(Ast.TypeParameter node) {
        print(node.getName().toString());
        if (node.getBounds().nonEmpty()) {
            print(" extends ");
            printExprs(node.getBounds(), " & ");
        }
    }

    @Override
    public void visitWildcard(Ast.Wildcard node) {
        visitTypeBoundKind(node.getTypeBoundKind());

        if (node.getTypeBoundKind().getBoundKind() != BoundKind.UNBOUND) {
            printExpr(node.getBound());
        }
    }

    @Override
    public void visitTypeBoundKind(Ast.TypeBoundKind node) {
        print(String.valueOf(node.getBoundKind()));
    }

    @Override
    public void visitModifiers(Ast.Modifiers node) {
        printAnnotations(node.getAnnotations());
        printFlags(node.getFlags());
    }

    @Override
    public void visitAnnotation(Ast.Annotation node) {
        print("@");
        printExpr(node.getAnnotationType());
        print("(");
        printExprs(node.getArguments());
        print(")");
    }

    @Override
    public void visitErroneous(Ast.Erroneous node) {
        print("(ERROR #" + node.getErrorNodes() + ")");
    }
}
