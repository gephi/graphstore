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
package org.gephi.graph.impl;

import cern.colt.bitvector.BitVector;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.TimestampBooleanMap;
import org.gephi.graph.api.types.TimestampByteMap;
import org.gephi.graph.api.types.TimestampCharMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampFloatMap;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.graph.api.types.TimestampLongMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.graph.api.types.TimestampShortMap;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.graph.api.Edge;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class SerializationTest {

    @Test
    public void testEdgeStoreMixed() throws IOException, ClassNotFoundException {
        GraphStore graphStore = new GraphStore();

        NodeStore nodeStore = graphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeImpl[] nodes = GraphGenerator.generateNodeList(5100);
        nodeStore.addAll(Arrays.asList(nodes));
        EdgeImpl[] edges = GraphGenerator.generateMixedEdgeList(nodeStore, 9000, 0, true);
        edgeStore.addAll(Arrays.asList(edges));

        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(graphStore);
        graphStore.clear();

        GraphStore l = (GraphStore) ser.deserialize(buf);
        Assert.assertTrue(nodeStore.equals(l.nodeStore));
        Assert.assertTrue(edgeStore.equals(l.edgeStore));
    }

    @Test
    public void testEdgeStoreMultipleTypes() throws IOException, ClassNotFoundException {
        GraphStore graphStore = new GraphStore();

        NodeStore nodeStore = graphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeImpl[] nodes = GraphGenerator.generateNodeList(5100);
        nodeStore.addAll(Arrays.asList(nodes));
        EdgeImpl[] edges = GraphGenerator.generateMultiTypeEdgeList(nodeStore, 9000, 3, true, true);
        edgeStore.addAll(Arrays.asList(edges));

        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(graphStore);
        graphStore.clear();

        GraphStore l = (GraphStore) ser.deserialize(buf);
        Assert.assertTrue(nodeStore.equals(l.nodeStore));
        Assert.assertTrue(edgeStore.equals(l.edgeStore));
    }

    @Test
    public void testEdgeStore() throws IOException, ClassNotFoundException {
        GraphStore graphStore = new GraphStore();

        NodeStore nodeStore = graphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeImpl[] nodes = GraphGenerator.generateSmallNodeList();
        nodeStore.addAll(Arrays.asList(nodes));
        EdgeImpl[] edges = GraphGenerator.generateSmallEdgeList();
        edgeStore.addAll(Arrays.asList(edges));

        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(graphStore);
        graphStore.clear();

        GraphStore l = (GraphStore) ser.deserialize(buf);
        Assert.assertTrue(nodeStore.equals(l.nodeStore));
        Assert.assertTrue(edgeStore.equals(l.edgeStore));
    }

    @Test
    public void testNodeStore() throws IOException, ClassNotFoundException {
        GraphStore graphStore = new GraphStore();

        NodeStore nodeStore = graphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeImpl[] nodes = GraphGenerator.generateSmallNodeList();
        nodeStore.addAll(Arrays.asList(nodes));

        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(graphStore);

        graphStore = new GraphStore();
        ser = new Serialization(graphStore);
        GraphStore l = (GraphStore) ser.deserialize(buf);
        Assert.assertTrue(nodeStore.deepEquals(l.nodeStore));
        Assert.assertTrue(edgeStore.deepEquals(l.edgeStore));
    }

    @Test
    public void testNode() throws IOException, ClassNotFoundException {
        GraphStore graphStore = new GraphStore();
        ColumnStore columnStore = graphStore.nodeColumnStore;
        ColumnImpl col1 = new ColumnImpl("0", Integer.class, "title", 8, Origin.DATA, false, false);
        ColumnImpl col2 = new ColumnImpl("1", String.class, null, "default", Origin.PROPERTY, false, false);
        ColumnImpl col3 = new ColumnImpl("2", int[].class, null, null, Origin.PROPERTY, false, false);
        columnStore.addColumn(col1);
        columnStore.addColumn(col2);
        columnStore.addColumn(col3);

        NodeImpl node = new NodeImpl("Foo", graphStore);
        node.setAttribute(col1, 1);
        node.setAttribute(col3, new int[]{1, 7, 3, 4});

        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(node);

        graphStore = new GraphStore();
        ser = new Serialization(graphStore);
        NodeImpl l = (NodeImpl) ser.deserialize(buf);
        Assert.assertTrue(node.equals(l));
        Assert.assertTrue(Arrays.deepEquals(l.attributes, node.attributes));
    }

    @Test
    public void testGraphFactory() throws IOException, ClassNotFoundException {
        GraphStore graphStore = new GraphStore();
        GraphFactoryImpl factory = graphStore.factory;

        factory.setNodeCounter(100);
        factory.setEdgeCounter(50);

        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(factory);

        graphStore = new GraphStore();
        ser = new Serialization(graphStore);
        GraphFactoryImpl l = (GraphFactoryImpl) ser.deserialize(buf);
        Assert.assertTrue(factory.deepEquals(l));
    }

    @Test
    public void testEdgeTypeStore() throws IOException, ClassNotFoundException {
        GraphStore graphStore = new GraphStore();

        EdgeTypeStore edgeTypeStore = graphStore.edgeTypeStore;
        edgeTypeStore.addType("Foo");
        edgeTypeStore.addType(8);
        edgeTypeStore.addType("Bar");

        edgeTypeStore.removeType("Foo");
        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(edgeTypeStore);

        graphStore = new GraphStore();
        ser = new Serialization(graphStore);
        EdgeTypeStore l = (EdgeTypeStore) ser.deserialize(buf);
        Assert.assertTrue(edgeTypeStore.deepEquals(l));
    }

    @Test
    public void testColumnStore() throws IOException, ClassNotFoundException {
        GraphStore graphStore = new GraphStore();

        ColumnStore columnStore = graphStore.nodeColumnStore;
        ColumnImpl col1 = new ColumnImpl("0", Integer.class, "title", 8, Origin.DATA, false, false);
        ColumnImpl col2 = new ColumnImpl("1", String.class, null, "default", Origin.PROPERTY, false, false);
        ColumnImpl col3 = new ColumnImpl("2", int[].class, null, null, Origin.PROPERTY, false, false);
        columnStore.addColumn(col1);
        columnStore.addColumn(col2);
        columnStore.addColumn(col3);
        columnStore.removeColumn(col1);

        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(columnStore);

        graphStore = new GraphStore();
        ser = new Serialization(graphStore);
        ColumnStore l = (ColumnStore) ser.deserialize(buf);
        Assert.assertTrue(columnStore.deepEquals(l));
    }

    @Test
    public void testColumn() throws IOException, ClassNotFoundException {
        ColumnImpl col = new ColumnImpl("0", Integer.class, "title", 8, Origin.DATA, false, false);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(col);
        ColumnImpl l = (ColumnImpl) ser.deserialize(buf);
        Assert.assertEquals(col, l);

        Assert.assertEquals(l.defaultValue, col.getDefaultValue());
        Assert.assertEquals(l.indexed, col.isIndexed());
        Assert.assertEquals(l.origin, col.getOrigin());
        Assert.assertEquals(l.title, col.getTitle());
        Assert.assertEquals(l.storeId, col.getStoreId());
        Assert.assertEquals(l.estimator, col.getEstimator());
    }

    @Test
    public void testViewStore() throws IOException, ClassNotFoundException {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewStore viewStore = graphStore.viewStore;
        GraphViewImpl view = viewStore.createView();
        GraphViewImpl view2 = viewStore.createView();

        Edge edge = graphStore.getEdge("0");
        view2.addNode(edge.getSource());
        view2.addNode(edge.getTarget());
        view2.addEdge(edge);

        viewStore.removeView(view);

        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(viewStore);

        graphStore = new GraphStore();
        ser = new Serialization(graphStore);
        GraphViewStore l = (GraphViewStore) ser.deserialize(buf);
        Assert.assertTrue(viewStore.deepEquals(l));
    }

    @Test
    public void testGraphView() throws IOException, ClassNotFoundException {
        GraphStore graphStore = GraphGenerator.generateSmallMultiTypeGraphStore();
        GraphViewImpl view = graphStore.viewStore.createView();

        Edge edge = graphStore.getEdge("0");
        view.addNode(edge.getSource());
        view.addNode(edge.getTarget());
        view.addEdge(edge);

        Serialization ser = new Serialization(graphStore);
        byte[] buf = ser.serialize(view);

        graphStore = new GraphStore();
        ser = new Serialization(graphStore);
        GraphViewImpl l = (GraphViewImpl) ser.deserialize(buf);
        Assert.assertTrue(view.deepEquals(l));
    }

    @Test
    public void testBitVector() throws IOException, ClassNotFoundException {
        BitVector bitVector = new BitVector(10);
        bitVector.set(1);
        bitVector.set(4);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(bitVector);
        BitVector l = (BitVector) ser.deserialize(buf);
        Assert.assertEquals(bitVector, l);
    }

    @Test
    public void testGraphVersion() throws IOException, ClassNotFoundException {
        GraphStore store = new GraphStore();
        GraphVersion version = store.version;
        version.nodeVersion = 1;
        version.edgeVersion = 2;

        Serialization ser = new Serialization(store);
        byte[] buf = ser.serialize(version);
        GraphVersion l = (GraphVersion) ser.deserialize(buf);
        Assert.assertTrue(version.deepEquals(l));
    }

    @Test
    public void testTextProperties() throws IOException, ClassNotFoundException {
        TextPropertiesImpl textProperties = new TextPropertiesImpl();
        textProperties.rgba = 100;
        textProperties.size = 3f;
        textProperties.text = "foo";
        textProperties.visible = true;

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(textProperties);
        TextPropertiesImpl l = (TextPropertiesImpl) ser.deserialize(buf);
        Assert.assertTrue(textProperties.deepEquals(l));
    }

    @Test
    public void testNodeProperties() throws IOException, ClassNotFoundException {
        NodeImpl.NodePropertiesImpl nodeProperties = new NodeImpl.NodePropertiesImpl();
        nodeProperties.x = 1f;
        nodeProperties.y = 2f;
        nodeProperties.z = 3f;
        nodeProperties.rgba = 100;
        nodeProperties.size = 4f;
        nodeProperties.fixed = true;
        nodeProperties.textProperties.rgba = 200;
        nodeProperties.textProperties.size = 5f;
        nodeProperties.textProperties.text = "foo";
        nodeProperties.textProperties.visible = true;

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(nodeProperties);
        NodeImpl.NodePropertiesImpl l = (NodeImpl.NodePropertiesImpl) ser.deserialize(buf);
        Assert.assertTrue(nodeProperties.deepEquals(l));
    }

    @Test
    public void testEdgeProperties() throws IOException, ClassNotFoundException {
        EdgeImpl.EdgePropertiesImpl edgeProperties = new EdgeImpl.EdgePropertiesImpl();
        edgeProperties.rgba = 100;
        edgeProperties.textProperties.rgba = 200;
        edgeProperties.textProperties.size = 5f;
        edgeProperties.textProperties.text = "foo";
        edgeProperties.textProperties.visible = true;

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(edgeProperties);
        EdgeImpl.EdgePropertiesImpl l = (EdgeImpl.EdgePropertiesImpl) ser.deserialize(buf);
        Assert.assertTrue(edgeProperties.deepEquals(l));
    }

    @Test
    public void testEstimator() throws IOException, ClassNotFoundException {
        Estimator estimator = Estimator.AVERAGE;

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(estimator);
        Estimator l = (Estimator) ser.deserialize(buf);
        Assert.assertEquals(estimator, l);
    }

    @Test
    public void testTimestampSet() throws IOException, ClassNotFoundException {
        TimestampSet timestampSet = new TimestampSet();
        timestampSet.add(6);
        timestampSet.add(1);
        timestampSet.add(2);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampSet l = (TimestampSet) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampBooleanSet() throws IOException, ClassNotFoundException {
        TimestampBooleanMap timestampSet = new TimestampBooleanMap();
        timestampSet.put(6, true);
        timestampSet.put(1, false);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampBooleanMap l = (TimestampBooleanMap) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampByteSet() throws IOException, ClassNotFoundException {
        TimestampByteMap timestampSet = new TimestampByteMap();
        timestampSet.put(6, (byte) 2);
        timestampSet.put(1, (byte) 1);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampByteMap l = (TimestampByteMap) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampCharSet() throws IOException, ClassNotFoundException {
        TimestampCharMap timestampSet = new TimestampCharMap();
        timestampSet.put(6, 'a');
        timestampSet.put(1, 'b');

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampCharMap l = (TimestampCharMap) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampFloatSet() throws IOException, ClassNotFoundException {
        TimestampFloatMap timestampSet = new TimestampFloatMap();
        timestampSet.put(6, 2f);
        timestampSet.put(1, 1f);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampFloatMap l = (TimestampFloatMap) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampDoubleSet() throws IOException, ClassNotFoundException {
        TimestampDoubleMap timestampSet = new TimestampDoubleMap();
        timestampSet.put(6, 2.0);
        timestampSet.put(1, 1.0);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampDoubleMap l = (TimestampDoubleMap) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampIntegerSet() throws IOException, ClassNotFoundException {
        TimestampIntegerMap timestampSet = new TimestampIntegerMap();
        timestampSet.put(6, 2);
        timestampSet.put(1, 1);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampIntegerMap l = (TimestampIntegerMap) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampLongSet() throws IOException, ClassNotFoundException {
        TimestampLongMap timestampSet = new TimestampLongMap();
        timestampSet.put(6, 2l);
        timestampSet.put(1, 1l);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampLongMap l = (TimestampLongMap) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampShortSet() throws IOException, ClassNotFoundException {
        TimestampShortMap timestampSet = new TimestampShortMap();
        timestampSet.put(6, (short) 2);
        timestampSet.put(1, (short) 1);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampShortMap l = (TimestampShortMap) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampStringSet() throws IOException, ClassNotFoundException {
        TimestampStringMap timestampSet = new TimestampStringMap();
        timestampSet.put(6, "foo");
        timestampSet.put(1, "bar");

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampStringMap l = (TimestampStringMap) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampMap() throws IOException, ClassNotFoundException {
        TimestampInternalMap timestampMap = new TimestampInternalMap();
        timestampMap.addTimestamp(6.0);
        timestampMap.addTimestamp(2.0);
        timestampMap.addTimestamp(4.0);
        timestampMap.removeTimestamp(2.0);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampInternalMap l = (TimestampInternalMap) ser.deserialize(buf);
        Assert.assertTrue(timestampMap.deepEquals(l));
    }

    @Test
    public void testGraphAttributes() throws IOException, ClassNotFoundException {
        GraphAttributesImpl graphAttributes = new GraphAttributesImpl();
        graphAttributes.setValue("foo", "bar");
        graphAttributes.setValue("A", "bar", 1.0);
        graphAttributes.setValue("B", "bar", 2.0);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(graphAttributes);
        GraphAttributesImpl l = (GraphAttributesImpl) ser.deserialize(buf);
        Assert.assertTrue(graphAttributes.deepEquals(l));
    }

    @Test
    public void testTimeFormat() throws IOException, ClassNotFoundException {
        GraphStore store = new GraphStore();
        store.timeFormat = TimeFormat.DATETIME;

        Serialization ser = new Serialization(store);
        byte[] buf = ser.serialize(store.timeFormat);
        TimeFormat l = (TimeFormat) ser.deserialize(buf);
        Assert.assertEquals(TimeFormat.DATETIME, l);
    }

    @Test
    public void testTimestampStore() throws IOException, ClassNotFoundException {
        GraphStore store = new GraphStore();
        TimestampStore timestampStore = store.timestampStore;
        timestampStore.nodeMap.getTimestampIndex(1.0);
        timestampStore.nodeMap.getTimestampIndex(2.0);
        timestampStore.edgeMap.getTimestampIndex(3.0);
        timestampStore.edgeMap.getTimestampIndex(4.0);

        Serialization ser = new Serialization(store);
        byte[] buf = ser.serialize(timestampStore);

        store = new GraphStore();
        ser = new Serialization(store);
        TimestampStore l = (TimestampStore) ser.deserialize(buf);
        Assert.assertTrue(timestampStore.deepEquals(l));
    }

    @Test
    public void testByte() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        byte[] vals = {
            -1, 0, 1, 2, Byte.MIN_VALUE, Byte.MAX_VALUE
        };
        for (byte i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Byte.class);
            Assert.assertEquals(l2, i);
        }

        for (byte i : vals) {
            byte[] array = new byte[]{i};
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == byte[].class);
            Assert.assertTrue(Arrays.equals(array, (byte[]) l2));
        }
    }

    @Test
    public void testInt() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        int[] vals = {
            Integer.MIN_VALUE,
            -Short.MIN_VALUE * 2,
            -Short.MIN_VALUE + 1,
            -Short.MIN_VALUE,
            -10, -9, -8, -7, -6, -5, -4, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            127, 254, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE + 1,
            Short.MAX_VALUE * 2, Integer.MAX_VALUE
        };
        for (int i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Integer.class);
            Assert.assertEquals(l2, i);
        }

        for (int i : vals) {
            int[] array = new int[]{i};
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == int[].class);
            Assert.assertTrue(Arrays.equals(array, (int[]) l2));
        }

        int[] longArrayByteValues = new int[256];
        longArrayByteValues[45] = 45;
        byte[] bufLongArray = ser.serialize(longArrayByteValues);
        Object l2LongArray = ser.deserialize(bufLongArray);
        Assert.assertTrue(l2LongArray.getClass() == int[].class);
        Assert.assertTrue(Arrays.equals(longArrayByteValues, (int[]) l2LongArray));
    }

    @Test
    public void testShort() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        short[] vals = {
            (short) (-Short.MIN_VALUE + 1),
            (short) -Short.MIN_VALUE,
            -10, -9, -8, -7, -6, -5, -4, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            127, 254, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE - 1,
            Short.MAX_VALUE
        };
        for (short i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Short.class);
            Assert.assertEquals(l2, i);
        }

        for (short i : vals) {
            short[] array = new short[]{i};
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == short[].class);
            Assert.assertTrue(Arrays.equals(array, (short[]) l2));
        }
    }

    @Test
    public void testDouble() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        double[] vals = {
            1f, 0f, -1f, Math.PI, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE + 1, -100
        };
        for (double i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Double.class);
            Assert.assertEquals(l2, i);
        }

        for (double i : vals) {
            double[] array = new double[]{i};
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == double[].class);
            Assert.assertTrue(Arrays.equals(array, (double[]) l2));
        }
    }

    @Test
    public void testFloat() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        float[] vals = {
            1f, 0f, -1f, (float) Math.PI, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE + 1, -100
        };
        for (float i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Float.class);
            Assert.assertEquals(l2, i);
        }

        for (float i : vals) {
            float[] array = new float[]{i};
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == float[].class);
            Assert.assertTrue(Arrays.equals(array, (float[]) l2));
        }
    }

    @Test
    public void testChar() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        char[] vals = {
            'a', ' '
        };
        for (char i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Character.class);
            Assert.assertEquals(l2, i);
        }

        for (char i : vals) {
            char[] array = new char[]{i};
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == char[].class);
            Assert.assertTrue(Arrays.equals(array, (char[]) l2));
        }
    }

    @Test
    public void testLong() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        long[] vals = {
            Long.MIN_VALUE,
            Integer.MIN_VALUE, Integer.MIN_VALUE - 1, Integer.MIN_VALUE + 1,
            -Short.MIN_VALUE * 2,
            -Short.MIN_VALUE + 1,
            -Short.MIN_VALUE,
            -10, -9, -8, -7, -6, -5, -4, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            127, 254, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE + 1,
            Short.MAX_VALUE * 2, Integer.MAX_VALUE, Integer.MAX_VALUE + 1, Long.MAX_VALUE
        };
        for (long i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Long.class);
            Assert.assertEquals(l2, i);
        }

        for (long i : vals) {
            long[] array = new long[]{i};
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == long[].class);
            Assert.assertTrue(Arrays.equals(array, (long[]) l2));
        }
    }

    @Test
    public void testBoolean1() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(true);
        Object l2 = ser.deserialize(buf);
        Assert.assertTrue(l2.getClass() == Boolean.class);
        Assert.assertEquals(l2, true);

        byte[] buf2 = ser.serialize(false);
        Object l22 = ser.deserialize(buf2);
        Assert.assertTrue(l22.getClass() == Boolean.class);
        Assert.assertEquals(l22, false);

        boolean[] array = new boolean[]{true, false, true};
        byte[] bufArray = ser.serialize(array);
        Object l2Array = ser.deserialize(bufArray);
        Assert.assertTrue(l2Array.getClass() == boolean[].class);
        Assert.assertTrue(Arrays.equals(array, (boolean[]) l2Array));
    }

    @Test
    public void testString() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize("Abcd");
        String l2 = (String) ser.deserialize(buf);
        Assert.assertEquals(l2, "Abcd");

        String[] array = new String[]{"foo", "bar"};
        byte[] bufArray = ser.serialize(array);
        Object l2Array = ser.deserialize(bufArray);
        Assert.assertTrue(l2Array.getClass() == Object[].class);
        Assert.assertTrue(Arrays.equals(array, (Object[]) l2Array));
    }

    @Test
    public void testStringEmpty() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize("");
        String l2 = (String) ser.deserialize(buf);
        Assert.assertEquals(l2, "");
    }

    @Test
    public void testBigString() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        String bigString = "";
        for (int i = 0; i < 1e4; i++) {
            bigString += i % 10;
        }
        byte[] buf = ser.serialize(bigString);
        String l2 = (String) ser.deserialize(buf);
        Assert.assertEquals(l2, bigString);
    }

    @Test
    public void testClass() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(String.class);
        Class l2 = (Class) ser.deserialize(buf);
        Assert.assertEquals(l2, String.class);
    }

    @Test
    public void testClass2() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(long[].class);
        Class l2 = (Class) ser.deserialize(buf);
        Assert.assertEquals(l2, long[].class);
    }

    @Test
    public void testUnicodeString() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        String s = "Ciudad Bolíva";
        byte[] buf = ser.serialize(s);
        Object l2 = ser.deserialize(buf);
        Assert.assertEquals(l2, s);
    }

    @Test
    public void testBooleanArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        boolean[] l = new boolean[]{true, false};
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (boolean[]) deserialize));
    }

    @Test
    public void testDoubleArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        double[] l = new double[]{Math.PI, 1D};
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (double[]) deserialize));
    }

    @Test
    public void testFloatArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        float[] l = new float[]{1F, 1.234235F};
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (float[]) deserialize));
    }

    @Test
    public void testByteArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        byte[] l = new byte[]{1, 34, -5};
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (byte[]) deserialize));
    }

    @Test
    public void testCharArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        char[] l = new char[]{'1', 'a', '&'};
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (char[]) deserialize));
    }

    @Test
    public void testDate() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        Date d = new Date(6546565565656L);
        Assert.assertEquals(d, ser.deserialize(ser.serialize(d)));
        d = new Date(System.currentTimeMillis());
        Assert.assertEquals(d, ser.deserialize(ser.serialize(d)));
    }

    @Test
    public void testBigDecimal() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        BigDecimal d = new BigDecimal("445656.7889889895165654423236");
        Assert.assertEquals(d, ser.deserialize(ser.serialize(d)));
        d = new BigDecimal("-53534534534534445656.7889889895165654423236");
        Assert.assertEquals(d, ser.deserialize(ser.serialize(d)));
    }

    @Test
    public void testBigInteger() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        BigInteger d = new BigInteger("4456567889889895165654423236");
        Assert.assertEquals(d, ser.deserialize(ser.serialize(d)));
        d = new BigInteger("-535345345345344456567889889895165654423236");
        Assert.assertEquals(d, ser.deserialize(ser.serialize(d)));
    }

    @Test
    public void testLocale() throws Exception {
        Serialization ser = new Serialization(null);
        Assert.assertEquals(Locale.FRANCE, ser.deserialize(ser.serialize(Locale.FRANCE)));
        Assert.assertEquals(Locale.CANADA_FRENCH, ser.deserialize(ser.serialize(Locale.CANADA_FRENCH)));
        Assert.assertEquals(Locale.SIMPLIFIED_CHINESE, ser.deserialize(ser.serialize(Locale.SIMPLIFIED_CHINESE)));
    }
}
