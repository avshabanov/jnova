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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests immutable list functional.
 */
public final class ImmListTest {

    @Test
    public void testBaseFunctional() {
        ImmList<Integer> list = ImmList.nil();

        assertTrue(list.isEmpty());

        list = list.append(1);
        assertFalse(list.isEmpty());
        assertTrue(list.size() == 1);

        list = list.append(2);
        assertFalse(list.isEmpty());
        assertTrue(list.size() == 2);

        {
            final Integer[] arr = list.toArray(new Integer[0]);
            assertArrayEquals(new Integer[] { 1, 2 }, arr);
        }
    }

    @Test
    public void testOfFunctional() {
        assertArrayEquals(new Integer[] { 1 }, ImmList.of(1).toArray(new Integer[0]));
        assertArrayEquals(new Integer[] { 10, 20 }, ImmList.of(10, 20).toArray(new Integer[0]));
        assertArrayEquals(new Integer[] { 10, 20, 30 }, ImmList.of(10, 20, 30).toArray(new Integer[0]));
        assertArrayEquals(new Integer[] { 5, 4, 3, 2, 1 },
                ImmList.of(5, 4, 3, 2, 1).toArray(new Integer[0]));
        assertArrayEquals(new Integer[] { 5, 4, 3, 2, 1, 0 },
                ImmList.of(5, 4, 3, 2, 1, 0).toArray(new Integer[0]));
    }

    @Test
    public void testReverse() {
        assertArrayEquals(new Integer[] { 1, 2, 3 },
                ImmList.of(3, 2, 1).reverse().toArray(new Integer[0]));
    }

    @Test
    public void testAppendList() {
        assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 },
                ImmList.of(1, 2).appendList(ImmList.of(3, 4, 5)).toArray(new Integer[0]));
    }

    @Test
    public void testPrependList() {
        assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 },
                ImmList.of(3, 4, 5).prependList(ImmList.of(1, 2)).toArray(new Integer[0]));
    }

    @Test
    public void testLast() {
        assertEquals(Integer.valueOf(3), ImmList.of(1, 2, 3).last());
    }

    @Test
    public void testHeadTail() {
        final Integer[] values = new Integer[] { 3, 4, 5 };
        final ImmList<Integer> list = ImmList.from(values);

        int i = 0;
        for (ImmList<Integer> it = list; it.getTail() != null; it = it.getTail()) {
            assertEquals(values[i], it.getHead());
            ++i;
        }

        assertEquals(values.length, i);
    }
}
