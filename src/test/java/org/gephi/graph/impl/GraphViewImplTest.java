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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.UndirectedSubgraph;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphViewImplTest {

    @Test
    public void testFill() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        UndirectedSubgraph unGraph = store.getUndirectedGraph(view);
        view.fill();

        Assert.assertEquals(view.getNodeCount(), graphStore.getNodeCount());
        Assert.assertEquals(view.getEdgeCount(), graphStore.getEdgeCount());
        for (Edge e : graphStore.getEdges()) {
            Assert.assertTrue(graph.contains(e));
        }
        for (Node n : graphStore.getNodes()) {
            Assert.assertTrue(graph.contains(n));
            Assert.assertEquals(graph.getDegree(n), graphStore.getDegree(n));
        }
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), graphStore.getEdgeCount(i));
        }
        for (Edge e : graphStore.undirectedDecorator.getEdges()) {
            Assert.assertTrue(unGraph.contains(e));
        }
        for (Node n : graphStore.undirectedDecorator.getNodes()) {
            Assert.assertTrue(unGraph.contains(n));
        }
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(unGraph.getEdgeCount(i), graphStore.undirectedDecorator.getEdgeCount(i));
        }
    }

    @Test
    public void testMainView() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewImpl view = new GraphViewStore(graphStore).createView();

        Assert.assertFalse(view.isMainView());
    }

    @Test
    public void testAddNodeMainView() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        NodeImpl node = new NodeImpl("A");
        graphStore.addNode(node);

        Assert.assertTrue(view.nodeBitVector.size() >= node.storeId);
        boolean a = view.addNode(node);
        Assert.assertTrue(a);
        Assert.assertTrue(view.containsNode(node));
    }

    @Test
    public void testAddEdgeMainView() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        NodeImpl source = new NodeImpl("A");
        NodeImpl target = new NodeImpl("B");
        graphStore.addNode(source);
        graphStore.addNode(target);
        view.addNode(source);
        view.addNode(target);

        EdgeImpl edge = new EdgeImpl("S", source, target, 0, 1.0, true);
        graphStore.addEdge(edge);

        Assert.assertTrue(view.edgeBitVector.size() >= edge.storeId);
        boolean a = view.addEdge(edge);
        Assert.assertTrue(a);
        Assert.assertTrue(view.containsEdge(edge));
    }

    @Test
    public void testEdgeViewNodeBehaviors() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true);
        Subgraph subgraph = store.getGraph(view);
        Assert.assertEquals(subgraph.getNodeCount(), graphStore.getNodeCount());
        Assert.assertEquals(subgraph.getNodes().stream().count(), graphStore.getNodeCount());
        for (Node node : subgraph.getNodes()) {
            Assert.assertSame(subgraph.getNode(node.getId()), node);
            Assert.assertEquals(subgraph.getDegree(node), 0);
        }
    }

    @Test
    public void testViewDeepEquals() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        NodeImpl n1 = graphStore.getNode("0");
        view.addNode(n1);

        Assert.assertTrue(view.deepEquals(view));

        GraphViewImpl view2 = store.createView();

        NodeImpl n2 = graphStore.getNode("0");
        view2.addNode(n2);

        Assert.assertTrue(view.deepEquals(view2));
    }

    @Test
    public void testViewDeepHashCode() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        NodeImpl n1 = graphStore.getNode("0");
        view.addNode(n1);

        Assert.assertEquals(view.hashCode(), view.hashCode());

        GraphViewImpl view2 = store.createView();

        NodeImpl n2 = graphStore.getNode("0");
        view2.addNode(n2);

        Assert.assertEquals(view.deepHashCode(), view2.deepHashCode());
    }

    @Test
    public void testViewIntersection() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        GraphViewImpl view2 = store.createView();

        EdgeImpl e1 = graphStore.getEdge("0");
        EdgeImpl e2 = graphStore.getEdge("5");
        NodeImpl n1 = e1.getSource();
        NodeImpl n2 = e1.getTarget();
        NodeImpl n3 = e2.getSource();
        NodeImpl n4 = e2.getTarget();
        view.addNode(n1);
        view2.addNode(n1);
        view.addNode(n2);
        view2.addNode(n2);

        view.addNode(n3);
        view.addNode(n4);

        view.addEdge(e1);
        view2.addEdge(e1);
        view.addEdge(e2);

        view.intersection(view2);

        // Positive assertions - expected elements ARE present
        Assert.assertTrue(view.containsNode(n1));
        Assert.assertTrue(view.containsNode(n2));
        Assert.assertTrue(view.containsEdge(e1));

        // Negative assertions - elements not in intersection should be absent
        Assert.assertFalse(view.containsNode(n3));
        Assert.assertFalse(view.containsNode(n4));
        Assert.assertFalse(view.containsEdge(e2));

        // Exact count assertions
        Assert.assertEquals(view.getNodeCount(), 2, "Should have exactly 2 nodes after intersection");
        Assert.assertEquals(view.getEdgeCount(), 1, "Should have exactly 1 edge after intersection");

        // Verify no other elements from the graph are present
        for (Node n : graphStore.getNodes()) {
            if (n != n1 && n != n2) {
                Assert.assertFalse(view
                        .containsNode((NodeImpl) n), "Node " + n.getId() + " should not be in view after intersection");
            }
        }
        for (Edge e : graphStore.getEdges()) {
            if (e != e1) {
                Assert.assertFalse(view
                        .containsEdge((EdgeImpl) e), "Edge " + e.getId() + " should not be in view after intersection");
            }
        }

        Assert.assertTrue(view2.deepEquals(view));
    }

    @Test
    public void testViewIntersectionEdgeView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true);
        GraphViewImpl view2 = store.createView(false, true);

        view.fill();
        view2.fill();

        int totalEdges = graphStore.getEdgeCount();
        EdgeImpl e1 = graphStore.getEdge("0");
        EdgeImpl e2 = graphStore.getEdge("5");

        view.removeEdge(e1);
        view2.removeEdge(e2);

        view.intersection(view2);

        // Negative assertions - removed edges should be absent
        Assert.assertFalse(view.containsEdge(e1));
        Assert.assertFalse(view.containsEdge(e2));

        // Exact count assertion - intersection excludes both removed edges
        Assert.assertEquals(view
                .getEdgeCount(), totalEdges - 2, "Should have all edges except e1 and e2 after intersection");

        // Verify all other edges are present
        for (Edge e : graphStore.getEdges()) {
            if (e != e1 && e != e2) {
                Assert.assertTrue(view
                        .containsEdge((EdgeImpl) e), "Edge " + e.getId() + " should be in view after intersection");
            }
        }
    }

    @Test
    public void testViewIntersectionNodeView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(true, false);
        GraphViewImpl view2 = store.createView();

        view.fill();
        view2.fill();

        int totalNodes = graphStore.getNodeCount();
        EdgeImpl e1 = graphStore.getEdge("0");
        EdgeImpl e2 = graphStore.getEdge("5");
        NodeImpl s1 = e1.getSource();

        view2.removeNode(s1);
        view2.removeEdge(e2);

        view.intersection(view2);

        // Node intersection: s1 was removed from view2, so it should be absent
        Assert.assertFalse(view.containsNode(s1));
        Assert.assertEquals(view.getNodeCount(), totalNodes - 1, "Should have all nodes except s1 after intersection");

        // Edge intersection: e1 removed because s1 is gone (node view), e2 present
        Assert.assertFalse(view.containsEdge(e1), "e1 should be absent (source node removed)");
        Assert.assertTrue(view.containsEdge(e2), "e2 should be present");

        // Verify all other nodes are present
        for (Node n : graphStore.getNodes()) {
            if (n != s1) {
                Assert.assertTrue(view
                        .containsNode((NodeImpl) n), "Node " + n.getId() + " should be in view after intersection");
            }
        }
    }

    @Test
    public void testViewUnion() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        GraphViewImpl view2 = store.createView();

        EdgeImpl e1 = graphStore.getEdge("0");
        EdgeImpl e2 = graphStore.getEdge("5");
        NodeImpl n1 = e1.getSource();
        NodeImpl n2 = e1.getTarget();
        NodeImpl n3 = e2.getSource();
        NodeImpl n4 = e2.getTarget();
        view.addNode(n1);
        view.addNode(n2);

        view2.addNode(n3);
        view2.addNode(n4);

        view.addEdge(e1);
        view2.addEdge(e2);

        view.union(view2);

        // Positive assertions - expected elements ARE present
        Assert.assertTrue(view.containsNode(n1));
        Assert.assertTrue(view.containsNode(n2));
        Assert.assertTrue(view.containsEdge(e1));
        Assert.assertTrue(view.containsNode(n3));
        Assert.assertTrue(view.containsNode(n4));
        Assert.assertTrue(view.containsEdge(e2));

        // Exact count assertions - verify ONLY expected elements
        Assert.assertEquals(view.getNodeCount(), 4, "Should have exactly 4 nodes after union");
        Assert.assertEquals(view.getEdgeCount(), 2, "Should have exactly 2 edges after union");

        // Negative assertions - verify other graph elements are NOT present
        for (Node n : graphStore.getNodes()) {
            if (n != n1 && n != n2 && n != n3 && n != n4) {
                Assert.assertFalse(view
                        .containsNode((NodeImpl) n), "Node " + n.getId() + " should not be in view after union");
            }
        }
        for (Edge e : graphStore.getEdges()) {
            if (e != e1 && e != e2) {
                Assert.assertFalse(view
                        .containsEdge((EdgeImpl) e), "Edge " + e.getId() + " should not be in view after union");
            }
        }
    }

    @Test
    public void testViewUnionEdgeView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true);
        GraphViewImpl view2 = store.createView(false, true);

        EdgeImpl e1 = graphStore.getEdge("0");
        EdgeImpl e2 = graphStore.getEdge("5");

        view.addEdge(e1);
        view2.addEdge(e2);

        view.union(view2);

        // Positive assertions
        Assert.assertTrue(view.containsEdge(e1));
        Assert.assertTrue(view.containsEdge(e2));

        // Exact count assertion
        Assert.assertEquals(view.getEdgeCount(), 2, "Should have exactly 2 edges after union");

        // Negative assertions - verify other edges are NOT present
        for (Edge e : graphStore.getEdges()) {
            if (e != e1 && e != e2) {
                Assert.assertFalse(view
                        .containsEdge((EdgeImpl) e), "Edge " + e.getId() + " should not be in view after union");
            }
        }
    }

    @Test
    public void testViewUnionNodeView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(true, false);
        GraphViewImpl view2 = store.createView(true, true);

        EdgeImpl e1 = graphStore.getEdge("0");
        EdgeImpl e2 = graphStore.getEdge("5");
        NodeImpl n1 = e1.getSource();
        NodeImpl n2 = e1.getTarget();
        NodeImpl n3 = e2.getSource();
        NodeImpl n4 = e2.getTarget();

        view2.addAllNodes(Arrays.asList(new NodeImpl[] { n1, n2, n3, n4 }));
        view2.addEdge(e1);
        Assert.assertFalse(view.containsEdge(e2));

        view.union(view2);

        // Positive assertions
        Assert.assertTrue(view.containsEdge(e1));
        Assert.assertTrue(view.containsEdge(e2), "e2 should be present (both endpoints are in union)");

        // All 4 nodes should be present
        Assert.assertTrue(view.containsNode(n1));
        Assert.assertTrue(view.containsNode(n2));
        Assert.assertTrue(view.containsNode(n3));
        Assert.assertTrue(view.containsNode(n4));
        Assert.assertEquals(view.getNodeCount(), 4, "Should have exactly 4 nodes after union");

        // Verify no other nodes are present
        for (Node n : graphStore.getNodes()) {
            if (n != n1 && n != n2 && n != n3 && n != n4) {
                Assert.assertFalse(view
                        .containsNode((NodeImpl) n), "Node " + n.getId() + " should not be in view after union");
            }
        }
    }

    @Test
    public void testViewNot() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        view.not();

        for (Node n : graphStore.getNodes()) {
            Assert.assertTrue(view.containsNode((NodeImpl) n));
        }
        for (Edge e : graphStore.getEdges()) {
            Assert.assertTrue(view.containsEdge((EdgeImpl) e));
        }
        Assert.assertEquals(view.getNodeCount(), graphStore.getNodeCount());
        Assert.assertEquals(view.getEdgeCount(), graphStore.getEdgeCount());

        view.not();

        Assert.assertEquals(view.getNodeCount(), 0);
        Assert.assertEquals(view.getEdgeCount(), 0);

        EdgeImpl e1 = graphStore.getEdge("0");
        NodeImpl n1 = e1.getSource();
        NodeImpl n2 = e1.getTarget();

        view.addNode(n1);
        view.addNode(n2);
        view.addEdge(e1);

        view.not();

        Assert.assertFalse(view.containsNode(n1));
        Assert.assertFalse(view.containsNode(n2));
        Assert.assertFalse(view.containsEdge(e1));
    }

    @Test
    public void testViewNotInterEdges() {
        GraphStore graphStore = new GraphModelImpl().store;
        GraphFactory factory = graphStore.factory;
        Node n1 = factory.newNode();
        Node n2 = factory.newNode();
        Node n3 = factory.newNode();
        graphStore.addAllNodes(Arrays.asList(new Node[] { n1, n2, n3 }));
        Edge e1 = factory.newEdge(n1, n2, false);
        Edge e2 = factory.newEdge(n1, n3, false);
        graphStore.addAllEdges(Arrays.asList(new Edge[] { e1, e2 }));

        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        view.fill();
        Graph viewGraph = store.getGraph(view);
        viewGraph.removeNode(n3);

        view.not();

        Assert.assertEquals(viewGraph.getNodeCount(), 1);
        Assert.assertEquals(viewGraph.getEdgeCount(), 0);
    }

    @Test
    public void testViewNotNodeView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(true, false);

        EdgeImpl e1 = graphStore.getEdge("0");
        NodeImpl n1 = e1.getSource();
        NodeImpl n2 = e1.getTarget();

        view.addNode(n1);
        view.addNode(n2);

        view.not();

        Assert.assertFalse(view.containsNode(n1));
        Assert.assertFalse(view.containsNode(n1));
        Assert.assertFalse(view.containsEdge(e1));

        view.not();

        Assert.assertTrue(view.containsNode(n1));
        Assert.assertTrue(view.containsNode(n2));
        Assert.assertTrue(view.containsEdge(e1));
    }

    @Test
    public void testNodeView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(true, false);

        for (Node n : graphStore.getNodes()) {
            view.addNode(n);

            Assert.assertTrue(view.containsNode((NodeImpl) n));
            for (Edge e : graphStore.getEdges(n)) {
                Node opposite = graphStore.getOpposite(n, e);
                if (view.containsNode((NodeImpl) opposite)) {
                    Assert.assertTrue(view.containsEdge((EdgeImpl) e));
                }
            }
        }
    }

    @Test
    public void testNodeViewEdgeUpdate() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(true, false);

        NodeImpl n1 = graphStore.getNode("0");
        NodeImpl n2 = graphStore.getNode("1");

        view.addNode(n1);
        view.addNode(n2);

        Assert.assertNull(graphStore.getEdge(n1, n2));
        EdgeImpl edge = (EdgeImpl) graphStore.factory.newEdge("edge", n1, n2, 0, 1.0, true);
        graphStore.addEdge(edge);

        Assert.assertTrue(view.containsEdge(edge));
    }

    @Test
    public void testEdgeViewUpdate() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true);

        GraphFactory factory = graphStore.factory;
        Node n1 = factory.newNode("foo1");
        Node n2 = factory.newNode("foo2");
        graphStore.addNode(n1);
        graphStore.addNode(n2);
        Assert.assertEquals(view.getNodeCount(), graphStore.getNodeCount());
        Edge e1 = factory.newEdge("foo", n1, n2, 0, 0.0, true);
        graphStore.addEdge(e1);
        Assert.assertFalse(view.containsEdge(e1));
    }

    @Test
    public void testIsNodeView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphView v1 = store.createView();
        GraphView v2 = store.createView(true, false);

        Assert.assertTrue(v1.isNodeView() && v1.isEdgeView());
        Assert.assertTrue(v2.isNodeView() && !v2.isEdgeView());
    }

    @Test
    public void testIsEdgeView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphView v1 = store.createView();
        GraphView v2 = store.createView(false, true);

        Assert.assertTrue(v1.isNodeView() && v1.isEdgeView());
        Assert.assertTrue(!v2.isNodeView() && v2.isEdgeView());
    }

    @Test
    public void testDefaultVisibleView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphView view = graphStore.viewStore.getVisibleView();

        Assert.assertNotNull(view);
        Assert.assertEquals(view, graphStore.mainGraphView);
    }

    @Test
    public void testMutualCounts() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStoreWithMutualEdge();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true);
        view.fill();
        Assert.assertEquals(view.getUndirectedEdgeCount(), 1);
        Assert.assertEquals(view.getEdgeCount(), 2);
        view.removeEdge(graphStore.getEdge("1"));
        Assert.assertEquals(view.getUndirectedEdgeCount(), 1);
    }

    @Test
    public void testEdgeViewSetEdgeType() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true);

        EdgeImpl e0 = graphStore.getEdge("0");
        Assert.assertEquals(view.getEdgeCount(0), 0);
        view.addEdge(e0);
        Assert.assertEquals(view.getEdgeCount(0), 1);
        e0.setType(1);
        Assert.assertEquals(view.getEdgeCount(0), 0);
        Assert.assertEquals(view.getEdgeCount(1), 1);
    }

    @Test
    public void testEdgeViewSetEdgeTypeMutualEdges() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStoreWithMutualEdge();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(true, false);
        view.fill();

        EdgeImpl e0 = graphStore.getEdge("0");
        e0.setType(1);
        Assert.assertEquals(view.getEdgeCount(0), 1);
        Assert.assertEquals(view.getEdgeCount(1), 1);
        Assert.assertEquals(view.getUndirectedEdgeCount(0), 1);
        Assert.assertEquals(view.getUndirectedEdgeCount(1), 1);

        EdgeImpl e1 = graphStore.getEdge("1");
        e1.setType(1);
        Assert.assertEquals(view.getEdgeCount(0), 0);
        Assert.assertEquals(view.getEdgeCount(1), 2);
        Assert.assertEquals(view.getUndirectedEdgeCount(0), 0);
        Assert.assertEquals(view.getUndirectedEdgeCount(1), 1);
    }

    @Test
    public void testCopyConstructor() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStoreWithMutualEdge();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        view.fill();

        // Original view should have mutual edges counted
        int originalEdgeCount = view.getEdgeCount();
        int originalMutualCount = view.mutualEdgesCount;
        int originalUndirectedCount = view.getUndirectedEdgeCount();

        // Create a copy using the copy constructor
        GraphViewImpl copiedView = new GraphViewImpl(view, true, true);

        // Verify
        Assert.assertEquals(copiedView.getNodeCount(), view
                .getNodeCount(), "Node count should be the same in copied view");
        Assert.assertEquals(copiedView.nodeBitVector, view.nodeBitVector, "Node bit vector should be the same in copied view");
        Assert.assertEquals(copiedView.edgeBitVector, view.edgeBitVector, "Edge bit vector should be the same in copied view");

        // Verify that mutualEdgesCount was copied correctly
        Assert.assertEquals(copiedView.mutualEdgesCount, originalMutualCount, "mutualEdgesCount should be copied in copy constructor");
        Assert.assertEquals(copiedView.getEdgeCount(), originalEdgeCount);
        Assert.assertEquals(copiedView
                .getUndirectedEdgeCount(), originalUndirectedCount, "getUndirectedEdgeCount() should return correct value after copy");
    }

    @Test
    public void testFilledViewRequiresExplicitAdd() {
        // Test that users must explicitly add edges to filled views
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        view.fill();
        int initialEdgeCount = view.getEdgeCount();

        // Add a new edge to the main graph store
        NodeImpl n1 = graphStore.getNode("0");
        NodeImpl n2 = graphStore.getNode("1");
        EdgeImpl newEdge = new EdgeImpl("newEdge", n1, n2, 0, 1.0, true);
        graphStore.addEdge(newEdge);

        // Edge should not be in view yet
        Assert.assertFalse(view.containsEdge(newEdge));

        // Explicitly add the edge to the view
        boolean added = view.addEdge(newEdge);

        // Now it should be in the view
        Assert.assertTrue(added, "addEdge should return true");
        Assert.assertTrue(view.containsEdge(newEdge), "Edge should be in view after explicit add");
        Assert.assertEquals(view.getEdgeCount(), initialEdgeCount + 1);
    }

    // ========== Tests for Mutual Edge Counts in Bulk Operations ==========

    @Test
    public void testIntersectionWithMutualEdges() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStoreWithMutualEdge();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView();
        GraphViewImpl view2 = store.createView();

        // Fill both views
        view1.fill();
        view2.fill();

        // Initial state: both views have mutual edges (mutual count is 1 for a pair)
        Assert.assertEquals(view1.mutualEdgesCount, 1, "View1 should have mutual count of 1");
        Assert.assertEquals(view1.getEdgeCount(), 2, "View1 should have 2 edges");
        Assert.assertEquals(view1.getUndirectedEdgeCount(), 1, "View1 should have 1 undirected edge");

        // Remove one of the mutual edges from view2
        EdgeImpl e0 = graphStore.getEdge("0");
        EdgeImpl e1 = graphStore.getEdge("1");
        view2.removeEdge(e1);

        // After removing one mutual edge, view2 should have no mutual edges
        Assert.assertEquals(view2.mutualEdgesCount, 0, "View2 should have 0 mutual edges after removal");
        Assert.assertEquals(view2.getEdgeCount(), 1, "View2 should have 1 edge");
        Assert.assertEquals(view2.getUndirectedEdgeCount(), 1, "View2 should have 1 undirected edge");

        // Intersection should result in view1 losing its mutual edge status
        view1.intersection(view2);

        Assert.assertEquals(view1.mutualEdgesCount, 0, "View1 should have 0 mutual edges after intersection");
        Assert.assertEquals(view1.getEdgeCount(), 1, "View1 should have 1 edge total");
        Assert.assertEquals(view1.getUndirectedEdgeCount(), 1, "View1 should have 1 undirected edge");

        // Verify which edge remains
        Assert.assertTrue(view1.containsEdge(e0), "e0 should remain after intersection");
        Assert.assertFalse(view1.containsEdge(e1), "e1 should be absent after intersection");
    }

    @Test
    public void testUnionWithMutualEdges() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStoreWithMutualEdge();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView();
        GraphViewImpl view2 = store.createView();

        EdgeImpl e0 = graphStore.getEdge("0");
        EdgeImpl e1 = graphStore.getEdge("1");
        NodeImpl n1 = e0.getSource();
        NodeImpl n2 = e0.getTarget();

        // View1 has only one edge of the mutual pair
        view1.addNode(n1);
        view1.addNode(n2);
        view1.addEdge(e0);

        // View2 has only the other edge of the mutual pair
        view2.addNode(n1);
        view2.addNode(n2);
        view2.addEdge(e1);

        // Neither view should have mutual edges yet
        Assert.assertEquals(view1.mutualEdgesCount, 0, "View1 should have 0 mutual edges initially");
        Assert.assertEquals(view2.mutualEdgesCount, 0, "View2 should have 0 mutual edges initially");

        // Union should create mutual edges (mutual count is 1 for a pair)
        view1.union(view2);

        Assert.assertEquals(view1.mutualEdgesCount, 1, "View1 should have mutual count of 1 after union");
        Assert.assertEquals(view1.getEdgeCount(), 2, "View1 should have 2 edges total");
        Assert.assertEquals(view1.getUndirectedEdgeCount(), 1, "View1 should have 1 undirected edge");

        // Verify both edges are present
        Assert.assertTrue(view1.containsEdge(e0), "e0 should be present after union");
        Assert.assertTrue(view1.containsEdge(e1), "e1 should be present after union");

        // Verify only the expected nodes are present
        Assert.assertEquals(view1.getNodeCount(), 2, "Should have exactly 2 nodes");
        Assert.assertTrue(view1.containsNode(n1));
        Assert.assertTrue(view1.containsNode(n2));
    }

    @Test
    public void testNotWithMutualEdges() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStoreWithMutualEdge();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        EdgeImpl e0 = graphStore.getEdge("0");
        NodeImpl n1 = e0.getSource();
        NodeImpl n2 = e0.getTarget();

        // Add only one edge of the mutual pair
        view.addNode(n1);
        view.addNode(n2);
        view.addEdge(e0);

        Assert.assertEquals(view.mutualEdgesCount, 0, "View should have 0 mutual edges initially");
        Assert.assertEquals(view.getEdgeCount(), 1, "View should have 1 edge");
        Assert.assertEquals(view.getNodeCount(), 2, "View should have 2 nodes");

        // NOT operation flips both nodes and edges
        // Since there are only 2 nodes total in the graph, after NOT we have 0 nodes
        // Edges without endpoints get removed, so we end up with 0 edges
        view.not();

        Assert.assertEquals(view.getNodeCount(), 0, "View should have 0 nodes after NOT (graph has 2 nodes total)");
        Assert.assertEquals(view.getEdgeCount(), 0, "View should have 0 edges after NOT (no nodes, so no edges)");
    }

    // ========== Tests for Multi-Type Edge Counts in Bulk Operations ==========

    @Test
    public void testIntersectionMultipleEdgeTypes() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView();
        GraphViewImpl view2 = store.createView();

        // Fill both views
        view1.fill();
        view2.fill();

        // Verify initial state has multiple edge types
        int type0CountInitial = view1.getEdgeCount(0);
        int type1CountInitial = view1.getEdgeCount(1);
        int type2CountInitial = view1.getEdgeCount(2);
        Assert.assertTrue(type0CountInitial > 0, "Should have type 0 edges");
        Assert.assertTrue(type1CountInitial > 0, "Should have type 1 edges");

        // Remove all type 0 edges from view2
        for (Edge e : graphStore.getEdges().toArray()) {
            if (e.getType() == 0) {
                view2.removeEdge(e);
            }
        }

        Assert.assertEquals(view2.getEdgeCount(0), 0, "View2 should have 0 type 0 edges");
        Assert.assertEquals(view2.getEdgeCount(1), type1CountInitial, "View2 should still have all type 1 edges");

        // Intersection should remove all type 0 edges from view1
        view1.intersection(view2);

        Assert.assertEquals(view1.getEdgeCount(0), 0, "View1 should have 0 type 0 edges after intersection");
        Assert.assertEquals(view1
                .getEdgeCount(1), type1CountInitial, "View1 should have all type 1 edges after intersection");
        Assert.assertEquals(view1
                .getEdgeCount(2), type2CountInitial, "View1 should have all type 2 edges after intersection");
        Assert.assertEquals(view1
                .getEdgeCount(), type1CountInitial + type2CountInitial, "Total edge count should match sum of type 1 and type 2");

        // Verify no type 0 edges are present
        for (Edge e : graphStore.getEdges()) {
            if (e.getType() == 0) {
                Assert.assertFalse(view1.containsEdge((EdgeImpl) e), "Type 0 edge " + e
                        .getId() + " should not be in view after intersection");
            }
        }
    }

    @Test
    public void testUnionMultipleEdgeTypes() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView();
        GraphViewImpl view2 = store.createView();

        // Add only type 0 edges to view1
        for (Node n : graphStore.getNodes()) {
            view1.addNode(n);
        }
        for (Edge e : graphStore.getEdges().toArray()) {
            if (e.getType() == 0) {
                view1.addEdge(e);
            }
        }

        // Add only type 1 and type 2 edges to view2
        for (Node n : graphStore.getNodes()) {
            view2.addNode(n);
        }
        for (Edge e : graphStore.getEdges().toArray()) {
            if (e.getType() == 1 || e.getType() == 2) {
                view2.addEdge(e);
            }
        }

        int type0Count = view1.getEdgeCount(0);
        int type1Count = view2.getEdgeCount(1);
        int type2Count = view2.getEdgeCount(2);

        Assert.assertTrue(type0Count > 0, "View1 should have type 0 edges");
        Assert.assertEquals(view2.getEdgeCount(0), 0, "View2 should have no type 0 edges");
        Assert.assertTrue(type1Count > 0, "View2 should have type 1 edges");
        Assert.assertTrue(type2Count > 0, "View2 should have type 2 edges");

        // Union should combine both types
        view1.union(view2);

        Assert.assertEquals(view1.getEdgeCount(0), type0Count, "View1 should have all type 0 edges after union");
        Assert.assertEquals(view1.getEdgeCount(1), type1Count, "View1 should have all type 1 edges after union");
        Assert.assertEquals(view1.getEdgeCount(2), type2Count, "View1 should have all type 2 edges after union");
        Assert.assertEquals(view1
                .getEdgeCount(), type0Count + type1Count + type2Count, "Total should be sum of all types");

        // Verify all edges of each type are present
        for (Edge e : graphStore.getEdges()) {
            Assert.assertTrue(view1.containsEdge((EdgeImpl) e), "Edge " + e.getId() + " of type " + e
                    .getType() + " should be in view after union");
        }
    }

    @Test
    public void testNotMultipleEdgeTypes() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        // Add all nodes but only type 0 edges
        for (Node n : graphStore.getNodes()) {
            view.addNode(n);
        }
        for (Edge e : graphStore.getEdges().toArray()) {
            if (e.getType() == 0) {
                view.addEdge(e);
            }
        }

        int type0Count = view.getEdgeCount(0);
        int nodeCount = view.getNodeCount();
        int totalNodesInStore = graphStore.getNodeCount();

        Assert.assertTrue(type0Count > 0, "View should have type 0 edges");

        // NOT operation flips both nodes and edges
        // Since we have all nodes, after NOT we have 0 nodes
        // All edges get removed because they have no valid endpoints
        view.not();

        Assert.assertEquals(view
                .getNodeCount(), totalNodesInStore - nodeCount, "View should have inverted node count after NOT");
        Assert.assertEquals(view
                .getEdgeCount(), 0, "View should have 0 edges after NOT (no nodes means no valid edges)");
    }

    // ========== Tests for Empty View Edge Cases ==========

    @Test
    public void testIntersectionWithEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView();
        GraphViewImpl view2 = store.createView(); // Empty view

        // Fill view1
        view1.fill();
        int initialNodeCount = view1.getNodeCount();
        int initialEdgeCount = view1.getEdgeCount();

        Assert.assertTrue(initialNodeCount > 0, "View1 should have nodes");
        Assert.assertTrue(initialEdgeCount > 0, "View1 should have edges");
        Assert.assertEquals(view2.getNodeCount(), 0, "View2 should be empty");
        Assert.assertEquals(view2.getEdgeCount(), 0, "View2 should be empty");

        // Intersection with empty view should result in empty view1
        view1.intersection(view2);

        Assert.assertEquals(view1.getNodeCount(), 0, "View1 should be empty after intersection with empty view");
        Assert.assertEquals(view1.getEdgeCount(), 0, "View1 should have no edges after intersection with empty view");

        // Verify all elements are absent
        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(view1.containsNode((NodeImpl) n), "Node " + n
                    .getId() + " should not be in view after intersection with empty view");
        }
        for (Edge e : graphStore.getEdges()) {
            Assert.assertFalse(view1.containsEdge((EdgeImpl) e), "Edge " + e
                    .getId() + " should not be in view after intersection with empty view");
        }
    }

    @Test
    public void testIntersectionOfEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView(); // Empty view
        GraphViewImpl view2 = store.createView();

        // Fill view2
        view2.fill();

        Assert.assertEquals(view1.getNodeCount(), 0, "View1 should be empty");
        Assert.assertTrue(view2.getNodeCount() > 0, "View2 should have nodes");

        // Intersection of empty view with filled view should stay empty
        view1.intersection(view2);

        Assert.assertEquals(view1.getNodeCount(), 0, "View1 should still be empty after intersection");
        Assert.assertEquals(view1.getEdgeCount(), 0, "View1 should still have no edges after intersection");

        // Verify all elements are absent
        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(view1.containsNode((NodeImpl) n), "Node " + n
                    .getId() + " should not be in empty view after intersection");
        }
    }

    @Test
    public void testUnionWithEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView();
        GraphViewImpl view2 = store.createView(); // Empty view

        // Fill view1
        view1.fill();
        int initialNodeCount = view1.getNodeCount();
        int initialEdgeCount = view1.getEdgeCount();

        Assert.assertTrue(initialNodeCount > 0, "View1 should have nodes");
        Assert.assertTrue(initialEdgeCount > 0, "View1 should have edges");
        Assert.assertEquals(view2.getNodeCount(), 0, "View2 should be empty");

        // Union with empty view should not change view1
        view1.union(view2);

        Assert.assertEquals(view1.getNodeCount(), initialNodeCount, "View1 node count should not change");
        Assert.assertEquals(view1.getEdgeCount(), initialEdgeCount, "View1 edge count should not change");

        // Verify all elements are still present
        for (Node n : graphStore.getNodes()) {
            Assert.assertTrue(view1.containsNode((NodeImpl) n), "Node " + n
                    .getId() + " should still be in view after union with empty view");
        }
        for (Edge e : graphStore.getEdges()) {
            Assert.assertTrue(view1.containsEdge((EdgeImpl) e), "Edge " + e
                    .getId() + " should still be in view after union with empty view");
        }
    }

    @Test
    public void testUnionOfEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView(); // Empty view
        GraphViewImpl view2 = store.createView();

        // Fill view2
        view2.fill();
        int view2NodeCount = view2.getNodeCount();
        int view2EdgeCount = view2.getEdgeCount();

        Assert.assertEquals(view1.getNodeCount(), 0, "View1 should be empty");
        Assert.assertTrue(view2NodeCount > 0, "View2 should have nodes");

        // Union of empty view with filled view should fill view1
        view1.union(view2);

        Assert.assertEquals(view1.getNodeCount(), view2NodeCount, "View1 should have same node count as view2");
        Assert.assertEquals(view1.getEdgeCount(), view2EdgeCount, "View1 should have same edge count as view2");

        // Verify all elements are present
        for (Node n : graphStore.getNodes()) {
            Assert.assertTrue(view1.containsNode((NodeImpl) n), "Node " + n.getId() + " should be in view after union");
        }
        for (Edge e : graphStore.getEdges()) {
            Assert.assertTrue(view1.containsEdge((EdgeImpl) e), "Edge " + e.getId() + " should be in view after union");
        }
    }

    @Test
    public void testNotOnEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(); // Empty view

        int totalNodes = graphStore.getNodeCount();
        int totalEdges = graphStore.getEdgeCount();

        Assert.assertEquals(view.getNodeCount(), 0, "View should be empty initially");
        Assert.assertEquals(view.getEdgeCount(), 0, "View should have no edges initially");

        // NOT on empty view should fill it completely
        view.not();

        Assert.assertEquals(view.getNodeCount(), totalNodes, "View should have all nodes after NOT");
        Assert.assertEquals(view.getEdgeCount(), totalEdges, "View should have all edges after NOT");

        // Verify all elements are present
        for (Node n : graphStore.getNodes()) {
            Assert.assertTrue(view.containsNode((NodeImpl) n), "View should contain all nodes after NOT");
        }
        for (Edge e : graphStore.getEdges()) {
            Assert.assertTrue(view.containsEdge((EdgeImpl) e), "View should contain all edges after NOT");
        }
    }

    @Test
    public void testIntersectionBothEmpty() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView(); // Empty
        GraphViewImpl view2 = store.createView(); // Empty

        // Both views empty
        Assert.assertEquals(view1.getNodeCount(), 0, "View1 should be empty");
        Assert.assertEquals(view2.getNodeCount(), 0, "View2 should be empty");

        // Intersection of two empty views should stay empty
        view1.intersection(view2);

        Assert.assertEquals(view1.getNodeCount(), 0, "View1 should still be empty");
        Assert.assertEquals(view1.getEdgeCount(), 0, "View1 should still have no edges");

        // Verify all elements are absent (trivial case but validates correctness)
        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(view1
                    .containsNode((NodeImpl) n), "No nodes should be in view after intersection of empty views");
        }
    }

    @Test
    public void testUnionBothEmpty() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view1 = store.createView(); // Empty
        GraphViewImpl view2 = store.createView(); // Empty

        // Both views empty
        Assert.assertEquals(view1.getNodeCount(), 0, "View1 should be empty");
        Assert.assertEquals(view2.getNodeCount(), 0, "View2 should be empty");

        // Union of two empty views should stay empty
        view1.union(view2);

        Assert.assertEquals(view1.getNodeCount(), 0, "View1 should still be empty");
        Assert.assertEquals(view1.getEdgeCount(), 0, "View1 should still have no edges");

        // Verify all elements are absent (trivial case but validates correctness)
        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(view1
                    .containsNode((NodeImpl) n), "No nodes should be in view after union of empty views");
        }
    }

    // ========== Tests for Retain Operations ==========

    @Test
    public void testRetainNodesBasic() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        view.fill();
        int initialNodeCount = view.getNodeCount();
        int initialEdgeCount = view.getEdgeCount();

        // Retain all nodes - should return false (no change)
        boolean changed = view.retainNodes(graphStore.getNodes().toCollection());
        Assert.assertFalse(changed, "Retaining all nodes should return false");
        Assert.assertEquals(view.getNodeCount(), initialNodeCount, "Node count should not change");
        Assert.assertEquals(view.getEdgeCount(), initialEdgeCount, "Edge count should not change");

        // Retain subset of nodes
        NodeImpl n1 = graphStore.getNode("0");
        NodeImpl n2 = graphStore.getNode("1");
        changed = view.retainNodes(Arrays.asList(n1, n2));

        Assert.assertTrue(changed, "Retaining subset should return true");
        Assert.assertEquals(view.getNodeCount(), 2, "Should have exactly 2 nodes");
        Assert.assertTrue(view.containsNode(n1), "Should contain node 0");
        Assert.assertTrue(view.containsNode(n2), "Should contain node 1");
    }

    @Test
    public void testRetainNodesEmpty() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        view.fill();

        // Retain empty collection - should clear everything
        boolean changed = view.retainNodes(Collections.emptyList());

        Assert.assertTrue(changed, "Retaining empty list should return true");
        Assert.assertEquals(view.getNodeCount(), 0, "Should have no nodes");
        Assert.assertEquals(view.getEdgeCount(), 0, "Should have no edges");
    }

    @Test
    public void testRetainEdgesBasic() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true); // Edge view only

        view.fill();
        int initialEdgeCount = view.getEdgeCount();

        // Retain all edges - should return false (no change)
        boolean changed = view.retainEdges(graphStore.getEdges().toCollection());
        Assert.assertFalse(changed, "Retaining all edges should return false");
        Assert.assertEquals(view.getEdgeCount(), initialEdgeCount, "Edge count should not change");

        // Retain subset of edges
        EdgeImpl e1 = graphStore.getEdge("0");
        EdgeImpl e2 = graphStore.getEdge("1");
        changed = view.retainEdges(Arrays.asList(e1, e2));

        Assert.assertTrue(changed, "Retaining subset should return true");
        Assert.assertEquals(view.getEdgeCount(), 2, "Should have exactly 2 edges");
        Assert.assertTrue(view.containsEdge(e1), "Should contain edge 0");
        Assert.assertTrue(view.containsEdge(e2), "Should contain edge 1");
    }

    @Test
    public void testRetainEdgesEmpty() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true); // Edge view only

        view.fill();

        // Retain empty collection - should clear all edges
        boolean changed = view.retainEdges(Collections.emptyList());

        Assert.assertTrue(changed, "Retaining empty list should return true");
        Assert.assertEquals(view.getEdgeCount(), 0, "Should have no edges");
    }

    @Test
    public void testRetainNodesWithMutualEdges() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStoreWithMutualEdge();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        view.fill();

        EdgeImpl e0 = graphStore.getEdge("0");
        NodeImpl n1 = e0.getSource();
        NodeImpl n2 = e0.getTarget();

        // Initial state: view has mutual edges
        Assert.assertEquals(view.mutualEdgesCount, 1, "View should have mutual count of 1");
        Assert.assertEquals(view.getEdgeCount(), 2, "View should have 2 edges");

        // Retain both nodes - mutual edges should remain
        boolean changed = view.retainNodes(Arrays.asList(n1, n2));

        Assert.assertFalse(changed, "Retaining all nodes should return false");
        Assert.assertEquals(view.mutualEdgesCount, 1, "Mutual edges should remain");
        Assert.assertEquals(view.getEdgeCount(), 2, "Should still have 2 edges");
    }

    @Test
    public void testRetainEdgesWithMultipleTypes() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true); // Edge view only

        view.fill();

        int type0Count = view.getEdgeCount(0);
        int type1Count = view.getEdgeCount(1);
        int type2Count = view.getEdgeCount(2);

        Assert.assertTrue(type0Count > 0, "Should have type 0 edges");
        Assert.assertTrue(type1Count > 0, "Should have type 1 edges");
        Assert.assertTrue(type2Count > 0, "Should have type 2 edges");

        // Collect only type 0 edges to retain
        List<Edge> type0Edges = new ArrayList<>();
        for (Edge e : graphStore.getEdges().toArray()) {
            if (e.getType() == 0) {
                type0Edges.add(e);
            }
        }

        // Retain only type 0 edges
        boolean changed = view.retainEdges(type0Edges);

        Assert.assertTrue(changed, "Should have removed edges");
        Assert.assertEquals(view.getEdgeCount(0), type0Count, "Should still have all type 0 edges");
        Assert.assertEquals(view.getEdgeCount(1), 0, "Should have no type 1 edges");
        Assert.assertEquals(view.getEdgeCount(2), 0, "Should have no type 2 edges");
        Assert.assertEquals(view.getEdgeCount(), type0Count, "Total should match type 0 count");
    }

    @Test
    public void testRetainNodesWithMultipleEdgeTypes() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        view.fill();

        int initialType0Count = view.getEdgeCount(0);
        int initialType1Count = view.getEdgeCount(1);

        // Retain subset of nodes
        List<Node> nodesToRetain = new ArrayList<>();
        int count = 0;
        for (Node n : graphStore.getNodes()) {
            nodesToRetain.add(n);
            count++;
            if (count >= 5) {
                break; // Keep first 5 nodes
            }
        }

        boolean changed = view.retainNodes(nodesToRetain);

        Assert.assertTrue(changed, "Should have removed nodes");
        Assert.assertEquals(view.getNodeCount(), 5, "Should have exactly 5 nodes");

        // Edge counts should have decreased but type tracking should still be correct
        int newType0Count = view.getEdgeCount(0);
        int newType1Count = view.getEdgeCount(1);

        Assert.assertTrue(newType0Count <= initialType0Count, "Type 0 count should not increase");
        Assert.assertTrue(newType1Count <= initialType1Count, "Type 1 count should not increase");
        Assert.assertEquals(view.getEdgeCount(), newType0Count + newType1Count + view
                .getEdgeCount(2), "Total edge count should match sum of types");
    }

    @Test
    public void testRetainNodesLargeScale() {
        // Test bulk operation performance with larger dataset
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        view.fill();
        int totalNodes = view.getNodeCount();

        // Retain half the nodes
        List<Node> nodesToRetain = new ArrayList<>();
        int count = 0;
        for (Node n : graphStore.getNodes()) {
            if (count % 2 == 0) {
                nodesToRetain.add(n);
            }
            count++;
        }

        boolean changed = view.retainNodes(nodesToRetain);

        Assert.assertTrue(changed, "Should have removed nodes");
        Assert.assertTrue(view.getNodeCount() <= totalNodes / 2 + 1, "Should have roughly half the nodes");
        Assert.assertTrue(view.getNodeCount() >= totalNodes / 2 - 1, "Should have roughly half the nodes");

        // Verify all retained nodes are in the view
        for (Node n : nodesToRetain) {
            Assert.assertTrue(view.containsNode((NodeImpl) n), "Retained node should be in view");
        }
    }

    @Test
    public void testRetainEdgesOnlyView() {
        // Test retain edges when nodeView=false, edgeView=true
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(false, true);

        view.fill();

        List<Edge> edgesToRetain = new ArrayList<>();
        int count = 0;
        for (Edge e : graphStore.getEdges().toArray()) {
            edgesToRetain.add(e);
            count++;
            if (count >= 10) {
                break;
            }
        }

        boolean changed = view.retainEdges(edgesToRetain);

        Assert.assertTrue(changed, "Should have removed edges");
        Assert.assertEquals(view.getEdgeCount(), 10, "Should have exactly 10 edges");

        for (Edge e : edgesToRetain) {
            Assert.assertTrue(view.containsEdge((EdgeImpl) e), "Retained edge should be in view");
        }
    }
}
