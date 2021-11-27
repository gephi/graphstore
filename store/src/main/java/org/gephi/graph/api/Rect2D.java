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

    public float width() {
        return maxX - minX;
    }

    public float height() {
        return maxY - minY;
    }

    public float[] center() {
        return new float[] { (maxX + minX) / 2, (maxY + minY) / 2 };
    }

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

    public String toString(NumberFormat formatter) {
        return "(" + formatter.format(minX) + " " + formatter.format(minY) + ") < " + "(" + formatter.format(maxX) + " " + formatter
                .format(maxY) + ")";
    }

    public boolean contains(Rect2D rect) {
        if (rect == this) {
            return true;
        }

        return contains(rect.minX, rect.minY, rect.maxX, rect.maxY);
    }

    public boolean intersects(Rect2D rect) {
        if (rect == this) {
            return true;
        }

        return intersects(rect.minX, rect.minY, rect.maxX, rect.maxY);
    }

    public boolean contains(float minX, float minY, float maxX, float maxY) {
        return this.minX <= minX && this.minY <= minY && this.maxX >= maxX && this.maxY >= maxY;
    }

    public boolean intersects(float minX, float minY, float maxX, float maxY) {
        return this.minX <= maxX && minX <= this.maxX && this.maxY >= minY && maxY >= this.minY;
    }
}
