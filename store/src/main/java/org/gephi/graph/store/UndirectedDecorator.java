package org.gephi.graph.store;

import java.util.Collection;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.UndirectedGraph;

/**
 *
 * @author mbastian
 */
public class UndirectedDecorator implements UndirectedGraph {

    protected final GraphStore store;

    public UndirectedDecorator(GraphStore store) {
        this.store = store;
    }

    @Override
    public boolean addEdge(Edge edge) {
        if (edge.isDirected()) {
            throw new IllegalArgumentException("Can't add a directed edge to an undirected graph");
        }
        return store.addEdge(edge);
    }

    @Override
    public boolean addNode(Node node) {
        return store.addNode(node);
    }

    @Override
    public boolean addAllEdges(Collection<? extends Edge> edges) {
        for (Edge edge : edges) {
            if (edge.isDirected()) {
                throw new IllegalArgumentException("Can't add a directed edge to an undirected graph");
            }
        }
        return store.addAllEdges(edges);
    }

    @Override
    public boolean addAllNodes(Collection<? extends Node> nodes) {
        return store.addAllNodes(nodes);
    }

    @Override
    public boolean removeEdge(Edge edge) {
        return store.removeEdge(edge);
    }

    @Override
    public boolean removeNode(Node node) {
        return store.removeNode(node);
    }

    @Override
    public boolean removeEdgeAll(Collection<? extends Edge> edges) {
        return store.removeEdgeAll(edges);
    }

    @Override
    public boolean removeNodeAll(Collection<? extends Node> nodes) {
        return store.removeNodeAll(nodes);
    }

    @Override
    public boolean contains(Node node) {
        return store.contains(node);
    }

    @Override
    public boolean contains(Edge edge) {
        return store.contains(edge);
    }

    @Override
    public Node getNode(Object id) {
        return store.getNode(id);
    }

    @Override
    public Edge getEdge(Object id) {
        return store.getEdge(id);
    }

    @Override
    public Edge getEdge(Node node1, Node node2, int type) {
        return store.getEdge(node1, node2, type);
    }

    @Override
    public NodeIterable getNodes() {
        return store.getNodes();
    }

    @Override
    public EdgeIterable getEdges() {
        return store.getEdgeIterableWrapper(store.edgeStore.iteratorUndirected());
    }
    
    @Override
    public EdgeIterable getSelfLoops() {
        return store.getEdgeIterableWrapper(store.edgeStore.iteratorSelfLoop());
    }

    @Override
    public NodeIterable getNeighbors(Node node) {
        return store.getNodeIterableWrapper(store.edgeStore.neighborIterator(node));
    }

    @Override
    public NodeIterable getNeighbors(Node node, int type) {
        return store.getNodeIterableWrapper(store.edgeStore.neighborIterator(node, type));
    }

    @Override
    public EdgeIterable getEdges(Node node) {
        return store.getEdgeIterableWrapper(store.edgeStore.edgeUndirectedIterator(node));
    }

    @Override
    public EdgeIterable getEdges(Node node, int type) {
        return store.getEdgeIterableWrapper(store.edgeStore.edgeUndirectedIterator(node, type));
    }

    @Override
    public int getNodeCount() {
        return store.getNodeCount();
    }

    @Override
    public int getEdgeCount() {
        return store.edgeStore.undirectedSize();
    }

    @Override
    public int getEdgeCount(int type) {
        store.autoReadLock();
        try {
            if (store.edgeTypeStore.contains(type)) {
                return store.edgeStore.undirectedSize(type);
            }
            return 0;
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public Node getOpposite(Node node, Edge edge) {
        return store.getOpposite(node, edge);
    }

    @Override
    public int getDegree(Node node) {
        return store.getDegree(node);
    }

    @Override
    public boolean isSelfLoop(Edge edge) {
        return store.isSelfLoop(edge);
    }

    @Override
    public boolean isDirected(Edge edge) {
        return false;
    }

    @Override
    public boolean isAdjacent(Node node1, Node node2) {
        return store.isAdjacent(node1, node2);
    }

    @Override
    public boolean isAdjacent(Node node1, Node node2, int type) {
        return store.isAdjacent(node1, node2, type);
    }

    @Override
    public boolean isIncident(Edge edge1, Edge edge2) {
        return store.isIncident(edge1, edge2);
    }

    @Override
    public boolean isIncident(Node node, Edge edge) {
        return store.isIncident(node, edge);
    }

    @Override
    public void clearEdges(Node node) {
        store.clearEdges(node);
    }

    @Override
    public void clearEdges(Node node, int type) {
        store.clearEdges(node, type);
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public void clearEdges() {
        store.clearEdges();
    }

    @Override
    public GraphView getView() {
        return store.mainGraphView;
    }

    @Override
    public void readLock() {
        store.readLock();
    }

    @Override
    public void readUnlock() {
        store.readUnlock();
    }

    @Override
    public void readUnlockAll() {
        store.readUnlockAll();
    }

    @Override
    public void writeLock() {
        store.writeLock();
    }

    @Override
    public void writeUnlock() {
        store.writeUnlock();
    }
}
