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
        if (index < values.length) {
            values[index] = value;
        } else {
            byte[] newArray = new byte[values.length + 1];
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
    public Byte get(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    public byte getByte(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
    }

    @Override
    public Byte[] toArray() {
        final Byte[] res = new Byte[size];
        for (int i = 0; i < size; i++) {
            res[i] = values[i];
        }
        return res;
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
}
