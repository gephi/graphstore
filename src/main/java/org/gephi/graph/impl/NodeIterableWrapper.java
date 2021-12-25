package org.gephi.graph.impl;

import java.util.Iterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;

public class NodeIterableWrapper extends ElementIterableWrapper<Node> implements NodeIterable {

    public NodeIterableWrapper(Iterator<Node> iterator) {
        super(iterator);
    }

    public NodeIterableWrapper(Iterator<Node> iterator, GraphLockImpl lock) {
        super(iterator, lock);
    }

    @Override
    public Node[] toArray() {
        return toArray(new Node[0]);
    }
}
