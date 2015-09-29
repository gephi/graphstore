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
import static org.gephi.graph.api.Estimator.AVERAGE;
import static org.gephi.graph.api.Estimator.FIRST;
import static org.gephi.graph.api.Estimator.LAST;
import static org.gephi.graph.api.Estimator.MAX;
import static org.gephi.graph.api.Estimator.MIN;
import static org.gephi.graph.api.Estimator.SUM;
import org.gephi.graph.api.Interval;

/**
 * Sorted map where keys are timestamp and values float values.
 */
public final class TimestampFloatMap extends TimestampMap<Float> {

    private float[] values;

    /**
     * Default constructor.
     * <p>
     * The map is empty with zero capacity.
     */
    public TimestampFloatMap() {
        super();
        values = new float[0];
    }

    /**
     * Constructor with capacity.
     * <p>
     * Using this constructor can improve performances if the number of
     * timestamps is known in advance as it minimizes array resizes.
     *
     * @param capacity timestamp capacity
     */
    public TimestampFloatMap(int capacity) {
        super(capacity);
        values = new float[capacity];
    }

    @Override
    public boolean put(double timestamp, Float value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return putFloat(timestamp, value);
    }

    /**
     * Put the <code>value</code> in this map at the given
     * <code>timestamp</code> key.
     *
     * @param timestamp timestamp
     * @param value value
     * @return true if timestamp is a new key, false otherwise
     */
    public boolean putFloat(double timestamp, float value) {
        final int index = putInner(timestamp);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size - 1 < values.length) {
                if (insertIndex < size - 1) {
                    System.arraycopy(values, insertIndex, values, insertIndex + 1, size - insertIndex - 1);
                }
                values[insertIndex] = value;
            } else {
                float[] newArray = new float[values.length + 1];
                System.arraycopy(values, 0, newArray, 0, insertIndex);
                System.arraycopy(values, insertIndex, newArray, insertIndex + 1, values.length - insertIndex);
                newArray[insertIndex] = value;
                values = newArray;
            }
            return true;
        } else {
            values[index] = value;
        }
        return false;
    }

    @Override
    public boolean remove(double timestamp) {
        final int removeIndex = removeInner(timestamp);
        if (removeIndex >= 0) {
            if (removeIndex != size) {
                System.arraycopy(values, removeIndex + 1, values, removeIndex, size - removeIndex);
            }
            return true;
        }
        return false;
    }

    @Override
    public Float get(double timestamp, Float defaultValue) {
        final int index = getIndex(timestamp);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    /**
     * Get the value for the given timestamp.
     *
     * @param timestamp timestamp
     * @return found value or the default value if not found
     * @throws IllegalArgumentException if the element doesn't exist
     */
    public float getFloat(double timestamp) {
        final int index = getIndex(timestamp);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    /**
     * Get the value for the given timestamp.
     * <p>
     * Return <code>defaultValue</code> if the value is not found.
     *
     * @param timestamp timestamp
     * @param defaultValue default value
     * @return found value or the default value if not found
     */
    public float getFloat(double timestamp, float defaultValue) {
        final int index = getIndex(timestamp);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    @Override
    public Object get(Interval interval, Estimator estimator) {
        switch (estimator) {
            case AVERAGE:
                BigDecimal ra = getAverageBigDecimal(interval);
                if (ra != null) {
                    return ra.floatValue();
                }
                return null;
            case SUM:
                BigDecimal rs = getSumBigDecimal(interval);
                if (rs != null) {
                    return rs.floatValue();
                }
                return null;
            case MIN:
                Double min = (Double) getMin(interval);
                if (min != null) {
                    return min.floatValue();
                }
                return null;
            case MAX:
                Double max = (Double) getMax(interval);
                if (max != null) {
                    return max.floatValue();
                }
                return null;
            case FIRST:
                return getFirst(interval);
            case LAST:
                return getLast(interval);
            default:
                throw new UnsupportedOperationException("Unknown estimator.");
        }
    }

    @Override
    public Float[] toArray() {
        final Float[] res = new Float[size];
        for (int i = 0; i < size; i++) {
            res[i] = values[i];
        }
        return res;
    }

    @Override
    public Class<Float> getTypeClass() {
        return Float.class;
    }

    /**
     * Returns an array of all values in this map.
     * <p>
     * This method may return a reference to the underlying array so clients
     * should make a copy if the array is written to.
     *
     * @return array of all values
     */
    public float[] toFloatArray() {
        if (size < values.length - 1) {
            final float[] res = new float[size];
            System.arraycopy(values, 0, res, 0, size);
            return res;
        } else {
            return values;
        }
    }

    @Override
    public void clear() {
        super.clear();
        values = new float[0];
    }

    @Override
    public boolean isSupported(Estimator estimator) {
        return estimator.is(Estimator.MIN, Estimator.MAX, Estimator.FIRST, Estimator.LAST, Estimator.AVERAGE, Estimator.SUM);
    }

    @Override
    protected Object getValue(int index) {
        return values[index];
    }
}
