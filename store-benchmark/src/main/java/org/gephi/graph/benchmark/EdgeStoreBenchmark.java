package org.gephi.graph.benchmark;

import java.util.Iterator;
import java.util.List;

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.EdgeStore;

/**
 *
 * @author mbastian, niteshbhargv
 */
public class EdgeStoreBenchmark {

    private Object object;
    
    public Runnable pushEdgeStore(int nodes, double prob) {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, prob, config).generate();
        final EdgeStore edgeStore = graph.getStore().getEdgeStore();
        final List<Node> nodeList = graph.getNodes();
        final List<Edge> edgeList = graph.getEdges();
        graph.getStore().addAllNodes(nodeList);
        
        Runnable runnable = () -> {
            edgeStore.clear();
            for(Edge edge : edgeList) {
                edgeStore.add(edge);
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStore(int nodes, double prob) {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, prob, config).generate().commit();
        final EdgeStore edgeStore = graph.getStore().getEdgeStore();
        
        Runnable runnable = () -> {
            Iterator<Edge> itr = edgeStore.iterator();
            for (; itr.hasNext();) {
                object = itr.next();
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStoreNeighborsOut(int nodes, double prob) {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, prob, config).generate().commit();
        final EdgeStore edgeStore = graph.getStore().getEdgeStore();
        final List<Node> nodeList = graph.getNodes();
        
        Runnable runnable = () -> {
            for (Node node : nodeList) {
                Iterator<Edge> itr = edgeStore.edgeOutIterator(node);
                for (; itr.hasNext();) {
                    object = itr.next();
                }
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStoreNeighborsInOut(int nodes, double prob) {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, prob, config).generate().commit();
        final EdgeStore edgeStore = graph.getStore().getEdgeStore();
        final List<Node> nodeList = graph.getNodes();
        
        Runnable runnable = () -> {
            for (Node node : nodeList) {
                Iterator<Edge> itr = edgeStore.edgeIterator(node);
                for (; itr.hasNext();) {
                    object = itr.next();
                }
            }
        };
        return runnable;
    }

    public Runnable resetEdgeStore(int nodes, double prob) {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, prob, config).generate().commit();
        final EdgeStore edgeStore = graph.getStore().getEdgeStore();
        final List<Edge> edgeList = graph.getEdges();
        
        Runnable runnable = () -> {
            for (Edge e : edgeList) {
                edgeStore.remove(e);
            }
            for (Edge e : edgeList) {
                edgeStore.add(e);
            }
        };
        return runnable;
    }
}
