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
package org.gephi.attribute.api;

import org.gephi.attribute.time.Estimator;

/**
 * The table is the container for columns.
 */
public interface Table extends Iterable<Column> {

    /**
     * Adds a new column to this table.
     *
     * @param id the unique column identifier
     * @param type the column type
     * @return the newly created column
     */
    public Column addColumn(String id, Class type);

    /**
     * Adds a new column to this table.
     *
     * @param id the unique column identifier
     * @param type the column type
     * @param origin the column's origin
     * @return the newly created column
     */
    public Column addColumn(String id, Class type, Origin origin);

    /**
     * Adds a new column to this table.
     *
     * @param id the unique column identifier
     * @param title the column title
     * @param type the column type
     * @param defaultValue the default value
     * @return the newly created column
     */
    public Column addColumn(String id, String title, Class type, Object defaultValue);

    /**
     * Adds a new column to this table.
     *
     * @param id the unique column identifier
     * @param title the column title
     * @param type the column type
     * @param origin the column's origin
     * @param defaultValue the default value
     * @param indexed whether the column should be indexed
     * @return the newly created column
     */
    public Column addColumn(String id, String title, Class type, Origin origin, Object defaultValue, boolean indexed);

    /**
     * Returns the column at the given index.
     *
     * @param index the column's index
     * @return the found column
     */
    public Column getColumn(int index);

    /**
     * Returns the column with the given identifier.
     *
     * @param id the column's id
     * @return the found column
     */
    public Column getColumn(String id);

    /**
     * Returns an array of all columns.
     *
     * @return the columns array
     */
    public Column[] getColumns();

    /**
     * Returns the estimator for the given column
     *
     * @param column the column to get the estimator
     * @return the estimator, or null if not set
     */
    public Estimator getEstimator(Column column);

    /**
     * Sets the estimator for the given column.
     *
     * @param column the column to set the estimator
     * @param estimator the estimator to set
     */
    public void setEstimator(Column column, Estimator estimator);

    /**
     * Returns true if this table has the column.
     *
     * @param id the column's identifier
     * @return true if the column exists, false otherwise
     */
    public boolean hasColumn(String id);

    /**
     * Removes the given column from this table.
     *
     * @param column the column to remove
     */
    public void removeColumn(Column column);

    /**
     * Removes the given column based on its identifier from this table.
     *
     * @param id the column's identifier
     */
    public void removeColumn(String id);

    /**
     * Counts the columns in this table.
     *
     * @return the number of columns
     */
    public int countColumns();

    /**
     * The element class of this column.
     *
     * @return the element class
     */
    public Class getElementClass();

    /**
     * Creates a new table observer and return it.
     *
     * @return a newly created table observer
     */
    public TableObserver createTableObserver();
}
