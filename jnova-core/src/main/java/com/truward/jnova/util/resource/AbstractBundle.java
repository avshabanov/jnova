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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Abstract implementation of the bundle interface.
 */
public abstract class AbstractBundle implements Bundle {
    private Reference<ResourceBundle> bundleReference;

    private ResourceBundle getBundle() {
        ResourceBundle bundle = null;
        if (bundleReference != null) {
            bundle = bundleReference.get();
        }

        if (bundle == null) {
            bundle = ResourceBundle.getBundle(getBundleName());
            bundleReference = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }

    private String messageOrDefault(String key, String defaultValue, Object... params) {
        if (getBundle() == null) {
            return defaultValue;
        }

        String value;
        try {
            value = getBundle().getString(key);
        }
        catch (MissingResourceException e) {
            if (defaultValue != null) {
                value = defaultValue;
            } else {
                value = "!" + key + "!";
                assert false: key + " is not found in " + getBundleName();
            }
        }

        value = postProcess(value);

        if (params.length > 0 && value.indexOf('{')>=0) {
            return MessageFormat.format(value, params);
        }

        return value;
    }

    protected abstract String getBundleName();

    protected String postProcess(String sourceMessage) {
        return sourceMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String message(String key, Object... params) {
        return messageOrDefault(key, null, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(String key) {
        return getBundle().getString(key);
    }
}
