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

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.HierarchicalGraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.UndirectedSubgraph;

public class GraphViewStore {

    // Const
    protected static final int NULL_VIEW = -1;
    // Config
    protected static final int DEFAULT_VIEWS = 0;
    // Data
    protected final IntSortedSet garbageQueue;
    protected final GraphStore graphStore;
    protected AbstractGraphView[] views;
    protected int length;
    // Visible view
    protected GraphView visibleView;

    public GraphViewStore(GraphStore graphStore) {
        if (graphStore == null) {
            throw new NullPointerException();
        }
        this.graphStore = graphStore;
        this.views = new AbstractGraphView[DEFAULT_VIEWS];
        this.garbageQueue = new IntRBTreeSet();
        this.visibleView = graphStore.mainGraphView;
    }

    public HierarchicalGraphView createHierarchicalView() {
        return createHierarchicalView(true, true);
    }

    public HierarchicalGraphView createHierarchicalView(boolean nodes, boolean edges) {
        graphStore.autoWriteLock();
        try {
            HierarchicalGraphViewImpl graphView = new HierarchicalGraphViewImpl(graphStore, nodes, edges);
            addView(graphView);
            return graphView;
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    public GraphViewImpl createView() {
        return createView(true, true);
    }

    public GraphViewImpl createView(boolean nodes, boolean edges) {
        graphStore.autoWriteLock();
        try {
            GraphViewImpl graphView = new GraphViewImpl(graphStore, nodes, edges);
            addView(graphView);
            return graphView;
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    public GraphViewImpl createView(GraphView view) {
        return createView(view, true, true);
    }

    public GraphViewImpl createView(GraphView view, boolean nodes, boolean edges) {
        if (view.isMainView()) {
            graphStore.autoWriteLock();
            try {
                GraphViewImpl graphView = new GraphViewImpl(graphStore, nodes, edges);
                graphView.fill();
                addView(graphView);
                return graphView;
            } finally {
                graphStore.autoWriteUnlock();
            }
        } else {
            checkNonNullViewObject(view);
            checkViewExist((AbstractGraphView) view);
            graphStore.autoWriteLock();
            try {
                final GraphViewImpl copy;
                if (view instanceof GraphViewImpl) {
                    copy = (GraphViewImpl) view;
                } else if (view instanceof HierarchicalGraphViewImpl) {
                    copy = ((HierarchicalGraphViewImpl) view).viewDelegate;
                } else {
                    throw new IllegalArgumentException();
                }
                GraphViewImpl graphView = new GraphViewImpl(copy, nodes, edges);
                addView(graphView);
                return graphView;
            } finally {
                graphStore.autoWriteUnlock();
            }
        }
    }

    public void destroyView(GraphView view) {
        graphStore.autoWriteLock();
        try {
            checkNonNullViewObject(view);

            TimeIndexStore nodeTimeStore = graphStore.timeStore.nodeIndexStore;
            if (nodeTimeStore != null) {
                nodeTimeStore.deleteViewIndex(((AbstractGraphView) view).getDirectedGraph());
            }

            TimeIndexStore edgeTimeStore = graphStore.timeStore.edgeIndexStore;
            if (edgeTimeStore != null) {
                edgeTimeStore.deleteViewIndex(((AbstractGraphView) view).getDirectedGraph());
            }

            IndexStore<Node> nodeIndexStore = graphStore.nodeTable.store.indexStore;
            if (nodeIndexStore != null) {
                nodeIndexStore.deleteViewIndex(((AbstractGraphView) view).getDirectedGraph());
            }

            IndexStore<Edge> edgeIndexStore = graphStore.edgeTable.store.indexStore;
            if (edgeIndexStore != null) {
                edgeIndexStore.deleteViewIndex(((AbstractGraphView) view).getDirectedGraph());
            }

            removeView((AbstractGraphView) view);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    public void setTimeInterval(GraphView view, Interval interval) {
        checkNonNullViewObject(view);
        checkViewExist((AbstractGraphView) view);

        graphStore.autoWriteLock();
        try {
            AbstractGraphView graphView = (AbstractGraphView) view;
            graphView.setTimeInterval(interval);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    public boolean contains(GraphView view) {
        graphStore.autoReadLock();
        try {
            checkNonNullViewObject(view);
            AbstractGraphView viewImpl = (AbstractGraphView) view;
            int id = viewImpl.getStoreId();
            if (id != NULL_VIEW && id < length && views[id] == view) {
                return true;
            }
            return false;
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    public int size() {
        return length - garbageQueue.size();
    }

    public Subgraph getGraph(GraphView view) {
        checkNonNullViewObject(view);

        if (graphStore.isUndirected()) {
            if (view.isMainView()) {
                return graphStore.undirectedDecorator;
            }
            return ((AbstractGraphView) view).getUndirectedGraph();
        } else {
            if (view.isMainView()) {
                return graphStore;
            }
            return ((AbstractGraphView) view).getDirectedGraph();
        }
    }

    public DirectedSubgraph getDirectedGraph(GraphView view) {
        checkNonNullViewObject(view);

        if (view.isMainView()) {
            return graphStore;
        }

        checkDirectedAllowed();
        return ((AbstractGraphView) view).getDirectedGraph();
    }

    public UndirectedSubgraph getUndirectedGraph(GraphView view) {
        checkNonNullViewObject(view);

        if (view.isMainView()) {
            return graphStore.undirectedDecorator;
        }
        return ((AbstractGraphView) view).getUndirectedGraph();
    }

    public GraphView getVisibleView() {
        return visibleView;
    }

    public void setVisibleView(GraphView view) {
        if (view == null || view == graphStore.mainGraphView) {
            visibleView = graphStore.mainGraphView;
        } else {
            checkNonNullViewObject(view);
            checkViewExist((AbstractGraphView) view);
            visibleView = view;
        }
    }

    public GraphObserverImpl createGraphObserver(Graph graph, boolean withDiff) {
        AbstractGraphView graphViewImpl = (AbstractGraphView) graph.getView();
        checkViewExist(graphViewImpl);

        return graphViewImpl.createGraphObserver(graph, withDiff);
    }

    public void destroyGraphObserver(GraphObserverImpl graphObserver) {
        AbstractGraphView graphViewImpl = (AbstractGraphView) graphObserver.graph.getView();
        checkViewExist(graphViewImpl);

        graphViewImpl.destroyGraphObserver(graphObserver);
    }

    protected void addNode(NodeImpl node) {
        if (views.length > 0) {
            for (AbstractGraphView view : views) {
                if (view != null) {
                    view.nodeAdded(node);
                }
            }
        }
    }

    protected void removeNode(NodeImpl node) {
        if (views.length > 0) {
            for (AbstractGraphView view : views) {
                if (view != null) {
                    view.nodeRemoved(node);
                }
            }
        }
    }

    protected void addEdge(EdgeImpl edge) {
        if (views.length > 0) {
            for (AbstractGraphView view : views) {
                if (view != null) {
                    view.edgeAdded(edge);
                }
            }
        }
    }

    protected void removeEdge(EdgeImpl edge) {
        if (views.length > 0) {
            for (AbstractGraphView view : views) {
                if (view != null) {
                    view.edgeRemoved(edge);
                }
            }
        }
    }

    protected int addView(final AbstractGraphView view) {
        checkNonNullViewObject(view);

        int id;
        if (!garbageQueue.isEmpty()) {
            id = garbageQueue.firstInt();
            garbageQueue.remove(id);
        } else {
            id = length++;
            ensureArraySize(id);
        }
        views[id] = view;
        view.setStoreId(id);
        return id;
    }

    protected void removeView(final AbstractGraphView view) {
        checkViewExist(view);

        int id = view.getStoreId();
        views[id] = null;
        garbageQueue.add(id);

        view.viewDestroyed();

        // Check if not visible view
        if (visibleView == view) {
            visibleView = graphStore.mainGraphView;
        }
    }

    private void ensureArraySize(int index) {
        if (index >= views.length) {
            AbstractGraphView[] newArray = new AbstractGraphView[index + 1];
            System.arraycopy(views, 0, newArray, 0, views.length);
            views = newArray;
        }
    }

    public int deepHashCode() {
        int hash = 5;
        for (AbstractGraphView view : this.views) {
            hash = 67 * hash + view.deepHashCode();
        }
        hash = 67 * hash + this.length;
        return hash;
    }

    public boolean deepEquals(GraphViewStore obj) {
        if (obj == null) {
            return false;
        }
        if (this.length != obj.length) {
            return false;
        }
        int l = this.views.length;
        if (l != obj.views.length) {
            return false;
        }
        for (int i = 0; i < l; i++) {
            AbstractGraphView e1 = this.views[i];
            AbstractGraphView e2 = obj.views[i];

            if (e1 == e2) {
                continue;
            }
            if (e1 == null) {
                return false;
            }
            if (!e1.deepEquals(e2)) {
                return false;
            }
        }

        return true;
    }

    protected void checkNonNullViewObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (o != graphStore.mainGraphView) {
            if (!(o instanceof AbstractGraphView)) {
                throw new ClassCastException("View must be a AbstractGraphView object");
            }
        }
    }

    protected void checkViewExist(final AbstractGraphView view) {
        final int id = view.getStoreId();
        if (id == NULL_VIEW || id >= length || views[id] != view) {
            throw new IllegalArgumentException("The view doesn't exist");
        }
    }

    private void checkDirectedAllowed() {
        if (graphStore.isUndirected()) {
            throw new RuntimeException("Can't get a directed subgraph from an undirected graph");
        }
    }
}
