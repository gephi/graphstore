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

import cern.colt.bitvector.BitVector;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.gephi.attribute.api.Origin;
import org.gephi.attribute.time.Estimator;
import org.gephi.attribute.api.TimeFormat;
import org.gephi.attribute.time.TimestampBooleanSet;
import org.gephi.attribute.time.TimestampByteSet;
import org.gephi.attribute.time.TimestampCharSet;
import org.gephi.attribute.time.TimestampDoubleSet;
import org.gephi.attribute.time.TimestampFloatSet;
import org.gephi.attribute.time.TimestampIntegerSet;
import org.gephi.attribute.time.TimestampLongSet;
import org.gephi.attribute.time.TimestampSet;
import org.gephi.attribute.time.TimestampShortSet;
import org.gephi.attribute.time.TimestampStringSet;
import org.gephi.attribute.time.TimestampValueSet;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.store.EdgeImpl.EdgePropertiesImpl;
import org.gephi.graph.store.NodeImpl.NodePropertiesImpl;
import org.gephi.graph.utils.DataInputOutput;
import org.gephi.graph.utils.LongPacker;

/**
 * Greatly inspired from JDBM https://github.com/jankotek/JDBM3
 *
 * @author mbastian
 */
public class Serialization {

    final static int NULL_ID = -1;
    final static int NULL = 0;
    final static int NORMAL = 1;
    final static int BOOLEAN_TRUE = 2;
    final static int BOOLEAN_FALSE = 3;
    final static int INTEGER_MINUS_1 = 4;
    final static int INTEGER_0 = 5;
    final static int INTEGER_1 = 6;
    final static int INTEGER_2 = 7;
    final static int INTEGER_3 = 8;
    final static int INTEGER_4 = 9;
    final static int INTEGER_5 = 10;
    final static int INTEGER_6 = 11;
    final static int INTEGER_7 = 12;
    final static int INTEGER_8 = 13;
    final static int INTEGER_255 = 14;
    final static int INTEGER_PACK_NEG = 15;
    final static int INTEGER_PACK = 16;
    final static int LONG_MINUS_1 = 17;
    final static int LONG_0 = 18;
    final static int LONG_1 = 19;
    final static int LONG_2 = 20;
    final static int LONG_3 = 21;
    final static int LONG_4 = 22;
    final static int LONG_5 = 23;
    final static int LONG_6 = 24;
    final static int LONG_7 = 25;
    final static int LONG_8 = 26;
    final static int LONG_PACK_NEG = 27;
    final static int LONG_PACK = 28;
    final static int LONG_255 = 29;
    final static int LONG_MINUS_MAX = 30;
    final static int SHORT_MINUS_1 = 31;
    final static int SHORT_0 = 32;
    final static int SHORT_1 = 33;
    final static int SHORT_255 = 34;
    final static int SHORT_FULL = 35;
    final static int BYTE_MINUS_1 = 36;
    final static int BYTE_0 = 37;
    final static int BYTE_1 = 38;
    final static int BYTE_FULL = 39;
    final static int CHAR = 40;
    final static int FLOAT_MINUS_1 = 41;
    final static int FLOAT_0 = 42;
    final static int FLOAT_1 = 43;
    final static int FLOAT_255 = 44;
    final static int FLOAT_SHORT = 45;
    final static int FLOAT_FULL = 46;
    final static int DOUBLE_MINUS_1 = 47;
    final static int DOUBLE_0 = 48;
    final static int DOUBLE_1 = 49;
    final static int DOUBLE_255 = 50;
    final static int DOUBLE_SHORT = 51;
    final static int DOUBLE_FULL = 52;
    final static int DOUBLE_ARRAY = 53;
    final static int BIGDECIMAL = 54;
    final static int BIGINTEGER = 55;
    final static int FLOAT_ARRAY = 56;
    final static int INTEGER_MINUS_MAX = 57;
    final static int SHORT_ARRAY = 58;
    final static int BOOLEAN_ARRAY = 59;
    final static int ARRAY_INT_B_255 = 60;
    final static int ARRAY_INT_B_INT = 61;
    final static int ARRAY_INT_S = 62;
    final static int ARRAY_INT_I = 63;
    final static int ARRAY_INT_PACKED = 64;
    final static int ARRAY_LONG_B = 65;
    final static int ARRAY_LONG_S = 66;
    final static int ARRAY_LONG_I = 67;
    final static int ARRAY_LONG_L = 68;
    final static int ARRAY_LONG_PACKED = 69;
    final static int CHAR_ARRAY = 70;
    final static int ARRAY_BYTE_INT = 71;
    final static int NOTUSED_ARRAY_OBJECT_255 = 72;
    final static int ARRAY_OBJECT = 73;
    final static int STRING_EMPTY = 101;
    final static int NOTUSED_STRING_255 = 102;
    final static int STRING = 103;
    final static int LOCALE = 124;
    final static int PROPERTIES = 125;
    final static int CLASS = 126;
    final static int DATE = 127;
    final static String EMPTY_STRING = "";
    //Specifics
    final static int NODE = 200;
    final static int EDGE = 201;
    final static int TIMESTAMP_SET = 202;
    final static int EDGETYPE_STORE = 203;
    final static int COLUMN_ORIGIN = 204;
    final static int COLUMN = 205;
    final static int COLUMN_STORE = 206;
    final static int GRAPH_STORE = 207;
    final static int GRAPH_FACTORY = 208;
    final static int GRAPH_VIEW_STORE = 209;
    final static int GRAPH_VIEW = 210;
    final static int BIT_VECTOR = 211;
    final static int GRAPH_STORE_CONFIGURATION = 212;
    final static int GRAPH_VERSION = 213;
    final static int NODE_PROPERTIES = 214;
    final static int EDGE_PROPERTIES = 215;
    final static int TEXT_PROPERTIES = 216;
    final static int ESTIMATOR = 217;
    final static int GRAPH_ATTRIBUTES = 218;
    final static int TIMESTAMP_BOOLEAN_SET = 219;
    final static int TIMESTAMP_BYTE_SET = 220;
    final static int TIMESTAMP_CHAR_SET = 221;
    final static int TIMESTAMP_DOUBLE_SET = 222;
    final static int TIMESTAMP_FLOAT_SET = 223;
    final static int TIMESTAMP_INTEGER_SET = 224;
    final static int TIMESTAMP_LONG_SET = 225;
    final static int TIMESTAMP_SHORT_SET = 226;
    final static int TIMESTAMP_STRING_SET = 227;
    final static int TIMESTAMP_MAP = 228;
    final static int TIME_FORMAT = 229;
    final static int TIMESTAMP_STORE = 230;
    //Store
    protected final GraphStore store;
    protected final Int2IntMap idMap;
    //Deserialized configuration
    protected GraphStoreConfigurationVersion graphStoreConfigurationVersion;

    public Serialization(GraphStore graphStore) {
        store = graphStore;
        idMap = new Int2IntOpenHashMap();
        idMap.defaultReturnValue(NULL_ID);
    }

    public void serializeGraphStore(DataOutput out) throws IOException {
        //Configuration
        serializeGraphStoreConfiguration(out);

        //GraphVersion
        serialize(out, store.version);

        //Edge types
        EdgeTypeStore edgeTypeStore = store.edgeTypeStore;
        serialize(out, edgeTypeStore);

        //Column
        serialize(out, store.nodeColumnStore);
        serialize(out, store.edgeColumnStore);
        
        //Timestamp
        serialize(out, store.timestampStore);

        //Factory
        serialize(out, store.factory);

        //Atts
        serialize(out, store.attributes);

        //TimeFormat
        serialize(out, store.timeFormat);

        //Nodes + Edges
        int nodesAndEdges = store.nodeStore.size() + store.edgeStore.size();
        serialize(out, nodesAndEdges);

        for (Node node : store.nodeStore) {
            serialize(out, node);
        }
        for (Edge edge : store.edgeStore) {
            serialize(out, edge);
        }

        //Views
        serialize(out, store.viewStore);
    }

