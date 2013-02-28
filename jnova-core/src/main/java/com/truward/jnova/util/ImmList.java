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

import java.lang.reflect.Array;
import java.util.*;

/**
 * Immutable linked list, main container class, used in this library.
 * <p>The list IS NOT expected to contain null elements.</p>
 * <p>This class is introduced for efficiency reasons.</p>
 */
public final class ImmList<A> extends AbstractCollection<A> implements List<A> {
    /**
     * The first element of the list, non-null, immutable.
     */
    private final A head;

    /**
     * The remainder of the list except for its first element, immutable.
     */
    private final ImmList<A> tail;

    /**
     * @return Head of the list.
     */
    public A getHead() {
        return head;
    }

    /**
     * @return Tail of the list.
     */
    public ImmList<A> getTail() {
        return tail;
    }

    /**
     * Construct a list given its head and tail.
     *
     * @param head List's head, not null.
     * @param tail List's tail.
     */
    private ImmList(A head, ImmList<A> tail) {
        assert head != null;
        this.head = head;
        this.tail = tail;
    }

    /**
     * Special constructor - for sentinel node only.
     * Constructor #ImmList ctor can't be used due to assertion on head.
     * This is introduced in order to not to break final modifier on this class as well as on it's state.
     *
     * @param sentinel Should always be true, identifies creating node for sentinel.
     */
    private ImmList(boolean sentinel) {
        assert sentinel;
        this.head = null;
        this.tail = null;
    }

    /**
     * Construct an empty list.
     * @return Empty list.
     */
    @SuppressWarnings("unchecked")
    public static <A> ImmList<A> nil() {
        return EMPTY_LIST;
    }

    // sentinel object
    private static ImmList EMPTY_LIST = new ImmList<Object>(true);

    /**
     * Construct a list consisting of given element.
     * @param x1 Element.
     * @return List of one element.
     */
    public static <A> ImmList<A> of(A x1) {
        return new ImmList<A>(x1, ImmList.<A>nil());
    }

    public static <A> ImmList<A> of(A x1, A x2) {
        return new ImmList<A>(x1, of(x2));
    }

    public static <A> ImmList<A> of(A x1, A x2, A x3) {
        return new ImmList<A>(x1, new ImmList<A>(x2, new ImmList<A>(x3, ImmList.<A>nil())));
    }

    public static <A> ImmList<A> of(A x1, A x2, A x3, A... rest) {
        return new ImmList<A>(x1, new ImmList<A>(x2, new ImmList<A>(x3, from(rest))));
    }

    /**
     * Construct a list consisting all elements of given array.
     * @param array an array; Return an empty list if null.
     * @return Newly created immutable list.
     */
    public static <A> ImmList<A> from(A[] array) {
        ImmList<A> xs = nil();
        if (array != null) {
            for (int i = array.length - 1; i >= 0; i--) {
                xs = new ImmList<A>(array[i], xs);
            }
        }

        return xs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return tail == null;
    }

