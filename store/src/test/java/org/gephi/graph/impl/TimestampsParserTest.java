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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.gephi.graph.api.types.TimestampBooleanMap;
import org.gephi.graph.api.types.TimestampByteMap;
import org.gephi.graph.api.types.TimestampCharMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampFloatMap;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.graph.api.types.TimestampLongMap;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampShortMap;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.graph.api.types.TimestampSet;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Eduardo Ramos
 */
public class TimestampsParserTest {

    private TimestampSet buildTimestampSet(double... timestamps) {
        TimestampSet set = new TimestampSet();
        for (double timestamp : timestamps) {
            set.add(timestamp);
        }

        return set;
    }

    private static final SimpleDateFormat dateFormat;
    private static final SimpleDateFormat dateTimeFormat;
    private static final SimpleDateFormat dateTimeFormatMillis;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        dateTimeFormatMillis = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateTimeFormatMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private long parseDateFormatIntoTimestamp(String str, SimpleDateFormat sdf) throws ParseException {
        return sdf.parse(str).getTime();
    }

    private long parseDateIntoTimestamp(String str) throws ParseException {
        return parseDateFormatIntoTimestamp(str, dateFormat);
    }

    private long parseDateTimeIntoTimestamp(String str) throws ParseException {
        return parseDateFormatIntoTimestamp(str, dateTimeFormat);
    }

    private long parseDateTimeMillisIntoTimestamp(String str) throws ParseException {
        return parseDateFormatIntoTimestamp(str, dateTimeFormatMillis);
    }

