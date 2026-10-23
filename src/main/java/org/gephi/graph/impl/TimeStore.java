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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;

public class TimeStore {

    protected final GraphStore graphStore;
    // Lock (optional)
    protected final TableLockImpl lock;
    // Store
    protected final TimeIndexStore nodeIndexStore;
    protected final TimeIndexStore edgeIndexStore;
    // True if any node or edge time key is indexed. Written under the lock, read without it.
    private volatile boolean dynamic;

    public TimeStore(GraphStore store, boolean indexed) {
        this.graphStore = store;
        this.lock = store != null && store.configuration.isEnableAutoLocking() ? new TableLockImpl() : null;

        TimeRepresentation timeRepresentation = GraphStoreConfiguration.DEFAULT_TIME_REPRESENTATION;
        if (store != null) {
            timeRepresentation = store.configuration.getTimeRepresentation();
        }
        if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
            nodeIndexStore = new IntervalIndexStore<>(Node.class, lock, indexed, this::updateDynamic);
            edgeIndexStore = new IntervalIndexStore<>(Edge.class, lock, indexed, this::updateDynamic);
        } else {
            nodeIndexStore = new TimestampIndexStore<>(Node.class, lock, indexed, this::updateDynamic);
            edgeIndexStore = new TimestampIndexStore<>(Edge.class, lock, indexed, this::updateDynamic);
        }
    }

    public double getMin(Graph graph) {
        if (!hasIndex()) {
            return Double.NEGATIVE_INFINITY;
        }
        return min(nodeIndexStore.getIndex(graph).getMinTimestamp(), edgeIndexStore.getIndex(graph).getMinTimestamp());
    }

    public double getMax(Graph graph) {
        if (!hasIndex()) {
            return Double.POSITIVE_INFINITY;
        }
        return max(nodeIndexStore.getIndex(graph).getMaxTimestamp(), edgeIndexStore.getIndex(graph).getMaxTimestamp());
    }

    /**
     * Gets the minimum and maximum time of the node and edge indexes of the graph's view. The four values are read in
     * one table lock hold, so they come from a single state of the time store.
     * <p>
     * For a view, the caller holds the graph read lock, as getting the view indexes may create them.
     *
     * @param graph graph to get the bounds of
     * @return time bounds, infinite if empty or if time indexing is disabled
     */
    public Interval getBounds(Graph graph) {
        if (!hasIndex()) {
            return Interval.INFINITY_INTERVAL;
        }
        TimeIndexImpl nodeIndex = (TimeIndexImpl) nodeIndexStore.getIndex(graph);
        TimeIndexImpl edgeIndex = (TimeIndexImpl) edgeIndexStore.getIndex(graph);
        lock();
        try {
            return new Interval(min(nodeIndex.getMinTimestampUnlocked(), edgeIndex.getMinTimestampUnlocked()),
                    max(nodeIndex.getMaxTimestampUnlocked(), edgeIndex.getMaxTimestampUnlocked()));
        } finally {
            unlock();
        }
    }

    public boolean hasIndex() {
        return nodeIndexStore.hasIndex() && edgeIndexStore.hasIndex();
    }

    private static double min(double nodeMin, double edgeMin) {
        if (Double.isInfinite(nodeMin)) {
            return edgeMin;
        }
        if (Double.isInfinite(edgeMin)) {
            return nodeMin;
        }
        return Math.min(nodeMin, edgeMin);
    }

    private static double max(double nodeMax, double edgeMax) {
        if (Double.isInfinite(nodeMax)) {
            return edgeMax;
        }
        if (Double.isInfinite(edgeMax)) {
            return nodeMax;
        }
        return Math.max(nodeMax, edgeMax);
    }

    public boolean isEmpty() {
        return !dynamic;
    }

    private void updateDynamic() {
        boolean value = nodeIndexStore.size() > 0 || edgeIndexStore.size() > 0;
        if (dynamic != value) {
            dynamic = value;
        }
    }

    public void clear() {
        lock();
        try {
            nodeIndexStore.clear();
            edgeIndexStore.clear();
        } finally {
            unlock();
        }
    }

    public void clearEdges() {
        edgeIndexStore.clear();
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 79 * hash + (this.nodeIndexStore != null ? this.nodeIndexStore.deepHashCode() : 0);
        hash = 79 * hash + (this.edgeIndexStore != null ? this.edgeIndexStore.deepHashCode() : 0);
        return hash;
    }

    public boolean deepEquals(TimeStore obj) {
        if (obj == null) {
            return false;
        }
        if (this.nodeIndexStore != obj.nodeIndexStore && (this.nodeIndexStore == null || !this.nodeIndexStore
                .deepEquals(obj.nodeIndexStore))) {
            return false;
        }
        if (this.edgeIndexStore != obj.edgeIndexStore && (this.edgeIndexStore == null || !this.edgeIndexStore
                .deepEquals(obj.edgeIndexStore))) {
            return false;
        }
        return true;
    }

    private void lock() {
        if (lock != null) {
            lock.lock();
        }
    }

    private void unlock() {
        if (lock != null) {
            lock.unlock();
        }
    }
}
