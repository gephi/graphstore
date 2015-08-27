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

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;
import org.gephi.attribute.api.AttributeUtils;
import org.gephi.attribute.api.Column;
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
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class AttributeUtilsTest {

    @Test
    public void testGetSupportedTypes() {
        Set<Class> types = AttributeUtils.getSupportedTypes();
        Assert.assertNotNull(types);
        Assert.assertFalse(types.isEmpty());
        Assert.assertTrue(Collections.unmodifiableSet(types).getClass().isInstance(types));
    }

    @Test
    public void testParse() {
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
        Assert.assertEquals((int[]) AttributeUtils.getPrimitiveArray(new Integer[]{1, 2}), new int[]{1, 2});
        Assert.assertEquals((float[]) AttributeUtils.getPrimitiveArray(new Float[]{1f, 2f}), new float[]{1f, 2f});
        Assert.assertEquals((double[]) AttributeUtils.getPrimitiveArray(new Double[]{1.0, 2.0}), new double[]{1.0, 2.0});
        Assert.assertEquals((long[]) AttributeUtils.getPrimitiveArray(new Long[]{1l, 2l}), new long[]{1l, 2l});
        Assert.assertEquals((char[]) AttributeUtils.getPrimitiveArray(new Character[]{1, 2}), new char[]{1, 2});
        Assert.assertEquals((short[]) AttributeUtils.getPrimitiveArray(new Short[]{1, 2}), new short[]{1, 2});
        Assert.assertEquals((byte[]) AttributeUtils.getPrimitiveArray(new Byte[]{1, 2}), new byte[]{1, 2});
        Assert.assertEquals((boolean[]) AttributeUtils.getPrimitiveArray(new Boolean[]{true, false}), new boolean[]{true, false});
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetPrimitiveArrayUnsupportedType() {
        AttributeUtils.getPrimitiveArray(new Color[]{Color.BLACK});
    }

    @Test
    public void testGetStandardizedType() {
        Assert.assertEquals(AttributeUtils.getStandardizedType(Integer.class), Integer.class);
        Assert.assertEquals(AttributeUtils.getStandardizedType(int.class), Integer.class);
        Assert.assertEquals(AttributeUtils.getStandardizedType(Integer[].class), int[].class);
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
        Assert.assertTrue(AttributeUtils.isSupported(TimestampDoubleSet.class));

        Assert.assertFalse(AttributeUtils.isSupported(Color.class));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testIsSupportedNull() {
        AttributeUtils.isSupported(null);
    }

    @Test
    public void testGetDynamicType() {
        Assert.assertEquals(AttributeUtils.getDynamicType(Integer.class), TimestampIntegerSet.class);
        Assert.assertEquals(AttributeUtils.getDynamicType(Float.class), TimestampFloatSet.class);
        Assert.assertEquals(AttributeUtils.getDynamicType(Double.class), TimestampDoubleSet.class);
        Assert.assertEquals(AttributeUtils.getDynamicType(Long.class), TimestampLongSet.class);
        Assert.assertEquals(AttributeUtils.getDynamicType(Character.class), TimestampCharSet.class);
        Assert.assertEquals(AttributeUtils.getDynamicType(Short.class), TimestampShortSet.class);
        Assert.assertEquals(AttributeUtils.getDynamicType(Byte.class), TimestampByteSet.class);
        Assert.assertEquals(AttributeUtils.getDynamicType(Boolean.class), TimestampBooleanSet.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetDynamicTypeUnsupportedType() {
        AttributeUtils.getDynamicType(Color.class);
    }

    @Test
    public void testGetStaticType() {
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampIntegerSet.class), Integer.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampFloatSet.class), Float.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampDoubleSet.class), Double.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampLongSet.class), Long.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampCharSet.class), Character.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampShortSet.class), Short.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampByteSet.class), Byte.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampBooleanSet.class), Boolean.class);
        Assert.assertEquals(AttributeUtils.getStaticType(TimestampStringSet.class), String.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetStaticTypeUnsupportedType() {
        AttributeUtils.getStaticType(TimestampValueSet.class);
    }

    @Test
    public void testStandardizeValue() {
        Assert.assertEquals(AttributeUtils.standardizeValue(new Integer(1)), 1);
        Assert.assertEquals((int[]) AttributeUtils.standardizeValue(new Integer[]{1, 2}), new int[]{1, 2});
    }

    @Test
    public void testStandardizeValueNull() {
        Assert.assertEquals(AttributeUtils.standardizeValue(null), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStandardizeValueUnsupportedType() {
        AttributeUtils.standardizeValue(new Color(0, 0, 0));
    }

    @Test
    public void testParseDate() {
        double d = AttributeUtils.parseDateTime("1970-01-01T00:00:00+00:00");
        Assert.assertEquals(d, 0.0);

        AttributeUtils.parseDateTime("2003-01-01");
        AttributeUtils.parseDateTime("2012-09-12T15:04:01");
        AttributeUtils.parseDateTime("20040401");
    }

    @Test
    public void testPrintDate() {
        String date = "2003-01-01";
        double d = AttributeUtils.parseDateTime(date);

        Assert.assertEquals(AttributeUtils.printDate(d), date);
    }

    @Test
    public void testPrintDateTime() {
        String date = "2003-01-01T00:00:00.000-08:00";
        double d = AttributeUtils.parseDateTime(date);

        String pr = AttributeUtils.printDateTime(d);
        Assert.assertEquals(AttributeUtils.parseDateTime(pr), d);
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
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampByteSet.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampDoubleSet.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampFloatSet.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampIntegerSet.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampLongSet.class));
        Assert.assertTrue(AttributeUtils.isNumberType(TimestampShortSet.class));
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
    public void testIsDynamicType() {
        Assert.assertTrue(AttributeUtils.isDynamicType(TimestampFloatSet.class));
        Assert.assertFalse(AttributeUtils.isDynamicType(TimestampFloatSet[].class));
        Assert.assertFalse(AttributeUtils.isDynamicType(Integer.class));
        Assert.assertFalse(AttributeUtils.isDynamicType(TimestampValueSet.class));
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
        TableImpl tableNode = new TableImpl(new ColumnStore(Node.class, false));
        TableImpl tableEdge = new TableImpl(new ColumnStore(Edge.class, false));
        Column column = tableNode.addColumn("0", Integer.class);

        Assert.assertTrue(AttributeUtils.isNodeColumn(column));
        Assert.assertFalse(AttributeUtils.isNodeColumn(tableEdge.addColumn("0", Float.class)));
    }

    @Test
    public void testIsEdgeColumn() {
        TableImpl tableNode = new TableImpl(new ColumnStore(Node.class, false));
        TableImpl tableEdge = new TableImpl(new ColumnStore(Edge.class, false));
        Column column = tableEdge.addColumn("0", Integer.class);

        Assert.assertTrue(AttributeUtils.isEdgeColumn(column));
        Assert.assertFalse(AttributeUtils.isEdgeColumn(tableNode.addColumn("0", Float.class)));
    }
}
