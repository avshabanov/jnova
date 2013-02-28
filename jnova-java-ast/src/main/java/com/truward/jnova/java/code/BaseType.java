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

package com.truward.jnova.java.code;

/**
 * This class represents Java types. The class itself defines the behavior of
 * the following types:
 * <pre>
 * base types (tags: BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN),
 * type `void' (tag: VOID),
 * the bottom type (tag: BOT),
 * the missing type (tag: NONE).
 * </pre>
 * <p>The behavior of the following types is defined in subclasses, which are
 * all static inner classes of this class:
 * <pre>
 * class types (tag: CLASS, class: ClassType),
 * array types (tag: ARRAY, class: ArrayType),
 * method types (tag: METHOD, class: MethodType),
 * package types (tag: PACKAGE, class: PackageType),
 * type variables (tag: TYPEVAR, class: TypeVar),
 * type arguments (tag: WILDCARD, class: WildcardType),
 * polymorphic types (tag: FORALL, class: ForAll),
 * the error type (tag: ERROR, class: ErrorType).
 * </pre>
 * </p>
 *
 * TODO: see com/truward/rep/tools/javac/code/Type.java
 */
public class BaseType {
}
