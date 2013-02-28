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

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the tag holder.
 */
public class DefaultTagHolder implements TagHolder {

    private final Map<Key<?>, Object> tagMap = new HashMap<Key<?>, Object>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T putTag(Key<T> key, T value) {
        return (T) tagMap.put(key, value);
    }

    @Override
    public <T> T getTag(Key<T> key) {
        @SuppressWarnings("unchecked")
        final T result = (T) tagMap.get(key);
        if (result == null) {
            return key.getDefaultValue();
        }
        return result;
    }
}
