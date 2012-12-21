package org.gephi.graph.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.EdgeIterator;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.NodeIterator;

/**
 *
 * @author mbastian
 */
public class GraphStore implements DirectedGraph {

    //Auto-lock
    public static boolean AUTO_LOCKING = true;
    public static boolean AUTO_TYPE_REGISTRATION = true;
    public static boolean INDEX_NODES = true;
    public static boolean INDEX_EDGES = true;
    //Model
    protected final GraphModelImpl graphModel;
    //Stores
    protected final NodeStore nodeStore;
    protected final EdgeStore edgeStore;
    protected final EdgeTypeStore edgeTypeStore;
    protected final ColumnStore<Node> nodePropertyStore;
    protected final ColumnStore<Edge> edgePropertyStore;
    //Factory
    protected final GraphFactoryImpl factory;
    //Lock
    protected final GraphLock lock;
    //Undirected
    protected final UndirectedDecorator undirectedDecorator;
    //Main Graph view
    protected final MainGraphView mainGraphView;

    public GraphStore() {
        this(null);
    }

    public GraphStore(GraphModelImpl model) {
        graphModel = model;
        lock = new GraphLock();
        edgeTypeStore = new EdgeTypeStore();
        edgeTypeStore.addType("Default Type");
        edgeStore = new EdgeStore(edgeTypeStore, AUTO_LOCKING ? lock : null);
        nodeStore = new NodeStore(edgeStore, AUTO_LOCKING ? lock : null);
        nodePropertyStore = new ColumnStore<Node>(Node.class, INDEX_NODES, AUTO_LOCKING ? lock : null);
        edgePropertyStore = new ColumnStore<Edge>(Edge.class, INDEX_EDGES, AUTO_LOCKING ? lock : null);
        factory = new GraphFactoryImpl(this);
        mainGraphView = new MainGraphView();

        undirectedDecorator = new UndirectedDecorator(this);
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
                if (AUTO_TYPE_REGISTRATION) {
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
    public boolean removeNodeAll(Collection<? extends Node> nodes) {
        autoWriteLock();
        try {
            for (Iterator<? extends Node> itr = nodes.iterator(); itr.hasNext();) {
                Node node = itr.next();
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
    public boolean removeEdgeAll(Collection<? extends Edge> edges) {
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
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public void clearEdges() {
        autoWriteLock();
        try {
            edgeStore.clear();
        } finally {
            autoWriteUnlock();
        }
    }

    @Override
    public GraphView getView() {
        return mainGraphView;
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
        if (AUTO_LOCKING) {
            readLock();
        }
    }

    protected void autoReadUnlock() {
        if (AUTO_LOCKING) {
            readUnlock();
        }
    }

    protected void autoWriteLock() {
        if (AUTO_LOCKING) {
            writeLock();
        }
    }

    protected void autoWriteUnlock() {
        if (AUTO_LOCKING) {
            writeUnlock();
        }
    }

    public boolean isDirected() {
        return edgeStore.isDirectedGraph();
    }

    public boolean isUndirected() {
        return edgeStore.isUndirectedGraph();
    }

    public boolean isMixed() {
        return edgeStore.isMixedGraph();
    }

    protected EdgeIterableWrapper getEdgeIterableWrapper(EdgeIterator edgeIterator) {
        return new EdgeIterableWrapper(edgeIterator);
    }

    protected NodeIterableWrapper getNodeIterableWrapper(NodeIterator nodeIterator) {
        return new NodeIterableWrapper(nodeIterator);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.nodeStore != null ? this.nodeStore.hashCode() : 0);
        hash = 29 * hash + (this.edgeStore != null ? this.edgeStore.hashCode() : 0);
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
        return true;
    }

    protected class NodeIterableWrapper implements NodeIterable {

        protected final NodeIterator iterator;

        public NodeIterableWrapper(NodeIterator iterator) {
            this.iterator = iterator;
        }

        @Override
        public NodeIterator iterator() {
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
        public void doBreak() {
            autoReadUnlock();
        }
    }

    protected class EdgeIterableWrapper implements EdgeIterable {

        protected final EdgeIterator iterator;

        public EdgeIterableWrapper(EdgeIterator iterator) {
            this.iterator = iterator;
        }

        @Override
        public EdgeIterator iterator() {
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
        public void doBreak() {
            autoReadUnlock();
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
    }
}
