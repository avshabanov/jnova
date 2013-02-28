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

import com.truward.jnova.util.ImmList;

/**
 * Scanning visitor.
 */
public class AstScanner extends AstVisitor {

    public void scan(Ast.Node node) {
        if (node != null) {
            node.accept(this);
        }
    }

    public final void scan(ImmList<? extends Ast.Node> nodes) {
        assert nodes != null;
        for (final Ast.Node node : nodes) {
            scan(node);
        }
    }

    //
    // Overriden visitor methods
    //



    /**
     * {@inheritDoc}
     * Nodes fallback.
     */
    @Override
    public void visitNode(Ast.Node node) {
        throw new AssertionError("Node " + node + " is unhandled");
    }


    @Override
    public void visitCompilationUnit(Ast.CompilationUnit node) {
        scan(node.getPackageAnnotations());
        scan(node.getDefinitions());
        scan(node.getPackageId());
    }

    @Override
    public void visitImport(Ast.Import node) {
        scan(node.getQualifier());
    }

    @Override
    public void visitClass(Ast.ClassDecl node) {
        scan(node.getModifiers());
        scan(node.getTypeParameters());
        scan(node.getExtending());
        scan(node.getImplementing());
        scan(node.getDefinitions());
    }

    @Override
    public void visitMethod(Ast.MethodDecl node) {
        scan(node.getModifiers());
        scan(node.getReturnType());
        scan(node.getTypeParameters());
        scan(node.getParameters());
        scan(node.getThrown());
        scan(node.getDefaultValue());
        scan(node.getBody());
    }

    @Override
    public void visitVariable(Ast.VariableDecl node) {
        scan(node.getModifiers());
        scan(node.getVariableType());
        scan(node.getInitializer());
    }

    @Override
    public void visitEmptyStatement(Ast.EmptyStatement node) {
    }

    @Override
    public void visitBlock(Ast.Block node) {
        scan(node.getStatements());
    }

    @Override
    public void visitDoWhileLoop(Ast.DoWhileLoop node) {
        scan(node.getBody());
        scan(node.getCondition());
    }

    @Override
    public void visitWhileLoop(Ast.WhileLoop node) {
        scan(node.getCondition());
        scan(node.getBody());
    }

    @Override
    public void visitForLoop(Ast.ForLoop node) {
        scan(node.getInitializers());
        scan(node.getCondition());
        scan(node.getStep());
        scan(node.getBody());
    }

    @Override
    public void visitForEachLoop(Ast.ForEachLoop node) {
        scan(node.getVariable());
        scan(node.getExpression());
        scan(node.getBody());
    }

    @Override
    public void visitLabeledStatement(Ast.LabeledStatement node) {
        scan(node.getBody());
    }

    @Override
    public void visitSwitch(Ast.Switch node) {
        scan(node.getSelector());
        scan(node.getCases());
    }

    @Override
    public void visitCase(Ast.Case node) {
        scan(node.getExpression());
        scan(node.getStatements());
    }

    @Override
    public void visitSynchronized(Ast.Synchronized node) {
        scan(node.getLock());
        scan(node.getBody());
    }

    @Override
    public void visitTry(Ast.Try node) {
        scan(node.getBody());
        scan(node.getCatchers());
        scan(node.getFinalizer());
    }

    @Override
    public void visitCatch(Ast.Catch node) {
        scan(node.getParameter());
        scan(node.getBody());
    }

    @Override
    public void visitConditionalExpression(Ast.Conditional node) {
        scan(node.getCondition());
        scan(node.getTruePart());
        scan(node.getFalsePart());
    }

    @Override
    public void visitIf(Ast.If node) {
        scan(node.getCondition());
        scan(node.getThenPart());
        scan(node.getElsePart());
    }

    @Override
    public void visitExpressionStatement(Ast.ExpressionStatement node) {
        scan(node.getExpression());
    }

    @Override
    public void visitBreak(Ast.Break node) {
    }

    @Override
    public void visitContinue(Ast.Continue node) {
    }

    @Override
    public void visitReturn(Ast.Return node) {
        scan(node.getExpression());
    }

    @Override
    public void visitThrow(Ast.Throw node) {
        scan(node.getExpression());
    }

    @Override
    public void visitAssert(Ast.Assert node) {
        scan(node.getCondition());
        scan(node.getDetail());
    }

    @Override
    public void visitMethodInvocation(Ast.MethodInvocation node) {
        scan(node.getMethodSelect());
        scan(node.getTypeArguments()); // TODO: Skip type arguments?
        scan(node.getArguments());
    }

    @Override
    public void visitNewClass(Ast.NewClass node) {
        scan(node.getEnclosingExpression());
        scan(node.getClassIdentifier());
        scan(node.getArguments());
        scan(node.getTypeArguments()); // TODO: Skip type arguments?
        scan(node.getClassBody());
    }

    @Override
    public void visitNewArray(Ast.NewArray node) {
        scan(node.getElementType());
        scan(node.getDimensions());
        scan(node.getInitializers());
    }

    @Override
    public void visitParens(Ast.Parens node) {
        scan(node.getExpression());
    }

    @Override
    public void visitAssignment(Ast.Assignment node) {
        scan(node.getVariable());
        scan(node.getExpression());
    }

    @Override
    public void visitCompoundAssignment(Ast.CompoundAssignment node) {
        scan(node.getVariable());
        scan(node.getExpression());
    }

    @Override
    public void visitUnary(Ast.Unary node) {
        scan(node.getExpression());
    }

    @Override
    public void visitBinary(Ast.Binary node) {
        scan(node.getLeftOperand());
        scan(node.getRightOperand());
    }

    @Override
    public void visitTypeCast(Ast.TypeCast node) {
        scan(node.getType());
        scan(node.getExpression());
    }

    @Override
    public void visitInstanceOf(Ast.InstanceOf node) {
        scan(node.getExpression());
        scan(node.getTestedClass());
    }

    @Override
    public void visitArrayAccess(Ast.ArrayAccess node) {
        scan(node.getExpression());
        scan(node.getIndex());
    }

    @Override
    public void visitFieldAccess(Ast.FieldAccess node) {
        scan(node.getExpression());
    }

    @Override
    public void visitIdent(Ast.Ident node) {
    }

    @Override
    public void visitLiteral(Ast.Literal node) {
    }

    @Override
    public void visitPrimitiveType(Ast.PrimitiveType node) {
    }

    @Override
    public void visitArrayType(Ast.ArrayType node) {
        scan(node.getElementType());
    }

    @Override
    public void visitParameterizedType(Ast.ParameterizedType node) {
        scan(node.getParameterizedClass());
        scan(node.getArguments());
    }

    @Override
    public void visitTypeParameter(Ast.TypeParameter node) {
        scan(node.getBounds());
    }

    @Override
    public void visitWildcard(Ast.Wildcard node) {
        scan(node.getTypeBoundKind());
        scan(node.getBound());
    }

    @Override
    public void visitTypeBoundKind(Ast.TypeBoundKind node) {
    }

    @Override
    public void visitModifiers(Ast.Modifiers node) {
        scan(node.getAnnotations());
    }

    @Override
    public void visitAnnotation(Ast.Annotation node) {
        scan(node.getAnnotationType());
        scan(node.getArguments());
    }

    @Override
    public void visitErroneous(Ast.Erroneous node) {
    }
}
