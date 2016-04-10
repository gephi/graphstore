package org.gephi.graph.impl;

import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraphView;
import org.gephi.graph.api.HierarchicalNodeGroup;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

public class HierarchicalGraphTest {
    @Test
    public void testSimpleExpandCollapse() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        HierarchicalGraphView view = store.createHierarchicalView();
        DirectedSubgraph graph = store.getDirectedGraph(view);

        for (Node node : graphStore.getNodes().toArray()) {
            graph.addNode(node);
        }
        for (Edge edge : graphStore.getEdges().toArray()) {
            graph.addEdge(edge);
        }

        int originalCount = store.getGraph(view).getNodeCount();

        Node parentNode = store.getGraph(view).getNode("7");
        HierarchicalNodeGroup group = view.getRoot().addNode(parentNode);
        Assert.assertEquals(originalCount, graph.getNodeCount());

        Node childNode = store.getGraph(view).getNode("3");
        group.addNode(childNode);
        Assert.assertEquals(originalCount, graph.getNodeCount());

        Assert.assertFalse(graph.getEdges(parentNode).toCollection().isEmpty());
        Assert.assertFalse(graph.getEdges(childNode).toCollection().isEmpty());

        int edgeCountForParent = graph.getDegree(parentNode);
        int edgeCountForChild = graph.getDegree(childNode);

        group.collapse();

        Assert.assertEquals(originalCount - 1, graph.getNodeCount());
        Assert.assertFalse(graph.hasNode("3"));
        Assert.assertEquals(edgeCountForParent + edgeCountForChild, graph.getDegree(parentNode));
        Assert.assertEquals(0, graph.getDegree(childNode));

        Assert.assertFalse(graph.getEdges(parentNode).toCollection().isEmpty());
        Assert.assertTrue(graph.getEdges(childNode).toCollection().isEmpty());

        graph.clearEdges();

        Assert.assertTrue(graph.getEdges(parentNode).toCollection().isEmpty());
        Assert.assertTrue(graph.getEdges(childNode).toCollection().isEmpty());
    }

    @Test
    public void testManipulateEdges() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        HierarchicalGraphView view = store.createHierarchicalView();
        DirectedSubgraph graph = store.getDirectedGraph(view);

        for (Node node : graphStore.getNodes().toArray()) {
            graph.addNode(node);
        }
        for (Edge edge : graphStore.getEdges().toArray()) {
            graph.addEdge(edge);
        }

        Node parentNode = store.getGraph(view).getNode("7");
        HierarchicalNodeGroup group = view.getRoot().addNode(parentNode);
        Node childNode = store.getGraph(view).getNode("3");
        group.addNode(childNode);

        Set<Node> others = new HashSet<Node>();
        for (Edge edge : graph.getEdges(childNode)) {
            Node other = graph.getOpposite(childNode, edge);
            Assert.assertNotNull(other);
            Assert.assertFalse(other.equals(childNode));
            others.add(other);
        }
        Assert.assertFalse(others.isEmpty());

        group.collapse();

        Set<Edge> mapped = new HashSet<Edge>();
        for (Node other : others) {
            for (Edge edge : graph.getEdges(other)) {
                if (edge instanceof HierarchicalGraphDecorator.MappedEdgeDecorator) {
                    if (edge.getSource() != edge.getTarget()) {
                        Assert.assertFalse(edge.getSource() == childNode || edge.getTarget() == childNode);
                        Assert.assertTrue(edge.getSource() == parentNode || edge.getTarget() == parentNode);
                        Assert.assertTrue(edge.getSource() == other || edge.getTarget() == other);
                        mapped.add(edge);
                    }
                }
            }
        }
        Assert.assertFalse(mapped.isEmpty());

        for (Edge edge : mapped) {
            Node other = graph.getOpposite(childNode, edge);
            Assert.assertNull(other);
            other = graph.getOpposite(parentNode, edge);
            Assert.assertNotNull(other);
            Assert.assertTrue(others.contains(other));
            graph.removeEdge(edge);
        }
    }
}
