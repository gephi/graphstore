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

import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import java.util.Random;
import org.gephi.graph.impl.NumberGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimestampSetTest {

    @Test
    public void testEmpty() {
        TimestampSet set = new TimestampSet();

        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void testAddOne() {
        TimestampSet set = new TimestampSet();

        double t = 1.0;
        Assert.assertTrue(set.add(t));
        Assert.assertEquals(1, set.size());
        Assert.assertFalse(set.isEmpty());
        Assert.assertTrue(set.contains(t));
    }

    @Test
    public void testAddDuplicate() {
        TimestampSet set = new TimestampSet();

        double t = 1.0;
        Assert.assertTrue(set.add(t));
        Assert.assertFalse(set.add(t));
        Assert.assertTrue(set.contains(t));
    }

    @Test
    public void testAddMultiple() {
        TimestampSet set = new TimestampSet();

        int count = 1000;
        double[] array = NumberGenerator.generateRandomDouble(count, false);
        for (double d : array) {
            set.add(d);
        }
        double[] tms = set.toPrimitiveArray();
        double[] sdr = NumberGenerator.sortAndRemoveDuplicates(array);

        Assert.assertEquals(tms.length, sdr.length);
        for (int i = 0; i < tms.length; i++) {
            Assert.assertEquals(tms[i], sdr[i]);
        }
    }

    @Test
    public void testAddMultiplesWithDuplicated() {
        TimestampSet set = new TimestampSet();

        int count = 1000;
        double[] array = NumberGenerator.generateRandomDouble(count, true);
        for (double d : array) {
            set.add(d);
        }

        testDoubleArrayEquals(set.toPrimitiveArray(), NumberGenerator.sortAndRemoveDuplicates(array));
    }

    @Test
    public void testCapacity() {
        TimestampSet set = new TimestampSet(2);

        Assert.assertTrue(set.isEmpty());
        Assert.assertEquals(0, set.size());

        Assert.assertTrue(set.add(1.0));
        Assert.assertEquals(1, set.size());

        Assert.assertTrue(set.add(6.0));

        Assert.assertTrue(set.add(4.0));
        Assert.assertTrue(set.add(2.0));

        Assert.assertEquals(4, set.size());
    }

    @Test
    public void testRemove() {
        TimestampSet set = new TimestampSet();

        double t = 1.0;
        set.add(t);

        Assert.assertTrue(set.remove(t));
        Assert.assertTrue(set.isEmpty());
        Assert.assertFalse(set.contains(t));
    }

    @Test
    public void testRemoveAdd() {
        TimestampSet set = new TimestampSet();

        set.add(1.0);
        set.add(2.0);

        Assert.assertTrue(set.remove(1.0));
        Assert.assertTrue(set.add(1.0));

        Assert.assertEquals(2, set.size());
        Assert.assertEquals(1.0, set.toPrimitiveArray()[0]);
        Assert.assertEquals(2.0, set.toPrimitiveArray()[1]);
    }

    @Test
    public void testRemoveAddLoop() {
        TimestampSet set = new TimestampSet();
        DoubleSet doubleSet = new DoubleOpenHashSet();

        int count = 1000;
        double[] array = NumberGenerator.generateRandomDouble(count, true);
        for (double d : array) {
            set.add(d);
            doubleSet.add(d);
        }
        Random r = new Random(129);
        for (int i = 0; i < count / 2; i++) {
            int pos = r.nextInt(count);
            double number = array[pos];
            if (number > 0) {
                set.remove(number);
                doubleSet.remove(number);
                array[pos] = -1;
            } else {
                i--;
            }
        }

        testDoubleArrayEquals(set.toPrimitiveArray(), NumberGenerator.sortAndRemoveDuplicates(doubleSet.toDoubleArray()));

        double[] newArray = NumberGenerator.generateRandomDouble(count / 2, true);
        for (int i = 0; i < count / 2; i++) {
            double number = newArray[i];
            set.add(number);
            doubleSet.add(number);
        }

        testDoubleArrayEquals(set.toPrimitiveArray(), NumberGenerator.sortAndRemoveDuplicates(doubleSet.toDoubleArray()));
    }

    @Test
    public void testClear() {
        TimestampSet set = new TimestampSet();

        set.add(1.0);

        set.clear();

        Assert.assertTrue(set.isEmpty());
        Assert.assertFalse(set.contains(1.0));
    }

    @Test
    public void testEquals() {
        TimestampSet set1 = new TimestampSet();
        set1.add(6.0);
        set1.add(1.0);

        TimestampSet set2 = new TimestampSet();
        set2.add(6.0);
        set2.add(1.0);

        TimestampSet set3 = new TimestampSet();
        set3.add(6.0);
        set3.add(2.0);

        Assert.assertTrue(set1.equals(set2));
        Assert.assertTrue(set2.equals(set1));
        Assert.assertFalse(set1.equals(set3));
        Assert.assertFalse(set3.equals(set1));

        Assert.assertTrue(set1.hashCode() == set2.hashCode());
        Assert.assertFalse(set1.hashCode() == set3.hashCode());
    }

    @Test
    public void testEqualsWithCapacity() {
        TimestampSet set1 = new TimestampSet(10);
        set1.add(6.0);
        set1.add(1.0);

        TimestampSet set2 = new TimestampSet();
        set2.add(6.0);
        set2.add(1.0);

        Assert.assertTrue(set1.equals(set2));
        Assert.assertTrue(set2.equals(set1));

        Assert.assertTrue(set1.hashCode() == set2.hashCode());
    }

    @Test
    public void testEqualsDifferentSize() {
        TimestampSet set1 = new TimestampSet();
        set1.add(6.0);
        set1.add(1.0);

        TimestampSet set2 = new TimestampSet();
        set2.add(6.0);

        Assert.assertFalse(set1.equals(set2));
        Assert.assertFalse(set2.equals(set1));
    }

    @Test
    public void testGetTimestamps() {
        TimestampSet set = new TimestampSet();
        set.add(0.0);
        set.add(1.0);
        set.remove(0.0);

        Assert.assertEquals(set.toPrimitiveArray(), new double[]{1.0});
    }

    @Test
    public void testToArray() {
        TimestampSet set = new TimestampSet();
        set.add(0.0);
        set.add(1.0);
        set.remove(0.0);

        Assert.assertEquals(set.toArray(), new Double[]{1.0});
    }

    @Test
    public void testCopyConstructor() {
        TimestampSet set1 = new TimestampSet();
        set1.add(1.0);
        set1.add(4.0);

        TimestampSet set2 = new TimestampSet(set1.toPrimitiveArray());
        Assert.assertTrue(set1.equals(set2));
        set1.clear();
        Assert.assertEquals(set2.size(), 2);
    }

    //UTILITY
    private void testDoubleArrayEquals(double[] a, double[] b) {
        Assert.assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i], b[i]);
        }
    }
}
