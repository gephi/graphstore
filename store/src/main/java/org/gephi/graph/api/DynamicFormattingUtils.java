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

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * Utils for parsing dynamic intervals and timestamps.
 * @author Eduardo Ramos
 */
public final class DynamicFormattingUtils {

    //Bounds
    public static final char DYNAMIC_TYPE_LEFT_BOUND = '<';
    public static final char DYNAMIC_TYPE_RIGHT_BOUND = '>';
    public static final char LEFT_BOUND_BRACKET = '(';
    public static final char LEFT_BOUND_SQUARE_BRACKET = '[';
    public static final char RIGHT_BOUND_BRACKET = ')';
    public static final char RIGHT_BOUND_SQUARE_BRACKET = ']';
    public static final char COMMA = ',';
    
    public static final String EMPTY_DYNAMIC_VALUE = "<empty>";
    
    /**
     * Parse literal value until detecting the end of it (quote can be ' or ")
     *
     * @param reader Input reader
     * @param quote Quote mode that started this literal (' or ")
     * @return Parsed value
     * @throws IOException
     */
    public static String parseLiteral(StringReader reader, char quote) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean escapeEnabled = false;

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            if (c == quote) {
                if (escapeEnabled) {
                    sb.append(quote);
                    escapeEnabled = false;
                } else {
                    return sb.toString();
                }
            } else {
                switch (c) {
                    case '\\':
                        if (escapeEnabled) {
                            sb.append('\\');

                            escapeEnabled = false;
                        } else {
                            escapeEnabled = true;
                        }
                        break;
                    default:
                        if (escapeEnabled) {
                            escapeEnabled = false;
                        }
                        sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    /**
     * Parses a value until end is detected either by a comma or a bounds closing character.
     *
     * @param reader Input reader
     * @return Parsed value
     * @throws IOException
     */
    public static String parseValue(StringReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case RIGHT_BOUND_BRACKET:
                case RIGHT_BOUND_SQUARE_BRACKET:
                    reader.skip(-1);//Go backwards 1 position, for detecting end of bounds
                case COMMA:
                    return sb.toString().trim();
                default:
                    sb.append(c);
            }
        }

        return sb.toString().trim();
    }
    
    /**
     * Converts a string parsed with {@link #parseValue(java.io.StringReader)} to the target type,
     * taking into account dynamic parsing quirks such as numbers with/without decimals and infinity values.
     * @param <T> Target type
     * @param typeClass Target type class
     * @param valString String to parse
     * @return Converted value
     */
    public static <T> T convertValue(Class<T> typeClass, String valString){
        Object value;
        if (typeClass.equals(Byte.class)
                || typeClass.equals(Short.class)
                || typeClass.equals(Integer.class)
                || typeClass.equals(Long.class)
                || typeClass.equals(BigInteger.class)) {
            value = DynamicFormattingUtils.parseNumberWithoutDecimals((Class<? extends Number>) typeClass, valString);
        } else if (typeClass.equals(Float.class)
                || typeClass.equals(Double.class)
                || typeClass.equals(BigDecimal.class)) {
            value = DynamicFormattingUtils.parseNumberWithDecimals((Class<? extends Number>) typeClass, valString);
        } else {
            value = AttributeUtils.parse(valString, typeClass);
        }

        if(value == null){
            throw new IllegalArgumentException("Invalid value for type: " + valString);
        }
        
        return (T) value;
    }

    /**
     * Parses an ISO date with or without time or a timestamp (in milliseconds).
     * Returns the date or timestamp converted to a timestamp in milliseconds.
     * @param timeStr
     * @return Timestamp
     */
    public static double parseDateTimeOrTimestamp(String timeStr) {
        double value;
        try {
            //Try first to parse as a single double:
            value = Double.parseDouble(infinityIgnoreCase(timeStr));
            if (Double.isNaN(value)) {
                throw new IllegalArgumentException("NaN is not allowed as an interval bound");
            }
        } catch (Exception ex) {
            value = AttributeUtils.parseDateTime(timeStr);
        }
        
        return value;
    }
    
    /**
     * Method for allowing inputs such as "infinity" when parsing decimal numbers
     *
     * @param value Input String
     * @return Input String with fixed "Infinity" syntax if necessary.
     */
    private static String infinityIgnoreCase(String value) {
        if (value.equalsIgnoreCase("Infinity")) {
            return "Infinity";
        }
        if (value.equalsIgnoreCase("-Infinity")) {
            return "-Infinity";
        }

        return value;
    }
    
    private static <T extends Number> T parseNumberWithoutDecimals(Class<T> typeClass, String valString){
        valString = DynamicFormattingUtils.removeDecimalDigitsFromString(valString);
        
        return (T) AttributeUtils.parse(valString, typeClass);
    }
    
    private static <T extends Number> T parseNumberWithDecimals(Class<T> typeClass, String valString){
        valString = DynamicFormattingUtils.infinityIgnoreCase(valString);
        
        return (T) AttributeUtils.parse(valString, typeClass);
    }
    
    /**
     * Removes the decimal digits and point of the numbers of string when necessary. Used for trying to parse decimal numbers as not decimal. For example BigDecimal to BigInteger.
     *
     * @param s String to remove decimal digits
     * @return String without dot and decimal digits.
     */
    private static String removeDecimalDigitsFromString(String s) {
        return removeDecimalDigitsFromStringPattern.matcher(s).replaceAll("");
    }
    private static final Pattern removeDecimalDigitsFromStringPattern = Pattern.compile("\\.[0-9]*");
    
}
