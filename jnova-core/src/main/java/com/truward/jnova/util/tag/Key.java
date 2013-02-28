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
 * Tag key.
 * @param <T> Value, associated with the given key.
 */
public final class Key<T> {
    private final T defaultValue;

    public Key(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Key() {
        this.defaultValue = null;
    }

    /**
     * Retrieves the default value associated with the key.
     * This value will be returned if the value, associated with the key provided is null or was
     * never specified.
     *
     * @return Default value, associated with the key given.
     */
    public T getDefaultValue() {
        return defaultValue;
    }
}
