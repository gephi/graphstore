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

public class IntervalTest {

    @Test
    public void testDefault() {
        Interval i = new Interval(1.0, 5.0);
        Assert.assertEquals(i.getLow(), 1.0);
        Assert.assertEquals(i.getHigh(), 5.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSwappedBounds() {
        new Interval(5.0, 1.0);
    }

    @Test
    public void testIntervalCopyConstructor() {
        Interval i = new Interval(1.0, 5.0);
        Interval j = new Interval(i);
        Assert.assertEquals(j.getLow(), 1.0);
        Assert.assertEquals(j.getHigh(), 5.0);
    }

    @Test
    public void testEquals() {
        Interval i = new Interval(1.0, 5.0);
        Interval j = new Interval(2.0, 5.0);
        Interval k = new Interval(1.0, 3.0);
        Interval l = new Interval(1.0, 5.0);

        Assert.assertFalse(i.equals(j));
        Assert.assertFalse(i.equals(k));
        Assert.assertTrue(i.equals(l));
    }

    @Test
    public void testHashCode() {
        Interval i = new Interval(1.0, 5.0);
        Interval j = new Interval(2.0, 5.0);
        Interval k = new Interval(1.0, 3.0);
        Interval l = new Interval(1.0, 5.0);

        Assert.assertNotEquals(i.hashCode(), j.hashCode());
        Assert.assertNotEquals(i.hashCode(), k.hashCode());
        Assert.assertEquals(i.hashCode(), l.hashCode());
    }

    @Test
    public void testToString() {
        Interval i = new Interval(1.0, 5.0);
        Assert.assertEquals(i.toString(), "[1.0, 5.0]");
    }

    @Test
    public void testCompareLeft() {
        Interval i = new Interval(1.0, 2.0);
        Interval j = new Interval(3.0, 4.0);
        Interval k = new Interval(2.0, 4.0);
        Assert.assertEquals(i.compareTo(j), -1);
        Assert.assertEquals(i.compareTo(k), 0);
    }

    @Test
    public void testCompareRightIncluded() {
        Interval i = new Interval(4.0, 5.0);
        Interval j = new Interval(1.0, 2.0);
        Interval k = new Interval(2.0, 4.0);
        Assert.assertEquals(i.compareTo(j), 1);
        Assert.assertEquals(i.compareTo(k), 0);
    }

    @Test
    public void testCompareOverlap() {
        Interval i = new Interval(1.0, 10.0);
        Interval j = new Interval(4.0, 5.0);
        Assert.assertEquals(i.compareTo(j), 0);
        Assert.assertEquals(j.compareTo(i), 0);
    }

    @Test
    public void testCompareTimetamp() {
        Interval i = new Interval(1.0, 10.0);
        Assert.assertEquals(i.compareTo(1.0), 0);
        Assert.assertEquals(i.compareTo(10.0), 0);
        Assert.assertEquals(i.compareTo(5.0), 0);
        Assert.assertEquals(i.compareTo(0.0), 1);
        Assert.assertEquals(i.compareTo(11.0), -1);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCompareIntervalToNull() {
        Interval i = new Interval(1.0, 5.0);
        i.compareTo((Interval) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCompareToTimestampNull() {
        Interval i = new Interval(1.0, 5.0);
        i.compareTo((Double) null);
    }
}
