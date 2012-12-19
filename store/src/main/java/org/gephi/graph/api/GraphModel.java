package org.gephi.graph.api;

import org.gephi.graph.store.GraphStore;

/**
 *
 * @author mbastian
 */
public interface GraphModel {
    
    public GraphFactory factory();
    
    public Graph getGraph();
    
    public DirectedGraph getDirectedGraph();
    
    public UndirectedGraph getUndirectedGraph();
    
    public DirectedSubgraph getDirectedGraph(GraphView view);
    
    public UndirectedSubgraph getUndirectedGraph(GraphView view);
    
    public int addEdgeType(Object label);

    public int getEdgeType(Object label);

    public Object getEdgeLabel(int id);
    
    public boolean isDirected();
    
    public boolean isUndirected();
    
    public boolean isMixed();
    
    public GraphView createView();
    
    public void destroyView(GraphView view);
    
    
}
