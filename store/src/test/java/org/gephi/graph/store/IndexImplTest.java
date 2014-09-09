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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class IndexImplTest {

    @Test
    public void testIndexName() {
        IndexImpl<Node> index = generateEmptyIndex();
        Assert.assertEquals(index.getIndexClass(), Node.class);
        Assert.assertEquals(index.getIndexName(), "index_" + Node.class.getCanonicalName());
    }

    @Test
    public void testCount() {
        IndexImpl<Node> index = generateEmptyIndex();
        NodeImpl[] nodes = generateNodesWithUniqueAttributes(index, true);
        putAll(nodes, index);

        for (Column col : index.columnStore) {
            if (col.isIndexed()) {
                Object2IntMap<Object> counter = new Object2IntOpenHashMap();
                for (NodeImpl n : nodes) {
                    Object obj = n.getAttribute(col);
                    counter.put(obj, counter.getInt(obj) + 1);
                }

                for (Object2IntMap.Entry entry : counter.object2IntEntrySet()) {
                    int count = entry.getIntValue();
                    Object val = entry.getKey();

                    Assert.assertEquals(index.count(col, val), count);
                    Assert.assertEquals(index.count(col.getId(), val), count);
                }
            }
        }
    }

    @Test
    public void testGet() {
        IndexImpl<Node> index = generateEmptyIndex();
        NodeImpl[] nodes = generateNodesWithUniqueAttributes(index, true);
        putAll(nodes, index);

        for (Column col : index.columnStore) {
            if (col.isIndexed()) {
                ObjectSet<Object> set = new ObjectOpenHashSet<Object>();
                for (NodeImpl n : nodes) {
                    Object obj = n.getAttribute(col);
                    set.add(obj);
                }

                for (Object value : set) {
                    ObjectSet actual1 = new ObjectOpenHashSet(getIterable(index.get(col, value)));
                    ObjectSet actual2 = new ObjectOpenHashSet(getIterable(index.get(col.getId(), value)));
                    ObjectSet expected = new ObjectOpenHashSet();
                    for (NodeImpl n : nodes) {
                        Object v = n.getAttribute(col);
                        if ((v == null && value == null) || (v != null && v.equals(value))) {
                            expected.add(n);
                        }
                    }
                    Assert.assertEquals(actual1, expected);
                    Assert.assertEquals(actual2, expected);
                }

                for (Map.Entry<Object, Set<Node>> entry : index.get(col)) {
                    Object value = entry.getKey();
                    Set<Node> actual = entry.getValue();

                    ObjectSet expected = new ObjectOpenHashSet();
                    for (NodeImpl n : nodes) {
                        Object v = n.getAttribute(col);
                        if ((v == null && value == null) || (v != null && v.equals(value))) {
                            expected.add(n);
                        }
                    }

                    Assert.assertEquals(actual, expected);
                }
            }
        }
    }

    @Test
    public void testMinMaxValue() {
        IndexImpl<Node> index = generateEmptyIndex();
        NodeImpl[] nodes = generateNodesWithUniqueAttributes(index, true);

        Column ageCol = index.columnStore.getColumn("age");

        Assert.assertNull(index.getMinValue(ageCol));
        Assert.assertNull(index.getMaxValue(ageCol));

        putAll(nodes, index);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (NodeImpl n : nodes) {
            Integer v = (Integer) n.getAttribute(ageCol);
            if (v != null) {
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
        }

        Assert.assertEquals(index.getMinValue(ageCol), min);
        Assert.assertEquals(index.getMaxValue(ageCol), max);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testMinValueNoNumber() {
        IndexImpl<Node> index = generateEmptyIndex();
        index.getMinValue(index.columnStore.getColumn("foo"));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testMaxValueNoNumber() {
        IndexImpl<Node> index = generateEmptyIndex();
        index.getMaxValue(index.columnStore.getColumn("foo"));
    }

    @Test
    public void testValues() {
        IndexImpl<Node> index = generateEmptyIndex();
        NodeImpl[] nodes = generateNodesWithUniqueAttributes(index, true);
        putAll(nodes, index);

        for (Column col : index.columnStore) {
            if (col.isIndexed()) {
                Collection collection = index.values(col);

                ObjectSet<Object> expected = new ObjectOpenHashSet<Object>();
                for (NodeImpl n : nodes) {
                    Object obj = n.getAttribute(col);
                    expected.add(obj);
                }

                Assert.assertEquals(collection.size(), expected.size());
                Assert.assertEquals(index.countValues(col), expected.size());
                Assert.assertEquals(new ObjectOpenHashSet<Object>(collection), expected);
            }
        }
    }

    @Test
    public void testCountElements() {
        IndexImpl<Node> index = generateEmptyIndex();

        for (Column col : index.columnStore) {
            if (col.isIndexed()) {
                Assert.assertEquals(index.countElements(col), 0);
            }
        }

        NodeImpl[] nodes = generateNodesWithUniqueAttributes(index, true);
        putAll(nodes, index);

        for (Column col : index.columnStore) {
            if (col.isIndexed()) {
                Assert.assertEquals(index.countElements(col), nodes.length);
            }
        }
    }

    @Test
    public void testPut() {
        IndexImpl<Node> index = generateEmptyIndex();
        Column column = index.columnStore.getColumn("age");
        NodeImpl n = new NodeImpl(0);

        Integer v = 10;
        Assert.assertSame(index.put(column, v, n), v);
        Assert.assertEquals(index.count(column, v), 1);

        Assert.assertSame(index.put(column, v, n), v);
        Assert.assertEquals(index.count(column, v), 1);
    }

    @Test
    public void testPutManagedValue() {
        IndexImpl<Node> index = generateEmptyIndex();
        Column column = index.columnStore.getColumn("age");
        NodeImpl n1 = new NodeImpl(0);
        NodeImpl n2 = new NodeImpl(1);

        Integer v = 10;
        index.put(column, v, n1);
        Assert.assertSame(index.put(column, 10, n2), v);
    }

    @Test
    public void testRemove() {
        IndexImpl<Node> index = generateEmptyIndex();
        Column column = index.columnStore.getColumn("age");
        NodeImpl n = new NodeImpl(0);
        index.put(column, 10, n);
        index.remove(column, 10, n);

        Assert.assertEquals(index.count(column, 10), 0);
    }

    @Test
    public void testPrimitiveNumberTypes() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        columnStore.addColumn(new ColumnImpl("c1", Integer.class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c2", Short.class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c3", Float.class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c4", Double.class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c5", Long.class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c6", Byte.class, null, null, Origin.DATA, true, false));

        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        NodeImpl n1 = new NodeImpl(0);
        NodeImpl n2 = new NodeImpl(1);

        index.put("c1", new Integer(1), n1);
        index.put("c2", new Short((short) 1), n1);
        index.put("c3", new Float(1f), n1);
        index.put("c4", new Double(1.0), n1);
        index.put("c5", new Long(1l), n1);
        index.put("c6", new Byte((byte) 1), n1);

        for (int i = 1; i < 7; i++) {
            index.put("c" + i, null, n2);
        }

        for (int i = 1; i < 7; i++) {
            Column column = columnStore.getColumn("c" + i);
            Number min = index.getMinValue(column);
            Assert.assertEquals(min.byteValue(), (byte) 1);
            Number max = index.getMaxValue(column);
            Assert.assertEquals(max.byteValue(), (byte) 1);

            Assert.assertEquals(index.count("c" + i, null), 1);
            Assert.assertEquals(index.countElements(column), 2);
            Assert.assertEquals(index.countValues(column), 2);

            Assert.assertTrue(index.values(column).contains(null));

            if (column.getTypeClass().equals(Integer.class)) {
                Assert.assertSame(getIterable(index.get("c" + i, new Integer(1)))[0], n1);
                Assert.assertEquals(index.count("c" + i, new Integer(1)), 1);
                Assert.assertTrue(index.values(column).contains(new Integer(1)));
            } else if (column.getTypeClass().equals(Short.class)) {
                Assert.assertSame(getIterable(index.get("c" + i, new Short((short) 1)))[0], n1);
                Assert.assertEquals(index.count("c" + i, new Short((short) 1)), 1);
                Assert.assertTrue(index.values(column).contains(new Short((short) 1)));
            } else if (column.getTypeClass().equals(Long.class)) {
                Assert.assertSame(getIterable(index.get("c" + i, new Long(1l)))[0], n1);
                Assert.assertEquals(index.count("c" + i, new Long(1l)), 1);
                Assert.assertTrue(index.values(column).contains(new Long(1l)));
            } else if (column.getTypeClass().equals(Byte.class)) {
                Assert.assertSame(getIterable(index.get("c" + i, new Byte((byte) 1)))[0], n1);
                Assert.assertEquals(index.count("c" + i, new Byte((byte) 1)), 1);
                Assert.assertTrue(index.values(column).contains(new Byte((byte) 1)));
            } else if (column.getTypeClass().equals(Float.class)) {
                Assert.assertSame(getIterable(index.get("c" + i, new Float(1f)))[0], n1);
                Assert.assertEquals(index.count("c" + i, new Float(1f)), 1);
                Assert.assertTrue(index.values(column).contains(new Float(1f)));
            } else if (column.getTypeClass().equals(Double.class)) {
                Assert.assertSame(getIterable(index.get("c" + i, new Double(1.0)))[0], n1);
                Assert.assertEquals(index.count("c" + i, new Double(1.0)), 1);
                Assert.assertTrue(index.values(column).contains(new Double(1.0)));
            }
        }
    }

    @Test
    public void testArrayNumberTypes() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        columnStore.addColumn(new ColumnImpl("c1", int[].class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c2", short[].class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c3", float[].class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c4", double[].class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c5", long[].class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c6", byte[].class, null, null, Origin.DATA, true, false));

        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        NodeImpl n1 = new NodeImpl(0);
        NodeImpl n2 = new NodeImpl(1);

        Object[] n1Objects = new Object[6];
        n1Objects[0] = new int[]{1, 2};
        n1Objects[1] = new short[]{1, 2};
        n1Objects[2] = new float[]{1, 2};
        n1Objects[3] = new double[]{1, 2};
        n1Objects[4] = new long[]{1, 2};
        n1Objects[5] = new byte[]{1, 2};

        for (int i = 1; i < 7; i++) {
            index.put("c" + i, n1Objects[i - 1], n1);
            index.put("c" + i, null, n2);
        }

        for (int i = 1; i < 7; i++) {
            Column column = columnStore.getColumn("c" + i);
            Number min = index.getMinValue(column);
            Assert.assertEquals(min.byteValue(), (byte) 1);
            Number max = index.getMaxValue(column);
            Assert.assertEquals(max.byteValue(), (byte) 2);

            Assert.assertEquals(index.count("c" + i, null), 1);
            Assert.assertEquals(index.countElements(column), 3);
            Assert.assertEquals(index.countValues(column), 3);

            Assert.assertTrue(index.values(column).contains(null));
        }

    }

    //UTILITIES
    private NodeImpl[] generateNodesWithUniqueAttributes(IndexImpl<Node> index, boolean withNulls) {
        int count = 100;
        Random random = new Random(342);
        NodeImpl[] nodes = new NodeImpl[count];
        for (int i = 0; i < 100; i++) {
            NodeImpl n = new NodeImpl(i);
            nodes[i] = n;

            for (Column col : index.columnStore) {
                if (!col.isReadOnly()) {
                    if (withNulls && random.nextDouble() < 0.1) {
                        n.setAttribute(col, null);
                    } else {
                        if (col.getTypeClass().equals(String.class)) {
                            n.setAttribute(col, "" + i);
                        } else if (col.getTypeClass().equals(Integer.class)) {
                            n.setAttribute(col, i);
                        }
                    }
                }
            }
        }
        return nodes;
    }

    private void putAll(NodeImpl[] nodes, IndexImpl<Node> index) {
        for (NodeImpl n : nodes) {
            for (Column col : index.columnStore) {
                if (col.isIndexed()) {
                    Object val = n.getAttribute(col);
                    index.put(col, val, n);
                }
            }
        }
    }

    private IndexImpl<Node> generateEmptyIndex() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        columnStore.addColumn(new ColumnImpl("foo", String.class, "foo", null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("age", Integer.class, "Age", null, Origin.DATA, true, false));
        return columnStore.indexStore.mainIndex;
    }

    private ColumnStore<Node> generateEmptyNodeStore() {
        GraphStore graphStore = new GraphStore();
        ColumnStore<Node> columnStore = graphStore.nodeColumnStore;
        return columnStore;
    }

    private Node[] getIterable(Iterable<Node> itr) {
        List<Node> list = new ArrayList<Node>();
        for (Node n : itr) {
            list.add(n);
        }
        return list.toArray(new Node[0]);
    }
}
