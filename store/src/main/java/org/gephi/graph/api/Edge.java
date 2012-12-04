package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface Edge extends Element {
    
    public Node getSource();
    
    public Node getTarget();
    
    public int getType();
    
    public boolean isSelfLoop();
    
    public boolean isDirected();
}
