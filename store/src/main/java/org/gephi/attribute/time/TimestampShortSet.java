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
package org.gephi.attribute.time;

import java.math.BigDecimal;

/**
 *
 * @author mbastian
 */
public final class TimestampShortSet extends TimestampValueSet<Short> {

    private short[] values;

    public TimestampShortSet() {
        super();
        values = new short[0];
    }

    public TimestampShortSet(int capacity) {
        super(capacity);
        values = new short[capacity];
    }

    @Override
    public void put(int timestampIndex, Short value) {
        if (value == null) {
            throw new NullPointerException();
        }
        putShort(timestampIndex, value);
    }

    public void putShort(int timestampIndex, short value) {
        final int index = putInner(timestampIndex);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size < values.length) {
                if (insertIndex < size - 1) {
                    System.arraycopy(values, insertIndex, values, insertIndex + 1, size - insertIndex - 1);
                }
                values[insertIndex] = value;
            } else {
                short[] newArray = new short[values.length + 1];
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
        if (removeIndex > 0) {
            if (removeIndex != size) {
                System.arraycopy(values, removeIndex + 1, values, removeIndex, size - removeIndex);
            }
        }
    }

    @Override
    public Short get(int timestampIndex, Short defaultValue) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    public short getShort(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }
    
    public short getShort(int timestampIndex, short defaultValue) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    @Override
    public Object get(double[] timestamps, int[] timestampIndices, Estimator estimator) {
        switch (estimator) {
            case AVERAGE:
                BigDecimal ra = getAverageBigDecimal(timestampIndices);
                if (ra != null) {
                    return ra.doubleValue();
                }
                return null;
            case SUM:
                BigDecimal rs = getSumBigDecimal(timestampIndices);
                if (rs != null) {
                    return rs.intValue();
                }
                return null;
            case MIN:
                Double min = (Double) getMin(timestampIndices);
                if (min != null) {
                    return min.shortValue();
                }
                return null;
            case MAX:
                Double max = (Double) getMax(timestampIndices);
                if (max != null) {
                    return max.shortValue();
                }
                return null;
            case FIRST:
                return getFirst(timestampIndices);
            case LAST:
                return getLast(timestampIndices);
            default:
                throw new IllegalArgumentException("Unknown estimator.");
        }
    }

    @Override
    public Short[] toArray() {
        final Short[] res = new Short[size];
        for (int i = 0; i < size; i++) {
            res[i] = values[i];
        }
        return res;
    }

    @Override
    public Class<Short> getTypeClass() {
        return Short.class;
    }

    public short[] toShortArray() {
        if (size < values.length - 1) {
            final short[] res = new short[size];
            System.arraycopy(values, 0, res, 0, size);
            return res;
        } else {
            return values;
        }
    }

    @Override
    public void clear() {
        super.clear();
        values = new short[0];
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
