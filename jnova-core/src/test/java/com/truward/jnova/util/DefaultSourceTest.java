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


import com.truward.jnova.util.source.Source;
import com.truward.jnova.util.source.support.DefaultSource;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class DefaultSourceTest {


    @Test
    public void testStringContent() {
        final String content = "content";
        final Source source = new DefaultSource(content);

        assertEquals(content, new String(source.getBuffer(), 0, source.length()));
        assertEquals(source.length(), source.getBuffer().length);

        final int overflowSize = 100;
        source.setOverflowSize(overflowSize);

        assertEquals(content, new String(source.getBuffer(), 0, source.length()));
        assertEquals(source.length() + overflowSize, source.getBuffer().length);
    }

    @Test
    public void testSourceRead() {
        final String path = "/testSource.txt";
        final InputStream inputStream = DefaultSourceTest.class.getResourceAsStream(path);
        assertNotNull(inputStream);

        final Source source = new DefaultSource(inputStream, path, "ISO-8859-1");

        assertEquals(path, source.getSourceName());
        assertEquals("This is a test", new String(source.getBuffer(), 0, source.length()));
    }

    @Test
    public void testEmptySource() {
        final Source source = new DefaultSource("");
        assertArrayEquals(new char[0], source.getBuffer());
        assertEquals(0, source.length());
    }
}
