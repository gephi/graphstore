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
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampIndexStoreTest {

    @Test
    public void testAddElement() {
        TimestampStore timestampStore = new TimestampStore(null, null, true);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(1.0, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.get(1.0))[0], nodeImpl);
    }

    @Test
    public void testRemoveElement() {
        TimestampStore timestampStore = new TimestampStore(null, null, true);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(1.0, nodeImpl);
        store.remove(1.0, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.get(1.0)).length, 0);
    }

    @Test
    public void testIndexNode() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.timestampMap.size(), 2);

        store.index(nodeImpl);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(1.0, 2.0)));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testIndexNodeAdd() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.timestampMap.size(), 2);

        graphStore.addNode(nodeImpl);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(1.0, 2.0)));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testClearNode() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.timestampMap.size(), 2);

        store.index(nodeImpl);
        store.clear(nodeImpl);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(1.0, 2.0)));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }

    @Test
    public void testClearRemove() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.timestampMap.size(), 2);

        graphStore.addNode(nodeImpl);
        graphStore.removeNode(nodeImpl);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(1.0, 2.0)));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }

    @Test
    public void testAddAfterAdd() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        nodeImpl.addTimestamp(3.0);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(3.0, 3.0)));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testRemoveAfterAdd() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        nodeImpl.removeTimestamp(1.0);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(1.0, 1.0)));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);

        ObjectSet r2 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(2.0, 2.0)));
        Assert.assertTrue(r2.contains(nodeImpl));
        Assert.assertEquals(r2.size(), 1);
    }

    @Test
    public void testRemoveTimestampNodes() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        graphStore.removeNode(nodeImpl);

        Assert.assertEquals(store.timestampMap.size(), 0);
    }

    @Test
    public void testCreateView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);
        n1.addTimestamp(2.0);

        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        GraphView view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        TimestampIndexImpl index = store.createViewIndex(graph);
        Assert.assertSame(store.getIndex(graph), index);
        Assert.assertFalse(index.hasElements());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateViewMainView() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;
        store.createViewIndex(graphStore);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDeleteViewMainView() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;
        store.deleteViewIndex(graphStore);
    }

    @Test
    public void testCreateViewWithElements() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);
        n1.addTimestamp(2.0);

        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimestampIndexImpl index = store.createViewIndex(graph);
        Assert.assertTrue(index.hasElements());
    }

    @Test
    public void testDeleteViewWithElements() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);
        n1.addTimestamp(2.0);

        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimestampIndexImpl index = store.createViewIndex(graph);
        store.deleteViewIndex(graph);
        Assert.assertFalse(index.hasElements());
        Assert.assertFalse(store.viewIndexes.containsKey(view));
    }

    @Test
    public void testIndexWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);

        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);

        TimestampIndexImpl index = store.createViewIndex(graph);
        graph.addNode(n1);

        Assert.assertTrue(index.hasElements());
        Assert.assertSame(getArrayFromIterable(index.get(1.0))[0], n1);
    }

    @Test
    public void testClearWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);

        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimestampIndexImpl index = store.createViewIndex(graph);
        store.clear();
        Assert.assertFalse(index.hasElements());
    }

    @Test
    public void testClearElementWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);

        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimestampIndexImpl index = store.createViewIndex(graph);
        n1.clearAttributes();
        Assert.assertFalse(index.hasElements());
    }

    @Test
    public void testClearViewWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);

        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimestampIndexImpl index = store.createViewIndex(graph);
        view.clear();
        Assert.assertFalse(index.hasElements());
    }

    //UTILITY
    private <T> Object[] getArrayFromIterable(Iterable<T> iterable) {
        List<T> list = new ArrayList<T>();
        for (T t : iterable) {
            list.add(t);
        }
        return list.toArray();
    }
}
