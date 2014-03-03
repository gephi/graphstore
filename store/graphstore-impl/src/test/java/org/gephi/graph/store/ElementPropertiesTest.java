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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class ElementPropertiesTest {

    @Test
    public void testNodePropertiesEquals() {
        NodeImpl.NodePropertiesImpl properties = new NodeImpl.NodePropertiesImpl();
        NodeImpl.NodePropertiesImpl properties2 = new NodeImpl.NodePropertiesImpl();

        Assert.assertEquals(properties, properties2);

        properties.x = 1;

        Assert.assertNotEquals(properties, properties2);
    }

    @Test
    public void testSetRGB() {
        NodeImpl.NodePropertiesImpl properties = new NodeImpl.NodePropertiesImpl();
        Assert.assertEquals(properties.getColor(), new Color(0, 0, 0, 255));

        Color color = new Color(255, 128, 128);
        properties.setR(color.getRed() / 255f);
        properties.setG(color.getGreen() / 255f);
        properties.setB(color.getBlue() / 255f);

        Assert.assertEquals(properties.getColor(), color);

        color = new Color(0, 2, 3);
        properties.setR(color.getRed() / 255f);
        properties.setG(color.getGreen() / 255f);
        properties.setB(color.getBlue() / 255f);

        Assert.assertEquals(properties.getColor(), color);

        Color newColor = new Color(properties.getRGBA(), true);
        Assert.assertEquals(newColor, color);

        Color rgbaColor = new Color(properties.r(), properties.g(), properties.b(), properties.alpha());
        Assert.assertEquals(rgbaColor, color);
    }
}
