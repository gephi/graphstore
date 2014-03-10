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
package org.gephi.attribute.api;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.gephi.attribute.time.*;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Attribute utilities.
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

        //Dynamic
        supportedTypes.add(TimestampSet.class);
        supportedTypes.add(TimestampBooleanSet.class);
        supportedTypes.add(TimestampIntegerSet.class);
        supportedTypes.add(TimestampShortSet.class);
        supportedTypes.add(TimestampLongSet.class);
        supportedTypes.add(TimestampByteSet.class);
        supportedTypes.add(TimestampFloatSet.class);
        supportedTypes.add(TimestampDoubleSet.class);
        supportedTypes.add(TimestampCharSet.class);
        supportedTypes.add(TimestampStringSet.class);

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
        DATE_TIME_FORMATTER = ISODateTimeFormat.dateOptionalTimeParser();
        DATE_PRINTER = ISODateTimeFormat.date();
        DATE_TIME_PRINTER = ISODateTimeFormat.dateTime();
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
            return new Character(str.charAt(0));
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
     *
     * @param type the type to test
     * @return true if <em>type</em> is standardized, false otherwise
     */
    public static boolean isStandardizedType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        Class t = TYPES_STANDARDIZATION.get(type);
        if (t != null && t.equals(type)) {
            return true;
        } else if (t == null) {
            return true;
        }
        return false;
    }

    /**
     * Returns the dynamic value type for the given type.
     *
     * @param type the static type
     * @return the dynamic type
     */
    public static Class<? extends TimestampValueSet> getDynamicType(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        type = getStandardizedType(type);
        if (type.equals(Boolean.class)) {
            return TimestampBooleanSet.class;
        } else if (type.equals(Integer.class)) {
            return TimestampIntegerSet.class;
        } else if (type.equals(Short.class)) {
            return TimestampShortSet.class;
        } else if (type.equals(Long.class)) {
            return TimestampLongSet.class;
        } else if (type.equals(Byte.class)) {
            return TimestampByteSet.class;
        } else if (type.equals(Float.class)) {
            return TimestampFloatSet.class;
        } else if (type.equals(Double.class)) {
            return TimestampDoubleSet.class;
        } else if (type.equals(Character.class)) {
            return TimestampCharSet.class;
        } else if (type.equals(String.class)) {
            return TimestampStringSet.class;
        }
        throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
    }

    /**
     * Returns the static type for the given dynamic type.
     *
     * @param type the dynamic type
     * @return the static type
     */
    public static Class getStaticType(Class<? extends TimestampValueSet> type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        if (type.equals(TimestampBooleanSet.class)) {
            return Boolean.class;
        } else if (type.equals(TimestampIntegerSet.class)) {
            return Integer.class;
        } else if (type.equals(TimestampShortSet.class)) {
            return Short.class;
        } else if (type.equals(TimestampLongSet.class)) {
            return Long.class;
        } else if (type.equals(TimestampByteSet.class)) {
            return Byte.class;
        } else if (type.equals(TimestampFloatSet.class)) {
            return Float.class;
        } else if (type.equals(TimestampDoubleSet.class)) {
            return Double.class;
        } else if (type.equals(TimestampCharSet.class)) {
            return Character.class;
        } else if (type.equals(TimestampStringSet.class)) {
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
                || Number[].class.isAssignableFrom(type)
                || type.equals(TimestampIntegerSet.class)
                || type.equals(TimestampFloatSet.class)
                || type.equals(TimestampDoubleSet.class)
                || type.equals(TimestampLongSet.class)
                || type.equals(TimestampShortSet.class)
                || type.equals(TimestampByteSet.class);
    }

    /**
     * Returns true if <em>type</em> is a dynamic type.
     *
     * @param type the type to test
     * @return true if <em>type</em> is a dynamic type, false otherwise
     */
    public static boolean isDynamicType(Class type) {
        return TimestampValueSet.class.isAssignableFrom(type);
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
     * Returns the date's string representation of the given timestamp.
     *
     * @param timestamp the time, in milliseconds
     * @return the formatted date
     */
    public static String printDate(double timestamp) {
        return DATE_PRINTER.print((long) timestamp);
    }

    /**
     * Returns the time's string representation of the given timestamp.
     *
     * @param timestamp the time, in milliseconds
     * @return the formatted time
     */
    public static String printDateTime(double timestamp) {
        return DATE_TIME_PRINTER.print((long) timestamp);
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
