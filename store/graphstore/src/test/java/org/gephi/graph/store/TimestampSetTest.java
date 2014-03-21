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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Random;
import org.gephi.attribute.time.TimestampSet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampSetTest {

    @Test
    public void testEmpty() {
        TimestampSet set = new TimestampSet();

        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void testAddOne() {
        TimestampSet set = new TimestampSet();

        int t = 1;
        Assert.assertTrue(set.add(t));
        Assert.assertEquals(1, set.size());
        Assert.assertFalse(set.isEmpty());
        Assert.assertTrue(set.contains(t));
    }

    @Test
    public void testAddDuplicate() {
        TimestampSet set = new TimestampSet();

        int t = 1;
        Assert.assertTrue(set.add(t));
        Assert.assertFalse(set.add(t));
        Assert.assertTrue(set.contains(t));
    }

    @Test
    public void testAddMultiple() {
        TimestampSet set = new TimestampSet();

        int count = 1000;
        int[] array = NumberGenerator.generateRandomInt(count, false);
        for (int d : array) {
            set.add(d);
        }
        int[] tms = set.getTimestamps();
        int[] sdr = NumberGenerator.sortAndRemoveDuplicates(array);

        Assert.assertEquals(tms.length, sdr.length);
        for (int i = 0; i < tms.length; i++) {
            Assert.assertEquals(tms[i], sdr[i]);
        }
    }

    @Test
    public void testAddMultiplesithDuplicated() {
        TimestampSet set = new TimestampSet();

        int count = 1000;
        int[] array = NumberGenerator.generateRandomInt(count, true);
        for (int d : array) {
            set.add(d);
        }

        testIntArrayEquals(set.getTimestamps(), NumberGenerator.sortAndRemoveDuplicates(array));
    }

    @Test
    public void testCapacity() {
        TimestampSet set = new TimestampSet(2);

        Assert.assertTrue(set.isEmpty());
        Assert.assertEquals(0, set.size());

        Assert.assertTrue(set.add(1));
        Assert.assertEquals(1, set.size());

        Assert.assertTrue(set.add(6));

        Assert.assertTrue(set.add(4));
        Assert.assertTrue(set.add(2));

        Assert.assertEquals(4, set.size());
    }

    @Test
    public void testRemove() {
        TimestampSet set = new TimestampSet();

        int t = 1;
        set.add(t);

        Assert.assertTrue(set.remove(t));
        Assert.assertTrue(set.isEmpty());
        Assert.assertFalse(set.contains(t));
    }

    @Test
    public void testRemoveAdd() {
        TimestampSet set = new TimestampSet();

        set.add(1);
        set.add(2);

        Assert.assertTrue(set.remove(1));
        Assert.assertTrue(set.add(1));

        Assert.assertEquals(2, set.size());
        Assert.assertEquals(1, set.getTimestamps()[0]);
        Assert.assertEquals(2, set.getTimestamps()[1]);
    }

    @Test
    public void testRemoveAddLoop() {
        TimestampSet set = new TimestampSet();
        IntSet intSet = new IntOpenHashSet();

        int count = 1000;
        int[] array = NumberGenerator.generateRandomInt(count, true);
        for (int d : array) {
            set.add(d);
            intSet.add(d);
        }
        Random r = new Random(129);
        for (int i = 0; i < count / 2; i++) {
            int pos = r.nextInt(count);
            int number = array[pos];
            if (number > 0) {
                set.remove(number);
                intSet.remove(number);
                array[pos] = -1;
            } else {
                i--;
            }
        }

        testIntArrayEquals(set.getTimestamps(), NumberGenerator.sortAndRemoveDuplicates(intSet.toIntArray()));

        int[] newArray = NumberGenerator.generateRandomInt(count / 2, true);
        for (int i = 0; i < count / 2; i++) {
            int number = newArray[i];
            set.add(number);
            intSet.add(number);
        }

        testIntArrayEquals(set.getTimestamps(), NumberGenerator.sortAndRemoveDuplicates(intSet.toIntArray()));
    }

    @Test
    public void testClear() {
        TimestampSet set = new TimestampSet();

        set.add(1);

        set.clear();

        Assert.assertTrue(set.isEmpty());
        Assert.assertFalse(set.contains(1));
    }

    @Test
    public void testEquals() {
        TimestampSet set1 = new TimestampSet();
        set1.add(6);
        set1.add(1);

        TimestampSet set2 = new TimestampSet();
        set2.add(6);
        set2.add(1);

        Assert.assertTrue(set1.equals(set2));
        Assert.assertTrue(set2.equals(set1));

        Assert.assertTrue(set1.hashCode() == set2.hashCode());
    }

    @Test
    public void testEqualsWithCapacity() {
        TimestampSet set1 = new TimestampSet(10);
        set1.add(6);
        set1.add(1);

        TimestampSet set2 = new TimestampSet();
        set2.add(6);
        set2.add(1);

        Assert.assertTrue(set1.equals(set2));
        Assert.assertTrue(set2.equals(set1));

        Assert.assertTrue(set1.hashCode() == set2.hashCode());
    }

    @Test
    public void tesGetTimestamps() {
        TimestampSet set = new TimestampSet();
        set.add(0);
        set.add(1);
        set.remove(0);

        Assert.assertEquals(set.getTimestamps(), new int[]{1});
    }

    //UTILITY
    private void testIntArrayEquals(int[] a, int[] b) {
        Assert.assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals(a[i], b[i]);
        }
    }
}
