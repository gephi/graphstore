package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface NodeIterable extends Iterable<Node> {
    
    @Override
    public NodeIterator iterator();

    public Node[] toArray();
    
    public void doBreak();
}
