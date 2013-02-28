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

package com.truward.jnova.java.parser.impl;

import com.truward.jnova.java.ast.AstNodeKind;
import com.truward.jnova.java.ast.Ast;
import com.truward.jnova.java.ast.AstInfo;
import com.truward.jnova.java.code.*;
import com.truward.jnova.java.parser.Lexer;
import com.truward.jnova.java.parser.Parser;
import com.truward.jnova.java.parser.Token;
import com.truward.jnova.java.code.Convert;
import com.truward.jnova.java.source.Source;
import com.truward.jnova.util.ImmList;

import com.truward.jnova.util.diagnostics.parameter.Offset;
import com.truward.jnova.util.naming.Symbol;
import com.truward.jnova.util.diagnostics.DiagnosticsLog;
import com.truward.jnova.java.source.Position;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Parser implementation.
 */
@SuppressWarnings({"ConstantConditions"})
public final class ParserImpl implements Parser {
    /**
     * The number of precedence levels of infix operators.
     */
    private static final int INFIX_PRECEDENCE_LEVELS = 10;


    @Resource
    private Lexer lexer;

    @Resource
    private DiagnosticsLog log;

    @Resource
    private ParserBundle bundle;

    @Resource
    private PredefinedNames names;

    @Resource
    private Source source;


    private static final class LocalAstFactory extends Ast.Factory {
        private int pos = Position.NOPOS;

        @Override
        protected void onPostConstruct(Ast.Node node) {
            node.setPos(pos);
        }

        // TODO: method of 'PositionAware' interface.
        public LocalAstFactory at(int newPos) {
            pos = newPos;
            return this;
        }
    }


    private final LocalAstFactory factory = new LocalAstFactory();


    //
    // parser state fields
    //

    private int errorPos = Position.NOPOS;



    /** When terms are parsed, the mode determines which is expected:
     *     mode = EXPR        : an expression
     *     mode = TYPE        : a type
     *     mode = NOPARAMS    : no parameters allowed for type
     *     mode = TYPEARG     : type argument
     */
    static final int EXPR = 1;
    static final int TYPE = 2;
    static final int NOPARAMS = 4;
    static final int TYPEARG = 8;

    /** The current mode.
     */
    private int mode = 0;

    /** The mode of the term that was parsed last.
     */
    private int lastmode = 0;

    //
    // options
    //

    private boolean allowAsserts;

    private boolean allowEnums;

    private boolean allowGenerics;

    private boolean allowVarargs;

    private boolean allowForeach;

    private boolean allowStaticImport;

    private boolean allowAnnotations;

