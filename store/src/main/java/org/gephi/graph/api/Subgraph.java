package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface Subgraph {

    public GraphView getView();

    public void fill();

    public void union(Subgraph subGraph);

    public void intersection(Subgraph subGraph);
}
