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

import org.gephi.graph.spi.LayoutData;
import java.awt.Color;

/**
 *
 * @author mbastian
 */
public interface NodeProperties {

    public float x();

    public float y();

    public float z();

    public float r();

    public float g();

    public float b();

    public int getRGBA();

    public Color getColor();

    public float alpha();

    public float size();

    public float radius();

    public boolean isFixed();

    public <T extends LayoutData> T getLayoutData();

    public TextProperties getTextProperties();

    public void setX(float x);

    public void setY(float y);

    public void setZ(float z);

    public void setPosition(float x, float y);

    public void setPosition(float x, float y, float z);

    public void setR(float r);

    public void setG(float g);

    public void setB(float b);

    public void setAlpha(float a);

    public void setColor(Color color);

    public void setSize(float size);

    public void setFixed(boolean fixed);

    public void setLayoutData(LayoutData layoutData);
}
