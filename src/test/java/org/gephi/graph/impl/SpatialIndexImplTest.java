package org.gephi.graph.impl;

import java.util.Arrays;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SpatialIndexImplTest {

    private static final float BOUNDS = 1000f;
    private static final Rect2D BOUNDS_RECT = new Rect2D(-BOUNDS, -BOUNDS, BOUNDS, BOUNDS);

    @Test
    public void testGetEdgesEmpty() {
        SpatialIndexImpl spatialIndex = new GraphStore(null, getConfig()).spatialIndex;
        Assert.assertTrue(spatialIndex.getEdgesInArea(BOUNDS_RECT).toCollection().isEmpty());
    }

    @Test
    public void testGetElementsBothNodesVisible() {
        GraphStore store = GraphGenerator.generateTinyGraphStore(getConfig());

        NodeImpl n1 = store.getNode("1");
        NodeImpl n2 = store.getNode("2");
        EdgeImpl e = store.getEdge("0");

        SpatialIndexImpl spatialIndex = store.spatialIndex;
        assertSame(spatialIndex.getNodesInArea(BOUNDS_RECT), n1, n2);
        assertSame(spatialIndex.getEdgesInArea(BOUNDS_RECT), e);
    }

    @Test
    public void testGetElementsOneNodeVisible() {
        GraphStore store = GraphGenerator.generateTinyGraphStore(getConfig());

        NodeImpl n1 = store.getNode("1");
        n1.setPosition(300000f, 300000f);
        NodeImpl n2 = store.getNode("2");
        EdgeImpl e = store.getEdge("0");

        SpatialIndexImpl spatialIndex = store.spatialIndex;
        assertSame(spatialIndex.getNodesInArea(BOUNDS_RECT), n2);
        assertSame(spatialIndex.getEdgesInArea(BOUNDS_RECT), e);
    }

    @Test
    public void testGetElementsWithoutNodeVisible() {
        GraphStore store = GraphGenerator.generateTinyGraphStore(getConfig());

        NodeImpl n1 = store.getNode("1");
        n1.setPosition(300000f, 300000f);
        NodeImpl n2 = store.getNode("2");
        n2.setPosition(300001f, 300001f);
        EdgeImpl e = store.getEdge("0");

        SpatialIndexImpl spatialIndex = store.spatialIndex;
        Assert.assertTrue(spatialIndex.getNodesInArea(BOUNDS_RECT).toCollection().isEmpty());
        Assert.assertTrue(spatialIndex.getEdgesInArea(BOUNDS_RECT).toCollection().isEmpty());
    }

    @Test
    public void testGetElementsWithSelfLoop() {
        GraphStore store = GraphGenerator.generateTinyGraphStoreWithSelfLoop(getConfig());

        NodeImpl n1 = store.getNode("1");
        EdgeImpl e = store.getEdge("0");

        SpatialIndexImpl spatialIndex = store.spatialIndex;
        assertSame(spatialIndex.getNodesInArea(BOUNDS_RECT), n1);
        assertSame(spatialIndex.getEdgesInArea(BOUNDS_RECT), e);
    }

    private void assertSame(NodeIterable iterable, Node... expected) {
        Assert.assertEquals(iterable.toCollection(), Arrays.asList(expected));
    }

    private void assertSame(EdgeIterable iterable, Edge... expected) {
        Assert.assertEquals(iterable.toCollection(), Arrays.asList(expected));
    }

    // Configuration with spatial index
    private Configuration getConfig() {
        return Configuration.builder().enableSpatialIndex(true).build();
    }
}
