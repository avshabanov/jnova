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

package com.truward.jnova.util;

import com.truward.jnova.util.naming.Symbol;
import com.truward.jnova.util.naming.SymbolTable;
import com.truward.jnova.util.naming.support.HashSymbolTable;
import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests symbol table's functionality.
 */
public final class SymbolTableTest {
    private SymbolTable table;

    private final Random random = new SecureRandom();

    @Before
    public void initTable() {
        table = new HashSymbolTable();
    }

    @Test
    public void testSymTableBase() {
        final Symbol sym1 = table.fromSequence("asdf");
        final Symbol sym2 = table.fromSequence("asdf2");
        final Symbol sym3 = table.fromSequence("Asd");
        final Symbol sym4 = table.fromSequence("as" +
                (Thread.currentThread().getName().contains("This should never ever happen") ? "d" : "df"));

        assertEquals(sym1, sym4);
        assertNotSame(sym1, sym2);
        assertNotSame(sym2, sym1);
        assertNotSame(sym2, sym3);
        assertNotSame(sym3, sym2);
        assertNotSame(sym3, sym1);

        // hash symbol table MUST return same symbol instance
        assertTrue("Equality by reference", sym1 == sym4);

        // the same must apply for symbol created from chars
        final Symbol sym5 = table.fromChars(new char[] {'a', 's', 'd', 'f'}, 0, 4);
        assertTrue("Equality by reference", sym1 == sym5);
    }

    @Test
    public void testEmptySymbols() {
        final Symbol s1 = table.fromSequence("");
        assertEquals(0, s1.getUtfLength());

        assertEquals(s1, table.fromSequence(""));
        assertEquals(s1, table.fromChars(new char[0], 0, 0));
        assertEquals(s1, table.fromChars(new char[]{'a', 'b', 'c'}, 0, 0));
        assertEquals(s1, table.fromChars(new char[]{'a', 'b', 'c'}, 1, 0));

        // Tests for writing to char buffer
        {
            // 0-length buffer
            assertEquals(0, s1.writeChars(new char[0], 0));

            final char[] ch1 = new char[] { 'a', 'a', 'a' };
            final char[] cloneOfCh1 = Arrays.copyOf(ch1, ch1.length);
            assertEquals(0, s1.writeChars(ch1, 0));
            assertArrayEquals(cloneOfCh1, ch1);
        }

        final Symbol s2 = table.fromSequence("a");
        assertNotSame(s1, s2);
        assertEquals(1, s2.getUtfLength());
        assertEquals(s1.toString(), "");
    }

    @Test
    public void testMultipleSymbols() {
        final String[] tests = new String[] {
                "asd", "Asd", "+1234", "<Afr>", "<afr>", "<aFr>",
                "This is a big string", "This is a big str1ng",
                "rop", "asdf", "asd", "<Afr>", "asd"
        };

        final Random random = new Random();

        // string map
        final Map<String, Integer> strMap = new HashMap<String, Integer>();
        for (final String t : tests) {
            final Integer count = strMap.get(t);
            strMap.put(t, count == null ? 1 : count + 1);
        }

        // symbol map
        final Map<Symbol, Integer> symMap = new HashMap<Symbol, Integer>();
        for (final String t : tests) {
            final Symbol symbol = table.fromSequence(t);
            assertEquals(t, symbol.toString());

            // check writing to char buffer
            final char[] expectedChars = t.toCharArray();
            final char[] actualChars = new char[expectedChars.length];
            assertEquals(t.length(), symbol.writeChars(actualChars, 0));
            assertArrayEquals(expectedChars, actualChars);

            // our string are UTF-7-compliant, so UTF-8 length shall match the corresponding string length
            assertEquals(t.length(), symbol.getUtfLength());

            // check twice
            assertEquals(t, symbol.toString());

            final Integer count = symMap.get(symbol);
            symMap.put(symbol, count == null ? 1 : count + 1);

            // get the same name from randomly specified char array
            final int leadingCharCount = random.nextInt(10);
            final int trailingCharCount = random.nextInt(10);
            final char[] chars = new char[leadingCharCount + t.length() + trailingCharCount];
            for (int i = 0; i < chars.length; ++i) {
                chars[i] = (char)(random.nextInt((int)'z' - (int)'a') + (int)'a');
            }
            t.getChars(0, t.length(), chars, leadingCharCount);

            // check that fromChars produces the very same symbol that it fromSequence equivalent.
            final Symbol sym2 = table.fromChars(chars, leadingCharCount, t.length());
            assertTrue("fromChars vs fromSequence", symbol == sym2);
        }

        // check exact match of symMap with strMap
        assertEquals(strMap.size(), symMap.size());
        for (final Symbol symbol : symMap.keySet()) {
            assertEquals(symMap.get(symbol), strMap.get(symbol.toString()));
        }
    }

