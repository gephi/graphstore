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
        Edge edge = graphFactory.newEdge(source, target, 9, true);

        Assert.assertEquals(edge.getId(), 0);
        Assert.assertEquals(graphFactory.getEdgeCounter(), 1);
        Assert.assertSame(edge.getSource(), source);
        Assert.assertSame(edge.getTarget(), target);
        Assert.assertEquals(edge.getType(), 9);
        Assert.assertTrue(edge.isDirected());
    }

    @Test
    public void testNewEdgeWithDirected() {
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
}
