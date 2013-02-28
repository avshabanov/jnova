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

package com.truward.jnova.util.diagnostics.formatter;

/*

            SAMPLE FORMAT MESSAGES

            (1)
Test.java:3: method test in class Foo<T#0> cannot be applied to given types
    test(t);
    \^
required: T#0
found: T#1
where T#0,T#1 are type­ variables:
 T#0 extends String
  (declared in class Foo)
 T#1 extends Integer
  (declared in method <T>foo(T))
1 error


            (2)
Test.java:7: warning:
[unchecked] unchecked generic array creation
for varargs parameter of type List<String>[]
    Arrays.asList(Arrays.asList("January",
    ^
1 warning



            (3)
$fullpath/DefaultDiagnosticsLog.java:[37,4] annotation type not applicable to this kind of declaration


            (4)
TBD

 */

/**
 * Represents log message formatters.
 */
public interface DiagnosticsFormatter {
    void format(DiagnosticsTemplate template);
}
