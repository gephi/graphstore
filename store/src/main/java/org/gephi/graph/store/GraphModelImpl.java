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

import org.gephi.attribute.api.AttributeModel;
import org.gephi.attribute.api.Index;
import org.gephi.attribute.api.Table;
import org.gephi.attribute.api.TimeFormat;
import org.gephi.attribute.api.TimestampIndex;
import org.gephi.attribute.time.Interval;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.UndirectedSubgraph;

/**
 *
 * @author mbastian
 */
public class GraphModelImpl implements GraphModel, AttributeModel {

    protected final GraphStore store;
    protected final TableImpl<Node> nodeTable;
    protected final TableImpl<Edge> edgeTable;

    public GraphModelImpl() {
        store = new GraphStore(this);
        nodeTable = new TableImpl<Node>(store.nodeColumnStore);
        edgeTable = new TableImpl<Edge>(store.edgeColumnStore);
    }

    @Override
    public GraphFactory factory() {
        return store.factory;
    }

    @Override
    public Graph getGraph() {
        return store;
    }

    @Override
    public Graph getGraphVisible() {
        return getGraph(store.viewStore.visibleView);
    }

    @Override
    public Subgraph getGraph(GraphView view) {
        if (store.isUndirected()) {
            return store.viewStore.getUndirectedGraph(view);
        }
        return store.viewStore.getDirectedGraph(view);
    }

    @Override
    public DirectedGraph getDirectedGraph() {
        return store;
    }

    @Override
    public DirectedGraph getDirectedGraphVisible() {
        return getDirectedGraph(store.viewStore.visibleView);
    }

    @Override
    public UndirectedGraph getUndirectedGraph() {
        return store.undirectedDecorator;
    }

    @Override
    public UndirectedGraph getUndirectedGraphVisible() {
        return getUndirectedGraph(store.viewStore.visibleView);
    }

    @Override
    public DirectedSubgraph getDirectedGraph(GraphView view) {
        return store.viewStore.getDirectedGraph(view);
    }

    @Override
    public UndirectedSubgraph getUndirectedGraph(GraphView view) {
        return store.viewStore.getUndirectedGraph(view);
    }

    @Override
    public GraphView getVisibleView() {
        return store.viewStore.getVisibleView();
    }

    @Override
    public void setVisibleView(GraphView view) {
        store.autoWriteLock();
        try {
            store.viewStore.setVisibleView(view);
        } finally {
            store.autoWriteUnlock();
        }
    }

    @Override
    public int addEdgeType(Object label) {
        store.autoWriteLock();
        try {
            return store.edgeTypeStore.addType(label);
        } finally {
            store.autoWriteUnlock();
        }
    }

    @Override
    public int getEdgeType(Object label) {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.getId(label);
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public Object getEdgeLabel(int id) {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.getLabel(id);
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public boolean isMultiGraph() {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.size() > 1;
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public boolean isDynamic() {
        store.autoReadLock();
        try {
            return !store.timestampStore.isEmpty();
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public boolean isDirected() {
        return store.isDirected();
    }

    @Override
    public boolean isUndirected() {
        return store.isUndirected();
    }

    @Override
    public boolean isMixed() {
        return store.isMixed();
    }

    @Override
    public GraphView createView() {
        return store.viewStore.createView();
    }

    @Override
    public GraphView createView(boolean node, boolean edge) {
        return store.viewStore.createView(node, edge);
    }

    @Override
    public GraphView copyView(GraphView view) {
        return store.viewStore.createView(view);
    }

    @Override
    public GraphView copyView(GraphView view, boolean node, boolean edge) {
        return store.viewStore.createView(view, node, edge);
    }

    @Override
    public void destroyView(GraphView view) {
        store.viewStore.destroyView(view);
    }

    @Override
    public void setTimeInterval(GraphView view, Interval interval) {
        store.viewStore.setTimeInterval(view, interval);
    }

    @Override
    public Table getNodeTable() {
        return nodeTable;
    }

    @Override
    public Table getEdgeTable() {
        return edgeTable;
    }

    @Override
    public Index getNodeIndex() {
        return getNodeIndex(store.mainGraphView);
    }

    @Override
    public Index getNodeIndex(GraphView view) {
        IndexStore<Node> indexStore = store.nodeColumnStore.indexStore;
        if (indexStore != null) {
            if(view.isMainView()) {
                return indexStore.getIndex(store);
            }
            return indexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public Index getEdgeIndex() {
        return getEdgeIndex(store.mainGraphView);
    }

    @Override
    public Index getEdgeIndex(GraphView view) {
        IndexStore<Edge> indexStore = store.edgeColumnStore.indexStore;
        if (indexStore != null) {
            if(view.isMainView()) {
                return indexStore.getIndex(store);
            }
            return indexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public TimestampIndex<Node> getNodeTimestampIndex() {
        return getNodeTimestampIndex(store.mainGraphView);
    }

    @Override
    public TimestampIndex<Node> getNodeTimestampIndex(GraphView view) {
        TimestampIndexStore timestampStore = store.timestampStore.nodeIndexStore;
        if (timestampStore != null) {
            if(view.isMainView()) {
                return timestampStore.getIndex(store);
            }
            return timestampStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public TimestampIndex<Edge> getEdgeTimestampIndex() {
        return getEdgeTimestampIndex(store.mainGraphView);
    }

    @Override
    public TimestampIndex<Edge> getEdgeTimestampIndex(GraphView view) {
        TimestampIndexStore timestampStore = store.timestampStore.edgeIndexStore;
        if (timestampStore != null) {
            if(view.isMainView()) {
                return timestampStore.getIndex(store);
            }
            return timestampStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public GraphObserver getGraphObserver(Graph graph, boolean withGraphDiff) {
        store.autoWriteLock();
        try {
            if (graph.getView().isMainView()) {
                return store.createGraphObserver(graph, withGraphDiff);
            } else {
                return store.viewStore.createGraphObserver(graph, withGraphDiff);
            }
        } finally {
            store.autoWriteUnlock();
        }
    }

    @Override
    public TimeFormat getTimeFormat() {
        return store.timeFormat;
    }

    @Override
    public void setTimeFormat(TimeFormat timeFormat) {
        store.timeFormat = timeFormat;
    }

    @Override
    public Interval getTimeBounds() {
        TimestampStore timestampStore = store.timestampStore;
        store.autoReadLock();
        try {
            double min = timestampStore.getMin(store);
            double max = timestampStore.getMax(store);
            return new Interval(min, max);
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public Interval getTimeBoundsVisible() {
        TimestampStore timestampStore = store.timestampStore;
        store.autoReadLock();
        try {
            double min = timestampStore.getMin(getGraphVisible());
            double max = timestampStore.getMax(getGraphVisible());
            return new Interval(min, max);
        } finally {
            store.autoReadUnlock();
        }
    }

    public void destroyGraphObserver(GraphObserver observer) {
        checkGraphObserver(observer);

        store.autoWriteLock();
        try {
            if (observer.getGraph().getView().isMainView()) {
                store.destroyGraphObserver((GraphObserverImpl) observer);
            } else {
                store.viewStore.destroyGraphObserver((GraphObserverImpl) observer);
            }
        } finally {
            store.autoWriteUnlock();
        }
    }

    public GraphStore getStore() {
        return store;
    }

    private void checkGraphObserver(GraphObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        if (!(observer instanceof GraphObserverImpl)) {
            throw new ClassCastException("The observer should be a GraphObserverImpl instance");
        }
        if (((GraphObserverImpl) observer).graphStore != store) {
            throw new RuntimeException("The observer doesn't belong to this store");
        }
    }
}
