jnova
=====

A Complete Java 6 parser.

Please, note that this work is not a developing parser "from scratch", some part of this work based on the
reverse engineered opensource Oracle's javac compiler.

The purpose of this work was to separate lexer parser into the stand alone library usable for the others.

The only dependency this library has is non-intrusive DI module. You may use spring, google guice, pico container or
mine micro-di library.

Core module defines reusable parts that is not tied to java, such as
- DiagnosticsLog - the reporter component that is able to provide the user with nicely formatted message, e.g.
```text
    Test.java:7: warning:
    [unchecked] unchecked generic array creation
    for varargs parameter of type List<String>[]
        Arrays.asList(Arrays.asList("January",
        ^
    1 warning
```
- Naming module - efficient UTF-8 symbol table
- Bundle - Resource bundle support
- Source - wrapper around certain source code provider (e.g. file) with fast access to the source buffer
- Tag - generic taggable base class that may be subclassed to provide certain class hierarchy with customizable user data
- ImmList - a very small and efficient immutable alternative to the java's LinkedList

## Maven Usage

Add the following repository:

    <repositories>
        <repository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>custom-central</id>
            <name>libs-release</name>
            <url>https://github.com/avshabanov/maven-repo/raw/master/libs-release</url>
        </repository>
    </repositories>

and then add jar dependency in your pom.xml, e.g.:

    <dependency>
        <groupId>com.truward.jnova</groupId>
        <artifactId>jnova-java-parser</artifactId>
        <version>1.0.1</version>
    </dependency>


## License

This library comes under Apache 2.0 license - see LICENSE.txt in the root directory.
You may freely use these library in both commercial and non-commercial development.

Have fun!