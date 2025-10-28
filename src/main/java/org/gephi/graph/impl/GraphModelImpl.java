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

import java.time.ZoneId;
import java.util.Arrays;
import java.util.function.Predicate;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphBridge;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.SpatialIndex;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeIndex;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.UndirectedSubgraph;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.TimestampDoubleMap;

public class GraphModelImpl implements GraphModel {

    protected final ConfigurationImpl configuration;
    protected final GraphStore store;
    protected final GraphBridgeImpl graphBridge;

    public GraphModelImpl() {
        this(Configuration.builder().build());
    }

    public GraphModelImpl(Configuration config) {
        checkValidConfiguration(config);

        configuration = new ConfigurationImpl(config);
        store = new GraphStore(this);
        graphBridge = new GraphBridgeImpl(store);
    }

    @Override
    public GraphFactory factory() {
        return store.factory;
    }

    @Override
    public GraphBridge bridge() {
        return graphBridge;
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
    public DefaultColumns defaultColumns() {
        return store.defaultColumns;
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
    public Object getEdgeTypeLabel(int id) {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.getLabel(id);
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public int getEdgeTypeCount() {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.size();
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public Object[] getEdgeTypeLabels() {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.getLabels();
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public Object[] getEdgeTypeLabels(boolean includeEmpty) {
        if (includeEmpty) {
            return getEdgeTypeLabels();
        }
        store.autoReadLock();
        try {
            return Arrays.stream(store.edgeTypeStore.getLabels())
                    .filter(l -> store.getEdgeCount(store.edgeTypeStore.getId(l)) > 0).toArray();
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public int[] getEdgeTypes() {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.getIdsAsInts();
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
            return !store.timeStore.isEmpty();
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
    public GraphView createView(Predicate<Node> nodeFilter, Predicate<Edge> edgeFilter) {
        return store.viewStore.createView(nodeFilter, edgeFilter);
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
        return store.nodeTable;
    }

    @Override
    public Table getEdgeTable() {
        return store.edgeTable;
    }

    @Override
    public Index getNodeIndex() {
        return getNodeIndex(store.mainGraphView);
    }

    @Override
    public Index getNodeIndex(GraphView view) {
        IndexStore<Node> indexStore = store.nodeTable.store.indexStore;
        if (indexStore != null) {
            if (view.isMainView()) {
                return indexStore.getIndex(store);
            }
            return indexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public Index<Element> getElementIndex(Table table) {
        return getElementIndex(table, store.mainGraphView);
    }

    @Override
    public Index<Element> getElementIndex(Table table, GraphView view) {
        if (table.isNodeTable()) {
            return getNodeIndex(view);
        } else if (table.isEdgeTable()) {
            return getEdgeIndex(view);
        }
        return null;
    }

    @Override
    public Index getEdgeIndex() {
        return getEdgeIndex(store.mainGraphView);
    }

    @Override
    public Index getEdgeIndex(GraphView view) {
        IndexStore<Edge> indexStore = store.edgeTable.store.indexStore;
        if (indexStore != null) {
            if (view.isMainView()) {
                return indexStore.getIndex(store);
            }
            return indexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public TimeIndex<Node> getNodeTimeIndex() {
        return getNodeTimeIndex(store.mainGraphView);
    }

    @Override
    public TimeIndex<Node> getNodeTimeIndex(GraphView view) {
        TimeIndexStore timeIndexStore = store.timeStore.nodeIndexStore;
        if (timeIndexStore != null) {
            if (view.isMainView()) {
                return timeIndexStore.getIndex(store);
            }
            return timeIndexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public TimeIndex<Edge> getEdgeTimeIndex() {
        return getEdgeTimeIndex(store.mainGraphView);
    }

    @Override
    public TimeIndex<Edge> getEdgeTimeIndex(GraphView view) {
        TimeIndexStore timeIndexStore = store.timeStore.edgeIndexStore;
        if (timeIndexStore != null) {
            if (view.isMainView()) {
                return timeIndexStore.getIndex(store);
            }
            return timeIndexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public GraphObserver createGraphObserver(Graph graph, boolean withGraphDiff) {
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
    public ZoneId getTimeZone() {
        return store.timeZone;
    }

    @Override
    public void setTimeZone(ZoneId timeZone) {
        store.timeZone = timeZone;
    }

    @Override
    public Interval getTimeBounds() {
        return getTimeBounds(store.getView());
    }

    @Override
    public Interval getTimeBoundsVisible() {
        return getTimeBounds(getGraphVisible().getView());
    }

    @Override
    public Interval getTimeBounds(GraphView view) {
        TimeStore timeStore = store.timeStore;
        store.autoReadLock();
        try {
            double min = timeStore.getMin(getGraph(view));
            double max = timeStore.getMax(getGraph(view));
            return new Interval(min, max);
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public Configuration getConfiguration() {
        return configuration.toConfiguration();
    }

    @Override
    public void setConfiguration(Configuration config) {
        throw new UnsupportedOperationException(
                "No longer supported. Configuration is immutable and needs to be passed at GraphModel creation time");
    }

    @Override
    public int getMaxEdgeStoreId() {
        return store.edgeStore.maxStoreId();
    }

    @Override
    public int getMaxNodeStoreId() {
        return store.nodeStore.maxStoreId();
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

    public boolean deepEquals(GraphModelImpl obj) {
        if (obj == null) {
            return false;
        }
        if (this.store != obj.store && (this.store == null || !this.store.deepEquals(obj.store))) {
            return false;
        }
        if (this.configuration != obj.configuration && (this.configuration == null || !this.configuration
                .equals(obj.configuration))) {
            return false;
        }
        return true;
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

    private void checkValidConfiguration(Configuration config) {
        if (config == null) {
            throw new NullPointerException("Configuration cannot be null, use Configuration.builder().build() instead");
        }
        Class edgeWeightType = config.getEdgeWeightType();
        if (edgeWeightType.equals(Double.class)) {
            return;// Double is always allowed
        }

        switch (config.getTimeRepresentation()) {
            case INTERVAL:
                if (!edgeWeightType.equals(IntervalDoubleMap.class)) {
                    throw new IllegalArgumentException(
                            "Edge weight type should be Double or IntervalDoubleMap for INTERVAL TimeRepresentation");
                }
                break;
            case TIMESTAMP:
                if (!edgeWeightType.equals(TimestampDoubleMap.class)) {
                    throw new IllegalArgumentException(
                            "Edge weight type should be Double or TimestampDoubleMap for TIMESTAMP TimeRepresentation");
                }
                break;
        }
    }
}
