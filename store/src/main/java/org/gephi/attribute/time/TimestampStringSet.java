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
        if (index < values.length) {
            values[index] = value;
        } else {
            String[] newArray = new String[values.length + 1];
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
    public String get(int timestampIndex) {
        final int index = getIndex(timestampIndex);
        if (index >= 0) {
            return values[index];
        }
        throw new IllegalArgumentException("The element doesn't exist");
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
    public void clear() {
        super.clear();
        values = new String[0];
    }
}
