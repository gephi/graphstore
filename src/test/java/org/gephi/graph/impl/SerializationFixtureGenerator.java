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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
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

/**
 * Builds and regenerates the golden serialization fixtures for the current minor version of graphstore.
 * <p>
 * The fixtures live in <code>src/test/resources/serialization/&lt;MINOR&gt;/</code> and are consumed by
 * {@link SerializationCompatibilityTest}. See <code>src/test/resources/serialization/README.md</code> for the layout
 * and the regeneration rules.
 * <p>
 * The current minor's fixtures are byte-pinned, so every model built here must serialize to the same bytes on every run
 * and every JVM. Run with:
 *
 * <pre>
 * mvn -q test-compile
 * mvn -q dependency:build-classpath -Dmdep.outputFile=target/test-cp.txt
 * java -cp "target/classes:target/test-classes:$(cat target/test-cp.txt)" \
 *     org.gephi.graph.impl.SerializationFixtureGenerator
 * </pre>
 */
public final class SerializationFixtureGenerator {

    /**
     * The minor version the generated fixtures belong to. Must match the project version's MINOR.
     */
    public static final String CURRENT_MINOR = "0.8";

    /**
     * Root of the fixture tree, relative to the project base directory.
     */
    public static final String FIXTURE_ROOT = "src/test/resources/serialization";

    public static final String FILE_EXTENSION = ".graphstore";

    // Fixture names (file name without extension)
    public static final String BASIC = "graph-basic";
    public static final String PARALLEL = "graph-parallel";
    public static final String TYPES_TIMESTAMP = "graph-types-timestamp";
    public static final String TYPES_INTERVAL = "graph-types-interval";
    public static final String VIEWS = "graph-views";

    // Shared constants, also used by the assertions in SerializationCompatibilityTest
    public static final String NODE_ID_1 = "node1";
    public static final String NODE_ID_2 = "node2";
    public static final String NODE_LABEL_1 = "Node 1";
    public static final String NODE_LABEL_2 = "Node 2";
    public static final String EDGE_ID_1 = "edge1";
    public static final String EDGE_ID_2 = "edge2";
    public static final String EDGE_TYPE_1 = "foo";
    public static final String EDGE_TYPE_2 = "bar";

    private SerializationFixtureGenerator() {
        // Utility
    }

    /**
     * Builds every fixture model for the current minor, keyed by fixture name. Iteration order is stable.
     *
     * @return ordered map of fixture name to freshly built graph model
     */
    public static Map<String, GraphModel> buildAll() {
        Map<String, GraphModel> models = new LinkedHashMap<>();
        models.put(BASIC, buildBasic());
        models.put(PARALLEL, buildParallel());
        models.put(TYPES_TIMESTAMP, buildTypeSurface(TimeRepresentation.TIMESTAMP));
        models.put(TYPES_INTERVAL, buildTypeSurface(TimeRepresentation.INTERVAL));
        models.put(VIEWS, buildViews());
        return models;
    }

    /**
     * Two nodes and one edge, mirroring the recipe used for the legacy 0.4 - 0.7 fixtures so the same content
     * assertions apply across every minor.
     *
     * @return graph model
     */
    public static GraphModel buildBasic() {
        GraphModel gm = GraphModel.Factory.newInstance();
        Node node1 = gm.factory().newNode(NODE_ID_1);
        Node node2 = gm.factory().newNode(NODE_ID_2);

        node1.setLabel(NODE_LABEL_1);
        node1.setColor(Color.CYAN);
        node1.setX(10.0f);
        node1.setY(10.0f);
        node1.setZ(1.0f);
        node1.setSize(11.0f);
        node1.setAlpha(0.5f);

        node2.setLabel(NODE_LABEL_2);
        node2.setColor(Color.RED);

        gm.getGraph().addNode(node1);
        gm.getGraph().addNode(node2);

        Edge edge = gm.factory().newEdge(EDGE_ID_1, node1, node2, 0, 1.0, true);
        gm.getGraph().addEdge(edge);

        return gm;
    }

