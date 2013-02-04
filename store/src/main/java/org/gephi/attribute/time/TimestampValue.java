package org.gephi.attribute.time;

/**
 *
 * @author mbastian
 */
public class TimestampValue<T> extends Timestamp {

    protected final T value; // the value stored in this point

    public TimestampValue(double timestamp, T value) {
        super(timestamp);
        this.value = value;
    }

    public TimestampValue(Timestamp timestamp, T value) {
        super(timestamp);
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
