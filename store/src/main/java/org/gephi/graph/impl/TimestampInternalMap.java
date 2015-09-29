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
import it.unimi.dsi.fastutil.doubles.Double2IntRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2IntSortedMap;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import static org.gephi.graph.impl.TimestampInternalMap.NULL_INDEX;
import org.gephi.graph.impl.utils.MapDeepEquals;

public class TimestampInternalMap {

    //Const
    public static final int NULL_INDEX = -1;
    //Timestamp index managament
    protected final Double2IntSortedMap timestampSortedMap;
    protected final IntSortedSet garbageQueue;
    protected int[] countMap;
    protected int length;

    public TimestampInternalMap() {
        garbageQueue = new IntRBTreeSet();
        timestampSortedMap = new Double2IntRBTreeMap();
        timestampSortedMap.defaultReturnValue(NULL_INDEX);
        countMap = new int[0];
    }

    public int getTimestampIndex(double timestamp) {
        return timestampSortedMap.get(timestamp);
    }

    public boolean hasTimestampIndex(double timestamp) {
        return timestampSortedMap.containsKey(timestamp);
    }

    public boolean contains(double timestamp) {
        checkDouble(timestamp);

        return timestampSortedMap.containsKey(timestamp);
    }

    public void clear() {
        timestampSortedMap.clear();
        garbageQueue.clear();
        countMap = new int[0];
        length = 0;
    }

    public int size() {
        return timestampSortedMap.size();
    }

    protected int addTimestamp(double timestamp) {
        checkDouble(timestamp);

        int id = timestampSortedMap.get(timestamp);
        if (id == NULL_INDEX) {
            if (!garbageQueue.isEmpty()) {
                id = garbageQueue.firstInt();
                garbageQueue.remove(id);
            } else {
                id = length++;
            }
            timestampSortedMap.put(timestamp, id);
            ensureArraySize(id);
            countMap[id] = 1;
        } else {
            countMap[id]++;
        }

        return id;
    }

    protected void removeTimestamp(double timestamp) {
        checkDouble(timestamp);

        int id = timestampSortedMap.get(timestamp);
        if (id != NULL_INDEX) {
            if (--countMap[id] == 0) {
                garbageQueue.add(id);
                timestampSortedMap.remove(timestamp);
            }
        }
    }

    protected void ensureArraySize(int index) {
        if (index >= countMap.length) {
            int newSize = Math.min(Math.max(index + 1, (int) (index * GraphStoreConfiguration.TIMESTAMP_INTERNAL_MAP_GROWING_FACTOR)), Integer.MAX_VALUE);
            int[] newArray = new int[newSize];
            System.arraycopy(countMap, 0, newArray, 0, countMap.length);
            countMap = newArray;
        }
    }

    protected void setTimestampMap(TimestampInternalMap map) {
        clear();
        timestampSortedMap.putAll(map.timestampSortedMap);
        garbageQueue.addAll(map.garbageQueue);
        countMap = new int[map.countMap.length];
        System.arraycopy(map.countMap, 0, countMap, 0, map.countMap.length);
        length = map.length;
    }

    void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can't be NaN or infinity");
        }
    }

    public int deepHashCode() {
        int hash = 3;
        for (Double2IntMap.Entry entry : timestampSortedMap.double2IntEntrySet()) {
            hash = 29 * hash + entry.getKey().hashCode();
            hash = 29 * hash + entry.getValue().hashCode();
            hash = 29 * hash + countMap[entry.getValue()];
        }
        return hash;
    }

    public boolean deepEquals(TimestampInternalMap obj) {
        if (obj == null) {
            return false;
        }
        if (!MapDeepEquals.mapDeepEquals(timestampSortedMap, obj.timestampSortedMap)) {
            return false;
        }
        int[] otherCountMap = obj.countMap;
        for (Integer k : timestampSortedMap.values()) {
            if (otherCountMap[k] != countMap[k]) {
                return false;
            }
        }
        return true;
    }
}
