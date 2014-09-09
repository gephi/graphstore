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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.TextProperties;
import org.gephi.graph.spi.LayoutData;

/**
 *
 * @author mbastian
 */
public class BasicGraphStore implements DirectedGraph {

    protected final BasicNodeStore nodeStore;
    protected final BasicEdgeStore edgeStore;

    public BasicGraphStore() {
        nodeStore = new BasicNodeStore();
        edgeStore = new BasicEdgeStore();
    }

    @Override
    public Edge getEdge(Node source, Node target, int type) {
        return edgeStore.getEdge(source, target, type);
    }

    @Override
    public NodeIterable getPredecessors(Node node) {
        return new NodeIterableWrapper(edgeStore.predecessors((BasicNode) node));
    }

    @Override
    public NodeIterable getPredecessors(Node node, int type) {
        return new NodeIterableWrapper(edgeStore.predecessors((BasicNode) node, type));
    }

    @Override
    public NodeIterable getSuccessors(Node node) {
        return new NodeIterableWrapper(edgeStore.successors((BasicNode) node));
    }

    @Override
    public NodeIterable getSuccessors(Node node, int type) {
        return new NodeIterableWrapper(edgeStore.successors((BasicNode) node, type));
    }

    @Override
    public EdgeIterable getInEdges(Node node) {
        return new EdgeIterableWrapper(edgeStore.inIterator((BasicNode) node));
    }

    @Override
    public EdgeIterable getInEdges(Node node, int type) {
        return new EdgeIterableWrapper(edgeStore.inIterator((BasicNode) node, type));
    }

    @Override
    public EdgeIterable getOutEdges(Node node) {
        return new EdgeIterableWrapper(edgeStore.outIterator((BasicNode) node));
    }

    @Override
    public EdgeIterable getOutEdges(Node node, int type) {
        return new EdgeIterableWrapper(edgeStore.outIterator((BasicNode) node, type));
    }

    @Override
    public boolean isAdjacent(Node source, Node target) {
        return edgeStore.getEdge(source, target) != null;
    }

    @Override
    public boolean isAdjacent(Node source, Node target, int type) {
        return edgeStore.getEdge(source, target, type) != null;
    }

    @Override
    public boolean addEdge(Edge edge) {
        return edgeStore.add(edge);
    }

    @Override
    public boolean addNode(Node node) {
        return nodeStore.add(node);
    }

    @Override
    public boolean addAllEdges(Collection<? extends Edge> edges) {
        return edgeStore.addAll(edges);
    }

    @Override
    public boolean addAllNodes(Collection<? extends Node> nodes) {
        return nodeStore.addAll(nodes);
    }

    @Override
    public boolean removeEdge(Edge edge) {
        return edgeStore.remove(edge);
    }

    @Override
    public boolean removeNode(Node node) {
        BasicNode basicNode = (BasicNode) node;
        Iterator<Edge> itr = edgeStore.inOutIterator(basicNode);
        for (; itr.hasNext();) {
            Edge edge = itr.next();
            edgeStore.remove(edge);
        }
        nodeStore.remove(node);
        return true;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Edge> edges) {
        return edgeStore.removeAll(edges);
    }

    @Override
    public boolean removeAllNodes(Collection<? extends Node> nodes) {
        for (Node n : nodes) {
            removeNode(n);
        }
        return true;
    }

    @Override
    public boolean contains(Node node) {
        return nodeStore.contains(node);
    }

    @Override
    public boolean contains(Edge edge) {
        return edgeStore.contains(edge);
    }

    @Override
    public Node getNode(Object id) {
        return nodeStore.get(id);
    }

    @Override
    public Edge getEdge(Object id) {
        return edgeStore.get(id);
    }

    @Override
    public NodeIterable getNodes() {
        return new NodeIterableWrapper(nodeStore.iterator());
    }

    @Override
    public EdgeIterable getEdges() {
        return new EdgeIterableWrapper(edgeStore.iterator());
    }

    @Override
    public NodeIterable getNeighbors(Node node) {
        return new NodeIterableWrapper(new NeighborsUndirectedIterator((BasicNode) node, edgeStore.inOutIterator((BasicNode) node)));
    }