    @Test
    public void testParseTimestampSet() throws ParseException {
        assertNull(TimestampsParser.parseTimestampSet(null));
        assertEquals(new TimestampSet(), TimestampsParser.parseTimestampSet("[]"));

        // Doubles:
        assertEquals(buildTimestampSet(1), TimestampsParser.parseTimestampSet("[1]"));
        assertEquals(buildTimestampSet(1, 2), TimestampsParser.parseTimestampSet("[1, 2]"));
        assertEquals(buildTimestampSet(1, 2, 3), TimestampsParser.parseTimestampSet("<[1, 2, 3]>"));
        assertEquals(buildTimestampSet(1, 2, 31), TimestampsParser.parseTimestampSet("<[1, 2,31.]>"));
        assertEquals(buildTimestampSet(1, 2, 31), TimestampsParser.parseTimestampSet("<[1,2,31.0)"));
        assertEquals(buildTimestampSet(-5000, -1, 0, 0.5), TimestampsParser.parseTimestampSet("(-5000,-1, 0, .5)"));
        assertEquals(buildTimestampSet(-5000, -1, 0, 0.5), TimestampsParser.parseTimestampSet("(-5e3, -1, 0, .5)"));

        // Dates:
        assertEquals(buildTimestampSet(parseDateIntoTimestamp("2015-01-01"), parseDateIntoTimestamp("2015-01-31")), TimestampsParser
                .parseTimestampSet("[2015-01-01, 2015-01-31]"));
        assertEquals(buildTimestampSet(parseDateIntoTimestamp("2015-01-01"), parseDateIntoTimestamp("2015-01-31")), TimestampsParser
                .parseTimestampSet("[2015-01, 2015-01-31]"));

        // Date times:
        assertEquals(buildTimestampSet(parseDateTimeIntoTimestamp("2015-01-01 21:12:05"), parseDateTimeIntoTimestamp("2015-01-02 00:00:00")), TimestampsParser
                .parseTimestampSet("[2015-01-01T21:12:05, 2015-01-02]"));
        assertEquals(buildTimestampSet(parseDateTimeMillisIntoTimestamp("2015-01-01 21:12:05.121"), parseDateTimeMillisIntoTimestamp("2015-01-02 00:00:01.999")), TimestampsParser
                .parseTimestampSet("[2015-01-01T21:12:05.121, 2015-01-02T00:00:01.999]"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampSetBadDateFormat1() {
        TimestampsParser.parseTimestampSet("[2015-13-01, 2015-01-31]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampSetBadDateFormat2() {
        TimestampsParser.parseTimestampSet("[2015-01-35, 2015-01-31]");
    }

    private <T> void assertEqualTimestampMaps(TimestampMap<T> expected, TimestampMap<T> result) {
        assertEquals(expected, result);
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testParseTimestampsEmpty() {
        TimestampStringMap expectedMap = new TimestampStringMap();
        assertEqualTimestampMaps(expectedMap, TimestampsParser.parseTimestampMap(String.class, "<empty>"));
        assertEqualTimestampMaps(expectedMap, TimestampsParser.parseTimestampMap(String.class, "<EMPTY>"));

        TimestampSet expectedSet = new TimestampSet();
        assertEquals(expectedSet, TimestampsParser.parseTimestampSet("<empty>"));
        assertEquals(expectedSet, TimestampsParser.parseTimestampSet("<EMPTY>"));
    }

    @Test
    public void testParseTimestampMapString() {
        TimestampStringMap expected = new TimestampStringMap();
        expected.put(1.0, "Value1");
        expected.put(3.0, "Value2");
        expected.put(5.0, "Value 3");
        expected.put(6.0, " Value 4 ");

        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(String.class, "[1, Value1]; [3, 'Value2']; [5, Value 3]; [6, \" Value 4 \"]"));
    }

    @Test
    public void testParseTimestampMapByte() {
        TimestampByteMap expected = new TimestampByteMap();
        expected.put(1.0, (byte) 1);
        expected.put(3.0, (byte) 2);
        expected.put(6.0, (byte) 3);
        expected.put(7.0, (byte) 4);

        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(Byte.class, "[1, 1]; [3, 2]; [6, '3']; [7, \"4\"]"));
        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(byte.class, "[1, 1]; [3, 2]; [6, '3']; [7, \"4\"]"));
    }

    @Test
    public void testParseTimestampMapShort() {
        TimestampShortMap expected = new TimestampShortMap();
        expected.put(1.0, (short) 1);
        expected.put(3.0, (short) 2);
        expected.put(5.0, (short) 3);
        expected.put(6.0, (short) 4);

        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(Short.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(short.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
    }

    @Test
    public void testParseTimestampMapInteger() {
        TimestampIntegerMap expected = new TimestampIntegerMap();
        expected.put(1.0, 1);
        expected.put(3.0, 2);
        expected.put(5.0, 3);
        expected.put(6.0, 4);

        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(Integer.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(int.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
    }

    @Test
    public void testParseTimestampMapLong() {
        TimestampLongMap expected = new TimestampLongMap();
        expected.put(1.0, 1l);
        expected.put(3.0, 2l);
        expected.put(5.0, 3l);
        expected.put(6.0, 4l);

        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(Long.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(long.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
    }

    @Test
    public void testParseTimestampMapFloat() {
        TimestampFloatMap expected = new TimestampFloatMap();
        expected.put(1.0, 1f);
        expected.put(3.0, 2f);
        expected.put(5.0, 3f);
        expected.put(6.0, 4f);

        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(Float.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(float.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
    }

    @Test
    public void testParseTimestampMapDouble() {
        TimestampDoubleMap expected = new TimestampDoubleMap();
        expected.put(1.0, 1d);
        expected.put(3.0, 2d);
        expected.put(5.0, 3d);
        expected.put(6.0, 4d);

        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(Double.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(double.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]"));
    }

    @Test
    public void testParseTimestampMapBoolean() {
        TimestampBooleanMap expected = new TimestampBooleanMap();
        expected.put(1.0, true);
        expected.put(3.0, false);
        expected.put(5.0, false);
        expected.put(6.0, true);

        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(Boolean.class, "[1, true]; [3, false]; [5, '0']; [6, \"1\"]"));
        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(boolean.class, "[1, true]; [3, false]; [5, '0']; [6, \"1\"]"));
    }

    @Test
    public void testParseTimestampMapChar() {
        TimestampCharMap expected = new TimestampCharMap();
        expected.put(1.0, 'a');
        expected.put(3.0, 'b');
        expected.put(5.0, 'c');
        expected.put(6.0, 'd');

        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(Character.class, "[1, a]; [3, b]; [5, 'c']; [6, \"d\"]"));
        assertEqualTimestampMaps(expected, TimestampsParser.parseTimestampMap(char.class, "[1, a]; [3, b]; [5, 'c']; [6, \"d\"]"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampMapByteBadFormat() {
        TimestampsParser.parseTimestampMap(Byte.class, "[1, a]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampMapShortBadFormat() {
        TimestampsParser.parseTimestampMap(Short.class, "[1, a]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampMapIntegerBadFormat() {
        TimestampsParser.parseTimestampMap(Integer.class, "[1, a]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampMapLongBadFormat() {
        TimestampsParser.parseTimestampMap(Long.class, "[1, a]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampMapFloatBadFormat() {
        TimestampsParser.parseTimestampMap(Float.class, "[1, 1..4]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampMapDoubleBadFormat() {
        TimestampsParser.parseTimestampMap(Double.class, "[1, 4oe]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampMapCharBadFormat() {
        TimestampsParser.parseTimestampMap(Character.class, "[1, abc]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseTimestampUnsupportedType() {
        TimestampsParser.parseTimestampMap(Date.class, "[1, 1]; [3, 2]; [5, '3']; [6, \"4\"]");
    }

    private String parseTimestampSetToString(String str) {
        return TimestampsParser.parseTimestampSet(str).toString();
    }

    private String parseTimestampMapToString(String str, Class type) {
        return TimestampsParser.parseTimestampMap(type, str).toString();
    }

    private String parseTimestampMapToString(String str) {
        return parseTimestampMapToString(str, String.class);
    }

    @Test
    public void testParseComplexFormatTimestampsAndDefaultStringRepresentation() {
        assertEquals("<[2.0, \"; A3R; JJG; JJG\"]; [3.5, \"; A3R; JJG; [ ] () , JJG\"]; [10.0, 30]>", parseTimestampMapToString("[2.0 , \"; A3R; JJG; JJG\"); [3.5, \"; A3R; JJG; [ ] () , JJG\"]; [10,30]"));

        assertEquals("<[2.0, \";a b c\"]>", parseTimestampMapToString("<[' 2.0', ';a b c')"));

        assertEquals("<[2.0, \" d\"]>", parseTimestampMapToString("<[' 2.0', ' d')"));

        assertEquals("<[1.0, xy]; [4.0, \"['a;b']\"]>", parseTimestampMapToString(" (  1,  xy)  (4, '[\\'a;b\\']']"));

        assertEquals("<[1.0, xy]; [4.0, \"['a;b\\\"']\"]>", parseTimestampMapToString(" (  1,  xy)  (4, '[\\'a;b\\\"\\']']")// Playing
                                                                                                                            // with
                                                                                                                            // double
                                                                                                                            // quote
                                                                                                                            // literals
        );

        assertEquals("<[1.0, xy]; [4.0, \"[\\\"a;b'\\\"]\"]>", parseTimestampMapToString(" (  1,   xy)  (4, '[\"a;b\\\'\"]']")// Playing
                                                                                                                              // with
                                                                                                                              // single
                                                                                                                              // quote
                                                                                                                              // literals
        );

        assertEquals("<[1.25, <test>]>", parseTimestampMapToString("[1.25, <test>]"));
        assertEquals("<[1.55, <test>]>", parseTimestampMapToString("['1.55', '<test>']"));

        assertEquals("<[1.25, 21.12]>", parseTimestampMapToString("[1.25, \"21.12  \"  ]", Double.class));

        assertEquals("<[1.25, 0.0]>", parseTimestampMapToString("[1.25,,0]", Double.class));

        assertEquals("<[1.25, 1.55]>", parseTimestampSetToString("[1.25,1.55]"));

        assertEquals("<[1.25, 1.55]>", parseTimestampSetToString(" 1.25,1.55")// We
                                                                              // don't
                                                                              // require
                                                                              // bounds
                                                                              // for
                                                                              // timestamp
                                                                              // sets,
                                                                              // since
                                                                              // only
                                                                              // one
                                                                              // array
                                                                              // is
                                                                              // necessary
        );
    }

    @Test
    public void testParseInfinityTimestampsAndDefaultStringRepresentation() {
        assertEquals("<[-Infinity, 1.55]>", parseTimestampSetToString("[-Infinity,1.55]"));

        assertEquals("<[0.0, Infinity]>", parseTimestampSetToString("[0.0,Infinity]"));

        assertEquals("<[-Infinity, Infinity]>", parseTimestampSetToString("[-Infinity,Infinity]"));

        assertEquals("<[-Infinity, 0.0, 1.0, 2.0]>", parseTimestampSetToString("[-INFINITY, 0.0, 1.0, 2.0]"));

        assertEquals("<[-Infinity, 0.0, 1.0, 2.0, Infinity]>", parseTimestampSetToString("[-infinity, 0.0, 1.0, 2.0, infinity]"));

        assertEquals("<[-Infinity, 1.0E12]; [1.0, 2.0]; [Infinity, 3.0]>", parseTimestampMapToString("[-Infinity, 1e12]; [1.0, 2.]; [Infinity, 3]", Double.class));
    }

    @Test
    public void testParseUnordered() {
        assertEquals("<[-Infinity, -3.0, 0.0, 1.0]>", parseTimestampSetToString("[-Infinity, 0.0, -3.0, 1.0]"));

        assertEquals("<[-Infinity, 1.0]; [-3.0, 3.0]; [0.0, 2.0]; [1.0, 4.0]>", parseTimestampMapToString("[-Infinity, 1] [0.0, 2] [-3.0, 3] [1.0, 4]", Double.class));
    }
}
