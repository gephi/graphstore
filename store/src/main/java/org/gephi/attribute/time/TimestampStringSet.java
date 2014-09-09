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
public final class TimestampStringSet extends TimestampValueSet<String> {

    private String[] values;

    public TimestampStringSet() {
        super();
        values = new String[0];
    }

    public TimestampStringSet(int capacity) {
        super(capacity);
        values = new String[capacity];
    }

    @Override
    public void put(int timestampIndex, String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        final int index = putInner(timestampIndex);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size < values.length) {
                if (insertIndex < size - 1) {
                    System.arraycopy(values, insertIndex, values, insertIndex + 1, size - insertIndex - 1);
                }
                values[insertIndex] = value;
            } else {
                String[] newArray = new String[values.length + 1];
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
    public String get(int timestampIndex, String defaultValue) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        return defaultValue;
    }

    @Override
    public Object get(double[] timestamps, int[] timestampIndices, Estimator estimator) {
        switch (estimator) {
            case FIRST:
                return getFirst(timestampIndices);
            case LAST:
                return getLast(timestampIndices);
            default:
                throw new IllegalArgumentException("Unknown estimator.");
        }
    }

    @Override
    public String[] toArray() {
        if (size < values.length - 1) {
            final String[] res = new String[size];
            System.arraycopy(values, 0, res, 0, size);
            return res;
        } else {
            return values;
        }
    }

    @Override
    public Class<String> getTypeClass() {
        return String.class;
    }

    @Override
    public void clear() {
        super.clear();
        values = new String[0];
    }

    @Override
    public boolean isSupported(Estimator estimator) {
        return estimator.is(Estimator.FIRST, Estimator.LAST);
    }

    @Override
    protected Object getValue(int index) {
        return values[index];
    }
}
