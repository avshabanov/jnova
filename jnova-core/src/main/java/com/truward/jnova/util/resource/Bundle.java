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

package com.truward.jnova.util.resource;

/**
 * Defines interface to the resource bundle.
 */
public interface Bundle {
    /**
     * Formats message by using the parameters provided.
     * The given key will be used to retrieve localized message and then
     * used in the MessageFormat's format method.
     * @see java.text.MessageFormat
     * @see #getString(String)
     *
     * @param key       Resource key.
     * @param params    Format parameters.
     * @return Formatted message.
     */
    String message(String key, Object... params);

    /**
     * Gets resource string associated with the key given.
     *
     * @param key       Resource key.
     * @return Associated message.
     */
    String getString(String key);
}
