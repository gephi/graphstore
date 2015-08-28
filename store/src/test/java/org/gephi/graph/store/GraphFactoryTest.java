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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class GraphFactoryTest {

    @Test
    public void testEmpty() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(null);
        Assert.assertEquals(graphFactory.getNodeCounter(), 0);
        Assert.assertEquals(graphFactory.getEdgeCounter(), 0);
    }

    @Test
    public void testNewNode() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(null);
        Node node = graphFactory.newNode();

        Assert.assertEquals(node.getId(), 0);
        Assert.assertEquals(graphFactory.getNodeCounter(), 1);
    }

    @Test
    public void testNewNodeWithId() {
        String id = "Foo";
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(null);
        Node node = graphFactory.newNode(id);

        Assert.assertEquals(node.getId(), id);
        Assert.assertEquals(graphFactory.getNodeCounter(), 0);
    }

    @Test
    public void testNewEdge() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(null);
        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");
        Edge edge = graphFactory.newEdge(source, target);

        Assert.assertEquals(edge.getId(), 0);
        Assert.assertEquals(graphFactory.getEdgeCounter(), 1);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getWeight(), 1.0);
        Assert.assertEquals(edge.getType(), EdgeTypeStore.NULL_LABEL);
        Assert.assertTrue(edge.isDirected());
    }

    @Test
    public void testNewEdgeWithDirected() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(null);
        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");
        Edge edge = graphFactory.newEdge(source, target, false);

        Assert.assertEquals(edge.getId(), 0);
        Assert.assertEquals(graphFactory.getEdgeCounter(), 1);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getWeight(), 1.0);
        Assert.assertEquals(edge.getType(), EdgeTypeStore.NULL_LABEL);
        Assert.assertFalse(edge.isDirected());
    }

    @Test
    public void testNewEdgeWithDirectedAndType() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(null);
        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");
        Edge edge = graphFactory.newEdge(source, target, 9, false);

        Assert.assertEquals(edge.getId(), 0);
        Assert.assertEquals(graphFactory.getEdgeCounter(), 1);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getType(), 9);
        Assert.assertEquals(edge.getWeight(), 1.0);
        Assert.assertFalse(edge.isDirected());
    }

    @Test
    public void testNewEdgeWithDirectedAndTypeAndWeight() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(null);
        Node source = graphFactory.newNode("source");
        Node target = graphFactory.newNode("target");
        Edge edge = graphFactory.newEdge(source, target, 9, 7.0, false);

        Assert.assertEquals(edge.getId(), 0);
        Assert.assertEquals(graphFactory.getEdgeCounter(), 1);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getType(), 9);
        Assert.assertEquals(edge.getWeight(), 7.0);
        Assert.assertFalse(edge.isDirected());
    }

    @Test
    public void testNewEdgeWithId() {
        GraphFactoryImpl graphFactory = new GraphFactoryImpl(null);
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
    public void testEdgeFields() {
        NodeImpl source = new NodeImpl("0");
        NodeImpl target = new NodeImpl("1");
        double weight = 2.0;
        EdgeImpl edge = new EdgeImpl("0", source, target, 0, weight, true);

        Assert.assertTrue(edge.isDirected());
        Assert.assertFalse(edge.isSelfLoop());
        Assert.assertFalse(edge.isMutual());
        Assert.assertFalse(edge.isValid());

        edge.setMutual(true);

        Assert.assertTrue(edge.isMutual());

        edge = new EdgeImpl("0", source, source, 0, weight, true);

        Assert.assertTrue(edge.isSelfLoop());

        edge = new EdgeImpl("0", source, target, 0, weight, false);

        Assert.assertFalse(edge.isDirected());

        edge.setMutual(true);

        Assert.assertFalse(edge.isMutual());
    }

    @Test
    public void testEquals() {
        GraphFactoryImpl gf1 = new GraphFactoryImpl(null);
        GraphFactoryImpl gf2 = new GraphFactoryImpl(null);

        Assert.assertTrue(gf1.equals(gf2));
        Assert.assertTrue(gf1.equals(gf1));

        gf1.setNodeCounter(42);
        gf1.setEdgeCounter(12);

        gf2.setNodeCounter(44);
        gf2.setEdgeCounter(5);

        Assert.assertFalse(gf1.equals(gf2));
        gf2.setNodeCounter(42);
        Assert.assertFalse(gf1.equals(gf2));
    }

    @Test
    public void testHashCode() {
        GraphFactoryImpl gf1 = new GraphFactoryImpl(null);
        GraphFactoryImpl gf2 = new GraphFactoryImpl(null);

        Assert.assertEquals(gf1.hashCode(), gf2.hashCode());
        Assert.assertEquals(gf1.hashCode(), gf1.hashCode());
        
        gf1.setNodeCounter(42);
        gf1.setEdgeCounter(12);

        gf2.setNodeCounter(44);
        gf2.setEdgeCounter(5);

        Assert.assertNotEquals(gf1.hashCode(), gf2.hashCode());
        gf2.setNodeCounter(42);
        Assert.assertNotEquals(gf1.hashCode(), gf2.hashCode());
    }
}
