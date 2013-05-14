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

/**
 *
 * @author mbastian
 */
public class AttributeUtils {

    private static final Set<Class> supportedTypes;
    private static final Map<Class, Class> typesStandardization;

    static {
        supportedTypes = new HashSet<Class>();

        //Primitives
        supportedTypes.add(Boolean.class);
        supportedTypes.add(boolean.class);
        supportedTypes.add(Integer.class);
        supportedTypes.add(int.class);
        supportedTypes.add(Short.class);
        supportedTypes.add(short.class);
        supportedTypes.add(Long.class);
        supportedTypes.add(long.class);
        supportedTypes.add(Byte.class);
        supportedTypes.add(byte.class);
        supportedTypes.add(Float.class);
        supportedTypes.add(float.class);
        supportedTypes.add(Double.class);
        supportedTypes.add(double.class);
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
        supportedTypes.add(Byte[].class);
        supportedTypes.add(byte[].class);
        supportedTypes.add(Float[].class);
        supportedTypes.add(float[].class);
        supportedTypes.add(Double[].class);
        supportedTypes.add(double[].class);
        supportedTypes.add(Character[].class);
        supportedTypes.add(char[].class);

        //Objects array
        supportedTypes.add(String[].class);

        //Dynamic
        supportedTypes.add(TimestampBooleanSet.class);
        supportedTypes.add(TimestampIntegerSet.class);
        supportedTypes.add(TimestampShortSet.class);
        supportedTypes.add(TimestampLongSet.class);
        supportedTypes.add(TimestampByteSet.class);
        supportedTypes.add(TimestampFloatSet.class);
        supportedTypes.add(TimestampDoubleSet.class);
        supportedTypes.add(TimestampCharSet.class);
        supportedTypes.add(TimestampStringSet.class);

        //Primitive types standardization
        typesStandardization = new HashMap<Class, Class>();
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
    }

    public static Object parse(String str, Class typeClass) {
        if (typeClass.equals(String.class)) {
            return str;
        } else if (typeClass.equals(Byte.class)) {
            return new Byte(removeDecimalDigitsFromString(str));
        } else if (typeClass.equals(Short.class)) {
            return new Short(removeDecimalDigitsFromString(str));
        } else if (typeClass.equals(Integer.class)) {
            return new Integer(removeDecimalDigitsFromString(str));
        } else if (typeClass.equals(Long.class)) {
            return new Long(removeDecimalDigitsFromString(str));
        } else if (typeClass.equals(Float.class)) {
            return new Float(str);
        } else if (typeClass.equals(Double.class)) {
            return new Double(str);
        } else if (typeClass.equals(Boolean.class)) {
            return Boolean.valueOf(str);
        } else if (typeClass.equals(Character.class)) {
            return new Character(str.charAt(0));
        } else if (typeClass.equals(BigInteger.class)) {
            return new BigInteger(removeDecimalDigitsFromString(str));
        } else if (typeClass.equals(BigDecimal.class)) {
            return new BigDecimal(str);
        }
        return null;
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
        Class arrayClass = array.getClass().getComponentType();
        if (!arrayClass.isPrimitive()) {
            Class primitiveClass = getPrimitiveType(arrayClass);

            int arrayLength = array.length;
            Object primitiveArray = Array.newInstance(primitiveClass, arrayLength);

            for (int i = 0; i < arrayLength; i++) {
                Object obj = array[i];
                if (obj != null) {
                    Array.set(array, i, obj);
                }
            }
            return primitiveArray;
        }
        return array;
    }

    public static Set<Class> getSupportedTypes() {
        return supportedTypes;
    }

    public static boolean isSupported(Class type) {
        return supportedTypes.contains(type);
    }

    public static Class getStandardizedType(Class type) {
        return typesStandardization.get(type);
    }

    public boolean isStandardizedType(Class type) {
        return typesStandardization.get(type).equals(type);
    }

    public static Class<? extends TimestampValueSet> getDynamicType(Class type) {
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
        throw new IllegalArgumentException("Unsupported type");
    }

    public static Object standardizeValue(Object value) {
        Class type = value.getClass();
        if (type.isArray()) {
            return getPrimitiveArray((Object[]) value);
        }
        return value;
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
