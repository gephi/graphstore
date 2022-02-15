package org.gephi.graph.impl;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphLock;
import org.gephi.graph.api.Node;

public class DegreeNoIndexImpl implements ColumnIndexImpl<Integer, Node> {

    // Type
    public enum DegreeType {
        DEGREE, IN_DEGREE, OUT_DEGREE
    }

    // Type
    protected final DegreeType degreeType;
    // Graph
    protected final Graph graph;
    protected final GraphLock graphLock;

    protected DegreeNoIndexImpl(Graph graph, DegreeType degreeType) {
        this.graph = graph;
        this.graphLock = graph.getLock();
        this.degreeType = degreeType;
    }

    @Override
    public int count(Integer value) {
        checkNull(value);

        Iterator<Node> nodeIterator = graph.getNodes().iterator();
        int count = 0;
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            int degree = getDegree(node);
            if (value == degree) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Iterable<Node> get(Integer degree) {
        checkNull(degree);
        return new NodeWithDegreeIterable(degree);
    }

    @Override
    public Collection<Integer> values() {
        Iterator<Node> nodeIterator = graph.getNodes().iterator();
        Set<Integer> set = new ObjectOpenHashSet<>();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            int degree = getDegree(node);
            set.add(degree);
        }
        return set;
    }

    @Override
    public int countValues() {
        return values().size();
    }

    @Override
    public int countElements() {
        return graph.getNodeCount();
    }

    @Override
    public boolean isSortable() {
        return true;
    }

    @Override
    public Integer getMinValue() {
        Integer min = null;
        Iterator<Node> nodeIterator = graph.getNodes().iterator();
        int minN = Integer.MAX_VALUE;
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            int degree = getDegree(node);
            if (min == null || (degree < minN)) {
                minN = degree;
                min = degree;
            }
        }
        return min;
    }

    @Override
    public Integer getMaxValue() {
        Integer max = null;
        Iterator<Node> nodeIterator = graph.getNodes().iterator();
        int maxN = Integer.MIN_VALUE;
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            int degree = getDegree(node);
            if (max == null || (degree > maxN)) {
                maxN = degree;
                max = degree;
            }
        }
        return max;
    }

    @Override
    public Column getColumn() {
        return null;
    }

    @Override
    public int getVersion() {
        return ((GraphModelImpl) graph.getModel()).store.version.nodeVersion;
    }

    @Override
    public Iterator<Map.Entry<Integer, ? extends Set<Node>>> iterator() {
        // TODO
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void clear() {
        // Nothing to clear
    }

    @Override
    public void destroy() {
        // Nothing to destroy
    }

    @Override
    public Integer putValue(Node element, Integer value) {
        return value;
    }

    @Override
    public Integer replaceValue(Node element, Integer oldValue, Integer newValue) {
        return newValue;
    }

    @Override
    public void removeValue(Node element, Integer value) {
        // Nothing to remove
    }

    private int getDegree(Node node) {
        switch (degreeType) {
            case DEGREE:
                return graph.getDegree(node);
            case IN_DEGREE:
                return ((DirectedGraph) graph).getInDegree(node);
            case OUT_DEGREE:
                return ((DirectedGraph) graph).getOutDegree(node);
        }
        throw new RuntimeException();
    }

    private void checkNull(Integer value) {
        if (value == null) {
            throw new NullPointerException();
        }
    }

    private class NodeWithDegreeIterable implements Iterable<Node> {

        private final Integer value;

        public NodeWithDegreeIterable(Integer degree) {
            this.value = degree;
        }

        @Override
        public Iterator<Node> iterator() {
            return new NodeWithDegreeIterator(value);
        }
    }

    private class NodeWithDegreeIterator implements Iterator<Node> {

        private final Iterator<Node> itr;
        private final Integer value;
        private Node pointer;

        public NodeWithDegreeIterator(Integer value) {
            this.itr = graph.getNodes().iterator();
            this.value = value;
        }

        @Override
        public boolean hasNext() {
            while (pointer == null && itr.hasNext()) {
                Node node = itr.next();
                int degree = getDegree(node);
                if (value == degree) {
                    pointer = node;
                }
            }
            return pointer != null;
        }

        @Override
        public Node next() {
            Node res = pointer;
            pointer = null;
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
