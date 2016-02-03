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

import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;

public class GraphFactoryImpl implements GraphFactory {

    protected enum AssignConfiguration {
        STRING, INTEGER, DISABLED
    }

    protected final AtomicInteger NODE_IDS = new AtomicInteger();
    protected final AtomicInteger EDGE_IDS = new AtomicInteger();
    // Config
    protected AssignConfiguration nodeAssignConfiguration;
    protected AssignConfiguration edgeAssignConfiguration;
    // Store
    protected final GraphStore store;

    public GraphFactoryImpl(GraphStore store) {
        this.store = store;
        this.nodeAssignConfiguration = getAssignConfiguration(AttributeUtils.getStandardizedType(store.configuration
                .getNodeIdType()));
        this.edgeAssignConfiguration = getAssignConfiguration(AttributeUtils.getStandardizedType(store.configuration
                .getEdgeIdType()));
    }

    @Override
    public Edge newEdge(Node source, Node target) {
        return new EdgeImpl(nextEdgeId(), store, (NodeImpl) source, (NodeImpl) target, EdgeTypeStore.NULL_LABEL,
                GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT, true);
    }

    @Override
    public Edge newEdge(Node source, Node target, boolean directed) {
        return new EdgeImpl(nextEdgeId(), store, (NodeImpl) source, (NodeImpl) target, EdgeTypeStore.NULL_LABEL,
                GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT, directed);
    }

    @Override
    public Edge newEdge(Node source, Node target, int type, boolean directed) {
        return new EdgeImpl(nextEdgeId(), store, (NodeImpl) source, (NodeImpl) target, type,
                GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT, directed);
    }

    @Override
    public Edge newEdge(Node source, Node target, int type, double weight, boolean directed) {
        return new EdgeImpl(nextEdgeId(), store, (NodeImpl) source, (NodeImpl) target, type, weight, directed);
    }

    @Override
    public Edge newEdge(Object id, Node source, Node target, int type, double weight, boolean directed) {
        EdgeImpl res = new EdgeImpl(id, store, (NodeImpl) source, (NodeImpl) target, type, weight, directed);
        switch (edgeAssignConfiguration) {
            case INTEGER:
                Integer idInt = (Integer) id;
                if (idInt >= EDGE_IDS.get()) {
                    EDGE_IDS.set(idInt + 1);
                }
                break;
            case STRING:
                String idStr = (String) id;
                if (isNumeric(idStr)) {
                    Integer idStrParsed = Integer.parseInt(idStr);
                    if (idStrParsed >= EDGE_IDS.get()) {
                        EDGE_IDS.set(idStrParsed + 1);
                    }
                }
                break;
        }
        return res;
    }

    @Override
    public Node newNode() {
        return new NodeImpl(nextNodeId(), store);
    }

    @Override
    public Node newNode(Object id) {
        NodeImpl res = new NodeImpl(id, store);
        switch (nodeAssignConfiguration) {
            case INTEGER:
                Integer idInt = (Integer) id;
                if (idInt >= NODE_IDS.get()) {
                    NODE_IDS.set(idInt + 1);
                }
                break;
            case STRING:
                String idStr = (String) id;
                if (isNumeric(idStr)) {
                    Integer idStrParsed = Integer.parseInt(idStr);
                    if (idStrParsed >= NODE_IDS.get()) {
                        NODE_IDS.set(idStrParsed + 1);
                    }
                }
                break;
        }
        return res;
    }

    private Object nextNodeId() {
        switch (nodeAssignConfiguration) {
            case INTEGER:
                return NODE_IDS.getAndIncrement();
            case STRING:
                return String.valueOf(NODE_IDS.getAndIncrement());
            case DISABLED:
            default:
                throw new UnsupportedOperationException(
                        "Automatic node ids assignement isn't available for this type: '" + store.configuration
                                .getNodeIdType().getName() + "'");
        }
    }

    private Object nextEdgeId() {
        switch (edgeAssignConfiguration) {
            case INTEGER:
                return EDGE_IDS.getAndIncrement();
            case STRING:
                return String.valueOf(EDGE_IDS.getAndIncrement());
            case DISABLED:
            default:
                throw new UnsupportedOperationException(
                        "Automatic edge ids assignement isn't available for this type: '" + store.configuration
                                .getEdgeIdType().getName() + "'");
        }
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

    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        char[] data = str.toCharArray();
        if (data.length <= 0 || data.length > 9) {
            return false;
        }
        int index = 0;
        if (data[0] == '-' && data.length > 1) {
            index = 1;
        }
        for (; index < data.length; index++) {
            if (data[index] < '0' || data[index] > '9') {
                return false;
            }
        }
        return true;
    }

    public int deepHashCode() {
        int hash = 3;
        Integer node = this.NODE_IDS.get();
        Integer edge = this.EDGE_IDS.get();
        hash = 59 * hash + node.hashCode();
        hash = 59 * hash + edge.hashCode();
        return hash;
    }

    public boolean deepEquals(GraphFactoryImpl obj) {
        if (obj == null) {
            return false;
        }
        Integer node = this.NODE_IDS.get();
        Integer edge = this.EDGE_IDS.get();
        Integer otherNode = obj.NODE_IDS.get();
        Integer otherEdge = obj.EDGE_IDS.get();
        if (this.NODE_IDS != obj.NODE_IDS && (!node.equals(otherNode))) {
            return false;
        }
        if (this.EDGE_IDS != obj.EDGE_IDS && (!edge.equals(otherEdge))) {
            return false;
        }
        return true;
    }

    public void resetConfiguration() {
        this.nodeAssignConfiguration = getAssignConfiguration(AttributeUtils.getStandardizedType(store.configuration
                .getNodeIdType()));
        this.edgeAssignConfiguration = getAssignConfiguration(AttributeUtils.getStandardizedType(store.configuration
                .getEdgeIdType()));
    }

    protected final AssignConfiguration getAssignConfiguration(Class type) {
        if (type.equals(Integer.class)) {
            return AssignConfiguration.INTEGER;
        } else if (type.equals(String.class)) {
            return AssignConfiguration.STRING;
        } else {
            return AssignConfiguration.DISABLED;
        }
    }
}
