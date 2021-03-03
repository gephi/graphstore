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
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalIntegerMap;
import org.gephi.graph.api.types.IntervalStringMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntervalIndexStoreTest {

    @Test
    public void testEmpty() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        Assert.assertTrue(store.size() == 0);
    }

    @Test
    public void testAddOne() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        int a = store.add(new Interval(1.0, 2.0));

        Assert.assertEquals(a, 0);
        Assert.assertTrue(store.contains(new Interval(1.0, 2.0)));
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(store.getMap().get(new Interval(1.0, 2.0)).intValue(), 0);
    }

    @Test
    public void testAddTwiceSame() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        int a = store.add(new Interval(1.0, 2.0));
        int b = store.add(new Interval(1.0, 2.0));

        Assert.assertEquals(a, b);
        Assert.assertTrue(store.contains(new Interval(1.0, 2.0)));
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(store.getMap().get(new Interval(1.0, 2.0)).intValue(), 0);
    }

    @Test
    public void testRemove() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        store.add(new Interval(1.0, 2.0));
        store.remove(new Interval(1.0, 2.0));

        Assert.assertTrue(store.size() == 0);
        Assert.assertFalse(store.contains(new Interval(1.0, 2.0)));
    }

    @Test
    public void testRemoveWithCount() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        store.add(new Interval(1.0, 2.0));
        store.add(new Interval(1.0, 2.0));
        store.remove(new Interval(1.0, 2.0));

        Assert.assertTrue(store.size() == 1);
        Assert.assertTrue(store.contains(new Interval(1.0, 2.0)));

        store.remove(new Interval(1.0, 2.0));

        Assert.assertTrue(store.size() == 0);
        Assert.assertFalse(store.contains(new Interval(1.0, 2.0)));
    }

    @Test
    public void testRemoveUnknown() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        store.remove(new Interval(1.0, 2.0));

        Assert.assertTrue(store.size() == 0);
        Assert.assertFalse(store.contains(new Interval(1.0, 2.0)));
    }

    @Test
    public void testNotContains() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        Assert.assertFalse(store.contains(new Interval(1.0, 2.0)));
    }

    @Test
    public void testAddAfterRemove() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        store.add(new Interval(1.0, 2.0));
        store.remove(new Interval(1.0, 2.0));
        int a = store.add(new Interval(3.0, 4.0));

        Assert.assertEquals(a, 0);
        Assert.assertTrue(store.contains(new Interval(3.0, 4.0)));
    }

    @Test
    public void testContains() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        Interval i = new Interval(1.0, 2.0);

        Assert.assertFalse(store.contains(i));

        store.add(i);

        Assert.assertTrue(store.contains(i));

        store.remove(i);

        Assert.assertFalse(store.contains(i));
    }

    @Test
    public void testGarbage() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);

        store.add(new Interval(1.0, 2.0));
        int pos = store.add(new Interval(3.0, 4.0));
        store.add(new Interval(3.0, 6.0));
        store.remove(new Interval(3.0, 4.0));

        Assert.assertEquals(1, store.garbageQueue.size());
        Assert.assertEquals(pos, store.garbageQueue.firstInt());
        Assert.assertEquals(2, store.size());

        int pos2 = store.add(new Interval(0.0, 4.0));

        Assert.assertEquals(pos, pos2);
        Assert.assertTrue(store.garbageQueue.isEmpty());
        Assert.assertEquals(3, store.size());
    }

    @Test
    public void testClear() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, false);
        store.clear();

        store.add(new Interval(3.0, 6.0));

        store.clear();

        Assert.assertEquals(0, store.size());
    }

    @Test
    public void testDeepEqualsEmpty() {
        IntervalIndexStore<Node> store1 = new IntervalIndexStore<Node>(Node.class, null, false);
        Assert.assertFalse(store1.deepEquals(null));
        Assert.assertTrue(store1.deepEquals(store1));

        IntervalIndexStore<Node> store2 = new IntervalIndexStore<Node>(Node.class, null, false);
        Assert.assertTrue(store1.deepEquals(store2));
    }

    @Test
    public void testDeepHashCodeEmpty() {
        IntervalIndexStore<Node> store1 = new IntervalIndexStore<Node>(Node.class, null, false);
        Assert.assertEquals(store1.deepHashCode(), store1.deepHashCode());

        IntervalIndexStore<Node> store2 = new IntervalIndexStore<Node>(Node.class, null, false);
        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
    }

    @Test
    public void testDeepEquals() {
        Interval i1 = new Interval(1.0, 2.0);
        Interval i2 = new Interval(3.0, 4.0);

        IntervalIndexStore<Node> store1 = new IntervalIndexStore<Node>(Node.class, null, false);
        store1.add(i1);
        store1.add(i2);
        store1.remove(i1);

        IntervalIndexStore<Node> store2 = new IntervalIndexStore<Node>(Node.class, null, false);
        store2.add(i1);
        store2.add(i2);
        store2.remove(i1);

        IntervalIndexStore<Node> store3 = new IntervalIndexStore<Node>(Node.class, null, false);
        store3.add(i1);

        IntervalIndexStore<Node> store4 = new IntervalIndexStore<Node>(Node.class, null, false);
        store4.add(i1);
        store4.add(i1);

        Assert.assertTrue(store1.deepEquals(store2));
        Assert.assertFalse(store1.deepEquals(store3));
        Assert.assertFalse(store3.deepEquals(store4));
    }

    @Test
    public void testDeepHashCode() {
        Interval i1 = new Interval(1.0, 2.0);
        Interval i2 = new Interval(3.0, 4.0);

        IntervalIndexStore<Node> store1 = new IntervalIndexStore<Node>(Node.class, null, false);
        store1.add(i1);
        store1.add(i2);
        store1.remove(i1);

        IntervalIndexStore<Node> store2 = new IntervalIndexStore<Node>(Node.class, null, false);
        store2.add(i1);
        store2.add(i2);
        store2.remove(i1);

        IntervalIndexStore<Node> store3 = new IntervalIndexStore<Node>(Node.class, null, false);
        store3.add(i1);

        IntervalIndexStore<Node> store4 = new IntervalIndexStore<Node>(Node.class, null, false);
        store4.add(i1);
        store4.add(i1);

        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
        Assert.assertNotEquals(store1.deepHashCode(), store3.deepHashCode());
        Assert.assertNotEquals(store3.deepHashCode(), store4.deepHashCode());
    }

    @Test
    public void testAddElement() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, true);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(new Interval(1.0, 2.0), nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.get(1.0))[0], nodeImpl);
    }

    @Test
    public void testRemoveElement() {
        IntervalIndexStore<Node> store = new IntervalIndexStore<Node>(Node.class, null, true);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(new Interval(1.0, 2.0), nodeImpl);
        store.remove(new Interval(1.0, 2.0), nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.get(1.0)).length, 0);
    }

    @Test
    public void testIndexNode() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphModel.store.factory.newNode("0");
        nodeImpl.addInterval(new Interval(1.0, 2.0));
        nodeImpl.addInterval(new Interval(3.0, 4.0));

        store.index(nodeImpl);
        Assert.assertEquals(store.size(), 2);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 2.0))));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testIndexNodeAdd() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphModel.store.factory.newNode("0");
        nodeImpl.addInterval(new Interval(1.0, 2.0));
        nodeImpl.addInterval(new Interval(3.0, 4.0));

        graphModel.store.addNode(nodeImpl);
        Assert.assertEquals(store.size(), 2);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 2.0))));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testClearNode() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphModel.store.factory.newNode("0");
        nodeImpl.addInterval(new Interval(1.0, 2.0));
        nodeImpl.addInterval(new Interval(3.0, 4.0));

        store.index(nodeImpl);
        store.clear(nodeImpl);

        Assert.assertEquals(store.size(), 0);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 2.0))));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }

    @Test
    public void testClearNodeWithAttributes() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        Column col = graphModel.store.nodeTable.addColumn("foo", IntervalStringMap.class);
        NodeImpl nodeImpl = (NodeImpl) graphModel.store.factory.newNode("0");
        nodeImpl.setAttribute(col, "bar", new Interval(1.0, 2.0));

        store.index(nodeImpl);
        store.clear(nodeImpl);

        Assert.assertEquals(store.size(), 0);
    }

    @Test
    public void testClearRemove() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphModel.store.factory.newNode("0");
        nodeImpl.addInterval(new Interval(1.0, 2.0));

        graphModel.store.addNode(nodeImpl);
        graphModel.store.removeNode(nodeImpl);

        Assert.assertEquals(store.size(), 0);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 2.0))));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }

    @Test
    public void testAddAfterAdd() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphModel.store.factory.newNode("0");
        nodeImpl.addInterval(new Interval(1.0, 2.0));

        graphModel.store.addNode(nodeImpl);
        nodeImpl.addInterval(new Interval(3.0, 4.0));

        Assert.assertEquals(store.size(), 2);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(3.0, 4.0))));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testRemoveAfterAdd() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphModel.store.factory.newNode("0");
        nodeImpl.addInterval(new Interval(1.0, 2.0));
        nodeImpl.addInterval(new Interval(3.0, 4.0));

        graphModel.store.addNode(nodeImpl);
        nodeImpl.removeInterval(new Interval(1.0, 2.0));

        Assert.assertEquals(store.size(), 1);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(1.0, 1.0))));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);

        ObjectSet r2 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(3.0, 3.0))));
        Assert.assertTrue(r2.contains(nodeImpl));
        Assert.assertEquals(r2.size(), 1);
    }

    @Test
    public void testSetAttribute() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        Column col = graphModel.store.nodeTable.addColumn("col", IntervalStringMap.class);
        NodeImpl nodeImpl = (NodeImpl) graphModel.store.factory.newNode("0");
        graphModel.store.addNode(nodeImpl);

        IntervalStringMap s1 = new IntervalStringMap();
        s1.put(new Interval(1.0, 2.0), "foo");
        s1.put(new Interval(3.0, 4.0), "bar");

        nodeImpl.setAttribute(col, s1);

        Assert.assertEquals(store.size(), 2);
        Assert.assertTrue(store.contains(new Interval(1.0, 2.0)));
        Assert.assertTrue(store.contains(new Interval(3.0, 4.0)));

        nodeImpl.setAttribute(col, new IntervalStringMap());
        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.contains(new Interval(1.0, 2.0)));
        Assert.assertFalse(store.contains(new Interval(3.0, 4.0)));
    }

    @Test
    public void testRemoveAttributeTimestamp() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphModel.store.factory.newNode("0");
        graphModel.store.addNode(nodeImpl);
        nodeImpl.addInterval(new Interval(1.0, 2.0));
        Assert.assertTrue(store.contains(new Interval(1.0, 2.0)));

        nodeImpl.removeAttribute(graphModel.store.nodeTable.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX));
        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.contains(new Interval(1.0, 2.0)));
    }

    @Test
    public void testAddWithAttribute() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphStore graphStore = new GraphModelImpl(config).store;
        TimeStore timestampStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode("0");
        graphStore.addNode(nodeImpl);
        nodeImpl.addInterval(new Interval(1.0, 2.0));

        Column col = graphStore.nodeTable.addColumn("0", IntervalIntegerMap.class);
        nodeImpl.setAttribute(col, 42, new Interval(3.0, 4.0));
        Assert.assertTrue(store.contains(new Interval(3.0, 4.0)));

        Object[] nodes = getArrayFromIterable(store.getIndex(graphStore).get(new Interval(1.0, 4.0)));
        Assert.assertEquals(nodes.length, 1);
        Assert.assertSame(nodes[0], nodeImpl);
    }

    @Test
    public void testCreateView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        NodeImpl n1 = graphStore.getNode("1");
        n1.addInterval(new Interval(1.0, 2.0));

        TimeStore timestampStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        GraphView view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        TimeIndexImpl index = store.createViewIndex(graph);
        Assert.assertSame(store.getIndex(graph), index);
        Assert.assertFalse(index.hasElements());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateViewMainView() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;
        store.createViewIndex(graphModel.store);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDeleteViewMainView() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timestampStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;
        store.deleteViewIndex(graphModel.store);
    }

    @Test
    public void testCreateViewWithElements() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        NodeImpl n1 = graphStore.getNode("1");
        n1.addInterval(new Interval(1.0, 2.0));

        TimeStore timestampStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        Assert.assertTrue(index.hasElements());
    }

    @Test
    public void testDeleteViewWithElements() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        NodeImpl n1 = graphStore.getNode("1");
        n1.addInterval(new Interval(1.0, 2.0));

        TimeStore timestampStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

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
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        NodeImpl n1 = graphStore.getNode("1");
        n1.addInterval(new Interval(1.0, 2.0));

        TimeStore timestampStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);

        TimeIndexImpl index = store.createViewIndex(graph);
        graph.addNode(n1);

        Assert.assertTrue(index.hasElements());
        Assert.assertSame(getArrayFromIterable(index.get(1.0))[0], n1);
    }

    @Test
    public void testClearWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        NodeImpl n1 = graphStore.getNode("1");
        n1.addInterval(new Interval(1.0, 2.0));

        TimeStore timestampStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        store.clear();
        Assert.assertFalse(index.hasElements());
    }

    @Test
    public void testRemoveWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        NodeImpl n1 = graphStore.getNode("1");
        n1.addInterval(new Interval(1.0, 2.0));

        TimeStore timestampStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        n1.removeInterval(new Interval(1.0, 2.0));
        Assert.assertFalse(index.hasElements());
    }

    @Test
    public void testClearElementWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        NodeImpl n1 = graphStore.getNode("1");
        n1.addInterval(new Interval(1.0, 2.0));

        TimeStore timestampStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);
        n1.clearAttributes();
        Assert.assertFalse(index.hasElements());
    }

    @Test
    public void testClearViewWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        NodeImpl n1 = graphStore.getNode("1");
        n1.addInterval(new Interval(1.0, 2.0));

        TimeStore timestampStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timestampStore.nodeIndexStore;

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
