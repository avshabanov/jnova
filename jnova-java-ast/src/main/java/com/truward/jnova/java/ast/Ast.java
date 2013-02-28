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

import com.truward.jnova.java.code.BoundKind;
import com.truward.jnova.java.code.PredefinedNames;
import com.truward.jnova.java.code.TypeTags;
import com.truward.jnova.util.ImmList;
import com.truward.jnova.util.naming.Symbol;

import java.io.StringWriter;

/**
 * Encapsulates all the AST nodes.
 * Expected to be subclassed by the respective implementations of the AstFactory.
 * <p>"JLS 3" mentioned in the comments to the inner classes stands for
 * "The Java Language Specification, 3-rd edition"</p>
 */
public final class Ast {
    private Ast() {} // Noninstantiable.

    /**
     * Base class for all the AST nodes.
     */
    public static abstract class Node {
        /**
         * The encoded position in the source file. See Position.
         */
        private int pos;

        public final int getPos() {
            return pos;
        }

        public final void setPos(int pos) {
            this.pos = pos;
        }


        /**
         * {@inheritDoc}
         * Convert a tree to a pretty-printed java source string.
         */
        public final String toString() {
            final StringWriter writer = new StringWriter();
            final AstPrettyPrinter prettyPrinter = new AstPrettyPrinter(writer, false);
            prettyPrinter.printExpr(this);
            return writer.toString();
        }

        /**
         * @return The kind of this node -- one of the constants declared above.
         */
        public abstract int getKind();

        /**
         * Accepts the provided visitor.
         *
         * @param visitor Visitor to be accepted.
         */
        public abstract void accept(AstVisitor visitor);
    }

    /**
     * Represents compilation units (source files) and package declarations (package-info.java).
     * @see "JLS 3, sections 7.3, and 7.4"
     */
    public static final class CompilationUnit extends Node {
        private final ImmList<Annotation> packageAnnotations;
        private final Expression packageId;
        private final ImmList<? extends Node> definitions;

        public ImmList<Annotation> getPackageAnnotations() {
            return packageAnnotations;
        }

        public Expression getPackageId() {
            return packageId;
        }

        public ImmList<? extends Node> getDefinitions() {
            return definitions;
        }

        protected CompilationUnit(ImmList<Annotation> packageAnnotations,
                                  Expression packageId,
                                  ImmList<? extends Node> definitions) {
            this.packageAnnotations = packageAnnotations;
            this.packageId = packageId;
            this.definitions = definitions;
        }

        @Override
        public int getKind() { return AstNodeKind.COMPILATION_UNIT; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitCompilationUnit(this); }
    }

    /**
     * Represents import statement.
     * @see "JLS 3, section 7.5"
     */
    public static final class Import extends Node {
        private final boolean staticImport;
        private final Node qualifier;

        public boolean isStaticImport() {
            return staticImport;
        }

        public Node getQualifier() {
            return qualifier;
        }

        protected Import(Node qualifier, boolean importStatic) {
            this.qualifier = qualifier;
            this.staticImport = importStatic;
        }

        @Override
        public int getKind() { return AstNodeKind.IMPORT; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitImport(this); }
    }

    /**
     * A tree node used as the base class for the different kinds of statements.
     * @see "JLS 3, chapter 14"
     */
    public static abstract class Statement extends Node {}

    /**
     * A tree node used as the base class for the different types of expressions.
     * @see "JLS 3, chapter 15"
     */
    public static abstract class Expression extends Node {}

    /**
     * A tree node for an annotation.
     * @see "JLS 3, section 9.7"
     */
    public static final class Annotation extends Expression {
        private final Expression annotationType;
        private final ImmList<? extends Expression> arguments;

        public Node getAnnotationType() {
            return annotationType;
        }

        public ImmList<? extends Expression> getArguments() {
            return arguments;
        }

        protected Annotation(Expression annotationType, ImmList<? extends Expression> arguments) {
            this.annotationType = annotationType;
            this.arguments = arguments;
        }

        @Override
        public int getKind() { return AstNodeKind.ANNOTATION; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitAnnotation(this); }
    }

    /**
     * A tree node for the modifiers, including annotations, for a declaration.
     * @see "JLS 3, sections 8.1.1, 8.3.1, 8.4.3, 8.5.1, 8.8.3, 9.1.1, and 9.7"
     */
    public static final class Modifiers extends Node {
        private final long flags;
        private final ImmList<Annotation> annotations;

        public long getFlags() {
            return flags;
        }

        public ImmList<Annotation> getAnnotations() {
            return annotations;
        }

        protected Modifiers(long flags, ImmList<Annotation> annotations) {
            this.flags = flags;
            this.annotations = annotations;
        }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitModifiers(this); }

