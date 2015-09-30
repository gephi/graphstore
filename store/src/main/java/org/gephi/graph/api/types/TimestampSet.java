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

import java.util.Arrays;

/**
 * Sorted set for timestamps.
 */
public final class TimestampSet {

    private double[] array;
    private int size = 0;

    /**
     * Default constructor.
     * <p>
     * The set is empty with zero capacity.
     */
    public TimestampSet() {
        array = new double[0];
    }

    /**
     * Constructor with capacity.
     * <p>
     * Using this constructor can improve performances if the number of
     * timestamps is known in advance as it minimizes array resizes.
     *
     * @param capacity timestamp capacity
     */
    public TimestampSet(int capacity) {
        array = new double[capacity];
        Arrays.fill(array, Double.MAX_VALUE);
    }

    /**
     * Constructor with an initial timestamp set.
     * <p>
     * The given array must be sorted and contain no duplicates.
     *
     * @param arr initial set content
     */
    public TimestampSet(double[] arr) {
        array = new double[arr.length];
        System.arraycopy(arr, 0, array, 0, arr.length);
        size = arr.length;
    }

    /**
     * Adds timestamp to this set.
     *
     * @param timestamp timestamp
     * @return true if added, false otherwise
     */
    public boolean add(double timestamp) {
        return addInner(timestamp) >= 0;
    }

    /**
     * Removes timestamp from this set.
     *
     * @param timestamp timestamp
     * @return true if removed, false otherwise
     */
    public boolean remove(double timestamp) {
        return removeInner(timestamp) >= 0;
    }

    /**
     * Returns the size of this set.
     *
     * @return the number of elements in this set
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this set is empty.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns true if this set contains <code>timestamp</code>.
     *
     * @param timestamp timestamp
     * @return true if contains, false otherwise
     */
    public boolean contains(double timestamp) {
        int index = Arrays.binarySearch(array, timestamp);
        return index >= 0 && index < size;
    }

    /**
     * Returns an array of all timestamps in this set.
     * <p>
     * This method may return a reference to the underlying array so clients
     * should make a copy if the array is written to.
     *
     * @return array of all timestamps
     */
    public double[] getTimestamps() {
        if (size < array.length) {
            double[] res = new double[size];
            System.arraycopy(array, 0, res, 0, size);
            return res;
        } else {
            return array;
        }
    }

    /**
     * Empties this set.
     */
    public void clear() {
        size = 0;
        array = new double[0];
    }

    private int addInner(double timestamp) {
        int index = Arrays.binarySearch(array, 0, size, timestamp);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size < array.length - 1) {
                if (insertIndex < size) {
                    System.arraycopy(array, insertIndex, array, insertIndex + 1, size - insertIndex);
                }
                array[insertIndex] = timestamp;
            } else {
                double[] newArray = new double[array.length + 1];
                System.arraycopy(array, 0, newArray, 0, insertIndex);
                System.arraycopy(array, insertIndex, newArray, insertIndex + 1, array.length - insertIndex);
                newArray[insertIndex] = timestamp;
                array = newArray;
            }

            size++;
            return insertIndex;
        }
        return -1;
    }

    private int removeInner(double timestamp) {
        int index = Arrays.binarySearch(array, 0, size, timestamp);
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.size;
        for (int i = 0; i < size; i++) {
            double t = this.array[i];
            hash = 37 * hash + (int) (Double.doubleToLongBits(t) ^ (Double.doubleToLongBits(t) >>> 32));
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
        final TimestampSet other = (TimestampSet) obj;
        if (this.size != other.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            double i1 = this.array[i];
            double i2 = other.array[i];
            if (i1 != i2) {
                return false;
            }
        }
        return true;
    }

}
