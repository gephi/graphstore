package org.gephi.graph.store;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.UndirectedGraph;

/**
 *
 * @author mbastian
 */
public class GraphModelImpl implements GraphModel {
    
    protected final GraphStore store;
    
    public GraphModelImpl() {
        store = new GraphStore();
    }

    @Override
    public Graph getGraph() {
        return store;
    }

    @Override
    public DirectedGraph getDirectedGraph() {
        return store;
    }

    @Override
    public UndirectedGraph getUndirectedGraph() {
        return store.undirectedDecorator;
    }

    @Override
    public boolean isDirected() {
        return store.isDirected();
    }

    @Override
    public boolean isUndirected() {
        return store.isUndirected();
    }

    @Override
    public boolean isMixed() {
        return store.isMixed();
    }
}
