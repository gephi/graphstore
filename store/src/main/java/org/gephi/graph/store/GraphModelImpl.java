package org.gephi.graph.store;

import org.gephi.attribute.api.Index;
import org.gephi.attribute.api.Table;
import org.gephi.attribute.api.TimestampIndex;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
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
    public Subgraph getGraph(GraphView view) {
        return store.viewStore.getDirectedGraph(view);
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
        //TODO Locking
        return store.viewStore.createView();
    }
    
    @Override
    public void destroyView(GraphView view) {
        //TODO Locking
        store.timestampStore.deleteViewIndex(store);
        store.edgePropertyStore.indexStore.deleteViewIndex(((GraphViewImpl) view).graphStore);
        store.nodePropertyStore.indexStore.deleteViewIndex(((GraphViewImpl) view).graphStore);
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
    
    @Override
    public Index getNodeIndex() {
        return store.nodePropertyStore.indexStore.getIndex(store);
    }
    
    @Override
    public Index getNodeIndex(GraphView view) {
        //TODO Locking
        return store.nodePropertyStore.indexStore.getIndex(((GraphViewImpl) view).graphStore);
    }
    
    @Override
    public Index getEdgeIndex() {
        return store.edgePropertyStore.indexStore.getIndex(store);
    }
    
    @Override
    public Index getEdgeIndex(GraphView view) {
        //TODO Locking
        return store.edgePropertyStore.indexStore.getIndex(((GraphViewImpl) view).graphStore);
    }
    
    @Override
    public TimestampIndex getTimestampIndex() {
        return store.timestampStore.getIndex(store);
    }
    
    @Override
    public TimestampIndex getTimestampIndex(GraphView view) {
        //TODO Locking
        return store.timestampStore.getIndex(((GraphViewImpl) view).graphStore);
    }
}
