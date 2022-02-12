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

    /**
     * Returns the index's version. The version is incremented every time the index
     * is modified.
     *
     * @return index's version
     */
    int getVersion();
}
