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

import org.gephi.attribute.time.Interval;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampMapTest {

    @Test
    public void testEmpty() {
        TimestampMap map = new TimestampMap();
        Assert.assertTrue(map.size() == 0);
    }

    @Test
    public void testAddOne() {
        TimestampMap map = new TimestampMap();
        int a = map.addTimestamp(1.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(map.contains(1.0));
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.getTimestampIndex(1.0), 0);
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
    public void testAddOnGet() {
        TimestampMap map = new TimestampMap();
        int a = map.getTimestampIndex(1.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(map.contains(1.0));
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.getTimestampIndex(1.0), 0);
    }

    @Test
    public void testMultipleGet() {
        TimestampMap map = new TimestampMap();
        int a = map.getTimestampIndex(1.0);
        int b = map.getTimestampIndex(1.0);

        Assert.assertEquals(a, 0);
        Assert.assertEquals(b, 0);
        Assert.assertTrue(map.contains(1.0));
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.getTimestampIndex(1.0), 0);
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
    public void testRemove() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);
        map.removeTimestamp(1.0);

        Assert.assertTrue(map.size() == 0);
        Assert.assertFalse(map.contains(1.0));
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
    public void testNotContains() {
        TimestampMap map = new TimestampMap();
        Assert.assertFalse(map.contains(1.0));
    }

    @Test
    public void testAddAfterRemove() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);
        map.removeTimestamp(1.0);
        int a = map.addTimestamp(2.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(map.contains(2.0));
    }

    @Test
    public void testGetIndicies() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);
        map.addTimestamp(2.0);

        int[] indicies = map.getTimestampIndices(new Interval(1.0, 2.0));
        Assert.assertEquals(indicies, new int[]{0, 1});

        int[] indicies2 = map.getTimestampIndices(new Interval(1.0, 1.0));
        Assert.assertEquals(indicies2, new int[]{0});
    }

    @Test
    public void testGetIndiciesOpen() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);
        map.addTimestamp(2.0);

        int[] indicies = map.getTimestampIndices(new Interval(1.0, 2.0, true, true));
        Assert.assertEquals(indicies, new int[]{});

        int[] indicies2 = map.getTimestampIndices(new Interval(1.0, 2.0, false, true));
        Assert.assertEquals(indicies2, new int[]{0});

        int[] indicies3 = map.getTimestampIndices(new Interval(1.0, 2.0, true, false));
        Assert.assertEquals(indicies3, new int[]{1});
    }

    @Test
    public void testGetIndiciesUnique() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);

        int[] indicies = map.getTimestampIndices(new Interval(1.0, 2.0));
        Assert.assertEquals(indicies, new int[]{0});
    }

    @Test
    public void testHasTimestampIndex() {
        TimestampMap map = new TimestampMap();

        Assert.assertFalse(map.hasTimestampIndex(1.0));

        map.addTimestamp(1.0);

        Assert.assertTrue(map.hasTimestampIndex(1.0));

        map.removeTimestamp(1.0);

        Assert.assertFalse(map.hasTimestampIndex(1.0));
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
