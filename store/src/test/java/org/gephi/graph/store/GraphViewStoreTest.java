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
package org.gephi.graph.store;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Random;
import org.gephi.attribute.time.Interval;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedSubgraph;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class GraphViewStoreTest {

    @Test
    public void testEmptyStore() {
        GraphStore graphStore = new GraphStore();
        GraphViewStore store = graphStore.viewStore;

        Assert.assertEquals(store.size(), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullGraphStore() {
        new GraphViewStore(null);
    }

    @Test
    public void testCreate() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphViewImpl view = store.createView();
        Assert.assertNotNull(view);
        Assert.assertTrue(store.contains(view));
        Assert.assertEquals(store.size(), 1);

        GraphViewImpl view2 = store.createView();
        Assert.assertTrue(store.contains(view2));
        Assert.assertEquals(store.size(), 2);
    }

    @Test
    public void testDestroy() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphViewImpl view = store.createView();
        store.destroyView(view);

        Assert.assertFalse(store.contains(view));
        Assert.assertEquals(store.size(), 0);
        Assert.assertEquals(view.storeId, GraphViewStore.NULL_VIEW);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDestroyTwice() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphViewImpl view = store.createView();
        store.destroyView(view);
        store.destroyView(view);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDestroyOtherStore() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        GraphStore graphStore2 = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store2 = new GraphViewStore(graphStore2);

        Assert.assertFalse(store2.contains(view));

        store2.createView();
        store2.destroyView(view);
    }

    @Test
    public void testGarbage() {
        GraphStore graphStore = new GraphStore();
        GraphViewStore store = graphStore.viewStore;

        Assert.assertEquals(store.garbageQueue.size(), 0);

        GraphViewImpl view = store.createView();

        Assert.assertEquals(store.length, 1);

        store.destroyView(view);

        Assert.assertEquals(store.length, 1);
        Assert.assertEquals(store.garbageQueue.size(), 1);

        view = store.createView();

        Assert.assertEquals(view.storeId, 0);
        Assert.assertEquals(store.length, 1);
        Assert.assertEquals(store.garbageQueue.size(), 0);
    }

    @Test
    public void testGetGraph() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph directedSubgraph = store.getDirectedGraph(view);
        Assert.assertNotNull(directedSubgraph);
        Assert.assertSame(view, directedSubgraph.getView());

        UndirectedSubgraph undirectedSubgraph = store.getUndirectedGraph(view);
        Assert.assertNotNull(undirectedSubgraph);
        Assert.assertSame(view, undirectedSubgraph.getView());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetDirectedGraphNull() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        store.getDirectedGraph(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetUndirectedGraphNull() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        store.getUndirectedGraph(null);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testGetViewAnonymousClass() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        store.getDirectedGraph(new GraphView() {
            @Override
            public GraphModel getGraphModel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isMainView() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isNodeView() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Interval getTimeInterval() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isEdgeView() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }

    @Test
    public void testDirectedEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);

        Assert.assertEquals(graph.getNodeCount(), 0);
        Assert.assertEquals(graph.getEdgeCount(), 0);
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), 0);
        }

        Assert.assertFalse(graph.getNodes().iterator().hasNext());
        Assert.assertFalse(graph.getEdges().iterator().hasNext());
        Assert.assertFalse(graph.getSelfLoops().iterator().hasNext());
    }

    @Test
    public void testUndirectedEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph graph = store.getUndirectedGraph(view);

        Assert.assertEquals(graph.getNodeCount(), 0);
        Assert.assertEquals(graph.getEdgeCount(), 0);
        for (int i = 0; i < graphStore.edgeTypeStore.length; i++) {
            Assert.assertEquals(graph.getEdgeCount(i), 0);
        }

        Assert.assertFalse(graph.getNodes().iterator().hasNext());
        Assert.assertFalse(graph.getEdges().iterator().hasNext());
        Assert.assertFalse(graph.getSelfLoops().iterator().hasNext());
    }

    @Test
    public void testDirectedContainsElementsEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);

        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(graph.contains(n));
        }

        for (Edge e : graphStore.getEdges()) {
            Assert.assertFalse(graph.contains(e));
        }
    }

    @Test
    public void testUndirectedContainsElementsEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph graph = store.getUndirectedGraph(view);

        for (Node n : graphStore.getNodes()) {
            Assert.assertFalse(graph.contains(n));
        }

        for (Edge e : graphStore.getEdges()) {
            Assert.assertFalse(graph.contains(e));
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testNodeExistCheckEmptyView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        DirectedSubgraph graph = store.getDirectedGraph(view);
        Node node = graphStore.getNodes().toArray()[0];
        graph.getEdges(node).iterator();
    }

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
    public void testViewEquals() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        NodeImpl n1 = graphStore.getNode("0");
        view.addNode(n1);

        Assert.assertTrue(view.equals(view));

        GraphViewImpl view2 = store.createView();

        NodeImpl n2 = graphStore.getNode("0");
        view2.addNode(n2);

        Assert.assertTrue(view.equals(view2));
    }

    @Test
    public void testViewHashCode() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        NodeImpl n1 = graphStore.getNode("0");
        view.addNode(n1);

        Assert.assertEquals(view.hashCode(), view.hashCode());

        GraphViewImpl view2 = store.createView();

        NodeImpl n2 = graphStore.getNode("0");
        view2.addNode(n2);

        Assert.assertEquals(view.hashCode(), view2.hashCode());
    }

    @Test
    public void testViewCopy() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        NodeImpl n1 = graphStore.getNode("0");
        view.addNode(n1);

        Assert.assertTrue(view.containsNode(n1));

        GraphViewImpl copyView = store.createView(view);
        Assert.assertTrue(copyView.equals(view));
        Assert.assertEquals(copyView.hashCode(), view.hashCode());
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

        Assert.assertTrue(view2.equals(view));
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
        EdgeImpl edge = (EdgeImpl)graphStore.factory.newEdge(n1, n2);
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

    @Test
    public void testSetVisibleView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphView view = store.createView();
        store.setVisibleView(view);

        GraphView visibleView = store.getVisibleView();
        Assert.assertNotNull(view);
        Assert.assertEquals(visibleView, view);
    }

    @Test
    public void testSetMainVisibleView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphView view = store.createView();
        store.setVisibleView(view);
        store.setVisibleView(null);

        GraphView visibleView = store.getVisibleView();
        Assert.assertNotNull(view);
        Assert.assertEquals(visibleView, graphStore.mainGraphView);
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
