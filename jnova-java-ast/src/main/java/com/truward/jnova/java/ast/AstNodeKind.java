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

/**
 * Tree tag values, identifying kinds of trees.
 * TODO: rewrite as enum class.
 */
public final class AstNodeKind {
    private AstNodeKind() {}


    /**
     * Toplevel nodes, of type CompilationUnit, representing entire source files.
     */
    public static final int COMPILATION_UNIT = 1;

    /**
     * Import clauses, of type Import.
     */
    public static final int IMPORT = COMPILATION_UNIT + 1;

    /**
     * Class definitions, of type ClassDecl.
     */
    public static final int CLASS_DECL = IMPORT + 1;

    /**
     * Method definitions, of type MethodDecl.
     */
    public static final int METHOD_DECL = CLASS_DECL + 1;

    /**
     * Variable definitions, of type VariableDecl.
     */
    public static final int VARIABLE_DECL = METHOD_DECL + 1;

    /**
     * The no-op statement ";", of type Skip
     */
    public static final int EMPTY_STATEMENT = VARIABLE_DECL + 1;

    /**
     * Blocks, of type Block.
     */
    public static final int BLOCK = EMPTY_STATEMENT + 1;

    /**
     * Do-while loops, of type DoLoop.
     */
    public static final int DOLOOP = BLOCK + 1;

    /**
     * While-loops, of type WhileLoop.
     */
    public static final int WHILELOOP = DOLOOP + 1;

    /**
     * For-loops, of type ForLoop.
     */
    public static final int FORLOOP = WHILELOOP + 1;

    /**
     * Foreach-loops, of type ForeachLoop.
     */
    public static final int FOREACHLOOP = FORLOOP + 1;

    /**
     * Labelled statements, of type Labelled.
     */
    public static final int LABELLED = FOREACHLOOP + 1;

    /**
     * Switch statements, of type Switch.
     */
    public static final int SWITCH = LABELLED + 1;

    /**
     * Case parts in switch statements, of type Case.
     */
    public static final int CASE = SWITCH + 1;

    /**
     * Synchronized statements, of type Synchonized.
     */
    public static final int SYNCHRONIZED = CASE + 1;

    /**
     * Try statements, of type Try.
     */
    public static final int TRY = SYNCHRONIZED + 1;

    /**
     * Catch clauses in try statements, of type Catch.
     */
    public static final int CATCH = TRY + 1;

    /**
     * Conditional expressions, of type Conditional.
     */
    public static final int CONDEXPR = CATCH + 1;

    /**
     * Conditional statements, of type If.
     */
    public static final int IF = CONDEXPR + 1;

    /**
     * Expression statements, of type Exec.
     */
    public static final int EXEC = IF + 1;

    /**
     * Break statements, of type Break.
     */
    public static final int BREAK = EXEC + 1;

    /**
     * Continue statements, of type Continue.
     */
    public static final int CONTINUE = BREAK + 1;

    /**
     * Return statements, of type Return.
     */
    public static final int RETURN = CONTINUE + 1;

    /**
     * Throw statements, of type Throw.
     */
    public static final int THROW = RETURN + 1;

    /**
     * Assert statements, of type Assert.
     */
    public static final int ASSERT = THROW + 1;

    /**
     * Method invocation expressions, of type Apply.
     */
    public static final int APPLY = ASSERT + 1;

    /**
     * Class instance creation expressions, of type NewClass.
     */
    public static final int NEWCLASS = APPLY + 1;

    /**
     * Array creation expressions, of type NewArray.
     */
    public static final int NEWARRAY = NEWCLASS + 1;

    /**
     * Parenthesized subexpressions, of type Parens.
     */
    public static final int PARENS = NEWARRAY + 1;

    /**
     * Assignment expressions, of type Assign.
     */
    public static final int ASSIGN = PARENS + 1;

    /**
     * Type cast expressions, of type TypeCast.
     */
    public static final int TYPECAST = ASSIGN + 1;

    /**
     * Type test expressions, of type TypeTest.
     */
    public static final int TYPETEST = TYPECAST + 1;

    /**
     * Indexed array expressions, of type Indexed.
     */
    public static final int INDEXED = TYPETEST + 1;

    /**
     * Selections, of type Select.
     */
    public static final int SELECT = INDEXED + 1;

