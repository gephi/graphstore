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
 * Sorted map where keys are timestamp and values double values.
 */
public final class TimestampDoubleMap extends TimestampMap<Double> {

    private double[] values;

    /**
     * Default constructor.
     * <p>
     * The map is empty with zero capacity.
     */
    public TimestampDoubleMap() {
        super();
        values = new double[0];
    }

    /**
     * Constructor with capacity.
     * <p>
     * Using this constructor can improve performances if the number of
     * timestamps is known in advance as it minimizes array resizes.
     *
     * @param capacity timestamp capacity
     */
    public TimestampDoubleMap(int capacity) {
        super(capacity);
        values = new double[capacity];
    }

    @Override
    public boolean put(double timestamp, Double value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return putDouble(timestamp, value);
    }

    /**
     * Put the <code>value</code> in this map at the given
     * <code>timestamp</code> key.
     *
     * @param timestamp timestamp index
     * @param value value
     * @return true if timestamp is a new key, false otherwise
     */
    public boolean putDouble(double timestamp, double value) {
        final int index = putInner(timestamp);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size - 1 < values.length) {
                if (insertIndex < size - 1) {
                    System.arraycopy(values, insertIndex, values, insertIndex + 1, size - insertIndex - 1);
                }
                values[insertIndex] = value;
            } else {
                double[] newArray = new double[values.length + 1];
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
    public Double get(double timestamp, Double defaultValue) {
        final int index = getIndex(timestamp);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    /**
     * Get the value for the given timestamp index.
     *
     * @param timestamp timestamp index
     * @return found value or the default value if not found
     * @throws IllegalArgumentException if the element doesn't exist
     */
    public double getDouble(double timestamp) {
        final int index = getIndex(timestamp);
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
     * @param timestamp timestamp index
     * @param defaultValue default value
     * @return found value or the default value if not found
     */
    public double getDouble(double timestamp, double defaultValue) {
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
                    return ra.doubleValue();
                }
            case SUM:
                BigDecimal rs = getSumBigDecimal(interval);
                if (rs != null) {
                    return rs.doubleValue();
                }
            case MIN:
                return getMin(interval);
            case MAX:
                return getMax(interval);
            case FIRST:
                return getFirst(interval);
            case LAST:
                return getLast(interval);
            default:
                throw new UnsupportedOperationException("Unknown estimator.");
        }
    }

    @Override
    public Double[] toArray() {
        final Double[] res = new Double[size];
        for (int i = 0; i < size; i++) {
            res[i] = values[i];
        }
        return res;
    }

    @Override
    public Class<Double> getTypeClass() {
        return Double.class;
    }

    /**
     * Returns an array of all values in this map.
     * <p>
     * This method may return a reference to the underlying array so clients
     * should make a copy if the array is written to.
     *
     * @return array of all values
     */
    public double[] toDoubleArray() {
        if (size < values.length - 1) {
            final double[] res = new double[size];
            System.arraycopy(values, 0, res, 0, size);
            return res;
        } else {
            return values;
        }
    }

    @Override
    public void clear() {
        super.clear();
        values = new double[0];
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
