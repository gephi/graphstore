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

/**
 *
 * @author mbastian
 */
public final class TimestampByteSet extends TimestampValueSet<Byte> {

    private byte[] values;

    public TimestampByteSet() {
        super();
        values = new byte[0];
    }

    public TimestampByteSet(int capacity) {
        super(capacity);
        values = new byte[capacity];
    }

    @Override
    public void put(int timestampIndex, Byte value) {
        if (value == null) {
            throw new NullPointerException();
        }
        putByte(timestampIndex, value);
    }

    public void putByte(int timestampIndex, byte value) {
        final int index = putInner(timestampIndex);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size < values.length) {
                if (insertIndex < size - 1) {
                    System.arraycopy(values, insertIndex, values, insertIndex + 1, size - insertIndex - 1);
                }
                values[insertIndex] = value;
            } else {
                byte[] newArray = new byte[values.length + 1];
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
    public Byte get(int timestampIndex, Byte defaultValue) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    public byte getByte(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }
    
    public byte getByte(int timestampIndex, byte defaultValue) {
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
                return getAverage(timestampIndices);
            case SUM:
                return getSum(timestampIndices);
            case MIN:
                Object rmin = getMin(timestampIndices);
                if (rmin != null) {
                    return ((Double) rmin).byteValue();
                }
                return null;
            case MAX:
                Object rmax = getMax(timestampIndices);
                if (rmax != null) {
                    return ((Double) rmax).byteValue();
                }
                return null;
            case FIRST:
                return getFirst(timestampIndices);
            case LAST:
                return getLast(timestampIndices);
            default:
                throw new UnsupportedOperationException("Unknown estimator.");
        }
    }

    private Object getAverage(final int[] timestampIndices) {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                byte val = values[index];
                sum += val;
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        sum /= count;
        return sum;
    }

    private Object getSum(final int[] timestampIndices) {
        int sum = 0;
        int count = 0;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                byte val = values[index];
                sum += val;
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return sum;
    }

    @Override
    public Byte[] toArray() {
        final Byte[] res = new Byte[size];
        for (int i = 0; i < size; i++) {
            res[i] = values[i];
        }
        return res;
    }

    @Override
    public Class<Byte> getTypeClass() {
        return Byte.class;
    }

    public byte[] toByteArray() {
        if (size < values.length - 1) {
            final byte[] res = new byte[size];
            System.arraycopy(values, 0, res, 0, size);
            return res;
        } else {
            return values;
        }
    }

    @Override
    public void clear() {
        super.clear();
        values = new byte[0];
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