    public GraphStore deserializeGraphStore(DataInput is) throws IOException, ClassNotFoundException {
        if (!store.nodeStore.isEmpty()) {   //TODO test other stores
            throw new IOException("The store is not empty");
        }

        //Store Configuration
        deserialize(is);

        //Graph Version
        GraphVersion version = (GraphVersion) deserialize(is);
        store.version.nodeVersion = version.nodeVersion;
        store.version.edgeVersion = version.edgeVersion;

        //Edge types
        deserialize(is);

        //Columns
        deserialize(is);
        deserialize(is);
        
        //Timestamp
        deserialize(is);

        //Factory
        deserialize(is);

        //Atts
        GraphAttributesImpl attributes = (GraphAttributesImpl) deserialize(is);
        store.attributes.setGraphAttributes(attributes);

        //TimeFormat
        deserialize(is);

        //Nodes and edges
        int nodesAndEdges = (Integer) deserialize(is);
        for (int i = 0; i < nodesAndEdges; i++) {
            deserialize(is);
        }

        //ViewStore
        deserialize(is);

        return store;
    }

    private void serializeNode(DataOutput out, NodeImpl node) throws IOException {
        serialize(out, node.getId());
        serialize(out, node.storeId);
        serialize(out, node.attributes);
        serialize(out, node.properties);
    }

    private void serializeEdge(DataOutput out, EdgeImpl edge) throws IOException {
        serialize(out, edge.getId());
        serialize(out, edge.source.storeId);
        serialize(out, edge.target.storeId);
        serialize(out, edge.type);
        serialize(out, edge.getWeight());
        serialize(out, edge.isDirected());
        serialize(out, edge.attributes);
        serialize(out, edge.properties);
    }

    private NodeImpl deserializeNode(DataInput is) throws IOException, ClassNotFoundException {
        Object id = deserialize(is);
        int storeId = (Integer) deserialize(is);
        Object[] attributes = (Object[]) deserialize(is);
        NodePropertiesImpl properties = (NodePropertiesImpl) deserialize(is);

        NodeImpl node = (NodeImpl) store.factory.newNode(id);
        node.attributes = attributes;
        if (node.properties != null) {
            node.setNodeProperties(properties);
        }
        store.nodeStore.add(node);

        idMap.put(storeId, node.storeId);

        return node;
    }

    private EdgeImpl deserializeEdge(DataInput is) throws IOException, ClassNotFoundException {
        Object id = deserialize(is);
        int sourceId = (Integer) deserialize(is);
        int targetId = (Integer) deserialize(is);
        int type = (Integer) deserialize(is);
        double weight = (Double) deserialize(is);
        boolean directed = (Boolean) deserialize(is);
        Object[] attributes = (Object[]) deserialize(is);
        EdgePropertiesImpl properties = (EdgePropertiesImpl) deserialize(is);

        int sourceNewId = idMap.get(sourceId);
        int targetNewId = idMap.get(targetId);

        if (sourceId == NULL_ID || targetId == NULL_ID) {
            throw new IOException("The edge source of target can't be found");
        }

        NodeImpl source = store.nodeStore.get(sourceNewId);
        NodeImpl target = store.nodeStore.get(targetNewId);

        EdgeImpl edge = (EdgeImpl) store.factory.newEdge(id, source, target, type, weight, directed);
        edge.attributes = attributes;
        if (edge.properties != null) {
            edge.setEdgeProperties(properties);
        }

        store.edgeStore.add(edge);

        return edge;
    }

    private void serializeEdgeTypeStore(final DataOutput out) throws IOException {
        EdgeTypeStore edgeTypeStore = store.edgeTypeStore;
        int length = edgeTypeStore.length;
        serialize(out, length);
        short[] ids = edgeTypeStore.getIds();
        serialize(out, ids);
        Object[] labels = edgeTypeStore.getLabels();
        serialize(out, labels);
        short[] garbage = edgeTypeStore.getGarbage();
        serialize(out, garbage);
    }

    private EdgeTypeStore deserializeEdgeTypeStore(final DataInput is) throws IOException, ClassNotFoundException {
        int length = (Integer) deserialize(is);
        short[] ids = (short[]) deserialize(is);
        Object[] labels = (Object[]) deserialize(is);
        short[] garbage = (short[]) deserialize(is);

        EdgeTypeStore edgeTypeStore = store.edgeTypeStore;
        edgeTypeStore.length = length;
        for (int i = 0; i < ids.length; i++) {
            short id = ids[i];
            Object label = labels[i];
            edgeTypeStore.idMap.put(id, label);
            edgeTypeStore.labelMap.put(label, id);
        }
        for (int i = 0; i < garbage.length; i++) {
            edgeTypeStore.garbageQueue.add(garbage[i]);
        }
        return edgeTypeStore;
    }

    private void serializeColumnStore(final DataOutput out, final ColumnStore columnStore) throws IOException {
        serialize(out, columnStore.elementType);

        int length = columnStore.length;
        serialize(out, length);

        for (int i = 0; i < length; i++) {
            ColumnImpl col = columnStore.columns[i];
            serialize(out, col);
        }

        serialize(out, columnStore.garbageQueue.toShortArray());
    }

    private ColumnStore deserializeColumnStore(final DataInput is) throws IOException, ClassNotFoundException {
        Class elementType = (Class) deserialize(is);
        ColumnStore columnStore = null;
        if (elementType.equals(Node.class)) {
            columnStore = store.nodeColumnStore;
        } else if (elementType.equals(Edge.class)) {
            columnStore = store.edgeColumnStore;
        } else {
            throw new RuntimeException("Not recognized column store");
        }

        int length = (Integer) deserialize(is);
        columnStore.length = length;

        for (int i = 0; i < length; i++) {
            ColumnImpl col = (ColumnImpl) deserialize(is);
            if (col != null) {
                columnStore.columns[col.storeId] = col;
                columnStore.idMap.put(col.id, columnStore.intToShort(col.storeId));
                if (columnStore.indexStore != null) {
                    columnStore.indexStore.addColumn(col);
                }
            }
        }

        short[] garbage = (short[]) deserialize(is);
        for (int i = 0; i < garbage.length; i++) {
            columnStore.garbageQueue.add(garbage[i]);
        }
        return columnStore;
    }

    private void serializeColumn(final DataOutput out, final ColumnImpl column) throws IOException {
        serialize(out, column.id);
        serialize(out, column.title);
        serialize(out, column.origin);
        serialize(out, column.storeId);
        serialize(out, column.typeClass);
        serialize(out, column.defaultValue);
        serialize(out, column.indexed);
        serialize(out, column.readOnly);
        serialize(out, column.estimator);
    }

    private ColumnImpl deserializeColumn(final DataInput is) throws IOException, ClassNotFoundException {
        String id = (String) deserialize(is);
        String title = (String) deserialize(is);
        Origin origin = (Origin) deserialize(is);
        int storeId = (Integer) deserialize(is);
        Class typeClass = (Class) deserialize(is);
        Object defaultValue = deserialize(is);
        boolean indexed = (Boolean) deserialize(is);
        boolean readOnly = (Boolean) deserialize(is);
        Estimator estimator = (Estimator) deserialize(is);

        ColumnImpl column = new ColumnImpl(id, typeClass, title, defaultValue, origin, indexed, readOnly);
        column.storeId = storeId;
        column.setEstimator(estimator);
        return column;
    }

