package org.gephi.graph.store;

import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class ElementImplTest {

    @Test
    public void testId() {
        NodeImpl nodeImpl = new NodeImpl(0);
        Assert.assertEquals(nodeImpl.getId(), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testIdNull() {
        new NodeImpl(null);
    }

    @Test
    public void testSetPropertyColumn() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setProperty(column, 1);

        Assert.assertEquals(node.properties.length, 1);
        Assert.assertEquals(node.properties[0], 1);
        Assert.assertEquals(node.getProperty(column), 1);
    }

    @Test
    public void testSetPropertyString() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setProperty("age", 1);

        Assert.assertEquals(node.properties.length, 1);
        Assert.assertEquals(node.properties[0], 1);
        Assert.assertEquals(node.getProperty(column), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetPropertyUnknownColumn() {
        GraphStore store = new GraphStore();
        ColumnImpl columnImpl = new ColumnImpl("0", String.class, "title", "", Origin.DATA, false);
        NodeImpl node = new NodeImpl(0, store);
        node.setProperty(columnImpl, "0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetPropertyOtherStoreColumn() {
        GraphStore otherStore = new GraphStore();
        Column column = generateBasicColumn(otherStore);
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);
        node.setProperty(column, "0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetPropertyWrongType() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setProperty(column, "a");
    }

    @Test
    public void testSetPropertyNull() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setProperty(column, null);

        Assert.assertEquals(node.properties.length, 1);
        Assert.assertNull(node.properties[0]);
        Assert.assertNull(node.getProperty(column));
    }

    @Test
    public void testGetPropertyColumn() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setProperty(column, 1);
        Object res = node.getProperty(column);

        Assert.assertEquals(res, 1);
        node.setProperty(column, 2);

        res = node.getProperty(column);
        Assert.assertEquals(res, 2);
    }

    @Test
    public void testGetPropertyKey() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setProperty(column, 1);
        Object res = node.getProperty(column.getId());
        Assert.assertEquals(res, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetPropertyUnknownColumn() {
        GraphStore store = new GraphStore();
        ColumnImpl columnImpl = new ColumnImpl("0", String.class, "title", "", Origin.DATA, false);
        NodeImpl node = new NodeImpl(0, store);
        node.getProperty(columnImpl);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetPropertyOtherStoreColumn() {
        GraphStore otherStore = new GraphStore();
        Column column = generateBasicColumn(otherStore);
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);
        node.getProperty(column);
    }

    @Test
    public void testGetDefaultValue() {
        GraphStore store = new GraphStore();
        Integer defaultValue = 25;
        Column column = new ColumnImpl("age", Integer.class, "Age", defaultValue, Origin.DATA, true);
        store.nodePropertyStore.addColumn(column);

        NodeImpl node = new NodeImpl(0, store);
        Object res = node.getProperty(column.getId());
        Assert.assertEquals(res, defaultValue);

        node.setProperty(column, null);
        res = node.getProperty(column.getId());
        Assert.assertEquals(res, defaultValue);

        node.setProperty(column, 1);
        res = node.getProperty(column.getId());
        Assert.assertEquals(res, 1);
    }

    @Test
    public void testGetPropertyKeysEmpty() {
        GraphStore store = new GraphStore();
        NodeImpl node = new NodeImpl(0, store);
        Set<String> pk = node.getPropertyKeys();
        Assert.assertTrue(pk.isEmpty());
    }

    @Test
    public void testGetPropertyKeys() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        Set<String> pk = node.getPropertyKeys();
        Assert.assertTrue(pk.contains(column.getId()));
        Assert.assertEquals(pk.size(), 1);
    }

    //Utility
    private Column generateBasicColumn(GraphStore graphStore) {
        graphStore.nodePropertyStore.addColumn(new ColumnImpl("age", Integer.class, "Age", null, Origin.DATA, true));
        return graphStore.nodePropertyStore.getColumn("age");
    }
}
