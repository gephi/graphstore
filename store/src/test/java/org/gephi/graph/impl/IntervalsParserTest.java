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
import org.gephi.graph.api.Interval;
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
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Eduardo Ramos
 */
public class IntervalsParserTest {

    private IntervalSet buildIntervalSet(Interval... intervals) {
        IntervalSet set = new IntervalSet();
        for (Interval interval : intervals) {
            set.add(interval);
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
    public void testParseIntervalSet() throws ParseException {
        assertNull(IntervalsParser.parseIntervalSet(null));

        // Doubles:
        assertEquals(buildIntervalSet(new Interval(1, 2)), IntervalsParser.parseIntervalSet("[1, 2]"));
        assertEquals(buildIntervalSet(new Interval(1, 2), new Interval(2, 3)), IntervalsParser.parseIntervalSet("<[1, 2]; [2,3]>"));
        assertEquals(buildIntervalSet(new Interval(1, 2), new Interval(2, 31)), IntervalsParser.parseIntervalSet("<[1, 2]; [2,31.]>"));
        assertEquals(buildIntervalSet(new Interval(1, 2), new Interval(2, 31)), IntervalsParser.parseIntervalSet("<[1, 2]; [2,31.0)"));
        assertEquals(buildIntervalSet(new Interval(-5000, -1), new Interval(0, 0.5)), IntervalsParser.parseIntervalSet("(-5000,-1][0, .5)"));
        assertEquals(buildIntervalSet(new Interval(-5000, -1), new Interval(0, 0.5)), IntervalsParser.parseIntervalSet("(-5e3,-1)(0, .5)"));

        // Dates:
        assertEquals(buildIntervalSet(new Interval(parseDateIntoTimestamp("2015-01-01"),
                parseDateIntoTimestamp("2015-01-31"))), IntervalsParser.parseIntervalSet("[2015-01-01, 2015-01-31]"));
        assertEquals(buildIntervalSet(new Interval(parseDateIntoTimestamp("2015-01-01"),
                parseDateIntoTimestamp("2015-01-31"))), IntervalsParser.parseIntervalSet("[2015-01, 2015-01-31]"));

        // Date times:
        assertEquals(buildIntervalSet(new Interval(parseDateTimeIntoTimestamp("2015-01-01 21:12:05"),
                parseDateTimeIntoTimestamp("2015-01-02 00:00:00"))), IntervalsParser.parseIntervalSet("[2015-01-01T21:12:05, 2015-01-02]"));
        assertEquals(buildIntervalSet(new Interval(parseDateTimeMillisIntoTimestamp("2015-01-01 21:12:05.121"),
                parseDateTimeMillisIntoTimestamp("2015-01-02 00:00:01.999"))), IntervalsParser.parseIntervalSet("[2015-01-01T21:12:05.121, 2015-01-02T00:00:01.999]"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalSetBadEmpty1() {
        IntervalsParser.parseIntervalSet("[]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalSetBadEmpty2() {
        IntervalsParser.parseIntervalSet("[1]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalSetBadEmpty3() {
        IntervalsParser.parseIntervalSet("[1,]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalSetBadEmpty4() {
        IntervalsParser.parseIntervalSet("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalSetBadEmpty5() {
        IntervalsParser.parseIntervalSet("<>");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalSetBadDateFormat1() {
        IntervalsParser.parseIntervalSet("[2015-13-01, 2015-01-31]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalSetBadDateFormat2() {
        IntervalsParser.parseIntervalSet("[2015-01-35, 2015-01-31]");
    }

    private <T> void assertEqualIntervalMaps(IntervalMap<T> expected, IntervalMap<T> result) {
        assertEquals(expected, result);
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testParseIntervalsEmpty() {
        IntervalStringMap expectedMap = new IntervalStringMap();
        assertEqualIntervalMaps(expectedMap, IntervalsParser.parseIntervalMap(String.class, "<empty>"));
        assertEqualIntervalMaps(expectedMap, IntervalsParser.parseIntervalMap(String.class, "<EMPTY>"));

        IntervalSet expectedSet = new IntervalSet();
        assertEquals(expectedSet, IntervalsParser.parseIntervalSet("<empty>"));
        assertEquals(expectedSet, IntervalsParser.parseIntervalSet("<EMPTY>"));
    }

    @Test
    public void testParseIntervalMapString() {
        IntervalStringMap expected = new IntervalStringMap();
        expected.put(new Interval(1, 2), "Value1");
        expected.put(new Interval(3, 5), "Value2");
        expected.put(new Interval(5, 6), "Value 3");
        expected.put(new Interval(6, 7), " Value 4 ");

        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(String.class, "[1, 2, Value1]; [3, 5, 'Value2']; [5, 6, Value 3]; [6, 7, \" Value 4 \"]"));
    }

    @Test
    public void testParseIntervalMapByte() {
        IntervalByteMap expected = new IntervalByteMap();
        expected.put(new Interval(1, 2), (byte) 1);
        expected.put(new Interval(3, 5), (byte) 2);
        expected.put(new Interval(5, 6), (byte) 3);
        expected.put(new Interval(6, 7), (byte) 4);

        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(Byte.class, "[1, 2, 1]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));
        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(byte.class, "[1, 2, 1]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));
    }

    @Test
    public void testParseIntervalMapShort() {
        IntervalShortMap expected = new IntervalShortMap();
        expected.put(new Interval(1, 2), (short) 1);
        expected.put(new Interval(3, 5), (short) 2);
        expected.put(new Interval(5, 6), (short) 3);
        expected.put(new Interval(6, 7), (short) 4);

        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(Short.class, "[1, 2, 1.1]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));// Decimals
                                                                                                                                               // are
                                                                                                                                               // ignored
        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(short.class, "[1, 2, 1.1]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));// Decimals
                                                                                                                                               // are
                                                                                                                                               // ignored
    }

    @Test
    public void testParseIntervalMapInteger() {
        IntervalIntegerMap expected = new IntervalIntegerMap();
        expected.put(new Interval(1, 2), 1);
        expected.put(new Interval(3, 5), 2);
        expected.put(new Interval(5, 6), 3);
        expected.put(new Interval(6, 7), 4);

        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(Integer.class, "[1, 2, 1.]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));// Decimals
                                                                                                                                                // are
                                                                                                                                                // ignored
        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(int.class, "[1, 2, 1.]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));// Decimals
                                                                                                                                            // are
                                                                                                                                            // ignored
    }

    @Test
    public void testParseIntervalMapLong() {
        IntervalLongMap expected = new IntervalLongMap();
        expected.put(new Interval(1, 2), 1l);
        expected.put(new Interval(3, 5), 2l);
        expected.put(new Interval(5, 6), 3l);
        expected.put(new Interval(6, 7), 4l);

        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(Long.class, "[1, 2, 1.0]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));// Decimals
                                                                                                                                              // are
                                                                                                                                              // ignored
        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(long.class, "[1, 2, 1.0]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));// Decimals
                                                                                                                                              // are
                                                                                                                                              // ignored
    }

    @Test
    public void testParseIntervalMapFloat() {
        IntervalFloatMap expected = new IntervalFloatMap();
        expected.put(new Interval(1, 2), 1f);
        expected.put(new Interval(3, 5), 2f);
        expected.put(new Interval(5, 6), 3f);
        expected.put(new Interval(6, 7), 4f);

        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(Float.class, "[1, 2, 1]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));
        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(float.class, "[1, 2, 1]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));
    }

    @Test
    public void testParseIntervalMapDouble() {
        IntervalDoubleMap expected = new IntervalDoubleMap();
        expected.put(new Interval(1, 2), 1d);
        expected.put(new Interval(3, 5), 2d);
        expected.put(new Interval(5, 6), 3d);
        expected.put(new Interval(6, 7), 4d);

        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(Double.class, "[1, 2, 1]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));
        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(double.class, "[1, 2, 1]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]"));
    }

    @Test
    public void testParseIntervalMapBoolean() {
        IntervalBooleanMap expected = new IntervalBooleanMap();
        expected.put(new Interval(1, 2), true);
        expected.put(new Interval(3, 5), false);
        expected.put(new Interval(5, 6), false);
        expected.put(new Interval(6, 7), true);

        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(Boolean.class, "[1, 2, true]; [3, 5, false]; [5, 6, '0']; [6, 7, \"1\"]"));
        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(boolean.class, "[1, 2, true]; [3, 5, false]; [5, 6, 0]; [6, 7, 1]"));
    }

    @Test
    public void testParseIntervalMapChar() {
        IntervalCharMap expected = new IntervalCharMap();
        expected.put(new Interval(1, 2), 'a');
        expected.put(new Interval(3, 5), 'b');
        expected.put(new Interval(5, 6), 'c');
        expected.put(new Interval(6, 7), 'd');

        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(Character.class, "[1, 2, a]; [3, 5, b]; [5, 6, 'c']; [6, 7, \"d\"]"));
        assertEqualIntervalMaps(expected, IntervalsParser.parseIntervalMap(char.class, "[1, 2, a]; [3, 5, b]; [5, 6, 'c']; [6, 7, \"d\"]"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalMapByteBadFormat() {
        IntervalsParser.parseIntervalMap(Byte.class, "[1, 2, a]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalMapShortBadFormat() {
        IntervalsParser.parseIntervalMap(Short.class, "[1, 2, a]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalMapIntegerBadFormat() {
        IntervalsParser.parseIntervalMap(Integer.class, "[1, 2, a]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalMapLongBadFormat() {
        IntervalsParser.parseIntervalMap(Long.class, "[1, 2, a]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalMapFloatBadFormat() {
        IntervalsParser.parseIntervalMap(Float.class, "[1, 2, 1..4]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalMapDoubleBadFormat() {
        IntervalsParser.parseIntervalMap(Double.class, "[1, 2, 4oe]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalMapCharBadFormat() {
        IntervalsParser.parseIntervalMap(Character.class, "[1, 2, abc]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseIntervalUnsupportedType() {
        IntervalsParser.parseIntervalMap(Date.class, "[1, 2, 1]; [3, 5, 2]; [5, 6, '3']; [6, 7, \"4\"]");
    }

    private String parseIntervalSetToString(String str) {
        return IntervalsParser.parseIntervalSet(str).toString();
    }

    private String parseIntervalMapToString(String str, Class type) {
        return IntervalsParser.parseIntervalMap(type, str).toString();
    }

    private String parseIntervalMapToString(String str) {
        return parseIntervalMapToString(str, String.class);
    }

    @Test
    public void testParseComplexFormatIntervalsAndDefaultStringRepresentation() {
        assertEquals("<[2.0, 3.5, \"; A3R; JJG; JJG\"]; [3.5, 8.0, \"; A3R; JJG; [ ] () , JJG\"]; [10.0, 20.0, 30]>", parseIntervalMapToString("[2.0, 3.5, \"; A3R; JJG; JJG\"); [3.5, 8.0, \"; A3R; JJG; [ ] () , JJG\"]; [10,20,30]"));

        assertEquals("<[2.0, 3.5, \";a b c\"]>", parseIntervalMapToString("<[' 2.0', '3.5', ';a b c')"));

        assertEquals("<[1.0, 2.0, xy]; [4.0, 5.0, \"['a;b']\"]>", parseIntervalMapToString(" (  1, 2,  xy)  (4,5, '[\\'a;b\\']']"));

        assertEquals("<[1.0, 2.0, xy]; [4.0, 5.0, \"['a;b\\\"']\"]>", parseIntervalMapToString(" (  1, 2,  xy)  (4,5, '[\\'a;b\\\"\\']']")// Playing
                                                                                                                                          // with
                                                                                                                                          // double
                                                                                                                                          // quote
                                                                                                                                          // literals
        );

        assertEquals("<[1.0, 2.0, xy]; [4.0, 5.0, \"[\\\"a;b'\\\"]\"]>", parseIntervalMapToString(" (  1, 2,  xy)  (4,5, '[\"a;b\\\'\"]']")// Playing
                                                                                                                                           // with
                                                                                                                                           // single
                                                                                                                                           // quote
                                                                                                                                           // literals
        );

        assertEquals("<[1.25, 1.55, <test>]>", parseIntervalMapToString("[1.25,1.55, <test>]"));
        assertEquals("<[1.25, 1.55, <test>]>", parseIntervalMapToString("[1.25,'1.55' '<test>']"));

        assertEquals("<[1.25, 1.55, 21.12]>", parseIntervalMapToString("[1.25,1.55,   \"21.12  \"  ]", Double.class));

        assertEquals("<[1.25, 1.55, 0.0]>", parseIntervalMapToString("[1.25,1.55,0]", Double.class));

        assertEquals("<[1.25, 1.55]>", parseIntervalSetToString("[1.25,1.55]"));
    }

    @Test
    public void testParseInfinityIntervalsAndDefaultStringRepresentation() {
        assertEquals("<[-Infinity, 1.55]>", parseIntervalSetToString("[-Infinity,1.55]"));

        assertEquals("<[0.0, Infinity]>", parseIntervalSetToString("[0.0,Infinity]"));

        assertEquals("<[-Infinity, Infinity]>", parseIntervalSetToString("[-Infinity,Infinity]"));

        assertEquals("<[-Infinity, 0.0]; [1.0, 2.0]>", parseIntervalSetToString("[-infinity, 0.0]; [1.0, 2.0]"));

        assertEquals("<[-Infinity, 0.0]; [1.0, 2.0]; [2.0, Infinity]>", parseIntervalSetToString("[-infinity, 0.0]; [1.0, 2.0]; [2.0, infinity]"));

        assertEquals("<[-Infinity, 0.0, 1.0E12]; [1.0, 2.0, 2.0]; [2.0, Infinity, 3.0]>", parseIntervalMapToString("[-Infinity, 0.0, 1e12]; [1.0, 2.0, 2.]; [2.0, Infinity, 3]", Double.class));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseInfinityBadIntervalsOverlapping() {
        IntervalsParser.parseIntervalSet("[-Infinity, 0.0]; [-3.0, 1.0]");// Overlapping
    }
}
