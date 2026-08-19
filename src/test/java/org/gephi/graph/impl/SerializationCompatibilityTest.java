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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalBooleanMap;
import org.gephi.graph.api.types.IntervalCharMap;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalIntegerMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.IntervalStringMap;
import org.gephi.graph.api.types.TimestampBooleanMap;
import org.gephi.graph.api.types.TimestampCharMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.graph.api.types.TimestampStringMap;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Golden-fixture regression suite for the serialization format.
 * <p>
 * Enforces three contracts:
 * <ol>
 * <li><b>Backward compatibility</b> - every fixture, from the oldest minor to the current one, deserializes and yields
 * the expected content.</li>
 * <li><b>Format drift</b> - for the current minor, serializing the model built by {@link SerializationFixtureGenerator}
 * produces bytes identical to the committed file. Older minors are exempt, since graphstore no longer writes those
 * formats.</li>
 * <li><b>Determinism</b> - the same content serializes to identical bytes regardless of build order.</li>
 * </ol>
 *
 * See <code>src/test/resources/serialization/README.md</code> before changing any fixture file.
 */
public class SerializationCompatibilityTest {

    private static final String RESOURCE_ROOT = "/serialization";
    private static final String CURRENT_MINOR = SerializationFixtureGenerator.CURRENT_MINOR;
    private static final String[] ALL_MINORS = { "0.4", "0.5", "0.6", "0.7", CURRENT_MINOR };

    // Column counts of a default model, as asserted on the legacy fixtures
    private static final int DEFAULT_NODE_COLUMNS = 3;
    private static final int DEFAULT_EDGE_COLUMNS = 4;

    // Columns added by SerializationFixtureGenerator on the type-surface fixtures
    private static final int GENERATED_STATIC_COLUMNS = 25;
    private static final int GENERATED_DYNAMIC_COLUMNS = 10;
    private static final int GENERATED_COLLECTION_COLUMNS = 3;

    // Contract 1: every minor deserializes and holds the expected content

    @Test(dataProvider = "allFixtures")
    public void testFixtureDeserializes(String minor, String fixture) throws IOException {
        byte[] committed = readFixture(minor, fixture);
        Assert.assertTrue(committed.length > 0, "Fixture " + path(minor, fixture) + " is empty");

        GraphModel graphModel = deserialize(committed);
        Assert.assertNotNull(graphModel, "Deserialization of " + path(minor, fixture) + " returned null");
        assertContent(minor, fixture, graphModel);
    }

    // Contract 2: the current minor is byte-pinned
    //
    // Compares today's write path against the committed golden file. Reading a fixture back and re-serializing it
    // would not work, because deserialization is not byte-idempotent here: GraphVersion counters and
    // TimeIndexStore.countMap are restored from the stream and then incremented again as elements are re-inserted, and
    // TextProperties width/height are dropped on read. Contract 1 covers the read path.

    @Test(dataProvider = "currentMinorFixtures")
    public void testCurrentMinorIsByteIdentical(String fixture) throws IOException {
        byte[] committed = readFixture(CURRENT_MINOR, fixture);
        byte[] regenerated = SerializationFixtureGenerator.serialize(buildFixtureModel(fixture));

        assertBytesEqual(committed, regenerated, "Serialization format drift for fixture " + path(CURRENT_MINOR, fixture) + ".\nThe model built by SerializationFixtureGenerator no longer serializes to the committed bytes.\nThis is either a bug or an intended format change; see src/test/resources/serialization/README.md.");
    }

    // Contract 3: determinism

    @Test(dataProvider = "currentMinorFixtures")
    public void testSerializationIsDeterministic(String fixture) throws IOException {
        GraphModel graphModel = deserialize(readFixture(CURRENT_MINOR, fixture));

        byte[] first = SerializationFixtureGenerator.serialize(graphModel);
        byte[] second = SerializationFixtureGenerator.serialize(graphModel);

        assertBytesEqual(first, second, "Serializing the same model twice produced different bytes for fixture " + path(CURRENT_MINOR, fixture) + ".\nSomething in the write path depends on iteration order or identity" + " hashing rather than on content.");
    }

