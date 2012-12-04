package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface DirectedGraph extends Graph {

    @Override
    public Edge getEdge(Node source, Node target, int type);

    public NodeIterable getPredecessors(Node node);

    public NodeIterable getPredecessors(Node node, int type);

    public NodeIterable getSuccessors(Node node);

    public NodeIterable getSuccessors(Node node, int type);

    public EdgeIterable getInEdges(Node node);

    public EdgeIterable getInEdges(Node node, int type);

    public EdgeIterable getOutEdges(Node node);

    public EdgeIterable getOutEdges(Node node, int type);

    @Override
    public boolean isAdjacent(Node source, Node target);

    @Override
    public boolean isAdjacent(Node source, Node target, int type);
}
