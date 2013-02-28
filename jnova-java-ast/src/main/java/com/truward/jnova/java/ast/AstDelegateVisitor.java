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
 * Delegates visiting the ast nodes to the given visitor.
 * This visitor will likely be used by the respective external clients.
 */
public class AstDelegateVisitor extends AstVisitor {
    private final AstVisitor delegate;

    public AstDelegateVisitor(AstVisitor delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }



    @Override
    public void visitNode(Ast.Node node) {
        // do nothing
    }

    @Override
    public final void visitCompilationUnit(Ast.CompilationUnit node) {
        delegate.visitCompilationUnit(node);
    }

    @Override
    public final void visitImport(Ast.Import node) {
        delegate.visitImport(node);
    }

    @Override
    public final void visitClass(Ast.ClassDecl node) {
        delegate.visitClass(node);
    }

    @Override
    public final void visitMethod(Ast.MethodDecl node) {
        delegate.visitMethod(node);
    }

    @Override
    public final void visitVariable(Ast.VariableDecl node) {
        delegate.visitVariable(node);
    }

    @Override
    public final void visitEmptyStatement(Ast.EmptyStatement node) {
        delegate.visitEmptyStatement(node);
    }

    @Override
    public final void visitBlock(Ast.Block node) {
        delegate.visitBlock(node);
    }

    @Override
    public final void visitDoWhileLoop(Ast.DoWhileLoop node) {
        delegate.visitDoWhileLoop(node);
    }

    @Override
    public final void visitWhileLoop(Ast.WhileLoop node) {
        delegate.visitWhileLoop(node);
    }

    @Override
    public final void visitForLoop(Ast.ForLoop node) {
        delegate.visitForLoop(node);
    }

    @Override
    public final void visitForEachLoop(Ast.ForEachLoop node) {
        delegate.visitForEachLoop(node);
    }

    @Override
    public final void visitLabeledStatement(Ast.LabeledStatement node) {
        delegate.visitLabeledStatement(node);
    }

    @Override
    public final void visitSwitch(Ast.Switch node) {
        delegate.visitSwitch(node);
    }

    @Override
    public final void visitCase(Ast.Case node) {
        delegate.visitCase(node);
    }

    @Override
    public final void visitSynchronized(Ast.Synchronized node) {
        delegate.visitSynchronized(node);
    }

    @Override
    public final void visitTry(Ast.Try node) {
        delegate.visitTry(node);
    }

    @Override
    public final void visitCatch(Ast.Catch node) {
        delegate.visitCatch(node);
    }

    @Override
    public final void visitConditionalExpression(Ast.Conditional node) {
        delegate.visitConditionalExpression(node);
    }

    @Override
    public final void visitIf(Ast.If node) {
        delegate.visitIf(node);
    }

    @Override
    public final void visitExpressionStatement(Ast.ExpressionStatement node) {
        delegate.visitExpressionStatement(node);
    }

    @Override
    public final void visitBreak(Ast.Break node) {
        delegate.visitBreak(node);
    }

    @Override
    public final void visitContinue(Ast.Continue node) {
        delegate.visitContinue(node);
    }

    @Override
    public final void visitReturn(Ast.Return node) {
        delegate.visitReturn(node);
    }

    @Override
    public final void visitThrow(Ast.Throw node) {
        delegate.visitThrow(node);
    }

    @Override
    public final void visitAssert(Ast.Assert node) {
        delegate.visitAssert(node);
    }

    @Override
    public final void visitMethodInvocation(Ast.MethodInvocation node) {
        delegate.visitMethodInvocation(node);
    }

    @Override
    public final void visitNewClass(Ast.NewClass node) {
        delegate.visitNewClass(node);
    }

    @Override
    public final void visitNewArray(Ast.NewArray node) {
        delegate.visitNewArray(node);
    }

    @Override
    public final void visitParens(Ast.Parens node) {
        delegate.visitParens(node);
    }

    @Override
    public final void visitAssignment(Ast.Assignment node) {
        delegate.visitAssignment(node);
    }

    @Override
    public final void visitCompoundAssignment(Ast.CompoundAssignment node) {
        delegate.visitCompoundAssignment(node);
    }

    @Override
    public final void visitUnary(Ast.Unary node) {
        delegate.visitUnary(node);
    }

    @Override
    public final void visitBinary(Ast.Binary node) {
        delegate.visitBinary(node);
    }

    @Override
    public final void visitTypeCast(Ast.TypeCast node) {
        delegate.visitTypeCast(node);
    }

    @Override
    public final void visitInstanceOf(Ast.InstanceOf node) {
        delegate.visitInstanceOf(node);
    }

    @Override
    public final void visitArrayAccess(Ast.ArrayAccess node) {
        delegate.visitArrayAccess(node);
    }

    @Override
    public final void visitFieldAccess(Ast.FieldAccess node) {
        delegate.visitFieldAccess(node);
    }

    @Override
    public final void visitIdent(Ast.Ident node) {
        delegate.visitIdent(node);
    }

    @Override
    public final void visitLiteral(Ast.Literal node) {
        delegate.visitLiteral(node);
    }

    @Override
    public final void visitPrimitiveType(Ast.PrimitiveType node) {
        delegate.visitPrimitiveType(node);
    }

    @Override
    public final void visitArrayType(Ast.ArrayType node) {
        delegate.visitArrayType(node);
    }

    @Override
    public final void visitParameterizedType(Ast.ParameterizedType node) {
        delegate.visitParameterizedType(node);
    }

    @Override
    public final void visitTypeParameter(Ast.TypeParameter node) {
        delegate.visitTypeParameter(node);
    }

    @Override
    public final void visitWildcard(Ast.Wildcard node) {
        delegate.visitWildcard(node);
    }

    @Override
    public final void visitTypeBoundKind(Ast.TypeBoundKind node) {
        delegate.visitTypeBoundKind(node);
    }

    @Override
    public final void visitModifiers(Ast.Modifiers node) {
        delegate.visitModifiers(node);
    }

    @Override
    public final void visitAnnotation(Ast.Annotation node) {
        delegate.visitAnnotation(node);
    }

    @Override
    public final void visitErroneous(Ast.Erroneous node) {
        delegate.visitErroneous(node);
    }
}
