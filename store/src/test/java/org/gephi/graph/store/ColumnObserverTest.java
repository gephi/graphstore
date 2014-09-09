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

import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.ColumnObserver;
import org.gephi.attribute.time.TimestampIntegerSet;
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
        TableImpl table = new TableImpl(new ColumnStore(Node.class, false));
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
        TableImpl table = new TableImpl(store.nodeColumnStore);
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();

        ColumnObserver observer = column.createColumnObserver();
        Assert.assertFalse(observer.hasColumnChanged());

        node.setAttribute(column, 1);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertFalse(observer.hasColumnChanged());
    }

    @Test
    public void testRemoveAttribute() {
        GraphStore store = new GraphStore();
        TableImpl table = new TableImpl(store.nodeColumnStore);
        Column column = table.addColumn("0", Integer.class);

        Node node = store.factory.newNode();
        node.setAttribute(column, 1);

        ColumnObserver observer = column.createColumnObserver();
        Assert.assertFalse(observer.hasColumnChanged());

        node.removeAttribute(column);
        Assert.assertTrue(observer.hasColumnChanged());
        Assert.assertFalse(observer.hasColumnChanged());
    }

    @Test
    public void testSetDynamicAttribute() {
        GraphStore store = new GraphStore();
        TableImpl table = new TableImpl(store.nodeColumnStore);
        Column column = table.addColumn("0", TimestampIntegerSet.class);

        Node node = store.factory.newNode();

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
        TableImpl table = new TableImpl(new ColumnStore(Node.class, false));
        Column column = table.addColumn("0", Integer.class);

        ColumnObserver observer = column.createColumnObserver();
        observer.destroy();
        Assert.assertTrue(observer.isDestroyed());
    }
}
