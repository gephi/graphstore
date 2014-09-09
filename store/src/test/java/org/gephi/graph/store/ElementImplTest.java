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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.gephi.attribute.time.TimestampIntegerSet;
import static org.gephi.graph.store.GraphStoreConfiguration.ENABLE_ELEMENT_LABEL;
import static org.gephi.graph.store.GraphStoreConfiguration.ENABLE_ELEMENT_TIMESTAMP_SET;
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
    public void testSetAttributeColumn() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1);

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertEquals(node.attributes[getFirstNonPropertyIndex()], 1);
        Assert.assertEquals(node.getAttribute(column), 1);
    }

    @Test
    public void testSetAttributeString() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute("age", 1);

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertEquals(node.attributes[getFirstNonPropertyIndex()], 1);
        Assert.assertEquals(node.getAttribute(column), 1);
    }

    @Test
    public void testReplaceAttribute() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute("age", 1);
        node.setAttribute("age", 2);

        Assert.assertEquals(node.getAttribute(column), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeUnknownColumn() {
        GraphStore store = new GraphStore();
        ColumnImpl columnImpl = new ColumnImpl("0", String.class, "title", "", Origin.DATA, false, false);
        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(columnImpl, "0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeOtherStoreColumn() {
        GraphStore otherStore = new GraphStore();
        Column column = generateBasicColumn(otherStore);
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, "0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeWrongType() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, "a");
    }

    @Test
    public void testSetAttributeNull() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, null);

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertNull(node.attributes[getFirstNonPropertyIndex()]);
        Assert.assertNull(node.getAttribute(column));
    }

    @Test
    public void testSetAttributeDynamicColumn() {
        GraphStore store = new GraphStore();
        Column column = generateDynamicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1, 2.0);
        node.setAttribute(column, 2, 1.0);

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertEquals(node.attributes[getFirstNonPropertyIndex()].getClass(), TimestampIntegerSet.class);
        Assert.assertEquals(node.getAttribute(column, 2.0), 1);
        Assert.assertEquals(node.getAttribute(column, 1.0), 2);
    }

    @Test
    public void testSetAttributeDynamicString() {
        GraphStore store = new GraphStore();
        Column column = generateDynamicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute("age", 1, 2.0);

        Assert.assertEquals(node.getAttribute(column, 2.0), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeNonDynamic() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1, 2.0);
    }

    @Test
    public void testReplaceDynamicAttribute() {
        GraphStore store = new GraphStore();
        Column column = generateDynamicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1, 2.0);
        node.setAttribute(column, 2, 2.0);

        Assert.assertEquals(node.getAttribute(column, 2.0), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNaNTimestamp() {
        GraphStore store = new GraphStore();
        Column column = generateDynamicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1, Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInfiniteTimestamp() {
        GraphStore store = new GraphStore();
        Column column = generateDynamicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testGetAttributeColumn() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1);
        Object res = node.getAttribute(column);

        Assert.assertEquals(res, 1);
        node.setAttribute(column, 2);

        res = node.getAttribute(column);
        Assert.assertEquals(res, 2);
    }

    @Test
    public void testGetAttributeKey() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1);
        Object res = node.getAttribute(column.getId());
        Assert.assertEquals(res, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetAttributeUnknownColumn() {
        GraphStore store = new GraphStore();
        ColumnImpl columnImpl = new ColumnImpl("0", String.class, "title", "", Origin.DATA, false, false);
        NodeImpl node = new NodeImpl(0, store);
        node.getAttribute(columnImpl);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetAttributeOtherStoreColumn() {
        GraphStore otherStore = new GraphStore();
        Column column = generateBasicColumn(otherStore);
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);
        node.getAttribute(column);
    }

    @Test
    public void testGetAttributeDynamicColumn() {
        GraphStore store = new GraphStore();
        Column column = generateDynamicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1, 1.0);

        Assert.assertEquals(node.getAttribute(column, 1.0), 1);
    }

    @Test
    public void testGetAttributeDynamicString() {
        GraphStore store = new GraphStore();
        Column column = generateDynamicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1, 1.0);

        Assert.assertEquals(node.getAttribute("age", 1.0), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetAttributeNonDynamic() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 1);

        node.getAttribute(column, 1.0);
    }

    @Test
    public void testGetDefaultValue() {
        GraphStore store = new GraphStore();
        Integer defaultValue = 25;
        Column column = new ColumnImpl("age", Integer.class, "Age", defaultValue, Origin.DATA, true, false);
        store.nodeColumnStore.addColumn(column);

        NodeImpl node = new NodeImpl(0, store);
        Object res = node.getAttribute(column.getId());
        Assert.assertEquals(res, defaultValue);

        node.setAttribute(column, null);
        res = node.getAttribute(column.getId());
        Assert.assertEquals(res, defaultValue);

        node.setAttribute(column, 1);
        res = node.getAttribute(column.getId());
        Assert.assertEquals(res, 1);
    }

    @Test
    public void testGetAttributeKeysEmpty() {
        GraphStore store = new GraphStore();
        NodeImpl node = new NodeImpl(0, store);
        Set<String> pk = node.getAttributeKeys();
        Assert.assertTrue(pk.size() == getElementPropertiesLength());
    }

    @Test
    public void testGetAttributeKeys() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        Set<String> pk = node.getAttributeKeys();
        Assert.assertTrue(pk.contains(column.getId()));
        Assert.assertEquals(pk.size(), 1 + getElementPropertiesLength());
    }

    @Test
    public void testLabel() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);

        Assert.assertNull(node.getLabel());

        String lbl = "test";
        node.setLabel(lbl);

        Assert.assertSame(node.getLabel(), lbl);
        node.setLabel(null);

        Assert.assertNull(node.getLabel());
    }

    @Test
    public void testAddTimestamp() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);
        Assert.assertTrue(node.addTimestamp(1.0));

        Assert.assertTrue(node.hasTimestamp(1.0));
    }

    @Test
    public void testRemoveTimestamp() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);
        node.addTimestamp(1.0);
        Assert.assertTrue(node.removeTimestamp(1.0));
        Assert.assertFalse(node.hasTimestamp(1.0));
    }

    @Test
    public void testHasTimestampEmpty() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);
        Assert.assertFalse(node.hasTimestamp(1.0));
    }

    @Test
    public void testRemoveAttribute() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 14);

        Object r = node.removeAttribute(column);
        Assert.assertEquals(r, 14);

        Assert.assertNull(node.getAttribute(column));
    }

    @Test
    public void testRemoveAttributeString() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 14);

        Object r = node.removeAttribute("age");
        Assert.assertEquals(r, 14);

        Assert.assertNull(node.getAttribute("age"));
    }

    @Test
    public void testGetTimestampsEmpty() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);
        Assert.assertEquals(node.getTimestamps(), new double[0]);
    }

    @Test
    public void testGetTimestamps() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl(0, store);
        node.addTimestamp(1.0);
        node.addTimestamp(2.0);

        Assert.assertEquals(node.getTimestamps(), new double[]{1.0, 2.0});

        node.removeTimestamp(1.0);

        Assert.assertEquals(node.getTimestamps(), new double[]{2.0});
    }

    @Test
    public void testGetDynamicAttributes() {
        GraphStore store = new GraphStore();
        Column column = generateDynamicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.setAttribute(column, 12, 1.0);
        node.setAttribute(column, 14, 3.0);
        node.setAttribute(column, 13, 2.0);

        Iterator<Map.Entry<Double, Object>> itr = node.getAttributes(column).iterator();
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Object> entry1 = itr.next();
        Assert.assertEquals(entry1.getKey(), 1.0);
        Assert.assertEquals(entry1.getValue(), 12);
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Object> entry2 = itr.next();
        Assert.assertEquals(entry2.getKey(), 3.0);
        Assert.assertEquals(entry2.getValue(), 14);
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Object> entry3 = itr.next();
        Assert.assertEquals(entry3.getKey(), 2.0);
        Assert.assertEquals(entry3.getValue(), 13);
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testGetDynamicAttributesEmpty() {
        GraphStore store = new GraphStore();
        Column column = generateDynamicColumn(store);

        NodeImpl node = new NodeImpl(0, store);

        Iterator<Map.Entry<Double, Object>> itr = node.getAttributes(column).iterator();
        Assert.assertFalse(itr.hasNext());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetDynamicAttributesStaticColumn() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl(0, store);
        node.getAttributes(column);
    }

    //Utility
    private Column generateBasicColumn(GraphStore graphStore) {
        graphStore.nodeColumnStore.addColumn(new ColumnImpl("age", Integer.class, "Age", null, Origin.DATA, true, false));
        return graphStore.nodeColumnStore.getColumn("age");
    }

    private Column generateDynamicColumn(GraphStore graphStore) {
        graphStore.nodeColumnStore.addColumn(new ColumnImpl("age", TimestampIntegerSet.class, "Age", null, Origin.DATA, false, false));
        return graphStore.nodeColumnStore.getColumn("age");
    }

    //Properties size
    public int getElementPropertiesLength() {
        return 1 + (ENABLE_ELEMENT_LABEL ? 1 : 0) + (ENABLE_ELEMENT_TIMESTAMP_SET ? 1 : 0);
    }

    public int getFirstNonPropertyIndex() {
        return getElementPropertiesLength();
    }
}
