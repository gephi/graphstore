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

import org.gephi.graph.api.Interval;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampInternalMapTest {

    @Test
    public void testEmpty() {
        TimestampInternalMap map = new TimestampInternalMap();
        Assert.assertTrue(map.size() == 0);
    }

    @Test
    public void testAddOne() {
        TimestampInternalMap map = new TimestampInternalMap();
        int a = map.addTimestamp(1.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(map.contains(1.0));
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.getTimestampIndex(1.0), 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddInfinityTimestamp() {
        TimestampInternalMap store = new TimestampInternalMap();
        store.addTimestamp(Double.POSITIVE_INFINITY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddNaNTimestamp() {
        TimestampInternalMap store = new TimestampInternalMap();
        store.addTimestamp(Double.NaN);
    }

    @Test
    public void testAddOnGet() {
        TimestampInternalMap map = new TimestampInternalMap();
        int a = map.getTimestampIndex(1.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(map.contains(1.0));
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.getTimestampIndex(1.0), 0);
    }

    @Test
    public void testMultipleGet() {
        TimestampInternalMap map = new TimestampInternalMap();
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
        TimestampInternalMap store = new TimestampInternalMap();
        store.contains(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsInfinity() {
        TimestampInternalMap store = new TimestampInternalMap();
        store.contains(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testRemove() {
        TimestampInternalMap map = new TimestampInternalMap();
        map.addTimestamp(1.0);
        map.removeTimestamp(1.0);

        Assert.assertTrue(map.size() == 0);
        Assert.assertFalse(map.contains(1.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveNaN() {
        TimestampInternalMap store = new TimestampInternalMap();
        store.removeTimestamp(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveInfinity() {
        TimestampInternalMap store = new TimestampInternalMap();
        store.removeTimestamp(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testNotContains() {
        TimestampInternalMap map = new TimestampInternalMap();
        Assert.assertFalse(map.contains(1.0));
    }

    @Test
    public void testAddAfterRemove() {
        TimestampInternalMap map = new TimestampInternalMap();
        map.addTimestamp(1.0);
        map.removeTimestamp(1.0);
        int a = map.addTimestamp(2.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(map.contains(2.0));
    }

    @Test
    public void testGetIndicies() {
        TimestampInternalMap map = new TimestampInternalMap();
        map.addTimestamp(1.0);
        map.addTimestamp(2.0);

        int[] indicies = map.getTimestampIndices(new Interval(1.0, 2.0));
        Assert.assertEquals(indicies, new int[]{0, 1});

        int[] indicies2 = map.getTimestampIndices(new Interval(1.0, 1.0));
        Assert.assertEquals(indicies2, new int[]{0});
    }

    @Test
    public void testGetIndiciesOpen() {
        TimestampInternalMap map = new TimestampInternalMap();
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
        TimestampInternalMap map = new TimestampInternalMap();
        map.addTimestamp(1.0);

        int[] indicies = map.getTimestampIndices(new Interval(1.0, 2.0));
        Assert.assertEquals(indicies, new int[]{0});
    }

    @Test
    public void testHasTimestampIndex() {
        TimestampInternalMap map = new TimestampInternalMap();

        Assert.assertFalse(map.hasTimestampIndex(1.0));

        map.addTimestamp(1.0);

        Assert.assertTrue(map.hasTimestampIndex(1.0));

        map.removeTimestamp(1.0);

        Assert.assertFalse(map.hasTimestampIndex(1.0));
    }

    @Test
    public void testGarbage() {
        TimestampInternalMap store = new TimestampInternalMap();

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
        TimestampInternalMap store = new TimestampInternalMap();
        store.clear();

        store.addTimestamp(1.0);

        store.clear();

        Assert.assertEquals(0, store.size());
    }

    @Test
    public void testDeepEqualsEmpty() {
        TimestampInternalMap store1 = new TimestampInternalMap();
        Assert.assertTrue(store1.deepEquals(store1));

        TimestampInternalMap store2 = new TimestampInternalMap();
        Assert.assertTrue(store1.deepEquals(store2));
    }

    @Test
    public void testDeepHashCodeEmpty() {
        TimestampInternalMap store1 = new TimestampInternalMap();
        Assert.assertEquals(store1.deepHashCode(), store1.deepHashCode());

        TimestampInternalMap store2 = new TimestampInternalMap();
        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
    }

    @Test
    public void testDeepEquals() {
        TimestampInternalMap store1 = new TimestampInternalMap();
        store1.addTimestamp(1.0);
        store1.addTimestamp(2.0);
        store1.addTimestamp(3.0);
        store1.removeTimestamp(1.0);

        TimestampInternalMap store2 = new TimestampInternalMap();
        store2.addTimestamp(1.0);
        store2.addTimestamp(2.0);
        store2.addTimestamp(3.0);
        store2.removeTimestamp(1.0);

        TimestampInternalMap store3 = new TimestampInternalMap();
        store3.addTimestamp(1.0);

        Assert.assertTrue(store1.deepEquals(store2));
        Assert.assertFalse(store1.deepEquals(store3));
    }

    @Test
    public void testDeepHashCode() {
        TimestampInternalMap store1 = new TimestampInternalMap();
        store1.addTimestamp(1.0);
        store1.addTimestamp(2.0);
        store1.addTimestamp(3.0);
        store1.removeTimestamp(1.0);

        TimestampInternalMap store2 = new TimestampInternalMap();
        store2.addTimestamp(1.0);
        store2.addTimestamp(2.0);
        store2.addTimestamp(3.0);
        store2.removeTimestamp(1.0);

        TimestampInternalMap store3 = new TimestampInternalMap();
        store3.addTimestamp(1.0);

        Assert.assertEquals(store1.deepHashCode(), store2.deepHashCode());
        Assert.assertNotEquals(store1.deepHashCode(), store3.deepHashCode());
    }
}
