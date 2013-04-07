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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 *
 * @author mbastian
 */
public class AttributeUtils {

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
