package org.gephi.graph.impl;

import java.util.Collections;
import java.util.Iterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DegreeNoIndexTest {

    @Test
    public void testEmpty() {
        GraphStore store = GraphGenerator.generateEmptyGraphStore();
        DegreeNoIndexImpl index = new DegreeNoIndexImpl(store, DegreeNoIndexImpl.DegreeType.DEGREE);
        Assert.assertEquals(index.countElements(), 0);
        Assert.assertEquals(index.countValues(), 0);
        Assert.assertEquals(index.count(0), 0);
        Assert.assertFalse(index.get(0).iterator().hasNext());
        Assert.assertTrue(index.isSortable());
        Assert.assertSame(index.getColumn(), store.getModel().defaultColumns().degree());
        Assert.assertNull(index.getMinValue());
        Assert.assertNull(index.getMaxValue());
        Assert.assertTrue(index.values().isEmpty());
    }

    @Test
    public void testOneNode() {
        GraphStore store = new GraphStore();
        Node node = store.factory.newNode();
        store.addNode(node);
        DegreeNoIndexImpl index = new DegreeNoIndexImpl(store, DegreeNoIndexImpl.DegreeType.DEGREE);
        Assert.assertEquals(index.countElements(), 1);
        Assert.assertEquals(index.countValues(), 1);
        Assert.assertEquals(index.count(0), 1);
        Assert.assertEquals(index.getMinValue().intValue(), 0);
        Assert.assertEquals(index.getMaxValue().intValue(), 0);
    }

    @Test
    public void testSmallGraph() {
        Graph graph = GraphGenerator.generateTinyGraphStore();
        Node node = graph.getModel().factory().newNode();
        graph.addNode(node);

        DegreeNoIndexImpl index = new DegreeNoIndexImpl(graph, DegreeNoIndexImpl.DegreeType.DEGREE);
        Assert.assertEquals(index.countElements(), 3);
        Assert.assertEquals(index.countValues(), 2);
        Assert.assertEquals(index.count(1), 2);
        Assert.assertEquals(index.getMinValue().intValue(), 0);
        Assert.assertEquals(index.getMaxValue().intValue(), 1);
    }

    @Test
    public void testValues() {
        Graph graph = GraphGenerator.generateTinyGraphStore();

        DegreeNoIndexImpl index = new DegreeNoIndexImpl(graph, DegreeNoIndexImpl.DegreeType.DEGREE);
        Assert.assertEquals(index.values(), Collections.singletonList(1));
    }

    @Test
    public void testGetIterator() {
        Graph graph = GraphGenerator.generateTinyGraphStore();

        DegreeNoIndexImpl index = new DegreeNoIndexImpl(graph, DegreeNoIndexImpl.DegreeType.DEGREE);
        Iterator<Node> itr = index.get(1).iterator();
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(itr.next(), graph.getNode("1"));
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(itr.next(), graph.getNode("2"));
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testInDegree() {
        Graph graph = GraphGenerator.generateTinyGraphStore();
        Edge edge = graph.getEdge("0");

        DegreeNoIndexImpl index = new DegreeNoIndexImpl(graph, DegreeNoIndexImpl.DegreeType.IN_DEGREE);
        index.get(1).iterator().forEachRemaining(n -> Assert.assertSame(n, edge.getTarget()));
        index.get(0).iterator().forEachRemaining(n -> Assert.assertSame(n, edge.getSource()));
    }

    @Test
    public void testOutDegree() {
        Graph graph = GraphGenerator.generateTinyGraphStore();
        Edge edge = graph.getEdge("0");

        DegreeNoIndexImpl index = new DegreeNoIndexImpl(graph, DegreeNoIndexImpl.DegreeType.OUT_DEGREE);
        index.get(0).iterator().forEachRemaining(n -> Assert.assertSame(n, edge.getTarget()));
        index.get(1).iterator().forEachRemaining(n -> Assert.assertSame(n, edge.getSource()));
    }

    @Test
    public void testVersion() {
        Graph graph = GraphGenerator.generateTinyGraphStore();

        DegreeNoIndexImpl index = new DegreeNoIndexImpl(graph, DegreeNoIndexImpl.DegreeType.DEGREE);
        int version = index.getVersion();
        graph.removeNode(graph.getNode("1"));
        Assert.assertNotEquals(index.getVersion(), version);
    }
}
