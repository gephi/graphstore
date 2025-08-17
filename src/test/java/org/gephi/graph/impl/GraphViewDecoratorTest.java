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
import java.util.Collections;
import java.util.Random;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Rect2D;
import org.gephi.graph.api.SpatialIndex;
import org.gephi.graph.api.UndirectedSubgraph;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphViewDecoratorTest {

    @Test
    public void testDirectedAdd() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);

        int count = 0;
        graph.writeLock();
        for (Node n : graphStore.getNodes()) {
            boolean a = graph.addNode(n);
            boolean b = graph.addNode(n);

            Assert.assertTrue(a);
            Assert.assertFalse(b);
            Assert.assertTrue(graph.contains(n));
            Assert.assertEquals(graph.getNodeCount(), ++count);
        }
        graph.writeUnlock();
        Assert.assertEquals(graph.getNodeCount(), graphStore.getNodeCount());

        count = 0;
        graph.writeLock();
        for (Edge e : graphStore.getEdges()) {
            boolean a = graph.addEdge(e);
            boolean b = graph.addEdge(e);

            Assert.assertTrue(a);
            Assert.assertFalse(b);
            Assert.assertTrue(graph.contains(e));
            Assert.assertEquals(graph.getEdgeCount(), ++count);
        }
        graph.writeUnlock();
        Assert.assertEquals(graph.getEdgeCount(), graphStore.getEdgeCount());
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), graphStore.getEdgeCount(i));
        }
    }

    @Test
    public void testUndirectedAdd() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph graph = store.getUndirectedGraph(view);

        int count = 0;
        graph.writeLock();
        for (Node n : graphStore.getNodes()) {
            boolean a = graph.addNode(n);
            boolean b = graph.addNode(n);

            Assert.assertTrue(a);
            Assert.assertFalse(b);
            Assert.assertTrue(graph.contains(n));
            Assert.assertEquals(graph.getNodeCount(), ++count);
        }
        graph.writeUnlock();
        Assert.assertEquals(graph.getNodeCount(), graphStore.undirectedDecorator.getNodeCount());

        count = 0;
        graph.writeLock();
        for (Edge e : graphStore.getEdges()) {
            boolean a = graph.addEdge(e);
            boolean b = graph.addEdge(e);

            Assert.assertTrue(a);
            Assert.assertFalse(b);
            Assert.assertTrue(graph.contains(e));
            EdgeImpl mutualEdge = graphStore.edgeStore.getMutualEdge(e);
            if (!(mutualEdge != null && !e.isSelfLoop() && graph.contains(mutualEdge))) {
                Assert.assertEquals(graph.getEdgeCount(), ++count);
            }
        }
        graph.writeUnlock();
        Assert.assertEquals(graph.getEdgeCount(), graphStore.undirectedDecorator.getEdgeCount());
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), graphStore.undirectedDecorator.getEdgeCount(i));
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testAddEdgeWithoutNodes() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        Edge edge = graphStore.getEdges().toArray()[0];
        store.getDirectedGraph(view).addEdge(edge);
    }

    @Test
    public void testDirectedAddAll() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());

        for (Node n : graphStore.getNodes()) {
            Assert.assertTrue(graph.contains(n));
        }
        Assert.assertEquals(graph.getNodeCount(), graphStore.getNodeCount());

        graph.addAllEdges(graphStore.getEdges().toCollection());

        for (Edge e : graphStore.getEdges()) {
            Assert.assertTrue(graph.contains(e));
        }
        Assert.assertEquals(graph.getEdgeCount(), graphStore.getEdgeCount());
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), graphStore.getEdgeCount(i));
        }
    }

    @Test
    public void testUndirectedAddAll() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph graph = store.getUndirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());

        for (Node n : graphStore.getNodes()) {
            Assert.assertTrue(graph.contains(n));
        }
        Assert.assertEquals(graph.getNodeCount(), graphStore.getNodeCount());

        graph.addAllEdges(graphStore.getEdges().toCollection());

        for (Edge e : graphStore.getEdges()) {
            Assert.assertTrue(graph.contains(e));
        }
        Assert.assertEquals(graph.getEdgeCount(), graphStore.undirectedDecorator.getEdgeCount());
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), graphStore.undirectedDecorator.getEdgeCount(i));
        }
    }

    @Test
    public void testDirectedRemove() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        graph.writeLock();
        int count = graph.getEdgeCount();
        for (Edge e : graphStore.getEdges()) {
            boolean a = graph.removeEdge(e);
            boolean b = graph.removeEdge(e);

            Assert.assertTrue(a);
            Assert.assertFalse(b);
            Assert.assertFalse(graph.contains(e));
            Assert.assertEquals(graph.getEdgeCount(), --count);
        }
        graph.writeUnlock();
        Assert.assertEquals(graph.getEdgeCount(), 0);
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), 0);
        }

        count = graph.getNodeCount();
        graph.writeLock();
        for (Node n : graphStore.getNodes()) {
            boolean a = graph.removeNode(n);
            boolean b = graph.removeNode(n);

            Assert.assertTrue(a);
            Assert.assertFalse(b);
            Assert.assertFalse(graph.contains(n));
            Assert.assertEquals(graph.getNodeCount(), --count);
        }
        graph.writeUnlock();
        Assert.assertEquals(graph.getNodeCount(), 0);
    }

    @Test
    public void testUndirectedRemove() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph graph = store.getUndirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        int count = graph.getEdgeCount();
        graph.writeLock();
        for (Edge e : graphStore.getEdges()) {
            boolean a = graph.removeEdge(e);
            boolean b = graph.removeEdge(e);

            Assert.assertTrue(a);
            Assert.assertFalse(b);
            Assert.assertFalse(graph.contains(e));
            EdgeImpl mutualEdge = graphStore.edgeStore.getMutualEdge(e);
            if (!(mutualEdge != null && !e.isSelfLoop() && graph.contains(mutualEdge))) {
                Assert.assertEquals(graph.getEdgeCount(), --count);
            }
        }
        graph.writeUnlock();
        Assert.assertEquals(graph.getEdgeCount(), 0);
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), 0);
        }

        count = graph.getNodeCount();
        graph.writeLock();
        for (Node n : graphStore.getNodes()) {
            boolean a = graph.removeNode(n);
            boolean b = graph.removeNode(n);

            Assert.assertTrue(a);
            Assert.assertFalse(b);
            Assert.assertFalse(graph.contains(n));
            Assert.assertEquals(graph.getNodeCount(), --count);
        }
        graph.writeUnlock();
        Assert.assertEquals(graph.getNodeCount(), 0);
    }

    @Test
    public void testDirectedRemoveAll() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        graph.removeAllEdges(graphStore.getEdges().toCollection());

        Assert.assertEquals(graph.getEdgeCount(), 0);
        for (Edge e : graphStore.getEdges()) {
            Assert.assertFalse(graph.contains(e));
        }

        graph.removeAllNodes(graphStore.getNodes().toCollection());

        Assert.assertEquals(graph.getNodeCount(), 0);
        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(graph.contains(n));
        }
    }

    @Test
    public void testUndirectedRemoveAll() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph graph = store.getUndirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        graph.removeAllEdges(graphStore.getEdges().toCollection());

        Assert.assertEquals(graph.getEdgeCount(), 0);
        for (Edge e : graphStore.getEdges()) {
            Assert.assertFalse(graph.contains(e));
        }

        graph.removeAllNodes(graphStore.getNodes().toCollection());

        Assert.assertEquals(graph.getNodeCount(), 0);
        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(graph.contains(n));
        }
    }

    @Test
    public void testDirectedRemoveNodesFirst() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        graph.writeLock();
        for (Node n : graphStore.getNodes()) {
            graph.removeNode(n);

            for (Edge e : graphStore.getEdges(n)) {
                Assert.assertFalse(graph.contains(e));
            }
        }
        graph.writeUnlock();

        Assert.assertEquals(graph.getEdgeCount(), 0);
    }

    @Test
    public void testUndirectedRemoveNodesFirst() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph graph = store.getUndirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        graph.writeLock();
        for (Node n : graphStore.getNodes()) {
            graph.removeNode(n);

            for (Edge e : graphStore.getEdges(n)) {
                Assert.assertFalse(graph.contains(e));
            }
        }
        graph.writeUnlock();

        Assert.assertEquals(graph.getEdgeCount(), 0);
    }

    @Test
    public void testDirectedRetainNodes() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStoreWithoutSelfLoop();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        view.fill();

        Assert.assertFalse(graph.retainNodes(graphStore.getNodes().toCollection()));
        Assert.assertEquals(graph.getNodeCount(), graphStore.getNodeCount());

        Assert.assertTrue(graph.retainNodes(Collections.EMPTY_LIST));
        Assert.assertEquals(graph.getNodeCount(), 0);

        view.fill();
        Edge edge = graphStore.getEdges().toArray()[0];
        Assert.assertTrue(graph.retainNodes(Arrays.asList(edge.getSource(), edge.getTarget())));
        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertTrue(graph.contains(edge.getSource()));
        Assert.assertTrue(graph.contains(edge.getTarget()));
        Assert.assertTrue(graph.contains(edge));
    }

    @Test
    public void testDirectedRetainEdges() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStoreWithoutSelfLoop();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        view.fill();

        Assert.assertFalse(graph.retainEdges(graphStore.getEdges().toCollection()));
        Assert.assertEquals(graph.getEdgeCount(), graphStore.getEdgeCount());

        Assert.assertTrue(graph.retainEdges(Collections.EMPTY_LIST));
        Assert.assertEquals(graph.getEdgeCount(), 0);

        view.fill();
        Edge edge = graphStore.getEdges().toArray()[0];
        Assert.assertTrue(graph.retainEdges(Collections.singletonList(edge)));
        Assert.assertEquals(graph.getNodeCount(), graphStore.getNodeCount());
        Assert.assertEquals(graph.getEdgeCount(), 1);
        Assert.assertTrue(graph.contains(edge));
    }

    @Test
    public void testDirectedClearEdges() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        graph.clearEdges();
        Assert.assertEquals(graph.getEdgeCount(), 0);
        for (Edge e : graphStore.getEdges()) {
            Assert.assertFalse(graph.contains(e));
        }
        Assert.assertEquals(graph.getEdgeCount(0), 0);
    }

    @Test
    public void testUndirectedClearEdges() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph graph = store.getUndirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        graph.clearEdges();
        Assert.assertEquals(graph.getEdgeCount(), 0);
        for (Edge e : graphStore.getEdges()) {
            Assert.assertFalse(graph.contains(e));
        }
        Assert.assertEquals(graph.getEdgeCount(0), 0);
    }

    @Test
    public void testDirectedClear() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        graph.clear();
        Assert.assertEquals(graph.getEdgeCount(), 0);
        Assert.assertEquals(graph.getNodeCount(), 0);
        for (Edge e : graphStore.getEdges()) {
            Assert.assertFalse(graph.contains(e));
        }
        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(graph.contains(n));
        }

        Assert.assertEquals(graph.getEdgeCount(0), 0);
    }

    @Test
    public void testUndirectedClear() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph graph = store.getUndirectedGraph(view);
        graph.addAllNodes(graphStore.getNodes().toCollection());
        graph.addAllEdges(graphStore.getEdges().toCollection());

        graph.clear();
        Assert.assertEquals(graph.getEdgeCount(), 0);
        Assert.assertEquals(graph.getNodeCount(), 0);
        for (Edge e : graphStore.getEdges()) {
            Assert.assertFalse(graph.contains(e));
        }
        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(graph.contains(n));
        }
        Assert.assertEquals(graph.getEdgeCount(0), 0);
    }

    @Test
    public void testDirectedIterators() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);
        int typeCount = graphStore.edgeTypeStore.length;

        DirectedSubgraph graph = store.getDirectedGraph(view);
        GraphStore copyGraphStore = convertToStore(view);

        Assert.assertTrue(isIterablesEqual(graph.getNodes(), copyGraphStore.getNodes()));
        Assert.assertTrue(isIterablesEqual(graph.getEdges(), copyGraphStore.getEdges()));
        Assert.assertTrue(isIterablesEqual(graph.getSelfLoops(), copyGraphStore.getSelfLoops()));

        for (Node n : graph.getNodes()) {
            Node m = copyGraphStore.getNode(n.getId());
            Assert.assertTrue(isIterablesEqual(graph.getEdges(n), copyGraphStore.getEdges(m)));
            Assert.assertTrue(isIterablesEqual(graph.getInEdges(n), copyGraphStore.getInEdges(m)));
            Assert.assertTrue(isIterablesEqual(graph.getOutEdges(n), copyGraphStore.getOutEdges(m)));
            Assert.assertTrue(isIterablesEqual(graph.getNeighbors(n), copyGraphStore.getNeighbors(m)));
            Assert.assertTrue(isIterablesEqual(graph.getSuccessors(n), copyGraphStore.getSuccessors(m)));
            Assert.assertTrue(isIterablesEqual(graph.getPredecessors(n), copyGraphStore.getPredecessors(m)));

            for (int i = 0; i < typeCount; i++) {
                Assert.assertTrue(isIterablesEqual(graph.getEdges(n, i), copyGraphStore.getEdges(m, i)));
                Assert.assertTrue(isIterablesEqual(graph.getInEdges(n, i), copyGraphStore.getInEdges(m, i)));
                Assert.assertTrue(isIterablesEqual(graph.getOutEdges(n, i), copyGraphStore.getOutEdges(m, i)));
                Assert.assertTrue(isIterablesEqual(graph.getNeighbors(n, i), copyGraphStore.getNeighbors(m, i)));
                Assert.assertTrue(isIterablesEqual(graph.getSuccessors(n, i), copyGraphStore.getSuccessors(m, i)));
                Assert.assertTrue(isIterablesEqual(graph.getPredecessors(n, i), copyGraphStore.getPredecessors(m, i)));
            }
        }
    }

    @Test
    public void testUndirectedIterators() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);
        int typeCount = graphStore.edgeTypeStore.length;

        UndirectedSubgraph graph = store.getUndirectedGraph(view);
        GraphStore copyGraphStore = convertToStore(view);

        Assert.assertTrue(isIterablesEqual(graph.getNodes(), copyGraphStore.undirectedDecorator.getNodes()));
        Assert.assertTrue(isIterablesEqual(graph.getEdges(), copyGraphStore.undirectedDecorator.getEdges()));
        Assert.assertTrue(isIterablesEqual(graph.getSelfLoops(), copyGraphStore.undirectedDecorator.getSelfLoops()));

        for (Node n : graph.getNodes()) {
            Node m = copyGraphStore.getNode(n.getId());
            Assert.assertTrue(isIterablesEqual(graph.getEdges(n), copyGraphStore.undirectedDecorator.getEdges(m)));
            Assert.assertTrue(isIterablesEqual(graph.getNeighbors(n), copyGraphStore.undirectedDecorator
                    .getNeighbors(m)));

            for (int i = 0; i < typeCount; i++) {
                Assert.assertTrue(isIterablesEqual(graph.getEdges(n, i), copyGraphStore.undirectedDecorator
                        .getEdges(m, i)));
                Assert.assertTrue(isIterablesEqual(graph.getNeighbors(n, i), copyGraphStore.undirectedDecorator
                        .getNeighbors(m, i)));
            }
        }
    }

    @Test
    public void testDirectedDegree() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);
        int typeCount = graphStore.edgeTypeStore.length;

        DirectedSubgraph graph = store.getDirectedGraph(view);
        GraphStore copyGraphStore = convertToStore(view);
        for (Node n : graph.getNodes()) {
            Node m = copyGraphStore.getNode(n.getId());
            Assert.assertEquals(graph.getDegree(n), copyGraphStore.getDegree(m));
            Assert.assertEquals(graph.getInDegree(n), copyGraphStore.getInDegree(m));
            Assert.assertEquals(graph.getOutDegree(n), copyGraphStore.getOutDegree(m));
        }
    }

    @Test
    public void testUndirectedDegree() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);

        UndirectedSubgraph graph = store.getUndirectedGraph(view);
        GraphStore copyGraphStore = convertToStore(view);
        for (Node n : graph.getNodes()) {
            Node m = copyGraphStore.getNode(n.getId());
            Assert.assertEquals(graph.getDegree(n), copyGraphStore.undirectedDecorator.getDegree(m));
        }
    }

    @Test
    public void testGetEdge() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);

        DirectedSubgraph graph = store.getDirectedGraph(view);
        for (Edge e : graph.getEdges()) {
            Assert.assertSame(graph.getEdge(e.getSource(), e.getTarget()), e);
        }
        graph.clearEdges();
        Node[] nodes = graph.getNodes().toArray();
        Assert.assertNull(graph.getEdge(nodes[0], nodes[1]));
    }

    @Test
    public void testGetEdgeWithType() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);

        DirectedSubgraph graph = store.getDirectedGraph(view);
        for (Edge e : graph.getEdges()) {
            Assert.assertSame(graph.getEdge(e.getSource(), e.getTarget(), e.getType()), e);
        }
        graph.clearEdges();
        Node[] nodes = graph.getNodes().toArray();
        Assert.assertNull(graph.getEdge(nodes[0], nodes[1], 0));
    }

    @Test
    public void testGetMutualEdge() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);

        DirectedSubgraph graph = store.getDirectedGraph(view);
        for (Edge e : graph.getEdges()) {
            if (graph.getEdge(e.getTarget(), e.getSource()) != null) {
                Edge m = graph.getMutualEdge(e);
                Assert.assertSame(m, graph.getEdge(e.getTarget(), e.getSource()));
            } else {
                Assert.assertNull(graph.getMutualEdge(e));
            }
        }
    }

    @Test
    public void testClearEdgesForNode() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);

        DirectedSubgraph graph = store.getDirectedGraph(view);
        for (Node n : graph.getNodes().toArray()) {
            graph.clearEdges(n);
            Assert.assertEquals(graph.getDegree(n), 0);
        }
        Assert.assertEquals(graph.getEdgeCount(), 0);
    }

    @Test
    public void testClearEdgesForNodeType() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);

        DirectedSubgraph graph = store.getDirectedGraph(view);
        for (Node n : graph.getNodes().toArray()) {
            graph.clearEdges(n, 0);
            Assert.assertEquals(graph.getDegree(n), 0);
        }
        Assert.assertEquals(graph.getEdgeCount(), 0);
    }

    @Test
    public void testIsAdjacent() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);

        DirectedSubgraph graph = store.getDirectedGraph(view);
        for (Edge e : graphStore.getEdges()) {
            if (graph.contains(e)) {
                Assert.assertTrue(graph.isAdjacent(e.getSource(), e.getTarget()));
            } else if (graph.contains(e.getSource()) && graph.contains(e.getTarget())) {
                Assert.assertFalse(graph.isAdjacent(e.getSource(), e.getTarget()));
            }
        }
        graph.clearEdges();
        Node[] nodes = graph.getNodes().toArray();
        Assert.assertFalse(graph.isAdjacent(nodes[0], nodes[1]));
    }

    @Test
    public void testIsAdjacentWithType() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        addSomeElements(graphStore, view);

        DirectedSubgraph graph = store.getDirectedGraph(view);
        for (Edge e : graph.getEdges()) {
            if (graph.contains(e)) {
                Assert.assertTrue(graph.isAdjacent(e.getSource(), e.getTarget(), e.getType()));
            } else if (graph.contains(e.getSource()) && graph.contains(e.getTarget())) {
                Assert.assertFalse(graph.isAdjacent(e.getSource(), e.getTarget(), e.getType()));
            }
        }
        graph.clearEdges();
        Node[] nodes = graph.getNodes().toArray();
        Assert.assertFalse(graph.isAdjacent(nodes[0], nodes[1], 0));
    }

    @Test
    public void testGetNodeById() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        NodeImpl n1 = graphStore.getNode("1");

        DirectedSubgraph graph = store.getDirectedGraph(view);
        graph.addNode(n1);

        Assert.assertSame(graph.getNode("1"), n1);
        Assert.assertNull(graph.getNode("2"));
        Assert.assertNull(graph.getNode("99"));
        Assert.assertTrue(graph.hasNode("1"));
        Assert.assertFalse(graph.hasNode("2"));
        Assert.assertFalse(graph.hasNode("99"));
    }

    @Test
    public void testGetEdgeById() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        view.fill();

        Assert.assertSame(graph.getEdge("0"), graphStore.getEdges().toArray()[0]);
        Assert.assertNull(graph.getEdge("99"));
        Assert.assertTrue(graph.hasEdge("0"));
        Assert.assertFalse(graph.hasEdge("99"));
    }

    @Test
    public void testGetOpposite() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        view.fill();

        Assert.assertSame(graph.getOpposite(graph.getNode("1"), graph.getEdge("0")), graph.getNode("2"));
    }

    @Test
    public void testIsSelfLoop() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        Edge edge = graphStore.factory.newEdge("edge", n1, n1, EdgeTypeStore.NULL_LABEL, 1.0, true);
        graphStore.addEdge(edge);

        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        view.fill();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        Assert.assertTrue(graph.isSelfLoop(edge));
        Assert.assertFalse(graph.isSelfLoop(graph.getEdge("0")));
    }

    @Test
    public void testIsDirected() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        DirectedSubgraph graph = store.getDirectedGraph(view);
        graph.fill();
        Assert.assertTrue(graph.isDirected(graph.getEdge("0")));
    }

    @Test
    public void testIsIncident() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        DirectedSubgraph graph = store.getDirectedGraph(view);
        view.fill();

        Node n1 = graph.getNode("1");
        Assert.assertTrue(graph.isIncident(n1, graph.getEdge("0")));

        Edge edge = graphStore.factory.newEdge("edge", n1, n1, EdgeTypeStore.NULL_LABEL, 1.0, true);
        graphStore.addEdge(edge);
        Assert.assertTrue(graph.isIncident(edge, graph.getEdge("0")));
    }

    @Test
    public void testAttributes() {
        GraphStore graphStore = new GraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        DirectedSubgraph graph = store.getDirectedGraph(view);

        Assert.assertNull(graph.getAttribute("foo"));
        graph.setAttribute("foo", "bar");
        Assert.assertEquals(graph.getAttribute("foo"), "bar");
        Assert.assertTrue(graph.getAttributeKeys().contains("foo"));
        graph.setAttribute("foo", "foo");
        Assert.assertEquals(graph.getAttribute("foo"), "foo");
        graph.removeAttribute("foo");
        Assert.assertFalse(graph.getAttributeKeys().contains("foo"));
        Assert.assertNull(graph.getAttribute("foo"));
    }

    @Test
    public void testAttributesWithTimestamps() {
        GraphStore graphStore = new GraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        DirectedSubgraph graph = store.getDirectedGraph(view);

        Assert.assertNull(graph.getAttribute("foo", 1.0));
        graph.setAttribute("foo", "bar", 1.0);
        Assert.assertEquals(graph.getAttribute("foo", 1.0), "bar");
        Assert.assertTrue(graph.getAttributeKeys().contains("foo"));
        graph.setAttribute("foo", "foo", 2.0);
        Assert.assertEquals(graph.getAttribute("foo", 2.0), "foo");
        graph.removeAttribute("foo", 1.0);
        Assert.assertNull(graph.getAttribute("foo", 1.0));
        graph.removeAttribute("foo", 2.0);
        Assert.assertFalse(graph.getAttributeKeys().contains("foo"));
    }

    @Test
    public void testAttributesWithIntervals() {
        GraphStore graphStore = new GraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();
        DirectedSubgraph graph = store.getDirectedGraph(view);

        Assert.assertNull(graph.getAttribute("foo", new Interval(1.0, 2.0)));
        graph.setAttribute("foo", "bar", new Interval(1.0, 2.0));
        Assert.assertEquals(graph.getAttribute("foo", new Interval(1.0, 2.0)), "bar");
        Assert.assertTrue(graph.getAttributeKeys().contains("foo"));
        graph.setAttribute("foo", "foo", new Interval(2.0, 4.0));
        Assert.assertEquals(graph.getAttribute("foo", new Interval(2.0, 4.0)), "foo");
        graph.removeAttribute("foo", new Interval(1.0, 2.0));
        Assert.assertNull(graph.getAttribute("foo", new Interval(1.0, 2.0)));
        graph.removeAttribute("foo", new Interval(2.0, 4.0));
        Assert.assertFalse(graph.getAttributeKeys().contains("foo"));
    }

    @Test
    public void testUnion() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphViewImpl view1 = store.createView();
        GraphViewImpl view2 = store.createView();
        DirectedSubgraph graph1 = store.getDirectedGraph(view1);
        DirectedSubgraph graph2 = store.getDirectedGraph(view2);

        Node n1 = graphStore.getNode("1");
        Node n2 = graphStore.getNode("2");

        graph1.addNode(n1);
        graph2.addNode(n2);

        graph1.union(graph2);
        Assert.assertTrue(graph1.contains(n2));
    }

    @Test
    public void testIntersection() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphViewImpl view1 = store.createView();
        GraphViewImpl view2 = store.createView();
        DirectedSubgraph graph1 = store.getDirectedGraph(view1);
        DirectedSubgraph graph2 = store.getDirectedGraph(view2);

        Node n1 = graphStore.getNode("1");
        Node n2 = graphStore.getNode("2");

        graph1.addNode(n1);
        graph1.addNode(n2);
        graph2.addNode(n2);

        graph1.intersection(graph2);
        Assert.assertFalse(graph1.contains(n1));
        Assert.assertTrue(graph1.contains(n2));
    }

    // UTILITY
    private boolean isIterablesEqual(ElementIterable n1, ElementIterable n2) {
        ObjectSet s1 = new ObjectOpenHashSet();
        for (Object n : n1) {
            s1.add(((Element) n).getId());
        }
        ObjectSet s2 = new ObjectOpenHashSet();
        for (Object n : n2) {
            s2.add(((Element) n).getId());
        }
        return s1.equals(s2);
    }

    private GraphStore convertToStore(GraphViewImpl view) {
        GraphStore store = new GraphStore();
        DirectedSubgraph graph = view.getDirectedGraph();
        for (Node n : graph.getNodes()) {
            NodeImpl m = new NodeImpl(n.getId());
            store.addNode(m);
        }
        for (Edge e : graph.getEdges()) {
            NodeImpl source = store.getNode(e.getSource().getId());
            NodeImpl target = store.getNode(e.getTarget().getId());
            EdgeImpl f = new EdgeImpl(e.getId(), source, target, e.getType(), e.getWeight(), e.isDirected());
            store.addEdge(f);
        }
        return store;
    }

    @Test
    public void testGetBoundariesEmptyView() {
        GraphStore graphStore = GraphGenerator.generateEmptyGraphStore(getSpatialConfig());
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);

        Assert.assertEquals(new Rect2D(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY), graph.getSpatialIndex().getBoundaries());
    }

    @Test
    public void testGetBoundariesSingleNodeInView() {
        GraphStore graphStore = GraphGenerator.generateEmptyGraphStore(getSpatialConfig());
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        // Add a node to the graph store
        NodeImpl node1 = (NodeImpl) graphStore.factory.newNode("1");
        node1.setPosition(100, 200);
        node1.setSize(10);
        graphStore.addNode(node1);

        NodeImpl node2 = (NodeImpl) graphStore.factory.newNode("2");
        node2.setPosition(500, 600);
        node2.setSize(20);
        graphStore.addNode(node2);

        // Add only node1 to the view
        view.addNode(node1);

        DirectedSubgraph graph = store.getDirectedGraph(view);

        // Should return boundaries only for node1
        Rect2D boundaries = graph.getSpatialIndex().getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, 90f); // 100 - 10
        Assert.assertEquals(boundaries.minY, 190f); // 200 - 10
        Assert.assertEquals(boundaries.maxX, 110f); // 100 + 10
        Assert.assertEquals(boundaries.maxY, 210f); // 200 + 10
    }

    @Test
    public void testGetBoundariesMultipleNodesInView() {
        GraphStore graphStore = GraphGenerator.generateEmptyGraphStore(getSpatialConfig());
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        // Add nodes to the graph store
        NodeImpl node1 = (NodeImpl) graphStore.factory.newNode("1");
        node1.setPosition(0, 0);
        node1.setSize(5);
        graphStore.addNode(node1);

        NodeImpl node2 = (NodeImpl) graphStore.factory.newNode("2");
        node2.setPosition(100, 200);
        node2.setSize(10);
        graphStore.addNode(node2);

        NodeImpl node3 = (NodeImpl) graphStore.factory.newNode("3");
        node3.setPosition(500, 600); // This node won't be in the view
        node3.setSize(20);
        graphStore.addNode(node3);

        // Add only node1 and node2 to the view
        view.addNode(node1);
        view.addNode(node2);

        DirectedSubgraph graph = store.getDirectedGraph(view);

        // Should return boundaries only for node1 and node2
        Rect2D boundaries = graph.getSpatialIndex().getBoundaries();
        Assert.assertNotNull(boundaries);
        Assert.assertEquals(boundaries.minX, -5f); // node1: 0 - 5
        Assert.assertEquals(boundaries.minY, -5f); // node1: 0 - 5
        Assert.assertEquals(boundaries.maxX, 110f); // node2: 100 + 10
        Assert.assertEquals(boundaries.maxY, 210f); // node2: 200 + 10
    }

    @Test
    public void testGetBoundariesViewSubsetVsFullGraph() {
        GraphStore graphStore = GraphGenerator.generateEmptyGraphStore(getSpatialConfig());
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        // Add nodes to the graph store
        NodeImpl node1 = (NodeImpl) graphStore.factory.newNode("1");
        node1.setPosition(0, 0);
        node1.setSize(5);
        graphStore.addNode(node1);

        NodeImpl node2 = (NodeImpl) graphStore.factory.newNode("2");
        node2.setPosition(100, 200);
        node2.setSize(10);
        graphStore.addNode(node2);

        NodeImpl node3 = (NodeImpl) graphStore.factory.newNode("3");
        node3.setPosition(-50, -100);
        node3.setSize(15);
        graphStore.addNode(node3);

        // Add only first two nodes to the view
        view.addNode(node1);
        view.addNode(node2);

        DirectedSubgraph viewGraph = store.getDirectedGraph(view);
        DirectedSubgraph fullGraph = graphStore;

        // Get boundaries for both
        Rect2D viewBoundaries = viewGraph.getSpatialIndex().getBoundaries();
        Rect2D fullBoundaries = graphStore.spatialIndex.getBoundaries();

        // View boundaries should only include node1 and node2
        Assert.assertNotNull(viewBoundaries);
        Assert.assertEquals(viewBoundaries.minX, -5f); // node1: 0 - 5
        Assert.assertEquals(viewBoundaries.minY, -5f); // node1: 0 - 5
        Assert.assertEquals(viewBoundaries.maxX, 110f); // node2: 100 + 10
        Assert.assertEquals(viewBoundaries.maxY, 210f); // node2: 200 + 10

        // Full graph boundaries should include all nodes
        Assert.assertNotNull(fullBoundaries);
        Assert.assertEquals(fullBoundaries.minX, -65f); // node3: -50 - 15
        Assert.assertEquals(fullBoundaries.minY, -115f); // node3: -100 - 15
        Assert.assertEquals(fullBoundaries.maxX, 110f); // node2: 100 + 10
        Assert.assertEquals(fullBoundaries.maxY, 210f); // node2: 200 + 10

        // They should be different
        Assert.assertFalse(viewBoundaries.minX == fullBoundaries.minX);
        Assert.assertFalse(viewBoundaries.minY == fullBoundaries.minY);
    }

    @Test
    public void testGetBoundariesAfterViewChanges() {
        GraphStore graphStore = GraphGenerator.generateEmptyGraphStore(getSpatialConfig());
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        // Add nodes to the graph store
        NodeImpl node1 = (NodeImpl) graphStore.factory.newNode("1");
        node1.setPosition(0, 0);
        node1.setSize(5);
        graphStore.addNode(node1);

        NodeImpl node2 = (NodeImpl) graphStore.factory.newNode("2");
        node2.setPosition(100, 200);
        node2.setSize(10);
        graphStore.addNode(node2);

        DirectedSubgraph graph = store.getDirectedGraph(view);

        // Initially empty view
        Rect2D boundaries = graph.getSpatialIndex().getBoundaries();
        Rect2D expected = new Rect2D(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY);
        Assert.assertEquals(expected, boundaries);

        // Add first node to view
        view.addNode(node1);
        Rect2D boundaries1 = graph.getSpatialIndex().getBoundaries();
        Assert.assertNotNull(boundaries1);
        Assert.assertEquals(boundaries1.minX, -5f);
        Assert.assertEquals(boundaries1.maxX, 5f);

        // Add second node to view
        view.addNode(node2);
        Rect2D boundaries2 = graph.getSpatialIndex().getBoundaries();
        Assert.assertNotNull(boundaries2);
        Assert.assertEquals(boundaries2.minX, -5f);
        Assert.assertEquals(boundaries2.maxX, 110f);

        // Remove first node from view
        view.removeNode(node1);
        Rect2D boundaries3 = graph.getSpatialIndex().getBoundaries();
        Assert.assertNotNull(boundaries3);
        Assert.assertEquals(boundaries3.minX, 90f); // Only node2 remains
        Assert.assertEquals(boundaries3.maxX, 110f);

        // Remove last node from view
        view.removeNode(node2);
        Assert.assertEquals(expected, graph.getSpatialIndex().getBoundaries());
    }

    private void addSomeElements(GraphStore store, GraphViewImpl view) {
        double perc = 0.8;
        Random rand = new Random(98324);
        for (Node n : store.getNodes()) {
            if (rand.nextDouble() <= perc) {
                view.addNode(n);
            }
        }
        for (Edge e : store.getEdges()) {
            if (view.containsNode((NodeImpl) e.getSource()) && view.containsNode((NodeImpl) e.getTarget())) {
                if (rand.nextDouble() <= perc) {
                    view.addEdge(e);
                }
            }
        }
    }

    // Configuration with spatial index enabled
    private Configuration getSpatialConfig() {
        return Configuration.builder().enableSpatialIndex(true).build();
    }
}
