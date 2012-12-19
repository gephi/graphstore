package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface Edge extends Element {
    
    public static int DEFAULT_TYPE = 0;
    
    public Node getSource();
    
    public Node getTarget();
    
    public double getWeight();
    
    public void setWeight(double weight);
    
    public int getType();
    
    public boolean isSelfLoop();
    
    public boolean isDirected();
}
