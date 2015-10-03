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
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Interval;
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
            testValues(set, new Interval[]{new Interval(1.0, 2.0)}, new Object[]{defaultValues[0]});
        }
    }
    
    @Test
    public void testPutTwice() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);
            
            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            Assert.assertFalse(set.put(new Interval(1.0, 2.0), defaultValues[1]));
            testValues(set, new Interval[]{new Interval(1.0, 2.0)}, new Object[]{defaultValues[1]});
        }
    }
    
    @Test
    public void testMultiplePut() {
        for (IntervalMap set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);
            
            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            Assert.assertTrue(set.put(new Interval(6.0, 8.0), defaultValues[1]));
            testValues(set, new Interval[]{new Interval(1.0, 2.0), new Interval(6.0, 8.0)}, defaultValues);
        }
    }
    
    @Test
    public void testMultiplePutWithCapacity() {
        for (IntervalMap set : getAllInstances(10)) {
            Object[] defaultValues = getTestValues(set);
            
            Assert.assertTrue(set.put(new Interval(1.0, 2.0), defaultValues[0]));
            Assert.assertTrue(set.put(new Interval(6.0, 8.0), defaultValues[1]));
            testValues(set, new Interval[]{new Interval(1.0, 2.0), new Interval(6.0, 8.0)}, defaultValues);
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
            defaultValues = new Object[]{defaultValues[0], defaultValues[0], defaultValues[1], defaultValues[1]};
            testValues(set, new Interval[]{new Interval(1.0, 2.0), new Interval(2.0, 2.0), new Interval(2.0, 3.0), new Interval(3.0, 4.0)}, defaultValues);
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
            testValues(set, new Interval[]{new Interval(1.0, 2.0), new Interval(3.0, 4.0)}, defaultValues);
            Assert.assertTrue(set.remove(new Interval(3.0, 4.0)));
            Assert.assertTrue(set.put(new Interval(3.0, 4.0), defaultValues[1]));
            testValues(set, new Interval[]{new Interval(1.0, 2.0), new Interval(3.0, 4.0)}, defaultValues);
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
            testValues(set, new Interval[]{new Interval(1.0, 2.0), new Interval(3.0, 4.0)}, defaultValues);
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
            
            testValues(set, new Interval[]{new Interval(1.0, 2.0), new Interval(3.0, 4.0)}, defaultValues);
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
    public void testGetTimestamps() {
//        TimestampDoubleMap set = new TimestampDoubleMap();
//
//        set.put(1.0, 1.0);
//        set.put(2.0, 2.0);
//
//        testDoubleArrayEquals(new double[]{1.0, 2.0}, set.getTimestamps());
    }
    
    @Test
    public void testGetTimestampsTrim() {
//        TimestampDoubleMap set = new TimestampDoubleMap();
//
//        set.put(1.0, 1.0);
//        set.put(2.0, 2.0);
//        set.remove(2.0);
//
//        testDoubleArrayEquals(new double[]{1.0}, set.getTimestamps());
    }
    
    @Test
    public void testIsSupported() {
        for (IntervalMap set : getAllInstances()) {
            Assert.assertTrue(set.isSupported(Estimator.FIRST));
            Assert.assertTrue(set.isSupported(Estimator.LAST));
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

//    @Test
//    public void testBooleanEstimators() {
//        TimestampBooleanMap set = new TimestampBooleanMap();
//        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
//
//        set.put(indices[0], Boolean.TRUE);
//        set.put(indices[1], Boolean.FALSE);
//        set.put(indices[2], Boolean.FALSE);
//        set.put(indices[3], Boolean.TRUE);
//
//        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
//        Assert.assertEquals(first, Boolean.TRUE);
//
//        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
//        Assert.assertEquals(last, Boolean.TRUE);
//
//        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
//        Assert.assertEquals(min, Boolean.FALSE);
//
//        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
//        Assert.assertEquals(max, Boolean.TRUE);
//    }
//
//    @Test
//    public void testByteEstimators() {
//        TimestampByteMap set = new TimestampByteMap();
//        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
//        byte[] values = new byte[]{12, 45, -31, 64};
//
//        set.put(indices[0], values[0]);
//        set.put(indices[1], values[1]);
//        set.put(indices[2], values[2]);
//        set.put(indices[3], values[3]);
//
//        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
//        Assert.assertEquals(first, values[0]);
//
//        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
//        Assert.assertEquals(last, values[3]);
//
//        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
//        Assert.assertEquals(min, values[2]);
//
//        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
//        Assert.assertEquals(max, values[3]);
//
//        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
//        Assert.assertTrue(avg instanceof Double);
//        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));
//
//        Object sum = set.get(new Interval(1.0, 7.0), Estimator.SUM);
//        Assert.assertTrue(sum instanceof Integer);
//        Assert.assertEquals(sum, (values[0] + values[1] + values[2] + values[3]));
//    }
//
//    @Test
//    public void testCharEstimators() {
//        TimestampCharMap set = new TimestampCharMap();
//        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
//        char[] values = new char[]{'a', 'z', 'e', 'c'};
//
//        set.put(indices[0], values[0]);
//        set.put(indices[1], values[1]);
//        set.put(indices[2], values[2]);
//        set.put(indices[3], values[3]);
//
//        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
//        Assert.assertEquals(first, values[0]);
//
//        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
//        Assert.assertEquals(last, values[3]);
//
//        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
//        Assert.assertEquals(min, values[0]);
//
//        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
//        Assert.assertEquals(max, values[1]);
//    }
//
//    @Test
//    public void testDoubleEstimators() {
//        TimestampDoubleMap set = new TimestampDoubleMap();
//        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
//        double[] values = new double[]{12.0, 45.3, -31.3, 64.4};
//
//        set.put(indices[0], values[0]);
//        set.put(indices[1], values[1]);
//        set.put(indices[2], values[2]);
//        set.put(indices[3], values[3]);
//
//        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
//        Assert.assertEquals(first, values[0]);
//
//        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
//        Assert.assertEquals(last, values[3]);
//
//        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
//        Assert.assertEquals(min, values[2]);
//
//        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
//        Assert.assertEquals(max, values[3]);
//
//        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
//        Assert.assertTrue(avg instanceof Double);
//        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));
//
//        Object sum = set.get(new Interval(1.0, 7.0), Estimator.SUM);
//        Assert.assertTrue(sum instanceof Double);
//        Assert.assertEquals(sum, (values[0] + values[1] + values[2] + values[3]));
//    }
//
//    @Test
//    public void testFloatEstimators() {
//        TimestampFloatMap set = new TimestampFloatMap();
//        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
//        float[] values = new float[]{12f, 45.3f, -31.3f, 64.4f};
//
//        set.put(indices[0], values[0]);
//        set.put(indices[1], values[1]);
//        set.put(indices[2], values[2]);
//        set.put(indices[3], values[3]);
//
//        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
//        Assert.assertEquals(first, values[0]);
//
//        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
//        Assert.assertEquals(last, values[3]);
//
//        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
//        Assert.assertEquals(min, values[2]);
//
//        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
//        Assert.assertEquals(max, values[3]);
//
//        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
//        Assert.assertTrue(avg instanceof Float);
//        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (float) values.length));
//
//        Object sum = set.get(new Interval(1.0, 7.0), Estimator.SUM);
//        Assert.assertTrue(sum instanceof Float);
//        Assert.assertEquals(sum, (values[0] + values[1] + values[2] + values[3]));
//    }
//
//    @Test
//    public void testIntegerEstimators() {
//        TimestampIntegerMap set = new TimestampIntegerMap();
//        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
//        int[] values = new int[]{120, 450, -3100, 6400};
//
//        set.put(indices[0], values[0]);
//        set.put(indices[1], values[1]);
//        set.put(indices[2], values[2]);
//        set.put(indices[3], values[3]);
//
//        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
//        Assert.assertEquals(first, values[0]);
//
//        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
//        Assert.assertEquals(last, values[3]);
//
//        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
//        Assert.assertEquals(min, values[2]);
//
//        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
//        Assert.assertEquals(max, values[3]);
//
//        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
//        Assert.assertTrue(avg instanceof Double);
//        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));
//
//        Object sum = set.get(new Interval(1.0, 7.0), Estimator.SUM);
//        Assert.assertTrue(sum instanceof Long);
//        Assert.assertEquals(sum, (long) (values[0] + values[1] + values[2] + values[3]));
//    }
//
//    @Test
//    public void testLongEstimators() {
//        TimestampLongMap set = new TimestampLongMap();
//        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
//        long[] values = new long[]{120l, 450000l, -31000002343l, 640000000001232l};
//
//        set.put(indices[0], values[0]);
//        set.put(indices[1], values[1]);
//        set.put(indices[2], values[2]);
//        set.put(indices[3], values[3]);
//
//        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
//        Assert.assertEquals(first, values[0]);
//
//        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
//        Assert.assertEquals(last, values[3]);
//
//        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
//        Assert.assertEquals(min, values[2]);
//
//        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
//        Assert.assertEquals(max, values[3]);
//
//        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
//        Assert.assertTrue(avg instanceof Double);
//        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));
//
//        Object sum = set.get(new Interval(1.0, 7.0), Estimator.SUM);
//        Assert.assertTrue(sum instanceof Long);
//        Assert.assertEquals(sum, (long) (values[0] + values[1] + values[2] + values[3]));
//    }
//
//    @Test
//    public void testShortEstimators() {
//        TimestampShortMap set = new TimestampShortMap();
//        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
//        short[] values = new short[]{12, 45, -31, 64};
//
//        set.put(indices[0], values[0]);
//        set.put(indices[1], values[1]);
//        set.put(indices[2], values[2]);
//        set.put(indices[3], values[3]);
//
//        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
//        Assert.assertEquals(first, values[0]);
//
//        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
//        Assert.assertEquals(last, values[3]);
//
//        Object min = set.get(new Interval(1.0, 7.0), Estimator.MIN);
//        Assert.assertEquals(min, values[2]);
//
//        Object max = set.get(new Interval(1.0, 7.0), Estimator.MAX);
//        Assert.assertEquals(max, values[3]);
//
//        Object avg = set.get(new Interval(1.0, 7.0), Estimator.AVERAGE);
//        Assert.assertTrue(avg instanceof Double);
//        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));
//
//        Object sum = set.get(new Interval(1.0, 7.0), Estimator.SUM);
//        Assert.assertTrue(sum instanceof Integer);
//        Assert.assertEquals(sum, (values[0] + values[1] + values[2] + values[3]));
//    }
//
//    @Test
//    public void testStringEstimators() {
//        TimestampStringMap set = new TimestampStringMap();
//        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
//        String[] values = new String[]{"a", "z", "e", "ch"};
//
//        set.put(indices[0], values[0]);
//        set.put(indices[1], values[1]);
//        set.put(indices[2], values[2]);
//        set.put(indices[3], values[3]);
//
//        Object first = set.get(new Interval(1.0, 7.0), Estimator.FIRST);
//        Assert.assertEquals(first, values[0]);
//
//        Object last = set.get(new Interval(1.0, 7.0), Estimator.LAST);
//        Assert.assertEquals(last, values[3]);
//    }
    @Test
    public void testEquals() {
        Interval[] indices = new Interval[]{new Interval(1.0, 2.0), new Interval(3.0, 4.0), new Interval(2.0, 2.0), new Interval(2.0, 3.0)};
        String[] values = new String[]{"a", "z", "e"};
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
        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
        String[] values = new String[]{"a", "z", "e"};
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
        double[] indices = new double[]{1.0, 2.0, 6.0, 7.0};
        String[] values = new String[]{"a", "z", "e"};
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

    //UTILITY
    private void testDoubleArrayEquals(double[] a, double[] b) {
        Assert.assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i], b[i]);
        }
    }
    
    private IntervalMap[] getAllInstances() {
        return new IntervalMap[]{
            new IntervalStringMap(),
            new IntervalBooleanMap(),
            new IntervalFloatMap(),
            new IntervalDoubleMap(),
            new IntervalIntegerMap(),
            new IntervalShortMap(),
            new IntervalLongMap(),
            new IntervalByteMap(),
            new IntervalCharMap()
        };
    }
    
    private IntervalMap[] getAllInstances(int capacity) {
        return new IntervalMap[]{
            new IntervalStringMap(capacity),
            new IntervalBooleanMap(capacity),
            new IntervalFloatMap(capacity),
            new IntervalDoubleMap(capacity),
            new IntervalIntegerMap(capacity),
            new IntervalShortMap(capacity),
            new IntervalLongMap(capacity),
            new IntervalByteMap(capacity),
            new IntervalCharMap(capacity)
        };
    }
    
    private Object[] getTestValues(IntervalMap set) {
        if (set.getTypeClass().equals(String.class)) {
            return new String[]{"foo", "bar"};
        } else if (set.getTypeClass().equals(Boolean.class)) {
            return new Boolean[]{Boolean.TRUE, Boolean.FALSE};
        } else if (set.getTypeClass().equals(Float.class)) {
            return new Float[]{1f, 2f};
        } else if (set.getTypeClass().equals(Double.class)) {
            return new Double[]{1.0, 2.0};
        } else if (set.getTypeClass().equals(Integer.class)) {
            return new Integer[]{1, 2};
        } else if (set.getTypeClass().equals(Short.class)) {
            return new Short[]{1, 2};
        } else if (set.getTypeClass().equals(Long.class)) {
            return new Long[]{1l, 2l};
        } else if (set.getTypeClass().equals(Byte.class)) {
            return new Byte[]{1, 2};
        } else if (set.getTypeClass().equals(Character.class)) {
            return new Character[]{'f', 'o'};
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
                    Method getMethodWithDefault = set.getClass().getMethod("get" + typeClass.getSimpleName(), Interval.class, getMethod.getReturnType());
                    
                    Assert.assertEquals(getMethod.invoke(set, expectedIntervals[i]), expectedValues[i]);
                    Assert.assertEquals(getMethodWithDefault.invoke(set, expectedIntervals[i], getDefaultValue(set)), expectedValues[i]);
                    Assert.assertEquals(getMethodWithDefault.invoke(set, new Interval(99999.0, 999999.0), getDefaultValue(set)), getDefaultValue(set));
                    
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
