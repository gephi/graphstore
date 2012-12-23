package org.gephi.graph.store;

import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.UndirectedSubgraph;

/**
 *
 * @author mbastian
 */
public class GraphViewStore {

    //Const
    public static final int NULL_VIEW = -1;
    public static final int DEFAULT_VIEWS = 0;
    //Data
    protected final IntPriorityQueue garbageQueue;
    protected final GraphStore graphStore;
    protected GraphViewImpl[] views;
    protected int length;

    public GraphViewStore(GraphStore graphStore) {
        if (graphStore == null) {
            throw new NullPointerException();
        }
        this.graphStore = graphStore;
        this.views = new GraphViewImpl[DEFAULT_VIEWS];
        this.garbageQueue = new IntHeapPriorityQueue(DEFAULT_VIEWS);
    }

    public GraphViewImpl createView() {
        graphStore.autoWriteLock();
        try {
            GraphViewImpl graphView = new GraphViewImpl(graphStore);
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

    public DirectedSubgraph getDirectedGraph(GraphView view) {
        checkNonNullViewObject(view);

        return ((GraphViewImpl) view).getDirectedGraph();
    }

    public UndirectedSubgraph getUndirectedGraph(GraphView view) {
        checkNonNullViewObject(view);

        return ((GraphViewImpl) view).getUndirectedGraph();
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
            id = garbageQueue.dequeueInt();
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
        garbageQueue.enqueue(id);
        view.storeId = NULL_VIEW;
    }

    private void ensureArraySize(int index) {
        if (index >= views.length) {
            GraphViewImpl[] newArray = new GraphViewImpl[index + 1];
            System.arraycopy(views, 0, newArray, 0, views.length);
            views = newArray;
        }
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
}
