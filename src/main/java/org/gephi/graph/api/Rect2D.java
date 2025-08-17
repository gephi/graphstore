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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Represents a 2D axis-aligned immutable rectangle.
 *
 * @author Eduardo Ramos
 */
public class Rect2D {

    public final float minX, minY;
    public final float maxX, maxY;

    /**
     * Create a new {@link Rect2D} as a copy of the given <code>source</code> .
     *
     * @param source the {@link Rect2D} to copy from
     */
    public Rect2D(Rect2D source) {
        this.minX = source.minX;
        this.minY = source.minY;
        this.maxX = source.maxX;
        this.maxY = source.maxY;
    }

    /**
     * Create a new {@link Rect2D} with the given minimum and maximum corner
     * coordinates.
     *
     * @param minX the x coordinate of the minimum corner
     * @param minY the y coordinate of the minimum corner
     * @param maxX the x coordinate of the maximum corner
     * @param maxY the y coordinate of the maximum corner
     */
    public Rect2D(float minX, float minY, float maxX, float maxY) {
        if (minX > maxX) {
            throw new IllegalArgumentException("minX > maxX");
        }

        if (minY > maxY) {
            throw new IllegalArgumentException("minX > maxX");
        }

        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /**
     * Return the rectangle's width.
     *
     * @return the rectangle's width
     */
    public float width() {
        return maxX - minX;
    }

    /**
     * Return the rectangle's height.
     *
     * @return the rectangle's height
     */
    public float height() {
        return maxY - minY;
    }

    /**
     * Return the rectangle's center, as an array where the first element is the x
     * coordinate and the second element is the y coordinate.
     *
     * @return the rectangle's center
     */
    public float[] center() {
        return new float[] { (maxX + minX) / 2, (maxY + minY) / 2 };
    }

    /**
     * Return the rectangle's radius.
     *
     * @return the rectangle's radius
     */
    public float radius() {
        float width = width();
        float height = height();
        return (float) Math.sqrt(width * width + height * height) / 2;
    }

    private static final DecimalFormat FORMAT = new DecimalFormat("0.###",
            DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    @Override
    public String toString() {
        return toString(FORMAT);
    }

    private String toString(NumberFormat formatter) {
        return "(" + formatter.format(minX) + " " + formatter.format(minY) + ") < " + "(" + formatter
                .format(maxX) + " " + formatter.format(maxY) + ")";
    }

    /**
     * Returns true if this rectangle contains the given rectangle.
     *
     * @param rect the rectangle to check
     * @return true if this rectangle contains, false otherwise
     */
    public boolean contains(Rect2D rect) {
        if (rect == this) {
            return true;
        }

        return contains(rect.minX, rect.minY, rect.maxX, rect.maxY);
    }

    /**
     * Returns true if this rectangle intersects the given rectangle.
     *
     * @param rect the rectangle to check
     * @return true if this rectangle intersects, false otherwise
     */
    public boolean intersects(Rect2D rect) {
        if (rect == this) {
            return true;
        }

        return intersects(rect.minX, rect.minY, rect.maxX, rect.maxY);
    }

    /**
     * Returns true if this rectangle contains the given rectangle.
     *
     * @param minX the x coordinate of the minimum corner
     * @param minY the y coordinate of the minimum corner
     * @param maxX the x coordinate of the maximum corner
     * @param maxY the y coordinate of the maximum corner
     *
     * @return true if this rectangle contains, false otherwise
     */
    public boolean contains(float minX, float minY, float maxX, float maxY) {
        return this.minX <= minX && this.minY <= minY && this.maxX >= maxX && this.maxY >= maxY;
    }

    /**
     * Returns true if this rectangle intersects the given rectangle.
     *
     * @param minX the x coordinate of the minimum corner
     * @param minY the y coordinate of the minimum corner
     * @param maxX the x coordinate of the maximum corner
     * @param maxY the y coordinate of the maximum corner
     *
     * @return true if this rectangle intersects, false otherwise
     */
    public boolean intersects(float minX, float minY, float maxX, float maxY) {
        return this.minX <= maxX && minX <= this.maxX && this.maxY >= minY && maxY >= this.minY;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Rect2D rect2D = (Rect2D) obj;
        return Float.compare(rect2D.minX, minX) == 0 && Float.compare(rect2D.minY, minY) == 0 && Float
                .compare(rect2D.maxX, maxX) == 0 && Float.compare(rect2D.maxY, maxY) == 0;
    }

    @Override
    public int hashCode() {
        int result = (minX != +0.0f ? Float.floatToIntBits(minX) : 0);
        result = 31 * result + (minY != +0.0f ? Float.floatToIntBits(minY) : 0);
        result = 31 * result + (maxX != +0.0f ? Float.floatToIntBits(maxX) : 0);
        result = 31 * result + (maxY != +0.0f ? Float.floatToIntBits(maxY) : 0);
        return result;
    }
}
