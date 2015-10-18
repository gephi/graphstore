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

import org.gephi.graph.impl.TimestampsParser;
import org.gephi.graph.impl.IntervalsParser;
import org.gephi.graph.impl.DynamicFormattingUtils;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampShortMap;
import org.gephi.graph.api.types.TimestampLongMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.graph.api.types.TimestampCharMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampBooleanMap;
import org.gephi.graph.api.types.TimestampFloatMap;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.graph.api.types.TimestampByteMap;
import org.gephi.graph.api.types.TimestampIntegerMap;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Set of utility methods to manipulate supported attribute types.
 * <p>
 * The attribute system is built with a set of supported column types. This
 * class contains utilities to parse and convert supported types. It also
 * contains utilities to manipulate primitive arrays (the preferred array type)
 * and date/time types.
 */
public class AttributeUtils {

    private static final Set<Class> SUPPORTED_TYPES;
    private static final Map<Class, Class> TYPES_STANDARDIZATION;
    private static final DateTimeFormatter DATE_TIME_FORMATTER;
    private static final DateTimeFormatter DATE_PRINTER;
    private static final DateTimeFormatter DATE_TIME_PRINTER;
    private static final DecimalFormat TIMESTAMP_PRINTER; 

    static {
        final Set<Class> supportedTypes = new HashSet<Class>();

        //Primitives
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

        //Objects
        supportedTypes.add(String.class);

        //Prinitives Array
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

        //Objects array
        supportedTypes.add(String[].class);

        //Dynamic (timestamps)
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

        //Dynamic (intervals)
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

        //Assign
        SUPPORTED_TYPES = Collections.unmodifiableSet(supportedTypes);

        //Primitive types standardization
        final Map<Class, Class> typesStandardization = new HashMap<Class, Class>();
        typesStandardization.put(boolean.class, Boolean.class);
        typesStandardization.put(int.class, Integer.class);
        typesStandardization.put(short.class, Short.class);
        typesStandardization.put(long.class, Long.class);
        typesStandardization.put(byte.class, Byte.class);
        typesStandardization.put(float.class, Float.class);
        typesStandardization.put(double.class, Double.class);
        typesStandardization.put(char.class, Character.class);

        //Array standardization
        typesStandardization.put(Boolean[].class, boolean[].class);
        typesStandardization.put(Integer[].class, int[].class);
        typesStandardization.put(Short[].class, short[].class);
        typesStandardization.put(Long[].class, long[].class);
        typesStandardization.put(Byte[].class, byte[].class);
        typesStandardization.put(Float[].class, float[].class);
        typesStandardization.put(Double[].class, double[].class);
        typesStandardization.put(Character[].class, char[].class);

        //Assign
        TYPES_STANDARDIZATION = Collections.unmodifiableMap(typesStandardization);

        //Datetime
        DATE_TIME_FORMATTER = ISODateTimeFormat.dateOptionalTimeParser()
                .withZoneUTC();//Make sure UTC+0 timezone is used by default
        DATE_PRINTER = ISODateTimeFormat.date()
                .withZoneUTC();
        DATE_TIME_PRINTER = ISODateTimeFormat.dateTime()
                .withZoneUTC();
        
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        decimalFormatSymbols.setInfinity(DynamicFormattingUtils.INFINITY);
        TIMESTAMP_PRINTER = new DecimalFormat("0.0###", decimalFormatSymbols);//1 to 4 decimals
    }

    private AttributeUtils() {
        // Only static methods
    }

    /**
     * Parses the given string using the type class provided and returns an
     * instance.
     *
     * @param str the string to parse
     * @param typeClass the class of the desired type
     * @return an instance of the type class, or null if <em>str</em> is null or
     * empty
     */
    public static Object parse(String str, Class typeClass) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        typeClass = getStandardizedType(typeClass);

