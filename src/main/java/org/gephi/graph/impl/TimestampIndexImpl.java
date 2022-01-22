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

import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;

public class TimestampIndexImpl<T extends Element> extends TimeIndexImpl<T, Double, TimestampSet, TimestampMap<?>> {

    public TimestampIndexImpl(TimeIndexStore<T, Double, TimestampSet, TimestampMap<?>> store, boolean main) {
        super(store, main);
    }

    @Override
    public double getMinTimestamp() {
        lock();
        try {
            Double2IntSortedMap sortedMap = (Double2IntSortedMap) timestampIndexStore.timeSortedMap;
            if (mainIndex) {
                if (!sortedMap.isEmpty()) {
                    return sortedMap.firstDoubleKey();
                }
            } else {
                if (!sortedMap.isEmpty()) {
                    ObjectBidirectionalIterator<Double2IntMap.Entry> bi = sortedMap.double2IntEntrySet().iterator();
                    while (bi.hasNext()) {
                        Double2IntMap.Entry entry = bi.next();
                        double timestamp = entry.getDoubleKey();
                        int index = entry.getIntValue();
                        if (index < timestamps.length) {
                            TimeIndexEntry timestampEntry = timestamps[index];
                            if (timestampEntry != null) {
                                return timestamp;
                            }
                        }
                    }
                }
            }
            return Double.NEGATIVE_INFINITY;
        } finally {
            unlock();
        }
    }

    @Override
    public double getMaxTimestamp() {
        lock();
        try {
            Double2IntSortedMap sortedMap = (Double2IntSortedMap) timestampIndexStore.timeSortedMap;
            if (mainIndex) {
                if (!sortedMap.isEmpty()) {
                    return sortedMap.lastDoubleKey();
                }
            } else {
                if (!sortedMap.isEmpty()) {
                    ObjectBidirectionalIterator<Double2IntMap.Entry> bi = sortedMap.double2IntEntrySet()
                            .iterator(sortedMap.double2IntEntrySet().last());
                    while (bi.hasPrevious()) {
                        Double2IntMap.Entry entry = bi.previous();
                        double timestamp = entry.getDoubleKey();
                        int index = entry.getIntValue();
                        if (index < timestamps.length) {
                            TimeIndexEntry timestampEntry = timestamps[index];
                            if (timestampEntry != null) {
                                return timestamp;
                            }
                        }
                    }
                }
            }
            return Double.POSITIVE_INFINITY;
        } finally {
            unlock();
        }
    }

    @Override
    public ElementIterable<T> get(double timestamp) {
        checkDouble(timestamp);

        lock();
        try {
            Integer index = timestampIndexStore.timeSortedMap.get(timestamp);
            if (index != null && index < timestamps.length) {
                TimeIndexEntry ts = timestamps[index];
                if (ts != null) {
                    return new ElementSetWrapperIterable(new ObjectOpenHashSet<>(ts.elementSet));
                }
            }
            return ElementIterable.EMPTY;
        } finally {
            unlock();
        }
    }

    @Override
    public ElementIterable<T> get(Interval interval) {
        checkDouble(interval.getLow());
        checkDouble(interval.getHigh());

        lock();
        try {
            ObjectSet<Element> elements = new ObjectOpenHashSet<>();
            Double2IntSortedMap sortedMap = (Double2IntSortedMap) timestampIndexStore.timeSortedMap;
            if (!sortedMap.isEmpty()) {
                for (Double2IntMap.Entry entry : sortedMap.tailMap(interval.getLow()).double2IntEntrySet()) {
                    double timestamp = entry.getDoubleKey();
                    int index = entry.getIntValue();
                    if (timestamp <= interval.getHigh()) {
                        if (index < timestamps.length) {
                            TimeIndexEntry ts = timestamps[index];
                            if (ts != null) {
                                elements.addAll(ts.elementSet);
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
            if (!elements.isEmpty()) {
                return new ElementSetWrapperIterable(elements);
            }
            return ElementIterable.EMPTY;
        } finally {
            unlock();
        }
    }
}
