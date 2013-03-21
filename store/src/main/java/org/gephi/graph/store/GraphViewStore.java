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

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.Arrays;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.UndirectedSubgraph;

/**
 *
 * @author mbastian
 */
public class GraphViewStore {

    //Const
    protected static final int NULL_VIEW = -1;
    //Config
    protected static final int DEFAULT_VIEWS = 0;
    //Data
    protected final IntSortedSet garbageQueue;
    protected final GraphStore graphStore;
    protected GraphViewImpl[] views;
    protected int length;

    public GraphViewStore(GraphStore graphStore) {
        if (graphStore == null) {
            throw new NullPointerException();
        }
        this.graphStore = graphStore;
        this.views = new GraphViewImpl[DEFAULT_VIEWS];
        this.garbageQueue = new IntRBTreeSet();
    }

    public GraphViewImpl createView() {
        graphStore.autoWriteLock();
        try {
            GraphViewImpl graphView = new GraphViewImpl(graphStore, false);
            addView(graphView);
            return graphView;
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    public GraphViewImpl createNodeView() {
        graphStore.autoWriteLock();
        try {
            GraphViewImpl graphView = new GraphViewImpl(graphStore, true);
            addView(graphView);
            return graphView;
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    public GraphViewImpl createView(GraphView view) {
        checkNonNullViewObject(view);
        checkViewExist((GraphViewImpl) view);

        graphStore.autoWriteLock();
        try {
            GraphViewImpl graphView = new GraphViewImpl((GraphViewImpl) view);
            addView(graphView);
            return graphView;
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    public void destroyView(GraphView view) {
        graphStore.autoWriteLock();
        try {
            checkNonNullViewObject(view);

            TimestampStore timestampStore = graphStore.timestampStore;
            if (timestampStore != null) {
                timestampStore.deleteViewIndex(((GraphViewImpl) view).getDirectedGraph());
            }

            IndexStore<Node> nodeIndexStore = graphStore.nodeColumnStore.indexStore;
            if (nodeIndexStore != null) {
                nodeIndexStore.deleteViewIndex(((GraphViewImpl) view).getDirectedGraph());
            }

            IndexStore<Edge> edgeIndexStore = graphStore.edgeColumnStore.indexStore;
            if (edgeIndexStore != null) {
                edgeIndexStore.deleteViewIndex(((GraphViewImpl) view).getDirectedGraph());
            }

            removeView((GraphViewImpl) view);
        } finally {
            graphStore.autoWriteUnlock();
        }
    }

    public boolean contains(GraphView view) {
        graphStore.autoReadLock();
        try {
            checkNonNullViewObject(view);
            GraphViewImpl viewImpl = (GraphViewImpl) view;
            int id = viewImpl.storeId;
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
            return ((GraphViewImpl) view).getUndirectedGraph();
        } else {
            return ((GraphViewImpl) view).getDirectedGraph();
        }
    }

    public DirectedSubgraph getDirectedGraph(GraphView view) {
        checkNonNullViewObject(view);
        checkDirectedAllowed();

        return ((GraphViewImpl) view).getDirectedGraph();
    }

    public UndirectedSubgraph getUndirectedGraph(GraphView view) {
        checkNonNullViewObject(view);

        return ((GraphViewImpl) view).getUndirectedGraph();
    }

    public GraphObserverImpl createGraphObserver(Graph graph, boolean withDiff) {
        GraphViewImpl graphViewImpl = (GraphViewImpl) graph.getView();
        checkViewExist(graphViewImpl);

        return graphViewImpl.createGraphObserver(graph, withDiff);
    }

    public void destroyGraphObserver(GraphObserverImpl graphObserver) {
        GraphViewImpl graphViewImpl = (GraphViewImpl) graphObserver.graph.getView();
        checkViewExist(graphViewImpl);

        graphViewImpl.destroyGraphObserver(graphObserver);
    }

    protected void addNode(NodeImpl node) {
        if (views.length > 0) {
            for (GraphViewImpl view : views) {
                view.ensureNodeVectorSize(node);
            }
        }
    }

    protected void removeNode(NodeImpl node) {
        if (views.length > 0) {
            for (GraphViewImpl view : views) {
                view.removeNode(node);
            }
        }
    }

    protected void addEdge(EdgeImpl edge) {
        if (views.length > 0) {
            for (GraphViewImpl view : views) {
                view.ensureEdgeVectorSize(edge);
            }
        }
    }

    protected void removeEdge(EdgeImpl edge) {
        if (views.length > 0) {
            for (GraphViewImpl view : views) {
                view.removeEdge(edge);
            }
        }
    }

    protected int addView(final GraphViewImpl view) {
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
        view.storeId = id;
        return id;
    }

    protected void removeView(final GraphViewImpl view) {
        checkViewExist(view);

        int id = view.storeId;
        views[id] = null;
        garbageQueue.add(id);
        view.storeId = NULL_VIEW;

        view.destroyAllObservers();
    }

    private void ensureArraySize(int index) {
        if (index >= views.length) {
            GraphViewImpl[] newArray = new GraphViewImpl[index + 1];
            System.arraycopy(views, 0, newArray, 0, views.length);
            views = newArray;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Arrays.deepHashCode(this.views);
        hash = 67 * hash + this.length;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GraphViewStore other = (GraphViewStore) obj;
        if (!Arrays.deepEquals(this.views, other.views)) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        return true;
    }

    private void checkNonNullViewObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof GraphViewImpl)) {
            throw new ClassCastException("View must be a GraphViewImpl object");
        }
    }

    private void checkViewExist(final GraphViewImpl view) {
        int id = view.storeId;
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
