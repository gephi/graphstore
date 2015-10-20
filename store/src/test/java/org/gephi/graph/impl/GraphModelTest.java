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
import java.util.Arrays;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.utils.DataInputOutput;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.gephi.graph.api.TimeIndex;
import org.joda.time.DateTimeZone;

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
        Assert.assertEquals(graphModel.getEdgeLabel(typeId), "foo");
        Assert.assertEquals(graphModel.getEdgeType("foo"), typeId);
    }

    @Test
    public void testIsMultiGraph() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertFalse(graphModel.isMultiGraph());
        graphModel.addEdgeType("foo");
        Assert.assertTrue(graphModel.isMultiGraph());
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
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[]{n1, n2}));
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
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[]{n1, n2, n3}));
        Edge e1 = graphModel.factory().newEdge(n1, n2, false);
        Edge e2 = graphModel.factory().newEdge(n1, n3, true);
        graphModel.getStore().addAllEdges(Arrays.asList(new Edge[]{e1, e2}));
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
        Assert.assertEquals(graphModel.getTimeZone(), DateTimeZone.UTC);//Default
        graphModel.setTimeZone(DateTimeZone.forID("-02:00"));
        Assert.assertEquals(graphModel.getTimeZone(), DateTimeZone.forID("-02:00"));
        graphModel.setTimeZone(DateTimeZone.UTC);
        Assert.assertEquals(graphModel.getTimeZone(), DateTimeZone.UTC);
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
    public void testGetTimeBoundsInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Assert.assertEquals(graphModel.getTimeBounds(view), Interval.INFINITY_INTERVAL);

        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[]{n1, n2}));

        n1.addTimestamp(1.0);
        n1.addTimestamp(5.0);

        n2.addTimestamp(2.0);
        n2.addTimestamp(3.0);

        graphModel.getGraph(view).addNode(n2);

        Assert.assertEquals(graphModel.getTimeBounds(view), new Interval(2.0, 3.0));
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
    public void testGetConfiguration() {
        Configuration config = new Configuration();
        config.setNodeIdType(Long.class);
        GraphModelImpl graphModelImpl = new GraphModelImpl(config);
        Assert.assertEquals(graphModelImpl.getConfiguration(), config);
    }

    @Test
    public void testGetConfigurationCopy() {
        Configuration config = new Configuration();
        config.setNodeIdType(Long.class);
        GraphModelImpl graphModelImpl = new GraphModelImpl(config);
        Assert.assertEquals(graphModelImpl.getConfiguration(), config);
        config.setNodeIdType(Float.class);
        Assert.assertEquals(graphModelImpl.getConfiguration().getNodeIdType(), Long.class);
    }

    @Test
    public void testSetConfiguration() {
        Configuration config = new Configuration();
        GraphModelImpl graphModelImpl = new GraphModelImpl(config);
        config.setNodeIdType(Integer.class);
        config.setEdgeIdType(Byte.class);
        graphModelImpl.setConfiguration(config);
        Assert.assertEquals(graphModelImpl.getConfiguration(), config);
        Assert.assertEquals(graphModelImpl.getNodeTable().getColumn("id").getTypeClass(), Integer.class);
        Assert.assertEquals(graphModelImpl.getEdgeTable().getColumn("id").getTypeClass(), Byte.class);
        Assert.assertEquals(graphModelImpl.store.factory.nodeAssignConfiguration, GraphFactoryImpl.AssignConfiguration.INTEGER);
        Assert.assertEquals(graphModelImpl.store.factory.edgeAssignConfiguration, GraphFactoryImpl.AssignConfiguration.DISABLED);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testSetConfigurationWithNodes() {
        GraphModelImpl graphModelImpl = new GraphModelImpl();
        graphModelImpl.store.addNode(graphModelImpl.factory().newNode());
        graphModelImpl.setConfiguration(new Configuration());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testSetConfigurationWithGraphAttributes() {
        GraphModelImpl graphModelImpl = new GraphModelImpl();
        graphModelImpl.getGraph().setAttribute("foo", "bar");
        graphModelImpl.setConfiguration(new Configuration());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testSetConfigurationWithNodeColumns() {
        GraphModelImpl graphModelImpl = new GraphModelImpl();
        graphModelImpl.store.nodeTable.addColumn("foo", Integer.class);
        graphModelImpl.setConfiguration(new Configuration());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testSetConfigurationWithEdgeColumns() {
        GraphModelImpl graphModelImpl = new GraphModelImpl();
        graphModelImpl.store.edgeTable.addColumn("foo", Integer.class);
        graphModelImpl.setConfiguration(new Configuration());
    }
}
