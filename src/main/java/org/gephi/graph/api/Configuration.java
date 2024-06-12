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
import org.gephi.graph.impl.ConfigurationImpl;

/**
 * Global configuration set at initialization.
 * <p>
 * This class can be passed as a parameter to
 * {@link GraphModel.Factory#newInstance(org.gephi.graph.api.Configuration)} to
 * create a <em>GraphModel</em> with custom configuration.
 * <p>
 * Create instances by using the builder:
 *
 * <pre>
 * Configuration config = Configuration.builder().build();
 * </pre>
 * <p>
 * Note that setting configurations after the <em>GraphModel</em> has been
 * created won't have any effect.
 * <p>
 * By default, both node and edge id types are <code>String.class</code> and the
 * time representation is <code>TIMESTAMP</code>.
 * <p>
 * See the builder documentation for more information on default values.
 *
 * @see GraphModel
 * @see Builder
 */
public class Configuration {

    private ConfigurationImpl delegate;

    /**
     * Default constructor.
     *
     * @deprecated Use the <code>builder()</code> method instead.
     */
    @Deprecated
    public Configuration() {
        this.delegate = new ConfigurationImpl();
    }

    protected Configuration(ConfigurationImpl delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Configuration builder.
     * <p>
     *
     * Note that this class is not thread-safe.
     */
    public static class Builder {

        private ConfigurationImpl configuration;

        private Builder() {
            configuration = new ConfigurationImpl();
        }

        private Builder(ConfigurationImpl configuration) {
            this.configuration = configuration;
        }

        /**
         * Builds the configuration.
         *
         * @return the configuration
         */
        public Configuration build() {
            // Check for potential inconsistencies
            if (!configuration.isEnableNodeProperties() && configuration.isEnableSpatialIndex()) {
                throw new IllegalStateException("Spatial index can't be enabled if node properties are disabled");
            }

            return new Configuration(configuration);
        }

        /**
         * Sets the node id type.
         * <p>
         * Only simple types such as primitives, wrappers and String are supported.
         * <p>
         * Default is <code>String.class</code>.
         *
         * @param nodeIdType node id type
         * @return this builder
         * @throws IllegalArgumentException if the type isn't supported
         */
        public Builder nodeIdType(final Class nodeIdType) {
            checkSimpleType(nodeIdType);
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public Class getNodeIdType() {
                    return nodeIdType;
                }
            });
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
         * @return this builder
         * @throws IllegalArgumentException if the type isn't supported
         */
        public Builder edgeIdType(final Class edgeIdType) {
            checkSimpleType(edgeIdType);
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public Class getEdgeIdType() {
                    return edgeIdType;
                }
            });
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
         * @return this builder
         * @throws IllegalArgumentException if the type isn't supported
         */
        public Builder edgeLabelType(final Class edgeLabelType) {
            checkSimpleType(edgeLabelType);
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public Class getEdgeLabelType() {
                    return edgeLabelType;
                }
            });
            return this;
        }

        /**
         * Sets the edge weight type.
         * <p>
         * <code>Double</code>, <code>IntervalDoubleMap</code> and
         * <code>TimestampDoubleMap</code> are supported.
         * <p>
         * Default is <code>Double.class</code>.
         *
         * @param edgeWeightType edge weight type
         * @return this builder
         * @throws IllegalArgumentException if the type isn't supported
         */
        public Builder edgeWeightType(final Class edgeWeightType) {
            if (!(Double.class.equals(edgeWeightType) || TimestampDoubleMap.class
                    .equals(edgeWeightType) || IntervalDoubleMap.class.equals(edgeWeightType))) {
                throw new IllegalArgumentException("Unsupported type " + edgeWeightType
                        .getCanonicalName() + ", should be Double, IntervalDoubleMap or TimestampDoubleMap");
            }
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public Class getEdgeWeightType() {
                    return edgeWeightType;
                }
            });
            return this;
        }

        /**
         * Sets the time representation.
         * <p>
         * Default is <code>TIMESTAMP</code>.
         *
         * @param timeRepresentation time representation
         * @return this builder
         */
        public Builder timeRepresentation(final TimeRepresentation timeRepresentation) {
            if (timeRepresentation == null) {
                throw new IllegalArgumentException("timeRepresentation cannot be null");
            }
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public TimeRepresentation getTimeRepresentation() {
                    return timeRepresentation;
                }
            });
            return this;
        }

        /**
         * Sets whether to create an edge weight column.
         * <p>
         * Default is <code>true</code>.
         *
         * @param edgeWeightColumn edge weight column
         * @return this builder
         */
        public Builder edgeWeightColumn(final boolean edgeWeightColumn) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public Boolean getEdgeWeightColumn() {
                    return edgeWeightColumn;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable observers on tables and columns.
         * <p>
         * Default is <code>true</code>.
         *
         * @param enableObservers enable observers
         * @return this builder
         */
        public Builder enableObservers(final boolean enableObservers) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableObservers() {
                    return enableObservers;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable auto edge type registration.
         * <p>
         * If enabled, edge types are automatically registered when edges are added. If
         * disabled, one needs to call {@link GraphModel#addEdgeType(Object)} explicitly
         * for each type.
         * <p>
         * Default is <code>true</code>.
         *
         * @param enableAutoEdgeTypeRegistration enable auto edge type registration
         * @return this builder
         */
        public Builder enableAutoEdgeTypeRegistration(final boolean enableAutoEdgeTypeRegistration) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableAutoEdgeTypeRegistration() {
                    return enableAutoEdgeTypeRegistration;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable node properties.
         * <p>
         * If enabled, {@link NodeProperties} are created for each node. If those
         * properties aren't needed, disabling them can save memory.
         * <p>
         * Default is <code>true</code>.
         *
         * @param enableNodeProperties enable node properties
         * @return this builder
         */
        public Builder enableNodeProperties(final boolean enableNodeProperties) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableNodeProperties() {
                    return enableNodeProperties;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable edge properties.
         * <p>
         * If enabled, {@link EdgeProperties} are created for each edge. If those
         * properties aren't needed, disabling them can save memory.
         * <p>
         * Default is <code>true</code>.
         *
         * @param enableEdgeProperties enable edge properties
         * @return this builder
         */
        public Builder enableEdgeProperties(final boolean enableEdgeProperties) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableEdgeProperties() {
                    return enableEdgeProperties;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable the {@link SpatialIndex}.
         * <p>
         * If enabled, the spatial index is updated while node positions are updated. If
         * unused, disabling it is recommended as it adds some overhead.
         * <p>
         * The spatial index can be retrieved from {@link GraphModel#getSpatialIndex()}.
         * <p>
         * Default is <code>false</code>.
         *
         * @param enableSpatialIndex enable edge properties
         * @return this builder
         */
        public Builder enableSpatialIndex(final boolean enableSpatialIndex) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableSpatialIndex() {
                    return enableSpatialIndex;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable the reverse indexing of node attributes.
         * <p>
         * If enabled, the reverse index is updated while node attributes are updated.
         * This powers {@link GraphModel#getNodeIndex()} but has a negative impact on
         * memory usage (as any reverse index does). When disabled, the features of
         * {@link Index<Node>} are still available but need to iterate over all nodes to
         * return results.
         * <p>
         * Default is <code>true</code>.
         *
         * @param enableIndexNodes enable node attribute indexing
         * @return this builder
         */
        public Builder enableIndexNodes(final boolean enableIndexNodes) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableIndexNodes() {
                    return enableIndexNodes;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable the reverse indexing of edge attributes.
         * <p>
         * If enabled, the reverse index is updated while node attributes are updated.
         * This powers {@link GraphModel#getEdgeIndex()} but has a negative impact on
         * memory usage (as any reverse index does). When disabled, the features of
         * {@link Index<Edge>} are still available but need to iterate over all nodes to
         * return results.
         * <p>
         * Default is <code>true</code>.
         *
         * @param enableIndexEdges enable edge attribute indexing
         * @return this builder
         */
        public Builder enableIndexEdges(final boolean enableIndexEdges) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableIndexEdges() {
                    return enableIndexEdges;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable the reverse indexing of timestamps and intervals.
         * <p>
         * If enabled, the reverse index is updated while element's time set is updated.
         * This powers {@link GraphModel#getNodeTimeIndex()} and
         * {@link GraphModel#getEdgeTimeIndex()} ()} but has a negative impact on memory
         * usage (as any reverse index does).
         * <p>
         * Default is <code>true</code>.
         *
         * @param enableIndexTime enable time indexing
         * @return this builder
         */
        public Builder enableIndexTime(final boolean enableIndexTime) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableIndexTime() {
                    return enableIndexTime;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable multiple edges of the same type between two nodes.
         * <p>
         * If disabled, only a single edge of a given type can exist between two nodes.
         * <p>
         * Default is <code>false</code>.
         *
         * @param enableParallelEdgesSameType enable parallel edges of the same type
         * @return this builder
         */
        public Builder enableParallelEdgesSameType(final boolean enableParallelEdgesSameType) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableParallelEdgesSameType() {
                    return enableParallelEdgesSameType;
                }
            });
            return this;
        }

        /**
         * Sets whether to enable auto locking when using read/write APIs.
         * <p>
         * If disabled, the client is responsible for handling multithreading themselves
         * or calling methods such as {@link Graph#readLock()} or
         * {@link Graph#writeLock()}. If enabled, each read methods (including
         * iterators) handle locking. Similarly, each write method handle locking.
         * <p>
         * Default is <code>true</code>.
         *
         * @param enableAutoLocking enable auto locking for read/write operations
         * @see GraphLock
         * @return this builder
         */
        public Builder enableAutoLocking(final boolean enableAutoLocking) {
            this.configuration = new ConfigurationImpl(new Configuration(this.configuration) {
                @Override
                public boolean isEnableAutoLocking() {
                    return enableAutoLocking;
                }
            });
            return this;
        }

        private static void checkSimpleType(Class type) {
            if (!AttributeUtils.isSimpleType(type)) {
                throw new IllegalArgumentException("Unsupported type " + type.getCanonicalName());
            }
        }
    }

    /**
     * Returns the node id type.
     *
     * @return node id type
     */
    public Class getNodeIdType() {
        return delegate.getNodeIdType();
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
        this.delegate = new Builder(this.delegate).nodeIdType(nodeIdType).configuration;
    }

    /**
     * Returns the edge id type.
     *
     * @return edge id type
     */
    public Class getEdgeIdType() {
        return delegate.getEdgeIdType();
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
        this.delegate = new Builder(this.delegate).edgeIdType(edgeIdType).configuration;
    }

    /**
     * Returns the edge label type.
     *
     * @return edge label type
     */
    public Class getEdgeLabelType() {
        return delegate.getEdgeLabelType();
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
        this.delegate = new Builder(this.delegate).edgeLabelType(edgeLabelType).configuration;
    }

    /**
     * Returns the edge weight type.
     *
     * @return edge weight type
     */
    public Class getEdgeWeightType() {
        return delegate.getEdgeWeightType();
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
        this.delegate = new Builder(this.delegate).edgeWeightType(edgeWeightType).configuration;
    }

    /**
     * Returns the time representation.
     *
     * @return time representation
     */
    public TimeRepresentation getTimeRepresentation() {
        return delegate.getTimeRepresentation();
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
        this.delegate = new Builder(this.delegate).timeRepresentation(timeRepresentation).configuration;
    }

    /**
     * Returns whether an edge weight column is created.
     *
     * @return edge weight column
     */
    public Boolean getEdgeWeightColumn() {
        return delegate.isEdgeWeightColumn();
    }

    /**
     * Sets whether to create an edge weight column.
     * <p>
     *
     * @deprecated Use {@link #builder()} instead.
     *
     * @param edgeWeightColumn edge weight column
     */
    @Deprecated
    public void setEdgeWeightColumn(Boolean edgeWeightColumn) {
        this.delegate = new Builder(this.delegate).edgeWeightColumn(edgeWeightColumn).configuration;
    }

    public boolean isEnableAutoLocking() {
        return delegate.isEnableAutoLocking();
    }

    public boolean isEnableAutoEdgeTypeRegistration() {
        return delegate.isEnableAutoEdgeTypeRegistration();
    }

    public boolean isEnableIndexNodes() {
        return delegate.isEnableIndexNodes();
    }

    public boolean isEnableIndexEdges() {
        return delegate.isEnableIndexEdges();
    }

    public boolean isEnableIndexTime() {
        return delegate.isEnableIndexTime();
    }

    public boolean isEnableObservers() {
        return delegate.isEnableObservers();
    }

    public boolean isEnableNodeProperties() {
        return delegate.isEnableNodeProperties();
    }

    public boolean isEnableEdgeProperties() {
        return delegate.isEnableEdgeProperties();
    }

    public boolean isEnableSpatialIndex() {
        return delegate.isEnableSpatialIndex();
    }

    public boolean isEnableParallelEdgesSameType() {
        return delegate.isEnableParallelEdgesSameType();
    }

    /**
     * Copy this configuration.
     *
     * @return a copy of this configuration
     */
    public Configuration copy() {
        return new Configuration(delegate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Configuration)) {
            return false;
        }

        Configuration that = (Configuration) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
