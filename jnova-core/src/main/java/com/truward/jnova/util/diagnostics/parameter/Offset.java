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

package com.truward.jnova.util.diagnostics.parameter;

/**
 * Represents offset in the source file.
 * Negative offset implies invalid value.
 */
public final class Offset implements DiagnosticsParameter {
    private final int offset;

    /**
     * Invalid offset instance introduced for convenience.
     */
    public static Offset INVALID = at(-1);

    private Offset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isValid() {
        return offset >= 0;
    }

    public static Offset at(int offset) {
        return new Offset(offset);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Offset that = (Offset) o;

        return offset == that.offset;

    }

    @Override
    public int hashCode() {
        return offset;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("#").append(offset);
        return sb.toString();
    }
}
