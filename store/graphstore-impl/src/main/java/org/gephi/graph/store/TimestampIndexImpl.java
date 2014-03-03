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
package org.gephi.graph.store;

import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.gephi.attribute.api.TimestampIndex;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;

/**
 *
 * @author mbastian
 */
public class TimestampIndexImpl<T extends Element> implements TimestampIndex<T> {
    //Const

    protected static final int NULL_INDEX = -1;
    //Data
    protected final GraphLock lock;
    protected final TimestampIndexStore timestampIndexStore;
    protected final TimestampMap timestampMap;
    protected final boolean mainIndex;
    protected TimestampIndexEntry[] timestamps;
    protected int elementCount;

    public TimestampIndexImpl(TimestampIndexStore store, boolean main) {
        timestampIndexStore = store;
        timestampMap = store.timestampMap;
        mainIndex = main;
        timestamps = new TimestampIndexEntry[0];
        lock = store.timestampStore.lock;
    }

    @Override
    public double getMinTimestamp() {
        if (mainIndex) {
            Double2IntSortedMap sortedMap = timestampMap.timestampSortedMap;
            if (!sortedMap.isEmpty()) {
                return sortedMap.firstDoubleKey();
            }
        } else {
            Double2IntSortedMap sortedMap = timestampMap.timestampSortedMap;
            if (!sortedMap.isEmpty()) {
                ObjectBidirectionalIterator<Double2IntMap.Entry> bi = sortedMap.double2IntEntrySet().iterator();
                while (bi.hasNext()) {
                    Double2IntMap.Entry entry = bi.next();
                    double timestamp = entry.getDoubleKey();
                    int index = entry.getIntValue();
                    if (index < timestamps.length) {
                        TimestampIndexEntry timestampEntry = timestamps[index];
                        if (timestampEntry != null) {
                            return timestamp;
                        }
                    }
                }
            }
        }
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double getMaxTimestamp() {
        if (mainIndex) {
            Double2IntSortedMap sortedMap = timestampMap.timestampSortedMap;
            if (!sortedMap.isEmpty()) {
                return sortedMap.lastDoubleKey();
            }
        } else {
            Double2IntSortedMap sortedMap = timestampMap.timestampSortedMap;
            if (!sortedMap.isEmpty()) {
                ObjectBidirectionalIterator<Double2IntMap.Entry> bi = sortedMap.double2IntEntrySet().iterator(sortedMap.double2IntEntrySet().last());
                while (bi.hasPrevious()) {
                    Double2IntMap.Entry entry = bi.previous();
                    double timestamp = entry.getDoubleKey();
                    int index = entry.getIntValue();
                    if (index < timestamps.length) {
                        TimestampIndexEntry timestampEntry = timestamps[index];
                        if (timestampEntry != null) {
                            return timestamp;
                        }
                    }
                }
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public ElementIterable get(double timestamp) {
        checkDouble(timestamp);

        readLock();
        int index = timestampMap.timestampMap.get(timestamp);
        if (index != NULL_INDEX) {
            TimestampIndexEntry ts = timestamps[index];
            if (ts != null) {
                return new ElementIterableImpl(new ElementIteratorImpl(ts.elementSet.iterator()));
            }
        }
        readUnlock();
        return ElementIterable.EMPTY;
    }

    @Override
    public ElementIterable get(double from, double to) {
        checkDouble(from);
        checkDouble(to);

        readLock();
        ObjectSet<ElementImpl> elements = new ObjectOpenHashSet<ElementImpl>();
        Double2IntSortedMap sortedMap = timestampMap.timestampSortedMap;
        if (!sortedMap.isEmpty()) {
            for (Double2IntMap.Entry entry : sortedMap.tailMap(from).double2IntEntrySet()) {
                double timestamp = entry.getDoubleKey();
                int index = entry.getIntValue();
                if (timestamp <= to) {
                    TimestampIndexEntry ts = timestamps[index];
                    if (ts != null) {
                        elements.addAll(ts.elementSet);
                    }
                } else {
                    break;
                }
            }
        }
        if (!elements.isEmpty()) {
            return new ElementIterableImpl(new ElementIteratorImpl(elements.iterator()));
        }
        return ElementIterable.EMPTY;
    }

    public boolean hasElements() {
        return elementCount > 0;
    }

    public void clear() {
        timestamps = new TimestampIndexEntry[0];
        elementCount = 0;
    }

    protected void add(int timestampIndex, ElementImpl element) {
        ensureArraySize(timestampIndex);
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry == null) {
            entry = addTimestamp(timestampIndex);
        }
        if (entry.add(element)) {
            elementCount++;
        }
    }

    protected void remove(int timestampIndex, ElementImpl element) {
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry.remove(element)) {
            elementCount--;
            if (entry.isEmpty()) {
                clearEntry(timestampIndex);
            }
        }
    }

    protected TimestampIndexEntry addTimestamp(final int index) {
        ensureArraySize(index);
        TimestampIndexEntry entry = new TimestampIndexEntry();
        timestamps[index] = entry;
        return entry;
    }

    protected void removeTimestamp(final int index) {
        timestamps[index] = null;
    }

    private void ensureArraySize(int index) {
        if (index >= timestamps.length) {
            TimestampIndexEntry[] newArray = new TimestampIndexEntry[index + 1];
            System.arraycopy(timestamps, 0, newArray, 0, timestamps.length);
            timestamps = newArray;
        }
    }

    private void clearEntry(int index) {
        timestamps[index] = null;
    }

    private void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can' be NaN or infinity");
        }
    }

    private void readLock() {
        if (lock != null) {
            lock.readLock();
        }
    }

    private void readUnlock() {
        if (lock != null) {
            lock.readUnlock();
        }
    }

    private void writeLock() {
        if (lock != null) {
            lock.writeLock();
        }
    }

    private void writeUnlock() {
        if (lock != null) {
            lock.writeUnlock();
        }
    }

    protected static class TimestampIndexEntry {

        protected final ObjectSet<ElementImpl> elementSet;

        public TimestampIndexEntry() {
            elementSet = new ObjectOpenHashSet<ElementImpl>();
        }

        public boolean add(ElementImpl element) {
            return elementSet.add(element);
        }

        public boolean remove(ElementImpl element) {
            return elementSet.remove(element);
        }

        public boolean isEmpty() {
            return elementSet.isEmpty();
        }
    }

    protected class ElementIteratorImpl implements Iterator<Element> {

        private final ObjectIterator<ElementImpl> itr;

        public ElementIteratorImpl(ObjectIterator<ElementImpl> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Element next() {
            return itr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    protected class ElementIterableImpl implements ElementIterable {

        protected final Iterator<Element> iterator;

        public ElementIterableImpl(Iterator<Element> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Iterator<Element> iterator() {
            return iterator;
        }

        @Override
        public Element[] toArray() {
            List<Element> list = new ArrayList<Element>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list.toArray(new Element[0]);
        }

        @Override
        public Collection<Element> toCollection() {
            List<Element> list = new ArrayList<Element>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list;
        }

        @Override
        public void doBreak() {
            readUnlock();
        }
    }
}
