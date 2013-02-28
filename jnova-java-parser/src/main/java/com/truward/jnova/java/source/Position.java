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

package com.truward.jnova.java.source;

/**
 * Utility class that defines source code positions as simple character offsets from the beginning of the file.
 * The first character is at position 0.
 *
 * Support is also provided for (line,column) coordinates, but tab expansion is optional and no Unicode
 * excape translation is considered. The first character is at location (1, 1).
 */
public class Position {
    private Position() {}





    public static final int NOPOS        = -1;

    public static final int FIRSTPOS     = 0;
    public static final int FIRSTLINE    = 1;
    public static final int FIRSTCOLUMN  = 1;

    public static final int LINESHIFT    = 10;
    public static final int MAXCOLUMN    = (1<<LINESHIFT) - 1;
    public static final int MAXLINE      = (1<<(Integer.SIZE-LINESHIFT)) - 1;

    public static final int MAXPOS       = Integer.MAX_VALUE;


}