    @Test(dataProvider = "currentMinorFixtures")
    public void testFreshlyBuiltModelsSerializeDeterministically(String fixture) throws IOException {
        byte[] first = SerializationFixtureGenerator.serialize(buildFixtureModel(fixture));
        byte[] second = SerializationFixtureGenerator.serialize(buildFixtureModel(fixture));

        assertBytesEqual(first, second, "Building the model for fixture '" + fixture + "' twice and serializing produced different bytes.");
    }

    @Test
    public void testGraphAttributesOrderIsCanonical() throws IOException {
        // The same graph attributes inserted in opposite orders must serialize identically. See GraphAttributesImpl.
        String[] keys = { "zulu", "alpha", "mike", "bravo", "yankee", "charlie", "november", "delta" };

        GraphModel forward = GraphModel.Factory.newInstance();
        for (int i = 0; i < keys.length; i++) {
            forward.getGraph().setAttribute(keys[i], "value-" + i);
        }

        GraphModel backward = GraphModel.Factory.newInstance();
        for (int i = keys.length - 1; i >= 0; i--) {
            backward.getGraph().setAttribute(keys[i], "value-" + i);
        }

        assertBytesEqual(SerializationFixtureGenerator.serialize(forward), SerializationFixtureGenerator
                .serialize(backward), "Graph attributes are not serialized in a canonical order: the same attributes" + " inserted in a different order produced different bytes.");
    }

    // Round-trip only, no byte assertions: the hash-ordered surface excluded from the fixtures

    @Test
    public void testRoundTripHashOrderedAttributes() throws IOException {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        graphModel.getNodeTable().addColumn("c_list", List.class);
        graphModel.getNodeTable().addColumn("c_set", Set.class);
        graphModel.getNodeTable().addColumn("c_map", Map.class);

        Graph graph = graphModel.getGraph();
        Node node = graphModel.factory().newNode("n1");
        graph.addNode(node);

        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Set<String> set = new HashSet<>(Arrays.asList("x", "y", "z"));
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        node.setAttribute("c_list", list);
        node.setAttribute("c_set", set);
        node.setAttribute("c_map", map);

        // Many graph attribute keys, of assorted types
        for (int i = 0; i < 50; i++) {
            graph.setAttribute("key-" + i, "value-" + i);
        }
        graph.setAttribute("an-int", 7);
        graph.setAttribute("a-char", 'q');
        graph.setAttribute("an-array", new int[] { 1, 2, 3 });

        GraphModel read = deserialize(SerializationFixtureGenerator.serialize(graphModel));
        Graph readGraph = read.getGraph();
        Node readNode = readGraph.getNode("n1");
        Assert.assertNotNull(readNode);

        Assert.assertEquals(new ArrayList<>((List) readNode.getAttribute("c_list")), list);
        Assert.assertEquals(new HashSet<>((Set) readNode.getAttribute("c_set")), set);
        Assert.assertEquals(new HashMap<>((Map) readNode.getAttribute("c_map")), map);

        Assert.assertEquals(readGraph.getAttributeKeys().size(), 53);
        for (int i = 0; i < 50; i++) {
            Assert.assertEquals(readGraph.getAttribute("key-" + i), "value-" + i);
        }
        Assert.assertEquals(readGraph.getAttribute("an-int"), 7);
        Assert.assertEquals(readGraph.getAttribute("a-char"), 'q');
        Assert.assertEquals((int[]) readGraph.getAttribute("an-array"), new int[] { 1, 2, 3 });
    }

    // Data providers

    @DataProvider(name = "allFixtures")
    public Object[][] allFixtures() {
        List<Object[]> rows = new ArrayList<>();
        for (String minor : ALL_MINORS) {
            for (String fixture : fixturesFor(minor)) {
                rows.add(new Object[] { minor, fixture });
            }
        }
        return rows.toArray(new Object[0][]);
    }

    @DataProvider(name = "currentMinorFixtures")
    public Object[][] currentMinorFixtures() {
        String[] fixtures = fixturesFor(CURRENT_MINOR);
        Object[][] rows = new Object[fixtures.length][];
        for (int i = 0; i < fixtures.length; i++) {
            rows[i] = new Object[] { fixtures[i] };
        }
        return rows;
    }

