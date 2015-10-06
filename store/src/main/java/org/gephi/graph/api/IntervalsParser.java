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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
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

/**
 * <p>
 * Class for parsing interval types.
 * </p>
 * 
 * <p>
 * The standard format for {@code IntervalMap} is &lt;[start, end, value1]; [start, end, value2]&gt;.
 * </p>
 * 
 * <p>
 * The standard format for {@code IntervalSet} is &lt;[start, end]; [start, end]&gt;.
 * </p>
 * 
 * <p>
 * Start and end values can be boht numbers and ISO dates or datetimes. Dates and datetimes will be converted to their millisecond-precision timestamp.
 * </p>
 *
 * Examples of valid dynamic intervals are:
 * <ul>
 * <li>&lt;(1, 2, v1); [3, 5, v2]&gt;</li>
 * <li>[1,2]</li>
 * <li>[1,2] (5,6)</li>
 * <li>[1,2]; [1.15,2.21, 'literal value " \' ,[]()']</li>
 * <li>&lt;[1,2]; [1.15,2.21, "literal value \" ' ,[]()"]&gt;</li>
 * </ul>
 * 
 * <p>
 * <b>All open intervals will be converted to closed intervals</b>
 * </p>
 *
 * <p>
 * The most correct examples are those that include &lt; &gt; and proper commas and semicolons for separation, but the parser will be indulgent when possible.
 * </p>
 *
 * <p>
 * See https://gephi.org/users/supported-graph-formats/spreadsheet for more examples
 * </p>
 *
 * @author Eduardo Ramos
 */
public final class IntervalsParser {

    private static final char LOPEN = '(';
    private static final char LCLOSE = '[';
    private static final char ROPEN = ')';
    private static final char RCLOSE = ']';
    private static final char COMMA = ',';

    /**
     * Parses a {@code IntervalSet} type with one or more intervals.
     *
     * @param input Input string to parse
     * @return Resulting {@code IntervalSet}, or null if the input equals '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if there are no intervals in the input string or bounds cannot be parsed into doubles or dates/datetimes.
     */
    public static IntervalSet parseIntervalSet(String input) throws IllegalArgumentException {
        if (input == null) {
            return null;
        }

        List<IntervalWithValue<Object>> intervals;
        try {
            intervals = parseIntervals(null, input);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected expection while parsing intervals", ex);
        }

        IntervalSet result = new IntervalSet(intervals.size());
        for (IntervalWithValue<Object> interval : intervals) {
            result.add(interval.getInterval());
        }

        return result;
    }

    /**
     * Parses a {@code IntervalMap} type with one or more intervals, and their associated values.
     *
     * @param <T> Underlying type of the {@code IntervalMap} values
     * @param typeClass Simple type or {@code IntervalMap} subtype for the result intervals' values.
     * @param input Input string to parse
     * @return Resulting {@code IntervalMap}, or null if the input equals '&lt;empty&gt;' or is null
     * @throws IllegalArgumentException Thrown if type class is not supported, any of the intervals don't have a value or have an invalid value, there are no intervals in the input string or bounds cannot be parsed into doubles or dates/datetimes.
     */
    public static <T> IntervalMap<T> parseIntervalMap(Class<T> typeClass, String input) throws IllegalArgumentException {
        if (typeClass == null) {
            throw new IllegalArgumentException("typeClass required");
        }

        if (input == null) {
            return null;
        }

        List<IntervalWithValue<T>> intervals;
        try {
            intervals = parseIntervals(typeClass, input);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected expection while parsing intervals", ex);
        }
        int capacity = intervals.size();

        IntervalMap result;
        typeClass = AttributeUtils.getStandardizedType(typeClass);
        if (typeClass.equals(String.class)) {
            result = new IntervalStringMap(capacity);
        } else if (typeClass.equals(Byte.class)) {
            result = new IntervalByteMap(capacity);
        } else if (typeClass.equals(Short.class)) {
            result = new IntervalShortMap(capacity);
        } else if (typeClass.equals(Integer.class)) {
            result = new IntervalIntegerMap(capacity);
        } else if (typeClass.equals(Long.class)) {
            result = new IntervalLongMap(capacity);
        } else if (typeClass.equals(Float.class)) {
            result = new IntervalFloatMap(capacity);
        } else if (typeClass.equals(Double.class)) {
            result = new IntervalDoubleMap(capacity);
        } else if (typeClass.equals(Boolean.class)) {
            result = new IntervalBooleanMap(capacity);
        } else if (typeClass.equals(Character.class)) {
            result = new IntervalCharMap(capacity);
        } else {
            throw new IllegalArgumentException("Unsupported type " + typeClass.getClass().getCanonicalName());
        }

        for (IntervalWithValue<T> interval : intervals) {
            T value = interval.getValue();
            if(value == null){
                throw new IllegalArgumentException("A value must be provided for each interval");
            }
            result.put(interval.getInterval(), interval.getValue());
        }

        return result;
    }

