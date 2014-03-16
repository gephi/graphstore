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

/**
 *
 * @author mbastian
 */
public interface Subgraph extends Graph {

    /**
     * Gets the view associated with this subgraph.
     * 
     * @return the graph view
     */
    @Override
    public GraphView getView();

    /**
     * Fills the subgraph so all elements in the graph are in the subgraph.
     */
    public void fill();

    /**
     * Unions the given subgraph with this sugbgraph.
     * <p>
     * The given subgraph remains unchanged.
     * 
     * @param subGraph the subgraph to do the union with
     */
    public void union(Subgraph subGraph);

    /**
     * Intersects the given subgraph with this sugbgraph.
     * <p>
     * The given subgraph remains unchanged.
     * 
     * @param subGraph the subgraph to do the intersection with
     */
    public void intersection(Subgraph subGraph);
}
