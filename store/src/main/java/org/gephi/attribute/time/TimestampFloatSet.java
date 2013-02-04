package org.gephi.attribute.time;

/**
 *
 * @author mbastian
 */
public final class TimestampFloatSet extends TimestampValueSet<Float> {

    private float[] values;

    public TimestampFloatSet() {
        super();
        values = new float[0];
    }

    public TimestampFloatSet(int capacity) {
        super(capacity);
        values = new float[capacity];
    }

    @Override
    public void put(int timestampIndex, Float value) {
        if (value == null) {
            throw new NullPointerException();
        }
        putFloat(timestampIndex, value);
    }

    public void putFloat(int timestampIndex, float value) {
        final int index = putInner(timestampIndex);
        if (index < values.length) {
            values[index] = value;
        } else {
            float[] newArray = new float[values.length + 1];
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
    public Float get(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    public float getFloat(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    @Override
    public Float[] toArray() {
        final Float[] res = new Float[size];
        for (int i = 0; i < size; i++) {
            res[i] = values[i];
        }
        return res;
    }

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
}
