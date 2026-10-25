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

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.UndirectedSubgraph;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class GraphViewStoreTest {

    // Node and edge, node-only and edge-only views
    private static final boolean[][] VIEW_KINDS = { { true, true }, { true, false }, { false, true } };

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
        Assert.assertFalse(view.isDestroyed());
        store.destroyView(view);

        Assert.assertFalse(store.contains(view));
        Assert.assertEquals(store.size(), 0);
        Assert.assertEquals(view.storeId, GraphViewStore.NULL_VIEW);
        Assert.assertTrue(view.isDestroyed());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDestroyMainView() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        store.destroyView(graphStore.mainGraphView);
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
    public void testDestroyVisible() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphViewImpl view = store.createView();
        store.setVisibleView(view);
        store.destroyView(view);

        Assert.assertFalse(store.contains(view));
        Assert.assertEquals(store.size(), 0);
        Assert.assertSame(store.getVisibleView(), graphStore.mainGraphView);
    }

    @Test
    public void testDestroyedViewKeepsContent() {
        for (boolean[] kind : VIEW_KINDS) {
            GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
            GraphViewImpl view = createPartialView(graphStore, kind[0], kind[1]);
            GraphViewImpl control = createPartialView(graphStore, kind[0], kind[1]);
            Subgraph graph = graphStore.viewStore.getDirectedGraph(view);
            Set<Node> nodes = toSet(graph.getNodes().toArray());
            Set<Edge> edges = toSet(graph.getEdges().toArray());
            Assert.assertTrue(edges.size() > 0 && edges.size() < graphStore.getEdgeCount());

            // Replaced by another view without changing the store, as when a filter is swapped
            graphStore.viewStore.setVisibleView(view);
            graphStore.viewStore.setVisibleView(graphStore.viewStore.createView(graphStore.mainGraphView));
            graphStore.viewStore.destroyView(view);

            Assert.assertEquals(toSet(graph.getNodes().toArray()), nodes);
            Assert.assertEquals(toSet(graph.getEdges().toArray()), edges);
            assertSameContent(graphStore, view, control);
        }
    }

    @Test
    public void testDestroyedViewFollowsStoreRemovals() {
        for (boolean[] kind : VIEW_KINDS) {
            GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
            GraphViewImpl view = createPartialView(graphStore, kind[0], kind[1]);
            GraphViewImpl control = createPartialView(graphStore, kind[0], kind[1]);
            Subgraph graph = graphStore.viewStore.getDirectedGraph(view);
            Set<Node> nodes = toSet(graph.getNodes().toArray());
            Set<Edge> edges = toSet(graph.getEdges().toArray());
            graphStore.viewStore.destroyView(view);

            List<Edge> removedEdges = Arrays.asList(graphStore.getEdges().toArray()).subList(0, 20);
            graphStore.removeAllEdges(removedEdges);
            edges.removeAll(removedEdges);
            List<Node> removedNodes = Arrays.asList(graphStore.getNodes().toArray()).subList(0, 5);
            for (Node node : removedNodes) {
                edges.removeAll(graphStore.getEdges(node).toCollection());
            }
            graphStore.removeAllNodes(removedNodes);
            nodes.removeAll(removedNodes);

            Assert.assertEquals(toSet(graph.getNodes().toArray()), nodes);
            Assert.assertEquals(toSet(graph.getEdges().toArray()), edges);
            Assert.assertEquals(graph.getNodeCount(), nodes.size());
            Assert.assertEquals(graph.getEdgeCount(), edges.size());
            assertSameContent(graphStore, view, control);
        }
    }

    @Test
    public void testDestroyedViewIgnoresReusedStoreIds() {
        for (boolean[] kind : VIEW_KINDS) {
            GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
            GraphViewImpl view = createPartialView(graphStore, kind[0], kind[1]);
            GraphViewImpl control = createPartialView(graphStore, kind[0], kind[1]);
            Subgraph graph = graphStore.viewStore.getDirectedGraph(view);
            graphStore.viewStore.destroyView(view);

            List<Node> removedNodes = Arrays.asList(graphStore.getNodes().toArray()).subList(0, 5);
            Set<Integer> removedNodeIds = new HashSet<>();
            removedNodes.forEach(n -> removedNodeIds.add(n.getStoreId()));
            graphStore.removeAllEdges(Arrays.asList(graphStore.getEdges().toArray()).subList(0, 20));
            graphStore.removeAllNodes(removedNodes);

            List<Node> addedNodes = new ArrayList<>();
            for (int i = 0; i < removedNodes.size(); i++) {
                addedNodes.add(graphStore.factory.newNode("added" + i));
            }
            graphStore.addAllNodes(addedNodes);
            Set<Integer> addedNodeIds = new HashSet<>();
            addedNodes.forEach(n -> addedNodeIds.add(n.getStoreId()));
            Assert.assertEquals(addedNodeIds, removedNodeIds);

            // Between all the nodes with a new type, so the added edges reuse the free store ids and then some
            Node[] allNodes = graphStore.getNodes().toArray();
            List<Edge> addedEdges = new ArrayList<>();
            for (int i = 0; i < allNodes.length; i++) {
                for (int j = i + 1; j < allNodes.length; j++) {
                    addedEdges.add(graphStore.factory
                            .newEdge("added" + i + "_" + j, allNodes[i], allNodes[j], 3, 1.0, true));
                }
            }
            graphStore.addAllEdges(addedEdges);

            Subgraph controlGraph = graphStore.viewStore.getDirectedGraph(control);
            for (Node node : addedNodes) {
                Assert.assertEquals(graph.contains(node), !kind[0]);
            }
            int addedInView = 0;
            for (Edge edge : addedEdges) {
                Assert.assertEquals(graph.contains(edge), controlGraph.contains(edge));
                addedInView += graph.contains(edge) ? 1 : 0;
            }
            // Only a node-only view takes the edges added between its nodes
            Assert.assertEquals(addedInView > 0, kind[0] && !kind[1]);
            assertSameContent(graphStore, view, control);
        }
    }

    @Test
    public void testDestroyedViewFollowsEdgeTypeChanges() {
        for (boolean[] kind : VIEW_KINDS) {
            GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
            GraphViewImpl view = createPartialView(graphStore, kind[0], kind[1]);
            GraphViewImpl control = createPartialView(graphStore, kind[0], kind[1]);
            graphStore.viewStore.destroyView(view);

            for (Edge edge : graphStore.getEdges().toArray()) {
                if (edge.getStoreId() % 3 == 0) {
                    edge.setType((edge.getType() + 1) % 4);
                }
            }
            assertSameContent(graphStore, view, control);
        }
    }

    @Test
    public void testDestroyedViewFollowsStoreClear() {
        for (boolean[] kind : VIEW_KINDS) {
            GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
            GraphViewImpl view = createPartialView(graphStore, kind[0], kind[1]);
            GraphViewImpl control = createPartialView(graphStore, kind[0], kind[1]);
            graphStore.viewStore.destroyView(view);

            graphStore.clear();
            Assert.assertEquals(view.getNodeCount(), 0);
            Assert.assertEquals(view.getEdgeCount(), 0);
            assertSameContent(graphStore, view, control);

            // The store ids start over
            Node n1 = graphStore.factory.newNode("1");
            Node n2 = graphStore.factory.newNode("2");
            graphStore.addAllNodes(Arrays.asList(n1, n2));
            graphStore.addEdge(graphStore.factory.newEdge(n1, n2, 0, true));
            assertSameContent(graphStore, view, control);
        }
    }

    @Test
    public void testDestroyedViewFollowsStoreClearEdges() {
        for (boolean[] kind : VIEW_KINDS) {
            GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
            GraphViewImpl view = createPartialView(graphStore, kind[0], kind[1]);
            GraphViewImpl control = createPartialView(graphStore, kind[0], kind[1]);
            Set<Node> nodes = toSet(graphStore.viewStore.getDirectedGraph(view).getNodes().toArray());
            graphStore.viewStore.destroyView(view);

            graphStore.clearEdges();
            Assert.assertEquals(view.getEdgeCount(), 0);
            Assert.assertEquals(toSet(graphStore.viewStore.getDirectedGraph(view).getNodes().toArray()), nodes);
            assertSameContent(graphStore, view, control);

            Node[] allNodes = graphStore.getNodes().toArray();
            for (int i = 1; i < allNodes.length; i++) {
                graphStore.addEdge(graphStore.factory.newEdge(allNodes[i - 1], allNodes[i], 0, true));
            }
            assertSameContent(graphStore, view, control);
        }
    }

    @Test
    public void testDestroyedViewWithIndexes() {
        for (TimeRepresentation timeRepresentation : TimeRepresentation.values()) {
            GraphStore graphStore = GraphGenerator.generateEmptyGraphStore(timeRepresentation);
            GraphModelImpl graphModel = graphStore.graphModel;
            Column nodeColumn = graphModel.getNodeTable().addColumn("age", Integer.class);
            Column edgeColumn = graphModel.getEdgeTable().addColumn("kind", String.class);
            List<Node> nodes = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Node node = graphStore.factory.newNode(String.valueOf(i));
                node.setAttribute(nodeColumn, i % 3);
                addTime(node, timeRepresentation, i);
                nodes.add(node);
            }
            graphStore.addAllNodes(nodes);
            for (int i = 1; i < nodes.size(); i++) {
                Edge edge = graphStore.factory.newEdge(nodes.get(i - 1), nodes.get(i), 0, true);
                edge.setAttribute(edgeColumn, "e" + (i % 2));
                addTime(edge, timeRepresentation, i);
                graphStore.addEdge(edge);
            }

            GraphViewImpl view = createPartialView(graphStore, true, true);
            GraphViewImpl control = createPartialView(graphStore, true, true);
            for (GraphView v : Arrays.asList(view, control)) {
                graphModel.getNodeIndex(v);
                graphModel.getEdgeIndex(v);
                graphModel.getNodeTimeIndex(v);
                graphModel.getEdgeTimeIndex(v);
            }
            graphStore.viewStore.destroyView(view);

            Node added = graphStore.factory.newNode("added");
            added.setAttribute(nodeColumn, 1);
            addTime(added, timeRepresentation, 20);
            graphStore.addNode(added);
            Edge addedEdge = graphStore.factory.newEdge(nodes.get(1), nodes.get(5), 0, true);
            addTime(addedEdge, timeRepresentation, 20);
            graphStore.addEdge(addedEdge);
            for (Node node : graphStore.getNodes().toArray()) {
                node.setAttribute(nodeColumn, 5);
                addTime(node, timeRepresentation, 30);
            }
            for (Edge edge : graphStore.getEdges().toArray()) {
                edge.setAttribute(edgeColumn, "changed");
                addTime(edge, timeRepresentation, 30);
            }
            graphStore.getEdges().toArray()[0].setType(1);
            graphStore.removeEdge(graphStore.getEdges().toArray()[0]);
            graphStore.removeNode(nodes.get(2));
            assertSameContent(graphStore, view, control);

            // Only the live view has indexes
            Set<GraphView> indexed = Collections.singleton(control);
            Assert.assertEquals(graphStore.nodeTable.store.indexStore.viewIndexes.keySet(), indexed);
            Assert.assertEquals(graphStore.edgeTable.store.indexStore.viewIndexes.keySet(), indexed);
            Assert.assertEquals(graphStore.timeStore.nodeIndexStore.viewIndexes.keySet(), indexed);
            Assert.assertEquals(graphStore.timeStore.edgeIndexStore.viewIndexes.keySet(), indexed);
            Assert.assertThrows(IllegalArgumentException.class, () -> graphModel.getNodeIndex(view));
            Assert.assertThrows(IllegalArgumentException.class, () -> graphModel.getEdgeTimeIndex(view));
            Assert.assertEquals(graphModel.getNodeIndex(control).count(nodeColumn, 5), graphModel.getGraph(control)
                    .getNodeCount());
        }
    }

    @Test
    public void testDestroyedViewsAreReleased() throws InterruptedException {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        List<WeakReference<GraphView>> released = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            GraphViewImpl view = store.createView(graphStore.mainGraphView);
            store.destroyView(view);
            released.add(new WeakReference<>(view));
        }
        // The store's references to the views are cleared at the same time as these ones
        WeakReference<Object> garbage = new WeakReference<>(new Object());
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while ((garbage.get() != null || released.stream().anyMatch(ref -> ref.get() != null)) && System
                .nanoTime() < deadline) {
            System.gc();
            Thread.sleep(10);
        }
        if (garbage.get() != null) {
            throw new SkipException("No garbage collection happened");
        }
        for (WeakReference<GraphView> ref : released) {
            Assert.assertNull(ref.get());
        }

        GraphViewImpl held = store.createView(graphStore.mainGraphView);
        store.destroyView(held);
        Assert.assertEquals(store.destroyedViews.size(), 1);
        Assert.assertSame(store.destroyedViews.get(0).get(), held);

        Assert.assertThrows(IllegalArgumentException.class, () -> store.destroyView(held));
        Assert.assertThrows(IllegalArgumentException.class, () -> store.createView(held));
        Assert.assertThrows(IllegalArgumentException.class, () -> store.setVisibleView(held));
        Assert.assertEquals(store.destroyedViews.size(), 1);

        graphStore.removeNode(graphStore.getNodes().toArray()[0]);
        Assert.assertEquals(store.getGraph(held).getNodes().toArray().length, graphStore.getNodeCount());
        Assert.assertEquals(store.getGraph(held).getEdges().toArray().length, graphStore.getEdgeCount());
    }

    @Test
    public void testOlderDestroyedViewsAreEmptied() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        int count = GraphViewStore.MAX_DESTROYED_VIEWS + 3;
        GraphViewImpl[] views = new GraphViewImpl[count];
        GraphViewImpl[] controls = new GraphViewImpl[count];
        for (int i = 0; i < count; i++) {
            boolean[] kind = VIEW_KINDS[i % VIEW_KINDS.length];
            views[i] = createPartialView(graphStore, kind[0], kind[1]);
            controls[i] = createPartialView(graphStore, kind[0], kind[1]);
        }
        for (GraphViewImpl view : views) {
            graphStore.viewStore.destroyView(view);
        }

        for (int round = 0; round < 2; round++) {
            graphStore.removeAllEdges(Arrays.asList(graphStore.getEdges().toArray()).subList(0, 10));
            graphStore.removeNode(graphStore.getNodes().toArray()[0]);
            Node node = graphStore.factory.newNode("added" + round);
            graphStore.addNode(node);
            graphStore.addEdge(graphStore.factory
                    .newEdge("added" + round, node, graphStore.getNodes().toArray()[0], 0, 1.0, true));

            for (int i = 0; i < count; i++) {
                if (i >= count - GraphViewStore.MAX_DESTROYED_VIEWS) {
                    assertSameContent(graphStore, views[i], controls[i]);
                    continue;
                }
                // Emptied once it was no longer one of the most recently destroyed views
                Subgraph directed = graphStore.viewStore.getDirectedGraph(views[i]);
                Subgraph undirected = graphStore.viewStore.getUndirectedGraph(views[i]);
                for (Subgraph graph : Arrays.asList(directed, undirected)) {
                    Assert.assertEquals(graph.getEdgeCount(), 0);
                    Assert.assertEquals(graph.getEdges().toArray().length, 0);
                    Assert.assertEquals(graph.getNodeCount(), views[i].nodeView ? 0 : graphStore.getNodeCount());
                    Assert.assertEquals(graph.getNodes().toArray().length, graph.getNodeCount());
                }
            }
        }
    }

    @Test
    public void testDestroyedViewsTrackingIsCapped() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        List<GraphView> held = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            GraphViewImpl view = store.createView(graphStore.mainGraphView);
            store.destroyView(view);
            held.add(view);
            Assert.assertEquals(store.destroyedViews.size(), Math.min(i + 1, GraphViewStore.MAX_DESTROYED_VIEWS));
        }
        // The most recently destroyed, oldest first
        int first = held.size() - GraphViewStore.MAX_DESTROYED_VIEWS;
        for (int i = 0; i < GraphViewStore.MAX_DESTROYED_VIEWS; i++) {
            Assert.assertSame(store.destroyedViews.get(i).get(), held.get(first + i));
        }
    }

    // Keeps part of the nodes and/or edges, the same ones on each call
    private static GraphViewImpl createPartialView(GraphStore graphStore, boolean nodes, boolean edges) {
        return graphStore.viewStore
                .createView(nodes ? n -> n.getStoreId() % 3 != 0 : null, edges ? e -> e.getStoreId() % 2 == 0 : null);
    }

    private static void addTime(Element element, TimeRepresentation timeRepresentation, double time) {
        if (timeRepresentation == TimeRepresentation.TIMESTAMP) {
            element.addTimestamp(time);
        } else {
            element.addInterval(new Interval(time, time + 1));
        }
    }

    private static <T> Set<T> toSet(T[] elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    // A destroyed view is expected to change with the store like a live view created the same way
    private static void assertSameContent(GraphStore graphStore, GraphViewImpl view, GraphViewImpl control) {
        Assert.assertTrue(view.isDestroyed());
        Assert.assertFalse(control.isDestroyed());
        Assert.assertTrue(view.deepEquals(control));
        assertSameContent(graphStore.viewStore.getDirectedGraph(view), graphStore.viewStore.getDirectedGraph(control));
        assertSameContent(graphStore.viewStore.getUndirectedGraph(view), graphStore.viewStore
                .getUndirectedGraph(control));
    }

    private static void assertSameContent(Subgraph graph, Subgraph control) {
        Assert.assertEquals(graph.getNodeCount(), control.getNodeCount());
        Assert.assertEquals(graph.getEdgeCount(), control.getEdgeCount());
        Assert.assertEquals(toSet(graph.getNodes().toArray()), toSet(control.getNodes().toArray()));
        Assert.assertEquals(toSet(graph.getEdges().toArray()), toSet(control.getEdges().toArray()));
        for (int type = 0; type < 4; type++) {
            Assert.assertEquals(graph.getEdgeCount(type), control.getEdgeCount(type));
            Assert.assertEquals(toSet(graph.getEdges(type).toArray()), toSet(control.getEdges(type).toArray()));
        }
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
    public void testGetDirectedGraph() {
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

    @Test
    public void testGetUnDirectedGraph() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        UndirectedSubgraph undirectedSubgraph = store.getUndirectedGraph(view);
        Assert.assertNotNull(undirectedSubgraph);
        Assert.assertSame(view, undirectedSubgraph.getView());
    }

    @Test
    public void testGetGraphDirected() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        Subgraph graph = store.getGraph(view);
        Assert.assertNotNull(graph);
        Assert.assertSame(view, graph.getView());
        Assert.assertTrue(graph.isDirected());
    }

    @Test
    public void testGetGraphUndirected() {
        GraphStore graphStore = GraphGenerator.generateSmallUndirectedGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        Subgraph graph = store.getGraph(view);
        Assert.assertNotNull(graph);
        Assert.assertSame(view, graph.getView());
        Assert.assertTrue(graph.isUndirected());
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

    @Test(expectedExceptions = IllegalArgumentException.class)
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
                return false;
            }

            @Override
            public boolean isNodeView() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Interval getTimeInterval() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isEdgeView() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isDestroyed() {
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
        Assert.assertEquals(graph.getEdgeCount(0), 0);

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
        Assert.assertEquals(graph.getEdgeCount(0), 0);

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
    public void testViewCopy() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView();

        NodeImpl n1 = graphStore.getNode("0");
        view.addNode(n1);

        Assert.assertTrue(view.containsNode(n1));

        GraphViewImpl copyView = store.createView(view);
        Assert.assertTrue(copyView.deepEquals(view));
        Assert.assertEquals(copyView.deepHashCode(), view.deepHashCode());

        Assert.assertTrue(copyView.containsNode(n1));
        view.removeNode(n1);
        Assert.assertTrue(copyView.containsNode(n1));
    }

    @Test
    public void testViewCopyMain() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        GraphViewImpl view = store.createView(store.getVisibleView());

        for (Node n : graphStore.getNodes()) {
            Assert.assertTrue(view.containsNode((NodeImpl) n));
        }
        for (Edge e : graphStore.getEdges()) {
            Assert.assertTrue(view.containsEdge((EdgeImpl) e));
        }
    }

    @Test
    public void testViewSetInterval() {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore store = graphStore.viewStore;
        Interval interval = new Interval(1.0, 2.0);

        GraphViewImpl view = store.createView();
        store.setTimeInterval(view, new Interval(1.0, 2.0));
        Assert.assertEquals(view.getTimeInterval(), interval);
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
        store.setVisibleView(graphStore.mainGraphView);
        Assert.assertEquals(visibleView, graphStore.mainGraphView);
    }

    @Test
    public void testAddRemoveNodeWithGarbage() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphView view = store.createView();
        store.destroyView(view);

        Node n = graphStore.factory.newNode("foo");
        Assert.assertTrue(graphStore.addNode(n));
        Assert.assertTrue(graphStore.removeNode(n));
    }

    @Test
    public void testAddRemoveEdgeWithGarbage() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphView view = store.createView();
        store.destroyView(view);

        Node n = graphStore.factory.newNode("foo");
        graphStore.addNode(n);
        Edge e = graphStore.factory.newEdge("bar", n, n, 0, 1.0, true);
        Assert.assertTrue(graphStore.addEdge(e));
        Assert.assertTrue(graphStore.removeEdge(e));
    }

    @Test
    public void testDeepHashCodeAfterDestroy() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        store.createView();
        GraphViewImpl view2 = store.createView();
        store.destroyView(view2);

        store.deepHashCode();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testDestroyViewNull() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        store.destroyView(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDestroyViewForeignStore() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        GraphStore graphStore2 = GraphGenerator.generateSmallGraphStore();
        GraphViewImpl foreignView = graphStore2.viewStore.createView();

        store.destroyView(foreignView);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsForeignViewImplementation() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        store.contains(new GraphView() {
            @Override
            public GraphModel getGraphModel() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isMainView() {
                return false;
            }

            @Override
            public boolean isNodeView() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Interval getTimeInterval() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isEdgeView() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isDestroyed() {
                throw new UnsupportedOperationException();
            }
        });
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetVisibleViewForeignViewImplementation() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        store.setVisibleView(new GraphView() {
            @Override
            public GraphModel getGraphModel() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isMainView() {
                return false;
            }

            @Override
            public boolean isNodeView() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Interval getTimeInterval() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isEdgeView() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isDestroyed() {
                throw new UnsupportedOperationException();
            }
        });
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCreateViewCopyNull() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewStore store = graphStore.viewStore;

        store.createView((GraphView) null);
    }

    @Test
    public void testRemoveEdgeWithNodeView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;
        Node n1 = graphStore.factory.newNode("1");
        Node n2 = graphStore.factory.newNode("2");
        Node n3 = graphStore.factory.newNode("3");
        graphStore.addAllNodes(java.util.Arrays.asList(n1, n2, n3));
        Edge inView = graphStore.factory.newEdge("a", n1, n2, 0, 1.0, true);
        Edge notInView = graphStore.factory.newEdge("b", n2, n3, 0, 1.0, true);
        graphStore.addEdge(inView);
        graphStore.addEdge(notInView);

        GraphViewImpl view = (GraphViewImpl) graphModel.createView(true, false);
        Subgraph graph = graphModel.getGraph(view);
        graph.addNode(n1);
        graph.addNode(n2);
        Assert.assertEquals(graph.getEdgeCount(), 1);

        graphStore.removeEdge(notInView);
        Assert.assertEquals(graph.getEdgeCount(), 1);
        graphStore.removeEdge(inView);
        Assert.assertEquals(graph.getEdgeCount(), 0);
        Assert.assertEquals(graph.getEdgeCount(0), 0);
        Assert.assertEquals(graph.getEdges().toArray().length, 0);
    }

    @Test
    public void testSetEdgeTypeWithNodeView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;
        Node n1 = graphStore.factory.newNode("1");
        Node n2 = graphStore.factory.newNode("2");
        Node n3 = graphStore.factory.newNode("3");
        graphStore.addAllNodes(java.util.Arrays.asList(n1, n2, n3));
        Edge inView = graphStore.factory.newEdge("a", n1, n2, 0, 1.0, true);
        Edge notInView = graphStore.factory.newEdge("b", n2, n3, 0, 1.0, true);
        graphStore.addEdge(inView);
        graphStore.addEdge(notInView);

        GraphViewImpl view = (GraphViewImpl) graphModel.createView(true, false);
        Subgraph graph = graphModel.getGraph(view);
        graph.addNode(n1);
        graph.addNode(n2);

        notInView.setType(1);
        Assert.assertEquals(graph.getEdgeCount(0), 1);
        inView.setType(1);
        Assert.assertEquals(graph.getEdgeCount(0), 0);
        Assert.assertEquals(graph.getEdgeCount(1), 1);
        Assert.assertEquals(graph.getEdgeCount(), 1);
    }

    @Test
    public void testCopyEdgeView() {
        GraphModelImpl graphModel = GraphGenerator.generateTinyGraphStore().graphModel;
        GraphView view = graphModel.createView(false, true);
        Edge edge = graphModel.getGraph().getEdges().toArray()[0];
        graphModel.getGraph(view).addEdge(edge);

        GraphView copy = graphModel.copyView(view);
        Assert.assertFalse(copy.isNodeView());
        Assert.assertTrue(copy.isEdgeView());
        Assert.assertEquals(graphModel.getGraph(copy).getEdgeCount(), 1);
        Assert.assertTrue(graphModel.getGraph(copy).contains(edge));
        Assert.assertEquals(graphModel.getGraph(copy).getNodeCount(), graphModel.getGraph().getNodeCount());
    }

    @Test
    public void testRemoveNodeWithEdgeView() {
        GraphModelImpl graphModel = GraphGenerator.generateTinyGraphStore().graphModel;
        GraphView view = graphModel.createView(false, true);
        Edge edge = graphModel.getGraph().getEdges().toArray()[0];
        Subgraph graph = graphModel.getGraph(view);
        graph.addEdge(edge);

        graphModel.getGraph().removeNode(edge.getSource());
        Assert.assertEquals(graph.getEdgeCount(), 0);
        Assert.assertEquals(graph.getNodeCount(), graphModel.getGraph().getNodeCount());
        Assert.assertEquals(graph.getNodes().toCollection(), graphModel.getGraph().getNodes().toCollection());

        graphModel.getGraph().removeAllNodes(graphModel.getGraph().getNodes().toCollection());
        Assert.assertEquals(graph.getNodeCount(), 0);
        Assert.assertEquals(graph.getEdgeCount(), 0);
    }

    @Test
    public void testEdgeViewNodeIndexOnStoreAddAndRemove() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphModelImpl graphModel = graphStore.graphModel;
        Column column = graphModel.getNodeTable().addColumn("age", Integer.class);
        GraphView view = graphModel.createView(false, true);
        Assert.assertEquals(graphModel.getNodeIndex(view).count(column, null), 2);

        Node node = graphModel.factory().newNode("3");
        graphModel.getGraph().addNode(node);
        Assert.assertEquals(graphModel.getNodeIndex(view).count(column, null), 3);

        graphModel.getGraph().removeNode(node);
        Assert.assertEquals(graphModel.getNodeIndex(view).count(column, null), 2);
    }

    @Test
    public void testNodeViewIndexOnStoreAddWithReusedStoreId() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        GraphModelImpl graphModel = graphStore.graphModel;
        Column column = graphModel.getNodeTable().addColumn("age", Integer.class);
        GraphView view = graphModel.createView();
        graphModel.getGraph(view).fill();
        Assert.assertEquals(graphModel.getNodeIndex(view).count(column, null), 2);

        Node removed = graphStore.getNode("1");
        int storeId = removed.getStoreId();
        graphModel.getGraph().removeNode(removed);
        Node node = graphModel.factory().newNode("3");
        graphModel.getGraph().addNode(node);
        Assert.assertEquals(node.getStoreId(), storeId);
        Assert.assertFalse(graphModel.getGraph(view).contains(node));
        Assert.assertEquals(graphModel.getNodeIndex(view).count(column, null), 1);
    }

    @Test
    public void testCopyEdgeViewWithNodes() {
        GraphModelImpl graphModel = GraphGenerator.generateTinyGraphStore().graphModel;
        GraphView view = graphModel.createView(false, true);
        Edge edge = graphModel.getGraph().getEdges().toArray()[0];
        graphModel.getGraph(view).addEdge(edge);

        GraphView copy = graphModel.copyView(view, true, true);
        Subgraph graph = graphModel.getGraph(copy);
        Assert.assertTrue(copy.isNodeView());
        Assert.assertEquals(graph.getNodeCount(), graphModel.getGraph().getNodeCount());
        for (Node node : graphModel.getGraph().getNodes()) {
            Assert.assertTrue(graph.contains(node));
        }
        Assert.assertEquals(graph.getEdgeCount(), 1);
        Assert.assertTrue(graph.contains(edge));
    }

    @Test
    public void testCopyNodeView() {
        GraphModelImpl graphModel = GraphGenerator.generateTinyGraphStore().graphModel;
        GraphView view = graphModel.createView(true, false);
        graphModel.getGraph(view).fill();

        GraphView copy = graphModel.copyView(view);
        Assert.assertTrue(copy.isNodeView());
        Assert.assertFalse(copy.isEdgeView());
        Assert.assertEquals(graphModel.getGraph(copy).getNodeCount(), graphModel.getGraph().getNodeCount());
        Assert.assertEquals(graphModel.getGraph(copy).getEdgeCount(), graphModel.getGraph().getEdgeCount());
    }

    @Test
    public void testRacingOperationsOnDestroyedViewThrow() throws Exception {
        String[] ops = { "createView", "destroyView", "setTimeInterval", "nodeIndex", "edgeIndex", "nodeTimeIndex", "edgeTimeIndex", "observer" };
        for (String op : ops) {
            GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
            GraphModelImpl model = (GraphModelImpl) graphStore.graphModel;
            GraphViewImpl view = graphStore.viewStore.createView();
            Callable<Object> action;
            switch (op) {
                case "createView":
                    action = () -> graphStore.viewStore.createView(view);
                    break;
                case "destroyView":
                    action = () -> {
                        graphStore.viewStore.destroyView(view);
                        return null;
                    };
                    break;
                case "setTimeInterval":
                    action = () -> {
                        graphStore.viewStore.setTimeInterval(view, new Interval(1, 2));
                        return null;
                    };
                    break;
                case "edgeIndex":
                    action = () -> model.getEdgeIndex(view);
                    break;
                case "nodeTimeIndex":
                    action = () -> model.getNodeTimeIndex(view);
                    break;
                case "edgeTimeIndex":
                    action = () -> model.getEdgeTimeIndex(view);
                    break;
                case "observer":
                    action = () -> graphStore.viewStore.createGraphObserver(view.getDirectedGraph(), false);
                    break;
                default:
                    action = () -> model.getNodeIndex(view);
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<Object> future;
                graphStore.writeLock();
                try {
                    future = executor.submit(action);
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                    while (!graphStore.lock.readWriteLock.hasQueuedThreads() && !future.isDone() && System
                            .nanoTime() < deadline) {
                        Thread.sleep(1);
                    }
                    Assert.assertTrue(graphStore.lock.readWriteLock.hasQueuedThreads(), op);
                    graphStore.viewStore.destroyView(view);
                } finally {
                    graphStore.writeUnlock();
                }
                try {
                    future.get(5, TimeUnit.SECONDS);
                    Assert.fail(op);
                } catch (ExecutionException e) {
                    Assert.assertTrue(e.getCause() instanceof IllegalArgumentException, op);
                }
            } finally {
                executor.shutdownNow();
            }
        }
    }

    @Test
    public void testViewOperationsWithoutTimeIndex() {
        GraphModel model = GraphModel.Factory.newInstance(Configuration.builder().enableIndexTime(false).build());
        Node n1 = model.factory().newNode("1");
        Node n2 = model.factory().newNode("2");
        Edge e = model.factory().newEdge(n1, n2);
        model.getDirectedGraph().addNode(n1);
        model.getDirectedGraph().addNode(n2);
        model.getDirectedGraph().addEdge(e);

        GraphView view = model.createView();
        Subgraph subgraph = model.getGraph(view);
        subgraph.fill();
        subgraph.removeNode(n1);
        subgraph.addNode(n1);
        subgraph.addEdge(e);
        subgraph.removeEdge(e);
        subgraph.not();
        subgraph.clear();
        model.destroyView(view);
        Assert.assertTrue(view.isDestroyed());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetTimeIntervalMainView() {
        GraphStore graphStore = new GraphStore();
        graphStore.viewStore.setTimeInterval(graphStore.mainGraphView, new Interval(1, 2));
    }
}
