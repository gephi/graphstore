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
import java.util.Arrays;
import java.util.Collections;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class ColumnNoIndexTest {

    private GraphStore graphStore;
    private ColumnNoIndexImpl<String, Node> fooIndex;
    private ColumnNoIndexImpl<Integer, Node> ageIndex;
    private ColumnNoIndexImpl<Integer, Node> priceIndex;

    @BeforeMethod
    public void setup() {
        graphStore = generateGraphStoreWithColumns();
        Column col = graphStore.nodeTable.getColumn("foo");
        Column col2 = graphStore.nodeTable.getColumn("age");
        Column col3 = graphStore.nodeTable.getColumn("price");

        fooIndex = createIndex(graphStore, col.getId());
        ageIndex = createIndex(graphStore, col2.getId());
        priceIndex = createIndex(graphStore, col3.getId());
    }

    @AfterMethod
    public void cleanUp() {
        fooIndex = null;
        ageIndex = null;
        priceIndex = null;
        graphStore = null;
    }

    @Test
    public void testEmpty() {
        ColumnNoIndexImpl<String, Node> index = createIndex(graphStore, "id");

        Assert.assertTrue(index.values().isEmpty());
        Assert.assertEquals(index.countValues(), 0);
        Assert.assertEquals(index.countElements(), 0);
        Assert.assertFalse(index.isSortable());
        Assert.assertEquals(index.count(null), 0);
        Assert.assertEquals(index.count("foo"), 0);
        Assert.assertFalse(index.get(null).iterator().hasNext());
        Assert.assertFalse(index.get("foo").iterator().hasNext());
    }

    @Test
    public void testEmptyEdgeInstead() {
        ColumnNoIndexImpl<String, Edge> index = createEdgeIndex(graphStore, "id");

        Assert.assertEquals(index.countElements(), 0);
        Assert.assertTrue(index.values().isEmpty());
        Assert.assertFalse(index.get("foo").iterator().hasNext());
    }

    @Test
    public void testCountsAdd() {
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "1", "bar");

        Assert.assertEquals(fooIndex.countValues(), 1);
        Assert.assertEquals(fooIndex.countElements(), 1);

        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "2", "bar");
        Assert.assertEquals(fooIndex.countValues(), 1);
        Assert.assertEquals(fooIndex.countElements(), 2);

        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "3", "foo");
        Assert.assertEquals(fooIndex.countValues(), 2);
        Assert.assertEquals(fooIndex.countElements(), 3);
    }

    @Test
    public void testCountsWithNulls() {
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "1", null);
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "2", null);

        Assert.assertEquals(fooIndex.countValues(), 1);
        Assert.assertEquals(fooIndex.countElements(), 2);
    }

    @Test
    public void testCountsRemove() {
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "1", null);
        graphStore.removeNode(graphStore.getNode("1"));

        Assert.assertEquals(fooIndex.countValues(), 0);
        Assert.assertEquals(fooIndex.countElements(), 0);
    }

    @Test
    public void testCountByValue() {
        Assert.assertEquals(fooIndex.count("bar"), 0);
        Assert.assertEquals(fooIndex.count(null), 0);

        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "1", "bar");
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "2", null);
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "3", "bar");

        Assert.assertEquals(fooIndex.count("bar"), 2);
        Assert.assertEquals(fooIndex.count(null), 1);
    }

    @Test
    public void testValues() {
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "1", "bar");
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "2", null);
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "3", "bar");

        Assert.assertEqualsNoOrder(fooIndex.values().toArray(new String[0]), new String[] { "bar", null });
    }

    @Test
    public void testGet() {
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "1", "bar");
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "2", null);
        addNodeWithAttribute(graphStore, fooIndex.getColumn(), "3", "bar");

        ArrayList<Node> res = new ArrayList<>();
        fooIndex.get("bar").forEach(res::add);
        Assert.assertEqualsNoOrder(res
                .toArray(new Node[0]), new Node[] { graphStore.getNode("1"), graphStore.getNode("3") });

        ArrayList<Node> res2 = new ArrayList<>();
        fooIndex.get(null).forEach(res2::add);
        Assert.assertEqualsNoOrder(res2.toArray(new Node[0]), new Node[] { graphStore.getNode("2") });
    }

    @Test
    public void testIsSortable() {
        Assert.assertFalse(fooIndex.isSortable());
        Assert.assertTrue(ageIndex.isSortable());
    }

    @Test
    public void testGetMinMaxValueEmpty() {
        Assert.assertNull(ageIndex.getMinValue());
        Assert.assertNull(ageIndex.getMaxValue());
    }

    @Test
    public void testGetMinMaxValue() {
        addNodeWithAttribute(graphStore, ageIndex.getColumn(), "1", 12);
        addNodeWithAttribute(graphStore, ageIndex.getColumn(), "2", null);
        addNodeWithAttribute(graphStore, ageIndex.getColumn(), "3", 6);

        Assert.assertEquals(ageIndex.getMinValue(), 6);
        Assert.assertEquals(ageIndex.getMaxValue(), 12);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetMaxValueNotSortable() {
        fooIndex.getMaxValue();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetMinValueNotSortable() {
        fooIndex.getMinValue();
    }

    @Test
    public void testDynamicAttribute() {
        TimestampIntegerMap t = new TimestampIntegerMap();
        t.put(2000.0, 100);
        t.put(2010.0, 150);

        addNodeWithAttribute(graphStore, priceIndex.column, "1", t);

        Assert.assertTrue(priceIndex.isSortable());
        Assert.assertEquals(priceIndex.getMinValue(), 100);
        Assert.assertEquals(priceIndex.getMaxValue(), 100);
        Assert.assertEquals(priceIndex.values(), Collections.singletonList(100));
        Assert.assertEquals(priceIndex.count(100), 1);
        Assert.assertEquals(priceIndex.countElements(), 1);
        Assert.assertEquals(priceIndex.countValues(), 1);
    }

    @Test
    public void testDynamicAttributeWithEstimator() {
        TimestampIntegerMap t = new TimestampIntegerMap();
        t.put(2000.0, 100);
        t.put(2010.0, 150);

        addNodeWithAttribute(graphStore, priceIndex.column, "1", t);
        priceIndex.column.setEstimator(Estimator.AVERAGE);

        Assert.assertEquals(priceIndex.getMinValue(), 125.0);
        Assert.assertEquals(priceIndex.getMaxValue(), 125.0);
    }

    @Test
    public void testVersion() {
        int version = fooIndex.getVersion();
        Node node = graphStore.factory.newNode("1");
        fooIndex.putValue(node, "bar");

        Assert.assertTrue(fooIndex.getVersion() > version);
    }

    private <K, T extends Element> ColumnNoIndexImpl<K, T> createIndex(GraphStore graphStore, String id) {
        return new ColumnNoIndexImpl(graphStore.nodeTable.getColumn(id), graphStore, Node.class);
    }

    private <K, T extends Element> ColumnNoIndexImpl<K, T> createEdgeIndex(GraphStore graphStore, String id) {
        return new ColumnNoIndexImpl(graphStore.nodeTable.getColumn(id), graphStore, Edge.class);
    }

    private Node addNodeWithAttribute(GraphStore store, Column column, String id, Object val) {
        Node node = store.factory.newNode(id);
        node.setAttribute(column, val);
        store.addNode(node);
        return node;
    }

    private GraphStore generateGraphStoreWithColumns() {
        GraphStore graphStore = new GraphStore();
        ColumnStore<Node> columnStore = graphStore.nodeTable.store;
        columnStore.addColumn(new ColumnImpl("foo", String.class, "foo", null, Origin.DATA, false, false));
        columnStore.addColumn(new ColumnImpl("age", Integer.class, "Age", null, Origin.DATA, true, false));
        columnStore
                .addColumn(new ColumnImpl("price", TimestampIntegerMap.class, "Price", null, Origin.DATA, true, false));

        return graphStore;
    }

}
