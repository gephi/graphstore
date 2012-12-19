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

    private static AtomicInteger NODE_IDS = new AtomicInteger();
    private static AtomicInteger EDGE_IDS = new AtomicInteger();
    //Store
    protected final GraphStore store;

    public GraphFactoryImpl(GraphStore store) {
        this.store = store;
    }

    @Override
    public Edge newEdge(Node source, Node target, int type) {
        return new EdgeImpl(EDGE_IDS.getAndIncrement(), store, (NodeImpl) source, (NodeImpl) target, type, 1.0, true);
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
        return new NodeImpl(NODE_IDS.getAndIncrement(), store);
    }
}
