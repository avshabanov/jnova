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

import com.truward.jnova.util.tag.DefaultTagHolder;
import com.truward.jnova.util.tag.Key;
import com.truward.jnova.util.tag.TagHolder;
import com.truward.jnova.util.tag.UnsupportedTagHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests tag holder.
 */
public final class TagHolderTest {

    private static final Key<Integer> KEY1 = new Key<Integer>();

    private static final Key<Integer> KEY2 = new Key<Integer>(42);

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedGetTag() {
        final TagHolder tagHolder = new UnsupportedTagHolder();
        tagHolder.getTag(KEY1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedPutTag() {
        final TagHolder tagHolder = new UnsupportedTagHolder();
        tagHolder.putTag(KEY1, 1);
    }

    @Test
    public void testDefaultTagHolder() {
        final TagHolder tagHolder = new DefaultTagHolder();

        // default value
        assertEquals(null, tagHolder.getTag(KEY1));
        assertEquals(KEY2.getDefaultValue(), tagHolder.getTag(KEY2));

        tagHolder.putTag(KEY1, 1000);
        tagHolder.putTag(KEY2, 2222);

        assertEquals(Integer.valueOf(1000), tagHolder.getTag(KEY1));
        assertEquals(Integer.valueOf(2222), tagHolder.getTag(KEY2));
    }
}
