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
 * Object to query the nodes and edges of the graph in a spatial context.
 * 
 * @author Eduardo Ramos
 */
public interface SpatialIndex {

    NodeIterable getNodesInArea(Rect2D rect);

    EdgeIterable getEdgesInArea(Rect2D rect);
}
