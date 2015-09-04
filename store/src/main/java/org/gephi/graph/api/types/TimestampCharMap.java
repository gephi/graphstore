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

/**
 * Sorted map where keys are timestamp indices and values character values.
 */
public final class TimestampCharMap extends TimestampValueMap<Character> {

    private char[] values;

    /**
     * Default constructor.
     * <p>
     * The map is empty with zero capacity.
     */
    public TimestampCharMap() {
        super();
        values = new char[0];
    }

    /**
     * Constructor with capacity.
     * <p>
     * Using this constructor can improve performances if the number of
     * timestamps is known in advance as it minimizes array resizes.
     *
     * @param capacity timestamp capacity
     */
    public TimestampCharMap(int capacity) {
        super(capacity);
        values = new char[capacity];
    }

    @Override
    public void put(int timestampIndex, Character value) {
        if (value == null) {
            throw new NullPointerException();
        }
        putCharacter(timestampIndex, value);
    }

    /**
     * Put the <code>value</code> in this map at the given
     * <code>timestampIndex</code> key.
     *
     * @param timestampIndex timestamp index
     * @param value value
     */
    public void putCharacter(int timestampIndex, char value) {
        final int index = putInner(timestampIndex);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size - 1 < values.length) {
                if (insertIndex < size - 1) {
                    System.arraycopy(values, insertIndex, values, insertIndex + 1, size - insertIndex - 1);
                }
                values[insertIndex] = value;
            } else {
                char[] newArray = new char[values.length + 1];
                System.arraycopy(values, 0, newArray, 0, insertIndex);
                System.arraycopy(values, insertIndex, newArray, insertIndex + 1, values.length - insertIndex);
                newArray[insertIndex] = value;
                values = newArray;
            }
        } else {
            values[index] = value;
        }
    }

    @Override
    public void remove(int timestampIndex) {
        final int removeIndex = removeInner(timestampIndex);
        if (removeIndex >= 0 && removeIndex != size) {
            System.arraycopy(values, removeIndex + 1, values, removeIndex, size - removeIndex);
        }
    }

    @Override
    public Character get(int timestampIndex, Character defaultValue) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    /**
     * Get the value for the given timestamp index.

     * @param timestampIndex timestamp index
     * @return found value or the default value if not found
     * @throws IllegalArgumentException if the element doesn't exist
     */
    public char getCharacter(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    /**
     * Get the value for the given timestamp index.
     * <p>
     * Return <code>defaultValue</code> if the value is not found.
     *
     * @param timestampIndex timestamp index
     * @param defaultValue default value
     * @return found value or the default value if not found
     */
    public char getCharacter(int timestampIndex, char defaultValue) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    @Override
    public Object get(double[] timestamps, int[] timestampIndices, Estimator estimator) {
        switch (estimator) {
            case MIN:
                return getMin(timestampIndices);
            case MAX:
                return getMax(timestampIndices);
            case FIRST:
                return getFirst(timestampIndices);
            case LAST:
                return getLast(timestampIndices);
            default:
                throw new UnsupportedOperationException("Unknown estimator.");
        }
    }

    @Override
    protected Object getMin(int[] timestampIndices) {
        char min = Character.MAX_VALUE;
        boolean found = false;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                char val = values[index];
                min = (char) Math.min(min, val);
                found = true;
            }
        }
        if (!found) {
            return null;
        }
        return min;
    }

    @Override
    protected Object getMax(int[] timestampIndices) {
        char max = Character.MIN_VALUE;
        boolean found = false;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                char val = values[index];
                max = (char) Math.max(max, val);
                found = true;
            }
        }
        if (!found) {
            return null;
        }
        return max;
    }

    @Override
    public Character[] toArray() {
        final Character[] res = new Character[size];
        for (int i = 0; i < size; i++) {
            res[i] = values[i];
        }
        return res;
    }

    @Override
    public Class<Character> getTypeClass() {
        return Character.class;
    }

    /**
     * Returns an array of all values in this map.
     * <p>
     * This method may return a reference to the underlying array so clients
     * should make a copy if the array is written to.
     *
     * @return array of all values
     */
    public char[] toCharacterArray() {
        if (size < values.length - 1) {
            final char[] res = new char[size];
            System.arraycopy(values, 0, res, 0, size);
            return res;
        } else {
            return values;
        }
    }

    @Override
    public void clear() {
        super.clear();
        values = new char[0];
    }

    @Override
    public boolean isSupported(Estimator estimator) {
        return estimator.is(Estimator.MIN, Estimator.MAX, Estimator.FIRST, Estimator.LAST);
    }

    @Override
    protected Object getValue(int index) {
        return values[index];
    }
}
