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

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.TimeIndex;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;

public abstract class TimeIndexImpl<T extends Element, K, S extends TimeSet<K>, M extends TimeMap<K, ?>> implements TimeIndex<T> {

    // Data
    protected final TableLockImpl lock;
    protected final TimeIndexStore<T, K, S, M> timestampIndexStore;
    protected final boolean mainIndex;
    protected TimeIndexEntry[] timestamps;
    protected int elementCount;

    protected TimeIndexImpl(TimeIndexStore<T, K, S, M> store, boolean main) {
        timestampIndexStore = store;
        mainIndex = main;
        timestamps = new TimeIndexEntry[0];
        lock = store.lock;
    }

    public boolean hasElements() {
        return elementCount > 0;
    }

    public void clear() {
        lock();
        timestamps = new TimeIndexEntry[0];
        elementCount = 0;
        unlock();
    }

    protected void add(int timestampIndex, Element element) {
        lock();
        try {
            ensureArraySize(timestampIndex);
            TimeIndexEntry entry = timestamps[timestampIndex];
            if (entry == null) {
                entry = addTimestamp(timestampIndex);
            }
            if (entry.add(element)) {
                elementCount++;
            }
        } finally {
            unlock();
        }
    }

    protected void remove(int timestampIndex, Element element) {
        lock();
        try {
            TimeIndexEntry entry = timestamps[timestampIndex];
            if (entry.remove(element)) {
                elementCount--;
                if (entry.isEmpty()) {
                    clearEntry(timestampIndex);
                }
            }
        } finally {
            unlock();
        }
    }

    private TimeIndexEntry addTimestamp(final int index) {
        ensureArraySize(index);
        TimeIndexEntry entry = new TimeIndexEntry();
        timestamps[index] = entry;
        return entry;
    }

    private void ensureArraySize(int index) {
        if (index >= timestamps.length) {
            TimeIndexEntry[] newArray = new TimeIndexEntry[index + 1];
            System.arraycopy(timestamps, 0, newArray, 0, timestamps.length);
            timestamps = newArray;
        }
    }

    private void clearEntry(int index) {
        timestamps[index] = null;
    }

    protected void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can' be NaN or infinity");
        }
    }

    protected void lock() {
        if (lock != null) {
            lock.lock();
        }
    }

    protected void unlock() {
        if (lock != null) {
            lock.unlock();
        }
    }

    protected static class TimeIndexEntry {

        protected final ObjectSet<Element> elementSet;

        public TimeIndexEntry() {
            elementSet = new ObjectOpenHashSet<>();
        }

        public boolean add(Element element) {
            return elementSet.add(element);
        }

        public boolean remove(Element element) {
            return elementSet.remove(element);
        }

        public boolean isEmpty() {
            return elementSet.isEmpty();
        }
    }

    protected class ElementSetWrapperIterable implements ElementIterable {

        protected final Set<Element> set;

        public ElementSetWrapperIterable(Set<Element> set) {
            this.set = set;
        }

        @Override
        public Iterator<Element> iterator() {
            return set.iterator();
        }

        @Override
        public Element[] toArray() {
            return set.toArray(new Element[0]);
        }

        @Override
        public Collection<Element> toCollection() {
            return set;
        }

        @Override
        public Set<Element> toSet() {
            return set;
        }

        @Override
        public void doBreak() {
        }
    }
}
