package org.gephi.graph.api;

import org.gephi.graph.store.GraphStore;

/**
 *
 * @author mbastian
 */
public interface GraphModel {
    
    public Graph getGraph();
    
    public DirectedGraph getDirectedGraph();
    
    public UndirectedGraph getUndirectedGraph();
    
    public boolean isDirected();
    
    public boolean isUndirected();
    
    public boolean isMixed();
}
