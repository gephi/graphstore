package org.gephi.graph.store;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterator;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.NodeIterator;

/**
 *
 * @author mbastian
 */
public class BasicGraphStore {

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
        public Object getProperty(String key) {
            return properties.get(key);
        }

        @Override
        public Object getProperty(Column column) {
            return properties.get(column.getId());
        }

        @Override
        public Object[] getProperties() {
            return properties.values().toArray();
        }

        @Override
        public Set<String> getPropertyKeys() {
            return properties.keySet();
        }

        @Override
        public Object removeProperty(String key) {
            return properties.remove(key);
        }

        @Override
        public Object removeProperty(Column column) {
            return properties.remove(column.getId());
        }

        @Override
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }

        @Override
        public void setProperty(Column column, Object value) {
            properties.put(column.getId(), value);
        }

        @Override
        public void clearProperties() {
            properties.clear();
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
    }

    public static class BasicEdge extends BasicElement implements Edge {

        protected final BasicNode source;
        protected final BasicNode target;
        protected final int type;

        public BasicEdge(Object id, BasicNode source, BasicNode target, int type) {
            super(id);
            this.source = source;
            this.target = target;
            this.type = type;
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
        public int getType() {
            return type;
        }

        @Override
        public boolean isSelfLoop() {
            return source == target;
        }

        @Override
        public boolean isDirected() {
            return true;
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
        public NodeIterator iterator() {
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

        private static class BasicNodeIterator implements NodeIterator {

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

        public BasicEdgeStore() {
            idToEdgeMap = new Object2ObjectOpenHashMap<Object, BasicGraphStore.BasicEdge>();
        }

        @Override
        public int size() {
            return idToEdgeMap.size();
        }

        @Override
        public boolean isEmpty() {
            return idToEdgeMap.isEmpty();
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

        @Override
        public BasicEdgeIterator iterator() {
            return new BasicEdgeIterator(idToEdgeMap.values().iterator());
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

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof BasicEdge)) {
                throw new RuntimeException("Wrong type " + o.getClass().getName());
            }
            BasicEdge edge = (BasicEdge) o;
            idToEdgeMap.remove(edge.getId());
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

        private static class BasicEdgeIterator implements EdgeIterator {

            private final Iterator<BasicEdge> itr;

            public BasicEdgeIterator(Iterator<BasicEdge> itr) {
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
                itr.remove();
            }
        }
    }
}