    private static String[] fixturesFor(String minor) {
        if (CURRENT_MINOR.equals(minor)) {
            return new String[] { SerializationFixtureGenerator.BASIC, SerializationFixtureGenerator.PARALLEL, SerializationFixtureGenerator.TYPES_TIMESTAMP, SerializationFixtureGenerator.TYPES_INTERVAL, SerializationFixtureGenerator.VIEWS };
        }
        // The legacy fixtures only cover the format skeleton
        return new String[] { SerializationFixtureGenerator.BASIC, SerializationFixtureGenerator.PARALLEL };
    }

    // Content assertions

    private void assertContent(String minor, String fixture, GraphModel graphModel) {
        switch (fixture) {
            case SerializationFixtureGenerator.BASIC:
                assertBasic(graphModel);
                break;
            case SerializationFixtureGenerator.PARALLEL:
                assertParallel(graphModel);
                break;
            case SerializationFixtureGenerator.TYPES_TIMESTAMP:
                assertTypeSurface(graphModel, TimeRepresentation.TIMESTAMP);
                break;
            case SerializationFixtureGenerator.TYPES_INTERVAL:
                assertTypeSurface(graphModel, TimeRepresentation.INTERVAL);
                break;
            case SerializationFixtureGenerator.VIEWS:
                assertViews(graphModel);
                break;
            default:
                Assert.fail("No content assertions defined for fixture " + path(minor, fixture));
        }
    }

    private void assertBasic(GraphModel graphModel) {
        Graph graph = graphModel.getGraph();
        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 1);
        Assert.assertEquals(graphModel.getNodeTable().countColumns(), DEFAULT_NODE_COLUMNS);
        Assert.assertEquals(graphModel.getEdgeTable().countColumns(), DEFAULT_EDGE_COLUMNS);

        Node node1 = graph.getNode(SerializationFixtureGenerator.NODE_ID_1);
        Node node2 = graph.getNode(SerializationFixtureGenerator.NODE_ID_2);
        Assert.assertNotNull(node1);
        Assert.assertNotNull(node2);
        Assert.assertEquals(node1.getLabel(), SerializationFixtureGenerator.NODE_LABEL_1);
        Assert.assertEquals(node2.getLabel(), SerializationFixtureGenerator.NODE_LABEL_2);
        Assert.assertEquals(node1.x(), 10.0f);
        Assert.assertEquals(node1.y(), 10.0f);
        Assert.assertEquals(node1.z(), 1.0f);
        Assert.assertEquals(node1.size(), 11.0f);