    private void serializeGraphFactory(final DataOutput out) throws IOException {
        GraphFactoryImpl factory = store.factory;

        serialize(out, factory.getNodeCounter());
        serialize(out, factory.getEdgeCounter());
    }

    private GraphFactoryImpl deserializeGraphFactory(final DataInput is) throws IOException, ClassNotFoundException {
        GraphFactoryImpl graphFactory = store.factory;

        int nodeCounter = (Integer) deserialize(is);
        int edgeCounter = (Integer) deserialize(is);

        graphFactory.setNodeCounter(nodeCounter);
        graphFactory.setEdgeCounter(edgeCounter);

        return graphFactory;
    }

    private void serializeViewStore(final DataOutput out) throws IOException {
        GraphViewStore viewStore = store.viewStore;

        serialize(out, viewStore.length);
        serialize(out, viewStore.views);
        serialize(out, viewStore.garbageQueue.toIntArray());
    }

    private GraphViewStore deserializeViewStore(final DataInput is) throws IOException, ClassNotFoundException {
        GraphViewStore viewStore = store.viewStore;

        int length = (Integer) deserialize(is);
        Object[] views = (Object[]) deserialize(is);
        int[] garbages = (int[]) deserialize(is);

        viewStore.length = length;
        viewStore.views = new GraphViewImpl[views.length];
        System.arraycopy(views, 0, viewStore.views, 0, views.length);
        for (int i = 0; i < garbages.length; i++) {
            viewStore.garbageQueue.add(garbages[i]);
        }
        return viewStore;
    }

    private void serializeGraphView(final DataOutput out, final GraphViewImpl view) throws IOException {
        serialize(out, view.nodeView);
        serialize(out, view.edgeView);
        serialize(out, view.storeId);
        serialize(out, view.nodeCount);
        serialize(out, view.edgeCount);

        serialize(out, view.nodeBitVector);
        serialize(out, view.edgeBitVector);

        serialize(out, view.typeCounts);
        serialize(out, view.mutualEdgeTypeCounts);
        serialize(out, view.mutualEdgesCount);

        serialize(out, view.version);

        serialize(out, view.attributes);
    }

    private GraphViewImpl deserializeGraphView(final DataInput is) throws IOException, ClassNotFoundException {
        boolean nodeView = (Boolean) deserialize(is);
        boolean edgeView = (Boolean) deserialize(is);
        GraphViewImpl view = new GraphViewImpl(store, nodeView, edgeView);

        int storeId = (Integer) deserialize(is);
        int nodeCount = (Integer) deserialize(is);
        int edgeCount = (Integer) deserialize(is);
        BitVector nodeCountVector = (BitVector) deserialize(is);
        BitVector edgeCountVector = (BitVector) deserialize(is);
        int[] typeCounts = (int[]) deserialize(is);
        int[] mutualEdgeTypeCounts = (int[]) deserialize(is);
        int mutualEdgesCount = (Integer) deserialize(is);
        GraphVersion version = (GraphVersion) deserialize(is);
        GraphAttributesImpl atts = (GraphAttributesImpl) deserialize(is);

        view.nodeCount = nodeCount;
        view.edgeCount = edgeCount;
        view.nodeBitVector = nodeCountVector;
        view.edgeBitVector = edgeCountVector;
        view.storeId = storeId;

        view.typeCounts = typeCounts;
        view.mutualEdgesCount = mutualEdgesCount;
        view.mutualEdgeTypeCounts = mutualEdgeTypeCounts;

        view.version.nodeVersion = version.nodeVersion;
        view.version.edgeVersion = version.edgeVersion;

        view.attributes.setGraphAttributes(atts);

        return view;
    }

    private void serializeBitVector(final DataOutput out, final BitVector bitVector) throws IOException {
        serialize(out, bitVector.size());
        serialize(out, bitVector.elements());
    }

    private BitVector deserializeBitVector(final DataInput is) throws IOException, ClassNotFoundException {
        int size = (Integer) deserialize(is);
        long[] elements = (long[]) deserialize(is);
        return new BitVector(elements, size);
    }

    private void serializeGraphStoreConfiguration(final DataOutput out) throws IOException {
        out.write(GRAPH_STORE_CONFIGURATION);
        serialize(out, GraphStoreConfiguration.ENABLE_ELEMENT_LABEL);
        serialize(out, GraphStoreConfiguration.ENABLE_ELEMENT_TIMESTAMP_SET);
        serialize(out, GraphStoreConfiguration.ENABLE_NODE_PROPERTIES);
        serialize(out, GraphStoreConfiguration.ENABLE_EDGE_PROPERTIES);
    }

    private GraphStoreConfigurationVersion deserializeGraphStoreConfiguration(final DataInput is) throws IOException, ClassNotFoundException {
        boolean enableElementLabel = (Boolean) deserialize(is);
        boolean enableElementTimestamp = (Boolean) deserialize(is);
        boolean enableNodeProperties = (Boolean) deserialize(is);
        boolean enableEdgeProperties = (Boolean) deserialize(is);

        graphStoreConfigurationVersion = new GraphStoreConfigurationVersion(enableElementLabel, enableElementTimestamp, enableNodeProperties, enableEdgeProperties);
        return graphStoreConfigurationVersion;
    }

    private void serializeGraphVersion(final DataOutput out, final GraphVersion graphVersion) throws IOException {
        serialize(out, graphVersion.nodeVersion);
        serialize(out, graphVersion.edgeVersion);
    }

    private GraphVersion deserializeGraphVersion(final DataInput is) throws IOException, ClassNotFoundException {
        GraphVersion graphVersion = new GraphVersion(null);

        int nodeVersion = (Integer) deserialize(is);
        int edgeVersion = (Integer) deserialize(is);

        graphVersion.nodeVersion = nodeVersion;
        graphVersion.edgeVersion = edgeVersion;

        return graphVersion;
    }

    private void serializeNodeProperties(final DataOutput out, final NodePropertiesImpl nodeProperties) throws IOException {
        serialize(out, nodeProperties.x);
        serialize(out, nodeProperties.y);
        serialize(out, nodeProperties.z);
        serialize(out, nodeProperties.rgba);
        serialize(out, nodeProperties.size);
        serialize(out, nodeProperties.fixed);
        serialize(out, nodeProperties.textProperties);
    }

    private NodePropertiesImpl deserializeNodeProperties(final DataInput is) throws IOException, ClassNotFoundException {
        float x = (Float) deserialize(is);
        float y = (Float) deserialize(is);
        float z = (Float) deserialize(is);
        int rgba = (Integer) deserialize(is);
        float size = (Float) deserialize(is);
        boolean fixed = (Boolean) deserialize(is);
        TextPropertiesImpl textProperties = (TextPropertiesImpl) deserialize(is);

        NodePropertiesImpl props = new NodePropertiesImpl();
        props.x = x;
        props.y = y;
        props.z = z;
        props.rgba = rgba;
        props.size = size;
        props.fixed = fixed;
        props.setTextProperties(textProperties);

        return props;
    }

    private void serializeEdgeProperties(final DataOutput out, final EdgePropertiesImpl edgeProperties) throws IOException {
        serialize(out, edgeProperties.rgba);
        serialize(out, edgeProperties.textProperties);
    }

