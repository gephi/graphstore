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
package org.gephi.graph.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.gephi.attribute.api.Origin;
import org.gephi.attribute.api.TimeFormat;
import org.gephi.attribute.time.Interval;
import org.gephi.attribute.time.TimestampDoubleSet;
import org.gephi.attribute.time.TimestampSet;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Subgraph;

/**
 *
 * @author mbastian
 */
public class GraphStore implements DirectedGraph, DirectedSubgraph {

    protected final GraphModelImpl graphModel;
    //Stores
    protected final NodeStore nodeStore;
    protected final EdgeStore edgeStore;
    protected final EdgeTypeStore edgeTypeStore;
    protected final ColumnStore<Node> nodeColumnStore;
    protected final ColumnStore<Edge> edgeColumnStore;
    protected final GraphViewStore viewStore;
    protected final TimestampStore timestampStore;
    protected final GraphAttributesImpl attributes;
    //Factory
    protected final GraphFactoryImpl factory;
    //Lock
    protected final GraphLock lock;
    //Version
    protected final GraphVersion version;
    protected final List<GraphObserverImpl> observers;
    //Undirected
    protected final UndirectedDecorator undirectedDecorator;
    //Main Graph view
    protected final GraphView mainGraphView;
    //TimeFormat
    protected TimeFormat timeFormat;

    public GraphStore() {
        this(null);
    }

    public GraphStore(GraphModelImpl model) {
        graphModel = model;
        lock = new GraphLock();
        edgeTypeStore = new EdgeTypeStore();
        mainGraphView = new MainGraphView();
        viewStore = new GraphViewStore(this);
        version = GraphStoreConfiguration.ENABLE_OBSERVERS ? new GraphVersion(this) : null;
        observers = GraphStoreConfiguration.ENABLE_OBSERVERS ? new ArrayList<GraphObserverImpl>() : null;
        edgeStore = new EdgeStore(edgeTypeStore, GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? lock : null, viewStore, GraphStoreConfiguration.ENABLE_OBSERVERS ? version : null);
        nodeStore = new NodeStore(edgeStore, GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? lock : null, viewStore, GraphStoreConfiguration.ENABLE_OBSERVERS ? version : null);
        nodeColumnStore = new ColumnStore<Node>(Node.class, GraphStoreConfiguration.ENABLE_INDEX_NODES);
        edgeColumnStore = new ColumnStore<Edge>(Edge.class, GraphStoreConfiguration.ENABLE_INDEX_EDGES);
        timestampStore = new TimestampStore(this, GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? lock : null);
        attributes = new GraphAttributesImpl();
        factory = new GraphFactoryImpl(this);
        timeFormat = GraphStoreConfiguration.DEFAULT_TIME_FORMAT;

        undirectedDecorator = new UndirectedDecorator(this);

        //Default cols
        nodeColumnStore.addColumn(new ColumnImpl(model != null ? model.nodeTable : null, "id", Object.class, "Id", null, Origin.PROPERTY, false, true));
        edgeColumnStore.addColumn(new ColumnImpl(model != null ? model.edgeTable : null, "id", Object.class, "Id", null, Origin.PROPERTY, false, true));
        if (GraphStoreConfiguration.ENABLE_ELEMENT_LABEL) {
            nodeColumnStore.addColumn(new ColumnImpl(model != null ? model.nodeTable : null, "label", String.class, "Label", null, Origin.PROPERTY, false, false));
            edgeColumnStore.addColumn(new ColumnImpl(model != null ? model.edgeTable : null, "label", String.class, "Label", null, Origin.PROPERTY, false, false));
        }
        if (GraphStoreConfiguration.ENABLE_ELEMENT_TIMESTAMP_SET) {
            nodeColumnStore.addColumn(new ColumnImpl(model != null ? model.nodeTable : null, "timestamp", TimestampSet.class, "Timestamp", null, Origin.PROPERTY, false, false));
            edgeColumnStore.addColumn(new ColumnImpl(model != null ? model.edgeTable : null, "timestamp", TimestampSet.class, "Timestamp", null, Origin.PROPERTY, false, false));
        }
        edgeColumnStore.addColumn(new ColumnImpl(model != null ? model.edgeTable : null, "weight", TimestampDoubleSet.class, "Weight", null, Origin.PROPERTY, false, false));
    }