    /**
     * Two nodes and two parallel edges of distinct types, mirroring the legacy 0.4 - 0.7 recipe.
     *
     * @return graph model
     */
    public static GraphModel buildParallel() {
        GraphModel gm = GraphModel.Factory.newInstance();
        Node node1 = gm.factory().newNode(NODE_ID_1);
        Node node2 = gm.factory().newNode(NODE_ID_2);

        node1.setLabel(NODE_LABEL_1);
        node1.setColor(Color.CYAN);
        node1.setX(10.0f);
        node1.setY(10.0f);
        node1.setZ(1.0f);
        node1.setSize(11.0f);
        node1.setAlpha(0.5f);

        node2.setLabel(NODE_LABEL_2);
        node2.setColor(Color.RED);

        gm.getGraph().addNode(node1);
        gm.getGraph().addNode(node2);

        int type1 = gm.addEdgeType(EDGE_TYPE_1);
        int type2 = gm.addEdgeType(EDGE_TYPE_2);

        gm.getGraph().addEdge(gm.factory().newEdge(EDGE_ID_1, node1, node2, type1, 1.0, true));
        gm.getGraph().addEdge(gm.factory().newEdge(EDGE_ID_2, node1, node2, type2, 1.0, true));

        return gm;
    }

    /**
     * The type-surface fixture: one node column per supported static type, one per dynamic type of the given time
     * representation, dynamic edge weights, graph attributes, text properties and non-default element properties.
     * <p>
     * The two time representations select different index-store implementations, hence one fixture each.
     *
     * @param timeRepresentation time representation to build for
     * @return graph model
     */
    public static GraphModel buildTypeSurface(TimeRepresentation timeRepresentation) {
        boolean interval = timeRepresentation == TimeRepresentation.INTERVAL;
        Configuration config = Configuration.builder().timeRepresentation(timeRepresentation)
                .edgeWeightType(interval ? IntervalDoubleMap.class : TimestampDoubleMap.class).build();
        GraphModel gm = GraphModel.Factory.newInstance(config);
        gm.setTimeFormat(interval ? TimeFormat.DATETIME : TimeFormat.DOUBLE);
        gm.setTimeZone(ZoneId.of("Europe/Paris"));

        Table nodeTable = gm.getNodeTable();
        addStaticColumns(nodeTable);
        addDynamicColumns(nodeTable, timeRepresentation);
        addCollectionColumns(nodeTable);

        Table edgeTable = gm.getEdgeTable();
        addStaticColumns(edgeTable);
        addDynamicColumns(edgeTable, timeRepresentation);

        Graph graph = gm.getGraph();
        Node node1 = gm.factory().newNode(NODE_ID_1);
        Node node2 = gm.factory().newNode(NODE_ID_2);
        node1.setLabel(NODE_LABEL_1);
        node2.setLabel(NODE_LABEL_2);
        graph.addNode(node1);
        graph.addNode(node2);

        // Non-default element and text properties
        node1.setColor(new Color(12, 34, 56));
        node1.setAlpha(0.75f);
        node1.setPosition(1.5f, -2.5f, 3.5f);
        node1.setSize(7.25f);
        node1.setFixed(true);
        node1.getTextProperties().setColor(new Color(200, 100, 50));
        node1.getTextProperties().setSize(13.5f);
        node1.getTextProperties().setVisible(false);
        node1.getTextProperties().setText("node one text");
        node1.getTextProperties().setDimensions(42.0f, 24.0f);

        // Fill every static and dynamic column on node1, leave node2 on defaults
        setStaticValues(node1);
        setDynamicValues(node1, timeRepresentation);
        setCollectionValues(node1);
        if (interval) {
            node1.addInterval(new Interval(1.0, 5.0));
            node2.addInterval(new Interval(2.0, 3.0));
        } else {
            node1.addTimestamp(1.0);
            node1.addTimestamp(4.0);
            node2.addTimestamp(2.0);
        }

        int typeFoo = gm.addEdgeType(EDGE_TYPE_1);
        Edge edge1 = gm.factory().newEdge(EDGE_ID_1, node1, node2, typeFoo, 1.0, true);
        graph.addEdge(edge1);
        edge1.setLabel("Edge 1");
        edge1.setColor(new Color(9, 8, 7));
        edge1.getTextProperties().setText("edge one text");
        edge1.getTextProperties().setSize(3.5f);
        setStaticValues(edge1);
        setDynamicValues(edge1, timeRepresentation);

        // Dynamic edge weight
        if (interval) {
            edge1.setWeight(2.5, new Interval(1.0, 2.0));
            edge1.setWeight(3.5, new Interval(3.0, 4.0));
        } else {
            edge1.setWeight(2.5, 1.0);
            edge1.setWeight(3.5, 3.0);
        }

        // Graph attributes. Safe to byte-pin: GraphAttributesImpl keeps a canonical (sorted) order.
        graph.setAttribute("attr-string", "graph level");
        graph.setAttribute("attr-int", 42);
        graph.setAttribute("attr-double", 3.5);
        graph.setAttribute("attr-char", 'g');
        graph.setAttribute("attr-char-array", new char[] { 'a', 'b' });
        graph.setAttribute("attr-instant", Instant.ofEpochSecond(1_600_000_000L, 123));
        if (interval) {
            graph.setAttribute("attr-dynamic", "first", new Interval(1.0, 2.0));
            graph.setAttribute("attr-dynamic", "second", new Interval(3.0, 4.0));
        } else {
            graph.setAttribute("attr-dynamic", "first", 1.0);
            graph.setAttribute("attr-dynamic", "second", 3.0);
        }

        return gm;
    }

