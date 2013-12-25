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
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeProperties;
import org.gephi.graph.api.TextProperties;
import org.gephi.graph.spi.LayoutData;

/**
 *
 * @author mbastian
 */
public class NodeImpl extends ElementImpl implements Node {

    protected int storeId = NodeStore.NULL_ID;
    protected EdgeImpl[] headOut = new EdgeImpl[GraphStoreConfiguration.EDGESTORE_DEFAULT_TYPE_COUNT];
    protected EdgeImpl[] headIn = new EdgeImpl[GraphStoreConfiguration.EDGESTORE_DEFAULT_TYPE_COUNT];
    //Degree
    protected int inDegree;
    protected int outDegree;
    protected int mutualDegree;
    //Props
    protected final NodePropertiesImpl properties;

    public NodeImpl(Object id, GraphStore graphStore) {
        super(id, graphStore);
        this.properties = GraphStoreConfiguration.ENABLE_NODE_PROPERTIES ? new NodePropertiesImpl() : null;
        this.attributes = new Object[GraphStoreConfiguration.ELEMENT_ID_INDEX + 1];
        this.attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX] = id;
    }

    public NodeImpl(Object id) {
        this(id, null);
    }

    @Override
    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int id) {
        this.storeId = id;
    }

    public int getDegree() {
        return inDegree + outDegree;
    }

    public int getInDegree() {
        return inDegree;
    }

    public int getOutDegree() {
        return outDegree;
    }

    public int getUndirectedDegree() {
        return inDegree + outDegree - mutualDegree;
    }

    @Override
    ColumnStore getColumnStore() {
        if (graphStore != null) {
            return graphStore.nodeColumnStore;
        }
        return null;
    }

    @Override
    TimestampMap getTimestampMap() {
        if (graphStore != null) {
            return graphStore.timestampStore.nodeMap;
        }
        return null;
    }

    @Override
    TimestampIndexStore<Node> getTimestampIndexStore() {
        if (graphStore != null) {
            return graphStore.timestampStore.nodeIndexStore;
        }
        return null;
    }

    @Override
    boolean isValid() {
        return storeId != NodeStore.NULL_ID;
    }

    @Override
    public float x() {
        return properties.x;
    }

    @Override
    public float y() {
        return properties.y;
    }

    @Override
    public float z() {
        return properties.z;
    }

    @Override
    public float r() {
        return properties.r();
    }

    @Override
    public float g() {
        return properties.g();
    }

    @Override
    public float b() {
        return properties.b();
    }

    @Override
    public float alpha() {
        return properties.alpha();
    }

    @Override
    public int getRGBA() {
        return properties.rgba;
    }

    @Override
    public Color getColor() {
        return properties.getColor();
    }

    @Override
    public float size() {
        return properties.size;
    }

    @Override
    public boolean isFixed() {
        return properties.isFixed();
    }

    @Override
    public <T extends LayoutData> T getLayoutData() {
        return properties.getLayoutData();
    }

    @Override
    public TextProperties getTextProperties() {
        return properties.getTextProperties();
    }

    protected void setNodeProperties(NodePropertiesImpl nodeProperties) {
        properties.x = nodeProperties.x;
        properties.y = nodeProperties.y;
        properties.z = nodeProperties.z;
        properties.rgba = nodeProperties.rgba;
        properties.size = nodeProperties.size;
        properties.fixed = nodeProperties.fixed;
        if (properties.textProperties != null) {
            properties.setTextProperties(nodeProperties.textProperties);
        }
    }

    @Override
    public void setX(float x) {
        properties.setX(x);
    }

    @Override
    public void setY(float y) {
        properties.setY(y);
    }

    @Override
    public void setZ(float z) {
        properties.setZ(z);
    }

    @Override
    public void setPosition(float x, float y) {
        properties.setPosition(x, y);
    }

    @Override
    public void setPosition(float x, float y, float z) {
        properties.setPosition(x, y, z);
    }

    @Override
    public void setR(float r) {
        properties.setR(r);
    }

    @Override
    public void setG(float g) {
        properties.setG(g);
    }

    @Override
    public void setB(float b) {
        properties.setB(b);
    }

    @Override
    public void setAlpha(float a) {
        properties.setAlpha(a);
    }

    @Override
    public void setColor(Color color) {
        properties.setColor(color);
    }

    @Override
    public void setSize(float size) {
        properties.setSize(size);
    }

    @Override
    public void setFixed(boolean fixed) {
        properties.setFixed(fixed);
    }

    @Override
    public void setLayoutData(LayoutData layoutData) {
        properties.setLayoutData(layoutData);
    }
    
    protected static class NodePropertiesImpl implements NodeProperties {

        protected final TextPropertiesImpl textProperties;
        protected float x;
        protected float y;
        protected float z;
        protected int rgba;
        protected float size;
        protected boolean fixed;
        protected LayoutData layoutData;

        public NodePropertiesImpl() {
            this.textProperties = new TextPropertiesImpl();
            this.rgba = 255 << 24;  //Alpha set to 1
        }

        @Override
        public float x() {
            return x;
        }

        @Override
        public float y() {
            return y;
        }

        @Override
        public float z() {
            return z;
        }

        @Override
        public float r() {
            return ((rgba >> 16) & 0xFF) / 255f;
        }

        @Override
        public float g() {
            return ((rgba >> 8) & 0xFF) / 255f;
        }

        @Override
        public float b() {
            return (rgba & 0xFF) / 255f;
        }

        @Override
        public float alpha() {
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
        public float size() {
            return size;
        }

        @Override
        public boolean isFixed() {
            return fixed;
        }

        @Override
        public <T extends LayoutData> T getLayoutData() {
            return (T) layoutData;
        }

        @Override
        public TextProperties getTextProperties() {
            return textProperties;
        }

        protected void setTextProperties(TextPropertiesImpl textProperties) {
            this.textProperties.rgba = textProperties.rgba;
            this.textProperties.size = textProperties.size;
            this.textProperties.text = textProperties.text;
            this.textProperties.visible = textProperties.visible;
        }

        @Override
        public void setX(float x) {
            this.x = x;
        }

        @Override
        public void setY(float y) {
            this.y = y;
        }

        @Override
        public void setZ(float z) {
            this.z = z;
        }

        @Override
        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void setPosition(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void setR(float r) {
            rgba = (rgba & 0xFF00FFFF) | (((int) (r * 255f)) << 16);
        }

        @Override
        public void setG(float g) {
            rgba = (rgba & 0xFFFF00FF) | ((int) (g * 255f)) << 8;
        }

        @Override
        public void setB(float b) {
            rgba = (rgba & 0xFFFFFF00) | ((int) (b * 255f));
        }

        @Override
        public void setAlpha(float a) {
            rgba = (rgba & 0xFFFFFF) | ((int) (a * 255f)) << 24;
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
        public void setFixed(boolean fixed) {
            this.fixed = fixed;
        }

        @Override
        public void setLayoutData(LayoutData layoutData) {
            this.layoutData = layoutData;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + Float.floatToIntBits(this.x);
            hash = 53 * hash + Float.floatToIntBits(this.y);
            hash = 53 * hash + Float.floatToIntBits(this.z);
            hash = 53 * hash + this.rgba;
            hash = 53 * hash + Float.floatToIntBits(this.size);
            hash = 53 * hash + (this.fixed ? 1 : 0);
            hash = 53 * hash + (this.layoutData != null ? this.layoutData.hashCode() : 0);
            hash = 53 * hash + (this.textProperties != null ? this.textProperties.hashCode() : 0);
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
            final NodePropertiesImpl other = (NodePropertiesImpl) obj;
            if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
                return false;
            }
            if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) {
                return false;
            }
            if (Float.floatToIntBits(this.z) != Float.floatToIntBits(other.z)) {
                return false;
            }
            if (this.rgba != other.rgba) {
                return false;
            }
            if (Float.floatToIntBits(this.size) != Float.floatToIntBits(other.size)) {
                return false;
            }
            if (this.fixed != other.fixed) {
                return false;
            }
            if (this.layoutData != other.layoutData && (this.layoutData == null || !this.layoutData.equals(other.layoutData))) {
                return false;
            }
            if (this.textProperties != other.textProperties && (this.textProperties == null || !this.textProperties.equals(other.textProperties))) {
                return false;
            }
            return true;
        }
    }
}
