package org.gephi.graph.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A column index is associated with a column and and keeps track of each unique
 * value and can also return the minimum and maximum values in case of a
 * sortable value type.
 *
 * @param <K> value type
 * @param <T> Element class
 */
public interface ColumnIndex<K, T extends Element> extends Iterable<Map.Entry<K, ? extends Set<T>>> {

    /**
     * Counts the elements with <em>value</em>.
     *
     * @param value the value
     * @return the number of elements in the column index with <em>value</em>, or
     *         zero if none
     */
    int count(K value);

    /**
     * Gets an Iterable of all elements in the column index with <em>value</em>.
     *
     * @param value the value
     * @return an iterable with element with <em>value</em>
     */
    Iterable<T> get(K value);

    /**
     * Returns all unique values.
     *
     * @return a collection of all unique values
     */
    Collection<K> values();

    /**
     * Counts the unique values.
     *
     * @return the number of distinct values.
     */
    int countValues();

    /**
     * Counts the elements.
     *
     * @return the number of elements in <em>column</em>
     */
    int countElements();

    /**
     * Returns whether the column index is numeric and sortable, and therefore
     * methods {@link #getMinValue()} and {@link #getMaxValue()} are available.
     *
     * @return true if sortable, false otherwise
     */
    boolean isSortable();

    /**
     * Returns the minimum value.
     * <p>
     * Only applies for sortable indices.
     *
     * @return the minimum value
     */
    Number getMinValue();

    /**
     * Returns the maximum value.
     * <p>
     * Only applies for sortable indices.
     *
     * @return the maximum value
     */
    Number getMaxValue();

    /**
     * Returns the column for which this column index belongs to.
     *
     * @return the column
     */
    Column getColumn();
}
