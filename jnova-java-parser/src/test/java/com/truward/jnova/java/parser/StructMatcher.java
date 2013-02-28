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

package com.truward.jnova.java.parser;

import org.junit.Ignore;

import java.lang.reflect.Field;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Utility matcher.
 */
@Ignore
public final class StructMatcher {
    private StructMatcher() {} // Prevent instantiation

    /**
     * Checks that two given objects are structurally equivalent (i.e. their fields match each other).
     *
     * @param baseClass Base class in the matched hierarchy for which structural equivalience should be checked.
     * @param expected Expected object.
     * @param actual Actual object.
     * @param <T> Base structure class.
     */
    public static <T> void assertStructMatches(Class<T> baseClass, Object expected, Object actual) {
        if (expected == null) {
            assertNull("Actual node should be null, but it isn't: " + actual, actual);
            return;
        }
        assertNotNull("Actual node is null, but expected isn't: " + expected, actual);

        assertEquals(actual.getClass(), expected.getClass());

        // special case: iterable
        if (Iterable.class.isAssignableFrom(actual.getClass())) {
            final Iterator actualIt = ((Iterable) actual).iterator();
            for (final Object expectedObj : (Iterable)expected) {
                assertTrue(actualIt.hasNext());
                assertStructMatches(baseClass, expectedObj, actualIt.next());
            }
            return;
        }

        // primitive type: no structural matching is required
        if (!baseClass.isAssignableFrom(actual.getClass())) {
            assertEquals(expected, actual);
            return;
        }

        // field-by-field match
        final Field[] expectedFields = expected.getClass().getDeclaredFields();
        final Field[] actualFields = actual.getClass().getDeclaredFields();

        assertEquals(expectedFields.length, actualFields.length);

        for (int i = 0; i < expectedFields.length; ++i) {
            final Field expectedField = expectedFields[i];
            final Field actualField = actualFields[i];

            final boolean prevExpectedAcc = expectedField.isAccessible();
            final boolean prevActualAcc = actualField.isAccessible();

            expectedField.setAccessible(true);
            actualField.setAccessible(true);

            try {
                final Object expectedValue = expectedField.get(expected);
                final Object actualValue = actualField.get(actual);
                assertStructMatches(baseClass, expectedValue, actualValue);
            } catch (IllegalAccessException e) {
                fail(e.getMessage());
            } finally {
                expectedField.setAccessible(prevExpectedAcc);
                actualField.setAccessible(prevActualAcc);
            }
        }
    }
}
