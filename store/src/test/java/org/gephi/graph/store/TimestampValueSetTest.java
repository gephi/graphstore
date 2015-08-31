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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.gephi.attribute.time.TimestampValueSet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampValueSetTest {

    @Test
    public void testEmpty() {
        for (TimestampValueSet set : getAllInstances()) {
            Assert.assertTrue(set.isEmpty());
            Assert.assertEquals(set.size(), 0);
        }
    }

    @Test
    public void testPutOne() {
        for (TimestampValueSet set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(1, defaultValues[0]);
            testValues(set, new int[]{1}, new Object[]{defaultValues[0]});
        }
    }

    @Test
    public void testPutTwice() {
        for (TimestampValueSet set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(1, defaultValues[0]);
            set.put(1, defaultValues[1]);
            testValues(set, new int[]{1}, new Object[]{defaultValues[1]});
        }
    }

    @Test
    public void testMultiplePut() {
        for (TimestampValueSet set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(1, defaultValues[0]);
            set.put(6, defaultValues[1]);
            testValues(set, new int[]{1, 6}, defaultValues);
        }
    }

    @Test
    public void testMultiplePutWithCapacity() {
        for (TimestampValueSet set : getAllInstances(10)) {
            Object[] defaultValues = getTestValues(set);

            set.put(1, defaultValues[0]);
            set.put(6, defaultValues[1]);
            testValues(set, new int[]{1, 6}, defaultValues);
        }
    }

    @Test
    public void testRemove() {
        for (TimestampValueSet set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(1, defaultValues[0]);
            set.put(2, defaultValues[1]);
            set.remove(1);
            Assert.assertFalse(set.contains(1));
            Assert.assertEquals(set.get(2, null), defaultValues[1]);
            set.remove(2);
            Assert.assertTrue(set.isEmpty());
            Assert.assertFalse(set.contains(1));
        }
    }

    @Test
    public void testRemoveAdd() {
        for (TimestampValueSet set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(1, defaultValues[0]);
            set.put(2, defaultValues[1]);
            set.remove(1);
            set.put(1, defaultValues[0]);
            testValues(set, new int[]{1, 2}, defaultValues);
            set.remove(2);
            set.put(2, defaultValues[1]);
            testValues(set, new int[]{1, 2}, defaultValues);
        }
    }

    @Test
    public void testRemoveUnknown() {
        for (TimestampValueSet set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);

            set.put(1, defaultValues[0]);
            set.put(2, defaultValues[1]);
            set.remove(3);
            set.remove(0);
            testValues(set, new int[]{1, 2}, defaultValues);
        }
    }

    @Test
    public void testClear() {
        for (TimestampValueSet set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);
            set.put(1, defaultValues[0]);
            set.clear();

            Assert.assertEquals(set.size(), 0);
            Assert.assertTrue(set.isEmpty());
        }
    }

    @Test
    public void testClearAdd() {
        for (TimestampValueSet set : getAllInstances()) {
            Object[] defaultValues = getTestValues(set);
            set.put(1, defaultValues[0]);
            set.clear();
            set.put(1, defaultValues[0]);
            set.put(2, defaultValues[1]);

            testValues(set, new int[]{1, 2}, defaultValues);
        }
    }

    @Test
    public void testPutNull() {
        for (TimestampValueSet set : getAllInstances()) {
            boolean thrown = false;
            try {
                set.put(1, null);
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
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);
        set.put(2, 2.0);

        testIntArrayEquals(new int[]{1, 2}, set.getTimestamps());
    }

    @Test
    public void testGetTimestampsTrim() {
        TimestampDoubleSet set = new TimestampDoubleSet();

        set.put(1, 1.0);
        set.put(2, 2.0);
        set.remove(2);

        testIntArrayEquals(new int[]{1}, set.getTimestamps());
    }

    @Test
    public void testIsSupported() {
        for (TimestampValueSet set : getAllInstances()) {
            Assert.assertTrue(set.isSupported(Estimator.FIRST));
            Assert.assertTrue(set.isSupported(Estimator.LAST));
        }
    }

    @Test
    public void testEstimatorDefault() {
        for (TimestampValueSet set : getAllInstances()) {
            for (Estimator e : Estimator.values()) {
                if (set.isSupported(e)) {
                    Assert.assertNull(set.get(null, new int[]{99}, e));
                }
            }
        }
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
        TimestampStringSet set3 = new TimestampStringSet();
        TimestampStringSet set4 = new TimestampStringSet();
        TimestampStringSet set5 = new TimestampStringSet();

        set1.put(indices[0], values[0]);
        set1.put(indices[1], values[1]);
        set1.put(indices[2], values[2]);

        set2.put(indices[2], values[2]);
        set2.put(indices[1], values[1]);
        set2.put(indices[0], values[0]);

        set3.put(indices[0], "f");
        set3.put(indices[1], "o");
        set3.put(indices[2], "o");

        set4.put(7, values[0]);
        set4.put(8, values[1]);
        set4.put(9, values[2]);

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

    private TimestampValueSet[] getAllInstances() {
        return new TimestampValueSet[]{
            new TimestampDoubleSet(),
            new TimestampByteSet(),
            new TimestampFloatSet(),
            new TimestampIntegerSet(),
            new TimestampLongSet(),
            new TimestampShortSet(),
            new TimestampStringSet(),
            new TimestampCharSet(),
            new TimestampBooleanSet()
        };
    }

    private TimestampValueSet[] getAllInstances(int capacity) {
        return new TimestampValueSet[]{
            new TimestampDoubleSet(capacity),
            new TimestampByteSet(capacity),
            new TimestampFloatSet(capacity),
            new TimestampIntegerSet(capacity),
            new TimestampLongSet(capacity),
            new TimestampShortSet(capacity),
            new TimestampStringSet(capacity),
            new TimestampCharSet(capacity),
            new TimestampBooleanSet(capacity)
        };
    }

    private Object[] getTestValues(TimestampValueSet set) {
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

    private Object getDefaultValue(TimestampValueSet set) {
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

    private void testValues(TimestampValueSet set, int[] expectedTimestamp, Object[] expectedValues) {
        Class typeClass = set.getTypeClass();

        Assert.assertEquals(expectedTimestamp.length, expectedValues.length);
        Assert.assertEquals(set.size(), expectedTimestamp.length);
        for (int i = 0; i < expectedTimestamp.length; i++) {
            Assert.assertEquals(set.get(expectedTimestamp[i], null), expectedValues[i]);
            Assert.assertEquals(set.get(99, getDefaultValue(set)), getDefaultValue(set));
            Assert.assertTrue(set.contains(expectedTimestamp[i]));

            if (typeClass != String.class) {
                try {
                    Method getMethod = set.getClass().getMethod("get" + typeClass.getSimpleName(), int.class);
                    Method getMethodWithDefault = set.getClass().getMethod("get" + typeClass.getSimpleName(), int.class, getMethod.getReturnType());

                    Assert.assertEquals(getMethod.invoke(set, expectedTimestamp[i]), expectedValues[i]);
                    Assert.assertEquals(getMethodWithDefault.invoke(set, expectedTimestamp[i], getDefaultValue(set)), expectedValues[i]);
                    Assert.assertEquals(getMethodWithDefault.invoke(set, 99, getDefaultValue(set)), getDefaultValue(set));

                    boolean thrown = false;
                    try {
                        getMethod.invoke(set, 99);
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
        Assert.assertEquals(set.toArray(), expectedValues);
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
