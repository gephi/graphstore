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
package org.gephi.graph.api.types;

import org.gephi.graph.api.Interval;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntervalSetTest {

    @Test
    public void testEmpty() {
        IntervalSet set = new IntervalSet();
        Assert.assertEquals(set.size(), 0);
        Assert.assertFalse(set.contains(new Interval(0.0, 1.0)));
        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void testUnique() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(4.0, 5.0)));
        Assert.assertEquals(set.size(), 1);
        Assert.assertFalse(set.isEmpty());
        Assert.assertTrue(set.contains(new Interval(4.0, 5.0)));
    }

    @Test
    public void testContains() {
        IntervalSet set = new IntervalSet();
        set.add(new Interval(4.0, 5.0));

        Assert.assertTrue(set.contains(new Interval(4.0, 5.0)));
        Assert.assertFalse(set.contains(new Interval(0.0, 1.0)));
        Assert.assertFalse(set.contains(new Interval(0.0, 4.0)));
        Assert.assertFalse(set.contains(new Interval(4.0, 4.0)));
        Assert.assertFalse(set.contains(new Interval(5.0, 5.0)));
        Assert.assertFalse(set.contains(new Interval(5.0, 6.0)));
        Assert.assertFalse(set.contains(new Interval(4.0, 7.0)));
        Assert.assertFalse(set.contains(new Interval(3.0, 5.0)));
    }

    @Test
    public void testContainsTimestamp() {
        IntervalSet set = new IntervalSet();
        set.add(new Interval(4.0, 5.0));

        Assert.assertTrue(set.contains(4.0));
        Assert.assertTrue(set.contains(5.0));
        Assert.assertFalse(set.contains(0.0));
    }

    @Test
    public void testSimple() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(4.0, 5.0)));
        Assert.assertTrue(set.add(new Interval(8.0, 9.0)));
        Assert.assertTrue(set.add(new Interval(0.0, 1.0)));
        Assert.assertTrue(set.add(new Interval(2.0, 3.0)));
        Assert.assertTrue(set.add(new Interval(10.0, 11.0)));
        Assert.assertTrue(set.add(new Interval(6.0, 7.0)));
        Assert.assertEquals(set.size(), 6);

        Assert.assertTrue(set.contains(new Interval(0.0, 1.0)));
        Assert.assertTrue(set.contains(new Interval(2.0, 3.0)));
        Assert.assertTrue(set.contains(new Interval(4.0, 5.0)));
        Assert.assertTrue(set.contains(new Interval(6.0, 7.0)));
        Assert.assertTrue(set.contains(new Interval(8.0, 9.0)));
        Assert.assertTrue(set.contains(new Interval(10.0, 11.0)));
    }

    @Test
    public void testDuplicate() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(0.0, 1.0)));
        Assert.assertFalse(set.add(new Interval(0.0, 1.0)));
    }

    @Test
    public void testContinous() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(0.0, 1.0)));
        Assert.assertTrue(set.add(new Interval(1.0, 2.0)));
        Assert.assertTrue(set.add(new Interval(1.0, 1.0)));
        Assert.assertFalse(set.add(new Interval(0.0, 1.0)));
        Assert.assertFalse(set.add(new Interval(1.0, 2.0)));
        Assert.assertFalse(set.add(new Interval(1.0, 1.0)));

        Assert.assertEquals(set.size(), 3);

        Assert.assertTrue(set.contains(new Interval(0.0, 1.0)));
        Assert.assertTrue(set.contains(new Interval(1.0, 2.0)));
        Assert.assertTrue(set.contains(new Interval(1.0, 1.0)));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStartOverlappingAbove() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(4.0, 5.0)));
        set.add(new Interval(4, 6));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStartOverlappingUnder() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(0, 6)));
        set.add(new Interval(0, 5));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStartOverlappingContinuousAbove() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(0.0, 1.0)));
        Assert.assertTrue(set.add(new Interval(1.0, 1.0)));
        Assert.assertTrue(set.add(new Interval(1.0, 2.0)));
        set.add(new Interval(1, 3));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStartOverlappingContinuousUnder() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(0, 0)));
        Assert.assertTrue(set.add(new Interval(0, 6)));
        set.add(new Interval(0, 5));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEndOverlappingOdd() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(2, 8)));
        set.add(new Interval(3, 5));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEndOverlappingUnder() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(2, 8)));
        set.add(new Interval(1, 5));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEndOverlappingAbove() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(2, 8)));
        set.add(new Interval(1, 10));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOverlapCustom() {
        IntervalSet set = new IntervalSet();
        Assert.assertTrue(set.add(new Interval(1.0, 2.0)));
        Assert.assertTrue(set.add(new Interval(3.0, 4.0)));
        Assert.assertTrue(set.add(new Interval(2.0, 2.0)));
        set.add(new Interval(2.0, 4.0));
    }

    @Test
    public void testRemoveEmpty() {
        IntervalSet set = new IntervalSet();
        Assert.assertFalse(set.remove(new Interval(0.0, 1.0)));
    }

    @Test
    public void testRemoveUnique() {
        IntervalSet set = new IntervalSet();
        set.add(new Interval(4.0, 5.0));
        Assert.assertTrue(set.remove(new Interval(4.0, 5.0)));
        Assert.assertEquals(set.size(), 0);
        Assert.assertFalse(set.contains(new Interval(4.0, 5.0)));
        Assert.assertFalse(set.remove(new Interval(4.0, 5.0)));
    }

    @Test
    public void testRemoveContinuous() {
        IntervalSet set = new IntervalSet();
        set.add(new Interval(0.0, 1.0));
        set.add(new Interval(1.0, 1.0));
        set.add(new Interval(1.0, 2.0));

        Assert.assertTrue(set.remove(new Interval(1.0, 1.0)));
        Assert.assertTrue(set.remove(new Interval(0.0, 1.0)));
        Assert.assertTrue(set.remove(new Interval(1.0, 2.0)));

        Assert.assertEquals(set.size(), 0);
        Assert.assertFalse(set.contains(new Interval(1.0, 2.0)));
        Assert.assertFalse(set.contains(new Interval(1.0, 1.0)));
        Assert.assertFalse(set.contains(new Interval(0.0, 1.0)));
    }

    @Test
    public void testRemoveAndAdd() {
        IntervalSet set = new IntervalSet();
        set.add(new Interval(4.0, 5.0));
        set.add(new Interval(6.0, 8.0));
        Assert.assertTrue(set.remove(new Interval(6.0, 8.0)));
        Assert.assertTrue(set.add(new Interval(5.0, 9.0)));
        Assert.assertEquals(set.size(), 2);
        Assert.assertTrue(set.contains(new Interval(5.0, 9.0)));
        Assert.assertFalse(set.contains(new Interval(6.0, 8.0)));
    }

    @Test
    public void testClear() {
        IntervalSet set = new IntervalSet();

        set.add(new Interval(1.0, 2.0));

        set.clear();

        Assert.assertTrue(set.isEmpty());
        Assert.assertFalse(set.contains(new Interval(1.0, 2.0)));
    }

    @Test
    public void testEquals() {
        IntervalSet set1 = new IntervalSet();
        set1.add(new Interval(6.0, 7.0));
        set1.add(new Interval(1.0, 2.0));

        IntervalSet set2 = new IntervalSet();
        set2.add(new Interval(6.0, 7.0));
        set2.add(new Interval(1.0, 2.0));

        IntervalSet set3 = new IntervalSet();
        set3.add(new Interval(6.0, 7.0));
        set3.add(new Interval(2.0, 3.0));

        Assert.assertTrue(set1.equals(set2));
        Assert.assertTrue(set2.equals(set1));
        Assert.assertFalse(set1.equals(set3));
        Assert.assertFalse(set3.equals(set1));

        Assert.assertTrue(set1.hashCode() == set2.hashCode());
        Assert.assertFalse(set1.hashCode() == set3.hashCode());
    }

    @Test
    public void testEqualsWithCapacity() {
        IntervalSet set1 = new IntervalSet(10);
        set1.add(new Interval(6.0, 7.0));
        set1.add(new Interval(1.0, 2.0));

        IntervalSet set2 = new IntervalSet();
        set2.add(new Interval(6.0, 7.0));
        set2.add(new Interval(1.0, 2.0));

        Assert.assertTrue(set1.equals(set2));
        Assert.assertTrue(set2.equals(set1));

        Assert.assertTrue(set1.hashCode() == set2.hashCode());
    }

    @Test
    public void testEqualsDifferentSize() {
        IntervalSet set1 = new IntervalSet();
        set1.add(new Interval(6.0, 7.0));
        set1.add(new Interval(1.0, 2.0));

        IntervalSet set2 = new IntervalSet();
        set2.add(new Interval(6.0, 7.0));

        Assert.assertFalse(set1.equals(set2));
        Assert.assertFalse(set2.equals(set1));
    }

    @Test
    public void tesGetIntervals() {
        IntervalSet set = new IntervalSet();
        set.add(new Interval(0.0, 5.0));
        Assert.assertEquals(set.getIntervals(), new double[]{0.0, 5.0});
        set.add(new Interval(6.0, 8.0));
        set.remove(new Interval(0.0, 5.0));

        Assert.assertEquals(set.getIntervals(), new double[]{6.0, 8.0});
    }

    @Test
    public void testoArray() {
        IntervalSet set = new IntervalSet();
        set.add(new Interval(0.0, 5.0));
        Assert.assertEquals(set.toArray(), new Interval[]{new Interval(0.0, 5.0)});
        set.add(new Interval(6.0, 8.0));
        set.remove(new Interval(0.0, 5.0));

        Assert.assertEquals(set.toArray(), new Interval[]{new Interval(6.0, 8.0)});
    }

    @Test
    public void testCopyConstructor() {
        IntervalSet set1 = new IntervalSet();
        set1.add(new Interval(1.0, 2.0));
        set1.add(new Interval(4.0, 5.0));

        IntervalSet set2 = new IntervalSet(set1.getIntervals());
        Assert.assertTrue(set1.equals(set2));
        set1.clear();
        Assert.assertEquals(set2.size(), 2);
    }
}
