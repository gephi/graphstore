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

package org.gephi.graph.api;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanOpenHashSet;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.graph.api.types.TimestampBooleanMap;
import org.gephi.graph.api.types.TimestampByteMap;
import org.gephi.graph.api.types.TimestampCharMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampFloatMap;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.graph.api.types.TimestampLongMap;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.graph.api.types.TimestampShortMap;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.graph.impl.ArraysParser;
import org.gephi.graph.impl.FormattingAndParsingUtils;
import org.gephi.graph.impl.GraphStoreConfiguration;
import org.gephi.graph.impl.IntervalsParser;
import org.gephi.graph.impl.TimestampsParser;

/**
 * Set of utility methods to manipulate supported attribute types.
 * <p>
 * The attribute system is built with a set of supported column types. This
 * class contains utilities to parse and convert supported types. It also
 * contains utilities to manipulate primitive arrays (the preferred array type)
 * and date/time types. Default time zone for parsing/printing dates is UTC.
 */
public class AttributeUtils {

    private static final Set<Class> SUPPORTED_TYPES;
    private static final Map<Class, Class> TYPES_STANDARDIZATION;
    private static final DateTimeFormatter DATE_TIME_PARSER;
    private static final DateTimeFormatter DATE_PRINTER;
    private static final DateTimeFormatter DATE_TIME_PRINTER;
    private static final DecimalFormat TIMESTAMP_PRINTER;

    // These are used to avoid creating a lot of new instances of
    // DateTimeFormatter
    private static final Map<ZoneId, DateTimeFormatter> DATE_PRINTERS_BY_TIMEZONE;
    private static final Map<ZoneId, DateTimeFormatter> DATE_TIME_PRINTERS_BY_TIMEZONE;
    private static final Map<ZoneId, DateTimeFormatter> DATE_TIME_PARSERS_BY_TIMEZONE;

    // Collectio types to speedup lookup
    private static final Set<Class> TYPED_LIST_TYPES;
    private static final Set<Class> TYPED_SET_TYPES;
    private static final Set<Class> TYPED_MAP_TYPES;

