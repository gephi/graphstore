/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gephi.graph.impl;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;

/**
 * Iterable over the elements of a graph iterable that match a filter. The graph read lock is held by the source
 * iterator until it is exhausted or {@link #doBreak()} is called.
 */
class FilteredElementIterable<T extends Element> implements ElementIterable<T> {

    private final ElementIterable<T> source;
    private final Predicate<T> filter;
    private final Class<T> elementClass;
    // Most recently opened iterator whose source still holds the lock
    private FilteredIterator last;

    FilteredElementIterable(ElementIterable<T> source, Predicate<T> filter, Class<T> elementClass) {
        this.source = source;
        this.filter = filter;
        this.elementClass = elementClass;
    }

    @Override
    public Iterator<T> iterator() {
        return new FilteredIterator(source.iterator());
    }

    @Override
    public Stream<T> parallelStream() {
        throw new UnsupportedOperationException("Parallel stream not supported for this operation.");
    }

    @Override
    public T[] toArray() {
        Collection<T> elements = toCollection();
        return elements.toArray((T[]) Array.newInstance(elementClass, elements.size()));
    }

    @Override
    public Collection<T> toCollection() {
        List<T> elements = new ArrayList<>();
        for (T element : this) {
            elements.add(element);
        }
        return elements;
    }

    @Override
    public Set<T> toSet() {
        return new ObjectOpenHashSet<>(toCollection());
    }

    @Override
    public void doBreak() {
        while (last != null) {
            last.release();
        }
    }

    private final class FilteredIterator implements Iterator<T> {

        private final Iterator<T> itr;
        private boolean open = true;
        private T pointer;
        private FilteredIterator prev;
        private FilteredIterator next;

        public FilteredIterator(Iterator<T> itr) {
            this.itr = itr;
            prev = last;
            if (prev != null) {
                prev.next = this;
            }
            last = this;
        }

        @Override
        public boolean hasNext() {
            try {
                while (pointer == null && open) {
                    if (itr.hasNext()) {
                        T element = itr.next();
                        if (filter.test(element)) {
                            pointer = element;
                        }
                    } else {
                        // The source iterator released the lock
                        unlink();
                    }
                }
            } catch (RuntimeException | Error e) {
                release();
                throw e;
            }
            return pointer != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T element = pointer;
            pointer = null;
            return element;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        private void release() {
            if (open) {
                source.doBreak();
                unlink();
            }
        }

        private void unlink() {
            open = false;
            pointer = null;
            if (prev != null) {
                prev.next = next;
            }
            if (next != null) {
                next.prev = prev;
            } else {
                last = prev;
            }
            prev = null;
            next = null;
        }
    }
}
