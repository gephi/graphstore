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
package org.gephi.attribute.time;

import java.math.BigDecimal;

/**
 *
 * @author mbastian
 */
public enum Estimator {

    AVERAGE,
    MEDIAN,
    SUM,
    MIN,
    MAX,
    FIRST,
    LAST;

    public static boolean getMedian(boolean[] values) {
        if (values.length % 2 == 1) {
            return values[values.length / 2];
        }
        return values[values.length / 2 - 1];
    }

    public static double getSum(double[] values) {
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < values.length; ++i) {
            sum = sum.add(new BigDecimal(values[i]));
        }
        return sum.doubleValue();
    }

    public static float getSum(float[] values) {
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < values.length; ++i) {
            sum = sum.add(new BigDecimal(values[i]));
        }
        return sum.floatValue();
    }

    public static long getSum(long[] values) {
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < values.length; ++i) {
            sum = sum.add(new BigDecimal(values[i]));
        }
        return sum.longValue();
    }

    public static short getSum(short[] values) {
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < values.length; ++i) {
            sum = sum.add(new BigDecimal(values[i]));
        }
        return sum.shortValue();
    }

    public static int getSum(int[] values) {
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < values.length; ++i) {
            sum = sum.add(new BigDecimal(values[i]));
        }
        return sum.intValue();
    }

    public static byte getSum(byte[] values) {
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < values.length; ++i) {
            sum = sum.add(new BigDecimal(values[i]));
        }
        return sum.byteValue();
    }

    public static double getMin(double[] values) {
        double minimum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] < minimum) {
                minimum = values[i];
            }
        }
        return minimum;
    }

    public static byte getMin(byte[] values) {
        byte minimum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] < minimum) {
                minimum = values[i];
            }
        }
        return minimum;
    }

    public static short getMin(short[] values) {
        short minimum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] < minimum) {
                minimum = values[i];
            }
        }
        return minimum;
    }

    public static int getMin(int[] values) {
        int minimum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] < minimum) {
                minimum = values[i];
            }
        }
        return minimum;
    }

    public static float getMin(float[] values) {
        float minimum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] < minimum) {
                minimum = values[i];
            }
        }
        return minimum;
    }

    public static long getMin(long[] values) {
        long minimum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] < minimum) {
                minimum = values[i];
            }
        }
        return minimum;
    }

    public static Comparable getMin(Comparable[] values) {
        Comparable minimum = (Comparable) values[0];
        for (int i = 1; i < values.length; ++i) {
            if (minimum.compareTo(values[i]) > 0) {
                minimum = (Comparable) values[i];
            }
        }
        return minimum;
    }

    public static double getMax(double[] values) {
        double maximum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] > maximum) {
                maximum = values[i];
            }
        }
        return maximum;
    }

    public static byte getMax(byte[] values) {
        byte maximum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] > maximum) {
                maximum = values[i];
            }
        }
        return maximum;
    }

    public static short getMax(short[] values) {
        short maximum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] > maximum) {
                maximum = values[i];
            }
        }
        return maximum;
    }

    public static int getMax(int[] values) {
        int maximum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] > maximum) {
                maximum = values[i];
            }
        }
        return maximum;
    }

    public static float getMax(float[] values) {
        float maximum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] > maximum) {
                maximum = values[i];
            }
        }
        return maximum;
    }

    public static long getMax(long[] values) {
        long maximum = values[0];
        for (int i = 1; i < values.length; ++i) {
            if (values[i] > maximum) {
                maximum = values[i];
            }
        }
        return maximum;
    }

    public static Comparable getMax(Comparable[] values) {
        Comparable minimum = (Comparable) values[0];
        for (int i = 1; i < values.length; ++i) {
            if (minimum.compareTo(values[i]) < 0) {
                minimum = (Comparable) values[i];
            }
        }
        return minimum;
    }

    public static double getFirst(double[] values) {
        return values[0];
    }

    public static float getFirst(float[] values) {
        return values[0];
    }

    public static int getFirst(int[] values) {
        return values[0];
    }

    public static byte getFirst(byte[] values) {
        return values[0];
    }

    public static long getFirst(long[] values) {
        return values[0];
    }

    public static short getFirst(short[] values) {
        return values[0];
    }

    public static char getFirst(char[] values) {
        return values[0];
    }

    public static boolean getFirst(boolean[] values) {
        return values[0];
    }

    public static Object getFirst(Object[] values) {
        return values[0];
    }

    public static double getLast(double[] values) {
        return values[values.length - 1];
    }

    public static float getLast(float[] values) {
        return values[values.length - 1];
    }

    public static int getLast(int[] values) {
        return values[values.length - 1];
    }

    public static byte getLast(byte[] values) {
        return values[values.length - 1];
    }

    public static long getLast(long[] values) {
        return values[values.length - 1];
    }

    public static short getLast(short[] values) {
        return values[values.length - 1];
    }

    public static char getLast(char[] values) {
        return values[values.length - 1];
    }

    public static boolean getLast(boolean[] values) {
        return values[values.length - 1];
    }

    public static Object getLast(Object[] values) {
        return values[values.length - 1];
    }
}
