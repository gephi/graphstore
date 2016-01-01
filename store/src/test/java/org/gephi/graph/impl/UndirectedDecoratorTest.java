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

import java.util.Arrays;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UndirectedDecoratorTest {

    @Test
    public void testAddEdge() {
        GraphStore store = generateTinyUndirectedGraph();
        UndirectedGraph graph = store.undirectedDecorator;
        Assert.assertNotNull(graph.getNode("1"));
        Assert.assertNotNull(graph.getNode("2"));
        Assert.assertNotNull(graph.getEdge("0"));
    }

    @Test
    public void testAddAllEdge() {
        GraphStore store = new GraphStore();
        Node n1 = store.factory.newNode("1");
        Node n2 = store.factory.newNode("2");
        store.addNode(n1);
        store.addNode(n2);
        Edge e0 = store.factory.newEdge("0", n1, n2, 0, 1.0, false);
        store.addAllEdges(Arrays.asList(new Edge[]{e0}));
        Assert.assertNotNull(store.undirectedDecorator.getEdge("0"));
        Assert.assertTrue(store.undirectedDecorator.contains(e0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddEdgeDirected() {
        GraphStore store = generateTinyUndirectedGraph();
        UndirectedGraph graph = store.undirectedDecorator;
        Edge e1 = store.factory.newEdge(graph.getNode("2"), graph.getNode("1"), true);
        graph.addEdge(e1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddAllEdgeDirected() {
        GraphStore store = generateTinyUndirectedGraph();
        UndirectedGraph graph = store.undirectedDecorator;
        Edge e1 = store.factory.newEdge(graph.getNode("2"), graph.getNode("1"), true);
        graph.addAllEdges(Arrays.asList(new Edge[]{e1}));
    }

    @Test
    public void testIsEdgeDirected() {
        GraphStore store = generateTinyUndirectedGraph();
        UndirectedGraph graph = store.undirectedDecorator;
        Assert.assertFalse(graph.isDirected(store.getEdge("0")));
    }

    @Test
    public void testIsUndirected() {
        GraphStore store = generateTinyUndirectedGraph();
        UndirectedGraph graph = store.undirectedDecorator;
        Assert.assertTrue(graph.isUndirected());
        Assert.assertFalse(graph.isMixed());
        Assert.assertFalse(graph.isDirected());
    }

    //UTILITY
    private GraphStore generateTinyUndirectedGraph() {
        GraphStore store = new GraphStore();
        Node n1 = store.factory.newNode("1");
        Node n2 = store.factory.newNode("2");
        store.addAllNodes(Arrays.asList(new Node[]{n1, n2}));
        Edge e0 = store.factory.newEdge("0", n1, n2, 0, 1.0, false);
        store.addEdge(e0);
        return store;
    }
}
