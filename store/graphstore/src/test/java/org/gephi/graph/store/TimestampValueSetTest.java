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

import org.gephi.attribute.time.Estimator;
import org.gephi.attribute.time.TimestampBooleanSet;
import org.gephi.attribute.time.TimestampByteSet;
import org.gephi.attribute.time.TimestampCharSet;
import org.gephi.attribute.time.TimestampDoubleSet;
import org.gephi.attribute.time.TimestampFloatSet;
import org.gephi.attribute.time.TimestampIntegerSet;
import org.gephi.attribute.time.TimestampLongSet;
import org.gephi.attribute.time.TimestampShortSet;
import org.gephi.attribute.time.TimestampStringSet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampValueSetTest {

    @Test
    public void testEmpty() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        Assert.assertTrue(set.isEmpty());
        Assert.assertEquals(set.size(), 0);
    }

    @Test
    public void testPutOne() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);
        Assert.assertEquals(set.size(), 1);
        Assert.assertFalse(set.isEmpty());
        Assert.assertTrue(set.contains(1));
        Assert.assertEquals(set.get(1, null), 1.0);
        Assert.assertEquals(set.getDouble(1), 1.0);
    }

    @Test
    public void testPutTwice() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        int t = 1;
        set.put(t, 1.0);
        set.put(t, 2.0);
        Assert.assertTrue(set.contains(t));
        Assert.assertEquals(set.get(1, null), 2.0);
        Assert.assertEquals(set.getDouble(1), 2.0);
    }
    
    @Test
    public void testMultiplePut() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        int t1 = 1;
        int t2 = 6;
        set.put(t2, 1.0);
        set.put(t1, 2.0);
        Assert.assertTrue(set.contains(t1));
        Assert.assertTrue(set.contains(t2));
        Assert.assertEquals(set.getDouble(t2), 1.0);
        Assert.assertEquals(set.getDouble(t1), 2.0);
    }
    
    @Test
    public void testMultiplePutWithCapacity() {
        TimestampDoubleSet set = new TimestampDoubleSet(10);

        int t1 = 1;
        int t2 = 6;
        set.put(t2, 1.0);
        set.put(t1, 2.0);
        Assert.assertTrue(set.contains(t1));
        Assert.assertTrue(set.contains(t2));
        Assert.assertEquals(set.getDouble(t2), 1.0);
        Assert.assertEquals(set.getDouble(t1), 2.0);
    }

    @Test
    public void testRemove() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        int t = 1;
        set.put(t, 1.0);

        set.remove(t);
        Assert.assertTrue(set.isEmpty());
        Assert.assertFalse(set.contains(t));
    }

    @Test
    public void testRemoveAdd() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);
        set.put(2, 1.0);

        set.remove(1);
        set.put(1, 1.0);

        Assert.assertEquals(set.size(), 2);
        Assert.assertEquals(set.getTimestamps()[0], 1);
        Assert.assertEquals(set.getTimestamps()[1], 2);
    }

    @Test
    public void testClear() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);
        set.clear();

        Assert.assertEquals(set.size(), 0);
        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void testGet() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);

        Assert.assertEquals(set.get(1, null), 1.0);
    }

    @Test
    public void testGetDouble() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);

        Assert.assertEquals(set.getDouble(1), 1.0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPutNull() {
        TimestampDoubleSet set = new TimestampDoubleSet();
        set.put(1, null);
    }

    @Test
    public void testGetUnknown() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        Assert.assertNull(set.get(1, null));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetDoubleUnknown() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.getDouble(1);
    }

    @Test
    public void testToArrayEmpty() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        Assert.assertEquals(set.toArray().length, 0);
        Assert.assertEquals(set.toDoubleArray().length, 0);
    }

    @Test
    public void testToArray() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);
        set.put(2, 2.0);

        Double[] res = set.toArray();
        double[] primitive = new double[res.length];
        for (int i = 0; i < res.length; i++) {
            Double d = res[i];
            Assert.assertNotNull(d);
            primitive[i] = d;
        }

        testDoubleArrayEquals(new double[]{1.0, 2.0}, primitive);
    }

    @Test
    public void testToDoubleArray() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);
        set.put(2, 2.0);

        testDoubleArrayEquals(new double[]{1.0, 2.0}, set.toDoubleArray());
    }

    @Test
    public void testGetTimestamps() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);
        set.put(2, 2.0);

        testIntArrayEquals(new int[]{1, 2}, set.getTimestamps());
    }

    @Test
    public void testBooleanEstimators() {
        TimestampBooleanSet set = new TimestampBooleanSet();
        int[] indices = new int[]{1, 2, 6, 7};

        set.put(indices[0], Boolean.TRUE);
        set.put(indices[1], Boolean.FALSE);
        set.put(indices[2], Boolean.FALSE);
        set.put(indices[3], Boolean.TRUE);

        Object first = set.get(null, indices, Estimator.FIRST);
        Assert.assertEquals(first, Boolean.TRUE);

        Object last = set.get(null, indices, Estimator.LAST);
        Assert.assertEquals(last, Boolean.TRUE);

        Object min = set.get(null, indices, Estimator.MIN);
        Assert.assertEquals(min, Boolean.FALSE);

        Object max = set.get(null, indices, Estimator.MAX);
        Assert.assertEquals(max, Boolean.TRUE);
    }

    @Test
    public void testByteEstimators() {
        TimestampByteSet set = new TimestampByteSet();
        int[] indices = new int[]{1, 2, 6, 7};
        byte[] values = new byte[]{12, 45, -31, 64};

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(null, indices, Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(null, indices, Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(null, indices, Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(null, indices, Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(null, indices, Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));

        Object sum = set.get(null, indices, Estimator.SUM);
        Assert.assertTrue(sum instanceof Integer);
        Assert.assertEquals(sum, (values[0] + values[1] + values[2] + values[3]));
    }

    @Test
    public void testCharEstimators() {
        TimestampCharSet set = new TimestampCharSet();
        int[] indices = new int[]{1, 2, 6, 7};
        char[] values = new char[]{'a', 'z', 'e', 'c'};

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(null, indices, Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(null, indices, Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(null, indices, Estimator.MIN);
        Assert.assertEquals(min, values[0]);

        Object max = set.get(null, indices, Estimator.MAX);
        Assert.assertEquals(max, values[1]);
    }

    @Test
    public void testDoubleEstimators() {
        TimestampDoubleSet set = new TimestampDoubleSet();
        int[] indices = new int[]{1, 2, 6, 7};
        double[] values = new double[]{12.0, 45.3, -31.3, 64.4};

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(null, indices, Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(null, indices, Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(null, indices, Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(null, indices, Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(null, indices, Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));

        Object sum = set.get(null, indices, Estimator.SUM);
        Assert.assertTrue(sum instanceof Double);
        Assert.assertEquals(sum, (values[0] + values[1] + values[2] + values[3]));
    }

    @Test
    public void testFloatEstimators() {
        TimestampFloatSet set = new TimestampFloatSet();
        int[] indices = new int[]{1, 2, 6, 7};
        float[] values = new float[]{12f, 45.3f, -31.3f, 64.4f};

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(null, indices, Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(null, indices, Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(null, indices, Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(null, indices, Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(null, indices, Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Float);
        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (float) values.length));

        Object sum = set.get(null, indices, Estimator.SUM);
        Assert.assertTrue(sum instanceof Float);
        Assert.assertEquals(sum, (values[0] + values[1] + values[2] + values[3]));
    }

    @Test
    public void testIntegerEstimators() {
        TimestampIntegerSet set = new TimestampIntegerSet();
        int[] indices = new int[]{1, 2, 6, 7};
        int[] values = new int[]{120, 450, -3100, 6400};

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(null, indices, Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(null, indices, Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(null, indices, Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(null, indices, Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(null, indices, Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));

        Object sum = set.get(null, indices, Estimator.SUM);
        Assert.assertTrue(sum instanceof Long);
        Assert.assertEquals(sum, (long) (values[0] + values[1] + values[2] + values[3]));
    }

    @Test
    public void testLongEstimators() {
        TimestampLongSet set = new TimestampLongSet();
        int[] indices = new int[]{1, 2, 6, 7};
        long[] values = new long[]{120l, 450000l, -31000002343l, 640000000001232l};

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(null, indices, Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(null, indices, Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(null, indices, Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(null, indices, Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(null, indices, Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));

        Object sum = set.get(null, indices, Estimator.SUM);
        Assert.assertTrue(sum instanceof Long);
        Assert.assertEquals(sum, (long) (values[0] + values[1] + values[2] + values[3]));
    }

    @Test
    public void testShortEstimators() {
        TimestampShortSet set = new TimestampShortSet();
        int[] indices = new int[]{1, 2, 6, 7};
        short[] values = new short[]{12, 45, -31, 64};

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(null, indices, Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(null, indices, Estimator.LAST);
        Assert.assertEquals(last, values[3]);

        Object min = set.get(null, indices, Estimator.MIN);
        Assert.assertEquals(min, values[2]);

        Object max = set.get(null, indices, Estimator.MAX);
        Assert.assertEquals(max, values[3]);

        Object avg = set.get(null, indices, Estimator.AVERAGE);
        Assert.assertTrue(avg instanceof Double);
        Assert.assertEquals(avg, ((values[0] + values[1] + values[2] + values[3]) / (double) values.length));

        Object sum = set.get(null, indices, Estimator.SUM);
        Assert.assertTrue(sum instanceof Integer);
        Assert.assertEquals(sum, (values[0] + values[1] + values[2] + values[3]));
    }

    @Test
    public void testStringEstimators() {
        TimestampStringSet set = new TimestampStringSet();
        int[] indices = new int[]{1, 2, 6, 7};
        String[] values = new String[]{"a", "z", "e", "ch"};

        set.put(indices[0], values[0]);
        set.put(indices[1], values[1]);
        set.put(indices[2], values[2]);
        set.put(indices[3], values[3]);

        Object first = set.get(null, indices, Estimator.FIRST);
        Assert.assertEquals(first, values[0]);

        Object last = set.get(null, indices, Estimator.LAST);
        Assert.assertEquals(last, values[3]);
    }
    
    @Test
    public void testEquals() {
        int[] indices = new int[]{1, 2, 6};
        String[] values = new String[]{"a", "z", "e"};
        TimestampStringSet set1 = new TimestampStringSet();
        TimestampStringSet set2 = new TimestampStringSet();
        
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
    public void testEqualsWithCapacity() {
        int[] indices = new int[]{1, 2, 6};
        String[] values = new String[]{"a", "z", "e"};
        TimestampStringSet set1 = new TimestampStringSet(10);
        TimestampStringSet set2 = new TimestampStringSet();
        
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
        int[] indices = new int[]{1, 2, 6};
        String[] values = new String[]{"a", "z", "e"};
        TimestampStringSet set1 = new TimestampStringSet();
        TimestampStringSet set2 = new TimestampStringSet();
        
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

    //UTILITY
    private void testIntArrayEquals(int[] a, int[] b) {
        Assert.assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i], b[i]);
        }
    }

    private void testDoubleArrayEquals(double[] a, double[] b) {
        Assert.assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i], b[i]);
        }
    }
}