    private EdgePropertiesImpl deserializeEdgeProperties(final DataInput is) throws IOException, ClassNotFoundException {
        int rgba = (Integer) deserialize(is);
        TextPropertiesImpl textProperties = (TextPropertiesImpl) deserialize(is);

        EdgePropertiesImpl props = new EdgePropertiesImpl();
        props.rgba = rgba;
        props.setTextProperties(textProperties);

        return props;
    }

    private void serializeTextProperties(final DataOutput out, final TextPropertiesImpl textProperties) throws IOException {
        serialize(out, textProperties.size);
        serialize(out, textProperties.rgba);
        serialize(out, textProperties.visible);
        serialize(out, textProperties.text);
    }

    private TextPropertiesImpl deserializeTextProperties(final DataInput is) throws IOException, ClassNotFoundException {
        float size = (Float) deserialize(is);
        int rgba = (Integer) deserialize(is);
        boolean visible = (Boolean) deserialize(is);
        String text = (String) deserialize(is);

        TextPropertiesImpl props = new TextPropertiesImpl();
        props.size = size;
        props.rgba = rgba;
        props.visible = visible;
        props.text = text;

        return props;
    }

    private void serializeTimestampSet(final DataOutput out, final TimestampSet timestampSet) throws IOException {
        serialize(out, timestampSet.getTimestamps());
    }

    private TimestampSet deserializeTimestampSet(DataInput is) throws IOException, ClassNotFoundException {
        int[] r = (int[]) deserialize(is);

        return new TimestampSet(r);
    }

    private void serializeTimestampValueSet(final DataOutput out, final TimestampValueSet timestampValueSet) throws IOException {
        serialize(out, timestampValueSet.size());
        serialize(out, timestampValueSet.getTimestamps());
        serialize(out, timestampValueSet.toArray());
    }

    private TimestampValueSet deserializeTimestampValueSet(final DataInput is, int head) throws IOException, ClassNotFoundException {
        int size = (Integer) deserialize(is);
        TimestampValueSet valueSet;
        switch (head) {
            case TIMESTAMP_BOOLEAN_SET:
                valueSet = new TimestampBooleanSet(size);
                break;
            case TIMESTAMP_BYTE_SET:
                valueSet = new TimestampByteSet(size);
                break;
            case TIMESTAMP_CHAR_SET:
                valueSet = new TimestampCharSet(size);
                break;
            case TIMESTAMP_DOUBLE_SET:
                valueSet = new TimestampDoubleSet(size);
                break;
            case TIMESTAMP_FLOAT_SET:
                valueSet = new TimestampFloatSet(size);
                break;
            case TIMESTAMP_INTEGER_SET:
                valueSet = new TimestampIntegerSet(size);
                break;
            case TIMESTAMP_LONG_SET:
                valueSet = new TimestampLongSet(size);
                break;
            case TIMESTAMP_SHORT_SET:
                valueSet = new TimestampShortSet(size);
                break;
            case TIMESTAMP_STRING_SET:
                valueSet = new TimestampStringSet(size);
                break;
            default:
                throw new RuntimeException("Not recognized Timestamp value set type");
        }
        int[] timeStamps = (int[]) deserialize(is);
        Object[] values = (Object[]) deserialize(is);

        for (int i = 0; i < timeStamps.length; i++) {
            valueSet.put(timeStamps[i], values[i]);
        }

        return valueSet;
    }

    private void serializeTimestampMap(final DataOutput out, final TimestampMap timestampMap) throws IOException {
        serialize(out, timestampMap.length);
        serialize(out, timestampMap.timestampSortedMap.keySet().toDoubleArray());
        serialize(out, timestampMap.timestampSortedMap.values().toIntArray());
        serialize(out, timestampMap.garbageQueue.toIntArray());
    }

    private TimestampMap deserializeTimestampMap(final DataInput is) throws IOException, ClassNotFoundException {
        TimestampMap timestampMap = new TimestampMap();
        int length = (Integer) deserialize(is);
        double[] doubles = (double[]) deserialize(is);
        int[] ints = (int[]) deserialize(is);
        int[] garbage = (int[]) deserialize(is);

        timestampMap.length = length;
        for (int i : garbage) {
            timestampMap.garbageQueue.add(i);
        }
        for (int i = 0; i < ints.length; i++) {
            timestampMap.timestampMap.put(doubles[i], ints[i]);
            timestampMap.timestampSortedMap.put(doubles[i], ints[i]);
            timestampMap.ensureArraySize(ints[i]);
            timestampMap.indexMap[ints[i]] = doubles[i];
        }
        return timestampMap;
    }

    private void serializeGraphAttributes(final DataOutput out, final GraphAttributesImpl graphAttributes) throws IOException {
        serialize(out, graphAttributes.timestampMap);
        serialize(out, graphAttributes.attributes.size());
        for (Map.Entry<String, Object> entry : graphAttributes.attributes.entrySet()) {
            serialize(out, entry.getKey());
            serialize(out, entry.getValue());
        }
    }

    private GraphAttributesImpl deserializeGraphAttributes(final DataInput is) throws IOException, ClassNotFoundException {
        GraphAttributesImpl attributes = new GraphAttributesImpl();
        attributes.timestampMap.setTimestampMap((TimestampMap) deserialize(is));
        int size = (Integer) deserialize(is);
        for (int i = 0; i < size; i++) {
            String key = (String) deserialize(is);
            Object value = deserialize(is);
            attributes.attributes.put(key, value);
        }
        return attributes;
    }

    private void serializeTimeFormat(final DataOutput out, final TimeFormat timeFormat) throws IOException {
        serialize(out, timeFormat.name());
    }

    private TimeFormat deserializeTimeFormat(final DataInput is) throws IOException, ClassNotFoundException {
        String name = (String) deserialize(is);

        TimeFormat tf = TimeFormat.valueOf(name);
        store.timeFormat = tf;

        return tf;
    }
    
    private void serializeTimestampStore(final DataOutput out) throws IOException {
        TimestampStore timestampStore = store.timestampStore;
        
        serialize(out, timestampStore.nodeMap);
        serialize(out, timestampStore.edgeMap);
    }
    
    private TimestampStore deserializeTimestampStore(final DataInput is) throws IOException, ClassNotFoundException {
        TimestampStore timestampStore = store.timestampStore;
        
        TimestampMap nodeMap = (TimestampMap)deserialize(is);
        TimestampMap edgeMap = (TimestampMap)deserialize(is);
        
        timestampStore.nodeMap.setTimestampMap(nodeMap);
        timestampStore.edgeMap.setTimestampMap(edgeMap);
        
        return timestampStore;
    }

    //SERIALIZE PRIMITIVES
    protected byte[] serialize(Object obj) throws IOException {
        DataInputOutput ba = new DataInputOutput();

        serialize(ba, obj);

        return ba.toByteArray();
    }

