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
 * Represents simple key-value store abstraction.
 * The data added to the instance of such an interface is implementation-specific.
 */
public interface TagHolder {
    /**
     * Associates value with the provided key.
     *
     * @param key   Tag key.
     * @param value Value, associated with the given tag key.
     * @param <T>   Type of value.
     * @return Value, that was previously associated with the key given.
     */
    <T> T putTag(Key<T> key, T value);

    /**
     * Gets associated value.
     *
     * @param key   Tag key.
     * @param <T>   Type of associated value.
     * @return      Associated value, or default value associated with key. {@see Key#getDefaultValue}
     */
    <T> T getTag(Key<T> key);
}
