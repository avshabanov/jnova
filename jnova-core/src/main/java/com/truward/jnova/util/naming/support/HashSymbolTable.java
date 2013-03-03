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

package com.truward.jnova.util.naming.support;

import com.truward.jnova.util.naming.Symbol;
import com.truward.jnova.util.naming.SymbolTable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Represents symbol table.
 */
public final class HashSymbolTable implements SymbolTable {

    /**
     * Internal symbol implementation.
     */
    private final class SymbolImpl implements Symbol {

        /**
         * Index of the symbol in the UTF-8 byte array.
         * @see HashSymbolTable#nameArray
         * @see #getIndex()
         */
        private final int index;

        /**
         * The number of bytes in this name.
         */
        private final int utfLength;

        /**
         * The next name occupying the same hash bucket.
         */
        private final SymbolImpl next;

        /**
         * Cached string representation.
         */
        private Reference<String> cachedString;



        private SymbolImpl(int index, int utfLength, SymbolImpl next) {
            this.index = index;
            this.utfLength = utfLength;
            this.next = next;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public int getUtfLength() {
            return utfLength;
        }

        @Override
        public int writeChars(char[] buffer, int startPos) {
            final int totalBytes = buffer.length >= (startPos + utfLength) ? utfLength : (buffer.length - startPos);
            int index = UtfUtil.utfToChars(HashSymbolTable.this.nameArray, this.index, buffer, startPos, totalBytes);
            return index - startPos;
        }

        @Override
        public String toString() {
            String result = cachedString != null ? cachedString.get() : null;

            if (result == null) {
                result = UtfUtil.utfToString(HashSymbolTable.this.nameArray, index, utfLength);
                cachedString = new WeakReference<String>(result);
            }

            return result;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public int compareTo(Symbol o) {
            if (o == null) {
                return -1;
            }

            if (!(o instanceof SymbolImpl)) {
                throw new UnsupportedOperationException("Compare to another instance of Symbol is not supported");
            }

            // fast equals
            if (this == o) {
                return 0;
            }

            final SymbolImpl another = (SymbolImpl) o;

            // compare lengths
            int len = this.utfLength;
            int anotherLen = another.utfLength;
            if (len != anotherLen) {
                return len - anotherLen;
            }

            // compare byte-to-byte
            final byte[] arr = getAssociatedArray();
            final byte[] anotherArr = another.getAssociatedArray();

            for (int i = 0; i < len; ++i) {
                final int bytesCmp = arr[index + i] - anotherArr[another.index + i];
                if (bytesCmp != 0) {
                    return bytesCmp;
                }
            }

            // symbols are equal to each other but from different symbol tables
            return 0;
        }

        private byte[] getAssociatedArray() {
            return HashSymbolTable.this.nameArray;
        }
    }

    /**
     * The hash table for names.
     */
    private SymbolImpl[] hashes;

    /**
     * The array that holds UTF-8 representations of stored symbols.
     */
    public byte[] nameArray;

    /**
     * The number of filled bytes in the name array.
     */
    private int bytesUtilized = 0;

    /**
     * The mask to be used for hashing
     */
    private int hashMask;



    /**
     * Public constructor.
     *
     * @param hashSize  Constant size to be used for the hash table needs to be a power of two.
     * @param nameSize  Initial size of the byte table used for storing UTF-8 representation of string.
     */
    public HashSymbolTable(int hashSize, int nameSize) {
        // check that hash size is a power of two
        assert (hashSize & (hashSize - 1)) == 0;

        hashMask = hashSize - 1;
        hashes = new SymbolImpl[hashSize];
        nameArray = new byte[nameSize];
    }

    /**
     * Default public constructor.
     */
    public HashSymbolTable() {
        this(0x8000, 0x20000);
    }

    // Calculates the hashcode of a name.
    private static int hashValue(byte utfName[], int start, int len) {
        int h = 0;
        int offset = start;

        for (int i = 0; i < len; i++) {
            h = utfName[offset++] + (h << 5) - h;
        }

        return h;
    }

    // Does the utf8 representation of name equal to cs[start..start+len-1]?
    private static boolean equals(byte[] names, int index, byte cs[], int start, int length) {
        int i = 0;

        while (i < length && names[index + i] == cs[start + i]) {
            i++;
        }

        return i == length;
    }


    @Override
    public Symbol fromChars(char[] src, int start, int length) {
        final int curBytesUtilized = this.bytesUtilized;
        byte[] curNameArray = this.nameArray;

        // double name buffer when getting close to its bounds
        while (curBytesUtilized + length * 3 >= curNameArray.length) {
            byte[] newnames = new byte[curNameArray.length * 2];
            System.arraycopy(curNameArray, 0, newnames, 0, curNameArray.length);
            curNameArray = this.nameArray = newnames;
        }

        // try to find existing symbol
        final int utfLen = UtfUtil.charsToUtf(src, start, curNameArray, curBytesUtilized, length) - curBytesUtilized;
        final int hash = hashValue(curNameArray, curBytesUtilized, utfLen) & this.hashMask;
        SymbolImpl symbol = this.hashes[hash];
        while (symbol != null && (symbol.utfLength != utfLen || !equals(curNameArray, symbol.index, curNameArray, curBytesUtilized, utfLen))) {
            symbol = symbol.next;
        }

        // insert new symbol if no existing found
        if (symbol == null) {
            symbol = new SymbolImpl(curBytesUtilized, utfLen, this.hashes[hash]);

            this.hashes[hash] = symbol;
            this.bytesUtilized = curBytesUtilized + utfLen;

            if (utfLen == 0) {
                this.bytesUtilized++;
            }
        }

        return symbol;
    }

    @Override
    public Symbol fromSequence(CharSequence src) {
        // TODO: Special treatment for CharSequence?
        return fromChars(src.toString().toCharArray(), 0, src.length());
    }

    @Override
    public Symbol fromIndex(int symbolIndex) {
        if (symbolIndex < 0) {
            throw new IllegalArgumentException("Symbol index #" + symbolIndex + " can not be negative");
        }

        for (final SymbolImpl symbolImpl : hashes) {
            for (SymbolImpl s = symbolImpl; s != null; s = s.next) {
                if (s.index == symbolIndex) {
                    return s;
                }
            }
        }

        throw new IllegalArgumentException("There is no symbol with the given index #" + symbolIndex);
    }
}