package org.gephi.graph.impl;

import java.util.Collections;
import java.util.Iterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EdgeTypeNoIndexTest {

    @Test
    public void testEmpty() {
        GraphStore store = GraphGenerator.generateEmptyGraphStore();
        EdgeTypeNoIndexImpl index = new EdgeTypeNoIndexImpl(store);
        Assert.assertEquals(index.countElements(), 0);
        Assert.assertEquals(index.countValues(), 0);
        Assert.assertEquals(index.count(null), 0);
        Assert.assertFalse(index.get(null).iterator().hasNext());
        Assert.assertFalse(index.isSortable());
        Assert.assertSame(index.getColumn(), store.getModel().defaultColumns().edgeType());
        Assert.assertTrue(index.values().isEmpty());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testMinValueException() {
        GraphStore store = new GraphStore();
        EdgeTypeNoIndexImpl index = new EdgeTypeNoIndexImpl(store);
        Assert.assertNull(index.getMinValue());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testMaxValueException() {
        GraphStore store = new GraphStore();
        EdgeTypeNoIndexImpl index = new EdgeTypeNoIndexImpl(store);
        Assert.assertNull(index.getMaxValue());
    }

    @Test
    public void testOneEdge() {
        GraphStore store = GraphGenerator.generateTinyGraphStore();
        EdgeTypeNoIndexImpl index = new EdgeTypeNoIndexImpl(store);
        Assert.assertEquals(index.countElements(), 1);
        Assert.assertEquals(index.countValues(), 1);
        Assert.assertEquals(index.count(null), 1);
    }

    @Test
    public void testSmallGraph() {
        Graph graph = GraphGenerator.generateSmallMultiTypeGraphStore();

        EdgeTypeNoIndexImpl index = new EdgeTypeNoIndexImpl(graph);
        Assert.assertEquals(index.countElements(), graph.getEdgeCount());
        Assert.assertEquals(index.countValues(), 3);
        Assert.assertEquals(index.count(null), graph.getEdgeCount(0));
        Assert.assertEquals(index.count("1"), graph.getEdgeCount(1));
    }

    @Test
    public void testValues() {
        Graph graph = GraphGenerator.generateTinyGraphStore();

        EdgeTypeNoIndexImpl index = new EdgeTypeNoIndexImpl(graph);
        Assert.assertEquals(index.values(), Collections.singletonList(null));
    }

    @Test
    public void testGetIterator() {
        Graph graph = GraphGenerator.generateTinyGraphStore();

        EdgeTypeNoIndexImpl index = new EdgeTypeNoIndexImpl(graph);
        Iterator<Edge> itr = index.get(null).iterator();
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(itr.next(), graph.getEdge("0"));
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testVersion() {
        Graph graph = GraphGenerator.generateTinyGraphStore();

        EdgeTypeNoIndexImpl index = new EdgeTypeNoIndexImpl(graph);
        int version = index.getVersion();
        graph.removeEdge(graph.getEdge("0"));
        Assert.assertNotEquals(index.getVersion(), version);
    }
}
