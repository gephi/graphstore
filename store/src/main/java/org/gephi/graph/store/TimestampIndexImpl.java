/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.store;

import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import org.gephi.attribute.api.TimestampIndex;

/**
 *
 * @author mbastian
 */
public class TimestampIndexImpl implements TimestampIndex {
    //Const
    public static final int NULL_INDEX = -1;
    //Data
    protected final TimestampStore timestampStore;
    protected final boolean mainIndex;
    protected TimestampIndexEntry[] timestamps;
    protected int nodeCount;
    protected int edgeCount;

    public TimestampIndexImpl(TimestampStore store, boolean main) {
        timestampStore = store;
        mainIndex = main;

        timestamps = new TimestampIndexEntry[0];
    }

    @Override
    public double getMinTimestamp() {
        if (mainIndex) {
            Double2IntSortedMap sortedMap = timestampStore.timestampSortedMap;
            if (!sortedMap.isEmpty()) {
                return sortedMap.firstDoubleKey();
            }
        } else {
            Double2IntSortedMap sortedMap = timestampStore.timestampSortedMap;
            if (!sortedMap.isEmpty()) {
                ObjectBidirectionalIterator<Double2IntMap.Entry> bi = sortedMap.double2IntEntrySet().iterator();
                while (bi.hasNext()) {
                    Double2IntMap.Entry entry = bi.next();
                    double timestamp = entry.getDoubleKey();
                    int index = entry.getIntValue();
                    if (index < timestamps.length) {
                        TimestampIndexEntry timestampEntry = timestamps[index];
                        if (timestampEntry != null) {
                            return timestamp;
                        }
                    }
                }
            }
        }
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double getMaxTimestamp() {
        if (mainIndex) {
            Double2IntSortedMap sortedMap = timestampStore.timestampSortedMap;
            if (!sortedMap.isEmpty()) {
                return sortedMap.lastDoubleKey();
            }
        } else {
            Double2IntSortedMap sortedMap = timestampStore.timestampSortedMap;
            if (!sortedMap.isEmpty()) {
                ObjectBidirectionalIterator<Double2IntMap.Entry> bi = sortedMap.double2IntEntrySet().iterator(sortedMap.double2IntEntrySet().last());
                while (bi.hasPrevious()) {
                    Double2IntMap.Entry entry = bi.previous();
                    double timestamp = entry.getDoubleKey();
                    int index = entry.getIntValue();
                    if (index < timestamps.length) {
                        TimestampIndexEntry timestampEntry = timestamps[index];
                        if (timestampEntry != null) {
                            return timestamp;
                        }
                    }
                }
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    public Iterable<NodeImpl> getNodes(double timestamp) {
        checkDouble(timestamp);

        int index = timestampStore.timestampMap.get(timestamp);
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
        Double2IntSortedMap sortedMap = timestampStore.timestampSortedMap;
        if (!sortedMap.isEmpty()) {
            for (Double2IntMap.Entry entry : sortedMap.tailMap(from).double2IntEntrySet()) {
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
        }
        return nodes;
    }

    public Iterable<EdgeImpl> getEdges(double timestamp) {
        int index = timestampStore.timestampMap.get(timestamp);
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
        Double2IntSortedMap sortedMap = timestampStore.timestampSortedMap;
        if (!sortedMap.isEmpty()) {
            for (Double2IntMap.Entry entry : sortedMap.tailMap(from).double2IntEntrySet()) {
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
        }
        return edges;
    }

    public boolean hasNodes() {
        return nodeCount > 0;
    }

    public boolean hasEdges() {
        return edgeCount > 0;
    }

    public void clear() {
        timestamps = new TimestampIndexEntry[0];
        nodeCount = 0;
        edgeCount = 0;
    }

    public void clearEdges() {
        if (nodeCount == 0) {
            clear();
        } else {
            for (int i = 0; i < timestamps.length; i++) {
                TimestampIndexEntry entry = timestamps[i];
                if (entry != null) {
                    entry.edgeSet.clear();
                    if (entry.isEmpty()) {
                        clearEntry(i);
                    }
                }
            }
            edgeCount = 0;
        }
    }

    protected void addNode(int timestampIndex, NodeImpl node) {
        ensureArraySize(timestampIndex);
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry == null) {
            entry = addTimestamp(timestampIndex);
        }
        if (entry.addNode(node)) {
            nodeCount++;
        }
    }

    protected void addEdge(int timestampIndex, EdgeImpl edge) {
        ensureArraySize(timestampIndex);
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry == null) {
            entry = addTimestamp(timestampIndex);
        }
        if (entry.addEdge(edge)) {
            edgeCount++;
        }
    }

    protected void removeNode(int timestampIndex, NodeImpl node) {
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry.removeNode(node)) {
            nodeCount--;
            if (entry.isEmpty()) {
                clearEntry(timestampIndex);
            }
        }
    }

    protected void removeEdge(int timestampIndex, EdgeImpl edge) {
        TimestampIndexEntry entry = timestamps[timestampIndex];
        if (entry.removeEdge(edge)) {
            edgeCount--;
            if (entry.isEmpty()) {
                clearEntry(timestampIndex);
            }
        }
    }

    protected TimestampIndexEntry addTimestamp(final int index) {
        ensureArraySize(index);
        TimestampIndexEntry entry = new TimestampIndexEntry();
        timestamps[index] = entry;
        return entry;
    }

    protected void removeTimestamp(final int index) {
        timestamps[index] = null;
    }

    private void ensureArraySize(int index) {
        if (index >= timestamps.length) {
            TimestampIndexEntry[] newArray = new TimestampIndexEntry[index + 1];
            System.arraycopy(timestamps, 0, newArray, 0, timestamps.length);
            timestamps = newArray;
        }
    }

    private void clearEntry(int index) {
        timestamps[index] = null;
    }

    private void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can' be NaN or infinity");
        }
    }

    protected static class TimestampIndexEntry {

        protected final ObjectSet<NodeImpl> nodeSet;
        protected final ObjectSet<EdgeImpl> edgeSet;

        public TimestampIndexEntry() {
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

        public boolean isEmpty() {
            return nodeSet.isEmpty() && edgeSet.isEmpty();
        }
    }
}
