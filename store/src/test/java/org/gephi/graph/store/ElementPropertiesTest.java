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
import org.gephi.graph.spi.LayoutData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class ElementPropertiesTest {

    @Test
    public void testNodeProperties() {
        NodeImpl.NodePropertiesImpl p = new NodeImpl.NodePropertiesImpl();
        p.setX(1f);
        p.setY(2f);
        p.setZ(3f);
        p.setR(0.2f);
        p.setG(0.4f);
        p.setB(0.6f);
        p.setAlpha(0.4f);
        p.setSize(5f);
        p.setFixed(true);

        Assert.assertEquals(p.x(), 1f);
        Assert.assertEquals(p.y(), 2f);
        Assert.assertEquals(p.z(), 3f);
        Assert.assertEquals(p.r(), 0.2f);
        Assert.assertEquals(p.g(), 0.4f);
        Assert.assertEquals(p.b(), 0.6f);
        Assert.assertEquals(p.alpha(), 0.4f);
        Assert.assertEquals(p.size(), 5f);
        Assert.assertTrue(p.isFixed());
    }

    @Test
    public void testNodeImplProperties() {
        NodeImpl p = new NodeImpl("foo");
        p.setX(1f);
        p.setY(2f);
        p.setZ(3f);
        p.setR(0.2f);
        p.setG(0.4f);
        p.setB(0.6f);
        p.setAlpha(0.4f);
        p.setSize(5f);
        p.setFixed(true);

        Assert.assertEquals(p.x(), 1f);
        Assert.assertEquals(p.y(), 2f);
        Assert.assertEquals(p.z(), 3f);
        Assert.assertEquals(p.r(), 0.2f);
        Assert.assertEquals(p.g(), 0.4f);
        Assert.assertEquals(p.b(), 0.6f);
        Assert.assertEquals(p.alpha(), 0.4f);
        Assert.assertEquals(p.size(), 5f);
        Assert.assertTrue(p.isFixed());
        Assert.assertEquals(new Color(p.getRGBA(), true), p.getColor());
    }

    @Test
    public void testNodePropertiesEquals() {
        NodeImpl.NodePropertiesImpl properties = new NodeImpl.NodePropertiesImpl();
        NodeImpl.NodePropertiesImpl properties2 = new NodeImpl.NodePropertiesImpl();

        Assert.assertEquals(properties, properties2);

        properties.x = 1;

        Assert.assertNotEquals(properties, properties2);
    }

    @Test
    public void testNodePropertiesHashCode() {
        NodeImpl.NodePropertiesImpl properties = new NodeImpl.NodePropertiesImpl();
        NodeImpl.NodePropertiesImpl properties2 = new NodeImpl.NodePropertiesImpl();

        Assert.assertEquals(properties.hashCode(), properties2.hashCode());

        properties.x = 1;

        Assert.assertNotEquals(properties.hashCode(), properties2.hashCode());
    }

    @Test
    public void testNodeSetPosition() {
        NodeImpl.NodePropertiesImpl properties = new NodeImpl.NodePropertiesImpl();
        properties.setPosition(12f, 5f);
        Assert.assertEquals(properties.x(), 12f);
        Assert.assertEquals(properties.y(), 5f);
        Assert.assertEquals(properties.z(), 0f);
        properties.setPosition(1f, 2f, 3f);
        Assert.assertEquals(properties.x(), 1f);
        Assert.assertEquals(properties.y(), 2f);
        Assert.assertEquals(properties.z(), 3f);
    }

    @Test
    public void testNodeImplSetPosition() {
        NodeImpl properties = new NodeImpl("foo");
        properties.setPosition(12f, 5f);
        Assert.assertEquals(properties.x(), 12f);
        Assert.assertEquals(properties.y(), 5f);
        Assert.assertEquals(properties.z(), 0f);
        properties.setPosition(1f, 2f, 3f);
        Assert.assertEquals(properties.x(), 1f);
        Assert.assertEquals(properties.y(), 2f);
        Assert.assertEquals(properties.z(), 3f);
    }

    @Test
    public void testNodeSetColor() {
        Color color = new Color(0.2f, 0.4f, 0.6f, 0.8f);
        NodeImpl.NodePropertiesImpl properties = new NodeImpl.NodePropertiesImpl();
        properties.setColor(color);
        Assert.assertEquals(properties.getColor(), color);
        Assert.assertEquals(properties.r(), 0.2f);
        Assert.assertEquals(properties.g(), 0.4f);
        Assert.assertEquals(properties.b(), 0.6f);
        Assert.assertEquals(properties.alpha(), 0.8f);
    }

    @Test
    public void testNodeImplSetColor() {
        Color color = new Color(0.2f, 0.4f, 0.6f, 0.8f);
        NodeImpl n = new NodeImpl("foo");
        n.setColor(color);
        Assert.assertEquals(n.getColor(), color);
    }

    @Test
    public void testNodeSetRGB() {
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

    @Test
    public void testNodeSetLayoutData() {
        NodeImpl.NodePropertiesImpl p = new NodeImpl.NodePropertiesImpl();
        LayoutData ld = new LayoutData() {
        };
        p.setLayoutData(ld);
        Assert.assertSame(p.getLayoutData(), ld);
    }

    @Test
    public void testNodeImplSetLayoutData() {
        NodeImpl p = new NodeImpl("foo");
        LayoutData ld = new LayoutData() {
        };
        p.setLayoutData(ld);
        Assert.assertSame(p.getLayoutData(), ld);
    }

    @Test
    public void testNodeSetTextProperties() {
        NodeImpl.NodePropertiesImpl p = new NodeImpl.NodePropertiesImpl();
        TextPropertiesImpl tp = new TextPropertiesImpl();
        tp.setSize(42f);
        p.setTextProperties(tp);
        Assert.assertEquals(p.getTextProperties(), tp);
    }

    @Test
    public void testNodeImplGetTextProperties() {
        NodeImpl n = new NodeImpl("foo");
        Assert.assertNotNull(n.getTextProperties());
    }

    @Test
    public void testTextProperties() {
        TextPropertiesImpl p = new TextPropertiesImpl();
        p.setR(0.2f);
        p.setG(0.4f);
        p.setB(0.6f);
        p.setAlpha(0.4f);
        p.setSize(5f);
        p.setText("foo");
        p.setVisible(false);

        Assert.assertEquals(p.getR(), 0.2f);
        Assert.assertEquals(p.getG(), 0.4f);
        Assert.assertEquals(p.getB(), 0.6f);
        Assert.assertEquals(p.getAlpha(), 0.4f);
        Assert.assertEquals(p.getSize(), 5f);
        Assert.assertEquals(p.getText(), "foo");
        Assert.assertFalse(p.isVisible());
    }

    @Test
    public void testTextPropertiesEquals() {
        TextPropertiesImpl properties = new TextPropertiesImpl();
        TextPropertiesImpl properties2 = new TextPropertiesImpl();

        Assert.assertEquals(properties, properties2);

        properties.size = 5f;

        Assert.assertNotEquals(properties, properties2);
    }

    @Test
    public void testTextPropertiesHashCode() {
        TextPropertiesImpl properties = new TextPropertiesImpl();
        TextPropertiesImpl properties2 = new TextPropertiesImpl();

        Assert.assertEquals(properties, properties2);

        properties.size = 5f;

        Assert.assertNotEquals(properties.hashCode(), properties2.hashCode());
    }

    @Test
    public void testTextSetColor() {
        Color color = new Color(0.2f, 0.4f, 0.6f, 0.8f);
        TextPropertiesImpl properties = new TextPropertiesImpl();
        properties.setColor(color);
        Assert.assertEquals(properties.getColor(), color);
        Assert.assertEquals(properties.getR(), 0.2f);
        Assert.assertEquals(properties.getG(), 0.4f);
        Assert.assertEquals(properties.getB(), 0.6f);
        Assert.assertEquals(properties.getAlpha(), 0.8f);
    }

    @Test
    public void testTextSetRGBA() {
        Color color = new Color(0.2f, 0.4f, 0.6f, 0.8f);
        TextPropertiesImpl properties = new TextPropertiesImpl();
        properties.setColor(color);

        Color newColor = new Color(properties.getRGBA(), true);
        Assert.assertEquals(newColor, color);
    }

    @Test
    public void testEdgeProperties() {
        EdgeImpl.EdgePropertiesImpl p = new EdgeImpl.EdgePropertiesImpl();
        p.setR(0.2f);
        p.setG(0.4f);
        p.setB(0.6f);
        p.setAlpha(0.4f);

        Assert.assertEquals(p.r(), 0.2f);
        Assert.assertEquals(p.g(), 0.4f);
        Assert.assertEquals(p.b(), 0.6f);
        Assert.assertEquals(p.alpha(), 0.4f);
    }

    @Test
    public void testEdgeImplProperties() {
        EdgeImpl p = new EdgeImpl("foo", null, null, 0, 1.0, true);
        p.setR(0.2f);
        p.setG(0.4f);
        p.setB(0.6f);
        p.setAlpha(0.4f);

        Assert.assertEquals(p.r(), 0.2f);
        Assert.assertEquals(p.g(), 0.4f);
        Assert.assertEquals(p.b(), 0.6f);
        Assert.assertEquals(p.alpha(), 0.4f);
        Assert.assertEquals(new Color(p.getRGBA(), true), p.getColor());
    }

    @Test
    public void testEdgeSetColor() {
        Color color = new Color(0.2f, 0.4f, 0.6f, 0.8f);
        EdgeImpl.EdgePropertiesImpl properties = new EdgeImpl.EdgePropertiesImpl();
        properties.setColor(color);
        Assert.assertEquals(properties.getColor(), color);
        Assert.assertEquals(properties.r(), 0.2f);
        Assert.assertEquals(properties.g(), 0.4f);
        Assert.assertEquals(properties.b(), 0.6f);
        Assert.assertEquals(properties.alpha(), 0.8f);
    }

    @Test
    public void testEdgeImplSetColor() {
        Color color = new Color(0.2f, 0.4f, 0.6f, 0.8f);
        EdgeImpl e = new EdgeImpl("foo", null, null, 0, 1.0, true);
        e.setColor(color);
        Assert.assertEquals(e.getColor(), color);
    }

    @Test
    public void testEdgeSetRGB() {
        EdgeImpl.EdgePropertiesImpl properties = new EdgeImpl.EdgePropertiesImpl();
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

    @Test
    public void testEdgePropertiesEquals() {
        EdgeImpl.EdgePropertiesImpl properties = new EdgeImpl.EdgePropertiesImpl();
        EdgeImpl.EdgePropertiesImpl properties2 = new EdgeImpl.EdgePropertiesImpl();

        Assert.assertEquals(properties, properties2);

        properties.rgba = 555;

        Assert.assertNotEquals(properties, properties2);
    }

    @Test
    public void testEdgePropertiesHashCode() {
        EdgeImpl.EdgePropertiesImpl properties = new EdgeImpl.EdgePropertiesImpl();
        EdgeImpl.EdgePropertiesImpl properties2 = new EdgeImpl.EdgePropertiesImpl();

        Assert.assertEquals(properties.hashCode(), properties2.hashCode());

        properties.rgba = 555;

        Assert.assertNotEquals(properties.hashCode(), properties2.hashCode());
    }

    @Test
    public void testEdgeSetTextProperties() {
        EdgeImpl.EdgePropertiesImpl p = new EdgeImpl.EdgePropertiesImpl();
        TextPropertiesImpl tp = new TextPropertiesImpl();
        tp.setSize(42f);
        p.setTextProperties(tp);
        Assert.assertEquals(p.getTextProperties(), tp);
    }

    @Test
    public void testEdgeImplGetTextProperties() {
        EdgeImpl e = new EdgeImpl("foo", null, null, 0, 1.0, true);
        Assert.assertNotNull(e.getTextProperties());
    }
}
