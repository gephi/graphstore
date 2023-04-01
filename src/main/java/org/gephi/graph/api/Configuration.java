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

import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.impl.GraphStoreConfiguration;

/**
 * Global configuration set at initialization.
 * <p>
 * This class can be passed as a parameter to
 * {@link GraphModel.Factory#newInstance(org.gephi.graph.api.Configuration)} to
 * create a <em>GraphModel</em> with custom configuration.
 * <p>
 * Note that setting configurations after the <em>GraphModel</em> has been
 * created won't have any effect.
 * <p>
 * By default, both node and edge id types are <code>String.class</code> and the
 * time representation is <code>TIMESTAMP</code>.
 *
 * @see GraphModel
 */
public class Configuration {

    private Class nodeIdType;
    private Class edgeIdType;
    private Class edgeLabelType;
    private Class edgeWeightType;
    private TimeRepresentation timeRepresentation;
    private Boolean edgeWeightColumn;

    /**
     * Default constructor.
     *
     * @deprecated Use the <code>builder()</code> method instead.
     */
    @Deprecated
    public Configuration() {
        nodeIdType = GraphStoreConfiguration.DEFAULT_NODE_ID_TYPE;
        edgeIdType = GraphStoreConfiguration.DEFAULT_EDGE_ID_TYPE;
        edgeLabelType = GraphStoreConfiguration.DEFAULT_EDGE_LABEL_TYPE;
        edgeWeightType = GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT_TYPE;
        timeRepresentation = GraphStoreConfiguration.DEFAULT_TIME_REPRESENTATION;
        edgeWeightColumn = true;
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Configuration configuration;

        private Builder() {
            configuration = new Configuration();
        }

        /**
         * Builds the configuration.
         *
         * @return the configuration
         */
        public Configuration build() {
            return configuration;
        }

        /**
         * Sets the node id type.
         * <p>
         * Only simple types such as primitives, wrappers and String are supported.
         * <p>
         * Default is <code>String.class</code>.
         *
         * @param nodeIdType node id type
         * @throws IllegalArgumentException if the type isn't supported
         */
        public Builder nodeIdType(Class nodeIdType) {
            configuration.setNodeIdType(nodeIdType);
            return this;
        }


        /**
         * Sets the edge id type.
         * <p>
         * Only simple types such as primitives, wrappers and String are supported.
         * <p>
         * Default is <code>String.class</code>.
         *
         * @param edgeIdType edge id type
         * @throws IllegalArgumentException if the type isn't supported
         */
        public Builder edgeIdType(Class edgeIdType) {
            configuration.setEdgeIdType(edgeIdType);
            return this;
        }

        /**
         * Sets the edge label type.
         * <p>
         * Only simple types such as primitives, wrappers and String are supported.
         * <p>
         * Default is <code>String.class</code>.
         *
         * @param edgeLabelType edge label type
         * @throws IllegalArgumentException if the type isn't supported
         */
        public Builder setEdgeLabelType(Class edgeLabelType) {
            configuration.setEdgeLabelType(edgeLabelType);
            return this;
        }

        /**
         * Sets the edge weight type.
         * <p>
         * <code>Double</code>, <code>IntervalDoubleMap</code> and <code>TimestampDoubleMap</code> are supported.
         * <p>
         * Default is <code>Double.class</code>.
         *
         * @param edgeWeightType edge weight type
         * @throws IllegalArgumentException if the type isn't supported
         */
        public Builder edgeWeightType(Class edgeWeightType) {
            configuration.setEdgeWeightType(edgeWeightType);
            return this;
        }

        /**
         * Sets the time representation.
         * <p>
         * Default is <code>TIMESTAMP</code>.
         *
         * @param timeRepresentation time representation
         */
        public Builder timeRepresentation(TimeRepresentation timeRepresentation) {
            configuration.setTimeRepresentation(timeRepresentation);
            return this;
        }

        /**
         * Sets whether to create an edge weight column.
         * <p>
         * Default is <code>true</code>.
         *
         * @param edgeWeightColumn edge weight column
         */
        public void edgeWeightColumn(Boolean edgeWeightColumn) {
            configuration.setEdgeWeightColumn(edgeWeightColumn);
        }
    }

    /**
     * Returns the node id type.
     *
     * @return node id type
     */
    public Class getNodeIdType() {
        return nodeIdType;
    }

    /**
     * Sets the node id type.
     * <p>
     * Only simple types such as primitives, wrappers and String are supported.
     *
     * @deprecated Use {@link #builder()} instead.
     *
     * @param nodeIdType node id type
     * @throws IllegalArgumentException if the type isn't supported
     */
    @Deprecated
    public void setNodeIdType(Class nodeIdType) {
        if (!AttributeUtils.isSimpleType(nodeIdType)) {
            throw new IllegalArgumentException("Unsupported type " + nodeIdType.getClass().getCanonicalName());
        }
        this.nodeIdType = nodeIdType;
    }

    /**
     * Returns the edge id type.
     *
     * @return edge id type
     */
    public Class getEdgeIdType() {
        return edgeIdType;
    }

