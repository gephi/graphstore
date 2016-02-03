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
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.types.TimestampStringMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimestampIndexStoreTest {

    @Test
    public void testEmpty() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        Assert.assertTrue(store.size() == 0);
    }

    @Test
    public void testAddOne() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        int a = store.add(1.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(store.contains(1.0));
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(store.getMap().get(1.0), 0);
    }

    @Test
    public void testAddTwiceSame() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        int a = store.add(1.0);
        int b = store.add(1.0);

        Assert.assertEquals(a, b);
        Assert.assertTrue(store.contains(1.0));
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(store.getMap().get(1.0), 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddInfinityTimestamp() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.add(Double.POSITIVE_INFINITY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddNaNTimestamp() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.add(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsNaN() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.contains(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsInfinity() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.contains(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testRemove() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.add(1.0);
        store.remove(1.0);

        Assert.assertTrue(store.size() == 0);
        Assert.assertFalse(store.contains(1.0));
    }

    @Test
    public void testRemoveWithCount() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.add(1.0);
        store.add(1.0);
        store.remove(1.0);

        Assert.assertTrue(store.size() == 1);
        Assert.assertTrue(store.contains(1.0));

        store.remove(1.0);

        Assert.assertTrue(store.size() == 0);
        Assert.assertFalse(store.contains(1.0));
    }

    @Test
    public void testRemoveUnknown() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.remove(1.0);

        Assert.assertTrue(store.size() == 0);
        Assert.assertFalse(store.contains(1.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveNaN() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.remove(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveInfinity() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.remove(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testNotContains() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        Assert.assertFalse(store.contains(1.0));
    }

    @Test
    public void testAddAfterRemove() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.add(1.0);
        store.remove(1.0);
        int a = store.add(2.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(store.contains(2.0));
    }

    @Test
    public void testContains() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);

        Assert.assertFalse(store.contains(1.0));

        store.add(1.0);

        Assert.assertTrue(store.contains(1.0));

        store.remove(1.0);

        Assert.assertFalse(store.contains(1.0));
    }

    @Test
    public void testGarbage() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);

        store.add(1.0);
        int pos = store.add(2.0);
        store.add(3.0);
        store.remove(2.0);

        Assert.assertEquals(1, store.garbageQueue.size());
        Assert.assertEquals(pos, store.garbageQueue.firstInt());
        Assert.assertEquals(2, store.size());

        int pos2 = store.add(6.0);

        Assert.assertEquals(pos, pos2);
        Assert.assertTrue(store.garbageQueue.isEmpty());
        Assert.assertEquals(3, store.size());
    }

    @Test
    public void testClear() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, false);
        store.clear();

        store.add(1.0);

        store.clear();

        Assert.assertEquals(0, store.size());
    }

    @Test
    public void testDeepEqualsEmpty() {
        TimestampIndexStore<Node> store1 = new TimestampIndexStore<Node>(Node.class, null, false);
        Assert.assertFalse(store1.deepEquals(null));
        Assert.assertTrue(store1.deepEquals(store1));

        TimestampIndexStore<Node> store2 = new TimestampIndexStore<Node>(Node.class, null, false);
        Assert.assertTrue(store1.deepEquals(store2));
    }

    @Test
    public void testDeepHashCodeEmpty() {
        TimestampIndexStore<Node> store1 = new TimestampIndexStore<Node>(Node.class, null, false);
        Assert.assertEquals(store1.deepHashCode(), store1.deepHashCode());

        TimestampIndexStore<Node> store2 = new TimestampIndexStore<Node>(Node.class, null, false);
        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
    }

    @Test
    public void testDeepEquals() {
        TimestampIndexStore<Node> store1 = new TimestampIndexStore<Node>(Node.class, null, false);
        store1.add(1.0);
        store1.add(2.0);
        store1.add(3.0);
        store1.remove(1.0);

        TimestampIndexStore<Node> store2 = new TimestampIndexStore<Node>(Node.class, null, false);
        store2.add(1.0);
        store2.add(2.0);
        store2.add(3.0);
        store2.remove(1.0);

        TimestampIndexStore<Node> store3 = new TimestampIndexStore<Node>(Node.class, null, false);
        store3.add(1.0);

        TimestampIndexStore<Node> store4 = new TimestampIndexStore<Node>(Node.class, null, false);
        store4.add(1.0);
        store4.add(1.0);

        Assert.assertTrue(store1.deepEquals(store2));
        Assert.assertFalse(store1.deepEquals(store3));
        Assert.assertFalse(store3.deepEquals(store4));
    }

    @Test
    public void testDeepHashCode() {
        TimestampIndexStore<Node> store1 = new TimestampIndexStore<Node>(Node.class, null, false);
        store1.add(1.0);
        store1.add(2.0);
        store1.add(3.0);
        store1.remove(1.0);

        TimestampIndexStore<Node> store2 = new TimestampIndexStore<Node>(Node.class, null, false);
        store2.add(1.0);
        store2.add(2.0);
        store2.add(3.0);
        store2.remove(1.0);

        TimestampIndexStore<Node> store3 = new TimestampIndexStore<Node>(Node.class, null, false);
        store3.add(1.0);

        TimestampIndexStore<Node> store4 = new TimestampIndexStore<Node>(Node.class, null, false);
        store4.add(1.0);
        store4.add(1.0);

        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
        Assert.assertNotEquals(store1.deepHashCode(), store3.deepHashCode());
        Assert.assertNotEquals(store3.deepHashCode(), store4.deepHashCode());
    }

    @Test
    public void testAddElement() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, true);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(1.0, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.get(1.0))[0], nodeImpl);
    }

    @Test
    public void testRemoveElement() {
        TimestampIndexStore<Node> store = new TimestampIndexStore<Node>(Node.class, null, true);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(1.0, nodeImpl);
        store.remove(1.0, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.get(1.0)).length, 0);
    }

    @Test
    public void testIndexNode() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        store.index(nodeImpl);
        Assert.assertEquals(store.size(), 2);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 2.0))));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testIndexNodeAdd() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        Assert.assertEquals(store.size(), 2);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 2.0))));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testClearNode() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        store.index(nodeImpl);
        store.clear(nodeImpl);

        Assert.assertEquals(store.size(), 0);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 2.0))));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }

    @Test
    public void testClearNodeWithAttributes() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        Column col = graphStore.nodeTable.addColumn("foo", TimestampStringMap.class);
        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.setAttribute(col, "bar", 1.0);

        store.index(nodeImpl);
        store.clear(nodeImpl);

        Assert.assertEquals(store.size(), 0);
    }

    @Test
    public void testClearRemove() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        graphStore.removeNode(nodeImpl);

        Assert.assertEquals(store.size(), 0);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 2.0))));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }

    @Test
    public void testAddAfterAdd() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        nodeImpl.addTimestamp(3.0);

        Assert.assertEquals(store.size(), 3);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(3.0, 3.0))));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testRemoveAfterAdd() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        nodeImpl.removeTimestamp(1.0);

        Assert.assertEquals(store.size(), 1);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 1.0))));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);

        ObjectSet r2 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(2.0, 2.0))));
        Assert.assertTrue(r2.contains(nodeImpl));
        Assert.assertEquals(r2.size(), 1);
    }

    @Test
    public void testSetAttribute() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        Column col = graphStore.nodeTable.addColumn("col", TimestampStringMap.class);
        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        graphStore.addNode(nodeImpl);

        TimestampStringMap s1 = new TimestampStringMap();
        s1.put(1.0, "foo");
        s1.put(2.0, "bar");

        nodeImpl.setAttribute(col, s1);

        Assert.assertEquals(store.size(), 2);
        Assert.assertTrue(store.contains(1.0));
        Assert.assertTrue(store.contains(2.0));

        nodeImpl.setAttribute(col, new TimestampStringMap());
        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.contains(1.0));
        Assert.assertFalse(store.contains(2.0));
    }

    @Test
    public void testRemoveAttributeTimestamp() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        graphStore.addNode(nodeImpl);
        nodeImpl.addTimestamp(3.0);
        Assert.assertTrue(store.contains(3.0));

        nodeImpl.removeAttribute(graphStore.nodeTable.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX));
        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.contains(3.0));
    }

    @Test
    public void testCreateView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);
        n1.addTimestamp(2.0);

        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        GraphView view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        TimeIndexImpl index = store.createViewIndex(graph);
        Assert.assertSame(store.getIndex(graph), index);
        Assert.assertFalse(index.hasElements());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateViewMainView() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;
        store.createViewIndex(graphStore);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDeleteViewMainView() {
        GraphStore graphStore = new GraphStore();
        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;
        store.deleteViewIndex(graphStore);
    }

    @Test
    public void testCreateViewWithElements() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);
        n1.addTimestamp(2.0);

        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        Assert.assertTrue(index.hasElements());
    }

    @Test
    public void testDeleteViewWithElements() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);
        n1.addTimestamp(2.0);

        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        store.deleteViewIndex(graph);
        Assert.assertFalse(index.hasElements());
        Assert.assertFalse(store.viewIndexes.containsKey(view));
    }

    @Test
    public void testIndexWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);

        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);

        TimeIndexImpl index = store.createViewIndex(graph);
        graph.addNode(n1);

        Assert.assertTrue(index.hasElements());
        Assert.assertSame(getArrayFromIterable(index.get(1.0))[0], n1);
    }

    @Test
    public void testClearWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);

        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        store.clear();
        Assert.assertFalse(index.hasElements());
    }

    @Test
    public void testRemoveWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);

        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        n1.removeTimestamp(1.0);
        Assert.assertFalse(index.hasElements());
    }

    @Test
    public void testClearElementWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);

        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        n1.clearAttributes();
        Assert.assertFalse(index.hasElements());
    }

    @Test
    public void testClearViewWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        n1.addTimestamp(1.0);

        TimeStore timestampStore = graphStore.timeStore;
        TimestampIndexStore store = (TimestampIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        view.clear();
        Assert.assertFalse(index.hasElements());
    }

    // UTILITY
    private <T> Object[] getArrayFromIterable(Iterable<T> iterable) {
        List<T> list = new ArrayList<T>();
        for (T t : iterable) {
            list.add(t);
        }
        return list.toArray();
    }
}
