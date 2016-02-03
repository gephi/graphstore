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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnIterable;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.types.TimestampBooleanMap;
import org.gephi.graph.api.types.TimestampByteMap;
import org.gephi.graph.api.types.TimestampCharMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampFloatMap;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.graph.api.types.TimestampLongMap;
import org.gephi.graph.api.types.TimestampShortMap;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalBooleanMap;
import org.gephi.graph.api.types.IntervalByteMap;
import org.gephi.graph.api.types.IntervalCharMap;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalFloatMap;
import org.gephi.graph.api.types.IntervalIntegerMap;
import org.gephi.graph.api.types.IntervalLongMap;
import org.gephi.graph.api.types.IntervalShortMap;
import org.gephi.graph.api.types.IntervalStringMap;
import org.gephi.graph.api.types.TimestampSet;
import static org.gephi.graph.impl.GraphStoreConfiguration.ENABLE_ELEMENT_LABEL;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.gephi.graph.impl.GraphStoreConfiguration.ENABLE_ELEMENT_TIME_SET;

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
    public void testGetAttributes() {
        NodeImpl nodeImpl = new NodeImpl(0);
        Assert.assertNotNull(nodeImpl.getAttributes());
    }

    @Test
    public void testSetAttributeColumn() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1);

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertEquals(node.attributes[getFirstNonPropertyIndex()], 1);
        Assert.assertEquals(node.getAttribute(column), 1);
    }

    @Test
    public void testSetAttributeString() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute("age", 1);

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertEquals(node.attributes[getFirstNonPropertyIndex()], 1);
        Assert.assertEquals(node.getAttribute(column), 1);
    }

    @Test
    public void testSetAttributeStandardizedType() {
        GraphStore store = new GraphStore();
        store.nodeTable.store
                .addColumn(new ColumnImpl("arr1", Integer[].class, "Array", null, Origin.DATA, true, false));
        store.nodeTable.store.addColumn(new ColumnImpl("arr2", int[].class, "Array", null, Origin.DATA, true, false));
        Column column1 = store.nodeTable.store.getColumn("arr1");
        Column column2 = store.nodeTable.store.getColumn("arr2");

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column1, new Integer[] { 42 });
        node.setAttribute(column2, new Integer[] { 42 });

        Assert.assertEquals((int[]) node.getAttribute(column1), new int[] { 42 });
        Assert.assertEquals((int[]) node.getAttribute(column2), new int[] { 42 });
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeStringNotFound() {
        GraphStore store = new GraphStore();
        generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute("foo", 1);
    }

    @Test
    public void testSetAttributeList() {
        GraphStore store = new GraphStore();
        Column column = generateBasicListColumn(store);

        List l = new ArrayList();

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, l);
        Assert.assertEquals(node.getAttribute(column), l);

        l.add("foo");
        l.add("bar");
        Assert.assertEquals(node.getAttribute(column), new ArrayList());
        node.setAttribute(column, l);
        Assert.assertEquals(node.getAttribute(column), l);
    }

    @Test
    public void testSetAttributeSet() {
        GraphStore store = new GraphStore();
        Column column = generateBasicSetColumn(store);

        Set s = new HashSet();

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, s);
        Assert.assertEquals(node.getAttribute(column), s);

        s.add("foo");
        s.add("bar");
        Assert.assertEquals(node.getAttribute(column), new HashSet());
        node.setAttribute(column, s);
        Assert.assertEquals(node.getAttribute(column), s);
    }

    @Test
    public void testSetAttributeMap() {
        GraphStore store = new GraphStore();
        Column column = generateBasicMapColumn(store);

        Map m = new HashMap();

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, m);
        Assert.assertEquals(node.getAttribute(column), m);

        m.put("foo", "bar");
        m.put("bar", 1);
        Assert.assertEquals(node.getAttribute(column), new HashMap());
        node.setAttribute(column, m);
        Assert.assertEquals(node.getAttribute(column), m);
    }

    @Test
    public void testSetAttributeTimestamp() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        TimestampIntegerMap ti = new TimestampIntegerMap();
        ti.put(1.0, 42);
        ti.put(2.0, 10);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, ti);

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertEquals(node.attributes[getFirstNonPropertyIndex()], ti);
        Assert.assertEquals(node.getAttribute(column), ti);
    }

    @Test
    public void testSetAttributeTimeset() {
        GraphStore store = new GraphStore();
        Column column = store.nodeTable.getColumn("timeset");

        TimestampSet ti = new TimestampSet();
        ti.add(1.0);
        ti.add(2.0);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, ti);

        Assert.assertEquals(node.getAttribute(column), ti);
    }

    @Test
    public void testReplaceAttribute() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute("age", 1);
        node.setAttribute("age", 2);

        Assert.assertEquals(node.getAttribute(column), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeUnknownColumn() {
        GraphStore store = new GraphStore();
        ColumnImpl columnImpl = new ColumnImpl("0", String.class, "title", "", Origin.DATA, false, false);
        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(columnImpl, "0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeOtherStoreColumn() {
        GraphStore otherStore = new GraphStore();
        Column column = generateBasicColumn(otherStore);
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, "0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeWrongType() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, "a");
    }

    @Test
    public void testSetAttributeNull() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, null);

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertNull(node.attributes[getFirstNonPropertyIndex()]);
        Assert.assertNull(node.getAttribute(column));
    }

    @Test
    public void testSetAttributeTimestampColumn() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, 2.0);
        node.setAttribute(column, 2, 1.0);

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertEquals(node.attributes[getFirstNonPropertyIndex()].getClass(), TimestampIntegerMap.class);
        Assert.assertEquals(node.getAttribute(column, 2.0), 1);
        Assert.assertEquals(node.getAttribute(column, 1.0), 2);
    }

    @Test
    public void testSetAttributeIntervalColumn() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, new Interval(3.0, 4.0));
        node.setAttribute(column, 2, new Interval(1.0, 2.0));

        Assert.assertEquals(node.attributes.length, 1 + getElementPropertiesLength());
        Assert.assertEquals(node.attributes[getFirstNonPropertyIndex()].getClass(), IntervalIntegerMap.class);
        Assert.assertEquals(node.getAttribute(column, new Interval(3.0, 4.0)), 1);
        Assert.assertEquals(node.getAttribute(column, new Interval(1.0, 2.0)), 2);
    }

    @Test
    public void testSetAttributeTimestampString() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute("age", 1, 2.0);

        Assert.assertEquals(node.getAttribute(column, 2.0), 1);
    }

    @Test
    public void testSetAttributeIntervalString() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute("age", 1, new Interval(1.0, 2.0));

        Assert.assertEquals(node.getAttribute(column, new Interval(1.0, 2.0)), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeNonTimestamp() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, 2.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetAttributeNonInterval() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, new Interval(1.0, 2.0));
    }

    @Test
    public void testSetNullToNewTimeIndexedNodeAttribute() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();
        Column col1 = table.addColumn("foo", TimestampDoubleMap.class);

        Node n1 = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n1);
        n1.setAttribute(col1, null);

        Assert.assertNull(n1.getAttribute(col1));
    }

    @Test
    public void testReplaceTimestampAttribute() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, 2.0);
        node.setAttribute(column, 2, 2.0);

        Assert.assertEquals(node.getAttribute(column, 2.0), 2);
    }

    @Test
    public void testReplaceIntervalAttribute() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, new Interval(1.0, 2.0));
        node.setAttribute(column, 2, new Interval(1.0, 2.0));

        Assert.assertEquals(node.getAttribute(column, new Interval(1.0, 2.0)), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNaNTimestamp() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInfiniteTimestamp() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testGetAttributeColumn() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
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

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1);
        Object res = node.getAttribute(column.getId());
        Assert.assertEquals(res, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetAttributeKeyUnknown() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);
        node.getAttribute("foo");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetAttributeUnknownColumn() {
        GraphStore store = new GraphStore();
        ColumnImpl columnImpl = new ColumnImpl("0", String.class, "title", "", Origin.DATA, false, false);
        NodeImpl node = new NodeImpl("0", store);
        node.getAttribute(columnImpl);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetAttributeOtherStoreColumn() {
        GraphStore otherStore = new GraphStore();
        Column column = generateBasicColumn(otherStore);
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);
        node.getAttribute(column);
    }

    @Test
    public void testGetAttributeTimestampColumn() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, 1.0);

        Assert.assertEquals(node.getAttribute(column, 1.0), 1);
        Assert.assertNull(node.getAttribute(column, 2.0));
    }

    @Test
    public void testGetAttributeIntervalColumn() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, new Interval(1.0, 2.0));

        Assert.assertEquals(node.getAttribute(column, new Interval(1.0, 2.0)), 1);
        Assert.assertNull(node.getAttribute(column, new Interval(2.0, 3.0)));
    }

    @Test
    public void testGetAttributeTimestampColumnNull() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        Assert.assertNull(node.getAttribute(column, 1.0));
    }

    @Test
    public void testGetAttributeIntervalColumnNull() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        Assert.assertNull(node.getAttribute(column, new Interval(1.0, 2.0)));
    }

    @Test
    public void testGetAttributeTimestampString() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, 1.0);

        Assert.assertEquals(node.getAttribute("age", 1.0), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetAttributeTimestampStringUnknown() {
        GraphStore store = new GraphStore();
        NodeImpl node = new NodeImpl("0", store);

        node.getAttribute("foo", 1.0);
    }

    @Test
    public void testGetAttributeIntervalString() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1, new Interval(1.0, 2.0));

        Assert.assertEquals(node.getAttribute("age", new Interval(1.0, 2.0)), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetAttributeIntervalStringUnknown() {
        GraphStore store = new GraphStore();
        NodeImpl node = new NodeImpl("0", store);

        node.getAttribute("foo", new Interval(1.0, 2.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetAttributeNonTimestamp() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1);

        node.getAttribute(column, 1.0);
    }

    @Test
    public void testGetDefaultValue() {
        GraphStore store = new GraphStore();
        Integer defaultValue = 25;
        Column column = new ColumnImpl("age", Integer.class, "Age", defaultValue, Origin.DATA, true, false);
        store.nodeTable.store.addColumn(column);

        NodeImpl node = new NodeImpl("0", store);
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
        NodeImpl node = new NodeImpl("0", store);
        Set<String> pk = node.getAttributeKeys();
        Assert.assertTrue(pk.size() == getElementPropertiesLength());
    }

    @Test
    public void testGetAttributeKeys() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        Set<String> pk = node.getAttributeKeys();
        Assert.assertTrue(pk.contains(column.getId()));
        Assert.assertEquals(pk.size(), 1 + getElementPropertiesLength());
    }

    @Test
    public void testGetAttributeColumnsEmpty() {
        GraphStore store = new GraphStore();
        NodeImpl node = new NodeImpl("0", store);
        Iterable<Column> pk = node.getAttributeColumns();
        Assert.assertNotNull(pk);
        Iterator<Column> itr = pk.iterator();
        Assert.assertNotNull(itr);
        int size = 0;
        for (; itr.hasNext();) {
            Column c = itr.next();
            Assert.assertTrue(c.isProperty());
            size++;
        }
        Assert.assertTrue(size == getElementPropertiesLength());
    }

    @Test
    public void testGetAttributeColumns() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        ColumnIterable ci = node.getAttributeColumns();
        Assert.assertTrue(ci.toList().contains(column));
        Assert.assertEquals(ci.toList().size(), 1 + getElementPropertiesLength());
    }

    @Test
    public void testLabel() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);

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

        NodeImpl node = new NodeImpl("0", store);
        Assert.assertTrue(node.addTimestamp(1.0));

        Assert.assertTrue(node.hasTimestamp(1.0));
    }

    @Test
    public void testRemoveTimestamp() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);
        node.addTimestamp(1.0);
        Assert.assertTrue(node.removeTimestamp(1.0));
        Assert.assertFalse(node.hasTimestamp(1.0));
    }

    @Test
    public void testHasTimestampEmpty() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);
        Assert.assertFalse(node.hasTimestamp(1.0));
    }

    @Test
    public void testAddInterval() {
        GraphStore store = getIntervalGraphStore();

        NodeImpl node = new NodeImpl("0", store);
        Assert.assertTrue(node.addInterval(new Interval(1.0, 2.0)));

        Assert.assertTrue(node.hasInterval(new Interval(1.0, 2.0)));
    }

    @Test
    public void testRemoveInterval() {
        GraphStore store = getIntervalGraphStore();

        NodeImpl node = new NodeImpl("0", store);
        node.addInterval(new Interval(1.0, 2.0));
        Assert.assertTrue(node.removeInterval(new Interval(1.0, 2.0)));
        Assert.assertFalse(node.hasInterval(new Interval(1.0, 2.0)));
    }

    @Test
    public void testHasIntervalEmpty() {
        GraphStore store = getIntervalGraphStore();

        NodeImpl node = new NodeImpl("0", store);
        Assert.assertFalse(node.hasInterval(new Interval(1.0, 2.0)));
    }

    @Test
    public void testRemoveAttribute() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 14);
        store.addNode(node);

        Object r = node.removeAttribute(column);
        Assert.assertEquals(r, 14);

        Assert.assertNull(node.getAttribute(column));
    }

    @Test
    public void testRemoveAttributeOnNewColumnWithoutSettingValue() {
        GraphStore store = new GraphStore();
        NodeImpl node = new NodeImpl("0", store);
        store.addNode(node);

        Column column = generateBasicColumn(store);

        Assert.assertNull(node.getAttribute(column));
        Assert.assertNull(node.removeAttribute(column));
        Assert.assertNull(node.getAttribute(column));

        node.setAttribute(column, 14);
        Assert.assertEquals(node.getAttribute(column), 14);
    }

    @Test
    public void testRemoveAttributeBoolean() {
        GraphStore store = new GraphStore();
        Column column = generateBasicBooleanColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, true);
        store.addNode(node);

        Object r = node.removeAttribute(column);
        Assert.assertEquals(r, true);

        Assert.assertNull(node.getAttribute(column));
    }

    @Test
    public void testRemoveAttributeByString() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 14);
        store.addNode(node);

        Object r = node.removeAttribute("age");
        Assert.assertEquals(r, 14);

        Assert.assertNull(node.getAttribute("age"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveAttributeByStringUnknown() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 14);
        store.addNode(node);

        node.removeAttribute("foo");
    }

    @Test
    public void testRemoveTimestampAttribute() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 15, 1.0);
        store.addNode(node);

        Assert.assertNotNull(node.removeAttribute(column));
        Assert.assertNull(node.getAttribute(column, 1.0));
    }

    @Test
    public void testRemoveIntervalAttribute() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 15, new Interval(1.0, 2.0));
        store.addNode(node);

        Assert.assertNotNull(node.removeAttribute(column));
        Assert.assertNull(node.getAttribute(column, new Interval(1.0, 2.0)));
    }

    @Test
    public void testRemoveTimestampSet() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);
        node.addTimestamp(1.0);

        Column col = store.nodeTable.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
        Assert.assertNotNull(node.removeAttribute(col));
        Assert.assertNull(node.getAttribute(col));
        Assert.assertEquals(node.getTimestamps(), new double[0]);
    }

    @Test
    public void testRemoveIntervalSet() {
        GraphStore store = getIntervalGraphStore();

        NodeImpl node = new NodeImpl("0", store);
        node.addInterval(new Interval(1.0, 2.0));

        Column col = store.nodeTable.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
        Assert.assertNotNull(node.removeAttribute(col));
        Assert.assertNull(node.getAttribute(col));
        Assert.assertEquals(node.getIntervals(), new Interval[0]);
    }

    @Test
    public void testRemoveAttributeWithTimestamp() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 15, 1.0);
        store.addNode(node);

        Assert.assertEquals(node.removeAttribute(column, 1.0), 15);
        Assert.assertNull(node.getAttribute(column, 1.0));
    }

    @Test
    public void testRemoveAttributeWithInterval() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 15, new Interval(1.0, 2.0));
        store.addNode(node);

        Assert.assertEquals(node.removeAttribute(column, new Interval(1.0, 2.0)), 15);
        Assert.assertNull(node.getAttribute(column, new Interval(1.0, 2.0)));
    }

    @Test
    public void testRemoveAttributeWithTimestampByString() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 15, 1.0);
        store.addNode(node);

        Assert.assertEquals(node.removeAttribute("age", 1.0), 15);
        Assert.assertNull(node.getAttribute(column, 1.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveAttributeWithTimestampByStringUnknown() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 15, 1.0);
        store.addNode(node);

        node.removeAttribute("foo", 1.0);
    }

    @Test
    public void testRemoveAttributeWithIntervalByString() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 15, new Interval(1.0, 2.0));
        store.addNode(node);

        Assert.assertEquals(node.removeAttribute("age", new Interval(1.0, 2.0)), 15);
        Assert.assertNull(node.getAttribute(column, new Interval(1.0, 2.0)));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveAttributeWithIntervalByStringUnknown() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 15, new Interval(1.0, 2.0));
        store.addNode(node);

        node.removeAttribute("foo", new Interval(1.0, 2.0));
    }

    @Test
    public void testGetTimestampsEmpty() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);
        Assert.assertEquals(node.getTimestamps(), new double[0]);
    }

    @Test
    public void testGetIntervalsEmpty() {
        GraphStore store = getIntervalGraphStore();

        NodeImpl node = new NodeImpl("0", store);
        Assert.assertEquals(node.getIntervals(), new Interval[0]);
    }

    @Test
    public void testGetTimestamps() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);
        node.addTimestamp(1.0);
        node.addTimestamp(2.0);

        Assert.assertEquals(node.getTimestamps(), new double[] { 1.0, 2.0 });

        node.removeTimestamp(1.0);

        Assert.assertEquals(node.getTimestamps(), new double[] { 2.0 });
    }

    @Test
    public void testGetIntervals() {
        GraphStore store = getIntervalGraphStore();

        Interval i1 = new Interval(1.0, 2.0);
        Interval i2 = new Interval(3.0, 4.0);

        NodeImpl node = new NodeImpl("0", store);
        node.addInterval(i1);
        node.addInterval(i2);

        Assert.assertEquals(node.getIntervals(), new Interval[] { i1, i2 });

        node.removeInterval(i1);

        Assert.assertEquals(node.getIntervals(), new Interval[] { i2 });
    }

    @Test
    public void testGetAttributesTimestamp() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 12, 1.0);
        node.setAttribute(column, 14, 3.0);
        node.setAttribute(column, 13, 2.0);

        Iterator<Map.Entry> itr = node.getAttributes(column).iterator();
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Object> entry1 = itr.next();
        Assert.assertEquals(entry1.getKey(), 1.0);
        Assert.assertEquals(entry1.getValue(), 12);
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Object> entry2 = itr.next();
        Assert.assertEquals(entry2.getKey(), 2.0);
        Assert.assertEquals(entry2.getValue(), 13);
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Object> entry3 = itr.next();
        Assert.assertEquals(entry3.getKey(), 3.0);
        Assert.assertEquals(entry3.getValue(), 14);
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testGetAttributesInterval() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 12, new Interval(3.0, 4.0));
        node.setAttribute(column, 14, new Interval(1.0, 2.0));

        Iterator<Map.Entry> itr = node.getAttributes(column).iterator();
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Object> entry1 = itr.next();
        Assert.assertEquals(entry1.getKey(), new Interval(1.0, 2.0));
        Assert.assertEquals(entry1.getValue(), 14);
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Object> entry2 = itr.next();
        Assert.assertEquals(entry2.getKey(), new Interval(3.0, 4.0));
        Assert.assertEquals(entry2.getValue(), 12);
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testGetAttributesTimestampEmpty() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);

        Iterator<Map.Entry> itr = node.getAttributes(column).iterator();
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testGetAttributesIntervalEmpty() {
        GraphStore store = getIntervalGraphStore();
        Column column = generateIntervalColumn(store);

        NodeImpl node = new NodeImpl("0", store);

        Iterator<Map.Entry> itr = node.getAttributes(column).iterator();
        Assert.assertFalse(itr.hasNext());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetDynamicAttributesStaticColumn() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.getAttributes(column);
    }

    @Test
    public void testGetAttributeInView() {
        GraphStore store = new GraphStore();
        Column column = generateBasicColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        node.setAttribute(column, 1);

        GraphView view = store.viewStore.createView();

        Assert.assertEquals(node.getAttribute(column, view), 1);
        Assert.assertEquals(node.getAttribute("age", view), 1);
    }

    @Test
    public void testGetTimestampAttributeInView() {
        GraphStore store = new GraphStore();
        Column column = generateTimestampColumn(store);

        NodeImpl node = new NodeImpl("0", store);
        GraphView view = store.viewStore.createView();

        Assert.assertNull(node.getAttribute(column, view));
        node.setAttribute(column, 10, 1.0);
        Assert.assertEquals(node.getAttribute(column, view), 10);
        node.setAttribute(column, 0, 5.0);
        node.setAttribute(column, 20, 2.0);

        store.viewStore.setTimeInterval(view, new Interval(5.0, 5.0));
        Assert.assertEquals(node.getAttribute(column, view), 0);
        store.viewStore.setTimeInterval(view, new Interval(1.0, 2.0));
        Assert.assertEquals(node.getAttribute(column, view), 10);
        column.setEstimator(Estimator.AVERAGE);
        Assert.assertEquals(node.getAttribute(column, view), 15.0);
        Assert.assertEquals(node.getAttribute("age", view), 15.0);
    }

    @Test
    public void testCheckType() {
        GraphStore store = new GraphStore();

        NodeImpl node = new NodeImpl("0", store);
        node.checkType(new ColumnImpl("0", TimestampIntegerMap.class, null, null, Origin.DATA, false, false), 1);
        node.checkType(new ColumnImpl("0", TimestampDoubleMap.class, null, null, Origin.DATA, false, false), 1.0);
        node.checkType(new ColumnImpl("0", TimestampFloatMap.class, null, null, Origin.DATA, false, false), 1f);
        node.checkType(new ColumnImpl("0", TimestampByteMap.class, null, null, Origin.DATA, false, false), (byte) 1);
        node.checkType(new ColumnImpl("0", TimestampShortMap.class, null, null, Origin.DATA, false, false), (short) 1);
        node.checkType(new ColumnImpl("0", TimestampLongMap.class, null, null, Origin.DATA, false, false), 1l);
        node.checkType(new ColumnImpl("0", TimestampCharMap.class, null, null, Origin.DATA, false, false), 'a');
        node.checkType(new ColumnImpl("0", TimestampBooleanMap.class, null, null, Origin.DATA, false, false), true);
        node.checkType(new ColumnImpl("0", TimestampStringMap.class, null, null, Origin.DATA, false, false), "foo");
        node.checkType(new ColumnImpl("0", IntervalIntegerMap.class, null, null, Origin.DATA, false, false), 1);
        node.checkType(new ColumnImpl("0", IntervalDoubleMap.class, null, null, Origin.DATA, false, false), 1.0);
        node.checkType(new ColumnImpl("0", IntervalFloatMap.class, null, null, Origin.DATA, false, false), 1f);
        node.checkType(new ColumnImpl("0", IntervalByteMap.class, null, null, Origin.DATA, false, false), (byte) 1);
        node.checkType(new ColumnImpl("0", IntervalShortMap.class, null, null, Origin.DATA, false, false), (short) 1);
        node.checkType(new ColumnImpl("0", IntervalLongMap.class, null, null, Origin.DATA, false, false), 1l);
        node.checkType(new ColumnImpl("0", IntervalCharMap.class, null, null, Origin.DATA, false, false), 'a');
        node.checkType(new ColumnImpl("0", IntervalBooleanMap.class, null, null, Origin.DATA, false, false), true);
        node.checkType(new ColumnImpl("0", IntervalStringMap.class, null, null, Origin.DATA, false, false), "foo");

    }

    @Test
    public void testGetTable() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        for (Node n : graphStore.getNodes()) {
            Assert.assertSame(n.getTable(), graphStore.getModel().getNodeTable());
        }
    }

    // Utility
    private GraphStore getIntervalGraphStore() {
        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        GraphStore store = graphModel.store;
        return store;
    }

    private Column generateBasicColumn(GraphStore graphStore) {
        graphStore.nodeTable.store
                .addColumn(new ColumnImpl("age", Integer.class, "Age", null, Origin.DATA, true, false));
        return graphStore.nodeTable.store.getColumn("age");
    }

    private Column generateBasicBooleanColumn(GraphStore graphStore) {
        graphStore.nodeTable.store.addColumn(new ColumnImpl("visible", Boolean.class, "Visible", null, Origin.DATA,
                true, false));
        return graphStore.nodeTable.store.getColumn("visible");
    }

    private Column generateBasicListColumn(GraphStore graphStore) {
        graphStore.nodeTable.store
                .addColumn(new ColumnImpl("list", List.class, "List", null, Origin.DATA, true, false));
        return graphStore.nodeTable.store.getColumn("list");
    }

    private Column generateBasicSetColumn(GraphStore graphStore) {
        graphStore.nodeTable.store.addColumn(new ColumnImpl("set", Set.class, "Set", null, Origin.DATA, true, false));
        return graphStore.nodeTable.store.getColumn("set");
    }

    private Column generateBasicMapColumn(GraphStore graphStore) {
        graphStore.nodeTable.store.addColumn(new ColumnImpl("map", Map.class, "Map", null, Origin.DATA, true, false));
        return graphStore.nodeTable.store.getColumn("map");
    }

    private Column generateTimestampColumn(GraphStore graphStore) {
        graphStore.nodeTable.store.addColumn(new ColumnImpl("age", TimestampIntegerMap.class, "Age", null, Origin.DATA,
                false, false));
        return graphStore.nodeTable.store.getColumn("age");
    }

    private Column generateIntervalColumn(GraphStore graphStore) {
        graphStore.nodeTable.store.addColumn(new ColumnImpl("age", IntervalIntegerMap.class, "Age", null, Origin.DATA,
                false, false));
        return graphStore.nodeTable.store.getColumn("age");
    }

    // Properties size
    public int getElementPropertiesLength() {
        return 1 + (ENABLE_ELEMENT_LABEL ? 1 : 0) + (ENABLE_ELEMENT_TIME_SET ? 1 : 0);
    }

    public int getFirstNonPropertyIndex() {
        return getElementPropertiesLength();
    }
}
