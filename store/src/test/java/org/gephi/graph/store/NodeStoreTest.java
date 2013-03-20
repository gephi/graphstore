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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class NodeStoreTest {

    public NodeStoreTest() {
    }

    @Test
    public void testDefaultSize() {
        NodeStore nodeStore = new NodeStore();
        int size = nodeStore.size();
        boolean isEmpty = nodeStore.isEmpty();

        Assert.assertEquals(isEmpty, true);
        Assert.assertEquals(size, 0);
    }

    @Test
    public void testSize() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl n1 = new NodeImpl("0");
        NodeImpl n2 = new NodeImpl("1");
        nodeStore.add(n1);
        nodeStore.add(n2);
        Assert.assertEquals(nodeStore.size(), 2);
        nodeStore.remove(n1);
        Assert.assertEquals(nodeStore.size(), 1);
        nodeStore.remove(n2);
        Assert.assertEquals(nodeStore.size(), 0);
        Assert.assertTrue(nodeStore.isEmpty());
    }

    @Test
    public void testAdd() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("0");
        boolean a = nodeStore.add(node);
        boolean b = nodeStore.add(node);

        Assert.assertEquals(a, true);
        Assert.assertEquals(b, false);

        Assert.assertEquals(nodeStore.isEmpty(), false);
        Assert.assertEquals(nodeStore.size(), 1);

        Assert.assertTrue(nodeStore.contains(node));
        Assert.assertNotSame(node.getStoreId(), NodeStore.NULL_ID);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddNull() {
        NodeStore nodeStore = new NodeStore();
        nodeStore.add(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddOtherStore() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("0");
        nodeStore.add(node);

        NodeStore nodeStore2 = new NodeStore();
        nodeStore2.add(node);
    }

    @Test
    public void testGet() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("0");
        nodeStore.add(node);

        Assert.assertEquals(nodeStore.get(0), node);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetInvalid() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("0");
        nodeStore.add(node);
        nodeStore.get(1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetNegative() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("0");
        nodeStore.add(node);
        nodeStore.get(-1);
    }

    @Test
    public void testGetMultiBlock() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = GraphGenerator.generateLargeNodeList();

        nodeStore.addAll(Arrays.asList(nodes));
        NodeImpl firstNode = nodeStore.get(0);
        NodeImpl middleNode = nodeStore.get(nodes.length / 2);
        NodeImpl lastNode = nodeStore.get(nodes.length - 1);

        Assert.assertEquals(firstNode, nodes[0]);
        Assert.assertEquals(middleNode, nodes[nodes.length / 2]);
        Assert.assertEquals(lastNode, nodes[nodes.length - 1]);
    }

    @Test
    public void testClear() {
        NodeStore nodeStore = new NodeStore();
        nodeStore.clear();

        NodeImpl node = new NodeImpl("0");
        nodeStore.add(node);
        nodeStore.clear();

        Assert.assertTrue(nodeStore.isEmpty());
        Assert.assertEquals(nodeStore.size(), 0);
        Assert.assertFalse(nodeStore.contains(node));

        Assert.assertEquals(node.getStoreId(), NodeStore.NULL_ID);
    }

    @Test
    public void testRemove() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("0");

        nodeStore.add(node);
        boolean a = nodeStore.remove(node);
        boolean b = nodeStore.remove(node);

        Assert.assertEquals(a, true);
        Assert.assertEquals(b, false);

        Assert.assertEquals(nodeStore.isEmpty(), true);
        Assert.assertEquals(nodeStore.size(), 0);

        Assert.assertFalse(nodeStore.contains(node));
        Assert.assertSame(node.getStoreId(), NodeStore.NULL_ID);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRemoveNull() {
        NodeStore nodeStore = new NodeStore();
        nodeStore.remove(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveOtherStore() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("0");

        nodeStore.add(node);

        NodeStore nodeStore2 = new NodeStore();
        NodeImpl node2 = new NodeImpl("0");

        nodeStore2.add(node2);

        nodeStore.remove(node2);
    }

    @Test
    public void testContains() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("0");
        nodeStore.add(node);
        Assert.assertTrue(nodeStore.contains(node));

        Assert.assertFalse(nodeStore.contains(new NodeImpl("0")));
        Assert.assertFalse(nodeStore.contains(new NodeImpl("2")));
    }

    @Test
    public void testContainsId() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("0");
        nodeStore.add(node);
        Assert.assertTrue(nodeStore.containsId("0"));

        Assert.assertFalse(nodeStore.containsId("2"));
    }

    @Test
    public void testAddAll() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = GraphGenerator.generateLargeNodeList();

        boolean a = nodeStore.addAll(Arrays.asList(nodes));

        Assert.assertEquals(nodeStore.size(), nodes.length);
        Assert.assertTrue(a);
        testContainsOnly(nodeStore, Arrays.asList(nodes));

        boolean b = nodeStore.addAll(Arrays.asList(nodes));
        Assert.assertFalse(b);

        boolean c = nodeStore.addAll(new ArrayList<Node>());
        Assert.assertFalse(c);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddAllSelf() {
        NodeStore nodeStore = new NodeStore();
        nodeStore.addAll(nodeStore);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddAllNull() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), null};
        nodeStore.addAll(Arrays.asList(nodes));
    }

    @Test
    public void testRemoveAll() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        nodeStore.addAll(Arrays.asList(nodes));

        boolean a = nodeStore.removeAll(new ArrayList<Node>());
        Assert.assertFalse(a);

        boolean b = nodeStore.removeAll(Arrays.asList(nodes));
        Assert.assertTrue(b);
        Assert.assertTrue(nodeStore.isEmpty());

        testContainsNone(nodeStore, Arrays.asList(nodes));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRemoveAllNull() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1")};
        nodeStore.addAll(Arrays.asList(nodes));
        nodes[0] = null;
        nodeStore.removeAll(Arrays.asList(nodes));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveAllSelf() {
        NodeStore nodeStore = new NodeStore();
        nodeStore.removeAll(nodeStore);
    }

    @Test
    public void testRetainAll() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        nodeStore.addAll(Arrays.asList(nodes));

        NodeImpl[] r = new NodeImpl[]{nodes[0]};
        boolean a = nodeStore.retainAll(Arrays.asList(r));
        boolean b = nodeStore.retainAll(Arrays.asList(r));

        Assert.assertTrue(a);
        Assert.assertFalse(b);

        Assert.assertEquals(nodeStore.size(), 1);
        Assert.assertTrue(nodeStore.contains(nodes[0]));

        nodeStore.retainAll(new ArrayList());
        Assert.assertTrue(nodeStore.isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRetainAllNull() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        nodeStore.addAll(Arrays.asList(nodes));
        nodes[0] = null;
        nodeStore.retainAll(Arrays.asList(nodes));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRetainAllSelf() {
        NodeStore nodeStore = new NodeStore();
        nodeStore.retainAll(nodeStore);
    }

    @Test
    public void testContainsAll() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        nodeStore.addAll(Arrays.asList(nodes));
        Assert.assertTrue(nodeStore.containsAll(Arrays.asList(nodes)));
    }

    @Test
    public void testIterator() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        nodeStore.addAll(Arrays.asList(nodes));

        NodeStore.NodeStoreIterator itr = nodeStore.iterator();
        int index = 0;
        while (itr.hasNext()) {
            NodeImpl n = itr.next();
            Assert.assertSame(n, nodes[index++]);
        }
        Assert.assertEquals(index, nodes.length);
    }

    @Test
    public void testIteratorRemove() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        nodeStore.addAll(Arrays.asList(nodes));

        NodeStore.NodeStoreIterator itr = nodeStore.iterator();
        int index = 0;
        while (itr.hasNext()) {
            NodeImpl n = itr.next();
            itr.remove();
            Assert.assertEquals(nodeStore.size(), nodes.length - ++index);
        }
        Assert.assertEquals(index, nodes.length);
        testContainsNone(nodeStore, Arrays.asList(nodes));
    }

    @Test
    public void testIteratorEmpty() {
        NodeStore nodeStore = new NodeStore();
        NodeStore.NodeStoreIterator itr = nodeStore.iterator();
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testIteratorAfterRemove() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        nodeStore.addAll(Arrays.asList(nodes));
        nodeStore.remove(nodes[1]);
        NodeStore.NodeStoreIterator itr = nodeStore.iterator();

        int index = 0;
        while (itr.hasNext()) {
            NodeImpl n = itr.next();
            Assert.assertTrue(nodeStore.contains(n));
            index++;
        }
        Assert.assertEquals(index, nodes.length - 1);

    }

    @Test
    public void testEqualsAndHashCode() {
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        NodeImpl[] nodes2 = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        NodeImpl[] nodes3 = new NodeImpl[]{new NodeImpl("1"), new NodeImpl("0"), new NodeImpl("2")};

        NodeStore nodeStore1 = new NodeStore();
        NodeStore nodeStore2 = new NodeStore();

        Assert.assertEquals(nodeStore1, nodeStore2);
        Assert.assertEquals(nodeStore1.hashCode(), nodeStore2.hashCode());

        nodeStore1.addAll(Arrays.asList(nodes));
        nodeStore2.addAll(Arrays.asList(nodes2));

        Assert.assertEquals(nodeStore1, nodeStore2);
        Assert.assertEquals(nodeStore1.hashCode(), nodeStore2.hashCode());

        NodeStore nodeStore3 = new NodeStore();
        nodeStore3.addAll(Arrays.asList(nodes3));

        Assert.assertNotEquals(nodeStore1, nodeStore3);
        Assert.assertNotEquals(nodeStore1.hashCode(), nodeStore3.hashCode());
    }

    @Test
    public void testToArray() {
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        NodeStore nodeStore = new NodeStore();
        Assert.assertEquals(new NodeImpl[0], nodeStore.toArray());
        nodeStore.addAll(Arrays.asList(nodes));
        Assert.assertEquals(nodes, nodeStore.toArray());
        Assert.assertEquals(nodes, nodeStore.toArray(new Node[0]));

        nodeStore.clear();
        Assert.assertEquals(new NodeImpl[0], nodeStore.toArray());
    }

    @Test
    public void testToArrayAfterRemove() {
        NodeImpl[] nodes = new NodeImpl[]{new NodeImpl("0"), new NodeImpl("1"), new NodeImpl("2")};
        NodeStore nodeStore = new NodeStore();

        nodeStore.addAll(Arrays.asList(nodes));
        nodeStore.remove(nodes[0]);

        Assert.assertEquals(nodeStore.toArray(), new NodeImpl[]{nodes[1], nodes[2]});
    }

    @Test
    public void testRemoveAdd() {
        NodeImpl[] nodes = GraphGenerator.generateSmallNodeList();
        NodeStore nodeStore = new NodeStore();
        nodeStore.addAll(Arrays.asList(nodes));

        removeAndReAddSameNodes(nodeStore);

        Assert.assertEquals(nodeStore.size(), nodes.length);
        Assert.assertEquals(nodeStore.toArray(), nodes);
    }

    @Test
    public void testGarbageSize() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = GraphGenerator.generateLargeNodeList();
        nodeStore.addAll(Arrays.asList(nodes));

        Assert.assertEquals(nodeStore.garbageSize, 0);

        removeSomeNodes(nodeStore, 0.5f);
        Assert.assertEquals(nodeStore.garbageSize, (int) (nodes.length * 0.5f));

        nodeStore.clear();
        Assert.assertEquals(nodeStore.garbageSize, 0);
    }

    @Test
    public void testBlockCounts() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = GraphGenerator.generateLargeNodeList();
        nodeStore.addAll(Arrays.asList(nodes));
        int blockCount = nodeStore.blocksCount;

        for (int i = 0; i < GraphStoreConfiguration.NODESTORE_BLOCK_SIZE; i++) {
            nodeStore.remove(nodes[nodes.length - 1 - i]);
        }

        Assert.assertEquals(nodeStore.blocksCount, blockCount - 1);
    }

    @Test
    public void testBlockCountsEmpty() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl[] nodes = GraphGenerator.generateLargeNodeList();
        nodeStore.addAll(Arrays.asList(nodes));
        nodeStore.removeAll(Arrays.asList(nodes));

        Assert.assertEquals(nodeStore.blocksCount, 1);
        Assert.assertEquals(nodeStore.blocks[0], nodeStore.currentBlock);
        Assert.assertEquals(nodeStore.currentBlockIndex, 0);
        Assert.assertEquals(nodeStore.size, 0);
        Assert.assertEquals(nodeStore.garbageSize, 0);
    }

    @Test
    public void testDictionary() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("1");
        nodeStore.add(node);

        Assert.assertTrue(nodeStore.contains(node));
        Assert.assertEquals(nodeStore.get("1"), node);
        Assert.assertNull(nodeStore.get("0"));
        Assert.assertFalse(nodeStore.contains(new NodeImpl("0")));

        nodeStore.remove(node);
        Assert.assertFalse(nodeStore.contains(node));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDictionaryDuplicate() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl node = new NodeImpl("1");
        nodeStore.add(node);
        NodeImpl node2 = new NodeImpl("1");
        nodeStore.add(node2);
    }

    private void testContainsOnly(NodeStore store, List<NodeImpl> list) {
        for (NodeImpl n : list) {
            Assert.assertTrue(store.contains(n));
            Assert.assertFalse(n.getStoreId() == NodeStore.NULL_ID);
        }
        Assert.assertEquals(store.size(), list.size());

        Set<Node> set = new HashSet<Node>(list);
        for (Node n : store) {
            Assert.assertTrue(set.remove(n));
        }
        Assert.assertTrue(set.isEmpty());
    }

    private void testContainsNone(NodeStore store, List<NodeImpl> list) {
        for (NodeImpl n : list) {
            Assert.assertFalse(store.contains(n));
        }
    }

    private void removeAndReAddSameNodes(NodeStore store) {
        List<NodeImpl> nodes = removeSomeNodes(store);
        Collections.reverse(nodes);
        for (Node node : nodes) {
            store.add(node);
        }
    }

    private List<NodeImpl> removeSomeNodes(NodeStore store) {
        return removeSomeNodes(store, 0.3f);
    }

    private List<NodeImpl> removeSomeNodes(NodeStore store, float ratio) {
        int size = store.size;
        int s = (int) (size * ratio);
        int[] randomIndexes = generateRandomUniqueInts(s, size);
        List<NodeImpl> nodes = new ArrayList<NodeImpl>(s);
        for (int index : randomIndexes) {
            NodeImpl node = store.get(index);
            if (store.remove(node)) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    private int[] generateRandomUniqueInts(int count, int bound) {
        Random rand = new Random(123);
        IntSet set = new IntOpenHashSet();
        while (set.size() < count) {
            int number = rand.nextInt(bound);
            if (!set.contains(number)) {
                set.add(number);
            }
        }
        return set.toIntArray();
    }
}
