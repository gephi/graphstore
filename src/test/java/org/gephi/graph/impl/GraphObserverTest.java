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
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphDiff;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class GraphObserverTest {

    @Test
    public void testDefaultObserver() {
        GraphStore store = new GraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, false);

        Assert.assertFalse(graphObserver.destroyed);
        Assert.assertSame(store.version, graphObserver.graphVersion);
        Assert.assertTrue(store.observers.contains(graphObserver));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCheckGraphStore() {
        GraphStore store = new GraphStore();
        GraphStore store2 = new GraphStore();
        store.createGraphObserver(store2, false);
    }

    @Test
    public void testDefaultObserverWithUndirectedGraph() {
        GraphStore store = new GraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store.undirectedDecorator, false);

        Assert.assertFalse(graphObserver.destroyed);
        Assert.assertSame(store.version, graphObserver.graphVersion);
        Assert.assertTrue(store.observers.contains(graphObserver));
    }

    @Test
    public void testDefaultViewObserver() {
        GraphStore store = new GraphStore();
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphObserverImpl graphObserver = viewStore.createGraphObserver(viewStore.getDirectedGraph(view), false);

        Assert.assertFalse(graphObserver.destroyed);
        Assert.assertSame(view.version, graphObserver.graphVersion);
        Assert.assertTrue(view.observers.contains(graphObserver));
    }

    @Test
    public void testDefaultViewObserverWithUndirectedGraph() {
        GraphStore store = new GraphStore();
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphObserverImpl graphObserver = viewStore.createGraphObserver(viewStore.getUndirectedGraph(view), false);

        Assert.assertFalse(graphObserver.destroyed);
        Assert.assertSame(view.version, graphObserver.graphVersion);
        Assert.assertTrue(view.observers.contains(graphObserver));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCheckGraphViewOtherStore() {
        GraphStore store = new GraphStore();
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphStore store2 = new GraphStore();
        GraphViewStore viewStore2 = store2.viewStore;
        viewStore2.createGraphObserver(viewStore.getUndirectedGraph(view), false);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCheckGraphViewDestroyedView() {
        GraphStore store = new GraphStore();
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        viewStore.destroyView(view);
        viewStore.createGraphObserver(viewStore.getUndirectedGraph(view), false);
    }

    @Test
    public void testDestroyGraphObserver() {
        GraphStore store = new GraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, false);
        store.destroyGraphObserver(graphObserver);

        Assert.assertTrue(graphObserver.destroyed);
        Assert.assertFalse(store.observers.contains(graphObserver));
    }

    @Test
    public void testDestroyGraphObserverAlternative() {
        GraphStore store = GraphGenerator.generateTinyGraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, false);
        graphObserver.destroy();

        Assert.assertTrue(graphObserver.destroyed);
        Assert.assertTrue(graphObserver.isDestroyed());
        Assert.assertFalse(graphObserver.isNew());
        Assert.assertFalse(store.observers.contains(graphObserver));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCheckDestroyOtherStore() {
        GraphStore store = new GraphStore();
        GraphStore store2 = new GraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, false);
        store2.destroyGraphObserver(graphObserver);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCheckDestroyAlreadyDestroyed() {
        GraphStore store = new GraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, false);
        store.destroyGraphObserver(graphObserver);
        store.destroyGraphObserver(graphObserver);
    }

    @Test
    public void testDestroyViewObserver() {
        GraphStore store = new GraphStore();
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphObserverImpl graphObserver = viewStore.createGraphObserver(viewStore.getDirectedGraph(view), false);

        viewStore.destroyGraphObserver(graphObserver);

        Assert.assertTrue(graphObserver.destroyed);
        Assert.assertFalse(view.observers.contains(graphObserver));
    }

    @Test
    public void testDestroyViewObserverAlternative() {
        GraphStore store = new GraphModelImpl().store;
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphObserverImpl graphObserver = viewStore.createGraphObserver(viewStore.getDirectedGraph(view), false);

        graphObserver.destroy();

        Assert.assertTrue(graphObserver.destroyed);
        Assert.assertFalse(view.observers.contains(graphObserver));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCheckDestroyOtherView() {
        GraphStore store = new GraphStore();
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphObserverImpl graphObserver = viewStore.createGraphObserver(viewStore.getDirectedGraph(view), false);
        GraphStore store2 = new GraphStore();
        GraphViewStore viewStore2 = store2.viewStore;
        viewStore2.destroyGraphObserver(graphObserver);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCheckViewDestroyAlreadyDestroyed() {
        GraphStore store = new GraphStore();
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphObserverImpl graphObserver = viewStore.createGraphObserver(viewStore.getDirectedGraph(view), false);
        viewStore.destroyGraphObserver(graphObserver);
        viewStore.destroyGraphObserver(graphObserver);
    }

    @Test
    public void testCheckDestroyedViewDestroy() {
        GraphStore store = new GraphStore();
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphObserverImpl graphObserver = viewStore.createGraphObserver(viewStore.getDirectedGraph(view), false);
        viewStore.destroyView(view);

        Assert.assertTrue(graphObserver.destroyed);
        Assert.assertFalse(view.observers.contains(graphObserver));
    }

    @Test
    public void testHasGraphChanged() {
        GraphStore store = new GraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, false);

        Assert.assertTrue(graphObserver.isNew());
        Assert.assertFalse(graphObserver.hasGraphChanged());
        Assert.assertFalse(graphObserver.isNew());

        store.addNode(store.factory.newNode());
        Assert.assertTrue(graphObserver.hasGraphChanged());
        Assert.assertFalse(graphObserver.hasGraphChanged());
    }

    @Test
    public void testHasGraphChangedAfterDestroy() {
        GraphStore store = new GraphModelImpl().store;
        GraphObserverImpl graphObserver = store.createGraphObserver(store, false);
        store.addNode(store.factory.newNode());
        graphObserver.destroy();

        Assert.assertFalse(graphObserver.hasGraphChanged());
    }

    @Test
    public void testHasViewChanged() {
        GraphStore store = new GraphStore();
        GraphViewStore viewStore = store.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphObserverImpl graphObserver = viewStore.createGraphObserver(viewStore.getDirectedGraph(view), false);

        Assert.assertFalse(graphObserver.hasGraphChanged());

        Node n = store.factory.newNode();
        store.addNode(n);
        Assert.assertFalse(graphObserver.hasGraphChanged());

        view.fill();

        Assert.assertTrue(graphObserver.hasGraphChanged());
        Assert.assertFalse(graphObserver.hasGraphChanged());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDiffWithoutSetting() {
        GraphStore store = new GraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, false);
        graphObserver.getDiff();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetDiffWithoutHasGraphChanged() {
        GraphStore store = new GraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);
        graphObserver.getDiff();
    }

    @Test
    public void testGetDiff() {
        GraphStore store = GraphGenerator.generateSmallGraphStore();
        Node[] nodesExpected = store.getNodes().toArray();
        Edge[] edgesExpected = store.getEdges().toArray();
        store.clear();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);
        store.addAllNodes(Arrays.asList(nodesExpected));
        store.addAllEdges(Arrays.asList(edgesExpected));

        boolean a = graphObserver.hasGraphChanged();
        GraphDiff diff = graphObserver.getDiff();

        Node[] nodes = diff.getAddedNodes().toArray();
        Edge[] edges = diff.getAddedEdges().toArray();

        Assert.assertTrue(a);
        Assert.assertTrue(Arrays.deepEquals(nodes, nodesExpected));
        Assert.assertTrue(Arrays.deepEquals(edges, edgesExpected));
        Assert.assertSame(diff.getRemovedNodes(), NodeIterable.EMPTY);
        Assert.assertSame(diff.getRemovedEdges(), EdgeIterable.EMPTY);
    }

    @Test
    public void testDiffAddedNodes() {
        GraphStore store = GraphGenerator.generateSmallGraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);

        Node[] addedNodes = new Node[] { store.factory.newNode("r1"), store.factory.newNode("r2") };
        store.addAllNodes(Arrays.asList(addedNodes));

        boolean a = graphObserver.hasGraphChanged();
        GraphDiff diff = graphObserver.getDiff();

        Node[] nodes = diff.getAddedNodes().toArray();

        Assert.assertTrue(a);
        Assert.assertTrue(Arrays.deepEquals(nodes, addedNodes));
        Assert.assertSame(diff.getAddedEdges(), EdgeIterable.EMPTY);
        Assert.assertSame(diff.getRemovedNodes(), NodeIterable.EMPTY);
        Assert.assertSame(diff.getRemovedEdges(), EdgeIterable.EMPTY);
    }

    @Test
    public void testDiffAddedEdges() {
        GraphStore store = GraphGenerator.generateSmallGraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);
        Node[] addedNodes = new Node[] { store.factory.newNode("r1"), store.factory.newNode("r2") };
        store.addAllNodes(Arrays.asList(addedNodes));

        graphObserver.hasGraphChanged();
        Edge[] addedEdges = new Edge[] { store.factory.newEdge("edge", addedNodes[0], addedNodes[1], 0, 1.0, true) };
        store.addEdge(addedEdges[0]);

        boolean a = graphObserver.hasGraphChanged();
        GraphDiff diff = graphObserver.getDiff();

        Edge[] edges = diff.getAddedEdges().toArray();

        Assert.assertTrue(a);
        Assert.assertTrue(Arrays.deepEquals(edges, addedEdges));
        Assert.assertSame(diff.getAddedNodes(), NodeIterable.EMPTY);
        Assert.assertSame(diff.getRemovedNodes(), NodeIterable.EMPTY);
        Assert.assertSame(diff.getRemovedEdges(), EdgeIterable.EMPTY);
    }

    @Test
    public void testDiffRemoveNodes() {
        GraphStore store = GraphGenerator.generateSmallGraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);

        Node[] removedNodes = new Node[] { store.factory.newNode("r1"), store.factory.newNode("r2") };
        store.addAllNodes(Arrays.asList(removedNodes));

        graphObserver.hasGraphChanged();
        store.removeAllNodes(Arrays.asList(removedNodes));

        boolean a = graphObserver.hasGraphChanged();
        GraphDiff diff = graphObserver.getDiff();

        Node[] nodes = diff.getRemovedNodes().toArray();

        Assert.assertTrue(a);
        Assert.assertTrue(Arrays.deepEquals(nodes, removedNodes));
        Assert.assertSame(diff.getAddedEdges(), EdgeIterable.EMPTY);
        Assert.assertSame(diff.getAddedNodes(), NodeIterable.EMPTY);
        Assert.assertSame(diff.getRemovedEdges(), EdgeIterable.EMPTY);
    }

    @Test
    public void testDiffRemoveEdges() {
        GraphStore store = GraphGenerator.generateSmallGraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);

        Node[] addedNodes = new Node[] { store.factory.newNode("r1"), store.factory.newNode("r2") };
        store.addAllNodes(Arrays.asList(addedNodes));
        Edge[] addedEdges = new Edge[] { store.factory.newEdge("edge", addedNodes[0], addedNodes[1], 0, 1.0, true) };
        store.addEdge(addedEdges[0]);
        graphObserver.hasGraphChanged();
        store.removeEdge(addedEdges[0]);

        boolean a = graphObserver.hasGraphChanged();
        GraphDiff diff = graphObserver.getDiff();

        Edge[] edges = diff.getRemovedEdges().toArray();

        Assert.assertTrue(a);
        Assert.assertTrue(Arrays.deepEquals(edges, addedEdges));
        Assert.assertSame(diff.getRemovedNodes(), NodeIterable.EMPTY);
        Assert.assertSame(diff.getAddedNodes(), NodeIterable.EMPTY);
        Assert.assertSame(diff.getAddedEdges(), EdgeIterable.EMPTY);
    }

    @Test
    public void testDiffRemoveAllNodes() {
        GraphStore store = GraphGenerator.generateSmallGraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);
        Node[] nodes = store.getNodes().toArray();
        Edge[] edges = store.getEdges().toArray();
        graphObserver.hasGraphChanged();
        store.clear();

        boolean a = graphObserver.hasGraphChanged();
        GraphDiff diff = graphObserver.getDiff();

        Node[] removedNodes = diff.getRemovedNodes().toArray();
        Edge[] removedEdges = diff.getRemovedEdges().toArray();

        Assert.assertTrue(a);
        Assert.assertTrue(Arrays.deepEquals(removedNodes, nodes));
        Assert.assertTrue(Arrays.deepEquals(removedEdges, edges));
        Assert.assertSame(diff.getAddedEdges(), EdgeIterable.EMPTY);
        Assert.assertSame(diff.getAddedNodes(), NodeIterable.EMPTY);
    }

    @Test
    public void testDiffReplaceNode() {
        GraphStore store = GraphGenerator.generateSmallGraphStore();
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);
        graphObserver.hasGraphChanged();
        Node node = store.getNodes().toArray()[0];
        Node newNode = store.factory.newNode();
        store.removeNode(node);
        store.addNode(newNode);

        graphObserver.hasGraphChanged();
        GraphDiff diff = graphObserver.getDiff();

        Node[] removedNodes = diff.getRemovedNodes().toArray();
        Node[] addedNodes = diff.getAddedNodes().toArray();

        Assert.assertTrue(Arrays.deepEquals(addedNodes, new Node[] { newNode }));
        Assert.assertTrue(Arrays.deepEquals(removedNodes, new Node[] { node }));
    }

    @Test
    public void testResetNodeVersion() {
        GraphStore store = GraphGenerator.generateSmallGraphStore();
        store.version.nodeVersion = Integer.MAX_VALUE - 1;
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);
        graphObserver.hasGraphChanged();
        store.addNode(store.factory.newNode("node"));

        int nodeVersion = store.version.nodeVersion;

        Assert.assertEquals(nodeVersion, Integer.MIN_VALUE + 1);
        Assert.assertEquals(graphObserver.nodeVersion, Integer.MIN_VALUE);
    }

    @Test
    public void testResetEdgeVersion() {
        GraphStore store = GraphGenerator.generateTinyGraphStore();
        Node n1 = store.getNode("1");
        Node n2 = store.getNode("2");
        store.version.edgeVersion = Integer.MAX_VALUE - 1;
        GraphObserverImpl graphObserver = store.createGraphObserver(store, true);
        graphObserver.hasGraphChanged();
        store.addEdge(store.factory.newEdge("edge", n2, n1, EdgeTypeStore.NULL_LABEL, 1.0, true));

        int edgeVersion = store.version.edgeVersion;

        Assert.assertEquals(edgeVersion, Integer.MIN_VALUE + 1);
        Assert.assertEquals(graphObserver.edgeVersion, Integer.MIN_VALUE);
    }
}
