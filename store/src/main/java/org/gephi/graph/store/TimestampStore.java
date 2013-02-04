package org.gephi.graph.store;

import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;
import it.unimi.dsi.fastutil.doubles.Double2IntRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2IntSortedMap;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import org.gephi.attribute.time.TimestampSet;

/**
 *
 * @author mbastian
 */
public class TimestampStore {

    //Const
    public static final int NULL_INDEX = -1;
    //Data
    protected final GraphStore graphStore;
    protected final Double2IntMap timestampMap;
    protected final Double2IntSortedMap timestampSortedMap;
    protected final IntPriorityQueue garbageQueue;
    protected TimestampIndexEntry[] timestamps;
    protected int length;
    protected int nodeCount;
    protected int edgeCount;

    public TimestampStore(GraphStore store) {
        graphStore = store;
        timestampMap = new Double2IntOpenHashMap();
        timestampSortedMap = new Double2IntRBTreeMap();
        timestampMap.defaultReturnValue(NULL_INDEX);
        garbageQueue = new IntHeapPriorityQueue(0);
        timestamps = new TimestampIndexEntry[0];
    }

    public int getTimestampIndex(double timestamp) {
        int index = timestampMap.get(timestamp);
        if (index == NULL_INDEX) {
            index = addTimestamp(timestamp);
        }
        return index;
    }

    public double getMin() {
        if (size() > 0) {
            return timestampSortedMap.firstDoubleKey();
        }
        return Double.NEGATIVE_INFINITY;
    }

    public double getMax() {
        if (size() > 0) {
            return timestampSortedMap.lastDoubleKey();
        }
        return Double.POSITIVE_INFINITY;
    }

    public Iterable<NodeImpl> getNodes(double timestamp) {
        checkDouble(timestamp);
        
        int index = timestampMap.get(timestamp);
        if (index != NULL_INDEX) {
            TimestampIndexEntry ts = timestamps[index];
            if (ts != null) {
                return ts.nodeSet;
            }
        }
        return new ArrayList<NodeImpl>();
    }

    public Iterable<NodeImpl> getNodes(double from, double to) {
        ObjectSet<NodeImpl> nodes = new ObjectOpenHashSet<NodeImpl>();
        for (Double2IntMap.Entry entry : timestampSortedMap.tailMap(from).double2IntEntrySet()) {
            double timestamp = entry.getDoubleKey();
            int index = entry.getIntValue();
            if (timestamp <= to) {
                TimestampIndexEntry ts = timestamps[index];
                if (ts != null) {
                    nodes.addAll(ts.nodeSet);
                }
            } else {
                break;
            }
        }
        return nodes;
    }
    
    public Iterable<EdgeImpl> getEdges(double timestamp) {
        int index = timestampMap.get(timestamp);
        if (index != NULL_INDEX) {
            TimestampIndexEntry ts = timestamps[index];
            if (ts != null) {
                return ts.edgeSet;
            }
        }
        return new ArrayList<EdgeImpl>();
    }

    public Iterable<EdgeImpl> getEdges(double from, double to) {
        ObjectSet<EdgeImpl> edges = new ObjectOpenHashSet<EdgeImpl>();
        for (Double2IntMap.Entry entry : timestampSortedMap.tailMap(from).double2IntEntrySet()) {
            double timestamp = entry.getDoubleKey();
            int index = entry.getIntValue();
            if (timestamp <= to) {
                TimestampIndexEntry ts = timestamps[index];
                if (ts != null) {
                    edges.addAll(ts.edgeSet);
                }
            } else {
                break;
            }
        }
        return edges;
    }

    public double[] getTimestamps(int[] indices) {
        int indicesLength = indices.length;
        double[] res = new double[indicesLength];
        for (int i = 0; i < indicesLength; i++) {
            int index = indices[i];
            checkIndex(index);
            TimestampIndexEntry entry = timestamps[index];
            if (entry == null) {
                throw new IllegalArgumentException("The timestamp index can't be found");
            }
            res[i] = entry.timestamp;
        }
        return res;
    }

    public boolean contains(double timestamp) {
        checkDouble(timestamp);

        return timestampMap.containsKey(timestamp);
    }
    
