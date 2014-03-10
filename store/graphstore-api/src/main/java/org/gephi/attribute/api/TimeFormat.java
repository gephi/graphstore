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

/**
 * Different representation of time.
 */
public enum TimeFormat {

    DATE {
                @Override
                public double parse(String str) {
                    return AttributeUtils.parseDateTime(str);
                }

                @Override
                public String print(double time) {
                    return AttributeUtils.printDate(time);
                }
            },
    DATETIME {
                @Override
                public double parse(String str) {
                    return AttributeUtils.parseDateTime(str);
                }

                @Override
                public String print(double time) {
                    return AttributeUtils.printDateTime(time);
                }
            },
    DOUBLE {
                @Override
                public double parse(String str) {
                    return Double.parseDouble(str);
                }

                @Override
                public String print(double time) {
                    return String.valueOf(time);
                }

            };

    /**
     * Parses the given string into the time format.
     *
     * @param str the string to parse
     * @return the time, in milliseconds
     */
    public abstract double parse(String str);

    /**
     * Prints the given time in this format.
     *
     * @param time the time
     * @return a formatted time
     */
    public abstract String print(double time);
}
