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
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;
import it.unimi.dsi.fastutil.doubles.Double2IntRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2IntSortedMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.gephi.attribute.time.Interval;
import org.gephi.graph.utils.MapDeepEquals;

/**
 *
 * @author mbastian
 */
public class TimestampMap {

    //Const
    public static final int NULL_INDEX = -1;
    //Timestamp index managament
    protected final Double2IntMap timestampMap;
    protected final Double2IntSortedMap timestampSortedMap;
    protected final IntSortedSet garbageQueue;
    protected double[] indexMap;
    protected int length;

    public TimestampMap() {
        timestampMap = new Double2IntOpenHashMap();
        timestampMap.defaultReturnValue(NULL_INDEX);
        garbageQueue = new IntRBTreeSet();
        timestampSortedMap = new Double2IntRBTreeMap();
        indexMap = new double[0];
    }

    public int getTimestampIndex(double timestamp) {
        int index = timestampMap.get(timestamp);
        if (index == NULL_INDEX) {
            index = addTimestamp(timestamp);
        }
        return index;
    }

    public boolean hasTimestampIndex(double timestamp) {
        return timestampMap.containsKey(timestamp);
    }

    public int[] getTimestampIndices(Interval interval) {
        IntList res = new IntArrayList();
        double low = interval.getLow();
        double high = interval.getHigh();
        for (Double2IntMap.Entry entry : timestampSortedMap.subMap(low, high).double2IntEntrySet()) {
            double val = entry.getDoubleKey();
            if (!interval.isLowExcluded() || (interval.isLowExcluded() && val != low)) {
                res.add(entry.getIntValue());
            }
        }
        if (!interval.isHighExcluded()) {
            if (timestampMap.containsKey(high)) {
                res.add(timestampMap.get(high));
            }
        }
        return res.toIntArray();
    }

    public boolean contains(double timestamp) {
        checkDouble(timestamp);

        return timestampMap.containsKey(timestamp);
    }

    public double[] getTimestamps(int[] indices) {
        int indicesLength = indices.length;
        double[] res = new double[indicesLength];
        for (int i = 0; i < indicesLength; i++) {
            int index = indices[i];
            checkIndex(index);
            res[i] = indexMap[i];
        }
        return res;
    }

    public void clear() {
        timestampMap.clear();
        timestampSortedMap.clear();
        garbageQueue.clear();
        indexMap = new double[0];
        length = 0;
    }

    public int size() {
        return timestampMap.size();
    }

    protected int addTimestamp(final double timestamp) {
        checkDouble(timestamp);

        int id;
        if (!garbageQueue.isEmpty()) {
            id = garbageQueue.firstInt();
            garbageQueue.remove(id);
        } else {
            id = length++;
        }
        timestampMap.put(timestamp, id);
        timestampSortedMap.put(timestamp, id);
        ensureArraySize(id);
        indexMap[id] = timestamp;

        return id;
    }

    protected void removeTimestamp(final double timestamp) {
        checkDouble(timestamp);

        int id = timestampMap.get(timestamp);
        garbageQueue.add(id);
        timestampMap.remove(timestamp);
        timestampSortedMap.remove(timestamp);
        indexMap[id] = Double.NaN;
    }

    protected void ensureArraySize(int index) {
        if (index >= indexMap.length) {
            double[] newArray = new double[index + 1];
            System.arraycopy(indexMap, 0, newArray, 0, indexMap.length);
            indexMap = newArray;
        }
    }

    protected void setTimestampMap(TimestampMap map) {
        clear();
        timestampMap.putAll(map.timestampMap);
        timestampSortedMap.putAll(map.timestampSortedMap);
        garbageQueue.addAll(map.garbageQueue);
        indexMap = new double[map.indexMap.length];
        System.arraycopy(map.indexMap, 0, indexMap, 0, map.indexMap.length);
        length = map.length;
    }

    void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can' be NaN or infinity");
        }
    }

    void checkIndex(int index) {
        if (index < 0 || index >= length) {
            throw new IllegalArgumentException("The timestamp store index is out of bounds");
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        for (Double2IntMap.Entry entry : timestampSortedMap.double2IntEntrySet()) {
            hash = 29 * hash + entry.getKey().hashCode();
            hash = 29 * hash + entry.getValue().hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimestampMap other = (TimestampMap) obj;
        if (!MapDeepEquals.mapDeepEquals(timestampSortedMap, other.timestampSortedMap)) {
            return false;
        }
        return true;
    }
}
