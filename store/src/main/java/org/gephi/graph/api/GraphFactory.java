package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface GraphFactory {

    public Edge newEdge(Node source, Node target, int type);

    public Edge newEdge(Node source, Node target, int type, double weight, boolean directed);

    public Edge newEdge(Object id, Node source, Node target, int type, double weight, boolean directed);

    public Node newNode();

    public Node newNode(Object id);
}
