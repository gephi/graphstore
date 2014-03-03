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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.gephi.attribute.time.*;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author mbastian
 */
public class AttributeUtils {

    private static final Set<Class> SUPPORTED_TYPES;
    private static final Map<Class, Class> TYPES_STANDARDIZATION;
    private static final DateTimeFormatter DATE_TIME_FORMATTER;
    private static final DateTimeFormatter DATE_PRINTER;
    private static final DateTimeFormatter DATE_TIME_PRINTER;

    static {
        SUPPORTED_TYPES = new HashSet<Class>();

        //Primitives
        SUPPORTED_TYPES.add(Boolean.class);
        SUPPORTED_TYPES.add(boolean.class);
        SUPPORTED_TYPES.add(Integer.class);
        SUPPORTED_TYPES.add(int.class);
        SUPPORTED_TYPES.add(Short.class);
        SUPPORTED_TYPES.add(short.class);
        SUPPORTED_TYPES.add(Long.class);
        SUPPORTED_TYPES.add(long.class);
        SUPPORTED_TYPES.add(BigInteger.class);
        SUPPORTED_TYPES.add(Byte.class);
        SUPPORTED_TYPES.add(byte.class);
        SUPPORTED_TYPES.add(Float.class);
        SUPPORTED_TYPES.add(float.class);
        SUPPORTED_TYPES.add(Double.class);
        SUPPORTED_TYPES.add(double.class);
        SUPPORTED_TYPES.add(BigDecimal.class);
        SUPPORTED_TYPES.add(Character.class);
        SUPPORTED_TYPES.add(char.class);

        //Objects
        SUPPORTED_TYPES.add(String.class);

        //Prinitives Array
        SUPPORTED_TYPES.add(Boolean[].class);
        SUPPORTED_TYPES.add(boolean[].class);
        SUPPORTED_TYPES.add(Integer[].class);
        SUPPORTED_TYPES.add(int[].class);
        SUPPORTED_TYPES.add(Short[].class);
        SUPPORTED_TYPES.add(short[].class);
        SUPPORTED_TYPES.add(Long[].class);
        SUPPORTED_TYPES.add(long[].class);
        SUPPORTED_TYPES.add(BigInteger[].class);
        SUPPORTED_TYPES.add(Byte[].class);
        SUPPORTED_TYPES.add(byte[].class);
        SUPPORTED_TYPES.add(Float[].class);
        SUPPORTED_TYPES.add(float[].class);
        SUPPORTED_TYPES.add(Double[].class);
        SUPPORTED_TYPES.add(double[].class);
        SUPPORTED_TYPES.add(BigDecimal[].class);
        SUPPORTED_TYPES.add(Character[].class);
        SUPPORTED_TYPES.add(char[].class);

        //Objects array
        SUPPORTED_TYPES.add(String[].class);

        //Dynamic
        SUPPORTED_TYPES.add(TimestampSet.class);
        SUPPORTED_TYPES.add(TimestampBooleanSet.class);
        SUPPORTED_TYPES.add(TimestampIntegerSet.class);
        SUPPORTED_TYPES.add(TimestampShortSet.class);
        SUPPORTED_TYPES.add(TimestampLongSet.class);
        SUPPORTED_TYPES.add(TimestampByteSet.class);
        SUPPORTED_TYPES.add(TimestampFloatSet.class);
        SUPPORTED_TYPES.add(TimestampDoubleSet.class);
        SUPPORTED_TYPES.add(TimestampCharSet.class);
        SUPPORTED_TYPES.add(TimestampStringSet.class);

        //Primitive types standardization
        TYPES_STANDARDIZATION = new HashMap<Class, Class>();
        TYPES_STANDARDIZATION.put(boolean.class, Boolean.class);
        TYPES_STANDARDIZATION.put(int.class, Integer.class);
        TYPES_STANDARDIZATION.put(short.class, Short.class);
        TYPES_STANDARDIZATION.put(long.class, Long.class);
        TYPES_STANDARDIZATION.put(byte.class, Byte.class);
        TYPES_STANDARDIZATION.put(float.class, Float.class);
        TYPES_STANDARDIZATION.put(double.class, Double.class);
        TYPES_STANDARDIZATION.put(char.class, Character.class);

        //Array standardization
        TYPES_STANDARDIZATION.put(Boolean[].class, boolean[].class);
        TYPES_STANDARDIZATION.put(Integer[].class, int[].class);
        TYPES_STANDARDIZATION.put(Short[].class, short[].class);
        TYPES_STANDARDIZATION.put(Long[].class, long[].class);
        TYPES_STANDARDIZATION.put(Byte[].class, byte[].class);
        TYPES_STANDARDIZATION.put(Float[].class, float[].class);
        TYPES_STANDARDIZATION.put(Double[].class, double[].class);
        TYPES_STANDARDIZATION.put(Character[].class, char[].class);

        //Datetime
        DATE_TIME_FORMATTER = ISODateTimeFormat.dateOptionalTimeParser();
        DATE_PRINTER = ISODateTimeFormat.date();
        DATE_TIME_PRINTER = ISODateTimeFormat.dateTime();
    }

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
                throw new IllegalArgumentException("The string has a lenght > 1");
            }
            return new Character(str.charAt(0));
        }
        throw new IllegalArgumentException("Unsupported type " + typeClass.getClass().getCanonicalName());
    }

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

    public static Set<Class> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public static boolean isSupported(Class type) {
        if (type == null) {
            throw new NullPointerException();
        }
        return SUPPORTED_TYPES.contains(type);
    }

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

    public static boolean isDynamicType(Class type) {
        return TimestampValueSet.class.isAssignableFrom(type);
    }

    public static String getTypeName(Class type) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
        }
        type = getStandardizedType(type);
        return type.getSimpleName().toLowerCase();
    }

    public static double parseDateTime(String dateTime) {
        return DATE_TIME_FORMATTER.parseDateTime(dateTime).getMillis();
    }

    public static String printDate(double timestamp) {
        return DATE_PRINTER.print((long) timestamp);
    }

    public static String printDateTime(double timestamp) {
        return DATE_TIME_PRINTER.print((long) timestamp);
    }

    public static boolean isNodeColumn(Column colum) {
        return colum.getTable().getElementClass().equals(Node.class);
    }

    public static boolean isEdgeColumn(Column colum) {
        return colum.getTable().getElementClass().equals(Edge.class);
    }

    /**
     * Removes the decimal digits and point of the numbers of string when
     * necessary. Used for trying to parse decimal numbers as not decimal. For
     * example BigDecimal to BigInteger.
     *
     * @param s String to remove decimal digits
     * @return String without dot and decimal digits.
     */
    private static String removeDecimalDigitsFromString(String s) {
        return removeDecimalDigitsFromStringPattern.matcher(s).replaceAll("");
    }
    private static final Pattern removeDecimalDigitsFromStringPattern = Pattern.compile("\\.[0-9]*");
}
