package org.gephi.graph.impl;

import java.util.Iterator;
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
        this.nodesTree = new NodesQuadTree(new Rect2D(-boundaries, -boundaries, boundaries, boundaries));
    }

    @Override
    public NodeIterable getNodesInArea(Rect2D rect) {
        return nodesTree.getNodes(rect);
    }

    @Override
    public EdgeIterable getEdgesInArea(Rect2D rect) {
        return new EdgeIterableWrapper(new EdgeIterator(rect, nodesTree.getNodes(rect).iterator()), nodesTree.lock);
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

    protected class EdgeIterator implements Iterator<Edge> {

        private final Iterator<Node> nodeItr;
        private final Rect2D rect2D;
        private Iterator<Edge> edgeItr;
        private Edge pointer;
        private Node node;

        public EdgeIterator(Rect2D rect2D, Iterator<Node> nodeIterator) {
            this.nodeItr = nodeIterator;
            this.rect2D = rect2D;

            nodesTree.readLock();
        }

        @Override
        public boolean hasNext() {
            while (pointer == null) {
                while (pointer == null && edgeItr != null && edgeItr.hasNext()) {
                    pointer = edgeItr.next();
                    if (!pointer.isSelfLoop()) {
                        Node oppositeNode = store.getOpposite(node, pointer);
                        // Skip edge - do not return same edges twice when both
                        // source and target nodes are visible
                        SpatialNodeDataImpl spatialData = ((NodeImpl) oppositeNode).getSpatialData();
                        if (oppositeNode.getStoreId() < node.getStoreId() && rect2D
                                .intersects(spatialData.minX, spatialData.minY, spatialData.maxX, spatialData.maxY)) {
                            pointer = null;
                        }
                    }
                }
                if (pointer == null) {
                    edgeItr = null;
                    if (nodeItr != null && nodeItr.hasNext()) {
                        node = nodeItr.next();
                        edgeItr = store.edgeStore.edgeIterator(node);
                    } else {
                        nodesTree.readUnlock();
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public Edge next() {
            Edge res = pointer;
            pointer = null;
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
