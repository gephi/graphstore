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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.gephi.graph.api.Interval;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntervalTreeMapTest {

    @Test
    public void testEmpty() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        Assert.assertTrue(m.isEmpty());
        Assert.assertEquals(m.size(), 0);
        Assert.assertEquals(m.getLow(), Double.NEGATIVE_INFINITY);
        Assert.assertEquals(m.getHigh(), Double.POSITIVE_INFINITY);
        Assert.assertNull(m.minimum());
        Assert.assertNull(m.maximum());
    }

    @Test
    public void testPut() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        Assert.assertNull(m.put(new Interval(1.0, 8.0), 42));
        Assert.assertEquals(m.size(), 1);
        Assert.assertFalse(m.isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPutNullKey() {
        new Interval2IntTreeMap().put(null, 42);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPutNullValue() {
        new Interval2IntTreeMap().put(new Interval(1.0, 2.0), null);
    }

    @Test
    public void testPutTwice() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        Assert.assertNull(m.put(new Interval(1.0, 8.0), 42));
        Assert.assertNotNull(m.put(new Interval(1.0, 8.0), 42));
        Assert.assertEquals(m.size(), 1);
    }

    @Test
    public void testGet() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        m.put(new Interval(1.0, 8.0), 42);
        Assert.assertEquals(m.get(new Interval(1.0, 8.0)).intValue(), 42);
    }

    @Test
    public void testRemove() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        Assert.assertNull(m.remove(new Interval(1.0, 2.0)));
        m.put(new Interval(1.0, 8.0), 42);
        Assert.assertNotNull(m.remove(new Interval(1.0, 8.0)));
        Assert.assertEquals(m.size(), 0);
        Assert.assertTrue(m.isEmpty());
    }

    @Test
    public void testRemoveMultiple() {
        Set<Interval> intervals = new HashSet(Arrays.asList(new Interval[]{
            new Interval(1.0, 2.0),
            new Interval(1.0, 4.0),
            new Interval(0.0, 7.0),
            new Interval(1.0, 3.0)
        }));

        Interval2IntTreeMap m = new Interval2IntTreeMap();
        for (Interval i : intervals) {
            m.put(i, 42);
        }

        Interval in = new Interval(1.0, 2.0);
        Assert.assertEquals(m.remove(in).intValue(), 42);
        intervals.remove(in);
        for (Interval i : intervals) {
            Assert.assertNotNull(m.get(i));
        }
    }

    @Test
    public void tetRemoveTwice() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        m.put(new Interval(1.0, 8.0), 42);
        Assert.assertNotNull(m.remove(new Interval(1.0, 8.0)));
        Assert.assertNull(m.remove(new Interval(1.0, 8.0)));
    }

    @Test
    public void testContainsKey() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        Assert.assertFalse(m.containsKey(new Interval(1.0, 8.0)));
        m.put(new Interval(1.0, 8.0), 42);
        Assert.assertTrue(m.containsKey(new Interval(1.0, 8.0)));
        Assert.assertFalse(m.containsKey(new Interval(1.0, 2.0)));
        Assert.assertFalse(m.containsKey(new Interval(3.0, 8.0)));
    }

    @Test
    public void testClear() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        m.put(new Interval(1.0, 8.0), 42);
        m.clear();
        Assert.assertEquals(m.size(), 0);
        Assert.assertTrue(m.isEmpty());
    }

    @Test
    public void testLowHigh() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        m.put(new Interval(1.0, 8.0), 42);
        Assert.assertEquals(m.getLow(), 1.0);
        Assert.assertEquals(m.getHigh(), 8.0);
        m.put(new Interval(3.0, 9.0), 42);
        Assert.assertEquals(m.getLow(), 1.0);
        Assert.assertEquals(m.getHigh(), 9.0);
        m.put(new Interval(1.0, 4.0), 42);
        Assert.assertEquals(m.getLow(), 1.0);
        Assert.assertEquals(m.getHigh(), 9.0);
        m.put(new Interval(-1.0, 12.0), 42);
        Assert.assertEquals(m.getLow(), -1.0);
        Assert.assertEquals(m.getHigh(), 12.0);
    }

    public void testMinMax() {
        Interval i1 = new Interval(1.0, 8.0);
        Interval i2 = new Interval(-1.0, 3.0);
        Interval i3 = new Interval(4.0, 9.0);

        Interval2IntTreeMap m = new Interval2IntTreeMap();
        m.put(i1, 42);
        Assert.assertSame(m.minimum(), i1);
        Assert.assertSame(m.maximum(), i1);
        m.put(i2, 42);
        Assert.assertSame(m.minimum(), i2);
        Assert.assertSame(m.maximum(), i1);
        m.put(i3, 42);
        Assert.assertSame(m.minimum(), i2);
        Assert.assertSame(m.maximum(), i3);
        m.remove(i3);
        m.remove(i2);
        Assert.assertSame(m.minimum(), i1);
        Assert.assertSame(m.maximum(), i1);
    }

    @Test
    public void testRandomTest() {
        Random random = new Random(303l);
        final int min = 5;
        final int max = 200;
        for (int i = 0; i < 100; i++) {
            Set<Interval> intervals = new HashSet<Interval>();
            Interval2IntTreeMap map = new Interval2IntTreeMap();
            double minTree = Double.POSITIVE_INFINITY;
            double maxTree = Double.NEGATIVE_INFINITY;
            while (intervals.size() < 100) {
                int start = random.nextInt(min + max) - min;
                int end = random.nextInt(min + max) - min;
                if (end > start) {
                    Interval interval = new Interval(start, end);
                    if (!intervals.contains(interval)) {
                        map.put(interval, random.nextInt());
                        intervals.add(interval);
                        minTree = Math.min(minTree, start);
                        maxTree = Math.max(maxTree, end);
                    }
                }
            }
            for (Interval interval : intervals) {
                Assert.assertTrue(map.containsKey(interval));
                Assert.assertNotNull(map.get(interval));
            }
            Assert.assertEquals(map.getLow(), minTree);
            Assert.assertEquals(map.getHigh(), maxTree);
            Assert.assertEquals(map.minimum().getLow(), minTree);
            Assert.assertEquals(map.maximum().getHigh(), maxTree);
            Set<Interval> intervalsNotInSet = new HashSet<Interval>();
            while (intervalsNotInSet.size() < 100) {
                int start = random.nextInt(min + max) - min;
                int end = random.nextInt(min + max) - min;
                if (end > start) {
                    Interval interval = new Interval(start, end);
                    if (!intervals.contains(interval)) {
                        intervalsNotInSet.add(interval);
                    }
                }
            }
            for (Interval interval : intervalsNotInSet) {
                Assert.assertFalse(map.containsKey(interval));
            }
            for (Interval interval : intervals) {
                Assert.assertNotNull(map.remove(interval));
            }
            Assert.assertEquals(map.size(), 0);
        }
    }

    @Test
    public void testGetIntervals() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        Assert.assertNotNull(m.getIntervals());
        Assert.assertEquals(m.getIntervals().size(), 0);

        Interval i1 = new Interval(1.0, 8.0);
        Interval i2 = new Interval(-1.0, 3.0);
        Interval i3 = new Interval(4.0, 14.0);
        Interval i4 = new Interval(4.0, 12.0);
        Interval i5 = new Interval(4.0, 13.0);
        Interval i6 = new Interval(4.0, 9.0);

        m.put(i1, 42);
        m.put(i2, 42);
        m.put(i3, 42);
        m.put(i4, 42);
        m.put(i5, 42);
        m.put(i6, 42);

        Assert.assertEquals(m.getIntervals(), Arrays.asList(new Interval[]{i2, i1, i6, i4, i5, i3}));
    }

    @Test
    public void testEntrySetFull() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        Assert.assertFalse(m.entrySet(Interval.INFINITY_INTERVAL).iterator().hasNext());
        Assert.assertFalse(m.values(Interval.INFINITY_INTERVAL).iterator().hasNext());

        Interval i1 = new Interval(1.0, 8.0);
        Interval i2 = new Interval(-1.0, 3.0);
        Interval i3 = new Interval(4.0, 14.0);
        Interval i4 = new Interval(4.0, 9.0);

        m.put(i1, 1);
        m.put(i2, 2);
        m.put(i3, 3);
        m.put(i4, 4);

        Iterator<Entry<Interval, Integer>> itr = m.entrySet(Interval.INFINITY_INTERVAL).iterator();
        Assert.assertNotNull(itr);
        testEntrySetIterator(itr, new Interval[]{i2, i1, i4, i3}, new int[]{2, 1, 4, 3});

        Iterator<Integer> itr2 = m.values(Interval.INFINITY_INTERVAL).iterator();
        Assert.assertNotNull(itr2);
        testValueSetIterator(itr2, new int[]{2, 1, 4, 3});

    }

    @Test
    public void testEntrySetSubSet() {
        Interval2IntTreeMap m = new Interval2IntTreeMap();
        Assert.assertFalse(m.values(0.0).iterator().hasNext());

        Interval i1 = new Interval(1.0, 8.0);
        Interval i2 = new Interval(-1.0, 3.0);
        Interval i3 = new Interval(4.0, 14.0);
        Interval i4 = new Interval(4.0, 9.0);

        m.put(i1, 1);
        m.put(i2, 2);
        m.put(i3, 3);
        m.put(i4, 4);

        Iterator<Entry<Interval, Integer>> itr = m.entrySet(new Interval(3.5, 16.0)).iterator();
        Assert.assertNotNull(itr);
        testEntrySetIterator(itr, new Interval[]{i1, i4, i3}, new int[]{1, 4, 3});

        Iterator<Entry<Interval, Integer>> itr2 = m.entrySet(2.0).iterator();
        Assert.assertNotNull(itr2);
        testEntrySetIterator(itr2, new Interval[]{i2, i1}, new int[]{2, 1});

        Iterator<Integer> itr3 = m.values(new Interval(3.5, 16.0)).iterator();
        Assert.assertNotNull(itr3);
        testValueSetIterator(itr3, new int[]{1, 4, 3});

        Iterator<Integer> itr4 = m.values(2.0).iterator();
        Assert.assertNotNull(itr4);
        testValueSetIterator(itr4, new int[]{2, 1});
    }

    @Test
    public void testEquals() {
        Assert.assertTrue(new Interval2IntTreeMap().equals(new Interval2IntTreeMap()));

        Interval2IntTreeMap i1 = new Interval2IntTreeMap();
        i1.put(new Interval(1.0, 2.0), 42);

        Interval2IntTreeMap i2 = new Interval2IntTreeMap();
        Assert.assertFalse(i1.equals(i2));
        Assert.assertFalse(i2.equals(i1));

        i2.put(new Interval(1.0, 2.0), 42);
        Assert.assertTrue(i1.equals(i2));
        Assert.assertTrue(i2.equals(i1));

        i2.clear();
        i2.put(new Interval(1.0, 2.0), 10);
        Assert.assertFalse(i1.equals(i2));
        Assert.assertFalse(i2.equals(i1));
    }

    @Test
    public void testHashCode() {
        Assert.assertTrue(new Interval2IntTreeMap().hashCode() == new Interval2IntTreeMap().hashCode());

        Interval2IntTreeMap i1 = new Interval2IntTreeMap();
        i1.put(new Interval(1.0, 2.0), 42);

        Interval2IntTreeMap i2 = new Interval2IntTreeMap();
        Assert.assertFalse(i1.hashCode() == i2.hashCode());
        Assert.assertFalse(i2.hashCode() == i1.hashCode());

        i2.put(new Interval(1.0, 2.0), 42);
        Assert.assertTrue(i1.hashCode() == i2.hashCode());
        Assert.assertTrue(i2.hashCode() == i1.hashCode());

        i2.clear();
        i2.put(new Interval(1.0, 2.0), 10);
        Assert.assertFalse(i1.hashCode() == i2.hashCode());
        Assert.assertFalse(i2.hashCode() == i1.hashCode());
    }

    // UTILITY
    private void testEntrySetIterator(Iterator<Map.Entry<Interval, Integer>> itr, Interval[] keys, int[] values) {
        Assert.assertEquals(keys.length, values.length);
        for (int i = 0; i < keys.length; i++) {
            Assert.assertTrue(itr.hasNext());
            Map.Entry<Interval, Integer> e = itr.next();
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getKey(), keys[i]);
            Assert.assertEquals(e.getValue().intValue(), values[i]);
        }
        Assert.assertFalse(itr.hasNext());
    }

    private void testValueSetIterator(Iterator<Integer> itr, int[] values) {
        for (int i = 0; i < values.length; i++) {
            Assert.assertTrue(itr.hasNext());
            Integer e = itr.next();
            Assert.assertNotNull(e);
            Assert.assertEquals(e.intValue(), values[i]);
        }
        Assert.assertFalse(itr.hasNext());
    }
}
