package org.gephi.graph.impl;

import java.util.Iterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;

public class EdgeIterableWrapper extends ElementIterableWrapper<Edge> implements EdgeIterable {

    public EdgeIterableWrapper(Iterator<Edge> iterator) {
        super(iterator);
    }

    public EdgeIterableWrapper(Iterator<Edge> iterator, GraphLockImpl lock) {
        super(iterator, lock);
    }

    @Override
    public Edge[] toArray() {
        return toArray(new Edge[0]);
    }
}
