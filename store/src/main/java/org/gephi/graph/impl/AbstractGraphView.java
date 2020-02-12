package org.gephi.graph.impl;

import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.UndirectedSubgraph;

abstract public class AbstractGraphView implements GraphView {
    protected final GraphStore graphStore;

    protected final GraphAttributesImpl attributes;

    protected final boolean nodeView;

    protected final boolean edgeView;

    private Interval interval;

    private int storeId = GraphViewStore.NULL_VIEW;

    public AbstractGraphView(final GraphStore store, boolean nodes, boolean edges) {
        this.graphStore = store;
        this.nodeView = nodes;
        this.edgeView = edges;
        this.interval = Interval.INFINITY_INTERVAL;
        this.attributes = new GraphAttributesImpl();
    }

    public AbstractGraphView(final AbstractGraphView view, boolean nodes, boolean edges) {
        this.graphStore = view.graphStore;
        this.nodeView = nodes;
        this.edgeView = edges;
        this.interval = view.interval;
        this.attributes = new GraphAttributesImpl();
    }

    public int getStoreId() {
        return this.storeId;
    }

    protected void setStoreId(final int id) {
        this.storeId = id;
    }

    @Override
    public boolean isDestroyed() {
        return GraphViewStore.NULL_VIEW == this.storeId;
    }

    @Override
    public GraphModelImpl getGraphModel() {
        return this.graphStore.graphModel;
    }

    @Override
    public boolean isMainView() {
        return false;
    }

    @Override
    public boolean isNodeView() {
        return this.nodeView;
    }

    @Override
    public boolean isEdgeView() {
        return this.edgeView;
    }

    public void setTimeInterval(Interval interval) {
        if (interval == null) {
            interval = Interval.INFINITY_INTERVAL;
        }
        this.interval = interval;
    }

    @Override
    public Interval getTimeInterval() {
        return this.interval;
    }

    abstract public DirectedSubgraph getDirectedGraph();

    abstract public UndirectedSubgraph getUndirectedGraph();

    abstract public boolean deepEquals(AbstractGraphView view);

    abstract public int deepHashCode();

    abstract protected void viewDestroyed();

    abstract protected void nodeAdded(NodeImpl node);

    abstract protected void nodeRemoved(NodeImpl node);

    abstract protected void edgeAdded(EdgeImpl edge);

    abstract protected void edgeRemoved(EdgeImpl edge);

    abstract protected GraphObserverImpl createGraphObserver(Graph graph, boolean withDiff);

    abstract protected void destroyGraphObserver(GraphObserver graphObserver);
}
