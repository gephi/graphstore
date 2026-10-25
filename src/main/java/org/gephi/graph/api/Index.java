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
package org.gephi.graph.api;

import java.util.Collection;

/**
 * An index is associated with each table and keeps track of each unique value in columns.
 * <p>
 * Each column is associated with a @{{@link ColumnIndex}}.
 *
 * @param <T> Element class
 */
public interface Index<T extends Element> {

    /**
     * Counts the elements with <em>value</em> in the given <em>column</em>.
     *
     * @param column the column to count values
     * @param value the value
     * @return the number of elements in the index with <em>value</em> in <em>column</em>, or zero if none
     */
    public int count(Column column, Object value);

    /**
     * Gets an Iterable of all elements in the index with <em>value</em> in the given <em>column</em>.
     * <p>
     * The index lock, or the graph read lock if the column isn't indexed, is held from {@code iterator()} until the
     * iterator is exhausted or {@link ElementIterable#doBreak()} is called. To stop early, or when the loop body
     * throws, call <code>doBreak()</code>, which also releases the nested iterations over the same result. A result is
     * iterated and broken from a single thread and isn't shared between threads. {@code stream()} iterates a copy and
     * holds no lock, parallel streams aren't supported.
     * <p>
     * While the lock is held, other threads wait for it: for an indexed column, the threads that set its value or add
     * or remove elements of its table, otherwise the threads that write to the graph. A loop body that calls the graph,
     * model or index APIs (reading or writing the graph, setting attributes, getting a view index) can deadlock with
     * them. In that case, copy the result with {@link ElementIterable#toArray()}, which releases the lock before
     * returning, and iterate the array. The loop body must not set the values of the column or add or remove elements.
     * <p>
     * A value of the wrong type for an indexed column may throw a <code>ClassCastException</code> when iterating, not
     * in this method.
     *
     * @param column the column to get values
     * @param value the value
     * @return an iterable with element with <em>value</em> in <em>column</em>, empty if value not found
     * @throws IllegalArgumentException if <em>column</em> is the edge type column and <em>value</em> isn't an edge type
     *         label
     */
    public ElementIterable<T> get(Column column, Object value);

    /**
     * Returns all unique values in the given column.
     *
     * @param column the column to get values from
     * @return a collection of all unique values
     */
    public Collection values(Column column);

    /**
     * Counts the unique values in the given column.
     *
     * @param column the column to count values
     * @return the number of distinct values in <em>column</em>
     */
    public int countValues(Column column);

    /**
     * Counts the elements in the given column.
     *
     * @param column the column to count elements
     * @return the number of elements in <em>column</em>
     */
    public int countElements(Column column);

    /**
     * Returns whether the column is numeric and sortable, and therefore methods
     * {@link #getMinValue(org.gephi.graph.api.Column)} and {@link #getMaxValue(org.gephi.graph.api.Column)} are
     * available for the column.
     *
     * @param column the column
     * @return true if the column is sortable, false otherwise
     */
    public boolean isSortable(Column column);

    /**
     * Returns the minimum value in the given column.
     * <p>
     * Only applies for numerical columns.
     *
     * @param column the column
     * @return the minimum value in the column
     */
    public Number getMinValue(Column column);

    /**
     * Returns the maximum value in the given column.
     * <p>
     * Only applies for numerical columns.
     *
     * @param column the column
     * @return the maximum value in the column
     */
    public Number getMaxValue(Column column);

    /**
     * Returns the element type of this index.
     *
     * @return the index element class
     */
    public Class<T> getIndexClass();

    /**
     * Returns the name of this index.
     *
     * @return the index name
     */
    public String getIndexName();

    /**
     * Returns the column index for the given column.
     *
     * @param column the column to get the index for
     * @return the column index
     */
    public ColumnIndex getColumnIndex(Column column);
}
