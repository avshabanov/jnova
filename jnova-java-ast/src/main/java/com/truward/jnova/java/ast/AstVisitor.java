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
 * Base class for AST nodes visitor.
 */
public abstract class AstVisitor {

    public abstract void visitNode(Ast.Node node);



    public void visitCompilationUnit(Ast.CompilationUnit node) { visitNode(node); }

    public void visitImport(Ast.Import node) { visitNode(node); }

    public void visitClass(Ast.ClassDecl node) { visitNode(node); }

    public void visitMethod(Ast.MethodDecl node) { visitNode(node); }

    public void visitVariable(Ast.VariableDecl node) { visitNode(node); }

    public void visitEmptyStatement(Ast.EmptyStatement node) { visitNode(node); }

    public void visitBlock(Ast.Block node) { visitNode(node); }

    public void visitDoWhileLoop(Ast.DoWhileLoop node) { visitNode(node); }

    public void visitWhileLoop(Ast.WhileLoop node) { visitNode(node); }

    public void visitForLoop(Ast.ForLoop node) { visitNode(node); }

    public void visitForEachLoop(Ast.ForEachLoop node) { visitNode(node); }

    public void visitLabeledStatement(Ast.LabeledStatement node) { visitNode(node); }

    public void visitSwitch(Ast.Switch node) { visitNode(node); }

    public void visitCase(Ast.Case node) { visitNode(node); }

    public void visitSynchronized(Ast.Synchronized node) { visitNode(node); }

    public void visitTry(Ast.Try node) { visitNode(node); }

    public void visitCatch(Ast.Catch node) { visitNode(node); }

    public void visitConditionalExpression(Ast.Conditional node) { visitNode(node); }

    public void visitIf(Ast.If node) { visitNode(node); }

    public void visitExpressionStatement(Ast.ExpressionStatement node) { visitNode(node); }

    public void visitBreak(Ast.Break node) { visitNode(node); }

    public void visitContinue(Ast.Continue node) { visitNode(node); }

    public void visitReturn(Ast.Return node) { visitNode(node); }

    public void visitThrow(Ast.Throw node) { visitNode(node); }

    public void visitAssert(Ast.Assert node) { visitNode(node); }

    public void visitMethodInvocation(Ast.MethodInvocation node) { visitNode(node); }

    public void visitNewClass(Ast.NewClass node) { visitNode(node); }

    public void visitNewArray(Ast.NewArray node) { visitNode(node); }

    public void visitParens(Ast.Parens node) { visitNode(node); }

    public void visitAssignment(Ast.Assignment node) { visitNode(node); }

    public void visitCompoundAssignment(Ast.CompoundAssignment node) { visitNode(node); }

    public void visitUnary(Ast.Unary node) { visitNode(node); }

    public void visitBinary(Ast.Binary node) { visitNode(node); }

    public void visitTypeCast(Ast.TypeCast node) { visitNode(node); }

    public void visitInstanceOf(Ast.InstanceOf node) { visitNode(node); }

    public void visitArrayAccess(Ast.ArrayAccess node) { visitNode(node); }

    public void visitFieldAccess(Ast.FieldAccess node) { visitNode(node); }

    public void visitIdent(Ast.Ident node) { visitNode(node); }

    public void visitLiteral(Ast.Literal node) { visitNode(node); }

    public void visitPrimitiveType(Ast.PrimitiveType node) { visitNode(node); }

    public void visitArrayType(Ast.ArrayType node) { visitNode(node); }

    public void visitParameterizedType(Ast.ParameterizedType node) { visitNode(node); }

    public void visitTypeParameter(Ast.TypeParameter node) { visitNode(node); }

    public void visitWildcard(Ast.Wildcard node) { visitNode(node); }

    public void visitTypeBoundKind(Ast.TypeBoundKind node) { visitNode(node); }

    public void visitModifiers(Ast.Modifiers node) { visitNode(node); }

    public void visitAnnotation(Ast.Annotation node) { visitNode(node); }

    public void visitErroneous(Ast.Erroneous node) { visitNode(node); }
}
