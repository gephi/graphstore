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

    public boolean addEdge(Edge edge);

    public boolean addNode(Node node);

    public boolean addAllEdges(Collection<? extends Edge> edges);

    public boolean addAllNodes(Collection<? extends Node> nodes);

    public boolean removeEdge(Edge edge);

    public boolean removeNode(Node node);

    public boolean removeEdgeAll(Collection<? extends Edge> edges);

    public boolean removeNodeAll(Collection<? extends Node> nodes);

    public boolean contains(Node node);

    public boolean contains(Edge edge);

    public Node getNode(Object id);

    public Edge getEdge(Object id);

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
