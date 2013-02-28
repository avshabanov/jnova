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

import com.truward.jnova.java.code.TypeTags;
import com.truward.jnova.util.ImmList;
import com.truward.jnova.util.naming.Symbol;
import com.truward.jnova.util.naming.SymbolTable;
import com.truward.jnova.util.naming.support.HashSymbolTable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AstDelegateVisitorTest {
    private int counter;

    private final Ast.Factory f = new Ast.Factory() {
        @Override
        protected void onPostConstruct(Ast.Node node) {
            ++counter;
            node.setPos(counter);
        }
    };

    private final SymbolTable table = new HashSymbolTable();

    private Symbol nm(String str) {
        return table.fromSequence(str);
    }

    @Before
    public void resetCounter() {
        counter = 0;
    }


    public static final class WrappedScanner extends AstScanner {
        int sum = 0;

        @Override
        public void scan(Ast.Node node) {
            super.scan(node);

            sum += node.getPos();
        }
    }


    @Test
    public void testBase() {
        assertTrue(true);

        final Ast.Node sampleNode = f.astAnnotation(
                f.astIdent(nm("id1")),
                ImmList.of(
                        f.astAssignment(
                                f.astIdent(nm("id2")),
                                f.astLiteral(TypeTags.INT, 12)),
                        f.astAssignment(
                                f.astIdent(nm("id3")),
                                f.astLiteral(TypeTags.CLASS, "Literal"))
                ));

        final WrappedScanner wrappedScanner = new WrappedScanner();
        final AstDelegateVisitor delegateVisitor = new AstDelegateVisitor(wrappedScanner);

        sampleNode.accept(delegateVisitor);
        final int n = counter - 1; // number of nodes expected to be scanned (excluding root annotation node)

        // WrappedScanner calculates sum of all the positions which is a simple arithmetic progression,
        // that is why we use (n * (n + 1)) / 2 to calculate the expected sum here
        assertEquals((n * (n + 1)) / 2, wrappedScanner.sum);
    }
}
