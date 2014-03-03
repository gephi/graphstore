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
package org.gephi.graph.store;

import java.awt.Color;
import org.gephi.graph.api.TextProperties;

/**
 *
 * @author mbastian
 */
public class TextPropertiesImpl implements TextProperties {

    protected boolean visible;
    protected int rgba;
    protected float size;
    protected String text;

    public TextPropertiesImpl() {
        this.rgba = 255 << 24;  //Alpha set to 1
        this.size = 1f;
        this.visible = true;
    }

    @Override
    public float getR() {
        return ((rgba >> 16) & 0xFF) / 255f;
    }

    @Override
    public float getG() {
        return ((rgba >> 8) & 0xFF) / 255f;
    }

    @Override
    public float getB() {
        return (rgba & 0xFF) / 255f;
    }

    @Override
    public float getAlpha() {
        return ((rgba >> 24) & 0xFF) / 255f;
    }

    @Override
    public int getRGBA() {
        return rgba;
    }

    @Override
    public Color getColor() {
        return new Color(rgba, true);
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setR(float r) {
        rgba |= ((int) (r * 255f)) << 16;
    }

    @Override
    public void setG(float g) {
        this.rgba |= ((int) (g * 255f)) << 8;
    }

    @Override
    public void setB(float b) {
        this.rgba |= ((int) (b * 255f));
    }

    @Override
    public void setAlpha(float a) {
        this.rgba |= ((int) (a * 255f)) << 24;
    }

    @Override
    public void setColor(Color color) {
        this.rgba = (color.getAlpha() << 24) | color.getRGB();
    }

    @Override
    public void setSize(float size) {
        this.size = size;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.visible ? 1 : 0);
        hash = 97 * hash + this.rgba;
        hash = 97 * hash + Float.floatToIntBits(this.size);
        hash = 97 * hash + (this.text != null ? this.text.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TextPropertiesImpl other = (TextPropertiesImpl) obj;
        if (this.visible != other.visible) {
            return false;
        }
        if (this.rgba != other.rgba) {
            return false;
        }
        if (Float.floatToIntBits(this.size) != Float.floatToIntBits(other.size)) {
            return false;
        }
        if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
            return false;
        }
        return true;
    }
}
