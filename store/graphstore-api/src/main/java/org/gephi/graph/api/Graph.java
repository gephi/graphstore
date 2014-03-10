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

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author mbastian
 */
public interface Graph {

    /**
     * Adds an edge to this graph.
     * 
     * @param edge the edge to add
     * @return true if the edge has been added, false if it already exists
     */
    public boolean addEdge(Edge edge);

    /**
     * Adds a node to this graph.
     * 
     * @param node the node to add
     * @return true if the node has been added, false if it already exists
     */
    public boolean addNode(Node node);

    /**
     * Adds all edges in the collection to this graph.
     * 
     * @param edges the edge collection
     * @return true if at least one edge has been added, false otherwise
     */
    public boolean addAllEdges(Collection<? extends Edge> edges);

    /**
     * Adds all nodes in the collection to this graph.
     * 
     * @param nodes the node collection
     * @return true if at least one node has been added, false otherwise
     */
    public boolean addAllNodes(Collection<? extends Node> nodes);

    /**
     * Removes an edge from this graph.
     * 
     * @param edge the edge to remove
     * @return true if the edge was removed, false if it didn't exist
     */
    public boolean removeEdge(Edge edge);

    /**
     * Removes a node from this graph.
     * 
     * @param node the node to remove
     * @return true if the node was removed, false if it didn't exist
     */
    public boolean removeNode(Node node);

    /**
     * Removes all edges in the collection from this graph.
     * 
     * @param edges the edge collection
     * @return true if at least one edge has been removed, false otherwise
     */
    public boolean removeEdgeAll(Collection<? extends Edge> edges);

    /**
     * Removes all nodes in the collection from this graph.
     * 
     * @param nodes the node collection
     * @return true if at least one node has been removed, false otherwise
     */
    public boolean removeNodeAll(Collection<? extends Node> nodes);

    /**
     * Returns true if <em>node</em> is contained in this graph.
     * 
     * @param node the node to test
     * @return true if this graph contains <em>node</em>, false otherwise
     */
    public boolean contains(Node node);

    /**
     * Returns true if <em>edge</em> is contained in this graph.
     * 
     * @param edge the edge to test
     * @return true if this graph contains <em>edge</em>, false otherwise
     */
    public boolean contains(Edge edge);

    /**
     * Gets a node given its identifier.
     * 
     * @param id the node id
     * @return the node, or null if not found
     */
    public Node getNode(Object id);

    /**
     * Gets an edge by its identifier.
     * 
     * @param id the edge id
     * @return the edge, or null if not found
     */
    public Edge getEdge(Object id);

    /**
     * Gets the edge adjacent to node1 and node2.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @return the adjacent edge, or null if not found
     */
    public Edge getEdge(Node node1, Node node2);

    public Edge getEdge(Node node1, Node node2, int type);

    public NodeIterable getNodes();

    public EdgeIterable getEdges();

    public EdgeIterable getSelfLoops();

    public NodeIterable getNeighbors(Node node);

    public NodeIterable getNeighbors(Node node, int type);

    public EdgeIterable getEdges(Node node);

    public EdgeIterable getEdges(Node node, int type);

    public int getNodeCount();

    public int getEdgeCount();

    public int getEdgeCount(int type);

    public Node getOpposite(Node node, Edge edge);

    public int getDegree(Node node);

    public boolean isSelfLoop(Edge edge);

    public boolean isDirected(Edge edge);

    public boolean isAdjacent(Node node1, Node node2);

    public boolean isAdjacent(Node node1, Node node2, int type);

    public boolean isIncident(Edge edge1, Edge edge2);

    public boolean isIncident(Node node, Edge edge);

    public void clearEdges(Node node);

    public void clearEdges(Node node, int type);

    public void clear();

    public void clearEdges();

    public GraphView getView();
    
    public Object getAttribute(String key);

    public Object getAttribute(String key, double timestamp);
    
    public void setAttribute(String key, Object value);
    
    public void setAttribute(String key, Object value, double timestamp);
    
    public Set<String> getAttributeKeys();

    public boolean isDirected();

    public boolean isUndirected();

    public boolean isMixed();

    public void readLock();

    public void readUnlock();

    public void readUnlockAll();

    public void writeLock();

    public void writeUnlock();
}
