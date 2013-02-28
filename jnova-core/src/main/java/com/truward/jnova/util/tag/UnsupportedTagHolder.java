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

package com.truward.jnova.util.tag;

/**
 * Base implementation for objects that are not expected to provide tag holder functionality (e.g. singletons).
 */
public class UnsupportedTagHolder implements TagHolder {
    @Override
    public <T> T putTag(Key<T> key, T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getTag(Key<T> key) {
        throw new UnsupportedOperationException();
    }
}
