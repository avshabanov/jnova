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

package com.truward.jnova.java.source;

import java.util.HashMap;
import java.util.Map;

/** The source language version accepted.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public enum Source {
    /** 1.0 had no inner classes, and so could not pass the JCK. */
    // public static final Source JDK1_0 =              new Source("1.0");

    /** 1.1 did not have strictfp, and so could not pass the JCK. */
    // public static final Source JDK1_1 =              new Source("1.1");

    /** 1.2 introduced strictfp. */
    JDK1_2("1.2"),

    /** 1.3 is the same language as 1.2. */
    JDK1_3("1.3"),

    /** 1.4 introduced assert. */
    JDK1_4("1.4"),

    /** 1.5 introduced generics, attributes, foreach, boxing, static import,
     *  covariant return, enums, varargs, et al. */
    JDK1_5("1.5"),

    /** 1.6 reports encoding problems as errors instead of warnings. */
    JDK1_6("1.6");

//    private static final SimpleContext.Key<Source> sourceKey
//        = new SimpleContext.Key<Source>();
//
//    public static Source instance(SimpleContext context) {
//        Source instance = context.get(sourceKey);
//        if (instance == null) {
//            Options options = Options.instance(context);
//            String sourceString = options.get("-source");
//            if (sourceString != null) instance = lookup(sourceString);
//            if (instance == null) instance = DEFAULT;
//            context.put(sourceKey, instance);
//        }
//        return instance;
//    }

    public final String name;

    private static Map<String,Source> tab = new HashMap<String,Source>();
    static {
        for (Source s : values()) {
            tab.put(s.name, s);
        }
        tab.put("5", JDK1_5); // Make 5 an alias for 1.5
        tab.put("6", JDK1_6); // Make 6 an alias for 1.6
    }

    private Source(String name) {
        this.name = name;
    }

    public static final Source DEFAULT = JDK1_5;

    public static Source lookup(String name) {
        return tab.get(name);
    }

//    public Target requiredTarget() {
//        if (this.compareTo(JDK1_6) >= 0) return Target.JDK1_6;
//        if (this.compareTo(JDK1_5) >= 0) return Target.JDK1_5;
//        if (this.compareTo(JDK1_4) >= 0) return Target.JDK1_4;
//        return Target.JDK1_1;
//    }

    /** Allow encoding errors, giving only warnings. */
    public boolean allowEncodingErrors() {
        return compareTo(JDK1_6) < 0;
    }
    public boolean allowAsserts() {
        return compareTo(JDK1_4) >= 0;
    }
    public boolean allowCovariantReturns() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean allowGenerics() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean allowEnums() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean allowForeach() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean allowStaticImport() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean allowBoxing() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean allowVarargs() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean allowAnnotations() {
        return compareTo(JDK1_5) >= 0;
    }
    // hex floating-point literals supported?
    public boolean allowHexFloats() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean allowAnonOuterThis() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean addBridges() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean enforceMandatoryWarnings() {
        return compareTo(JDK1_5) >= 0;
    }

//    public static SourceVersion toSourceVersion(Source source) {
//        switch(source) {
//        case JDK1_2:
//            return RELEASE_2;
//        case JDK1_3:
//            return RELEASE_3;
//        case JDK1_4:
//            return RELEASE_4;
//        case JDK1_5:
//            return RELEASE_5;
//        case JDK1_6:
//            return RELEASE_6;
//        default:
//            return null;
//        }
//    }
}