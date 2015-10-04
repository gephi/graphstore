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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphBridgeTest {

    @Test(expectedExceptions = RuntimeException.class)
    public void testVerifyConfiguration() {
        Configuration destConfig = new Configuration();
        destConfig.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl dest = new GraphModelImpl(destConfig);

        new GraphBridgeImpl(dest.store).copyNodes(GraphGenerator.generateTinyGraphStore().getNodes().toArray());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testNodeColumnExistsDifferentType() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        source.nodeTable.addColumn("foo", String.class);

        GraphStore dest = new GraphStore();
        dest.nodeTable.addColumn("foo", Integer.class);

        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testEdgeColumnExistsDifferentType() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        source.edgeTable.addColumn("foo", String.class);

        GraphStore dest = new GraphStore();
        dest.edgeTable.addColumn("foo", Integer.class);

        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testEdgeExists() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        GraphStore dest = new GraphStore();
        Node n1 = dest.factory.newNode("foo");
        Node n2 = dest.factory.newNode("bar");
        dest.addAllNodes(Arrays.asList(new Node[]{n1, n2}));
        Edge e0 = dest.factory.newEdge("0", n1, n2, 0, 1.0, true);
        dest.addEdge(e0);

        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testElementOtherStore() {
        GraphStore store = new GraphStore();
        Node[] nodes = new Node[]{store.factory.newNode("foo")};

        new GraphBridgeImpl(new GraphStore()).copyNodes(nodes);
    }

    @Test
    public void testCopyColumns() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        source.edgeTable.addColumn("foo", String.class);
        source.nodeTable.addColumn("bar", Integer.class);
        source.nodeTable.addColumn("exists", Float.class);

        GraphStore dest = new GraphStore();
        dest.nodeTable.addColumn("exists", Float.class);

        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());
        Assert.assertNotNull(dest.nodeTable.getColumn("exists"));
        Assert.assertNotNull(dest.edgeTable.getColumn("foo"));
        Assert.assertNotNull(dest.nodeTable.getColumn("bar"));
    }

    @Test
    public void testCopyNodeProperties() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = source.getNode("1");
        n1.setColor(Color.RED);
        n1.setAlpha(0.5f);
        n1.setSize(42f);
        n1.setLabel("foo");
        n1.setPosition(2f, 3f, 4f);
        n1.setFixed(true);

        GraphStore dest = new GraphStore();
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());

        NodeImpl nodeCopy = dest.getNode("1");
        Assert.assertTrue(nodeCopy.properties.deepEquals(n1.properties));
    }

    @Test
    public void testCopyEdgeProperties() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        EdgeImpl e0 = source.getEdge("0");
        e0.setColor(Color.RED);
        e0.setAlpha(0.5f);
        e0.setLabel("foo");

        GraphStore dest = new GraphStore();
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());

        EdgeImpl edgeCopy = dest.getEdge("0");
        Assert.assertTrue(edgeCopy.properties.deepEquals(e0.properties));
    }

    @Test
    public void testCopyNodeTextProperties() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = source.getNode("1");
        n1.getTextProperties().setColor(Color.RED);
        n1.getTextProperties().setAlpha(0.5f);
        n1.getTextProperties().setSize(5f);
        n1.getTextProperties().setVisible(false);

        GraphStore dest = new GraphStore();
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());

        NodeImpl nodeCopy = dest.getNode("1");
        Assert.assertTrue(nodeCopy.getTextProperties().deepEquals(n1.getTextProperties()));
    }

    @Test
    public void testCopyEdgeTextProperties() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        EdgeImpl e0 = source.getEdge("0");
        e0.getTextProperties().setColor(Color.RED);
        e0.getTextProperties().setAlpha(0.5f);
        e0.getTextProperties().setSize(5f);
        e0.getTextProperties().setVisible(false);

        GraphStore dest = new GraphStore();
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());

        EdgeImpl edgeCopy = dest.getEdge("0");
        Assert.assertTrue(edgeCopy.getTextProperties().deepEquals(e0.getTextProperties()));
    }

    @Test
    public void testCopyEdgeWeightStatic() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        EdgeImpl e0 = source.getEdge("0");
        e0.setWeight(42.0);

        GraphStore dest = new GraphStore();
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());

        EdgeImpl edgeCopy = dest.getEdge("0");
        Assert.assertEquals(edgeCopy.getWeight(), 42.0);
    }

    @Test
    public void testCopyEdgeWeightTimestamp() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        EdgeImpl e0 = source.getEdge("0");
        e0.setWeight(42.0, 1.0);
        e0.setWeight(5.0, 2.0);

        GraphStore dest = new GraphStore();
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());

        EdgeImpl edgeCopy = dest.getEdge("0");
        Assert.assertEquals(edgeCopy.getWeight(1.0), 42.0);
        Assert.assertEquals(edgeCopy.getWeight(2.0), 5.0);
    }

    @Test
    public void testCopyEdgeWeightInterval() {
        GraphStore source = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        EdgeImpl e0 = source.getEdge("0");
        e0.setWeight(42.0, new Interval(1.0, 2.0));
        e0.setWeight(5.0, new Interval(3.0, 4.0));

        Configuration config = new Configuration();
        config.setTimeRepresentation(TimeRepresentation.INTERVAL);
        GraphModelImpl gm = new GraphModelImpl(config);
        GraphStore dest = gm.store;
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());

        EdgeImpl edgeCopy = dest.getEdge("0");
        Assert.assertEquals(edgeCopy.getWeight(new Interval(1.0, 2.0)), 42.0);
        Assert.assertEquals(edgeCopy.getWeight(new Interval(3.0, 4.0)), 5.0);
    }

    @Test
    public void testCopyPartial() {
        GraphStore source = GraphGenerator.generateSmallMultiTypeGraphStore();
        Node[] nodes = Arrays.copyOf(source.getNodes().toArray(), 10);

        GraphStore dest = new GraphStore();
        for (Node n : nodes) {
            Node nodeCopy = dest.factory.newNode(n.getId());
            dest.addNode(nodeCopy);
        }
        Set<Integer> typeIds = new HashSet<Integer>();
        for (Edge edge : source.getEdges()) {
            if (dest.getNode(edge.getSource().getId()) != null && dest.getNode(edge.getTarget().getId()) != null) {
                Edge edgeCopy = dest.factory.newEdge(edge.getId(),
                        dest.getNode(edge.getSource().getId()),
                        dest.getNode(edge.getTarget().getId()),
                        edge.getType(), edge.getWeight(), edge.isDirected());
                dest.addEdge(edgeCopy);
                typeIds.add(edge.getType());
                if (dest.getEdgeCount() >= 15) {
                    break;
                }
            }
        }
        for (Integer typeId : typeIds) {
            source.edgeTypeStore.addType(String.valueOf(typeId), typeId);
        }

        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());
        for (Node n : source.getNodes()) {
            Assert.assertNotNull(dest.getNode(n.getId()));
        }
        for (Edge e : source.getEdges()) {
            Assert.assertNotNull(dest.getEdge(e.getId()));
        }
    }

    @Test
    public void testCopyEdgeLabels() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        Edge e0 = source.getEdge("0");
        int type = source.edgeTypeStore.addType("foo");
        Edge e1 = source.factory.newEdge("1", e0.getSource(), e0.getTarget(), type, 1.0, true);
        source.addEdge(e1);

        GraphStore dest = new GraphStore();
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());

        Assert.assertEquals(dest.edgeTypeStore.getLabel(type), "foo");
    }

    @Test
    public void testCopyNodeAttributes() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        Column c1 = source.nodeTable.addColumn("foo", String.class);
        Column c2 = source.nodeTable.addColumn("bar", TimestampIntegerMap.class);
        Node n1 = source.getNode("1");
        n1.setAttribute(c1, "test");
        n1.setAttribute(c2, 10, 1.0);
        n1.setAttribute(c2, 20, 2.0);

        GraphStore dest = new GraphStore();
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());
        Column c1Copy = dest.nodeTable.getColumn("foo");
        Column c2Copy = dest.nodeTable.getColumn("bar");
        Node n1Copy = dest.getNode("1");
        Assert.assertEquals(n1Copy.getAttribute(c1Copy), "test");
        Assert.assertEquals(n1Copy.getAttribute(c2Copy, 1.0), 10);
        Assert.assertEquals(n1Copy.getAttribute(c2Copy, 2.0), 20);
    }

    @Test
    public void testCopyEdgeAttributes() {
        GraphStore source = GraphGenerator.generateTinyGraphStore();
        Column c1 = source.edgeTable.addColumn("foo", String.class);
        Column c2 = source.edgeTable.addColumn("bar", TimestampIntegerMap.class);
        Edge e0 = source.getEdge("0");
        e0.setAttribute(c1, "test");
        e0.setAttribute(c2, 10, 1.0);
        e0.setAttribute(c2, 20, 2.0);

        GraphStore dest = new GraphStore();
        new GraphBridgeImpl(dest).copyNodes(source.getNodes().toArray());
        Column c1Copy = dest.edgeTable.getColumn("foo");
        Column c2Copy = dest.edgeTable.getColumn("bar");
        Edge e0Copy = dest.getEdge("0");
        Assert.assertEquals(e0Copy.getAttribute(c1Copy), "test");
        Assert.assertEquals(e0Copy.getAttribute(c2Copy, 1.0), 10);
        Assert.assertEquals(e0Copy.getAttribute(c2Copy, 2.0), 20);
    }
}