        //Simple types:
        if (typeClass.equals(String.class)) {
            return str;
        } else if (typeClass.equals(Byte.class)) {
            return new Byte(str);
        } else if (typeClass.equals(Short.class)) {
            return new Short(str);
        } else if (typeClass.equals(Integer.class)) {
            return new Integer(str);
        } else if (typeClass.equals(Long.class)) {
            return new Long(str);
        } else if (typeClass.equals(Float.class)) {
            return new Float(str);
        } else if (typeClass.equals(Double.class)) {
            return new Double(str);
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
        
        //Interval types:
        if (typeClass.equals(IntervalSet.class)) {
            return IntervalsParser.parseIntervalSet(str);
        } else if (typeClass.equals(IntervalStringMap.class)) {
            return IntervalsParser.parseIntervalMap(String.class, str);
        } else if (typeClass.equals(IntervalByteMap.class)) {
            return IntervalsParser.parseIntervalMap(Byte.class, str);
        } else if (typeClass.equals(IntervalShortMap.class)) {
            return IntervalsParser.parseIntervalMap(Short.class, str);
        } else if (typeClass.equals(IntervalIntegerMap.class)) {
            return IntervalsParser.parseIntervalMap(Integer.class, str);
        } else if (typeClass.equals(IntervalLongMap.class)) {
            return IntervalsParser.parseIntervalMap(Long.class, str);
        } else if (typeClass.equals(IntervalFloatMap.class)) {
            return IntervalsParser.parseIntervalMap(Float.class, str);
        } else if (typeClass.equals(IntervalDoubleMap.class)) {
            return IntervalsParser.parseIntervalMap(Double.class, str);
        } else if (typeClass.equals(IntervalBooleanMap.class)) {
            return IntervalsParser.parseIntervalMap(Boolean.class, str);
        } else if (typeClass.equals(IntervalCharMap.class)) {
            return IntervalsParser.parseIntervalMap(Character.class, str);
        }
        
        //Timestamp types:
        if (typeClass.equals(TimestampSet.class)) {
            return TimestampsParser.parseTimestampSet(str);
        } else if (typeClass.equals(TimestampStringMap.class)) {
            return TimestampsParser.parseTimestampMap(String.class, str);
        } else if (typeClass.equals(TimestampByteMap.class)) {
            return TimestampsParser.parseTimestampMap(Byte.class, str);
        } else if (typeClass.equals(TimestampShortMap.class)) {
            return TimestampsParser.parseTimestampMap(Short.class, str);
        } else if (typeClass.equals(TimestampIntegerMap.class)) {
            return TimestampsParser.parseTimestampMap(Integer.class, str);
        } else if (typeClass.equals(TimestampLongMap.class)) {
            return TimestampsParser.parseTimestampMap(Long.class, str);
        } else if (typeClass.equals(TimestampFloatMap.class)) {
            return TimestampsParser.parseTimestampMap(Float.class, str);
        } else if (typeClass.equals(TimestampDoubleMap.class)) {
            return TimestampsParser.parseTimestampMap(Double.class, str);
        } else if (typeClass.equals(TimestampBooleanMap.class)) {
            return TimestampsParser.parseTimestampMap(Boolean.class, str);
        } else if (typeClass.equals(TimestampCharMap.class)) {
            return TimestampsParser.parseTimestampMap(Character.class, str);
        }
        
        throw new IllegalArgumentException("Unsupported type " + typeClass.getClass().getCanonicalName());
    }

    /**
     * Returns the primitive type for the given wrapped primitive.
     * <p>
     * Example: Returns <em>int.class</em> given <em>Integer.class</em>
     *
     * @param type the type to get the primitive type from
     * @return the primitive type
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
     * @param array a wrapped primitive array instance
     * @return a primitive array instance
     */
    public static Object getPrimitiveArray(Object[] array) {
        if (!isSupported(array.getClass())) {
            throw new IllegalArgumentException("Unsupported type " + array.getClass().getCanonicalName());
        }
        Class arrayClass = array.getClass().getComponentType();
        if (!arrayClass.isPrimitive()) {
            Class primitiveClass = getPrimitiveType(arrayClass);

            int arrayLength = array.length;
            Object primitiveArray = Array.newInstance(primitiveClass, arrayLength);

            for (int i = 0; i < arrayLength; i++) {
                Object obj = array[i];
                if (obj != null) {
                    Array.set(primitiveArray, i, obj);
                }
            }
            return primitiveArray;
        }
        return array;
    }

    /**
     * Returns the set of types supported.
     *
     * @return the set of supported types
     */
    public static Set<Class> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    /**
     * Returns true if <em>type</em> is a supported type.
     *
     * @param type the type to test support
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(Class type) {
        if (type == null) {
            throw new NullPointerException();
        }
        return SUPPORTED_TYPES.contains(type);
    }

    /**
     * Returns the standardized type for the given type class.
     * <p>
     * For instance, <code>getStandardizedType(int.class)</code> would return
     * <code>Integer.class</code>.
     *
     * @param type the type to standardize
     * @return the standardized type
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
     * @param type the static type
     * @return the timestamp map type
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
     * @param type the static type
     * @return the timestamp map type
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
     * @param type the time map type
     * @return the static type
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
     * @param value the value to standardize
     * @return the standardized value, or <em>value</em> if already standardized
     */
    public static Object standardizeValue(Object value) {
        if (value == null) {
            return null;
        }
        Class type = value.getClass();
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        if (type.isArray()) {
            return getPrimitiveArray((Object[]) value);
        }
        return value;
    }

