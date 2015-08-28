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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    public void testAddColumn() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        ColumnImpl col = new ColumnImpl("foo", String.class, "foo", null, Origin.DATA, true, false);
        col.setStoreId(0);

        Assert.assertEquals(index.size(), 0);
        index.addColumn(col);
        Assert.assertEquals(index.size(), 1);
        Assert.assertSame(index.getIndex(col).column, col);
    }

    @Test
    public void testHasColumn() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        ColumnImpl col1 = new ColumnImpl("foo", String.class, "foo", null, Origin.DATA, true, false);
        ColumnImpl col2 = new ColumnImpl("bar", String.class, "bar", null, Origin.DATA, false, false);
        col1.setStoreId(0);
        col2.setStoreId(1);

        Assert.assertFalse(index.hasColumn(col1));
        index.addColumn(col1);
        index.addColumn(col2);
        Assert.assertTrue(index.hasColumn(col1));
        Assert.assertFalse(index.hasColumn(col2));
    }

    @Test
    public void testHasColumnDifferentIndex() {
        ColumnStore<Node> columnStore1 = generateEmptyNodeStore();
        IndexImpl<Node> index1 = columnStore1.indexStore.mainIndex;

        ColumnStore<Node> columnStore2 = generateEmptyNodeStore();
        IndexImpl<Node> index2 = columnStore2.indexStore.mainIndex;

        ColumnImpl col1 = new ColumnImpl("foo", String.class, "foo", null, Origin.DATA, true, false);
        ColumnImpl col2 = new ColumnImpl("bar", String.class, "bar", null, Origin.DATA, true, false);
        col1.setStoreId(0);
        col2.setStoreId(0);

        index1.addColumn(col1);
        index2.addColumn(col2);
        Assert.assertFalse(index1.hasColumn(col2));
        Assert.assertFalse(index2.hasColumn(col1));
    }

    @Test
    public void testAddAllColumns() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        ColumnImpl col1 = new ColumnImpl("1", String.class, "1", null, Origin.DATA, true, false);
        ColumnImpl col2 = new ColumnImpl("2", String.class, "2", null, Origin.DATA, false, false);
        ColumnImpl col3 = new ColumnImpl("3", String.class, "3", null, Origin.DATA, true, false);
        col1.setStoreId(0);
        col2.setStoreId(1);
        col3.setStoreId(2);

        index.addAllColumns(new ColumnImpl[]{col1, col2, col3});
        Assert.assertEquals(index.size(), 2);
    }

    @Test
    public void testDestroy() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        ColumnImpl col = new ColumnImpl("1", String.class, "1", null, Origin.DATA, true, false);
        col.setStoreId(0);
        index.addColumn(col);
        index.destroy();
        Assert.assertEquals(index.size(), 0);
        Assert.assertNull(index.getIndex(col));
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
    public void testWithNullDecorator() {
        IndexImpl<Node> index = generateEmptyIndex();
        Column ageColumn = index.columnStore.getColumn("age");
        Column fooColumn = index.columnStore.getColumn("foo");
        NodeImpl n1 = new NodeImpl(0);
        NodeImpl n2 = new NodeImpl(1);
        NodeImpl n3 = new NodeImpl(2);
        index.put(ageColumn, 10, n1);
        index.put(ageColumn, 20, n2);
        index.put(fooColumn, null, n1);
        index.put(fooColumn, "bar", n3);

        IndexImpl.AbstractIndex withNullIndex = index.getIndex("foo");
        Collection withNullCollection = withNullIndex.values();
        Assert.assertEquals(withNullCollection.size(), 2);
        Assert.assertFalse(withNullCollection.isEmpty());
        Assert.assertTrue(withNullCollection.contains(null));
        Assert.assertTrue(withNullCollection.contains("bar"));
        Assert.assertFalse(withNullCollection.contains("none"));
        Assert.assertEquals(withNullCollection.toArray(), new Object[]{null, "bar"});
        Assert.assertEquals(withNullCollection.toArray(new Object[0]), new Object[]{null, "bar"});
        Assert.assertTrue(withNullCollection.containsAll(Arrays.asList(new Object[]{null, "bar"})));
        Assert.assertFalse(withNullCollection.containsAll(Arrays.asList(new Object[]{null, "none"})));
        Iterator withNullItr = withNullCollection.iterator();
        Assert.assertTrue(withNullItr.hasNext());
        Assert.assertNull(withNullItr.next());
        Assert.assertTrue(withNullItr.hasNext());
        Assert.assertEquals(withNullItr.next(), "bar");
        Assert.assertFalse(withNullItr.hasNext());

        IndexImpl.AbstractIndex withoutNullIndex = index.getIndex("age");
        Collection withoutNullCollection = withoutNullIndex.values();
        Assert.assertEquals(withoutNullCollection.size(), 2);
        Assert.assertFalse(withoutNullCollection.isEmpty());
        Assert.assertFalse(withoutNullCollection.contains(null));
        Assert.assertTrue(withoutNullCollection.contains(10));
        Assert.assertFalse(withoutNullCollection.contains(30));
        Assert.assertEquals(withoutNullCollection.toArray(), new Object[]{10, 20});
        Assert.assertEquals(withoutNullCollection.toArray(new Object[0]), new Object[]{10, 20});
        Assert.assertTrue(withoutNullCollection.containsAll(Arrays.asList(new Object[]{10, 20})));
        Assert.assertFalse(withoutNullCollection.containsAll(Arrays.asList(new Object[]{null})));
        Assert.assertFalse(withoutNullCollection.containsAll(Arrays.asList(new Object[]{30})));
        Iterator withoutNullItr = withoutNullCollection.iterator();
        Assert.assertTrue(withoutNullItr.hasNext());
        Assert.assertEquals(withoutNullItr.next(), 10);
        Assert.assertTrue(withoutNullItr.hasNext());
        Assert.assertEquals(withoutNullItr.next(), 20);
        Assert.assertFalse(withoutNullItr.hasNext());
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
    public void testRemoveByColumn() {
        IndexImpl<Node> index = generateEmptyIndex();
        Column column = index.columnStore.getColumn("age");
        NodeImpl n = new NodeImpl(0);
        index.put(column, 10, n);
        index.remove(column, 10, n);

        Assert.assertEquals(index.count(column, 10), 0);
    }

    @Test
    public void testRemoveByString() {
        IndexImpl<Node> index = generateEmptyIndex();
        Column column = index.columnStore.getColumn("age");
        NodeImpl n = new NodeImpl(0);
        index.put(column, 10, n);
        index.remove("age", 10, n);

        Assert.assertEquals(index.count(column, 10), 0);
    }

    @Test
    public void testSetByColumn() {
        IndexImpl<Node> index = generateEmptyIndex();
        Column column = index.columnStore.getColumn("age");
        NodeImpl n = new NodeImpl(0);
        index.put(column, 10, n);
        index.set(column, 10, 20, n);

        Assert.assertEquals(index.count(column, 10), 0);
        Assert.assertEquals(index.count(column, 20), 1);
        Assert.assertEquals(index.countValues(column), 1);
        Assert.assertEquals(index.countElements(column), 1);
    }

    @Test
    public void testSetByString() {
        IndexImpl<Node> index = generateEmptyIndex();
        Column column = index.columnStore.getColumn("age");
        NodeImpl n = new NodeImpl(0);
        index.put(column, 10, n);
        index.set("age", 10, 20, n);

        Assert.assertEquals(index.count(column, 10), 0);
        Assert.assertEquals(index.count(column, 20), 1);
        Assert.assertEquals(index.countValues(column), 1);
        Assert.assertEquals(index.countElements(column), 1);
    }

    @Test
    public void testGetIteratorNull() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        columnStore.addColumn(new ColumnImpl("c", String.class, null, null, Origin.DATA, true, false));

        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        NodeImpl n = new NodeImpl(0);
        index.put("c", null, n);

        Iterator<Node> itr = index.get("c", null).iterator();
        Assert.assertTrue(itr.hasNext());
        Assert.assertSame(itr.next(), n);
    }

    @Test
    public void testGetNullEntry() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        columnStore.addColumn(new ColumnImpl("c", String.class, null, null, Origin.DATA, true, false));

        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        NodeImpl n = new NodeImpl(0);
        index.put("c", null, n);

        Iterator<Entry<Object, Set<Node>>> itr = index.get(columnStore.getColumn("c")).iterator();
        Assert.assertTrue(itr.hasNext());
        Entry<Object, Set<Node>> entry = itr.next();
        Assert.assertNull(entry.getKey());
        Assert.assertEquals(entry.getValue().size(), 1);
        Assert.assertTrue(entry.getValue().contains(n));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetNullEntrySetValue() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        columnStore.addColumn(new ColumnImpl("c", String.class, null, null, Origin.DATA, true, false));

        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        NodeImpl n = new NodeImpl(0);
        index.put("c", null, n);

        Iterator<Entry<Object, Set<Node>>> itr = index.get(columnStore.getColumn("c")).iterator();
        Assert.assertTrue(itr.hasNext());
        Entry<Object, Set<Node>> entry = itr.next();
        entry.setValue(null);
    }

    @Test
    public void testNonNumberTypes() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        columnStore.addColumn(new ColumnImpl("c1", String.class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c2", Boolean.class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c3", Character.class, null, null, Origin.DATA, true, false));

        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        NodeImpl n1 = new NodeImpl(0);
        NodeImpl n2 = new NodeImpl(1);

        Object[] values = new Object[]{"foo", Boolean.TRUE, 'f'};

        for (int i = 1; i <= values.length; i++) {
            index.put("c" + i, values[i - 1], n1);
            index.put("c" + i, null, n2);
        }

        for (int i = 1; i <= values.length; i++) {
            Column column = columnStore.getColumn("c" + i);

            Assert.assertEquals(index.countElements(column), 2);
            Assert.assertEquals(index.countValues(column), 2);

            Assert.assertSame(getIterable(index.get("c" + i, values[i - 1]))[0], n1);
            Assert.assertSame(getIterable(index.get("c" + i, null))[0], n2);
            Assert.assertSame(getIterable(index.get(column, values[i - 1]))[0], n1);
            Assert.assertSame(getIterable(index.get(column, null))[0], n2);
            Assert.assertEquals(index.count("c" + i, values[i - 1]), 1);
            Assert.assertEquals(index.count("c" + i, null), 1);
            Assert.assertEquals(index.count(column, values[i - 1]), 1);
            Assert.assertEquals(index.count(column, null), 1);
            Assert.assertTrue(index.values(column).contains(null));
            Assert.assertTrue(index.values(column).contains(values[i - 1]));
        }

        for (int i = 1; i <= values.length; i++) {
            index.remove("c" + i, values[i - 1], n1);
            index.remove("c" + i, null, n2);
        }

        for (int i = 1; i <= values.length; i++) {
            Column column = columnStore.getColumn("c" + i);

            Assert.assertEquals(index.countElements(column), 0);
            Assert.assertEquals(index.countValues(column), 0);
        }
    }

    @Test
    public void testArrayNonNumberTypes() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        columnStore.addColumn(new ColumnImpl("c1", boolean[].class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c2", char[].class, null, null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("c3", String[].class, null, null, Origin.DATA, true, false));

        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        NodeImpl n1 = new NodeImpl(0);
        NodeImpl n2 = new NodeImpl(1);

        Object[] values = new Object[]{new boolean[]{false, true}, new char[]{'f', 'o'}, new String[]{"foo", "bar"}};

        for (int i = 1; i <= values.length; i++) {
            index.put("c" + i, values[i - 1], n1);
            index.put("c" + i, null, n2);
        }

        for (int i = 1; i <= values.length; i++) {
            Column column = columnStore.getColumn("c" + i);

            Assert.assertEquals(index.countElements(column), 3);
            Assert.assertEquals(index.countValues(column), 3);

            Assert.assertSame(getIterable(index.get("c" + i, Array.get(values[i - 1], 0)))[0], n1);
            Assert.assertSame(getIterable(index.get("c" + i, null))[0], n2);
            Assert.assertSame(getIterable(index.get(column, Array.get(values[i - 1], 0)))[0], n1);
            Assert.assertSame(getIterable(index.get(column, null))[0], n2);
            Assert.assertEquals(index.count("c" + i, Array.get(values[i - 1], 0)), 1);
            Assert.assertEquals(index.count("c" + i, null), 1);
            Assert.assertEquals(index.count(column, Array.get(values[i - 1], 0)), 1);
            Assert.assertEquals(index.count(column, null), 1);
            Assert.assertTrue(index.values(column).contains(null));
            Assert.assertTrue(index.values(column).contains(Array.get(values[i - 1], 0)));
        }

        for (int i = 1; i <= values.length; i++) {
            index.remove("c" + i, values[i - 1], n1);
            index.remove("c" + i, null, n2);
        }

        for (int i = 1; i <= values.length; i++) {
            Column column = columnStore.getColumn("c" + i);

            Assert.assertEquals(index.countElements(column), 0);
            Assert.assertEquals(index.countValues(column), 0);
        }
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

        Object[] values = new Object[]{1, (short) 1, 1f, 1.0, 1l, (byte) 1};

        for (int i = 1; i <= values.length; i++) {
            index.put("c" + i, values[i - 1], n1);
            index.put("c" + i, null, n2);
        }

        for (int i = 1; i <= values.length; i++) {
            Column column = columnStore.getColumn("c" + i);

            Assert.assertEquals(index.countElements(column), 2);
            Assert.assertEquals(index.countValues(column), 2);

            Assert.assertSame(getIterable(index.get("c" + i, values[i - 1]))[0], n1);
            Assert.assertSame(getIterable(index.get("c" + i, null))[0], n2);
            Assert.assertSame(getIterable(index.get(column, values[i - 1]))[0], n1);
            Assert.assertSame(getIterable(index.get(column, null))[0], n2);
            Assert.assertEquals(index.count("c" + i, values[i - 1]), 1);
            Assert.assertEquals(index.count("c" + i, null), 1);
            Assert.assertEquals(index.count(column, values[i - 1]), 1);
            Assert.assertEquals(index.count(column, null), 1);
            Assert.assertTrue(index.values(column).contains(null));
            Assert.assertTrue(index.values(column).contains(values[i - 1]));

            Number min = index.getMinValue(column);
            Assert.assertEquals(min.byteValue(), (byte) 1);
            Number max = index.getMaxValue(column);
            Assert.assertEquals(max.byteValue(), (byte) 1);
        }

        for (int i = 1; i <= values.length; i++) {
            index.remove("c" + i, values[i - 1], n1);
            index.remove("c" + i, null, n2);
        }

        for (int i = 1; i <= values.length; i++) {
            Column column = columnStore.getColumn("c" + i);

            Assert.assertEquals(index.countElements(column), 0);
            Assert.assertEquals(index.countValues(column), 0);
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

        Object[] values = new Object[6];
        values[0] = new int[]{1, 2};
        values[1] = new short[]{1, 2};
        values[2] = new float[]{1, 2};
        values[3] = new double[]{1, 2};
        values[4] = new long[]{1, 2};
        values[5] = new byte[]{1, 2};

        for (int i = 1; i <= values.length; i++) {
            index.put("c" + i, values[i - 1], n1);
            index.put("c" + i, null, n2);
        }

        for (int i = 1; i <= values.length; i++) {
            Column column = columnStore.getColumn("c" + i);

            Assert.assertEquals(index.countElements(column), 3);
            Assert.assertEquals(index.countValues(column), 3);

            Assert.assertSame(getIterable(index.get("c" + i, Array.get(values[i - 1], 0)))[0], n1);
            Assert.assertSame(getIterable(index.get("c" + i, null))[0], n2);
            Assert.assertSame(getIterable(index.get(column, Array.get(values[i - 1], 0)))[0], n1);
            Assert.assertSame(getIterable(index.get(column, null))[0], n2);
            Assert.assertEquals(index.count("c" + i, Array.get(values[i - 1], 0)), 1);
            Assert.assertEquals(index.count("c" + i, null), 1);
            Assert.assertEquals(index.count(column, Array.get(values[i - 1], 0)), 1);
            Assert.assertEquals(index.count(column, null), 1);
            Assert.assertTrue(index.values(column).contains(null));
            Assert.assertTrue(index.values(column).contains(Array.get(values[i - 1], 0)));

            Number min = index.getMinValue(column);
            Assert.assertEquals(min.byteValue(), (byte) 1);
            Number max = index.getMaxValue(column);
            Assert.assertEquals(max.byteValue(), (byte) 2);
        }

        for (int i = 1; i <= values.length; i++) {
            index.remove("c" + i, values[i - 1], n1);
            index.remove("c" + i, null, n2);
        }

        for (int i = 1; i <= values.length; i++) {
            Column column = columnStore.getColumn("c" + i);

            Assert.assertEquals(index.countElements(column), 0);
            Assert.assertEquals(index.countValues(column), 0);
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
                    } else if (col.getTypeClass().equals(String.class)) {
                        n.setAttribute(col, "" + i);
                    } else if (col.getTypeClass().equals(Integer.class)) {
                        n.setAttribute(col, i);
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
