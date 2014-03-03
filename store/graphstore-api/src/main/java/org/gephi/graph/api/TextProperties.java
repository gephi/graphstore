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

import java.awt.Color;

/**
 *
 * @author mbastian
 */
public interface TextProperties {

    public float getR();

    public float getG();

    public float getB();

    public int getRGBA();

    public Color getColor();

    public float getAlpha();

    public float getSize();

    public boolean isVisible();

    public String getText();

    public void setR(float r);

    public void setG(float g);

    public void setB(float b);

    public void setAlpha(float a);

    public void setColor(Color color);

    public void setSize(float size);

    public void setVisible(boolean visible);

    public void setText(String text);
}
