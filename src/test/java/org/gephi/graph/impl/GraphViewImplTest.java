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

import java.util.Arrays;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
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

        Assert.assertTrue(view.containsNode(n1));
        Assert.assertTrue(view.containsNode(n2));
        Assert.assertTrue(view.containsEdge(e1));
        Assert.assertFalse(view.containsNode(n3));
        Assert.assertFalse(view.containsNode(n4));
        Assert.assertFalse(view.containsEdge(e2));

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

        EdgeImpl e1 = graphStore.getEdge("0");
        EdgeImpl e2 = graphStore.getEdge("5");

        view.removeEdge(e1);
        view2.removeEdge(e2);

        view.intersection(view2);

        Assert.assertFalse(view.containsEdge(e1));
        Assert.assertFalse(view.containsEdge(e2));
    }

    @Test
    public void testViewIntersectionNodeView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(true, false);
        GraphViewImpl view2 = store.createView();

        view.fill();
        view2.fill();

        EdgeImpl e1 = graphStore.getEdge("0");
        EdgeImpl e2 = graphStore.getEdge("5");
        NodeImpl s1 = e1.getSource();

        view2.removeNode(s1);
        view2.removeEdge(e2);

        view.intersection(view2);

        Assert.assertFalse(view.containsEdge(e1));
        Assert.assertFalse(view.containsNode(s1));
        Assert.assertTrue(view.containsEdge(e2));
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

        Assert.assertTrue(view.containsNode(n1));
        Assert.assertTrue(view.containsNode(n2));
        Assert.assertTrue(view.containsEdge(e1));
        Assert.assertTrue(view.containsNode(n3));
        Assert.assertTrue(view.containsNode(n4));
        Assert.assertTrue(view.containsEdge(e2));
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

        Assert.assertTrue(view.containsEdge(e1));
        Assert.assertTrue(view.containsEdge(e2));
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

        Assert.assertTrue(view.containsEdge(e1));
        Assert.assertTrue(view.containsEdge(e2));
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
}
