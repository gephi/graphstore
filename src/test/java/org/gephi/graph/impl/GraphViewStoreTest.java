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

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.UndirectedSubgraph;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

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
