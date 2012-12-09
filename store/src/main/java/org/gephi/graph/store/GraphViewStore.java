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
        this.graphStore = graphStore;
        this.views = new GraphViewImpl[DEFAULT_VIEWS];
        this.garbageQueue = new IntHeapPriorityQueue(DEFAULT_VIEWS);
    }

    public GraphViewImpl createView() {
        GraphViewImpl graphView = new GraphViewImpl(graphStore);
        addView(graphView);
        return graphView;
    }

    public DirectedSubgraph getDirectedGraph(GraphView view) {
        checkNonNullViewObject(view);

        return ((GraphViewImpl) view).getDirectedGraph();
    }

    public UndirectedSubgraph getUndirectedGraph(GraphView view) {
        checkNonNullViewObject(view);

        return ((GraphViewImpl) view).getUndirectedGraph();
    }

    protected int addView(final GraphViewImpl view) {
        checkNonNullViewObject(view);

        int id;
        if (!garbageQueue.isEmpty()) {
            id = garbageQueue.dequeueInt();
        } else {
            id = length++;
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
