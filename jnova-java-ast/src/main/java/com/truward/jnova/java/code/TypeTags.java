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
 * An interface for type tag values, which distinguish between different sorts of types.
 */
public final class TypeTags {
    private TypeTags() {} // Noninstantiable

    /** The tag of the basic type `byte'.
     */
    public static final int BYTE = 1;

    /** The tag of the basic type `char'.
     */
    public static final int CHAR = BYTE + 1;

    /** The tag of the basic type `short'.
     */
    public static final int SHORT = CHAR + 1;

    /** The tag of the basic type `int'.
     */
    public static final int INT = SHORT + 1;

    /** The tag of the basic type `long'.
     */
    public static final int LONG = INT + 1;

    /** The tag of the basic type `float'.
     */
    public static final int FLOAT = LONG + 1;

    /** The tag of the basic type `double'.
     */
    public static final int DOUBLE = FLOAT + 1;

    /** The tag of the basic type `boolean'.
     */
    public static final int BOOLEAN = DOUBLE + 1;

    /** The tag of the type `void'.
     */
    public static final int VOID = BOOLEAN + 1;

    /** The tag of all class and interface types.
     */
    public static final int CLASS = VOID + 1;

    /** The tag of all array types.
     */
    public static final int ARRAY = CLASS + 1;

    /** The tag of all (monomorphic) method types.
     */
    public static final int METHOD = ARRAY + 1;

    /** The tag of all package "types".
     */
    public static final int PACKAGE = METHOD + 1;

    /** The tag of all (source-level) type variables.
     */
    public static final int TYPEVAR = PACKAGE + 1;

    /** The tag of all type arguments.
     */
    public static final int WILDCARD = TYPEVAR + 1;

    /** The tag of all polymorphic (method-) types.
     */
    public static final int FORALL = WILDCARD + 1;

    /** The tag of the bottom type <null>.
     */
    public static final int BOT = FORALL + 1;

    /** The tag of a missing type.
     */
    public static final int NONE = BOT + 1;

    /** The tag of the error type.
     */
    public static final int ERROR = NONE + 1;

    /** The tag of an unknown type
     */
    public static final int UNKNOWN = ERROR + 1;

    /** The tag of all instantiatable type variables.
     */
    public static final int UNDETVAR = UNKNOWN + 1;

    /** The number of type tags.
     */
    public static final int TYPE_TAG_COUNT = UNDETVAR + 1;

    /** The maximum tag of a basic type.
     */
    public static final int LAST_BASE_TAG = BOOLEAN;

    /** The minimum tag of a partial type
     */
    public static final int FIRST_PARTIAL_TAG = ERROR;
}