    /**
     * Sets the edge id type.
     * <p>
     * Only simple types such as primitives, wrappers and String are supported.
     *
     * @deprecated Use {@link #builder()} instead.
     *
     * @param edgeIdType edge id type
     * @throws IllegalArgumentException if the type isn't supported
     */
    @Deprecated
    public void setEdgeIdType(Class edgeIdType) {
        if (!AttributeUtils.isSimpleType(edgeIdType)) {
            throw new IllegalArgumentException("Unsupported type " + edgeIdType.getClass().getCanonicalName());
        }
        this.edgeIdType = edgeIdType;
    }

    /**
     * Returns the edge label type.
     *
     * @return edge label type
     */
    public Class getEdgeLabelType() {
        return edgeLabelType;
    }

    /**
     * Sets the edge label type.
     *
     * @deprecated Use {@link #builder()} instead.
     *
     * @param edgeLabelType edge label type
     * @throws IllegalArgumentException if the type isn't supported
     */
    @Deprecated
    public void setEdgeLabelType(Class edgeLabelType) {
        if (!AttributeUtils.isSimpleType(edgeLabelType)) {
            throw new IllegalArgumentException("Unsupported type " + edgeLabelType.getClass().getCanonicalName());
        }
        this.edgeLabelType = edgeLabelType;
    }

    /**
     * Returns the edge weight type.
     *
     * @return edge weight type
     */
    public Class getEdgeWeightType() {
        return edgeWeightType;
    }

    /**
     * Sets the edge weight type.
     *
     * @deprecated Use {@link #builder()} instead.
     *
     * @param edgeWeightType edge weight type
     * @throws IllegalArgumentException if the type isn't supported
     */
    @Deprecated
    public void setEdgeWeightType(Class edgeWeightType) {
        if (Double.class.equals(edgeWeightType) || TimestampDoubleMap.class
                .equals(edgeWeightType) || IntervalDoubleMap.class.equals(edgeWeightType)) {
            this.edgeWeightType = edgeWeightType;
        } else {
            throw new IllegalArgumentException("Unsupported type " + edgeWeightType.getClass().getCanonicalName());
        }
    }

    /**
     * Returns the time representation.
     *
     * @return time representation
     */
    public TimeRepresentation getTimeRepresentation() {
        return timeRepresentation;
    }

    /**
     * Sets the time representation.
     *
     * @deprecated Use {@link #builder()} instead.
     *
     * @param timeRepresentation time representation
     */
    @Deprecated
    public void setTimeRepresentation(TimeRepresentation timeRepresentation) {
        if (timeRepresentation == null) {
            throw new IllegalArgumentException("timeRepresentation cannot be null");
        }
        this.timeRepresentation = timeRepresentation;
    }

    /**
     * Returns whether an edge weight column is created.
     *
     * @return edge weight column
     */
    public Boolean getEdgeWeightColumn() {
        return edgeWeightColumn;
    }

    /**
     * Sets whether to create an edge weight column.
     * <p>
     * @deprecated Use {@link #builder()} instead.
     *
     * @param edgeWeightColumn edge weight column
     */
    @Deprecated
    public void setEdgeWeightColumn(Boolean edgeWeightColumn) {
        this.edgeWeightColumn = edgeWeightColumn;
    }

    /**
     * Copy this configuration.
     *
     * @return a copy of this configuration
     */
    public Configuration copy() {
        Configuration copy = new Configuration();
        copy.nodeIdType = nodeIdType;
        copy.edgeIdType = edgeIdType;
        copy.edgeLabelType = edgeLabelType;
        copy.edgeWeightType = edgeWeightType;
        copy.timeRepresentation = timeRepresentation;
        copy.edgeWeightColumn = edgeWeightColumn;
        return copy;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.nodeIdType != null ? this.nodeIdType.hashCode() : 0);
        hash = 19 * hash + (this.edgeIdType != null ? this.edgeIdType.hashCode() : 0);
        hash = 19 * hash + (this.edgeLabelType != null ? this.edgeLabelType.hashCode() : 0);
        hash = 19 * hash + (this.edgeWeightType != null ? this.edgeWeightType.hashCode() : 0);
        hash = 19 * hash + (this.timeRepresentation != null ? this.timeRepresentation.hashCode() : 0);
        hash = 19 * hash + (this.edgeWeightColumn != null ? this.edgeWeightColumn.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Configuration other = (Configuration) obj;
        if (this.nodeIdType != other.nodeIdType && (this.nodeIdType == null || !this.nodeIdType
                .equals(other.nodeIdType))) {
            return false;
        }
        if (this.edgeIdType != other.edgeIdType && (this.edgeIdType == null || !this.edgeIdType
                .equals(other.edgeIdType))) {
            return false;
        }
        if (this.edgeLabelType != other.edgeLabelType && (this.edgeLabelType == null || !this.edgeLabelType
                .equals(other.edgeLabelType))) {
            return false;
        }
        if (this.edgeWeightType != other.edgeWeightType && (this.edgeWeightType == null || !this.edgeWeightType
                .equals(other.edgeWeightType))) {
            return false;
        }
        if (this.timeRepresentation != other.timeRepresentation && (this.timeRepresentation == null || !this.timeRepresentation
                .equals(other.timeRepresentation))) {
            return false;
        }
        if (this.edgeWeightColumn != other.edgeWeightColumn && (this.edgeWeightColumn == null || !this.edgeWeightColumn
                .equals(other.edgeWeightColumn))) {
            return false;
        }
        return true;
    }
}
