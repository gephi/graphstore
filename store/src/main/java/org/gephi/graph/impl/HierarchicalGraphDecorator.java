package org.gephi.graph.impl;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnIterable;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TextProperties;
import org.gephi.graph.api.UndirectedSubgraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class HierarchicalGraphDecorator implements DirectedSubgraph, UndirectedSubgraph {
    private final boolean undirected;

    private final HierarchicalGraphViewImpl view;

    private final GraphStore graphStore;

    public HierarchicalGraphDecorator(GraphStore graphStore, HierarchicalGraphViewImpl view, boolean undirected) {
        this.graphStore = graphStore;
        this.view = view;
        this.undirected = undirected;
    }

    @Override
    public Edge getEdge(Node node1, Node node2) {
        graphStore.autoReadLock();
        try {
            for (final Node n1 : view.mapWithHidden(node1)) {
                for (final Node n2 : view.mapWithHidden(node2)) {
                    EdgeImpl edge = graphStore.edgeStore.get(n1, n2, undirected);
                    if (edge != null && view.containsEdge(edge)) {
                        return decorateEdge(edge);
                    }
                }
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public Edge getEdge(Node node1, Node node2, int type) {
        graphStore.autoReadLock();
        try {
            for (final Node n1 : view.mapWithHidden(node1)) {
                for (final Node n2 : view.mapWithHidden(node2)) {
                    EdgeImpl edge = graphStore.edgeStore.get(n1, n2, type, undirected);
                    if (edge != null && view.containsEdge(edge)) {
                        return decorateEdge(edge);
                    }
                }
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public EdgeIterable getEdges(final Node node1, final Node node2) {
        List<Callable<Iterator<Edge>>> list = new ArrayList<Callable<Iterator<Edge>>>();
        for (final Node n1 : view.mapWithHidden(node1)) {
            for (final Node n2 : view.mapWithHidden(node2)) {
                list.add(new Callable<Iterator<Edge>>() {
                    @Override
                    public Iterator<Edge> call() throws Exception {
                        return graphStore.edgeStore.getAll(n1, n2, undirected);
                    }
                });
            }
        }
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(new ChainedFutureIterator<Edge>(list)));
    }

    @Override
    public EdgeIterable getEdges(final Node node1, final Node node2, final int type) {
        List<Callable<Iterator<Edge>>> list = new ArrayList<Callable<Iterator<Edge>>>();
        for (final Node n1 : view.mapWithHidden(node1)) {
            for (final Node n2 : view.mapWithHidden(node2)) {
                list.add(new Callable<Iterator<Edge>>() {
                    @Override
                    public Iterator<Edge> call() throws Exception {
                        return graphStore.edgeStore.getAll(n1, n2, type, undirected);
                    }
                });
            }
        }
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(new ChainedFutureIterator<Edge>(list)));
    }

    @Override
    public Edge getMutualEdge(Edge edge) {
        graphStore.autoReadLock();
        try {
            final Edge unpacked = undecorateEdge(edge);
            for (final Node n1 : view.mapWithHidden(unpacked.getSource())) {
                for (final Node n2 : view.mapWithHidden(unpacked.getTarget())) {
                    Edge e = graphStore.getEdge(n1, n2);
                    EdgeImpl mutual = graphStore.edgeStore.getMutualEdge(e);
                    if (mutual != null && view.containsEdge(mutual)) {
                        return decorateEdge(mutual);
                    }
                }
            }
            return null;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public NodeIterable getPredecessors(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new EdgeViewIterator(
                graphStore.edgeStore.edgeInIterator(node))));
    }

    @Override
    public NodeIterable getPredecessors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new EdgeViewIterator(
                graphStore.edgeStore.edgeInIterator(node, type))));
    }

    @Override
    public NodeIterable getSuccessors(Node node) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new EdgeViewIterator(
                graphStore.edgeStore.edgeOutIterator(node))));
    }

    @Override
    public NodeIterable getSuccessors(Node node, int type) {
        checkValidInViewNodeObject(node);
        return graphStore.getNodeIterableWrapper(new NeighborsIterator((NodeImpl) node, new EdgeViewIterator(
                graphStore.edgeStore.edgeOutIterator(node, type))));
    }

    @Override
    public EdgeIterable getInEdges(Node node) {
        checkValidInViewNodeObject(node);
        List<Callable<Iterator<Edge>>> list = new ArrayList<Callable<Iterator<Edge>>>();
        for (final Node n : view.mapWithHidden(node)) {
            list.add(new Callable<Iterator<Edge>>() {
                @Override
                public Iterator<Edge> call() throws Exception {
                    return graphStore.edgeStore.edgeInIterator(n);
                }
            });
        }
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(new ChainedFutureIterator<Edge>(list)));
    }

    @Override
    public EdgeIterable getInEdges(final Node node, final int type) {
        checkValidInViewNodeObject(node);
        List<Callable<Iterator<Edge>>> list = new ArrayList<Callable<Iterator<Edge>>>();
        for (final Node n : view.mapWithHidden(node)) {
            list.add(new Callable<Iterator<Edge>>() {
                @Override
                public Iterator<Edge> call() throws Exception {
                    return graphStore.edgeStore.edgeInIterator(n, type);
                }
            });
        }
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(new ChainedFutureIterator<Edge>(list)));
    }

    @Override
    public EdgeIterable getOutEdges(final Node node) {
        checkValidInViewNodeObject(node);
        List<Callable<Iterator<Edge>>> list = new ArrayList<Callable<Iterator<Edge>>>();
        for (final Node n : view.mapWithHidden(node)) {
            list.add(new Callable<Iterator<Edge>>() {
                @Override
                public Iterator<Edge> call() throws Exception {
                    return graphStore.edgeStore.edgeOutIterator(n);
                }
            });
        }
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(new ChainedFutureIterator<Edge>(list)));
    }

    @Override
    public EdgeIterable getOutEdges(final Node node, final int type) {
        checkValidInViewNodeObject(node);
        List<Callable<Iterator<Edge>>> list = new ArrayList<Callable<Iterator<Edge>>>();
        for (final Node n : view.mapWithHidden(node)) {
            list.add(new Callable<Iterator<Edge>>() {
                @Override
                public Iterator<Edge> call() throws Exception {
                    return graphStore.edgeStore.edgeOutIterator(n, type);
                }
            });
        }
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(new ChainedFutureIterator<Edge>(list)));
    }

    @Override
    public boolean isAdjacent(Node source, Node target) {
        checkValidInViewNodeObject(source);
        checkValidInViewNodeObject(target);
        graphStore.autoReadLock();
        try {
            for (final Node mappedSource : view.mapWithHidden(source)) {
                for (final Node mappedTarget : view.mapWithHidden(target)) {
                    EdgeImpl edge = graphStore.edgeStore.get(mappedSource, mappedTarget, undirected);
                    if (edge != null && view.containsEdge(edge)) {
                        return true;
                    }
                }
            }
            return false;
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
            for (final Node mappedSource : view.mapWithHidden(source)) {
                for (final Node mappedTarget : view.mapWithHidden(target)) {
                    EdgeImpl edge = graphStore.edgeStore.get(mappedSource, mappedTarget, type, undirected);
                    if (edge != null && view.containsEdge(edge)) {
                        return true;
                    }
                }
            }
            return false;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean addEdge(Edge edge) {
        Edge unpacked = undecorateEdge(edge);
        checkValidEdgeObject(unpacked);
        graphStore.autoWriteLock();
        try {
            return view.addEdge(unpacked);
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
            boolean updated = false;
            for (final Edge edge : edges) {
                updated |= view.addEdge(edge);
            }
            return updated;
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean addAllNodes(Collection<? extends Node> nodes) {
        graphStore.autoWriteLock();
        try {
            boolean updated = false;
            for (final Node node : nodes) {
                updated |= view.addNode(node);
            }
            return updated;
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean removeEdge(Edge edge) {
        Edge unpacked = undecorateEdge(edge);
        checkValidEdgeObject(unpacked);
        graphStore.autoWriteLock();
        try {
            return view.removeEdge(unpacked);
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
            return this.removeAllEdgesWithLock(edges.iterator());
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    private boolean removeAllEdgesWithLock(Iterator<? extends Edge> itr) {
        if (null == itr) {
            return false;
        }

        boolean updated = false;
        while (itr.hasNext()) {
            final Edge edge = itr.next();
            if (edge != null) {
                updated |= view.removeEdge(undecorateEdge(edge));
            }
        }
        return updated;
    }

    @Override
    public boolean removeAllNodes(Collection<? extends Node> nodes) {
        graphStore.autoWriteLock();
        try {
            boolean updated = false;
            for (final Node node : nodes) {
                updated |= view.removeNode(node);
            }
            return updated;
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public boolean contains(Node node) {
        checkValidNodeObject(node);
        graphStore.autoReadLock();
        try {
            return view.containsNode((NodeImpl) node) && view.visibleNode((NodeImpl) node);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean contains(Edge edge) {
        Edge unpacked = undecorateEdge(edge);
        checkValidEdgeObject(unpacked);
        graphStore.autoReadLock();
        try {
            return view.containsEdge((EdgeImpl) unpacked);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public Node getNode(Object id) {
        graphStore.autoReadLock();
        try {
            NodeImpl node = graphStore.getNode(id);
            if (node != null && view.containsNode(node) && view.visibleNode(node)) {
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
                return decorateEdge(edge);
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
    public EdgeIterable getSelfLoops() {
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(graphStore.edgeStore.iteratorSelfLoop()));
    }

    @Override
    public NodeIterable getNeighbors(final Node node) {
        checkValidInViewNodeObject(node);
        Set<Node> set = new HashSet<Node>();
        Set<Node> mapped = new HashSet<Node>();
        mapped.add(node);
        mapped.addAll(view.mapWithHidden(node));
        for (Node n : mapped) {
            for (Edge edge : this.getEdges(n)) {
                set.add(edge.getSource());
                set.add(edge.getTarget());
            }
        }
        set.removeAll(mapped);
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(set.iterator()));
    }

    @Override
    public NodeIterable getNeighbors(final Node node, final int type) {
        checkValidInViewNodeObject(node);
        Set<Node> set = new HashSet<Node>();
        Set<Node> mapped = new HashSet<Node>();
        mapped.add(node);
        mapped.addAll(view.mapWithHidden(node));
        for (Node n : mapped) {
            for (Edge edge : this.getEdges(n, type)) {
                set.add(edge.getSource());
                set.add(edge.getTarget());
            }
        }
        set.removeAll(mapped);
        return graphStore.getNodeIterableWrapper(new NodeViewIterator(set.iterator()));
    }

    @Override
    public EdgeIterable getEdges(Node node) {
        checkValidInViewNodeObject(node);
        List<Callable<Iterator<Edge>>> list = new ArrayList<Callable<Iterator<Edge>>>();
        for (final Node n : view.mapWithHidden(node)) {
            list.add(new Callable<Iterator<Edge>>() {
                @Override
                public Iterator<Edge> call() throws Exception {
                    if (undirected) {
                        return new UndirectedEdgeViewIterator(graphStore.edgeStore.edgeIterator(n));
                    } else {
                        return graphStore.edgeStore.edgeIterator(n);
                    }
                }
            });
        }
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(new ChainedFutureIterator<Edge>(list)));
    }

    @Override
    public EdgeIterable getEdges(final Node node, final int type) {
        checkValidInViewNodeObject(node);
        List<Callable<Iterator<Edge>>> list = new ArrayList<Callable<Iterator<Edge>>>();
        for (final Node n : view.mapWithHidden(node)) {
            list.add(new Callable<Iterator<Edge>>() {
                @Override
                public Iterator<Edge> call() throws Exception {
                    if (undirected) {
                        return new UndirectedEdgeViewIterator(graphStore.edgeStore.edgeIterator(n, type));
                    } else {
                        return graphStore.edgeStore.edgeIterator(n, type);
                    }
                }
            });
        }
        return graphStore.getEdgeIterableWrapper(new EdgeViewIterator(new ChainedFutureIterator<Edge>(list)));
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
        Edge e = undecorateEdge(edge);
        Node n1 = view.mapToVisible(e.getSource());
        Node n2 = view.mapToVisible(e.getTarget());
        checkValidInViewNodeObject(n1);
        checkValidInViewNodeObject(n2);
        checkValidInViewNodeObject(node);
        checkValidInViewEdgeObject(e);
        if (n1.equals(node)) {
            return n2;
        } else if (n2.equals(node)) {
            return n1;
        } else {
            return null;
        }
    }

    @Override
    public int getDegree(final Node node) {
        if (!this.contains(node)) {
            return 0;
        }

        if (undirected) {
            int count = 0;
            for (final Node related : view.mapWithHidden(node)) {
                EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(related);
                while (itr.hasNext()) {
                    EdgeImpl edge = itr.next();
                    if (view.containsEdge(edge) && !isUndirectedToIgnore(edge)) {
                        count++;
                        if (edge.isSelfLoop()) {
                            count++;
                        }
                    }
                }
            }
            return count;
        } else {
            int count = 0;
            for (final Node related : view.mapWithHidden(node)) {
                EdgeStore.EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(related);
                while (itr.hasNext()) {
                    EdgeImpl edge = itr.next();
                    if (view.containsEdge(edge)) {
                        count++;
                        if (edge.isSelfLoop()) {
                            count++;
                        }
                    }
                }
            }
            return count;
        }
    }

    @Override
    public int getInDegree(final Node node) {
        if (!this.contains(node)) {
            return 0;
        }

        int count = 0;
        for (final Node related : view.mapWithHidden(node)) {
            EdgeStore.EdgeInIterator itr = graphStore.edgeStore.edgeInIterator(related);
            while (itr.hasNext()) {
                if (view.containsEdge(itr.next())) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public int getOutDegree(final Node node) {
        if (!this.contains(node)) {
            return 0;
        }

        int count = 0;
        for (final Node related : view.mapWithHidden(node)) {
            EdgeStore.EdgeOutIterator itr = graphStore.edgeStore.edgeOutIterator(related);
            while (itr.hasNext()) {
                if (view.containsEdge(itr.next())) {
                    count++;
                }
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
            if (edge1 instanceof MappedEdgeDecorator) {
                edge1 = undecorateEdge(edge1);
            } else {
                edge1 = decorateEdge(edge1);
            }

            if (edge2 instanceof MappedEdgeDecorator) {
                edge2 = undecorateEdge(edge2);
            } else {
                edge2 = decorateEdge(edge2);
            }

            Set<Node> n1 = new HashSet<Node>();
            for (final Node n : Arrays.asList(edge1.getSource(), edge1.getTarget())) {
                n1.addAll(view.mapWithHidden(n));
            }

            Set<Node> n2 = new HashSet<Node>();
            for (final Node n : Arrays.asList(edge2.getSource(), edge2.getTarget())) {
                n2.addAll(view.mapWithHidden(n));
            }

            for (Node n : n1) {
                if (n2.contains(n)) {
                    return true;
                }
            }

            for (Node n : n2) {
                if (n1.contains(n)) {
                    return true;
                }
            }

            return false;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public boolean isIncident(Node node, Edge edge) {
        graphStore.autoReadLock();
        try {
            if (edge instanceof MappedEdgeDecorator) {
                edge = undecorateEdge(edge);
            } else {
                edge = decorateEdge(edge);
            }

            Set<Node> n1 = new HashSet<Node>();
            for (final Node n : Arrays.asList(edge.getSource(), edge.getTarget())) {
                n1.addAll(view.mapWithHidden(n));
            }

            Set<Node> n2 = new HashSet<Node>(view.mapWithHidden(node));

            for (Node n : n1) {
                if (n2.contains(n)) {
                    return true;
                }
            }

            for (Node n : n2) {
                if (n1.contains(n)) {
                    return true;
                }
            }

            return false;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public void clearEdges(final Node node) {
        graphStore.autoWriteLock();
        try {
            this.removeAllEdgesWithLock(graphStore.edgeStore.edgeIterator(node));
            for (final Node related : view.mapWithHidden(node)) {
                this.removeAllEdgesWithLock(graphStore.edgeStore.edgeIterator(related));
            }
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void clearEdges(final Node node, final int type) {
        graphStore.autoWriteLock();
        try {
            this.removeAllEdgesWithLock(graphStore.edgeStore.edgeIterator(node, type));
            for (final Node related : view.mapWithHidden(node)) {
                this.removeAllEdgesWithLock(graphStore.edgeStore.edgeIterator(related, type));
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
    public void union(final Subgraph subGraph) {
        checkValidViewObject(subGraph.getView());

        graphStore.autoWriteLock();
        try {
            if (subGraph instanceof GraphViewDecorator) {
                view.viewDelegate.union((GraphViewImpl) subGraph.getView());
            } else if (subGraph instanceof HierarchicalGraphDecorator) {
                final HierarchicalGraphViewImpl other = (HierarchicalGraphViewImpl) subGraph.getView();
                view.viewDelegate.union(other.viewDelegate);
            }
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    @Override
    public void intersection(final Subgraph subGraph) {
        checkValidViewObject(subGraph.getView());

        graphStore.autoWriteLock();
        try {
            if (subGraph instanceof GraphViewDecorator) {
                view.viewDelegate.intersection((GraphViewImpl) subGraph.getView());
            } else if (subGraph instanceof HierarchicalGraphDecorator) {
                final HierarchicalGraphViewImpl other = (HierarchicalGraphViewImpl) subGraph.getView();
                view.viewDelegate.intersection(other.viewDelegate);
            }
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

    void checkValidEdgeObject(final Edge e) {
        if (e == null) {
            throw new NullPointerException();
        }
        if (!(e instanceof EdgeImpl)) {
            throw new ClassCastException("Object must be a EdgeImpl object");
        }
        if (((EdgeImpl) e).storeId == EdgeStore.NULL_ID) {
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
        if (!(view instanceof AbstractGraphView)) {
            throw new ClassCastException("Object must be a AbstractGraphView object");
        }
        if (((AbstractGraphView) view).graphStore != graphStore) {
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

    private final class ChainedFutureIterator<T> implements Iterator<T> {
        private final List<Callable<Iterator<T>>> delegates;

        private Iterator<Callable<Iterator<T>>> itr = null;

        private Iterator<T> delegatePointer = null;

        private T itemPointer = null;

        private ChainedFutureIterator(final Collection<? extends Callable<Iterator<T>>> c) {
            this.delegates = new ArrayList<Callable<Iterator<T>>>(c);
        }

        @Override
        public boolean hasNext() {
            itemPointer = null;

            if (null == this.itr) {
                itr = delegates.iterator();
            }

            while (null == itemPointer) {
                while (null == delegatePointer) {
                    if (!itr.hasNext()) {
                        return false;
                    }
                    try {
                        delegatePointer = itr.next().call();
                    } catch (final Exception e) {
                        throw new IllegalStateException(e);
                    }
                }

                if (delegatePointer.hasNext()) {
                    itemPointer = delegatePointer.next();
                } else {
                    delegatePointer = null;
                }
            }

            return true;
        }

        @Override
        public T next() {
            return itemPointer;
        }
    }

    private final class NodeViewIterator implements Iterator<Node> {
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
                if (pointer != null) {
                    if (!view.containsNode(pointer) || !view.visibleNode(pointer)) {
                        pointer = null;
                    }
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

    private final class EdgeViewIterator implements Iterator<Edge> {
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
                if (pointer != null) {
                    if (!view.containsEdge(pointer)) {
                        pointer = null;
                    }
                }
            }
            return true;
        }

        @Override
        public Edge next() {
            return decorateEdge(pointer);
        }

        @Override
        public void remove() {
            checkWriteLock();
            removeEdge(pointer);
        }
    }

    private final class UndirectedEdgeViewIterator implements Iterator<Edge> {
        private final Iterator<Edge> itr;

        private EdgeImpl pointer;

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
        public Edge next() {
            return decorateEdge(pointer);
        }

        @Override
        public void remove() {
            itr.remove();
        }
    }

    private class NeighborsIterator implements Iterator<Node> {
        private final Node node;

        private final Iterator<Edge> itr;

        public NeighborsIterator(Node node, Iterator<Edge> itr) {
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

    private Edge undecorateEdge(final Edge edge) {
        if (null == edge) {
            return null;
        }

        Edge unpacked = edge;
        while (unpacked instanceof MappedEdgeDecorator) {
            unpacked = ((MappedEdgeDecorator) unpacked).edge;
        }

        return unpacked;
    }

    private Edge decorateEdge(final Edge edge) {
        if (null == edge) {
            return null;
        }

        if (edge instanceof MappedEdgeDecorator) {
            return edge;
        }

        final Node mappedSource = this.view.mapToVisible(edge.getSource());
        final Node mappedTarget = this.view.mapToVisible(edge.getTarget());

        if (mappedSource == edge.getSource() && mappedTarget == edge.getTarget()) {
            return edge;
        }

        return new MappedEdgeDecorator(edge, mappedSource, mappedTarget);
    }

    protected class MappedEdgeDecorator implements Edge {
        private final Edge edge;

        private final Node source;

        private final Node target;

        private MappedEdgeDecorator(final Edge edge, final Node source, final Node target) {
            this.edge = edge;
            this.source = source;
            this.target = target;
        }

        @Override
        public Node getSource() {
            return this.source;
        }

        @Override
        public Node getTarget() {
            return this.target;
        }

        @Override
        public double getWeight() {
            return edge.getWeight();
        }

        @Override
        public double getWeight(double timestamp) {
            return edge.getWeight(timestamp);
        }

        @Override
        public double getWeight(Interval interval) {
            return edge.getWeight(interval);
        }

        @Override
        public double getWeight(GraphView view) {
            return edge.getWeight(view);
        }

        @Override
        public Iterable<Map.Entry> getWeights() {
            return edge.getWeights();
        }

        @Override
        public void setWeight(double weight) {
            edge.setWeight(weight);
        }

        @Override
        public void setWeight(double weight, double timestamp) {
            edge.setWeight(weight, timestamp);
        }

        @Override
        public void setWeight(double weight, Interval interval) {
            edge.setWeight(weight, interval);
        }

        @Override
        public boolean hasDynamicWeight() {
            return edge.hasDynamicWeight();
        }

        @Override
        public int getType() {
            return edge.getType();
        }

        @Override
        public Object getTypeLabel() {
            return edge.getTypeLabel();
        }

        @Override
        public boolean isSelfLoop() {
            return edge.isSelfLoop();
        }

        @Override
        public boolean isDirected() {
            return edge.isSelfLoop();
        }

        @Override
        public Object getId() {
            return edge.getId();
        }

        @Override
        public String getLabel() {
            return edge.getLabel();
        }

        @Override
        public Object getAttribute(String key) {
            return edge.getAttribute(key);
        }

        @Override
        public Object getAttribute(Column column) {
            return edge.getAttribute(column);
        }

        @Override
        public Object getAttribute(String key, double timestamp) {
            return edge.getAttribute(key, timestamp);
        }

        @Override
        public Object getAttribute(Column column, double timestamp) {
            return edge.getAttribute(column, timestamp);
        }

        @Override
        public Object getAttribute(String key, Interval interval) {
            return edge.getAttribute(key, interval);
        }

        @Override
        public Object getAttribute(Column column, Interval interval) {
            return edge.getAttribute(column, interval);
        }

        @Override
        public Object getAttribute(String key, GraphView view) {
            return edge.getAttribute(key, view);
        }

        @Override
        public Object getAttribute(Column column, GraphView view) {
            return edge.getAttribute(column, view);
        }

        @Override
        public Iterable<Map.Entry> getAttributes(Column column) {
            return edge.getAttributes(column);
        }

        @Override
        public Object[] getAttributes() {
            return edge.getAttributes();
        }

        @Override
        public Set<String> getAttributeKeys() {
            return edge.getAttributeKeys();
        }

        @Override
        public ColumnIterable getAttributeColumns() {
            return edge.getAttributeColumns();
        }

        @Override
        public int getStoreId() {
            return edge.getStoreId();
        }

        @Override
        public Object removeAttribute(String key) {
            return edge.removeAttribute(key);
        }

        @Override
        public Object removeAttribute(Column column) {
            return edge.removeAttribute(column);
        }

        @Override
        public Object removeAttribute(String key, double timestamp) {
            return edge.removeAttribute(key, timestamp);
        }

        @Override
        public Object removeAttribute(Column column, double timestamp) {
            return edge.removeAttribute(column, timestamp);
        }

        @Override
        public Object removeAttribute(String key, Interval interval) {
            return edge.removeAttribute(key, interval);
        }

        @Override
        public Object removeAttribute(Column column, Interval interval) {
            return edge.removeAttribute(column, interval);
        }

        @Override
        public void setLabel(String label) {
            edge.setLabel(label);
        }

        @Override
        public void setAttribute(String key, Object value) {
            edge.setAttribute(key, value);
        }

        @Override
        public void setAttribute(Column column, Object value) {
            edge.setAttribute(column, value);
        }

        @Override
        public void setAttribute(String key, Object value, double timestamp) {
            edge.setAttribute(key, value, timestamp);
        }

        @Override
        public void setAttribute(Column column, Object value, double timestamp) {
            edge.setAttribute(column, value, timestamp);
        }

        @Override
        public void setAttribute(String key, Object value, Interval interval) {
            edge.setAttribute(key, value, interval);
        }

        @Override
        public void setAttribute(Column column, Object value, Interval interval) {
            edge.setAttribute(column, value, interval);
        }

        @Override
        public boolean addTimestamp(double timestamp) {
            return edge.addTimestamp(timestamp);
        }

        @Override
        public boolean removeTimestamp(double timestamp) {
            return edge.removeTimestamp(timestamp);
        }

        @Override
        public boolean hasTimestamp(double timestamp) {
            return edge.hasTimestamp(timestamp);
        }

        @Override
        public double[] getTimestamps() {
            return edge.getTimestamps();
        }

        @Override
        public boolean addInterval(Interval interval) {
            return edge.addInterval(interval);
        }

        @Override
        public boolean removeInterval(Interval interval) {
            return edge.removeInterval(interval);
        }

        @Override
        public boolean hasInterval(Interval interval) {
            return edge.hasInterval(interval);
        }

        @Override
        public Interval[] getIntervals() {
            return edge.getIntervals();
        }

        @Override
        public void clearAttributes() {
            edge.clearAttributes();
        }

        @Override
        public Table getTable() {
            return edge.getTable();
        }

        @Override
        public float r() {
            return edge.r();
        }

        @Override
        public float g() {
            return edge.g();
        }

        @Override
        public float b() {
            return edge.b();
        }

        @Override
        public int getRGBA() {
            return edge.getRGBA();
        }

        @Override
        public Color getColor() {
            return edge.getColor();
        }

        @Override
        public float alpha() {
            return edge.alpha();
        }

        @Override
        public TextProperties getTextProperties() {
            return edge.getTextProperties();
        }

        @Override
        public void setR(float r) {
            edge.setR(r);
        }

        @Override
        public void setG(float g) {
            edge.setG(g);
        }

        @Override
        public void setB(float b) {
            edge.setB(b);
        }

        @Override
        public void setAlpha(float a) {
            edge.setAlpha(a);
        }

        @Override
        public void setColor(Color color) {
            edge.setColor(color);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MappedEdgeDecorator that = (MappedEdgeDecorator) o;

            if (edge != null ? !edge.equals(that.edge) : that.edge != null) {
                return false;
            }
            if (source != null ? !source.equals(that.source) : that.source != null) {
                return false;
            }
            return target != null ? target.equals(that.target) : that.target == null;
        }

        @Override
        public int hashCode() {
            int result = edge != null ? edge.hashCode() : 0;
            result = 31 * result + (source != null ? source.hashCode() : 0);
            result = 31 * result + (target != null ? target.hashCode() : 0);
            return result;
        }
    }
}
