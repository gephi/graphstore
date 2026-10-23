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

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.impl.utils.DataInputOutput;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.gephi.graph.api.TimeIndex;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampSet;

public class GraphModelTest {

    @Test
    public void testFactory() {
        Assert.assertNotNull(GraphModel.Factory.newInstance());
    }

    @Test
    public void testEmpty() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertNotNull(graphModel.getStore());
        Assert.assertNotNull(graphModel.getGraph());
        Assert.assertNotNull(graphModel.factory());
        Assert.assertNotNull(graphModel.getDirectedGraph());
        Assert.assertNotNull(graphModel.getUndirectedGraph());
        Assert.assertNotNull(graphModel.getNodeTable());
        Assert.assertNotNull(graphModel.getEdgeTable());
    }

    @Test
    public void testGetGraphVisibleDefault() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertSame(graphModel.getGraphVisible(), graphModel.getGraph());
    }

    @Test
    public void testGetGraphVisible() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        graphModel.setVisibleView(view);
        Assert.assertSame(graphModel.getGraphVisible(), graphModel.getGraph(view));
        graphModel.setVisibleView(null);
        Assert.assertSame(graphModel.getGraphVisible(), graphModel.getGraph());
        graphModel.setVisibleView(view);
        graphModel.setVisibleView(graphModel.store.getView());
        Assert.assertSame(graphModel.getGraphVisible(), graphModel.getGraph());
    }

    // A reader that polls isDynamic() must never see false while the graph keeps a time
    @Test
    public void testIsDynamicWhenReplacingTimeMap() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Column column = graphModel.getNodeTable().addColumn("foo", TimestampDoubleMap.class);
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        TimestampDoubleMap first = new TimestampDoubleMap();
        first.put(5.0, 1.0);
        TimestampDoubleMap second = new TimestampDoubleMap();
        second.put(5.0, 2.0);
        n.setAttribute(column, first);

        // The only time stays in the graph while the value is replaced
        int notDynamic = countNotDynamicAnswers(graphModel, () -> {
            for (int i = 0; i < 100000; i++) {
                n.setAttribute(column, (i & 1) == 0 ? second : first);
            }
        });
        Assert.assertEquals(notDynamic, 0);
    }

    // A reader that polls isDynamic() must never see false while the graph keeps a time
    @Test
    public void testIsDynamicWhenReplacingTimeSet() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        n.addTimestamp(5.0);
        TimestampSet first = new TimestampSet();
        first.add(5.0);
        TimestampSet second = new TimestampSet();
        second.add(5.0);
        second.add(6.0);
        Column timeset = graphModel.defaultColumns().nodeTimeSet();

        int notDynamic = countNotDynamicAnswers(graphModel, () -> {
            for (int i = 0; i < 100000; i++) {
                n.setAttribute(timeset, (i & 1) == 0 ? second : first);
            }
        });
        Assert.assertEquals(notDynamic, 0);
        Assert.assertTrue(n.hasTimestamp(5.0));
    }

    // A value changed in place and set again indexes its new times
    @Test
    public void testSetTimeMapChangedInPlace() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Column column = graphModel.getNodeTable().addColumn("foo", TimestampDoubleMap.class);
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        TimestampDoubleMap map = new TimestampDoubleMap();
        map.put(5.0, 1.0);
        n.setAttribute(column, map);

        map.put(7.0, 1.0);
        n.setAttribute(column, map);

        Assert.assertEquals(graphModel.getTimeBounds(), new Interval(5.0, 7.0));
    }

    // A value changed in place and set again indexes its new times
    @Test
    public void testSetTimeSetChangedInPlace() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        n.addTimestamp(5.0);
        Column timeset = graphModel.defaultColumns().nodeTimeSet();

        TimestampSet set = (TimestampSet) n.getAttribute(timeset);
        set.add(7.0);
        n.setAttribute(timeset, set);

        Assert.assertEquals(graphModel.getTimeBounds(), new Interval(5.0, 7.0));
    }

    @Test
    public void testReplaceTimeSetAfterRemovingTimestamps() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        for (double t = 1.0; t <= 5.0; t++) {
            n.addTimestamp(t);
        }
        n.removeTimestamp(1.0);
        n.removeTimestamp(2.0);
        n.removeTimestamp(3.0);
        Column timeset = graphModel.defaultColumns().nodeTimeSet();

        // Replacing the time set after removals must index exactly the timestamps of the new set
        n.setAttribute(timeset, new TimestampSet(new double[] { 4.0, 5.0 }));
        Assert.assertEquals(graphModel.getTimeBounds(), new Interval(4.0, 5.0));

        n.setAttribute(timeset, new TimestampSet());
        Assert.assertFalse(graphModel.isDynamic());
    }

    private static int countNotDynamicAnswers(GraphModelImpl graphModel, Runnable writer) throws Exception {
        AtomicBoolean done = new AtomicBoolean();
        AtomicInteger notDynamic = new AtomicInteger();
        Thread reader = new Thread(() -> {
            while (!done.get()) {
                if (!graphModel.isDynamic()) {
                    notDynamic.incrementAndGet();
                }
            }
        });
        reader.start();
        try {
            writer.run();
        } finally {
            done.set(true);
            reader.join();
        }
        return notDynamic.get();
    }

    @Test
    public void testGetVisibleViewDefault() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertSame(graphModel.getVisibleView(), graphModel.store.mainGraphView);
    }

    @Test
    public void testGetVisibleView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        graphModel.setVisibleView(view);
        Assert.assertSame(graphModel.getVisibleView(), view);
        graphModel.setVisibleView(null);
        Assert.assertSame(graphModel.getVisibleView(), graphModel.store.getView());
    }

    @Test
    public void testAddEdgeType() {
        GraphModelImpl graphModel = new GraphModelImpl();
        int typeId = graphModel.addEdgeType("foo");
        Assert.assertEquals(graphModel.getEdgeTypeLabel(typeId), "foo");
        Assert.assertEquals(graphModel.getEdgeType("foo"), typeId);
    }

    @Test
    public void testGetEdgeTypes() {
        GraphModelImpl graphModel = new GraphModelImpl();
        int typeId = graphModel.addEdgeType("foo");
        Assert.assertEquals(graphModel.getEdgeTypeCount(), 2);
        Assert.assertEquals(graphModel.getEdgeTypes(), new int[] { EdgeTypeStore.NULL_LABEL, typeId });
    }

    @Test
    public void testGetEdgeTypeLabels() {
        GraphModelImpl graphModel = new GraphModelImpl();
        graphModel.addEdgeType("foo");
        Assert.assertEquals(graphModel.getEdgeTypeLabels(), new Object[] { null, "foo" });
        Assert.assertEquals(graphModel.getEdgeTypeLabels(true), new Object[] { null, "foo" });
    }

    @Test
    public void testGetEdgeTypeLabelsEmpty() {
        GraphModelImpl graphModel = new GraphModelImpl();
        graphModel.addEdgeType("foo");
        Assert.assertEquals(graphModel.getEdgeTypeLabels(false), new Object[] {});
    }

    @Test
    public void testGetEdgeTypeLabelsNotEmpty() {
        GraphModelImpl graphModel = GraphGenerator.generateTinyGraphStore().graphModel;
        Assert.assertEquals(graphModel.getEdgeTypeLabels(false), new Object[] { null });
    }

    @Test
    public void testGetEdgeTypeLabelsNotEmptyMultiGraph() {
        GraphModelImpl graphModel = GraphGenerator.generateTinyGraphStore().graphModel;
        Node n1 = graphModel.store.getNode("1");
        Node n2 = graphModel.store.getNode("2");
        graphModel.addEdgeType("bar");
        int type = graphModel.addEdgeType("foo");
        Edge e1 = graphModel.store.factory.newEdge("1", n1, n2, type, 1.0, false);
        graphModel.store.addEdge(e1);
        Assert.assertEquals(graphModel.getEdgeTypeLabels(false), new Object[] { null, "foo" });
    }

    @Test
    public void testIsMultiGraph() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertFalse(graphModel.isMultiGraph());
        int type = graphModel.addEdgeType("foo");
        Assert.assertFalse(graphModel.isMultiGraph());

        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        Edge e0 = graphModel.factory().newEdge("0", n1, n2, EdgeTypeStore.NULL_LABEL, 1.0, true);
        Edge e1 = graphModel.factory().newEdge("1", n1, n2, type, 1.0, true);
        graphModel.store.addEdge(e0);
        Assert.assertFalse(graphModel.isMultiGraph());
        graphModel.store.addEdge(e1);
        Assert.assertTrue(graphModel.isMultiGraph());

        graphModel.store.removeEdge(e1);
        Assert.assertFalse(graphModel.isMultiGraph());

        e0.setType(type);
        Assert.assertFalse(graphModel.isMultiGraph());
        graphModel.store.addEdge(graphModel.factory().newEdge("2", n2, n1, EdgeTypeStore.NULL_LABEL, 1.0, true));
        Assert.assertTrue(graphModel.isMultiGraph());

        graphModel.store.clearEdges();
        Assert.assertFalse(graphModel.isMultiGraph());
    }

    @Test
    public void testIsMultiGraphWithAutoRegisteredType() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        graphModel.store.addEdge(graphModel.factory().newEdge("0", n1, n2, 0, 1.0, true));
        graphModel.store.addEdge(graphModel.factory().newEdge("1", n1, n2, 1, 1.0, true));
        Assert.assertTrue(graphModel.store.edgeTypeStore.contains(1));
        Assert.assertTrue(graphModel.isMultiGraph());
    }

    @Test
    public void testIsMultiGraphRemoveEdgesOneByOne() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        Node n3 = graphModel.factory().newNode("3");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2, n3));
        graphModel.store.addEdge(graphModel.factory().newEdge("0", n1, n2, 0, 1.0, true));
        Edge e1 = graphModel.factory().newEdge("1", n1, n2, 1, 1.0, true);
        Edge e2 = graphModel.factory().newEdge("2", n2, n3, 1, 1.0, true);
        graphModel.store.addAllEdges(Arrays.asList(e1, e2));

        graphModel.store.removeEdge(e1);
        Assert.assertTrue(graphModel.isMultiGraph());
        graphModel.store.removeEdge(e2);
        Assert.assertFalse(graphModel.isMultiGraph());
    }

    @Test
    public void testIsMultiGraphRemoveNode() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        Node n3 = graphModel.factory().newNode("3");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2, n3));
        graphModel.store.addEdge(graphModel.factory().newEdge("0", n1, n2, 0, 1.0, true));
        graphModel.store.addEdge(graphModel.factory().newEdge("1", n2, n3, 1, 1.0, true));
        Assert.assertTrue(graphModel.isMultiGraph());

        graphModel.store.removeNode(n3);
        Assert.assertFalse(graphModel.isMultiGraph());
    }

    @Test
    public void testIsMultiGraphSetTypeBeforeAdd() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        graphModel.store.addEdge(graphModel.factory().newEdge("0", n1, n2, 0, 1.0, true));
        Edge edge = graphModel.factory().newEdge("1", n2, n1, 0, 1.0, true);

        edge.setType(1);
        Assert.assertFalse(graphModel.isMultiGraph());
        graphModel.store.addEdge(edge);
        Assert.assertTrue(graphModel.isMultiGraph());
    }

    @Test
    public void testIsMultiGraphAfterSerialization() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        int type = graphModel.addEdgeType("foo");
        graphModel.addEdgeType("bar");
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        graphModel.store.addEdge(graphModel.factory().newEdge("0", n1, n2, type, 1.0, true));
        Assert.assertFalse(roundTrip(graphModel).isMultiGraph());

        graphModel.store.addEdge(graphModel.factory().newEdge("1", n2, n1, 0, 1.0, true));
        Assert.assertTrue(roundTrip(graphModel).isMultiGraph());
    }

    @Test
    public void testAddEdgeTypeWithConfiguredLabelType() {
        GraphModelImpl graphModel = new GraphModelImpl(Configuration.builder().edgeLabelType(Integer.class).build());

        int type = graphModel.addEdgeType(42);

        Assert.assertEquals(graphModel.getEdgeTypeLabel(type), 42);
        Assert.assertThrows(IllegalArgumentException.class, () -> graphModel.addEdgeType("foo"));
    }

    @Test
    public void testAddEdgeWithConfiguredAutoRegistrationDisabled() {
        GraphModelImpl graphModel = new GraphModelImpl(
                Configuration.builder().enableAutoEdgeTypeRegistration(false).build());
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));

        Assert.assertThrows(UnsupportedOperationException.class, () -> graphModel.store
                .addEdge(graphModel.factory().newEdge("0", n1, n2, 5, 1.0, true)));
        Assert.assertEquals(graphModel.store.getEdgeCount(), 0);

        int type = graphModel.addEdgeType("foo");
        Assert.assertTrue(graphModel.store.addEdge(graphModel.factory().newEdge("1", n1, n2, type, 1.0, true)));
    }

    @Test
    public void testGetEdgeTypeCountAfterSerialization() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        graphModel.addEdgeType("foo");
        graphModel.addEdgeType("bar");

        Assert.assertEquals(roundTrip(graphModel).getEdgeTypeCount(), 3);
    }

    @Test
    public void testGetEdgeTypeCountDoesNotBlockOnWriteLock() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        graphModel.addEdgeType("foo");

        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            graphModel.store.writeLock();
            try {
                locked.countDown();
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                graphModel.store.writeUnlock();
            }
        });
        writer.start();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Assert.assertTrue(locked.await(5, TimeUnit.SECONDS));
            Future<Integer> count = executor.submit(graphModel::getEdgeTypeCount);
            Assert.assertEquals(count.get(5, TimeUnit.SECONDS).intValue(), 2);
        } finally {
            release.countDown();
            writer.join();
            executor.shutdownNow();
        }
    }

    // The default type is always registered, so the count is never below one while edges and their types are cleared
    @Test
    public void testGetEdgeTypeCountWhenEdgesAreCleared() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));

        AtomicBoolean done = new AtomicBoolean();
        AtomicInteger invalid = new AtomicInteger();
        Thread reader = new Thread(() -> {
            while (!done.get()) {
                int count = graphModel.getEdgeTypeCount();
                if (count < 1 || count > 2) {
                    invalid.incrementAndGet();
                }
            }
        });
        reader.start();
        try {
            for (int i = 0; i < 20000; i++) {
                graphModel.store.addEdge(graphModel.factory().newEdge("e", n1, n2, 1, 1.0, true));
                graphModel.store.clearEdges();
            }
        } finally {
            done.set(true);
            reader.join();
        }
        Assert.assertEquals(invalid.get(), 0);
    }

    private static GraphModelImpl roundTrip(GraphModelImpl graphModel) throws Exception {
        Serialization ser = new Serialization(graphModel);
        DataInputOutput dio = new DataInputOutput();
        ser.serializeGraphModel(dio, graphModel);
        return ser.deserializeGraphModel(dio.reset(dio.toByteArray()));
    }

    @Test
    public void testIsMultiGraphDoesNotBlockOnWriteLock() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        graphModel.store.addEdge(graphModel.factory().newEdge("0", n1, n2, 0, 1.0, true));
        graphModel.store.addEdge(graphModel.factory().newEdge("1", n1, n2, 1, 1.0, true));

        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            graphModel.store.writeLock();
            try {
                locked.countDown();
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                graphModel.store.writeUnlock();
            }
        });
        writer.start();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Assert.assertTrue(locked.await(5, TimeUnit.SECONDS));
            Future<Boolean> multiGraph = executor.submit(graphModel::isMultiGraph);
            Assert.assertTrue(multiGraph.get(5, TimeUnit.SECONDS));
        } finally {
            release.countDown();
            writer.join();
            executor.shutdownNow();
        }
    }

    @Test
    public void testIsDirectedDefault() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertTrue(graphModel.isDirected());
        Assert.assertFalse(graphModel.isUndirected());
    }

    @Test
    public void testIsUndirected() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[] { n1, n2 }));
        Edge e = graphModel.factory().newEdge(n1, n2, false);
        graphModel.getStore().addEdge(e);
        Assert.assertTrue(graphModel.isUndirected());
        Assert.assertFalse(graphModel.isDirected());
    }

    @Test
    public void testIsMixed() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        Node n3 = graphModel.factory().newNode("3");
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[] { n1, n2, n3 }));
        Edge e1 = graphModel.factory().newEdge(n1, n2, false);
        Edge e2 = graphModel.factory().newEdge(n1, n3, true);
        graphModel.getStore().addAllEdges(Arrays.asList(new Edge[] { e1, e2 }));
        Assert.assertTrue(graphModel.isMixed());
        Assert.assertFalse(graphModel.isDirected());
        Assert.assertFalse(graphModel.isUndirected());
    }

    @Test
    public void testCreateView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Assert.assertNotNull(view);
        Assert.assertSame(view.getGraphModel(), graphModel);
    }

    @Test
    public void testCreateViewCustom() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView(true, false);
        Assert.assertTrue(view.isNodeView());
        Assert.assertFalse(view.isEdgeView());
    }

    @Test
    public void testCreateViewWithPredicates() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView(n -> true, e -> true);
        Assert.assertNotNull(view);
        Assert.assertSame(view.getGraphModel(), graphModel);
        Assert.assertTrue(view.isNodeView());
        Assert.assertTrue(view.isEdgeView());
    }

    @Test
    public void testCreateViewWithNodePredicateOnly() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView(n -> true, null);
        Assert.assertNotNull(view);
        Assert.assertTrue(view.isNodeView());
        Assert.assertFalse(view.isEdgeView());
    }

    @Test
    public void testCreateViewWithEdgePredicateOnly() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView(null, e -> true);
        Assert.assertNotNull(view);
        Assert.assertFalse(view.isNodeView());
        Assert.assertTrue(view.isEdgeView());
    }

    @Test
    public void testCreateViewWithBothPredicatesNull() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView(null, null);
        Assert.assertNotNull(view);
        Assert.assertFalse(view.isNodeView());
        Assert.assertFalse(view.isEdgeView());
    }

    @Test
    public void testCreateViewWithNodePredicateFiltering() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();
        Column col = table.addColumn("value", Integer.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col, 10);
        Node n2 = graphModel.factory().newNode("2");
        n2.setAttribute(col, 20);
        Node n3 = graphModel.factory().newNode("3");
        n3.setAttribute(col, 30);
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[] { n1, n2, n3 }));

        // Create view with predicate that filters nodes with value > 15
        GraphView view = graphModel.createView(n -> {
            Integer value = (Integer) n.getAttribute(col);
            return value != null && value > 15;
        }, null);

        Graph subgraph = graphModel.getGraph(view);
        Assert.assertEquals(subgraph.getNodeCount(), 2);
        Assert.assertTrue(subgraph.contains(n2));
        Assert.assertTrue(subgraph.contains(n3));
        Assert.assertFalse(subgraph.contains(n1));
    }

    @Test
    public void testCreateViewWithEdgePredicateFiltering() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getEdgeTable();
        Column col = table.addColumn("weight_custom", Double.class);

        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        Node n3 = graphModel.factory().newNode("3");
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[] { n1, n2, n3 }));

        Edge e1 = graphModel.factory().newEdge(n1, n2);
        e1.setAttribute(col, 1.0);
        Edge e2 = graphModel.factory().newEdge(n2, n3);
        e2.setAttribute(col, 5.0);
        Edge e3 = graphModel.factory().newEdge(n1, n3);
        e3.setAttribute(col, 10.0);
        graphModel.getStore().addAllEdges(Arrays.asList(new Edge[] { e1, e2, e3 }));

        // Create view with predicate that filters edges with weight >= 5.0
        GraphView view = graphModel.createView(null, e -> {
            Double weight = (Double) e.getAttribute(col);
            return weight != null && weight >= 5.0;
        });

        Graph subgraph = graphModel.getGraph(view);
        Assert.assertEquals(subgraph.getNodeCount(), 3); // All nodes included
        Assert.assertEquals(subgraph.getEdgeCount(), 2);
        Assert.assertTrue(subgraph.contains(e2));
        Assert.assertTrue(subgraph.contains(e3));
        Assert.assertFalse(subgraph.contains(e1));
    }

    @Test
    public void testCreateViewWithBothPredicatesFiltering() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table nodeTable = graphModel.getNodeTable();
        Table edgeTable = graphModel.getEdgeTable();
        Column nodeCol = nodeTable.addColumn("active", Boolean.class);
        Column edgeCol = edgeTable.addColumn("strength", Double.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(nodeCol, true);
        Node n2 = graphModel.factory().newNode("2");
        n2.setAttribute(nodeCol, false);
        Node n3 = graphModel.factory().newNode("3");
        n3.setAttribute(nodeCol, true);
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[] { n1, n2, n3 }));

        Edge e1 = graphModel.factory().newEdge(n1, n2);
        e1.setAttribute(edgeCol, 0.5);
        Edge e2 = graphModel.factory().newEdge(n2, n3);
        e2.setAttribute(edgeCol, 0.8);
        Edge e3 = graphModel.factory().newEdge(n1, n3);
        e3.setAttribute(edgeCol, 0.3);
        graphModel.getStore().addAllEdges(Arrays.asList(new Edge[] { e1, e2, e3 }));

        // Create view with both predicates
        GraphView view = graphModel.createView(n -> Boolean.TRUE.equals(n.getAttribute(nodeCol)), e -> {
            Double strength = (Double) e.getAttribute(edgeCol);
            return strength != null && strength > 0.4;
        });

        Graph subgraph = graphModel.getGraph(view);

        Assert.assertEquals(subgraph.getNodeCount(), 2); // n1 and n3
        Assert.assertTrue(subgraph.contains(n1));
        Assert.assertTrue(subgraph.contains(n3));
        Assert.assertFalse(subgraph.contains(n2));

        // No edges should be included:
        // - e1 has n2 which is not in node view (active=false)
        // - e2 has n2 which is not in node view (active=false)
        // - e3 connects n1 and n3 (both in view) but strength 0.3 fails edge filter
        Assert.assertEquals(subgraph.getEdgeCount(), 0);
        Assert.assertFalse(subgraph.contains(e1));
        Assert.assertFalse(subgraph.contains(e2));
        Assert.assertFalse(subgraph.contains(e3));
    }

    @Test
    public void testCreateViewWithBothPredicatesFilteringWithMatchingEdges() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table nodeTable = graphModel.getNodeTable();
        Table edgeTable = graphModel.getEdgeTable();
        Column nodeCol = nodeTable.addColumn("category", String.class);
        Column edgeCol = edgeTable.addColumn("score", Double.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(nodeCol, "A");
        Node n2 = graphModel.factory().newNode("2");
        n2.setAttribute(nodeCol, "B");
        Node n3 = graphModel.factory().newNode("3");
        n3.setAttribute(nodeCol, "A");
        Node n4 = graphModel.factory().newNode("4");
        n4.setAttribute(nodeCol, "A");
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[] { n1, n2, n3, n4 }));

        Edge e1 = graphModel.factory().newEdge(n1, n2);
        e1.setAttribute(edgeCol, 5.0);
        Edge e2 = graphModel.factory().newEdge(n1, n3);
        e2.setAttribute(edgeCol, 3.0);
        Edge e3 = graphModel.factory().newEdge(n3, n4);
        e3.setAttribute(edgeCol, 8.0);
        Edge e4 = graphModel.factory().newEdge(n2, n4);
        e4.setAttribute(edgeCol, 1.0);
        graphModel.getStore().addAllEdges(Arrays.asList(new Edge[] { e1, e2, e3, e4 }));

        // Create view: nodes with category "A" AND edges with score > 2.0
        GraphView view = graphModel.createView(n -> "A".equals(n.getAttribute(nodeCol)), e -> {
            Double score = (Double) e.getAttribute(edgeCol);
            return score != null && score > 2.0;
        });

        Graph subgraph = graphModel.getGraph(view);

        // Nodes: n1, n3, n4 (all category "A")
        Assert.assertEquals(subgraph.getNodeCount(), 3);
        Assert.assertTrue(subgraph.contains(n1));
        Assert.assertTrue(subgraph.contains(n3));
        Assert.assertTrue(subgraph.contains(n4));
        Assert.assertFalse(subgraph.contains(n2));

        // Edges: only e2 (n1-n3, score 3.0) and e3 (n3-n4, score 8.0)
        // e1 excluded: n2 not in view
        // e4 excluded: n2 not in view
        Assert.assertEquals(subgraph.getEdgeCount(), 2);
        Assert.assertFalse(subgraph.contains(e1));
        Assert.assertTrue(subgraph.contains(e2));
        Assert.assertTrue(subgraph.contains(e3));
        Assert.assertFalse(subgraph.contains(e4));
    }

    @Test
    public void testCopyView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        GraphView viewCopy = graphModel.copyView(view);
        Assert.assertNotNull(viewCopy);
        Assert.assertSame(view.getGraphModel(), graphModel);
    }

    @Test
    public void testCopyViewCustom() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        GraphView viewCopy = graphModel.copyView(view, true, false);
        Assert.assertTrue(viewCopy.isNodeView());
        Assert.assertFalse(viewCopy.isEdgeView());
    }

    @Test
    public void testDestroyView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        graphModel.destroyView(view);
        Assert.assertFalse(graphModel.store.viewStore.contains(view));
    }

    @Test
    public void testSetTimeInterval() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Interval i = new Interval(1.0, 2.0);
        graphModel.setTimeInterval(view, i);
        Assert.assertEquals(view.getTimeInterval(), i);
    }

    @Test
    public void testSetTimeFormat() {
        GraphModelImpl graphModel = new GraphModelImpl();
        graphModel.setTimeFormat(TimeFormat.DATETIME);
        Assert.assertEquals(graphModel.getTimeFormat(), TimeFormat.DATETIME);
        graphModel.setTimeFormat(TimeFormat.DOUBLE);
        Assert.assertEquals(graphModel.getTimeFormat(), TimeFormat.DOUBLE);
    }

    @Test
    public void testSetTimeZone() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertEquals(graphModel.getTimeZone(), ZoneId.of("UTC"));// Default
        graphModel.setTimeZone(ZoneId.of("-02:00"));
        Assert.assertEquals(graphModel.getTimeZone(), ZoneId.of("-02:00"));
        graphModel.setTimeZone(ZoneId.of("UTC"));
        Assert.assertEquals(graphModel.getTimeZone(), ZoneId.of("UTC"));
    }

    @Test
    public void testGetTimeBoundsMainView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertEquals(graphModel.getTimeBounds(), Interval.INFINITY_INTERVAL);
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        n.addTimestamp(1.0);
        n.addTimestamp(5.0);
        Assert.assertEquals(graphModel.getTimeBounds(), new Interval(1.0, 5.0));
    }

    @Test
    public void testIsDynamic() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertFalse(graphModel.isDynamic());
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        n.addTimestamp(1.0);
        Assert.assertTrue(graphModel.isDynamic());
    }

    @Test
    public void testIsDynamicFollowsTimesOfNodesAndEdges() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        Edge e = graphModel.factory().newEdge("0", n1, n2, 0, 1.0, true);
        graphModel.store.addEdge(e);
        Assert.assertFalse(graphModel.isDynamic());

        n1.addTimestamp(1.0);
        e.addTimestamp(2.0);
        Assert.assertTrue(graphModel.isDynamic());

        n1.removeTimestamp(1.0);
        Assert.assertTrue(graphModel.isDynamic());

        graphModel.store.removeEdge(e);
        Assert.assertFalse(graphModel.isDynamic());

        n2.addTimestamp(3.0);
        Assert.assertTrue(graphModel.isDynamic());
        graphModel.store.clear();
        Assert.assertFalse(graphModel.isDynamic());
    }

    @Test
    public void testIsDynamicWithIntervals() {
        GraphModelImpl graphModel = new GraphModelImpl(
                Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build());
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        Assert.assertFalse(graphModel.isDynamic());

        n.addInterval(new Interval(1.0, 2.0));
        Assert.assertTrue(graphModel.isDynamic());
        n.removeInterval(new Interval(1.0, 2.0));
        Assert.assertFalse(graphModel.isDynamic());
    }

    @Test
    public void testIsDynamicAfterSerialization() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        Assert.assertFalse(roundTrip(graphModel).isDynamic());

        n.addTimestamp(1.0);
        Assert.assertTrue(roundTrip(graphModel).isDynamic());
    }

    @Test
    public void testIsDynamicDoesNotBlockOnWriteLock() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        n.addTimestamp(1.0);

        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            graphModel.store.writeLock();
            try {
                locked.countDown();
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                graphModel.store.writeUnlock();
            }
        });
        writer.start();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Assert.assertTrue(locked.await(5, TimeUnit.SECONDS));
            Future<Boolean> dynamic = executor.submit(graphModel::isDynamic);
            Assert.assertTrue(dynamic.get(5, TimeUnit.SECONDS));
        } finally {
            release.countDown();
            writer.join();
            executor.shutdownNow();
        }
    }

    // A reader that polls isDynamic() must never see false while the graph keeps a time
    @Test
    public void testIsDynamicWhenTimeMovesBetweenNodeAndEdge() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        Edge e = graphModel.factory().newEdge("0", n1, n2, 0, 1.0, true);
        graphModel.store.addEdge(e);
        n1.addTimestamp(5.0);

        int notDynamic = countNotDynamicAnswers(graphModel, () -> {
            for (int i = 0; i < 100000; i++) {
                e.addTimestamp(5.0);
                n1.removeTimestamp(5.0);
                n1.addTimestamp(5.0);
                e.removeTimestamp(5.0);
            }
        });
        Assert.assertEquals(notDynamic, 0);
    }

    @Test
    public void testIsDynamicAfterRemovingDynamicColumn() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Column column = graphModel.getNodeTable().addColumn("foo", TimestampDoubleMap.class);
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        n.setAttribute(column, 1.0, 5.0);
        Assert.assertTrue(graphModel.isDynamic());

        graphModel.getNodeTable().removeColumn(column);

        Assert.assertFalse(graphModel.isDynamic());
        Assert.assertEquals(graphModel.getTimeBounds(), Interval.INFINITY_INTERVAL);
    }

    @Test
    public void testTimeBoundsAfterRemovingDynamicColumn() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Column column = graphModel.getNodeTable().addColumn("foo", TimestampDoubleMap.class);
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        n1.setAttribute(column, 1.0, 1.0);
        n1.setAttribute(column, 1.0, 9.0);
        n2.setAttribute(column, 1.0, 5.0);
        n2.addTimestamp(5.0);

        graphModel.getNodeTable().removeColumn(column);

        Assert.assertEquals(graphModel.getTimeBounds(), new Interval(5.0, 5.0));
    }

    @Test
    public void testTimesOfOtherColumnsAfterRemovingDynamicColumn() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Column removed = graphModel.getNodeTable().addColumn("foo", TimestampDoubleMap.class);
        Column kept = graphModel.getNodeTable().addColumn("bar", TimestampDoubleMap.class);
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        n.setAttribute(removed, 1.0, 5.0);
        n.setAttribute(kept, 2.0, 5.0);
        n.setAttribute(kept, 2.0, 7.0);

        graphModel.getNodeTable().removeColumn(removed);

        Assert.assertEquals(graphModel.getTimeBounds(), new Interval(5.0, 7.0));
        Assert.assertTrue(graphModel.isDynamic());
    }

    @Test
    public void testTimesOfOtherNodesAfterRemovingTimeSetColumn() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Column column = graphModel.getNodeTable().addColumn("foo", TimestampSet.class);
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        // The value is set before the node joins the graph, so its times are not in the time index
        n1.setAttribute(column, new TimestampSet(new double[] { 5.0 }));
        n2.addTimestamp(5.0);
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));

        graphModel.getNodeTable().removeColumn(column);

        Assert.assertTrue(graphModel.isDynamic());
        Assert.assertEquals(graphModel.getTimeBounds(), new Interval(5.0, 5.0));
    }

    @Test
    public void testGetTimeBoundsInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Assert.assertEquals(graphModel.getTimeBounds(view), Interval.INFINITY_INTERVAL);

        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[] { n1, n2 }));

        n1.addTimestamp(1.0);
        n1.addTimestamp(5.0);

        n2.addTimestamp(2.0);
        n2.addTimestamp(3.0);

        graphModel.getGraph(view).addNode(n2);

        Assert.assertEquals(graphModel.getTimeBounds(view), new Interval(2.0, 3.0));
    }

    @Test
    public void testGetTimeBoundsDoesNotBlockOnWriteLock() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        n.addTimestamp(1.0);
        n.addTimestamp(5.0);

        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            graphModel.store.writeLock();
            try {
                locked.countDown();
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                graphModel.store.writeUnlock();
            }
        });
        writer.start();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Assert.assertTrue(locked.await(5, TimeUnit.SECONDS));
            Assert.assertEquals(executor.submit(() -> graphModel.getTimeBounds())
                    .get(5, TimeUnit.SECONDS), new Interval(1.0, 5.0));
            Assert.assertEquals(executor.submit(() -> graphModel.getTimeBoundsVisible())
                    .get(5, TimeUnit.SECONDS), new Interval(1.0, 5.0));
        } finally {
            release.countDown();
            writer.join();
            executor.shutdownNow();
        }
    }

    // Bounds are read from a single state of the time index, so they are never a mix of two states
    @Test
    public void testGetTimeBoundsWhenNodesAreAddedAndRemoved() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node anchor = graphModel.factory().newNode("anchor");
        graphModel.store.addNode(anchor);
        anchor.addTimestamp(10.0);
        anchor.addTimestamp(20.0);
        Node n = graphModel.factory().newNode("1");
        n.addTimestamp(1.0);
        n.addTimestamp(30.0);

        int invalid = countInvalidTimeBounds(graphModel, bounds -> bounds.equals(new Interval(10.0, 20.0)) || bounds
                .equals(new Interval(1.0, 30.0)), () -> {
                    for (int i = 0; i < 100000; i++) {
                        graphModel.store.addNode(n);
                        graphModel.store.removeNode(n);
                    }
                });
        Assert.assertEquals(invalid, 0);
    }

    @Test
    public void testGetTimeBoundsWhenTimesChange() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        n.addTimestamp(30.0);

        int invalid = countInvalidTimeBounds(graphModel, bounds -> bounds.equals(new Interval(30.0, 30.0)) || bounds
                .equals(new Interval(10.0, 30.0)) || bounds.equals(new Interval(10.0, 10.0)), () -> {
                    for (int i = 0; i < 100000; i++) {
                        n.addTimestamp(10.0);
                        n.removeTimestamp(30.0);
                        n.addTimestamp(30.0);
                        n.removeTimestamp(10.0);
                    }
                });
        Assert.assertEquals(invalid, 0);
    }

    // Edges only exist with their nodes, so the bounds never come from the edge times alone
    @Test
    public void testGetTimeBoundsWhenGraphIsCleared() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        n1.addTimestamp(30.0);
        Edge e = graphModel.factory().newEdge("0", n1, n2, 0, 1.0, true);
        e.addTimestamp(40.0);
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        graphModel.store.addEdge(e);

        int invalid = countInvalidTimeBounds(graphModel, bounds -> !bounds.equals(new Interval(40.0, 40.0)), () -> {
            for (int i = 0; i < 20000; i++) {
                graphModel.store.clear();
                graphModel.store.addAllNodes(Arrays.asList(n1, n2));
                graphModel.store.addEdge(e);
            }
        });
        Assert.assertEquals(invalid, 0);
    }

    @Test
    public void testGetTimeBoundsVisible() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        n1.addTimestamp(1.0);
        n2.addTimestamp(5.0);
        Assert.assertEquals(graphModel.getTimeBoundsVisible(), new Interval(1.0, 5.0));

        GraphView view = graphModel.createView();
        graphModel.getGraph(view).addNode(n2);
        graphModel.setVisibleView(view);
        Assert.assertEquals(graphModel.getTimeBoundsVisible(), new Interval(5.0, 5.0));

        graphModel.destroyView(view);
        Assert.assertEquals(graphModel.getTimeBoundsVisible(), new Interval(1.0, 5.0));
    }

    @Test
    public void testGetTimeBoundsVisibleWhenViewIsDestroyedWhileWaiting() throws Exception {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.store.addAllNodes(Arrays.asList(n1, n2));
        n1.addTimestamp(1.0);
        n2.addTimestamp(5.0);
        GraphView view = graphModel.createView();
        graphModel.getGraph(view).addNode(n2);
        graphModel.setVisibleView(view);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Interval> bounds;
            graphModel.store.writeLock();
            try {
                bounds = executor.submit(() -> graphModel.getTimeBoundsVisible());
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (!graphModel.store.lock.readWriteLock.hasQueuedThreads() && !bounds.isDone() && System
                        .nanoTime() < deadline) {
                    Thread.sleep(1);
                }
                graphModel.destroyView(view);
            } finally {
                graphModel.store.writeUnlock();
            }
            Assert.assertEquals(bounds.get(5, TimeUnit.SECONDS), new Interval(1.0, 5.0));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testGetTimeBoundsWithIndexConfigDisabled() {
        GraphModelImpl graphModel = new GraphModelImpl(Configuration.builder().enableIndexTime(false).build());
        Node n = graphModel.factory().newNode("1");
        graphModel.store.addNode(n);
        n.addTimestamp(1.0);
        GraphView view = graphModel.createView();

        Assert.assertEquals(graphModel.getTimeBounds(), Interval.INFINITY_INTERVAL);
        Assert.assertEquals(graphModel.getTimeBounds(view), Interval.INFINITY_INTERVAL);
        Assert.assertEquals(graphModel.getTimeBoundsVisible(), Interval.INFINITY_INTERVAL);
        Assert.assertThrows(IllegalArgumentException.class, () -> graphModel.getTimeBounds(foreignView()));
    }

    private static GraphView foreignView() {
        return new GraphView() {
            @Override
            public GraphModel getGraphModel() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isMainView() {
                return false;
            }

            @Override
            public boolean isNodeView() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isEdgeView() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Interval getTimeInterval() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isDestroyed() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static int countInvalidTimeBounds(GraphModelImpl graphModel, Predicate<Interval> valid, Runnable writer) throws Exception {
        AtomicBoolean done = new AtomicBoolean();
        AtomicInteger invalid = new AtomicInteger();
        Thread reader = new Thread(() -> {
            while (!done.get()) {
                try {
                    if (!valid.test(graphModel.getTimeBounds())) {
                        invalid.incrementAndGet();
                    }
                } catch (RuntimeException e) {
                    invalid.incrementAndGet();
                }
            }
        });
        reader.start();
        try {
            writer.run();
        } finally {
            done.set(true);
            reader.join();
        }
        return invalid.get();
    }

    @Test
    public void testCreateGraphObserver() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphObserver obs = graphModel.createGraphObserver(graphModel.getGraph(), true);
        Assert.assertNotNull(obs);
        Assert.assertSame(obs.getGraph(), graphModel.getGraph());
    }

    @Test
    public void testDestroyGraphObserver() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphObserver obs = graphModel.createGraphObserver(graphModel.getGraph(), true);
        graphModel.destroyGraphObserver(obs);
        Assert.assertTrue(obs.isDestroyed());
    }

    @Test
    public void testCreateGraphObserverInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        GraphObserver obs = graphModel.createGraphObserver(graphModel.getGraph(view), true);
        Assert.assertNotNull(obs);
        Assert.assertSame(obs.getGraph(), graphModel.getGraph(view));
    }

    @Test
    public void testDestroyGraphObserverInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        GraphObserver obs = graphModel.createGraphObserver(graphModel.getGraph(view), true);
        graphModel.destroyGraphObserver(obs);
        Assert.assertTrue(obs.isDestroyed());
    }

    @Test
    public void testGetNodeIndex() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();
        Column col = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col, "bar");
        graphModel.getStore().addNode(n1);

        Index index = graphModel.getNodeIndex();
        Assert.assertEquals(index.count(col, "bar"), 1);
        Assert.assertSame(graphModel.getElementIndex(table), index);
    }

    @Test
    public void testGetNodeIndexWithIndexConfigDisabled() {
        GraphModelImpl graphModel = new GraphModelImpl(Configuration.builder().enableIndexNodes(false).build());
        Assert.assertNotNull(graphModel.getNodeIndex());
        Assert.assertNotNull(graphModel.getNodeIndex().getColumnIndex(graphModel.defaultColumns().nodeId()));
    }

    @Test
    public void testGetNodeIndexInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Table table = graphModel.getNodeTable();
        Column col = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col, "bar");
        graphModel.getStore().addNode(n1);
        graphModel.getGraph(view).fill();

        Index index = graphModel.getNodeIndex(view);
        Assert.assertEquals(index.count(col, "bar"), 1);
        Assert.assertSame(graphModel.getElementIndex(table, view), index);
    }

    @Test
    public void testGetEdgeIndex() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getEdgeTable();
        Column col = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n1);
        Edge e = graphModel.factory().newEdge(n1, n1);
        e.setAttribute(col, "bar");
        graphModel.getStore().addEdge(e);

        Index index = graphModel.getEdgeIndex();
        Assert.assertEquals(index.count(col, "bar"), 1);
        Assert.assertSame(graphModel.getElementIndex(table), index);
    }

    @Test
    public void testIndexVersionWithIndexedColumn() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();
        Column col = table.addColumn("foo", String.class);
        Index index = graphModel.getNodeIndex();
        int version = index.getColumnIndex(col).getVersion();
        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col, "bar");
        graphModel.getStore().addNode(n1);
        Assert.assertTrue(index.getColumnIndex(col).getVersion() > version);
    }

    @Test
    public void testIndexVersionWithNoIndexedColumn() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();
        Column col = table.addColumn("foo", "foo", Integer.class, Origin.DATA, null, false);
        Index index = graphModel.getNodeIndex();
        int version = index.getColumnIndex(col).getVersion();
        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col, 42);
        graphModel.getStore().addNode(n1);
        Assert.assertTrue(index.getColumnIndex(col).getVersion() > version);
    }

    @Test
    public void testGetEdgeIndexWithIndexConfigDisabled() {
        GraphModelImpl graphModel = new GraphModelImpl(Configuration.builder().enableIndexEdges(false).build());
        Assert.assertNotNull(graphModel.getEdgeIndex());
        Assert.assertNotNull(graphModel.getEdgeIndex().getColumnIndex(graphModel.defaultColumns().edgeId()));
    }

    @Test
    public void testGetEdgeIndexInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Table table = graphModel.getEdgeTable();
        Column col = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n1);
        Edge e = graphModel.factory().newEdge(n1, n1);
        e.setAttribute(col, "bar");
        graphModel.getStore().addEdge(e);

        graphModel.getGraph(view).fill();

        Index index = graphModel.getEdgeIndex(view);
        Assert.assertEquals(index.count(col, "bar"), 1);
        Assert.assertSame(graphModel.getElementIndex(table, view), index);
    }

    @Test
    public void testGetNodeTimestampIndex() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        n.addTimestamp(1.0);

        TimeIndex<Node> index = graphModel.getNodeTimeIndex();
        Assert.assertEquals(index.getMinTimestamp(), 1.0);
        Assert.assertEquals(index.getMaxTimestamp(), 1.0);
    }

    @Test
    public void testGetNodeTimeIndexWithIndexConfigDisabled() {
        GraphModelImpl graphModel = new GraphModelImpl(Configuration.builder().enableIndexTime(false).build());
        Assert.assertNull(graphModel.getNodeTimeIndex());
    }

    @Test
    public void testGetEdgeTimestampIndex() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        Edge e = graphModel.factory().newEdge(n, n);
        graphModel.getStore().addEdge(e);
        e.addTimestamp(1.0);

        TimeIndex<Edge> index = graphModel.getEdgeTimeIndex();
        Assert.assertEquals(index.getMinTimestamp(), 1.0);
        Assert.assertEquals(index.getMaxTimestamp(), 1.0);
    }

    @Test
    public void testGetEdgeTimeIndexWithIndexConfigDisabled() {
        GraphModelImpl graphModel = new GraphModelImpl(Configuration.builder().enableIndexTime(false).build());
        Assert.assertNull(graphModel.getEdgeTimeIndex());
    }

    @Test
    public void testGetNodeTimestampIndexInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        n.addTimestamp(1.0);
        graphModel.getGraph(view).fill();

        TimeIndex<Node> index = graphModel.getNodeTimeIndex(view);
        Assert.assertEquals(index.getMinTimestamp(), 1.0);
        Assert.assertEquals(index.getMaxTimestamp(), 1.0);
    }

    @Test
    public void testGetEdgeTimestampIndexInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        Edge e = graphModel.factory().newEdge(n, n);
        graphModel.getStore().addEdge(e);
        e.addTimestamp(1.0);
        graphModel.getGraph(view).fill();

        TimeIndex<Edge> index = graphModel.getEdgeTimeIndex(view);
        Assert.assertEquals(index.getMinTimestamp(), 1.0);
        Assert.assertEquals(index.getMaxTimestamp(), 1.0);
    }

    @Test
    public void testSerialization() throws IOException {
        DataInputOutput dio = new DataInputOutput();
        GraphModelImpl graphModelImpl = GraphGenerator.generateSmallGraphStore().graphModel;
        GraphModel.Serialization.write(dio, graphModelImpl);
        byte[] bytes = dio.toByteArray();
        GraphModelImpl readModelImpl = (GraphModelImpl) GraphModel.Serialization.read(dio.reset(bytes));
        Assert.assertTrue(readModelImpl.deepEquals(readModelImpl));
    }

    @Test
    public void testSerializationReadWithoutVersionHeader() throws IOException {
        DataInputOutput dio = new DataInputOutput();
        GraphModelImpl graphModelImpl = GraphGenerator.generateSmallGraphStore().graphModel;
        GraphModel.Serialization.write(dio, graphModelImpl);
        byte[] bytes = dio.toByteArray();
        dio.reset(bytes);
        dio.skip(5);// id byte + FLOAT_255 (int)
        GraphModelImpl readModelImpl = (GraphModelImpl) GraphModel.Serialization
                .readWithoutVersionHeader(dio, Serialization.VERSION);
        Assert.assertTrue(readModelImpl.deepEquals(readModelImpl));
    }

    @Test
    public void testGetConfiguration() {
        Configuration config = Configuration.builder().nodeIdType(Long.class).build();
        GraphModelImpl graphModelImpl = new GraphModelImpl(config);
        Assert.assertEquals(graphModelImpl.getConfiguration(), config);
    }

    @Test
    @SuppressWarnings("deprecated")
    public void testGetConfigurationCopy() {
        Configuration config = Configuration.builder().nodeIdType(Long.class).build();
        GraphModelImpl graphModelImpl = new GraphModelImpl(config);
        Assert.assertEquals(graphModelImpl.getConfiguration(), config);
        config.setNodeIdType(Float.class);
        Assert.assertEquals(graphModelImpl.getConfiguration().getNodeIdType(), Long.class);
    }

    @Test
    public void testSetConfigurationIntervals() {
        Configuration config = Configuration.builder().nodeIdType(Integer.class).edgeIdType(Byte.class)
                .timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModelImpl = new GraphModelImpl(config);
        Assert.assertEquals(graphModelImpl.getConfiguration(), config);
        Assert.assertEquals(graphModelImpl.getNodeTable().getColumn("id").getTypeClass(), Integer.class);
        Assert.assertEquals(graphModelImpl.getEdgeTable().getColumn("id").getTypeClass(), Byte.class);
        Assert.assertEquals(graphModelImpl.store.factory.nodeAssignConfiguration, GraphFactoryImpl.AssignConfiguration.INTEGER);
        Assert.assertEquals(graphModelImpl.store.factory.edgeAssignConfiguration, GraphFactoryImpl.AssignConfiguration.DISABLED);
        Assert.assertEquals(graphModelImpl.getNodeTable().getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID)
                .getTypeClass(), IntervalSet.class);
        Assert.assertEquals(graphModelImpl.getEdgeTable().getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID)
                .getTypeClass(), IntervalSet.class);
        Assert.assertEquals(graphModelImpl.store.timeStore.nodeIndexStore.getClass(), IntervalIndexStore.class);
        Assert.assertEquals(graphModelImpl.store.timeStore.edgeIndexStore.getClass(), IntervalIndexStore.class);

        Assert.assertEquals(graphModelImpl.getEdgeTable().getColumn(GraphStoreConfiguration.EDGE_WEIGHT_INDEX)
                .getTypeClass(), Double.class);
    }

    @Test
    public void testSetConfigurationTimestamps() {
        Configuration config = Configuration.builder().nodeIdType(Integer.class).edgeIdType(Byte.class)
                .timeRepresentation(TimeRepresentation.TIMESTAMP).build();
        GraphModelImpl graphModelImpl = new GraphModelImpl(config);
        Assert.assertEquals(graphModelImpl.getConfiguration(), config);
        Assert.assertEquals(graphModelImpl.getNodeTable().getColumn("id").getTypeClass(), Integer.class);
        Assert.assertEquals(graphModelImpl.getEdgeTable().getColumn("id").getTypeClass(), Byte.class);
        Assert.assertEquals(graphModelImpl.store.factory.nodeAssignConfiguration, GraphFactoryImpl.AssignConfiguration.INTEGER);
        Assert.assertEquals(graphModelImpl.store.factory.edgeAssignConfiguration, GraphFactoryImpl.AssignConfiguration.DISABLED);
        Assert.assertEquals(graphModelImpl.getNodeTable().getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID)
                .getTypeClass(), TimestampSet.class);
        Assert.assertEquals(graphModelImpl.getEdgeTable().getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID)
                .getTypeClass(), TimestampSet.class);
        Assert.assertEquals(graphModelImpl.store.timeStore.nodeIndexStore.getClass(), TimestampIndexStore.class);
        Assert.assertEquals(graphModelImpl.store.timeStore.edgeIndexStore.getClass(), TimestampIndexStore.class);

        Assert.assertEquals(graphModelImpl.getEdgeTable().getColumn(GraphStoreConfiguration.EDGE_WEIGHT_INDEX)
                .getTypeClass(), Double.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBadEdgeWeightTypeConfigurationIntervals() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(TimestampDoubleMap.class).build();
        new GraphModelImpl(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBadEdgeWeightTypeConfigurationTimestamps() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.TIMESTAMP)
                .edgeWeightType(IntervalDoubleMap.class).build();
        new GraphModelImpl(config);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testSetConfiguration() {
        GraphModelImpl graphModelImpl = new GraphModelImpl();
        graphModelImpl.setConfiguration(Configuration.builder().build());
    }

    @Test
    public void testSetConfigurationEdgeWeightColumnFalse() {
        Configuration config = Configuration.builder().edgeWeightColumn(false).build();
        GraphModelImpl graphModelImpl = new GraphModelImpl(config);

        Assert.assertFalse(graphModelImpl.store.edgeTable.hasColumn("weight"));
        Assert.assertNotEquals(graphModelImpl.store.edgeTable.addColumn("foo", Integer.class)
                .getIndex(), GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
    }

    @Test
    public void testSetConfigurationEdgeWeightColumnTrue() {
        Configuration config = Configuration.builder().edgeWeightColumn(true).build();
        GraphModelImpl graphModelImpl = new GraphModelImpl(config);

        Assert.assertTrue(graphModelImpl.store.edgeTable.hasColumn("weight"));
        Assert.assertEquals(graphModelImpl.store.edgeTable.getColumn("weight")
                .getIndex(), GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
    }

    @Test
    public void testNodeAttributesAddAndRemoveColumns1() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();
        Column col1 = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col1, "bar");
        graphModel.getStore().addNode(n1);

        Index index = graphModel.getNodeIndex();
        Assert.assertEquals(index.count(col1, "bar"), 1);
        table.removeColumn(col1);
        Column col2 = table.addColumn("foo2", String.class);

        Assert.assertNull(n1.getAttribute(col2));
        Assert.assertEquals(index.count(col2, "bar"), 0);
    }

    @Test
    public void testNodeAttributesAddAndRemoveColumns2() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();
        Column col1 = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col1, "bar");
        graphModel.getStore().addNode(n1);

        Index index = graphModel.getNodeIndex();
        Assert.assertEquals(index.count(col1, "bar"), 1);
        table.removeColumn(col1);
        Column col2 = table.addColumn("foo2", String.class);

        n1.setAttribute(col2, "test");
    }

    @Test
    public void testRemoveColumnWithNodes() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();

        Node n1 = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n1);

        Column col1 = table.addColumn("foo", String.class);
        table.removeColumn(col1);
        Assert.assertFalse(table.hasColumn("foo"));
    }

    @Test
    public void testNodeAttributesAddAndClearColumns() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();
        Column col1 = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col1, "bar");
        graphModel.getStore().addNode(n1);

        Column col2 = table.addColumn("foo2", String.class);

        Assert.assertNull(n1.getAttribute(col2));
    }

    @Test
    public void testDefaultColumns() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphModel.DefaultColumns defaultColumns = graphModel.defaultColumns();
        Assert.assertNotNull(defaultColumns);

        Assert.assertNotNull(defaultColumns.degree());
        Assert.assertNotNull(defaultColumns.inDegree());
        Assert.assertNotNull(defaultColumns.outDegree());
        Assert.assertNotNull(defaultColumns.nodeId());
        Assert.assertNotNull(defaultColumns.edgeId());
        Assert.assertNotNull(defaultColumns.nodeLabel());
        Assert.assertNotNull(defaultColumns.edgeLabel());
        Assert.assertNotNull(defaultColumns.nodeTimeSet());
        Assert.assertNotNull(defaultColumns.edgeTimeSet());
        Assert.assertNotNull(defaultColumns.edgeType());

    }
}
