package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface NodeIterable {
    
    public NodeIterator iterator();

    public Node[] toArray();
    
    public void doBreak();
}
