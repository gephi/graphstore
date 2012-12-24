package org.gephi.graph.store;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
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
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false);

        store.addColumn(col);

        Assert.assertTrue(store.hasColumn("0"));
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(store.getColumn("0"), col);
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
            public Object getDefaultValue() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddColumnTwice() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false);
        store.addColumn(col);
        store.addColumn(col);
    }

    @Test
    public void testRemoveColumn() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false);

        store.addColumn(col);
        store.removeColumn(col);

        Assert.assertFalse(store.hasColumn("0"));
        Assert.assertEquals(store.size(), 0);
        Assert.assertEquals(col.getIndex(), ColumnStore.NULL_ID);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRemoveColumnNull() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);

        store.removeColumn((Column)null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveColumnNotExist() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false);
        store.removeColumn(col);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetColumnUnknown() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false);
        store.getColumn("");
    }

    @Test
    public void testHasColumn() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false);
        store.addColumn(col);

        Assert.assertTrue(store.hasColumn(col.getId()));
        Assert.assertFalse(store.hasColumn(""));
        Assert.assertFalse(store.hasColumn("A"));
    }

    @Test
    public void testGetPropertyKeys() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false);
        store.addColumn(col);

        ObjectSet<String> set = new ObjectOpenHashSet<String>();
        set.add("0");
        Assert.assertEquals(store.getPropertyKeys(), set);
    }

    @Test
    public void testGarbage() {
        ColumnStore<Node> store = new ColumnStore(Node.class, false);
        ColumnImpl col = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false);

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
}
