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

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanOpenHashSet;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphModel;
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
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.UnsupportedFormatVersionException;
import org.gephi.graph.api.types.IntervalBooleanMap;
import org.gephi.graph.api.types.IntervalByteMap;
import org.gephi.graph.api.types.IntervalCharMap;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalFloatMap;
import org.gephi.graph.api.types.IntervalIntegerMap;
import org.gephi.graph.api.types.IntervalLongMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.IntervalShortMap;
import org.gephi.graph.api.types.IntervalStringMap;
import org.gephi.graph.impl.utils.DataInputOutput;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class SerializationTest {

    @Test
    public void testEdgeStoreMixed() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;

        NodeStore nodeStore = graphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeImpl[] nodes = GraphGenerator.generateNodeList(5100);
        nodeStore.addAll(Arrays.asList(nodes));
        EdgeImpl[] edges = GraphGenerator.generateMixedEdgeList(nodeStore, 9000, 0, true);
        edgeStore.addAll(Arrays.asList(edges));

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(graphStore);

        GraphModelImpl graphModel2 = new GraphModelImpl();
        ser = new Serialization(graphModel2);
        GraphStore l = (GraphStore) ser.deserialize(buf);
        Assert.assertTrue(nodeStore.deepEquals(l.nodeStore));
        Assert.assertTrue(edgeStore.deepEquals(l.edgeStore));
    }

    @Test
    public void testEdgeStoreMultipleTypes() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;

        NodeStore nodeStore = graphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeImpl[] nodes = GraphGenerator.generateNodeList(5100);
        nodeStore.addAll(Arrays.asList(nodes));
        EdgeImpl[] edges = GraphGenerator.generateMultiTypeEdgeList(nodeStore, 9000, 3, true, true);
        edgeStore.addAll(Arrays.asList(edges));

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(graphStore);

        GraphModelImpl graphModel2 = new GraphModelImpl();
        ser = new Serialization(graphModel2);
        GraphStore l = (GraphStore) ser.deserialize(buf);
        Assert.assertTrue(nodeStore.deepEquals(l.nodeStore));
        Assert.assertTrue(edgeStore.deepEquals(l.edgeStore));
    }

    @Test
    public void testEdgeStore() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;

        NodeStore nodeStore = graphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeImpl[] nodes = GraphGenerator.generateSmallNodeList();
        nodeStore.addAll(Arrays.asList(nodes));
        EdgeImpl[] edges = GraphGenerator.generateEdgeList(nodeStore, 100, 0, true, true, false);
        edgeStore.addAll(Arrays.asList(edges));

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(graphStore);

        GraphModelImpl graphModel2 = new GraphModelImpl();
        ser = new Serialization(graphModel2);
        GraphStore l = (GraphStore) ser.deserialize(buf);
        Assert.assertTrue(nodeStore.deepEquals(l.nodeStore));
        Assert.assertTrue(edgeStore.deepEquals(l.edgeStore));
    }

    @Test
    public void testNodeStore() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;

        NodeStore nodeStore = graphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeImpl[] nodes = GraphGenerator.generateSmallNodeList();
        nodeStore.addAll(Arrays.asList(nodes));

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(graphStore);

        graphModel = new GraphModelImpl();
        ser = new Serialization(graphModel);
        GraphStore l = (GraphStore) ser.deserialize(buf);
        Assert.assertTrue(nodeStore.deepEquals(l.nodeStore));
        Assert.assertTrue(edgeStore.deepEquals(l.edgeStore));
    }

    @Test
    public void testNode() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;
        ColumnStore columnStore = graphStore.nodeTable.store;
        ColumnImpl col1 = new ColumnImpl("0", Integer.class, "title", 8, Origin.DATA, false, false);
        ColumnImpl col2 = new ColumnImpl("1", String.class, null, "default", Origin.PROPERTY, false, false);
        ColumnImpl col3 = new ColumnImpl("2", int[].class, null, null, Origin.PROPERTY, false, false);
        columnStore.addColumn(col1);
        columnStore.addColumn(col2);
        columnStore.addColumn(col3);

        NodeImpl node = new NodeImpl("Foo", graphStore);
        node.setAttribute(col1, 1);
        node.setAttribute(col3, new int[] { 1, 7, 3, 4 });

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(node);

        graphModel = new GraphModelImpl();
        ser = new Serialization(graphModel);
        NodeImpl l = (NodeImpl) ser.deserialize(buf);
        Assert.assertTrue(node.equals(l));
        Assert.assertTrue(Arrays.deepEquals(l.getAttributes(), node.getAttributes()));
    }

    @Test
    public void testGraphFactory() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;
        GraphFactoryImpl factory = graphStore.factory;

        factory.setNodeCounter(100);
        factory.setEdgeCounter(50);

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(factory);

        graphModel = new GraphModelImpl();
        ser = new Serialization(graphModel);
        GraphFactoryImpl l = (GraphFactoryImpl) ser.deserialize(buf);
        Assert.assertTrue(factory.deepEquals(l));
    }

    @Test
    public void testEdgeTypeStore() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;

        EdgeTypeStore edgeTypeStore = graphStore.edgeTypeStore;
        edgeTypeStore.addType("Foo");
        edgeTypeStore.addType("Bar");

        edgeTypeStore.removeType("Foo");
        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(edgeTypeStore);

        graphModel = new GraphModelImpl();
        ser = new Serialization(graphModel);
        EdgeTypeStore l = (EdgeTypeStore) ser.deserialize(buf);
        Assert.assertTrue(edgeTypeStore.deepEquals(l));
    }

    @Test
    public void testTable() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;

        ColumnStore columnStore = graphStore.nodeTable.store;
        ColumnImpl col1 = new ColumnImpl("0", Integer.class, "title", 8, Origin.DATA, false, false);
        ColumnImpl col2 = new ColumnImpl("1", String.class, null, "default", Origin.PROPERTY, false, false);
        ColumnImpl col3 = new ColumnImpl("2", int[].class, null, null, Origin.PROPERTY, false, false);
        columnStore.addColumn(col1);
        columnStore.addColumn(col2);
        columnStore.addColumn(col3);
        columnStore.removeColumn(col1);

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(graphStore.nodeTable);

        graphModel = new GraphModelImpl();
        ser = new Serialization(graphModel);
        TableImpl l = (TableImpl) ser.deserialize(buf);
        Assert.assertTrue(graphStore.nodeTable.deepEquals(l));

        ColumnImpl c = (ColumnImpl) l.getColumn("1");
        Assert.assertEquals(c.defaultValue, col2.getDefaultValue());
        Assert.assertEquals(c.indexed, col2.isIndexed());
        Assert.assertEquals(c.origin, col2.getOrigin());
        Assert.assertEquals(c.title, col2.getTitle());
        Assert.assertEquals(c.storeId, col2.getStoreId());
        Assert.assertEquals(c.estimator, col2.getEstimator());
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
        view2.setTimeInterval(new Interval(1.0, 4.0));

        viewStore.removeView(view);

        Serialization ser = new Serialization(graphStore.graphModel);
        byte[] buf = ser.serialize(viewStore);

        GraphModelImpl graphModel = new GraphModelImpl();
        ser = new Serialization(graphModel);
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

        Serialization ser = new Serialization(graphStore.graphModel);
        byte[] buf = ser.serialize(view);

        GraphModelImpl graphModel = new GraphModelImpl();
        ser = new Serialization(graphModel);
        GraphViewImpl l = (GraphViewImpl) ser.deserialize(buf);
        Assert.assertTrue(view.deepEquals(l));
    }

    @Test
    public void testBitSet() throws IOException, ClassNotFoundException {
        BitSet bitVector = new BitSet(10);
        bitVector.set(1);
        bitVector.set(4);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(bitVector);
        BitSet l = (BitSet) ser.deserialize(buf);
        Assert.assertEquals(bitVector, l);
    }

    @Test
    public void testGraphVersion() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore store = graphModel.store;
        GraphVersion version = store.version;
        version.nodeVersion = 1;
        version.edgeVersion = 2;

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(version);
        GraphVersion l = (GraphVersion) ser.deserialize(buf);
        Assert.assertTrue(version.deepEquals(l));
    }

    @Test
    public void testTextProperties() throws IOException, ClassNotFoundException {
        TextPropertiesImpl textProperties = new TextPropertiesImpl();
        textProperties.rgba = 0x01FF0000;
        textProperties.size = 3f;
        textProperties.text = "foo";
        textProperties.visible = true;
        textProperties.width = 5;
        textProperties.height = 8;

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
        nodeProperties.textProperties.rgba = 0x01FF0000;
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
        edgeProperties.rgba = 0x01FF0000;
        edgeProperties.textProperties.rgba = 0x01FF0000;
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
    public void testTimestampRepresentation() throws IOException, ClassNotFoundException {
        TimeRepresentation timeRepresentation = TimeRepresentation.INTERVAL;

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timeRepresentation);
        TimeRepresentation l = (TimeRepresentation) ser.deserialize(buf);
        Assert.assertEquals(timeRepresentation, l);
    }

    @Test
    public void testTimestampSet() throws IOException, ClassNotFoundException {
        TimestampSet timestampSet = new TimestampSet();
        timestampSet.add(6.0);
        timestampSet.add(1.0);
        timestampSet.add(2.0);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampSet);
        TimestampSet l = (TimestampSet) ser.deserialize(buf);
        Assert.assertEquals(timestampSet, l);
    }

    @Test
    public void testTimestampBooleanMap() throws IOException, ClassNotFoundException {
        TimestampBooleanMap timestampMap = new TimestampBooleanMap();
        timestampMap.put(6.0, true);
        timestampMap.put(1.0, false);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampBooleanMap l = (TimestampBooleanMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testTimestampByteMap() throws IOException, ClassNotFoundException {
        TimestampByteMap timestampMap = new TimestampByteMap();
        timestampMap.put(6.0, (byte) 2);
        timestampMap.put(1.0, (byte) 1);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampByteMap l = (TimestampByteMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testTimestampCharMap() throws IOException, ClassNotFoundException {
        TimestampCharMap timestampMap = new TimestampCharMap();
        timestampMap.put(6.0, 'a');
        timestampMap.put(1.0, 'b');

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampCharMap l = (TimestampCharMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testTimestampFloatMap() throws IOException, ClassNotFoundException {
        TimestampFloatMap timestampMap = new TimestampFloatMap();
        timestampMap.put(6.0, 2f);
        timestampMap.put(1.0, 1f);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampFloatMap l = (TimestampFloatMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testTimestampDoubleMap() throws IOException, ClassNotFoundException {
        TimestampDoubleMap timestampMap = new TimestampDoubleMap();
        timestampMap.put(6.0, 2.0);
        timestampMap.put(1.0, 1.0);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampDoubleMap l = (TimestampDoubleMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testTimestampIntegerMap() throws IOException, ClassNotFoundException {
        TimestampIntegerMap timestampMap = new TimestampIntegerMap();
        timestampMap.put(6.0, 2);
        timestampMap.put(1.0, 1);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampIntegerMap l = (TimestampIntegerMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testTimestampLongMap() throws IOException, ClassNotFoundException {
        TimestampLongMap timestampMap = new TimestampLongMap();
        timestampMap.put(6.0, 2l);
        timestampMap.put(1.0, 1l);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampLongMap l = (TimestampLongMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testTimestampShortMap() throws IOException, ClassNotFoundException {
        TimestampShortMap timestampMap = new TimestampShortMap();
        timestampMap.put(6.0, (short) 2);
        timestampMap.put(1.0, (short) 1);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampShortMap l = (TimestampShortMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testTimestampStringMap() throws IOException, ClassNotFoundException {
        TimestampStringMap timestampMap = new TimestampStringMap();
        timestampMap.put(6.0, "foo");
        timestampMap.put(1.0, "bar");

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        TimestampStringMap l = (TimestampStringMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testIntervalSet() throws IOException, ClassNotFoundException {
        IntervalSet intervalSet = new IntervalSet();
        intervalSet.add(new Interval(1.0, 2.0));
        intervalSet.add(new Interval(4.0, 6.0));
        intervalSet.add(new Interval(3.0, 4.0));

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(intervalSet);
        IntervalSet l = (IntervalSet) ser.deserialize(buf);
        Assert.assertEquals(intervalSet, l);
    }

    @Test
    public void testIntervalBooleanMap() throws IOException, ClassNotFoundException {
        IntervalBooleanMap timestampMap = new IntervalBooleanMap();
        timestampMap.put(new Interval(6, 7), true);
        timestampMap.put(new Interval(1, 2), false);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        IntervalBooleanMap l = (IntervalBooleanMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testIntervalByteMap() throws IOException, ClassNotFoundException {
        IntervalByteMap timestampMap = new IntervalByteMap();
        timestampMap.put(new Interval(6, 7), (byte) 2);
        timestampMap.put(new Interval(1, 2), (byte) 1);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        IntervalByteMap l = (IntervalByteMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testIntervalCharMap() throws IOException, ClassNotFoundException {
        IntervalCharMap timestampMap = new IntervalCharMap();
        timestampMap.put(new Interval(6, 7), 'a');
        timestampMap.put(new Interval(1, 2), 'b');

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        IntervalCharMap l = (IntervalCharMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testIntervalFloatMap() throws IOException, ClassNotFoundException {
        IntervalFloatMap timestampMap = new IntervalFloatMap();
        timestampMap.put(new Interval(6, 7), 2f);
        timestampMap.put(new Interval(1, 2), 1f);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        IntervalFloatMap l = (IntervalFloatMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testIntervalDoubleMap() throws IOException, ClassNotFoundException {
        IntervalDoubleMap timestampMap = new IntervalDoubleMap();
        timestampMap.put(new Interval(6, 7), 2.0);
        timestampMap.put(new Interval(1, 2), 1.0);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        IntervalDoubleMap l = (IntervalDoubleMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testIntervalIntegerMap() throws IOException, ClassNotFoundException {
        IntervalIntegerMap timestampMap = new IntervalIntegerMap();
        timestampMap.put(new Interval(6, 7), 2);
        timestampMap.put(new Interval(1, 2), 1);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        IntervalIntegerMap l = (IntervalIntegerMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testIntervalLongMap() throws IOException, ClassNotFoundException {
        IntervalLongMap timestampMap = new IntervalLongMap();
        timestampMap.put(new Interval(6, 7), 2l);
        timestampMap.put(new Interval(1, 2), 1l);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        IntervalLongMap l = (IntervalLongMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testIntervalShortMap() throws IOException, ClassNotFoundException {
        IntervalShortMap timestampMap = new IntervalShortMap();
        timestampMap.put(new Interval(6, 7), (short) 2);
        timestampMap.put(new Interval(1, 2), (short) 1);

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        IntervalShortMap l = (IntervalShortMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testIntervalStringMap() throws IOException, ClassNotFoundException {
        IntervalStringMap timestampMap = new IntervalStringMap();
        timestampMap.put(new Interval(6, 7), "foo");
        timestampMap.put(new Interval(1, 2), "bar");

        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(timestampMap);
        IntervalStringMap l = (IntervalStringMap) ser.deserialize(buf);
        Assert.assertEquals(timestampMap, l);
    }

    @Test
    public void testInstant() throws IOException, ClassNotFoundException {
        Instant instant = Instant.ofEpochSecond(1234567890, 44553);
        Serialization ser = new Serialization(null);
        byte[] buf = ser.serialize(instant);
        Instant l = (Instant) ser.deserialize(buf);
        Assert.assertEquals(instant, l);
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
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore store = graphModel.store;
        store.timeFormat = TimeFormat.DATETIME;

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(store.timeFormat);
        TimeFormat l = (TimeFormat) ser.deserialize(buf);
        Assert.assertEquals(TimeFormat.DATETIME, l);
    }

    @Test
    public void testTimeZone() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore store = graphModel.store;
        store.timeZone = ZoneId.of("+01:30");

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(store.timeZone);
        ZoneId l = (ZoneId) ser.deserialize(buf);
        Assert.assertEquals(ZoneId.of("+01:30"), l);
    }

    /**
     * The time store block is derived state, rebuilt from the elements as they are inserted. Its content is not
     * written, read standalone it carries nothing, and it must consume exactly its own bytes:
     * Serialization.deserialize(byte[]) throws if any are left.
     */
    @Test
    public void testTimestampStore() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore store = graphModel.store;
        TimeStore timestampStore = store.timeStore;
        timestampStore.nodeIndexStore.add(6.0);
        timestampStore.nodeIndexStore.add(2.0);
        timestampStore.nodeIndexStore.add(4.0);
        timestampStore.nodeIndexStore.add(4.0);
        timestampStore.nodeIndexStore.remove(2.0);
        timestampStore.edgeIndexStore.add(3.0);
        timestampStore.edgeIndexStore.add(4.0);

        Assert.assertFalse(timestampStore.isEmpty());
        byte[] buf = new Serialization(graphModel).serialize(timestampStore);

        // The index is derived state: its content leaves no trace in the bytes and reading yields an empty store
        GraphModelImpl empty = new GraphModelImpl();
        Assert.assertEquals(buf, new Serialization(empty).serialize(empty.store.timeStore));

        GraphModelImpl readModel = new GraphModelImpl();
        TimeStore l = (TimeStore) new Serialization(readModel).deserialize(buf);
        Assert.assertTrue(l.isEmpty());
    }

    @Test
    public void testIntervalStore() throws IOException, ClassNotFoundException {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModel = new GraphModelImpl(config);
        GraphStore store = graphModel.store;
        TimeStore timestampStore = store.timeStore;
        timestampStore.nodeIndexStore.add(new Interval(1.0, 6.0));
        timestampStore.nodeIndexStore.add(new Interval(3.0, 4.0));
        timestampStore.nodeIndexStore.add(new Interval(5.0, 6.0));
        timestampStore.nodeIndexStore.add(new Interval(5.0, 6.0));
        timestampStore.nodeIndexStore.remove(new Interval(3.0, 4.0));
        timestampStore.edgeIndexStore.add(new Interval(2.0, 3.0));
        timestampStore.edgeIndexStore.add(new Interval(2.0, 3.0));

        Assert.assertFalse(timestampStore.isEmpty());
        byte[] buf = new Serialization(graphModel).serialize(timestampStore);

        // The index is derived state: its content leaves no trace in the bytes and reading yields an empty store
        GraphModelImpl empty = new GraphModelImpl(config);
        Assert.assertEquals(buf, new Serialization(empty).serialize(empty.store.timeStore));

        GraphModelImpl readModel = new GraphModelImpl(config);
        TimeStore l = (TimeStore) new Serialization(readModel).deserialize(buf);
        Assert.assertTrue(l.isEmpty());
    }

    @Test
    public void testGraphStoreTimestampIndexCounts() throws IOException, ClassNotFoundException {
        assertTimeIndexCountsSurviveRoundTrip(TimeRepresentation.TIMESTAMP);
    }

    @Test
    public void testGraphStoreIntervalIndexCounts() throws IOException, ClassNotFoundException {
        assertTimeIndexCountsSurviveRoundTrip(TimeRepresentation.INTERVAL);
    }

    @Test
    public void testGraphStoreTimestampIndexRemoveAfterRoundTrip() throws IOException, ClassNotFoundException {
        assertTimeIsRemovedFromIndexAfterRoundTrip(TimeRepresentation.TIMESTAMP);
    }

    @Test
    public void testGraphStoreIntervalIndexRemoveAfterRoundTrip() throws IOException, ClassNotFoundException {
        assertTimeIsRemovedFromIndexAfterRoundTrip(TimeRepresentation.INTERVAL);
    }

    @Test
    public void testGraphStoreTimestampIndexIsRebuiltOnRead() throws IOException, ClassNotFoundException {
        assertTimeIndexIsRebuiltOnRead(TimeRepresentation.TIMESTAMP);
    }

    @Test
    public void testGraphStoreIntervalIndexIsRebuiltOnRead() throws IOException, ClassNotFoundException {
        assertTimeIndexIsRebuiltOnRead(TimeRepresentation.INTERVAL);
    }

    /**
     * Files written before #288 carry reference counts above the number of references, and time values nothing points
     * at. Reading rebuilds the index from the elements, which drops both. The index is inflated directly here, since
     * the write path no longer produces that state.
     */
    private void assertTimeIndexIsRebuiltOnRead(TimeRepresentation timeRepresentation) throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl(
                Configuration.builder().timeRepresentation(timeRepresentation).build());
        GraphStore graphStore = graphModel.store;
        boolean interval = timeRepresentation.equals(TimeRepresentation.INTERVAL);

        Column column = graphStore.nodeTable
                .addColumn("dynamic", interval ? IntervalIntegerMap.class : TimestampIntegerMap.class);
        NodeImpl node1 = (NodeImpl) graphStore.factory.newNode("1");
        NodeImpl node2 = (NodeImpl) graphStore.factory.newNode("2");
        graphStore.addNode(node1);
        graphStore.addNode(node2);

        setTimeAttribute(node1, column, timeRepresentation, 1.0, 10);
        setTimeAttribute(node1, column, timeRepresentation, 2.0, 20);
        setTimeAttribute(node1, column, timeRepresentation, 3.0, 30);
        addTime(node2, timeRepresentation, 2.0);

        Object first = timeKey(timeRepresentation, 1.0);
        Object shared = timeKey(timeRepresentation, 2.0);
        Object last = timeKey(timeRepresentation, 3.0);
        Object stranded = timeKey(timeRepresentation, 7.0);

        // Emulate a store written before #288: a count above the number of references, and a time no element holds
        TimeIndexStore nodeIndexStore = graphStore.timeStore.nodeIndexStore;
        nodeIndexStore.add(first);
        nodeIndexStore.add(stranded);
        Assert.assertEquals(nodeIndexStore.countMap[(Integer) nodeIndexStore.timeSortedMap.get(first)], 2);
        Assert.assertTrue(nodeIndexStore.contains(stranded));

        GraphStore read = roundTrip(graphStore, timeRepresentation);
        TimeIndexStore readIndexStore = read.timeStore.nodeIndexStore;

        // Rebuilt from the elements: node 1 holds 1.0, 2.0 and 3.0, node 2 holds 2.0
        Assert.assertFalse(readIndexStore.contains(stranded));
        Assert.assertEquals(readIndexStore.size(), 3);
        Assert.assertEquals(readIndexStore.countMap[(Integer) readIndexStore.timeSortedMap.get(first)], 1);
        Assert.assertEquals(readIndexStore.countMap[(Integer) readIndexStore.timeSortedMap.get(shared)], 2);
        Assert.assertEquals(readIndexStore.countMap[(Integer) readIndexStore.timeSortedMap.get(last)], 1);

        // The only reference to 3.0 is node 1's map, so dropping it frees the time
        removeTimeAttribute(read.getNode("1"), read.nodeTable.getColumn("dynamic"), timeRepresentation, 3.0);
        Assert.assertFalse(readIndexStore.contains(last));
    }

    private void assertTimeIndexCountsSurviveRoundTrip(TimeRepresentation timeRepresentation) throws IOException, ClassNotFoundException {
        GraphStore graphStore = newTimeIndexGraphStore(timeRepresentation);
        GraphStore read = roundTrip(graphStore, timeRepresentation);

        assertTimeIndexCountsEqual(graphStore.timeStore.nodeIndexStore, read.timeStore.nodeIndexStore);
        assertTimeIndexCountsEqual(graphStore.timeStore.edgeIndexStore, read.timeStore.edgeIndexStore);
    }

    private void assertTimeIsRemovedFromIndexAfterRoundTrip(TimeRepresentation timeRepresentation) throws IOException, ClassNotFoundException {
        GraphStore read = roundTrip(newTimeIndexGraphStore(timeRepresentation), timeRepresentation);
        TimeIndexStore nodeIndexStore = read.timeStore.nodeIndexStore;

        // Time 2.0 is referenced by two nodes and nothing else
        Object timeKey = timeKey(timeRepresentation, 2.0);
        Assert.assertTrue(nodeIndexStore.contains(timeKey));
        int timeIndex = (Integer) nodeIndexStore.timeSortedMap.get(timeKey);

        removeTime(read.getNode("1"), timeRepresentation, 2.0);
        Assert.assertTrue(nodeIndexStore.contains(timeKey));
        removeTime(read.getNode("2"), timeRepresentation, 2.0);

        Assert.assertFalse(nodeIndexStore.contains(timeKey));
        Assert.assertTrue(nodeIndexStore.garbageQueue.contains(timeIndex));
    }

    /**
     * Compares the time values and their reference counts. Slot numbering and the garbage queue are allocation state,
     * not part of the contract: the index is rebuilt from the elements, so slots may be numbered differently.
     */
    private static void assertTimeIndexCountsEqual(TimeIndexStore expected, TimeIndexStore actual) {
        // With equal sizes, resolving every expected time value in actual makes this a full set comparison
        Assert.assertEquals(actual.size(), expected.size());
        for (Object o : expected.timeSortedMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object timeKey = entry.getKey();
            Object actualTimeIndex = actual.timeSortedMap.get(timeKey);
            Assert.assertNotNull(actualTimeIndex, "Missing time index for " + timeKey);
            Assert.assertEquals(actual.countMap[(Integer) actualTimeIndex], expected.countMap[(Integer) entry
                    .getValue()], "Reference count for " + timeKey);
        }
    }

    /**
     * Builds a store with element time sets, a dynamic column, times shared by several elements, a parallel edge, a
     * freed time index and a dynamic time set twice.
     */
    private static GraphStore newTimeIndexGraphStore(TimeRepresentation timeRepresentation) {
        GraphModelImpl graphModel = new GraphModelImpl(
                Configuration.builder().timeRepresentation(timeRepresentation).build());
        GraphStore graphStore = graphModel.store;
        boolean interval = timeRepresentation.equals(TimeRepresentation.INTERVAL);

        Column column = graphStore.nodeTable
                .addColumn("dynamic", interval ? IntervalIntegerMap.class : TimestampIntegerMap.class);

        NodeImpl node1 = (NodeImpl) graphStore.factory.newNode("1");
        NodeImpl node2 = (NodeImpl) graphStore.factory.newNode("2");
        NodeImpl node3 = (NodeImpl) graphStore.factory.newNode("3");
        graphStore.addNode(node1);
        graphStore.addNode(node2);
        graphStore.addNode(node3);

        EdgeImpl edge1 = (EdgeImpl) graphStore.factory.newEdge("1", node1, node2, 0, 1.0, true);
        EdgeImpl edge2 = (EdgeImpl) graphStore.factory
                .newEdge("2", node1, node2, graphStore.edgeTypeStore.addType("type"), 1.0, true);
        graphStore.addEdge(edge1);
        graphStore.addEdge(edge2);

        addTime(node1, timeRepresentation, 1.0);
        addTime(node1, timeRepresentation, 2.0);
        addTime(node2, timeRepresentation, 2.0);
        addTime(edge1, timeRepresentation, 1.0);
        addTime(edge2, timeRepresentation, 1.0);
        addTime(edge2, timeRepresentation, 3.0);

        // Frees a time index, leaving the garbage queue non-empty
        addTime(node3, timeRepresentation, 4.0);
        removeTime(node3, timeRepresentation, 4.0);

        if (interval) {
            IntervalIntegerMap map = new IntervalIntegerMap();
            map.put(new Interval(5.0, 5.0), 10);
            map.put(new Interval(6.0, 6.0), 20);
            node1.setAttribute(column, map);
        } else {
            TimestampIntegerMap map = new TimestampIntegerMap();
            map.put(5.0, 10);
            map.put(6.0, 20);
            node1.setAttribute(column, map);
        }

        // 5.0 is already in the map and stays at one reference, 8.0 adds one
        setTimeAttribute(node1, column, timeRepresentation, 5.0, 30);
        setTimeAttribute(node1, column, timeRepresentation, 8.0, 40);

        return graphStore;
    }

    private static GraphStore roundTrip(GraphStore graphStore, TimeRepresentation timeRepresentation) throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(graphStore.graphModel);
        byte[] buf = ser.serialize(graphStore);

        GraphModelImpl readModel = new GraphModelImpl(
                Configuration.builder().timeRepresentation(timeRepresentation).build());
        return (GraphStore) new Serialization(readModel).deserialize(buf);
    }

    private static Object timeKey(TimeRepresentation timeRepresentation, double time) {
        return timeRepresentation.equals(TimeRepresentation.INTERVAL) ? new Interval(time, time) : (Double) time;
    }

    private static void addTime(ElementImpl element, TimeRepresentation timeRepresentation, double time) {
        if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
            element.addInterval(new Interval(time, time));
        } else {
            element.addTimestamp(time);
        }
    }

    private static void setTimeAttribute(ElementImpl element, Column column, TimeRepresentation timeRepresentation, double time, int value) {
        if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
            element.setAttribute(column, value, new Interval(time, time));
        } else {
            element.setAttribute(column, value, time);
        }
    }

    private static void removeTimeAttribute(ElementImpl element, Column column, TimeRepresentation timeRepresentation, double time) {
        if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
            element.removeAttribute(column, new Interval(time, time));
        } else {
            element.removeAttribute(column, time);
        }
    }

    private static void removeTime(ElementImpl element, TimeRepresentation timeRepresentation, double time) {
        if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
            element.removeInterval(new Interval(time, time));
        } else {
            element.removeTimestamp(time);
        }
    }

    @Test
    public void testConfiguration() throws IOException, ClassNotFoundException {
        GraphModelImpl graphModel = new GraphModelImpl(Configuration.builder().nodeIdType(Float.class)
                .edgeIdType(Long.class).timeRepresentation(TimeRepresentation.INTERVAL).build());

        Serialization ser = new Serialization(graphModel);
        byte[] buf = ser.serialize(graphModel.configuration);

        ser = new Serialization(new GraphModelImpl());
        ConfigurationImpl l = (ConfigurationImpl) ser.deserialize(buf);
        Assert.assertTrue(graphModel.configuration.equals(l));
    }

    @Test
    public void testList() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);

        List emptyList = new ArrayList();
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(emptyList)), emptyList);

        List intList = new ArrayList();
        intList.add(5);
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(intList)), intList);

        List stringList = new ArrayList();
        stringList.add("foo");
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(stringList)), stringList);

        List mixedList = new ArrayList();
        mixedList.add("foo");
        mixedList.add(42);
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(mixedList)), mixedList);

        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new IntArrayList(new int[] { 42 }))), new IntArrayList(new int[] { 42 }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new FloatArrayList(new float[] { 42f }))), new FloatArrayList(new float[] { 42f }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new DoubleArrayList(new double[] { 42.0 }))), new DoubleArrayList(new double[] { 42.0 }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new ShortArrayList(new short[] { 42 }))), new ShortArrayList(new short[] { 42 }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new ByteArrayList(new byte[] { 42 }))), new ByteArrayList(new byte[] { 42 }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new LongArrayList(new long[] { 42l }))), new LongArrayList(new long[] { 42l }));
        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new BooleanArrayList(new boolean[] { true }))), new BooleanArrayList(
                        new boolean[] { true }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new CharArrayList(new char[] { 'a' }))), new CharArrayList(new char[] { 'a' }));
    }

    @Test
    public void testSet() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);

        Set emptySet = new HashSet();
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(emptySet)), emptySet);

        Set intSet = new ObjectOpenHashSet();
        intSet.add(5);
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(intSet)), intSet);

        Set stringSet = new ObjectOpenHashSet();
        stringSet.add("foo");
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(stringSet)), stringSet);

        Set mixedSet = new ObjectOpenHashSet();
        mixedSet.add("foo");
        mixedSet.add(42);
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(mixedSet)), mixedSet);

        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new IntOpenHashSet(new int[] { 42 }))), new IntOpenHashSet(new int[] { 42 }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new FloatOpenHashSet(new float[] { 42f }))), new FloatOpenHashSet(new float[] { 42f }));
        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new DoubleOpenHashSet(new double[] { 42.0 }))), new DoubleOpenHashSet(
                        new double[] { 42.0 }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new ShortOpenHashSet(new short[] { 42 }))), new ShortOpenHashSet(new short[] { 42 }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new ByteOpenHashSet(new byte[] { 42 }))), new ByteOpenHashSet(new byte[] { 42 }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new LongOpenHashSet(new long[] { 42l }))), new LongOpenHashSet(new long[] { 42l }));
        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new BooleanOpenHashSet(new boolean[] { true }))), new BooleanOpenHashSet(
                        new boolean[] { true }));
        Assert.assertEquals(new Serialization(null).deserialize(ser
                .serialize(new CharOpenHashSet(new char[] { 'a' }))), new CharOpenHashSet(new char[] { 'a' }));
    }

    @Test
    public void testMap() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);

        Map emptyMap = new HashMap();
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(emptyMap)), emptyMap);

        Map intMap = new Object2ObjectOpenHashMap();
        intMap.put(5, 5);
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(intMap)), intMap);

        Map stringMap = new Object2ObjectOpenHashMap();
        stringMap.put("foo", "bar");
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(stringMap)), stringMap);

        Map mixedMap = new Object2ObjectOpenHashMap();
        mixedMap.put("foo", "bar");
        mixedMap.put(42, 42);
        Assert.assertEquals(new Serialization(null).deserialize(ser.serialize(mixedMap)), mixedMap);

        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new Int2ObjectOpenHashMap(new int[] { 42 },
                        new Object[] { "foo" }))), new Int2ObjectOpenHashMap(new int[] { 42 }, new Object[] { "foo" }));
        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new Float2ObjectOpenHashMap(new float[] { 42f },
                        new Object[] { "foo" }))), new Float2ObjectOpenHashMap(new float[] { 42f },
                                new Object[] { "foo" }));
        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new Double2ObjectOpenHashMap(new double[] { 42.0 },
                        new Object[] { "foo" }))), new Double2ObjectOpenHashMap(new double[] { 42.0 },
                                new Object[] { "foo" }));
        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new Short2ObjectOpenHashMap(new short[] { 42 },
                        new Object[] { "foo" }))), new Short2ObjectOpenHashMap(new short[] { 42 },
                                new Object[] { "foo" }));
        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new Byte2ObjectOpenHashMap(new byte[] { 42 },
                        new Object[] { "foo" }))), new Byte2ObjectOpenHashMap(new byte[] { 42 },
                                new Object[] { "foo" }));
        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new Long2ObjectOpenHashMap(new long[] { 42l },
                        new Object[] { "foo" }))), new Long2ObjectOpenHashMap(new long[] { 42l },
                                new Object[] { "foo" }));
        Assert.assertEquals(new Serialization(null)
                .deserialize(ser.serialize(new Char2ObjectOpenHashMap(new char[] { 'a' },
                        new Object[] { "foo" }))), new Char2ObjectOpenHashMap(new char[] { 'a' },
                                new Object[] { "foo" }));
    }

    @Test
    public void testByte() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        byte[] vals = { -1, 0, 1, 2, Byte.MIN_VALUE, Byte.MAX_VALUE };
        for (byte i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Byte.class);
            Assert.assertEquals(l2, i);
        }

        for (byte i : vals) {
            byte[] array = new byte[] { i };
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == byte[].class);
            Assert.assertTrue(Arrays.equals(array, (byte[]) l2));
        }
    }

    @Test
    public void testInt() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        int[] vals = { Integer.MIN_VALUE, -Short.MIN_VALUE * 2, -Short.MIN_VALUE + 1, -Short.MIN_VALUE, -10, -9, -8, -7, -6, -5, -4, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 127, 254, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE + 1, Short.MAX_VALUE * 2, Integer.MAX_VALUE };
        for (int i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Integer.class);
            Assert.assertEquals(l2, i);
        }

        for (int i : vals) {
            int[] array = new int[] { i };
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
        short[] vals = { (short) (-Short.MIN_VALUE + 1), (short) -Short.MIN_VALUE, -10, -9, -8, -7, -6, -5, -4, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 127, 254, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE - 1, Short.MAX_VALUE };
        for (short i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Short.class);
            Assert.assertEquals(l2, i);
        }

        for (short i : vals) {
            short[] array = new short[] { i };
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == short[].class);
            Assert.assertTrue(Arrays.equals(array, (short[]) l2));
        }
    }

    @Test
    public void testDouble() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        double[] vals = { 1f, 0f, -1f, Math.PI, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE + 1, -100 };
        for (double i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Double.class);
            Assert.assertEquals(l2, i);
        }

        for (double i : vals) {
            double[] array = new double[] { i };
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == double[].class);
            Assert.assertTrue(Arrays.equals(array, (double[]) l2));
        }
    }

    @Test
    public void testFloat() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        float[] vals = { 1f, 0f, -1f, (float) Math.PI, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE + 1, -100 };
        for (float i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Float.class);
            Assert.assertEquals(l2, i);
        }

        for (float i : vals) {
            float[] array = new float[] { i };
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == float[].class);
            Assert.assertTrue(Arrays.equals(array, (float[]) l2));
        }
    }

    @Test
    public void testChar() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        // Chars are written as a 2-byte short, so cover the boundaries of that
        // range: above 0x7F, above 0x7FF, either side of the signed-short flip,
        // the 16-bit maximum, and an unpaired surrogate
        char[] vals = { 'a', ' ', '\u00e9', '\u4e2d', '\u7fff', '\u8000', '\uffff', '\ud83d' };
        for (char i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Character.class);
            Assert.assertEquals(l2, i);
        }

        for (char i : vals) {
            char[] array = new char[] { i };
            byte[] buf = ser.serialize(array);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == char[].class);
            Assert.assertTrue(Arrays.equals(array, (char[]) l2));
        }
    }

    @Test
    public void testLong() throws IOException, ClassNotFoundException {
        Serialization ser = new Serialization(null);
        long[] vals = { Long.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE - 1, Integer.MIN_VALUE + 1, -Short.MIN_VALUE * 2, -Short.MIN_VALUE + 1, -Short.MIN_VALUE, -10, -9, -8, -7, -6, -5, -4, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 127, 254, 255, 256, Short.MAX_VALUE, Short.MAX_VALUE + 1, Short.MAX_VALUE * 2, Integer.MAX_VALUE, Integer.MAX_VALUE + 1, Long.MAX_VALUE };
        for (long i : vals) {
            byte[] buf = ser.serialize(i);
            Object l2 = ser.deserialize(buf);
            Assert.assertTrue(l2.getClass() == Long.class);
            Assert.assertEquals(l2, i);
        }

        for (long i : vals) {
            long[] array = new long[] { i };
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

        boolean[] array = new boolean[] { true, false, true };
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
        boolean[] l = new boolean[] { true, false };
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (boolean[]) deserialize));
    }

    @Test
    public void testDoubleArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        double[] l = new double[] { Math.PI, 1D };
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (double[]) deserialize));
    }

    @Test
    public void testFloatArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        float[] l = new float[] { 1F, 1.234235F };
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (float[]) deserialize));
    }

    @Test
    public void testByteArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        byte[] l = new byte[] { 1, 34, -5 };
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (byte[]) deserialize));
    }

    @Test
    public void testCharArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        char[] l = new char[] { '1', 'a', '&', '\u00e9', '\u4e2d', '\u8000', '\uffff' };
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (char[]) deserialize));
    }

    @Test
    public void testStringArray() throws ClassNotFoundException, IOException {
        Serialization ser = new Serialization(null);
        String[] l = new String[] { "foo", "bar", "" };
        Object deserialize = ser.deserialize(ser.serialize(l));
        Assert.assertTrue(Arrays.equals(l, (String[]) deserialize));
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
    public void testSmallGraphModel() throws Exception {
        GraphModelImpl gm = GraphGenerator.generateSmallGraphStore().graphModel;
        Serialization ser = new Serialization(gm);

        DataInputOutput dio = new DataInputOutput();
        ser.serializeGraphModel(dio, gm);
        byte[] bytes = dio.toByteArray();

        GraphModelImpl read = ser.deserializeGraphModel(dio.reset(bytes));
        Assert.assertTrue(read.deepEquals(gm));
    }

    @Test
    public void testSmallUndirectedGraphModel() throws Exception {
        GraphModelImpl gm = GraphGenerator.generateSmallUndirectedGraphStore().graphModel;
        Serialization ser = new Serialization(gm);

        DataInputOutput dio = new DataInputOutput();
        ser.serializeGraphModel(dio, gm);
        byte[] bytes = dio.toByteArray();

        GraphModelImpl read = ser.deserializeGraphModel(dio.reset(bytes));
        Assert.assertTrue(read.deepEquals(gm));
    }

    @Test
    public void testDefaultColumns() throws Exception {
        GraphModelImpl gm = GraphGenerator.generateSmallUndirectedGraphStore().graphModel;

        Serialization ser = new Serialization(gm);

        DataInputOutput dio = new DataInputOutput();
        ser.serializeGraphModel(dio, gm);
        byte[] bytes = dio.toByteArray();

        GraphModelImpl read = ser.deserializeGraphModel(dio.reset(bytes));
        Assert.assertSame(read.defaultColumns().nodeId(), read.getNodeTable().getColumn("id"));
        Assert.assertSame(read.defaultColumns().nodeLabel(), read.getNodeTable().getColumn("label"));
        Assert.assertSame(read.defaultColumns().edgeId(), read.getEdgeTable().getColumn("id"));
        Assert.assertSame(read.defaultColumns().edgeLabel(), read.getEdgeTable().getColumn("label"));
        Assert.assertSame(read.defaultColumns().edgeWeight(), read.getEdgeTable().getColumn("weight"));
    }

    @Test
    public void testDeserializeWithGraphModel() throws Exception {
        GraphModelImpl gm = GraphGenerator.generateSmallUndirectedGraphStore().graphModel;
        Serialization ser = new Serialization(gm);

        DataInputOutput dio = new DataInputOutput();
        ser.serializeGraphModel(dio, gm);
        byte[] bytes = dio.toByteArray();

        GraphModelImpl read = GraphModel.Factory.newInstance();
        read = ser.deserializeGraphModel(dio.reset(bytes), read);
        Assert.assertTrue(read.deepEquals(gm));
    }

    @Test
    public void testDeserializeWithoutVersion() throws Exception {
        GraphModelImpl gm = GraphGenerator.generateSmallGraphStore().graphModel;
        Serialization ser = new Serialization(gm) {
            @Override
            public void serializeGraphModel(DataOutput out, GraphModelImpl model) throws IOException {
                this.model = model;
                serialize(out, model.configuration);
                serialize(out, model.store);
            }
        };

        DataInputOutput dio = new DataInputOutput();
        ser.serializeGraphModel(dio, gm);
        byte[] bytes = dio.toByteArray();

        ser = new Serialization();
        GraphModelImpl read = ser.deserializeGraphModelWithoutVersionPrefix(dio.reset(bytes), Serialization.VERSION);
        Assert.assertTrue(read.deepEquals(gm));
    }

    @Test
    public void testDeserializeGraphModelRejectsNewerVersion() throws Exception {
        GraphModelImpl gm = GraphGenerator.generateSmallGraphStore().graphModel;
        Serialization ser = new Serialization(gm) {
            @Override
            public void serializeGraphModel(DataOutput out, GraphModelImpl model) throws IOException {
                this.model = model;
                serialize(out, VERSION + 1f);
                serialize(out, model.configuration);
                serialize(out, model.store);
            }
        };

        DataInputOutput dio = new DataInputOutput();
        ser.serializeGraphModel(dio, gm);
        byte[] bytes = dio.toByteArray();

        UnsupportedFormatVersionException ex = Assert
                .expectThrows(UnsupportedFormatVersionException.class, () -> new Serialization()
                        .deserializeGraphModel(dio.reset(bytes)));
        Assert.assertEquals(ex.getFileVersion(), Serialization.VERSION + 1f);
        Assert.assertEquals(ex.getMaxSupportedVersion(), Serialization.VERSION);
    }

    @Test
    public void testDeserializeGraphModelWithoutVersionPrefixRejectsNewerVersion() throws Exception {
        GraphModelImpl gm = GraphGenerator.generateSmallGraphStore().graphModel;
        Serialization ser = new Serialization(gm) {
            @Override
            public void serializeGraphModel(DataOutput out, GraphModelImpl model) throws IOException {
                this.model = model;
                serialize(out, model.configuration);
                serialize(out, model.store);
            }
        };

        DataInputOutput dio = new DataInputOutput();
        ser.serializeGraphModel(dio, gm);
        byte[] bytes = dio.toByteArray();

        UnsupportedFormatVersionException ex = Assert
                .expectThrows(UnsupportedFormatVersionException.class, () -> new Serialization()
                        .deserializeGraphModelWithoutVersionPrefix(dio.reset(bytes), Serialization.VERSION + 1f));
        Assert.assertEquals(ex.getFileVersion(), Serialization.VERSION + 1f);
        Assert.assertEquals(ex.getMaxSupportedVersion(), Serialization.VERSION);
    }

    @Test
    public void testReuseSerializationInstanceForDeserialization() throws Exception {
        GraphModelImpl gm1 = GraphGenerator.generateSmallGraphStore().graphModel;
        GraphModelImpl gm2 = GraphGenerator.generateSmallMultiTypeGraphStore().graphModel;

        DataInputOutput dio1 = new DataInputOutput();
        new Serialization(gm1).serializeGraphModel(dio1, gm1);
        byte[] bytes1 = dio1.toByteArray();

        DataInputOutput dio2 = new DataInputOutput();
        new Serialization(gm2).serializeGraphModel(dio2, gm2);
        byte[] bytes2 = dio2.toByteArray();

        Serialization reused = new Serialization();
        GraphModelImpl read1 = reused.deserializeGraphModel(dio1.reset(bytes1));
        Assert.assertTrue(read1.deepEquals(gm1));

        GraphModelImpl read2 = reused.deserializeGraphModel(dio2.reset(bytes2));
        Assert.assertTrue(read2.deepEquals(gm2));
    }

    @Test
    public void testBitVectorEqual() throws Exception {
        Serialization ser = new Serialization();

        Random random = new Random();
        for (int i = 0; i < 20000; i += 97) {
            BitSet bs = new BitSet(i);

            int p = random.nextInt(99) + 1;
            for (int j = 0; j < i; j++) {
                if (random.nextInt(100) < p) {
                    bs.set(j);
                }
            }
            DataInputOutput dio = new DataInputOutput();
            ser.serializeBitSet(dio, bs);
            byte[] bytes = dio.toByteArray();

            BitSet deserializedBs = ser.deserializeBitSet(dio.reset(bytes));
            Assert.assertEquals(bs, deserializedBs);
        }
    }

    @Test
    public void testSerializationTagsAreUnique() throws Exception {
        // NULL_ID is excluded: it's an idMap sentinel (-1), not a wire tag
        Map<Integer, String> tagsByValue = new HashMap<>();
        for (Field field : Serialization.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers) || field.getType() != int.class) {
                continue;
            }
            String name = field.getName();
            if (name.equals("NULL_ID")) {
                continue;
            }
            field.setAccessible(true);
            int value = field.getInt(null);
            String previous = tagsByValue.put(value, name);
            Assert.assertNull(previous, "Duplicate serialization tag " + value + " shared by " + previous + " and " + name);
        }
    }

    @Test(timeOut = 5000)
    public void testSerializeGraphStoreHoldsReadLockForEntireDuration() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphStore graphStore = graphModel.store;
        NodeImpl[] nodes = GraphGenerator.generateSmallNodeList();
        graphStore.nodeStore.addAll(Arrays.asList(nodes));

        CountDownLatch writeStarted = new CountDownLatch(1);
        CountDownLatch proceed = new CountDownLatch(1);
        BlockingDataOutput blockingOutput = new BlockingDataOutput(writeStarted, proceed);

        Serialization ser = new Serialization(graphModel);
        AtomicReference<Exception> writerError = new AtomicReference<>();
        Thread writer = new Thread(() -> {
            try {
                ser.serializeGraphStore(blockingOutput, graphStore);
            } catch (Exception e) {
                writerError.set(e);
            }
        });
        writer.start();

        Assert.assertTrue(writeStarted.await(2, TimeUnit.SECONDS), "serialization should have started writing");

        AtomicBoolean writeLockAcquired = new AtomicBoolean(false);
        Thread locker = new Thread(() -> {
            graphStore.writeLock();
            writeLockAcquired.set(true);
            graphStore.writeUnlock();
        });
        locker.start();

        // Give the locker thread a chance to attempt (and be blocked by) the write lock
        Thread.sleep(300);
        Assert.assertFalse(writeLockAcquired
                .get(), "write lock must not be acquired while serializeGraphStore still holds the read lock");

        proceed.countDown();
        writer.join(2000);
        locker.join(2000);

        Assert.assertNull(writerError.get());
        Assert.assertTrue(writeLockAcquired.get(), "write lock should be acquired after serialization completes");
    }

    /**
     * A DataOutput that blocks on its very first write call until released, so tests can deterministically assert what
     * lock state holds while a serialization call is in progress.
     */
    private static class BlockingDataOutput implements DataOutput {

        private final DataOutputStream delegate = new DataOutputStream(new ByteArrayOutputStream());
        private final CountDownLatch started;
        private final CountDownLatch proceed;
        private final AtomicBoolean first = new AtomicBoolean(true);

        private BlockingDataOutput(CountDownLatch started, CountDownLatch proceed) {
            this.started = started;
            this.proceed = proceed;
        }

        private void beforeWrite() throws IOException {
            if (first.compareAndSet(true, false)) {
                started.countDown();
                try {
                    proceed.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e);
                }
            }
        }

        @Override
        public void write(int b) throws IOException {
            beforeWrite();
            delegate.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            beforeWrite();
            delegate.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            beforeWrite();
            delegate.write(b, off, len);
        }

        @Override
        public void writeBoolean(boolean v) throws IOException {
            beforeWrite();
            delegate.writeBoolean(v);
        }

        @Override
        public void writeByte(int v) throws IOException {
            beforeWrite();
            delegate.writeByte(v);
        }

        @Override
        public void writeShort(int v) throws IOException {
            beforeWrite();
            delegate.writeShort(v);
        }

        @Override
        public void writeChar(int v) throws IOException {
            beforeWrite();
            delegate.writeChar(v);
        }

        @Override
        public void writeInt(int v) throws IOException {
            beforeWrite();
            delegate.writeInt(v);
        }

        @Override
        public void writeLong(long v) throws IOException {
            beforeWrite();
            delegate.writeLong(v);
        }

        @Override
        public void writeFloat(float v) throws IOException {
            beforeWrite();
            delegate.writeFloat(v);
        }

        @Override
        public void writeDouble(double v) throws IOException {
            beforeWrite();
            delegate.writeDouble(v);
        }

        @Override
        public void writeBytes(String s) throws IOException {
            beforeWrite();
            delegate.writeBytes(s);
        }

        @Override
        public void writeChars(String s) throws IOException {
            beforeWrite();
            delegate.writeChars(s);
        }

        @Override
        public void writeUTF(String s) throws IOException {
            beforeWrite();
            delegate.writeUTF(s);
        }
    }

    @Test(timeOut = 5000)
    public void testDeserializeGraphStoreHoldsWriteLockForEntireDuration() throws Exception {
        GraphModelImpl sourceModel = new GraphModelImpl();
        GraphStore sourceStore = sourceModel.store;
        NodeImpl[] nodes = GraphGenerator.generateSmallNodeList();
        sourceStore.nodeStore.addAll(Arrays.asList(nodes));
        DataInputOutput dio = new DataInputOutput();
        new Serialization(sourceModel).serializeGraphStore(dio, sourceStore);
        byte[] bytes = dio.toByteArray();

        GraphModelImpl targetModel = new GraphModelImpl();
        GraphStore targetStore = targetModel.store;

        CountDownLatch readStarted = new CountDownLatch(1);
        CountDownLatch proceed = new CountDownLatch(1);
        BlockingDataInput blockingInput = new BlockingDataInput(bytes, readStarted, proceed);

        Serialization ser = new Serialization(targetModel);
        AtomicReference<Exception> readerError = new AtomicReference<>();
        Thread reader = new Thread(() -> {
            try {
                ser.deserializeGraphStore(blockingInput);
            } catch (Exception e) {
                readerError.set(e);
            }
        });
        reader.start();

        Assert.assertTrue(readStarted.await(2, TimeUnit.SECONDS), "deserialization should have started reading");

        AtomicBoolean readLockAcquired = new AtomicBoolean(false);
        Thread locker = new Thread(() -> {
            targetStore.readLock();
            readLockAcquired.set(true);
            targetStore.readUnlock();
        });
        locker.start();

        // Give the locker thread a chance to attempt (and be blocked by) the read lock
        Thread.sleep(300);
        Assert.assertFalse(readLockAcquired
                .get(), "read lock must not be acquired while deserializeGraphStore still holds the write lock");

        proceed.countDown();
        reader.join(2000);
        locker.join(2000);

        Assert.assertNull(readerError.get());
        Assert.assertTrue(readLockAcquired.get(), "read lock should be acquired after deserialization completes");
    }

    /**
     * A DataInput that blocks on its very first read call until released, so tests can deterministically assert what
     * lock state holds while a deserialization call is in progress.
     */
    private static class BlockingDataInput implements DataInput {

        private final DataInputStream delegate;
        private final CountDownLatch started;
        private final CountDownLatch proceed;
        private final AtomicBoolean first = new AtomicBoolean(true);

        private BlockingDataInput(byte[] bytes, CountDownLatch started, CountDownLatch proceed) {
            this.delegate = new DataInputStream(new ByteArrayInputStream(bytes));
            this.started = started;
            this.proceed = proceed;
        }

        private void beforeRead() throws IOException {
            if (first.compareAndSet(true, false)) {
                started.countDown();
                try {
                    proceed.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e);
                }
            }
        }

        @Override
        public void readFully(byte[] b) throws IOException {
            beforeRead();
            delegate.readFully(b);
        }

        @Override
        public void readFully(byte[] b, int off, int len) throws IOException {
            beforeRead();
            delegate.readFully(b, off, len);
        }

        @Override
        public int skipBytes(int n) throws IOException {
            beforeRead();
            return delegate.skipBytes(n);
        }

        @Override
        public boolean readBoolean() throws IOException {
            beforeRead();
            return delegate.readBoolean();
        }

        @Override
        public byte readByte() throws IOException {
            beforeRead();
            return delegate.readByte();
        }

        @Override
        public int readUnsignedByte() throws IOException {
            beforeRead();
            return delegate.readUnsignedByte();
        }

        @Override
        public short readShort() throws IOException {
            beforeRead();
            return delegate.readShort();
        }

        @Override
        public int readUnsignedShort() throws IOException {
            beforeRead();
            return delegate.readUnsignedShort();
        }

        @Override
        public char readChar() throws IOException {
            beforeRead();
            return delegate.readChar();
        }

        @Override
        public int readInt() throws IOException {
            beforeRead();
            return delegate.readInt();
        }

        @Override
        public long readLong() throws IOException {
            beforeRead();
            return delegate.readLong();
        }

        @Override
        public float readFloat() throws IOException {
            beforeRead();
            return delegate.readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            beforeRead();
            return delegate.readDouble();
        }

        @Override
        public String readLine() throws IOException {
            beforeRead();
            return delegate.readLine();
        }

        @Override
        public String readUTF() throws IOException {
            beforeRead();
            return delegate.readUTF();
        }
    }
}
