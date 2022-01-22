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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimeStoreTest {

    @Test
    public void testEmpty() {
        TimeStore store = new TimeStore(null, false);
        Assert.assertTrue(store.isEmpty());
    }

    @Test
    public void testDeepEqualsEmpty() {
        TimeStore store1 = new TimeStore(null, false);
        TimeStore store2 = new TimeStore(null, false);

        Assert.assertTrue(store1.deepEquals(store2));
        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
    }

    @Test
    public void testGetMinNull() {
        GraphStore graphStore = new GraphStore();
        TimeStore store = new TimeStore(graphStore, true);
        Assert.assertEquals(store.getMin(graphStore), Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testGetMaxNull() {
        GraphStore graphStore = new GraphStore();
        TimeStore store = new TimeStore(graphStore, true);
        Assert.assertEquals(store.getMax(graphStore), Double.POSITIVE_INFINITY);
    }

    @Test
    public void testGetMin() {
        GraphStore graphStore = new GraphStore();
        TimeStore store = new TimeStore(graphStore, true);
        store.nodeIndexStore.add(1.0);
        store.nodeIndexStore.add(2.0);
        Assert.assertEquals(store.getMin(graphStore), 1.0);
    }

    @Test
    public void testGetMax() {
        GraphStore graphStore = new GraphStore();
        TimeStore store = new TimeStore(graphStore, true);
        store.nodeIndexStore.add(1.0);
        store.nodeIndexStore.add(2.0);
        Assert.assertEquals(store.getMax(graphStore), 2.0);
    }

    @Test
    public void testClearEdges() {
        TimeStore store = new TimeStore(null, true);
        store.nodeIndexStore.add(1.0);
        store.edgeIndexStore.add(2.0);
        store.clearEdges();
        Assert.assertEquals(store.edgeIndexStore.size(), 0);
        Assert.assertEquals(store.nodeIndexStore.size(), 1);
        Assert.assertFalse(store.isEmpty());
    }

    @Test
    public void testClear() {
        TimeStore store = new TimeStore(null, true);
        store.nodeIndexStore.add(1.0);
        store.edgeIndexStore.add(2.0);
        store.clear();
        Assert.assertEquals(store.edgeIndexStore.size(), 0);
        Assert.assertEquals(store.nodeIndexStore.size(), 0);
        Assert.assertTrue(store.isEmpty());
    }

    @Test
    public void testDeepEquals() {
        TimeStore store1 = new TimeStore(null, true);
        TimeStore store2 = new TimeStore(null, true);
        store1.nodeIndexStore.add(1.0);
        store1.edgeIndexStore.add(2.0);
        store2.nodeIndexStore.add(1.0);
        store2.edgeIndexStore.add(2.0);

        Assert.assertTrue(store1.deepEquals(store2));
        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
    }
}
