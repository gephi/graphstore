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
import org.gephi.attribute.api.TimestampIndex;
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
        store = new GraphStore();
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
    public Graph getGraphVisivle() {
        return getGraph(store.viewStore.visibleView);
    }

    @Override
    public Subgraph getGraph(GraphView view) {
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
    public boolean isDirected() {
        store.autoReadLock();
        try {
            return store.isDirected();
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public boolean isUndirected() {
        store.autoReadLock();
        try {
            return store.isUndirected();
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public boolean isMixed() {
        store.autoReadLock();
        try {
            return store.isMixed();
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public GraphView createView() {
        return store.viewStore.createView();
    }

    @Override
    public GraphView createNodeView() {
        return store.viewStore.createNodeView();
    }

    @Override
    public void destroyView(GraphView view) {
        store.viewStore.destroyView(view);
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
        IndexStore<Node> indexStore = store.nodeColumnStore.indexStore;
        if (indexStore != null) {
            return indexStore.getIndex(store);
        }
        return null;
    }

    @Override
    public Index getNodeIndex(GraphView view) {
        IndexStore<Node> indexStore = store.nodeColumnStore.indexStore;
        if (indexStore != null) {
            return indexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public Index getEdgeIndex() {
        IndexStore<Edge> indexStore = store.edgeColumnStore.indexStore;
        if (indexStore != null) {
            return indexStore.getIndex(store);
        }
        return null;
    }

    @Override
    public Index getEdgeIndex(GraphView view) {
        IndexStore<Edge> indexStore = store.edgeColumnStore.indexStore;
        if (indexStore != null) {
            return indexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public TimestampIndex getTimestampIndex() {
        TimestampStore timestampStore = store.timestampStore;
        if (timestampStore != null) {
            return timestampStore.getIndex(store);
        }
        return null;
    }

    @Override
    public TimestampIndex getTimestampIndex(GraphView view) {
        TimestampStore timestampStore = store.timestampStore;
        if (timestampStore != null) {
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
