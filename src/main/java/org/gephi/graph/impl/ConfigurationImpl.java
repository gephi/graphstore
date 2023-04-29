package org.gephi.graph.impl;

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.TimeRepresentation;

public class ConfigurationImpl {

    // Node Id Type (default String)
    private final Class nodeIdType;
    // Edge Id Type (default String)
    private final Class edgeIdType;
    // Edge Label Type (default String)
    private final Class edgeLabelType;
    // Edge Weight Type (default Double)
    private final Class edgeWeightType;
    // Time representation (default Timestamp)
    private final TimeRepresentation timeRepresentation;
    // Use edge weight column, or just double (default True)
    private final boolean edgeWeightColumn;
    // Automatically use read/write locks when iterating/writing graph elements
    // (default True)
    private final boolean enableAutoLocking;
    // Automatically register edge types when adding elements (default True)
    private final boolean enableAutoEdgeTypeRegistration;
    // Enable reverse index for node attributes (default True)
    private final boolean enableIndexNodes;
    // Enable reverse index for edge attributes (default True)
    private final boolean enableIndexEdges;
    // Enable reverse index for timestamps (default True)
    private final boolean enableIndexTimestamps;
    // Enable observers (default True)
    private final boolean enableObservers;
    // Node properties are X, Y, Color etc. (default True)
    private final boolean enableNodeProperties;
    // Edge properties are Color, etc. (default True)
    private final boolean enableEdgeProperties;
    // Enable spatial index (default False)
    private final boolean enableSpatialIndex;
    // Enable parallel edges of the same type (default True)
    private final boolean enableParallelEdgesSameType;

    public ConfigurationImpl() {
        nodeIdType = GraphStoreConfiguration.DEFAULT_NODE_ID_TYPE;
        edgeIdType = GraphStoreConfiguration.DEFAULT_EDGE_ID_TYPE;
        edgeLabelType = GraphStoreConfiguration.DEFAULT_EDGE_LABEL_TYPE;
        edgeWeightType = GraphStoreConfiguration.DEFAULT_EDGE_WEIGHT_TYPE;
        timeRepresentation = GraphStoreConfiguration.DEFAULT_TIME_REPRESENTATION;
        edgeWeightColumn = GraphStoreConfiguration.DEFAULT_ENABLE_EDGE_WEIGHT_COLUMN;
        enableAutoLocking = GraphStoreConfiguration.DEFAULT_ENABLE_AUTO_LOCKING;
        enableAutoEdgeTypeRegistration = GraphStoreConfiguration.DEFAULT_ENABLE_AUTO_EDGE_TYPE_REGISTRATION;
        enableIndexNodes = GraphStoreConfiguration.DEFAULT_ENABLE_INDEX_NODES;
        enableIndexEdges = GraphStoreConfiguration.DEFAULT_ENABLE_INDEX_EDGES;
        enableIndexTimestamps = GraphStoreConfiguration.DEFAULT_ENABLE_INDEX_TIMESTAMP;
        enableObservers = GraphStoreConfiguration.DEFAULT_ENABLE_OBSERVERS;
        enableNodeProperties = GraphStoreConfiguration.DEFAULT_ENABLE_NODE_PROPERTIES;
        enableEdgeProperties = GraphStoreConfiguration.DEFAULT_ENABLE_EDGE_PROPERTIES;
        enableSpatialIndex = GraphStoreConfiguration.DEFAULT_ENABLE_SPATIAL_INDEX;
        enableParallelEdgesSameType = GraphStoreConfiguration.DEFAULT_ENABLE_PARALLEL_EDGES_SAME_TYPE;
    }

    public ConfigurationImpl(Configuration configuration) {
        nodeIdType = configuration.getNodeIdType();
        edgeIdType = configuration.getEdgeIdType();
        edgeLabelType = configuration.getEdgeLabelType();
        edgeWeightType = configuration.getEdgeWeightType();
        timeRepresentation = configuration.getTimeRepresentation();
        edgeWeightColumn = configuration.getEdgeWeightColumn();
        enableAutoLocking = configuration.isEnableAutoLocking();
        enableAutoEdgeTypeRegistration = configuration.isEnableAutoEdgeTypeRegistration();
        enableIndexNodes = configuration.isEnableIndexNodes();
        enableIndexEdges = configuration.isEnableIndexEdges();
        enableIndexTimestamps = configuration.isEnableIndexTimestamps();
        enableObservers = configuration.isEnableObservers();
        enableNodeProperties = configuration.isEnableNodeProperties();
        enableEdgeProperties = configuration.isEnableEdgeProperties();
        enableSpatialIndex = configuration.isEnableSpatialIndex();
        enableParallelEdgesSameType = configuration.isEnableParallelEdgesSameType();
    }