    static {
        final Set<Class> supportedTypes = new HashSet<>();

        // Primitives
        supportedTypes.add(Boolean.class);
        supportedTypes.add(boolean.class);
        supportedTypes.add(Integer.class);
        supportedTypes.add(int.class);
        supportedTypes.add(Short.class);
        supportedTypes.add(short.class);
        supportedTypes.add(Long.class);
        supportedTypes.add(long.class);
        supportedTypes.add(BigInteger.class);
        supportedTypes.add(Byte.class);
        supportedTypes.add(byte.class);
        supportedTypes.add(Float.class);
        supportedTypes.add(float.class);
        supportedTypes.add(Double.class);
        supportedTypes.add(double.class);
        supportedTypes.add(BigDecimal.class);
        supportedTypes.add(Character.class);
        supportedTypes.add(char.class);

        // Objects
        supportedTypes.add(String.class);

        // Primitives Array
        supportedTypes.add(Boolean[].class);
        supportedTypes.add(boolean[].class);
        supportedTypes.add(Integer[].class);
        supportedTypes.add(int[].class);
        supportedTypes.add(Short[].class);
        supportedTypes.add(short[].class);
        supportedTypes.add(Long[].class);
        supportedTypes.add(long[].class);
        supportedTypes.add(BigInteger[].class);
        supportedTypes.add(Byte[].class);
        supportedTypes.add(byte[].class);
        supportedTypes.add(Float[].class);
        supportedTypes.add(float[].class);
        supportedTypes.add(Double[].class);
        supportedTypes.add(double[].class);
        supportedTypes.add(BigDecimal[].class);
        supportedTypes.add(Character[].class);
        supportedTypes.add(char[].class);

        // Objects array
        supportedTypes.add(String[].class);

        // Dynamic (timestamps)
        supportedTypes.add(TimestampSet.class);
        supportedTypes.add(TimestampBooleanMap.class);
        supportedTypes.add(TimestampIntegerMap.class);
        supportedTypes.add(TimestampShortMap.class);
        supportedTypes.add(TimestampLongMap.class);
        supportedTypes.add(TimestampByteMap.class);
        supportedTypes.add(TimestampFloatMap.class);
        supportedTypes.add(TimestampDoubleMap.class);
        supportedTypes.add(TimestampCharMap.class);
        supportedTypes.add(TimestampStringMap.class);

        // Dynamic (intervals)
        supportedTypes.add(IntervalSet.class);
        supportedTypes.add(IntervalBooleanMap.class);
        supportedTypes.add(IntervalIntegerMap.class);
        supportedTypes.add(IntervalShortMap.class);
        supportedTypes.add(IntervalLongMap.class);
        supportedTypes.add(IntervalByteMap.class);
        supportedTypes.add(IntervalFloatMap.class);
        supportedTypes.add(IntervalDoubleMap.class);
        supportedTypes.add(IntervalCharMap.class);
        supportedTypes.add(IntervalStringMap.class);

        // Lists, Maps, Sets
        supportedTypes.add(List.class);
        supportedTypes.add(Set.class);
        supportedTypes.add(Map.class);

        // Assign
        SUPPORTED_TYPES = Collections.unmodifiableSet(supportedTypes);

        // Primitive types standardization
        final Map<Class, Class> typesStandardization = new HashMap<>();
        typesStandardization.put(boolean.class, Boolean.class);
        typesStandardization.put(int.class, Integer.class);
        typesStandardization.put(short.class, Short.class);
        typesStandardization.put(long.class, Long.class);
        typesStandardization.put(byte.class, Byte.class);
        typesStandardization.put(float.class, Float.class);
        typesStandardization.put(double.class, Double.class);
        typesStandardization.put(char.class, Character.class);

        // Array standardization
        typesStandardization.put(Boolean[].class, boolean[].class);
        typesStandardization.put(Integer[].class, int[].class);
        typesStandardization.put(Short[].class, short[].class);
        typesStandardization.put(Long[].class, long[].class);
        typesStandardization.put(Byte[].class, byte[].class);
        typesStandardization.put(Float[].class, float[].class);
        typesStandardization.put(Double[].class, double[].class);
        typesStandardization.put(Character[].class, char[].class);

        // Assign
        TYPES_STANDARDIZATION = Collections.unmodifiableMap(typesStandardization);

        // Datetime - make sure UTC timezone is used by default
        DATE_TIME_PARSER = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .appendOptional(DateTimeFormatter.ISO_DATE).appendOptional(DateTimeFormatter.ofPattern("yyyyMMdd"))
                .optionalStart().appendLiteral('T').append(DateTimeFormatter.ISO_TIME)
                .appendPattern("[.SSSSSSSSS][.SSSSSS][.SSS]").optionalEnd().optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 9, 9, true).optionalEnd()
                // optional nanos with 6 digits (including decimal point)
                .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true).optionalEnd()
                // optional nanos with 3 digits (including decimal point)
                .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 3, 3, true).optionalEnd()
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                .toFormatter().withZone(GraphStoreConfiguration.DEFAULT_TIME_ZONE);
        DATE_PRINTER = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("yyyy-MM-dd").toFormatter()
                .withZone(GraphStoreConfiguration.DEFAULT_TIME_ZONE);
        DATE_TIME_PRINTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('T').appendPattern("HH:mm:ss")
                .appendPattern(".SSS").parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 0).appendOffset("+HH:MM", "Z").toFormatter()
                .withZone(GraphStoreConfiguration.DEFAULT_TIME_ZONE);

        DATE_PRINTERS_BY_TIMEZONE = new HashMap<>();
        DATE_TIME_PRINTERS_BY_TIMEZONE = new HashMap<>();
        DATE_TIME_PARSERS_BY_TIMEZONE = new HashMap<>();

        DATE_PRINTERS_BY_TIMEZONE.put(DATE_PRINTER.getZone(), DATE_PRINTER);
        DATE_TIME_PRINTERS_BY_TIMEZONE.put(DATE_TIME_PRINTER.getZone(), DATE_TIME_PRINTER);
        DATE_TIME_PARSERS_BY_TIMEZONE.put(DATE_TIME_PARSER.getZone(), DATE_TIME_PARSER);

        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        decimalFormatSymbols.setInfinity(FormattingAndParsingUtils.INFINITY);
        // 1 to 4 decimals
        TIMESTAMP_PRINTER = new DecimalFormat("0.0###", decimalFormatSymbols);

        // List types
        TYPED_LIST_TYPES = new HashSet<>();
        TYPED_LIST_TYPES.add(IntArrayList.class);
        TYPED_LIST_TYPES.add(FloatArrayList.class);
        TYPED_LIST_TYPES.add(DoubleArrayList.class);
        TYPED_LIST_TYPES.add(ShortArrayList.class);
        TYPED_LIST_TYPES.add(LongArrayList.class);
        TYPED_LIST_TYPES.add(ByteArrayList.class);
        TYPED_LIST_TYPES.add(BooleanArrayList.class);
        TYPED_LIST_TYPES.add(CharArrayList.class);

        // Set types
        TYPED_SET_TYPES = new HashSet<>();
        TYPED_SET_TYPES.add(IntOpenHashSet.class);
        TYPED_SET_TYPES.add(FloatOpenHashSet.class);
        TYPED_SET_TYPES.add(DoubleOpenHashSet.class);
        TYPED_SET_TYPES.add(ShortOpenHashSet.class);
        TYPED_SET_TYPES.add(LongOpenHashSet.class);
        TYPED_SET_TYPES.add(ByteOpenHashSet.class);
        TYPED_SET_TYPES.add(BooleanOpenHashSet.class);
        TYPED_SET_TYPES.add(CharOpenHashSet.class);

        // Map types
        TYPED_MAP_TYPES = new HashSet<>();
        TYPED_MAP_TYPES.add(Int2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Float2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Double2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Short2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Long2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Byte2ObjectOpenHashMap.class);
        TYPED_MAP_TYPES.add(Char2ObjectOpenHashMap.class);
    }

    private AttributeUtils() {
        // Only static methods
    }

    private static DateTimeFormatter getDateTimeFormatterByTimeZone(Map<ZoneId, DateTimeFormatter> cache, DateTimeFormatter baseFormatter, ZoneId zoneId) {
        if (zoneId == null) {
            return baseFormatter;
        }

        DateTimeFormatter formatter = cache.get(zoneId);
        if (formatter == null) {
            formatter = baseFormatter.withZone(zoneId);
            cache.put(zoneId, formatter);
        }

        return formatter;
    }

    private static DateTimeFormatter getDateTimeParserByTimeZone(ZoneId zoneId) {
        return getDateTimeFormatterByTimeZone(DATE_TIME_PARSERS_BY_TIMEZONE, DATE_TIME_PARSER, zoneId);
    }

    private static DateTimeFormatter getDateTimePrinterByTimeZone(ZoneId zoneId) {
        return getDateTimeFormatterByTimeZone(DATE_TIME_PRINTERS_BY_TIMEZONE, DATE_TIME_PRINTER, zoneId);
    }

    private static DateTimeFormatter getDatePrinterByTimeZone(ZoneId zoneId) {
        return getDateTimeFormatterByTimeZone(DATE_PRINTERS_BY_TIMEZONE, DATE_PRINTER, zoneId);
    }

    /**
     * Returns the string representation of the given value.
     *
     * @param value value
     * @return string representation
     */
    public static String print(Object value) {
        return print(value, TimeFormat.DOUBLE, null);
    }

    /**
     * Returns the string representation of the given value.
     *
     * @param value value
     * @param timeFormat time format
     * @param zoneId time zone
     * @return string representation
     */
    public static String print(Object value, TimeFormat timeFormat, ZoneId zoneId) {
        if (value == null) {
            return "null";
        }
        if (value instanceof TimeSet) {
            return ((TimeSet) value).toString(timeFormat, zoneId);
        }
        if (value instanceof TimeMap) {
            return ((TimeMap) value).toString(timeFormat, zoneId);
        }
        if (value.getClass().isArray()) {
            return printArray(value);
        }
        return value.toString();
    }

    /**
     * Parses the given string using the type class provided and returns an
     * instance.
     *
     * @param str string to parse
     * @param typeClass class of the desired type
     * @param zoneId time zone to use or null to use default time zone (UTC), for
     *        dynamic types only
     * @return an instance of the type class, or null if <em>str</em> is null or
     *         empty
     */
    public static Object parse(String str, Class typeClass, ZoneId zoneId) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        if (str.equalsIgnoreCase("null")) {
            return null;
        }

        if (typeClass.isPrimitive()) {
            typeClass = getStandardizedType(typeClass);// For primitives we can
                                                       // use auto-unboxing
        }

        // Simple and primitive types:
        if (typeClass.equals(String.class)) {
            return str;
        } else if (typeClass.equals(Byte.class)) {
            return Byte.valueOf(str);
        } else if (typeClass.equals(Short.class)) {
            return Short.valueOf(str);
        } else if (typeClass.equals(Integer.class)) {
            return Integer.valueOf(str);
        } else if (typeClass.equals(Long.class)) {
            return Long.valueOf(str);
        } else if (typeClass.equals(Float.class)) {
            return Float.valueOf(str);
        } else if (typeClass.equals(Double.class)) {
            return Double.valueOf(str);
        } else if (typeClass.equals(BigInteger.class)) {
            return new BigInteger(str);
        } else if (typeClass.equals(BigDecimal.class)) {
            return new BigDecimal(str);
        } else if (typeClass.equals(Boolean.class)) {
            if (str.length() == 1) {
                if (str.charAt(0) == '1') {
                    return Boolean.TRUE;
                } else if (str.charAt(0) == '0') {
                    return Boolean.FALSE;
                }
            }
            return Boolean.valueOf(str);
        } else if (typeClass.equals(Character.class)) {
            if (str.length() > 1) {
                throw new IllegalArgumentException("The string has a length > 1");
            }
            return str.charAt(0);
        }

        // Interval types:
        if (typeClass.equals(IntervalSet.class)) {
            return IntervalsParser.parseIntervalSet(str, zoneId);
        } else if (typeClass.equals(IntervalStringMap.class)) {
            return IntervalsParser.parseIntervalMap(String.class, str, zoneId);
        } else if (typeClass.equals(IntervalByteMap.class)) {
            return IntervalsParser.parseIntervalMap(Byte.class, str, zoneId);
        } else if (typeClass.equals(IntervalShortMap.class)) {
            return IntervalsParser.parseIntervalMap(Short.class, str, zoneId);
        } else if (typeClass.equals(IntervalIntegerMap.class)) {
            return IntervalsParser.parseIntervalMap(Integer.class, str, zoneId);
        } else if (typeClass.equals(IntervalLongMap.class)) {
            return IntervalsParser.parseIntervalMap(Long.class, str, zoneId);
        } else if (typeClass.equals(IntervalFloatMap.class)) {
            return IntervalsParser.parseIntervalMap(Float.class, str, zoneId);
        } else if (typeClass.equals(IntervalDoubleMap.class)) {
            return IntervalsParser.parseIntervalMap(Double.class, str, zoneId);
        } else if (typeClass.equals(IntervalBooleanMap.class)) {
            return IntervalsParser.parseIntervalMap(Boolean.class, str, zoneId);
        } else if (typeClass.equals(IntervalCharMap.class)) {
            return IntervalsParser.parseIntervalMap(Character.class, str, zoneId);
        }

        // Timestamp types:
        if (typeClass.equals(TimestampSet.class)) {
            return TimestampsParser.parseTimestampSet(str, zoneId);
        } else if (typeClass.equals(TimestampStringMap.class)) {
            return TimestampsParser.parseTimestampMap(String.class, str, zoneId);
        } else if (typeClass.equals(TimestampByteMap.class)) {
            return TimestampsParser.parseTimestampMap(Byte.class, str, zoneId);
        } else if (typeClass.equals(TimestampShortMap.class)) {
            return TimestampsParser.parseTimestampMap(Short.class, str, zoneId);
        } else if (typeClass.equals(TimestampIntegerMap.class)) {
            return TimestampsParser.parseTimestampMap(Integer.class, str, zoneId);
        } else if (typeClass.equals(TimestampLongMap.class)) {
            return TimestampsParser.parseTimestampMap(Long.class, str, zoneId);
        } else if (typeClass.equals(TimestampFloatMap.class)) {
            return TimestampsParser.parseTimestampMap(Float.class, str, zoneId);
        } else if (typeClass.equals(TimestampDoubleMap.class)) {
            return TimestampsParser.parseTimestampMap(Double.class, str, zoneId);
        } else if (typeClass.equals(TimestampBooleanMap.class)) {
            return TimestampsParser.parseTimestampMap(Boolean.class, str, zoneId);
        } else if (typeClass.equals(TimestampCharMap.class)) {
            return TimestampsParser.parseTimestampMap(Character.class, str, zoneId);
        }

        // Array types:
        if (typeClass.equals(boolean[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Boolean[].class, str);
        } else if (typeClass.equals(char[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Character[].class, str);
        } else if (typeClass.equals(byte[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Byte[].class, str);
        } else if (typeClass.equals(short[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Short[].class, str);
        } else if (typeClass.equals(int[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Integer[].class, str);
        } else if (typeClass.equals(long[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Long[].class, str);
        } else if (typeClass.equals(float[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Float[].class, str);
        } else if (typeClass.equals(double[].class)) {
            return ArraysParser.parseArrayAsPrimitiveArray(Double[].class, str);
        } else if (typeClass.equals(Boolean[].class) || typeClass.equals(String[].class) || typeClass
                .equals(Character[].class) || typeClass.equals(Byte[].class) || typeClass
                        .equals(Short[].class) || typeClass.equals(Integer[].class) || typeClass
                                .equals(Long[].class) || typeClass.equals(Float[].class) || typeClass
                                        .equals(Double[].class) || typeClass
                                                .equals(BigInteger[].class) || typeClass.equals(BigDecimal[].class)) {
            return ArraysParser.parseArray(typeClass, str);
        }

        throw new IllegalArgumentException("Unsupported type " + typeClass.getCanonicalName());
    }

    /**
     * Parses the given string using the type class provided and returns an
     * instance.
     *
     * Default time zone is used (UTC) for dynamic types (timestamps/intervals).
     *
     * @param str string to parse
     * @param typeClass class of the desired type
     * @return an instance of the type class, or null if <em>str</em> is null or
     *         empty
     */
    public static Object parse(String str, Class typeClass) {
        return parse(str, typeClass, null);
    }

    /**
     * Returns the primitive type for the given wrapped primitive.
     * <p>
     * Example: Returns <em>int.class</em> given <em>Integer.class</em>
     *
     * @param type type to get the primitive type from
     * @return primitive type
     */
    public static Class getPrimitiveType(Class type) {
        if (!type.isPrimitive()) {
            if (type.equals(Boolean.class)) {
                return boolean.class;
            } else if (type.equals(Integer.class)) {
                return int.class;
            } else if (type.equals(Short.class)) {
                return short.class;
            } else if (type.equals(Long.class)) {
                return long.class;
            } else if (type.equals(Byte.class)) {
                return byte.class;
            } else if (type.equals(Float.class)) {
                return float.class;
            } else if (type.equals(Double.class)) {
                return double.class;
            } else if (type.equals(Character.class)) {
                return char.class;
            }
        }
        throw new IllegalArgumentException("The type should be a wrapped primitive");
    }

    /**
     * Returns the primitive array given a wrapped primitive array.
     * <p>
     * Example: Returns <em>int[]</em> array given an <em>Integer[]</em> array
     *
     * @param array wrapped primitive array instance
     * @return primitive array instance
     * @throws IllegalArgumentException Thrown if any of the array values is null
     */
    public static Object getPrimitiveArray(Object[] array) {
        if (!isSupported(array.getClass())) {
            throw new IllegalArgumentException("Unsupported type " + array.getClass().getCanonicalName());
        }
        Class arrayClass = array.getClass().getComponentType();
        if (!arrayClass
                .isPrimitive() && (arrayClass == Double.class || arrayClass == Float.class || arrayClass == Long.class || arrayClass == Integer.class || arrayClass == Short.class || arrayClass == Character.class || arrayClass == Byte.class || arrayClass == Boolean.class)) {
            Class primitiveClass = getPrimitiveType(arrayClass);

            int arrayLength = array.length;
            Object primitiveArray = Array.newInstance(primitiveClass, arrayLength);

            for (int i = 0; i < arrayLength; i++) {
                Object obj = array[i];
                Array.set(primitiveArray, i, obj);
            }
            return primitiveArray;
        }
        return array;
    }

    /**
     * Returns the set of types supported.
     *
     * @return set of supported types
     */
    public static Set<Class> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    /**
     * Returns true if <em>type</em> is a supported type.
     *
     * @param type type to test support
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(Class type) {
        if (type == null) {
            throw new NullPointerException();
        }
        return SUPPORTED_TYPES.contains(type) || isCollectionType(type) || isMapType(type);
    }

    /**
     * Returns the standardized type for the given type class.
     * <p>
     * For instance, <code>getStandardizedType(int.class)</code> would return
     * <code>Integer.class</code>.
     *
     * @param type type to standardize
     * @return standardized type
     */
    public static Class getStandardizedType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        Class t = TYPES_STANDARDIZATION.get(type);
        if (t != null) {
            return t;
        }
        return type;
    }

    /**
     * Returns true if <em>type</em> is a standardized type.
     * <p>
     * Non standardized types are transformed into standardized types using
     * {@link #getStandardizedType(java.lang.Class) }.
     *
     * @param type the type to test
     * @return true if <em>type</em> is standardized, false otherwise
     */
    public static boolean isStandardizedType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        return TYPES_STANDARDIZATION.get(type) == null;
    }

    /**
     * Returns the dynamic timestamp map value type for the given type.
     *
     * @param type static type
     * @return timestamp map type
     */
    public static Class<? extends TimestampMap> getTimestampMapType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        type = getStandardizedType(type);
        if (type.equals(Boolean.class)) {
            return TimestampBooleanMap.class;
        } else if (type.equals(Integer.class)) {
            return TimestampIntegerMap.class;
        } else if (type.equals(Short.class)) {
            return TimestampShortMap.class;
        } else if (type.equals(Long.class)) {
            return TimestampLongMap.class;
        } else if (type.equals(Byte.class)) {
            return TimestampByteMap.class;
        } else if (type.equals(Float.class)) {
            return TimestampFloatMap.class;
        } else if (type.equals(Double.class)) {
            return TimestampDoubleMap.class;
        } else if (type.equals(Character.class)) {
            return TimestampCharMap.class;
        } else if (type.equals(String.class)) {
            return TimestampStringMap.class;
        }
        throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
    }

    /**
     * Returns the dynamic timestamp map value type for the given type.
     *
     * @param type static type
     * @return timestamp map type
     */
    public static Class<? extends IntervalMap> getIntervalMapType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        type = getStandardizedType(type);
        if (type.equals(Boolean.class)) {
            return IntervalBooleanMap.class;
        } else if (type.equals(Integer.class)) {
            return IntervalIntegerMap.class;
        } else if (type.equals(Short.class)) {
            return IntervalShortMap.class;
        } else if (type.equals(Long.class)) {
            return IntervalLongMap.class;
        } else if (type.equals(Byte.class)) {
            return IntervalByteMap.class;
        } else if (type.equals(Float.class)) {
            return IntervalFloatMap.class;
        } else if (type.equals(Double.class)) {
            return IntervalDoubleMap.class;
        } else if (type.equals(Character.class)) {
            return IntervalCharMap.class;
        } else if (type.equals(String.class)) {
            return IntervalStringMap.class;
        }
        throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
    }

    /**
     * Returns the static type for the given time map type.
     *
     * @param type time map type
     * @return static type
     */
    public static Class getStaticType(Class<? extends TimeMap> type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }

        if (type.equals(TimestampBooleanMap.class) || type.equals(IntervalBooleanMap.class)) {
            return Boolean.class;
        } else if (type.equals(TimestampIntegerMap.class) || type.equals(IntervalIntegerMap.class)) {
            return Integer.class;
        } else if (type.equals(TimestampShortMap.class) || type.equals(IntervalShortMap.class)) {
            return Short.class;
        } else if (type.equals(TimestampLongMap.class) || type.equals(IntervalLongMap.class)) {
            return Long.class;
        } else if (type.equals(TimestampByteMap.class) || type.equals(IntervalByteMap.class)) {
            return Byte.class;
        } else if (type.equals(TimestampFloatMap.class) || type.equals(IntervalFloatMap.class)) {
            return Float.class;
        } else if (type.equals(TimestampDoubleMap.class) || type.equals(IntervalDoubleMap.class)) {
            return Double.class;
        } else if (type.equals(TimestampCharMap.class) || type.equals(IntervalCharMap.class)) {
            return Character.class;
        } else if (type.equals(TimestampStringMap.class) || type.equals(IntervalStringMap.class)) {
            return String.class;
        }
        throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
    }

    /**
     * Transform the given <em>value</em> instance in a standardized type if
     * necessary.
     * <p>
     * This function transforms wrapped primitive arrays in primitive arrays.
     *
     * @param value value to standardize
     * @return standardized value, or <em>value</em> if already standardized
     */
    public static Object standardizeValue(Object value) {
        if (value == null) {
            return null;
        }
        Class type = value.getClass();
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        if (type.isArray() && !type.getComponentType().isPrimitive()) {
            return getPrimitiveArray((Object[]) value);
        }
        if (List.class.isAssignableFrom(type)) {
            return getStandardizedList((List) value);
        } else if (Set.class.isAssignableFrom(type)) {
            return getStandardizedSet((Set) value);
        } else if (Map.class.isAssignableFrom(type)) {
            return getStandardizedMap((Map) value);
        }
        return value;
    }

    private static List getStandardizedList(List list) {
        Class listClass = list.getClass();
        if (TYPED_LIST_TYPES.contains(listClass)) {
            return list;
        }

        Class oCls = null;
        for (Object o : list) {
            if (o != null) {
                if (oCls == null) {
                    oCls = o.getClass();
                } else if (!o.getClass().equals(oCls)) {
                    throw new IllegalArgumentException("The list contains mixed classes");
                }
            }
        }
        if (oCls != null && !(isSimpleType(oCls) || isArrayType(oCls))) {
            throw new IllegalArgumentException(
                    "The list contains unsupported type " + oCls.getClass().getCanonicalName());
        }
        if (oCls != null) {
            if (oCls.equals(Integer.class)) {
                return new IntArrayList(list);
            } else if (oCls.equals(Float.class)) {
                return new FloatArrayList(list);
            } else if (oCls.equals(Double.class)) {
                return new DoubleArrayList(list);
            } else if (oCls.equals(Short.class)) {
                return new ShortArrayList(list);
            } else if (oCls.equals(Byte.class)) {
                return new ByteArrayList(list);
            } else if (oCls.equals(Long.class)) {
                return new LongArrayList(list);
            } else if (oCls.equals(Boolean.class)) {
                return new BooleanArrayList(list);
            } else if (oCls.equals(Character.class)) {
                return new CharArrayList(list);
            }
        }
        List result = new ObjectArrayList(list.size());
        for (Object o : list) {
            result.add(standardizeValue(o));
        }
        return result;
    }

    private static Set getStandardizedSet(Set set) {
        Class listClass = set.getClass();
        if (TYPED_LIST_TYPES.contains(listClass)) {
            return set;
        }

        Class oCls = null;
        for (Object o : set) {
            if (o != null) {
                if (oCls == null) {
                    oCls = o.getClass();
                } else if (!o.getClass().equals(oCls)) {
                    throw new IllegalArgumentException("The set contains mixed classes");
                }
            }
        }
        if (oCls != null && !(isSimpleType(oCls) || isArrayType(oCls))) {
            throw new IllegalArgumentException(
                    "The set contains unsupported type " + oCls.getClass().getCanonicalName());
        }
        if (oCls != null) {
            if (oCls.equals(Integer.class)) {
                return new IntOpenHashSet(set);
            } else if (oCls.equals(Float.class)) {
                return new FloatOpenHashSet(set);
            } else if (oCls.equals(Double.class)) {
                return new DoubleOpenHashSet(set);
            } else if (oCls.equals(Short.class)) {
                return new ShortOpenHashSet(set);
            } else if (oCls.equals(Byte.class)) {
                return new ByteOpenHashSet(set);
            } else if (oCls.equals(Long.class)) {
                return new LongOpenHashSet(set);
            } else if (oCls.equals(Boolean.class)) {
                return new BooleanOpenHashSet(set);
            } else if (oCls.equals(Character.class)) {
                return new CharOpenHashSet(set);
            }
        }
        Set result = new ObjectOpenHashSet(set.size());
        for (Object o : set) {
            result.add(standardizeValue(o));
        }
        return result;
    }

    private static Map getStandardizedMap(Map<?, ?> map) {
        Class mapClass = map.getClass();
        if (TYPED_MAP_TYPES.contains(mapClass)) {
            return map;
        }

        Class oCls = null;
        for (Map.Entry entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key != null) {
                if (oCls == null) {
                    oCls = key.getClass();
                } else if (!key.getClass().equals(oCls)) {
                    throw new IllegalArgumentException("The map contains mixed key classes");
                }
            }
            if (value != null && !(isSimpleType(value.getClass()) || isArrayType(value.getClass()))) {
                throw new IllegalArgumentException(
                        "The map contains unsupported value type " + value.getClass().getCanonicalName());
            }
        }
        if (oCls != null && !isSimpleType(oCls)) {
            throw new IllegalArgumentException(
                    "The map contains unsupported key type " + oCls.getClass().getCanonicalName());
        }
        if (oCls != null) {
            if (oCls.equals(Integer.class)) {
                return new Int2ObjectOpenHashMap(map);
            } else if (oCls.equals(Float.class)) {
                return new Float2ObjectOpenHashMap(map);
            } else if (oCls.equals(Double.class)) {
                return new Double2ObjectOpenHashMap(map);
            } else if (oCls.equals(Short.class)) {
                return new Short2ObjectOpenHashMap(map);
            } else if (oCls.equals(Byte.class)) {
                return new Byte2ObjectOpenHashMap(map);
            } else if (oCls.equals(Long.class)) {
                return new Long2ObjectOpenHashMap(map);
            } else if (oCls.equals(Character.class)) {
                return new Char2ObjectOpenHashMap(map);
            }
        }
        Map result = new Object2ObjectOpenHashMap(map.size());
        for (Map.Entry o : map.entrySet()) {
            result.put(o.getKey(), standardizeValue(o.getValue()));
        }
        return result;
    }

    /**
     * Returns true if <em>type</em> is a number type.
     * <p>
     * This can be true for static, arrays and dynamic types.
     *
     * @param type type to test
     * @return true if <em>type</em> is a number type, false otherwise
     */
    public static boolean isNumberType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        type = getStandardizedType(type);
        return Number.class.isAssignableFrom(type) || int[].class.isAssignableFrom(type) || float[].class
                .isAssignableFrom(type) || double[].class.isAssignableFrom(type) || byte[].class
                        .isAssignableFrom(type) || short[].class.isAssignableFrom(type) || long[].class
                                .isAssignableFrom(type) || type.equals(TimestampIntegerMap.class) || type
                                        .equals(TimestampFloatMap.class) || type
                                                .equals(TimestampDoubleMap.class) || type
                                                        .equals(TimestampLongMap.class) || type
                                                                .equals(TimestampShortMap.class) || type
                                                                        .equals(TimestampByteMap.class) || type
                                                                                .equals(IntervalIntegerMap.class) || type
                                                                                        .equals(IntervalFloatMap.class) || type
                                                                                                .equals(IntervalDoubleMap.class) || type
                                                                                                        .equals(IntervalLongMap.class) || type
                                                                                                                .equals(IntervalShortMap.class) || type
                                                                                                                        .equals(IntervalByteMap.class);
    }

    /**
     * Returns true if <em>type</em> is a string type
     * <p>
     * This can be true for static, arrays and dynamic types.
     *
     * @param type type to test
     * @return true if <em>type</em> is a string type, false otherwise
     */
    public static boolean isStringType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        return type.equals(String.class) || type.equals(String[].class) || type.equals(TimestampStringMap.class) || type
                .equals(IntervalStringMap.class);
    }

    /**
     * Returns true if <em>type</em> is a boolean type
     * <p>
     * This can be true for static, arrays and dynamic types.
     *
     * @param type type to test
     * @return true if <em>type</em> is a boolean type, false otherwise
     */
    public static boolean isBooleanType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        type = getStandardizedType(type);
        return type.equals(Boolean.class) || type.equals(boolean[].class) || type
                .equals(TimestampBooleanMap.class) || type.equals(IntervalBooleanMap.class);
    }

    /**
     * Returns true if <em>type</em> is a dynamic type.
     *
     * @param type type to test
     * @return true if <em>type</em> is a dynamic type, false otherwise
     */
    public static boolean isDynamicType(Class type) {
        return (!type.equals(TimestampMap.class) && TimestampMap.class.isAssignableFrom(type)) || type
                .equals(TimestampSet.class) || (!type.equals(IntervalMap.class) && IntervalMap.class
                        .isAssignableFrom(type)) || type.equals(IntervalSet.class);
    }

    /**
     * Returns true if <em>type</em> is a simple type.
     * <p>
     * Simple types are primitives, String and wrapper types (e.g. Integer).
     *
     * @param type type to test
     * @return true if <em>type</em> is a simple type, false otherwise
     */
    public static boolean isSimpleType(Class type) {
        return (type
                .isPrimitive() && type != void.class) || type == Double.class || type == Float.class || type == Long.class || type == Integer.class || type == Short.class || type == Character.class || type == Byte.class || type == Boolean.class || type == String.class;
    }

    /**
     * Returns true if <em>type</em> is an array type.
     *
     * @param type type to test
     * @return true if <em>type</em> is an array type, false otherwise
     */
    public static boolean isArrayType(Class type) {
        return type.isArray() && isSupported(type.getComponentType());
    }

    /**
     * Returns true if <em>type</em> is a collection type.
     * <p>
     * Collection types are either <em>List</em> or <em>Set</em>.
     *
     * @param type type to test
     * @return true if <em>type</em> is a collection type, false otherwise
     */
    public static boolean isCollectionType(Class type) {
        return List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type);
    }

    /**
     * Returns true if <em>type</em> is a map type.
     * <p>
     * Collection types implement the <em>Map</em> interface.
     *
     * @param type type to test
     * @return true if <em>type</em> is a map type, false otherwise
     */
    public static boolean isMapType(Class type) {
        return Map.class.isAssignableFrom(type);
    }

    /**
     * Returns the type name for the given type.
     *
     * @param type type to get its name
     * @return type name
     */
    public static String getTypeName(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        type = getStandardizedType(type);
        return type.getSimpleName().toLowerCase();
    }

    /**
     * Parses the given time and returns its milliseconds representation.
     *
     * @param dateTime type to parse
     * @param zoneId time zone to use or null to use default time zone (UTC)
     * @return milliseconds representation
     * @throws DateTimeParseException if the time cannot be parsed
     */
    public static double parseDateTime(String dateTime, ZoneId zoneId) throws DateTimeParseException {
        DateTimeFormatter dateTimeParserByTimeZone = getDateTimeParserByTimeZone(zoneId);
        Instant instant = dateTimeParserByTimeZone.parse(dateTime, Instant::from);
        return (double) instant.toEpochMilli();
    }

    /**
     * Parses the given time and returns its milliseconds representation. Default
     * time zone is used (UTC).
     *
     * @param dateTime the type to parse
     * @return milliseconds representation
     * @throws DateTimeParseException if the time cannot be parsed
     */
    public static double parseDateTime(String dateTime) throws DateTimeParseException {
        return parseDateTime(dateTime, null);
    }

    /**
     * Parses an ISO date with or without time or a timestamp (in milliseconds).
     * Returns the date or timestamp converted to a timestamp in milliseconds.
     *
     * @param timeStr Date or timestamp string
     * @param zoneId Time zone to use or null to use default time zone (UTC)
     * @return Timestamp
     * @throws DateTimeParseException if the time cannot be parsed
     */
    public static double parseDateTimeOrTimestamp(String timeStr, ZoneId zoneId) throws DateTimeParseException {
        return FormattingAndParsingUtils.parseDateTimeOrTimestamp(timeStr, zoneId);
    }

    /**
     * Parses an ISO date with or without time or a timestamp (in milliseconds).
     * Returns the date or timestamp converted to a timestamp in milliseconds.
     * Default time zone is used (UTC).
     *
     * @param timeStr Date or timestamp string
     * @return Timestamp
     * @throws DateTimeParseException if the time cannot be parsed
     */
    public static double parseDateTimeOrTimestamp(String timeStr) throws DateTimeParseException {
        return FormattingAndParsingUtils.parseDateTimeOrTimestamp(timeStr);
    }

    /**
     * Returns the string representation of the given timestamp.
     *
     * @param timestamp the time, in milliseconds
     * @return formatted timestamp
     */
    public static String printTimestamp(double timestamp) {
        return TIMESTAMP_PRINTER.format(timestamp);
    }

    /**
     * Returns the date's string representation of the given timestamp.
     *
     * @param timestamp time, in milliseconds
     * @param zoneId time zone to use or null to use default time zone (UTC)
     * @return formatted date
     */
    public static String printDate(double timestamp, ZoneId zoneId) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            return printTimestamp(timestamp);
        }
        Instant ofEpochMilli = Instant.ofEpochMilli((long) timestamp);
        DateTimeFormatter datePrinterByTimeZone = getDatePrinterByTimeZone(zoneId);
        ZonedDateTime zonedDateTime = ofEpochMilli.atZone(datePrinterByTimeZone.getZone());
        return zonedDateTime.format(datePrinterByTimeZone);
    }

    /**
     * Returns the date's string representation of the given timestamp. Default time
     * zone is used (UTC).
     *
     * @param timestamp time, in milliseconds
     * @return formatted date
     */
    public static String printDate(double timestamp) {
        return printDate(timestamp, null);
    }

    /**
     * Returns the time's string representation of the given timestamp.
     *
     * @param timestamp time, in milliseconds
     * @param zoneId time zone to use or null to use default time zone (UTC)
     * @return formatted time
     */
    public static String printDateTime(double timestamp, ZoneId zoneId) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            return printTimestamp(timestamp);
        }
        DateTimeFormatter dateTimePrinterByTimeZone = getDateTimePrinterByTimeZone(zoneId);
        Instant ofEpochMilli = Instant.ofEpochMilli((long) timestamp);
        ZonedDateTime zonedDateTime2 = ofEpochMilli.atZone(dateTimePrinterByTimeZone.getZone());
        OffsetDateTime time = OffsetDateTime.from(zonedDateTime2);
        return time.format(dateTimePrinterByTimeZone);
    }

    /**
     * Returns the time's tring representation of the given timestamp. Default time
     * zone is used (UTC).
     *
     * @param timestamp time, in milliseconds
     * @return formatted time
     */
    public static String printDateTime(double timestamp) {
        return printDateTime(timestamp, null);
    }

    /**
     * Returns the string representation of the given timestamp in the given format.
     *
     * @param timestamp time, in milliseconds
     * @param timeFormat time format
     * @param zoneId time zone to use or null to use default time zone (UTC).
     * @return formatted timestamp
     */
    public static String printTimestampInFormat(double timestamp, TimeFormat timeFormat, ZoneId zoneId) {
        switch (timeFormat) {
            case DATE:
                return AttributeUtils.printDate(timestamp, zoneId);
            case DATETIME:
                return AttributeUtils.printDateTime(timestamp, zoneId);
            case DOUBLE:
                return AttributeUtils.printTimestamp(timestamp);
        }

        throw new UnsupportedOperationException("Unknown TimeFormat");
    }

    /**
     * Returns the string representation of the given timestamp in the given format.
     * Default time zone is used (UTC).
     *
     * @param timestamp time, in milliseconds
     * @param timeFormat time format
     * @return formatted timestamp
     */
    public static String printTimestampInFormat(double timestamp, TimeFormat timeFormat) {
        return printTimestampInFormat(timestamp, timeFormat, null);
    }

    /**
     * Returns the string representation of the given array. The used format is the
     * same format supported by {@link #parse(java.lang.String, java.lang.Class)}
     * method
     *
     * @param arr Input array. Can be an array of objects or primitives.
     * @return formatted array
     */
    public static String printArray(Object arr) {
        return FormattingAndParsingUtils.printArray(arr);
    }

    /**
     * Returns true if the given column is a node column.
     *
     * @param colum column to test
     * @return true if the column is a node column, false otherwise
     */
    public static boolean isNodeColumn(Column colum) {
        return colum.getTable().getElementClass().equals(Node.class);
    }

    /**
     * Returns true if the given column is an edge column.
     *
     * @param colum column to test
     * @return true if the column is an edge column, false otherwise
     */
    public static boolean isEdgeColumn(Column colum) {
        return colum.getTable().getElementClass().equals(Edge.class);
    }

    /**
     * Returns a copy of the provided object.
     * <p>
     * The copy is a deep copy for arrays, {@link IntervalSet},
     * {@link TimestampSet}, sets and lists
     *
     * @param obj object to copy
     * @return copy of the provided object
     */
    public static Object copy(Object obj) {
        if (obj == null) {
            return null;
        }
        Class typeClass = obj.getClass();
        if (!isSupported(typeClass)) {
            throw new IllegalArgumentException("Unsupported type " + typeClass.getCanonicalName());
        }
        typeClass = getStandardizedType(typeClass);
        obj = standardizeValue(obj);

        // Primitive
        if (isSimpleType(typeClass)) {
            return obj;
        }

        // Interval types:
        if (typeClass.equals(IntervalSet.class)) {
            return new IntervalSet((IntervalSet) obj);
        } else if (typeClass.equals(IntervalStringMap.class)) {
            return new IntervalStringMap((IntervalStringMap) obj);
        } else if (typeClass.equals(IntervalByteMap.class)) {
            return new IntervalByteMap((IntervalByteMap) obj);
        } else if (typeClass.equals(IntervalShortMap.class)) {
            return new IntervalShortMap((IntervalShortMap) obj);
        } else if (typeClass.equals(IntervalIntegerMap.class)) {
            return new IntervalIntegerMap((IntervalIntegerMap) obj);
        } else if (typeClass.equals(IntervalLongMap.class)) {
            return new IntervalLongMap((IntervalLongMap) obj);
        } else if (typeClass.equals(IntervalFloatMap.class)) {
            return new IntervalFloatMap((IntervalFloatMap) obj);
        } else if (typeClass.equals(IntervalDoubleMap.class)) {
            return new IntervalDoubleMap((IntervalDoubleMap) obj);
        } else if (typeClass.equals(IntervalBooleanMap.class)) {
            return new IntervalBooleanMap((IntervalBooleanMap) obj);
        } else if (typeClass.equals(IntervalCharMap.class)) {
            return new IntervalCharMap((IntervalCharMap) obj);
        }

        // Timestamp types:
        if (typeClass.equals(TimestampSet.class)) {
            return new TimestampSet((TimestampSet) obj);
        } else if (typeClass.equals(TimestampStringMap.class)) {
            return new TimestampStringMap((TimestampStringMap) obj);
        } else if (typeClass.equals(TimestampByteMap.class)) {
            return new TimestampByteMap((TimestampByteMap) obj);
        } else if (typeClass.equals(TimestampShortMap.class)) {
            return new TimestampShortMap((TimestampShortMap) obj);
        } else if (typeClass.equals(TimestampIntegerMap.class)) {
            return new TimestampIntegerMap((TimestampIntegerMap) obj);
        } else if (typeClass.equals(TimestampLongMap.class)) {
            return new TimestampLongMap((TimestampLongMap) obj);
        } else if (typeClass.equals(TimestampFloatMap.class)) {
            return new TimestampFloatMap((TimestampFloatMap) obj);
        } else if (typeClass.equals(TimestampDoubleMap.class)) {
            return new TimestampDoubleMap((TimestampDoubleMap) obj);
        } else if (typeClass.equals(TimestampBooleanMap.class)) {
            return new TimestampBooleanMap((TimestampBooleanMap) obj);
        } else if (typeClass.equals(TimestampCharMap.class)) {
            return new TimestampCharMap((TimestampCharMap) obj);
        }

        // Array
        if (isArrayType(typeClass)) {
            Class componentType = typeClass.getComponentType();
            int length = Array.getLength(obj);
            Object dest = Array.newInstance(componentType, length);
            System.arraycopy(obj, 0, dest, 0, length);
            return dest;
        }

        // List
        if (obj instanceof CharArrayList) {
            return new CharArrayList((CharArrayList) obj);
        } else if (obj instanceof BooleanArrayList) {
            return new BooleanArrayList((BooleanArrayList) obj);
        } else if (obj instanceof ByteArrayList) {
            return new ByteArrayList((ByteArrayList) obj);
        } else if (obj instanceof ShortArrayList) {
            return new ShortArrayList((ShortArrayList) obj);
        } else if (obj instanceof IntArrayList) {
            return new IntArrayList((IntArrayList) obj);
        } else if (obj instanceof LongArrayList) {
            return new LongArrayList((LongArrayList) obj);
        } else if (obj instanceof FloatArrayList) {
            return new FloatArrayList((FloatArrayList) obj);
        } else if (obj instanceof DoubleArrayList) {
            return new DoubleArrayList((DoubleArrayList) obj);
        } else if (obj instanceof ObjectArrayList) {
            return new ObjectArrayList((ObjectArrayList) obj);
        }

        // Map
        if (obj instanceof Char2ObjectOpenHashMap) {
            return new Char2ObjectOpenHashMap((Char2ObjectOpenHashMap) obj);
        } else if (obj instanceof Byte2ObjectOpenHashMap) {
            return new Byte2ObjectOpenHashMap((Byte2ObjectOpenHashMap) obj);
        } else if (obj instanceof Short2ObjectOpenHashMap) {
            return new Short2ObjectOpenHashMap((Short2ObjectOpenHashMap) obj);
        } else if (obj instanceof Int2ObjectOpenHashMap) {
            return new Int2ObjectOpenHashMap((Int2ObjectOpenHashMap) obj);
        } else if (obj instanceof Long2ObjectOpenHashMap) {
            return new Long2ObjectOpenHashMap((Long2ObjectOpenHashMap) obj);
        } else if (obj instanceof Float2ObjectOpenHashMap) {
            return new Float2ObjectOpenHashMap((Float2ObjectOpenHashMap) obj);
        } else if (obj instanceof Double2ObjectOpenHashMap) {
            return new Double2ObjectOpenHashMap((Double2ObjectOpenHashMap) obj);
        } else if (obj instanceof Object2ObjectOpenHashMap) {
            return new Object2ObjectOpenHashMap((Object2ObjectOpenHashMap) obj);
        }

        // Set
        if (obj instanceof CharOpenHashSet) {
            return new CharOpenHashSet((CharOpenHashSet) obj);
        } else if (obj instanceof BooleanOpenHashSet) {
            return new BooleanOpenHashSet((BooleanOpenHashSet) obj);
        } else if (obj instanceof ByteOpenHashSet) {
            return new ByteOpenHashSet((ByteOpenHashSet) obj);
        } else if (obj instanceof ShortOpenHashSet) {
            return new ShortOpenHashSet((ShortOpenHashSet) obj);
        } else if (obj instanceof IntOpenHashSet) {
            return new IntOpenHashSet((IntOpenHashSet) obj);
        } else if (obj instanceof LongOpenHashSet) {
            return new LongOpenHashSet((LongOpenHashSet) obj);
        } else if (obj instanceof FloatOpenHashSet) {
            return new FloatOpenHashSet((FloatOpenHashSet) obj);
        } else if (obj instanceof DoubleOpenHashSet) {
            return new DoubleOpenHashSet((DoubleOpenHashSet) obj);
        } else if (obj instanceof ObjectOpenHashSet) {
            return new ObjectOpenHashSet((ObjectOpenHashSet) obj);
        }

        return obj;
    }
}
