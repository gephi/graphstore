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

    public boolean isEmpty() {
        return nodeMap.size() == 0 && edgeMap.size() == 0;
    }

    public void clear() {
    }

    public void clearEdges() {
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
}
