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

import java.util.Set;
import org.gephi.attribute.api.Column;

/**
 * An element is a node or an edge.
 */
public interface Element extends ElementProperties {

    /**
     * Returns the identifier.
     *
     * @return the identifier
     */
    public Object getId();

    /**
     * Returns the label.
     *
     * @return the label
     */
    public String getLabel();

    /**
     * Gets the attribute for the given key.
     *
     * @param key the column's key
     * @return the attribute value, or null
     */
    public Object getAttribute(String key);

    /**
     * Gets the attribute for the given column.
     *
     * @param column the column
     * @return the attribute value, or null
     */
    public Object getAttribute(Column column);

    /**
     * Gets the attribute for the given key and timestamp.
     *
     * @param key the column's key
     * @param timestamp the timestamp
     * @return the attribute value, or null
     */
    public Object getAttribute(String key, double timestamp);

    /**
     * Gets the attribute for the given column and timestamp.
     *
     * @param column the column
     * @param timestamp the timestamp
     * @return the attribute value, or null
     */
    public Object getAttribute(Column column, double timestamp);

    /**
     * Gets the attribute for the given key and graph view.
     *
     * @param key the column's key
     * @param view the graph view
     * @return the attribute value, or null
     */
    public Object getAttribute(String key, GraphView view);

    /**
     * Gets the attribute for the given column and graph view.
     *
     * @param column the column
     * @param view the graph view
     * @return the attribute value, or null
     */
    public Object getAttribute(Column column, GraphView view);

    /**
     * Returns all the attribute values in an array.
     * <p>
     * Some attribute values may be null.
     *
     * @return the attribute values array
     */
    public Object[] getAttributes();

    /**
     * Returns the column identifier keys.
     *
     * @return the attribute keys
     */
    public Set<String> getAttributeKeys();

    /**
     * Returns the location of this element in the store.
     *
     * @return the store id
     */
    public int getStoreId();

    /**
     * Removes the attribute at the given key.
     *
     * @param key the key
     * @return the value being removed, or null
     */
    public Object removeAttribute(String key);

    /**
     * Removes the attribute at the given column.
     *
     * @param column the column
     * @return the value being removed, or null
     */
    public Object removeAttribute(Column column);

    /**
     * Sets the label.
     *
     * @param label the label
     */
    public void setLabel(String label);

    /**
     * Sets the attribute with the given key and value.
     *
     * @param key the column's key
     * @param value the value to set
     */
    public void setAttribute(String key, Object value);

    /**
     * Sets the attribute with the given column and value.
     *
     * @param column the column
     * @param value the value to set
     */
    public void setAttribute(Column column, Object value);

    /**
     * Sets the attribute at the given key and timestamp.
     *
     * @param key the column's key
     * @param value the value to set
     * @param timestamp the timestamp
     */
    public void setAttribute(String key, Object value, double timestamp);

    /**
     * Sets the attribute at the given column and timestamp.
     *
     * @param column the column
     * @param value the value to set
     * @param timestamp the timestamp
     */
    public void setAttribute(Column column, Object value, double timestamp);

    /**
     * Adds a timestamp.
     *
     * @param timestamp the timestamp to add
     * @return true if the timestamp has been added, false if it existed already
     */
    public boolean addTimestamp(double timestamp);

    /**
     * Removes a timestamp.
     *
     * @param timestamp the timestamp to remove
     * @return true if the timestamp has been removed, false if it didn't exist
     */
    public boolean removeTimestamp(double timestamp);

    /**
     * Returns true if this element has the given timestamp.
     *
     * @param timestamp the timestamp
     * @return true if this element has the timestamp, false otherwise
     */
    public boolean hasTimestamp(double timestamp);

    /**
     * Returns all the timestamps this element belong to.
     *
     * @return the timestamp array
     */
    public double[] getTimestamps();

    /**
     * Clears all attribute values.
     */
    public void clearAttributes();
}
