package org.gephi.graph.api;

import org.gephi.attribute.api.Index;
import org.gephi.attribute.api.Table;
import org.gephi.attribute.api.TimestampIndex;

/**
 *
 * @author mbastian
 */
public interface GraphModel {

    public GraphFactory factory();

    public Graph getGraph();

    public Subgraph getGraph(GraphView view);

    public DirectedGraph getDirectedGraph();

    public UndirectedGraph getUndirectedGraph();

    public DirectedSubgraph getDirectedGraph(GraphView view);

    public UndirectedSubgraph getUndirectedGraph(GraphView view);

    public int addEdgeType(Object label);

    public int getEdgeType(Object label);

    public Object getEdgeLabel(int id);

    public boolean isDirected();

    public boolean isUndirected();

    public boolean isMixed();

    public GraphView createView();

    public GraphView createNodeView();

    public void destroyView(GraphView view);

    public Table getNodeTable();

    public Table getEdgeTable();

    public Index getNodeIndex();

    public Index getNodeIndex(GraphView view);

    public Index getEdgeIndex();

    public Index getEdgeIndex(GraphView view);

    public TimestampIndex getTimestampIndex();

    public TimestampIndex getTimestampIndex(GraphView view);
}