    /**
     * Tests list for non-emptiness. Opposite to {@see #isEmpty}.
     * @return True if list is not empty, false otherwise.
     */
    public boolean nonEmpty() {
        return !isEmpty();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public int size() {
        ImmList<A> l = this;
        int len = 0;
        while (l.tail != null) {
            l = l.tail;
            len++;
        }
        return len;
    }

    /**
     * Prepend given element to front of list, forming and returning a new list.
     *
     * @param x element to be prepended.
     * @return New instance of the immutable list.
     */
    public ImmList<A> prepend(A x) {
        return new ImmList<A>(x, this);
    }

    /**
     * Prepend given list of elements to front of list, forming and returning a new list.
     *
     * @param xs List to be appended.
     * @return Resultant list.
     */
    public ImmList<A> prependList(ImmList<? extends A> xs) {
        if (this.isEmpty()) {
            // suppress unchecked warning - this should never fail as we're picking subtypes of A
            @SuppressWarnings("unchecked")
            final ImmList<A> adapted = (ImmList<A>) xs;
            assert adapted != null; // TODO: remove
            return adapted;
        }

        if (xs.isEmpty()) {
            return this;
        }

        if (xs.tail.isEmpty()) {
            return prepend(xs.head);
        }

        return this.prependList(xs.tail).prepend(xs.head);
    }

    /**
     * Reverse list.
     * If the list is empty or a singleton, then the same list is returned.
     * Otherwise a new list is formed.
     *
     * @return Reversed list.
     */
    public ImmList<A> reverse() {
        // if it is empty or a singleton, return itself
        if (isEmpty() || tail.isEmpty()) {
            return this;
        }

        ImmList<A> rev = nil();
        for (ImmList<A> l = this; !l.isEmpty(); l = l.tail) {
            rev = new ImmList<A>(l.head, rev);
        }

        return rev;
    }

    /**
     * Append given element at length, forming and returning a new list.
     *
     * @param x Element to be appended.
     * @return Newly allocated list.
     */
    public ImmList<A> append(A x) {
        return of(x).prependList(this);
    }

    /**
     * Append given list at length, forming and returning a new list.
     *
     * @param x List to be appended.
     * @return New list instance
     */
    public ImmList<A> appendList(ImmList<? extends A> x) {
        // suppress convertation - this should never fail as we're picking subtypes of A
        @SuppressWarnings("unchecked")
        final ImmList<A> adapted = (ImmList<A>) x;
        return adapted.prependList(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] vec) {
        int i = 0;
        ImmList<T> l = (ImmList<T>) this;

        while (!l.isEmpty() && i < vec.length) {
            vec[i] = l.head;
            l = l.tail;
            i++;
        }

        if (l.isEmpty()) {
            if (i < vec.length) {
                vec[i] = null;
            }

            return vec;
        }

        vec = (T[]) Array.newInstance(vec.getClass().getComponentType(), size());
        return toArray(vec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    /**
     * Form a string listing all elements with given separator character.
     *
     * @param sep Char separator.
     * @return Stringified list representation.
     */
    public String toString(String sep) {
        if (isEmpty()) {
            return "";
        } else {
            final StringBuilder builder = new StringBuilder();

            builder.append(head);
            for (ImmList<A> l = tail; !l.isEmpty(); l = l.tail) {
                builder.append(sep);
                builder.append(l.head);
            }

            return builder.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString(",");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        ImmList<A> l = this;
        int h = 1;
        while (l.tail != null) {
            h = h * 31 + (l.head == null ? 0 : l.head.hashCode());
            l = l.tail;
        }
        return h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmList<?>) {
            return equals(this, (ImmList<?>)other);
        }

        if (other instanceof List<?>) {
            ImmList<A> t = this;
            Iterator<?> oIter = ((java.util.List<?>) other).iterator();
            while (t.tail != null && oIter.hasNext()) {
                Object o = oIter.next();
                if ( !(t.head == null ? o == null : t.head.equals(o)))
                    return false;
                t = t.tail;
            }
            return (t.isEmpty() && !oIter.hasNext());
        }

        return false;
    }

    /**
     * Checks whether the two given lists the same?
     *
     * @param xs Left list parameter.
     * @param ys Right list parameter.
     * @return True if equals, false otherwise.
     */
    public static boolean equals(ImmList xs, ImmList ys) {
        while (xs.tail != null && ys.tail != null) {
            if (xs.head == null) {
                if (ys.head != null) return false;
            } else {
                if (!xs.head.equals(ys.head)) return false;
            }
            xs = xs.tail;
            ys = ys.tail;
        }
        return xs.tail == null && ys.tail == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object x) {
        ImmList<A> l = this;
        while (l.tail != null) {
            if (x == null) {
                if (l.head == null) return true;
            } else {
                if (l.head.equals(x)) return true;
            }
            l = l.tail;
        }
        return false;
    }

    /**
     * @return The last element in the list, if any, or null.
     */
    public A last() {
        A last = null;
        ImmList<A> t = this;
        while (t.tail != null) {
            last = t.head;
            t = t.tail;
        }

        return last;
    }

//    @SuppressWarnings("unchecked")
//    public static <T> List<T> convert(ClassDecl<T> klass, List<?> list) {
//        if (list == null) {
//            return null;
//        }
//
//        for (Object o : list) {
//            klass.cast(o);
//        }
//
//        return (List<T>)list;
//    }

    private static final Iterator EMPTYITERATOR = new Iterator() {
        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public Object next() {
            throw new NoSuchElementException();
        }

        /**
         * {@inheritDoc}
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    @SuppressWarnings("unchecked")
    private static <A> Iterator<A> emptyIterator() {
        return EMPTYITERATOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<A> iterator() {
        if (tail == null) {
            return emptyIterator();
        }

        return new Iterator<A>() {
            ImmList<A> elems = ImmList.this;

            /**
             * {@inheritDoc}
             */
            public boolean hasNext() {
                return elems.tail != null;
            }

            /**
             * {@inheritDoc}
             */
            public A next() {
                if (elems.tail == null) {
                    throw new NoSuchElementException();
                }

                final A result = elems.head;
                elems = elems.tail;
                return result;
            }

            /**
             * {@inheritDoc}
             */
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public A get(int index) {
        if (index < 0)
            throw new IndexOutOfBoundsException(String.valueOf(index));

        ImmList<A> l = this;
        for (int i = index; i-- > 0 && !l.isEmpty(); l = l.tail) {}

        if (l.isEmpty()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", " +
                    "Size: " + size());
        }

        return l.head;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(int index, Collection<? extends A> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public A set(int index, A element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int index, A element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public A remove(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Object o) {
        int i = 0;
        for (ImmList<A> l = this; l.tail != null; l = l.tail, i++) {
            if (l.head == null ? o == null : l.head.equals(o)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(Object o) {
        int last = -1;
        int i = 0;
        for (ImmList<A> l = this; l.tail != null; l = l.tail, i++) {
            if (l.head == null ? o == null : l.head.equals(o)) {
                last = i;
            }
        }
        return last;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<A> listIterator() {
        return Collections.unmodifiableList(new ArrayList<A>(this)).listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<A> listIterator(int index) {
        return Collections.unmodifiableList(new ArrayList<A>(this)).listIterator(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<A> subList(int fromIndex, int toIndex) {
        if  (fromIndex < 0 || toIndex > size() || fromIndex > toIndex)
            throw new IllegalArgumentException();

        ArrayList<A> a = new ArrayList<A>(toIndex - fromIndex);
        int i = 0;
        for (ImmList<A> l = this; l.tail != null; l = l.tail, i++) {
            if (i == toIndex)
                break;
            if (i >= fromIndex)
                a.add(l.head);
        }

        return Collections.unmodifiableList(a);
    }
}
