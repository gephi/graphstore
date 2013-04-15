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

import org.gephi.attribute.api.Index;
import org.gephi.attribute.api.Table;
import org.gephi.attribute.api.TimestampIndex;

/**
 *
 * @author mbastian
 */
public interface GraphModel {

    public GraphFactory factory();

    public Graph getGraph();

    public Graph getGraphVisivle();

    public Subgraph getGraph(GraphView view);

    public DirectedGraph getDirectedGraph();

    public DirectedGraph getDirectedGraphVisible();

    public UndirectedGraph getUndirectedGraph();

    public UndirectedGraph getUndirectedGraphVisible();

    public DirectedSubgraph getDirectedGraph(GraphView view);

    public UndirectedSubgraph getUndirectedGraph(GraphView view);

    public GraphView getVisibleView();

    public void setVisibleView(GraphView view);

    public int addEdgeType(Object label);

    public int getEdgeType(Object label);

    public Object getEdgeLabel(int id);

    public boolean isDirected();

    public boolean isUndirected();

    public boolean isMixed();

    public GraphView createView();

    public GraphView createNodeView();

    public void destroyView(GraphView view);

    public Table getNodeTable();

    public Table getEdgeTable();

    public Index getNodeIndex();

    public Index getNodeIndex(GraphView view);

    public Index getEdgeIndex();

    public Index getEdgeIndex(GraphView view);

    public TimestampIndex getTimestampIndex();

    public TimestampIndex getTimestampIndex(GraphView view);

    public GraphObserver getGraphObserver(Graph graph, boolean withGraphDiff);
}