        @Override
        public int getKind() { return AstNodeKind.MODIFIERS; }
    }

    /**
     * A tree node for a type parameter.
     * @see "JLS 3, section 4.4"
     */
    public static final class TypeParameter extends Node {
        private final Symbol name;
        private final ImmList<Expression> bounds;

        public Symbol getName() {
            return name;
        }

        public ImmList<Expression> getBounds() {
            return bounds;
        }

        protected TypeParameter(Symbol name, ImmList<Expression> bounds) {
            this.name = name;
            this.bounds = bounds;
        }

        @Override
        public int getKind() { return AstNodeKind.TYPE_PARAMETER; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitTypeParameter(this); }
    }

    /**
     * A tree node for a wildcard type argument.
     * @see "JLS 3, section 4.5.1"
     */
    public static final class Wildcard extends Expression {
        private final TypeBoundKind typeBoundKind;
        private final Node bound;

        public TypeBoundKind getTypeBoundKind() {
            return typeBoundKind;
        }

        public Node getBound() {
            return bound;
        }

        protected Wildcard(TypeBoundKind typeBoundKind, Node bound) {
            this.typeBoundKind = typeBoundKind;
            this.bound = bound;
        }

        @Override
        public int getKind() { return AstNodeKind.WILDCARD; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitWildcard(this); }
    }

    /**
     * FOR INTERNAL USE ONLY!!!
     * NOT PART OF PUBLIC API!!!
     */
    public static class TypeBoundKind extends Node {
        private final BoundKind boundKind;

        public BoundKind getBoundKind() {
            return boundKind;
        }

        protected TypeBoundKind(BoundKind boundKind) {
            this.boundKind = boundKind;
        }

        @Override
        public int getKind() { return AstNodeKind.TYPEBOUNDKIND; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitTypeBoundKind(this); }
    }

    /**
     * A tree node for a class, interface, enum, or annotation type declaration.
     * @see "JLS 3, sections 8.1, 8.9, 9.1, and 9.6"
     */
    public static final class ClassDecl extends Statement {
        private final Modifiers modifiers;
        private final Symbol name;
        private final ImmList<TypeParameter> typeParameters;
        private final Expression extending;
        private final ImmList<? extends Expression> implementing;
        private final ImmList<? extends Node> definitions;

        public Modifiers getModifiers() {
            return modifiers;
        }

        public Symbol getName() {
            return name;
        }

        public ImmList<TypeParameter> getTypeParameters() {
            return typeParameters;
        }

        public Expression getExtending() {
            return extending;
        }

        public ImmList<? extends Expression> getImplementing() {
            return implementing;
        }

        public ImmList<? extends Node> getDefinitions() {
            return definitions;
        }

        protected ClassDecl(Modifiers modifiers,
                            Symbol name,
                            ImmList<TypeParameter> typeParameters,
                            Expression extending,
                            ImmList<? extends Expression> implementing,
                            ImmList<? extends Node> definitions) {
            this.modifiers = modifiers;
            this.name = name;
            this.typeParameters = typeParameters;
            this.extending = extending;
            this.implementing = implementing;
            this.definitions = definitions;
        }

        @Override
        public int getKind() { return AstNodeKind.CLASS_DECL; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitClass(this); }
    }

    /**
     * A tree node for a method or annotation type element declaration.
     * @see "JLS 3, sections 8.4, 8.6, 8.7, 9.4, and 9.6"
     */
    public static final class MethodDecl extends Node {
        private final Modifiers modifiers;
        private final Symbol name;
        private final Expression returnType;
        private final ImmList<TypeParameter> typeParameters;
        private final ImmList<VariableDecl> parameters;
        private final ImmList<Expression> thrown;
        private final Block body;
        private final Expression defaultValue; // for annotation types

        public Modifiers getModifiers() {
            return modifiers;
        }

        public Symbol getName() {
            return name;
        }

        public Expression getReturnType() {
            return returnType;
        }

        public ImmList<TypeParameter> getTypeParameters() {
            return typeParameters;
        }

        public ImmList<VariableDecl> getParameters() {
            return parameters;
        }

        public ImmList<Expression> getThrown() {
            return thrown;
        }

        public Block getBody() {
            return body;
        }

        public Expression getDefaultValue() {
            return defaultValue;
        }

        protected MethodDecl(Modifiers modifiers,
                             Symbol name,
                             Expression returnType,
                             ImmList<TypeParameter> typeParameters,
                             ImmList<VariableDecl> parameters,
                             ImmList<Expression> thrown,
                             Block body,
                             Expression defaultValue) {
            this.modifiers = modifiers;
            this.name = name;
            this.returnType = returnType;
            this.typeParameters = typeParameters;
            this.parameters = parameters;
            this.thrown = thrown;
            this.body = body;
            this.defaultValue = defaultValue;
        }

        @Override
        public int getKind() { return AstNodeKind.METHOD_DECL; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitMethod(this); }
    }

    /**
     * A tree node for a variable declaration.
     * @see "JLS 3, sections 8.3 and 14.4"
     */
    public static final class VariableDecl extends Statement {
        private final Modifiers modifiers;
        private final Symbol name;
        private final Expression variableType;
        private final Expression initializer;

        public Modifiers getModifiers() {
            return modifiers;
        }

        public Symbol getName() {
            return name;
        }

        public Expression getVariableType() {
            return variableType;
        }

        public Expression getInitializer() {
            return initializer;
        }

        protected VariableDecl(Modifiers modifiers, Symbol name, Expression variableType, Expression initializer) {
            this.modifiers = modifiers;
            this.name = name;
            this.variableType = variableType;
            this.initializer = initializer;
        }

        @Override
        public int getKind() { return AstNodeKind.VARIABLE_DECL; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitVariable(this); }
    }

    /**
     * A tree node for a statement block.
     * Flags might represents annotation that defines scope of this block, e.g. static initializer block:
     * <code>
     * static {
     *     a = 1;
     * }
     * </code>
     * @see "JLS 3, section 14.2"
     */
    public static final class Block extends Statement {
        private final long flags;
        private final ImmList<? extends Statement> statements;

        public long getFlags() {
            return flags;
        }

        public ImmList<? extends Statement> getStatements() {
            return statements;
        }

        protected Block(long flags, ImmList<? extends Statement> statements) {
            this.flags = flags;
            this.statements = statements;
        }

        @Override
        public int getKind() { return AstNodeKind.BLOCK; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitBlock(this); }
    }

    /**
     * A tree node for a 'do' statement.
     * @see "JLS 3, section 14.13"
     */
    public static final class DoWhileLoop extends Statement {
        private final Statement body;
        private final Expression condition;

        public Statement getBody() {
            return body;
        }

        public Expression getCondition() {
            return condition;
        }

        protected DoWhileLoop(Statement body, Expression condition) {
            this.body = body;
            this.condition = condition;
        }

        @Override
        public int getKind() { return AstNodeKind.DOLOOP; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitDoWhileLoop(this); }
    }

    /**
     * A tree node for a 'while' loop statement.
     * @see "JLS 3, section 14.12"
     */
    public static final class WhileLoop extends Statement {
        private final Statement body;
        private final Expression condition;

        public Statement getBody() {
            return body;
        }

        public Expression getCondition() {
            return condition;
        }

        protected WhileLoop(Expression condition, Statement body) {
            this.body = body;
            this.condition = condition;
        }

        @Override
        public int getKind() { return AstNodeKind.WHILELOOP; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitWhileLoop(this); }
    }

    /**
     * A tree node for an expression statement.
     * @see "JLS 3, section 14.8"
     */
    public static final class ExpressionStatement extends Statement {
        private final Expression expression;

        public Expression getExpression() {
            return expression;
        }

        protected ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        @Override
        public int getKind() { return AstNodeKind.EXEC; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitExpressionStatement(this); }
    }

    /**
     * A tree node for an expression statement.
     * @see "JLS 3, section 14.14.1"
     */
    public static final class ForLoop extends Statement {
        private final ImmList<? extends Statement> initializers;
        private final Expression condition;
        private final ImmList<ExpressionStatement> step;
        private final Statement body;

        public ImmList<? extends Statement> getInitializers() {
            return initializers;
        }

        public Expression getCondition() {
            return condition;
        }

        public ImmList<ExpressionStatement> getStep() {
            return step;
        }

        public Statement getBody() {
            return body;
        }

        protected ForLoop(ImmList<? extends Statement> initializers,
                          Expression condition,
                          ImmList<ExpressionStatement> step,
                          Statement body) {
            this.initializers = initializers;
            this.condition = condition;
            this.step = step;
            this.body = body;
        }

        @Override
        public int getKind() { return AstNodeKind.FORLOOP; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitForLoop(this); }
    }

    /**
     * A tree node for an "enhanced" 'for' loop statement (foreach).
     * @see "JLS 3, section 14.14.2"
     */
    public static final class ForEachLoop extends Statement {
        private final VariableDecl variable;
        private final Expression expression;
        private final Statement body;

        public VariableDecl getVariable() {
            return variable;
        }

        public Expression getExpression() {
            return expression;
        }

        public Statement getBody() {
            return body;
        }

        protected ForEachLoop(VariableDecl variable, Expression expression, Statement body) {
            this.variable = variable;
            this.expression = expression;
            this.body = body;
        }

        @Override
        public int getKind() { return AstNodeKind.FOREACHLOOP; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitForEachLoop(this); }
    }

    /**
     * A tree node for a labeled statement.
     * @see "JLS 3, section 14.7"
     */
    public static final class LabeledStatement extends Statement {
        private final Symbol label;
        private final Statement body;

        public Symbol getLabel() {
            return label;
        }

        public Statement getBody() {
            return body;
        }

        protected LabeledStatement(Symbol label, Statement body) {
            this.label = label;
            this.body = body;
        }

        @Override
        public int getKind() { return AstNodeKind.LABELLED; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitLabeledStatement(this); }
    }

    /**
     * A tree node for a 'switch' statement.
     * @see "JLS 3, section 14.11"
     */
    public static final class Switch extends Statement {
        private final Expression selector;
        private final ImmList<Case> cases;

        public Expression getSelector() {
            return selector;
        }

        public ImmList<Case> getCases() {
            return cases;
        }

        protected Switch(Expression selector, ImmList<Case> cases) {
            this.selector = selector;
            this.cases = cases;
        }

        @Override
        public int getKind() { return AstNodeKind.SWITCH; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitSwitch(this); }
    }


    /**
     * A tree node for a 'case' in a 'switch' statement.
     * @see "JLS 3, section 14.11"
     */
    public static final class Case extends Statement {
        private final Expression expression;
        private final ImmList<? extends Statement> statements;

        public Expression getExpression() {
            return expression;
        }

        public ImmList<? extends Statement> getStatements() {
            return statements;
        }

        protected Case(Expression expression, ImmList<? extends Statement> statements) {
            this.expression = expression;
            this.statements = statements;
        }

        @Override
        public int getKind() { return AstNodeKind.CASE; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitCase(this); }
    }

    /**
     * A tree node for a 'synchronized' statement.
     * @see "JLS 3, section 14.19"
     */
    public static final class Synchronized extends Statement {
        private final Expression lock;
        private final Block body;

        public Expression getLock() {
            return lock;
        }

        public Block getBody() {
            return body;
        }

        protected Synchronized(Expression lock, Block body) {
            this.lock = lock;
            this.body = body;
        }

        @Override
        public int getKind() { return AstNodeKind.SYNCHRONIZED; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitSynchronized(this); }
    }

    /**
     * A tree node for a 'try' statement.
     * @see "JLS 3, section 14.20"
     */
    public static final class Try extends Statement {
        private final Block body;
        private final ImmList<Catch> catchers;
        private final Block finalizer;

        public Block getBody() {
            return body;
        }

        public ImmList<Catch> getCatchers() {
            return catchers;
        }

        public Block getFinalizer() {
            return finalizer;
        }

        protected Try(Block body, ImmList<Catch> catchers, Block finalizer) {
            this.body = body;
            this.catchers = catchers;
            this.finalizer = finalizer;
        }

        @Override
        public int getKind() { return AstNodeKind.TRY; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitTry(this); }
    }

    /**
     * A tree node for a 'catch' block in a 'try' statement.
     * @see "JLS 3, section 14.20"
     */
    public static final class Catch extends Statement {
        private final VariableDecl parameter;
        private final Block body;

        public VariableDecl getParameter() {
            return parameter;
        }

        public Block getBody() {
            return body;
        }

        protected Catch(VariableDecl parameter, Block body) {
            this.parameter = parameter;
            this.body = body;
        }

        @Override
        public int getKind() { return AstNodeKind.CATCH; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitCatch(this); }
    }

    /**
     * A tree node for the conditional operator '(testExpr) ? (exprThen) : (exprElse)'.
     * @see "JLS 3, section 15.25"
     */
    public static final class Conditional extends Expression {
        private final Expression condition;
        private final Expression truePart;
        private final Expression falsePart;

        public Expression getCondition() {
            return condition;
        }

        public Expression getTruePart() {
            return truePart;
        }

        public Expression getFalsePart() {
            return falsePart;
        }

        protected Conditional(Expression condition, Expression truePart, Expression falsePart) {
            this.condition = condition;
            this.truePart = truePart;
            this.falsePart = falsePart;
        }

        @Override
        public int getKind() { return AstNodeKind.CONDEXPR; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitConditionalExpression(this); }
    }

    /**
     * A tree node for an 'if' statement.
     * @see "JLS 3, section 14.9"
     */
    public static final class If extends Statement {
        private final Expression condition;
        private final Statement thenPart;
        private final Statement elsePart;

        public Expression getCondition() {
            return condition;
        }

        public Statement getThenPart() {
            return thenPart;
        }

        public Statement getElsePart() {
            return elsePart;
        }

        protected If(Expression condition, Statement thenPart, Statement elsePart) {
            this.condition = condition;
            this.thenPart = thenPart;
            this.elsePart = elsePart;
        }

        @Override
        public int getKind() { return AstNodeKind.IF; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitIf(this); }
    }

    /**
     * A tree node for a 'break' statement.
     * @see "JLS 3, section 14.15"
     */
    public static final class Break extends Statement {
        private final Symbol label;

        public Symbol getLabel() {
            return label;
        }

        protected Break(Symbol label) {
            this.label = label;
        }

        @Override
        public int getKind() { return AstNodeKind.BREAK; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitBreak(this); }
    }

    /**
     * A tree node for a 'continue' statement.
     * @see "JLS 3, section 14.16"
     */
    public static final class Continue extends Statement {
        private final Symbol label;

        public Symbol getLabel() {
            return label;
        }

        protected Continue(Symbol label) {
            this.label = label;
        }

        @Override
        public int getKind() { return AstNodeKind.CONTINUE; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitContinue(this); }
    }

    /**
     * A tree node for a 'return' statement.
     * @see "JLS 3, section 14.17"
     */
    public static final class Return extends Statement {
        private final Expression expression;

        public Expression getExpression() {
            return expression;
        }

        protected Return(Expression expression) {
            this.expression = expression;
        }

        @Override
        public int getKind() { return AstNodeKind.RETURN; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitReturn(this); }
    }

    /**
     * A tree node for a 'throw' statement.
     * @see "JLS 3, section 14.18"
     */
    public static final class Throw extends Statement {
        private final Expression expression;

        public Expression getExpression() {
            return expression;
        }

        protected Throw(Expression expression) {
            this.expression = expression;
        }

        @Override
        public int getKind() { return AstNodeKind.THROW; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitThrow(this); }
    }

    /**
     * A tree node for a 'assert' statement.
     * @see "JLS 3, section 14.10"
     */
    public static final class Assert extends Statement {
        private final Expression condition;
        private final Expression detail;

        public Expression getCondition() {
            return condition;
        }

        public Expression getDetail() {
            return detail;
        }

        protected Assert(Expression condition, Expression detail) {
            this.condition = condition;
            this.detail = detail;
        }

        @Override
        public int getKind() { return AstNodeKind.ASSERT; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitAssert(this); }
    }

    /**
     * A tree node for a method invocation expression.
     * @see "JLS 3, section 15.2"
     */
    public static final class MethodInvocation extends Expression {
        private final ImmList<Expression> typeArguments;
        private final Expression methodSelect;
        private final ImmList<Expression> arguments;

        public ImmList<Expression> getTypeArguments() {
            return typeArguments;
        }

        public Expression getMethodSelect() {
            return methodSelect;
        }

        public ImmList<Expression> getArguments() {
            return arguments;
        }

        protected MethodInvocation(ImmList<Expression> typeArguments,
                                   Expression methodSelect,
                                   ImmList<Expression> arguments) {
            this.typeArguments = typeArguments;
            this.methodSelect = methodSelect;
            this.arguments = arguments;
        }

        @Override
        public int getKind() { return AstNodeKind.APPLY; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitMethodInvocation(this); }
    }

    /**
     * A tree node to declare a new instance of a class - new () statement.
     * @see "JLS 3, section 15.9"
     */
    public static final class NewClass extends Expression {
        private final Expression enclosingExpression;
        private final ImmList<Expression> typeArguments;
        private final Expression classIdentifier;
        private final ImmList<Expression> arguments;
        private final ClassDecl classBody;

        public Expression getEnclosingExpression() {
            return enclosingExpression;
        }

        public ImmList<Expression> getTypeArguments() {
            return typeArguments;
        }

        public Expression getClassIdentifier() {
            return classIdentifier;
        }

        public ImmList<Expression> getArguments() {
            return arguments;
        }

        public ClassDecl getClassBody() {
            return classBody;
        }

        protected NewClass(Expression enclosingExpression,
                           ImmList<Expression> typeArguments,
                           Expression classIdentifier,
                           ImmList<Expression> arguments,
                           ClassDecl classBody) {
            this.enclosingExpression = enclosingExpression;
            this.typeArguments = typeArguments;
            this.classIdentifier = classIdentifier;
            this.arguments = arguments;
            this.classBody = classBody;
        }

        @Override
        public int getKind() { return AstNodeKind.NEWCLASS; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitNewClass(this); }
    }

    /**
     * A tree node for an expression to create a new instance of an array - new [...] operation.
     * @see "JLS 3, section 15.10"
     */
    public static final class NewArray extends Expression {
        private final Expression elementType;
        private final ImmList<Expression> dimensions;
        private final ImmList<Expression> initializers;

        public Expression getElementType() {
            return elementType;
        }

        public ImmList<Expression> getDimensions() {
            return dimensions;
        }

        public ImmList<Expression> getInitializers() {
            return initializers;
        }

        protected NewArray(Expression elementType,
                           ImmList<Expression> dimensions,
                           ImmList<Expression> initializers) {
            this.elementType = elementType;
            this.dimensions = dimensions;
            this.initializers = initializers;
        }

        @Override
        public int getKind() { return AstNodeKind.NEWARRAY; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitNewArray(this); }
    }

    /**
     * A tree node for a parenthesized expression.
     * @see "JLS 3, section 15.8.5"
     */
    public static final class Parens extends Expression {
        private final Expression expression;

        public Expression getExpression() {
            return expression;
        }

        protected Parens(Expression expression) {
            this.expression = expression;
        }

        @Override
        public int getKind() { return AstNodeKind.PARENS; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitParens(this); }
    }

    /**
     * A tree node for an assignment expression - e.g. assignment with "=".
     * @see "JLS 3, section 15.26.1"
     */
    public static final class Assignment extends Expression {
        private final Expression variable;
        private final Expression expression;

        public Expression getVariable() {
            return variable;
        }

        public Expression getExpression() {
            return expression;
        }

        protected Assignment(Expression variable, Expression expression) {
            this.variable = variable;
            this.expression = expression;
        }

        @Override
        public int getKind() { return AstNodeKind.ASSIGN; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitAssignment(this); }
    }

    /**
     * A tree node for compound assignment operator - e.g. assignment with "+=", "|=", etc.
     * @see "JLS 3, section 15.26.2"
     */
    public static final class CompoundAssignment extends Expression {
        private final int opcode; // AstNodeKind
        private final Expression variable;
        private final Expression expression;

        public Expression getVariable() {
            return variable;
        }

        public Expression getExpression() {
            return expression;
        }

        protected CompoundAssignment(int opcode,
                                     Expression variable,
                                     Expression expression) {
            this.opcode = opcode;
            this.variable = variable;
            this.expression = expression;
        }

        @Override
        public int getKind() { return opcode; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitCompoundAssignment(this); }
    }

    /**
     * A tree node for postfix and unary expressions.
     * Use {@link #getKind()} to determine the kind of operatior.
     * @see "JLS 3, section 15.14 and 15.15"
     */
    public static final class Unary extends Expression {
        private final int opcode; // AstNodeKind
        private final Expression expression;

        public Expression getExpression() {
            return expression;
        }

        protected Unary(int opcode, Expression expression) {
            this.opcode = opcode;
            this.expression = expression;
        }

        @Override
        public int getKind() { return opcode; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitUnary(this); }
    }

    /**
     * A tree node for a binary expression.
     * Use {@link #getKind()} to determine the kind of operatior.
     * @see "JLS 3, section 15.17 to 15.24"
     */
    public static final class Binary extends Expression {
        private final int opcode; // AstNodeKind
        private final Expression leftOperand;
        private final Expression rightOperand;

        public Expression getLeftOperand() {
            return leftOperand;
        }

        public Expression getRightOperand() {
            return rightOperand;
        }

        protected Binary(int opcode, Expression leftOperand, Expression rightOperand) {
            this.opcode = opcode;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
        }

        @Override
        public int getKind() { return opcode; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitBinary(this); }
    }

    /**
     * A tree node for a type cast expression.
     * @see "JLS 3, section 15.16"
     */
    public static final class TypeCast extends Expression {
        private final Node type;
        private final Expression expression;

        public Node getType() {
            return type;
        }

        public Expression getExpression() {
            return expression;
        }

        protected TypeCast(Node type, Expression expression) {
            this.type = type;
            this.expression = expression;
        }

        @Override
        public int getKind() { return AstNodeKind.TYPECAST; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitTypeCast(this); }
    }

    /**
     * A tree node for an 'instanceof' expression.
     * @see "JLS 3, section 15.20.2"
     */
    public static final class InstanceOf extends Expression {
        private final Expression expression;
        private final Node testedClass;

        public Expression getExpression() {
            return expression;
        }

        public Node getTestedClass() {
            return testedClass;
        }

        protected InstanceOf(Expression expression, Node testedClass) {
            this.expression = expression;
            this.testedClass = testedClass;
        }

        @Override
        public int getKind() { return AstNodeKind.TYPETEST; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitInstanceOf(this); }
    }

    /**
     * A tree node for an array access expression.
     * @see "JLS 3, section 15.13"
     */
    public static final class ArrayAccess extends Expression {
        private final Expression expression;
        private final Expression index;

        public Expression getExpression() {
            return expression;
        }

        public Expression getIndex() {
            return index;
        }

        protected ArrayAccess(Expression expression, Expression index) {
            this.expression = expression;
            this.index = index;
        }

        @Override
        public int getKind() { return AstNodeKind.INDEXED; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitArrayAccess(this); }
    }

    /**
     * A tree node for an array access expression.
     * @see "JLS 3, section 15.13"
     */
    public static final class FieldAccess extends Expression {
        private final Expression expression;
        private final Symbol identifier;

        public Expression getExpression() {
            return expression;
        }

        public Symbol getIdentifier() {
            return identifier;
        }

        protected FieldAccess(Expression expression, Symbol identifier) {
            this.expression = expression;
            this.identifier = identifier;
        }

        @Override
        public int getKind() { return AstNodeKind.SELECT; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitFieldAccess(this); }
    }


    /**
     * A tree node for an identifier expression.
     * @see "JLS 3, section 6.5.6.1"
     */
    public static final class Ident extends Expression {
        private final Symbol name;

        public Symbol getName() {
            return name;
        }

        protected Ident(Symbol name) {
            this.name = name;
        }

        @Override
        public int getKind() { return AstNodeKind.IDENT; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitIdent(this); }
    }

    /**
     * A tree node for a literal expression.
     * @see "JLS 3, section 15.28"
     */
    public static final class Literal extends Expression {
        private final int typeTag;
        private final Object value;

        public int getTypeTag() {
            return typeTag;
        }

        public Object getValue() {
            // TODO: try to rewrite without relying on type tag
            switch (getTypeTag()) {
                case TypeTags.BOOLEAN:
                    final int booleanValue = (Integer) value;
                    return (booleanValue != 0);
                case TypeTags.CHAR:
                    final int charValue = (Integer) value;
                    final char ch = (char) charValue;
                    if (ch != charValue) {
                        throw new AssertionError("Bad value for char literal");
                    }
                    return ch;
                default:
                    return value;
            }
        }

        protected Literal(int typeTag, Object value) {
            this.typeTag = typeTag;
            this.value = value;
        }

        @Override
        public int getKind() { return AstNodeKind.LITERAL; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitLiteral(this); }
    }

    /**
     * A tree node for a primitive type.
     * @see "JLS 3, section 4.2"
     */
    public static final class PrimitiveType extends Expression {
        private final int typeTag; // TypeTags

        public int getTypeTag() {
            return typeTag;
        }

        protected PrimitiveType(int typeTag) {
            this.typeTag = typeTag;
        }

        @Override
        public int getKind() { return AstNodeKind.TYPEIDENT; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitPrimitiveType(this); }
    }

    /**
     * A tree node for an array type.
     * @see "JLS 3, section 10.1"
     */
    public static final class ArrayType extends Expression {
        private final Expression elementType;

        public Expression getElementType() {
            return elementType;
        }

        protected ArrayType(Expression elementType) {
            this.elementType = elementType;
        }

        @Override
        public int getKind() { return AstNodeKind.TYPEARRAY; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitArrayType(this); }
    }

    /**
     * A tree node for a type expression involving type parameters (e.g. List or Map).
     * @see "JLS 3, section 4.5.1"
     */
    public static final class ParameterizedType extends Expression {
        private final Expression parameterizedClass;
        private final ImmList<Expression> arguments;

        public Expression getParameterizedClass() {
            return parameterizedClass;
        }

        public ImmList<Expression> getArguments() {
            return arguments;
        }

        protected ParameterizedType(Expression parameterizedClass, ImmList<Expression> arguments) {
            this.parameterizedClass = parameterizedClass;
            this.arguments = arguments;
        }

        @Override
        public int getKind() { return AstNodeKind.TYPEAPPLY; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitParameterizedType(this); }
    }

    /**
     * A tree node for an empty (skip) statement, e.g. semicolon.
     * @see "JLS 3, section 14.6"
     */
    public static final class EmptyStatement extends Statement {
        protected EmptyStatement() {}

        @Override
        public int getKind() { return AstNodeKind.EMPTY_STATEMENT; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitEmptyStatement(this); }
    }

    /**
     * A tree node to stand in for a malformed expression.
     */
    public static final class Erroneous extends Expression {
        private final ImmList<? extends Node> errorNodes;

        // TODO: remove error nodes, they are seems to be unused.
        public ImmList<? extends Node> getErrorNodes() {
            return errorNodes;
        }

        protected Erroneous(ImmList<? extends Node> errorNodes) {
            this.errorNodes = errorNodes;
        }

        @Override
        public int getKind() { return AstNodeKind.ERRONEOUS; }

        @Override
        public void accept(AstVisitor visitor) { visitor.visitErroneous(this); }
    }



    //
    // End of AST nodes
    //


    /**
     * AST nodes factory.
     */
    public static abstract class Factory {
        protected abstract void onPostConstruct(Node node);

        public final CompilationUnit astCompilationUnit(ImmList<Annotation> packageAnnotations,
                                                        Expression packageId,
                                                        ImmList<? extends Node> definitions) {
            final CompilationUnit node = new CompilationUnit(packageAnnotations, packageId, definitions);
            onPostConstruct(node);
            return node;

        }


        public final Import astImport(Node qualifier, boolean staticImport) {
            final Import node = new Import(qualifier, staticImport);
            onPostConstruct(node);
            return node;
        }


        public final ClassDecl astClassDecl(Modifiers modifiers,
                                            Symbol name,
                                            ImmList<TypeParameter> typeParameters,
                                            Expression extending,
                                            ImmList<? extends Expression> implementing,
                                            ImmList<? extends Node> definitions) {
            final ClassDecl node = new ClassDecl(modifiers, name, typeParameters, extending,
                    implementing, definitions);
            onPostConstruct(node);
            return node;
        }


        public final ClassDecl astAnonymousClassDecl(PredefinedNames names, Modifiers modifiers, ImmList<? extends Node> definitions) {
            final ClassDecl node = new ClassDecl(modifiers, names.empty,
                    ImmList.<TypeParameter>nil(),
                    null,
                    ImmList.<Expression>nil(),
                    definitions);
            onPostConstruct(node);
            return node;
        }


        public final MethodDecl astMethodDecl(Modifiers modifiers,
                                              Symbol name,
                                              Expression returnType,
                                              ImmList<TypeParameter> typeParameters,
                                              ImmList<VariableDecl> parameters,
                                              ImmList<Expression> thrown,
                                              Block body,
                                              Expression defaultValue) {
            final MethodDecl node = new MethodDecl(modifiers, name, returnType, typeParameters, parameters,
                    thrown, body, defaultValue);
            onPostConstruct(node);
            return node;
        }


        public final VariableDecl astVariableDecl(Modifiers modifiers,
                                                  Symbol name,
                                                  Expression variableType,
                                                  Expression initializer) {
            final VariableDecl node = new VariableDecl(modifiers, name, variableType, initializer);
            onPostConstruct(node);
            return node;
        }


        public final EmptyStatement astEmptyStatement() {
            final EmptyStatement node = new EmptyStatement();
            onPostConstruct(node);
            return node;
        }


        public final Block astBlock(long flags, ImmList<? extends Statement> statements) {
            final Block node = new Block(flags, statements);
            onPostConstruct(node);
            return node;
        }

        public final Block astBlock(ImmList<? extends Statement> statements) {
            return astBlock(0, statements);
        }


        public final Annotation astAnnotation(Expression annotationType, ImmList<? extends Expression> arguments) {
            final Annotation node = new Annotation(annotationType, arguments);
            onPostConstruct(node);
            return node;
        }


        public final Modifiers astModifiers(long flags, ImmList<Annotation> annotations) {
            final Modifiers node = new Modifiers(flags, annotations);
            onPostConstruct(node);
            return node;
        }


        public final Modifiers astModifiers(long flags) {
            return astModifiers(flags, ImmList.<Annotation>nil());
        }


        public final If astIf(Expression condition, Statement thenPart, Statement elsePart) {
            final If node = new If(condition, thenPart, elsePart);
            onPostConstruct(node);
            return node;
        }


        public final FieldAccess astFieldAccess(Expression expression, Symbol identifier) {
            final FieldAccess node = new FieldAccess(expression, identifier);
            onPostConstruct(node);
            return node;
        }


        public final PrimitiveType astPrimitiveType(int typeTag) {
            final PrimitiveType node = new PrimitiveType(typeTag);
            onPostConstruct(node);
            return node;
        }


        public final ParameterizedType astParameterizedType(Expression parameterizedClass,
                                                            ImmList<Expression> arguments) {
            final ParameterizedType node = new ParameterizedType(parameterizedClass, arguments);
            onPostConstruct(node);
            return node;
        }


        public final ArrayType astArrayType(Expression elementType) {
            final ArrayType node = new ArrayType(elementType);
            onPostConstruct(node);
            return node;
        }


        public final ArrayAccess astArrayAccess(Expression expression, Expression index) {
            final ArrayAccess node = new ArrayAccess(expression, index);
            onPostConstruct(node);
            return node;
        }


        public final NewArray astNewArray(Expression elementType,
                                          ImmList<Expression> dimensions,
                                          ImmList<Expression> initializers) {
            final NewArray node = new NewArray(elementType, dimensions, initializers);
            onPostConstruct(node);
            return node;
        }


        public final NewClass astNewClass(Expression enclosingExpression,
                                          ImmList<Expression> typeArguments,
                                          Expression classIdentifier,
                                          ImmList<Expression> arguments,
                                          ClassDecl classBody) {
            final NewClass node = new NewClass(enclosingExpression, typeArguments,
                    classIdentifier, arguments, classBody);
            onPostConstruct(node);
            return node;
        }


        public final Ident astIdent(Symbol name) {
            final Ident node = new Ident(name);
            onPostConstruct(node);
            return node;
        }


        public final Literal astLiteral(int typeTag, Object value) {
            final Literal node = new Literal(typeTag, value);
            onPostConstruct(node);
            return node;
        }


        public final TypeParameter astTypeParameter(Symbol name, ImmList<Expression> bounds) {
            final TypeParameter node = new TypeParameter(name, bounds);
            onPostConstruct(node);
            return node;
        }


        public final TypeBoundKind astTypeBoundKind(BoundKind boundKind) {
            final TypeBoundKind node = new TypeBoundKind(boundKind);
            onPostConstruct(node);
            return node;
        }


        public final Wildcard astWildcard(TypeBoundKind typeBoundKind, Node bound) {
            final Wildcard node = new Wildcard(typeBoundKind, bound);
            onPostConstruct(node);
            return node;
        }


        public final Assignment astAssignment(Expression variable, Expression expression) {
            final Assignment node = new Assignment(variable, expression);
            onPostConstruct(node);
            return node;
        }


        public final ExpressionStatement astExpressionStatement(Expression expression) {
            final ExpressionStatement node = new ExpressionStatement(expression);
            onPostConstruct(node);
            return node;
        }


        public final CompoundAssignment astCompoundAssignment(int opcode,
                                                              Expression variable,
                                                              Expression expression) {
            final CompoundAssignment node = new CompoundAssignment(opcode, variable, expression);
            onPostConstruct(node);
            return node;
        }


        public final TypeCast astTypeCast(Node type, Expression expression) {
            final TypeCast node = new TypeCast(type, expression);
            onPostConstruct(node);
            return node;
        }


        public final InstanceOf astInstanceOf(Expression expression, Node testedClass) {
            final InstanceOf node = new InstanceOf(expression, testedClass);
            onPostConstruct(node);
            return node;
        }


        public final Unary astUnary(int opcode, Expression expression) {
            final Unary node = new Unary(opcode, expression);
            onPostConstruct(node);
            return node;
        }


        public final Binary astBinary(int opcode, Expression leftOperand, Expression rightOperand) {
            final Binary node = new Binary(opcode, leftOperand, rightOperand);
            onPostConstruct(node);
            return node;
        }


        public final MethodInvocation astMethodInvocation(ImmList<Expression> typeArguments,
                                                          Expression methodSelect,
                                                          ImmList<Expression> arguments) {
            final MethodInvocation node = new MethodInvocation(typeArguments, methodSelect, arguments);
            onPostConstruct(node);
            return node;
        }

        public final MethodInvocation astMethodInvocation(Expression methodSelect, ImmList<Expression> arguments) {
            return astMethodInvocation(null, methodSelect, arguments);
        }


        public final Conditional astConditional(Expression condition, Expression truePart, Expression falsePart) {
            final Conditional node = new Conditional(condition, truePart, falsePart);
            onPostConstruct(node);
            return node;
        }


        public final LabeledStatement astLabeledStatement(Symbol label, Statement body) {
            final LabeledStatement node = new LabeledStatement(label, body);
            onPostConstruct(node);
            return node;
        }


        public final ForLoop astForLoop(ImmList<? extends Statement> initializer,
                                        Expression condition,
                                        ImmList<ExpressionStatement> step,
                                        Statement body) {
            final ForLoop node = new ForLoop(initializer, condition, step, body);
            onPostConstruct(node);
            return node;
        }


        public final ForEachLoop astForEachLoop(VariableDecl variable, Expression expression, Statement body) {
            final ForEachLoop node = new ForEachLoop(variable, expression, body);
            onPostConstruct(node);
            return node;
        }


        public final WhileLoop astWhileLoop(Expression condition, Statement body) {
            final WhileLoop node = new WhileLoop(condition, body);
            onPostConstruct(node);
            return node;
        }

        public final DoWhileLoop astDoWhileLoop(Statement body, Expression condition) {
            final DoWhileLoop node = new DoWhileLoop(body, condition);
            onPostConstruct(node);
            return node;
        }


        public final Switch astSwitch(Expression selector, ImmList<Case> cases) {
            final Switch node = new Switch(selector, cases);
            onPostConstruct(node);
            return node;
        }


        public final Case astCase(Expression expression, ImmList<? extends Statement> statements) {
            final Case node = new Case(expression, statements);
            onPostConstruct(node);
            return node;
        }


        public final Break astBreak(Symbol label) {
            final Break node = new Break(label);
            onPostConstruct(node);
            return node;
        }


        public final Continue astContinue(Symbol label) {
            final Continue node = new Continue(label);
            onPostConstruct(node);
            return node;
        }


        public final Return astReturn(Expression expression) {
            final Return node = new Return(expression);
            onPostConstruct(node);
            return node;
        }


        public final Throw astThrow(Expression expression) {
            final Throw node = new Throw(expression);
            onPostConstruct(node);
            return node;
        }


        public final Try astTry(Block body, ImmList<Catch> catchers, Block finalizer) {
            final Try node = new Try(body, catchers, finalizer);
            onPostConstruct(node);
            return node;
        }


        public final Catch astCatch(VariableDecl parameter, Block body) {
            final Catch node = new Catch(parameter, body);
            onPostConstruct(node);
            return node;
        }


        public final Synchronized astSynchronized(Expression lock, Block body) {
            final Synchronized node = new Synchronized(lock, body);
            onPostConstruct(node);
            return node;
        }


        public final Parens astParens(Expression expression) {
            final Parens node = new Parens(expression);
            onPostConstruct(node);
            return node;
        }


        public final Assert astAssert(Expression condition, Expression detail) {
            final Assert node = new Assert(condition, detail);
            onPostConstruct(node);
            return node;
        }


        public final Erroneous astErroneous(ImmList<? extends Node> errorNodes) {
            final Erroneous node = new Erroneous(errorNodes);
            onPostConstruct(node);
            return node;
        }

        public final Erroneous astErroneous() {
            return astErroneous(ImmList.<Node>nil());
        }
    }
}
