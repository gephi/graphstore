package org.gephi.graph.store;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.UndirectedSubgraph;

/**
 *
 * @author mbastian
 */
public class GraphModelImpl implements GraphModel {
    
    protected final GraphStore store;
    protected final GraphViewStore viewStore;
    
    public GraphModelImpl() {
        store = new GraphStore();
        viewStore = new GraphViewStore(store);
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
    public DirectedSubgraph getDirectedGraph(GraphView view) {
        return viewStore.getDirectedGraph(view);
    }
    
    @Override
    public UndirectedSubgraph getUndirectedGraph(GraphView view) {
        return viewStore.getUndirectedGraph(view);
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