    /**
     * Parses intervals with values (of <code>type</code> Class) or without values (null <code>type</code> Class)
     *
     * @param <T> Type of the interval value
     * @param typeClass Class of the intervals' values or null to parse intervals without values
     * @param input Input to parse
     * @return List of Interval
     */
    private static <T> List<IntervalWithValue<T>> parseIntervals(Class<T> typeClass, String input) throws IOException, IllegalArgumentException {
        if (input == null) {
            return null;
        }

        input = input.trim();
        if (input.equalsIgnoreCase("<empty>")) {
            return null;
        }

        List<IntervalWithValue<T>> intervals = new ArrayList<IntervalWithValue<T>>();

        StringReader reader = new StringReader(input + ' ');//Add 1 space so reader.skip function always works when necessary (end of string not reached).

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case LCLOSE:
                case LOPEN:
                    intervals.add(parseInterval(typeClass, reader));
                    break;
                default:
                //Ignore other chars outside of intervals
            }
        }

        if (intervals.isEmpty()) {
            throw new IllegalArgumentException("No dynamic intervals could be parsed");
        }

        return intervals;
    }

    private static <T> IntervalWithValue<T> parseInterval(Class<T> typeClass, StringReader reader) throws IOException {
        ArrayList<String> values = new ArrayList<String>();

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case RCLOSE:
                case ROPEN:
                    return buildInterval(typeClass, values);
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                case COMMA:
                    //Ignore leading whitespace or similar until a value or literal starts:
                    break;
                case '"':
                case '\'':
                    values.add(parseLiteral(reader, c));
                    break;
                default:
                    reader.skip(-1);//Go backwards 1 position, for reading start of value
                    values.add(parseValue(reader));
            }
        }

        return buildInterval(typeClass, values);
    }

    /**
     * Parse literal value until detecting the end of it (quote can be ' or ")
     *
     * @param reader Input reader
     * @param quote Quote mode that started this literal (' or ")
     * @return Parsed value
     * @throws IOException
     */
    private static String parseLiteral(StringReader reader, char quote) throws IOException {
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
     * Parses a value until end is detected either by a comma or an interval closing character.
     *
     * @param reader Input reader
     * @return Parsed value
     * @throws IOException
     */
    private static String parseValue(StringReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case ROPEN:
                case RCLOSE:
                    reader.skip(-1);//Go backwards 1 position, for detecting end of interval
                case COMMA:
                    return sb.toString().trim();
                default:
                    sb.append(c);
            }
        }

        return sb.toString().trim();
    }

    private static <T> IntervalWithValue<T> buildInterval(Class<T> typeClass, ArrayList<String> values) {
        double low, high;
        
        if(typeClass == null && values.size() != 2){
            throw new IllegalArgumentException("Each interval must have 2 values");
        }else if(typeClass != null && values.size() != 3){
            throw new IllegalArgumentException("Each interval must have 3 values");
        }

        low = parseDateTime(values.get(0));
        high = parseDateTime(values.get(1));

        if (typeClass == null) {
            return new IntervalWithValue(low, high, null);
        } else {
            Object value = null;
            if (values.size() == 3) {
                //Interval with value:
                String valString = values.get(2);
                if (typeClass.equals(Byte.class)
                        || typeClass.equals(Short.class)
                        || typeClass.equals(Integer.class)
                        || typeClass.equals(Long.class)
                        || typeClass.equals(BigInteger.class)) {
                    valString = removeDecimalDigitsFromString(valString);
                } else if (typeClass.equals(Float.class)
                        || typeClass.equals(Double.class)
                        || typeClass.equals(BigDecimal.class)) {
                    valString = infinityIgnoreCase(valString);
                }

                value = AttributeUtils.parse(valString, typeClass);
                if(value == null){
                    throw new IllegalArgumentException("Invalid value for type: " + valString);
                }
            }
            
            
            return new IntervalWithValue(low, high, value);
        }
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

    private static double parseDateTime(String time) {
        double value;
        try {
            //Try first to parse as a single double:
            value = Double.parseDouble(infinityIgnoreCase(time));
            if (Double.isNaN(value)) {
                throw new IllegalArgumentException("NaN is not allowed as an interval bound");
            }
        } catch (Exception ex) {
            value = AttributeUtils.parseDateTime(time);
        }
        
        return value;
    }

    /**
     * Represents an Interval with an associated value for it. Only for internal usage in this class.
     *
     * @author Eduardo Ramos
     * @param <T> Type of the value
     */
    private static class IntervalWithValue<T> {

        private final Interval interval;
        private final T value;

        public IntervalWithValue(double low, double high, T value) {
            this.interval = new Interval(low, high);
            this.value = value;
        }

        public IntervalWithValue(Interval interval, T value) {
            this.interval = interval;
            this.value = value;
        }

        public Interval getInterval() {
            return interval;
        }

        public T getValue() {
            return value;
        }
    }
}
