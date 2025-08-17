package org.gephi.graph.impl;

import java.util.Collection;
import java.util.Random;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NodesQuadTreeTest {

    private static final float BOUNDS = 1e6f;
    private static final Rect2D BOUNDS_RECT = new Rect2D(-BOUNDS, -BOUNDS, BOUNDS, BOUNDS);

    @Test
    public void testBoundaries() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        Assert.assertEquals(q.quadRect(), BOUNDS_RECT);
    }

    @Test
    public void testGetAllNodesEmpty() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        Assert.assertTrue(q.getAllNodes().toCollection().isEmpty());
        Assert.assertEquals(q.getAllNodes().toArray().length, 0);
    }

    @Test
    public void testGetNodesEmpty() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        Assert.assertTrue(q.getNodes(new Rect2D(-1, -1, 1, 1)).toCollection().isEmpty());
    }

    @Test
    public void testRemoveEmpty() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        NodeImpl node = new NodeImpl("0");
        Assert.assertFalse(q.removeNode(node));
    }

    @Test
    public void testAddNode() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        NodeImpl node = new NodeImpl("0");
        Assert.assertTrue(q.addNode(node));
        Assert.assertNotNull(node.getSpatialData().quadTreeNode);
    }

    @Test
    public void testAddNodeTwice() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        NodeImpl node = new NodeImpl("0");
        q.addNode(node);
        Assert.assertFalse(q.addNode(node));
    }

    @Test
    public void testClear() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        NodeImpl node = new NodeImpl("0");
        q.addNode(node);
        q.clear();
        Assert.assertNull(node.getSpatialData().quadTreeNode);
        Assert.assertTrue(q.getAllNodes().toCollection().isEmpty());
        Assert.assertFalse(q.removeNode(node));
    }

    @Test
    public void testCount() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        Assert.assertEquals(q.getObjectCount(), 0);
        NodeImpl node = new NodeImpl("0");
        q.addNode(node);
        Assert.assertEquals(q.getObjectCount(), 1);
        q.clear();
        Assert.assertEquals(q.getObjectCount(), 0);
    }

    @Test
    public void testUpdate() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        Assert.assertEquals(q.getObjectCount(), 0);
        NodeImpl node = new NodeImpl("0");
        q.addNode(node);
        Assert.assertTrue(q.updateNode(node, -100, -50, -50, 0));
    }

    @Test
    public void testDepthZero() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        Assert.assertEquals(q.getDepth(), 0);
    }

    @Test
    public void testDepth() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        Random random = new Random();
        for (int i = 0; i <= GraphStoreConfiguration.SPATIAL_INDEX_MAX_OBJECTS_PER_NODE; i++) {
            NodeImpl node = new NodeImpl(String.valueOf(i));
            node.setPosition(random.nextInt((int) BOUNDS * 2) - BOUNDS, random.nextInt((int) BOUNDS * 2) - BOUNDS);
            q.addNode(node);
        }
        Assert.assertTrue(q.getDepth() >= 1);
    }

    @Test
    public void testGetAll() {
        final NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(100, 100);
        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(0, 0);
        NodeImpl n3 = new NodeImpl("3");
        n2.setPosition(-100, -100);

        q.addNode(n1);
        q.addNode(n2);
        q.addNode(n3);

        Collection<Node> all = q.getAllNodes().toCollection();
        Assert.assertEquals(all.size(), 3);

        Collection<Node> rectContainingAll = q.getNodes(BOUNDS_RECT).toCollection();
        Assert.assertEquals(rectContainingAll, all);

        Collection<Node> bigRectContainingAll = q.getNodes(-BOUNDS * 2, -BOUNDS * 2, BOUNDS, BOUNDS).toCollection();
        Assert.assertEquals(bigRectContainingAll, all);
    }

    @Test
    public void testOutOfBoundsStillWorks() {
        final NodesQuadTree q = new NodesQuadTree(new Rect2D(0, 0, 10, 10));

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(100, 100);
        n1.setSize(10);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(0, 0);
        n2.setSize(5);

        NodeImpl n3 = new NodeImpl("3");
        n3.setPosition(-100, -100);
        n3.setSize(3);

        q.addNode(n1);
        q.addNode(n2);
        q.addNode(n3);

        Collection<Node> all = q.getAllNodes().toCollection();
        Assert.assertEquals(all.size(), 3);

        assertEmpty(q.getNodes(80, 80, 89.99f, 89.99f));

        assertSame(q.getNodes(95, 95, 99, 99), n1);
        assertSame(q.getNodes(0, 0, 101, 101), n1, n2);
        assertSame(q.getNodes(4, 4, 91, 91), n1, n2);
    }

    @Test
    public void testGetZone1() {
        final NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(100, 100);
        n1.setSize(10);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(0, 0);
        n2.setSize(5);

        NodeImpl n3 = new NodeImpl("3");
        n3.setPosition(-100, -100);
        n3.setSize(3);

        q.addNode(n1);
        q.addNode(n2);
        q.addNode(n3);

        assertEmpty(q.getNodes(80, 80, 89.99f, 89.99f));

        assertSame(q.getNodes(95, 95, 99, 99), n1);
        assertSame(q.getNodes(0, 0, 101, 101), n2, n1);
        assertSame(q.getNodes(4, 4, 91, 91), n2, n1);
    }

    @Test
    public void testGetZone2() {
        final NodesQuadTree q = new NodesQuadTree(new Rect2D(120, -120, 120, 120));

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(100, 100);
        n1.setSize(10);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(0, 0);
        n2.setSize(5);

        NodeImpl n3 = new NodeImpl("3");
        n3.setPosition(-100, -100);
        n3.setSize(3);

        q.addNode(n1);
        q.addNode(n2);
        q.addNode(n3);

        assertEmpty(q.getNodes(80, 80, 89.99f, 89.99f));

        assertSame(q.getNodes(95, 95, 99, 99), n1);
        assertSame(q.getNodes(0, 0, 101, 101), n1, n2);
        assertSame(q.getNodes(4, 4, 91, 91), n1, n2);
    }

    private void assertSame(NodeIterable iterable, Node... expected) {
        Assert.assertEqualsNoOrder(iterable.toArray(), expected);
    }

    private void assertEmpty(NodeIterable iterable) {
        Assert.assertEquals(iterable.toCollection().size(), 0);
    }

    @Test
    public void testGetBoundariesEmpty() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, Float.NEGATIVE_INFINITY);
        Assert.assertEquals(boundaries.minY, Float.NEGATIVE_INFINITY);
        Assert.assertEquals(boundaries.maxX, Float.POSITIVE_INFINITY);
        Assert.assertEquals(boundaries.maxY, Float.POSITIVE_INFINITY);
    }

    @Test
    public void testGetBoundariesSingleNode() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        NodeImpl node = new NodeImpl("0");
        node.setPosition(100, 200);
        node.setSize(10);

        q.addNode(node);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, 90f); // x - size
        Assert.assertEquals(boundaries.minY, 190f); // y - size
        Assert.assertEquals(boundaries.maxX, 110f); // x + size
        Assert.assertEquals(boundaries.maxY, 210f); // y + size
    }

    @Test
    public void testGetBoundariesMultipleNodes() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(0, 0);
        n1.setSize(5);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(100, 200);
        n2.setSize(10);

        NodeImpl n3 = new NodeImpl("3");
        n3.setPosition(-50, -100);
        n3.setSize(15);

        q.addNode(n1);
        q.addNode(n2);
        q.addNode(n3);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, -65f); // n3: -50 - 15
        Assert.assertEquals(boundaries.minY, -115f); // n3: -100 - 15
        Assert.assertEquals(boundaries.maxX, 110f); // n2: 100 + 10
        Assert.assertEquals(boundaries.maxY, 210f); // n2: 200 + 10
    }

    @Test
    public void testGetBoundariesAfterClear() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl node = new NodeImpl("0");
        node.setPosition(100, 200);
        node.setSize(10);
        q.addNode(node);

        // Should have boundaries initially
        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertNotEquals(boundaries.minX, Float.NEGATIVE_INFINITY);

        // After clear, should return empty rectangle
        q.clear();
        boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, Float.NEGATIVE_INFINITY);
        Assert.assertEquals(boundaries.minY, Float.NEGATIVE_INFINITY);
        Assert.assertEquals(boundaries.maxX, Float.POSITIVE_INFINITY);
        Assert.assertEquals(boundaries.maxY, Float.POSITIVE_INFINITY);
    }

    @Test
    public void testGetBoundariesAfterRemove() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(0, 0);
        n1.setSize(5);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(100, 200);
        n2.setSize(10);

        q.addNode(n1);
        q.addNode(n2);

        // Remove one node
        q.removeNode(n1);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, 90f); // Only n2 remains
        Assert.assertEquals(boundaries.minY, 190f);
        Assert.assertEquals(boundaries.maxX, 110f);
        Assert.assertEquals(boundaries.maxY, 210f);

        // Remove last node
        q.removeNode(n2);
        boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, Float.NEGATIVE_INFINITY);
        Assert.assertEquals(boundaries.minY, Float.NEGATIVE_INFINITY);
        Assert.assertEquals(boundaries.maxX, Float.POSITIVE_INFINITY);
        Assert.assertEquals(boundaries.maxY, Float.POSITIVE_INFINITY);
    }

    @Test
    public void testGetBoundariesAfterRemoveBoundaryNode() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(0, 0);
        n1.setSize(5);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(100, 200);
        n2.setSize(10); // This will be at the boundary

        NodeImpl n3 = new NodeImpl("3");
        n3.setPosition(50, 100);
        n3.setSize(8);

        q.addNode(n1);
        q.addNode(n2);
        q.addNode(n3);

        // Remove the node that was at the max boundary
        q.removeNode(n2);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, -5f); // n1: 0 - 5
        Assert.assertEquals(boundaries.minY, -5f); // n1: 0 - 5
        Assert.assertEquals(boundaries.maxX, 58f); // n3: 50 + 8
        Assert.assertEquals(boundaries.maxY, 108f); // n3: 100 + 8
    }

    @Test
    public void testGetBoundariesAfterUpdate() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl node = new NodeImpl("0");
        node.setPosition(0, 0);
        node.setSize(5);
        q.addNode(node);

        // Update position
        node.setPosition(100, 200);
        node.setSize(15);
        q.updateNode(node, 85, 185, 115, 215); // 100±15, 200±15

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, 85f);
        Assert.assertEquals(boundaries.minY, 185f);
        Assert.assertEquals(boundaries.maxX, 115f);
        Assert.assertEquals(boundaries.maxY, 215f);
    }

    @Test
    public void testGetBoundariesAfterUpdateBoundaryNode() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(0, 0);
        n1.setSize(5);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(100, 200);
        n2.setSize(10);

        q.addNode(n1);
        q.addNode(n2);

        // Move the boundary node to a smaller position
        n2.setPosition(50, 100);
        n2.setSize(5);
        q.updateNode(n2, 45, 95, 55, 105);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, -5f); // n1: 0 - 5
        Assert.assertEquals(boundaries.minY, -5f); // n1: 0 - 5
        Assert.assertEquals(boundaries.maxX, 55f); // n2: 50 + 5
        Assert.assertEquals(boundaries.maxY, 105f); // n2: 100 + 5
    }

    @Test
    public void testGetBoundariesWithZeroSizeNodes() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(10, 20);
        n1.setSize(0);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(-5, -10);
        n2.setSize(0);

        q.addNode(n1);
        q.addNode(n2);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, -5f);
        Assert.assertEquals(boundaries.minY, -10f);
        Assert.assertEquals(boundaries.maxX, 10f);
        Assert.assertEquals(boundaries.maxY, 20f);
    }

    @Test
    public void testGetBoundariesWithOutOfBoundsNodes() {
        NodesQuadTree q = new NodesQuadTree(new Rect2D(-10, -10, 10, 10));

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(100, 200); // Way out of bounds
        n1.setSize(5);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(0, 0); // Within bounds
        n2.setSize(2);

        q.addNode(n1);
        q.addNode(n2);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, -2f); // n2: 0 - 2
        Assert.assertEquals(boundaries.minY, -2f); // n2: 0 - 2
        Assert.assertEquals(boundaries.maxX, 105f); // n1: 100 + 5
        Assert.assertEquals(boundaries.maxY, 205f); // n1: 200 + 5
    }

    @Test
    public void testGetBoundariesWithNegativeCoordinates() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(-100, -200);
        n1.setSize(10);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(-50, -150);
        n2.setSize(5);

        q.addNode(n1);
        q.addNode(n2);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, -110f); // n1: -100 - 10
        Assert.assertEquals(boundaries.minY, -210f); // n1: -200 - 10
        Assert.assertEquals(boundaries.maxX, -45f); // n2: -50 + 5
        Assert.assertEquals(boundaries.maxY, -145f); // n2: -150 + 5
    }

    @Test
    public void testGetBoundariesWithMixedCoordinates() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl n1 = new NodeImpl("1");
        n1.setPosition(-50, 100);
        n1.setSize(20);

        NodeImpl n2 = new NodeImpl("2");
        n2.setPosition(75, -80);
        n2.setSize(15);

        q.addNode(n1);
        q.addNode(n2);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, -70f); // n1: -50 - 20
        Assert.assertEquals(boundaries.minY, -95f); // n2: -80 - 15
        Assert.assertEquals(boundaries.maxX, 90f); // n2: 75 + 15
        Assert.assertEquals(boundaries.maxY, 120f); // n1: 100 + 20
    }

    @Test
    public void testGetBoundariesSingleNodeAtOrigin() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        NodeImpl node = new NodeImpl("0");
        node.setPosition(0, 0);
        node.setSize(1);

        q.addNode(node);

        Rect2D boundaries = q.getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, -1f);
        Assert.assertEquals(boundaries.minY, -1f);
        Assert.assertEquals(boundaries.maxX, 1f);
        Assert.assertEquals(boundaries.maxY, 1f);
    }
}
