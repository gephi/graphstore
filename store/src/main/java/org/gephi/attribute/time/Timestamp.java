package org.gephi.attribute.time;

/**
 *
 * @author mbastian
 */
public class Timestamp implements Comparable<Timestamp> {
    
    private final double timestamp;
    
    public Timestamp(double timestamp) {
        this.timestamp = timestamp;
    }
    
    public Timestamp(Timestamp timestamp) {
        this(timestamp.timestamp);
    }

    public double getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.valueOf(timestamp);
    }
    
    public String toString(boolean timesAsDoubles) {
        return ""+timestamp;
    }

    @Override
    public int compareTo(Timestamp t) {
        if(timestamp < t.timestamp) {
            return -1;
        } else if(timestamp > t.timestamp) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.timestamp) ^ (Double.doubleToLongBits(this.timestamp) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Timestamp other = (Timestamp) obj;
        if (Double.doubleToLongBits(this.timestamp) != Double.doubleToLongBits(other.timestamp)) {
            return false;
        }
        return true;
    }
}
