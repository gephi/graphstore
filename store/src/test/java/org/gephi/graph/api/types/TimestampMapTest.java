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
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimestampMapTest {

    @Test
    public void testEmpty() {
        for (TimestampMap set : getAllInstances()) {
            Assert.assertTrue(set.isEmpty());
            Assert.assertEquals(set.size(), 0);
        }
    }

    @Test
    public void testPutOne() {
        for (TimestampMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertTrue(set.put(1.0, defaultValues[0]));
            testValues(set, new double[] { 1.0 }, new Object[] { defaultValues[0] });
        }
    }

    @Test
    public void testPutTwice() {
        for (TimestampMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertTrue(set.put(1.0, defaultValues[0]));
            Assert.assertFalse(set.put(1.0, defaultValues[1]));
            testValues(set, new double[] { 1.0 }, new Object[] { defaultValues[1] });
        }
    }

    @Test
    public void testMultiplePut() {
        for (TimestampMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertTrue(set.put(1.0, defaultValues[0]));
            Assert.assertTrue(set.put(6.0, defaultValues[1]));
            testValues(set, new double[] { 1.0, 6.0 }, defaultValues);
        }
    }

    @Test
    public void testMultiplePutWithCapacity() {
        for (TimestampMap set : getAllInstances(10)) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertTrue(set.put(1.0, defaultValues[0]));
            Assert.assertTrue(set.put(6.0, defaultValues[1]));
            testValues(set, new double[] { 1.0, 6.0 }, defaultValues);
        }
    }

    @Test
    public void testRemove() {
        for (TimestampMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(1.0, defaultValues[0]);
            set.put(2.0, defaultValues[1]);
            Assert.assertTrue(set.remove(1.0));
            Assert.assertFalse(set.contains(1.0));
            Assert.assertEquals(set.get(2.0, null), defaultValues[1]);
            Assert.assertTrue(set.remove(2.0));
            Assert.assertTrue(set.isEmpty());
            Assert.assertFalse(set.contains(1.0));
        }
    }

    @Test
    public void testRemoveAdd() {
        for (TimestampMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(1.0, defaultValues[0]);
            set.put(2.0, defaultValues[1]);
            Assert.assertTrue(set.remove(1.0));
            Assert.assertTrue(set.put(1.0, defaultValues[0]));
            testValues(set, new double[] { 1.0, 2.0 }, defaultValues);
            Assert.assertTrue(set.remove(2.0));
            Assert.assertTrue(set.put(2.0, defaultValues[1]));
            testValues(set, new double[] { 1.0, 2.0 }, defaultValues);
        }
    }

    @Test
    public void testRemoveUnknown() {
        for (TimestampMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(1.0, defaultValues[0]);
            set.put(2.0, defaultValues[1]);
            Assert.assertFalse(set.remove(3.0));
            Assert.assertFalse(set.remove(0.0));
            testValues(set, new double[] { 1.0, 2.0 }, defaultValues);
        }
    }

    @Test
    public void testContains() {
        for (TimestampMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            Assert.assertFalse(set.contains(1.0));
            set.put(1.0, defaultValues[0]);
            set.remove(1.0);
            Assert.assertFalse(set.contains(1.0));
        }
    }

    @Test
    public void testClear() {
        for (TimestampMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);
            set.put(1.0, defaultValues[0]);
            set.clear();

            Assert.assertEquals(set.size(), 0);
            Assert.assertTrue(set.isEmpty());
        }
    }

    @Test
    public void testClearAdd() {
        for (TimestampMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);
            set.put(1.0, defaultValues[0]);
            set.clear();
            Assert.assertTrue(set.put(1.0, defaultValues[0]));
            Assert.assertTrue(set.put(2.0, defaultValues[1]));

            testValues(set, new double[] { 1.0, 2.0 }, defaultValues);
        }
    }

    @Test
    public void testPutNull() {
        for (TimestampMap set : getAllInstances()) {
            boolean thrown = false;
            try {
                set.put(1.0, null);
            } catch (NullPointerException e) {
                thrown = true;
            }
            if (!thrown) {
                Assert.fail("Didn't throw an exception for " + set.getClass());
            }
        }
    }

    @Test
    public void testGetTimestamps() {
        TimestampDoubleMap set = new TimestampDoubleMap();

        set.put(1.0, 1.0);
        set.put(2.0, 2.0);

        testDoubleArrayEquals(new double[] { 1.0, 2.0 }, set.getTimestamps());
    }

    @Test
    public void testGetTimestampsTrim() {
        TimestampDoubleMap set = new TimestampDoubleMap();

        set.put(1.0, 1.0);
        set.put(2.0, 2.0);
        set.remove(2.0);

        testDoubleArrayEquals(new double[] { 1.0 }, set.getTimestamps());
    }

    @Test
    public void testGetOverlappingTimestamps() {
        TimestampIntegerMap i = new TimestampIntegerMap();
        i.put(2001.0, 42);

        Assert.assertEquals(i.getOverlappingTimestamps(2001, 2002), new int[] { 0 });
        Assert.assertEquals(i.getOverlappingTimestamps(2000, 2001), new int[] { 0 });
        Assert.assertEquals(i.getOverlappingTimestamps(2000, Double.POSITIVE_INFINITY), new int[] { 0 });

        TimestampIntegerMap j = new TimestampIntegerMap();
        j.put(2001.0, 42);
        j.put(2002.0, 42);
        j.put(2004.0, 42);
        j.put(2005.0, 42);

        Assert.assertEquals(j.getOverlappingTimestamps(1998, 1999), new int[] {});
        Assert.assertEquals(j.getOverlappingTimestamps(1998, 2001), new int[] { 0 });
        Assert.assertEquals(j.getOverlappingTimestamps(1998, 2001.5), new int[] { 0 });
        Assert.assertEquals(j.getOverlappingTimestamps(1998, 2002), new int[] { 0, 1 });
        Assert.assertEquals(j.getOverlappingTimestamps(2001, 2001), new int[] { 0 });
        Assert.assertEquals(j.getOverlappingTimestamps(2001, 2002), new int[] { 0, 1 });
        Assert.assertEquals(j.getOverlappingTimestamps(2003, 2005), new int[] { 2, 3 });
        Assert.assertEquals(j.getOverlappingTimestamps(2005, 2005), new int[] { 3 });
        Assert.assertEquals(j.getOverlappingTimestamps(2005, 2016), new int[] { 3 });
        Assert.assertEquals(j.getOverlappingTimestamps(2006, 2007), new int[] {});
    }

    @Test
    public void testIsSupported() {
        for (TimestampMap set : getAllInstances()) {
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
        for (TimestampMap set : getAllInstances()) {
            for (Estimator e : Estimator.values()) {
                if (set.isSupported(e)) {
                    Assert.assertNull(set.get(new Interval(1.0, 2.0), e));
                }
            }
        }
    }

    @Test
    public void testEstimatorNull() {
        for (TimestampMap set : getAllInstances()) {
            set.put(2.0, getDefaultValue(set));

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
        TimestampBooleanMap set = new TimestampBooleanMap();
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };

        set.put(indices[0], Boolean.TRUE);
        set.put(indices[1], Boolean.FALSE);
        set.put(indices[2], Boolean.FALSE);
        set.put(indices[3], Boolean.TRUE);

        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
        Assert.assertEquals(first, Boolean.TRUE);

        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
        Assert.assertEquals(last, Boolean.TRUE);

        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
        Assert.assertEquals(min, Boolean.FALSE);

        Object minTrue = set.get(new Interval(1.0, 1.0), Estimator.MIN);
        Assert.assertEquals(minTrue, Boolean.TRUE);

        Object minNoMatch = set.get(new Interval(0.0, 2.0), Estimator.MIN);
        Assert.assertEquals(minNoMatch, Boolean.FALSE);

        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
        Assert.assertEquals(max, Boolean.TRUE);

        Object maxFalse = set.get(new Interval(2.0, 6.0), Estimator.MAX);
        Assert.assertEquals(maxFalse, Boolean.FALSE);

        Object maxNoMatch = set.get(new Interval(5.0, 7.0), Estimator.MAX);
        Assert.assertEquals(maxNoMatch, Boolean.TRUE);
    }

    @Test
    public void testByteEstimators() {
        TimestampByteMap set = new TimestampByteMap();
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        byte[] values = new byte[] { 12, 45, -31, 64 };

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        double expected = ((12 + 45) / 2.0 + 2.0 * (-31 + 45) + (-31 + 64) / 2.0) / 6.0;
        Assert.assertEquals(avg, expected);
    }

    @Test
    public void testCharEstimators() {
        TimestampCharMap set = new TimestampCharMap();
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        char[] values = new char[] { 'a', 'z', 'e', 'c' };

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
        Assert.assertEquals(last, values[3]);
    }

    @Test
    public void testDoubleEstimators() {
        TimestampDoubleMap set = new TimestampDoubleMap();
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        double[] values = new double[] { 12.0, 45.3, -31.3, 64.4 };

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        double expected = ((12 + 45.3) / 2.0 + 2.0 * (-31.3 + 45.3) + (-31.3 + 64.4) / 2.0) / 6.0;
        Assert.assertTrue(Math.abs((Double) avg - expected) < 0.0000001);
    }

    @Test
    public void testFloatEstimators() {
        TimestampFloatMap set = new TimestampFloatMap();
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        float[] values = new float[] { 12f, 45.3f, -31.3f, 64.4f };

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Float);
        float expected = (float) (((12 + 45.3) / 2.0 + 2.0 * (-31.3 + 45.3) + (-31.3 + 64.4) / 2.0) / 6.0);
        Assert.assertEquals(expected, avg);
    }

    @Test
    public void testIntegerEstimators() {
        TimestampIntegerMap set = new TimestampIntegerMap();
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        int[] values = new int[] { 120, 450, -3100, 6400 };

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        double expected = (((120 + 450) / 2.0 + 2.0 * (-3100 + 450) + (-3100 + 6400) / 2.0) / 6.0);
        Assert.assertTrue(Math.abs((Double) avg - expected) < 0.00001);
    }

    @Test
    public void testLongEstimators() {
        TimestampLongMap set = new TimestampLongMap();
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        long[] values = new long[] { 120l, 450000l, -31000002343l, 640000000001232l };

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        double expected = (((120l + 450000l) / 2.0 + 2.0 * (-31000002343l + 450000l) + (-31000002343l + 640000000001232l) / 2.0) / 6.0);
        Assert.assertTrue(Math.abs((Double) avg - expected) < 0.00001);
    }

    @Test
    public void testShortEstimators() {
        TimestampShortMap set = new TimestampShortMap();
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        short[] values = new short[] { 12, 45, -31, 64 };

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Assert.assertNull(set.get(new Interval(0.1, 0.2), Estimator.MIN));
        Assert.assertNull(set.get(new Interval(0.1, 0.2), Estimator.MAX));

        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object minNoMatch = set.get(new Interval(0.0, 5.0), Estimator.MIN);
        Assert.assertEquals(minNoMatch, values[0]);

        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object maxNoMatch = set.get(new Interval(0.0, 5.0), Estimator.MAX);
        Assert.assertEquals(maxNoMatch, values[1]);

        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        double expected = ((((short) 12 + (short) 45) / 2.0 + 2.0 * ((short) -31 + (short) 45) + ((short) -31 + (short) 64) / 2.0) / 6.0);
        Assert.assertTrue(Math.abs((Double) avg - expected) < 0.00001);
    }

    @Test
    public void testStringEstimators() {
        TimestampStringMap set = new TimestampStringMap();
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        String[] values = new String[] { "a", "z", "e", "ch" };

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
        Assert.assertEquals(last, values[3]);
    }

    @Test
    public void testEquals() {
        double[] indices = new double[] { 1.0, 2.0, 6.0, 7.0 };
        String[] values = new String[] { "a", "z", "e" };
        TimestampStringMap set1 = new TimestampStringMap();
        TimestampStringMap set2 = new TimestampStringMap();
        TimestampStringMap set3 = new TimestampStringMap();
        TimestampStringMap set4 = new TimestampStringMap();
        TimestampStringMap set5 = new TimestampStringMap();

        set1.put(indices[0], values[0]);
        set1.put(indices[1], values[1]);
        set1.put(indices[2], values[2]);

        set2.put(indices[2], values[2]);
        set2.put(indices[1], values[1]);
        set2.put(indices[0], values[0]);

        set3.put(indices[0], "f");
        set3.put(indices[1], "o");
        set3.put(indices[2], "o");

        set4.put(7.0, values[0]);
        set4.put(8.0, values[1]);
        set4.put(9.0, values[2]);

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
        TimestampStringMap set1 = new TimestampStringMap();
        set1.put(1.0, "foo");
        set1.put(2.0, "bar");

        TimestampStringMap set2 = new TimestampStringMap(set1.getTimestamps(), set1.toValuesArray());
        Assert.assertTrue(set1.equals(set2));
        set1.clear();
        Assert.assertEquals(set2.size(), 2);
    }

    @Test
    public void testToStringDouble() {
        TimestampStringMap map1 = new TimestampStringMap();
        Assert.assertEquals(map1.toString(), "<empty>");

        map1.put(1.0, "foo");
        Assert.assertEquals(map1.toString(), "<[1.0, foo]>");

        map1.put(5.5, "bar");
        Assert.assertEquals(map1.toString(), "<[1.0, foo]; [5.5, bar]>");

        map1.put(6.0, " 'test' ");
        map1.put(9.0, " 'test' ");
        Assert.assertEquals(map1
                .toString(TimeFormat.DOUBLE), "<[1.0, foo]; [5.5, bar]; [6.0, \" 'test' \"]; [9.0, \" 'test' \"]>");
    }

    @Test
    public void testToStringDate() {
        TimestampStringMap map1 = new TimestampStringMap();
        Assert.assertEquals(map1.toString(TimeFormat.DATE), "<empty>");

        map1.put(AttributeUtils.parseDateTime("2012-02-29"), "foo");
        Assert.assertEquals(map1.toString(TimeFormat.DATE), "<[2012-02-29, foo]>");

        map1.put(AttributeUtils.parseDateTime("2012-02-29T00:02:21"), "bar");
        Assert.assertEquals(map1.toString(TimeFormat.DATE), "<[2012-02-29, foo]; [2012-02-29, bar]>");
        Assert.assertEquals(map1.toString(TimeFormat.DOUBLE), "<[1330473600000.0, foo]; [1330473741000.0, bar]>");

        // Test with time zone printing:
        Assert.assertEquals(map1.toString(TimeFormat.DATE, DateTimeZone.UTC), "<[2012-02-29, foo]; [2012-02-29, bar]>");
        Assert.assertEquals(map1
                .toString(TimeFormat.DATE, DateTimeZone.forID("+03:00")), "<[2012-02-29, foo]; [2012-02-29, bar]>");
        Assert.assertEquals(map1
                .toString(TimeFormat.DATE, DateTimeZone.forID("-03:00")), "<[2012-02-28, foo]; [2012-02-28, bar]>");

        // Test infinity:
        TimestampStringMap mapInf = new TimestampStringMap();
        mapInf.put(Double.NEGATIVE_INFINITY, "value");
        mapInf.put(Double.POSITIVE_INFINITY, "value");
        Assert.assertEquals(mapInf.toString(TimeFormat.DATE), "<[-Infinity, value]; [Infinity, value]>");
    }

    @Test
    public void testToStringDatetime() {
        TimestampStringMap map1 = new TimestampStringMap();
        Assert.assertEquals(map1.toString(TimeFormat.DATETIME), "<empty>");

        // Test with default timezone UTC+0
        map1.put(AttributeUtils.parseDateTime("2012-02-29"), "foo");
        Assert.assertEquals(map1.toString(TimeFormat.DATETIME), "<[2012-02-29T00:00:00.000Z, foo]>");

        map1.put(AttributeUtils.parseDateTime("2012-02-29T01:10:44"), "bar");
        Assert.assertEquals(map1
                .toString(TimeFormat.DATETIME), "<[2012-02-29T00:00:00.000Z, foo]; [2012-02-29T01:10:44.000Z, bar]>");
        Assert.assertEquals(map1.toString(TimeFormat.DOUBLE), "<[1330473600000.0, foo]; [1330477844000.0, bar]>");

        // Test with time zone printing:
        Assert.assertEquals(map1
                .toString(TimeFormat.DATETIME, DateTimeZone.UTC), "<[2012-02-29T00:00:00.000Z, foo]; [2012-02-29T01:10:44.000Z, bar]>");
        Assert.assertEquals(map1.toString(TimeFormat.DATETIME, DateTimeZone
                .forID("-01:30")), "<[2012-02-28T22:30:00.000-01:30, foo]; [2012-02-28T23:40:44.000-01:30, bar]>");

        // Test with timezone parsing and UTC printing:
        TimestampStringMap map2 = new TimestampStringMap();
        map2.put(AttributeUtils.parseDateTime("2012-02-29T00:00:00+02:30"), "foo");
        Assert.assertEquals(map2.toString(TimeFormat.DATETIME), "<[2012-02-28T21:30:00.000Z, foo]>");

        map2.put(AttributeUtils.parseDateTime("2012-02-29T01:10:44-01:00"), "bar");
        Assert.assertEquals(map2
                .toString(TimeFormat.DATETIME), "<[2012-02-28T21:30:00.000Z, foo]; [2012-02-29T02:10:44.000Z, bar]>");
        Assert.assertEquals(map2.toString(TimeFormat.DOUBLE), "<[1330464600000.0, foo]; [1330481444000.0, bar]>");

        // Test infinity:
        TimestampStringMap mapInf = new TimestampStringMap();
        mapInf.put(Double.NEGATIVE_INFINITY, "value");
        mapInf.put(Double.POSITIVE_INFINITY, "value");
        Assert.assertEquals(mapInf.toString(TimeFormat.DATETIME), "<[-Infinity, value]; [Infinity, value]>");
    }

    // UTILITY
    private void testDoubleArrayEquals(double[] a, double[] b) {
        Assert.assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i], b[i]);
        }
    }

    private TimestampMap[] getAllInstances() {
        return new TimestampMap[] { new TimestampDoubleMap(), new TimestampByteMap(), new TimestampFloatMap(), new TimestampIntegerMap(), new TimestampLongMap(), new TimestampShortMap(), new TimestampStringMap(), new TimestampCharMap(), new TimestampBooleanMap() };
    }

    private TimestampMap[] getAllInstances(int capacity) {
        return new TimestampMap[] { new TimestampDoubleMap(capacity), new TimestampByteMap(
                capacity), new TimestampFloatMap(capacity), new TimestampIntegerMap(capacity), new TimestampLongMap(
                        capacity), new TimestampShortMap(capacity), new TimestampStringMap(
                                capacity), new TimestampCharMap(capacity), new TimestampBooleanMap(capacity) };
    }

    private Object[] getTestValues(TimestampMap set) {
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

    private Object getDefaultValue(TimestampMap set) {
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

    private void testValues(TimestampMap set, double[] expectedTimestamp, Object[] expectedValues) {
        Class typeClass = set.getTypeClass();

        Assert.assertEquals(expectedTimestamp.length, expectedValues.length);
        Assert.assertEquals(set.size(), expectedTimestamp.length);
        Double[] keysArray = set.toKeysArray();
        for (int i = 0; i < expectedTimestamp.length; i++) {
            Assert.assertEquals(set.get(expectedTimestamp[i], null), expectedValues[i]);
            Assert.assertEquals(set.get(999999.0, getDefaultValue(set)), getDefaultValue(set));
            Assert.assertTrue(set.contains(expectedTimestamp[i]));
            Assert.assertEquals(keysArray[i], expectedTimestamp[i]);

            if (typeClass != String.class) {
                try {
                    Method getMethod = set.getClass().getMethod("get" + typeClass.getSimpleName(), double.class);
                    Method getMethodWithDefault = set.getClass()
                            .getMethod("get" + typeClass.getSimpleName(), double.class, getMethod.getReturnType());

                    Assert.assertEquals(getMethod.invoke(set, expectedTimestamp[i]), expectedValues[i]);
                    Assert.assertEquals(getMethodWithDefault
                            .invoke(set, expectedTimestamp[i], getDefaultValue(set)), expectedValues[i]);
                    Assert.assertEquals(getMethodWithDefault
                            .invoke(set, 999999.0, getDefaultValue(set)), getDefaultValue(set));

                    boolean thrown = false;
                    try {
                        getMethod.invoke(set, 99.0);
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
    }
}
