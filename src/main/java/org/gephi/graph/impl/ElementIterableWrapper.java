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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;

public abstract class ElementIterableWrapper<T extends Element> implements ElementIterable<T> {

    protected final Supplier<Iterator<T>> iteratorSupplier;
    protected final Supplier<Spliterator<T>> spliteratorSupplier;
    protected final GraphLockImpl lock;
    protected final boolean parallelPossible;

    public ElementIterableWrapper(Supplier<Iterator<T>> iteratorSupplier, GraphLockImpl lock) {
        this.iteratorSupplier = iteratorSupplier;
        this.spliteratorSupplier = () -> Spliterators
                .spliteratorUnknownSize(iteratorSupplier.get(), Spliterator.ORDERED | Spliterator.NONNULL);
        this.lock = lock;
        this.parallelPossible = false;
    }

    public ElementIterableWrapper(Supplier<Iterator<T>> iteratorSupplier, Supplier<Spliterator<T>> spliteratorSupplier, GraphLockImpl lock) {
        this.iteratorSupplier = iteratorSupplier;
        this.spliteratorSupplier = spliteratorSupplier;
        this.lock = lock;
        this.parallelPossible = true;
    }

    @Override
    public Iterator<T> iterator() {
        return iteratorSupplier.get();
    }

    @Override
    public Spliterator<T> spliterator() {
        return spliteratorSupplier.get();
    }

    @Override
    public Stream<T> parallelStream() {
        if (!parallelPossible) {
            throw new UnsupportedOperationException("Parallel stream not supported for this operation.");
        }
        return ElementIterable.super.parallelStream();
    }

    public abstract T[] toArray();

    @Override
    public Collection<T> toCollection() {
        if (parallelPossible && lock != null) {
            lock.readLock();
            try {
                return StreamSupport.stream(spliterator(), true).collect(Collectors.toList());
            } finally {
                lock.readUnlock();
            }
        }
        return StreamSupport.stream(spliterator(), parallelPossible).collect(Collectors.toList());
    }

    @Override
    public Set<T> toSet() {
        if (parallelPossible && lock != null) {
            lock.readLock();
            try {
                return StreamSupport.stream(spliterator(), true).collect(Collectors.toSet());
            } finally {
                lock.readUnlock();
            }
        }
        return StreamSupport.stream(spliterator(), parallelPossible).collect(Collectors.toSet());
    }

    @Override
    public void doBreak() {
        if (lock != null) {
            lock.readUnlock();
        }
    }
}
