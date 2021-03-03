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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
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
        Assert.assertEquals(set.getIntervals(), new double[] { 0.0, 5.0 });
        set.add(new Interval(6.0, 8.0));
        set.remove(new Interval(0.0, 5.0));

        Assert.assertEquals(set.getIntervals(), new double[] { 6.0, 8.0 });
    }

    @Test
    public void testoArray() {
        IntervalSet set = new IntervalSet();
        set.add(new Interval(0.0, 5.0));
        Assert.assertEquals(set.toArray(), new Interval[] { new Interval(0.0, 5.0) });
        set.add(new Interval(6.0, 8.0));
        set.remove(new Interval(0.0, 5.0));

        Assert.assertEquals(set.toArray(), new Interval[] { new Interval(6.0, 8.0) });
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

    @Test
    public void testToStringDouble() {
        IntervalSet set1 = new IntervalSet();
        Assert.assertEquals(set1.toString(), "<empty>");

        set1.add(new Interval(1.0, 2.0));
        Assert.assertEquals(set1.toString(), "<[1.0, 2.0]>");

        set1.add(new Interval(4.0, 5.21));
        Assert.assertEquals(set1.toString(TimeFormat.DOUBLE), "<[1.0, 2.0]; [4.0, 5.21]>");
    }

    @Test
    public void testToStringDate() {
        IntervalSet set1 = new IntervalSet();
        Assert.assertEquals(set1.toString(TimeFormat.DATE), "<empty>");

        set1.add(new Interval(AttributeUtils.parseDateTime("2012-02-29"), AttributeUtils.parseDateTime("2012-03-01")));
        Assert.assertEquals(set1.toString(TimeFormat.DATE), "<[2012-02-29, 2012-03-01]>");

        set1.add(new Interval(AttributeUtils.parseDateTime("2012-07-17T00:02:21"), AttributeUtils
                .parseDateTime("2012-07-17T00:03:00")));
        Assert.assertEquals(set1.toString(TimeFormat.DATE), "<[2012-02-29, 2012-03-01]; [2012-07-17, 2012-07-17]>");
        Assert.assertEquals(set1.toString(TimeFormat.DOUBLE), "<[1330473600000.0, 1330560000000.0]; [1342483341000.0, 1342483380000.0]>");

        // Test with time zone printing:
        Assert.assertEquals(set1.toString(TimeFormat.DATE, ZonedDateTime.now(ZoneId.of("UTC"))), "<[2012-02-29, 2012-03-01]; [2012-07-17, 2012-07-17]>");
        Assert.assertEquals(set1.toString(TimeFormat.DATE, ZonedDateTime.now(ZoneId.of("+12:00"))), "<[2012-02-29, 2012-03-01]; [2012-07-17, 2012-07-17]>");
        set1.add(new Interval(AttributeUtils.parseDateTime("2012-07-18T18:30:00"), AttributeUtils
                .parseDateTime("2012-07-18T18:30:01")));
        Assert.assertEquals(set1.toString(TimeFormat.DATE, ZonedDateTime.now(ZoneId.of("+08:00"))), "<[2012-02-29, 2012-03-01]; [2012-07-17, 2012-07-17]; [2012-07-19, 2012-07-19]>");
        Assert.assertEquals(set1.toString(TimeFormat.DATE, ZonedDateTime.now(ZoneId.of("-10:00"))), "<[2012-02-28, 2012-02-29]; [2012-07-16, 2012-07-16]; [2012-07-18, 2012-07-18]>");

        // Test infinity:
        IntervalSet setInf = new IntervalSet();
        setInf.add(new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        Assert.assertEquals(setInf.toString(TimeFormat.DATE), "<[-Infinity, Infinity]>");
    }

    @Test
    public void testToStringDatetime() {
        IntervalSet set1 = new IntervalSet();
        Assert.assertEquals(set1.toString(TimeFormat.DATETIME), "<empty>");

        // Test with default timezone UTC+0
        set1.add(new Interval(AttributeUtils.parseDateTime("2012-02-29"), AttributeUtils.parseDateTime("2012-03-01")));
        Assert.assertEquals(set1.toString(TimeFormat.DATETIME), "<[2012-02-29T00:00:00.000Z, 2012-03-01T00:00:00.000Z]>");

        set1.add(new Interval(AttributeUtils.parseDateTime("2012-07-17T01:10:44"), AttributeUtils
                .parseDateTime("2012-07-17T01:10:45")));
        Assert.assertEquals(set1.toString(TimeFormat.DATETIME), "<[2012-02-29T00:00:00.000Z, 2012-03-01T00:00:00.000Z]; [2012-07-17T01:10:44.000Z, 2012-07-17T01:10:45.000Z]>");
        Assert.assertEquals(set1.toString(TimeFormat.DOUBLE), "<[1330473600000.0, 1330560000000.0]; [1342487444000.0, 1342487445000.0]>");

        // Test with time zone printing:
        Assert.assertEquals(set1.toString(TimeFormat.DATETIME, ZonedDateTime.now(ZoneId.of("UTC"))), "<[2012-02-29T00:00:00.000Z, 2012-03-01T00:00:00.000Z]; [2012-07-17T01:10:44.000Z, 2012-07-17T01:10:45.000Z]>");
        Assert.assertEquals(set1.toString(TimeFormat.DATETIME, ZonedDateTime.now(ZoneId.of("+12:00"))), "<[2012-02-29T12:00:00.000+12:00, 2012-03-01T12:00:00.000+12:00]; [2012-07-17T13:10:44.000+12:00, 2012-07-17T13:10:45.000+12:00]>");

        // Test with timezone parsing and UTC printing:
        IntervalSet set2 = new IntervalSet();
        set2.add(new Interval(AttributeUtils.parseDateTime("2012-02-29T00:00:00+02:30"), AttributeUtils
                .parseDateTime("2012-02-29T02:30:00+02:30")));
        Assert.assertEquals(set2.toString(TimeFormat.DATETIME), "<[2012-02-28T21:30:00.000Z, 2012-02-29T00:00:00.000Z]>");

        set2.add(new Interval(AttributeUtils.parseDateTime("2012-02-29T01:10:44+00:00"), AttributeUtils
                .parseDateTime("2012-02-29T01:10:45+00:00")));
        Assert.assertEquals(set2.toString(TimeFormat.DATETIME), "<[2012-02-28T21:30:00.000Z, 2012-02-29T00:00:00.000Z]; [2012-02-29T01:10:44.000Z, 2012-02-29T01:10:45.000Z]>");
        Assert.assertEquals(set2.toString(TimeFormat.DOUBLE), "<[1330464600000.0, 1330473600000.0]; [1330477844000.0, 1330477845000.0]>");

        // Test infinity:
        IntervalSet setInf = new IntervalSet();
        setInf.add(new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        Assert.assertEquals(setInf.toString(TimeFormat.DATETIME), "<[-Infinity, Infinity]>");
    }
}