    public boolean hasNodes() {
        return nodeCount > 0;
    }
    
    public boolean hasEdges() {
        return edgeCount > 0;
    }

    public void clear() {
        timestampMap.clear();
        timestampSortedMap.clear();
        timestamps = new TimestampIndexEntry[0];
        garbageQueue.clear();
        length = 0;
        nodeCount = 0;
        edgeCount = 0;
    }
    
    public void clearEdges() {
        if(nodeCount == 0) {
            clear();
        } else {
            for(TimestampIndexEntry entry : timestamps) {
                if(entry != null) {
                    entry.edgeSet.clear();
                }
            }
            edgeCount = 0;
        }
    }

    public int size() {
        return length - garbageQueue.size();
    }

    public void addElement(int timestampIndex, ElementImpl element) {
        if (element instanceof NodeImpl) {
            addNode(timestampIndex, (NodeImpl) element);
        } else {
            addEdge(timestampIndex, (EdgeImpl) element);
        }
    }

    public void removeElement(int timestampIndex, ElementImpl element) {
        if (element instanceof NodeImpl) {
            removeNode(timestampIndex, (NodeImpl) element);
        } else {
            removeEdge(timestampIndex, (EdgeImpl) element);
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

    protected void indexNode(NodeImpl node) {
        TimestampSet set = node.timestampSet;
        if (set != null) {
            int[] ts = set.getTimestamps();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestamp = ts[i];
                addNode(timestamp, node);
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
                addEdge(timestamp, edge);
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
                removeNode(timestamp, node);
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
                removeEdge(timestamp, edge);
            }
        }
    }

    protected void addNode(int timestampIndex, NodeImpl node) {
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry.addNode(node)) {
            nodeCount++;
        }
    }

    protected void addEdge(int timestampIndex, EdgeImpl edge) {
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry.addEdge(edge)) {
            edgeCount++;
        }
    }

    protected void removeNode(int timestampIndex, NodeImpl node) {
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry.removeNode(node)) {
            nodeCount--;
        }
    }

    protected void removeEdge(int timestampIndex, EdgeImpl edge) {
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry.removeEdge(edge)) {
            edgeCount--;
        }
    }

    protected int addTimestamp(final double timestamp) {
        checkDouble(timestamp);

        int id;
        if (!garbageQueue.isEmpty()) {
            id = garbageQueue.dequeueInt();
        } else {
            id = length++;
            ensureArraySize(id);
        }
        timestampMap.put(timestamp, id);
        timestampSortedMap.put(timestamp, id);
        timestamps[id] = new TimestampIndexEntry(timestamp);
        return id;
    }

    protected void removeTimestamp(final double timestamp) {
        checkDouble(timestamp);

        int id = timestampMap.get(timestamp);
        garbageQueue.enqueue(id);

        timestampMap.remove(timestamp);
        timestampSortedMap.remove(timestamp);
        timestamps[id] = null;
    }

    private void ensureArraySize(int index) {
        if (index >= timestamps.length) {
            TimestampIndexEntry[] newArray = new TimestampIndexEntry[index + 1];
            System.arraycopy(timestamps, 0, newArray, 0, timestamps.length);
            timestamps = newArray;
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= length) {
            throw new IllegalArgumentException("The type must be included between 0 and 65535");
        }
    }

    private void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can' be NaN or infinity");
        }
    }

    protected static class TimestampIndexEntry {

        protected final double timestamp;
        protected final ObjectSet<NodeImpl> nodeSet;
        protected final ObjectSet<EdgeImpl> edgeSet;

        public TimestampIndexEntry(double timestamp) {
            this.timestamp = timestamp;
            nodeSet = new ObjectOpenHashSet<NodeImpl>();
            edgeSet = new ObjectOpenHashSet<EdgeImpl>();
        }

        public boolean addNode(NodeImpl node) {
            return nodeSet.add(node);
        }

        public boolean addEdge(EdgeImpl edge) {
            return edgeSet.add(edge);
        }

        public boolean removeNode(NodeImpl node) {
            return nodeSet.remove(node);
        }

        public boolean removeEdge(EdgeImpl edge) {
            return edgeSet.remove(edge);
        }
    }
}