    @Override
    public NodeIterable getNeighbors(Node node, int type) {
        return new NodeIterableWrapper(new NeighborsUndirectedIterator((BasicNode) node, edgeStore.inOutIterator((BasicNode) node, type)));
    }

    @Override
    public EdgeIterable getEdges(Node node) {
        return new EdgeIterableWrapper(edgeStore.inOutIterator((BasicNode) node));
    }

    @Override
    public EdgeIterable getEdges(Node node, int type) {
        return new EdgeIterableWrapper(edgeStore.inOutIterator((BasicNode) node, type));
    }

    @Override
    public int getNodeCount() {
        return nodeStore.size();
    }

    @Override
    public int getEdgeCount() {
        return edgeStore.size();
    }

    @Override
    public int getEdgeCount(int type) {
        return edgeStore.size(type);
    }

    @Override
    public Node getOpposite(Node node, Edge edge) {
        if (edge.getSource() == node) {
            return edge.getTarget();
        } else if (edge.getTarget() == node) {
            return edge.getSource();
        }
        return null;
    }

    @Override
    public int getDegree(Node node) {
        Iterator<Edge> itr = edgeStore.inOutIterator((BasicNode) node);
        int i = 0;
        for (; itr.hasNext();) {
            i++;
        }
        return i;
    }

    @Override
    public int getInDegree(Node node) {
        Iterator<Edge> itr = edgeStore.inIterator((BasicNode) node);
        int i = 0;
        for (; itr.hasNext();) {
            i++;
        }
        return i;
    }

    @Override
    public int getOutDegree(Node node) {
        Iterator<Edge> itr = edgeStore.outIterator((BasicNode) node);
        int i = 0;
        for (; itr.hasNext();) {
            i++;
        }
        return i;
    }

    @Override
    public boolean isSelfLoop(Edge edge) {
        return edge.isSelfLoop();
    }

    @Override
    public boolean isDirected(Edge edge) {
        return edge.isDirected();
    }

    @Override
    public boolean isIncident(Edge edge1, Edge edge2) {
        return edge1.getSource() == edge2.getSource() || edge1.getTarget() == edge2.getTarget() || edge1.getSource() == edge2.getTarget() || edge1.getTarget() == edge2.getSource();
    }

    @Override
    public boolean isIncident(Node node, Edge edge) {
        return edge.getSource() == node || edge.getTarget() == node;
    }

    @Override
    public void clearEdges(Node node) {
        BasicNode basicNode = (BasicNode) node;
        Iterator<Edge> itr = edgeStore.inOutIterator(basicNode);
        for (; itr.hasNext();) {
            Edge edge = itr.next();
            edgeStore.remove(edge);
        }
    }

    @Override
    public void clearEdges(Node node, int type) {
        BasicNode basicNode = (BasicNode) node;
        Iterator<Edge> itr = edgeStore.inOutIterator(basicNode, type);
        for (; itr.hasNext();) {
            Edge edge = itr.next();
            edgeStore.remove(edge);
        }
    }

    @Override
    public void clear() {
        edgeStore.clear();
        nodeStore.clear();
    }

    @Override
    public void clearEdges() {
        edgeStore.clear();
    }

    @Override
    public GraphView getView() {
        return null;
    }

    @Override
    public void readLock() {
    }

    @Override
    public void readUnlock() {
    }

    @Override
    public void readUnlockAll() {
    }

    @Override
    public void writeLock() {
    }

    @Override
    public void writeUnlock() {
    }

