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

/**
 * Entry point for attribute columns management.
 * <p>
 * This model gives access to the node and edge tables, which controls the set
 * of columns each element (node/edge) has. It also provide method to get and
 * set the current time display format.
 */
public interface AttributeModel {

    /**
     * Returns the <b>node</b> table. Contains all the columns associated to
     * node elements.
     * <p>
     * An <code>AttributeModel</code> has always <b>node</b> and <b>edge</b>
     * tables by default.
     *
     * @return the node table, contains node columns
     */
    public Table getNodeTable();

    /**
     * Returns the <b>edge</b> table. Contains all the columns associated to
     * edge elements.
     * <p>
     * An <code>AttributeModel</code> has always <b>node</b> and <b>edge</b>
     * tables by default.
     *
     * @return the edge table, contains edge columns
     */
    public Table getEdgeTable();

    /**
     * Returns the time format used to display time.
     *
     * @return the time format
     */
    public TimeFormat getTimeFormat();

    /**
     * Sets the time format used to display time.
     *
     * @param timeFormat the time format
     */
    public void setTimeFormat(TimeFormat timeFormat);
}
