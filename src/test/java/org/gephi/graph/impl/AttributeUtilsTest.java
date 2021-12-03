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
package org.gephi.graph.impl;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.types.TimestampBooleanMap;
import org.gephi.graph.api.types.TimestampByteMap;
import org.gephi.graph.api.types.TimestampCharMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampFloatMap;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.graph.api.types.TimestampLongMap;
import org.gephi.graph.api.types.TimestampShortMap;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalBooleanMap;
import org.gephi.graph.api.types.IntervalByteMap;
import org.gephi.graph.api.types.IntervalCharMap;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalFloatMap;
import org.gephi.graph.api.types.IntervalIntegerMap;
import org.gephi.graph.api.types.IntervalLongMap;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.IntervalShortMap;
import org.gephi.graph.api.types.IntervalStringMap;
import org.gephi.graph.api.types.TimestampSet;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AttributeUtilsTest {

    @Test
    public void testGetSupportedTypes() {
        Set<Class> types = AttributeUtils.getSupportedTypes();
        Assert.assertNotNull(types);
        Assert.assertFalse(types.isEmpty());
        Assert.assertTrue(Collections.unmodifiableSet(types).getClass().isInstance(types));
    }

    @Test
    public void testParseSimpleTypes() {
        Assert.assertEquals(AttributeUtils.parse("foo", String.class), "foo");
        Assert.assertEquals(AttributeUtils.parse("0", Integer.class), 0);
        Assert.assertEquals(AttributeUtils.parse("0", Float.class), 0f);
        Assert.assertEquals(AttributeUtils.parse("0", Double.class), 0.0);
        Assert.assertEquals(AttributeUtils.parse("0", Long.class), 0l);
        Assert.assertEquals(AttributeUtils.parse("0", Short.class), (short) 0);
        Assert.assertEquals(AttributeUtils.parse("0", Byte.class), (byte) 0);
        Assert.assertEquals(AttributeUtils.parse("0", Character.class), '0');
        Assert.assertEquals(AttributeUtils.parse("true", Boolean.class), true);
        Assert.assertEquals(AttributeUtils.parse("1", Boolean.class), true);
        Assert.assertEquals(AttributeUtils.parse("0", Boolean.class), false);
        Assert.assertEquals(AttributeUtils
                .parse("123456789123456789123456789123456789", BigInteger.class), new BigInteger(
                        "123456789123456789123456789123456789"));
        Assert.assertEquals(AttributeUtils
                .parse("123456789123456789123456789123456789.123456789123456789123456789123456789", BigDecimal.class), new BigDecimal(
                        "123456789123456789123456789123456789.123456789123456789123456789123456789"));
    }

    @Test
    public void testParsePrimitiveTypes() {
        Assert.assertEquals(AttributeUtils.parse("0", int.class), 0);
        Assert.assertEquals(AttributeUtils.parse("0", float.class), 0f);
        Assert.assertEquals(AttributeUtils.parse("0", double.class), 0.0);
        Assert.assertEquals(AttributeUtils.parse("0", long.class), 0l);
        Assert.assertEquals(AttributeUtils.parse("0", short.class), (short) 0);
        Assert.assertEquals(AttributeUtils.parse("0", byte.class), (byte) 0);
        Assert.assertEquals(AttributeUtils.parse("0", char.class), '0');
        Assert.assertEquals(AttributeUtils.parse("true", boolean.class), true);
        Assert.assertEquals(AttributeUtils.parse("1", boolean.class), true);
        Assert.assertEquals(AttributeUtils.parse("0", boolean.class), false);
    }

    @Test
    public void testParseArrayTypes() {
        Assert.assertEquals(AttributeUtils
                .parse("[true, false, 1, 0, null]", Boolean[].class), new Boolean[] { true, false, true, false, null });
        Assert.assertEquals(AttributeUtils
                .parse("[true, false, 1, 0]", boolean[].class), new boolean[] { true, false, true, false });

        Assert.assertEquals(AttributeUtils.parse("[-1, 3, null]", Integer[].class), new Integer[] { -1, 3, null });
        Assert.assertEquals(AttributeUtils.parse("[-1, 3, null]", Integer[].class).getClass(), Integer[].class);
        Assert.assertEquals(AttributeUtils.parse("[-1, 0, 2]", int[].class), new int[] { -1, 0, 2 });

        Assert.assertEquals(AttributeUtils.parse("[-1, 3, null]", Byte[].class), new Byte[] { -1, 3, null });
        Assert.assertEquals(AttributeUtils.parse("[-1, 0, 2]", byte[].class), new byte[] { -1, 0, 2 });

        Assert.assertEquals(AttributeUtils.parse("[-1, 3, null]", Short[].class), new Short[] { -1, 3, null });
        Assert.assertEquals(AttributeUtils.parse("[-1, 0, \"2\"]", short[].class), new short[] { -1, 0, 2 });

        Assert.assertEquals(AttributeUtils.parse("[-1, 3, null]", Long[].class), new Long[] { -1l, 3l, null });
        Assert.assertEquals(AttributeUtils.parse("[-1, 0, 2]", long[].class), new long[] { -1, 0, 2 });

        Assert.assertEquals(AttributeUtils
                .parse("[-1e6, 1, .001, 2000000., null]", Float[].class), new Float[] { -1e6f, 1.0f, .001f, 2e6f, null });
        Assert.assertEquals(AttributeUtils.parse("[1]", float[].class).getClass(), float[].class);
        Assert.assertEquals(AttributeUtils
                .parse("[-1e6, 1, .001, 2e6]", float[].class), new float[] { -1e6f, 1.0f, .001f, 2e6f });

        Assert.assertEquals(AttributeUtils
                .parse("[-1e6, 1, .001, 2000000., null]", Double[].class), new Double[] { -1e6, 1.0, .001, 2e6, null });
        Assert.assertEquals(AttributeUtils
                .parse("[-1e6, 1, .001, 2e6]", double[].class), new double[] { -1e6, 1.0, .001, 2e6 });
        Assert.assertEquals(AttributeUtils.parse("[-1e6, 1, .001, 2e6]", double[].class).getClass(), double[].class);

        Assert.assertEquals(AttributeUtils
                .parse("[' true ', 'null', null]", String[].class), new String[] { " true ", "null", null });
        Assert.assertEquals(AttributeUtils
                .parse("['123456789123456789123456789123456789']", BigInteger[].class), new BigInteger[] { new BigInteger(
                        "123456789123456789123456789123456789") });
        Assert.assertEquals(AttributeUtils
                .parse("['123456789123456789123456789123456789.123456789123456789123456789123456789']", BigDecimal[].class), new BigDecimal[] { new BigDecimal(
                        "123456789123456789123456789123456789.123456789123456789123456789123456789") });
    }

    @Test
    public void testParseDynamicIntervalTypes() {
        IntervalSet set = new IntervalSet();
        set.add(new Interval(1, 2));
        set.add(new Interval(21, 124));
        Assert.assertEquals(AttributeUtils.parse("<[1, 2]; [21.0, 124.0]>", IntervalSet.class), set);

        IntervalStringMap mString = new IntervalStringMap();
        mString.put(new Interval(1, 2), "value");
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, value]>", IntervalStringMap.class), mString);

        IntervalIntegerMap mInteger = new IntervalIntegerMap();
        mInteger.put(new Interval(1, 2), 25);
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, 25]>", IntervalIntegerMap.class), mInteger);

        IntervalFloatMap mFloat = new IntervalFloatMap();
        mFloat.put(new Interval(1, 2), 25f);
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, 25]>", IntervalFloatMap.class), mFloat);

        IntervalDoubleMap mDouble = new IntervalDoubleMap();
        mDouble.put(new Interval(1, 2), 25d);
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, 25]>", IntervalDoubleMap.class), mDouble);

        IntervalLongMap mLong = new IntervalLongMap();
        mLong.put(new Interval(1, 2), 25l);
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, 25]>", IntervalLongMap.class), mLong);

        IntervalShortMap mShort = new IntervalShortMap();
        mShort.put(new Interval(1, 2), (short) 25);
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, 25]>", IntervalShortMap.class), mShort);

        IntervalByteMap mByte = new IntervalByteMap();
        mByte.put(new Interval(1, 2), (byte) 6);
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, 6]>", IntervalByteMap.class), mByte);

        IntervalCharMap mChar = new IntervalCharMap();
        mChar.put(new Interval(1, 2), 'z');
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, z]>", IntervalCharMap.class), mChar);

        IntervalBooleanMap mBool = new IntervalBooleanMap();
        mBool.put(new Interval(1, 2), true);
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, true]>", IntervalBooleanMap.class), mBool);
    }

    @Test
    public void testParseDynamicTimestampTypes() {
        TimestampSet set = new TimestampSet();
        set.add(1.0);
        set.add(2.0);
        set.add(21.0);
        set.add(124.0);
        Assert.assertEquals(AttributeUtils.parse("<[1, 2, 21.0, 124.0]>", TimestampSet.class), set);

        TimestampStringMap mString = new TimestampStringMap();
        mString.put(1.0, "value");
        Assert.assertEquals(AttributeUtils.parse("<[1, value]>", TimestampStringMap.class), mString);

        TimestampIntegerMap mInteger = new TimestampIntegerMap();
        mInteger.put(1.0, 25);
        Assert.assertEquals(AttributeUtils.parse("<[1, 25]>", TimestampIntegerMap.class), mInteger);

        TimestampFloatMap mFloat = new TimestampFloatMap();
        mFloat.put(1.0, 25f);
        Assert.assertEquals(AttributeUtils.parse("<[1, 25]>", TimestampFloatMap.class), mFloat);

        TimestampDoubleMap mDouble = new TimestampDoubleMap();
        mDouble.put(1.0, 25d);
        Assert.assertEquals(AttributeUtils.parse("<[1, 25]>", TimestampDoubleMap.class), mDouble);

        TimestampLongMap mLong = new TimestampLongMap();
        mLong.put(1.0, 25l);
        Assert.assertEquals(AttributeUtils.parse("<[1, 25]>", TimestampLongMap.class), mLong);

        TimestampShortMap mShort = new TimestampShortMap();
        mShort.put(1.0, (short) 25);
        Assert.assertEquals(AttributeUtils.parse("<[1, 25]>", TimestampShortMap.class), mShort);

        TimestampByteMap mByte = new TimestampByteMap();
        mByte.put(1.0, (byte) 6);
        Assert.assertEquals(AttributeUtils.parse("<[1, 6]>", TimestampByteMap.class), mByte);

        TimestampCharMap mChar = new TimestampCharMap();
        mChar.put(1.0, 'z');
        Assert.assertEquals(AttributeUtils.parse("<[1, z]>", TimestampCharMap.class), mChar);

        TimestampBooleanMap mBool = new TimestampBooleanMap();
        mBool.put(1.0, true);
        Assert.assertEquals(AttributeUtils.parse("<[1, true]>", TimestampBooleanMap.class), mBool);
    }

    @Test
    public void testParseDynamicTimestampTypesWithTimeZone() {
        // Sets
        Assert.assertEquals(AttributeUtils.parse("<[2015-01-01T00:00:00]>", TimestampSet.class), AttributeUtils
                .parse("<[2015-01-01T00:00:00]>", TimestampSet.class, null));
        Assert.assertEquals(AttributeUtils.parse("<[2015-01-01T00:00:00]>", TimestampSet.class), AttributeUtils
                .parse("<[2015-01-01T00:00:00]>", TimestampSet.class, DateTimeZone.UTC));
        Assert.assertEquals(AttributeUtils.parse("<[2015-01-01T00:00:00]>", TimestampSet.class), AttributeUtils
                .parse("<[2015-01-01T01:30:00]>", TimestampSet.class, DateTimeZone.forID("+01:30")));

        // Maps
        Assert.assertEquals(AttributeUtils
                .parse("<[2015-01-01T00:00:00, val]>", TimestampStringMap.class), AttributeUtils
                        .parse("<[2015-01-01T00:00:00, val]>", TimestampStringMap.class, null));
        Assert.assertEquals(AttributeUtils
                .parse("<[2015-01-01T00:00:00, val]>", TimestampStringMap.class), AttributeUtils
                        .parse("<[2015-01-01T00:00:00, val]>", TimestampStringMap.class, DateTimeZone.UTC));
        Assert.assertEquals(AttributeUtils
                .parse("<[2015-01-01T00:00:00, val]>", TimestampStringMap.class), AttributeUtils
                        .parse("<[2015-01-01T01:30:00, val]>", TimestampStringMap.class, DateTimeZone.forID("+01:30")));
    }

    @Test
    public void testParseDynamicIntervalTypesWithTimeZone() {
        // Sets
        Assert.assertEquals(AttributeUtils
                .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00]>", IntervalSet.class), AttributeUtils
                        .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00]>", IntervalSet.class, null));
        Assert.assertEquals(AttributeUtils
                .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00]>", IntervalSet.class), AttributeUtils
                        .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00]>", IntervalSet.class, DateTimeZone.UTC));
        Assert.assertEquals(AttributeUtils
                .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00]>", IntervalSet.class), AttributeUtils
                        .parse("<[2014-12-31T22:00:00, 2015-01-01T00:00:00]>", IntervalSet.class, DateTimeZone
                                .forID("-02:00")));

        // Maps
        Assert.assertEquals(AttributeUtils
                .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00, val]>", IntervalStringMap.class), AttributeUtils
                        .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00, val]>", IntervalStringMap.class, null));
        Assert.assertEquals(AttributeUtils
                .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00, val]>", IntervalStringMap.class), AttributeUtils
                        .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00, val]>", IntervalStringMap.class, DateTimeZone.UTC));
        Assert.assertEquals(AttributeUtils
                .parse("<[2015-01-01T00:00:00, 2015-01-01T02:00:00, val]>", IntervalStringMap.class), AttributeUtils
                        .parse("<[2014-12-31T22:00:00, 2015-01-01T00:00:00, val]>", IntervalStringMap.class, DateTimeZone
                                .forID("-02:00")));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseCharInvalid() {
        Assert.assertNull(AttributeUtils.parse("test", Character.class));
    }

    @Test
    public void testParseNull() {
        Assert.assertNull(AttributeUtils.parse(null, Integer.class));
    }

    @Test
    public void testParseEmpty() {
        Assert.assertNull(AttributeUtils.parse("", Integer.class));
    }

    @Test
    public void testParseInfinity() {
        Assert.assertEquals(AttributeUtils.parse("Infinity", Double.class), Double.POSITIVE_INFINITY);
        Assert.assertEquals(AttributeUtils.parse("+Infinity", Double.class), Double.POSITIVE_INFINITY);
        Assert.assertEquals(AttributeUtils.parse("-Infinity", Double.class), Double.NEGATIVE_INFINITY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseUnsupportedType() {
        AttributeUtils.parse("test", Color.class);
    }

    @Test
    public void testGetPrimitiveType() {
        Assert.assertEquals(AttributeUtils.getPrimitiveType(Integer.class), int.class);
        Assert.assertEquals(AttributeUtils.getPrimitiveType(Float.class), float.class);
        Assert.assertEquals(AttributeUtils.getPrimitiveType(Double.class), double.class);
        Assert.assertEquals(AttributeUtils.getPrimitiveType(Long.class), long.class);
        Assert.assertEquals(AttributeUtils.getPrimitiveType(Character.class), char.class);
        Assert.assertEquals(AttributeUtils.getPrimitiveType(Short.class), short.class);
        Assert.assertEquals(AttributeUtils.getPrimitiveType(Byte.class), byte.class);
        Assert.assertEquals(AttributeUtils.getPrimitiveType(Boolean.class), boolean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetPrimitiveTypeUnsupportedType() {
        AttributeUtils.getPrimitiveType(Color.class);
    }

    @Test
    public void testGetPrimitiveArray() {
        Assert.assertEquals((int[]) AttributeUtils.getPrimitiveArray(new Integer[] { 1, 2 }), new int[] { 1, 2 });
        Assert.assertEquals((float[]) AttributeUtils.getPrimitiveArray(new Float[] { 1f, 2f }), new float[] { 1f, 2f });
        Assert.assertEquals((double[]) AttributeUtils
                .getPrimitiveArray(new Double[] { 1.0, 2.0 }), new double[] { 1.0, 2.0 });
        Assert.assertEquals((long[]) AttributeUtils.getPrimitiveArray(new Long[] { 1l, 2l }), new long[] { 1l, 2l });
        Assert.assertEquals((char[]) AttributeUtils.getPrimitiveArray(new Character[] { 1, 2 }), new char[] { 1, 2 });
        Assert.assertEquals((short[]) AttributeUtils.getPrimitiveArray(new Short[] { 1, 2 }), new short[] { 1, 2 });
        Assert.assertEquals((byte[]) AttributeUtils.getPrimitiveArray(new Byte[] { 1, 2 }), new byte[] { 1, 2 });
        Assert.assertEquals((boolean[]) AttributeUtils
                .getPrimitiveArray(new Boolean[] { true, false }), new boolean[] { true, false });
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetPrimitiveArrayUnsupportedType() {
        AttributeUtils.getPrimitiveArray(new Color[] { Color.BLACK });
    }

    @Test
    public void testGetStandardizedType() {
        Assert.assertEquals(AttributeUtils.getStandardizedType(Integer.class), Integer.class);
        Assert.assertEquals(AttributeUtils.getStandardizedType(int.class), Integer.class);
        Assert.assertEquals(AttributeUtils.getStandardizedType(Integer[].class), int[].class);
        Assert.assertEquals(AttributeUtils.getStandardizedType(String[].class), String[].class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetStandardizedTypeUnsupportedType() {
        AttributeUtils.getStandardizedType(Color.class);
    }

    @Test
    public void testIsStandardizedType() {
        Assert.assertTrue(AttributeUtils.isStandardizedType(Integer.class));
        Assert.assertTrue(AttributeUtils.isStandardizedType(int[].class));
        Assert.assertFalse(AttributeUtils.isStandardizedType(Integer[].class));
        Assert.assertTrue(AttributeUtils.isStandardizedType(String.class));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsStandardizedTypeUnsupportedType() {
        AttributeUtils.isStandardizedType(Color.class);
    }

    @Test
    public void testIsSupported() {
        Assert.assertTrue(AttributeUtils.isSupported(Integer.class));
        Assert.assertTrue(AttributeUtils.isSupported(int.class));
        Assert.assertTrue(AttributeUtils.isSupported(int[].class));
        Assert.assertTrue(AttributeUtils.isSupported(TimestampDoubleMap.class));
        Assert.assertTrue(AttributeUtils.isSupported(TimestampSet.class));
        Assert.assertTrue(AttributeUtils.isSupported(IntervalDoubleMap.class));
        Assert.assertTrue(AttributeUtils.isSupported(IntervalSet.class));
        Assert.assertTrue(AttributeUtils.isSupported(List.class));
        Assert.assertTrue(AttributeUtils.isSupported(ArrayList.class));
        Assert.assertTrue(AttributeUtils.isSupported(Set.class));
        Assert.assertTrue(AttributeUtils.isSupported(HashSet.class));
        Assert.assertTrue(AttributeUtils.isSupported(Map.class));
        Assert.assertTrue(AttributeUtils.isSupported(HashMap.class));

        Assert.assertFalse(AttributeUtils.isSupported(Color.class));
        Assert.assertFalse(AttributeUtils.isSupported(Collection.class));
        Assert.assertFalse(AttributeUtils.isSupported(Iterable.class));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testIsSupportedNull() {
        AttributeUtils.isSupported(null);
    }

    @Test
    public void testGetTimestampMapType() {
        Assert.assertEquals(AttributeUtils.getTimestampMapType(Integer.class), TimestampIntegerMap.class);
        Assert.assertEquals(AttributeUtils.getTimestampMapType(Float.class), TimestampFloatMap.class);
        Assert.assertEquals(AttributeUtils.getTimestampMapType(Double.class), TimestampDoubleMap.class);
        Assert.assertEquals(AttributeUtils.getTimestampMapType(Long.class), TimestampLongMap.class);
        Assert.assertEquals(AttributeUtils.getTimestampMapType(Character.class), TimestampCharMap.class);
        Assert.assertEquals(AttributeUtils.getTimestampMapType(Short.class), TimestampShortMap.class);
        Assert.assertEquals(AttributeUtils.getTimestampMapType(Byte.class), TimestampByteMap.class);
        Assert.assertEquals(AttributeUtils.getTimestampMapType(Boolean.class), TimestampBooleanMap.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetTimestampMapTypeUnsupportedType() {
        AttributeUtils.getTimestampMapType(Color.class);
    }

    @Test
    public void testGetIntervalMapType() {
        Assert.assertEquals(AttributeUtils.getIntervalMapType(Integer.class), IntervalIntegerMap.class);
        Assert.assertEquals(AttributeUtils.getIntervalMapType(Float.class), IntervalFloatMap.class);
        Assert.assertEquals(AttributeUtils.getIntervalMapType(Double.class), IntervalDoubleMap.class);
        Assert.assertEquals(AttributeUtils.getIntervalMapType(Long.class), IntervalLongMap.class);
        Assert.assertEquals(AttributeUtils.getIntervalMapType(Character.class), IntervalCharMap.class);
        Assert.assertEquals(AttributeUtils.getIntervalMapType(Short.class), IntervalShortMap.class);
        Assert.assertEquals(AttributeUtils.getIntervalMapType(Byte.class), IntervalByteMap.class);
        Assert.assertEquals(AttributeUtils.getIntervalMapType(Boolean.class), IntervalBooleanMap.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetIntervalMapTypeUnsupportedType() {
        AttributeUtils.getIntervalMapType(Color.class);
    }

    @Test
    public void testGetStaticTypeTimestampMap() {
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampIntegerMap.class), Integer.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampFloatMap.class), Float.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampDoubleMap.class), Double.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampLongMap.class), Long.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampCharMap.class), Character.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampShortMap.class), Short.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampByteMap.class), Byte.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampBooleanMap.class), Boolean.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampStringMap.class), String.class);
    }

    @Test
    public void testGetStaticTypeIntervalMap() {
        Assert.assertEquals(AttributeUtils.getStaticType(IntervalIntegerMap.class), Integer.class);
        Assert.assertEquals(AttributeUtils.getStaticType(IntervalFloatMap.class), Float.class);
        Assert.assertEquals(AttributeUtils.getStaticType(IntervalDoubleMap.class), Double.class);
        Assert.assertEquals(AttributeUtils.getStaticType(IntervalLongMap.class), Long.class);
        Assert.assertEquals(AttributeUtils.getStaticType(IntervalCharMap.class), Character.class);
        Assert.assertEquals(AttributeUtils.getStaticType(IntervalShortMap.class), Short.class);
        Assert.assertEquals(AttributeUtils.getStaticType(IntervalByteMap.class), Byte.class);
        Assert.assertEquals(AttributeUtils.getStaticType(IntervalBooleanMap.class), Boolean.class);
        Assert.assertEquals(AttributeUtils.getStaticType(IntervalStringMap.class), String.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetStaticTypeUnsupportedTypeTimestampMap() {
        AttributeUtils.getStaticType(TimestampMap.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetStaticTypeUnsupportedTypeIntervalMap() {
        AttributeUtils.getStaticType(IntervalMap.class);
    }

    @Test
    public void testStandardizeValue() {
        Assert.assertEquals(AttributeUtils.standardizeValue(1), 1);
        Assert.assertEquals((int[]) AttributeUtils.standardizeValue(new Integer[] { 1, 2 }), new int[] { 1, 2 });
        Assert.assertEquals((int[]) AttributeUtils.standardizeValue(new int[] { 1, 2 }), new int[] { 1, 2 });
        Assert.assertEquals((String[]) AttributeUtils.standardizeValue(new String[] { "foo" }), new String[] { "foo" });

        Assert.assertEquals(AttributeUtils.standardizeValue(new ArrayList()), new ArrayList());
        Assert.assertEquals(AttributeUtils.standardizeValue(new HashSet()), new HashSet());
        Assert.assertEquals(AttributeUtils.standardizeValue(new HashMap()), new HashMap());

        List<Integer> list1 = Arrays.asList(42);
        Assert.assertEquals((List<Integer>) AttributeUtils.standardizeValue(list1), list1);

        List list2 = Arrays.asList("foo");
        Assert.assertEquals((List) AttributeUtils.standardizeValue(list2), list2);

        List list3 = new ArrayList();
        list3.add(new Integer[] { 1, 2 });
        Assert.assertEquals(((List) AttributeUtils.standardizeValue(list3)).get(0), new int[] { 1, 2 });

        Set set1 = new HashSet(Arrays.asList("bar", "foo"));
        Assert.assertEquals((Set) AttributeUtils.standardizeValue(set1), set1);

        Set set2 = new HashSet(Arrays.asList(32, 42));
        Assert.assertEquals((Set) AttributeUtils.standardizeValue(set2), set2);

        Map map1 = new HashMap();
        map1.put("foo", "bar");
        map1.put("bar", new Integer[] { 1, 2 });
        Map map1Result = (Map) AttributeUtils.standardizeValue(map1);
        Assert.assertEquals(map1Result.get("foo"), "bar");
        Assert.assertEquals(map1Result.get("bar"), new int[] { 1, 2 });

        Map map2 = new HashMap();
        map2.put(42, "bar");
        Assert.assertEquals((Map) AttributeUtils.standardizeValue(map2), map2);
    }

    @Test
    public void testStandardizeValueNull() {
        Assert.assertEquals(AttributeUtils.standardizeValue(null), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStandardizeValueUnsupportedType() {
        AttributeUtils.standardizeValue(new Color(0, 0, 0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStandardizeValueUnsupportedListContent() {
        AttributeUtils.standardizeValue(Arrays.asList(Color.BLACK));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStandardizeValueMixedListContent() {
        AttributeUtils.standardizeValue(Arrays.asList("foo", 42));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStandardizeValueUnsupportedSetContent() {
        AttributeUtils.standardizeValue(new HashSet(Arrays.asList(Color.BLACK)));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStandardizeValueMixedSetContent() {
        AttributeUtils.standardizeValue(new HashSet(Arrays.asList("foo", 42)));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStandardizeValueUnsupportedMapKeyContent() {
        Map map = new HashMap();
        map.put(new Object[] { 1 }, "bar");
        AttributeUtils.standardizeValue(map);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStandardizeValueUnsupportedMapValueContent() {
        Map map = new HashMap();
        map.put("foo", Color.BLACK);
        AttributeUtils.standardizeValue(map);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStandardizeValueMixedMapKeyContent() {
        Map map = new HashMap();
        map.put(42, "foo");
        map.put("foo", 42);
        AttributeUtils.standardizeValue(map);
    }

    @Test
    public void testParseDate() {
        Assert.assertEquals(AttributeUtils.parseDateTime("1970-01-01T00:00:00"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTime("1970-01-01T00:00:00", null), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTime("1970-01-01T00:00:00", DateTimeZone.UTC), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTime("1970-01-01T01:30:00", DateTimeZone.forID("+01:30")), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTime("1970-01-01T00:00:00+00:00"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTime("1970-01-01T00:00:00Z"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTime("1970-01-01T00:00:00.000+00:00"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTime("1970-01-01T00:00:00.000Z"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTime("1969-12-31T22:00:00-02:00"), 0.0);

        AttributeUtils.parseDateTime("2003-01-01");
        AttributeUtils.parseDateTime("2012-09-12T15:04:01");
        AttributeUtils.parseDateTime("20040401");

        Assert.assertEquals(AttributeUtils.parseDateTime("2012-09-12T15:04:01"), AttributeUtils
                .parseDateTime("2012-09-12T15:04:01", DateTimeZone.forID("+00:00")));
        Assert.assertEquals(AttributeUtils.parseDateTime("2012-09-12T15:04:01+03:30"), AttributeUtils
                .parseDateTime("2012-09-12T15:04:01", DateTimeZone.forID("+03:30")));
    }

    @Test
    public void testParseDateTimeOrTimestamp() {
        // Unix timestamps:
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("0"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("0.0"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("1.0"), 1.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("-123."), -123.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("2.5e6"), 2.5e6);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("-Infinity"), Double.NEGATIVE_INFINITY);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("Infinity"), Double.POSITIVE_INFINITY);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("0"), AttributeUtils
                .parseDateTimeOrTimestamp("1970-01-01T00:00:00"));
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("0"), AttributeUtils
                .parseDateTimeOrTimestamp("1970-01-01T00:00:00Z"));
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("0"), AttributeUtils
                .parseDateTimeOrTimestamp("1970-01-01T00:00:00", DateTimeZone.forID("+00:00")));

        // Dates
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("1970-01-01T00:00:00"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("1970-01-01T00:00:00", null), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("1970-01-01T00:00:00", DateTimeZone.UTC), 0.0);
        Assert.assertEquals(AttributeUtils
                .parseDateTimeOrTimestamp("1970-01-01T01:30:00", DateTimeZone.forID("+01:30")), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("1970-01-01T00:00:00+00:00"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("1970-01-01T00:00:00Z"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("1970-01-01T00:00:00.000+00:00"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("1970-01-01T00:00:00.000Z"), 0.0);
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("1969-12-31T22:00:00-02:00"), 0.0);

        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("2012-09-12T15:04:01"), AttributeUtils
                .parseDateTime("2012-09-12T15:04:01", DateTimeZone.forID("+00:00")));
        Assert.assertEquals(AttributeUtils.parseDateTimeOrTimestamp("2012-09-12T15:04:01+03:30"), AttributeUtils
                .parseDateTime("2012-09-12T15:04:01", DateTimeZone.forID("+03:30")));
    }

    @Test
    public void testPrintDate() {
        String date = "2003-01-01";
        double d = AttributeUtils.parseDateTime(date);

        Assert.assertEquals(AttributeUtils.printDate(d), date);

        Assert.assertEquals(AttributeUtils.printDate(d, DateTimeZone.UTC), date);
        Assert.assertEquals(AttributeUtils.printDate(d, null), date);
        Assert.assertEquals(AttributeUtils.printDate(d, DateTimeZone.forID("+00:30")), "2003-01-01");// Still
                                                                                                     // same
                                                                                                     // day
        Assert.assertEquals(AttributeUtils.printDate(d, DateTimeZone.forID("+12:00")), "2003-01-01");// Still
                                                                                                     // same
                                                                                                     // day
        Assert.assertEquals(AttributeUtils.printDate(d, DateTimeZone.forID("-00:30")), "2002-12-31");// Previous
                                                                                                     // day
    }

    @Test
    public void testPrintDateTime() {
        String date = "2003-01-01T00:00:00.000-08:00";
        double d = AttributeUtils.parseDateTime(date);

        String dateInUTC = AttributeUtils.printDateTime(d);
        Assert.assertEquals(AttributeUtils.parseDateTime(dateInUTC), d);

        Assert.assertEquals(AttributeUtils.printDateTime(d, DateTimeZone.UTC), dateInUTC);
        Assert.assertEquals(AttributeUtils.printDateTime(d, null), dateInUTC);
        Assert.assertEquals(AttributeUtils.printDateTime(d), "2003-01-01T08:00:00.000Z");
        Assert.assertEquals(AttributeUtils
                .printDateTime(d, DateTimeZone.forID("+00:30")), "2003-01-01T08:30:00.000+00:30");
        Assert.assertEquals(AttributeUtils
                .printDateTime(d, DateTimeZone.forID("+12:00")), "2003-01-01T20:00:00.000+12:00");
        Assert.assertEquals(AttributeUtils
                .printDateTime(d, DateTimeZone.forID("-12:00")), "2002-12-31T20:00:00.000-12:00");

        Assert.assertEquals(AttributeUtils.printDateTime(AttributeUtils
                .parseDateTime("2003-01-01T16:00:00", DateTimeZone.forID("+00:00")), DateTimeZone
                        .forID("+12:00")), "2003-01-02T04:00:00.000+12:00");
    }

    @Test
    public void testPrintArray() {
        Assert.assertEquals(AttributeUtils
                .printArray(new String[] { null, "null", " b ", "\"c" }), "[null, \"null\", \" b \", \"\\\"c\"]");
        Assert.assertEquals(AttributeUtils.printArray(new Integer[] { -1, 2, 3, null }), "[-1, 2, 3, null]");
        Assert.assertEquals(AttributeUtils.printArray(new int[] { -1, 2, 3 }), "[-1, 2, 3]");
        Assert.assertEquals(AttributeUtils.printArray(new boolean[] { true, false, true }), "[true, false, true]");
        Assert.assertEquals(AttributeUtils.printArray(new char[] {}), "<empty>");
        Assert.assertEquals(AttributeUtils.printArray(new String[] { "[a, b, c]" }), "[\"[a, b, c]\"]");
    }

    @Test
    public void testPrint() {
        Assert.assertEquals(AttributeUtils.print(null), "null");
        Assert.assertEquals(Integer.toString(42), AttributeUtils.print(42));
        Assert.assertEquals("foo", AttributeUtils.print("foo"));
        Assert.assertEquals("[-1, 2, 3]", AttributeUtils.print(new int[] { -1, 2, 3 }));

        String date = "2003-01-01";
        double d = AttributeUtils.parseDateTime(date);

        TimestampSet ts = new TimestampSet();
        ts.add(d);

        Assert.assertEquals(AttributeUtils.print(ts), ts.toString(TimeFormat.DOUBLE));
        Assert.assertEquals(AttributeUtils.print(ts, TimeFormat.DATE, null), ts.toString(TimeFormat.DATE, null));
        Assert.assertEquals(AttributeUtils.print(ts, TimeFormat.DATETIME, DateTimeZone.forID("+00:30")), ts
                .toString(TimeFormat.DATETIME, DateTimeZone.forID("+00:30")));

        TimestampIntegerMap tm = new TimestampIntegerMap();
        tm.put(d, 42);

        Assert.assertEquals(AttributeUtils.print(tm), tm.toString(TimeFormat.DOUBLE));
        Assert.assertEquals(AttributeUtils.print(tm, TimeFormat.DATE, null), tm.toString(TimeFormat.DATE, null));
        Assert.assertEquals(AttributeUtils.print(tm, TimeFormat.DATETIME, DateTimeZone.forID("+00:30")), tm
                .toString(TimeFormat.DATETIME, DateTimeZone.forID("+00:30")));
    }

    @Test
    public void testPrintInfinity() {
        Assert.assertEquals(AttributeUtils.print(Double.POSITIVE_INFINITY), "Infinity");
        Assert.assertEquals(AttributeUtils.print(Double.NEGATIVE_INFINITY), "-Infinity");
    }

    @Test
    public void testIsNumberType() {
        Assert.assertTrue(AttributeUtils.isNumberType(Integer.class));
        Assert.assertTrue(AttributeUtils.isNumberType(Integer[].class));
        Assert.assertTrue(AttributeUtils.isNumberType(int.class));
        Assert.assertTrue(AttributeUtils.isNumberType(int[].class));
        Assert.assertTrue(AttributeUtils.isNumberType(float[].class));
        Assert.assertTrue(AttributeUtils.isNumberType(double[].class));
        Assert.assertTrue(AttributeUtils.isNumberType(short[].class));
        Assert.assertTrue(AttributeUtils.isNumberType(long[].class));
        Assert.assertTrue(AttributeUtils.isNumberType(byte[].class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampByteMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampDoubleMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampFloatMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampIntegerMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampLongMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampShortMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(IntervalByteMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(IntervalDoubleMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(IntervalFloatMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(IntervalIntegerMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(IntervalLongMap.class));
        Assert.assertTrue(AttributeUtils.isNumberType(IntervalShortMap.class));
        Assert.assertFalse(AttributeUtils.isNumberType(String.class));
        Assert.assertFalse(AttributeUtils.isNumberType(String[].class));
        Assert.assertTrue(AttributeUtils.isNumberType(BigDecimal.class));
        Assert.assertTrue(AttributeUtils.isNumberType(BigInteger.class));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsNumberTypeUnsupportedType() {
        AttributeUtils.isNumberType(Color.class);
    }

    @Test
    public void testIsBooleanType() {
        Assert.assertTrue(AttributeUtils.isBooleanType(Boolean.class));
        Assert.assertTrue(AttributeUtils.isBooleanType(Boolean[].class));
        Assert.assertTrue(AttributeUtils.isBooleanType(boolean[].class));
        Assert.assertTrue(AttributeUtils.isBooleanType(boolean.class));
        Assert.assertTrue(AttributeUtils.isBooleanType(IntervalBooleanMap.class));
        Assert.assertTrue(AttributeUtils.isBooleanType(TimestampBooleanMap.class));
        Assert.assertFalse(AttributeUtils.isBooleanType(String.class));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsBooleanTypeUnsupportedType() {
        AttributeUtils.isBooleanType(Color.class);
    }

    @Test
    public void testIsStringType() {
        Assert.assertTrue(AttributeUtils.isStringType(String.class));
        Assert.assertTrue(AttributeUtils.isStringType(String[].class));
        Assert.assertTrue(AttributeUtils.isStringType(IntervalStringMap.class));
        Assert.assertTrue(AttributeUtils.isStringType(TimestampStringMap.class));
        Assert.assertFalse(AttributeUtils.isStringType(Integer.class));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIsStringTypeUnsupportedType() {
        AttributeUtils.isStringType(Color.class);
    }

    @Test
    public void testIsDynamicType() {
        // Interval types:
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalSet.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalStringMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalByteMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalShortMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalIntegerMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalLongMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalFloatMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalDoubleMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalBooleanMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(IntervalCharMap.class));

        // Timestamp types:
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampSet.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampStringMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampByteMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampShortMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampIntegerMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampLongMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampFloatMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampDoubleMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampBooleanMap.class));
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampCharMap.class));

        // Some types that should not be dynamic
        Assert.assertFalse(AttributeUtils.isDynamicType(TimestampFloatMap[].class));
        Assert.assertFalse(AttributeUtils.isDynamicType(IntervalFloatMap[].class));
        Assert.assertFalse(AttributeUtils.isDynamicType(Integer.class));
        Assert.assertFalse(AttributeUtils.isDynamicType(TimestampMap.class));
        Assert.assertFalse(AttributeUtils.isDynamicType(IntervalMap.class));
    }

    @Test
    public void testIsCollectionType() {
        Assert.assertTrue(AttributeUtils.isCollectionType(List.class));
        Assert.assertTrue(AttributeUtils.isCollectionType(ArrayList.class));
        Assert.assertTrue(AttributeUtils.isCollectionType(Set.class));
        Assert.assertTrue(AttributeUtils.isCollectionType(HashSet.class));
        Assert.assertFalse(AttributeUtils.isCollectionType(Integer.class));
        Assert.assertFalse(AttributeUtils.isCollectionType(Collection.class));
        Assert.assertFalse(AttributeUtils.isCollectionType(Object[].class));
        Assert.assertFalse(AttributeUtils.isCollectionType(List[].class));
    }

    @Test
    public void testIsMapType() {
        Assert.assertTrue(AttributeUtils.isMapType(Map.class));
        Assert.assertTrue(AttributeUtils.isMapType(HashMap.class));
        Assert.assertFalse(AttributeUtils.isMapType(List.class));
        Assert.assertFalse(AttributeUtils.isMapType(Map[].class));
    }

    @Test
    public void getTypeName() {
        Assert.assertEquals(AttributeUtils.getTypeName(Integer.class), Integer.class.getSimpleName().toLowerCase());
        Assert.assertEquals(AttributeUtils.getTypeName(int.class), Integer.class.getSimpleName().toLowerCase());
        Assert.assertEquals(AttributeUtils.getTypeName(Integer[].class), int[].class.getSimpleName().toLowerCase());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testgetTypeNameUnsupportedType() {
        AttributeUtils.getTypeName(Color.class);
    }

    @Test
    public void testIsNodeColumn() {
        TableImpl tableNode = new TableImpl(Node.class, false);
        TableImpl tableEdge = new TableImpl(Edge.class, false);
        Column column = tableNode.addColumn("0", Integer.class);

        Assert.assertTrue(AttributeUtils.isNodeColumn(column));
        Assert.assertFalse(AttributeUtils.isNodeColumn(tableEdge.addColumn("0", Float.class)));
    }

    @Test
    public void testIsEdgeColumn() {
        TableImpl tableNode = new TableImpl(Node.class, false);
        TableImpl tableEdge = new TableImpl(Edge.class, false);
        Column column = tableEdge.addColumn("0", Integer.class);

        Assert.assertTrue(AttributeUtils.isEdgeColumn(column));
        Assert.assertFalse(AttributeUtils.isEdgeColumn(tableNode.addColumn("0", Float.class)));
    }

    @Test
    public void testIsSimpleType() {
        Assert.assertTrue(AttributeUtils.isSimpleType(int.class));
        Assert.assertTrue(AttributeUtils.isSimpleType(String.class));
        Assert.assertTrue(AttributeUtils.isSimpleType(Double.class));
        Assert.assertTrue(AttributeUtils.isSimpleType(Float.class));
        Assert.assertTrue(AttributeUtils.isSimpleType(Integer.class));
        Assert.assertTrue(AttributeUtils.isSimpleType(Short.class));
        Assert.assertTrue(AttributeUtils.isSimpleType(Byte.class));
        Assert.assertTrue(AttributeUtils.isSimpleType(Long.class));
        Assert.assertTrue(AttributeUtils.isSimpleType(Character.class));
        Assert.assertTrue(AttributeUtils.isSimpleType(Boolean.class));

        Assert.assertFalse(AttributeUtils.isSimpleType(void.class));
        Assert.assertFalse(AttributeUtils.isSimpleType(int[].class));
        Assert.assertFalse(AttributeUtils.isSimpleType(TimestampBooleanMap.class));
        Assert.assertFalse(AttributeUtils.isSimpleType(IntervalBooleanMap.class));
    }
}