    @Override
    public boolean addNode(final Node node) {
        autoWriteLock();
        try {
            return nodeStore.add(node);
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public boolean addAllNodes(final Collection<? extends Node> nodes) {
        autoWriteLock();
        try {
            return nodeStore.addAll(nodes);
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public boolean addEdge(final Edge edge) {
        autoWriteLock();
        try {
            int type = edge.getType();
            if (edgeTypeStore != null && !edgeTypeStore.contains(type)) {
                if (GraphStoreConfiguration.ENABLE_AUTO_TYPE_REGISTRATION) {
                    edgeTypeStore.addType(type);
                } else {
                    throw new RuntimeException("The type doesn't exist");
                }
            }
            return edgeStore.add(edge);
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public boolean addAllEdges(Collection<? extends Edge> edges) {
        autoWriteLock();
        try {
            return edgeStore.addAll(edges);
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public NodeImpl getNode(final Object id) {
        autoReadLock();
        try {
            return nodeStore.get(id);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public EdgeImpl getEdge(final Object id) {
        autoReadLock();
        try {
            return edgeStore.get(id);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public Edge getMutualEdge(Edge edge) {
        autoReadLock();
        try {
            return edgeStore.getMutualEdge(edge);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public NodeIterable getNodes() {
        return nodeStore;
    }

    @Override
    public EdgeIterable getEdges() {
        return edgeStore;
    }

    @Override
    public EdgeIterable getSelfLoops() {
        return new EdgeIterableWrapper(edgeStore.iteratorSelfLoop());
    }

    @Override
    public boolean removeNode(final Node node) {
        autoWriteLock();
        try {
            nodeStore.checkNonNullNodeObject(node);
            for (EdgeStore.EdgeInOutIterator edgeIterator = edgeStore.edgeIterator((NodeImpl) node); edgeIterator.hasNext();) {
                edgeIterator.next();
                edgeIterator.remove();
            }
            return nodeStore.remove(node);
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public boolean removeEdge(final Edge edge) {
        autoWriteLock();
        try {
            return edgeStore.remove(edge);
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public boolean removeAllNodes(Collection<? extends Node> nodes) {
        autoWriteLock();
        try {
            for (Node node : nodes) {
                nodeStore.checkNonNullNodeObject(node);
                for (EdgeStore.EdgeInOutIterator edgeIterator = edgeStore.edgeIterator((NodeImpl) node); edgeIterator.hasNext();) {
                    edgeIterator.next();
                    edgeIterator.remove();
                }
            }
            return nodeStore.removeAll(nodes);
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Edge> edges) {
        autoWriteLock();
        try {
            return edgeStore.removeAll(edges);
        } finally {
            autoWriteUnlock();
        }
    }

    public NodeStore getNodeStore() {
        return nodeStore;
    }

    public EdgeStore getEdgeStore() {
        return edgeStore;
    }

    @Override
    public boolean contains(final Node node) {
        autoReadLock();
        try {
            return nodeStore.contains(node);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public boolean contains(final Edge edge) {
        autoReadLock();
        try {
            return edgeStore.contains(edge);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public Edge getEdge(final Node node1, final Node node2, final int type) {
        autoReadLock();
        try {
            return edgeStore.get(node1, node2, type);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public Edge getEdge(final Node node1, final Node node2) {
        autoReadLock();
        try {
            return edgeStore.get(node1, node2);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public NodeIterable getNeighbors(final Node node) {
        return new NodeIterableWrapper(edgeStore.neighborIterator(node));
    }

    @Override
    public NodeIterable getNeighbors(final Node node, final int type) {
        return new NodeIterableWrapper(edgeStore.neighborIterator(node, type));
    }

    @Override
    public NodeIterable getPredecessors(final Node node) {
        return new NodeIterableWrapper(edgeStore.neighborInIterator(node));
    }

    @Override
    public NodeIterable getPredecessors(final Node node, final int type) {
        return new NodeIterableWrapper(edgeStore.neighborInIterator(node, type));
    }

    @Override
    public NodeIterable getSuccessors(final Node node) {
        return new NodeIterableWrapper(edgeStore.neighborOutIterator(node));
    }

    @Override
    public NodeIterable getSuccessors(final Node node, final int type) {
        return new NodeIterableWrapper(edgeStore.neighborOutIterator(node, type));
    }

    @Override
    public EdgeIterable getEdges(final Node node) {
        return new EdgeIterableWrapper(edgeStore.edgeIterator(node));
    }

    @Override
    public EdgeIterable getEdges(final Node node, final int type) {
        return new EdgeIterableWrapper(edgeStore.edgeIterator(node, type));
    }

    @Override
    public EdgeIterable getInEdges(final Node node) {
        return new EdgeIterableWrapper(edgeStore.edgeInIterator(node));
    }

    @Override
    public EdgeIterable getInEdges(final Node node, final int type) {
        return new EdgeIterableWrapper(edgeStore.edgeInIterator(node, type));
    }

    @Override
    public EdgeIterable getOutEdges(final Node node) {
        return new EdgeIterableWrapper(edgeStore.edgeOutIterator(node));
    }

    @Override
    public EdgeIterable getOutEdges(final Node node, final int type) {
        return new EdgeIterableWrapper(edgeStore.edgeOutIterator(node, type));
    }

    @Override
    public int getNodeCount() {
        return nodeStore.size();
    }

    @Override
    public int getEdgeCount() {
        return edgeStore.size();
    }

    @Override
    public int getEdgeCount(final int type) {
        autoReadLock();
        try {
            if (edgeTypeStore.contains(type)) {
                return edgeStore.size(type);
            }
            return 0;
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public Node getOpposite(final Node node, final Edge edge) {
        nodeStore.checkNonNullNodeObject(node);
        edgeStore.checkNonNullEdgeObject(edge);
        return edge.getSource() == node ? edge.getTarget() : edge.getSource();
    }

    @Override
    public int getDegree(final Node node) {
        nodeStore.checkNonNullNodeObject(node);
        return ((NodeImpl) node).getDegree();
    }

    public int getUndirectedDegree(final Node node) {
        nodeStore.checkNonNullNodeObject(node);
        return ((NodeImpl) node).getUndirectedDegree();
    }

    @Override
    public int getInDegree(final Node node) {
        nodeStore.checkNonNullNodeObject(node);
        return ((NodeImpl) node).getInDegree();
    }

    @Override
    public int getOutDegree(final Node node) {
        nodeStore.checkNonNullNodeObject(node);
        return ((NodeImpl) node).getOutDegree();
    }

    @Override
    public boolean isSelfLoop(final Edge edge) {
        return edge.isSelfLoop();
    }

    @Override
    public boolean isDirected(final Edge edge) {
        return edge.isDirected();
    }

    @Override
    public boolean isAdjacent(final Node node1, final Node node2) {
        autoReadLock();
        try {
            return edgeStore.isAdjacent(node1, node2);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public boolean isAdjacent(final Node node1, final Node node2, final int type) {
        autoReadLock();
        try {
            return edgeStore.isAdjacent(node1, node2, type);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public boolean isIncident(final Edge edge1, final Edge edge2) {
        autoReadLock();
        try {
            edgeStore.checkNonNullEdgeObject(edge1);
            edgeStore.checkNonNullEdgeObject(edge2);

            return edgeStore.isIncident((EdgeImpl) edge1, (EdgeImpl) edge2);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public boolean isIncident(final Node node, final Edge edge) {
        autoReadLock();
        try {
            nodeStore.checkNonNullNodeObject(node);
            edgeStore.checkNonNullEdgeObject(edge);

            return edgeStore.isIncident((NodeImpl) node, (EdgeImpl) edge);
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public void clearEdges(final Node node) {
        autoWriteLock();
        try {
            EdgeStore.EdgeInOutIterator itr = edgeStore.edgeIterator(node);
            for (; itr.hasNext();) {
                itr.next();
                itr.remove();
            }
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public void clearEdges(final Node node, final int type) {
        autoWriteLock();
        try {
            EdgeStore.EdgeTypeInOutIterator itr = edgeStore.edgeIterator(node, type);
            for (; itr.hasNext();) {
                itr.next();
                itr.remove();
            }
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public void clear() {
        autoWriteLock();
        try {
            edgeStore.clear();
            nodeStore.clear();
            edgeTypeStore.clear();
            edgeColumnStore.indexStore.clear();
            nodeColumnStore.indexStore.clear();
            timestampStore.clear();
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public void clearEdges() {
        autoWriteLock();
        try {
            edgeStore.clear();
            edgeTypeStore.clear();
            edgeColumnStore.indexStore.clear();
            timestampStore.clearEdges();
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public GraphView getView() {
        return mainGraphView;
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.getValue(key);
    }

    @Override
    public Object getAttribute(String key, double timestamp) {
        return attributes.getValue(key, timestamp);
    }

    @Override
    public Set<String> getAttributeKeys() {
        return attributes.getKeys();
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.setValue(key, value);
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        attributes.setValue(key, value, timestamp);
    }

    @Override
    public void readLock() {
        lock.readLock();
    }

    @Override
    public void readUnlock() {
        lock.readUnlock();
    }

    @Override
    public void readUnlockAll() {
        lock.readUnlockAll();
    }

    @Override
    public void writeLock() {
        lock.writeLock();
    }

    @Override
    public void writeUnlock() {
        lock.writeUnlock();
    }

    protected void autoReadLock() {
        if (GraphStoreConfiguration.ENABLE_AUTO_LOCKING) {
            readLock();
        }
    }

    protected void autoReadUnlock() {
        if (GraphStoreConfiguration.ENABLE_AUTO_LOCKING) {
            readUnlock();
        }
    }

    protected void autoWriteLock() {
        if (GraphStoreConfiguration.ENABLE_AUTO_LOCKING) {
            writeLock();
        }
    }

    protected void autoWriteUnlock() {
        if (GraphStoreConfiguration.ENABLE_AUTO_LOCKING) {
            writeUnlock();
        }
    }

    @Override
    public boolean isDirected() {
        autoReadLock();
        try {
            return edgeStore.isDirectedGraph();
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public boolean isUndirected() {
        autoReadLock();
        try {
            return edgeStore.isUndirectedGraph();
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public boolean isMixed() {
        autoReadLock();
        try {
            return edgeStore.isMixedGraph();
        } finally {
            autoReadUnlock();
        }
    }

    @Override
    public void union(Subgraph subGraph) {
        throw new UnsupportedOperationException("Not supported for the main view.");
    }

    @Override
    public void intersection(Subgraph subGraph) {
        throw new UnsupportedOperationException("Not supported for the main view.");
    }

    @Override
    public void fill() {
        throw new UnsupportedOperationException("Not supported for the main view.");
    }
    
    @Override
    public Graph getRootGraph() {
        return this;
    }

    protected GraphObserverImpl createGraphObserver(Graph graph, boolean withDiff) {
        if (graph.getView() != mainGraphView) {
            throw new RuntimeException("This graph doesn't belong to this store");
        }

        if (observers != null) {
            GraphObserverImpl observer = new GraphObserverImpl(this, version, graph, withDiff);
            observers.add(observer);

            return observer;
        }
        return null;
    }

    protected void destroyGraphObserver(GraphObserverImpl observer) {
        if (observers != null) {
            if (observer.graph.getView() != mainGraphView) {
                throw new RuntimeException("This graph doesn't belong to this store");
            }
            observers.remove(observer);
            observer.destroyObserver();
        }
    }

    protected EdgeIterableWrapper getEdgeIterableWrapper(Iterator<Edge> edgeIterator) {
        return new EdgeIterableWrapper(edgeIterator);
    }

    protected NodeIterableWrapper getNodeIterableWrapper(Iterator<Node> nodeIterator) {
        return new NodeIterableWrapper(nodeIterator);
    }

    protected EdgeIterableWrapper getEdgeIterableWrapper(Iterator<Edge> edgeIterator, boolean blocking) {
        return new EdgeIterableWrapper(edgeIterator, blocking);
    }

    protected NodeIterableWrapper getNodeIterableWrapper(Iterator<Node> nodeIterator, boolean blocking) {
        return new NodeIterableWrapper(nodeIterator, blocking);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.nodeStore != null ? this.nodeStore.hashCode() : 0);
        hash = 29 * hash + (this.edgeStore != null ? this.edgeStore.hashCode() : 0);
        hash = 29 * hash + (this.edgeTypeStore != null ? this.edgeTypeStore.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GraphStore other = (GraphStore) obj;
        if (this.nodeStore != other.nodeStore && (this.nodeStore == null || !this.nodeStore.equals(other.nodeStore))) {
            return false;
        }
        if (this.edgeStore != other.edgeStore && (this.edgeStore == null || !this.edgeStore.equals(other.edgeStore))) {
            return false;
        }
        if (this.edgeTypeStore != other.edgeTypeStore && (this.edgeTypeStore == null || !this.edgeTypeStore.equals(other.edgeTypeStore))) {
            return false;
        }
        return true;
    }

    protected class NodeIterableWrapper implements NodeIterable {

        protected final Iterator<Node> iterator;
        protected final boolean blocking;

        public NodeIterableWrapper(Iterator<Node> iterator) {
            this(iterator, true);
        }

        public NodeIterableWrapper(Iterator<Node> iterator, boolean blocking) {
            this.iterator = iterator;
            this.blocking = blocking;
        }

        @Override
        public Iterator<Node> iterator() {
            return iterator;
        }

        @Override
        public Node[] toArray() {
            List<Node> list = new ArrayList<Node>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list.toArray(new Node[0]);
        }

        @Override
        public Collection<Node> toCollection() {
            List<Node> list = new ArrayList<Node>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list;
        }

        @Override
        public void doBreak() {
            if (blocking) {
                autoReadUnlock();
            }
        }
    }

    protected class EdgeIterableWrapper implements EdgeIterable {

        protected final Iterator<Edge> iterator;
        protected final boolean blocking;

        public EdgeIterableWrapper(Iterator<Edge> iterator) {
            this(iterator, true);
        }

        public EdgeIterableWrapper(Iterator<Edge> iterator, boolean blocking) {
            this.iterator = iterator;
            this.blocking = blocking;
        }

        @Override
        public Iterator<Edge> iterator() {
            return iterator;
        }

        @Override
        public Edge[] toArray() {
            List<Edge> list = new ArrayList<Edge>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list.toArray(new Edge[0]);
        }

        @Override
        public Collection<Edge> toCollection() {
            List<Edge> list = new ArrayList<Edge>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list;
        }

        @Override
        public void doBreak() {
            if (blocking) {
                autoReadUnlock();
            }
        }
    }

    private final class MainGraphView implements GraphView {

        @Override
        public GraphModel getGraphModel() {
            return graphModel;
        }

        @Override
        public boolean isMainView() {
            return true;
        }

        @Override
        public boolean isNodeView() {
            return true;
        }

        @Override
        public boolean isEdgeView() {
            return true;
        }

        @Override
        public Interval getTimeInterval() {
            return Interval.INFINITY_INTERVAL;
        }
    }
}
