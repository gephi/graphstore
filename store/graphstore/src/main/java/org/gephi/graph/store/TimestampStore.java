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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author mbastian
 */
public class TimestampStore {

    protected final GraphStore graphStore;
    //Lock (optional
    protected final GraphLock lock;
    //Store
    protected final TimestampMap nodeMap;
    protected final TimestampMap edgeMap;
    protected final TimestampIndexStore<Node> nodeIndexStore;
    protected final TimestampIndexStore<Edge> edgeIndexStore;

    public TimestampStore(GraphStore store, GraphLock graphLock) {
        lock = graphLock;
        graphStore = store;
        nodeMap = new TimestampMap();
        edgeMap = new TimestampMap();
        nodeIndexStore = new TimestampIndexStore<Node>(this, nodeMap);
        edgeIndexStore = new TimestampIndexStore<Edge>(this, edgeMap);
    }

    public double getMin(Graph graph) {
        double nodeMin = nodeIndexStore.getIndex(graph).getMinTimestamp();
        double edgeMin = edgeIndexStore.getIndex(graph).getMinTimestamp();
        if (Double.isInfinite(nodeMin)) {
            return edgeMin;
        }
        if (Double.isInfinite(edgeMin)) {
            return nodeMin;
        }
        return Math.min(nodeMin, edgeMin);
    }

    public double getMax(Graph graph) {
        double nodeMax = nodeIndexStore.getIndex(graph).getMaxTimestamp();
        double edgeMax = edgeIndexStore.getIndex(graph).getMaxTimestamp();
        if (Double.isInfinite(nodeMax)) {
            return edgeMax;
        }
        if (Double.isInfinite(edgeMax)) {
            return nodeMax;
        }
        return Math.max(nodeMax, edgeMax);
    }

    public boolean isEmpty() {
        return nodeMap.size() == 0 && edgeMap.size() == 0;
    }

    public void clear() {
        nodeMap.clear();
        edgeMap.clear();
    }

    public void clearEdges() {
        edgeMap.clear();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.nodeMap != null ? this.nodeMap.hashCode() : 0);
        hash = 79 * hash + (this.edgeMap != null ? this.edgeMap.hashCode() : 0);
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
        final TimestampStore other = (TimestampStore) obj;
        if (this.nodeMap != other.nodeMap && (this.nodeMap == null || !this.nodeMap.equals(other.nodeMap))) {
            return false;
        }
        if (this.edgeMap != other.edgeMap && (this.edgeMap == null || !this.edgeMap.equals(other.edgeMap))) {
            return false;
        }
        return true;
    }
}
