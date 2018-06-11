package org.gephi.graph.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.gephi.graph.api.SpatialContext;

/**
 * Graph spatial indexing interface.
 * TODO: unit tests!!
 * TODO: measure performance loss due to having this.
 * @author Eduardo Ramos
 */
public class GraphStoreSpatialContextImpl implements SpatialContext {

    private final GraphStore store;
    private final NodesQuadTree nodesTree;
    private final EdgesQuadTree edgesTree;

    public GraphStoreSpatialContextImpl(GraphStore store) {
        this.store = store;
        this.nodesTree = new NodesQuadTree(GraphStoreConfiguration.SPATIAL_INDEX_DIMENSION_BOUNDARY);
        this.edgesTree = new EdgesQuadTree(GraphStoreConfiguration.SPATIAL_INDEX_DIMENSION_BOUNDARY);
    }

    @Override
    public NodeIterable getNodesInArea(Rect2D rect) {
        return nodesTree.getNodes(rect);
    }

    @Override
    public void getNodesInArea(Rect2D rect, Consumer<Node> callback) {
        nodesTree.getNodes(rect, callback);
    }

    @Override
    public EdgeIterable getEdgesInArea(Rect2D rect) {
        return edgesTree.getEdges(rect);
    }

    @Override
    public void getEdgesInArea(Rect2D rect, Consumer<Edge> callback) {
        edgesTree.getEdges(rect, callback);
    }

    protected void clearNodes() {
        nodesTree.clear();
    }

    protected void addNode(final Node node) {
        final float x = node.x();
        final float y = node.y();
        final float size = node.size();

        final float minX = x - size;
        final float minY = y - size;
        final float maxX = x + size;
        final float maxY = y + size;

        nodesTree.addNode(node, minX, minY, maxX, maxY);
    }

    protected void removeNode(final Node node) {
        nodesTree.removeNode(node);
    }

    protected void moveNode(final Node node) {
        final float x = node.x();
        final float y = node.y();
        final float size = node.size();

        final float minX = x - size;
        final float minY = y - size;
        final float maxX = x + size;
        final float maxY = y + size;

        nodesTree.updateNode(node, minX, minY, maxX, maxY);

        // Update node edges:
        for (Edge edge : store.getEdges(node)) {
            final Node opposite = edge.getSource() == node ? edge.getTarget() : edge.getSource();

            final float x2 = opposite.x();
            final float y2 = opposite.y();

            edgesTree.updateEdge(edge, min(x, x2), min(y, y2), max(x, x2), max(y, y2));
        }
    }

    protected void addEdge(Edge edge) {
        final Node source = edge.getSource();
        final Node target = edge.getTarget();

        final float x1 = source.x();
        final float y1 = source.y();
        final float x2 = target.x();
        final float y2 = target.y();

        final float minX = min(x1, x2);
        final float minY = min(y1, y2);
        final float maxX = max(x1, x2);
        final float maxY = max(y1, y2);

        edgesTree.addEdge(edge, minX, minY, maxX, maxY);
    }

    protected void removeEdge(Edge edge) {
        edgesTree.removeEdge(edge);
    }

    protected void clearEdges() {
        edgesTree.clear();
    }

    private static float min(float a, float b) {
        return (a <= b) ? a : b;
    }

    private static float max(float a, float b) {
        return (a >= b) ? a : b;
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
            List<Node> list = new ArrayList<>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list.toArray(new Node[0]);
        }

        @Override
        public Collection<Node> toCollection() {
            List<Node> list = new ArrayList<>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list;
        }

        @Override
        public void doBreak() {
            if (blocking) {
                nodesTree.readUnlock();
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
            List<Edge> list = new ArrayList<>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list.toArray(new Edge[0]);
        }

        @Override
        public Collection<Edge> toCollection() {
            List<Edge> list = new ArrayList<>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list;
        }

        @Override
        public void doBreak() {
            if (blocking) {
                edgesTree.readUnlock();
            }
        }
    }
}
