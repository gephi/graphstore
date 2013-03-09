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

import org.gephi.attribute.time.TimestampDoubleSet;
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
        Assert.assertEquals(set.get(1), 1.0);
        Assert.assertEquals(set.getDouble(1), 1.0);
    }

    @Test
    public void testPutTwice() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        int t = 1;
        set.put(t, 1.0);
        set.put(t, 2.0);
        Assert.assertTrue(set.contains(t));
        Assert.assertEquals(set.get(1), 2.0);
        Assert.assertEquals(set.getDouble(1), 2.0);
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

        Assert.assertEquals(set.get(1), 1.0);
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

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetUnknown() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.get(1);
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
