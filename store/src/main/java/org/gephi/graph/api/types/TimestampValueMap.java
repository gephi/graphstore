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
package org.gephi.graph.api.types;

import org.gephi.graph.api.Estimator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Abstract class that implement a sorted map between timestamp indices and
 * attribute values.
 * <p>
 * Implementations which extend this class customize the map for a unique type,
 * which is represented by the <code>T</code> parameter.
 *
 * @param <T> Value type
 */
public abstract class TimestampValueMap<T> {

    protected int[] array;
    protected int size = 0;

    /**
     * Default constructor.
     * <p>
     * The map is empty with zero capacity.
     */
    public TimestampValueMap() {
        array = new int[0];
    }

    /**
     * Constructor with capacity.
     * <p>
     * Using this constructor can improve performances if the number of
     * timestamps is known in advance as it minimizes array resizes.
     *
     * @param capacity timestamp capacity
     */
    public TimestampValueMap(int capacity) {
        array = new int[capacity];
        Arrays.fill(array, Integer.MAX_VALUE);
    }

    /**
     * Put the value at the given timestamp index.
     *
     * @param timestampIndex timestamp index
     * @param value value
     */
    public abstract void put(int timestampIndex, T value);

    /**
     * Remove the value at the given timestamp index.
     *
     * @param timestampIndex timestamp index
     */
    public abstract void remove(int timestampIndex);

    /**
     * Get the value for the given timestamp index.
     * <p>
     * Return <code>defaultValue</code> if the value is not found.
     *
     * @param timestampIndex timestamp index
     * @param defaultValue default value
     * @return found value or the default value if not found
     */
    public abstract T get(int timestampIndex, T defaultValue);

    /**
     * Get the estimated value for the given array of timestamps.
     * <p>
     * The estimator is used to determine the way multiple timestamp values are
     * merged together (e.g average).
     *
     * @param timestamps the timestamp values
     * @param timestampIndices the timestamp indices for the given
     * <code>timestamps</code>
     * @param estimator the estimator used
     * @return the estimated value
     */
    public abstract Object get(double[] timestamps, int[] timestampIndices, Estimator estimator);

    /**
     * Returns all the values as an array.
     *
     * @return values array
     */
    public abstract T[] toArray();

    /**
     * Returns the value type class.
     *
     * @return type class
     */
    public abstract Class<T> getTypeClass();

    /**
     * Returns whether <code>estimator</code> is supported.
     *
     * @param estimator estimator
     * @return true if this map supports <code>estimator</code>
     */
    public abstract boolean isSupported(Estimator estimator);

    protected abstract Object getValue(int index);

    protected int putInner(int timestampIndex) {
        int index = Arrays.binarySearch(array, 0, size, timestampIndex);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size < array.length) {
                if (insertIndex < size) {
                    System.arraycopy(array, insertIndex, array, insertIndex + 1, size - insertIndex);
                }
                array[insertIndex] = timestampIndex;
            } else {
                int[] newArray = new int[array.length + 1];
                System.arraycopy(array, 0, newArray, 0, insertIndex);
                System.arraycopy(array, insertIndex, newArray, insertIndex + 1, array.length - insertIndex);
                newArray[insertIndex] = timestampIndex;
                array = newArray;
            }

            size++;
        }
        return index;
    }

    protected int removeInner(int timestampIndex) {
        int index = Arrays.binarySearch(array, 0, size, timestampIndex);
        if (index >= 0) {
            int removeIndex = index;

            if (removeIndex == size - 1) {
                size--;
            } else {
                System.arraycopy(array, removeIndex + 1, array, removeIndex, size - removeIndex - 1);
                size--;
            }

            return removeIndex;
        }
        return -1;
    }

    /**
     * Returns the size.
     *
     * @return the number of elements in this map
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this map is empty.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    protected int getIndex(int timestampIndex) {
        return Arrays.binarySearch(array, timestampIndex);
    }

    /**
     * Returns true if this map contains <code>timestampIndex</code>.
     *
     * @param timestampIndex timestamp index
     * @return true if contains, false otherwise
     */
    public boolean contains(int timestampIndex) {
        int index = Arrays.binarySearch(array, timestampIndex);
        return index >= 0 && index < size;
    }

    /**
     * Returns an array of all timestamps in this map.
     * <p>
     * This method may return a reference to the underlying array so clients
     * should make a copy if the array is written to.
     *
     * @return array of all timestamps
     */
    public int[] getTimestamps() {
        if (size < array.length) {
            int[] res = new int[size];
            System.arraycopy(array, 0, res, 0, size);
            return res;
        } else {
            return array;
        }
    }

    /**
     * Empties this map.
     */
    public void clear() {
        size = 0;
        array = new int[0];
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.size;
        for (int i = 0; i < size; i++) {
            int index = this.array[i];
            hash = 29 * hash + index;
            Object obj = this.getValue(i);
            hash = 29 * hash + obj.hashCode();
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
        final TimestampValueMap<?> other = (TimestampValueMap<?>) obj;
        if (this.size != other.size) {
            return false;
        }
        for (int i = 0; i < this.array.length && i < other.array.length; i++) {
            int i1 = this.array[i];
            int i2 = other.array[i];
            if (i1 != i2) {
                return false;
            }
            Object o1 = this.getValue(i);
            Object o2 = other.getValue(i);
            if ((o1 == null && o2 != null) || (o1 != null && o2 == null) || (o1 != null && o2 != null && !o1.equals(o2))) {
                return false;
            }
        }
        return true;
    }

    //Estimators
    protected Object getFirst(final int[] timestampIndices) {
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                Object val = getValue(index);
                return val;
            }
        }
        return null;
    }

    protected Object getLast(final int[] timestampIndices) {
        for (int i = timestampIndices.length - 1; i > 0; i--) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                Object val = getValue(index);
                return val;
            }
        }
        return null;
    }

    protected Object getMin(final int[] timestampIndices) {
        double min = Double.POSITIVE_INFINITY;
        boolean found = false;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                double val = ((Number) getValue(index)).doubleValue();
                min = (double) Math.min(min, val);
                found = true;
            }
        }
        if (!found) {
            return null;
        }
        return min;
    }

    protected Object getMax(final int[] timestampIndices) {
        double max = Double.NEGATIVE_INFINITY;
        boolean found = false;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                double val = ((Number) getValue(index)).doubleValue();
                max = (double) Math.max(max, val);
                found = true;
            }
        }
        if (!found) {
            return null;
        }
        return max;
    }

    protected BigDecimal getAverageBigDecimal(final int[] timestampIndices) {
        BigDecimal total = new BigDecimal(0);
        int count = 0;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                double val = ((Number) getValue(index)).doubleValue();
                total = total.add(BigDecimal.valueOf(val));
                count++;
            }

        }
        if (count == 0) {
            return null;
        }
        return total.divide(BigDecimal.valueOf(count), 10, RoundingMode.HALF_EVEN);
    }

    protected BigDecimal getSumBigDecimal(final int[] timestampIndices) {
        BigDecimal total = new BigDecimal(0);
        int count = 0;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                double val = ((Number) getValue(index)).doubleValue();
                total = total.add(BigDecimal.valueOf(val));
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return total;
    }
}
