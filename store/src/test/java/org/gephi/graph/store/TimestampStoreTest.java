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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampStoreTest {

    @Test
    public void testEmpty() {
        TimestampStore store = new TimestampStore(null);

        Assert.assertEquals(store.size(), 0);
    }

    @Test
    public void testAddTimestamp() {
        TimestampStore store = new TimestampStore(null);

        int pos = store.addTimestamp(1.0);
        Assert.assertEquals(pos, 0);
        int pos2 = store.addTimestamp(2.0);
        Assert.assertEquals(pos2, 1);

        Assert.assertEquals(store.size(), 2);
        Assert.assertEquals(pos, store.getTimestampIndex(1.0));
        Assert.assertEquals(pos2, store.getTimestampIndex(2.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddInfinityTimestamp() {
        TimestampStore store = new TimestampStore(null);
        store.addTimestamp(Double.POSITIVE_INFINITY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddNaNTimestamp() {
        TimestampStore store = new TimestampStore(null);
        store.addTimestamp(Double.NaN);
    }

    @Test
    public void testGetTimestampIndex() {
        TimestampStore store = new TimestampStore(null);

        int pos = store.getTimestampIndex(1.0);
        Assert.assertEquals(pos, 0);
        Assert.assertEquals(store.size(), 1);
    }

    @Test
    public void testContains() {
        TimestampStore store = new TimestampStore(null);

        store.addTimestamp(1.0);
        Assert.assertTrue(store.contains(1.0));
        Assert.assertFalse(store.contains(2.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsNaN() {
        TimestampStore store = new TimestampStore(null);
        store.contains(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsInfinity() {
        TimestampStore store = new TimestampStore(null);
        store.contains(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testRemoveTimestamp() {
        TimestampStore store = new TimestampStore(null);

        store.addTimestamp(1.0);
        store.removeTimestamp(1.0);

        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.contains(1.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveNaN() {
        TimestampStore store = new TimestampStore(null);
        store.removeTimestamp(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveInfinity() {
        TimestampStore store = new TimestampStore(null);
        store.removeTimestamp(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testGarbage() {
        TimestampStore store = new TimestampStore(null);

        store.addTimestamp(1.0);
        int pos = store.addTimestamp(2.0);
        store.addTimestamp(3.0);
        store.removeTimestamp(2.0);

        Assert.assertEquals(1, store.garbageQueue.size());
        Assert.assertEquals(pos, store.garbageQueue.firstInt());
        Assert.assertEquals(2, store.size());

        int pos2 = store.addTimestamp(6.0);

        Assert.assertEquals(pos, pos2);
        Assert.assertTrue(store.garbageQueue.isEmpty());
        Assert.assertEquals(3, store.size());
    }

    @Test
    public void testClear() {
        TimestampStore store = new TimestampStore(null);
        store.clear();

        store.addTimestamp(1.0);

        store.clear();

        Assert.assertEquals(0, store.size());
    }

    @Test
    public void testGetMin() {
        TimestampStore store = new TimestampStore(null);
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), Double.NEGATIVE_INFINITY);

        store.addTimestamp(1.0);
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 1.0);

        store.addTimestamp(2.0);
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 1.0);

        store.removeTimestamp(1.0);
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 2.0);
    }

    @Test
    public void testGetMax() {
        TimestampStore store = new TimestampStore(null);
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), Double.POSITIVE_INFINITY);

        store.addTimestamp(1.0);
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 1.0);

        store.addTimestamp(2.0);
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 2.0);

        store.removeTimestamp(2.0);
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 1.0);
    }

    @Test
    public void testAddElement() {
        TimestampStore store = new TimestampStore(null);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.addElement(1.0, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.getNodes(1.0))[0], nodeImpl);
    }

    @Test
    public void testRemoveElement() {
        TimestampStore store = new TimestampStore(null);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.addElement(1.0, nodeImpl);
        store.removeElement(1.0, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.mainIndex.getNodes(1.0)).length, 0);
    }

    @Test
    public void testGetElements() {
        TimestampStore store = new TimestampStore(null);

        NodeImpl n0 = new NodeImpl(0);
        NodeImpl n1 = new NodeImpl(1);
        NodeImpl n2 = new NodeImpl(2);
        NodeImpl n3 = new NodeImpl(3);

        store.addElement(1.0, n0);
        store.addElement(1.0, n1);
        store.addElement(2.0, n2);
        store.addElement(3.0, n3);

        ObjectSet r1 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(1.0, 1.0)));
        Assert.assertTrue(r1.contains(n0));
        Assert.assertTrue(r1.contains(n1));
        Assert.assertEquals(r1.size(), 2);

        ObjectSet r2 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(-1, 1.9)));
        Assert.assertTrue(r2.contains(n0));
        Assert.assertTrue(r2.contains(n1));
        Assert.assertEquals(r2.size(), 2);

        ObjectSet r3 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(-1, 2.0)));
        Assert.assertTrue(r3.contains(n0));
        Assert.assertTrue(r3.contains(n1));
        Assert.assertTrue(r3.contains(n2));
        Assert.assertEquals(r3.size(), 3);

        ObjectSet r4 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(2.0, 2.0)));
        Assert.assertTrue(r4.contains(n2));
        Assert.assertEquals(r4.size(), 1);

        ObjectSet r5 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(2.0, 3.5)));
        Assert.assertTrue(r5.contains(n2));
        Assert.assertTrue(r5.contains(n3));
        Assert.assertEquals(r5.size(), 2);
    }

    @Test
    public void testHasNodesEdgesEmpty() {
        TimestampStore store = new TimestampStore(null);
        Assert.assertFalse(store.mainIndex.hasNodes());
        Assert.assertFalse(store.mainIndex.hasEdges());
    }

    @Test
    public void testHasNodes() {
        TimestampStore store = new TimestampStore(null);
        Assert.assertFalse(store.mainIndex.hasNodes());

        NodeImpl nodeImpl = new NodeImpl(0);

        store.addElement(1.0, nodeImpl);
        store.addElement(2.0, nodeImpl);
        Assert.assertTrue(store.mainIndex.hasNodes());

        store.removeElement(1.0, nodeImpl);
        Assert.assertTrue(store.mainIndex.hasNodes());
        store.removeElement(2.0, nodeImpl);
        Assert.assertFalse(store.mainIndex.hasNodes());
    }

    @Test
    public void testHasNodesClear() {
        TimestampStore store = new TimestampStore(null);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.addElement(1.0, nodeImpl);
        store.clear();
        Assert.assertFalse(store.mainIndex.hasNodes());
    }

    @Test
    public void testIndexNode() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.size(), 2);

        store.index(nodeImpl);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(1.0, 2.0)));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testIndexNodeAdd() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.size(), 2);

        graphStore.addNode(nodeImpl);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(1.0, 2.0)));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testClearNode() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.size(), 2);

        store.index(nodeImpl);
        store.clear(nodeImpl);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(1.0, 2.0)));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }

    @Test
    public void testClearRemove() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.size(), 2);

        graphStore.addNode(nodeImpl);
        graphStore.removeNode(nodeImpl);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(1.0, 2.0)));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }

    @Test
    public void testAddAfterAdd() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        nodeImpl.addTimestamp(3.0);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(3.0, 3.0)));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }

    @Test
    public void testRemoveAfterAdd() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        nodeImpl.removeTimestamp(1.0);

        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(1.0, 1.0)));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);

        ObjectSet r2 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.getNodes(2.0, 2.0)));
        Assert.assertTrue(r2.contains(nodeImpl));
        Assert.assertEquals(r2.size(), 1);
    }

    @Test
    public void testRemoveTimestampNodes() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl) graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        graphStore.removeNode(nodeImpl);

        Assert.assertEquals(store.size(), 0);
    }

    @Test
    public void testEqualsEmpty() {
        TimestampStore store1 = new TimestampStore(null);
        Assert.assertEquals(store1, store1);

        TimestampStore store2 = new TimestampStore(null);
        Assert.assertEquals(store1, store2);
    }

    @Test
    public void testHashCodeEmpty() {
        TimestampStore store1 = new TimestampStore(null);
        Assert.assertEquals(store1, store1);

        TimestampStore store2 = new TimestampStore(null);
        Assert.assertEquals(store1.hashCode(), store2.hashCode());
    }

    @Test
    public void testEquals() {
        TimestampStore store1 = new TimestampStore(null);
        store1.addTimestamp(1.0);
        store1.addTimestamp(2.0);
        store1.addTimestamp(3.0);
        store1.removeTimestamp(1.0);

        TimestampStore store2 = new TimestampStore(null);
        store2.addTimestamp(1.0);
        store2.addTimestamp(2.0);
        store2.addTimestamp(3.0);
        store2.removeTimestamp(1.0);

        TimestampStore store3 = new TimestampStore(null);
        store3.addTimestamp(1.0);

        Assert.assertEquals(store1, store2);
        Assert.assertNotEquals(store1, store3);
    }

    @Test
    public void testHashCode() {
        TimestampStore store1 = new TimestampStore(null);
        store1.addTimestamp(1.0);
        store1.addTimestamp(2.0);
        store1.addTimestamp(3.0);
        store1.removeTimestamp(1.0);

        TimestampStore store2 = new TimestampStore(null);
        store2.addTimestamp(1.0);
        store2.addTimestamp(2.0);
        store2.addTimestamp(3.0);
        store2.removeTimestamp(1.0);

        TimestampStore store3 = new TimestampStore(null);
        store3.addTimestamp(1.0);

        Assert.assertEquals(store1.hashCode(), store2.hashCode());
        Assert.assertNotEquals(store1.hashCode(), store3.hashCode());
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
