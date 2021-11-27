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

import java.math.BigDecimal;
import java.math.BigInteger;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Eduardo Ramos
 */
public class ArraysParserTest {

    @Test
    public void testParseBoolean() {
        Boolean[] a1 = ArraysParser.parseArray(Boolean[].class, "[false, true, false]");
        boolean[] a2 = (boolean[]) ArraysParser.parseArrayAsPrimitiveArray(Boolean[].class, "[true, true, false]");
        Boolean[] b1 = ArraysParser.parseArray(Boolean[].class, "[0, 1]");
        boolean[] b2 = (boolean[]) ArraysParser.parseArrayAsPrimitiveArray(Boolean[].class, "[1, 0]");

        Assert.assertEquals(new Boolean[] { false, true, false }, a1);
        Assert.assertEquals(new boolean[] { true, true, false }, a2);
        Assert.assertEquals(new Boolean[] { false, true }, b1);
        Assert.assertEquals(new boolean[] { true, false }, b2);
    }

    @Test
    public void testParseString() {
        String[] a1 = ArraysParser.parseArray(String[].class, "[-1, ' value',    value2]");

        Assert.assertEquals(new String[] { "-1", " value", "value2" }, a1);
    }

    @Test
    public void testParseCharacter() {
        Character[] a1 = ArraysParser.parseArray(Character[].class, "[a, b, c, 2, 9]");
        char[] a2 = (char[]) ArraysParser.parseArrayAsPrimitiveArray(Character[].class, "[a, b, c, 2, 9]");

        Assert.assertEquals(new Character[] { 'a', 'b', 'c', '2', '9' }, a1);
        Assert.assertEquals(new char[] { 'a', 'b', 'c', '2', '9' }, a2);
    }

    @Test
    public void testParseByte() {
        Byte[] a1 = ArraysParser.parseArray(Byte[].class, "[-1, 1, 8]");
        byte[] a2 = (byte[]) ArraysParser.parseArrayAsPrimitiveArray(Byte[].class, "[1, 2, 3]");

        Assert.assertEquals(new Byte[] { -1, 1, 8 }, a1);
        Assert.assertEquals(new byte[] { 1, 2, 3 }, a2);
    }

    @Test
    public void testParseShort() {
        Short[] a1 = ArraysParser.parseArray(Short[].class, "[-1, 1, 50]");
        short[] a2 = (short[]) ArraysParser.parseArrayAsPrimitiveArray(Short[].class, "[1, 2, 3]");

        Assert.assertEquals(new Short[] { -1, 1, 50 }, a1);
        Assert.assertEquals(new short[] { 1, 2, 3 }, a2);
    }

    @Test
    public void testParseInteger() {
        Integer[] a1 = ArraysParser.parseArray(Integer[].class, "[-1, 1, 50]");
        int[] a2 = (int[]) ArraysParser.parseArrayAsPrimitiveArray(Integer[].class, "[1, 2, 3]");

        Assert.assertEquals(new Integer[] { -1, 1, 50 }, a1);
        Assert.assertEquals(new int[] { 1, 2, 3 }, a2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntegerIncorrect() {
        ArraysParser.parseArray(Integer[].class, "[1, b]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntegerOutOfBounds() {
        ArraysParser.parseArray(Integer[].class, "[10000000000000000]");
    }

    @Test
    public void testParseLong() {
        Long[] a1 = ArraysParser.parseArray(Long[].class, "[-1, 1, 9223372036854775807]");
        long[] a2 = (long[]) ArraysParser.parseArrayAsPrimitiveArray(Long[].class, "[1, 2, 3]");

        Assert.assertEquals(new Long[] { -1l, 1l, 9223372036854775807L }, a1);
        Assert.assertEquals(new long[] { 1, 2, 3 }, a2);
    }

    @Test
    public void testParseFloat() {
        Float[] a1 = ArraysParser.parseArray(Float[].class, "[-1e6, .01, 1., 2e6]");
        float[] a2 = (float[]) ArraysParser.parseArrayAsPrimitiveArray(Float[].class, "[-1e6, .01, 1., 2e6]");

        Assert.assertEquals(new Float[] { -1e6f, .01f, 1.f, 2e6f }, a1);
        Assert.assertEquals(new float[] { -1e6f, .01f, 1.f, 2e6f }, a2);
    }

    @Test
    public void testParseDouble() {
        Double[] a1 = ArraysParser.parseArray(Double[].class, "[-1e6, .01, 1., 2e6]");
        double[] a2 = (double[]) ArraysParser.parseArrayAsPrimitiveArray(Double[].class, "[-1e6, .01, 1., 2e6]");

        Assert.assertEquals(new Double[] { -1e6, .01, 1., 2e6 }, a1);
        Assert.assertEquals(new double[] { -1e6, .01, 1., 2e6 }, a2);
    }

    @Test
    public void testParseBigInteger() {
        BigInteger[] a1 = ArraysParser
                .parseArray(BigInteger[].class, "[123456789123456789123456789, -123456789123456789123456789]");

        Assert.assertEquals(new BigInteger[] { new BigInteger("123456789123456789123456789"), new BigInteger(
                "-123456789123456789123456789") }, a1);
    }

    @Test
    public void testParseBigDecimal() {
        BigDecimal[] a1 = ArraysParser
                .parseArray(BigDecimal[].class, "[123456789123456789123456789.123456789123456789123456789, -123456789123456789123456789.123456789123456789123456789]");

        Assert.assertEquals(new BigDecimal[] { new BigDecimal(
                "123456789123456789123456789.123456789123456789123456789"), new BigDecimal(
                        "-123456789123456789123456789.123456789123456789123456789") }, a1);
    }

    @Test
    public void testParseNull() {
        Boolean[] a1 = ArraysParser.parseArray(Boolean[].class, "[false, null, false]");
        String[] a2 = ArraysParser.parseArray(String[].class, "[\"null\", null, 'null', value]");

        Assert.assertEquals(new Boolean[] { false, null, false }, a1);
        Assert.assertEquals(new String[] { "null", null, "null", "value" }, a2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseNullPrimitiveNotAllowed() {
        ArraysParser.parseArrayAsPrimitiveArray(Integer[].class, "[1, null]");
    }

    @Test
    public void testParseEmpty() {
        String[] a1 = ArraysParser.parseArray(String[].class, "<empty>");
        long[] a2 = (long[]) ArraysParser.parseArrayAsPrimitiveArray(Long[].class, "<empty>");
        String[] a3 = ArraysParser.parseArray(String[].class, "[]");

        Assert.assertEquals(new String[] {}, a1);
        Assert.assertEquals(new String[] {}, a3);
        Assert.assertEquals(new long[] {}, a2);
    }

    @Test
    public void testParseComplexFormats() {
        Assert.assertEquals(new String[] { "value1", "value2", " value 'b' \" 3 " }, ArraysParser
                .parseArray(String[].class, "value1,value2, \" value 'b' \\\" 3 \""));

        Assert.assertEquals(new String[] { "value1", "value2", " value \"b\" ' 3 " }, ArraysParser
                .parseArray(String[].class, "value1, value2,' value \"b\" \\' 3 '"));
    }
}