    /**
     * Simple identifiers, of type Ident.
     */
    public static final int IDENT = SELECT + 1;

    /**
     * Literals, of type Literal.
     */
    public static final int LITERAL = IDENT + 1;

    /**
     * Basic type identifiers, of type TypeIdent.
     */
    public static final int TYPEIDENT = LITERAL + 1;

    /**
     * Array types, of type TypeArray.
     */
    public static final int TYPEARRAY = TYPEIDENT + 1;

    /**
     * Parameterized types, of type TypeApply.
     */
    public static final int TYPEAPPLY = TYPEARRAY + 1;

    /**
     * Formal type parameters, of type TypeParameter.
     */
    public static final int TYPE_PARAMETER = TYPEAPPLY + 1;

    /**
     * Type argument.
     */
    public static final int WILDCARD = TYPE_PARAMETER + 1;

    /**
     * Bound kind: extends, super, exact, or unbound
     */
    public static final int TYPEBOUNDKIND = WILDCARD + 1;

    /**
     * Metadata: Annotation.
     */
    public static final int ANNOTATION = TYPEBOUNDKIND + 1;

    /**
     * Metadata: Modifiers
     */
    public static final int MODIFIERS = ANNOTATION + 1;

    /**
     * Error trees, of type Erroneous.
     */
    public static final int ERRONEOUS = MODIFIERS + 1;

    /**
     * Unary operators, of type Unary.
     */
    public static final int POS = ERRONEOUS + 1;             // +
    public static final int NEG = POS + 1;                   // -
    public static final int NOT = NEG + 1;                   // !
    public static final int COMPL = NOT + 1;                 // ~
    public static final int PREINC = COMPL + 1;              // ++ _
    public static final int PREDEC = PREINC + 1;             // -- _
    public static final int POSTINC = PREDEC + 1;            // _ ++
    public static final int POSTDEC = POSTINC + 1;           // _ --

    /**
     * Unary operator for null reference checks, only used internally.
     */
    public static final int NULLCHK = POSTDEC + 1;

    /**
     * Binary operators, of type Binary.
     */
    public static final int OR = NULLCHK + 1;                // ||
    public static final int AND = OR + 1;                    // &&
    public static final int BITOR = AND + 1;                 // |
    public static final int BITXOR = BITOR + 1;              // ^
    public static final int BITAND = BITXOR + 1;             // &
    public static final int EQ = BITAND + 1;                 // ==
    public static final int NE = EQ + 1;                     // !=
    public static final int LT = NE + 1;                     // <
    public static final int GT = LT + 1;                     // >
    public static final int LE = GT + 1;                     // <=
    public static final int GE = LE + 1;                     // >=
    public static final int SL = GE + 1;                     // <<
    public static final int SR = SL + 1;                     // >>
    public static final int USR = SR + 1;                    // >>>
    public static final int PLUS = USR + 1;                  // +
    public static final int MINUS = PLUS + 1;                // -
    public static final int MUL = MINUS + 1;                 // *
    public static final int DIV = MUL + 1;                   // /
    public static final int MOD = DIV + 1;                   // %

    /**
     * Assignment operators, of type Assignop.
     */
    public static final int BITOR_ASG = MOD + 1;             // |=
    public static final int BITXOR_ASG = BITOR_ASG + 1;      // ^=
    public static final int BITAND_ASG = BITXOR_ASG + 1;     // &=

    public static final int SL_ASG = SL + BITOR_ASG - BITOR; // <<=
    public static final int SR_ASG = SL_ASG + 1;             // >>=
    public static final int USR_ASG = SR_ASG + 1;            // >>>=
    public static final int PLUS_ASG = USR_ASG + 1;          // +=
    public static final int MINUS_ASG = PLUS_ASG + 1;        // -=
    public static final int MUL_ASG = MINUS_ASG + 1;         // *=
    public static final int DIV_ASG = MUL_ASG + 1;           // /=
    public static final int MOD_ASG = DIV_ASG + 1;           // %=

    /**
     * A synthetic let expression, of type LetExpr.
     */
    //public static final int LETEXPR = MOD_ASG + 1;           // ala scheme


    /**
     * The offset between assignment operators and normal operators.
     */
    public static final int ASG_OFFSET = BITOR_ASG - BITOR;
}
