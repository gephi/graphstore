package org.gephi.graph.store;

import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;
import it.unimi.dsi.fastutil.doubles.Double2IntRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2IntSortedMap;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.gephi.attribute.time.TimestampSet;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;

/**
 *
 * @author mbastian
 */
public class TimestampStore {

    //Const
    public static final int NULL_INDEX = -1;
    //Lock (optional
    protected final GraphLock lock;
    //Timestamp index managament
    protected final Double2IntMap timestampMap;
    protected final Double2IntSortedMap timestampSortedMap;
    protected final IntPriorityQueue garbageQueue;
    protected double[] indexMap;
    protected int length;
    //Index
    protected final TimestampIndexImpl mainIndex;
    protected final Map<GraphView, TimestampIndexImpl> viewIndexes;

    public TimestampStore(GraphLock graphLock) {
        lock = graphLock;
        timestampMap = new Double2IntOpenHashMap();
        timestampMap.defaultReturnValue(NULL_INDEX);
        mainIndex = new TimestampIndexImpl(this, true);
        garbageQueue = new IntHeapPriorityQueue(0);
        viewIndexes = new Object2ObjectOpenHashMap<GraphView, TimestampIndexImpl>();
        timestampSortedMap = new Double2IntRBTreeMap();
        indexMap = new double[0];
    }

    public TimestampIndexImpl getIndex(Graph graph) {
        GraphView view = graph.getView();
        if (view.isMainView()) {
            return mainIndex;
        }
        TimestampIndexImpl viewIndex = viewIndexes.get(graph.getView());
        if (viewIndex == null) {
            viewIndex = createViewIndex(graph);
        }
        return viewIndex;
    }

