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
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.UndirectedGraph;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ParallelEdgesTest {

    @DataProvider
    public static Object[][] stores() {
        List<Object[]> stores = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            stores.add(new Object[] { kind, true });
            stores.add(new Object[] { kind, false });
        }
        return stores.toArray(new Object[0][]);
    }

    // Every edge's count and index match getEdges(source, target) on every graph, under random changes of the store
    // and the views, with reads skipped at times
    @Test(dataProvider = "stores")
    public void testMatchesPairLookups(Kind kind, boolean observers) {
        Random random = new Random(kind.ordinal() * 2 + (observers ? 1 : 0));
        for (int run = 0; run < 12; run++) {
            Fixture fixture = new Fixture(kind, observers);
            // A single type at first, so that the store goes from one type to several
            int singleTypeSteps = random.nextInt(60);
            for (int step = 0; step < 100; step++) {
                fixture.randomChange(random, step < singleTypeSteps ? 1 : 3);
                fixture.assertMatchesPairLookups(random);
            }
            fixture.assertMatchesPairLookups(null);
        }
    }

    @DataProvider
    public static Object[][] viewMutations() {
        List<Object[]> mutations = new ArrayList<>();
        mutations.add(mutation("view addEdge", f -> {
            f.graph(f.nodeEdgeView).addEdge(f.edge(1));
            f.graph(f.edgeView).addEdge(f.edge(0));
        }));
        mutations.add(mutation("view addAllEdges", f -> {
            f.graph(f.nodeEdgeView).addAllEdges(Arrays.asList(f.edge(1), f.edge(3)));
            f.graph(f.edgeView).addAllEdges(f.storeEdges());
        }));
        mutations.add(mutation("view removeEdge", f -> {
            f.graph(f.nodeEdgeView).removeEdge(f.edge(0));
            f.graph(f.edgeView).removeEdge(f.edge(3));
        }));
        mutations.add(mutation("view removeAllEdges", f -> {
            f.graph(f.nodeEdgeView).removeAllEdges(Arrays.asList(f.edge(0), f.edge(8)));
            f.graph(f.edgeView).removeAllEdges(Arrays.asList(f.edge(1), f.edge(9)));
        }));
        mutations.add(mutation("view addNode", f -> f.graph(f.nodeView).addNode(f.node(3))));
        mutations.add(mutation("view addAllNodes", f -> f.graph(f.nodeView).addAllNodes(f.nodes)));
        mutations.add(mutation("view removeNode", f -> {
            f.graph(f.nodeEdgeView).removeNode(f.node(2));
            f.graph(f.nodeView).removeNode(f.node(0));
        }));
        mutations.add(mutation("view removeAllNodes", f -> {
            f.graph(f.nodeEdgeView).removeAllNodes(Arrays.asList(f.node(0), f.node(3)));
            f.graph(f.nodeView).removeAllNodes(Arrays.asList(f.node(1), f.node(2)));
        }));
        mutations.add(mutation("view retainNodes", f -> {
            f.graph(f.nodeEdgeView).retainNodes(Arrays.asList(f.node(1), f.node(2)));
            f.graph(f.nodeView).retainNodes(Arrays.asList(f.node(0), f.node(1)));
        }));
        mutations.add(mutation("view retainEdges", f -> {
            f.graph(f.nodeEdgeView).retainEdges(Arrays.asList(f.edge(0), f.edge(2)));
            f.graph(f.edgeView).retainEdges(Arrays.asList(f.edge(1), f.edge(7)));
        }));
        mutations.add(mutation("view clear", f -> f.views.forEach(view -> f.graph(view).clear())));
        mutations.add(mutation("view clearEdges", f -> f.views.forEach(view -> f.graph(view).clearEdges())));
        mutations.add(mutation("view clearEdges(node)", f -> {
            f.graph(f.nodeEdgeView).clearEdges(f.node(0));
            f.graph(f.edgeView).clearEdges(f.node(1));
        }));
        mutations.add(mutation("view clearEdges(node, type)", f -> {
            f.graph(f.nodeEdgeView).clearEdges(f.node(1), 0);
            f.graph(f.edgeView).clearEdges(f.node(0), 0);
        }));
        mutations.add(mutation("view fill", f -> f.views.forEach(view -> f.graph(view).fill())));
        mutations.add(mutation("view not", f -> f.views.forEach(view -> f.graph(view).not())));
        mutations.add(mutation("view union", f -> {
            f.graph(f.nodeEdgeView).union(f.graph(f.edgeView));
            f.graph(f.nodeView).union(f.graph(f.nodeEdgeView));
        }));
        mutations.add(mutation("view intersection", f -> {
            f.graph(f.edgeView).intersection(f.graph(f.nodeView));
            f.graph(f.nodeEdgeView).intersection(f.graph(f.edgeView));
        }));
        mutations.add(mutation("store add in a node view", f -> f.addEdge(f.node(0), f.node(1), 1, true)));
        mutations.add(mutation("store add parallel to an edge alone in a view", f -> {
            // The edge from node 3 to node 0 is in the node and edge view, and alone in the store
            f.addEdge(f.node(3), f.node(0), 0, true);
        }));
        mutations.add(mutation("store remove in the views", f -> f.main.removeEdge(f.edge(0))));
        mutations.add(mutation("store remove outside a view", f -> f.main.removeEdge(f.edge(1))));
        mutations.add(mutation("store removeNode", f -> f.main.removeNode(f.node(1))));
        mutations.add(mutation("store setType in the views", f -> f.edge(0).setType(3)));
        mutations.add(mutation("store setType outside a view", f -> f.edge(1).setType(1)));
        mutations.add(mutation("store clear", f -> {
            f.main.clear();
            f.assertMatchesPairLookups(null);
            f.main.addAllNodes(f.nodes);
            f.populate();
        }));
        mutations.add(mutation("store clearEdges", f -> {
            f.main.clearEdges();
            f.assertMatchesPairLookups(null);
            f.populate();
        }));
        mutations.add(mutation("store id reuse", f -> {
            f.main.removeEdge(f.edge(0));
            f.addEdge(f.node(0), f.node(1), 0, true);
        }));
        mutations.add(mutation("store type change", f -> {
            // A directed edge of a type the store doesn't have, so that an undirected store goes mixed and back
            Edge edge = f.model.factory().newEdge(f.node(0), f.node(1), 3, true);
            f.main.addEdge(edge);
            f.edges.add(edge);
            f.assertMatchesPairLookups(null);
            f.main.removeEdge(edge);
        }));
        mutations.add(mutation("destroyed view", f -> {
            f.model.destroyView(f.nodeEdgeView);
            f.main.removeEdge(f.edge(2));
        }));

        List<Object[]> rows = new ArrayList<>();
        for (Object[] mutation : mutations) {
            for (Kind kind : Kind.values()) {
                rows.add(new Object[] { mutation[0], kind, mutation[1] });
            }
        }
        return rows.toArray(new Object[0][]);
    }

    // The values match getEdges(source, target) after the mutation, once read before it
    @Test(dataProvider = "viewMutations")
    public void testViewMutation(String name, Kind kind, Consumer<Fixture> mutation) {
        Fixture fixture = new Fixture(kind, true);
        fixture.populate();
        fixture.assertMatchesPairLookups(null);
        mutation.accept(fixture);
        fixture.assertMatchesPairLookups(null);
    }

    @DataProvider
    public static Object[][] queuedWriterGraphs() {
        return new Object[][] { { "stale directed view" }, { "undirected view" }, { "undirected graph" } };
    }

    // A read on behalf of a thread holding the read lock completes while a writer is queued
    @Test(dataProvider = "queuedWriterGraphs")
    public void testQueuedWriter(String name) throws Exception {
        GraphModel model = GraphModel.Factory.newInstance();
        Graph main = model.getDirectedGraph();
        Node a = model.factory().newNode("a");
        Node b = model.factory().newNode("b");
        main.addAllNodes(Arrays.asList(a, b));
        Edge e1 = model.factory().newEdge(a, b, 0, true);
        Edge e2 = model.factory().newEdge(a, b, 1, true);
        main.addAllEdges(Arrays.asList(e1, e2));
        GraphView view = model.createView();
        Subgraph viewGraph = model.getDirectedGraph(view);
        viewGraph.addAllNodes(Arrays.asList(a, b));
        viewGraph.addEdge(e1);
        Graph graph;
        if (name.equals("stale directed view")) {
            graph = viewGraph;
            graph.getParallelEdgeCount(e1);
            viewGraph.addEdge(e2);
        } else if (name.equals("undirected view")) {
            graph = model.getUndirectedGraph(view);
            viewGraph.addEdge(e2);
        } else {
            graph = model.getUndirectedGraph();
        }

        Thread writer = new Thread(() -> {
            graph.writeLock();
            graph.writeUnlock();
        });
        ExecutorService executor = Executors.newSingleThreadExecutor();
        graph.readLock();
        try {
            writer.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (graph.getLock().getQueueLength() == 0) {
                Assert.assertTrue(System.nanoTime() < deadline, "The writer isn't queued");
                Thread.sleep(1);
            }
            Future<Integer> count = executor.submit(() -> graph.getParallelEdgeCount(e1));
            Assert.assertEquals((int) count.get(10, TimeUnit.SECONDS), 2);
        } finally {
            graph.readUnlock();
            writer.join();
            executor.shutdownNow();
        }
    }

    @Test
    public void testMixedGroups() {
        GraphModel model = GraphModel.Factory.newInstance();
        Graph graph = model.getDirectedGraph();
        Node a = model.factory().newNode("a");
        Node b = model.factory().newNode("b");
        graph.addAllNodes(Arrays.asList(a, b));
        Edge d0 = model.factory().newEdge(a, b, 0, true);
        Edge u1 = model.factory().newEdge(b, a, 1, false);
        Edge r2 = model.factory().newEdge(b, a, 2, true);
        graph.addAllEdges(Arrays.asList(d0, u1, r2));

        // The undirected edge is in both groups, and its values come from its source and target
        Assert.assertEquals(Arrays.asList(graph.getEdges(a, b).toArray()), Arrays.asList(d0, u1));
        Assert.assertEquals(Arrays.asList(graph.getEdges(b, a).toArray()), Arrays.asList(u1, r2));
        assertParallelEdge(graph, d0, 2, 0);
        assertParallelEdge(graph, u1, 2, 0);
        assertParallelEdge(graph, r2, 2, 1);
    }

    @Test
    public void testMixedGetEdge() {
        GraphModel model = GraphModel.Factory.newInstance();
        Graph graph = model.getDirectedGraph();
        Node a = model.factory().newNode("a");
        Node b = model.factory().newNode("b");
        graph.addAllNodes(Arrays.asList(a, b));
        Edge r0 = model.factory().newEdge(b, a, 0, true);
        Edge u1 = model.factory().newEdge(b, a, 1, false);
        graph.addAllEdges(Arrays.asList(r0, u1));

        // The first edge from a to b comes after the edge from b to a in the undirected edge's group
        Assert.assertEquals(Arrays.asList(graph.getEdges(a, b).toArray()), Arrays.asList(u1));
        Assert.assertSame(graph.getEdge(a, b), u1);
        assertParallelEdge(graph, u1, 2, 1);
        assertParallelEdge(graph, r0, 2, 0);
    }

    @Test
    public void testSelfLoops() {
        GraphModel model = GraphModel.Factory.newInstance();
        Graph graph = model.getDirectedGraph();
        Node a = model.factory().newNode("a");
        graph.addNode(a);
        Edge l0 = model.factory().newEdge(a, a, 0, true);
        Edge l1 = model.factory().newEdge(a, a, 1, false);
        Edge l2 = model.factory().newEdge(a, a, 2, true);
        Edge l3 = model.factory().newEdge(a, a, 0, true);
        graph.addAllEdges(Arrays.asList(l0, l1, l2, l3));

        // Every loop on the node, of any type and directedness
        for (Graph g : Arrays.asList(graph, model.getUndirectedGraph())) {
            Assert.assertEquals(Arrays.asList(g.getEdges(a, a).toArray()), Arrays.asList(l0, l3, l1, l2));
            assertParallelEdge(g, l0, 4, 0);
            assertParallelEdge(g, l3, 4, 1);
            assertParallelEdge(g, l1, 4, 2);
            assertParallelEdge(g, l2, 4, 3);
        }
    }

    @Test
    public void testUndirectedGraphOfDirectedStore() {
        GraphModel model = GraphModel.Factory.newInstance();
        Graph main = model.getDirectedGraph();
        Node a = model.factory().newNode("a");
        Node b = model.factory().newNode("b");
        main.addAllNodes(Arrays.asList(a, b));
        Edge ab0 = model.factory().newEdge(a, b, 0, true);
        Edge ab = model.factory().newEdge(a, b, 1, true);
        Edge ba = model.factory().newEdge(b, a, 1, true);
        Edge ab2 = model.factory().newEdge(a, b, 1, true);
        main.addAllEdges(Arrays.asList(ab0, ab, ba, ab2));
        Assert.assertSame(model.getDirectedGraph().getMutualEdge(ab), ba);
        GraphView view = model.createView();
        model.getGraph(view).fill();

        for (UndirectedGraph graph : Arrays.asList(model.getUndirectedGraph(), model.getUndirectedGraph(view))) {
            // The positions are those in getEdges(a, b), a having the lower store id, whatever the argument order
            Assert.assertEquals(Arrays.asList(graph.getEdges(a, b).toArray()), Arrays.asList(ab0, ab2, ba));
            Assert.assertEquals(Arrays.asList(graph.getEdges(b, a).toArray()), Arrays.asList(ab0, ba, ab2));
            assertParallelEdge(graph, ab0, 3, 0);
            assertParallelEdge(graph, ab2, 3, 1);
            assertParallelEdge(graph, ba, 3, 2);
            // The hidden half has its mutual edge's values
            assertParallelEdge(graph, ab, 3, 2);
        }

        // A view without the mutual edge shows the other half
        model.getGraph(view).removeEdge(ba);
        UndirectedGraph graph = model.getUndirectedGraph(view);
        Assert.assertEquals(Arrays.asList(graph.getEdges(a, b).toArray()), Arrays.asList(ab0, ab, ab2));
        assertParallelEdge(graph, ab, 3, 1);
        assertParallelEdge(graph, ab2, 3, 2);
        assertParallelEdge(graph, ba, 1, 0);
    }

    @Test
    public void testSetTypeMovesEdgeLast() {
        GraphModel model = GraphModel.Factory.newInstance();
        Graph graph = model.getDirectedGraph();
        Node a = model.factory().newNode("a");
        Node b = model.factory().newNode("b");
        graph.addAllNodes(Arrays.asList(a, b));
        Edge e0 = model.factory().newEdge(a, b, 0, true);
        Edge e1 = model.factory().newEdge(a, b, 0, true);
        Edge e2 = model.factory().newEdge(a, b, 1, true);
        graph.addAllEdges(Arrays.asList(e0, e1, e2));

        e0.setType(1);
        assertParallelEdge(graph, e1, 3, 0);
        assertParallelEdge(graph, e2, 3, 1);
        assertParallelEdge(graph, e0, 3, 2);

        e0.setType(0);
        assertParallelEdge(graph, e1, 3, 0);
        assertParallelEdge(graph, e0, 3, 1);
        assertParallelEdge(graph, e2, 3, 2);
    }

    @Test
    public void testEdgesNotInGraph() {
        GraphModel model = GraphModel.Factory.newInstance();
        Graph main = model.getDirectedGraph();
        Node a = model.factory().newNode("a");
        Node b = model.factory().newNode("b");
        main.addAllNodes(Arrays.asList(a, b));
        Edge e1 = model.factory().newEdge(a, b, 0, true);
        Edge e2 = model.factory().newEdge(a, b, 0, true);
        main.addAllEdges(Arrays.asList(e1, e2));
        GraphView view = model.createView();
        model.getGraph(view).addAllNodes(Arrays.asList(a, b));
        model.getGraph(view).addEdge(e1);
        Edge notAdded = model.factory().newEdge(a, b, 0, true);
        List<Graph> graphs = Arrays
                .asList(main, model.getUndirectedGraph(), model.getDirectedGraph(view), model.getUndirectedGraph(view));

        assertParallelEdge(main, e2, 2, 1);
        assertParallelEdge(model.getGraph(view), e1, 1, 0);
        assertParallelEdge(model.getGraph(view), e2, 1, 0);
        main.removeEdge(e2);
        for (Graph graph : graphs) {
            assertParallelEdge(graph, e2, 1, 0);
            assertParallelEdge(graph, notAdded, 1, 0);
            Assert.assertThrows(NullPointerException.class, () -> graph.getParallelEdgeCount(null));
            Assert.assertThrows(NullPointerException.class, () -> graph.getParallelEdgeIndex(null));
        }
    }

    @DataProvider
    public static Object[][] kinds() {
        return new Object[][] { { Kind.DIRECTED }, { Kind.UNDIRECTED }, { Kind.MIXED } };
    }

    // The values match getEdges(source, target) once loaded
    @Test(dataProvider = "kinds")
    public void testSerialization(Kind kind) throws IOException {
        Random random = new Random(kind.ordinal());
        Fixture fixture = new Fixture(kind, true);
        for (int step = 0; step < 100; step++) {
            fixture.randomChange(random, 3);
        }
        Fixture.loaded(saveAndLoad(fixture.model)).assertMatchesPairLookups(null);
    }

    // The values match getEdges(source, target) after edges are added to a store loaded from one with free ids
    @Test
    public void testSerializationWithFreeIds() throws IOException {
        Fixture fixture = new Fixture(Kind.DIRECTED, true);
        fixture.populate();
        // The views hold the last edges, and the first edges are removed
        for (GraphView view : Arrays.asList(fixture.nodeEdgeView, fixture.edgeView)) {
            Subgraph graph = fixture.graph(view);
            graph.clearEdges();
            graph.addAllEdges(fixture.storeEdges().subList(6, 13));
        }
        for (int i = 0; i < 6; i++) {
            fixture.main.removeEdge(fixture.edge(i));
        }

        Fixture loaded = Fixture.loaded(saveAndLoad(fixture.model));
        loaded.assertMatchesPairLookups(null);
        // The new edges get the store ids the saved edges had
        for (int i = 0; i < 6; i++) {
            loaded.addEdge(loaded.node(0), loaded.node(1), 0, true);
        }
        loaded.assertMatchesPairLookups(null);
    }

    private static Object[] mutation(String name, Consumer<Fixture> mutation) {
        return new Object[] { name, mutation };
    }

    private static void assertParallelEdge(Graph graph, Edge edge, int count, int index) {
        Assert.assertEquals(graph.getParallelEdgeCount(edge), count);
        Assert.assertEquals(graph.getParallelEdgeIndex(edge), index);
    }

    private static GraphModel saveAndLoad(GraphModel model) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        GraphModel.Serialization.write(new DataOutputStream(bytes), model);
        return GraphModel.Serialization.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
    }

    enum Kind {
        DIRECTED, UNDIRECTED, MIXED
    }

    // A graph checked against the pair lookups, and the directed graph of the same view
    private static final class CheckedGraph {

        private final String name;
        private final Graph graph;
        private final boolean undirected;
        private final DirectedGraph directed;

        private CheckedGraph(String name, Graph graph, boolean undirected, DirectedGraph directed) {
            this.name = name;
            this.graph = graph;
            this.undirected = undirected;
            this.directed = directed;
        }

        // The edge's count and index are the size of getEdges(source, target) and its position there, or of
        // getEdges(low, high) on undirected graphs of directed or mixed stores, where a hidden half takes its mutual
        // edge's values. Edges the graph doesn't contain get 1 and 0
        private void assertMatchesPairLookups(GraphModel model, Edge edge) {
            int count = 1;
            int index = 0;
            List<Edge> group = null;
            if (model.getGraph().contains(edge) && graph.contains(edge)) {
                Edge position = edge;
                if (undirected && !model.isUndirected()) {
                    boolean sourceFirst = edge.getSource().getStoreId() < edge.getTarget().getStoreId();
                    Node low = sourceFirst ? edge.getSource() : edge.getTarget();
                    Node high = sourceFirst ? edge.getTarget() : edge.getSource();
                    group = Arrays.asList(graph.getEdges(low, high).toArray());
                    if (!group.contains(edge)) {
                        position = directed.getMutualEdge(edge);
                    }
                } else {
                    group = Arrays.asList(graph.getEdges(edge.getSource(), edge.getTarget()).toArray());
                }
                count = group.size();
                index = group.indexOf(position);
                if (index < 0) {
                    Assert.fail(name + ", " + describe(edge) + " isn't in " + describe(group));
                }
            }
            int actualCount = graph.getParallelEdgeCount(edge);
            int actualIndex = graph.getParallelEdgeIndex(edge);
            if (actualCount != count || actualIndex != index) {
                Assert.fail(name + ", " + describe(edge) + ": (" + actualCount + ", " + actualIndex + ") instead of (" + count + ", " + index + ") in " + describe(group));
            }
        }

        private static String describe(List<Edge> edges) {
            if (edges == null) {
                return "none";
            }
            List<String> descriptions = new ArrayList<>();
            edges.forEach(edge -> descriptions.add(describe(edge)));
            return descriptions.toString();
        }

        private static String describe(Edge edge) {
            return edge.getId() + " " + edge.getSource().getId() + (edge.isDirected() ? "->" : "--") + edge.getTarget()
                    .getId() + " type " + edge.getType();
        }
    }

    // A store with a node and edge view, an edge view and a node view, whose graphs are checked
    private static final class Fixture {

        private final Kind kind;
        private final GraphModel model;
        private final Graph main;
        private final List<Node> nodes = new ArrayList<>();
        // Every edge created, the removed ones included
        private final List<Edge> edges = new ArrayList<>();
        private final List<GraphView> views = new ArrayList<>();
        private final GraphView nodeEdgeView;
        private final GraphView edgeView;
        private final GraphView nodeView;
        private final List<CheckedGraph> graphs = new ArrayList<>();

        private Fixture(Kind kind, boolean observers) {
            this.kind = kind;
            model = GraphModel.Factory.newInstance(Configuration.builder().enableObservers(observers).build());
            main = model.getDirectedGraph();
            for (int i = 0; i < 4; i++) {
                nodes.add(model.factory().newNode(String.valueOf(i)));
            }
            main.addAllNodes(nodes);
            nodeEdgeView = model.createView();
            edgeView = model.createView(false, true);
            nodeView = model.createView(true, false);
            views.addAll(Arrays.asList(nodeEdgeView, edgeView, nodeView));
            addGraphs();
        }

        private Fixture(GraphModel model, List<GraphView> views) {
            this.kind = null;
            this.model = model;
            main = model.getDirectedGraph();
            main.getNodes().forEach(nodes::add);
            main.getEdges().forEach(edges::add);
            this.views.addAll(views);
            nodeEdgeView = null;
            edgeView = null;
            nodeView = null;
            addGraphs();
        }

        private static Fixture loaded(GraphModel model) {
            List<GraphView> views = new ArrayList<>();
            for (GraphViewImpl view : ((GraphModelImpl) model).store.viewStore.views) {
                if (view != null) {
                    views.add(view);
                }
            }
            return new Fixture(model, views);
        }

        private void addGraphs() {
            graphs.add(new CheckedGraph("main", main, false, null));
            graphs.add(new CheckedGraph("main undirected", model.getUndirectedGraph(), true, model.getDirectedGraph()));
            for (GraphView view : views) {
                String name = view.isNodeView() ? view.isEdgeView() ? "node and edge view" : "node view" : "edge view";
                // A view's directed graph can't be had from an undirected store
                DirectedGraph directed = model.isUndirected() ? null : model.getDirectedGraph(view);
                if (directed != null) {
                    graphs.add(new CheckedGraph(name, directed, false, null));
                }
                graphs.add(new CheckedGraph(name + " undirected", model.getUndirectedGraph(view), true, directed));
            }
        }

        // Checks each graph, or a random selection of them
        private void assertMatchesPairLookups(Random random) {
            for (CheckedGraph graph : graphs) {
                if (random == null || random.nextInt(3) > 0) {
                    for (Edge edge : edges) {
                        graph.assertMatchesPairLookups(model, edge);
                    }
                }
            }
        }

        // Edges of several types between the same nodes in both directions, mutual edges and self-loops, some of them
        // undirected on mixed stores
        private void populate() {
            Node a = node(0);
            Node b = node(1);
            Node c = node(2);
            Node d = node(3);
            int first = edges.size();
            addEdge(a, b, 0, true);
            addEdge(b, a, 0, true);
            addEdge(a, b, 0, true);
            addEdge(a, b, 1, true);
            addEdge(b, a, 2, false);
            addEdge(c, c, 0, true);
            addEdge(c, c, 1, false);
            addEdge(b, c, 0, true);
            addEdge(b, c, 0, true);
            addEdge(c, b, 1, true);
            addEdge(c, d, 0, false);
            addEdge(d, c, 0, false);
            addEdge(d, a, 1, true);
            // The edge view holds the edges the node and edge view doesn't, and the node view the first three nodes
            Subgraph nodeEdgeGraph = graph(nodeEdgeView);
            nodeEdgeGraph.addAllNodes(nodes);
            for (int i = first; i < edges.size(); i++) {
                graph(i % 2 == 0 ? nodeEdgeView : edgeView).addEdge(edges.get(i));
            }
            graph(nodeView).addAllNodes(Arrays.asList(a, b, c));
        }

        // Adds an edge, undirected on undirected stores and directed on directed stores
        private Edge addEdge(Node source, Node target, int type, boolean directed) {
            Edge edge = model.factory().newEdge(source, target, type, kind != null
                    ? kind == Kind.DIRECTED || (kind == Kind.MIXED && directed) : directed);
            main.addEdge(edge);
            edges.add(edge);
            return edge;
        }

        private void randomChange(Random random, int typeCount) {
            Edge[] stored = main.getEdges().toArray();
            int change = stored.length == 0 ? 0 : random.nextInt(16);
            Subgraph graph = graph(views.get(random.nextInt(views.size())));
            Node node = nodes.get(random.nextInt(nodes.size()));
            Edge edge = stored.length == 0 ? null : stored[random.nextInt(stored.length)];
            try {
                if (change < 4) {
                    // Adds are more likely, so that parallel edges pile up
                    Node target = nodes.get(random.nextInt(nodes.size()));
                    addEdge(node, target, random.nextInt(typeCount), random.nextBoolean());
                } else if (change == 4) {
                    main.removeEdge(edge);
                } else if (change == 5) {
                    // Adds a removed edge again
                    Edge removed = edges.get(random.nextInt(edges.size()));
                    main.addEdge(removed);
                } else if (change == 6) {
                    edge.setType(random.nextInt(typeCount));
                } else if (change == 7 && graph.getView().isEdgeView()) {
                    if (!graph.removeEdge(edge)) {
                        if (graph.getView().isNodeView()) {
                            graph.addNode(edge.getSource());
                            graph.addNode(edge.getTarget());
                        }
                        graph.addEdge(edge);
                    }
                } else if (change == 8 && graph.getView().isNodeView()) {
                    if (!graph.removeNode(node)) {
                        graph.addNode(node);
                    }
                } else if (change == 9) {
                    graph.not();
                } else if (change == 10) {
                    Subgraph other = graph(views.get(random.nextInt(views.size())));
                    if (random.nextBoolean()) {
                        graph.union(other);
                    } else {
                        graph.intersection(other);
                    }
                } else if (change == 11) {
                    graph.fill();
                } else if (change == 12) {
                    graph.clearEdges();
                } else if (change == 13 && graph.getView().isEdgeView()) {
                    graph.clearEdges(node);
                } else if (change == 14) {
                    if (random.nextInt(4) == 0) {
                        main.clearEdges();
                    } else {
                        main.clearEdges(node);
                    }
                } else if (change == 15) {
                    main.removeNode(node);
                    main.addNode(node);
                }
            } catch (IllegalArgumentException e) {
                // A type doesn't hold both directed and undirected edges between two nodes
            }
        }

        private Subgraph graph(GraphView view) {
            return model.getGraph(view);
        }

        private Node node(int i) {
            return nodes.get(i);
        }

        private Edge edge(int i) {
            return edges.get(i);
        }

        private List<Edge> storeEdges() {
            return Arrays.asList(main.getEdges().toArray());
        }
    }
}
