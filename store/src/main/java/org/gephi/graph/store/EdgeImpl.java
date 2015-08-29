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
import org.gephi.attribute.time.Estimator;
import org.gephi.attribute.time.Interval;
import org.gephi.attribute.time.TimestampDoubleSet;
import org.gephi.attribute.time.TimestampValueSet;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeProperties;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.TextProperties;

/**
 *
 * @author mbastian
 */
public class EdgeImpl extends ElementImpl implements Edge {

    //Const
    protected static final byte DIRECTED_BYTE = 1;
    protected static final byte MUTUAL_BYTE = 1 << 1;
    //Final Data
    protected final NodeImpl source;
    protected final NodeImpl target;
    protected final int type;
    //Pointers
    protected int storeId = EdgeStore.NULL_ID;
    protected int nextOutEdge = EdgeStore.NULL_ID;
    protected int nextInEdge = EdgeStore.NULL_ID;
    protected int previousOutEdge = EdgeStore.NULL_ID;
    protected int previousInEdge = EdgeStore.NULL_ID;
    //Flags
    protected byte flags;
    //Props
    protected final EdgePropertiesImpl properties;

    public EdgeImpl(Object id, GraphStore graphStore, NodeImpl source, NodeImpl target, int type, double weight, boolean directed) {
        super(id, graphStore);
        this.source = source;
        this.target = target;
        this.flags = (byte) (directed ? 1 : 0);
        this.type = type;
        this.properties = GraphStoreConfiguration.ENABLE_EDGE_PROPERTIES ? new EdgePropertiesImpl() : null;
        this.attributes = new Object[GraphStoreConfiguration.EDGE_WEIGHT_INDEX + 1];
        this.attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX] = id;
        this.attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX] = weight;
    }

    public EdgeImpl(Object id, NodeImpl source, NodeImpl target, int type, double weight, boolean directed) {
        this(id, null, source, target, type, weight, directed);
    }

    @Override
    public NodeImpl getSource() {
        return source;
    }

    @Override
    public NodeImpl getTarget() {
        return target;
    }

    @Override
    public double getWeight() {
        synchronized (this) {
            Object weightObject = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
            if (weightObject instanceof Double) {
                return (Double) weightObject;
            }
            throw new IllegalStateException("The weight is dynamic, call getWeight(timestamp) instead");
        }
    }

    @Override
    public boolean hasDynamicWeight() {
        synchronized (this) {
            return !(attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX] instanceof Double);
        }
    }

    @Override
    public void setWeight(double weight, double timestamp) {
        synchronized (this) {
            final TimestampMap timestampMap = getColumnStore().getTimestampMap(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
            if (timestampMap != null) {
                Object oldValue = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
                TimestampDoubleSet dynamicValue;
                if (!(oldValue instanceof TimestampValueSet)) {
                    dynamicValue = new TimestampDoubleSet();
                    attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX] = dynamicValue;
                    timestampMap.clear();
                } else {
                    dynamicValue = (TimestampDoubleSet) oldValue;
                }
                int timestampIndex = timestampMap.getTimestampIndex(timestamp);
                dynamicValue.put(timestampIndex, weight);
            }
        }
    }

    @Override
    public double getWeight(double timestamp) {
        synchronized (this) {
            final TimestampMap timestampMap = getColumnStore().getTimestampMap(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
            if (timestampMap != null) {
                Object weightValue = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
                if (weightValue instanceof Double) {
                    throw new IllegalStateException("The weight is static, call getWeight() instead");
                }
                TimestampDoubleSet dynamicValue = (TimestampDoubleSet) weightValue;
                int timestampIndex = timestampMap.getTimestampIndex(timestamp);
                return dynamicValue.getDouble(timestampIndex, 0.0);
            }
        }
        return 0.0;
    }

    @Override
    public double getWeight(GraphView view) {
        synchronized (this) {
            Object value = attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
            if (value instanceof TimestampDoubleSet) {
                Interval interval = view.getTimeInterval();
                checkEnabledTimestampSet();
                checkViewExist((GraphView) view);
                final ColumnStore columnStore = getColumnStore();
                final TimestampMap timestampMap = columnStore.getTimestampMap(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
                if (timestampMap != null) {
                    TimestampDoubleSet dynamicValue = (TimestampDoubleSet) attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX];
                    int[] timestampIndices = timestampMap.getTimestampIndices(interval);
                    Estimator estimator = columnStore.getEstimator(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
                    if (estimator == null) {
                        estimator = Estimator.FIRST;
                    }
                    return (Double) dynamicValue.get(null, timestampIndices, estimator);
                } else {
                    throw new RuntimeException("The timestamp store is not available");
                }
            } else {
                return (Double) value;
            }
        }
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Object getTypeLabel() {
        graphStore.autoReadLock();
        try {
            return graphStore.edgeTypeStore.getLabel(type);
        } finally {
            graphStore.autoReadUnlock();
        }
    }

    @Override
    public void setWeight(double weight) {
        attributes[GraphStoreConfiguration.EDGE_WEIGHT_INDEX] = weight;
    }

    public int getNextOutEdge() {
        return nextOutEdge;
    }

    public int getNextInEdge() {
        return nextInEdge;
    }

    public int getPreviousOutEdge() {
        return previousOutEdge;
    }

    public int getPreviousInEdge() {
        return previousInEdge;
    }

    @Override
    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int id) {
        this.storeId = id;
    }

    public long getLongId() {
        return EdgeStore.getLongId(source, target, isDirected());
    }

    @Override
    public boolean isDirected() {
        return (flags & DIRECTED_BYTE) == 1;
    }

    protected void setMutual(boolean mutual) {
        if (isDirected()) {
            if (mutual) {
                flags |= MUTUAL_BYTE;
            } else {
                flags &= ~MUTUAL_BYTE;
            }
        }
    }

    protected boolean isMutual() {
        return (flags & MUTUAL_BYTE) == MUTUAL_BYTE;
    }

    @Override
    public boolean isSelfLoop() {
        return source == target;
    }

    @Override
    ColumnStore getColumnStore() {
        if (graphStore != null) {
            return graphStore.edgeColumnStore;
        }
        return null;
    }

    @Override
    TimestampMap getTimestampMap() {
        if (graphStore != null) {
            return graphStore.timestampStore.edgeMap;
        }
        return null;
    }

    @Override
    TimestampIndexStore<Edge> getTimestampIndexStore() {
        if (graphStore != null) {
            return graphStore.timestampStore.edgeIndexStore;
        }
        return null;
    }

    @Override
    boolean isValid() {
        return storeId != EdgeStore.NULL_ID;
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
    public TextProperties getTextProperties() {
        return properties.getTextProperties();
    }

    protected void setEdgeProperties(EdgePropertiesImpl edgeProperties) {
        properties.rgba = edgeProperties.rgba;
        if (properties.textProperties != null) {
            properties.setTextProperties(edgeProperties.textProperties);
        }
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

    protected static class EdgePropertiesImpl implements EdgeProperties {

        protected final TextPropertiesImpl textProperties;
        protected int rgba;

        public EdgePropertiesImpl() {
            textProperties = new TextPropertiesImpl();
            this.rgba = 255 << 24;  //Alpha set to 1
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
        public Color getColor() {
            return new Color(rgba, true);
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
            rgba = (color.getAlpha() << 24) | color.getRGB();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + this.rgba;
            hash = 29 * hash + (this.textProperties != null ? this.textProperties.hashCode() : 0);
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
            final EdgePropertiesImpl other = (EdgePropertiesImpl) obj;
            if (this.rgba != other.rgba) {
                return false;
            }
            if (this.textProperties != other.textProperties && (this.textProperties == null || !this.textProperties.equals(other.textProperties))) {
                return false;
            }
            return true;
        }
    }
}
