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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampStoreTest {

    @Test
    public void testEmpty() {
        TimestampMap store = new TimestampMap();

        Assert.assertEquals(store.size(), 0);
    }

    @Test
    public void testAddTimestamp() {
        TimestampMap store = new TimestampMap();

        int pos = store.addTimestamp(1.0);
        Assert.assertEquals(pos, 0);
        int pos2 = store.addTimestamp(2.0);
        Assert.assertEquals(pos2, 1);

        Assert.assertEquals(store.size(), 2);
        Assert.assertEquals(pos, store.getTimestampIndex(1.0));
        Assert.assertEquals(pos2, store.getTimestampIndex(2.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddInfinityTimestamp() {
        TimestampMap store = new TimestampMap();
        store.addTimestamp(Double.POSITIVE_INFINITY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddNaNTimestamp() {
        TimestampMap store = new TimestampMap();
        store.addTimestamp(Double.NaN);
    }

    @Test
    public void testGetTimestampIndex() {
        TimestampMap store = new TimestampMap();

        int pos = store.getTimestampIndex(1.0);
        Assert.assertEquals(pos, 0);
        Assert.assertEquals(store.size(), 1);
    }

    @Test
    public void testContains() {
        TimestampMap store = new TimestampMap();

        store.addTimestamp(1.0);
        Assert.assertTrue(store.contains(1.0));
        Assert.assertFalse(store.contains(2.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsNaN() {
        TimestampMap store = new TimestampMap();
        store.contains(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsInfinity() {
        TimestampMap store = new TimestampMap();
        store.contains(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testRemoveTimestamp() {
        TimestampMap store = new TimestampMap();

        store.addTimestamp(1.0);
        store.removeTimestamp(1.0);

        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.contains(1.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveNaN() {
        TimestampMap store = new TimestampMap();
        store.removeTimestamp(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveInfinity() {
        TimestampMap store = new TimestampMap();
        store.removeTimestamp(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testGarbage() {
        TimestampMap store = new TimestampMap();

        store.addTimestamp(1.0);
        int pos = store.addTimestamp(2.0);
        store.addTimestamp(3.0);
        store.removeTimestamp(2.0);

        Assert.assertEquals(1, store.garbageQueue.size());
        Assert.assertEquals(pos, store.garbageQueue.firstInt());
        Assert.assertEquals(2, store.size());

        int pos2 = store.addTimestamp(6.0);

        Assert.assertEquals(pos, pos2);
        Assert.assertTrue(store.garbageQueue.isEmpty());
        Assert.assertEquals(3, store.size());
    }

    @Test
    public void testClear() {
        TimestampMap store = new TimestampMap();
        store.clear();

        store.addTimestamp(1.0);

        store.clear();

        Assert.assertEquals(0, store.size());
    }

    @Test
    public void testEqualsEmpty() {
        TimestampMap store1 = new TimestampMap();
        Assert.assertEquals(store1, store1);

        TimestampMap store2 = new TimestampMap();
        Assert.assertEquals(store1, store2);
    }

    @Test
    public void testHashCodeEmpty() {
        TimestampMap store1 = new TimestampMap();
        Assert.assertEquals(store1, store1);

        TimestampMap store2 = new TimestampMap();
        Assert.assertEquals(store1.hashCode(), store2.hashCode());
    }

    @Test
    public void testEquals() {
        TimestampMap store1 = new TimestampMap();
        store1.addTimestamp(1.0);
        store1.addTimestamp(2.0);
        store1.addTimestamp(3.0);
        store1.removeTimestamp(1.0);

        TimestampMap store2 = new TimestampMap();
        store2.addTimestamp(1.0);
        store2.addTimestamp(2.0);
        store2.addTimestamp(3.0);
        store2.removeTimestamp(1.0);

        TimestampMap store3 = new TimestampMap();
        store3.addTimestamp(1.0);

        Assert.assertEquals(store1, store2);
        Assert.assertNotEquals(store1, store3);
    }

    @Test
    public void testHashCode() {
        TimestampMap store1 = new TimestampMap();
        store1.addTimestamp(1.0);
        store1.addTimestamp(2.0);
        store1.addTimestamp(3.0);
        store1.removeTimestamp(1.0);

        TimestampMap store2 = new TimestampMap();
        store2.addTimestamp(1.0);
        store2.addTimestamp(2.0);
        store2.addTimestamp(3.0);
        store2.removeTimestamp(1.0);

        TimestampMap store3 = new TimestampMap();
        store3.addTimestamp(1.0);

        Assert.assertEquals(store1.hashCode(), store2.hashCode());
        Assert.assertNotEquals(store1.hashCode(), store3.hashCode());
    }
}
