package org.gephi.graph.impl;

import java.util.Arrays;
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
        Assert.assertEquals(iterable.toCollection(), Arrays.asList(expected));
    }

    private void assertEmpty(NodeIterable iterable) {
        Assert.assertEquals(iterable.toCollection().size(), 0);
    }

    private String listIds(Collection<Node> nodes) {
        StringBuilder sb = new StringBuilder();

        for (Node node : nodes) {
            sb.append(node.getId()).append(' ');
        }

        return sb.toString();
    }
}
