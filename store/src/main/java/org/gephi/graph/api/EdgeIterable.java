package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface EdgeIterable extends Iterable<Edge> {

    @Override
    public EdgeIterator iterator();

    public Edge[] toArray();

    public void doBreak();
}
