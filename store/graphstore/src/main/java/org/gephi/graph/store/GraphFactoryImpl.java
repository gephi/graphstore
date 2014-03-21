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

import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;

/**
 *
 * @author mbastian
 */
public class GraphFactoryImpl implements GraphFactory {

    protected final AtomicInteger NODE_IDS = new AtomicInteger();
    protected final AtomicInteger EDGE_IDS = new AtomicInteger();
    //Store
    protected final GraphStore store;

    public GraphFactoryImpl(GraphStore store) {
        this.store = store;
    }

    @Override
    public Edge newEdge(Node source, Node target) {
        return new EdgeImpl(EDGE_IDS.getAndIncrement(), store, (NodeImpl) source, (NodeImpl) target, EdgeTypeStore.NULL_LABEL, 1.0, true);
    }

    @Override
    public Edge newEdge(Node source, Node target, boolean directed) {
        return new EdgeImpl(EDGE_IDS.getAndIncrement(), store, (NodeImpl) source, (NodeImpl) target, EdgeTypeStore.NULL_LABEL, 1.0, directed);
    }

    @Override
    public Edge newEdge(Node source, Node target, int type, boolean directed) {
        return new EdgeImpl(EDGE_IDS.getAndIncrement(), store, (NodeImpl) source, (NodeImpl) target, type, 1.0, directed);
    }

    @Override
    public Edge newEdge(Node source, Node target, int type, double weight, boolean directed) {
        return new EdgeImpl(EDGE_IDS.getAndIncrement(), store, (NodeImpl) source, (NodeImpl) target, type, weight, directed);
    }

    @Override
    public Edge newEdge(Object id, Node source, Node target, int type, double weight, boolean directed) {
        return new EdgeImpl(id, store, (NodeImpl) source, (NodeImpl) target, type, weight, directed);
    }

    @Override
    public Node newNode() {
        return new NodeImpl(NODE_IDS.getAndIncrement(), store);
    }

    @Override
    public Node newNode(Object id) {
        return new NodeImpl(id, store);
    }

    protected int getNodeCounter() {
        return NODE_IDS.get();
    }

    protected int getEdgeCounter() {
        return EDGE_IDS.get();
    }

    protected void setNodeCounter(int count) {
        NODE_IDS.set(count);
    }

    protected void setEdgeCounter(int count) {
        EDGE_IDS.set(count);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        Integer node = this.NODE_IDS.get();
        Integer edge = this.EDGE_IDS.get();
        hash = 59 * hash + node.hashCode();
        hash = 59 * hash + edge.hashCode();
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
        final GraphFactoryImpl other = (GraphFactoryImpl) obj;
        Integer node = this.NODE_IDS.get();
        Integer edge = this.EDGE_IDS.get();
        Integer otherNode = other.NODE_IDS.get();
        Integer otherEdge = other.EDGE_IDS.get();
        if (this.NODE_IDS != other.NODE_IDS && (!node.equals(otherNode))) {
            return false;
        }
        if (this.EDGE_IDS != other.EDGE_IDS && (!edge.equals(otherEdge))) {
            return false;
        }
        return true;
    }
}
