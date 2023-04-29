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

import java.util.Arrays;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnDiff;
import org.gephi.graph.api.ColumnObserver;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class ColumnObserverTest {

    @Test
    public void testDefaultObserver() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        ColumnObserver observer = column.createColumnObserver(false);
        Assert.assertNotNull(observer);
        Assert.assertSame(observer.getColumn(), column);
        Assert.assertFalse(observer.isDestroyed());
        Assert.assertFalse(observer.hasColumnChanged());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testCreateObserverWhenDisabled() {
        GraphStore store = new GraphStore(null, Configuration.builder().enableObservers(false).build());
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        column.createColumnObserver(false);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDiffWhenDisabled() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        ColumnObserver observer = column.createColumnObserver(false);
        observer.getDiff();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDiffBadState() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        ColumnObserver observer = column.createColumnObserver(true);

        observer.getDiff();
    }

    @Test
    public void testSetAttribute() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();
        store.addNode(node);

        ColumnObserver observer = column.createColumnObserver(false);
        Assert.assertFalse(observer.hasColumnChanged());

        node.setAttribute(column, 1);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertFalse(observer.hasColumnChanged());
    }

    @Test
    public void testSetAttributeDiff() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();
        store.addNode(node);

        ColumnObserver observer = column.createColumnObserver(true);
        node.setAttribute(column, 1);
        Assert.assertTrue(observer.hasColumnChanged());
        ColumnDiff diff = observer.getDiff();
        Assert.assertNotNull(diff);
        Assert.assertEquals(diff.getTouchedElements().toArray(), new Element[] { node });
    }

    @Test
    public void testSetAttributeDiffGrow() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();
        store.addNode(node);

        ColumnObserver observer = column.createColumnObserver(true);
        node.setAttribute(column, 1);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertNotNull(observer.getDiff());

        Node node2 = store.factory.newNode();
        store.addNode(node2);
        node2.setAttribute(column, 2);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertEquals(observer.getDiff().getTouchedElements().toArray(), new Element[] { node2 });
    }

    @Test
    public void testRemoveAttribute() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();
        node.setAttribute(column, 1);
        store.addNode(node);

        ColumnObserver observer = column.createColumnObserver(false);
        Assert.assertFalse(observer.hasColumnChanged());

        node.removeAttribute(column);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertFalse(observer.hasColumnChanged());
    }

    @Test
    public void testRemoveAttributeDiff() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();
        node.setAttribute(column, 1);
        store.addNode(node);

        ColumnObserver observer = column.createColumnObserver(true);
        node.removeAttribute(column);
        Assert.assertTrue(observer.hasColumnChanged());
        ColumnDiff diff = observer.getDiff();
        Assert.assertNotNull(diff);
        Assert.assertEquals(diff.getTouchedElements().toArray(), new Element[] { node });
    }

    @Test
    public void testSetLabel() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;

        Node node = store.factory.newNode();
        store.addNode(node);

        Column labelColumn = table.getColumn(GraphStoreConfiguration.ELEMENT_LABEL_COLUMN_ID);
        ColumnObserver observer = labelColumn.createColumnObserver(false);
        Assert.assertFalse(observer.hasColumnChanged());

        node.setLabel("foo");
        Assert.assertTrue(observer.hasColumnChanged());
    }

    @Test
    public void testAddTimestamp() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;

        Node node = store.factory.newNode();
        store.addNode(node);

        Column timestampColumn = table.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID);
        ColumnObserver observer = timestampColumn.createColumnObserver(false);
        Assert.assertFalse(observer.hasColumnChanged());

        node.addTimestamp(1.0);
        Assert.assertTrue(observer.hasColumnChanged());
    }

    @Test
    public void testRemoveTimestamp() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;

        Node node = store.factory.newNode();
        node.addTimestamp(1.0);
        store.addNode(node);

        Column timestampColumn = table.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID);
        ColumnObserver observer = timestampColumn.createColumnObserver(false);
        Assert.assertFalse(observer.hasColumnChanged());

        node.removeTimestamp(1.0);
        Assert.assertTrue(observer.hasColumnChanged());
    }

    @Test
    public void testSetEdgeWeight() {
        GraphStore store = new GraphStore();
        TableImpl table = store.edgeTable;

        Node n1 = store.factory.newNode();
        Node n2 = store.factory.newNode();
        store.addAllNodes(Arrays.asList(new Node[] { n1, n2 }));
        Edge edge = store.factory.newEdge(n1, n2);
        edge.setWeight(2.0);
        store.addEdge(edge);

        Column weightColumn = table.getColumn(GraphStoreConfiguration.EDGE_WEIGHT_COLUMN_ID);
        ColumnObserver observer = weightColumn.createColumnObserver(false);
        Assert.assertFalse(observer.hasColumnChanged());

        edge.setWeight(3.0);
        Assert.assertTrue(observer.hasColumnChanged());
        edge.setWeight(1.0);
        Assert.assertTrue(observer.hasColumnChanged());
    }

    @Test
    public void testSetDynamicAttribute() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", TimestampIntegerMap.class);

        Node node = store.factory.newNode();
        store.addNode(node);

        ColumnObserver observer = column.createColumnObserver(false);
        Assert.assertFalse(observer.hasColumnChanged());

        node.setAttribute(column, 1, 0.0);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertFalse(observer.hasColumnChanged());

        node.setAttribute(column, 2, 0.0);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertFalse(observer.hasColumnChanged());

        node.setAttribute(column, 1, 1.0);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertFalse(observer.hasColumnChanged());
    }

    @Test
    public void testDestroyObserver() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        ColumnObserver observer = column.createColumnObserver(false);
        observer.destroy();
        Assert.assertTrue(observer.isDestroyed());
    }

    @Test
    public void testDiffRemoveElement() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();
        store.addNode(node);

        ColumnObserver observer = column.createColumnObserver(true);
        node.setAttribute(column, 1);
        store.removeNode(node);

        Assert.assertTrue(observer.hasColumnChanged());
        ColumnDiff diff = observer.getDiff();
        Assert.assertNotNull(diff);
        Assert.assertTrue(diff.getTouchedElements().toCollection().isEmpty());
    }
}
