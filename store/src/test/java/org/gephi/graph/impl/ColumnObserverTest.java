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
import org.gephi.graph.api.ColumnObserver;
import org.gephi.graph.api.Edge;
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
        TableImpl table = new TableImpl(Node.class, false);
        Column column = table.addColumn("0", Integer.class);

        ColumnObserver observer = column.createColumnObserver();
        Assert.assertNotNull(observer);
        Assert.assertSame(observer.getColumn(), column);
        Assert.assertFalse(observer.isDestroyed());
        Assert.assertFalse(observer.hasColumnChanged());
    }

    @Test
    public void testSetAttribute() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();
        store.addNode(node);

        ColumnObserver observer = column.createColumnObserver();
        Assert.assertFalse(observer.hasColumnChanged());

        node.setAttribute(column, 1);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertFalse(observer.hasColumnChanged());
    }

    @Test
    public void testRemoveAttribute() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();
        node.setAttribute(column, 1);
        store.addNode(node);

        ColumnObserver observer = column.createColumnObserver();
        Assert.assertFalse(observer.hasColumnChanged());

        node.removeAttribute(column);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertFalse(observer.hasColumnChanged());
    }

    @Test
    public void testSetLabel() {
        GraphStore store = new GraphStore();
        TableImpl table = store.nodeTable;

        Node node = store.factory.newNode();
        store.addNode(node);

        Column labelColumn = table.getColumn(GraphStoreConfiguration.ELEMENT_LABEL_COLUMN_ID);
        ColumnObserver observer = labelColumn.createColumnObserver();
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
        ColumnObserver observer = timestampColumn.createColumnObserver();
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
        ColumnObserver observer = timestampColumn.createColumnObserver();
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
        store.addAllNodes(Arrays.asList(new Node[]{n1, n2}));
        Edge edge = store.factory.newEdge(n1, n2);
        edge.setWeight(2.0);
        store.addEdge(edge);

        Column weightColumn = table.getColumn(GraphStoreConfiguration.EDGE_WEIGHT_COLUMN_ID);
        ColumnObserver observer = weightColumn.createColumnObserver();
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

        ColumnObserver observer = column.createColumnObserver();
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
        TableImpl table = new TableImpl(Node.class, false);
        Column column = table.addColumn("0", Integer.class);

        ColumnObserver observer = column.createColumnObserver();
        observer.destroy();
        Assert.assertTrue(observer.isDestroyed());
    }
}
