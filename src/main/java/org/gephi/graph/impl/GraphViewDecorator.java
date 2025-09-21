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
package org.gephi.graph.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.ConcurrentModificationException;
import java.util.function.Consumer;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.gephi.graph.api.SpatialIndex;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.UndirectedSubgraph;

public class GraphViewDecorator implements DirectedSubgraph, UndirectedSubgraph, SpatialIndex {

    protected final boolean undirected;
    protected final GraphViewImpl view;
    protected final GraphStore graphStore;

    public GraphViewDecorator(GraphStore graphStore, GraphViewImpl view, boolean undirected) {
        this.graphStore = graphStore;
        this.view = view;
        this.undirected = undirected;
    }

    @Override
    public Edge getEdge(Node node1, Node node2) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.get(node1, node2, undirected);
            if (edge != null && view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public EdgeIterable getEdges(Node node1, Node node2) {
        return new EdgeIterableWrapper(
                () -> new EdgeViewIterator(graphStore.edgeStore.getAll(node1, node2, undirected)),
                graphStore.getAutoLock());
    }

    @Override
    public Edge getEdge(Node node1, Node node2, int type) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.get(node1, node2, type, undirected);
            if (edge != null && view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public EdgeIterable getEdges(Node node1, Node node2, int type) {
        return new EdgeIterableWrapper(
                () -> new EdgeViewIterator(graphStore.edgeStore.getAll(node1, node2, type, undirected)),
                graphStore.getAutoLock());
    }

    @Override
    public Edge getMutualEdge(Edge e) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.getMutualEdge(e);
            if (edge != null && view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public NodeIterable getPredecessors(Node node) {
        checkValidInViewNodeObject(node);
        return new NodeIterableWrapper(() -> new NeighborsIterator((NodeImpl) node,
                new EdgeViewIterator(graphStore.edgeStore.edgeInIterator(node))), graphStore.getAutoLock());
    }

    @Override
    public NodeIterable getPredecessors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return new NodeIterableWrapper(
                () -> new NeighborsIterator((NodeImpl) node,
                        new EdgeViewIterator(graphStore.edgeStore.edgeInIterator(node, type))),
                graphStore.getAutoLock());
    }

    @Override
    public NodeIterable getSuccessors(Node node) {
        checkValidInViewNodeObject(node);
        return new NodeIterableWrapper(() -> new NeighborsIterator((NodeImpl) node,
                new EdgeViewIterator(graphStore.edgeStore.edgeOutIterator(node))), graphStore.getAutoLock());
    }

    @Override
    public NodeIterable getSuccessors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return new NodeIterableWrapper(
                () -> new NeighborsIterator((NodeImpl) node,
                        new EdgeViewIterator(graphStore.edgeStore.edgeOutIterator(node, type))),
                graphStore.getAutoLock());
    }

    @Override
    public EdgeIterable getInEdges(Node node) {
        checkValidInViewNodeObject(node);
        return new EdgeIterableWrapper(() -> new EdgeViewIterator(graphStore.edgeStore.edgeInIterator(node)),
                graphStore.getAutoLock());
    }

    @Override
    public EdgeIterable getInEdges(Node node, int type) {
        checkValidInViewNodeObject(node);
        return new EdgeIterableWrapper(() -> new EdgeViewIterator(graphStore.edgeStore.edgeInIterator(node, type)),
                graphStore.getAutoLock());
    }

    @Override
    public EdgeIterable getOutEdges(Node node) {
        checkValidInViewNodeObject(node);
        return new EdgeIterableWrapper(() -> new EdgeViewIterator(graphStore.edgeStore.edgeOutIterator(node)),
                graphStore.getAutoLock());
    }

    @Override
    public EdgeIterable getOutEdges(Node node, int type) {
        checkValidInViewNodeObject(node);
        return new EdgeIterableWrapper(() -> new EdgeViewIterator(graphStore.edgeStore.edgeOutIterator(node, type)),
                graphStore.getAutoLock());
    }

    @Override
    public boolean isAdjacent(Node source, Node target) {
        checkValidInViewNodeObject(source);
        checkValidInViewNodeObject(target);
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.get(source, target, undirected);
            return edge != null && view.containsEdge(edge);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean isAdjacent(Node source, Node target, int type) {
        checkValidInViewNodeObject(source);
        checkValidInViewNodeObject(target);
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.get(source, target, type, undirected);
            return edge != null && view.containsEdge(edge);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean addEdge(Edge edge) {
        checkValidEdgeObject(edge);
        graphStore.autoWriteLock();
        try {
            return view.addEdge(edge);
        } finally {
            graphStore.autoWriteUnlock();
        }

    }

    @Override
    public boolean addNode(Node node) {
        checkValidNodeObject(node);
        graphStore.autoWriteLock();
        try {
            return view.addNode(node);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean addAllEdges(Collection<? extends Edge> edges) {
        graphStore.autoWriteLock();
        try {
            return view.addAllEdges(edges);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean addAllNodes(Collection<? extends Node> nodes) {
        graphStore.autoWriteLock();
        try {
            return view.addAllNodes(nodes);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeEdge(Edge edge) {
        checkValidEdgeObject(edge);
        graphStore.autoWriteLock();
        try {
            return view.removeEdge(edge);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeNode(Node node) {
        checkValidNodeObject(node);
        graphStore.autoWriteLock();
        try {
            return view.removeNode(node);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Edge> edges) {
        graphStore.autoWriteLock();
        try {
            return view.removeEdgeAll(edges);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeAllNodes(Collection<? extends Node> nodes) {
        graphStore.autoWriteLock();
        try {
            return view.removeNodeAll(nodes);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean retainNodes(Collection<? extends Node> nodes) {
        graphStore.autoWriteLock();
        try {
            return view.retainNodes(nodes);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean retainEdges(Collection<? extends Edge> edges) {
        graphStore.autoWriteLock();
        try {
            return view.retainEdges(edges);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean contains(Node node) {
        checkValidNodeObject(node);
        graphStore.autoReadLock();
        try {
            return view.containsNode((NodeImpl) node);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean contains(Edge edge) {
        checkValidEdgeObject(edge);
        graphStore.autoReadLock();
        try {
            return view.containsEdge((EdgeImpl) edge);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public Node getNode(Object id) {
        graphStore.autoReadLock();
        try {
            NodeImpl node = graphStore.getNode(id);
            if (node != null && view.containsNode(node)) {
                return node;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public Node getNodeByStoreId(int id) {
        graphStore.autoReadLock();
        try {
            NodeImpl node = graphStore.getNodeByStoreId(id);
            if (node != null && view.containsNode(node)) {
                return node;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean hasNode(final Object id) {
        return getNode(id) != null;
    }

    @Override
    public Edge getEdge(Object id) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.getEdge(id);
            if (edge != null && view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public Edge getEdgeByStoreId(int id) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.getEdgeByStoreId(id);
            if (edge != null && view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean hasEdge(final Object id) {
        return getEdge(id) != null;
    }

    @Override
    public NodeIterable getNodes() {
        return new NodeIterableWrapper(() -> new NodeViewIterator(graphStore.nodeStore.iterator()),
            NodeViewSpliterator::new, graphStore.getAutoLock());
    }

    @Override
    public EdgeIterable getEdges() {
        if (undirected) {
            return new EdgeIterableWrapper(() -> new UndirectedEdgeViewIterator(graphStore.edgeStore.iterator()),
                    () -> graphStore.edgeStore
                            .newFilteredSpliterator(e -> view.containsEdge(e) && !isUndirectedToIgnore(e)),
                    graphStore.getAutoLock());
        } else {
            return new EdgeIterableWrapper(() -> new EdgeViewIterator(graphStore.edgeStore.iterator()),
                    () -> graphStore.edgeStore.newFilteredSpliterator(view::containsEdge), graphStore.getAutoLock());
        }
    }

    @Override
    public EdgeIterable getEdges(int type) {
        if (undirected) {
            return new EdgeIterableWrapper(
                    () -> new UndirectedEdgeViewIterator(graphStore.edgeStore.iteratorType(type, undirected)),
                    () -> graphStore.edgeStore.newFilteredSpliterator(e -> e.getType() == type && view
                            .containsEdge(e) && !isUndirectedToIgnore(e)),
                    graphStore.getAutoLock());
        } else {
            return new EdgeIterableWrapper(
                    () -> new EdgeViewIterator(graphStore.edgeStore.iteratorType(type, undirected)),
                    () -> graphStore.edgeStore.newFilteredSpliterator(e -> e.getType() == type && view.containsEdge(e)),
                    graphStore.getAutoLock());
        }
    }

    @Override
    public EdgeIterable getSelfLoops() {
        return new EdgeIterableWrapper(() -> new EdgeViewIterator(graphStore.edgeStore.iteratorSelfLoop()),
                () -> graphStore.edgeStore.newFilteredSpliterator(e -> e.isSelfLoop() && view.containsEdge(e)),
                graphStore.getAutoLock());
    }

    @Override
    public NodeIterable getNeighbors(Node node) {
        checkValidInViewNodeObject(node);
        return new NodeIterableWrapper(
                () -> new NeighborsIterator((NodeImpl) node,
                        new UndirectedEdgeViewIterator(graphStore.edgeStore.edgeIterator(node))),
                graphStore.getAutoLock());
    }

    @Override
    public NodeIterable getNeighbors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return new NodeIterableWrapper(
                () -> new NeighborsIterator((NodeImpl) node,
                        new UndirectedEdgeViewIterator(graphStore.edgeStore.edgeIterator(node, type))),
                graphStore.getAutoLock());
    }

    @Override
    public EdgeIterable getEdges(Node node) {
        checkValidInViewNodeObject(node);
        if (undirected) {
            return new EdgeIterableWrapper(
                    () -> new UndirectedEdgeViewIterator(graphStore.edgeStore.edgeIterator(node)),
                    graphStore.getAutoLock());
        } else {
            return new EdgeIterableWrapper(() -> new EdgeViewIterator(graphStore.edgeStore.edgeIterator(node)),
                    graphStore.getAutoLock());
        }
    }

    @Override
    public EdgeIterable getEdges(Node node, int type) {
        checkValidInViewNodeObject(node);
        if (undirected) {
            return new EdgeIterableWrapper(
                    () -> new UndirectedEdgeViewIterator(graphStore.edgeStore.edgeIterator(node, type)),
                    graphStore.getAutoLock());
        } else {
            return new EdgeIterableWrapper(() -> new EdgeViewIterator(graphStore.edgeStore.edgeIterator(node, type)),
                    graphStore.getAutoLock());
        }
    }

    @Override
    public int getNodeCount() {
        return view.getNodeCount();
    }

    @Override
    public int getEdgeCount() {
        if (undirected) {
            return view.getUndirectedEdgeCount();
        } else {
            return view.getEdgeCount();
        }
    }

    @Override
    public int getEdgeCount(int type) {
        if (undirected) {
            return view.getUndirectedEdgeCount(type);
        } else {
            return view.getEdgeCount(type);
        }
    }

    @Override
    public Node getOpposite(Node node, Edge edge) {
        checkValidInViewNodeObject(node);
        checkValidInViewEdgeObject(edge);

        return graphStore.getOpposite(node, edge);
    }

    @Override
    public int getDegree(Node node) {
        if (undirected) {
            int count = 0;
            EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                EdgeImpl edge = itr.next();
                if (view.containsEdge(edge) && !isUndirectedToIgnore(edge)) {
                    count++;
                    if (edge.isSelfLoop()) {
                        count++;
                    }
                }
            }
            return count;
        } else {
            int count = 0;
            EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                EdgeImpl edge = itr.next();
                if (view.containsEdge(edge)) {
                    count++;
                    if (edge.isSelfLoop()) {
                        count++;
                    }
                }
            }
            return count;
        }
    }

    @Override
    public int getInDegree(Node node) {
        int count = 0;
        EdgeStore.EdgeInIterator itr = graphStore.edgeStore.edgeInIterator(node);
        while (itr.hasNext()) {
            if (view.containsEdge(itr.next())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getOutDegree(Node node) {
        int count = 0;
        EdgeStore.EdgeOutIterator itr = graphStore.edgeStore.edgeOutIterator(node);
        while (itr.hasNext()) {
            if (view.containsEdge(itr.next())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isSelfLoop(Edge edge) {
        return edge.isSelfLoop();
    }

    @Override
    public boolean isDirected(Edge edge) {
        return edge.isDirected();
    }

    @Override
    public boolean isIncident(Edge edge1, Edge edge2) {
        graphStore.autoReadLock();
        try {
            checkValidInViewEdgeObject(edge1);
            checkValidInViewEdgeObject(edge2);

            return graphStore.edgeStore.isIncident((EdgeImpl) edge1, (EdgeImpl) edge2);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean isIncident(final Node node, final Edge edge) {
        graphStore.autoReadLock();
        try {
            checkValidInViewNodeObject(node);
            checkValidInViewEdgeObject(edge);

            return graphStore.edgeStore.isIncident((NodeImpl) node, (EdgeImpl) edge);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public void clearEdges(Node node) {
        graphStore.autoWriteLock();
        try {
            EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                EdgeImpl edge = itr.next();
                view.removeEdge(edge);
            }
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void clearEdges(Node node, int type) {
        graphStore.autoWriteLock();
        try {
            EdgeStore.EdgeTypeInOutIterator itr = graphStore.edgeStore.edgeIterator(node, type);
            while (itr.hasNext()) {
                EdgeImpl edge = itr.next();
                view.removeEdge(edge);
            }
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public void clearEdges() {
        view.clearEdges();
    }

    @Override
    public Object getAttribute(String key) {
        return view.attributes.getValue(key);
    }

    @Override
    public Object getAttribute(String key, double timestamp) {
        return view.attributes.getValue(key, timestamp);
    }

    @Override
    public Object getAttribute(String key, Interval interval) {
        return view.attributes.getValue(key, interval);
    }

    @Override
    public Set<String> getAttributeKeys() {
        return view.attributes.getKeys();
    }

    @Override
    public void setAttribute(String key, Object value) {
        view.attributes.setValue(key, value);
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        view.attributes.setValue(key, value, timestamp);
    }

    @Override
    public void setAttribute(String key, Object value, Interval interval) {
        view.attributes.setValue(key, value, interval);
    }

    @Override
    public void removeAttribute(String key) {
        view.attributes.removeValue(key);
    }

    @Override
    public void removeAttribute(String key, double timestamp) {
        view.attributes.removeValue(key, timestamp);
    }

    @Override
    public void removeAttribute(String key, Interval interval) {
        view.attributes.removeValue(key, interval);
    }

    @Override
    public GraphModel getModel() {
        return graphStore.graphModel;
    }

    @Override
    public int getVersion() {
        return view.getVersion();
    }

    @Override
    public boolean isDirected() {
        return graphStore.isDirected();
    }

    @Override
    public boolean isUndirected() {
        return graphStore.isUndirected();
    }

    @Override
    public boolean isMixed() {
        return graphStore.isMixed();
    }

    @Override
    public void readLock() {
        graphStore.lock.readLock();
    }

    @Override
    public void readUnlock() {
        graphStore.lock.readUnlock();
    }

    @Override
    public void readUnlockAll() {
        graphStore.lock.readUnlockAll();
    }

    @Override
    public void writeLock() {
        graphStore.lock.writeLock();
    }

    @Override
    public GraphLockImpl getLock() {
        return graphStore.lock;
    }

    @Override
    public void writeUnlock() {
        graphStore.lock.writeUnlock();
    }

    @Override
    public GraphView getView() {
        return view;
    }

    @Override
    public void fill() {
        graphStore.autoWriteLock();
        try {
            view.fill();
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void union(Subgraph subGraph) {
        checkValidViewObject(subGraph.getView());

        graphStore.autoWriteLock();
        try {
            view.union((GraphViewImpl) subGraph.getView());
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void intersection(Subgraph subGraph) {
        checkValidViewObject(subGraph.getView());

        graphStore.autoWriteLock();
        try {
            view.intersection((GraphViewImpl) subGraph.getView());
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void not() {
        graphStore.autoWriteLock();
        try {
            view.not();
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public Graph getRootGraph() {
        return graphStore;
    }

    @Override
    public SpatialIndex getSpatialIndex() {
        if (graphStore.spatialIndex == null) {
            throw new UnsupportedOperationException("Spatial index is disabled (from Configuration)");
        }
        return this;
    }

    void checkWriteLock() {
        if (graphStore.lock != null) {
            graphStore.lock.checkHoldWriteLock();
        }
    }

    void checkValidNodeObject(final Node n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (!(n instanceof NodeImpl)) {
            throw new ClassCastException("Object must be a NodeImpl object");
        }
        if (((NodeImpl) n).storeId == NodeStore.NULL_ID) {
            throw new IllegalArgumentException("Node should belong to a store");
        }
    }

    void checkValidInViewNodeObject(final Node n) {
        checkValidNodeObject(n);

        if (!view.containsNode((NodeImpl) n)) {
            throw new RuntimeException("Node doesn't belong to this view");
        }
    }

    void checkValidEdgeObject(final Edge n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (!(n instanceof EdgeImpl)) {
            throw new ClassCastException("Object must be a EdgeImpl object");
        }
        if (((EdgeImpl) n).storeId == EdgeStore.NULL_ID) {
            throw new IllegalArgumentException("Edge should belong to a store");
        }
    }

    void checkValidInViewEdgeObject(final Edge e) {
        checkValidEdgeObject(e);

        if (!view.containsEdge((EdgeImpl) e)) {
            throw new RuntimeException("Edge doesn't belong to this view");
        }
    }

    void checkValidViewObject(final GraphView view) {
        if (view == null) {
            throw new NullPointerException();
        }
        if (!(view instanceof GraphViewImpl)) {
            throw new ClassCastException("Object must be a GraphViewImpl object");
        }
        if (((GraphViewImpl) view).graphStore != graphStore) {
            throw new RuntimeException("The view doesn't belong to this store");
        }
    }

    boolean isUndirectedToIgnore(final EdgeImpl edge) {
        if (edge.isMutual() && edge.source.storeId < edge.target.storeId) {
            if (view.containsEdge(graphStore.edgeStore.get(edge.target, edge.source, edge.type, false))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NodeIterable getNodesInArea(Rect2D rect) {
        if (graphStore.spatialIndex == null) {
            throw new UnsupportedOperationException("Spatial index is disabled (from Configuration)");
        }
        return new NodeIterableWrapper(
                () -> new NodeViewIterator(graphStore.spatialIndex.getNodesInArea(rect).iterator()),
                graphStore.spatialIndex.nodesTree.lock);
    }

    @Override
    public EdgeIterable getEdgesInArea(Rect2D rect) {
        if (graphStore.spatialIndex == null) {
            throw new UnsupportedOperationException("Spatial index is disabled (from Configuration)");
        }
        return new EdgeIterableWrapper(
                () -> new EdgeViewIterator(graphStore.spatialIndex.getEdgesInArea(rect).iterator()),
                graphStore.spatialIndex.nodesTree.lock);
    }

    @Override
    public Rect2D getBoundaries() {
        graphStore.autoReadLock();
        try {
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;

            boolean hasNodes = false;

            // Iterate only through nodes visible in this view
            for (Node node : getNodes()) {
                hasNodes = true;
                final float x = node.x();
                final float y = node.y();
                final float size = node.size();

                final float nodeMinX = x - size;
                final float nodeMinY = y - size;
                final float nodeMaxX = x + size;
                final float nodeMaxY = y + size;

                if (nodeMinX < minX)
                    minX = nodeMinX;
                if (nodeMinY < minY)
                    minY = nodeMinY;
                if (nodeMaxX > maxX)
                    maxX = nodeMaxX;
                if (nodeMaxY > maxY)
                    maxY = nodeMaxY;
            }

            return hasNodes ? new Rect2D(minX, minY, maxX, maxY) : new Rect2D(Float.NEGATIVE_INFINITY,
                    Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    private final class NodeViewSpliterator implements Spliterator<Node> {

        private final int endBlockExclusive;
        private int blockIndex;
        private int indexInBlock;
        private NodeImpl[] currentArray;
        private int currentLength;
        private final int expectedVersion;
        private int totalSize;
        private int consumed;

        NodeViewSpliterator() {
            this(0, graphStore.nodeStore.blocksCount);
        }

        NodeViewSpliterator(int startBlock, int endBlockExclusive) {
            this.blockIndex = startBlock;
            this.endBlockExclusive = endBlockExclusive;
            this.expectedVersion = graphStore.version != null ? graphStore.version.getNodeVersion() : 0;
            this.consumed = 0;

            // Use the view's node count for exact sizing
            // Use the total store size for the root spliterator (covering all blocks)
            if (startBlock == 0 && endBlockExclusive == graphStore.nodeStore.blocksCount) {
                this.totalSize = view.getNodeCount();
            } else {
                // For split spliterators, compute proportionally
                this.totalSize = computeSizeEstimate(startBlock, endBlockExclusive);
            }

            if (startBlock < endBlockExclusive) {
                NodeStore.NodeBlock b = graphStore.nodeStore.blocks[startBlock];
                currentArray = b.backingArray;
                currentLength = b.nodeLength;
                indexInBlock = 0;
            } else {
                currentArray = null;
                currentLength = 0;
                indexInBlock = 0;
            }
        }

        private void advanceBlock() {
            blockIndex++;
            if (blockIndex < endBlockExclusive) {
                NodeStore.NodeBlock b = graphStore.nodeStore.blocks[blockIndex];
                currentArray = b.backingArray;
                currentLength = b.nodeLength;
                indexInBlock = 0;
            } else {
                currentArray = null;
                currentLength = 0;
                indexInBlock = 0;
            }
        }

        private void checkForComodification() {
            if (graphStore.version != null && expectedVersion != graphStore.version.getNodeVersion()) {
                throw new ConcurrentModificationException();
            }
        }

        private int computeSizeEstimate(int start, int end) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                NodeStore.NodeBlock b = graphStore.nodeStore.blocks[i];
                if (b != null) {
                    // Exact count: nodeLength minus garbageLength
                    sum += (b.nodeLength - b.garbageLength);
                }
            }
            if (sum > 0) {
                // Scale by view ratio to estimate number of nodes in view
                double viewRatio = (double) view.getNodeCount() / graphStore.nodeStore.size;
                sum = (int) Math.round(sum * viewRatio);
            }
            return sum;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Node> action) {
            checkForComodification();
            while (currentArray != null) {
                while (indexInBlock < currentLength) {
                    NodeImpl n = currentArray[indexInBlock++];
                    if (n != null && view.containsNode(n)) {
                        consumed++;
                        action.accept(n);
                        return true;
                    }
                }
                advanceBlock();
            }
            return false;
        }

        @Override
        public Spliterator<Node> trySplit() {
            // Only split at block boundaries to preserve encounter order
            if (indexInBlock != 0) {
                return null;
            }

            int currentPos = blockIndex;
            int remainingBlocks = endBlockExclusive - currentPos;

            if (remainingBlocks <= 1) {
                return null;
            }

            int mid = currentPos + remainingBlocks / 2;

            // Create left half
            NodeViewSpliterator left = new NodeViewSpliterator(currentPos, mid);

            // Update this spliterator to become the right half
            blockIndex = mid;
            if (mid < endBlockExclusive) {
                NodeStore.NodeBlock b = graphStore.nodeStore.blocks[mid];
                currentArray = b.backingArray;
                currentLength = b.nodeLength;
                indexInBlock = 0;
            } else {
                currentArray = null;
                currentLength = 0;
                indexInBlock = 0;
            }

            // Update this spliterator size
            this.totalSize = Math.max(0, totalSize - left.totalSize);

            return left;
        }

        @Override
        public long estimateSize() {
            // Use the exact view size minus what we've consumed
            long remaining = totalSize - consumed;
            return remaining < 0 ? 0 : remaining;
        }

        @Override
        public int characteristics() {
            // SIZED because we know the exact count from view.getNodeCount()
            // But not SUBSIZED because splits can't guarantee exact size distribution
            return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SIZED;
        }
    }

    protected final class NodeViewIterator implements Iterator<Node> {

        private final Iterator<Node> nodeIterator;
        private NodeImpl pointer;

        public NodeViewIterator(Iterator<Node> nodeIterator) {
            this.nodeIterator = nodeIterator;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                if (!nodeIterator.hasNext()) {
                    return false;
                }
                pointer = (NodeImpl) nodeIterator.next();
                if (!view.containsNode(pointer)) {
                    pointer = null;
                }
            }
            return true;
        }

        @Override
        public Node next() {
            return pointer;
        }

        @Override
        public void remove() {
            checkWriteLock();
            removeNode(pointer);
        }
    }

    protected final class EdgeViewIterator implements Iterator<Edge> {

        private final Iterator<Edge> edgeIterator;
        private EdgeImpl pointer;

        public EdgeViewIterator(Iterator<Edge> edgeIterator) {
            this.edgeIterator = edgeIterator;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                if (!edgeIterator.hasNext()) {
                    return false;
                }
                pointer = (EdgeImpl) edgeIterator.next();
                if (!view.containsEdge(pointer)) {
                    pointer = null;
                }
            }
            return true;
        }

        @Override
        public Edge next() {
            return pointer;
        }

        @Override
        public void remove() {
            checkWriteLock();
            removeEdge(pointer);
        }
    }

    protected final class UndirectedEdgeViewIterator implements Iterator<Edge> {

        protected final Iterator<Edge> itr;
        protected EdgeImpl pointer;

        public UndirectedEdgeViewIterator(Iterator<Edge> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null || !view.containsEdge(pointer) || isUndirectedToIgnore(pointer)) {
                if (!itr.hasNext()) {
                    return false;
                }
                pointer = (EdgeImpl) itr.next();
            }
            return true;
        }

        @Override
        public EdgeImpl next() {
            return pointer;
        }

        @Override
        public void remove() {
            itr.remove();
        }
    }

    protected static class NeighborsIterator implements Iterator<Node> {

        protected final NodeImpl node;
        protected final Iterator<Edge> itr;

        public NeighborsIterator(NodeImpl node, Iterator<Edge> itr) {
            this.node = node;
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Node next() {
            Edge e = itr.next();
            return e.getSource() == node ? e.getTarget() : e.getSource();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported for this iterator");
        }
    }
}
