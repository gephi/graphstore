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
import org.gephi.attribute.time.Interval;

/**
 *
 * @author mbastian
 */
public interface GraphModel {

    public GraphFactory factory();

    public Graph getGraph();

    public Graph getGraphVisible();

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

    public boolean isDynamic();

    public boolean isMultiGraph();

    public GraphView createView();

    public GraphView createView(boolean node, boolean edge);

    public GraphView copyView(GraphView view);

    public GraphView copyView(GraphView view, boolean node, boolean edge);

    public void destroyView(GraphView view);

    public void setTimeInterval(GraphView view, Interval interval);

    public Table getNodeTable();

    public Table getEdgeTable();

    public Index<Node> getNodeIndex();

    public Index<Node> getNodeIndex(GraphView view);

    public Index<Edge> getEdgeIndex();

    public Index<Edge> getEdgeIndex(GraphView view);

    public TimestampIndex<Node> getNodeTimestampIndex();

    public TimestampIndex<Node> getNodeTimestampIndex(GraphView view);

    public TimestampIndex<Edge> getEdgeTimestampIndex();

    public TimestampIndex<Edge> getEdgeTimestampIndex(GraphView view);
    
    public Interval getTimeBounds();
    
    public Interval getTimeBoundsVisible();

    public GraphObserver getGraphObserver(Graph graph, boolean withGraphDiff);
}
