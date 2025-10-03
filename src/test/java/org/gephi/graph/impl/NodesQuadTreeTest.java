/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gephi.graph.impl;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
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
        Assert.assertEquals(q.getNodeCount(false), 1);
    }

    @Test
    public void testGetNodeCount() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        Assert.assertEquals(q.getNodeCount(false), 1);
        Assert.assertEquals(q.getNodeCount(true), 0);
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
        Assert.assertEquals(q.getNodeCount(true), 0);
    }

    @Test
    public void testAddNode() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        NodeImpl node = new NodeImpl("0");
        Assert.assertTrue(q.addNode(node));
        Assert.assertNotNull(node.getSpatialData().quadTreeNode);
        Assert.assertEquals(q.getNodeCount(true), 1);
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
        Assert.assertEquals(q.getNodeCount(true), 0);
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
        Assert.assertEquals(q.getNodeCount(true), 4);
        Assert.assertEquals(q.getNodeCount(false), 5);
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

        Collection<Node> bigRectContainingAll = q.getNodes(new Rect2D(-BOUNDS * 2, -BOUNDS * 2, BOUNDS, BOUNDS))
                .toCollection();
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
        all = q.getAllNodes().stream().collect(Collectors.toList());
        Assert.assertEquals(all.size(), 3);

        assertEmpty(q.getNodes(new Rect2D(80, 80, 89.99f, 89.99f)));

        assertSameSet(q.getNodes(new Rect2D(95, 95, 99, 99)), n1);
        assertSameSet(q.getNodes(new Rect2D(0, 0, 101, 101)), n1, n2);
        assertSameSet(q.getNodes(new Rect2D(4, 4, 91, 91)), n1, n2);
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

        assertEmpty(q.getNodes(new Rect2D(80, 80, 89.99f, 89.99f)));

        assertSameSet(q.getNodes(new Rect2D(95, 95, 99, 99)), n1);
        assertSameSet(q.getNodes(new Rect2D(0, 0, 101, 101)), n2, n1);
        assertSameSet(q.getNodes(new Rect2D(4, 4, 91, 91)), n2, n1);
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

        assertEmpty(q.getNodes(new Rect2D(80, 80, 89.99f, 89.99f)));

        assertSameSet(q.getNodes(new Rect2D(95, 95, 99, 99)), n1);
        assertSameSet(q.getNodes(new Rect2D(0, 0, 101, 101)), n1, n2);
        assertSameSet(q.getNodes(new Rect2D(4, 4, 91, 91)), n1, n2);
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

    @Test
    public void testGetMaximumObjectsReached() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        addRandomNodes(q, GraphStoreConfiguration.SPATIAL_INDEX_MAX_OBJECTS_PER_NODE, 0);
        Assert.assertEquals(q.getObjectCount(), GraphStoreConfiguration.SPATIAL_INDEX_MAX_OBJECTS_PER_NODE);
        Assert.assertEquals(q.getNodeCount(true), 1);
        Assert.assertEquals(q.getNodeCount(false), 1);
        NodeImpl[] newNodes = addRandomNodes(q, 1, GraphStoreConfiguration.SPATIAL_INDEX_MAX_OBJECTS_PER_NODE);
        Assert.assertEquals(q.getNodeCount(true), 4);
        Assert.assertEquals(q.getObjectCount(), GraphStoreConfiguration.SPATIAL_INDEX_MAX_OBJECTS_PER_NODE + 1);
        q.removeNode(newNodes[0]);
        Assert.assertEquals(q.getObjectCount(), GraphStoreConfiguration.SPATIAL_INDEX_MAX_OBJECTS_PER_NODE);
    }

    @Test
    public void testCountsWithLargerDepth() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        int totalNodes = GraphStoreConfiguration.SPATIAL_INDEX_MAX_OBJECTS_PER_NODE * 10;
        addRandomNodes(q, totalNodes, 0);
        Assert.assertEquals(q.getObjectCount(), totalNodes);
        Assert.assertTrue(q.getNodeCount(true) >= 10);
        Assert.assertTrue(q.getNodeCount(false) >= 10);
    }

    @Test
    public void testIteratorWithLargerGraph() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);
        int totalNodes = 100000;
        NodeImpl[] nodes = addRandomNodes(q, totalNodes, 0);
        assertSameSet(q.getAllNodes(), nodes);
    }

    @Test
    public void testGetNodesInArea() {
        Rect2D area = new Rect2D(-1000, -1000, 1000, 1000);
        NodesQuadTree q = new NodesQuadTree(area);
        int totalNodes = 30000;
        NodeImpl[] nodes = addRandomNodes(q, totalNodes, 0, area);
        Rect2D subarea = new Rect2D(-100, -100, 100, 100);

        assertSameSet(q
                .getNodes(subarea, false), Arrays
                        .stream(nodes).filter(n -> subarea.intersects(n.getSpatialData().minX, n
                                .getSpatialData().minY, n.getSpatialData().maxX, n.getSpatialData().maxY))
                        .toArray(NodeImpl[]::new));
    }

    @Test
    public void testGetNodesInAreaApproximate() {
        Rect2D area = new Rect2D(-1000, -1000, 1000, 1000);
        NodesQuadTree q = new NodesQuadTree(area);
        int totalNodes = 30000;
        NodeImpl[] nodes = addRandomNodes(q, totalNodes, 0, area);
        Rect2D subarea = new Rect2D(-100, -100, -1, -1);

        // Approximate should return all nodes that are in the quadtree nodes
        // intersecting the area
        assertSameSet(q.getNodes(subarea, true), Arrays.stream(nodes)
                .filter(n -> subarea.intersects(n.getSpatialData().quadTreeNode.quadRect())).toArray(NodeImpl[]::new));
    }

    @Test
    public void testGetNodesInAreaWithPredicate() {
        NodesQuadTree q = new NodesQuadTree(BOUNDS_RECT);

        Rect2D rect = new Rect2D(-10, -10, 10, 10);
        NodeImpl[] nodes = addRandomNodes(q, 2, 0, rect);
        NodeImpl node1 = nodes[0];

        assertSameSet(q.getNodes(rect, false, n -> n == node1), node1);
    }

    @Test
    public void testGetAllEdges() {
        GraphStore store = GraphGenerator.generateEmptyGraphStore(getConfig());
        NodesQuadTree q = store.spatialIndex.nodesTree;
        NodeImpl[] nodes = addRandomNodes(q, 2, 0);
        EdgeImpl[] edges = addRandomEdges(store, nodes, 10);

        assertSameSet(q.getEdges(), edges);
    }

    @Test
    public void testGetAllEdgesLarge() {
        GraphStore store = GraphGenerator.generateEmptyGraphStore(getConfig());
        NodesQuadTree q = store.spatialIndex.nodesTree;
        NodeImpl[] nodes = addRandomNodes(q, 10000, 0);
        EdgeImpl[] edges = addRandomEdges(store, nodes, 100000);

        assertSameSet(q.getEdges(), edges);
    }

    @Test
    public void testGetEdgesInArea() {
        Rect2D area = new Rect2D(-1000, -1000, 1000, 1000);
        GraphStore store = GraphGenerator.generateEmptyGraphStore(getConfig());
        NodesQuadTree q = store.spatialIndex.nodesTree;
        NodeImpl[] nodes = addRandomNodes(q, 30000, 0, area);
        EdgeImpl[] edges = addRandomEdges(store, nodes, 100000);

        Rect2D subarea = new Rect2D(-100, -100, 100, 100);

        assertSameSet(q.getEdges(subarea, false), Arrays.stream(edges)
                .filter(e -> edgeIntersectsArea(e, subarea, false)).toArray(EdgeImpl[]::new));
    }

    @Test
    public void testGetEdgesInAreaGlobal() {
        Rect2D area = new Rect2D(-1000, -1000, 1000, 1000);
        GraphStore store = GraphGenerator.generateEmptyGraphStore(getConfig());
        NodesQuadTree q = store.spatialIndex.nodesTree;
        NodeImpl[] nodes = addRandomNodes(q, 30000, 0, area);
        EdgeImpl[] edges = addRandomEdges(store, nodes, 100000);

        Rect2D subarea = new Rect2D(-600, -600, 600, 600);

        assertSameSet(q.getEdges(subarea, false), Arrays.stream(edges)
                .filter(e -> edgeIntersectsArea(e, subarea, false)).toArray(EdgeImpl[]::new));
    }

    @Test
    public void testGetEdgesInAreaApproximate() {
        Rect2D area = new Rect2D(-1000, -1000, 1000, 1000);
        GraphStore store = GraphGenerator.generateEmptyGraphStore(getConfig());
        NodesQuadTree q = store.spatialIndex.nodesTree;
        NodeImpl[] nodes = addRandomNodes(q, 30000, 0, area);
        EdgeImpl[] edges = addRandomEdges(store, nodes, 100000);

        Rect2D subarea = new Rect2D(-100, -100, -1, -1);

        assertSameSet(q.getEdges(subarea, true), Arrays.stream(edges).filter(e -> edgeIntersectsArea(e, subarea, true))
                .toArray(EdgeImpl[]::new));
    }

    @Test
    public void testGetEdgesInAreaWithPredicate() {
        GraphStore store = GraphGenerator.generateEmptyGraphStore(getConfig());
        NodesQuadTree q = store.spatialIndex.nodesTree;

        Rect2D rect = new Rect2D(-10, -10, 10, 10);
        NodeImpl[] nodes = addRandomNodes(q, 2, 0, rect);
        EdgeImpl[] edges = addRandomEdges(store, nodes, 2);
        EdgeImpl edge1 = edges[0];

        assertSameSet(q.getEdges(rect, false), edges);
        assertSameSet(q.getEdges(rect, false, e -> e == edge1), edge1);
        assertSameSet(q.getEdges(rect, true), edges);
        assertSameSet(q.getEdges(rect, true, e -> e == edge1), edge1);
    }

    @Test
    public void testGetEdgesInAreaBidirectional() {
        Rect2D rect = new Rect2D(100, 100, 100, 100);
        GraphStore store = GraphGenerator.generateEmptyGraphStore(getConfig());
        NodeImpl[] nodes = GraphGenerator.generateNodeList(10000, store, rect);
        store.addAllNodes(Arrays.asList(nodes));
        NodesQuadTree q = store.spatialIndex.nodesTree;

        nodes[0].setPosition(-1000, -1000);
        EdgeImpl[] edges = addRandomEdges(store, new NodeImpl[] { nodes[0], nodes[1] }, 1);

        // Edge should be returned once, as only one node is in the area
        assertSameSetAndCount(q.getEdges(new Rect2D(-2000, -2000, -999, -999), false), edges);

        nodes[1].setPosition(-1000, -1000);

        // Edge should be returned twice, once for each node
        assertSameSetAndCount(q
                .getEdges(new Rect2D(-2000, -2000, -999, -999), false), new EdgeImpl[] { edges[0], edges[0] });
    }

    // Utils

    private void assertSameSet(NodeIterable iterable, Node... expected) {
        Assert.assertTrue(expected.length > 0, "Expected array must not be empty");
        ObjectSet<Node> set = new ObjectOpenHashSet<>(expected.length);
        set.addAll(Arrays.asList(expected));
        Assert.assertEquals(iterable.toSet(), set);
        Assert.assertEquals(iterable.stream().collect(Collectors.toSet()), set);
        Assert.assertEquals(iterable.parallelStream().collect(Collectors.toSet()), set);
    }

    private void assertSameSetAndCount(NodeIterable iterable, Node... expected) {
        assertSameSet(iterable, expected);
        Assert.assertEquals(iterable.toCollection().size(), expected.length);
        Assert.assertEquals(iterable.stream().count(), expected.length);
        Assert.assertEquals(iterable.parallelStream().count(), expected.length);
    }

    private void assertSameSet(EdgeIterable iterable, Edge... expected) {
        Assert.assertTrue(expected.length > 0, "Expected array must not be empty");
        ObjectSet<Edge> set = new ObjectOpenHashSet<>(expected.length);
        set.addAll(Arrays.asList(expected));
        Assert.assertEquals(iterable.toSet(), set);
        Assert.assertEquals(iterable.stream().collect(Collectors.toSet()), set);
        Assert.assertEquals(iterable.parallelStream().collect(Collectors.toSet()), set);
    }

    private void assertSameSetAndCount(EdgeIterable iterable, Edge... expected) {
        assertSameSet(iterable, expected);
        Assert.assertEquals(iterable.toCollection().size(), expected.length);
        Assert.assertEquals(iterable.stream().count(), expected.length);
        Assert.assertEquals(iterable.parallelStream().count(), expected.length);
    }

    private void assertEmpty(NodeIterable iterable) {
        Assert.assertEquals(iterable.toCollection().size(), 0);
    }

    private NodeImpl[] addRandomNodes(GraphStore store, int count, int startIndex) {
        return addRandomNodes(store, count, startIndex, BOUNDS_RECT);
    }

    private NodeImpl[] addRandomNodes(GraphStore store, int count, int startIndex, Rect2D area) {
        NodeImpl[] nodes = generateNodes(count, startIndex, area);
        for (NodeImpl n : nodes) {
            store.addNode(n);
        }
        return nodes;
    }

    private NodeImpl[] addRandomNodes(NodesQuadTree q, int count, int startIndex) {
        return addRandomNodes(q, count, startIndex, BOUNDS_RECT);
    }

    private NodeImpl[] addRandomNodes(NodesQuadTree q, int count, int startIndex, Rect2D area) {
        NodeImpl[] nodes = generateNodes(count, startIndex, area);
        for (NodeImpl n : nodes) {
            q.addNode(n);
        }
        return nodes;
    }

    private NodeImpl[] generateNodes(int count, int startIndex, Rect2D area) {
        Random rand = new Random();
        NodeImpl[] nodes = new NodeImpl[count];
        for (int i = 0; i < count; i++) {
            NodeImpl node = new NodeImpl(String.valueOf(startIndex++));
            float x = area.minX + rand.nextFloat() * (area.maxX - area.minX);
            float y = area.minY + rand.nextFloat() * (area.maxY - area.minY);
            node.setPosition(x, y);
            node.setSize(1.0f);
            nodes[i] = node;
        }
        return nodes;
    }

    private EdgeImpl[] addRandomEdges(GraphStore store, NodeImpl[] nodes, int count) {
        for (NodeImpl n : nodes) {
            store.addNode(n);
        }
        EdgeImpl[] edges = new EdgeImpl[count];
        int edgeIndex = 0;
        while (edgeIndex < count) {
            NodeImpl source = nodes[new Random().nextInt(nodes.length)];
            NodeImpl target = nodes[new Random().nextInt(nodes.length)];
            if (source != target) {
                EdgeImpl edge = new EdgeImpl(String.valueOf(edgeIndex), store, source, target, 0, 1.0, true);
                edges[edgeIndex] = edge;
                store.addEdge(edge);
                edgeIndex++;
            }
        }
        return edges;
    }

    private Configuration getConfig() {
        return Configuration.builder().enableSpatialIndex(true).build();
    }

    private boolean edgeIntersectsArea(EdgeImpl e, Rect2D area, boolean approximate) {
        if (approximate) {
            return area.intersects(e.source.getSpatialData().quadTreeNode.quadRect()) || area
                    .intersects(e.target.getSpatialData().quadTreeNode.quadRect());
        }
        return area.intersects(e.source.getSpatialData().minX, e.source.getSpatialData().minY, e.source
                .getSpatialData().maxX, e.source.getSpatialData().maxY) || area
                        .intersects(e.target.getSpatialData().minX, e.target.getSpatialData().minY, e.target
                                .getSpatialData().maxX, e.target.getSpatialData().maxY);
    }
}