    protected void serialize(final DataOutput out, final Object obj) throws IOException {
        final Class clazz = obj != null ? obj.getClass() : null;

        if (obj == null) {
            out.write(NULL);
        } else if (clazz == Boolean.class) {
            if (((Boolean) obj).booleanValue()) {
                out.write(BOOLEAN_TRUE);
            } else {
                out.write(BOOLEAN_FALSE);
            }
        } else if (clazz == Integer.class) {
            final int val = (Integer) obj;
            writeInteger(out, val);
        } else if (clazz == Double.class) {
            double v = (Double) obj;
            if (v == -1d) {
                out.write(DOUBLE_MINUS_1);
            } else if (v == 0d) {
                out.write(DOUBLE_0);
            } else if (v == 1d) {
                out.write(DOUBLE_1);
            } else if (v >= 0 && v <= 255 && (int) v == v) {
                out.write(DOUBLE_255);
                out.write((int) v);
            } else if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE && (short) v == v) {
                out.write(DOUBLE_SHORT);
                out.writeShort((int) v);
            } else {
                out.write(DOUBLE_FULL);
                out.writeDouble(v);
            }
        } else if (clazz == Float.class) {
            float v = (Float) obj;
            if (v == -1f) {
                out.write(FLOAT_MINUS_1);
            } else if (v == 0f) {
                out.write(FLOAT_0);
            } else if (v == 1f) {
                out.write(FLOAT_1);
            } else if (v >= 0 && v <= 255 && (int) v == v) {
                out.write(FLOAT_255);
                out.write((int) v);
            } else if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE && (short) v == v) {
                out.write(FLOAT_SHORT);
                out.writeShort((int) v);

            } else {
                out.write(FLOAT_FULL);
                out.writeFloat(v);
            }
        } else if (clazz == Long.class) {
            final long val = (Long) obj;
            writeLong(out, val);
        } else if (clazz == BigInteger.class) {
            out.write(BIGINTEGER);
            byte[] buf = ((BigInteger) obj).toByteArray();
            serializeByteArrayInt(out, buf);
        } else if (clazz == BigDecimal.class) {
            out.write(BIGDECIMAL);
            BigDecimal d = (BigDecimal) obj;
            serializeByteArrayInt(out, d.unscaledValue().toByteArray());
            LongPacker.packInt(out, d.scale());
        } else if (clazz == Short.class) {
            short val = (Short) obj;
            if (val == -1) {
                out.write(SHORT_MINUS_1);
            } else if (val == 0) {
                out.write(SHORT_0);
            } else if (val == 1) {
                out.write(SHORT_1);
            } else if (val > 0 && val < 255) {
                out.write(SHORT_255);
                out.write(val);
            } else {
                out.write(SHORT_FULL);
                out.writeShort(val);
            }
        } else if (clazz == Byte.class) {
            byte val = (Byte) obj;
            if (val == -1) {
                out.write(BYTE_MINUS_1);
            } else if (val == 0) {
                out.write(BYTE_0);
            } else if (val == 1) {
                out.write(BYTE_1);
            } else {
                out.write(BYTE_FULL);
                out.writeByte(val);
            }
        } else if (clazz == Character.class) {
            out.write(CHAR);
            out.writeChar((Character) obj);
        } else if (clazz == String.class) {
            String s = (String) obj;
            if (s.length() == 0) {
                out.write(STRING_EMPTY);
            } else {
                out.write(STRING);
                serializeString(out, s);
            }
        } else if (obj instanceof Class) {
            out.write(CLASS);
            serialize(out, ((Class) obj).getName());
        } else if (obj instanceof int[]) {
            writeIntArray(out, (int[]) obj);
        } else if (obj instanceof long[]) {
            writeLongArray(out, (long[]) obj);
        } else if (obj instanceof short[]) {
            out.write(SHORT_ARRAY);
            short[] a = (short[]) obj;
            LongPacker.packInt(out, a.length);
            for (short s : a) {
                out.writeShort(s);
            }
        } else if (obj instanceof boolean[]) {
            out.write(BOOLEAN_ARRAY);
            boolean[] a = (boolean[]) obj;
            LongPacker.packInt(out, a.length);
            for (boolean s : a) {
                out.writeBoolean(s); //TODO pack 8 booleans to single byte
            }
        } else if (obj instanceof double[]) {
            out.write(DOUBLE_ARRAY);
            double[] a = (double[]) obj;
            LongPacker.packInt(out, a.length);
            for (double s : a) {
                out.writeDouble(s);
            }
        } else if (obj instanceof float[]) {
            out.write(FLOAT_ARRAY);
            float[] a = (float[]) obj;
            LongPacker.packInt(out, a.length);
            for (float s : a) {
                out.writeFloat(s);
            }
        } else if (obj instanceof char[]) {
            out.write(CHAR_ARRAY);
            char[] a = (char[]) obj;
            LongPacker.packInt(out, a.length);
            for (char s : a) {
                out.writeChar(s);
            }
        } else if (obj instanceof byte[]) {
            byte[] b = (byte[]) obj;
            out.write(ARRAY_BYTE_INT);
            serializeByteArrayInt(out, b);
        } else if (clazz == Date.class) {
            out.write(DATE);
            out.writeLong(((Date) obj).getTime());
        } else if (clazz == Locale.class) {
            out.write(LOCALE);
            Locale l = (Locale) obj;
            out.writeUTF(l.getLanguage());
            out.writeUTF(l.getCountry());
            out.writeUTF(l.getVariant());
        } else if (obj instanceof Object[]) {
            Object[] b = (Object[]) obj;
            out.write(ARRAY_OBJECT);
            LongPacker.packInt(out, b.length);
            for (Object o : b) {
                serialize(out, o);
            }
        } else if (obj instanceof TimestampSet) {
            TimestampSet b = (TimestampSet) obj;
            out.write(TIMESTAMP_SET);
            serializeTimestampSet(out, b);
        } else if (obj instanceof NodeImpl) {
            NodeImpl b = (NodeImpl) obj;
            out.write(NODE);
            serializeNode(out, b);
        } else if (obj instanceof EdgeImpl) {
            EdgeImpl b = (EdgeImpl) obj;
            out.write(EDGE);
            serializeEdge(out, b);
        } else if (obj instanceof EdgeTypeStore) {
            EdgeTypeStore b = (EdgeTypeStore) obj;
            out.write(EDGETYPE_STORE);
            serializeEdgeTypeStore(out);
        } else if (obj instanceof Origin) {
            Origin b = (Origin) obj;
            out.write(COLUMN_ORIGIN);
            serialize(out, b.name());
        } else if (obj instanceof ColumnImpl) {
            ColumnImpl b = (ColumnImpl) obj;
            out.write(COLUMN);
            serializeColumn(out, b);
        } else if (obj instanceof ColumnStore) {
            ColumnStore b = (ColumnStore) obj;
            out.write(COLUMN_STORE);
            serializeColumnStore(out, b);
        } else if (obj instanceof GraphStore) {
            GraphStore b = (GraphStore) obj;
            out.write(GRAPH_STORE);
            serializeGraphStore(out);
        } else if (obj instanceof GraphFactoryImpl) {
            GraphFactoryImpl b = (GraphFactoryImpl) obj;
            out.write(GRAPH_FACTORY);
            serializeGraphFactory(out);
        } else if (obj instanceof GraphViewStore) {
            GraphViewStore b = (GraphViewStore) obj;
            out.write(GRAPH_VIEW_STORE);
            serializeViewStore(out);
        } else if (obj instanceof GraphViewImpl) {
            GraphViewImpl b = (GraphViewImpl) obj;
            out.write(GRAPH_VIEW);
            serializeGraphView(out, b);
        } else if (obj instanceof BitVector) {
            BitVector bv = (BitVector) obj;
            out.write(BIT_VECTOR);
            serializeBitVector(out, bv);
        } else if (obj instanceof GraphVersion) {
            GraphVersion b = (GraphVersion) obj;
            out.write(GRAPH_VERSION);
            serializeGraphVersion(out, b);
        } else if (obj instanceof NodePropertiesImpl) {
            NodePropertiesImpl b = (NodePropertiesImpl) obj;
            out.write(NODE_PROPERTIES);
            serializeNodeProperties(out, b);
        } else if (obj instanceof EdgePropertiesImpl) {
            EdgePropertiesImpl b = (EdgePropertiesImpl) obj;
            out.write(EDGE_PROPERTIES);
            serializeEdgeProperties(out, b);
        } else if (obj instanceof TextPropertiesImpl) {
            TextPropertiesImpl b = (TextPropertiesImpl) obj;
            out.write(TEXT_PROPERTIES);
            serializeTextProperties(out, b);
        } else if (obj instanceof Estimator) {
            Estimator b = (Estimator) obj;
            out.write(ESTIMATOR);
            serializeString(out, b.name());
        } else if (obj instanceof TimestampBooleanSet) {
            TimestampBooleanSet b = (TimestampBooleanSet) obj;
            out.write(TIMESTAMP_BOOLEAN_SET);
            serializeTimestampValueSet(out, b);
        } else if (obj instanceof TimestampByteSet) {
            TimestampByteSet b = (TimestampByteSet) obj;
            out.write(TIMESTAMP_BYTE_SET);
            serializeTimestampValueSet(out, b);
        } else if (obj instanceof TimestampCharSet) {
            TimestampCharSet b = (TimestampCharSet) obj;
            out.write(TIMESTAMP_CHAR_SET);
            serializeTimestampValueSet(out, b);
        } else if (obj instanceof TimestampDoubleSet) {
            TimestampDoubleSet b = (TimestampDoubleSet) obj;
            out.write(TIMESTAMP_DOUBLE_SET);
            serializeTimestampValueSet(out, b);
        } else if (obj instanceof TimestampFloatSet) {
            TimestampFloatSet b = (TimestampFloatSet) obj;
            out.write(TIMESTAMP_FLOAT_SET);
            serializeTimestampValueSet(out, b);
        } else if (obj instanceof TimestampIntegerSet) {
            TimestampIntegerSet b = (TimestampIntegerSet) obj;
            out.write(TIMESTAMP_INTEGER_SET);
            serializeTimestampValueSet(out, b);
        } else if (obj instanceof TimestampLongSet) {
            TimestampLongSet b = (TimestampLongSet) obj;
            out.write(TIMESTAMP_LONG_SET);
            serializeTimestampValueSet(out, b);
        } else if (obj instanceof TimestampShortSet) {
            TimestampShortSet b = (TimestampShortSet) obj;
            out.write(TIMESTAMP_SHORT_SET);
            serializeTimestampValueSet(out, b);
        } else if (obj instanceof TimestampStringSet) {
            TimestampStringSet b = (TimestampStringSet) obj;
            out.write(TIMESTAMP_STRING_SET);
            serializeTimestampValueSet(out, b);
        } else if (obj instanceof TimestampMap) {
            TimestampMap b = (TimestampMap) obj;
            out.write(TIMESTAMP_MAP);
            serializeTimestampMap(out, b);
        } else if (obj instanceof GraphAttributesImpl) {
            GraphAttributesImpl b = (GraphAttributesImpl) obj;
            out.write(GRAPH_ATTRIBUTES);
            serializeGraphAttributes(out, b);
        } else if (obj instanceof TimeFormat) {
            TimeFormat b = (TimeFormat) obj;
            out.write(TIME_FORMAT);
            serializeTimeFormat(out, b);
        } else if (obj instanceof TimestampStore) {
            TimestampStore b = (TimestampStore) obj;
            out.write(TIMESTAMP_STORE);
            serializeTimestampStore(out);
        } else {
            throw new IOException("No serialization handler for this class: " + clazz.getName());
        }
    }

    public static void serializeString(DataOutput out, String obj) throws IOException {
        final int len = obj.length();
        LongPacker.packInt(out, len);
        for (int i = 0; i < len; i++) {
            int c = (int) obj.charAt(i); //TODO investigate if c could be negative here
            LongPacker.packInt(out, c);
        }
    }

    private void serializeByteArrayInt(DataOutput out, byte[] b) throws IOException {
        LongPacker.packInt(out, b.length);
        out.write(b);
    }

    private void writeLongArray(DataOutput da, long[] obj) throws IOException {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        for (long i : obj) {
            max = Math.max(max, i);
            min = Math.min(min, i);
        }

        if (0 <= min && max <= 255) {
            da.write(ARRAY_LONG_B);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                da.write((int) l);
            }
        } else if (0 <= min && max <= Long.MAX_VALUE) {
            da.write(ARRAY_LONG_PACKED);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                LongPacker.packLong(da, l);
            }
        } else if (Short.MIN_VALUE <= min && max <= Short.MAX_VALUE) {
            da.write(ARRAY_LONG_S);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                da.writeShort((short) l);
            }
        } else if (Integer.MIN_VALUE <= min && max <= Integer.MAX_VALUE) {
            da.write(ARRAY_LONG_I);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                da.writeInt((int) l);
            }
        } else {
            da.write(ARRAY_LONG_L);
            LongPacker.packInt(da, obj.length);
            for (long l : obj) {
                da.writeLong(l);
            }
        }
    }

    private void writeIntArray(DataOutput da, int[] obj) throws IOException {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (int i : obj) {
            max = Math.max(max, i);
            min = Math.min(min, i);
        }

        boolean fitsInByte = 0 <= min && max <= 255;
        boolean fitsInShort = Short.MIN_VALUE >= min && max <= Short.MAX_VALUE;

        if (obj.length <= 255 && fitsInByte) {
            da.write(ARRAY_INT_B_255);
            da.write(obj.length);
            for (int i : obj) {
                da.write(i);
            }
        } else if (fitsInByte) {
            da.write(ARRAY_INT_B_INT);
            LongPacker.packInt(da, obj.length);
            for (int i : obj) {
                da.write(i);
            }
        } else if (0 <= min && max <= Integer.MAX_VALUE) {
            da.write(ARRAY_INT_PACKED);
            LongPacker.packInt(da, obj.length);
            for (int l : obj) {
                LongPacker.packInt(da, l);
            }
        } else if (fitsInShort) {
            da.write(ARRAY_INT_S);
            LongPacker.packInt(da, obj.length);
            for (int i : obj) {
                da.writeShort(i);
            }
        } else {
            da.write(ARRAY_INT_I);
            LongPacker.packInt(da, obj.length);
            for (int i : obj) {
                da.writeInt(i);
            }
        }

    }

    private void writeInteger(DataOutput da, final int val) throws IOException {
        if (val == -1) {
            da.write(INTEGER_MINUS_1);
        } else if (val == 0) {
            da.write(INTEGER_0);
        } else if (val == 1) {
            da.write(INTEGER_1);
        } else if (val == 2) {
            da.write(INTEGER_2);
        } else if (val == 3) {
            da.write(INTEGER_3);
        } else if (val == 4) {
            da.write(INTEGER_4);
        } else if (val == 5) {
            da.write(INTEGER_5);
        } else if (val == 6) {
            da.write(INTEGER_6);
        } else if (val == 7) {
            da.write(INTEGER_7);
        } else if (val == 8) {
            da.write(INTEGER_8);
        } else if (val == Integer.MIN_VALUE) {
            da.write(INTEGER_MINUS_MAX);
        } else if (val > 0 && val < 255) {
            da.write(INTEGER_255);
            da.write(val);
        } else if (val < 0) {
            da.write(INTEGER_PACK_NEG);
            LongPacker.packInt(da, -val);
        } else {
            da.write(INTEGER_PACK);
            LongPacker.packInt(da, val);
        }
    }

    private void writeLong(DataOutput da, final long val) throws IOException {
        if (val == -1) {
            da.write(LONG_MINUS_1);
        } else if (val == 0) {
            da.write(LONG_0);
        } else if (val == 1) {
            da.write(LONG_1);
        } else if (val == 2) {
            da.write(LONG_2);
        } else if (val == 3) {
            da.write(LONG_3);
        } else if (val == 4) {
            da.write(LONG_4);
        } else if (val == 5) {
            da.write(LONG_5);
        } else if (val == 6) {
            da.write(LONG_6);
        } else if (val == 7) {
            da.write(LONG_7);
        } else if (val == 8) {
            da.write(LONG_8);
        } else if (val == Long.MIN_VALUE) {
            da.write(LONG_MINUS_MAX);
        } else if (val > 0 && val < 255) {
            da.write(LONG_255);
            da.write((int) val);
        } else if (val < 0) {
            da.write(LONG_PACK_NEG);
            LongPacker.packLong(da, -val);
        } else {
            da.write(LONG_PACK);
            LongPacker.packLong(da, val);
        }
    }

    //DESERIALIZE PRIMITIVES
    protected Object deserialize(byte[] buf) throws ClassNotFoundException, IOException {
        DataInputOutput bs = new DataInputOutput(buf);
        Object ret = deserialize(bs);
        if (bs.available() != 0) {
            throw new RuntimeException("bytes left: " + bs.available());
        }

        return ret;
    }

    protected Object deserialize(DataInput is) throws IOException, ClassNotFoundException {
        Object ret = null;

        final int head = is.readUnsignedByte();

        switch (head) {
            case NULL:
                break;
            case BOOLEAN_TRUE:
                ret = Boolean.TRUE;
                break;
            case BOOLEAN_FALSE:
                ret = Boolean.FALSE;
                break;
            case INTEGER_MINUS_1:
                ret = Integer.valueOf(-1);
                break;
            case INTEGER_0:
                ret = Integer.valueOf(0);
                break;
            case INTEGER_1:
                ret = Integer.valueOf(1);
                break;
            case INTEGER_2:
                ret = Integer.valueOf(2);
                break;
            case INTEGER_3:
                ret = Integer.valueOf(3);
                break;
            case INTEGER_4:
                ret = Integer.valueOf(4);
                break;
            case INTEGER_5:
                ret = Integer.valueOf(5);
                break;
            case INTEGER_6:
                ret = Integer.valueOf(6);
                break;
            case INTEGER_7:
                ret = Integer.valueOf(7);
                break;
            case INTEGER_8:
                ret = Integer.valueOf(8);
                break;
            case INTEGER_MINUS_MAX:
                ret = Integer.valueOf(Integer.MIN_VALUE);
                break;
            case INTEGER_255:
                ret = Integer.valueOf(is.readUnsignedByte());
                break;
            case INTEGER_PACK_NEG:
                ret = Integer.valueOf(-LongPacker.unpackInt(is));
                break;
            case INTEGER_PACK:
                ret = Integer.valueOf(LongPacker.unpackInt(is));
                break;
            case LONG_MINUS_1:
                ret = Long.valueOf(-1);
                break;
            case LONG_0:
                ret = Long.valueOf(0);
                break;
            case LONG_1:
                ret = Long.valueOf(1);
                break;
            case LONG_2:
                ret = Long.valueOf(2);
                break;
            case LONG_3:
                ret = Long.valueOf(3);
                break;
            case LONG_4:
                ret = Long.valueOf(4);
                break;
            case LONG_5:
                ret = Long.valueOf(5);
                break;
            case LONG_6:
                ret = Long.valueOf(6);
                break;
            case LONG_7:
                ret = Long.valueOf(7);
                break;
            case LONG_8:
                ret = Long.valueOf(8);
                break;
            case LONG_255:
                ret = Long.valueOf(is.readUnsignedByte());
                break;
            case LONG_PACK_NEG:
                ret = Long.valueOf(-LongPacker.unpackLong(is));
                break;
            case LONG_PACK:
                ret = Long.valueOf(LongPacker.unpackLong(is));
                break;
            case LONG_MINUS_MAX:
                ret = Long.valueOf(Long.MIN_VALUE);
                break;
            case SHORT_MINUS_1:
                ret = Short.valueOf((short) -1);
                break;
            case SHORT_0:
                ret = Short.valueOf((short) 0);
                break;
            case SHORT_1:
                ret = Short.valueOf((short) 1);
                break;
            case SHORT_255:
                ret = Short.valueOf((short) is.readUnsignedByte());
                break;
            case SHORT_FULL:
                ret = Short.valueOf(is.readShort());
                break;
            case BYTE_MINUS_1:
                ret = Byte.valueOf((byte) -1);
                break;
            case BYTE_0:
                ret = Byte.valueOf((byte) 0);
                break;
            case BYTE_1:
                ret = Byte.valueOf((byte) 1);
                break;
            case BYTE_FULL:
                ret = Byte.valueOf(is.readByte());
                break;
            case SHORT_ARRAY:
                int size = LongPacker.unpackInt(is);
                ret = new short[size];
                for (int i = 0; i < size; i++) {
                    ((short[]) ret)[i] = is.readShort();
                }
                break;
            case BOOLEAN_ARRAY:
                size = LongPacker.unpackInt(is);
                ret = new boolean[size];
                for (int i = 0; i < size; i++) {
                    ((boolean[]) ret)[i] = is.readBoolean();
                }
                break;
            case DOUBLE_ARRAY:
                size = LongPacker.unpackInt(is);
                ret = new double[size];
                for (int i = 0; i < size; i++) {
                    ((double[]) ret)[i] = is.readDouble();
                }
                break;
            case FLOAT_ARRAY:
                size = LongPacker.unpackInt(is);
                ret = new float[size];
                for (int i = 0; i < size; i++) {
                    ((float[]) ret)[i] = is.readFloat();
                }
                break;
            case CHAR_ARRAY:
                size = LongPacker.unpackInt(is);
                ret = new char[size];
                for (int i = 0; i < size; i++) {
                    ((char[]) ret)[i] = is.readChar();
                }
                break;
            case CHAR:
                ret = Character.valueOf(is.readChar());
                break;
            case FLOAT_MINUS_1:
                ret = Float.valueOf(-1);
                break;
            case FLOAT_0:
                ret = Float.valueOf(0);
                break;
            case FLOAT_1:
                ret = Float.valueOf(1);
                break;
            case FLOAT_255:
                ret = Float.valueOf(is.readUnsignedByte());
                break;
            case FLOAT_SHORT:
                ret = Float.valueOf(is.readShort());
                break;
            case FLOAT_FULL:
                ret = Float.valueOf(is.readFloat());
                break;
            case DOUBLE_MINUS_1:
                ret = Double.valueOf(-1);
                break;
            case DOUBLE_0:
                ret = Double.valueOf(0);
                break;
            case DOUBLE_1:
                ret = Double.valueOf(1);
                break;
            case DOUBLE_255:
                ret = Double.valueOf(is.readUnsignedByte());
                break;
            case DOUBLE_SHORT:
                ret = Double.valueOf(is.readShort());
                break;
            case DOUBLE_FULL:
                ret = Double.valueOf(is.readDouble());
                break;
            case BIGINTEGER:
                ret = new BigInteger(deserializeArrayByteInt(is));
                break;
            case BIGDECIMAL:
                ret = new BigDecimal(new BigInteger(deserializeArrayByteInt(is)), LongPacker.unpackInt(is));
                break;
            case STRING:
                ret = deserializeString(is);
                break;
            case STRING_EMPTY:
                ret = EMPTY_STRING;
                break;
            case CLASS:
                ret = deserializeClass(is);
                break;
            case DATE:
                ret = new Date(is.readLong());
                break;
            case ARRAY_INT_B_255:
                ret = deserializeArrayIntB255(is);
                break;
            case ARRAY_INT_B_INT:
                ret = deserializeArrayIntBInt(is);
                break;
            case ARRAY_INT_S:
                ret = deserializeArrayIntSInt(is);
                break;
            case ARRAY_INT_I:
                ret = deserializeArrayIntIInt(is);
                break;
            case ARRAY_INT_PACKED:
                ret = deserializeArrayIntPack(is);
                break;
            case ARRAY_LONG_B:
                ret = deserializeArrayLongB(is);
                break;
            case ARRAY_LONG_S:
                ret = deserializeArrayLongS(is);
                break;
            case ARRAY_LONG_I:
                ret = deserializeArrayLongI(is);
                break;
            case ARRAY_LONG_L:
                ret = deserializeArrayLongL(is);
                break;
            case ARRAY_LONG_PACKED:
                ret = deserializeArrayLongPack(is);
                break;
            case ARRAY_BYTE_INT:
                ret = deserializeArrayByteInt(is);
                break;
            case LOCALE:
                ret = new Locale(is.readUTF(), is.readUTF(), is.readUTF());
                break;
            case ARRAY_OBJECT:
                ret = deserializeArrayObject(is);
                break;
            case TIMESTAMP_SET:
                ret = deserializeTimestampSet(is);
                break;
            case NODE:
                ret = deserializeNode(is);
                break;
            case EDGE:
                ret = deserializeEdge(is);
                break;
            case EDGETYPE_STORE:
                ret = deserializeEdgeTypeStore(is);
                break;
            case COLUMN_ORIGIN:
                ret = Origin.valueOf((String) deserialize(is));
                break;
            case COLUMN:
                ret = deserializeColumn(is);
                break;
            case COLUMN_STORE:
                ret = deserializeColumnStore(is);
                break;
            case GRAPH_STORE:
                ret = deserializeGraphStore(is);
                break;
            case GRAPH_FACTORY:
                ret = deserializeGraphFactory(is);
                break;
            case GRAPH_VIEW_STORE:
                ret = deserializeViewStore(is);
                break;
            case GRAPH_VIEW:
                ret = deserializeGraphView(is);
                break;
            case BIT_VECTOR:
                ret = deserializeBitVector(is);
                break;
            case GRAPH_STORE_CONFIGURATION:
                ret = deserializeGraphStoreConfiguration(is);
                break;
            case GRAPH_VERSION:
                ret = deserializeGraphVersion(is);
                break;
            case NODE_PROPERTIES:
                ret = deserializeNodeProperties(is);
                break;
            case EDGE_PROPERTIES:
                ret = deserializeEdgeProperties(is);
                break;
            case TEXT_PROPERTIES:
                ret = deserializeTextProperties(is);
                break;
            case ESTIMATOR:
                ret = Estimator.valueOf(deserializeString(is));
                break;
            case TIMESTAMP_BOOLEAN_SET:
                ret = deserializeTimestampValueSet(is, head);
                break;
            case TIMESTAMP_BYTE_SET:
                ret = deserializeTimestampValueSet(is, head);
                break;
            case TIMESTAMP_CHAR_SET:
                ret = deserializeTimestampValueSet(is, head);
                break;
            case TIMESTAMP_DOUBLE_SET:
                ret = deserializeTimestampValueSet(is, head);
                break;
            case TIMESTAMP_FLOAT_SET:
                ret = deserializeTimestampValueSet(is, head);
                break;
            case TIMESTAMP_INTEGER_SET:
                ret = deserializeTimestampValueSet(is, head);
                break;
            case TIMESTAMP_LONG_SET:
                ret = deserializeTimestampValueSet(is, head);
                break;
            case TIMESTAMP_SHORT_SET:
                ret = deserializeTimestampValueSet(is, head);
                break;
            case TIMESTAMP_STRING_SET:
                ret = deserializeTimestampValueSet(is, head);
                break;
            case TIMESTAMP_MAP:
                ret = deserializeTimestampMap(is);
                break;
            case GRAPH_ATTRIBUTES:
                ret = deserializeGraphAttributes(is);
                break;
            case TIME_FORMAT:
                ret = deserializeTimeFormat(is);
                break;
            case TIMESTAMP_STORE:
                ret = deserializeTimestampStore(is);
                break;
            case -1:
                throw new EOFException();

        }
        return ret;
    }

    public static String deserializeString(DataInput buf) throws IOException {
        int len = LongPacker.unpackInt(buf);
        char[] b = new char[len];
        for (int i = 0; i < len; i++) {
            b[i] = (char) LongPacker.unpackInt(buf);
        }

        return new String(b);
    }

    private Class deserializeClass(DataInput is) throws IOException, ClassNotFoundException {
        String className = (String) deserialize(is);
        Class cls = Class.forName(className);
        return cls;
    }

    private byte[] deserializeArrayByteInt(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        byte[] b = new byte[size];
        is.readFully(b);
        return b;
    }

    private long[] deserializeArrayLongL(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readLong();
        }
        return ret;
    }

    private long[] deserializeArrayLongI(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readInt();
        }
        return ret;
    }

    private long[] deserializeArrayLongS(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readShort();
        }
        return ret;
    }

    private long[] deserializeArrayLongB(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readUnsignedByte();
            if (ret[i] < 0) {
                throw new EOFException();
            }
        }
        return ret;
    }

    private int[] deserializeArrayIntIInt(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readInt();
        }
        return ret;
    }

    private int[] deserializeArrayIntSInt(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readShort();
        }
        return ret;
    }

    private int[] deserializeArrayIntBInt(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readUnsignedByte();
            if (ret[i] < 0) {
                throw new EOFException();
            }
        }
        return ret;
    }

    private int[] deserializeArrayIntPack(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        if (size < 0) {
            throw new EOFException();
        }

        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = LongPacker.unpackInt(is);
        }
        return ret;
    }

    private long[] deserializeArrayLongPack(DataInput is) throws IOException {
        int size = LongPacker.unpackInt(is);
        if (size < 0) {
            throw new EOFException();
        }

        long[] ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = LongPacker.unpackLong(is);
        }
        return ret;
    }

    private int[] deserializeArrayIntB255(DataInput is) throws IOException {
        int size = is.readUnsignedByte();
        if (size < 0) {
            throw new EOFException();
        }

        int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = is.readUnsignedByte();
            if (ret[i] < 0) {
                throw new EOFException();
            }
        }
        return ret;
    }

    private Object[] deserializeArrayObject(DataInput is) throws IOException, ClassNotFoundException {
        int size = LongPacker.unpackInt(is);

        Object[] s = (Object[]) Array.newInstance(Object.class, size);
        for (int i = 0; i < size; i++) {
            s[i] = deserialize(is);
        }
        return s;
    }

    protected static class GraphStoreConfigurationVersion {

        protected final boolean enableElementLabel;
        protected final boolean enableElementTimestamp;
        protected final boolean enableNodeProperties;
        protected final boolean enableEdgeProperties;

        public GraphStoreConfigurationVersion(boolean enableElementLabel, boolean enableElementTimestamp, boolean enableNodeProperties, boolean enableEdgeProperties) {
            this.enableElementLabel = enableElementLabel;
            this.enableElementTimestamp = enableElementTimestamp;
            this.enableNodeProperties = enableNodeProperties;
            this.enableEdgeProperties = enableEdgeProperties;
        }
    }
}
