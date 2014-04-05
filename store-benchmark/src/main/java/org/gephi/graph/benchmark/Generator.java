package org.gephi.graph.benchmark;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.graph.store.GraphModelImpl;
import org.gephi.graph.store.GraphStore;

public abstract class Generator {

    protected final GraphFactory factory;
    protected final GraphStore graphStore;
    protected List<Node> nodes;
    protected List<Edge> edges;

    public Generator() {
        GraphModelImpl model = new GraphModelImpl();
        factory = model.factory();
        graphStore = model.getStore();
        nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
    }

    public GraphStore getStore() {
        return graphStore;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void clean() {
        nodes = null;
        edges = null;
    }

    public abstract Generator generate();

    public abstract Generator commit();

    protected void commitInner() {
        for (Node node : nodes) {
            graphStore.addNode(node);
        }
        for (Edge edge : edges) {
            graphStore.addEdge(edge);
        }
    }
}
