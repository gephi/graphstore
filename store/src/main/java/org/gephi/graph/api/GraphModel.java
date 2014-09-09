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

    /**
     * Returns the graph factory.
     *
     * @return the graph factory
     */
    public GraphFactory factory();

    /**
     * Gets the full graph.
     *
     * @return the graph
     */
    public Graph getGraph();

    /**
     * Get the visible graph.
     * <p>
     * The visible graph may be the full graph (default) or a graph view.
     *
     * @return the visible graph
     */
    public Graph getGraphVisible();

    /**
     * Gets the graph for the given graph view.
     *
     * @param view the graph view
     * @return the graph for this view
     */
    public Subgraph getGraph(GraphView view);

    /**
     * Gets the full graph with the directed interface.
     *
     * @return the directed graph
     */
    public DirectedGraph getDirectedGraph();

    /**
     * Gets the visible graph with the directed interface.
     *
     * @return the visible graph
     */
    public DirectedGraph getDirectedGraphVisible();

    /**
     * Gets the full graph with the undirected interface.
     *
     * @return the undirected graph
     */
    public UndirectedGraph getUndirectedGraph();

    /**
     * Gets the visible graph with the undirected interface.
     *
     * @return the visible graph
     */
    public UndirectedGraph getUndirectedGraphVisible();

    /**
     * Gets the directed graph for the given graph view.
     *
     * @param view the graph view
     * @return the directed graph for this view
     */
    public DirectedSubgraph getDirectedGraph(GraphView view);

    /**
     * Gets the undirected graph for the given graph view.
     *
     * @param view the graph view
     * @return the undirected graph for this view
     */
    public UndirectedSubgraph getUndirectedGraph(GraphView view);

    /**
     * Gets the visible view.
     *
     * @return the visible view
     */
    public GraphView getVisibleView();

    /**
     * Sets the visible view.
     * <p>
     * If <em>view</em> is null, it restores the main view.
     *
     * @param view the view
     */
    public void setVisibleView(GraphView view);

    /**
     * Adds a new edge type and returns the integer identifier.
     * <p>
     * If the type already exists, it returns the existing identifier.
     *
     * @param label the edge type label
     * @return the newly created edge type identifier.
     */
    public int addEdgeType(Object label);

    /**
     * Gets the edge type for the given label.
     *
     * @param label the edge label
     * @return the edge type identifier, or -1 if not found
     */
    public int getEdgeType(Object label);

    /**
     * Gets the edge label associated with the given type.
     *
     * @param id the edge type
     * @return the edge label
     */
    public Object getEdgeLabel(int id);

    /**
     * Returns true if the graph is directed.
     *
     * @return true if directed, false otherwise
     */
    public boolean isDirected();

    /**
     * Returns true if the graph is undirected.
     *
     * @return true if undirected, false otherwise
     */
    public boolean isUndirected();

    /**
     * Returns true if the graph is mixed (both directed and undirected edges).
     *
     * @return true if mixed, false otherwise
     */
    public boolean isMixed();

    /**
     * Returns true if the graph is dynamic.
     *
     * @return true if dynamic, false otherwise
     */
    public boolean isDynamic();

    /**
     * Returns true if the graph is multi-graph (multiple types of edges).
     *
     * @return true if multi-graph, false otherwise
     */
    public boolean isMultiGraph();

    /**
     * Creates a new graph view.
     *
     * @return the newly created graph view
     */
    public GraphView createView();

    /**
     * Creates a new graph view.
     * <p>
     * The node and edge parameters allows to restrict the view filtering to
     * only nodes or only edges. By default, the view applies to both nodes and
     * edges.
     *
     * @param node true to enable node view, false otherwise
     * @param edge true to enable edge view, false otherwise
     * @return the newly created graph view
     */
    public GraphView createView(boolean node, boolean edge);

    /**
     * Creates a new graph view based on an existing view.
     *
     * @param view the view to copy
     * @return the newly created graph view
     */
    public GraphView copyView(GraphView view);

    /**
     * Creates a new graph based on an existing view.
     * <p>
     * The node and edge parameters allows to restrict the view filtering to
     * only nodes or only edges. By default, the view applies to both nodes and
     * edges.
     *
     * @param view the view to copy
     * @param node true to enable node view, false otherwise
     * @param edge true to enable edge view, false otherwise
     * @return the newly created graph view
     */
    public GraphView copyView(GraphView view, boolean node, boolean edge);

    /**
     * Destroys the given view.
     *
     * @param view the view to destryo
     */
    public void destroyView(GraphView view);

    /**
     * Sets the given time interval to the view.
     * <p>
     * Each view can be configured with a time interval to filter a graph over
     * time.
     *
     * @param view the view to configure
     * @param interval the time interval
     */
    public void setTimeInterval(GraphView view, Interval interval);

    /**
     * Gets the node table.
     *
     * @return the node table
     */
    public Table getNodeTable();

    /**
     * Gets the edge table.
     *
     * @return the edge table
     */
    public Table getEdgeTable();

    /**
     * Gets the node index.
     *
     * @return the node index
     */
    public Index<Node> getNodeIndex();

    /**
     * Gets the node index for the given graph view.
     *
     * @param view the view to get the index from
     * @return the node index
     */
    public Index<Node> getNodeIndex(GraphView view);

    /**
     * Gets the edge index.
     *
     * @return the edge index
     */
    public Index<Edge> getEdgeIndex();

    /**
     * Gets the edge index for the given graph view.
     *
     * @param view the view to get the index from
     * @return the edge index
     */
    public Index<Edge> getEdgeIndex(GraphView view);

    /**
     * Gets the node timestamp index.
     *
     * @return the node timestamp index
     */
    public TimestampIndex<Node> getNodeTimestampIndex();

    /**
     * Gets the node timestamp index for the given view.
     *
     * @param view the view to get the index from
     * @return the node timestamp index
     */
    public TimestampIndex<Node> getNodeTimestampIndex(GraphView view);

    /**
     * Gets the edge timestamp index.
     *
     * @return the edge timestamp index
     */
    public TimestampIndex<Edge> getEdgeTimestampIndex();

    /**
     * Gets the edge timestamp index for the given view.
     *
     * @param view the view to get the index from
     * @return the edge timestamp index
     */
    public TimestampIndex<Edge> getEdgeTimestampIndex(GraphView view);

    /**
     * Gets the time bounds.
     * <p>
     * The time bounds is an interval made of the minimum and maximum time
     * observed in the entire graph.
     *
     * @return the time bounds
     */
    public Interval getTimeBounds();

    /**
     * Gets the time bounds for the visible graph.
     * <p>
     * The time bounds is an interval made of the minimum and maximum time
     * observed in the entire graph.
     *
     * @return the time bounds
     */
    public Interval getTimeBoundsVisible();

    /**
     * Gets the time bounds for the given graph view.
     * <p>
     * The time bounds is an interval made of the minimum and maximum time
     * observed in the entire graph.
     *
     * @param view the graph view
     * @return the time bounds
     */
    public Interval getTimeBounds(GraphView view);

    /**
     * Creates and returns a new graph observer.
     *
     * @param graph the graph to observe
     * @param withGraphDiff true to include graph difference feature, false
     * otherwise
     * @return the newly created graph observer
     */
    public GraphObserver createGraphObserver(Graph graph, boolean withGraphDiff);
}