    @Test
    public void testWriteChars() {
        final String str = "abcdefg";
        final Symbol symbol = table.fromSequence(str);

        // assume our string is treated as UTF-7
        assertEquals(str.length(), symbol.getUtfLength());

        final char[] buf = new char[symbol.getUtfLength()];
        assertEquals(str.length(), symbol.writeChars(buf, 0));
        assertEquals(str, new String(buf, 0, str.length()));

        assertEquals(str.length() - 1, symbol.writeChars(buf, 1));
        assertEquals(str.substring(0, str.length() - 1), new String(buf, 1, str.length() - 1));

        assertEquals(str.length() - 3, symbol.writeChars(buf, 3));
        assertEquals(str.substring(0, str.length() - 3), new String(buf, 3, str.length() - 3));
    }

    @Test
    public void testWriteUtfChars() throws Exception {
        final String richStr = "\u0457\u0458\u0459";
        final Symbol symbol = table.fromSequence(richStr);

        assertEquals(richStr, symbol.toString());

        assertEquals(richStr.getBytes("UTF-8").length, symbol.getUtfLength());

        {
            final char[] buf = new char[symbol.getUtfLength()];
            assertEquals(richStr.length(), symbol.writeChars(buf, 0));
            assertEquals(richStr, new String(buf, 0, richStr.length()));
        }
    }

    private static enum ComparableResult {
        NEGATIVE,
        POSITIVE,
        ZERO,
        NOT_ZERO
    }

    private static void assertCompare(Symbol lhs, Symbol rhs, ComparableResult comparableResult) {
        final int result = lhs.compareTo(rhs);
        final int negResult = rhs.compareTo(lhs);
        assertEquals("Comparable contract should be followed", result, -negResult);

        if (comparableResult == ComparableResult.NOT_ZERO) {
            assertNotSame("Symbol '" + lhs + "' is not expected to be equals to '" + rhs + "'",
                    ComparableResult.ZERO, comparableResult);
            return;
        }

        if (result < 0) {
            assertEquals("Symbol '" + lhs + "' expected to be less than '" + rhs + "'",
                    ComparableResult.NEGATIVE, comparableResult);
        } else if (result > 0) {
            assertEquals("Symbol '" + lhs + "' expected to be greater than " + rhs + "'",
                    ComparableResult.POSITIVE, comparableResult);
        } else {
            assertEquals("Symbol '" + lhs + "' expected to be equals to " + rhs + "'",
                    ComparableResult.ZERO, comparableResult);
        }
    }

    @Test
    public void testSimpleComparable() {
        final Symbol s1 = table.fromSequence("abc");

        assertCompare(s1, table.fromSequence("abc"), ComparableResult.ZERO);
        assertCompare(s1, table.fromSequence("abd"), ComparableResult.NEGATIVE);
        assertCompare(s1, table.fromSequence("abb"), ComparableResult.POSITIVE);

        assertCompare(s1, table.fromSequence("abcd"), ComparableResult.NEGATIVE);
        assertCompare(s1, table.fromSequence("ab"), ComparableResult.POSITIVE);
    }

    @Test
    public void testComparableInDifferentTables() {
        final String[] strs = new String[] {
                "abc", "av", "abb", "abd", "abcd", "ABC", "Abc", "abC", "a", "ZZZ", "zzz"
        };

        final Symbol[] sym1 = new Symbol[strs.length];
        for (int i = 0; i < strs.length; ++i) {
            sym1[i] = table.fromSequence(strs[i]);
        }

        final SymbolTable anotherTable = new HashSymbolTable();
        final Symbol[] sym2 = new Symbol[strs.length];
        for (int i = 0; i < strs.length; ++i) {
            sym2[i] = anotherTable.fromSequence(strs[i]);
        }

        final Random random = new SecureRandom();
        for (int i = 0; i < 100; ++i) {
            final int index1 = random.nextInt(strs.length);
            final int index2 = random.nextInt(strs.length);
            final boolean areEquals = strs[index1].equals(strs[index2]);

            assertCompare(sym1[index1], sym2[index2], areEquals ? ComparableResult.ZERO : ComparableResult.NOT_ZERO);
        }
    }

    private String randString() {
        final int len = random.nextInt(10) + 1;
        final char[] resultName = new char[len];
        final String randChars = "ABCDEFabcdef0123456789_+=:";
        
        for (int i = 0; i < len; ++i) {
            resultName[i] = randChars.charAt(random.nextInt(randChars.length()));
        }
        
        return new String(resultName, 0, len);
    }
    
    @Test
    public void testFromIndex() {
        // check table with guaranteed collisions
        final SymbolTable table = new HashSymbolTable(4, 12);
        final int STRS_SIZE = 250;
        final String[] strs = new String[STRS_SIZE];
        final Symbol[] syms = new Symbol[STRS_SIZE];
        for (int i = 0; i < STRS_SIZE; ++i) {
            strs[i] = randString();
            syms[i] = table.fromSequence(strs[i]);
        }

        // check index
        for (int i = 0; i < STRS_SIZE; ++i) {
            final Symbol symbol = syms[i];

            assertEquals(symbol, table.fromIndex(symbol.getIndex()));
            
            // check string representation, just in case
            assertEquals(strs[i], symbol.toString());
        }
    }
}
