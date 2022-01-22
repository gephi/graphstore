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
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Map;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;

public class IntervalIndexImpl<T extends Element> extends TimeIndexImpl<T, Interval, IntervalSet, IntervalMap<?>> {

    public IntervalIndexImpl(TimeIndexStore<T, Interval, IntervalSet, IntervalMap<?>> store, boolean main) {
        super(store, main);
    }

    @Override
    public double getMinTimestamp() {
        lock();
        try {
            Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
            if (mainIndex) {
                if (!sortedMap.isEmpty()) {
                    return sortedMap.getLow();
                }
            } else {
                if (!sortedMap.isEmpty()) {
                    for (Map.Entry<Interval, Integer> entry : sortedMap.entrySet()) {
                        int index = entry.getValue();
                        if (index < timestamps.length) {
                            TimeIndexEntry intervalEntry = timestamps[index];
                            if (intervalEntry != null) {
                                return entry.getKey().getLow();
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
            if (mainIndex) {
                Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
                if (!sortedMap.isEmpty()) {
                    return sortedMap.getHigh();
                }
            } else {
                // TODO Better algorithm to find max
                Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
                if (!sortedMap.isEmpty()) {
                    double max = Double.NEGATIVE_INFINITY;
                    boolean found = false;
                    for (Map.Entry<Interval, Integer> entry : sortedMap.entrySet()) {
                        int index = entry.getValue();
                        if (index < timestamps.length) {
                            TimeIndexEntry intervalEntry = timestamps[index];
                            if (intervalEntry != null) {
                                found = true;
                                max = Math.max(max, entry.getKey().getHigh());
                            }
                        }
                    }
                    if (found) {
                        return max;
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
            ObjectSet<Element> elements = new ObjectOpenHashSet<>();
            Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
            if (!sortedMap.isEmpty()) {
                for (Integer index : sortedMap.values(timestamp)) {
                    if (index < timestamps.length) {
                        TimeIndexEntry ts = timestamps[index];
                        if (ts != null) {
                            elements.addAll(ts.elementSet);
                        }
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

    @Override
    public ElementIterable<T> get(Interval interval) {

        lock();
        try {
            ObjectSet<Element> elements = new ObjectOpenHashSet<>();
            Interval2IntTreeMap sortedMap = (Interval2IntTreeMap) timestampIndexStore.timeSortedMap;
            if (!sortedMap.isEmpty()) {
                for (Integer index : sortedMap.values(interval)) {
                    if (index < timestamps.length) {
                        TimeIndexEntry ts = timestamps[index];
                        if (ts != null) {
                            elements.addAll(ts.elementSet);
                        }
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
