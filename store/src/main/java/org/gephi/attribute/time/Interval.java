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
 * Immutable time interval which supports included and excluded bounds.
 */
public final class Interval {

    /**
     * Infinity interval on both bounds.
     */
    public static final Interval INFINITY_INTERVAL = new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    
    private final double low;   // the left endpoint
    private final double high;  // the right endpoint
    private final boolean lopen; // indicates if the left endpoint is excluded
    private final boolean ropen; // indicates if the right endpoint is excluded

    /**
     * Copy constructor.
     * 
     * @param interval interval to copy
     */
    public Interval(Interval interval) {
        this.low = interval.low;
        this.high = interval.high;
        this.lopen = interval.lopen;
        this.ropen = interval.ropen;
    }

    /**
     * Constructor with bounds and whether they are included or excluded.
     * 
     * @param low interval's low bound
     * @param high interval's high bound
     * @param lopen whether the low bound is excluded
     * @param ropen whether the high bound is excluded
     */
    public Interval(double low, double high, boolean lopen, boolean ropen) {
        if (low > high) {
            throw new IllegalArgumentException(
                    "The left endpoint of the interval must be less than "
                    + "the right endpoint.");
        }

        this.low = low;
        this.high = high;
        this.lopen = lopen;
        this.ropen = ropen;
    }

    /**
     * Constructor with low and high bounds, both included. 
     * 
     * @param low interval's low bound
     * @param high interval's high bound
     */
    public Interval(double low, double high) {
        this(low, high, false, false);
    }

    /**
     * Compares this interval with the specified interval for order.
     *
     * <p>Any two intervals <i>i</i> and <i>i'</i> satisfy the interval
     * trichotomy; that is, exactly one of the following three properties
     * holds: 
     * <ol> 
     * <li> <i>i</i> and <i>i'</i> overlap
     * <li> <i>i</i> is to the left of <i>i'</i>
     * <li> <i>i</i> is to the right of <i>i'</i>
     * </ol>
     *
     * <p>Note that if two intervals are equal ({@code i.low = i'.low} and
     * {@code i.high = i'.high}), they overlap as well. But if they simply
     * overlap (for instance {@code i.low < i'.low} and {@code i.high >
     * i'.high}) they aren't equal. Remember that if two intervals are equal,
     * they have got the same bounds excluded or included.
     *
     * @param interval the interval to be compared
     *
     * @return a negative integer, zero, or a positive integer as this interval
     * is to the left of, overlaps with, or is to the right of the specified
     * interval.
     *
     * @throws NullPointerException if {@code interval} is null.
     */
    public int compareTo(Interval interval) {
        if (interval == null) {
            throw new NullPointerException("Interval cannot be null.");
        }

        if (high < interval.low || high <= interval.low && (ropen || interval.lopen)) {
            return -1;
        }
        if (interval.high < low || interval.high <= low && (interval.ropen || lopen)) {
            return 1;
        }
        return 0;
    }

    /**
     * Returns the left endpoint.
     *
     * @return the left endpoint.
     */
    public double getLow() {
        return low;
    }

    /**
     * Returns the right endpoint.
     *
     * @return the right endpoint.
     */
    public double getHigh() {
        return high;
    }

    /**
     * Indicates if the left endpoint is excluded.
     *
     * @return {@code true} if the left endpoint is excluded, {@code false}
     * otherwise.
     */
    public boolean isLowExcluded() {
        return lopen;
    }

    /**
     * Indicates if the right endpoint is excluded.
     *
     * @return {@code true} if the right endpoint is excluded, {@code false}
     * otherwise.
     */
    public boolean isHighExcluded() {
        return ropen;
    }

    /**
     * Compares this interval with the specified object for equality.
     *
     * <p>Note that two intervals are equal if {@code i.low = i'.low} and
     * {@code i.high = i'.high} and they have got the bounds excluded/included.
     *
     * @param obj object to which this interval is to be compared
     *
     * @return {@code true} if and only if the specified {@code Object} is a
     * {@code Interval} whose low and high are equal to this {@code Interval's}.
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(this.getClass())) {
            Interval interval = (Interval) obj;
            if (low == interval.low && high == interval.high
                    && lopen == interval.lopen && ropen == interval.ropen) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.low) ^ (Double.doubleToLongBits(this.low) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.high) ^ (Double.doubleToLongBits(this.high) >>> 32));
        hash = 97 * hash + (this.lopen ? 1 : 0);
        hash = 97 * hash + (this.ropen ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lopen ? '(' : '[');
        sb.append(low);
        sb.append(", ");
        sb.append(high);

        sb.append(ropen ? ')' : ']');

        return sb.toString();
    }
}