    /**
     * Views, edge types, mixed directedness and self-loops.
     *
     * @return graph model
     */
    public static GraphModel buildViews() {
        GraphModel gm = GraphModel.Factory.newInstance();

        Table nodeTable = gm.getNodeTable();
        nodeTable.addColumn("weight", Double.class);
        nodeTable.addColumn("tag", String.class);

        Graph graph = gm.getGraph();
        Node[] nodes = new Node[5];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = gm.factory().newNode("n" + i);
            nodes[i].setLabel("Node " + i);
            nodes[i].setAttribute("weight", (double) i);
            nodes[i].setAttribute("tag", "tag" + i);
            graph.addNode(nodes[i]);
        }

        int typeFoo = gm.addEdgeType(EDGE_TYPE_1);
        int typeBar = gm.addEdgeType(EDGE_TYPE_2);

        // Directed, undirected and self-loop edges over multiple types
        graph.addEdge(gm.factory().newEdge("e0", nodes[0], nodes[1], 0, 1.0, true));
        graph.addEdge(gm.factory().newEdge("e1", nodes[1], nodes[2], typeFoo, 2.0, true));
        graph.addEdge(gm.factory().newEdge("e2", nodes[2], nodes[3], typeBar, 3.0, false));
        graph.addEdge(gm.factory().newEdge("e3", nodes[3], nodes[4], typeFoo, 4.0, false));
        graph.addEdge(gm.factory().newEdge("e4", nodes[4], nodes[4], typeBar, 5.0, true));

        // A node+edge view holding a subset
        GraphView nodeAndEdgeView = gm.createView();
        Graph subGraph = gm.getGraph(nodeAndEdgeView);
        subGraph.addNode(nodes[0]);
        subGraph.addNode(nodes[1]);
        subGraph.addNode(nodes[2]);
        subGraph.addEdge(graph.getEdge("e0"));
        subGraph.setAttribute("view-attr", "on the view");
        subGraph.setAttribute("view-count", 3);

        // A node-only view
        GraphView nodeOnlyView = gm.createView(true, false);
        Graph nodeOnlyGraph = gm.getGraph(nodeOnlyView);
        nodeOnlyGraph.addNode(nodes[3]);
        nodeOnlyGraph.addNode(nodes[4]);

        // The visible view stays on the main view. GraphViewStore.visibleView is not part of the serialized format,
        // see Serialization.serializeViewStore.

        graph.setAttribute("graph-attr", "main graph");

