package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface EdgeIterable {

    public EdgeIterator iterator();

    public Edge[] toArray();
    
    public void doBreak();
}
