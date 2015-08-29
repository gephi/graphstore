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

public class IntervalTest {

    @Test
    public void testDefault() {
        Interval i = new Interval(1.0, 5.0);
        Assert.assertEquals(i.getLow(), 1.0);
        Assert.assertEquals(i.getHigh(), 5.0);
        Assert.assertFalse(i.isLowExcluded());
        Assert.assertFalse(i.isHighExcluded());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSwappedBounds() {
        new Interval(5.0, 1.0);
    }

    @Test
    public void testIntervalCopyConstructor() {
        Interval i = new Interval(1.0, 5.0, true, true);
        Interval j = new Interval(i);
        Assert.assertEquals(j.getLow(), 1.0);
        Assert.assertEquals(j.getHigh(), 5.0);
        Assert.assertTrue(j.isLowExcluded());
        Assert.assertTrue(j.isHighExcluded());
    }

    @Test
    public void testEquals() {
        Interval i = new Interval(1.0, 5.0, true, true);
        Interval j = new Interval(2.0, 5.0, true, true);
        Interval k = new Interval(1.0, 3.0, true, true);
        Interval l = new Interval(1.0, 5.0, false, true);
        Interval m = new Interval(1.0, 5.0, true, false);
        Interval n = new Interval(1.0, 5.0, true, true);

        Assert.assertFalse(i.equals(j));
        Assert.assertFalse(i.equals(k));
        Assert.assertFalse(i.equals(l));
        Assert.assertFalse(i.equals(m));
        Assert.assertTrue(i.equals(n));
    }

    @Test
    public void testHashCode() {
        Interval i = new Interval(1.0, 5.0, true, true);
        Interval j = new Interval(2.0, 5.0, true, true);
        Interval k = new Interval(1.0, 3.0, true, true);
        Interval l = new Interval(1.0, 5.0, false, true);
        Interval m = new Interval(1.0, 5.0, true, false);
        Interval n = new Interval(1.0, 5.0, true, true);

        Assert.assertNotEquals(i.hashCode(), j.hashCode());
        Assert.assertNotEquals(i.hashCode(), k.hashCode());
        Assert.assertNotEquals(i.hashCode(), l.hashCode());
        Assert.assertNotEquals(i.hashCode(), m.hashCode());
        Assert.assertEquals(i.hashCode(), n.hashCode());
    }

    @Test
    public void testToString() {
        Interval i = new Interval(1.0, 5.0, true, true);
        Interval j = new Interval(1.0, 5.0, false, false);
        Assert.assertEquals(i.toString(), "(1.0, 5.0)");
        Assert.assertEquals(j.toString(), "[1.0, 5.0]");
    }

    @Test
    public void testCompareLeftIncluded() {
        Interval i = new Interval(1.0, 2.0, false, false);
        Interval j = new Interval(3.0, 4.0, false, false);
        Interval k = new Interval(2.0, 4.0, false, false);
        Assert.assertEquals(i.compareTo(j), -1);
        Assert.assertEquals(i.compareTo(k), 0);
    }

    @Test
    public void testCompareLeftExcluded() {
        Interval i = new Interval(1.0, 2.0, false, false);
        Interval j = new Interval(1.0, 2.0, false, true);
        Interval k = new Interval(2.0, 4.0, true, true);
        Assert.assertEquals(i.compareTo(k), -1);
        Assert.assertEquals(j.compareTo(k), -1);
    }

    @Test
    public void testCompareRightIncluded() {
        Interval i = new Interval(4.0, 5.0, false, false);
        Interval j = new Interval(1.0, 2.0, false, false);
        Interval k = new Interval(2.0, 4.0, false, false);
        Assert.assertEquals(i.compareTo(j), 1);
        Assert.assertEquals(i.compareTo(k), 0);
    }

    @Test
    public void testCompareRightExcluded() {
        Interval i = new Interval(4.0, 5.0, false, false);
        Interval j = new Interval(4.0, 5.0, true, false);
        Interval k = new Interval(2.0, 4.0, true, true);
        Assert.assertEquals(i.compareTo(k), 1);
        Assert.assertEquals(j.compareTo(k), 1);
    }

    @Test
    public void testCompareOverlap() {
        Interval i = new Interval(1.0, 10.0, false, false);
        Interval j = new Interval(4.0, 5.0, false, false);
        Assert.assertEquals(i.compareTo(j), 0);
        Assert.assertEquals(j.compareTo(i), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCompareToNull() {
        Interval i = new Interval(1.0, 5.0);
        i.compareTo(null);
    }
}