        return gm;
    }

    // Column and value helpers

    /**
     * Every supported static type, in a fixed order. Boxed array types are intentionally included: they standardize to
     * their primitive counterparts, which is part of the surface worth pinning.
     */
    private static void addStaticColumns(Table table) {
        table.addColumn("t_boolean", Boolean.class);
        table.addColumn("t_integer", Integer.class);
        table.addColumn("t_short", Short.class);
        table.addColumn("t_long", Long.class);
        table.addColumn("t_biginteger", BigInteger.class);
        table.addColumn("t_byte", Byte.class);
        table.addColumn("t_float", Float.class);
        table.addColumn("t_double", Double.class);
        table.addColumn("t_bigdecimal", BigDecimal.class);
        table.addColumn("t_character", Character.class);
        table.addColumn("t_string", String.class);
        table.addColumn("t_instant", Instant.class);

        table.addColumn("t_boolean_array", boolean[].class);
        table.addColumn("t_int_array", int[].class);
        table.addColumn("t_short_array", short[].class);
        table.addColumn("t_long_array", long[].class);
        table.addColumn("t_biginteger_array", BigInteger[].class);
        table.addColumn("t_byte_array", byte[].class);
        table.addColumn("t_float_array", float[].class);
        table.addColumn("t_double_array", double[].class);
        table.addColumn("t_bigdecimal_array", BigDecimal[].class);
        table.addColumn("t_char_array", char[].class);
        table.addColumn("t_string_array", String[].class);

        // Boxed arrays, standardized to primitive arrays by the table
        table.addColumn("t_boxed_boolean_array", Boolean[].class);
        table.addColumn("t_boxed_character_array", Character[].class);
    }

    private static void addDynamicColumns(Table table, TimeRepresentation timeRepresentation) {
        if (timeRepresentation == TimeRepresentation.INTERVAL) {
            table.addColumn("t_interval_set", IntervalSet.class);
            table.addColumn("t_interval_boolean", IntervalBooleanMap.class);
            table.addColumn("t_interval_integer", IntervalIntegerMap.class);
            table.addColumn("t_interval_short", IntervalShortMap.class);
            table.addColumn("t_interval_long", IntervalLongMap.class);
            table.addColumn("t_interval_byte", IntervalByteMap.class);
            table.addColumn("t_interval_float", IntervalFloatMap.class);
            table.addColumn("t_interval_double", IntervalDoubleMap.class);
            table.addColumn("t_interval_char", IntervalCharMap.class);
            table.addColumn("t_interval_string", IntervalStringMap.class);
        } else {
            table.addColumn("t_timestamp_set", TimestampSet.class);
            table.addColumn("t_timestamp_boolean", TimestampBooleanMap.class);
            table.addColumn("t_timestamp_integer", TimestampIntegerMap.class);
            table.addColumn("t_timestamp_short", TimestampShortMap.class);
            table.addColumn("t_timestamp_long", TimestampLongMap.class);
            table.addColumn("t_timestamp_byte", TimestampByteMap.class);
            table.addColumn("t_timestamp_float", TimestampFloatMap.class);
            table.addColumn("t_timestamp_double", TimestampDoubleMap.class);
            table.addColumn("t_timestamp_char", TimestampCharMap.class);
            table.addColumn("t_timestamp_string", TimestampStringMap.class);
        }
    }

    /**
     * List, Set and Map columns. Only the List gets a value: lists round-trip order-preserving, whereas generic sets
     * and maps are serialized in hash order and come back as fastutil implementations, so they cannot be byte-pinned.
     */
    private static void addCollectionColumns(Table table) {
        table.addColumn("t_list", List.class);
        table.addColumn("t_set", java.util.Set.class);
        table.addColumn("t_map", Map.class);
    }

    private static void setStaticValues(org.gephi.graph.api.Element element) {
        element.setAttribute("t_boolean", Boolean.TRUE);
        element.setAttribute("t_integer", 123456);
        element.setAttribute("t_short", (short) -12);
        element.setAttribute("t_long", 9876543210L);
        element.setAttribute("t_biginteger", new BigInteger("123456789012345678901234567890"));
        element.setAttribute("t_byte", (byte) 7);
        element.setAttribute("t_float", 1.25f);
        element.setAttribute("t_double", -2.5);
        element.setAttribute("t_bigdecimal", new BigDecimal("1234567890.0987654321"));
        element.setAttribute("t_character", 'Z');
        element.setAttribute("t_string", "hello é中文");
        element.setAttribute("t_instant", Instant.ofEpochSecond(1_500_000_000L, 42));

        element.setAttribute("t_boolean_array", new boolean[] { true, false, true });
        element.setAttribute("t_int_array", new int[] { 1, -2, 3 });
        element.setAttribute("t_short_array", new short[] { 4, -5 });
        element.setAttribute("t_long_array", new long[] { 6L, -7L });
        element.setAttribute("t_biginteger_array", new BigInteger[] { BigInteger.ONE, new BigInteger("-99") });
        element.setAttribute("t_byte_array", new byte[] { 8, -9 });
        element.setAttribute("t_float_array", new float[] { 1.5f, -2.5f });
        element.setAttribute("t_double_array", new double[] { 3.5, -4.5 });
        element.setAttribute("t_bigdecimal_array", new BigDecimal[] { BigDecimal.ONE, new BigDecimal("-0.5") });
        element.setAttribute("t_char_array", new char[] { 'a', 'é', '中' });
        // No null elements: Serialization.serializeStringArray does not support them
        element.setAttribute("t_string_array", new String[] { "one", "two", "three" });

        element.setAttribute("t_boxed_boolean_array", new Boolean[] { Boolean.FALSE, Boolean.TRUE });
        element.setAttribute("t_boxed_character_array", new Character[] { 'x', 'y' });
    }

    private static void setDynamicValues(org.gephi.graph.api.Element element, TimeRepresentation timeRepresentation) {
        if (timeRepresentation == TimeRepresentation.INTERVAL) {
            IntervalSet set = new IntervalSet();
            set.add(new Interval(1.0, 2.0));
            set.add(new Interval(3.0, 4.0));
            element.setAttribute("t_interval_set", set);

            IntervalBooleanMap booleanMap = new IntervalBooleanMap();
            booleanMap.put(new Interval(1.0, 2.0), true);
            booleanMap.put(new Interval(3.0, 4.0), false);
            element.setAttribute("t_interval_boolean", booleanMap);

            IntervalIntegerMap integerMap = new IntervalIntegerMap();
            integerMap.put(new Interval(1.0, 2.0), 10);
            integerMap.put(new Interval(3.0, 4.0), -20);
            element.setAttribute("t_interval_integer", integerMap);

            IntervalShortMap shortMap = new IntervalShortMap();
            shortMap.put(new Interval(1.0, 2.0), (short) 30);
            element.setAttribute("t_interval_short", shortMap);

            IntervalLongMap longMap = new IntervalLongMap();
            longMap.put(new Interval(1.0, 2.0), 40L);
            element.setAttribute("t_interval_long", longMap);

            IntervalByteMap byteMap = new IntervalByteMap();
            byteMap.put(new Interval(1.0, 2.0), (byte) 50);
            element.setAttribute("t_interval_byte", byteMap);

            IntervalFloatMap floatMap = new IntervalFloatMap();
            floatMap.put(new Interval(1.0, 2.0), 6.5f);
            element.setAttribute("t_interval_float", floatMap);

            IntervalDoubleMap doubleMap = new IntervalDoubleMap();
            doubleMap.put(new Interval(1.0, 2.0), 7.5);
            element.setAttribute("t_interval_double", doubleMap);

            IntervalCharMap charMap = new IntervalCharMap();
            charMap.put(new Interval(1.0, 2.0), 'c');
            charMap.put(new Interval(3.0, 4.0), '中');
            element.setAttribute("t_interval_char", charMap);

            IntervalStringMap stringMap = new IntervalStringMap();
            stringMap.put(new Interval(1.0, 2.0), "alpha");
            stringMap.put(new Interval(3.0, 4.0), "beta");
            element.setAttribute("t_interval_string", stringMap);
        } else {
            TimestampSet set = new TimestampSet();
            set.add(1.0);
            set.add(3.0);
            element.setAttribute("t_timestamp_set", set);

            TimestampBooleanMap booleanMap = new TimestampBooleanMap();
            booleanMap.put(1.0, true);
            booleanMap.put(3.0, false);
            element.setAttribute("t_timestamp_boolean", booleanMap);

            TimestampIntegerMap integerMap = new TimestampIntegerMap();
            integerMap.put(1.0, 10);
            integerMap.put(3.0, -20);
            element.setAttribute("t_timestamp_integer", integerMap);

            TimestampShortMap shortMap = new TimestampShortMap();
            shortMap.put(1.0, (short) 30);
            element.setAttribute("t_timestamp_short", shortMap);

            TimestampLongMap longMap = new TimestampLongMap();
            longMap.put(1.0, 40L);
            element.setAttribute("t_timestamp_long", longMap);

            TimestampByteMap byteMap = new TimestampByteMap();
            byteMap.put(1.0, (byte) 50);
            element.setAttribute("t_timestamp_byte", byteMap);

            TimestampFloatMap floatMap = new TimestampFloatMap();
            floatMap.put(1.0, 6.5f);
            element.setAttribute("t_timestamp_float", floatMap);

            TimestampDoubleMap doubleMap = new TimestampDoubleMap();
            doubleMap.put(1.0, 7.5);
            element.setAttribute("t_timestamp_double", doubleMap);

            TimestampCharMap charMap = new TimestampCharMap();
            charMap.put(1.0, 'c');
            charMap.put(3.0, '中');
            element.setAttribute("t_timestamp_char", charMap);

            TimestampStringMap stringMap = new TimestampStringMap();
            stringMap.put(1.0, "alpha");
            stringMap.put(3.0, "beta");
            element.setAttribute("t_timestamp_string", stringMap);
        }
    }

    private static void setCollectionValues(org.gephi.graph.api.Element element) {
        List<String> list = new ArrayList<>();
        list.add("first");
        list.add("second");
        element.setAttribute("t_list", list);
        // t_set and t_map stay null, see addCollectionColumns
    }

    // Serialization helpers

    /**
     * Serializes the model through the production path, exactly as the fixture files on disk were written.
     *
     * @param graphModel model to serialize
     * @return serialized bytes
     * @throws IOException if an io error occurs
     */
    public static byte[] serialize(GraphModel graphModel) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            GraphModel.Serialization.write(dos, graphModel);
        }
        return baos.toByteArray();
    }

    /**
     * Regenerates every fixture of the current minor.
     *
     * @param args optional single argument, the fixture root directory (defaults to {@link #FIXTURE_ROOT})
     * @throws IOException if an io error occurs
     */
    public static void main(String[] args) throws IOException {
        File root = new File(args.length > 0 ? args[0] : FIXTURE_ROOT, CURRENT_MINOR);
        if (!root.exists() && !root.mkdirs()) {
            throw new IOException("Can't create the fixture folder " + root.getAbsolutePath());
        }
        System.out.println("Writing " + CURRENT_MINOR + " fixtures to " + root.getAbsolutePath());
        for (Map.Entry<String, GraphModel> entry : buildAll().entrySet()) {
            File file = new File(root, entry.getKey() + FILE_EXTENSION);
            byte[] bytes = serialize(entry.getValue());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
            }
            System.out.println("  " + file.getName() + " (" + bytes.length + " bytes)");
        }

        // Cheap self-check: every declared type must actually be supported
        for (Class<?> type : new Class<?>[] { Boolean.class, Integer.class, Short.class, Long.class, BigInteger.class, Byte.class, Float.class, Double.class, BigDecimal.class, Character.class, String.class, Instant.class, boolean[].class, int[].class, short[].class, long[].class, BigInteger[].class, byte[].class, float[].class, double[].class, BigDecimal[].class, char[].class, String[].class, List.class, java.util.Set.class, Map.class }) {
            if (!AttributeUtils.isSupported(type)) {
                throw new IllegalStateException("Type no longer supported: " + type);
            }
        }
        System.out.println("Done.");
    }
}
