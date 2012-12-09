package org.gephi.graph.store;

import java.util.Collection;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.EdgeIterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.NodeIterator;
import org.gephi.graph.api.UndirectedSubgraph;

/**
 *
 * @author mbastian
 */
public class GraphViewDecorator implements DirectedSubgraph, UndirectedSubgraph {

    protected final boolean undirected;
    protected final GraphViewImpl view;
    protected final GraphStore graphStore;

    public GraphViewDecorator(GraphStore graphStore, GraphViewImpl view, boolean undirected) {
        this.graphStore = graphStore;
        this.view = view;
        this.undirected = undirected;
    }

    @Override
    public Edge getEdge(Node node1, Node node2, int type) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.edgeStore.get(node1, node1, type);
            if (view.containsEdge(edge)) {
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
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(graphStore.edgeStore.neighborInIterator(node)));
    }

    @Override
    public NodeIterable getPredecessors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(graphStore.edgeStore.neighborInIterator(node, type)));
    }

    @Override
    public NodeIterable getSuccessors(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(graphStore.edgeStore.neighborOutIterator(node)));
    }

    @Override
    public NodeIterable getSuccessors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(graphStore.edgeStore.neighborOutIterator(node, type)));
    }

    @Override
    public EdgeIterable getInEdges(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeInIterator(node)));
    }

    @Override
    public EdgeIterable getInEdges(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeInIterator(node, type)));
    }

    @Override
    public EdgeIterable getOutEdges(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeOutIterator(node)));
    }

    @Override
    public EdgeIterable getOutEdges(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeOutIterator(node, type)));
    }

    @Override
    public boolean isAdjacent(Node source, Node target) {
        checkValidInViewNodeObject(source);
        checkValidInViewNodeObject(target);
        graphStore.autoReadLock();
        try {
            return graphStore.edgeStore.isAdjacent(source, target);
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
            return graphStore.edgeStore.isAdjacent(source, target, type);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean addEdge(Edge edge) {
        graphStore.autoWriteLock();
        try {
            return view.addEdge(edge);
        } finally {
            graphStore.autoWriteUnlock();
        }

    }

    @Override
    public boolean addNode(Node node) {
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
        graphStore.autoWriteLock();
        try {
            return view.removeEdge(edge);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeNode(Node node) {
        graphStore.autoWriteLock();
        try {
            return view.removeNode(node);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeEdgeAll(Collection<? extends Edge> edges) {
        graphStore.autoWriteLock();
        try {
            return view.removeEdgeAll(edges);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeNodeAll(Collection<? extends Node> nodes) {
        graphStore.autoWriteLock();
        try {
            return view.removeNodeAll(nodes);
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
            if (view.containsNode(node)) {
                return node;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public Edge getEdge(Object id) {
        graphStore.autoReadLock();
        try {
            EdgeImpl edge = graphStore.getEdge(id);
            if (view.containsEdge(edge)) {
                return edge;
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public NodeIterable getNodes() {
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(graphStore.nodeStore.iterator()));
    }

    @Override
    public EdgeIterable getEdges() {
        if (undirected) {
            return graphStore.getEdgeIterableWrapper(new UndirectedEdgeViewIterator(graphStore.edgeStore.iterator()));
        } else {
            return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.iterator()));
        }
    }

    @Override
    public NodeIterable getNeighbors(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(graphStore.edgeStore.neighborIterator(node)));
    }

    @Override
    public NodeIterable getNeighbors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(graphStore.edgeStore.neighborIterator(node, type)));
    }

    @Override
    public EdgeIterable getEdges(Node node) {
        checkValidInViewNodeObject(node);
        if (undirected) {
            return graphStore.getEdgeIterableWrapper(new UndirectedEdgeViewIterator(graphStore.edgeStore.edgeIterator(node)));
        } else {
            return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeIterator(node)));
        }
    }

    @Override
    public EdgeIterable getEdges(Node node, int type) {
        checkValidInViewNodeObject(node);
        if (undirected) {
            return graphStore.getEdgeIterableWrapper(new UndirectedEdgeViewIterator(graphStore.edgeStore.edgeIterator(node, type)));
        } else {
            return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.edgeIterator(node, type)));
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
        return graphStore.getOpposite(node, edge);
    }

    @Override
    public int getDegree(Node node) {
        if (undirected) {
            int count = 0;
            EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                EdgeImpl edge = itr.next();
                if (!isUndirectedToIgnore(edge) && view.containsEdge(edge)) {
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
            for (; itr.hasNext();) {
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
            for (; itr.hasNext();) {
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
    public void writeUnlock() {
        graphStore.lock.writeUnlock();
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

    boolean isUndirectedToIgnore(final EdgeImpl edge) {
        if (edge.isMutual() && edge.source.storeId < edge.target.storeId) {
            if (view.containsEdge(graphStore.edgeStore.get(edge.target, edge.source, edge.type))) {
                return true;
            }
        }
        return false;
    }

    protected final class NodeViewIterator implements NodeIterator {

        private final NodeIterator nodeIterator;
        private NodeImpl pointer;

        public NodeViewIterator(NodeIterator nodeIterator) {
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

    protected final class EdgeViewIterator implements EdgeIterator {

        private final EdgeIterator edgeIterator;
        private EdgeImpl pointer;

        public EdgeViewIterator(EdgeIterator edgeIterator) {
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

    protected final class UndirectedEdgeViewIterator implements EdgeIterator {

        protected final EdgeIterator itr;
        protected EdgeImpl pointer;

        public UndirectedEdgeViewIterator(EdgeIterator itr) {
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
}
