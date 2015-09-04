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
    protected final TimestampInternalMap nodeMap;
    protected final TimestampInternalMap edgeMap;
    protected final TimestampIndexStore<Node> nodeIndexStore;
    protected final TimestampIndexStore<Edge> edgeIndexStore;

    public TimestampStore(GraphStore store, GraphLock graphLock, boolean indexed) {
        lock = graphLock;
        graphStore = store;
        nodeMap = new TimestampInternalMap();
        edgeMap = new TimestampInternalMap();
        nodeIndexStore = indexed ? new TimestampIndexStore<Node>(this, Node.class, nodeMap) : null;
        edgeIndexStore = indexed ? new TimestampIndexStore<Edge>(this, Edge.class, edgeMap) : null;
    }

    public double getMin(Graph graph) {
        if(nodeIndexStore == null || edgeIndexStore == null) {
            //TODO: Manual calculation
            return Double.NEGATIVE_INFINITY;
        }
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
        if(nodeIndexStore == null || edgeIndexStore == null) {
            //TODO: Manual calculation
            return Double.POSITIVE_INFINITY;
        }
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

    public int deepHashCode() {
        int hash = 3;
        hash = 79 * hash + (this.nodeMap != null ? this.nodeMap.deepHashCode(): 0);
        hash = 79 * hash + (this.edgeMap != null ? this.edgeMap.deepHashCode() : 0);
        return hash;
    }

    public boolean deepEquals(TimestampStore obj) {
        if (obj == null) {
            return false;
        }
        if (this.nodeMap != obj.nodeMap && (this.nodeMap == null || !this.nodeMap.deepEquals(obj.nodeMap))) {
            return false;
        }
        if (this.edgeMap != obj.edgeMap && (this.edgeMap == null || !this.edgeMap.deepEquals(obj.edgeMap))) {
            return false;
        }
        return true;
    }
}
