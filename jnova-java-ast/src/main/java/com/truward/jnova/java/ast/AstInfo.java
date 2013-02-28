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
import com.truward.jnova.util.naming.Symbol;

import java.util.Collections;
import java.util.Map;

/**
 * Utility class containing inspector methods for trees.
 */
public final class AstInfo {

    /** Operator precedences values.
     */
    public static final int
            notExpression = -1,   // not an expression
            noPrec = 0,           // no enclosing expression
            assignPrec = 1,
            assignopPrec = 2,
            condPrec = 3,
            orPrec = 4,
            andPrec = 5,
            bitorPrec = 6,
            bitxorPrec = 7,
            bitandPrec = 8,
            eqPrec = 9,
            ordPrec = 10,
            shiftPrec = 11,
            addPrec = 12,
            mulPrec = 13,
            prefixPrec = 14,
            postfixPrec = 15,
            precCount = 16;


    @SuppressWarnings({"ConstantConditions"})
    public static int opPrec(int opcode) {
        switch(opcode) {
            case AstNodeKind.POS:
            case AstNodeKind.NEG:
            case AstNodeKind.NOT:
            case AstNodeKind.COMPL:
            case AstNodeKind.PREINC:
            case AstNodeKind.PREDEC: return prefixPrec;
            case AstNodeKind.POSTINC:
            case AstNodeKind.POSTDEC:
            case AstNodeKind.NULLCHK: return postfixPrec;
            case AstNodeKind.ASSIGN: return assignPrec;
            case AstNodeKind.BITOR_ASG:
            case AstNodeKind.BITXOR_ASG:
            case AstNodeKind.BITAND_ASG:
            case AstNodeKind.SL_ASG:
            case AstNodeKind.SR_ASG:
            case AstNodeKind.USR_ASG:
            case AstNodeKind.PLUS_ASG:
            case AstNodeKind.MINUS_ASG:
            case AstNodeKind.MUL_ASG:
            case AstNodeKind.DIV_ASG:
            case AstNodeKind.MOD_ASG: return assignopPrec;
            case AstNodeKind.OR: return orPrec;
            case AstNodeKind.AND: return andPrec;
            case AstNodeKind.EQ:
            case AstNodeKind.NE: return eqPrec;
            case AstNodeKind.LT:
            case AstNodeKind.GT:
            case AstNodeKind.LE:
            case AstNodeKind.GE: return ordPrec;
            case AstNodeKind.BITOR: return bitorPrec;
            case AstNodeKind.BITXOR: return bitxorPrec;
            case AstNodeKind.BITAND: return bitandPrec;
            case AstNodeKind.SL:
            case AstNodeKind.SR:
            case AstNodeKind.USR: return shiftPrec;
            case AstNodeKind.PLUS:
            case AstNodeKind.MINUS: return addPrec;
            case AstNodeKind.MUL:
            case AstNodeKind.DIV:
            case AstNodeKind.MOD: return mulPrec;
            case AstNodeKind.TYPETEST: return ordPrec;

            default:
                throw new AssertionError();
        }
    }


    /**
     * If this tree is an identifier or a field or a parameterized type, return its name, otherwise return null.
     *
     * @param tree Ast node to be analyzed.
     * @return Name for identifier / field access / parameterized type or null.
     */
    public static Symbol name(Ast.Node tree) {
        switch (tree.getKind()) {
        case AstNodeKind.IDENT:
            return ((Ast.Ident) tree).getName();
        case AstNodeKind.SELECT:
            return ((Ast.FieldAccess) tree).getIdentifier();
        case AstNodeKind.TYPEAPPLY:
            return name(((Ast.ParameterizedType) tree).getParameterizedClass());
        default:
            return null;
        }
    }

    /**
     * Gets javadoc comments associated with the given compilation unit.
     *
     * @param node Node from which comments should be extracted.
     * @return Comments mapping.
     */
    public static Map<Ast.Node, String> getDocComments(Ast.CompilationUnit node) {
        // TODO: gather real doc comments
        assert node != null;
        return Collections.emptyMap();
    }

    public static Symbol getEnclosingClassName(Ast.NewClass node) {
        // TODO: remove???, see AstPrettyPrinter.visitNewClass
        return node.getClassBody() != null ? node.getClassBody().getName() : null;
        // node.getClassBody().getName() != null ? node.getClassBody().getName() :
        //   node.type != null && node.type.tsym.name != node.type.tsym.name.table.empty ? node.type.tsym.name :
        //      null;
    }

    /**
     * Return flags as a string, separated by " ".
     * @param flags Bit mask of {@see Flags} members.
     * @return Readable flags representation.
     */
    public static String flagNames(long flags) {
        return Flags.toString(flags & Flags.StandardFlags).trim();
    }
}