    @Override
    public EdgeIterable getSelfLoops() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Edge getMutualEdge(Edge edge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDirected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isUndirected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMixed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Edge getEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getAttribute(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getAttribute(String key, double timestamp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttribute(String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getAttributeKeys() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class BasicElement implements Element {

        protected final Map<String, Object> properties = new HashMap<String, Object>();
        protected final Object id;

        public BasicElement(Object id) {
            this.id = id;
        }

        @Override
        public Object getId() {
            return id;
        }

        @Override
        public Object getAttribute(String key) {
            return properties.get(key);
        }

        @Override
        public Object getAttribute(Column column) {
            return properties.get(column.getId());
        }

        @Override
        public Object[] getAttributes() {
            return properties.values().toArray();
        }

        @Override
        public Set<String> getAttributeKeys() {
            return properties.keySet();
        }

        @Override
        public Object removeAttribute(String key) {
            return properties.remove(key);
        }

        @Override
        public Object removeAttribute(Column column) {
            return properties.remove(column.getId());
        }

        @Override
        public void setAttribute(String key, Object value) {
            properties.put(key, value);
        }

        @Override
        public void setAttribute(Column column, Object value) {
            properties.put(column.getId(), value);
        }

        @Override
        public void clearAttributes() {
            properties.clear();
        }

        @Override
        public void setAttribute(String key, Object value, double timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAttribute(Column column, Object value, double timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addTimestamp(double timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeTimestamp(double timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double[] getTimestamps() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLabel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLabel(String label) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getStoreId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getAttribute(String key, double timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getAttribute(Column column, double timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getAttribute(String key, GraphView view) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getAttribute(Column column, GraphView view) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float r() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float g() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float b() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getRGBA() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Color getColor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float alpha() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TextProperties getTextProperties() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setR(float r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setG(float g) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setB(float b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAlpha(float a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setColor(Color color) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasTimestamp(double timestamp) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public static class BasicNode extends BasicElement implements Node {

        protected final Int2ObjectMap<Object2ObjectMap<Object, BasicEdge>> outEdges;
        protected final Int2ObjectMap<Object2ObjectMap<Object, BasicEdge>> inEdges;

        public BasicNode(Object id) {
            super(id);
            this.outEdges = new Int2ObjectOpenHashMap<Object2ObjectMap<Object, BasicEdge>>(1);
            this.inEdges = new Int2ObjectOpenHashMap<Object2ObjectMap<Object, BasicEdge>>(1);
            this.outEdges.put(0, new Object2ObjectOpenHashMap<Object, BasicEdge>());
            this.inEdges.put(0, new Object2ObjectOpenHashMap<Object, BasicEdge>());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }

        @Override
        public Object getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BasicNode other = (BasicNode) obj;
            if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId())) {
                return false;
            }
            return true;
        }

        @Override
        public float x() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float y() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float z() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float r() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float g() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float b() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getRGBA() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Color getColor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float alpha() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float size() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setX(float x) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setY(float y) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setZ(float z) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setPosition(float x, float y) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setPosition(float x, float y, float z) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setR(float r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setG(float g) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setB(float b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAlpha(float a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setColor(Color color) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSize(float size) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isFixed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T extends LayoutData> T getLayoutData() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFixed(boolean fixed) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLayoutData(LayoutData layoutData) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TextProperties getTextProperties() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class BasicEdge extends BasicElement implements Edge {

        protected final BasicNode source;
        protected final BasicNode target;
        protected final int type;
        protected final boolean directed;
        protected double weight;

        public BasicEdge(Object id, BasicNode source, BasicNode target, int type, double weight, boolean directed) {
            super(id);
            this.source = source;
            this.target = target;
            this.type = type;
            this.directed = directed;
            this.weight = weight;
        }

        @Override
        public BasicNode getSource() {
            return source;
        }

        @Override
        public BasicNode getTarget() {
            return target;
        }

        @Override
        public Object getId() {
            return id;
        }

        @Override
        public double getWeight() {
            return weight;
        }

        @Override
        public void setWeight(double weight) {
            this.weight = weight;
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public Object getTypeLabel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSelfLoop() {
            return source == target;
        }

        @Override
        public boolean isDirected() {
            return directed;
        }

        public String getStringId() {
            return BasicEdgeStore.getStringId(source, target, directed);
        }

        @Override
        public float r() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float g() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float b() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getRGBA() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Color getColor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float alpha() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setR(float r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setG(float g) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setB(float b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAlpha(float a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setColor(Color color) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TextProperties getTextProperties() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getWeight(double timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getWeight(GraphView view) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setWeight(double weight, double timestamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasDynamicWeight() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class BasicNodeStore implements Collection<Node>, NodeIterable {

        protected final Object2ObjectMap<Object, BasicNode> idToNodeMap;

        public BasicNodeStore() {
            idToNodeMap = new Object2ObjectOpenHashMap<Object, BasicNode>();
        }

        @Override
        public int size() {
            return idToNodeMap.size();
        }

        @Override
        public boolean isEmpty() {
            return idToNodeMap.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof BasicNode)) {
                throw new RuntimeException("Wrong type " + o.getClass().getName());
            }
            BasicNode node = (BasicNode) o;
            if (node.getId() == null) {
                throw new NullPointerException("Node id is null");
            }
            return idToNodeMap.containsKey(node.getId());
        }

        public BasicNode get(Object id) {
            return idToNodeMap.get(id);
        }

        @Override
        public Iterator<Node> iterator() {
            return new BasicNodeIterator(idToNodeMap.values().iterator());
        }

        @Override
        public Node[] toArray() {
            return idToNodeMap.values().toArray(new Node[0]);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return idToNodeMap.values().toArray(a);
        }

        @Override
        public Collection<Node> toCollection() {
            return Arrays.asList(idToNodeMap.values().toArray(new Node[0]));
        }

        @Override
        public boolean add(Node node) {
            if (((BasicNode) node).getId() == null) {
                throw new NullPointerException("Node id is null");
            }
            if (!idToNodeMap.containsKey(((BasicNode) node).getId())) {
                idToNodeMap.put(((BasicNode) node).getId(), (BasicNode) node);
                return true;
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof BasicNode)) {
                throw new RuntimeException("Wrong type " + o.getClass().getName());
            }
            BasicNode node = (BasicNode) o;
            if (node.getId() == null) {
                throw new NullPointerException("Node id is null");
            }
            idToNodeMap.remove(node.getId());
            return true;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return idToNodeMap.values().containsAll(c);
        }

        public boolean containsId(Object id) {
            return idToNodeMap.containsKey(id);
        }

        @Override
        public boolean addAll(Collection<? extends Node> c) {
            for (Node b : c) {
                add(b);
            }
            return true;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            for (Object b : c) {
                if (!(b instanceof BasicNode)) {
                    throw new RuntimeException("Wrong type " + b.getClass().getName());
                }
                remove((BasicNode) b);
            }
            return true;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            idToNodeMap.values().retainAll(c);
            return true;
        }

        @Override
        public void clear() {
            idToNodeMap.clear();
        }

        @Override
        public boolean equals(Object obj) {
            return idToNodeMap.equals(obj);
        }

        @Override
        public void doBreak() {
        }

        private static class BasicNodeIterator implements Iterator<Node> {

            private final Iterator<BasicNode> itr;

            public BasicNodeIterator(Iterator<BasicNode> itr) {
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
                itr.remove();
            }
        }
    }

    public static class BasicEdgeStore implements Collection<Edge> {

        protected final Object2ObjectMap<Object, BasicEdge> idToEdgeMap;
        protected final Object2ObjectMap<String, BasicEdge> sourceTargetIdEdgeMap;
        protected final Int2IntMap typeCountMap;

        public BasicEdgeStore() {
            idToEdgeMap = new Object2ObjectLinkedOpenHashMap<Object, BasicGraphStore.BasicEdge>();
            sourceTargetIdEdgeMap = new Object2ObjectLinkedOpenHashMap<String, BasicEdge>();
            typeCountMap = new Int2IntOpenHashMap();
        }

        @Override
        public int size() {
            return idToEdgeMap.size();
        }

        public int size(int type) {
            return typeCountMap.get(type);
        }

        @Override
        public boolean isEmpty() {
            return idToEdgeMap.isEmpty();
        }

        public BasicEdge get(Object id) {
            return idToEdgeMap.get(id);
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof BasicEdge)) {
                throw new RuntimeException("Wrong type " + o.getClass().getName());
            }
            BasicEdge edge = (BasicEdge) o;
            return idToEdgeMap.containsKey(edge.getId());
        }

        public boolean containsId(Object id) {
            return idToEdgeMap.containsKey(id);
        }

        public BasicEdge getEdge(Node source, Node target, int type) {
            return sourceTargetIdEdgeMap.get(getStringId(source, target, type));
        }

        public BasicEdge getEdge(Node source, Node target) {
            return sourceTargetIdEdgeMap.get(getStringId(source, target));
        }

        @Override
        public Iterator<Edge> iterator() {
            return new BasicEdgeIterator(idToEdgeMap.values().iterator());
        }

        public Iterator<Edge> outIterator(BasicNode node) {
            ObjectSet<BasicEdge> set = new ObjectLinkedOpenHashSet<BasicEdge>();
            for (Object2ObjectMap<Object, BasicEdge> col : node.outEdges.values()) {
                set.addAll(col.values());
            }
            return new BasicEdgeIterator(set);
        }

        public Iterator<Edge> outIterator(BasicNode node, int type) {
            ObjectSet<BasicEdge> set = new ObjectLinkedOpenHashSet<BasicEdge>();
            if (node.outEdges.containsKey(type)) {
                set.addAll(node.outEdges.get(type).values());
            }
            return new BasicEdgeIterator(set);
        }

        public Iterator<Edge> inIterator(BasicNode node) {
            ObjectSet<BasicEdge> set = new ObjectLinkedOpenHashSet<BasicEdge>();
            for (Object2ObjectMap<Object, BasicEdge> col : node.inEdges.values()) {
                set.addAll(col.values());
            }
            return new BasicEdgeIterator(set);
        }

        public Iterator<Edge> inIterator(BasicNode node, int type) {
            ObjectSet<BasicEdge> set = new ObjectLinkedOpenHashSet<BasicEdge>();
            if (node.inEdges.containsKey(type)) {
                set.addAll(node.inEdges.get(type).values());
            }
            return new BasicEdgeIterator(set);
        }

        public Iterator<Edge> inOutIterator(BasicNode node) {
            ObjectSet<BasicEdge> set = new ObjectLinkedOpenHashSet<BasicEdge>();
            for (Object2ObjectMap<Object, BasicEdge> col : node.outEdges.values()) {
                set.addAll(col.values());
            }
            for (Object2ObjectMap<Object, BasicEdge> col : node.inEdges.values()) {
                set.addAll(col.values());
            }
            return new BasicEdgeIterator(set);
        }

        public Iterator<Edge> inOutIterator(BasicNode node, int type) {
            ObjectSet<BasicEdge> set = new ObjectLinkedOpenHashSet<BasicEdge>();
            if (node.outEdges.containsKey(type)) {
                set.addAll(node.outEdges.get(type).values());
            }
            if (node.inEdges.containsKey(type)) {
                set.addAll(node.inEdges.get(type).values());
            }
            return new BasicEdgeIterator(set);
        }

        public Iterator<Node> predecessors(BasicNode node) {
            Iterator<Edge> itr = inIterator(node);
            return new NeighborsIterator(node, itr);
        }

        public Iterator<Node> predecessors(BasicNode node, int type) {
            Iterator<Edge> itr = inIterator(node, type);
            return new NeighborsIterator(node, itr);
        }

        public Iterator<Node> successors(BasicNode node) {
            Iterator<Edge> itr = outIterator(node);
            return new NeighborsIterator(node, itr);
        }

        public Iterator<Node> successors(BasicNode node, int type) {
            Iterator<Edge> itr = outIterator(node, type);
            return new NeighborsIterator(node, itr);
        }

        @Override
        public Object[] toArray() {
            return idToEdgeMap.values().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return idToEdgeMap.values().toArray(a);
        }

        @Override
        public boolean add(Edge edge) {
            if (idToEdgeMap.containsKey(edge.getId())) {
                throw new RuntimeException("Edge already exists");
            }
            idToEdgeMap.put(edge.getId(), (BasicEdge) edge);

            BasicEdge basicEdge = (BasicEdge) edge;
            BasicNode source = basicEdge.getSource();
            BasicNode target = basicEdge.getTarget();
            int type = edge.getType();

            sourceTargetIdEdgeMap.put(getStringId(source, target, type), basicEdge);
            sourceTargetIdEdgeMap.put(getStringId(source, target), basicEdge);

            int typeCount = typeCountMap.get(type);
            typeCountMap.put(type, typeCount + 1);

            Object2ObjectMap<Object, BasicEdge> outMap = source.outEdges.get(type);
            if (outMap == null) {
                outMap = new Object2ObjectOpenHashMap<Object, BasicEdge>();
                source.outEdges.put(type, outMap);
            }
            outMap.put(target.getId(), basicEdge);

            Object2ObjectMap<Object, BasicEdge> inMap = target.inEdges.get(type);
            if (inMap == null) {
                inMap = new Object2ObjectOpenHashMap<Object, BasicEdge>();
                target.inEdges.put(type, inMap);
            }
            inMap.put(source.getId(), basicEdge);

            return true;
        }

        private String getStringId(Node source, Node target, int type) {
            return source + "-" + target + "-" + type;
        }

        private String getStringId(Node source, Node target) {
            return source + "-" + target + "-" + "X";
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof BasicEdge)) {
                throw new RuntimeException("Wrong type " + o.getClass().getName());
            }
            BasicEdge edge = (BasicEdge) o;
            BasicNode source = edge.getSource();
            BasicNode target = edge.getTarget();
            int type = edge.getType();

            idToEdgeMap.remove(edge.getId());
            sourceTargetIdEdgeMap.remove(getStringId(source, target, type));
            sourceTargetIdEdgeMap.remove(getStringId(source, target));

            int typeCount = typeCountMap.get(type);
            typeCountMap.put(type, typeCount - 1);

            Object2ObjectMap<Object, BasicEdge> outMap = source.outEdges.get(type);
            outMap.remove(target.getId());

            Object2ObjectMap<Object, BasicEdge> inMap = target.inEdges.get(type);
            inMap.remove(source.getId());

            return true;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return idToEdgeMap.values().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Edge> c) {
            for (Edge b : c) {
                add(b);
            }
            return true;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            for (Object b : c) {
                if (!(b instanceof BasicEdge)) {
                    throw new RuntimeException("Wrong type " + b.getClass().getName());
                }
                remove((BasicEdge) b);
            }
            return true;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            idToEdgeMap.values().retainAll(c);
            return true;
        }

        @Override
        public void clear() {
            idToEdgeMap.clear();
        }

        protected static String getStringId(BasicNode source, BasicNode target, boolean directed) {
            if (directed) {
                return source.getId() + "->" + target.getId();
            } else {
                if (source.getId().hashCode() > target.getId().hashCode()) {
                    return source.getId() + "->" + target.getId();
                } else {
                    return target.getId() + "->" + source.getId();
                }
            }
        }

        private static class BasicEdgeIterator implements Iterator<Edge> {

            private final Iterator<BasicEdge> itr;

            public BasicEdgeIterator(Iterator<BasicEdge> itr) {
                this.itr = itr;
            }

            public BasicEdgeIterator(Collection<BasicEdge> collection) {
                this.itr = collection.iterator();
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
                itr.remove();
            }
        }
    }

    private static class NeighborsIterator implements Iterator<Node> {

        protected final BasicNode node;
        protected final Iterator<Edge> itr;

        public NeighborsIterator(BasicNode node, Iterator<Edge> itr) {
            this.node = node;
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Node next() {
            Edge e = itr.next();
            return e.getSource() == node ? e.getTarget() : e.getSource();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported for this iterator");
        }
    }

    private static class NeighborsUndirectedIterator implements Iterator<Node> {

        protected Set<BasicNode> output = new ObjectOpenHashSet<BasicNode>();
        protected final BasicNode node;
        protected final Iterator<Edge> itr;
        protected BasicNode pointer;

        public NeighborsUndirectedIterator(BasicNode node, Iterator<Edge> itr) {
            this.node = node;
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                if (!itr.hasNext()) {
                    return false;
                }
                Edge e = itr.next();
                pointer = (BasicNode) (e.getSource() == node ? e.getTarget() : e.getSource());
                if (!output.add(pointer)) {
                    pointer = null;
                }
            }
            return true;
        }

        @Override
        public Node next() {
            return pointer;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported for this iterator");
        }
    }

    protected class NodeIterableWrapper implements NodeIterable {

        protected final Iterator<Node> iterator;

        public NodeIterableWrapper(Iterator<Node> iterator) {
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
            //Not used because no locking
        }
    }

    protected class EdgeIterableWrapper implements EdgeIterable {

        protected final Iterator<Edge> iterator;

        public EdgeIterableWrapper(Iterator<Edge> iterator) {
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
            //Not used because no locking
        }
    }
}
