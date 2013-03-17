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

import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.gephi.attribute.api.TimestampIndex;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;

/**
 *
 * @author mbastian
 */
public class TimestampIndexImpl implements TimestampIndex {
    //Const

    protected static final int NULL_INDEX = -1;
    //Data
    protected final GraphLock lock;
    protected final TimestampStore timestampStore;
    protected final boolean mainIndex;
    protected TimestampIndexEntry[] timestamps;
    protected int nodeCount;
    protected int edgeCount;

    public TimestampIndexImpl(TimestampStore store, boolean main) {
        timestampStore = store;
        mainIndex = main;
        lock = store.lock;

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

    @Override
    public NodeIterable getNodes(double timestamp) {
        checkDouble(timestamp);

        readLock();
        int index = timestampStore.timestampMap.get(timestamp);
        if (index != NULL_INDEX) {
            TimestampIndexEntry ts = timestamps[index];
            if (ts != null) {
                return new NodeIterableImpl(new NodeIteratorImpl(ts.nodeSet.iterator()));
            }
        }
        readUnlock();
        return NodeIterable.EMPTY;
    }

    @Override
    public NodeIterable getNodes(double from, double to) {
        checkDouble(from);
        checkDouble(to);

        readLock();
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
        if (!nodes.isEmpty()) {
            return new NodeIterableImpl(new NodeIteratorImpl(nodes.iterator()));
        }
        return NodeIterable.EMPTY;
    }

    @Override
    public EdgeIterable getEdges(double timestamp) {
        checkDouble(timestamp);

        readLock();
        int index = timestampStore.timestampMap.get(timestamp);
        if (index != NULL_INDEX) {
            TimestampIndexEntry ts = timestamps[index];
            if (ts != null) {
                return new EdgeIterableImpl(new EdgeIteratorImpl(ts.edgeSet.iterator()));
            }
        }
        readUnlock();
        return EdgeIterable.EMPTY;
    }

    @Override
    public EdgeIterable getEdges(double from, double to) {
        checkDouble(from);
        checkDouble(to);

        readLock();
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
        if (!edges.isEmpty()) {
            return new EdgeIterableImpl(new EdgeIteratorImpl(edges.iterator()));
        } else {
            return EdgeIterable.EMPTY;
        }
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

    private void readLock() {
        if (lock != null) {
            lock.readLock();
        }
    }

    private void readUnlock() {
        if (lock != null) {
            lock.readUnlock();
        }
    }

    private void writeLock() {
        if (lock != null) {
            lock.writeLock();
        }
    }

    private void writeUnlock() {
        if (lock != null) {
            lock.writeUnlock();
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

    protected class NodeIteratorImpl implements Iterator<Node> {

        private final ObjectIterator<NodeImpl> itr;

        public NodeIteratorImpl(ObjectIterator<NodeImpl> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Node next() {
            return itr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    protected class EdgeIteratorImpl implements Iterator<Edge> {

        private final ObjectIterator<EdgeImpl> itr;

        public EdgeIteratorImpl(ObjectIterator<EdgeImpl> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Edge next() {
            return itr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    protected class NodeIterableImpl implements NodeIterable {

        protected final Iterator<Node> iterator;

        public NodeIterableImpl(Iterator<Node> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Iterator<Node> iterator() {
            return iterator;
        }

        @Override
        public Node[] toArray() {
            List<Node> list = new ArrayList<Node>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list.toArray(new Node[0]);
        }

        @Override
        public Collection<Node> toCollection() {
            List<Node> list = new ArrayList<Node>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list;
        }

        @Override
        public void doBreak() {
            readUnlock();
        }
    }

    protected class EdgeIterableImpl implements EdgeIterable {

        protected final Iterator<Edge> iterator;

        public EdgeIterableImpl(Iterator<Edge> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Iterator<Edge> iterator() {
            return iterator;
        }

        @Override
        public Edge[] toArray() {
            List<Edge> list = new ArrayList<Edge>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list.toArray(new Edge[0]);
        }

        @Override
        public Collection<Edge> toCollection() {
            List<Edge> list = new ArrayList<Edge>();
            for (; iterator.hasNext();) {
                list.add(iterator.next());
            }
            return list;
        }

        @Override
        public void doBreak() {
            readUnlock();
        }
    }
}
