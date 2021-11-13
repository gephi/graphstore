package org.gephi.graph.impl;

import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraphView;
import org.gephi.graph.api.HierarchicalNodeGroup;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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

    @Test
    public void testNeighbors() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        HierarchicalGraphView view = store.createHierarchicalView();
        DirectedSubgraph graph = store.getDirectedGraph(view);

        Set<Integer> types = new TreeSet<Integer>();
        for (Node node : graphStore.getNodes().toArray()) {
            graph.addNode(node);
        }
        for (Edge edge : graphStore.getEdges().toArray()) {
            graph.addEdge(edge);
            types.add(edge.getType());
        }

        Node parentNode = store.getGraph(view).getNode("5");
        HierarchicalNodeGroup group = view.getRoot().addNode(parentNode);
        Node childNode1 = store.getGraph(view).getNode("1");
        group.addNode(childNode1);
        Node childNode2 = store.getGraph(view).getNode("2");
        group.addNode(childNode2);

        int parentNeighborsCount = graph.getNeighbors(parentNode).toCollection().size();

        Collection<Edge> expandedEdges = new HashSet<Edge>();
        for (int type : types) {
            expandedEdges.addAll(graph.getEdges(parentNode, type).toCollection());
        }
        Assert.assertFalse(expandedEdges.isEmpty());

        group.collapse();

        Collection<Edge> collapsedEdges = new HashSet<Edge>();
        for (int type : types) {
            collapsedEdges.addAll(graph.getEdges(parentNode, type).toCollection());
        }
        Assert.assertFalse(collapsedEdges.isEmpty());
        Assert.assertTrue(collapsedEdges.size() > expandedEdges.size());

        group.expand();

        Collection<Edge> resetEdges = new HashSet<Edge>();
        for (int type : types) {
            resetEdges.addAll(graph.getEdges(parentNode, type).toCollection());
        }
        Assert.assertFalse(resetEdges.isEmpty());
        Assert.assertEquals(expandedEdges.size(), resetEdges.size());

        for (Node child : Arrays.asList(childNode1, childNode2)) {
            Collection<Node> originalNeighbors = graph.getNeighbors(child).toCollection();
            Assert.assertNotNull(originalNeighbors.isEmpty());
            for (Node neighbor : originalNeighbors) {
                Collection<Edge> edges = new HashSet<Edge>();
                for (int type : types) {
                    edges.addAll(graph.getEdges(child, neighbor, type).toCollection());
                    edges.addAll(graph.getEdges(neighbor, child, type).toCollection());
                }
                Assert.assertFalse(edges.isEmpty());
            }

            group.collapse();

            Collection<Node> collapsedNeighbors = graph.getNeighbors(child).toCollection();
            Assert.assertTrue(collapsedNeighbors.isEmpty());
            Assert.assertTrue(graph.getNeighbors(parentNode).toCollection().size() > parentNeighborsCount);
            for (Node neighbor : originalNeighbors) {
                Collection<Edge> edges = new HashSet<Edge>();
                for (int type : types) {
                    edges.addAll(graph.getEdges(child, neighbor, type).toCollection());
                    edges.addAll(graph.getEdges(neighbor, child, type).toCollection());
                }
                Assert.assertTrue(edges.isEmpty());
            }

            group.expand();

            Collection<Node> expandedNeighbors = graph.getNeighbors(child).toCollection();
            Assert.assertEquals(originalNeighbors.size(), expandedNeighbors.size());
            Assert.assertEquals(parentNeighborsCount, graph.getNeighbors(parentNode).toCollection().size());
            for (Node neighbor : originalNeighbors) {
                Collection<Edge> edges = new HashSet<Edge>();
                for (int type : types) {
                    edges.addAll(graph.getEdges(child, neighbor, type).toCollection());
                    edges.addAll(graph.getEdges(neighbor, child, type).toCollection());
                }
                Assert.assertFalse(edges.isEmpty());
            }
        }
    }
}
