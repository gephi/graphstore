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
public final class TimestampBooleanSet extends TimestampValueSet<Boolean> {

    private boolean[] values;

    public TimestampBooleanSet() {
        super();
        values = new boolean[0];
    }

    public TimestampBooleanSet(int capacity) {
        super(capacity);
        values = new boolean[capacity];
    }

    @Override
    public void put(int timestampIndex, Boolean value) {
        if (value == null) {
            throw new NullPointerException();
        }
        putBoolean(timestampIndex, value);
    }

    public void putBoolean(int timestampIndex, boolean value) {
        final int index = putInner(timestampIndex);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size < values.length) {
                if (insertIndex < size - 1) {
                    System.arraycopy(values, insertIndex, values, insertIndex + 1, size - insertIndex - 1);
                }
                values[insertIndex] = value;
            } else {
                boolean[] newArray = new boolean[values.length + 1];
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
    public Boolean get(int timestampIndex, Boolean defaultValue) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }
    
    public boolean getBoolean(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }
    
    public boolean getBoolean(int timestampIndex, boolean defaultValue) {
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
                throw new UnsupportedOperationException("Not supported estimator.");
        }
    }

    @Override
    protected Object getMin(final int[] timestampIndices) {
        boolean t = false;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                boolean val = values[index];
                if (!val) {
                    return Boolean.FALSE;
                } else {
                    t = true;
                }
            }
        }
        if (t) {
            return Boolean.TRUE;
        }
        return null;
    }

    @Override
    protected Object getMax(final int[] timestampIndices) {
        boolean f = false;
        for (int i = 0; i < timestampIndices.length; i++) {
            int timestampIndex = timestampIndices[i];
            int index = getIndex(timestampIndex);
            if (index >= 0) {
                boolean val = values[index];
                if (val) {
                    return Boolean.TRUE;
                } else {
                    f = true;
                }
            }
        }
        if (f) {
            return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public Boolean[] toArray() {
        final Boolean[] res = new Boolean[size];
        for (int i = 0; i < size; i++) {
            res[i] = values[i];
        }
        return res;
    }

    @Override
    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }

    public boolean[] toBooleanArray() {
        if (size < values.length - 1) {
            final boolean[] res = new boolean[size];
            System.arraycopy(values, 0, res, 0, size);
            return res;
        } else {
            return values;
        }
    }

    @Override
    public void clear() {
        super.clear();
        values = new boolean[0];
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