    /**
     * Initializes parser after construction.
     */
    @PostConstruct
    public void postConstruct() {
        // parsing options
        allowAsserts = source.allowAsserts();
        allowEnums = source.allowEnums();
        allowGenerics = source.allowGenerics();
        allowVarargs = source.allowGenerics();
        allowForeach = source.allowForeach();
        allowStaticImport = source.allowStaticImport();
        allowAnnotations = source.allowAnnotations();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ast.Node parseTest() {
        lexer.nextToken();
        switch (lexer.token()) {
            case IMPORT:
                return importDeclaration();

            case MONKEYS_AT:
                lexer.nextToken();
                return annotation(lexer.pos());

            default:
                return factory.astEmptyStatement();
        }
    }

    @Override
    public Ast.CompilationUnit parseCompilationUnit() {
        lexer.nextToken();
        return compilationUnit();
    }

    /* ---------- doc comments --------- */

//    /** A hashtable to store all documentation comments
//     *  indexed by the tree nodes they refer to.
//     *  defined only if option flag keepDocComment is set.
//     */
//    Map<Ast.Node, String> docComments;

    /** Make an entry into docComments hashtable,
     *  provided flag keepDocComments is set and given doc comment is non-null.
     *  @param node   The tree to be used as index in the hashtable
     *  @param dc     The doc comment to associate with the tree, or null.
     */
    void attach(Ast.Node node, String dc) {
        // TODO: implement
        if (dc != null) {
            assert node != null;
        }
//        if (keepDocComments && dc != null) {
//            docComments.put(tree, dc);
//        }
    }


    /* ---------- utility functions -------------- */

    private String tokenToString(Token token) {
        return token.getName() == null ? token.toString() : token.getName();
    }

    /* ---------- error recovery -------------- */

    // Skip forward until a suitable stop token is found.
    private void skip(boolean stopAtImport, boolean stopAtMemberDecl, boolean stopAtIdentifier, boolean stopAtStatement) {
        while (true) {
            switch (lexer.token()) {
                case SEMI:
                    lexer.nextToken();
                    return;
                case PUBLIC:
                case FINAL:
                case ABSTRACT:
                case MONKEYS_AT:
                case EOF:
                case CLASS:
                case INTERFACE:
                case ENUM:
                    return;
                case IMPORT:
                    if (stopAtImport)
                        return;
                    break;
                case LBRACE:
                case RBRACE:
                case PRIVATE:
                case PROTECTED:
                case STATIC:
                case TRANSIENT:
                case NATIVE:
                case VOLATILE:
                case SYNCHRONIZED:
                case STRICTFP:
                case LT:
                case BYTE:
                case SHORT:
                case CHAR:
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case BOOLEAN:
                case VOID:
                    if (stopAtMemberDecl)
                        return;
                    break;
                case IDENTIFIER:
                    if (stopAtIdentifier)
                        return;
                    break;
                case CASE:
                case DEFAULT:
                case IF:
                case FOR:
                case WHILE:
                case DO:
                case TRY:
                case SWITCH:
                case RETURN:
                case THROW:
                case BREAK:
                case CONTINUE:
                case ELSE:
                case FINALLY:
                case CATCH:
                    if (stopAtStatement)
                        return;
                    break;
            }
            lexer.nextToken();
        }
    }

    /* -------- source positions ------- */

    private int errorEndPos = -1;


    private void setErrorEndPos(int errPos) {
        if (errPos > errorEndPos) {
            errorEndPos = errPos;
        }
    }


    /* ---------- parsing -------------- */

    private void logError(String key, Object... args) {
        log.error(bundle.message(key, args), Offset.at(lexer.pos()));
    }

    private void logWarning(String key, Object... args) {
        log.warning(bundle.message(key, args), Offset.at(lexer.pos()));
    }

    private void reportSyntaxError(int pos, String error, Object... args) {
        if (pos > lexer.errPos() || pos == Position.NOPOS) {
            if (lexer.token() == Token.EOF) {
                log.error(bundle.message("premature.eof"));
            } else {
                log.error(bundle.message(error, args), Offset.at(pos));
            }
        }

        lexer.setErrPos(pos);

        if (lexer.pos() == errorPos) {
            lexer.nextToken(); // guarantee progress
        }

        errorPos = lexer.pos();
    }


    private Ast.Erroneous syntaxError(int pos, String key, Object... arg) {
        return syntaxError(pos, ImmList.<Ast.Node>nil(), key, arg);
    }

    private Ast.Erroneous syntaxError(int pos, ImmList<Ast.Node> errs, String key, Object... arg) {
        setErrorEndPos(pos);
        reportSyntaxError(pos, key, arg);
        return factory.at(pos).astErroneous(errs);
    }

    // Generate a syntax error at current position unless one was already reported at the same position.
    private Ast.Erroneous syntaxError(String key) {
        return syntaxError(lexer.pos(), key);
    }

    // Generate a syntax error at current position unless one was already reported at the same position.
    private Ast.Erroneous syntaxError(String key, String arg) {
        return syntaxError(lexer.pos(), key, arg);
    }

    // Report an illegal start of expression/type error at given position.
    Ast.Expression illegal(int pos) {
        setErrorEndPos(lexer.pos());
        if ((mode & EXPR) != 0)
            return syntaxError(pos, "illegal.start.of.expr");
        else
            return syntaxError(pos, "illegal.start.of.type");

    }

    /*  Report an illegal start of expression/type error at current position.
     */
    private Ast.Expression illegal() {
        return illegal(lexer.pos());
    }

    /**
     * Diagnose a modifier flag from the set, if any.
     * @param mods Modifiers flags.
     */
    private void checkNoMods(long mods) {
        if (mods != 0) {
            long lowestMod = mods & -mods;
            log.error(bundle.message("mod.not.allowed.here", Flags.toString(lowestMod).trim()), Offset.at(lexer.pos()));
        }
    }


    /**
     * If next input token matches given token, skip it, otherwise report an error.
     *
     * @param token Token to be expected.
     */
    public void accept(Token token) {
        if (lexer.token() == token) {
            lexer.nextToken();
        } else {
            setErrorEndPos(lexer.pos());
            reportSyntaxError(lexer.prevEndPos(), "expected1", tokenToString(token));
        }
    }




    /**
     * CompilationUnit = [ { "@" Annotation } PACKAGE Qualident ";"] {ImportDeclaration} {TypeDeclaration}
     * @return Ast compilation unit node.
     */
    private Ast.CompilationUnit compilationUnit() {
        int pos = lexer.pos();
        Ast.Expression pid = null;
        String dc = lexer.docComment();
        Ast.Modifiers mods = null;
        ImmList<Ast.Annotation> packageAnnotations = ImmList.nil();
        if (lexer.token() == Token.MONKEYS_AT) {
            mods = modifiersOpt();
        }

        if (lexer.token() == Token.PACKAGE) {
            if (mods != null) {
                checkNoMods(mods.getFlags());
                packageAnnotations = mods.getAnnotations();
                mods = null;
            }
            lexer.nextToken();
            pid = qualident();
            accept(Token.SEMI);
        }
        ImmList<Ast.Node> definitions = ImmList.nil();
        boolean checkForImports = true;
        while (lexer.token() != Token.EOF) {
            if (lexer.pos() <= errorEndPos) {
                // error recovery
                skip(checkForImports, false, false, false);
                if (lexer.token() == Token.EOF)
                    break;
            }
            if (checkForImports && mods == null && lexer.token() == Token.IMPORT) {
                definitions = definitions.append(importDeclaration());
            } else {
                Ast.Node def = typeDeclaration(mods);
                if (def instanceof Ast.ExpressionStatement)
                    def = ((Ast.ExpressionStatement)def).getExpression();
                definitions = definitions.append(def);
                if (def instanceof Ast.ClassDecl)
                    checkForImports = false;
                mods = null;
            }
        }
        Ast.CompilationUnit toplevel = factory.at(pos).astCompilationUnit(packageAnnotations, pid, definitions);
        attach(toplevel, dc);

//        // TODO: keepDocComments
//        if (keepDocComments) {
//            toplevel.docComments = docComments;
//        }
        return toplevel;
    }




    /**
     * ImportDeclaration = IMPORT [ STATIC ] Ident { "." Ident } [ "." "*" ] ";"
     * @return Import declaration.
     */
    public Ast.Node importDeclaration() {
        int pos = lexer.pos();
        lexer.nextToken();
        boolean importStatic = false;

        if (lexer.token() == Token.STATIC) {
            checkStaticImports();
            importStatic = true;
            lexer.nextToken();
        }

        Ast.Expression pid = factory.at(lexer.pos()).astIdent(ident());

        do {
            int pos1 = lexer.pos();
            accept(Token.DOT);
            if (lexer.token() == Token.STAR) {
                pid = factory.at(pos1).astFieldAccess(pid, names.asterisk);
                lexer.nextToken();
                break;
            } else {
                pid = factory.at(pos1).astFieldAccess(pid, ident());
            }
        } while (lexer.token() == Token.DOT);

        accept(Token.SEMI);
        return factory.at(pos).astImport(pid, importStatic);
    }


    /**
     * TypeDeclaration = ClassOrInterfaceOrEnumDeclaration
     *                   | ";"
     *
     * @param mods  Modifiers.
     * @return Type declaration or empty statement.
     */
    Ast.Node typeDeclaration(Ast.Modifiers mods) {
        int pos = lexer.pos();
        if (mods == null && lexer.token() == Token.SEMI) {
            lexer.nextToken();
            return factory.at(pos).astEmptyStatement();
        } else {
            String dc = lexer.docComment();
            return classOrInterfaceOrEnumDeclaration(modifiersOpt(mods), dc);
        }
    }


    /**
     * Ident = IDENTIFIER
     * @return The name, enclosed in the current identifier.
     */
    public Symbol ident() {
        if (lexer.token() == Token.IDENTIFIER) {
            Symbol name = lexer.name();
            lexer.nextToken();
            return name;
        } else if (lexer.token() == Token.ASSERT) {
            if (allowAsserts) {
                logError("assert.as.identifier");
                lexer.nextToken();
                return names.error;
            } else {
                logWarning("assert.as.identifier");
                Symbol name = lexer.name();
                lexer.nextToken();
                return name;
            }
        } else if (lexer.token() == Token.ENUM) {
            if (allowEnums) {
                logError("enum.as.identifier");
                lexer.nextToken();
                return names.error;
            } else {
                logWarning("enum.as.identifier");
                Symbol name = lexer.name();
                lexer.nextToken();
                return name;
            }
        }

        accept(Token.IDENTIFIER);
        return names.error;
    }



    /*
     * Literal =
     *     INTLITERAL
     *   | LONGLITERAL
     *   | FLOATLITERAL
     *   | DOUBLELITERAL
     *   | CHARLITERAL
     *   | STRINGLITERAL
     *   | TRUE
     *   | FALSE
     *   | NULL
     */
    Ast.Expression literal(Symbol prefix) {
        int pos = lexer.pos();
        Ast.Expression t = factory.astErroneous();
        switch (lexer.token()) {
            case INTLITERAL:
                try {
                    t = factory.at(pos).astLiteral(
                            TypeTags.INT,
                            Convert.string2int(strval(prefix), lexer.radix()));
                } catch (NumberFormatException ex) {
                    logError("int.number.too.large", strval(prefix));
                }
                break;
            case LONGLITERAL:
                try {
                    t = factory.at(pos).astLiteral(
                            TypeTags.LONG,
                            Convert.string2long(strval(prefix), lexer.radix()));
                } catch (NumberFormatException ex) {
                    logError("int.number.too.large", strval(prefix));
                }
                break;
            case FLOATLITERAL: {
                String proper = (lexer.radix() == 16 ? ("0x"+ lexer.stringVal()) : lexer.stringVal());
                Float n;
                try {
                    n = Float.valueOf(proper);
                } catch (NumberFormatException ex) {
                    // error already repoted in lexer
                    n = Float.NaN;
                }
                if (n == 0.0f && !isZero(proper)) {
                    logError("fp.number.too.small");
                } else if (n == Float.POSITIVE_INFINITY) {
                    logError("fp.number.too.large");
                } else {
                    t = factory.at(pos).astLiteral(TypeTags.FLOAT, n);
                }
                break;
            }
            case DOUBLELITERAL: {
                String proper = (lexer.radix() == 16 ? ("0x"+ lexer.stringVal()) : lexer.stringVal());
                Double n;
                try {
                    n = Double.valueOf(proper);
                } catch (NumberFormatException ex) {
                    // error already reported in lexer
                    n = Double.NaN;
                }
                if (n == 0.0d && !isZero(proper)) {
                    logError("fp.number.too.small");
                } else if (n == Double.POSITIVE_INFINITY) {
                    logError("fp.number.too.large");
                } else {
                    t = factory.at(pos).astLiteral(TypeTags.DOUBLE, n);
                }
                break;
            }
            case CHARLITERAL:
                t = factory.at(pos).astLiteral(
                        TypeTags.CHAR,
                        lexer.stringVal().charAt(0) + 0);
                break;
            case STRINGLITERAL:
                t = factory.at(pos).astLiteral(
                        TypeTags.CLASS,
                        lexer.stringVal());
                break;
            case TRUE: case FALSE:
                t = factory.at(pos).astLiteral(
                        TypeTags.BOOLEAN,
                        (lexer.token() == Token.TRUE ? 1 : 0));
                break;
            case NULL:
                t = factory.at(pos).astLiteral(
                        TypeTags.BOT,
                        null);
                break;
            default:
                assert false;
        }

        if (t instanceof Ast.Erroneous) {
            t.setPos(pos);
        }

        lexer.nextToken();
        return t;
    }

    private static boolean isZero(String s) {
        final char[] cs = s.toCharArray();
        final int base = ((cs.length > 1 && Character.toLowerCase(cs[1]) == 'x') ? 16 : 10);
        int i = ((base == 16) ? 2 : 0);

        while (i < cs.length && (cs[i] == '0' || cs[i] == '.')) {
            i++;
        }

        return !(i < cs.length && (Character.digit(cs[i], base) > 0));
    }

    private String strval(Symbol prefix) {
        final String s = lexer.stringVal();
        return (prefix.getUtfLength() == 0) ? s : prefix + s;
    }

    /*  Terms can be either expressions or types.
     */
    private Ast.Expression expression() {
        return term(EXPR);
    }

    private Ast.Expression type() {
        return term(TYPE);
    }

    private Ast.Expression term(int newmode) {
        int prevmode = mode;
        mode = newmode;
        Ast.Expression t = term();
        lastmode = mode;
        mode = prevmode;
        return t;
    }

    /*
     *  Expression = Expression1 [ExpressionRest]
     *  ExpressionRest = [AssignmentOperator Expression1]
     *  AssignmentOperator = "=" | "+=" | "-=" | "*=" | "/=" |
     *                       "&=" | "|=" | "^=" |
     *                       "%=" | "<<=" | ">>=" | ">>>="
     *  Type = Type1
     *  TypeNoParams = TypeNoParams1
     *  StatementExpression = Expression
     *  ConstantExpression = Expression
     */
    private Ast.Expression term() {
        Ast.Expression t = term1();

        if ((mode & EXPR) != 0 &&
                lexer.token() == Token.EQ ||
                Token.PLUSEQ.compareTo(lexer.token()) <= 0 &&
                        lexer.token().compareTo(Token.GTGTGTEQ) <= 0) {
            return termRest(t);
        }

        return t;
    }

    private Ast.Expression termRest(Ast.Expression t) {
        switch (lexer.token()) {
            case EQ: {
                int pos = lexer.pos();
                lexer.nextToken();
                mode = EXPR;
                Ast.Expression t1 = term();
                return factory.at(pos).astAssignment(t, t1);
            }
            case PLUSEQ:
            case SUBEQ:
            case STAREQ:
            case SLASHEQ:
            case PERCENTEQ:
            case AMPEQ:
            case BAREQ:
            case CARETEQ:
            case LTLTEQ:
            case GTGTEQ:
            case GTGTGTEQ:
                int pos = lexer.pos();
                Token token = lexer.token();
                lexer.nextToken();
                mode = EXPR;
                Ast.Expression t1 = term();
                return factory.at(pos).astCompoundAssignment(optag(token), t, t1);
            default:
                return t;
        }
    }

    /*  Expression1   = Expression2 [Expression1Rest]
     *  Type1         = Type2
     *  TypeNoParams1 = TypeNoParams2
     */
    private Ast.Expression term1() {
        Ast.Expression t = term2();
        if ((mode & EXPR) != 0 && lexer.token() == Token.QUES) {
            mode = EXPR;
            return term1Rest(t);
        } else {
            return t;
        }
    }

    /*  Expression1Rest = ["?" Expression ":" Expression1]
     */
    private Ast.Expression term1Rest(Ast.Expression t) {
        if (lexer.token() == Token.QUES) {
            int pos = lexer.pos();
            lexer.nextToken();
            Ast.Expression t1 = term();
            accept(Token.COLON);
            Ast.Expression t2 = term1();
            return factory.at(pos).astConditional(t, t1, t2);
        } else {
            return t;
        }
    }

    /*  Expression2   = Expression3 [Expression2Rest]
     *  Type2         = Type3
     *  TypeNoParams2 = TypeNoParams3
     */
    private Ast.Expression term2() {
        Ast.Expression t = term3();
        if ((mode & EXPR) != 0 && prec(lexer.token()) >= AstInfo.orPrec) {
            mode = EXPR;
            return term2Rest(t, AstInfo.orPrec);
        } else {
            return t;
        }
    }

    /*  Expression2Rest = {infixop Expression3}
     *                  | Expression3 instanceof Type
     *  infixop         = "||"
     *                  | "&&"
     *                  | "|"
     *                  | "^"
     *                  | "&"
     *                  | "==" | "!="
     *                  | "<" | ">" | "<=" | ">="
     *                  | "<<" | ">>" | ">>>"
     *                  | "+" | "-"
     *                  | "*" | "/" | "%"
     */
    private Ast.Expression term2Rest(Ast.Expression t, int minprec) {
        // TODO: optimize?
        final Ast.Expression[] odStack = new Ast.Expression[INFIX_PRECEDENCE_LEVELS + 1];
        final Token[] opStack = new Token[INFIX_PRECEDENCE_LEVELS + 1];

        int top = 0;
        odStack[0] = t;

        final int startPos = lexer.pos();
        Token topOp = Token.ERROR;

        while (prec(lexer.token()) >= minprec) {
            opStack[top] = topOp;
            top++;
            topOp = lexer.token();
            int pos = lexer.pos();
            lexer.nextToken();
            odStack[top] = topOp == Token.INSTANCEOF ? type() : term3();
            while (top > 0 && prec(topOp) >= prec(lexer.token())) {
                odStack[top-1] = makeOp(pos, topOp, odStack[top-1],
                        odStack[top]);
                top--;
                topOp = opStack[top];
            }
        }

        assert top == 0;
        t = odStack[0];

        if (t.getKind() == AstNodeKind.PLUS) {
            final String fold = foldStrings(t);
            if (fold != null) {
                t = factory.at(startPos).astLiteral(TypeTags.CLASS, fold);
            }
        }

        return t;
    }

    // Construct a binary or type test node.
    private Ast.Expression makeOp(int pos,
                                  Token topOp,
                                  Ast.Expression od1,
                                  Ast.Expression od2)
    {
        if (topOp == Token.INSTANCEOF) {
            return factory.at(pos).astInstanceOf(od1, od2);
        } else {
            return factory.at(pos).astBinary(optag(topOp), od1, od2);
        }
    }

    // If tree is a concatenation of string literals,
    // replace it by a single literal representing the concatenated string.
    protected String foldStrings(Ast.Node node) {
        ImmList<String> buf = ImmList.nil();
        while (true) {
            if (node.getKind() == AstNodeKind.LITERAL) {
                Ast.Literal lit = (Ast.Literal) node;
                if (lit.getTypeTag() == TypeTags.CLASS) {
                    final StringBuilder builder = new StringBuilder((String) lit.getValue());
                    while (!buf.isEmpty()) {
                        builder.append(buf.getHead());
                        buf = buf.getTail();
                    }
                    return builder.toString();
                }
            } else if (node.getKind() == AstNodeKind.PLUS) {
                Ast.Binary op = (Ast.Binary) node;
                if (op.getRightOperand().getKind() == AstNodeKind.LITERAL) {
                    Ast.Literal lit = (Ast.Literal) op.getRightOperand();
                    if (lit.getTypeTag() == TypeTags.CLASS) {
                        buf = buf.prepend((String) lit.getValue());
                        node = op.getLeftOperand();
                        continue;
                    }
                }
            }

            return null;
        }
    }

    /*  Expression3    = PrefixOp Expression3
     *                 | "(" Expr | TypeNoParams ")" Expression3
     *                 | Primary {Selector} {PostfixOp}
     *  Primary        = "(" Expression ")"
     *                 | Literal
     *                 | [TypeArguments] THIS [Arguments]
     *                 | [TypeArguments] SUPER SuperSuffix
     *                 | NEW [TypeArguments] Creator
     *                 | Ident { "." Ident }
     *                   [ "[" ( "]" BracketsOpt "." CLASS | Expression "]" )
     *                   | Arguments
     *                   | "." ( CLASS | THIS | [TypeArguments] SUPER Arguments | NEW [TypeArguments] InnerCreator )
     *                   ]
     *                 | BasicType BracketsOpt "." CLASS
     *  PrefixOp       = "++" | "--" | "!" | "~" | "+" | "-"
     *  PostfixOp      = "++" | "--"
     *  Type3          = Ident { "." Ident } [TypeArguments] {TypeSelector} BracketsOpt
     *                 | BasicType
     *  TypeNoParams3  = Ident { "." Ident } BracketsOpt
     *  Selector       = "." [TypeArguments] Ident [Arguments]
     *                 | "." THIS
     *                 | "." [TypeArguments] SUPER SuperSuffix
     *                 | "." NEW [TypeArguments] InnerCreator
     *                 | "[" Expression "]"
     *  TypeSelector   = "." Ident [TypeArguments]
     *  SuperSuffix    = Arguments | "." Ident [Arguments]
     */
    protected Ast.Expression term3() {
        int pos = lexer.pos();
        Ast.Expression t;
        ImmList<Ast.Expression> typeArgs = typeArgumentsOpt(EXPR);
        switch (lexer.token()) {
            case QUES:
                if ((mode & TYPE) != 0 && (mode & (TYPEARG | NOPARAMS)) == TYPEARG) {
                    mode = TYPE;
                    return typeArgument();
                } else
                    return illegal();
            case PLUSPLUS: case SUBSUB: case BANG: case TILDE: case PLUS: case SUB:
                if (typeArgs == null && (mode & EXPR) != 0) {
                    Token token = lexer.token();
                    lexer.nextToken();
                    mode = EXPR;
                    if (token == Token.SUB &&
                            (lexer.token() == Token.INTLITERAL || lexer.token() == Token.LONGLITERAL) &&
                            lexer.radix() == 10) {
                        mode = EXPR;
                        t = literal(names.hyphen);
                    } else {
                        t = term3();
                        return factory.at(pos).astUnary(unoptag(token), t);
                    }
                } else return illegal();
                break;
            case LPAREN:
                if (typeArgs == null && (mode & EXPR) != 0) {
                    lexer.nextToken();
                    mode = EXPR | TYPE | NOPARAMS;
                    t = term3();
                    if ((mode & TYPE) != 0 && lexer.token() == Token.LT) {
                        // Could be a cast to a parameterized type
                        int op = AstNodeKind.LT;
                        int pos1 = lexer.pos();
                        lexer.nextToken();
                        mode &= (EXPR | TYPE);
                        mode |= TYPEARG;
                        Ast.Expression t1 = term3();
                        if ((mode & TYPE) != 0 &&
                                (lexer.token() == Token.COMMA || lexer.token() == Token.GT)) {
                            mode = TYPE;

                            ImmList<Ast.Expression> args = ImmList.nil();
                            args = args.append(t1);
                            while (lexer.token() == Token.COMMA) {
                                lexer.nextToken();
                                args = args.append(typeArgument());
                            }
                            accept(Token.GT);
                            t = factory.at(pos1).astParameterizedType(t, args);
                            checkGenerics();
                            t = bracketsOpt(t);
                        } else if ((mode & EXPR) != 0) {
                            mode = EXPR;
                            t = factory.at(pos1).astBinary(op, t, term2Rest(t1, AstInfo.shiftPrec));
                            t = termRest(term1Rest(term2Rest(t, AstInfo.orPrec)));
                        } else {
                            accept(Token.GT);
                        }
                    } else {
                        t = termRest(term1Rest(term2Rest(t, AstInfo.orPrec)));
                    }
                    accept(Token.RPAREN);
                    lastmode = mode;
                    mode = EXPR;
                    if ((lastmode & EXPR) == 0) {
                        Ast.Expression t1 = term3();
                        return factory.at(pos).astTypeCast(t, t1);
                    } else if ((lastmode & TYPE) != 0) {
                        switch (lexer.token()) {
                            /*case PLUSPLUS: case SUBSUB: */
                            case BANG: case TILDE:
                            case LPAREN: case THIS: case SUPER:
                            case INTLITERAL: case LONGLITERAL: case FLOATLITERAL:
                            case DOUBLELITERAL: case CHARLITERAL: case STRINGLITERAL:
                            case TRUE: case FALSE: case NULL:
                            case NEW: case IDENTIFIER: case ASSERT: case ENUM:
                            case BYTE: case SHORT: case CHAR: case INT:
                            case LONG: case FLOAT: case DOUBLE: case BOOLEAN: case VOID:
                                Ast.Expression t1 = term3();
                                return factory.at(pos).astTypeCast(t, t1);
                        }
                    }
                } else return illegal();
                t = factory.at(pos).astParens(t);
                break;
            case THIS:
                if ((mode & EXPR) != 0) {
                    mode = EXPR;
                    t = factory.at(pos).astIdent(names._this);
                    lexer.nextToken();
                    if (typeArgs == null)
                        t = argumentsOpt(null, t);
                    else
                        t = arguments(typeArgs, t);
                    typeArgs = null;
                } else return illegal();
                break;
            case SUPER:
                if ((mode & EXPR) != 0) {
                    mode = EXPR;
                    t = superSuffix(typeArgs, factory.at(pos).astIdent(names._super));
                    typeArgs = null;
                } else return illegal();
                break;
            case INTLITERAL: case LONGLITERAL: case FLOATLITERAL: case DOUBLELITERAL:
            case CHARLITERAL: case STRINGLITERAL:
            case TRUE: case FALSE: case NULL:
                if (typeArgs == null && (mode & EXPR) != 0) {
                    mode = EXPR;
                    t = literal(names.empty);
                } else return illegal();
                break;
            case NEW:
                if (typeArgs != null) return illegal();
                if ((mode & EXPR) != 0) {
                    mode = EXPR;
                    lexer.nextToken();
                    if (lexer.token() == Token.LT) typeArgs = typeArguments();
                    t = creator(pos, typeArgs);
                    typeArgs = null;
                } else return illegal();
                break;
            case IDENTIFIER: case ASSERT: case ENUM:
                if (typeArgs != null) return illegal();
                t = factory.at(lexer.pos()).astIdent(ident());
                loop: while (true) {
                    pos = lexer.pos();
                    switch (lexer.token()) {
                        case LBRACKET:
                            lexer.nextToken();
                            if (lexer.token() == Token.RBRACKET) {
                                lexer.nextToken();
                                t = bracketsOpt(t);
                                t = factory.at(pos).astArrayType(t);
                                t = bracketsSuffix(t);
                            } else {
                                if ((mode & EXPR) != 0) {
                                    mode = EXPR;
                                    Ast.Expression t1 = term();
                                    t = factory.at(pos).astArrayAccess(t, t1);
                                }
                                accept(Token.RBRACKET);
                            }
                            break loop;
                        case LPAREN:
                            if ((mode & EXPR) != 0) {
                                mode = EXPR;
                                t = arguments(typeArgs, t);
                                typeArgs = null;
                            }
                            break loop;
                        case DOT:
                            lexer.nextToken();
                            typeArgs = typeArgumentsOpt(EXPR);
                            if ((mode & EXPR) != 0) {
                                switch (lexer.token()) {
                                    case CLASS:
                                        if (typeArgs != null) return illegal();
                                        mode = EXPR;
                                        t = factory.at(pos).astFieldAccess(t, names._class);
                                        lexer.nextToken();
                                        break loop;
                                    case THIS:
                                        if (typeArgs != null) return illegal();
                                        mode = EXPR;
                                        t = factory.at(pos).astFieldAccess(t, names._this);
                                        lexer.nextToken();
                                        break loop;
                                    case SUPER:
                                        mode = EXPR;
                                        t = factory.at(pos).astFieldAccess(t, names._super);
                                        t = superSuffix(typeArgs, t);
                                        typeArgs = null;
                                        break loop;
                                    case NEW:
                                        if (typeArgs != null) return illegal();
                                        mode = EXPR;
                                        int pos1 = lexer.pos();
                                        lexer.nextToken();
                                        if (lexer.token() == Token.LT) typeArgs = typeArguments();
                                        t = innerCreator(pos1, typeArgs, t);
                                        typeArgs = null;
                                        break loop;
                                }
                            }
                            // typeArgs saved for next loop iteration.
                            t = factory.at(pos).astFieldAccess(t, ident());
                            break;
                        default:
                            break loop;
                    }
                }
                if (typeArgs != null) illegal();
                t = typeArgumentsOpt(t);
                break;
            case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
            case DOUBLE: case BOOLEAN:
                if (typeArgs != null) illegal();
                t = bracketsSuffix(bracketsOpt(basicType()));
                break;
            case VOID:
                if (typeArgs != null) illegal();
                if ((mode & EXPR) != 0) {
                    lexer.nextToken();
                    if (lexer.token() == Token.DOT) {
                        Ast.PrimitiveType ti = factory.at(pos).astPrimitiveType(TypeTags.VOID);
                        t = bracketsSuffix(ti);
                    } else {
                        return illegal(pos);
                    }
                } else {
                    return illegal();
                }
                break;
            default:
                return illegal();
        }
        if (typeArgs != null) illegal();
        while (true) {
            int pos1 = lexer.pos();
            if (lexer.token() == Token.LBRACKET) {
                lexer.nextToken();
                if ((mode & TYPE) != 0) {
                    int oldmode = mode;
                    mode = TYPE;
                    if (lexer.token() == Token.RBRACKET) {
                        lexer.nextToken();
                        t = bracketsOpt(t);
                        t = factory.at(pos1).astArrayType(t);
                        return t;
                    }
                    mode = oldmode;
                }
                if ((mode & EXPR) != 0) {
                    mode = EXPR;
                    Ast.Expression t1 = term();
                    t = factory.at(pos1).astArrayAccess(t, t1);
                }
                accept(Token.RBRACKET);
            } else if (lexer.token() == Token.DOT) {
                lexer.nextToken();
                typeArgs = typeArgumentsOpt(EXPR);
                if (lexer.token() == Token.SUPER && (mode & EXPR) != 0) {
                    mode = EXPR;
                    t = factory.at(pos1).astFieldAccess(t, names._super);
                    lexer.nextToken();
                    t = arguments(typeArgs, t);
                    typeArgs = null;
                } else if (lexer.token() == Token.NEW && (mode & EXPR) != 0) {
                    if (typeArgs != null) return illegal();
                    mode = EXPR;
                    int pos2 = lexer.pos();
                    lexer.nextToken();
                    if (lexer.token() == Token.LT) typeArgs = typeArguments();
                    t = innerCreator(pos2, typeArgs, t);
                    typeArgs = null;
                } else {
                    t = factory.at(pos1).astFieldAccess(t, ident());
                    t = argumentsOpt(typeArgs, typeArgumentsOpt(t));
                    typeArgs = null;
                }
            } else {
                break;
            }
        }
        while ((lexer.token() == Token.PLUSPLUS || lexer.token() == Token.SUBSUB) && (mode & EXPR) != 0) {
            mode = EXPR;
            t = factory.at(lexer.pos()).astUnary(
                    lexer.token() == Token.PLUSPLUS ? AstNodeKind.POSTINC : AstNodeKind.POSTDEC, t);
            lexer.nextToken();
        }

        assert typeArgs == null; // Remove this if untrue
        return t;
    }

    /*  SuperSuffix = Arguments | "." [TypeArguments] Ident [Arguments]
     */
    Ast.Expression superSuffix(ImmList<Ast.Expression> typeArgs, Ast.Expression t) {
        lexer.nextToken();
        if (lexer.token() == Token.LPAREN || typeArgs != null) {
            t = arguments(typeArgs, t);
        } else {
            int pos = lexer.pos();
            accept(Token.DOT);
            typeArgs = (lexer.token() == Token.LT) ? typeArguments() : null;
            t = factory.at(pos).astFieldAccess(t, ident());
            t = argumentsOpt(typeArgs, t);
        }
        return t;
    }

    // BasicType = BYTE | SHORT | CHAR | INT | LONG | FLOAT | DOUBLE | BOOLEAN
    Ast.PrimitiveType basicType() {
        Ast.PrimitiveType t = factory.at(lexer.pos()).astPrimitiveType(typetag(lexer.token()));
        lexer.nextToken();
        return t;
    }


    // ArgumentsOpt = [ Arguments ]
    Ast.Expression argumentsOpt(ImmList<Ast.Expression> typeArgs, Ast.Expression t) {
        if ((mode & EXPR) != 0 && lexer.token() == Token.LPAREN || typeArgs != null) {
            mode = EXPR;
            return arguments(typeArgs, t);
        } else {
            return t;
        }
    }

    // Arguments = "(" [Expression { COMMA Expression }] ")"
    ImmList<Ast.Expression> arguments() {
        ImmList<Ast.Expression> args = ImmList.nil();
        if (lexer.token() == Token.LPAREN) {
            lexer.nextToken();
            if (lexer.token() != Token.RPAREN) {
                args = args.append(expression());
                while (lexer.token() == Token.COMMA) {
                    lexer.nextToken();
                    args = args.append(expression());
                }
            }
            accept(Token.RPAREN);
        } else {
            syntaxError(lexer.pos(), "expected", Token.LPAREN);
        }
        return args;
    }

    Ast.MethodInvocation arguments(ImmList<Ast.Expression> typeArgs, Ast.Expression t) {
        return factory.at(lexer.pos()).astMethodInvocation(typeArgs, t, arguments());
    }

    /*   TypeArgumentsOpt = [ TypeArguments ]
     */
    Ast.Expression typeArgumentsOpt(Ast.Expression t) {
        if (lexer.token() == Token.LT &&
                (mode & TYPE) != 0 &&
                (mode & NOPARAMS) == 0) {
            mode = TYPE;
            checkGenerics();
            return typeArguments(t);
        } else {
            return t;
        }
    }

    ImmList<Ast.Expression> typeArgumentsOpt() {
        return typeArgumentsOpt(TYPE);
    }

    ImmList<Ast.Expression> typeArgumentsOpt(int useMode) {
        if (lexer.token() == Token.LT) {
            checkGenerics();
            if ((mode & useMode) == 0 ||
                    (mode & NOPARAMS) != 0) {
                illegal();
            }
            mode = useMode;
            return typeArguments();
        }

        // TODO: ImmList.nil()
        return null;
    }

    //  TypeArguments  = "<" TypeArgument {"," TypeArgument} ">"
    ImmList<Ast.Expression> typeArguments() {
        ImmList<Ast.Expression> args = ImmList.nil();
        if (lexer.token() == Token.LT) {
            lexer.nextToken();
            args = args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            while (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                args = args.append(((mode & EXPR) == 0) ? typeArgument() : type());
            }
            switch (lexer.token()) {
                case GTGTGTEQ:
                    lexer.setToken(Token.GTGTEQ);
                    break;
                case GTGTEQ:
                    lexer.setToken(Token.GTEQ);
                    break;
                case GTEQ:
                    lexer.setToken(Token.EQ);
                    break;
                case GTGTGT:
                    lexer.setToken(Token.GTGT);
                    break;
                case GTGT:
                    lexer.setToken(Token.GT);
                    break;
                default:
                    accept(Token.GT);
                    break;
            }
        } else {
            syntaxError(lexer.pos(), "expected", Token.LT);
        }
        return args;
    }

    /*  TypeArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type {"&" Type}
     *               | "?" SUPER Type
     */
    Ast.Expression typeArgument() {
        if (lexer.token() != Token.QUES) return type();
        int pos = lexer.pos();
        lexer.nextToken();
        if (lexer.token() == Token.EXTENDS) {
            Ast.TypeBoundKind t = factory.at(lexer.pos()).astTypeBoundKind(BoundKind.EXTENDS);
            lexer.nextToken();
            return factory.at(pos).astWildcard(t, type());
        } else if (lexer.token() == Token.SUPER) {
            Ast.TypeBoundKind t = factory.at(lexer.pos()).astTypeBoundKind(BoundKind.SUPER);
            lexer.nextToken();
            return factory.at(pos).astWildcard(t, type());
        } else if (lexer.token() == Token.IDENTIFIER) {
            //error recovery
            reportSyntaxError(lexer.prevEndPos(), "expected3",
                    Token.GT, Token.EXTENDS, Token.SUPER);
            Ast.TypeBoundKind t = factory.at(Position.NOPOS).astTypeBoundKind(BoundKind.UNBOUND);
            Ast.Expression wc = factory.at(pos).astWildcard(t, null);
            Ast.Ident id = factory.at(lexer.pos()).astIdent(ident());
            return factory.at(pos).astErroneous(ImmList.<Ast.Node>of(wc, id));
        } else {
            Ast.TypeBoundKind t = factory.at(Position.NOPOS).astTypeBoundKind(BoundKind.UNBOUND);
            return factory.at(pos).astWildcard(t, null);
        }
    }

    Ast.ParameterizedType typeArguments(Ast.Expression t) {
        int pos = lexer.pos();
        ImmList<Ast.Expression> args = typeArguments();
        return factory.at(pos).astParameterizedType(t, args);
    }

    // BracketsOpt = {"[" "]"}
    private Ast.Expression bracketsOpt(Ast.Expression t) {
        if (lexer.token() == Token.LBRACKET) {
            int pos = lexer.pos();
            lexer.nextToken();
            t = bracketsOptCont(t, pos);
            factory.at(pos);
        }
        return t;
    }

    private Ast.ArrayType bracketsOptCont(Ast.Expression t, int pos) {
        accept(Token.RBRACKET);
        t = bracketsOpt(t);
        return factory.at(pos).astArrayType(t);
    }

    /*  BracketsSuffixExpr = "." CLASS
     *  BracketsSuffixType =
     */
    Ast.Expression bracketsSuffix(Ast.Expression t) {
        if ((mode & EXPR) != 0 && lexer.token() == Token.DOT) {
            mode = EXPR;
            final int pos = lexer.pos();
            lexer.nextToken();
            accept(Token.CLASS);
            if (lexer.pos() == errorEndPos) {
                // error recovery
                final Symbol name;
                if (lexer.token() == Token.IDENTIFIER) {
                    name = lexer.name();
                    lexer.nextToken();
                } else {
                    name = names.error;
                }
                t = factory.at(pos).astErroneous(ImmList.<Ast.Node>of(
                        factory.at(pos).astFieldAccess(t, name)));
            } else {
                t = factory.at(pos).astFieldAccess(t, names._class);
            }
        } else if ((mode & TYPE) != 0) {
            mode = TYPE;
        } else {
            syntaxError(lexer.pos(), "dot.class.expected");
        }
        return t;
    }


    /*
     *  Creator = Qualident [TypeArguments] ( ArrayCreatorRest | ClassCreatorRest )
     */
    private Ast.Expression creator(int newpos, ImmList<Ast.Expression> typeArgs) {
        switch (lexer.token()) {
            case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
            case DOUBLE: case BOOLEAN:
                if (typeArgs == null)
                    return arrayCreatorRest(newpos, basicType());
                break;
            default:
        }
        Ast.Expression t = qualident();
        int oldmode = mode;
        mode = TYPE;
        if (lexer.token() == Token.LT) {
            checkGenerics();
            t = typeArguments(t);
        }
        while (lexer.token() == Token.DOT) {
            int pos = lexer.pos();
            lexer.nextToken();
            t = factory.at(pos).astFieldAccess(t, ident());
            if (lexer.token() == Token.LT) {
                checkGenerics();
                t = typeArguments(t);
            }
        }
        mode = oldmode;
        if (lexer.token() == Token.LBRACKET) {
            Ast.Expression e = arrayCreatorRest(newpos, t);
            if (typeArgs != null) {
                int pos = newpos;
                if (!typeArgs.isEmpty() && typeArgs.getHead().getPos() != Position.NOPOS) {
                    // note: this should always happen but we should
                    // not rely on this as the parser is continuously
                    // modified to improve error recovery.
                    pos = typeArgs.getHead().getPos();
                }
                setErrorEndPos(lexer.prevEndPos());
                reportSyntaxError(pos, "cannot.create.array.with.type.arguments");
                return factory.at(newpos).astErroneous(typeArgs.prepend(e));
            }
            return e;
        } else if (lexer.token() == Token.LPAREN) {
            return classCreatorRest(newpos, null, typeArgs, t);
        } else {
            reportSyntaxError(lexer.pos(), "expected2", Token.LPAREN, Token.LBRACKET);
            t = factory.at(newpos).astNewClass(null, typeArgs, t, ImmList.<Ast.Expression>nil(), null);
            return factory.at(newpos).astErroneous(ImmList.<Ast.Node>of(t));
        }
    }

    /*  InnerCreator = Ident [TypeArguments] ClassCreatorRest
     */
    Ast.Expression innerCreator(int newpos, ImmList<Ast.Expression> typeArgs, Ast.Expression encl) {
        Ast.Expression t = factory.at(lexer.pos()).astIdent(ident());
        if (lexer.token() == Token.LT) {
            checkGenerics();
            t = typeArguments(t);
        }
        return classCreatorRest(newpos, encl, typeArgs, t);
    }

    /*  ArrayCreatorRest = "[" ( "]" BracketsOpt ArrayInitializer
     *                         | Expression "]" {"[" Expression "]"} BracketsOpt )
     */
    Ast.Expression arrayCreatorRest(int newpos, Ast.Expression elemtype) {
        accept(Token.LBRACKET);
        if (lexer.token() == Token.RBRACKET) {
            accept(Token.RBRACKET);
            elemtype = bracketsOpt(elemtype);
            if (lexer.token() == Token.LBRACE) {
                return arrayInitializer(newpos, elemtype);
            } else {
                return syntaxError(lexer.pos(), "array.dimension.missing");
            }
        } else {
            ImmList<Ast.Expression> dims = ImmList.nil();
            dims.append(expression());
            accept(Token.RBRACKET);
            while (lexer.token() == Token.LBRACKET) {
                int pos = lexer.pos();
                lexer.nextToken();
                if (lexer.token() == Token.RBRACKET) {
                    elemtype = bracketsOptCont(elemtype, pos);
                } else {
                    dims = dims.append(expression());
                    accept(Token.RBRACKET);
                }
            }
            return factory.at(newpos).astNewArray(elemtype, dims, null);
        }
    }

    /*  ClassCreatorRest = Arguments [ClassBody]
    */
    Ast.Expression classCreatorRest(int newpos,
                                    Ast.Expression encl,
                                    ImmList<Ast.Expression> typeArgs,
                                    Ast.Expression t)
    {
        ImmList<Ast.Expression> args = arguments();
        Ast.ClassDecl body = null;
        if (lexer.token() == Token.LBRACE) {
            int pos = lexer.pos();
            ImmList<Ast.Node> defs = classOrInterfaceBody(names.empty, false);
            Ast.Modifiers mods = factory.at(Position.NOPOS).astModifiers(0, ImmList.<Ast.Annotation>nil());

            // anonymous class decl
            body = factory.at(pos).astAnonymousClassDecl(names, mods, defs);
        }
        return factory.at(newpos).astNewClass(encl, typeArgs, t, args, body);
    }

    /*  ArrayInitializer = "{" [VariableInitializer {"," VariableInitializer}] [","] "}"
     */
    Ast.Expression arrayInitializer(int newpos, Ast.Expression t) {
        accept(Token.LBRACE);
        ImmList<Ast.Expression> elems = ImmList.nil();
        if (lexer.token() == Token.COMMA) {
            lexer.nextToken();
        } else if (lexer.token() != Token.RBRACE) {
            elems = elems.append(variableInitializer());
            while (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                if (lexer.token() == Token.RBRACE) break;
                elems = elems.append(variableInitializer());
            }
        }
        accept(Token.RBRACE);
        return factory.at(newpos).astNewArray(t, ImmList.<Ast.Expression>nil(), elems);
    }

    /*  VariableInitializer = ArrayInitializer | Expression
     */
    private Ast.Expression variableInitializer() {
        return lexer.token() == Token.LBRACE ?
                arrayInitializer(lexer.pos(), null) :
                expression();
    }


    /*
     *  ParExpression = "(" Expression ")"
     */
    private Ast.Expression parExpression() {
        accept(Token.LPAREN);
        Ast.Expression t = expression();
        accept(Token.RPAREN);
        return t;
    }

    /*
     *  Block = <modifiers, e.g. static> "{" BlockStatements "}"
     */
    Ast.Block block(int pos, long flags) {
        accept(Token.LBRACE);
        ImmList<? extends Ast.Statement> stats = blockStatements();
        Ast.Block t = factory.at(pos).astBlock(flags, stats);
        while (lexer.token() == Token.CASE || lexer.token() == Token.DEFAULT) {
            syntaxError("orphaned", tokenToString(lexer.token()));
            switchBlockStatementGroups();
        }
        // the Block node has a field "endpos" for first char of last token, which is
        // usually but not necessarily the last char of the last token.
        // TODO: associate endpos
        //t.endpos = lexer.pos();

        accept(Token.RBRACE);
        return t;
    }

    private Ast.Block block() {
        return block(lexer.pos(), 0);
    }

    /*  BlockStatements = { BlockStatement }
     *  BlockStatement  = LocalVariableDeclarationStatement
     *                  | ClassOrInterfaceOrEnumDeclaration
     *                  | [Ident ":"] Statement
     *  LocalVariableDeclarationStatement
     *                  = { FINAL | '@' Annotation } Type VariableDeclarators ";"
     */
    @SuppressWarnings({"fallthrough", "ConstantConditions"})
    ImmList<? extends Ast.Statement> blockStatements() {
        // TODO: skip to anchor on error(?)
        int lastErrPos = -1;
        ImmList<Ast.Statement> statements = ImmList.nil();
        while (true) {
            int pos = lexer.pos();
            switch (lexer.token()) {
                case RBRACE: case CASE: case DEFAULT: case EOF:
                    return statements;
                case LBRACE: case IF: case FOR: case WHILE: case DO: case TRY:
                case SWITCH: case SYNCHRONIZED: case RETURN: case THROW: case BREAK:
                case CONTINUE: case SEMI: case ELSE: case FINALLY: case CATCH:
                    statements = statements.append(statement());
                    break;
                case MONKEYS_AT:
                case FINAL: {
                    String dc = lexer.docComment();
                    Ast.Modifiers mods = modifiersOpt();
                    if (lexer.token() == Token.INTERFACE ||
                            lexer.token() == Token.CLASS ||
                            allowEnums && lexer.token() == Token.ENUM) {
                        statements = statements.append(classOrInterfaceOrEnumDeclaration(mods, dc));
                    } else {
                        Ast.Expression t = type();
                        statements = statements.appendList(variableDeclarators(mods, t));
                        // A "LocalVariableDeclarationStatement" subsumes the terminating semicolon
                        accept(Token.SEMI);
                    }
                    break;
                }
                case ABSTRACT: case STRICTFP: {
                    String dc = lexer.docComment();
                    Ast.Modifiers mods = modifiersOpt();
                    statements = statements.append(classOrInterfaceOrEnumDeclaration(mods, dc));
                    break;
                }
                case INTERFACE:
                case CLASS:
                    statements = statements.append(classOrInterfaceOrEnumDeclaration(modifiersOpt(),
                            lexer.docComment()));
                    break;
                case ENUM:
                case ASSERT:
                    if (allowEnums && lexer.token() == Token.ENUM) {
                        logError("local.enum");
                        statements = statements.
                                append(classOrInterfaceOrEnumDeclaration(modifiersOpt(),
                                        lexer.docComment()));
                        break;
                    } else if (allowAsserts && lexer.token() == Token.ASSERT) {
                        statements = statements.append(statement());
                        break;
                    }
                    /* fall through to default */
                default:
                    Symbol name = lexer.name();
                    Ast.Expression t = term(EXPR | TYPE);
                    if (lexer.token() == Token.COLON && t.getKind() == AstNodeKind.IDENT) {
                        lexer.nextToken();
                        Ast.Statement stat = statement();
                        statements = statements.append(factory.at(pos).astLabeledStatement(name, stat));
                    } else if ((lastmode & TYPE) != 0 &&
                            (lexer.token() == Token.IDENTIFIER ||
                                    lexer.token() == Token.ASSERT ||
                                    lexer.token() == Token.ENUM)) {
                        pos = lexer.pos();
                        Ast.Modifiers mods = factory.at(Position.NOPOS).astModifiers(0);
                        factory.at(pos);
                        statements = statements.appendList(variableDeclarators(mods, t));
                        // A "LocalVariableDeclarationStatement" subsumes the terminating semicolon
                        accept(Token.SEMI);
                    } else {
                        // This Exec is an "ExpressionStatement"; it subsumes the terminating semicolon
                        statements = statements.append(factory.at(pos).astExpressionStatement(checkExprStat(t)));
                        accept(Token.SEMI);
                    }
            }

            // error recovery
            if (lexer.pos() == lastErrPos)
                return statements;
            if (lexer.pos() <= errorEndPos) {
                skip(false, true, true, true);
                lastErrPos = lexer.pos();
            }

            // ensure no dangling /** @deprecated */ active
            lexer.resetDeprecatedFlag();
        }
    }

    /*  Statement =
     *       Block
     *     | IF ParExpression Statement [ELSE Statement]
     *     | FOR "(" ForInitOpt ";" [Expression] ";" ForUpdateOpt ")" Statement
     *     | FOR "(" FormalParameter : Expression ")" Statement
     *     | WHILE ParExpression Statement
     *     | DO Statement WHILE ParExpression ";"
     *     | TRY Block ( Catches | [Catches] FinallyPart )
     *     | SWITCH ParExpression "{" SwitchBlockStatementGroups "}"
     *     | SYNCHRONIZED ParExpression Block
     *     | RETURN [Expression] ";"
     *     | THROW Expression ";"
     *     | BREAK [Ident] ";"
     *     | CONTINUE [Ident] ";"
     *     | ASSERT Expression [ ":" Expression ] ";"
     *     | ";"
     *     | ExpressionStatement
     *     | Ident ":" Statement
     */
    @SuppressWarnings("fallthrough")
    private Ast.Statement statement() {
        int pos = lexer.pos();
        switch (lexer.token()) {
            case LBRACE:
                return block();
            case IF: {
                lexer.nextToken();
                Ast.Expression cond = parExpression();
                Ast.Statement thenpart = statement();
                Ast.Statement elsepart = null;
                if (lexer.token() == Token.ELSE) {
                    lexer.nextToken();
                    elsepart = statement();
                }
                return factory.at(pos).astIf(cond, thenpart, elsepart);
            }
            case FOR: {
                lexer.nextToken();
                accept(Token.LPAREN);
                ImmList<? extends Ast.Statement> inits = lexer.token() == Token.SEMI ? ImmList.<Ast.Statement>nil() : forInit();
                if (inits.size() == 1 &&
                        inits.getHead().getKind() == AstNodeKind.VARIABLE_DECL &&
                        ((Ast.VariableDecl) inits.getHead()).getInitializer() == null &&
                        lexer.token() == Token.COLON) {
                    checkForeach();
                    Ast.VariableDecl var = (Ast.VariableDecl) inits.getHead();
                    accept(Token.COLON);
                    Ast.Expression expr = expression();
                    accept(Token.RPAREN);
                    Ast.Statement body = statement();
                    return factory.at(pos).astForEachLoop(var, expr, body);
                } else {
                    accept(Token.SEMI);
                    Ast.Expression cond = lexer.token() == Token.SEMI ? null : expression();
                    accept(Token.SEMI);
                    ImmList<Ast.ExpressionStatement> steps = lexer.token() == Token.RPAREN ? ImmList.<Ast.ExpressionStatement>nil() : forUpdate();
                    accept(Token.RPAREN);
                    Ast.Statement body = statement();
                    return factory.at(pos).astForLoop(inits, cond, steps, body);
                }
            }
            case WHILE: {
                lexer.nextToken();
                Ast.Expression cond = parExpression();
                Ast.Statement body = statement();
                return factory.at(pos).astWhileLoop(cond, body);
            }
            case DO: {
                lexer.nextToken();
                Ast.Statement body = statement();
                accept(Token.WHILE);
                Ast.Expression cond = parExpression();
                Ast.DoWhileLoop t = factory.at(pos).astDoWhileLoop(body, cond);
                accept(Token.SEMI);
                return t;
            }
            case TRY: {
                lexer.nextToken();
                Ast.Block body = block();
                ImmList<Ast.Catch> catchers = ImmList.nil();
                Ast.Block finalizer = null;
                if (lexer.token() == Token.CATCH || lexer.token() == Token.FINALLY) {
                    while (lexer.token() == Token.CATCH) {
                        catchers = catchers.append(catchClause());
                    }
                    if (lexer.token() == Token.FINALLY) {
                        lexer.nextToken();
                        finalizer = block();
                    }
                } else {
                    logError("try.without.catch.or.finally");
                }
                return factory.at(pos).astTry(body, catchers, finalizer);
            }
            case SWITCH: {
                lexer.nextToken();
                Ast.Expression selector = parExpression();
                accept(Token.LBRACE);
                ImmList<Ast.Case> cases = switchBlockStatementGroups();
                Ast.Switch t = factory.at(pos).astSwitch(selector, cases);
                accept(Token.RBRACE);
                return t;
            }
            case SYNCHRONIZED: {
                lexer.nextToken();
                Ast.Expression lock = parExpression();
                Ast.Block body = block();
                return factory.at(pos).astSynchronized(lock, body);
            }
            case RETURN: {
                lexer.nextToken();
                Ast.Expression result = lexer.token() == Token.SEMI ? null : expression();
                Ast.Return t = factory.at(pos).astReturn(result);
                accept(Token.SEMI);
                return t;
            }
            case THROW: {
                lexer.nextToken();
                Ast.Expression exc = expression();
                Ast.Throw t = factory.at(pos).astThrow(exc);
                accept(Token.SEMI);
                return t;
            }
            case BREAK: {
                lexer.nextToken();
                Symbol label = (lexer.token() == Token.IDENTIFIER || lexer.token() == Token.ASSERT ||
                        lexer.token() == Token.ENUM) ? ident() : null;
                Ast.Break t = factory.at(pos).astBreak(label);
                accept(Token.SEMI);
                return t;
            }
            case CONTINUE: {
                lexer.nextToken();
                Symbol label = (lexer.token() == Token.IDENTIFIER || lexer.token() == Token.ASSERT ||
                        lexer.token() == Token.ENUM) ? ident() : null;
                Ast.Continue t =  factory.at(pos).astContinue(label);
                accept(Token.SEMI);
                return t;
            }
            case SEMI:
                lexer.nextToken();
                return factory.at(pos).astEmptyStatement();
            case ELSE:
                return factory.astExpressionStatement(syntaxError("else.without.if"));
            case FINALLY:
                return factory.astExpressionStatement(syntaxError("finally.without.try"));
            case CATCH:
                return factory.astExpressionStatement(syntaxError("catch.without.try"));
            case ASSERT: {
                if (allowAsserts && lexer.token() == Token.ASSERT) {
                    lexer.nextToken();
                    Ast.Expression assertion = expression();
                    Ast.Expression message = null;
                    if (lexer.token() == Token.COLON) {
                        lexer.nextToken();
                        message = expression();
                    }
                    Ast.Assert t = factory.at(pos).astAssert(assertion, message);
                    accept(Token.SEMI);
                    return t;
                }
                /* else fall through to default case */
            }
            case ENUM:
            default:
                Symbol name = lexer.name();
                Ast.Expression expr = expression();
                if (lexer.token() == Token.COLON && expr.getKind() == AstNodeKind.IDENT) {
                    lexer.nextToken();
                    Ast.Statement stat = statement();
                    return factory.at(pos).astLabeledStatement(name, stat);
                } else {
                    // This Exec is an "ExpressionStatement"; it subsumes the terminating semicolon
                    Ast.ExpressionStatement stat = factory.at(pos).astExpressionStatement(checkExprStat(expr));
                    accept(Token.SEMI);
                    return stat;
                }
        }
    }

    /*  CatchClause     = CATCH "(" FormalParameter ")" Block
     */
    private Ast.Catch catchClause() {
        int pos = lexer.pos();
        accept(Token.CATCH);
        accept(Token.LPAREN);
        Ast.VariableDecl formal = variableDeclaratorId(optFinal(Flags.PARAMETER), qualident());
        accept(Token.RPAREN);
        Ast.Block body = block();
        return factory.at(pos).astCatch(formal, body);
    }

    /*  SwitchBlockStatementGroups = { SwitchBlockStatementGroup }
     *  SwitchBlockStatementGroup = SwitchLabel BlockStatements
     *  SwitchLabel = CASE ConstantExpression ":" | DEFAULT ":"
     */
    private ImmList<Ast.Case> switchBlockStatementGroups() {
        ImmList<Ast.Case> cases = ImmList.nil();
        while (true) {
            int pos = lexer.pos();
            switch (lexer.token()) {
                case CASE: {
                    lexer.nextToken();
                    Ast.Expression pat = expression();
                    accept(Token.COLON);
                    ImmList<? extends Ast.Statement> stats = blockStatements();
                    cases = cases.append(factory.at(pos).astCase(pat, stats));
                    break;
                }
                case DEFAULT: {
                    lexer.nextToken();
                    accept(Token.COLON);
                    ImmList<? extends Ast.Statement> stats = blockStatements();
                    Ast.Case c = factory.at(pos).astCase(null, stats);
                    cases = cases.append(c);
                    break;
                }
                case RBRACE: case EOF:
                    return cases;
                default:
                    lexer.nextToken(); // to ensure progress
                    syntaxError(pos, "expected3",
                            tokenToString(Token.CASE),
                            tokenToString(Token.DEFAULT),
                            tokenToString(Token.RBRACE));
            }
        }
    }

    /*  MoreStatementExpressions = { COMMA StatementExpression }
     */
    private ImmList<Ast.ExpressionStatement> moreStatementExpressions(int pos, Ast.Expression first) {
        // This Exec is a "StatementExpression"; it subsumes no terminating token
        ImmList<Ast.ExpressionStatement> statements = ImmList.nil();

        statements = statements.append(factory.at(pos).astExpressionStatement(checkExprStat(first)));

        while (lexer.token() == Token.COMMA) {
            lexer.nextToken();
            pos = lexer.pos();
            Ast.Expression t = expression();
            // This Exec is a "StatementExpression"; it subsumes no terminating token
            statements = statements.append(factory.at(pos).astExpressionStatement(checkExprStat(t)));
        }

        return statements;
    }

    /*  ForInit = StatementExpression MoreStatementExpressions
     *           |  { FINAL | '@' Annotation } Type VariableDeclarators
     */
    private ImmList<? extends Ast.Statement> forInit() {
        int pos = lexer.pos();

        if (lexer.token() == Token.FINAL || lexer.token() == Token.MONKEYS_AT) {
            return variableDeclarators(optFinal(0), type());
        } else {
            Ast.Expression t = term(EXPR | TYPE);
            if ((lastmode & TYPE) != 0 && (lexer.token() == Token.IDENTIFIER ||
                    lexer.token() == Token.ASSERT || lexer.token() == Token.ENUM)) {
                return variableDeclarators(modifiersOpt(), t);
            } else {
                return moreStatementExpressions(pos, t);
            }
        }
    }

    /*  ForUpdate = StatementExpression MoreStatementExpressions
     */
    private ImmList<Ast.ExpressionStatement> forUpdate() {
        return moreStatementExpressions(lexer.pos(), expression());
    }


    /**
     * Qualident = Ident { DOT Ident }
     * @return Qualified identifier.
     */
    private Ast.Expression qualident() {
        Ast.Expression qualifiedIdent = factory.at(lexer.pos()).astIdent(ident());
        while (lexer.token() == Token.DOT) {
            final int pos = lexer.pos();
            lexer.nextToken();
            qualifiedIdent = factory.at(pos).astFieldAccess(qualifiedIdent, ident());
        }

        return qualifiedIdent;
    }

    /*  AnnotationsOpt = { '@' Annotation }
     */
    private ImmList<Ast.Annotation> annotationsOpt() {
        if (lexer.token() != Token.MONKEYS_AT) {
            return ImmList.nil(); // optimization
        }

        ImmList<Ast.Annotation> buf = ImmList.nil();

        while (lexer.token() == Token.MONKEYS_AT) {
            int pos = lexer.pos();
            lexer.nextToken();
            buf = buf.append(annotation(pos));
        }

        return buf;
    }

    /*  Annotation                  = "@" Qualident [ "(" AnnotationFieldValues ")" ]
     */
    private Ast.Annotation annotation(int pos) {
        // AT consumed by caller
        checkAnnotations();
        Ast.Expression ident = qualident();
        ImmList<Ast.Expression> fieldValues = annotationFieldValuesOpt();
        return factory.at(pos).astAnnotation(ident, fieldValues);
    }

    private ImmList<Ast.Expression> annotationFieldValuesOpt() {
        return (lexer.token() == Token.LPAREN) ?
                annotationFieldValues() :
                ImmList.<Ast.Expression>nil();
    }

    /*  AnnotationFieldValues       = "(" [ AnnotationFieldValue { "," AnnotationFieldValue } ] ")"
     */
    private ImmList<Ast.Expression> annotationFieldValues() {
        accept(Token.LPAREN);
        ImmList<Ast.Expression> buf = ImmList.nil();
        if (lexer.token() != Token.RPAREN) {
            buf = buf.append(annotationFieldValue());
            while (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                buf = buf.append(annotationFieldValue());
            }
        }
        accept(Token.RPAREN);
        return buf;
    }

    /*  AnnotationFieldValue        = AnnotationValue | Identifier "=" AnnotationValue
     */
    private Ast.Expression annotationFieldValue() {
        if (lexer.token() == Token.IDENTIFIER) {
            mode = EXPR;
            Ast.Expression t1 = term1();
            if (t1.getKind() == AstNodeKind.IDENT && lexer.token() == Token.EQ) {
                int pos = lexer.pos();
                accept(Token.EQ);
                return factory.at(pos).astAssignment(t1, annotationValue());
            } else {
                return t1;
            }
        }

        return annotationValue();
    }

    // AnnotationValue          = ConditionalExpression
    //                          | Annotation
    //                          | "{" [ AnnotationValue { "," AnnotationValue } ] [","] "}"
    private Ast.Expression annotationValue() {
        int pos;
        switch (lexer.token()) {
            case MONKEYS_AT:
                pos = lexer.pos();
                lexer.nextToken();
                return annotation(pos);
            case LBRACE:
                pos = lexer.pos();
                accept(Token.LBRACE);
                ImmList<Ast.Expression> buf = ImmList.nil();
                if (lexer.token() != Token.RBRACE) {
                    buf.append(annotationValue());
                    while (lexer.token() == Token.COMMA) {
                        lexer.nextToken();
                        if (lexer.token() == Token.RBRACE) break;
                        buf.append(annotationValue());
                    }
                }
                accept(Token.RBRACE);
                return factory.at(pos).astNewArray(null, ImmList.<Ast.Expression>nil(), buf);
            default:
                mode = EXPR;
                return term1();
        }
    }


    /*  VariableDeclarators = VariableDeclarator { "," VariableDeclarator }
     */
    private  ImmList<Ast.VariableDecl> variableDeclarators(Ast.Modifiers mods, Ast.Expression type) {
        return variableDeclaratorsRest(lexer.pos(), mods, type, ident(), false, null);
    }

    /*  VariableDeclaratorsRest = VariableDeclaratorRest { "," VariableDeclarator }
     *  ConstantDeclaratorsRest = ConstantDeclaratorRest { "," ConstantDeclarator }
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    private ImmList<Ast.VariableDecl> variableDeclaratorsRest(int pos,
                                                              Ast.Modifiers mods,
                                                              Ast.Expression type,
                                                              Symbol name,
                                                              boolean reqInit,
                                                              String dc) {
        ImmList<Ast.VariableDecl> variables = ImmList.nil();

        variables = variables.append(variableDeclaratorRest(pos, mods, type, name, reqInit, dc));

        while (lexer.token() == Token.COMMA) {
            // All but last of multiple declarators subsume a comma
            lexer.nextToken();
            variables = variables.append(variableDeclarator(mods, type, reqInit, dc));
        }
        return variables;
    }

    /*  VariableDeclarator = Ident VariableDeclaratorRest
     *  ConstantDeclarator = Ident ConstantDeclaratorRest
     */
    private Ast.VariableDecl variableDeclarator(Ast.Modifiers mods, Ast.Expression type, boolean reqInit, String dc) {
        return variableDeclaratorRest(lexer.pos(), mods, type, ident(), reqInit, dc);
    }

    /*  VariableDeclaratorRest = BracketsOpt ["=" VariableInitializer]
     *  ConstantDeclaratorRest = BracketsOpt "=" VariableInitializer
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    private Ast.VariableDecl variableDeclaratorRest(int pos, Ast.Modifiers mods, Ast.Expression type, Symbol name,
                                                    boolean reqInit, String dc) {
        type = bracketsOpt(type);
        Ast.Expression init = null;
        if (lexer.token() == Token.EQ) {
            lexer.nextToken();
            init = variableInitializer();
        }
        else if (reqInit) syntaxError(lexer.pos(), "expected", tokenToString(Token.EQ));
        Ast.VariableDecl result =
                factory.at(pos).astVariableDecl(mods, name, type, init);
        attach(result, dc);
        return result;
    }

    /*  VariableDeclaratorId = Ident BracketsOpt
     */
    private Ast.VariableDecl variableDeclaratorId(Ast.Modifiers mods, Ast.Expression type) {
        int pos = lexer.pos();
        Symbol name = ident();
        if ((mods.getFlags() & Flags.VARARGS) == 0) {
            type = bracketsOpt(type);
        }
        return factory.at(pos).astVariableDecl(mods, name, type, null);
    }

    /*  ModifiersOpt = { Modifier }
     *  Modifier = PUBLIC | PROTECTED | PRIVATE | STATIC | ABSTRACT | FINAL
     *           | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | "@"
     *           | "@" Annotation
     */
    private Ast.Modifiers modifiersOpt() {
        return modifiersOpt(null);
    }

    private Ast.Modifiers modifiersOpt(Ast.Modifiers partial) {
        long flags = (partial == null) ? 0 : partial.getFlags();
        if (lexer.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            lexer.resetDeprecatedFlag();
        }
        ImmList<Ast.Annotation> annotations = ImmList.nil();
        if (partial != null) {
            annotations = annotations.appendList(partial.getAnnotations());
        }
        int pos = lexer.pos();
        int lastPos;

        loop:
        while (true) {
            long flag;
            switch (lexer.token()) {
                case PRIVATE     : flag = Flags.PRIVATE; break;
                case PROTECTED   : flag = Flags.PROTECTED; break;
                case PUBLIC      : flag = Flags.PUBLIC; break;
                case STATIC      : flag = Flags.STATIC; break;
                case TRANSIENT   : flag = Flags.TRANSIENT; break;
                case FINAL       : flag = Flags.FINAL; break;
                case ABSTRACT    : flag = Flags.ABSTRACT; break;
                case NATIVE      : flag = Flags.NATIVE; break;
                case VOLATILE    : flag = Flags.VOLATILE; break;
                case SYNCHRONIZED: flag = Flags.SYNCHRONIZED; break;
                case STRICTFP    : flag = Flags.STRICTFP; break;
                case MONKEYS_AT  : flag = Flags.ANNOTATION; break;
                default: break loop;
            }
            if ((flags & flag) != 0) {
                logError("repeated.modifier");
            }
            lastPos = lexer.pos();
            lexer.nextToken();
            if (flag == Flags.ANNOTATION) {
                checkAnnotations();
                if (lexer.token() != Token.INTERFACE) {
                    Ast.Annotation ann = annotation(lastPos);
                    // if first modifier is an annotation, set pos to annotation's.
                    if (flags == 0 && annotations.isEmpty()) {
                        pos = ann.getPos();
                    }

                    annotations = annotations.append(ann);
                    flag = 0;
                }
            }
            flags |= flag;
        }

        switch (lexer.token()) {
            case ENUM: flags |= Flags.ENUM; break;
            case INTERFACE: flags |= Flags.INTERFACE; break;
            default: break;
        }

        // A modifiers tree with no modifier tokens or annotations has no text position.
        if (flags == 0 && annotations.isEmpty()) {
            pos = Position.NOPOS;
        }

        return factory.at(pos).astModifiers(flags, annotations);
    }


    /*  MethodDeclaratorRest =
     *      FormalParameters BracketsOpt [Throws TypeList] ( MethodBody | [DEFAULT AnnotationValue] ";")
     *  VoidMethodDeclaratorRest =
     *      FormalParameters [Throws TypeList] ( MethodBody | ";")
     *  InterfaceMethodDeclaratorRest =
     *      FormalParameters BracketsOpt [THROWS TypeList] ";"
     *  VoidInterfaceMethodDeclaratorRest =
     *      FormalParameters [THROWS TypeList] ";"
     *  ConstructorDeclaratorRest =
     *      "(" FormalParameterListOpt ")" [THROWS TypeList] MethodBody
     */
    private Ast.Node methodDeclaratorRest(int pos,
                                          Ast.Modifiers mods,
                                          Ast.Expression type,
                                          Symbol name,
                                          ImmList<Ast.TypeParameter> typarams,
                                          boolean isVoid,
                                          String dc) {
        ImmList<Ast.VariableDecl> params = formalParameters();
        if (!isVoid) type = bracketsOpt(type);
        ImmList<Ast.Expression> thrown = ImmList.nil();
        if (lexer.token() == Token.THROWS) {
            lexer.nextToken();
            thrown = qualidentList();
        }
        Ast.Block body = null;
        Ast.Expression defaultValue;
        if (lexer.token() == Token.LBRACE) {
            body = block();
            defaultValue = null;
        } else {
            if (lexer.token() == Token.DEFAULT) {
                accept(Token.DEFAULT);
                defaultValue = annotationValue();
            } else {
                defaultValue = null;
            }
            accept(Token.SEMI);
            if (lexer.pos() <= errorEndPos) {
                // error recovery
                skip(false, true, false, false);
                if (lexer.token() == Token.LBRACE) {
                    body = block();
                }
            }
        }

        Ast.MethodDecl result = factory.at(pos).astMethodDecl(mods, name, type, typarams,
                params, thrown,
                body, defaultValue);

        attach(result, dc);
        return result;
    }


    /*  ClassBody     = "{" {ClassBodyDeclaration} "}"
     *  InterfaceBody = "{" {InterfaceBodyDeclaration} "}"
     */
    private ImmList<Ast.Node> classOrInterfaceBody(Symbol className, boolean isInterface) {
        accept(Token.LBRACE);
        if (lexer.pos() <= errorEndPos) {
            // error recovery
            skip(false, true, false, false);
            if (lexer.token() == Token.LBRACE) {
                lexer.nextToken();
            }
        }

        ImmList<Ast.Node> defs = ImmList.nil();
        while (lexer.token() != Token.RBRACE && lexer.token() != Token.EOF) {
            defs = defs.appendList(classOrInterfaceBodyDeclaration(className, isInterface));
            if (lexer.pos() <= errorEndPos) {
                // error recovery
                skip(false, true, true, false);
            }
        }

        accept(Token.RBRACE);
        return defs;
    }

    /*  ClassBodyDeclaration =
     *      ";"
     *    | [STATIC] Block
     *    | ModifiersOpt
     *      ( Type Ident
     *        ( VariableDeclaratorsRest ";" | MethodDeclaratorRest )
     *      | VOID Ident MethodDeclaratorRest
     *      | TypeParameters (Type | VOID) Ident MethodDeclaratorRest
     *      | Ident ConstructorDeclaratorRest
     *      | TypeParameters Ident ConstructorDeclaratorRest
     *      | ClassOrInterfaceOrEnumDeclaration
     *      )
     *  InterfaceBodyDeclaration =
     *      ";"
     *    | ModifiersOpt Type Ident
     *      ( ConstantDeclaratorsRest | InterfaceMethodDeclaratorRest ";" )
     */
    private ImmList<? extends Ast.Node> classOrInterfaceBodyDeclaration(Symbol className, boolean isInterface) {
        if (lexer.token() == Token.SEMI) {
            lexer.nextToken();
            return ImmList.<Ast.Node>of(factory.at(Position.NOPOS).astBlock(0, ImmList.<Ast.Statement>nil()));
        } else {
            String dc = lexer.docComment();
            int pos = lexer.pos();
            Ast.Modifiers mods = modifiersOpt();
            if (lexer.token() == Token.CLASS ||
                    lexer.token() == Token.INTERFACE ||
                    allowEnums && lexer.token() == Token.ENUM) {
                return ImmList.<Ast.Node>of(classOrInterfaceOrEnumDeclaration(mods, dc));
            } else if (lexer.token() == Token.LBRACE && !isInterface &&
                    (mods.getFlags() & Flags.StandardFlags & ~Flags.STATIC) == 0 &&
                    mods.getAnnotations().isEmpty()) {
                return ImmList.<Ast.Node>of(block(pos, mods.getFlags()));
            } else {
                pos = lexer.pos();
                ImmList<Ast.TypeParameter> typarams = typeParametersOpt();
                // Hack alert:  if there are type arguments but no Modifiers, the start
                // position will be lost unless we set the Modifiers position.  There
                // should be an AST node for type parameters (BugId 5005090).
                if (typarams.size() > 0 && mods.getPos() == Position.NOPOS) {
                    mods.setPos(pos);
                }
                Symbol name = lexer.name();
                pos = lexer.pos();
                Ast.Expression type;
                boolean isVoid = lexer.token() == Token.VOID;
                if (isVoid) {
                    type = factory.at(pos).astPrimitiveType(TypeTags.VOID);
                    lexer.nextToken();
                } else {
                    type = type();
                }
                if (lexer.token() == Token.LPAREN && !isInterface && type.getKind() == AstNodeKind.IDENT) {
                    if (isInterface || name != className) {
                        logError("invalid.meth.decl.ret.type.req");
                    }
                    return ImmList.of(methodDeclaratorRest(
                            pos, mods, null, names.init, typarams,
                            true, dc));
                } else {
                    pos = lexer.pos();
                    name = ident();
                    if (lexer.token() == Token.LPAREN) {
                        return ImmList.of(methodDeclaratorRest(
                                pos, mods, type, name, typarams,
                                isVoid, dc));
                    } else if (!isVoid && typarams.isEmpty()) {
                        final ImmList<? extends Ast.Node> defs = variableDeclaratorsRest(
                                pos, mods, type, name, isInterface, dc);

                        accept(Token.SEMI);

                        return defs;
                    } else {
                        pos = lexer.pos();
                        final ImmList<Ast.Node> err = isVoid ?
                                ImmList.<Ast.Node>of(factory.at(pos).astMethodDecl(
                                        mods, name, type, typarams,
                                        ImmList.<Ast.VariableDecl>nil(),
                                        ImmList.<Ast.Expression>nil(), null, null)) :
                                null;

                        return ImmList.<Ast.Node>of(syntaxError(lexer.pos(), err, "expected", tokenToString(Token.LPAREN)));
                    }
                }
            }
        }
    }

    /*  ClassOrInterfaceOrEnumDeclaration = ModifiersOpt
     *           (ClassDeclaration | InterfaceDeclaration | EnumDeclaration)
     */
    private Ast.Statement classOrInterfaceOrEnumDeclaration(Ast.Modifiers mods, String dc) {
        if (lexer.token() == Token.CLASS) {
            return classDeclaration(mods, dc);
        } else if (lexer.token() == Token.INTERFACE) {
            return interfaceDeclaration(mods, dc);
        } else if (allowEnums) {
            if (lexer.token() == Token.ENUM) {
                return enumDeclaration(mods, dc);
            } else {
                int pos = lexer.pos();
                ImmList<Ast.Node> errs;
                if (lexer.token() == Token.IDENTIFIER) {
                    errs = ImmList.of(mods, factory.at(pos).astIdent(ident()));
                    setErrorEndPos(lexer.pos());
                } else {
                    errs = ImmList.<Ast.Node>of(mods);
                }
                return factory.astExpressionStatement(syntaxError(pos, errs, "expected3",
                        tokenToString(Token.CLASS),
                        tokenToString(Token.INTERFACE),
                        tokenToString(Token.ENUM)));
            }
        } else {
            if (lexer.token() == Token.ENUM) {
                logError("enums.not.supported.in.source", source.name);
                allowEnums = true;
                return enumDeclaration(mods, dc);
            }
            int pos = lexer.pos();
            ImmList<Ast.Node> errs;
            if (lexer.token() == Token.IDENTIFIER) {
                errs = ImmList.of(mods, factory.at(pos).astIdent(ident()));
                setErrorEndPos(lexer.pos());
            } else {
                errs = ImmList.<Ast.Node>of(mods);
            }
            return factory.astExpressionStatement(syntaxError(pos, errs, "expected2",
                    tokenToString(Token.CLASS),
                    tokenToString(Token.INTERFACE)));
        }
    }

    /*  ClassDeclaration = CLASS Ident TypeParametersOpt [EXTENDS Type]
     *                     [IMPLEMENTS TypeList] ClassBody
     */
    private Ast.ClassDecl classDeclaration(Ast.Modifiers mods, String dc) {
        int pos = lexer.pos();
        accept(Token.CLASS);
        Symbol name = ident();

        ImmList<Ast.TypeParameter> typarams = typeParametersOpt();

        Ast.Expression extending = null;
        if (lexer.token() == Token.EXTENDS) {
            lexer.nextToken();
            extending = type();
        }
        ImmList<Ast.Expression> implementing = ImmList.nil();
        if (lexer.token() == Token.IMPLEMENTS) {
            lexer.nextToken();
            implementing = typeList();
        }
        ImmList<Ast.Node> defs = classOrInterfaceBody(name, false);
        Ast.ClassDecl result = factory.at(pos).astClassDecl(
                mods, name, typarams, extending, implementing, defs);
        attach(result, dc);
        return result;
    }

    /*  InterfaceDeclaration = INTERFACE Ident TypeParametersOpt
     *                         [EXTENDS TypeList] InterfaceBody
     */
    private Ast.ClassDecl interfaceDeclaration(Ast.Modifiers mods, String dc) {
        int pos = lexer.pos();
        accept(Token.INTERFACE);
        Symbol name = ident();

        ImmList<Ast.TypeParameter> typarams = typeParametersOpt();

        ImmList<Ast.Expression> extending = ImmList.nil();
        if (lexer.token() == Token.EXTENDS) {
            lexer.nextToken();
            extending = typeList();
        }
        ImmList<Ast.Node> defs = classOrInterfaceBody(name, true);
        Ast.ClassDecl result = factory.at(pos).astClassDecl(
                mods, name, typarams, null, extending, defs);
        attach(result, dc);
        return result;
    }

    /*  EnumDeclaration = ENUM Ident [IMPLEMENTS TypeList] EnumBody
     */
    private Ast.ClassDecl enumDeclaration(Ast.Modifiers mods, String dc) {
        int pos = lexer.pos();
        accept(Token.ENUM);
        Symbol name = ident();

        ImmList<Ast.Expression> implementing = ImmList.nil();
        if (lexer.token() == Token.IMPLEMENTS) {
            lexer.nextToken();
            implementing = typeList();
        }

        ImmList<Ast.Node> defs = enumBody(name);
        Ast.Modifiers newMods = factory.at(mods.getPos())
                .astModifiers(mods.getFlags() | Flags.ENUM, mods.getAnnotations());

        Ast.ClassDecl result = factory.at(pos)
                .astClassDecl(newMods, name, ImmList.<Ast.TypeParameter>nil(),
                        null, implementing, defs);
        attach(result, dc);
        return result;
    }

    /*  EnumBody = "{" { EnumeratorDeclarationList } [","]
     *                  [ ";" {ClassBodyDeclaration} ] "}"
     */
    private ImmList<Ast.Node> enumBody(Symbol enumName) {
        accept(Token.LBRACE);
        ImmList<Ast.Node> definitions = ImmList.nil();

        if (lexer.token() == Token.COMMA) {
            lexer.nextToken();
        } else if (lexer.token() != Token.RBRACE && lexer.token() != Token.SEMI) {
            definitions = definitions.append(enumeratorDeclaration(enumName));
            while (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                if (lexer.token() == Token.RBRACE || lexer.token() == Token.SEMI) break;
                definitions = definitions.append(enumeratorDeclaration(enumName));
            }
            if (lexer.token() != Token.SEMI && lexer.token() != Token.RBRACE) {
                definitions = definitions.append(syntaxError(lexer.pos(), "expected3",
                        tokenToString(Token.COMMA),
                        tokenToString(Token.RBRACE),
                        tokenToString(Token.SEMI)));
                lexer.nextToken();
            }
        }
        if (lexer.token() == Token.SEMI) {
            lexer.nextToken();
            while (lexer.token() != Token.RBRACE && lexer.token() != Token.EOF) {
                definitions = definitions.appendList(classOrInterfaceBodyDeclaration(enumName,
                        false));
                if (lexer.pos() <= errorEndPos) {
                    // error recovery
                    skip(false, true, true, false);
                }
            }
        }

        accept(Token.RBRACE);
        return definitions;
    }


    /*  EnumeratorDeclaration = AnnotationsOpt [TypeArguments] IDENTIFIER [ Arguments ] [ "{" ClassBody "}" ]
     */
    private Ast.Node enumeratorDeclaration(Symbol enumName) {
        String dc = lexer.docComment();
        int flags = Flags.PUBLIC | Flags.STATIC | Flags.FINAL | Flags.ENUM;
        if (lexer.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            lexer.resetDeprecatedFlag();
        }
        int pos = lexer.pos();
        ImmList<Ast.Annotation> annotations = annotationsOpt();
        Ast.Modifiers mods = factory.at(annotations.isEmpty() ? Position.NOPOS : pos).astModifiers(flags, annotations);
        ImmList<Ast.Expression> typeArgs = typeArgumentsOpt();
        int identPos = lexer.pos();
        Symbol name = ident();
        int createPos = lexer.pos();
        ImmList<Ast.Expression> args = (lexer.token() == Token.LPAREN)
                ? arguments() : ImmList.<Ast.Expression>nil();
        Ast.ClassDecl body = null;
        if (lexer.token() == Token.LBRACE) {
            Ast.Modifiers mods1 = factory.at(Position.NOPOS).astModifiers(Flags.ENUM | Flags.STATIC, ImmList.<Ast.Annotation>nil());
            ImmList<Ast.Node> defs = classOrInterfaceBody(names.empty, false);
            body = factory.at(identPos).astAnonymousClassDecl(names, mods1, defs);
        }
        if (args.isEmpty() && body == null)
            createPos = Position.NOPOS;
        Ast.Ident ident = factory.at(Position.NOPOS).astIdent(enumName);
        Ast.NewClass create = factory.at(createPos).astNewClass(null, typeArgs, ident, args, body);
//        if (createPos != Position.NOPOS)
//            storeEnd(create, lexer.prevEndPos());
        ident = factory.at(Position.NOPOS).astIdent(enumName);
        Ast.Node result = factory.at(pos).astVariableDecl(mods, name, ident, create);
        attach(result, dc);
        return result;
    }

    /*  TypeList = Type {"," Type}
     */
    private ImmList<Ast.Expression> typeList() {
        ImmList<Ast.Expression> ts = ImmList.nil();
        ts = ts.append(type());
        while (lexer.token() == Token.COMMA) {
            lexer.nextToken();
            ts = ts.append(type());
        }
        return ts;
    }


    /*  QualidentList = Qualident {"," Qualident}
     */
    private ImmList<Ast.Expression> qualidentList() {
        ImmList<Ast.Expression> ts = ImmList.nil();
        ts = ts.append(qualident());
        while (lexer.token() == Token.COMMA) {
            lexer.nextToken();
            ts = ts.append(qualident());
        }
        return ts;
    }


    /*  TypeParametersOpt = ["<" TypeParameter {"," TypeParameter} ">"]
     */
    private ImmList<Ast.TypeParameter> typeParametersOpt() {
        ImmList<Ast.TypeParameter> typarams = ImmList.nil();

        if (lexer.token() == Token.LT) {
            checkGenerics();
            lexer.nextToken();
            typarams = typarams.append(typeParameter());
            while (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                typarams = typarams.append(typeParameter());
            }
            accept(Token.GT);
        }

        return typarams;
    }

    /*  TypeParameter = TypeVariable [TypeParameterBound]
     *  TypeParameterBound = EXTENDS Type {"&" Type}
     *  TypeVariable = Ident
     */
    private Ast.TypeParameter typeParameter() {
        int pos = lexer.pos();
        Symbol name = ident();
        ImmList<Ast.Expression> bounds = ImmList.nil();

        if (lexer.token() == Token.EXTENDS) {
            lexer.nextToken();
            bounds = bounds.append(type());
            while (lexer.token() == Token.AMP) {
                lexer.nextToken();
                bounds = bounds.append(type());
            }
        }
        return factory.at(pos).astTypeParameter(name, bounds);
    }

    /*  FormalParameters = "(" [ FormalParameterList ] ")"
     *  FormalParameterList = [ FormalParameterListNovarargs , ] LastFormalParameter
     *  FormalParameterListNovarargs = [ FormalParameterListNovarargs , ] FormalParameter
     */
    private ImmList<Ast.VariableDecl> formalParameters() {
        ImmList<Ast.VariableDecl> params = ImmList.nil();
        Ast.VariableDecl lastParam;

        accept(Token.LPAREN);

        if (lexer.token() != Token.RPAREN) {
            params = params.append(lastParam = formalParameter());
            while ((lastParam.getModifiers().getFlags() & Flags.VARARGS) == 0 && lexer.token() == Token.COMMA) {
                lexer.nextToken();
                params = params.append(lastParam = formalParameter());
            }
        }

        accept(Token.RPAREN);
        return params;
    }

    private Ast.Modifiers optFinal(long flags) {
        Ast.Modifiers mods = modifiersOpt();

        checkNoMods(mods.getFlags() & ~(Flags.FINAL | Flags.DEPRECATED));

        // check whether flags gets modified, if yes - create another modifiers instance
        if ((mods.getFlags() & flags) != flags) {
            // TODO: Due to mutability - recreate modifiers - think of better way
            mods = factory.astModifiers(mods.getFlags() | flags, mods.getAnnotations());
        }

        return mods;
    }

    /*  FormalParameter = { FINAL | '@' Annotation } Type VariableDeclaratorId
     *  LastFormalParameter = { FINAL | '@' Annotation } Type '...' Ident | FormalParameter
     */
    private Ast.VariableDecl formalParameter() {
        Ast.Modifiers mods = optFinal(Flags.PARAMETER);
        Ast.Expression type = type();
        if (lexer.token() == Token.ELLIPSIS) {
            checkVarargs();

            mods = factory.astModifiers(mods.getFlags() | Flags.VARARGS, mods.getAnnotations());

            type = factory.at(lexer.pos()).astArrayType(type);
            lexer.nextToken();
        }

        return variableDeclaratorId(mods, type);
    }

    //
    // auxiliary methods
    //

    /**
     * Check that given tree is a legal expression statement.
     * @param t Expression node.
     * @return Self.
     */
    protected Ast.Expression checkExprStat(Ast.Expression t) {
        switch(t.getKind()) {
            case AstNodeKind.PREINC: case AstNodeKind.PREDEC:
            case AstNodeKind.POSTINC: case AstNodeKind.POSTDEC:
            case AstNodeKind.ASSIGN:
            case AstNodeKind.BITOR_ASG: case AstNodeKind.BITXOR_ASG: case AstNodeKind.BITAND_ASG:
            case AstNodeKind.SL_ASG: case AstNodeKind.SR_ASG: case AstNodeKind.USR_ASG:
            case AstNodeKind.PLUS_ASG: case AstNodeKind.MINUS_ASG:
            case AstNodeKind.MUL_ASG: case AstNodeKind.DIV_ASG: case AstNodeKind.MOD_ASG:
            case AstNodeKind.APPLY: case AstNodeKind.NEWCLASS:
            case AstNodeKind.ERRONEOUS:
                return t;
            default:
                log.error(bundle.message("not.stmt"), Offset.at(t.getPos()));
                return factory.at(t.getPos()).astErroneous(ImmList.<Ast.Node>of(t));
        }
    }

    /**
     * Return precedence of operator represented by token,
     * -1 if token is not a binary operator. {}@see AstInfo#opPrec(int)}
     * @param token Token.
     * @return Precedence.
     */
    private static int prec(Token token) {
        int oc = optag(token);
        return (oc >= 0) ? AstInfo.opPrec(oc) : -1;
    }

    /**
     * Return operation tag of binary operator represented by token,
     * -1 if token is not a binary operator.
     * @param token Token.
     * @return Ast node kind.
     */
    private static int optag(Token token) {
        switch (token) {
            case BARBAR:
                return AstNodeKind.OR;
            case AMPAMP:
                return AstNodeKind.AND;
            case BAR:
                return AstNodeKind.BITOR;
            case BAREQ:
                return AstNodeKind.BITOR_ASG;
            case CARET:
                return AstNodeKind.BITXOR;
            case CARETEQ:
                return AstNodeKind.BITXOR_ASG;
            case AMP:
                return AstNodeKind.BITAND;
            case AMPEQ:
                return AstNodeKind.BITAND_ASG;
            case EQEQ:
                return AstNodeKind.EQ;
            case BANGEQ:
                return AstNodeKind.NE;
            case LT:
                return AstNodeKind.LT;
            case GT:
                return AstNodeKind.GT;
            case LTEQ:
                return AstNodeKind.LE;
            case GTEQ:
                return AstNodeKind.GE;
            case LTLT:
                return AstNodeKind.SL;
            case LTLTEQ:
                return AstNodeKind.SL_ASG;
            case GTGT:
                return AstNodeKind.SR;
            case GTGTEQ:
                return AstNodeKind.SR_ASG;
            case GTGTGT:
                return AstNodeKind.USR;
            case GTGTGTEQ:
                return AstNodeKind.USR_ASG;
            case PLUS:
                return AstNodeKind.PLUS;
            case PLUSEQ:
                return AstNodeKind.PLUS_ASG;
            case SUB:
                return AstNodeKind.MINUS;
            case SUBEQ:
                return AstNodeKind.MINUS_ASG;
            case STAR:
                return AstNodeKind.MUL;
            case STAREQ:
                return AstNodeKind.MUL_ASG;
            case SLASH:
                return AstNodeKind.DIV;
            case SLASHEQ:
                return AstNodeKind.DIV_ASG;
            case PERCENT:
                return AstNodeKind.MOD;
            case PERCENTEQ:
                return AstNodeKind.MOD_ASG;
            case INSTANCEOF:
                return AstNodeKind.TYPETEST;
            default:
                return -1;
        }
    }

    /**
     * Return operation tag of unary operator represented by token,
     * -1 if token is not a binary operator.
     * @param token Token.
     * @return Ast node kind.
     */
    private static int unoptag(Token token) {
        switch (token) {
            case PLUS:
                return AstNodeKind.POS;
            case SUB:
                return AstNodeKind.NEG;
            case BANG:
                return AstNodeKind.NOT;
            case TILDE:
                return AstNodeKind.COMPL;
            case PLUSPLUS:
                return AstNodeKind.PREINC;
            case SUBSUB:
                return AstNodeKind.PREDEC;
            default:
                return -1;
        }
    }

    /**
     * Return type tag of basic type represented by token,
     * -1 if token is not a basic type identifier.
     * @param token Token.
     * @return Type tag.
     */
    private static int typetag(Token token) {
        switch (token) {
            case BYTE:
                return TypeTags.BYTE;
            case CHAR:
                return TypeTags.CHAR;
            case SHORT:
                return TypeTags.SHORT;
            case INT:
                return TypeTags.INT;
            case LONG:
                return TypeTags.LONG;
            case FLOAT:
                return TypeTags.FLOAT;
            case DOUBLE:
                return TypeTags.DOUBLE;
            case BOOLEAN:
                return TypeTags.BOOLEAN;
            default:
                return -1;
        }
    }

    private void checkGenerics() {
        if (!allowGenerics) {
            logError("generics.not.supported.in.source", source.name);
            allowGenerics = true;
        }
    }

    private void checkVarargs() {
        if (!allowVarargs) {
            logError("varargs.not.supported.in.source", source.name);
            allowVarargs = true;
        }
    }

    private void checkForeach() {
        if (!allowForeach) {
            logError("foreach.not.supported.in.source", source.name);
            allowForeach = true;
        }
    }

    private void checkStaticImports() {
        if (!allowStaticImport) {
            logError("static.import.not.supported.in.source", source.name);
            allowStaticImport = true;
        }
    }

    private void checkAnnotations() {
        if (!allowAnnotations) {
            logError("annotations.not.supported.in.source", source.name);
            allowAnnotations = true;
        }
    }
}
