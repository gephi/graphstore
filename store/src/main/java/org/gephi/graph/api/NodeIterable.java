package org.gephi.graph.api;

import java.util.Collection;

/**
 *
 * @author mbastian
 */
public interface NodeIterable extends ElementIterable<Node> {

    @Override
    public NodeIterator iterator();

    @Override
    public Node[] toArray();

    @Override
    public Collection<Node> toCollection();
}
