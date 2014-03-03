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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class EdgeStoreTest {

    public EdgeStoreTest() {
    }

    @Test
    public void testDefaultSize() {
        EdgeStore edgeStore = new EdgeStore();
        int size = edgeStore.size();
        boolean isEmpty = edgeStore.isEmpty();

        Assert.assertEquals(isEmpty, true);
        Assert.assertEquals(size, 0);
    }

    @Test
    public void testSize() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(2);
        EdgeImpl e1 = edges[0];
        EdgeImpl e2 = edges[1];
        edgeStore.add(e1);
        edgeStore.add(e2);
        Assert.assertEquals(edgeStore.size(), 2);
        Assert.assertEquals(edgeStore.size(0), 2);
        edgeStore.remove(e1);
        Assert.assertEquals(edgeStore.size(), 1);
        edgeStore.remove(e2);
        Assert.assertEquals(edgeStore.size(), 0);
        Assert.assertTrue(edgeStore.isEmpty());
    }

    @Test
    public void testAdd() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        boolean a = edgeStore.add(edge);
        boolean b = edgeStore.add(edge);

        Assert.assertEquals(a, true);
        Assert.assertEquals(b, false);

        Assert.assertEquals(edgeStore.isEmpty(), false);
        Assert.assertEquals(edgeStore.size(), 1);

        Assert.assertTrue(edgeStore.contains(edge));
        Assert.assertNotSame(edge.getStoreId(), EdgeStore.NULL_ID);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddNull() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.add(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddOtherStore() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);

        EdgeStore edgeStore2 = new EdgeStore();
        edgeStore2.add(edge);
    }

    @Test
    public void testGet() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);

        Assert.assertEquals(edgeStore.get(0), edge);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetInvalid() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);
        edgeStore.get(1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetNegative() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);
        edgeStore.get(-1);
    }

    @Test
    public void testGetMultiBlock() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateLargeEdgeList();

        edgeStore.addAll(Arrays.asList(edges));
        EdgeImpl firstEdge = edgeStore.get(0);
        EdgeImpl middleEdge = edgeStore.get(edges.length / 2);
        EdgeImpl lastEdge = edgeStore.get(edges.length - 1);

        Assert.assertEquals(firstEdge, edges[0]);
        Assert.assertEquals(middleEdge, edges[edges.length / 2]);
        Assert.assertEquals(lastEdge, edges[edges.length - 1]);
    }

    @Test
    public void testClear() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.clear();

        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);
        edgeStore.clear();

        Assert.assertTrue(edgeStore.isEmpty());
        Assert.assertEquals(edgeStore.size(), 0);
        Assert.assertFalse(edgeStore.contains(edge));

        Assert.assertEquals(edge.getStoreId(), EdgeStore.NULL_ID);
    }

    @Test
    public void testRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();

        edgeStore.add(edge);
        boolean a = edgeStore.remove(edge);
        boolean b = edgeStore.remove(edge);

        Assert.assertEquals(a, true);
        Assert.assertEquals(b, false);

        Assert.assertEquals(edgeStore.isEmpty(), true);
        Assert.assertEquals(edgeStore.size(), 0);

        Assert.assertFalse(edgeStore.contains(edge));
        Assert.assertSame(edge.getStoreId(), EdgeStore.NULL_ID);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRemoveNull() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.remove(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveOtherStore() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();

        edgeStore.add(edge);

        EdgeStore edgeStore2 = new EdgeStore();
        EdgeImpl edge2 = GraphGenerator.generateSingleEdge();

        edgeStore2.add(edge2);

        edgeStore.remove(edge2);
    }

    @Test
    public void testContains() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);
        Assert.assertTrue(edgeStore.contains(edge));

        Assert.assertFalse(edgeStore.contains(GraphGenerator.generateSingleEdge()));
    }

    @Test
    public void testContainsId() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);
        Assert.assertTrue(edgeStore.containsId("0"));

        Assert.assertFalse(edgeStore.containsId("2"));
    }

    @Test
    public void testContainsBySourceTarget() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);
        Assert.assertTrue(edgeStore.contains(edge.getSource(), edge.getTarget(), edge.getType()));
        Assert.assertFalse(edgeStore.contains(edge.getTarget(), edge.getSource(), edge.getType()));
        Assert.assertFalse(edgeStore.contains(edge.getSource(), edge.getTarget(), 10));
    }

    @Test
    public void testGetBySourceTarget() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);
        Assert.assertEquals(edgeStore.get(edge.getSource(), edge.getTarget(), edge.getType()), edge);
    }

    @Test
    public void testAddAll() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateLargeEdgeList();

        boolean a = edgeStore.addAll(Arrays.asList(edges));

        Assert.assertEquals(edgeStore.size(), edges.length);
        Assert.assertTrue(a);
        testContainsOnly(edgeStore, Arrays.asList(edges));

        boolean b = edgeStore.addAll(Arrays.asList(edges));
        Assert.assertFalse(b);

        boolean c = edgeStore.addAll(new ArrayList<Edge>());
        Assert.assertFalse(c);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddAllSelf() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(edgeStore);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddAllNull() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        edges[1] = null;
        edgeStore.addAll(Arrays.asList(edges));
    }

    @Test
    public void testRemoveAll() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        edgeStore.addAll(Arrays.asList(edges));

        boolean a = edgeStore.removeAll(new ArrayList<Edge>());
        Assert.assertFalse(a);

        boolean b = edgeStore.removeAll(Arrays.asList(edges));
        Assert.assertTrue(b);
        Assert.assertTrue(edgeStore.isEmpty());

        testContainsNone(edgeStore, Arrays.asList(edges));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRemoveAllNull() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        edgeStore.addAll(Arrays.asList(edges));
        edges[0] = null;
        edgeStore.removeAll(Arrays.asList(edges));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveAllSelf() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.removeAll(edgeStore);
    }

    @Test
    public void testRetainAll() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        edgeStore.addAll(Arrays.asList(edges));

        EdgeImpl[] r = new EdgeImpl[]{edges[0]};
        boolean a = edgeStore.retainAll(Arrays.asList(r));
        boolean b = edgeStore.retainAll(Arrays.asList(r));

        Assert.assertTrue(a);
        Assert.assertFalse(b);

        Assert.assertEquals(edgeStore.size(), 1);
        Assert.assertTrue(edgeStore.contains(edges[0]));

        edgeStore.retainAll(new ArrayList());
        Assert.assertTrue(edgeStore.isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRetainAllNull() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        edgeStore.addAll(Arrays.asList(edges));
        edges[0] = null;
        edgeStore.retainAll(Arrays.asList(edges));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRetainAllSelf() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.retainAll(edgeStore);
    }

    @Test
    public void testContainsAll() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        edgeStore.addAll(Arrays.asList(edges));
        Assert.assertTrue(edgeStore.containsAll(Arrays.asList(edges)));
    }

    @Test
    public void testIterator() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        edgeStore.addAll(Arrays.asList(edges));

        EdgeStore.EdgeStoreIterator itr = edgeStore.iterator();
        int index = 0;
        while (itr.hasNext()) {
            EdgeImpl n = itr.next();
            Assert.assertSame(n, edges[index++]);
        }
        Assert.assertEquals(index, edges.length);
    }

    @Test
    public void testIteratorRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        edgeStore.addAll(Arrays.asList(edges));

        EdgeStore.EdgeStoreIterator itr = edgeStore.iterator();
        int index = 0;
        while (itr.hasNext()) {
            EdgeImpl n = itr.next();
            itr.remove();
            Assert.assertEquals(edgeStore.size(), edges.length - ++index);
        }
        Assert.assertEquals(index, edges.length);
        testContainsNone(edgeStore, Arrays.asList(edges));
    }

    @Test
    public void testIteratorEmpty() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeStore.EdgeStoreIterator itr = edgeStore.iterator();
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testIteratorAfterRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        edgeStore.addAll(Arrays.asList(edges));
        edgeStore.remove(edges[1]);
        EdgeStore.EdgeStoreIterator itr = edgeStore.iterator();

        int index = 0;
        while (itr.hasNext()) {
            EdgeImpl n = itr.next();
            Assert.assertTrue(edgeStore.contains(n));
            index++;
        }
        Assert.assertEquals(index, edges.length - 1);
    }

    @Test
    public void testEqualsAndHashCode() {
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        EdgeImpl[] edges2 = GraphGenerator.generateEdgeList(3);
        EdgeImpl[] edges3 = GraphGenerator.generateEdgeList(3);
        EdgeImpl t = edges3[0];
        edges3[0] = edges3[1];
        edges3[1] = t;

        EdgeStore edgeStore1 = new EdgeStore();
        EdgeStore edgeStore2 = new EdgeStore();

        Assert.assertEquals(edgeStore1, edgeStore2);
        Assert.assertEquals(edgeStore1.hashCode(), edgeStore2.hashCode());

        edgeStore1.addAll(Arrays.asList(edges));
        edgeStore2.addAll(Arrays.asList(edges2));

        Assert.assertEquals(edgeStore1, edgeStore2);
        Assert.assertEquals(edgeStore1.hashCode(), edgeStore2.hashCode());

        EdgeStore edgeStore3 = new EdgeStore();
        edgeStore3.addAll(Arrays.asList(edges3));

        Assert.assertNotEquals(edgeStore1, edgeStore3);
        Assert.assertNotEquals(edgeStore1.hashCode(), edgeStore3.hashCode());
    }

    @Test
    public void testToArray() {
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        EdgeStore edgeStore = new EdgeStore();
        Assert.assertEquals(new EdgeImpl[0], edgeStore.toArray());
        edgeStore.addAll(Arrays.asList(edges));
        Assert.assertEquals(edges, edgeStore.toArray());
        Assert.assertEquals(edges, edgeStore.toArray(new Edge[0]));

        edgeStore.clear();
        Assert.assertEquals(new EdgeImpl[0], edgeStore.toArray());
    }

    @Test
    public void testToArrayAfterRemove() {
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(3);
        EdgeStore edgeStore = new EdgeStore();

        edgeStore.addAll(Arrays.asList(edges));
        edgeStore.remove(edges[0]);

        Assert.assertEquals(edgeStore.toArray(), new EdgeImpl[]{edges[1], edges[2]});
    }

    @Test
    public void testRemoveAdd() {
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        removeAndReAddSameEdges(edgeStore);

        Assert.assertEquals(edgeStore.size(), edges.length);
        Assert.assertEquals(edgeStore.toArray(), edges);
    }

    @Test
    public void testGarbageSize() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateLargeEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        Assert.assertEquals(edgeStore.garbageSize, 0);

        removeSomeEdges(edgeStore, 0.5f);
        Assert.assertEquals(edgeStore.garbageSize, (int) (edges.length * 0.5f));

        edgeStore.clear();
        Assert.assertEquals(edgeStore.garbageSize, 0);
    }

    @Test
    public void testBlockCounts() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateLargeEdgeList();
        edgeStore.addAll(Arrays.asList(edges));
        int blockCount = edgeStore.blocksCount;

        for (int i = 0; i < GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE; i++) {
            edgeStore.remove(edges[edges.length - 1 - i]);
        }

        Assert.assertEquals(edgeStore.blocksCount, blockCount - 1);
    }

    @Test
    public void testBlockCountsEmpty() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateLargeEdgeList();
        edgeStore.addAll(Arrays.asList(edges));
        edgeStore.removeAll(Arrays.asList(edges));

        Assert.assertEquals(edgeStore.blocksCount, 1);
        Assert.assertEquals(edgeStore.blocks[0], edgeStore.currentBlock);
        Assert.assertEquals(edgeStore.currentBlockIndex, 0);
        Assert.assertEquals(edgeStore.size, 0);
        Assert.assertEquals(edgeStore.garbageSize, 0);
    }

    @Test
    public void testDictionary() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.contains(edge));
        Assert.assertEquals(edgeStore.get("0"), edge);
        Assert.assertNull(edgeStore.get("1"));
        Assert.assertFalse(edgeStore.contains(GraphGenerator.generateSingleEdge()));

        edgeStore.remove(edge);
        Assert.assertFalse(edgeStore.contains(edge));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDictionaryDuplicate() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);
        EdgeImpl edge2 = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge2);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullSource() {
        NodeImpl n1 = new NodeImpl("0");
        EdgeImpl e1 = new EdgeImpl("0", null, n1, 0, 1.0, true);
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.add(e1);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testInvalidEdges() {
        NodeImpl n1 = new NodeImpl("0");
        NodeImpl n2 = new NodeImpl("1");
        EdgeImpl e1 = new EdgeImpl("0", n1, n2, 0, 1.0, true);
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.add(e1);
    }

    @Test
    public void testAddMultiTypes() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl n1 = new NodeImpl("0");
        NodeImpl n2 = new NodeImpl("1");
        nodeStore.add(n1);
        nodeStore.add(n2);

        EdgeImpl e1 = new EdgeImpl("0", n1, n2, 0, 1.0, true);
        EdgeImpl e2 = new EdgeImpl("1", n1, n2, 1, 1.0, true);
        EdgeImpl e3 = new EdgeImpl("2", n1, n2, 1, 1.0, true);
        EdgeStore edgeStore = new EdgeStore();
        boolean a = edgeStore.add(e1);
        boolean b = edgeStore.add(e2);
        boolean c = edgeStore.add(e3);

        Assert.assertTrue(a);
        Assert.assertTrue(b);
        Assert.assertFalse(c);

        Assert.assertTrue(edgeStore.contains(e1));
        Assert.assertTrue(edgeStore.contains(e2));
        Assert.assertFalse(edgeStore.contains(e3));
    }

    @Test
    public void testRemoveMultitypes() {
        NodeStore nodeStore = new NodeStore();
        NodeImpl n1 = new NodeImpl("0");
        NodeImpl n2 = new NodeImpl("1");
        nodeStore.add(n1);
        nodeStore.add(n2);

        EdgeImpl e1 = new EdgeImpl("0", n1, n2, 0, 1.0, true);
        EdgeImpl e2 = new EdgeImpl("1", n1, n2, 1, 1.0, true);
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.add(e1);
        edgeStore.add(e2);

        Assert.assertEquals(edgeStore.size(0), 1);
        Assert.assertEquals(edgeStore.size(1), 1);

        boolean a = edgeStore.remove(e1);
        boolean b = edgeStore.remove(e2);
        boolean c = edgeStore.remove(e2);

        Assert.assertTrue(a);
        Assert.assertTrue(b);
        Assert.assertFalse(c);

        Assert.assertEquals(edgeStore.size(0), 0);
        Assert.assertEquals(edgeStore.size(1), 0);
    }

    @Test
    public void testOutIterator() {
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        Object2ObjectMap<Object, EdgeImpl> edgeMap = getObjectMap(edges);

        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeOutIterator itr = edgeStore.edgeOutIterator(n);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                Assert.assertEquals(e.source, n);
                Assert.assertEquals(edgeMap.remove(e.getId()), e);
            }
        }
        Assert.assertEquals(edgeMap.size(), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testOutIteratorNull() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeOutIterator(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOutIteratorInvalid() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeOutIterator(new NodeImpl("0"));
    }

    @Test
    public void testOutIteratorAfterRemove() {
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>(Arrays.asList(edges));
        edgeStore.addAll(edgeList);
        List<EdgeImpl> deletedEdges = removeSomeEdges(edgeStore);
        edgeList.removeAll(deletedEdges);

        Object2ObjectMap<Object, EdgeImpl> edgeMap = getObjectMap(edgeList.toArray(new EdgeImpl[0]));

        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeOutIterator itr = edgeStore.edgeOutIterator(n);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                Assert.assertEquals(e.source, n);
                Assert.assertEquals(edgeMap.remove(e.getId()), e);
            }
        }
        Assert.assertEquals(edgeMap.size(), 0);
    }

    @Test
    public void testOutIteratorRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        int index = 0;
        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeOutIterator itr = edgeStore.edgeOutIterator(n);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                itr.remove();
                Assert.assertFalse(edgeStore.contains(e));
                Assert.assertEquals(edgeStore.size(), edges.length - ++index);
            }
        }
        Assert.assertEquals(index, edges.length);
        testContainsNone(edgeStore, Arrays.asList(edges));
    }

    @Test
    public void testInIterator() {
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        Object2ObjectMap<Object, EdgeImpl> edgeMap = getObjectMap(edges);

        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeInIterator itr = edgeStore.edgeInIterator(n);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                Assert.assertEquals(e.target, n);
                Assert.assertEquals(edgeMap.remove(e.getId()), e);
            }
        }
        Assert.assertEquals(edgeMap.size(), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testInIteratorNull() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeInIterator(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInIteratorInvalid() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeInIterator(new NodeImpl("0"));
    }

    @Test
    public void testInIteratorAfterRemove() {
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>(Arrays.asList(edges));
        edgeStore.addAll(edgeList);
        List<EdgeImpl> deletedEdges = removeSomeEdges(edgeStore);
        edgeList.removeAll(deletedEdges);

        Object2ObjectMap<Object, EdgeImpl> edgeMap = getObjectMap(edgeList.toArray(new EdgeImpl[0]));

        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeInIterator itr = edgeStore.edgeInIterator(n);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                Assert.assertEquals(e.target, n);
                Assert.assertEquals(edgeMap.remove(e.getId()), e);
            }
        }
        Assert.assertEquals(edgeMap.size(), 0);
    }

    @Test
    public void testInIteratorRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        int index = 0;
        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeInIterator itr = edgeStore.edgeInIterator(n);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                itr.remove();
                Assert.assertFalse(edgeStore.contains(e));
                Assert.assertEquals(edgeStore.size(), edges.length - ++index);
            }
        }
        Assert.assertEquals(index, edges.length);
        testContainsNone(edgeStore, Arrays.asList(edges));
    }

    @Test
    public void testInOutIterator() {
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        Object2ObjectMap<Object, EdgeImpl> inEdgeMap = getObjectMap(edges);
        Object2ObjectMap<Object, EdgeImpl> outEdgeMap = getObjectMap(edges);

        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeInOutIterator itr = edgeStore.edgeIterator(n);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                if (e.isSelfLoop()) {
                    if (itr.out) {
                        Assert.assertEquals(outEdgeMap.remove(e.getId()), e);
                        Assert.assertEquals(inEdgeMap.remove(e.getId()), e);
                    }
                } else {
                    if (e.source == n) {
                        Assert.assertEquals(outEdgeMap.remove(e.getId()), e);
                    } else {
                        Assert.assertEquals(inEdgeMap.remove(e.getId()), e);
                    }
                }
            }
        }
        Assert.assertEquals(outEdgeMap.size(), 0);
        Assert.assertEquals(inEdgeMap.size(), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testInOutIteratorNull() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeIterator(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInOutIteratorInvalid() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeIterator(new NodeImpl("0"));
    }

    @Test
    public void testInOutIteratorAfterRemove() {
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>(Arrays.asList(edges));
        edgeStore.addAll(edgeList);
        List<EdgeImpl> deletedEdges = removeSomeEdges(edgeStore);
        edgeList.removeAll(deletedEdges);

        Object2ObjectMap<Object, EdgeImpl> inEdgeMap = getObjectMap(edgeList.toArray(new EdgeImpl[0]));
        Object2ObjectMap<Object, EdgeImpl> outEdgeMap = getObjectMap(edgeList.toArray(new EdgeImpl[0]));

        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeInOutIterator itr = edgeStore.edgeIterator(n);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                if (e.isSelfLoop()) {
                    if (itr.out) {
                        Assert.assertEquals(outEdgeMap.remove(e.getId()), e);
                        Assert.assertEquals(inEdgeMap.remove(e.getId()), e);
                    }
                } else {
                    if (e.source == n) {
                        Assert.assertEquals(outEdgeMap.remove(e.getId()), e);
                    } else {
                        Assert.assertEquals(inEdgeMap.remove(e.getId()), e);
                    }
                }
            }
        }
        Assert.assertEquals(outEdgeMap.size(), 0);
        Assert.assertEquals(inEdgeMap.size(), 0);
    }

    @Test
    public void testInOutIteratorRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        int index = 0;
        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeInOutIterator itr = edgeStore.edgeIterator(n);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                itr.remove();
                Assert.assertFalse(edgeStore.contains(e));
                Assert.assertEquals(edgeStore.size(), edges.length - ++index);
            }
        }
        Assert.assertEquals(index, edges.length);
        testContainsNone(edgeStore, Arrays.asList(edges));
    }

    @Test
    public void testOutTypeIterator() {
        EdgeImpl[] edges = GraphGenerator.generateSmallMultiTypeEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        Object2ObjectMap<Object, EdgeImpl> edgeMap = getObjectMap(edges);

        for (NodeImpl n : getNodes(edges)) {
            for (int i = 0; i < n.headOut.length; i++) {
                EdgeStore.EdgeTypeOutIterator itr = edgeStore.edgeOutIterator(n, i);
                for (; itr.hasNext();) {
                    EdgeImpl e = itr.next();
                    Assert.assertEquals(e.type, i);
                    Assert.assertEquals(e.source, n);
                    Assert.assertEquals(edgeMap.remove(e.getId()), e);
                }
            }
        }
        Assert.assertEquals(edgeMap.size(), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testOutTypeIteratorNull() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeOutIterator(null, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOutTypeIteratorInvalid() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeOutIterator(new NodeImpl("0"), 0);
    }

    @Test
    public void testOutTypeIteratorUnknownType() {
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.add(edge);
        Assert.assertFalse(edgeStore.edgeOutIterator(edge.source, 99).hasNext());
    }

    @Test
    public void testOutTypeIteratorRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        int index = 0;
        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeTypeOutIterator itr = edgeStore.edgeOutIterator(n, 0);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                itr.remove();
                Assert.assertFalse(edgeStore.contains(e));
                Assert.assertEquals(edgeStore.size(), edges.length - ++index);
            }
        }
        Assert.assertEquals(index, edges.length);
        testContainsNone(edgeStore, Arrays.asList(edges));
    }

    @Test
    public void testInTypeIterator() {
        EdgeImpl[] edges = GraphGenerator.generateSmallMultiTypeEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        Object2ObjectMap<Object, EdgeImpl> edgeMap = getObjectMap(edges);

        for (NodeImpl n : getNodes(edges)) {
            for (int i = 0; i < n.headIn.length; i++) {
                EdgeStore.EdgeTypeInIterator itr = edgeStore.edgeInIterator(n, i);
                for (; itr.hasNext();) {
                    EdgeImpl e = itr.next();
                    Assert.assertEquals(e.type, i);
                    Assert.assertEquals(e.target, n);
                    Assert.assertEquals(edgeMap.remove(e.getId()), e);
                }
            }
        }
        Assert.assertEquals(edgeMap.size(), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testInTypeIteratorNull() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeInIterator(null, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInTypeIteratorInvalid() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeInIterator(new NodeImpl("0"), 0);
    }

    @Test
    public void testInTypeIteratorUnknownType() {
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.add(edge);
        Assert.assertFalse(edgeStore.edgeInIterator(edge.target, 99).hasNext());
    }

    @Test
    public void testInTypeIteratorRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        int index = 0;
        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeTypeInIterator itr = edgeStore.edgeInIterator(n, 0);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                itr.remove();
                Assert.assertFalse(edgeStore.contains(e));
                Assert.assertEquals(edgeStore.size(), edges.length - ++index);
            }
        }
        Assert.assertEquals(index, edges.length);
        testContainsNone(edgeStore, Arrays.asList(edges));
    }

    @Test
    public void testInOutTypeIterator() {
        EdgeImpl[] edges = GraphGenerator.generateSmallMultiTypeEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        Object2ObjectMap<Object, EdgeImpl> inEdgeMap = getObjectMap(edges);
        Object2ObjectMap<Object, EdgeImpl> outEdgeMap = getObjectMap(edges);

        for (NodeImpl n : getNodes(edges)) {
            for (int i = 0; i < Math.max(n.headIn.length, n.headOut.length); i++) {
                EdgeStore.EdgeTypeInOutIterator itr = edgeStore.edgeIterator(n, i);
                for (; itr.hasNext();) {
                    EdgeImpl e = itr.next();
                    if (e.isSelfLoop()) {
                        if (itr.out) {
                            Assert.assertEquals(outEdgeMap.remove(e.getId()), e);
                            Assert.assertEquals(inEdgeMap.remove(e.getId()), e);
                        } else {
                            Assert.fail();
                        }
                    } else {
                        if (e.source == n) {
                            Assert.assertEquals(outEdgeMap.remove(e.getId()), e);
                        } else {
                            Assert.assertEquals(inEdgeMap.remove(e.getId()), e);
                        }
                    }
                    Assert.assertEquals(e.type, i);
                }
            }
        }
        Assert.assertEquals(outEdgeMap.size(), 0);
        Assert.assertEquals(inEdgeMap.size(), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testInOutTypeIteratorNull() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeIterator(null, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInOutTypeIteratorInvalid() {
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.edgeIterator(new NodeImpl("0"), 0);
    }

    @Test
    public void testInOutTypeIteratorUnknownType() {
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.add(edge);
        Assert.assertFalse(edgeStore.edgeIterator(edge.source, 99).hasNext());
    }

    @Test
    public void testInOutTypeIteratorRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        int index = 0;
        for (NodeImpl n : getNodes(edges)) {
            EdgeStore.EdgeTypeInOutIterator itr = edgeStore.edgeIterator(n, 0);
            for (; itr.hasNext();) {
                EdgeImpl e = itr.next();
                itr.remove();
                Assert.assertFalse(edgeStore.contains(e));
                if (itr.out || !e.isSelfLoop()) {
                    Assert.assertEquals(edgeStore.size(), edges.length - ++index);
                }
            }
        }
        Assert.assertEquals(index, edges.length);
        testContainsNone(edgeStore, Arrays.asList(edges));
    }

    @Test
    public void testAddUndirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleUndirectedEdge();
        boolean a = edgeStore.add(edge);
        boolean b = edgeStore.add(edge);

        Assert.assertEquals(a, true);
        Assert.assertEquals(b, false);

        Assert.assertEquals(edgeStore.isEmpty(), false);
        Assert.assertEquals(edgeStore.size(), 1);

        Assert.assertTrue(edgeStore.contains(edge));
        Assert.assertNotSame(edge.getStoreId(), EdgeStore.NULL_ID);
    }

    @Test
    public void testUndirectedLongId() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleUndirectedEdge();
        edgeStore.add(edge);

        Assert.assertEquals(EdgeStore.getLongId(edge.source, edge.target, false), EdgeStore.getLongId(edge.target, edge.source, false));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddUndirectedExistingDirected() {
        EdgeImpl directed = GraphGenerator.generateSingleEdge();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.add(directed);

        EdgeImpl undirectedSameOrder = new EdgeImpl("4", directed.source, directed.target, directed.type, 1.0, false);
        Assert.assertFalse(edgeStore.add(undirectedSameOrder));

        EdgeImpl undirectedOtherOrder = new EdgeImpl('5', directed.target, directed.source, directed.type, 1.0, false);
        edgeStore.add(undirectedOtherOrder);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddDirectedExistingUndirected() {
        EdgeImpl undirected = GraphGenerator.generateSingleUndirectedEdge();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.add(undirected);

        EdgeImpl directed = new EdgeImpl('5', undirected.target, undirected.source, undirected.type, 1.0, true);
        edgeStore.add(directed);
    }

    @Test
    public void testContainsBySourceTargetUndirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleUndirectedEdge();
        edgeStore.add(edge);
        Assert.assertTrue(edgeStore.contains(edge.getSource(), edge.getTarget(), edge.getType()));
        Assert.assertTrue(edgeStore.contains(edge.getTarget(), edge.getSource(), edge.getType()));
        Assert.assertFalse(edgeStore.contains(edge.getSource(), edge.getTarget(), 10));
    }

    @Test
    public void testGetBySourceTargetUndirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleUndirectedEdge();
        edgeStore.add(edge);
        Assert.assertEquals(edgeStore.get(edge.getSource(), edge.getTarget(), edge.getType()), edge);
        Assert.assertEquals(edgeStore.get(edge.getTarget(), edge.getSource(), edge.getType()), edge);
    }

    @Test
    public void testGetBySourceTargetMixed() {
        EdgeImpl[] edges = GraphGenerator.generateMixedEdgeList(20);
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        for (EdgeImpl edge : edges) {
            if (edge.isDirected()) {
                Assert.assertEquals(edgeStore.get(edge.getSource(), edge.getTarget(), edge.getType()), edge);
            } else {
                Assert.assertEquals(edgeStore.get(edge.getSource(), edge.getTarget(), edge.getType()), edge);
                Assert.assertEquals(edgeStore.get(edge.getTarget(), edge.getSource(), edge.getType()), edge);
            }
        }
    }

    @Test
    public void testContainsBySourceTargetMixed() {
        EdgeImpl[] edges = GraphGenerator.generateMixedEdgeList(20);
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        for (EdgeImpl edge : edges) {
            if (edge.isDirected()) {
                Assert.assertTrue(edgeStore.contains(edge.getSource(), edge.getTarget(), edge.getType()));
            } else {
                Assert.assertTrue(edgeStore.contains(edge.getSource(), edge.getTarget(), edge.getType()));
                Assert.assertTrue(edgeStore.contains(edge.getTarget(), edge.getSource(), edge.getType()));
            }
        }
    }

    @Test
    public void testUndirectedCount() {
        EdgeImpl[] edges = GraphGenerator.generateSmallMixedEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        Assert.assertEquals(0, edgeStore.undirectedSize);

        edgeStore.addAll(Arrays.asList(edges));

        int undirected = 0;
        for (EdgeImpl e : edges) {
            if (!e.isDirected()) {
                undirected++;
            }
        }
        Assert.assertEquals(undirected, edgeStore.undirectedSize);

        for (EdgeImpl e : edges) {
            if (!e.isDirected()) {
                undirected--;
            }
            edgeStore.remove(e);
            Assert.assertEquals(undirected, edgeStore.undirectedSize);
        }
        Assert.assertEquals(0, edgeStore.undirectedSize);
    }

    @Test
    public void testMutual() {
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(1000);
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        Long2ObjectOpenHashMap<EdgeImpl> map = new Long2ObjectOpenHashMap<EdgeImpl>();
        for (EdgeImpl e : edges) {
            map.put(e.getLongId(), e);
        }

        int mutualEdges = 0;
        for (EdgeImpl e : edges) {
            EdgeImpl mutual = map.get(EdgeStore.getLongId(e.target, e.source, e.isDirected()));
            if (mutual != null && !e.isSelfLoop()) {
                Assert.assertTrue(e.isMutual());
                Assert.assertTrue(mutual.isMutual());
                mutualEdges++;
            }
        }
        Assert.assertTrue(mutualEdges > 0);
    }

    @Test
    public void testAddSelfLoop() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSelfLoop(0, true);
        boolean a = edgeStore.add(edge);
        boolean b = edgeStore.add(edge);

        EdgeImpl reverse = new EdgeImpl("1", edge.target, edge.source, edge.type, 1.0, true);
        boolean c = edgeStore.add(reverse);

        Assert.assertTrue(a);
        Assert.assertFalse(b);
        Assert.assertFalse(c);

        Assert.assertEquals(edgeStore.isEmpty(), false);
        Assert.assertEquals(edgeStore.size(), 1);

        Assert.assertTrue(edgeStore.contains(edge));
        Assert.assertNotSame(edge.getStoreId(), EdgeStore.NULL_ID);
    }

    @Test
    public void testContainsBySourceTargetSelfLoop() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSelfLoop(0, true);
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.contains(edge.source, edge.target, edge.type));
        Assert.assertTrue(edgeStore.contains(edge.target, edge.source, edge.type));
    }

    @Test
    public void testGetBySourceTargetSelfLoop() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSelfLoop(0, true);
        edgeStore.add(edge);

        Assert.assertEquals(edge, edgeStore.get(edge.source, edge.target, edge.type));

        EdgeStore edgeStore2 = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore2.addAll(Arrays.asList(edges));

        Iterator<Edge> itr = edgeStore2.iteratorSelfLoop();
        while (itr.hasNext()) {
            Edge e = itr.next();
            Assert.assertEquals(e, edgeStore2.get(e.getSource(), e.getTarget(), 0));
        }
    }

    @Test
    public void testAddSelfLoopUndirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSelfLoop(0, false);
        boolean a = edgeStore.add(edge);
        boolean b = edgeStore.add(edge);

        EdgeImpl reverse = new EdgeImpl("1", edge.target, edge.source, edge.type, 1.0, false);
        boolean c = edgeStore.add(reverse);

        Assert.assertTrue(a);
        Assert.assertFalse(b);
        Assert.assertFalse(c);

        Assert.assertEquals(edgeStore.isEmpty(), false);
        Assert.assertEquals(edgeStore.size(), 1);

        Assert.assertTrue(edgeStore.contains(edge));
        Assert.assertNotSame(edge.getStoreId(), EdgeStore.NULL_ID);
    }

    @Test
    public void testContainsBySourceTargetSelfLoopUndirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSelfLoop(0, false);
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.contains(edge.source, edge.target, edge.type));
        Assert.assertTrue(edgeStore.contains(edge.target, edge.source, edge.type));
    }

    @Test
    public void testGetBySourceTargetSelfLoopUndirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSelfLoop(0, false);
        edgeStore.add(edge);

        Assert.assertEquals(edge, edgeStore.get(edge.source, edge.target, edge.type));
        Assert.assertEquals(edge, edgeStore.get(edge.target, edge.source, edge.type));
    }

    @Test
    public void testDegreeDirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(300, 0, true, true);

        LongSet idSet = new LongOpenHashSet();
        int mutualEdges = 0;
        int selfLoops = 0;

        for (EdgeImpl e : edges) {
            NodeImpl source = e.source;
            NodeImpl target = e.target;
            int inDegree = target.inDegree;
            int outDegree = source.outDegree;
            int sourceMutual = source.mutualDegree;
            int targetMutual = target.mutualDegree;
            int sourceDegree = source.getDegree();
            int targetDegree = target.getDegree();

            edgeStore.add(e);
            Assert.assertEquals(source.outDegree, outDegree + 1);
            Assert.assertEquals(target.inDegree, inDegree + 1);
            if (source == target) {
                Assert.assertEquals(source.getDegree(), sourceDegree + 2);
            } else {
                Assert.assertEquals(source.getDegree(), sourceDegree + 1);
                Assert.assertEquals(target.getDegree(), targetDegree + 1);
            }

            if (!e.isSelfLoop() && idSet.contains(EdgeStore.getLongId(e.target, e.source, true))) {
                //Mutual
                Assert.assertEquals(source.mutualDegree, sourceMutual + 1);
                Assert.assertEquals(target.mutualDegree, targetMutual + 1);
                mutualEdges++;
            } else if (e.isSelfLoop()) {
                selfLoops++;
            }

            idSet.add(e.getLongId());
        }

        int totalInDegree = 0;
        int totalOutDegree = 0;
        int totalDegree = 0;
        int totalUndirectedDegree = 0;
        for (NodeImpl node : getNodes(edges)) {
            totalInDegree += node.inDegree;
            totalOutDegree += node.outDegree;
            totalDegree += node.getDegree();
            totalUndirectedDegree += node.getUndirectedDegree();
        }
        Assert.assertEquals(totalInDegree, edges.length);
        Assert.assertEquals(totalOutDegree, edges.length);
        Assert.assertEquals(totalDegree, edges.length * 2);
        Assert.assertEquals(totalUndirectedDegree, edges.length * 2 - mutualEdges * 2);
    }

    @Test
    public void testNeighborOutIterators() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        Set<EdgeImpl> edgeSet = new ObjectOpenHashSet<EdgeImpl>(edges);

        for (NodeImpl node : getNodes(edges)) {
            Set<NodeImpl> nodeSet = new ObjectOpenHashSet<NodeImpl>();

            for (EdgeStore.NeighborsIterator itr = edgeStore.neighborOutIterator(node); itr.hasNext();) {
                NodeImpl neighbor = (NodeImpl) itr.next();
                EdgeImpl edge = edgeStore.get(node, neighbor, 0);
                Assert.assertTrue(edgeSet.remove(edge));
                Assert.assertTrue(nodeSet.add(neighbor));
            }
        }
        Assert.assertEquals(0, edgeSet.size());
    }

    @Test
    public void testNeighborInIterators() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        Set<EdgeImpl> edgeSet = new ObjectOpenHashSet<EdgeImpl>(edges);

        for (NodeImpl node : getNodes(edges)) {
            Set<NodeImpl> nodeSet = new ObjectOpenHashSet<NodeImpl>();

            for (EdgeStore.NeighborsIterator itr = edgeStore.neighborInIterator(node); itr.hasNext();) {
                NodeImpl neighbor = (NodeImpl) itr.next();
                EdgeImpl edge = edgeStore.get(neighbor, node, 0);
                Assert.assertTrue(edgeSet.remove(edge));
                Assert.assertTrue(nodeSet.add(neighbor));
            }
        }
        Assert.assertEquals(0, edgeSet.size());
    }

    @Test
    public void testNeighborInOutIterators() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        Set<EdgeImpl> inEdgeSet = new ObjectOpenHashSet<EdgeImpl>(edges);
        Set<EdgeImpl> outEdgeSet = new ObjectOpenHashSet<EdgeImpl>(edges);
        int mutualEdges = 0;

        for (NodeImpl node : getNodes(edges)) {
            Set<NodeImpl> nodeSet = new ObjectOpenHashSet<NodeImpl>();
            for (EdgeStore.NeighborsIterator itr = edgeStore.neighborIterator(node); itr.hasNext();) {
                NodeImpl neighbor = (NodeImpl) itr.next();
                boolean out = ((EdgeStore.EdgeInOutIterator) itr.itr).out;

                if (out) {
                    EdgeImpl edge = edgeStore.get(node, neighbor, 0);
                    if (edge.isSelfLoop()) {
                        Assert.assertTrue(inEdgeSet.remove(edge));
                    }
                    Assert.assertTrue(outEdgeSet.remove(edge));
                    if (edge.isMutual()) {
                        mutualEdges++;
                    }
                } else {
                    EdgeImpl edge = edgeStore.get(neighbor, node, 0);
                    Assert.assertTrue(inEdgeSet.remove(edge));
                }

                Assert.assertTrue(nodeSet.add(neighbor));
            }
        }
        Assert.assertEquals(0, outEdgeSet.size() - mutualEdges);
        Assert.assertEquals(0, inEdgeSet.size() - mutualEdges);
    }

    @Test
    public void testNodeAdjacentDirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.isAdjacent(edge.source, edge.target, edge.type));
        Assert.assertFalse(edgeStore.isAdjacent(edge.target, edge.source, edge.type));
        Assert.assertFalse(edgeStore.isAdjacent(edge.source, edge.source, edge.type));
        Assert.assertFalse(edgeStore.isAdjacent(edge.source, edge.target, 1));
    }

    @Test
    public void testNodeAdjacentUndirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleUndirectedEdge();
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.isAdjacent(edge.source, edge.target, edge.type));
        Assert.assertTrue(edgeStore.isAdjacent(edge.target, edge.source, edge.type));
        Assert.assertFalse(edgeStore.isAdjacent(edge.source, edge.source, edge.type));
        Assert.assertFalse(edgeStore.isAdjacent(edge.source, edge.target, 1));
    }

    @Test
    public void testNodeAdjacentSelfLoopDirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSelfLoop(0, true);
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.isAdjacent(edge.source, edge.target, edge.type));
        Assert.assertFalse(edgeStore.isAdjacent(edge.source, edge.target, 1));
    }

    @Test
    public void testNodeAdjacentSelfLoopUnDirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSelfLoop(0, false);
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.isAdjacent(edge.source, edge.target, edge.type));
        Assert.assertFalse(edgeStore.isAdjacent(edge.source, edge.target, 1));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNodeAdjacentInvalid() {
        EdgeStore edgeStore = new EdgeStore();
        NodeImpl node1 = new NodeImpl("0");
        NodeImpl node2 = new NodeImpl("1");

        edgeStore.isAdjacent(node1, node2, 0);
    }

    @Test
    public void testNodeAdjacentAllTypes() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateMultiTypeEdgeList(100, 3, true, true);
        edgeStore.addAll(Arrays.asList(edges));

        for (EdgeImpl edge : edges) {
            Assert.assertTrue(edgeStore.isAdjacent(edge.source, edge.target));
            if (!edge.isSelfLoop() && !edge.isMutual()) {
                Assert.assertFalse(edgeStore.isAdjacent(edge.target, edge.source));
            } else if (edge.isMutual()) {
                Assert.assertTrue(edgeStore.isAdjacent(edge.target, edge.source));
            }
        }
    }

    @Test
    public void testEdgeNodeIncident() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.isIncident(edge.source, edge));
        Assert.assertTrue(edgeStore.isIncident(edge.target, edge));

        EdgeImpl selfLoop = new EdgeImpl("2", edge.source, edge.source, 0, 1.0, true);
        edgeStore.add(selfLoop);
        Assert.assertTrue(edgeStore.isIncident(edge.source, selfLoop));
    }

    @Test
    public void testEdgeNodeIncidentUndirected() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl edge = GraphGenerator.generateSingleUndirectedEdge();
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.isIncident(edge.source, edge));
        Assert.assertTrue(edgeStore.isIncident(edge.target, edge));

        EdgeImpl selfLoop = new EdgeImpl("2", edge.source, edge.source, 0, 1.0, true);
        edgeStore.add(selfLoop);
        Assert.assertTrue(edgeStore.isIncident(edge.source, selfLoop));
    }

    @Test
    public void testEdgeIncident() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();

        for (EdgeImpl e : edges) {
            for (EdgeImpl f : edges) {
                boolean isIncident = edgeStore.isIncident(e, f);
                boolean should = e.source == f.source || e.target == f.target || e.source == f.target || e.target == f.source;
                Assert.assertEquals(isIncident, should);
            }
        }
    }

    @Test
    public void testTypeCounting() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        EdgeStore edgeStore = new EdgeStore(edgeTypeStore, null, null, null);
        EdgeImpl[] edges = GraphGenerator.generateSmallMultiTypeEdgeList();

        Int2IntMap counts = new Int2IntOpenHashMap();
        int maxType = 0;
        for (EdgeImpl e : edges) {
            int c = counts.get(e.type);
            c++;
            counts.put(e.type, c);
            maxType = Math.max(maxType, e.type);
        }

        for (int i = 0; i <= maxType; i++) {
            edgeTypeStore.addType("" + i);
        }

        edgeStore.addAll(Arrays.asList(edges));

        for (Int2IntMap.Entry entry : counts.int2IntEntrySet()) {
            int count = edgeStore.size(entry.getIntKey());
            Assert.assertEquals(count, entry.getIntValue());
        }

        for (EdgeImpl e : removeSomeEdges(edgeStore)) {
            int c = counts.get(e.type);
            c--;
            counts.put(e.type, c);
        }

        for (Int2IntMap.Entry entry : counts.int2IntEntrySet()) {
            int count = edgeStore.size(entry.getIntKey());
            Assert.assertEquals(count, entry.getIntValue());
        }
    }

    @Test
    public void testGraphTypeDirected() {
        EdgeStore edgeStore = new EdgeStore();

        Assert.assertTrue(edgeStore.isDirectedGraph());
        Assert.assertFalse(edgeStore.isMixedGraph());
        Assert.assertFalse(edgeStore.isUndirectedGraph());

        EdgeImpl edge = GraphGenerator.generateSingleEdge();
        edgeStore.add(edge);

        Assert.assertTrue(edgeStore.isDirectedGraph());
        Assert.assertFalse(edgeStore.isMixedGraph());
        Assert.assertFalse(edgeStore.isUndirectedGraph());
    }

    @Test
    public void testGraphTypeMixed() {
        EdgeStore edgeStore = new EdgeStore();

        EdgeImpl[] edges = GraphGenerator.generateMixedEdgeList(20);
        edgeStore.addAll(Arrays.asList(edges));

        Assert.assertFalse(edgeStore.isDirectedGraph());
        Assert.assertTrue(edgeStore.isMixedGraph());
        Assert.assertFalse(edgeStore.isUndirectedGraph());

        for (EdgeImpl e : edges) {
            if (e.isDirected()) {
                edgeStore.remove(e);
            }
        }

        Assert.assertFalse(edgeStore.isDirectedGraph());
        Assert.assertFalse(edgeStore.isMixedGraph());
        Assert.assertTrue(edgeStore.isUndirectedGraph());
    }

    @Test
    public void testUndirectedIterator() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        ObjectSet<EdgeImpl> edgeSet = new ObjectOpenHashSet<EdgeImpl>();
        for (EdgeImpl e : edges) {
            if (e.isSelfLoop() || !e.isMutual() || e.source.storeId > e.target.storeId) {
                edgeSet.add(e);
            }
        }

        EdgeStore.EdgeStoreIterator undirectedIterator = edgeStore.iteratorUndirected();
        for (; undirectedIterator.hasNext();) {
            EdgeImpl e = undirectedIterator.next();
            Assert.assertTrue(edgeSet.remove(e));
        }

        Assert.assertEquals(0, edgeSet.size());
    }

    @Test
    public void testUndirectedIteratorRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallUndirectedEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        EdgeStore.EdgeStoreIterator undirectedIterator = edgeStore.iteratorUndirected();
        for (; undirectedIterator.hasNext();) {
            EdgeImpl e = undirectedIterator.next();
            undirectedIterator.remove();
        }

        Assert.assertEquals(edgeStore.size(), 0);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testUndirectedIteratorRemoveDecorator() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        EdgeStore.EdgeStoreIterator undirectedIterator = edgeStore.iteratorUndirected();
        for (; undirectedIterator.hasNext();) {
            EdgeImpl e = undirectedIterator.next();
            undirectedIterator.remove();
        }
    }

    @Test
    public void testInOutUndirectedIteratorRemove() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallUndirectedEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        for (NodeImpl n : getNodes(edges)) {
            Iterator<Edge> itr = edgeStore.edgeUndirectedIterator(n);
            for (; itr.hasNext();) {
                itr.next();
                itr.remove();
            }
        }

        Assert.assertEquals(edgeStore.size(), 0);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testInOutUndirectedIteratorRemoveDecorator() {
        EdgeStore edgeStore = new EdgeStore();
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        for (NodeImpl n : getNodes(edges)) {
            Iterator<Edge> itr = edgeStore.edgeUndirectedIterator(n);
            for (; itr.hasNext();) {
                itr.next();
                itr.remove();
            }
        }
    }

    @Test
    public void testInOutUndirectedIterator() {
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(500, 0, true, true);
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        Object2ObjectMap<NodeImpl, Set<EdgeImpl>> neighbours = getNeighboursMap(edges, 0, true);

        for (NodeImpl n : getNodes(edges)) {
            Iterator<Edge> itr = edgeStore.edgeUndirectedIterator(n);

            Set<EdgeImpl> incidentEdges = neighbours.get(n);

            for (; itr.hasNext();) {
                EdgeImpl e = (EdgeImpl) itr.next();
                Assert.assertTrue(incidentEdges.remove(e));
            }

            Assert.assertEquals(incidentEdges.size(), 0);
            neighbours.remove(n);
        }
        Assert.assertEquals(0, neighbours.size());
    }

    @Test
    public void testInOutTypeUndirectedIterator() {
        EdgeImpl[] edges = GraphGenerator.generateSmallMultiTypeEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        Object2ObjectMap<NodeImpl, Set<EdgeImpl>> neighbours = getNeighboursMap(edges, 2, true);

        for (NodeImpl n : getNodes(edges)) {
            Iterator<Edge> itr = edgeStore.edgeUndirectedIterator(n, 2);

            Set<EdgeImpl> incidentEdges = neighbours.get(n);

            for (; itr.hasNext();) {
                EdgeImpl e = (EdgeImpl) itr.next();
                Assert.assertTrue(incidentEdges.remove(e));
            }

            Assert.assertEquals(incidentEdges.size(), 0);
            neighbours.remove(n);
        }
        Assert.assertEquals(0, neighbours.size());
    }

    @Test
    public void testSelfLoopIterator() {
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        EdgeStore edgeStore = new EdgeStore();
        edgeStore.addAll(Arrays.asList(edges));

        int selfLoops = 0;
        for (EdgeImpl e : edges) {
            if (e.isSelfLoop()) {
                selfLoops++;
            }
        }

        int count = 0;
        EdgeStore.SelfLoopIterator itr = edgeStore.iteratorSelfLoop();
        while (itr.hasNext()) {
            Edge e = itr.next();
            Assert.assertTrue(e.isSelfLoop());
            count++;
        }

        Assert.assertEquals(count, selfLoops);
    }

    /*
     * UTILITY METHODS
     */
    private void testContainsOnly(EdgeStore store, List<EdgeImpl> list) {
        for (EdgeImpl n : list) {
            Assert.assertTrue(store.contains(n));
            Assert.assertFalse(n.getStoreId() == EdgeStore.NULL_ID);
        }
        Assert.assertEquals(store.size(), list.size());

        Set<Edge> set = new HashSet<Edge>(list);
        for (Edge n : store) {
            Assert.assertTrue(set.remove(n));
        }
        Assert.assertTrue(set.isEmpty());
    }

    private void testContainsNone(EdgeStore store, List<EdgeImpl> list) {
        for (EdgeImpl n : list) {
            Assert.assertFalse(store.contains(n));
        }
    }

    private void removeAndReAddSameEdges(EdgeStore store) {
        List<EdgeImpl> edges = removeSomeEdges(store);
        Collections.reverse(edges);
        for (Edge edge : edges) {
            store.add(edge);
        }
    }

    private List<EdgeImpl> removeSomeEdges(EdgeStore store) {
        return removeSomeEdges(store, 0.3f);
    }

    private List<EdgeImpl> removeSomeEdges(EdgeStore store, float ratio) {
        int size = store.size;
        int s = (int) (size * ratio);
        int[] randomIndexes = generateRandomUniqueInts(s, size);
        List<EdgeImpl> edges = new ArrayList<EdgeImpl>(s);
        for (int index : randomIndexes) {
            EdgeImpl edge = store.get(index);
            if (store.remove(edge)) {
                edges.add(edge);
            }
        }
        return edges;
    }

    private Object2ObjectMap<NodeImpl, Set<EdgeImpl>> getNeighboursMap(EdgeImpl[] edges, int type, boolean undirected) {
        Object2ObjectMap<NodeImpl, Set<EdgeImpl>> neighbours = new Object2ObjectOpenHashMap<NodeImpl, Set<EdgeImpl>>();
        for (EdgeImpl e : edges) {
            if (type == e.type) {
                Set<EdgeImpl> sourceSet = neighbours.get(e.source);
                Set<EdgeImpl> targetSet = neighbours.get(e.target);
                if (sourceSet == null) {
                    sourceSet = new ObjectOpenHashSet<EdgeImpl>();
                    neighbours.put(e.source, sourceSet);
                }
                if (!e.isSelfLoop() && targetSet == null) {
                    targetSet = new ObjectOpenHashSet<EdgeImpl>();
                    neighbours.put(e.target, targetSet);
                }
                if (e.isSelfLoop()) {
                    sourceSet.add(e);
                } else if (e.isMutual() && undirected) {
                    if (e.source.storeId > e.target.storeId) {
                        sourceSet.add(e);
                        targetSet.add(e);
                    }
                } else {
                    sourceSet.add(e);
                    targetSet.add(e);
                }
            }
        }
        return neighbours;
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

    private Object2ObjectMap<Object, EdgeImpl> getObjectMap(EdgeImpl[] edges) {
        Object2ObjectMap<Object, EdgeImpl> edgeMap = new Object2ObjectOpenHashMap<Object, EdgeImpl>();
        for (EdgeImpl e : edges) {
            edgeMap.put(e.getId(), e);
        }
        return edgeMap;
    }

    private NodeImpl[] getNodes(EdgeImpl[] edges) {
        ObjectSet<NodeImpl> nodes = new ObjectOpenHashSet<NodeImpl>();
        for (EdgeImpl e : edges) {
            nodes.add(e.source);
            nodes.add(e.target);
        }
        return nodes.toArray(new NodeImpl[0]);
    }

    public List<EdgeImpl> iteratorToArray(Iterator<EdgeImpl> edgeIterator) {
        List<EdgeImpl> list = new ArrayList<EdgeImpl>();
        for (; edgeIterator.hasNext();) {
            EdgeImpl e = edgeIterator.next();
            list.add(e);
        }
        return list;
    }
}