    /**
     * Returns true if <em>type</em> is a number type.
     * <p>
     * This can be true for static, arrays and dynamic types.
     *
     * @param type the type to test
     * @return true if <em>type</em> is a number type, false otherwise
     */
    public static boolean isNumberType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        type = getStandardizedType(type);
        return Number.class.isAssignableFrom(type)
                || int[].class.isAssignableFrom(type)
                || float[].class.isAssignableFrom(type)
                || double[].class.isAssignableFrom(type)
                || byte[].class.isAssignableFrom(type)
                || short[].class.isAssignableFrom(type)
                || long[].class.isAssignableFrom(type)
                || type.equals(TimestampIntegerMap.class)
                || type.equals(TimestampFloatMap.class)
                || type.equals(TimestampDoubleMap.class)
                || type.equals(TimestampLongMap.class)
                || type.equals(TimestampShortMap.class)
                || type.equals(TimestampByteMap.class)
                || type.equals(IntervalIntegerMap.class)
                || type.equals(IntervalFloatMap.class)
                || type.equals(IntervalDoubleMap.class)
                || type.equals(IntervalLongMap.class)
                || type.equals(IntervalShortMap.class)
                || type.equals(IntervalByteMap.class);
    }

    /**
     * Returns true if <em>type</em> is a dynamic type.
     *
     * @param type the type to test
     * @return true if <em>type</em> is a dynamic type, false otherwise
     */
    public static boolean isDynamicType(Class type) {
        return (!type.equals(TimestampMap.class)
                && TimestampMap.class.isAssignableFrom(type))
                || type.equals(TimestampSet.class)
                || (!type.equals(IntervalMap.class)
                && IntervalMap.class.isAssignableFrom(type));
    }

    /**
     * Returns true if <em>type</em> is a simple type.
     * <p>
     * Simple types are primitives, String and wrapper types (e.g. Integer).
     *
     * @param type the type to test
     * @return true if <em>type</em> is a simple type, false otherwise
     */
    public static boolean isSimpleType(Class type) {
        return (type.isPrimitive() && type != void.class)
                || type == Double.class || type == Float.class || type == Long.class
                || type == Integer.class || type == Short.class || type == Character.class
                || type == Byte.class || type == Boolean.class || type == String.class;
    }

    /**
     * Returns the type name for the given type.
     *
     * @param type the type to get its name
     * @return the type name
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
     * @param dateTime the type to parse
     * @return the milliseconds representation
     */
    public static double parseDateTime(String dateTime) {
        return DATE_TIME_FORMATTER.parseDateTime(dateTime).getMillis();
    }

    /**
     * Returns the string representation of the given timestamp.
     *
     * @param timestamp the time, in milliseconds
     * @return the formatted timestamp
     */
    public static String printTimestamp(double timestamp) {
        return TIMESTAMP_PRINTER.format(timestamp);
    }
    
    /**
     * Returns the date's string representation of the given timestamp.
     *
     * @param timestamp the time, in milliseconds
     * @return the formatted date
     */
    public static String printDate(double timestamp) {
        if(Double.isInfinite(timestamp)){
            return printTimestamp(timestamp);
        }
        return DATE_PRINTER.print((long) timestamp);
    }

    /**
     * Returns the time's string representation of the given timestamp.
     *
     * @param timestamp the time, in milliseconds
     * @return the formatted time
     */
    public static String printDateTime(double timestamp) {
        if(Double.isInfinite(timestamp)){
            return printTimestamp(timestamp);
        }
        return DATE_TIME_PRINTER.print((long) timestamp);
    }

    /**
     * Returns the representation of the given timestamp in the given format.
     *
     * @param timestamp the time, in milliseconds
     * @param timeFormat the time format
     * @return the formatted timestamp
     */
    public static String printTimestampInFormat(double timestamp, TimeFormat timeFormat) {
        switch (timeFormat) {
            case DATE:
                return AttributeUtils.printDate(timestamp);
            case DATETIME:
                return AttributeUtils.printDateTime(timestamp);
            case DOUBLE:
                return AttributeUtils.printTimestamp(timestamp);
        }
        
        throw new UnsupportedOperationException("Unknown TimeFormat");
    }
    
    /**
     * Returns true if the given column is a node column.
     *
     * @param colum the column to test
     * @return true if the column is a node column, false otherwise
     */
    public static boolean isNodeColumn(Column colum) {
        return colum.getTable().getElementClass().equals(Node.class);
    }

    /**
     * Returns true if the given column is an edge column.
     *
     * @param colum the column to test
     * @return true if the column is an edge column, false otherwise
     */
    public static boolean isEdgeColumn(Column colum) {
        return colum.getTable().getElementClass().equals(Edge.class);
    }
}
