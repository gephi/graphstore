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
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnObserver;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class ColumnStoreTest {

    @Test
    public void testEmpty() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);

        Assert.assertEquals(store.size(), 0);
    }

    @Test
    public void testAddColumn() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("a", Integer.class, null, null, Origin.DATA, false, false);

        store.addColumn(col);

        Assert.assertTrue(store.hasColumn("a"));
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(store.getColumn("a"), col);
        Assert.assertEquals(store.getColumn("A"), col);
        Assert.assertEquals(col.getIndex(), 0);
        Assert.assertEquals(store.getColumnByIndex(0), col);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testAddColumnNull() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        store.addColumn(null);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testAddColumnAnonymousClass() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        store.addColumn(new Column() {
            @Override
            public String getId() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getIndex() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getTitle() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Class getTypeClass() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Origin getOrigin() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isIndexed() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isArray() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getDefaultValue() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isDynamic() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isNumber() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isProperty() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Table getTable() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isReadOnly() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ColumnObserver createColumnObserver(boolean withDiff) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Estimator getEstimator() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setEstimator(Estimator estimator) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddColumnTwice() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        store.addColumn(col);
        store.addColumn(col);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddColumnDifferentCase() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        store.addColumn(new ColumnImpl("A", Integer.class, null, null, Origin.DATA, false, false));
        store.addColumn(new ColumnImpl("a", Integer.class, null, null, Origin.DATA, false, false));
    }

    @Test
    public void testDefaultValue() {
        Integer defaultValue = 25;
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, defaultValue, Origin.DATA, false, false);

        store.addColumn(col);
        Assert.assertEquals(col.getDefaultValue(), defaultValue);
    }

    @Test
    public void testRemoveColumn() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);

        store.addColumn(col);
        store.removeColumn(col);

        Assert.assertFalse(store.hasColumn("0"));
        Assert.assertEquals(store.size(), 0);
        Assert.assertEquals(col.getIndex(), ColumnStore.NULL_ID);
    }

    @Test
    public void testRemoveColumnString() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);

        store.addColumn(col);
        store.removeColumn("0");

        Assert.assertFalse(store.hasColumn("0"));
        Assert.assertEquals(store.size(), 0);
        Assert.assertEquals(col.getIndex(), ColumnStore.NULL_ID);
    }

    @Test
    public void testRemoveColumnStringDifferentCase() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("a", Integer.class, null, null, Origin.DATA, false, false);

        store.addColumn(col);
        store.removeColumn("A");

        Assert.assertFalse(store.hasColumn("a"));
        Assert.assertEquals(store.size(), 0);
        Assert.assertEquals(col.getIndex(), ColumnStore.NULL_ID);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRemoveColumnNull() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);

        store.removeColumn((Column) null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveColumnNotExist() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        store.removeColumn(col);
    }

    @Test
    public void testGetColumnUnknown() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        Assert.assertNull(store.getColumn(""));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetColumnByIndexUnknown() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        store.getColumnByIndex(10);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetColumnIndexUnknown() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        store.getColumnIndex("");
    }

    @Test
    public void testGetColumnIndex() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("a", Integer.class, null, null, Origin.DATA, false, false);
        store.addColumn(col);

        Assert.assertEquals(store.getColumnIndex("a"), 0);
        Assert.assertEquals(store.getColumnIndex("A"), 0);
    }

    @Test
    public void testHasColumn() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        store.addColumn(col);

        Assert.assertTrue(store.hasColumn(col.getId()));
        Assert.assertFalse(store.hasColumn(""));
        Assert.assertFalse(store.hasColumn("A"));
    }

    @Test
    public void testHasColumnDifferentCase() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("A", Integer.class, null, null, Origin.DATA, false, false);
        store.addColumn(col);

        Assert.assertTrue(store.hasColumn("A"));
        Assert.assertTrue(store.hasColumn("a"));
    }

    @Test
    public void testGetColumnKeys() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        store.addColumn(col);

        ObjectSet<String> set = new ObjectOpenHashSet<>();
        set.add("0");
        Assert.assertEquals(store.getColumnKeys(), set);
    }

    @Test
    public void testClear() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);

        store.addColumn(col);
        store.clear();

        Assert.assertFalse(store.hasColumn("0"));
        Assert.assertEquals(store.size(), 0);
    }

    @Test
    public void testGarbage() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);

        store.addColumn(col);
        store.removeColumn(col);

        Assert.assertEquals(store.garbageQueue.size(), 1);

        store.addColumn(col);

        Assert.assertTrue(store.hasColumn("0"));
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(col.getIndex(), 0);
        Assert.assertEquals(store.garbageQueue.size(), 0);
        Assert.assertEquals(store.getColumnByIndex(0), col);
    }

    @Test
    public void testDeepEqualsEmpty() {
        ColumnStore<Node> store1 = new ColumnStore<>(Node.class, false);
        Assert.assertTrue(store1.deepEquals(store1));
        ColumnStore<Node> store2 = new ColumnStore<>(Node.class, false);
        Assert.assertTrue(store1.deepEquals(store2));
    }

    @Test
    public void testDeepHashCodeEmpty() {
        ColumnStore<Node> store1 = new ColumnStore<>(Node.class, false);
        Assert.assertEquals(store1.deepHashCode(), store1.deepHashCode());
        ColumnStore<Node> store2 = new ColumnStore<>(Node.class, false);
        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
        ColumnStore<Edge> store3 = new ColumnStore<>(Edge.class, false);
        Assert.assertNotEquals(store1.deepHashCode(), store3.deepHashCode());
    }

    @Test
    public void testDeepEquals() {
        ColumnStore<Node> store1 = new ColumnStore<>(Node.class, false);
        ColumnImpl col1 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        store1.addColumn(col1);

        ColumnStore<Node> store2 = new ColumnStore<>(Node.class, false);
        ColumnImpl col2 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        store2.addColumn(col2);

        Assert.assertTrue(store1.deepEquals(store2));

        ColumnStore<Node> store3 = new ColumnStore<>(Node.class, false);
        ColumnImpl col3 = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        store3.addColumn(col3);

        Assert.assertFalse(store1.deepEquals(store3));
    }

    @Test
    public void testDeepHashCode() {
        ColumnStore<Node> store1 = new ColumnStore<>(Node.class, false);
        ColumnImpl col1 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        store1.addColumn(col1);

        ColumnStore<Node> store2 = new ColumnStore<>(Node.class, false);
        ColumnImpl col2 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        store2.addColumn(col2);

        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());

        ColumnStore<Node> store3 = new ColumnStore<>(Node.class, false);
        ColumnImpl col3 = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        store3.addColumn(col3);

        Assert.assertNotEquals(store1.deepHashCode(), store3.deepHashCode());
    }

    @Test
    public void testDeepEqualsWithGarbage() {
        ColumnStore<Node> store1 = new ColumnStore<>(Node.class, false);
        ColumnImpl col11 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col12 = new ColumnImpl("1", String.class, "title", "default", Origin.PROPERTY, false, false);
        store1.addColumn(col11);
        store1.addColumn(col12);
        store1.removeColumn(col11);

        ColumnStore<Node> store2 = new ColumnStore<>(Node.class, false);
        ColumnImpl col21 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col22 = new ColumnImpl("1", String.class, "title", "default", Origin.PROPERTY, false, false);
        store2.addColumn(col21);
        store2.addColumn(col22);
        store2.removeColumn(col21);

        Assert.assertTrue(store1.deepEquals(store2));
    }

    @Test
    public void testDeepHashCodeWithGarbage() {
        ColumnStore<Node> store1 = new ColumnStore<>(Node.class, false);
        ColumnImpl col11 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col12 = new ColumnImpl("1", String.class, "title", "default", Origin.PROPERTY, false, false);
        store1.addColumn(col11);
        store1.addColumn(col12);
        store1.removeColumn(col11);

        ColumnStore<Node> store2 = new ColumnStore<>(Node.class, false);
        ColumnImpl col21 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col22 = new ColumnImpl("1", String.class, "title", "default", Origin.PROPERTY, false, false);
        store2.addColumn(col21);
        store2.addColumn(col22);
        store2.removeColumn(col21);

        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
    }

    @Test
    public void testToArray() {
        ColumnStore<Node> store = new ColumnStore<>(Node.class, false);

        Assert.assertEquals(store.toArray().length, 0);

        ColumnImpl col11 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col12 = new ColumnImpl("1", String.class, "title", "default", Origin.PROPERTY, false, false);
        store.addColumn(col11);
        store.addColumn(col12);

        Column[] cols = store.toArray();
        Assert.assertEquals(cols.length, 2);
        Assert.assertSame(cols[0], col11);
        Assert.assertEquals(cols[1], col12);
    }

    @Test
    public void testToArrayWithGarbage() {
        ColumnStore<Node> store = new ColumnStore<>(Node.class, false);

        Assert.assertEquals(store.toArray().length, 0);

        ColumnImpl col11 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col12 = new ColumnImpl("1", String.class, "title", "default", Origin.PROPERTY, false, false);
        store.addColumn(col11);
        store.addColumn(col12);
        store.removeColumn(col11);

        Column[] cols = store.toArray();
        Assert.assertEquals(cols.length, 1);
        Assert.assertSame(cols[0], col12);
    }

    @Test
    public void testToList() {
        ColumnStore<Node> store = new ColumnStore<>(Node.class, false);

        Assert.assertEquals(store.toList().size(), 0);

        ColumnImpl col11 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col12 = new ColumnImpl("1", String.class, "title", "default", Origin.PROPERTY, false, false);
        store.addColumn(col11);
        store.addColumn(col12);

        List<Column> cols = store.toList();
        Assert.assertEquals(cols.size(), 2);
        Assert.assertSame(cols.get(0), col11);
        Assert.assertEquals(cols.get(1), col12);
    }

    @Test
    public void testToListWithGarbage() {
        ColumnStore<Node> store = new ColumnStore<>(Node.class, false);

        Assert.assertEquals(store.toList().size(), 0);

        ColumnImpl col11 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col12 = new ColumnImpl("1", String.class, "title", "default", Origin.PROPERTY, false, false);
        store.addColumn(col11);
        store.addColumn(col12);
        store.removeColumn(col11);

        List<Column> cols = store.toList();
        Assert.assertEquals(cols.size(), 1);
        Assert.assertSame(cols.get(0), col12);
    }

    @Test
    public void testColumnStoreIterator() {
        ColumnStore<Node> store = new ColumnStore<>(Node.class, false);

        ColumnImpl col11 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col12 = new ColumnImpl("1", String.class, "title", "default", Origin.PROPERTY, false, false);
        store.addColumn(col11);
        store.addColumn(col12);

        Iterator<Column> itr = store.iterator();
        Assert.assertTrue(itr.hasNext());
        Assert.assertSame(itr.next(), col11);
        Assert.assertTrue(itr.hasNext());
        Assert.assertSame(itr.next(), col12);
        Assert.assertFalse(itr.hasNext());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testColumnStoreIteratorRemove() {
        ColumnStore<Node> store = new ColumnStore<>(Node.class, false);

        ColumnImpl col11 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        store.addColumn(col11);

        Iterator<Column> itr = store.iterator();
        Assert.assertTrue(itr.hasNext());
        Assert.assertSame(itr.next(), col11);
        itr.remove();
    }
}
