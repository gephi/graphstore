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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntervalMapTest {

    @Test
    public void testEmpty() {
        for (IntervalMap set : getAllInstances()) {
            Assert.assertTrue(set.isEmpty());
            Assert.assertEquals(set.size(), 0);
        }
    }

    @Test
    public void testPutOne() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            testValues(set, new Interval[] { new Interval(1.0, 2.0) }, new Object[] { defaultValues[0] });
        }
    }

    @Test
    public void testPutTwice() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            Assert.assertFalse(set.put(new Interval(1.0, 2.0), defaultValues[1]));
            testValues(set, new Interval[] { new Interval(1.0, 2.0) }, new Object[] { defaultValues[1] });
        }
    }

    @Test
    public void testMultiplePut() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            Assert.assertTrue(set.put(new Interval(6.0, 8.0), defaultValues[1]));
            testValues(set, new Interval[] { new Interval(1.0, 2.0), new Interval(6.0, 8.0) }, defaultValues);
        }
    }

    @Test
    public void testMultiplePutWithCapacity() {
        for (IntervalMap set : getAllInstances(10)) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            Assert.assertTrue(set.put(new Interval(6.0, 8.0), defaultValues[1]));
            testValues(set, new Interval[] { new Interval(1.0, 2.0), new Interval(6.0, 8.0) }, defaultValues);
        }
    }

    @Test
    public void testMultiplePutWithOverlap() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            Assert.assertTrue(set.put(new Interval(3.0, 4.0), defaultValues[1]));
            Assert.assertTrue(set.put(new Interval(2.0, 2.0), defaultValues[0]));
            Assert.assertTrue(set.put(new Interval(2.0, 3.0), defaultValues[1]));
            defaultValues = new Object[] { defaultValues[0], defaultValues[0], defaultValues[1], defaultValues[1] };
            testValues(set, new Interval[] { new Interval(1.0, 2.0), new Interval(2.0, 2.0), new Interval(2.0,
                    3.0), new Interval(3.0, 4.0) }, defaultValues);
        }
    }

    @Test
    public void testRemove() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(new Interval(1.0, 2.0), defaultValues[0]);
            set.put(new Interval(3.0, 4.0), defaultValues[1]);
            Assert.assertTrue(set.remove(new Interval(1.0, 2.0)));
            Assert.assertFalse(set.contains(new Interval(1.0, 2.0)));
            Assert.assertEquals(set.get(new Interval(3.0, 4.0), defaultValues[0]), defaultValues[1]);
            Assert.assertTrue(set.remove(new Interval(3.0, 4.0)));
            Assert.assertTrue(set.isEmpty());
            Assert.assertFalse(set.contains(new Interval(1.0, 2.0)));
        }
    }

    @Test
    public void testRemoveAdd() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(new Interval(1.0, 2.0), defaultValues[0]);
            set.put(new Interval(3.0, 4.0), defaultValues[1]);
            Assert.assertTrue(set.remove(new Interval(1.0, 2.0)));
            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            testValues(set, new Interval[] { new Interval(1.0, 2.0), new Interval(3.0, 4.0) }, defaultValues);
            Assert.assertTrue(set.remove(new Interval(3.0, 4.0)));
            Assert.assertTrue(set.put(new Interval(3.0, 4.0), defaultValues[1]));
            testValues(set, new Interval[] { new Interval(1.0, 2.0), new Interval(3.0, 4.0) }, defaultValues);
        }
    }

    @Test
    public void testRemoveUnknown() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(new Interval(1.0, 2.0), defaultValues[0]);
            set.put(new Interval(3.0, 4.0), defaultValues[1]);
            Assert.assertFalse(set.remove(new Interval(5.0, 6.0)));
            Assert.assertFalse(set.remove(new Interval(2.0, 3.0)));
            Assert.assertFalse(set.remove(new Interval(1.0, 4.0)));
            testValues(set, new Interval[] { new Interval(1.0, 2.0), new Interval(3.0, 4.0) }, defaultValues);
        }
    }

    @Test
    public void testContains() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertFalse(set.contains(new Interval(1.0, 2.0)));
            set.put(new Interval(1.0, 2.0), defaultValues[0]);
            set.remove(new Interval(1.0, 2.0));
            Assert.assertFalse(set.contains(new Interval(1.0, 2.0)));
        }
    }

    @Test
    public void testClear() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);
            set.put(new Interval(1.0, 2.0), defaultValues[0]);
            set.clear();

            Assert.assertEquals(set.size(), 0);
            Assert.assertTrue(set.isEmpty());
        }
    }

    @Test
    public void testClearAdd() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);
            set.put(new Interval(1.0, 2.0), defaultValues[0]);
            set.clear();
            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            Assert.assertTrue(set.put(new Interval(3.0, 4.0), defaultValues[1]));

            testValues(set, new Interval[] { new Interval(1.0, 2.0), new Interval(3.0, 4.0) }, defaultValues);
        }
    }

    @Test
    public void testPutNull() {
        for (IntervalMap set : getAllInstances()) {
            boolean thrown = false;
            try {
                set.put(new Interval(1.0, 2.0), null);
            } catch (NullPointerException e) {
                thrown = true;
            }
            if (!thrown) {
                Assert.fail("Didn't throw an exception for " + set.getClass());
            }
        }
    }

    @Test
    public void testGetOverlappingIntervals() {
        IntervalIntegerMap i = new IntervalIntegerMap();
        i.put(new Interval(2001, 2004), 42);

        Assert.assertEquals(i.getOverlappingIntervals(2001, 2004), new int[] { 0 });
        Assert.assertEquals(i.getOverlappingIntervals(2001, 2005), new int[] { 0 });
        Assert.assertEquals(i.getOverlappingIntervals(2001, Double.POSITIVE_INFINITY), new int[] { 0 });

        IntervalIntegerMap j = new IntervalIntegerMap();
        j.put(new Interval(2000, 2002), 42);
        j.put(new Interval(2002, 2002), 42);
        j.put(new Interval(2002, 2004), 42);
        j.put(new Interval(2005, 2006), 42);

        Assert.assertEquals(j.getOverlappingIntervals(1998, 1999), new int[] {});
        Assert.assertEquals(j.getOverlappingIntervals(1998, 2001), new int[] { 0 });
        Assert.assertEquals(j.getOverlappingIntervals(1998, 2000), new int[] { 0 });
        Assert.assertEquals(j.getOverlappingIntervals(1998, 2002), new int[] { 0, 1, 2 });
        Assert.assertEquals(j.getOverlappingIntervals(2000, 2001), new int[] { 0 });
        Assert.assertEquals(j.getOverlappingIntervals(2001, 2001), new int[] { 0 });
        Assert.assertEquals(j.getOverlappingIntervals(2003, 2003), new int[] { 2 });
        Assert.assertEquals(j.getOverlappingIntervals(2003, 2007), new int[] { 2, 3 });
        Assert.assertEquals(j.getOverlappingIntervals(2009, 2010), new int[] {});
        Assert.assertEquals(j.getOverlappingIntervals(2002, 2004.9), new int[] { 1, 2 });
    }

    @Test
    public void getIntervalsWeight() {
        IntervalIntegerMap j = new IntervalIntegerMap();
        j.put(new Interval(2000, 2002), 42);
        j.put(new Interval(2003, 2004), 42);
        j.put(new Interval(2005, 2006), 42);

        Assert.assertEquals(j
                .getIntervalsWeight(2000, 2002, j.getOverlappingIntervals(2000, 2002)), new double[] { 2.0 });
        Assert.assertEquals(j
                .getIntervalsWeight(2001, 2002, j.getOverlappingIntervals(2001, 2002)), new double[] { 1.0 });
        Assert.assertEquals(j
                .getIntervalsWeight(2000, 2001, j.getOverlappingIntervals(2000, 2001)), new double[] { 1.0 });
        Assert.assertEquals(j
                .getIntervalsWeight(2000.5, 2001.5, j.getOverlappingIntervals(2000.5, 2001.5)), new double[] { 1.0 });
        Assert.assertEquals(j
                .getIntervalsWeight(1999, 2000, j.getOverlappingIntervals(1999, 2000)), new double[] { 0 });
        Assert.assertEquals(j
                .getIntervalsWeight(1999, 2001, j.getOverlappingIntervals(1999, 2001)), new double[] { 1.0 });
        Assert.assertEquals(j
                .getIntervalsWeight(2000, 2003, j.getOverlappingIntervals(2000, 2003)), new double[] { 2.0, 0 });
        Assert.assertEquals(j
                .getIntervalsWeight(2000, 2004, j.getOverlappingIntervals(2000, 2004)), new double[] { 2.0, 1.0 });
    }

    @Test
    public void testIsSupported() {
        for (IntervalMap set : getAllInstances()) {
            Assert.assertTrue(set.isSupported(Estimator.FIRST));
            Assert.assertTrue(set.isSupported(Estimator.LAST));
            if (Number.class.isAssignableFrom(set.getTypeClass())) {
                Assert.assertTrue(set.isSupported(Estimator.MAX));
                Assert.assertTrue(set.isSupported(Estimator.MIN));
                Assert.assertTrue(set.isSupported(Estimator.AVERAGE));
            }
        }
    }

    @Test
    public void testEstimatorDefault() {
        for (IntervalMap set : getAllInstances()) {
            for (Estimator e : Estimator.values()) {
                if (set.isSupported(e)) {
                    Assert.assertNull(set.get(new Interval(1.0, 2.0), e));
                }
            }
        }
    }

    @Test
    public void testEstimatorNull() {
        for (IntervalMap set : getAllInstances()) {
            set.put(new Interval(0, 2), getDefaultValue(set));

            Assert.assertNull(set.getFirst(new Interval(6.0, 7.0)));
            Assert.assertNull(set.getLast(new Interval(6.0, 7.0)));
            if (set.isSupported(Estimator.MIN)) {
                Assert.assertNull(set.getMin(new Interval(6.0, 7.0)));
            }
            if (set.isSupported(Estimator.MAX)) {
                Assert.assertNull(set.getMax(new Interval(6.0, 7.0)));
            }
            if (set.isSupported(Estimator.AVERAGE)) {
                Assert.assertNull(set.getAverage(new Interval(6.0, 7.0)));
            }
        }
    }

    @Test
    public void testBooleanEstimators() {
        IntervalBooleanMap set = new IntervalBooleanMap();
        set.put(new Interval(0, 2), Boolean.TRUE);
        set.put(new Interval(2, 4), Boolean.FALSE);

        Assert.assertEquals(set.getFirst(new Interval(0, 4)), Boolean.TRUE);
        Assert.assertEquals(set.getLast(new Interval(0, 4)), Boolean.FALSE);

        Assert.assertEquals(set.getMin(new Interval(0, 1)), Boolean.TRUE);
        Assert.assertEquals(set.getMin(new Interval(3, 4)), Boolean.FALSE);
        Assert.assertEquals(set.getMin(new Interval(0, 2)), Boolean.FALSE);

        Assert.assertEquals(set.getMax(new Interval(0, 1)), Boolean.TRUE);
        Assert.assertEquals(set.getMax(new Interval(3, 4)), Boolean.FALSE);
        Assert.assertEquals(set.getMax(new Interval(2, 4)), Boolean.TRUE);
    }

    @Test
    public void testByteEstimators() {
        IntervalByteMap set = new IntervalByteMap();
        set.put(new Interval(0, 2), (byte) 2);
        set.put(new Interval(2, 5), (byte) 4);

        Assert.assertEquals(set.getFirst(new Interval(0, 5)), (byte) 2);
        Assert.assertEquals(set.getLast(new Interval(0, 5)), (byte) 4);

        Assert.assertEquals(set.getMin(new Interval(0, 5)), (byte) 2);
        Assert.assertEquals(set.getMax(new Interval(0, 5)), (byte) 4);
        Assert.assertEquals(set.getAverage(new Interval(0, 5)), (2.0 * 2 + 3.0 * 4) / 5.0);
    }

    @Test
    public void testCharEstimators() {
        IntervalCharMap set = new IntervalCharMap();
        set.put(new Interval(0, 2), 'a');
        set.put(new Interval(2, 5), 'b');

        Assert.assertEquals(set.getFirst(new Interval(0, 5)), 'a');
        Assert.assertEquals(set.getLast(new Interval(0, 5)), 'b');
    }

    @Test
    public void testDoubleEstimators() {
        IntervalDoubleMap set = new IntervalDoubleMap();
        set.put(new Interval(0, 2), 2.0);
        set.put(new Interval(2, 5), 4.0);

        Assert.assertEquals(set.getFirst(new Interval(0, 5)), 2.0);
        Assert.assertEquals(set.getLast(new Interval(0, 5)), 4.0);

        Assert.assertEquals(set.getMin(new Interval(0, 5)), 2.0);
        Assert.assertEquals(set.getMax(new Interval(0, 5)), 4.0);
        Assert.assertEquals(set.getAverage(new Interval(0, 5)), (2.0 * 2.0 + 3.0 * 4.0) / 5.0);
    }

    @Test
    public void testDoubleEstimatorsBig() {
        IntervalDoubleMap set = new IntervalDoubleMap();
        set.put(new Interval(0, 2), Double.MIN_VALUE);
        set.put(new Interval(2, 5), Double.MAX_VALUE);

        BigDecimal expected = new BigDecimal(Double.MIN_VALUE).multiply(new BigDecimal(2.0));
        expected = expected.add(new BigDecimal(Double.MAX_VALUE).multiply(new BigDecimal(3.0)));
        expected = expected.divide(new BigDecimal(5.0));

        Assert.assertEquals(set.getAverage(new Interval(0, 5)), expected.doubleValue());
    }

    @Test
    public void testFloatEstimators() {
        IntervalFloatMap set = new IntervalFloatMap();
        set.put(new Interval(0, 2), 2f);
        set.put(new Interval(2, 5), 4f);

        Assert.assertEquals(set.getFirst(new Interval(0, 5)), 2f);
        Assert.assertEquals(set.getLast(new Interval(0, 5)), 4f);

        Assert.assertEquals(set.getMin(new Interval(0, 5)), 2f);
        Assert.assertEquals(set.getMax(new Interval(0, 5)), 4f);
        Assert.assertEquals(set.getAverage(new Interval(0, 5)), (float) ((2.0 * 2.0 + 3.0 * 4.0) / 5.0));
    }

    @Test
    public void testIntegerEstimators() {
        IntervalIntegerMap set = new IntervalIntegerMap();
        set.put(new Interval(0, 2), 2);
        set.put(new Interval(2, 5), 4);

        Assert.assertEquals(set.getFirst(new Interval(0, 5)), 2);
        Assert.assertEquals(set.getLast(new Interval(0, 5)), 4);

        Assert.assertEquals(set.getMin(new Interval(0, 5)), 2);
        Assert.assertEquals(set.getMax(new Interval(0, 5)), 4);
        Assert.assertEquals(set.getAverage(new Interval(0, 5)), (double) ((2.0 * 2 + 3.0 * 4) / 5.0));
    }

    @Test
    public void testLongEstimators() {
        IntervalLongMap set = new IntervalLongMap();
        set.put(new Interval(0, 2), 2l);
        set.put(new Interval(2, 5), 4l);

        Assert.assertEquals(set.getFirst(new Interval(0, 5)), 2l);
        Assert.assertEquals(set.getLast(new Interval(0, 5)), 4l);

        Assert.assertEquals(set.getMin(new Interval(0, 5)), 2l);
        Assert.assertEquals(set.getMax(new Interval(0, 5)), 4l);
        Assert.assertEquals(set.getAverage(new Interval(0, 5)), (double) ((2.0 * 2l + 3.0 * 4l) / 5.0));
    }

    @Test
    public void testShortEstimators() {
        IntervalShortMap set = new IntervalShortMap();
        set.put(new Interval(0, 2), (short) 2);
        set.put(new Interval(2, 5), (short) 4);

        Assert.assertEquals(set.getFirst(new Interval(0, 5)), (short) 2);
        Assert.assertEquals(set.getLast(new Interval(0, 5)), (short) 4);

        Assert.assertEquals(set.getMin(new Interval(0, 5)), (short) 2);
        Assert.assertEquals(set.getMax(new Interval(0, 5)), (short) 4);
        Assert.assertEquals(set.getAverage(new Interval(0, 5)), (double) ((2.0 * (short) 2 + 3.0 * (short) 4) / 5.0));
    }

    @Test
    public void testEquals() {
        Interval[] indices = new Interval[] { new Interval(1.0, 2.0), new Interval(3.0, 4.0), new Interval(2.0,
                2.0), new Interval(2.0, 3.0) };
        String[] values = new String[] { "a", "z", "e" };
        IntervalStringMap set1 = new IntervalStringMap();
        IntervalStringMap set2 = new IntervalStringMap();
        IntervalStringMap set3 = new IntervalStringMap();
        IntervalStringMap set4 = new IntervalStringMap();
        IntervalStringMap set5 = new IntervalStringMap();

        set1.put(indices[0], values[0]);
        set1.put(indices[1], values[1]);
        set1.put(indices[2], values[2]);

        set2.put(indices[2], values[2]);
        set2.put(indices[1], values[1]);
        set2.put(indices[0], values[0]);

        set3.put(indices[0], "f");
        set3.put(indices[1], "o");
        set3.put(indices[2], "o");

        Assert.assertTrue(set1.equals(set2));
        Assert.assertTrue(set2.equals(set1));

        Assert.assertFalse(set1.equals(set3));
        Assert.assertFalse(set1.equals(set4));
        Assert.assertFalse(set1.equals(set5));

        Assert.assertTrue(set1.hashCode() == set2.hashCode());
        Assert.assertFalse(set1.hashCode() == set3.hashCode());
        Assert.assertFalse(set1.hashCode() == set4.hashCode());
    }

    @Test
    public void testEqualsWithCapacity() {
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        String[] values = new String[] { "a", "z", "e" };
        TimestampStringMap set1 = new TimestampStringMap(10);
        TimestampStringMap set2 = new TimestampStringMap();

        set1.put(indices[0], values[0]);
        set1.put(indices[1], values[1]);
        set1.put(indices[2], values[2]);

        set2.put(indices[2], values[2]);
        set2.put(indices[1], values[1]);
        set2.put(indices[0], values[0]);

        Assert.assertTrue(set1.equals(set2));
        Assert.assertTrue(set2.equals(set1));

        Assert.assertTrue(set1.hashCode() == set2.hashCode());
    }

    @Test
    public void testEqualsWithRemove() {
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        String[] values = new String[] { "a", "z", "e" };
        TimestampStringMap set1 = new TimestampStringMap();
        TimestampStringMap set2 = new TimestampStringMap();

        set1.put(indices[0], values[0]);
        set1.put(indices[1], values[1]);
        set1.put(indices[2], values[2]);
        set1.remove(indices[1]);

        set2.put(indices[0], values[0]);
        set2.put(indices[2], values[2]);

        Assert.assertTrue(set1.equals(set2));
        Assert.assertTrue(set2.equals(set1));

        Assert.assertTrue(set1.hashCode() == set2.hashCode());
    }

    @Test
    public void testCopyConstructor() {
        IntervalStringMap map1 = new IntervalStringMap();
        map1.put(new Interval(1.0, 2.0), "foo");
        map1.put(new Interval(4.0, 5.0), "bar");

        IntervalStringMap map2 = new IntervalStringMap(map1.getIntervals(), map1.toValuesArray());
        Assert.assertTrue(map1.equals(map2));
        map1.clear();
        Assert.assertEquals(map2.size(), 2);
    }

    @Test
    public void testToStringDouble() {
        IntervalStringMap map1 = new IntervalStringMap();
        Assert.assertEquals(map1.toString(), "<empty>");

        map1.put(new Interval(1.0, 2.0), "foo");
        Assert.assertEquals(map1.toString(), "<[1.0, 2.0, foo]>");

        map1.put(new Interval(4.0, 5.5), "bar");
        Assert.assertEquals(map1.toString(), "<[1.0, 2.0, foo]; [4.0, 5.5, bar]>");

        map1.put(new Interval(6.0, 9.0), " 'test' ");
        Assert.assertEquals(map1
                .toString(TimeFormat.DOUBLE), "<[1.0, 2.0, foo]; [4.0, 5.5, bar]; [6.0, 9.0, \" 'test' \"]>");
    }

    @Test
    public void testToStringDate() {
        IntervalStringMap map1 = new IntervalStringMap();
        Assert.assertEquals(map1.toString(TimeFormat.DATE), "<empty>");

        map1.put(new Interval(AttributeUtils.parseDateTime("2012-02-29"),
                AttributeUtils.parseDateTime("2012-03-01")), "foo");
        Assert.assertEquals(map1.toString(TimeFormat.DATE), "<[2012-02-29, 2012-03-01, foo]>");

        map1.put(new Interval(AttributeUtils.parseDateTime("2012-07-17T00:02:21"),
                AttributeUtils.parseDateTime("2012-07-17T00:03:00")), "bar");
        Assert.assertEquals(map1
                .toString(TimeFormat.DATE), "<[2012-02-29, 2012-03-01, foo]; [2012-07-17, 2012-07-17, bar]>");
        Assert.assertEquals(map1
                .toString(TimeFormat.DOUBLE), "<[1330473600000.0, 1330560000000.0, foo]; [1342483341000.0, 1342483380000.0, bar]>");

        // Test with time zone printing:
        Assert.assertEquals(map1
                .toString(TimeFormat.DATE, DateTimeZone.UTC), "<[2012-02-29, 2012-03-01, foo]; [2012-07-17, 2012-07-17, bar]>");
        Assert.assertEquals(map1.toString(TimeFormat.DATE, DateTimeZone
                .forID("+03:00")), "<[2012-02-29, 2012-03-01, foo]; [2012-07-17, 2012-07-17, bar]>");
        Assert.assertEquals(map1.toString(TimeFormat.DATE, DateTimeZone
                .forID("-03:00")), "<[2012-02-28, 2012-02-29, foo]; [2012-07-16, 2012-07-16, bar]>");

        // Test infinity:
        IntervalStringMap mapInf = new IntervalStringMap();
        mapInf.put(new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), "value");
        Assert.assertEquals(mapInf.toString(TimeFormat.DATE), "<[-Infinity, Infinity, value]>");
    }

    @Test
    public void testToStringDatetime() {
        IntervalStringMap map1 = new IntervalStringMap();
        Assert.assertEquals(map1.toString(TimeFormat.DATETIME), "<empty>");

        // Test with default timezone UTC+0
        map1.put(new Interval(AttributeUtils.parseDateTime("2012-02-29"),
                AttributeUtils.parseDateTime("2012-03-01")), "foo");
        Assert.assertEquals(map1
                .toString(TimeFormat.DATETIME), "<[2012-02-29T00:00:00.000Z, 2012-03-01T00:00:00.000Z, foo]>");

        map1.put(new Interval(AttributeUtils.parseDateTime("2012-07-17T01:10:44"),
                AttributeUtils.parseDateTime("2012-07-17T01:10:45")), "bar");
        Assert.assertEquals(map1
                .toString(TimeFormat.DATETIME), "<[2012-02-29T00:00:00.000Z, 2012-03-01T00:00:00.000Z, foo]; [2012-07-17T01:10:44.000Z, 2012-07-17T01:10:45.000Z, bar]>");
        Assert.assertEquals(map1
                .toString(TimeFormat.DOUBLE), "<[1330473600000.0, 1330560000000.0, foo]; [1342487444000.0, 1342487445000.0, bar]>");

        // Test with time zone printing:
        Assert.assertEquals(map1
                .toString(TimeFormat.DATETIME, DateTimeZone.UTC), "<[2012-02-29T00:00:00.000Z, 2012-03-01T00:00:00.000Z, foo]; [2012-07-17T01:10:44.000Z, 2012-07-17T01:10:45.000Z, bar]>");
        Assert.assertEquals(map1.toString(TimeFormat.DATETIME, DateTimeZone
                .forID("+12:30")), "<[2012-02-29T12:30:00.000+12:30, 2012-03-01T12:30:00.000+12:30, foo]; [2012-07-17T13:40:44.000+12:30, 2012-07-17T13:40:45.000+12:30, bar]>");

        // Test with timezone parsing and UTC printing:
        IntervalStringMap map2 = new IntervalStringMap();
        map2.put(new Interval(AttributeUtils.parseDateTime("2012-02-29T00:00:00+02:30"),
                AttributeUtils.parseDateTime("2012-02-29T02:30:00+02:30")), "foo");
        Assert.assertEquals(map2
                .toString(TimeFormat.DATETIME), "<[2012-02-28T21:30:00.000Z, 2012-02-29T00:00:00.000Z, foo]>");

        map2.put(new Interval(AttributeUtils.parseDateTime("2012-02-29T01:10:44+00:00"),
                AttributeUtils.parseDateTime("2012-02-29T01:10:45+00:00")), "bar");
        Assert.assertEquals(map2
                .toString(TimeFormat.DATETIME), "<[2012-02-28T21:30:00.000Z, 2012-02-29T00:00:00.000Z, foo]; [2012-02-29T01:10:44.000Z, 2012-02-29T01:10:45.000Z, bar]>");
        Assert.assertEquals(map2
                .toString(TimeFormat.DOUBLE), "<[1330464600000.0, 1330473600000.0, foo]; [1330477844000.0, 1330477845000.0, bar]>");

        // Test infinity:
        IntervalStringMap mapInf = new IntervalStringMap();
        mapInf.put(new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), "value");
        Assert.assertEquals(mapInf.toString(TimeFormat.DATETIME), "<[-Infinity, Infinity, value]>");
    }

    // UTILITY
    private void testDoubleArrayEquals(double[] a, double[] b) {
        Assert.assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i], b[i]);
        }
    }

    private IntervalMap[] getAllInstances() {
        return new IntervalMap[] { new IntervalStringMap(), new IntervalBooleanMap(), new IntervalFloatMap(), new IntervalDoubleMap(), new IntervalIntegerMap(), new IntervalShortMap(), new IntervalLongMap(), new IntervalByteMap(), new IntervalCharMap() };
    }

    private IntervalMap[] getAllInstances(int capacity) {
        return new IntervalMap[] { new IntervalStringMap(capacity), new IntervalBooleanMap(
                capacity), new IntervalFloatMap(capacity), new IntervalDoubleMap(capacity), new IntervalIntegerMap(
                        capacity), new IntervalShortMap(capacity), new IntervalLongMap(
                                capacity), new IntervalByteMap(capacity), new IntervalCharMap(capacity) };
    }

    private Object[] getTestValues(IntervalMap set) {
        if (set.getTypeClass().equals(String.class)) {
            return new String[] { "foo", "bar" };
        } else if (set.getTypeClass().equals(Boolean.class)) {
            return new Boolean[] { Boolean.TRUE, Boolean.FALSE };
        } else if (set.getTypeClass().equals(Float.class)) {
            return new Float[] { 1f, 2f };
        } else if (set.getTypeClass().equals(Double.class)) {
            return new Double[] { 1.0, 2.0 };
        } else if (set.getTypeClass().equals(Integer.class)) {
            return new Integer[] { 1, 2 };
        } else if (set.getTypeClass().equals(Short.class)) {
            return new Short[] { 1, 2 };
        } else if (set.getTypeClass().equals(Long.class)) {
            return new Long[] { 1l, 2l };
        } else if (set.getTypeClass().equals(Byte.class)) {
            return new Byte[] { 1, 2 };
        } else if (set.getTypeClass().equals(Character.class)) {
            return new Character[] { 'f', 'o' };
        } else {
            throw new RuntimeException("Unrecognized type");
        }
    }

    private Object getDefaultValue(IntervalMap set) {
        if (set.getTypeClass().equals(Boolean.class)) {
            return Boolean.FALSE;
        } else if (set.getTypeClass().equals(Float.class)) {
            return -1f;
        } else if (set.getTypeClass().equals(Double.class)) {
            return -1.0;
        } else if (set.getTypeClass().equals(Integer.class)) {
            return -1;
        } else if (set.getTypeClass().equals(Short.class)) {
            return (short) -1;
        } else if (set.getTypeClass().equals(Long.class)) {
            return -1l;
        } else if (set.getTypeClass().equals(Byte.class)) {
            return (byte) -1;
        } else if (set.getTypeClass().equals(Character.class)) {
            return '#';
        } else if (set.getTypeClass().equals(String.class)) {
            return "-1";
        } else {
            throw new RuntimeException("Unrecognized type " + set.getTypeClass());
        }
    }

    private void testValues(IntervalMap set, Interval[] expectedIntervals, Object[] expectedValues) {
        Class typeClass = set.getTypeClass();

        Assert.assertEquals(expectedIntervals.length, expectedValues.length);
        Assert.assertEquals(set.size(), expectedIntervals.length);
        for (int i = 0; i < expectedIntervals.length; i++) {
            Assert.assertEquals(set.get(expectedIntervals[i], getDefaultValue(set)), expectedValues[i]);
            Assert.assertEquals(set.get(new Interval(99999.0, 999999.0), getDefaultValue(set)), getDefaultValue(set));
            Assert.assertTrue(set.contains(expectedIntervals[i]));

            if (typeClass != String.class) {
                try {
                    Method getMethod = set.getClass().getMethod("get" + typeClass.getSimpleName(), Interval.class);
                    Method getMethodWithDefault = set.getClass()
                            .getMethod("get" + typeClass.getSimpleName(), Interval.class, getMethod.getReturnType());

                    Assert.assertEquals(getMethod.invoke(set, expectedIntervals[i]), expectedValues[i]);
                    Assert.assertEquals(getMethodWithDefault
                            .invoke(set, expectedIntervals[i], getDefaultValue(set)), expectedValues[i]);
                    Assert.assertEquals(getMethodWithDefault
                            .invoke(set, new Interval(99999.0, 999999.0), getDefaultValue(set)), getDefaultValue(set));

                    boolean thrown = false;
                    try {
                        getMethod.invoke(set, new Interval(99999.0, 999999.0));
                    } catch (InvocationTargetException e) {
                        thrown = e.getTargetException().getClass().equals(IllegalArgumentException.class);
                    }
                    if (!thrown) {
                        Assert.fail("The get method didn't throw an IllegalArgumentException exception");
                    }
                } catch (Exception ex) {
                    Assert.fail("Error in getMethod for " + typeClass, ex);
                }
            }
        }
        Assert.assertEquals(set.toValuesArray(), expectedValues);
        if (typeClass != String.class) {
            try {
                Method toArrayMethod = set.getClass().getMethod("to" + typeClass.getSimpleName() + "Array");
                Assert.assertEquals(toArrayMethod.invoke(set), expectedValues);
            } catch (Exception ex) {
                Assert.fail("Error in getMethod for " + typeClass, ex);
            }
        }
        Assert.assertEquals(set.toKeysArray(), expectedIntervals);
    }
}
