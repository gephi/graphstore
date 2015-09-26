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

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphFactoryTest {

    @Test
    public void testEmpty() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(new GraphStore());
        Assert.assertEquals(graphFactory.getNodeCounter(), 0);
        Assert.assertEquals(graphFactory.getEdgeCounter(), 0);
    }

    @Test
    public void testNewNode() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(new GraphStore());
        Node node = graphFactory.newNode();

        Assert.assertEquals(node.getId(), "0");
        Assert.assertEquals(graphFactory.getNodeCounter(), 1);
    }

    @Test
    public void testNewNodeWithId() {
        String id = "Foo";
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(new GraphStore());
        Node node = graphFactory.newNode(id);

        Assert.assertEquals(node.getId(), id);
        Assert.assertEquals(graphFactory.getNodeCounter(), 0);
    }

    @Test
    public void testNewEdge() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(new GraphStore());
        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");
        Edge edge = graphFactory.newEdge(source, target);

        Assert.assertEquals(edge.getId(), "0");
        Assert.assertEquals(graphFactory.getEdgeCounter(), 1);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getWeight(), 1.0);
        Assert.assertEquals(edge.getType(), EdgeTypeStore.NULL_LABEL);
        Assert.assertTrue(edge.isDirected());
    }

    @Test
    public void testNewEdgeWithDirected() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(new GraphStore());
        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");
        Edge edge = graphFactory.newEdge(source, target, false);

        Assert.assertEquals(edge.getId(), "0");
        Assert.assertEquals(graphFactory.getEdgeCounter(), 1);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getWeight(), 1.0);
        Assert.assertEquals(edge.getType(), EdgeTypeStore.NULL_LABEL);
        Assert.assertFalse(edge.isDirected());
    }

    @Test
    public void testNewEdgeWithDirectedAndType() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(new GraphStore());
        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");
        Edge edge = graphFactory.newEdge(source, target, 9, false);

        Assert.assertEquals(edge.getId(), "0");
        Assert.assertEquals(graphFactory.getEdgeCounter(), 1);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getType(), 9);
        Assert.assertEquals(edge.getWeight(), 1.0);
        Assert.assertFalse(edge.isDirected());
    }

    @Test
    public void testNewEdgeWithDirectedAndTypeAndWeight() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(new GraphStore());
        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");
        Edge edge = graphFactory.newEdge(source, target, 9, 7.0, false);

        Assert.assertEquals(edge.getId(), "0");
        Assert.assertEquals(graphFactory.getEdgeCounter(), 1);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getType(), 9);
        Assert.assertEquals(edge.getWeight(), 7.0);
        Assert.assertFalse(edge.isDirected());
    }

    @Test
    public void testNewEdgeWithId() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(new GraphStore());
        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");
        String id = "foo";
        Edge edge = graphFactory.newEdge(id, source, target, 9, 7.0, false);

        Assert.assertEquals(edge.getId(), id);
        Assert.assertEquals(graphFactory.getEdgeCounter(), 0);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getType(), 9);
        Assert.assertEquals(edge.getWeight(), 7.0);
        Assert.assertFalse(edge.isDirected());
    }

    @Test
    public void testIntegerNodeId() {
        Configuration config = new Configuration();
        config.setNodeIdType(Integer.class);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(graphModel.store);
        Node node = graphFactory.newNode();
        Assert.assertEquals(node.getId(), 0);
    }

    @Test
    public void testIntegerEdgeId() {
        Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(graphModel.store);

        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");

        Edge edge = graphFactory.newEdge(source, target);
        Assert.assertEquals(edge.getId(), 0);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testUnsupportedNodeId() {
        Configuration config = new Configuration();
        config.setNodeIdType(Float.class);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(graphModel.store);
        graphFactory.newNode();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testUnsupportedEdgeId() {
        Configuration config = new Configuration();
        config.setEdgeIdType(Float.class);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(graphModel.store);

        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");

        graphFactory.newEdge(source, target);
    }

    @Test
    public void testAutoIncrementNodeInt() {
        Configuration config = new Configuration();
        config.setNodeIdType(Integer.class);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(graphModel.store);
        graphFactory.newNode(10);
        Assert.assertEquals(graphFactory.newNode().getId(), 11);
        graphFactory.newNode(5);
        Assert.assertEquals(graphFactory.newNode().getId(), 12);
    }

    @Test
    public void testAutoIncrementEdgeInt() {
        Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        GraphModelImpl graphModel = new GraphModelImpl(config);
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(graphModel.store);
        Node n1 = graphFactory.newNode();
        Node n2 = graphFactory.newNode();
        Node n3 = graphFactory.newNode();

        graphFactory.newEdge(10, n1, n2, 9, 7.0, false);
        Assert.assertEquals(graphFactory.newEdge(n2, n3).getId(), 11);
        graphFactory.newEdge(5, n1, n2, 9, 7.0, false);
        Assert.assertEquals(graphFactory.newEdge(n1, n3).getId(), 12);
    }

    @Test
    public void testAutoIncrementNodeString() {
        GraphModelImpl graphModel = new GraphModelImpl(new Configuration());
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(graphModel.store);
        graphFactory.newNode("10");
        Assert.assertEquals(graphFactory.newNode().getId(), "11");
        graphFactory.newNode("s15");
        Assert.assertEquals(graphFactory.newNode().getId(), "12");
        graphFactory.newNode("5");
        Assert.assertEquals(graphFactory.newNode().getId(), "13");
    }

    @Test
    public void testAutoIncrementEdgeString() {
        GraphModelImpl graphModel = new GraphModelImpl(new Configuration());
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(graphModel.store);
        Node n1 = graphFactory.newNode();
        Node n2 = graphFactory.newNode();
        Node n3 = graphFactory.newNode();

        graphFactory.newEdge("10", n1, n2, 9, 7.0, false);
        Assert.assertEquals(graphFactory.newEdge(n2, n3).getId(), "11");
        graphFactory.newEdge("e15", n3, n2, 9, 7.0, false);
        Assert.assertEquals(graphFactory.newEdge(n1, n3).getId(), "12");
        graphFactory.newEdge("5", n1, n3, 9, 7.0, false);
        Assert.assertEquals(graphFactory.newEdge(n1, n3).getId(), "13");
    }

    @Test
    public void testDeepEquals() {
        GraphFactoryImpl gf1 = new GraphFactoryImpl(new GraphStore());
        GraphFactoryImpl gf2 = new GraphFactoryImpl(new GraphStore());

        Assert.assertTrue(gf1.deepEquals(gf2));
        Assert.assertTrue(gf1.deepEquals(gf1));

        gf1.setNodeCounter(42);
        gf1.setEdgeCounter(12);

        gf2.setNodeCounter(44);
        gf2.setEdgeCounter(5);

        Assert.assertFalse(gf1.deepEquals(gf2));
        gf2.setNodeCounter(42);
        Assert.assertFalse(gf1.deepEquals(gf2));
    }

    @Test
    public void testDeepHashCode() {
        GraphFactoryImpl gf1 = new GraphFactoryImpl(new GraphStore());
        GraphFactoryImpl gf2 = new GraphFactoryImpl(new GraphStore());

        Assert.assertEquals(gf1.deepHashCode(), gf2.deepHashCode());
        Assert.assertEquals(gf1.deepHashCode(), gf1.deepHashCode());

        gf1.setNodeCounter(42);
        gf1.setEdgeCounter(12);

        gf2.setNodeCounter(44);
        gf2.setEdgeCounter(5);

        Assert.assertNotEquals(gf1.deepHashCode(), gf2.deepHashCode());
        gf2.setNodeCounter(42);
        Assert.assertNotEquals(gf1.deepHashCode(), gf2.deepHashCode());
    }
}
