package org.gephi.graph.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;

public class EdgeTypeNoIndexImpl implements ColumnIndexImpl<Object, Edge> {

    // Graph
    protected final Graph graph;

    protected EdgeTypeNoIndexImpl(Graph graph) {
        this.graph = graph;
    }

    @Override
    public int count(Object label) {
        return graph.getEdgeCount(labelToType(label));
    }

    @Override
    public Iterable<Edge> get(Object label) {
        return graph.getEdges(labelToType(label));
    }

    @Override
    public Collection<Object> values() {
        return Arrays.asList(graph.getModel().getEdgeTypeLabels(false));
    }

    @Override
    public int countValues() {
        return values().size();
    }

    @Override
    public int countElements() {
        return graph.getEdgeCount();
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    public Number getMinValue() {
        throw new UnsupportedOperationException("Edge type index is not sortable");
    }

    @Override
    public Number getMaxValue() {
        throw new UnsupportedOperationException("Edge type index is not sortable");
    }

    @Override
    public Column getColumn() {
        return graph.getModel().defaultColumns().edgeType();
    }

    @Override
    public int getVersion() {
        return ((GraphModelImpl) graph.getModel()).store.version.edgeVersion;
    }

    @Override
    public Iterator<Map.Entry<Object, ? extends Set<Edge>>> iterator() {
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
    public Object putValue(Edge element, Object value) {
        return value;
    }

    @Override
    public Object replaceValue(Edge element, Object oldValue, Object newValue) {
        return newValue;
    }

    @Override
    public void removeValue(Edge element, Object value) {
        // Nothing to remove
    }

    private int labelToType(Object label) {
        int type = graph.getModel().getEdgeType(label);
        if (type == -1) {
            throw new IllegalArgumentException("Edge label " + label + " doesn't exist");
        }
        return type;
    }
}
