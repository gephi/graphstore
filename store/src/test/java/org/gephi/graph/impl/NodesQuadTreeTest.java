package org.gephi.graph.impl;

import java.util.Arrays;
import java.util.Collection;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NodesQuadTreeTest {

    private static final float BOUNDS = 1e6f;
    private static final Rect2D BOUNDS_RECT = new Rect2D(-BOUNDS, -BOUNDS, BOUNDS, BOUNDS);

    @Test
    public void testGetAll() {
        final NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        Node n1 = new NodeImpl("1");
        n1.setPosition(100, 100);

        Node n2 = new NodeImpl("2");
        n2.setPosition(0, 0);

        Node n3 = new NodeImpl("3");
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
        final NodesQuadTree q = new NodesQuadTree(0, 0, 10, 10);

        Node n1 = new NodeImpl("1");
        n1.setPosition(100, 100);
        n1.setSize(10);

        Node n2 = new NodeImpl("2");
        n2.setPosition(0, 0);
        n2.setSize(5);

        Node n3 = new NodeImpl("3");
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

        Node n1 = new NodeImpl("1");
        n1.setPosition(100, 100);
        n1.setSize(10);

        Node n2 = new NodeImpl("2");
        n2.setPosition(0, 0);
        n2.setSize(5);

        Node n3 = new NodeImpl("3");
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
        final NodesQuadTree q = new NodesQuadTree(-120, -120, 120, 120);

        Node n1 = new NodeImpl("1");
        n1.setPosition(100, 100);
        n1.setSize(10);

        Node n2 = new NodeImpl("2");
        n2.setPosition(0, 0);
        n2.setSize(5);

        Node n3 = new NodeImpl("3");
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

    private void assertSame(NodeIterable iterable, Collection<Node> expected) {
        Collection<Node> found = iterable.toCollection();
        try {
            Assert.assertEquals(found, expected);
        } catch (AssertionError ex) {
            System.out.println("Found: " + listIds(found));
            System.out.println("Expected: " + listIds(expected));
            throw ex;
        }
    }

    private void assertSame(NodeIterable iterable, Node... expected) {
        assertSame(iterable, Arrays.asList(expected));
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
