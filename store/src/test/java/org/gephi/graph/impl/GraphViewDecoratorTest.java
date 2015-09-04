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

import org.gephi.graph.impl.EdgeImpl;
import org.gephi.graph.impl.GraphViewImpl;
import org.gephi.graph.impl.GraphStore;
import org.gephi.graph.impl.NodeImpl;
import org.gephi.graph.impl.GraphViewStore;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Random;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Node;
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
            boolean mutualToIgnore = graphStore.edgeStore.isUndirectedToIgnore((EdgeImpl) e);
            if (!mutualToIgnore) {
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
            boolean mutualToIgnore = graphStore.edgeStore.isUndirectedToIgnore((EdgeImpl) e);
            if (!mutualToIgnore) {
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
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), 0);
        }
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
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), 0);
        }
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
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), 0);
        }
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
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), 0);
        }
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
            Assert.assertTrue(isIterablesEqual(graph.getNeighbors(n), copyGraphStore.undirectedDecorator.getNeighbors(m)));

            for (int i = 0; i < typeCount; i++) {
                Assert.assertTrue(isIterablesEqual(graph.getEdges(n, i), copyGraphStore.undirectedDecorator.getEdges(m, i)));
                Assert.assertTrue(isIterablesEqual(graph.getNeighbors(n, i), copyGraphStore.undirectedDecorator.getNeighbors(m, i)));
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
        for (Edge e : graph.getEdges()) {
            Assert.assertTrue(graph.isAdjacent(e.getSource(), e.getTarget()));
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
            Assert.assertTrue(graph.isAdjacent(e.getSource(), e.getTarget(), e.getType()));
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
        Edge edge = graphStore.factory.newEdge(n1, n1);
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

        Edge edge = graphStore.factory.newEdge(n1, n1);
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

    //UTILITY
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
}
