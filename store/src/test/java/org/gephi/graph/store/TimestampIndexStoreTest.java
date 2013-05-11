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
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampIndexStoreTest {

    @Test
    public void testGetMin() {
        TimestampStore timestampStore = new TimestampStore(null, null);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), Double.NEGATIVE_INFINITY);

        store.timestampMap.addTimestamp(1.0);
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 1.0);

        store.timestampMap.addTimestamp(2.0);
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 1.0);

        store.timestampMap.removeTimestamp(1.0);
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 2.0);
    }

    @Test
    public void testGetMax() {
        TimestampStore timestampStore = new TimestampStore(null, null);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), Double.POSITIVE_INFINITY);

        store.timestampMap.addTimestamp(1.0);
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 1.0);

        store.timestampMap.addTimestamp(2.0);
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 2.0);

        store.timestampMap.removeTimestamp(2.0);
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 1.0);
    }

    @Test
    public void testAddElement() {
        TimestampStore timestampStore = new TimestampStore(null, null);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(1.0, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.get(1.0))[0], nodeImpl);
    }

    @Test
    public void testRemoveElement() {
        TimestampStore timestampStore = new TimestampStore(null, null);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(1.0, nodeImpl);
        store.remove(1.0, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.get(1.0)).length, 0);
    }

    @Test
    public void testGetElements() {
        TimestampStore timestampStore = new TimestampStore(null, null);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl n0 = new NodeImpl(0);
        NodeImpl n1 = new NodeImpl(1);
        NodeImpl n2 = new NodeImpl(2);
        NodeImpl n3 = new NodeImpl(3);

        store.add(1.0, n0);
        store.add(1.0, n1);
        store.add(2.0, n2);
        store.add(3.0, n3);

        ObjectSet r1 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(1.0, 1.0)));
        Assert.assertTrue(r1.contains(n0));
        Assert.assertTrue(r1.contains(n1));
        Assert.assertEquals(r1.size(), 2);

        ObjectSet r2 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(-1, 1.9)));
        Assert.assertTrue(r2.contains(n0));
        Assert.assertTrue(r2.contains(n1));
        Assert.assertEquals(r2.size(), 2);

        ObjectSet r3 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(-1, 2.0)));
        Assert.assertTrue(r3.contains(n0));
        Assert.assertTrue(r3.contains(n1));
        Assert.assertTrue(r3.contains(n2));
        Assert.assertEquals(r3.size(), 3);

        ObjectSet r4 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(2.0, 2.0)));
        Assert.assertTrue(r4.contains(n2));
        Assert.assertEquals(r4.size(), 1);

        ObjectSet r5 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(2.0, 3.5)));
        Assert.assertTrue(r5.contains(n2));
        Assert.assertTrue(r5.contains(n3));
        Assert.assertEquals(r5.size(), 2);
    }

    @Test
    public void testHasNodesEdgesEmpty() {
        TimestampStore timestampStore = new TimestampStore(null, null);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;
        Assert.assertFalse(store.mainIndex.hasElements());
    }

    @Test
    public void testHasNodes() {
        TimestampStore timestampStore = new TimestampStore(null, null);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;
        Assert.assertFalse(store.mainIndex.hasElements());

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(1.0, nodeImpl);
        store.add(2.0, nodeImpl);
        Assert.assertTrue(store.mainIndex.hasElements());

        store.remove(1.0, nodeImpl);
        Assert.assertTrue(store.mainIndex.hasElements());
        store.remove(2.0, nodeImpl);
        Assert.assertFalse(store.mainIndex.hasElements());
    }

    @Test
    public void testHasNodesClear() {
        TimestampStore timestampStore = new TimestampStore(null, null);
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(1.0, nodeImpl);
        store.clear();
        Assert.assertFalse(store.mainIndex.hasElements());
    }

    @Test
    public void testIndexNode() {
        GraphStore graphStore = new GraphStore();
        TimestampStore timestampStore = graphStore.timestampStore;
        TimestampIndexStore<Node> store = timestampStore.nodeIndexStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
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

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
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

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
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

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
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

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
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

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
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

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        graphStore.removeNode(nodeImpl);

        Assert.assertEquals(store.timestampMap.size(), 0);
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
