package org.gephi.graph.api;

import java.util.Collection;

/**
 *
 * @author mbastian
 */
public interface EdgeIterable extends ElementIterable<Edge> {

    @Override
    public EdgeIterator iterator();

    @Override
    public Edge[] toArray();
    
    @Override
    public Collection<Edge> toCollection();
}
