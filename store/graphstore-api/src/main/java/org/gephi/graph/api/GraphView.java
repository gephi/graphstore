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

import org.gephi.attribute.time.Interval;

/**
 * View on the graph.
 * <p>
 * Each graph can have views on the entire graph and use these views to obtain
 * subgraphs. A view is a filter on the main graph structure where some nodes
 * and/or edges are missing.
 * 
 * @see GraphModel
 */
public interface GraphView {

    /**
     * Gets the graph model this view belongs to.
     *
     * @return the graph model
     */
    public GraphModel getGraphModel();

    /**
     * Returns true if this view is the main view.
     *
     * @return true if main view, false otherwise
     */
    public boolean isMainView();

    /**
     * Returns true if this view supports node filtering.
     *
     * @return true if node view, false otherwise
     */
    public boolean isNodeView();

    /**
     * Returns true if this view supports edge filtering.
     *
     * @return true if edge view, false otherwise
     */
    public boolean isEdgeView();

    /**
     * Gets the time interval for this view.
     * <p>
     * If no interval is set, it returns a [-inf, +inf] interval.
     *
     * @return the time interval, or [-inf, +inf] if not set
     */
    public Interval getTimeInterval();
}