    protected TimestampIndexImpl createViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't create a view index for the main view");
        }
        TimestampIndexImpl viewIndex = new TimestampIndexImpl(this, false);
        viewIndexes.put(graph.getView(), viewIndex);

        for (Node node : graph.getNodes()) {
            indexNode((NodeImpl) node);
        }
        for (Edge edge : graph.getEdges()) {
            indexEdge((EdgeImpl) edge);
        }

        return viewIndex;
    }

    protected void deleteViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't delete a view index for the main view");
        }
        TimestampIndexImpl index = viewIndexes.remove(graph.getView());
        if (index != null) {
            index.clear();
        }
    }

    //Protected
    public int getTimestampIndex(double timestamp) {
        int index = timestampMap.get(timestamp);
        if (index == NULL_INDEX) {
            index = addTimestamp(timestamp);
        }
        return index;
    }

    public boolean contains(double timestamp) {
        checkDouble(timestamp);

        return timestampMap.containsKey(timestamp);
    }

    public double[] getTimestamps(int[] indices) {
        int indicesLength = indices.length;
        double[] res = new double[indicesLength];
        for (int i = 0; i < indicesLength; i++) {
            int index = indices[i];
            checkIndex(index);
            res[i] = indexMap[i];
        }
        return res;
    }

    public int size() {
        return timestampMap.size();
    }

    public void clear() {
        timestampMap.clear();
        timestampSortedMap.clear();
        garbageQueue.clear();
        indexMap = new double[0];

        mainIndex.clear();

        if (!viewIndexes.isEmpty()) {
            for (TimestampIndexImpl index : viewIndexes.values()) {
                index.clear();
            }
        }
    }

    public void clearEdges() {
        if (!mainIndex.hasNodes()) {
            clear();
        } else {
            mainIndex.clearEdges();

            if (!viewIndexes.isEmpty()) {
                for (TimestampIndexImpl index : viewIndexes.values()) {
                    index.clearEdges();
                }
            }
        }
    }

    public int addElement(double timestamp, ElementImpl element) {
        if (element instanceof NodeImpl) {
            return addNode(timestamp, (NodeImpl) element);
        } else {
            return addEdge(timestamp, (EdgeImpl) element);
        }
    }

    public int removeElement(double timestamp, ElementImpl element) {
        if (element instanceof NodeImpl) {
            return removeNode(timestamp, (NodeImpl) element);
        } else {
            return removeEdge(timestamp, (EdgeImpl) element);
        }
    }

    protected void index(ElementImpl element) {
        if (element instanceof NodeImpl) {
            indexNode((NodeImpl) element);
        } else {
            indexEdge((EdgeImpl) element);
        }
    }

    protected void clear(ElementImpl element) {
        if (element instanceof NodeImpl) {
            clearNode((NodeImpl) element);
        } else {
            clearEdge((EdgeImpl) element);
        }
    }

    //private
    protected void indexNode(NodeImpl node) {
        TimestampSet set = node.timestampSet;
        if (set != null) {
            int[] ts = set.getTimestamps();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestamp = ts[i];
                mainIndex.addNode(timestamp, node);
            }

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    if (graph.contains(node)) {
                        for (int i = 0; i < tsLength; i++) {
                            int timestamp = ts[i];
                            entry.getValue().addNode(timestamp, node);
                        }
                    }
                }
            }
        }
    }

    protected void indexEdge(EdgeImpl edge) {
        TimestampSet set = edge.timestampSet;
        if (set != null) {
            int[] ts = set.getTimestamps();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestamp = ts[i];
                mainIndex.addEdge(timestamp, edge);
            }

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    if (graph.contains(edge)) {
                        for (int i = 0; i < tsLength; i++) {
                            int timestamp = ts[i];
                            entry.getValue().addEdge(timestamp, edge);
                        }
                    }
                }
            }
        }
    }

    protected void clearNode(NodeImpl node) {
        TimestampSet set = node.timestampSet;
        if (set != null) {
            int[] ts = set.getTimestamps();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestamp = ts[i];
                mainIndex.removeNode(timestamp, node);

                if (mainIndex.timestamps[i] == null) {
                    removeTimestamp(indexMap[i]);
                }
            }

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    if (graph.contains(node)) {
                        for (int i = 0; i < tsLength; i++) {
                            int timestamp = ts[i];
                            entry.getValue().removeNode(timestamp, node);
                        }
                    }
                }
            }
        }
    }

    protected void clearEdge(EdgeImpl edge) {
        TimestampSet set = edge.timestampSet;
        if (set != null) {
            int[] ts = set.getTimestamps();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestamp = ts[i];
                mainIndex.removeEdge(timestamp, edge);

                if (mainIndex.timestamps[i] == null) {
                    removeTimestamp(indexMap[i]);
                }
            }

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    if (graph.contains(edge)) {
                        for (int i = 0; i < tsLength; i++) {
                            int timestamp = ts[i];
                            entry.getValue().removeEdge(timestamp, edge);
                        }
                    }
                }
            }
        }
    }

    protected int addNode(double timestamp, NodeImpl node) {
        int timestampIndex = getTimestampIndex(timestamp);
        mainIndex.addNode(timestampIndex, node);

        if (!viewIndexes.isEmpty()) {
            for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                DirectedSubgraph graph = graphView.getDirectedGraph();
                if (graph.contains(node)) {
                    entry.getValue().addNode(timestampIndex, node);
                }
            }
        }

        return timestampIndex;
    }

    protected int addEdge(double timestamp, EdgeImpl edge) {
        int timestampIndex = getTimestampIndex(timestamp);
        mainIndex.addEdge(timestampIndex, edge);

        if (!viewIndexes.isEmpty()) {
            for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                DirectedSubgraph graph = graphView.getDirectedGraph();
                if (graph.contains(edge)) {
                    entry.getValue().addEdge(timestampIndex, edge);
                }
            }
        }

        return timestampIndex;
    }

    protected int removeNode(double timestamp, NodeImpl node) {
        int timestampIndex = getTimestampIndex(timestamp);
        mainIndex.removeNode(timestampIndex, node);

        if (!viewIndexes.isEmpty()) {
            for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                DirectedSubgraph graph = graphView.getDirectedGraph();
                if (graph.contains(node)) {
                    entry.getValue().removeNode(timestampIndex, node);
                }
            }
        }

        if (mainIndex.timestamps[timestampIndex] == null) {
            removeTimestamp(timestamp);
        }

        return timestampIndex;
    }

    protected int removeEdge(double timestamp, EdgeImpl edge) {
        int timestampIndex = getTimestampIndex(timestamp);
        mainIndex.removeEdge(timestampIndex, edge);

        if (!viewIndexes.isEmpty()) {
            for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                DirectedSubgraph graph = graphView.getDirectedGraph();
                if (graph.contains(edge)) {
                    entry.getValue().removeEdge(timestampIndex, edge);
                }
            }
        }

        if (mainIndex.timestamps[timestampIndex] == null) {
            removeTimestamp(timestamp);
        }

        return timestampIndex;
    }

    protected int addTimestamp(final double timestamp) {
        checkDouble(timestamp);

        int id;
        if (!garbageQueue.isEmpty()) {
            id = garbageQueue.dequeueInt();
        } else {
            id = length++;
        }
        timestampMap.put(timestamp, id);
        timestampSortedMap.put(timestamp, id);
        ensureArraySize(id);
        indexMap[id] = timestamp;

        return id;
    }

    protected void removeTimestamp(final double timestamp) {
        checkDouble(timestamp);

        int id = timestampMap.get(timestamp);
        garbageQueue.enqueue(id);
        timestampMap.remove(timestamp);
        timestampSortedMap.remove(timestamp);
        indexMap[id] = Double.NaN;
    }

    private void ensureArraySize(int index) {
        if (index >= indexMap.length) {
            double[] newArray = new double[index + 1];
            System.arraycopy(indexMap, 0, newArray, 0, indexMap.length);
            indexMap = newArray;
        }
    }

    void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can' be NaN or infinity");
        }
    }

    void checkIndex(int index) {
        if (index < 0 || index >= length) {
            throw new IllegalArgumentException("The timestamp store index is out of bounds");
        }
    }
}
