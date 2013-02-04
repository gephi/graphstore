package org.gephi.graph.store;

import org.gephi.attribute.api.Table;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.UndirectedSubgraph;

/**
 *
 * @author mbastian
 */
public class GraphModelImpl implements GraphModel {

    protected final GraphStore store;
    protected final TableImpl<Node> nodeTable;
    protected final TableImpl<Edge> edgeTable;

    public GraphModelImpl() {
        store = new GraphStore();
        nodeTable = new TableImpl<Node>(store.nodePropertyStore);
        edgeTable = new TableImpl<Edge>(store.edgePropertyStore);
    }

    @Override
    public GraphFactory factory() {
        return store.factory;
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
        return store.viewStore.getDirectedGraph(view);
    }

    @Override
    public UndirectedSubgraph getUndirectedGraph(GraphView view) {
        return store.viewStore.getUndirectedGraph(view);
    }

    @Override
    public int addEdgeType(Object label) {
        //TODO Locking
        return store.edgeTypeStore.addType(label);
    }

    @Override
    public int getEdgeType(Object label) {
        //TODO Locking
        return store.edgeTypeStore.getId(label);
    }

    @Override
    public Object getEdgeLabel(int id) {
        //TODO Locking
        return store.edgeTypeStore.getLabel(id);
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

    @Override
    public GraphView createView() {
        return store.viewStore.createView();
    }

    @Override
    public void destroyView(GraphView view) {
        store.viewStore.destroyView(view);
    }

    @Override
    public Table getNodeTable() {
        return nodeTable;
    }

    @Override
    public Table getEdgeTable() {
        return edgeTable;
    }
}
