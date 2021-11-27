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
     * Using this constructor can improve performances if the number of timestamps
     * is known in advance as it minimizes array resizes.
     *
     * @param capacity timestamp capacity
     */
    public TimestampFloatMap(int capacity) {
        super(capacity);
        values = new float[capacity];
    }

    /**
     * Constructor with an initial timestamp map.
     * <p>
     * The <code>keys</code> array must be sorted and contain no duplicates.
     *
     * @param keys initial keys content
     * @param vals initial values content
     */
    public TimestampFloatMap(double[] keys, float[] vals) {
        super(keys);
        values = new float[vals.length];
        System.arraycopy(vals, 0, values, 0, vals.length);
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
    protected Object getAverage(Interval interval) {
        BigDecimal average = getAverageBigDecimal(interval);
        return average != null ? average.floatValue() : null;
    }

    @Override
    protected Object getMax(Interval interval) {
        Double max = getMaxDouble(interval);
        return max != null ? max.floatValue() : null;
    }

    @Override
    protected Object getMin(Interval interval) {
        Double min = getMinDouble(interval);
        return min != null ? min.floatValue() : null;
    }

    @Override
    public Class<Float> getTypeClass() {
        return Float.class;
    }

    /**
     * Returns an array of all values in this map.
     * <p>
     * This method may return a reference to the underlying array so clients should
     * make a copy if the array is written to.
     *
     * @return array of all values
     */
    public float[] toFloatArray() {
        return (float[]) toPrimitiveArray();
    }

    @Override
    public boolean isSupported(Estimator estimator) {
        return estimator.is(Estimator.MIN, Estimator.MAX, Estimator.FIRST, Estimator.LAST, Estimator.AVERAGE);
    }

    @Override
    protected Float getValue(int index) {
        return values[index];
    }

    @Override
    protected Object getValuesArray() {
        return values;
    }

    @Override
    protected void setValuesArray(Object array) {
        values = (float[]) array;
    }
}
