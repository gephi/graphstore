package org.gephi.graph.impl;

import java.util.function.Predicate;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.gephi.graph.api.SpatialIndex;

/**
 * Graph spatial indexing interface.
 *
 * @author Eduardo Ramos
 */
public class SpatialIndexImpl implements SpatialIndex {

    private final GraphStore store;
    protected final NodesQuadTree nodesTree;

    public SpatialIndexImpl(GraphStore store) {
        this.store = store;
        float boundaries = GraphStoreConfiguration.SPATIAL_INDEX_DIMENSION_BOUNDARY;
        this.nodesTree = new NodesQuadTree(store, new Rect2D(-boundaries, -boundaries, boundaries, boundaries));
    }

    @Override
    public NodeIterable getNodesInArea(Rect2D rect) {
        return nodesTree.getNodes(rect, false);
    }

    @Override
    public NodeIterable getApproximateNodesInArea(Rect2D rect) {
        return nodesTree.getNodes(rect, true);
    }

    @Override
    public EdgeIterable getEdgesInArea(Rect2D rect) {
        return nodesTree.getEdges(rect, false);
    }

    @Override
    public EdgeIterable getApproximateEdgesInArea(Rect2D rect) {
        return nodesTree.getEdges(rect, true);
    }

    public NodeIterable getNodesInArea(Rect2D rect, Predicate<? super Node> predicate) {
        return nodesTree.getNodes(rect, false, predicate);
    }

    public NodeIterable getApproximateNodesInArea(Rect2D rect, Predicate<? super Node> predicate) {
        return nodesTree.getNodes(rect, true, predicate);
    }

    public EdgeIterable getEdgesInArea(Rect2D rect, Predicate<? super Edge> predicate) {
        return nodesTree.getEdges(rect, false, predicate);
    }

    public EdgeIterable getApproximateEdgesInArea(Rect2D rect, Predicate<? super Edge> predicate) {
        return nodesTree.getEdges(rect, true, predicate);
    }

    protected void clearNodes() {
        nodesTree.clear();
    }

    protected void addNode(final NodeImpl node) {
        nodesTree.addNode(node);
    }

    protected void removeNode(final NodeImpl node) {
        nodesTree.removeNode(node);
    }

    protected void moveNode(final NodeImpl node) {
        final float x = node.x();
        final float y = node.y();
        final float size = node.size();

        final float minX = x - size;
        final float minY = y - size;
        final float maxX = x + size;
        final float maxY = y + size;

        nodesTree.updateNode(node, minX, minY, maxX, maxY);
    }

    @Override
    public Rect2D getBoundaries() {
        return nodesTree.getBoundaries();
    }

    public Rect2D getBoundaries(Predicate<? super Node> predicate) {
        return nodesTree.getBoundaries(predicate);
    }

}