        Edge edge = graph.getEdge(SerializationFixtureGenerator.EDGE_ID_1);
        Assert.assertNotNull(edge);
        Assert.assertEquals(edge.getSource(), node1);
        Assert.assertEquals(edge.getTarget(), node2);
        Assert.assertTrue(edge.isDirected());
        Assert.assertEquals(edge.getWeight(), 1.0);
    }

    private void assertParallel(GraphModel graphModel) {
        Graph graph = graphModel.getGraph();
        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 2);
        Assert.assertEquals(graphModel.getNodeTable().countColumns(), DEFAULT_NODE_COLUMNS);
        Assert.assertEquals(graphModel.getEdgeTable().countColumns(), DEFAULT_EDGE_COLUMNS);

        Edge edge1 = graph.getEdge(SerializationFixtureGenerator.EDGE_ID_1);
        Edge edge2 = graph.getEdge(SerializationFixtureGenerator.EDGE_ID_2);
        Assert.assertNotNull(edge1);
        Assert.assertNotNull(edge2);
        Assert.assertNotEquals(edge1.getType(), edge2.getType());
        Assert.assertEquals(edge1.getTypeLabel(), SerializationFixtureGenerator.EDGE_TYPE_1);
        Assert.assertEquals(edge2.getTypeLabel(), SerializationFixtureGenerator.EDGE_TYPE_2);
    }

    private void assertTypeSurface(GraphModel graphModel, TimeRepresentation timeRepresentation) {
        boolean interval = timeRepresentation == TimeRepresentation.INTERVAL;

        Configuration config = graphModel.getConfiguration();
        Assert.assertEquals(config.getTimeRepresentation(), timeRepresentation);
        Assert.assertEquals(config.getEdgeWeightType(), interval ? IntervalDoubleMap.class : TimestampDoubleMap.class);
        Assert.assertEquals(graphModel.getTimeFormat(), interval ? TimeFormat.DATETIME : TimeFormat.DOUBLE);
        Assert.assertEquals(graphModel.getTimeZone(), ZoneId.of("Europe/Paris"));

        Assert.assertEquals(graphModel.getNodeTable()
                .countColumns(), DEFAULT_NODE_COLUMNS + GENERATED_STATIC_COLUMNS + GENERATED_DYNAMIC_COLUMNS + GENERATED_COLLECTION_COLUMNS);
        Assert.assertEquals(graphModel.getEdgeTable()
                .countColumns(), DEFAULT_EDGE_COLUMNS + GENERATED_STATIC_COLUMNS + GENERATED_DYNAMIC_COLUMNS);

        // Column types survived the round trip, including the boxed-array standardization
        Assert.assertEquals(graphModel.getNodeTable().getColumn("t_character").getTypeClass(), Character.class);
        Assert.assertEquals(graphModel.getNodeTable().getColumn("t_char_array").getTypeClass(), char[].class);
        Assert.assertEquals(graphModel.getNodeTable().getColumn("t_boxed_boolean_array")
                .getTypeClass(), boolean[].class);
        Assert.assertEquals(graphModel.getNodeTable().getColumn("t_boxed_character_array")
                .getTypeClass(), char[].class);
        Assert.assertEquals(graphModel.getNodeTable().getColumn("t_set").getTypeClass(), Set.class);
        Assert.assertEquals(graphModel.getNodeTable().getColumn("t_map").getTypeClass(), Map.class);

        Graph graph = graphModel.getGraph();
        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 1);

        Node node1 = graph.getNode(SerializationFixtureGenerator.NODE_ID_1);
        Assert.assertNotNull(node1);
        assertStaticValues(node1);
        assertDynamicValues(node1, timeRepresentation);

        // Collections: only the list carries a value, see SerializationFixtureGenerator
        Assert.assertEquals(new ArrayList<>((List) node1.getAttribute("t_list")), Arrays.asList("first", "second"));
        Assert.assertNull(node1.getAttribute("t_set"));
        Assert.assertNull(node1.getAttribute("t_map"));

        // Element and text properties
        Assert.assertEquals(node1.x(), 1.5f);
        Assert.assertEquals(node1.y(), -2.5f);
        Assert.assertEquals(node1.z(), 3.5f);
        Assert.assertEquals(node1.size(), 7.25f);
        Assert.assertEquals(node1.alpha(), 0.75f, 0.005f);
        Assert.assertEquals(node1.getTextProperties().getText(), "node one text");
        Assert.assertEquals(node1.getTextProperties().getSize(), 13.5f);
        Assert.assertFalse(node1.getTextProperties().isVisible());
        // The fixture was written with text dimensions 42x24 and the bytes carry them, but NodeImpl.setTextProperties
        // does not copy width and height back, so they are lost on read. Pinned here as the current behaviour.
        Assert.assertEquals(node1.getTextProperties().getWidth(), 0.0f);
        Assert.assertEquals(node1.getTextProperties().getHeight(), 0.0f);

        // Element time set
        Node node2 = graph.getNode(SerializationFixtureGenerator.NODE_ID_2);
        Assert.assertNotNull(node2);
        if (interval) {
            Assert.assertEquals(node1.getIntervals(), new Interval[] { new Interval(1.0, 5.0) });
            Assert.assertEquals(node2.getIntervals(), new Interval[] { new Interval(2.0, 3.0) });
        } else {
            Assert.assertEquals(node1.getTimestamps(), new double[] { 1.0, 4.0 });
            Assert.assertEquals(node2.getTimestamps(), new double[] { 2.0 });
        }

        // Edge, edge type and dynamic weight
        Edge edge1 = graph.getEdge(SerializationFixtureGenerator.EDGE_ID_1);
        Assert.assertNotNull(edge1);
        Assert.assertEquals(edge1.getTypeLabel(), SerializationFixtureGenerator.EDGE_TYPE_1);
        Assert.assertTrue(edge1.hasDynamicWeight());
        Assert.assertEquals(edge1.getTextProperties().getText(), "edge one text");
        assertStaticValues(edge1);
        assertDynamicValues(edge1, timeRepresentation);
        if (interval) {
            Assert.assertEquals(edge1.getWeight(new Interval(1.0, 2.0)), 2.5);
            Assert.assertEquals(edge1.getWeight(new Interval(3.0, 4.0)), 3.5);
        } else {
            Assert.assertEquals(edge1.getWeight(1.0), 2.5);
            Assert.assertEquals(edge1.getWeight(3.0), 3.5);
        }

        // Graph attributes
        Assert.assertEquals(graph.getAttribute("attr-string"), "graph level");
        Assert.assertEquals(graph.getAttribute("attr-int"), 42);
        Assert.assertEquals(graph.getAttribute("attr-double"), 3.5);
        Assert.assertEquals(graph.getAttribute("attr-char"), 'g');
        Assert.assertEquals((char[]) graph.getAttribute("attr-char-array"), new char[] { 'a', 'b' });
        Assert.assertEquals(graph.getAttribute("attr-instant"), Instant.ofEpochSecond(1_600_000_000L, 123));
        if (interval) {
            Assert.assertEquals(graph.getAttribute("attr-dynamic", new Interval(1.0, 2.0)), "first");
            Assert.assertEquals(graph.getAttribute("attr-dynamic", new Interval(3.0, 4.0)), "second");
        } else {
            Assert.assertEquals(graph.getAttribute("attr-dynamic", 1.0), "first");
            Assert.assertEquals(graph.getAttribute("attr-dynamic", 3.0), "second");
        }
    }

    private void assertStaticValues(org.gephi.graph.api.Element element) {
        Assert.assertEquals(element.getAttribute("t_boolean"), Boolean.TRUE);
        Assert.assertEquals(element.getAttribute("t_integer"), 123456);
        Assert.assertEquals(element.getAttribute("t_short"), (short) -12);
        Assert.assertEquals(element.getAttribute("t_long"), 9876543210L);
        Assert.assertEquals(element.getAttribute("t_biginteger"), new BigInteger("123456789012345678901234567890"));
        Assert.assertEquals(element.getAttribute("t_byte"), (byte) 7);
        Assert.assertEquals(element.getAttribute("t_float"), 1.25f);
        Assert.assertEquals(element.getAttribute("t_double"), -2.5);
        Assert.assertEquals(element.getAttribute("t_bigdecimal"), new BigDecimal("1234567890.0987654321"));
        Assert.assertEquals(element.getAttribute("t_character"), 'Z');
        Assert.assertEquals(element.getAttribute("t_string"), "hello é中文");
        Assert.assertEquals(element.getAttribute("t_instant"), Instant.ofEpochSecond(1_500_000_000L, 42));

        Assert.assertEquals((boolean[]) element.getAttribute("t_boolean_array"), new boolean[] { true, false, true });
        Assert.assertEquals((int[]) element.getAttribute("t_int_array"), new int[] { 1, -2, 3 });
        Assert.assertEquals((short[]) element.getAttribute("t_short_array"), new short[] { 4, -5 });
        Assert.assertEquals((long[]) element.getAttribute("t_long_array"), new long[] { 6L, -7L });
        // BigInteger[] and BigDecimal[] go through the generic ARRAY_OBJECT path and come back as Object[]. The
        // element values survive, the array component type does not.
        Assert.assertEquals((Object[]) element
                .getAttribute("t_biginteger_array"), new Object[] { BigInteger.ONE, new BigInteger("-99") });
        Assert.assertEquals((byte[]) element.getAttribute("t_byte_array"), new byte[] { 8, -9 });
        Assert.assertEquals((float[]) element.getAttribute("t_float_array"), new float[] { 1.5f, -2.5f });
        Assert.assertEquals((double[]) element.getAttribute("t_double_array"), new double[] { 3.5, -4.5 });
        Assert.assertEquals((Object[]) element
                .getAttribute("t_bigdecimal_array"), new Object[] { BigDecimal.ONE, new BigDecimal("-0.5") });
        Assert.assertEquals((char[]) element.getAttribute("t_char_array"), new char[] { 'a', 'é', '中' });
        Assert.assertEquals((String[]) element.getAttribute("t_string_array"), new String[] { "one", "two", "three" });

        Assert.assertEquals((boolean[]) element.getAttribute("t_boxed_boolean_array"), new boolean[] { false, true });
        Assert.assertEquals((char[]) element.getAttribute("t_boxed_character_array"), new char[] { 'x', 'y' });
    }

    private void assertDynamicValues(org.gephi.graph.api.Element element, TimeRepresentation timeRepresentation) {
        if (timeRepresentation == TimeRepresentation.INTERVAL) {
            IntervalSet set = (IntervalSet) element.getAttribute("t_interval_set");
            Assert.assertNotNull(set);
            Assert.assertEquals(set.size(), 2);
            Assert.assertTrue(set.contains(new Interval(1.0, 2.0)));
            Assert.assertTrue(set.contains(new Interval(3.0, 4.0)));

            Assert.assertEquals(((IntervalBooleanMap) element.getAttribute("t_interval_boolean"))
                    .getBoolean(new Interval(1.0, 2.0)), true);
            Assert.assertEquals(((IntervalIntegerMap) element.getAttribute("t_interval_integer"))
                    .getInteger(new Interval(3.0, 4.0)), -20);
            Assert.assertEquals(((IntervalCharMap) element.getAttribute("t_interval_char"))
                    .getCharacter(new Interval(3.0, 4.0)), '中');
            Assert.assertEquals(((IntervalStringMap) element.getAttribute("t_interval_string"))
                    .get(new Interval(1.0, 2.0), (String) null), "alpha");
            Assert.assertNotNull(element.getAttribute("t_interval_short"));
            Assert.assertNotNull(element.getAttribute("t_interval_long"));
            Assert.assertNotNull(element.getAttribute("t_interval_byte"));
            Assert.assertNotNull(element.getAttribute("t_interval_float"));
            Assert.assertNotNull(element.getAttribute("t_interval_double"));
        } else {
            TimestampSet set = (TimestampSet) element.getAttribute("t_timestamp_set");
            Assert.assertNotNull(set);
            Assert.assertEquals(set.toPrimitiveArray(), new double[] { 1.0, 3.0 });

            Assert.assertEquals(((TimestampBooleanMap) element.getAttribute("t_timestamp_boolean"))
                    .getBoolean(1.0), true);
            Assert.assertEquals(((TimestampIntegerMap) element.getAttribute("t_timestamp_integer"))
                    .getInteger(3.0), -20);
            Assert.assertEquals(((TimestampCharMap) element.getAttribute("t_timestamp_char")).getCharacter(3.0), '中');
            Assert.assertEquals(((TimestampStringMap) element.getAttribute("t_timestamp_string"))
                    .get(1.0, (String) null), "alpha");
            Assert.assertNotNull(element.getAttribute("t_timestamp_short"));
            Assert.assertNotNull(element.getAttribute("t_timestamp_long"));
            Assert.assertNotNull(element.getAttribute("t_timestamp_byte"));
            Assert.assertNotNull(element.getAttribute("t_timestamp_float"));
            Assert.assertNotNull(element.getAttribute("t_timestamp_double"));
        }
    }

    private void assertViews(GraphModel graphModel) {
        Graph graph = graphModel.getGraph();
        Assert.assertEquals(graph.getNodeCount(), 5);
        Assert.assertEquals(graph.getEdgeCount(), 5);

        // Default type plus the two registered labels
        Assert.assertEquals(graphModel.getEdgeTypeCount(), 3);
        Assert.assertTrue(graphModel.isMultiGraph());
        Assert.assertTrue(graphModel.isMixed());

        Assert.assertTrue(graph.getEdge("e0").isDirected());
        Assert.assertFalse(graph.getEdge("e2").isDirected());
        Assert.assertTrue(graph.getEdge("e4").isSelfLoop());
        Assert.assertEquals(graph.getEdge("e3").getWeight(), 4.0);

        Assert.assertEquals(((GraphModelImpl) graphModel).store.viewStore.length, 2);

        GraphViewImpl nodeAndEdgeView = null;
        GraphViewImpl nodeOnlyView = null;
        for (GraphViewImpl view : ((GraphModelImpl) graphModel).store.viewStore.views) {
            if (view == null) {
                continue;
            }
            if (view.isEdgeView()) {
                nodeAndEdgeView = view;
            } else {
                nodeOnlyView = view;
            }
        }

        Assert.assertNotNull(nodeAndEdgeView, "The node and edge view is missing");
        Assert.assertTrue(nodeAndEdgeView.isNodeView());
        Graph subGraph = graphModel.getGraph(nodeAndEdgeView);
        Assert.assertEquals(subGraph.getNodeCount(), 3);
        Assert.assertEquals(subGraph.getEdgeCount(), 1);
        Assert.assertEquals(subGraph.getAttribute("view-attr"), "on the view");
        Assert.assertEquals(subGraph.getAttribute("view-count"), 3);

        // The other view is node-only and holds the two remaining nodes
        Assert.assertNotNull(nodeOnlyView, "The node-only view is missing");
        Assert.assertEquals(graphModel.getGraph(nodeOnlyView).getNodeCount(), 2);

        // GraphViewStore.visibleView is not part of the serialized format, so it always comes back as the main view
        Assert.assertTrue(graphModel.getVisibleView().isMainView());

        Assert.assertEquals(graph.getAttribute("graph-attr"), "main graph");
        Assert.assertEquals(graph.getNode("n2").getAttribute("weight"), 2.0);
        Assert.assertEquals(graph.getNode("n2").getAttribute("tag"), "tag2");
    }

    // Helpers

    private static String path(String minor, String fixture) {
        return RESOURCE_ROOT + "/" + minor + "/" + fixture + SerializationFixtureGenerator.FILE_EXTENSION;
    }

    private static byte[] readFixture(String minor, String fixture) throws IOException {
        String resource = path(minor, fixture);
        try (InputStream is = SerializationCompatibilityTest.class.getResourceAsStream(resource)) {
            Assert.assertNotNull(is, "Missing fixture resource " + resource);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toByteArray();
        }
    }

    private static GraphModel buildFixtureModel(String fixture) {
        GraphModel model = SerializationFixtureGenerator.buildAll().get(fixture);
        Assert.assertNotNull(model, "SerializationFixtureGenerator does not build fixture '" + fixture + "'");
        return model;
    }

    private static GraphModel deserialize(byte[] bytes) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            return GraphModel.Serialization.read(dis);
        }
    }

    /**
     * Byte comparison that reports where the two streams diverge, not just that they do.
     *
     * @param expected expected bytes
     * @param actual actual bytes
     * @param context message explaining what the mismatch means
     */
    private static void assertBytesEqual(byte[] expected, byte[] actual, String context) {
        if (Arrays.equals(expected, actual)) {
            return;
        }

        int common = Math.min(expected.length, actual.length);
        int offset = -1;
        for (int i = 0; i < common; i++) {
            if (expected[i] != actual[i]) {
                offset = i;
                break;
            }
        }

        StringBuilder sb = new StringBuilder(context);
        sb.append("\n\nExpected ").append(expected.length).append(" bytes, got ").append(actual.length)
                .append(" bytes.");
        if (offset < 0) {
            offset = common;
            sb.append("\nThe first ").append(common)
                    .append(" bytes are identical; the streams differ in length only, starting at offset ")
                    .append(common).append('.');
        } else {
            sb.append("\nFirst difference at offset ").append(offset).append(" (0x").append(Integer.toHexString(offset))
                    .append("): expected 0x").append(hexByte(expected[offset])).append(", got 0x")
                    .append(hexByte(actual[offset])).append('.');
        }

        int from = Math.max(0, offset - 16);
        int to = offset + 16;
        sb.append("\n  expected[").append(from).append("..").append(Math.min(to, expected.length) - 1).append("]: ")
                .append(hexDump(expected, from, to));
        sb.append("\n  actual  [").append(from).append("..").append(Math.min(to, actual.length) - 1).append("]: ")
                .append(hexDump(actual, from, to));

        Assert.fail(sb.toString());
    }

    private static String hexByte(byte b) {
        return String.format("%02x", b);
    }

    private static String hexDump(byte[] bytes, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < Math.min(to, bytes.length); i++) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(hexByte(bytes[i]));
        }
        return sb.length() == 0 ? "<past end of stream>" : sb.toString();
    }
}
