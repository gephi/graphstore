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
 *
 * @author mbastian
 */
public interface AttributeModel {

    /**
     * Returns the <b>node</b> table. Contains all the columns associated to
     * node elements.
     * <p>
     * An
     * <code>AttributeModel</code> has always <b>node</b>, <b>edge</b> and
     * <b>graph</b> tables by default.
     *
     * @return the node table, contains node columns
     */
    public Table getNodeTable();

    /**
     * Returns the <b>edge</b> table. Contains all the columns associated to
     * edge elements.
     * <p>
     * An
     * <code>AttributeModel</code> has always <b>node</b>, <b>edge</b> and
     * <b>graph</b> tables by default.
     *
     * @return the edge table, contains edge columns
     */
    public Table getEdgeTable();
    
    public TimeFormat getTimeFormat();
    
    public void setTimeFormat(TimeFormat timeFormat);
}
