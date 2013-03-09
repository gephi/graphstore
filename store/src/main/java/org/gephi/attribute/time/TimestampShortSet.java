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
        if (index < values.length) {
            values[index] = value;
        } else {
            short[] newArray = new short[values.length + 1];
            System.arraycopy(values, 0, newArray, 0, index);
            System.arraycopy(values, index, newArray, index + 1, values.length - index);
            newArray[index] = value;
            values = newArray;
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
    public Short get(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    public short getShort(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    @Override
    public Short[] toArray() {
        final Short[] res = new Short[size];
        for (int i = 0; i < size; i++) {
            res[i] = values[i];
        }
        return res;
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
}