    public Configuration toConfiguration() {
        return new ConfigurationProxy(this);
    }

    public Class getNodeIdType() {
        return nodeIdType;
    }

    public Class getEdgeIdType() {
        return edgeIdType;
    }

    public Class getEdgeLabelType() {
        return edgeLabelType;
    }

    public Class getEdgeWeightType() {
        return edgeWeightType;
    }

    public TimeRepresentation getTimeRepresentation() {
        return timeRepresentation;
    }

    public boolean isEdgeWeightColumn() {
        return edgeWeightColumn;
    }

    public boolean isEnableAutoLocking() {
        return enableAutoLocking;
    }

    public boolean isEnableAutoEdgeTypeRegistration() {
        return enableAutoEdgeTypeRegistration;
    }

    public boolean isEnableIndexNodes() {
        return enableIndexNodes;
    }

    public boolean isEnableIndexEdges() {
        return enableIndexEdges;
    }

    public boolean isEnableIndexTimestamps() {
        return enableIndexTimestamps;
    }

    public boolean isEnableObservers() {
        return enableObservers;
    }

    public boolean isEnableNodeProperties() {
        return enableNodeProperties;
    }

    public boolean isEnableEdgeProperties() {
        return enableEdgeProperties;
    }

    public boolean isEnableSpatialIndex() {
        return enableSpatialIndex;
    }

    public boolean isEnableParallelEdgesSameType() {
        return enableParallelEdgesSameType;
    }

    // Used to return a Configuration instance
    private static class ConfigurationProxy extends Configuration {

        private ConfigurationProxy(ConfigurationImpl impl) {
            super(impl);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigurationImpl)) {
            return false;
        }

        ConfigurationImpl that = (ConfigurationImpl) o;

        if (isEdgeWeightColumn() != that.isEdgeWeightColumn()) {
            return false;
        }
        if (isEnableAutoLocking() != that.isEnableAutoLocking()) {
            return false;
        }
        if (isEnableAutoEdgeTypeRegistration() != that.isEnableAutoEdgeTypeRegistration()) {
            return false;
        }
        if (isEnableIndexNodes() != that.isEnableIndexNodes()) {
            return false;
        }
        if (isEnableIndexEdges() != that.isEnableIndexEdges()) {
            return false;
        }
        if (isEnableIndexTimestamps() != that.isEnableIndexTimestamps()) {
            return false;
        }
        if (isEnableObservers() != that.isEnableObservers()) {
            return false;
        }
        if (isEnableNodeProperties() != that.isEnableNodeProperties()) {
            return false;
        }
        if (isEnableEdgeProperties() != that.isEnableEdgeProperties()) {
            return false;
        }
        if (isEnableSpatialIndex() != that.isEnableSpatialIndex()) {
            return false;
        }
        if (isEnableParallelEdgesSameType() != that.isEnableParallelEdgesSameType()) {
            return false;
        }
        if (!getNodeIdType().equals(that.getNodeIdType())) {
            return false;
        }
        if (!getEdgeIdType().equals(that.getEdgeIdType())) {
            return false;
        }
        if (!getEdgeLabelType().equals(that.getEdgeLabelType())) {
            return false;
        }
        if (!getEdgeWeightType().equals(that.getEdgeWeightType())) {
            return false;
        }
        return getTimeRepresentation() == that.getTimeRepresentation();
    }

    @Override
    public int hashCode() {
        int result = getNodeIdType().hashCode();
        result = 31 * result + getEdgeIdType().hashCode();
        result = 31 * result + getEdgeLabelType().hashCode();
        result = 31 * result + getEdgeWeightType().hashCode();
        result = 31 * result + getTimeRepresentation().hashCode();
        result = 31 * result + (isEdgeWeightColumn() ? 1 : 0);
        result = 31 * result + (isEnableAutoLocking() ? 1 : 0);
        result = 31 * result + (isEnableAutoEdgeTypeRegistration() ? 1 : 0);
        result = 31 * result + (isEnableIndexNodes() ? 1 : 0);
        result = 31 * result + (isEnableIndexEdges() ? 1 : 0);
        result = 31 * result + (isEnableIndexTimestamps() ? 1 : 0);
        result = 31 * result + (isEnableObservers() ? 1 : 0);
        result = 31 * result + (isEnableNodeProperties() ? 1 : 0);
        result = 31 * result + (isEnableEdgeProperties() ? 1 : 0);
        result = 31 * result + (isEnableSpatialIndex() ? 1 : 0);
        result = 31 * result + (isEnableParallelEdgesSameType() ? 1 : 0);
        return result;
    }
}
