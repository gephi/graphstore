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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UndirectedDecoratorTest {

    @Test
    public void testAddEdge() {
        GraphStore store = GraphGenerator.generateTinyUndirectedGraphStore();
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
        store.addAllEdges(Arrays.asList(new Edge[] { e0 }));
        Assert.assertNotNull(store.undirectedDecorator.getEdge("0"));
        Assert.assertTrue(store.undirectedDecorator.contains(e0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddEdgeDirected() {
        GraphStore store = GraphGenerator.generateTinyUndirectedGraphStore();
        UndirectedGraph graph = store.undirectedDecorator;
        Edge e1 = store.factory.newEdge(graph.getNode("2"), graph.getNode("1"), true);
        graph.addEdge(e1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddAllEdgeDirected() {
        GraphStore store = GraphGenerator.generateTinyUndirectedGraphStore();
        UndirectedGraph graph = store.undirectedDecorator;
        Edge e1 = store.factory.newEdge(graph.getNode("2"), graph.getNode("1"), true);
        graph.addAllEdges(Arrays.asList(new Edge[] { e1 }));
    }

    @Test
    public void testIsEdgeDirected() {
        GraphStore store = GraphGenerator.generateTinyGraphStore();
        UndirectedGraph graph = store.undirectedDecorator;
        Assert.assertFalse(graph.isDirected(store.getEdge("0")));
    }

    @Test
    public void testIsGraphUndirected() {
        GraphStore store = GraphGenerator.generateTinyGraphStore();
        UndirectedGraph graph = store.undirectedDecorator;
        Assert.assertTrue(graph.isUndirected());
        Assert.assertFalse(graph.isMixed());
        Assert.assertFalse(graph.isDirected());
    }

    @Test
    public void testGetEdgeDirected() {
        GraphStore store = GraphGenerator.generateTinyGraphStore();
        UndirectedGraph graph = store.undirectedDecorator;
        Node n1 = graph.getNode("1");
        Node n2 = graph.getNode("2");
        Assert.assertNotNull(graph.getEdge(n1, n2));
        Assert.assertNotNull(graph.getEdge(n2, n1));
        Assert.assertNotNull(graph.getEdge(n1, n2, 0));
        Assert.assertNotNull(graph.getEdge(n2, n1, 0));
    }

    @Test
    public void testGetEdgeUndirected() {
        GraphStore store = GraphGenerator.generateTinyUndirectedGraphStore();
        UndirectedGraph graph = store.undirectedDecorator;
        Node n1 = graph.getNode("1");
        Node n2 = graph.getNode("2");
        Assert.assertNotNull(graph.getEdge(n1, n2));
        Assert.assertNotNull(graph.getEdge(n2, n1));
        Assert.assertNotNull(graph.getEdge(n1, n2, 0));
        Assert.assertNotNull(graph.getEdge(n2, n1, 0));
    }

    @Test
    public void testGetEdgeMixed() {
        GraphStore store = GraphGenerator.generateSmallMixedGraphStore();
        UndirectedGraph graph = store.undirectedDecorator;
        for (Edge e : graph.getEdges()) {
            Assert.assertNotNull(graph.getEdge(e.getSource(), e.getTarget()));
            Assert.assertNotNull(graph.getEdge(e.getTarget(), e.getSource()));
            Assert.assertNotNull(graph.getEdge(e.getSource(), e.getTarget(), 0));
            Assert.assertNotNull(graph.getEdge(e.getTarget(), e.getSource(), 0));
        }
    }

    @Test
    public void testGetEdgesDirected() {
        GraphStore store = new GraphStore();
        Node n1 = store.factory.newNode("1");
        Node n2 = store.factory.newNode("2");
        store.addNode(n1);
        store.addNode(n2);
        Edge e0 = store.factory.newEdge("0", n1, n2, 0, 1.0, true);
        Edge e1 = store.factory.newEdge("1", n1, n2, 0, 1.0, true);
        Edge e2 = store.factory.newEdge("2", n2, n1, 0, 1.0, true);
        Edge e3 = store.factory.newEdge("3", n2, n1, 0, 1.0, true);

        store.addAllEdges(Arrays.asList(new Edge[] { e0, e1, e2, e3 }));
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2), new Edge[] { e2, e3 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2, 0), new Edge[] { e2, e3 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2, 1), new Edge[] {});
    }

    @Test
    public void testGetEdgesUndirected() {
        GraphStore store = new GraphStore();
        Node n1 = store.factory.newNode("1");
        Node n2 = store.factory.newNode("2");
        store.addNode(n1);
        store.addNode(n2);
        Edge e0 = store.factory.newEdge("0", n1, n2, 0, 1.0, false);
        Edge e1 = store.factory.newEdge("1", n2, n1, 0, 1.0, false);

        store.addAllEdges(Arrays.asList(new Edge[] { e0, e1 }));
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2), new Edge[] { e0, e1 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2, 0), new Edge[] { e0, e1 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2, 1), new Edge[] {});
    }

    @Test
    public void testGetEdgesUndirectedAllWithNonNullType() {
        GraphStore store = new GraphStore();
        Node n1 = store.factory.newNode("1");
        Node n2 = store.factory.newNode("2");
        store.addNode(n1);
        store.addNode(n2);
        Edge e0 = store.factory.newEdge("0", n1, n2, 1, 1.0, false);
        Edge e1 = store.factory.newEdge("1", n2, n1, 2, 1.0, false);

        store.addAllEdges(Arrays.asList(new Edge[] { e0, e1 }));
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2), new Edge[] {});
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2, 0), new Edge[] {});
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2, 1), new Edge[] { e0 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2, 2), new Edge[] { e1 });
    }

    @Test
    public void testGetEdgesMixed() {
        GraphStore store = new GraphStore();
        Node n1 = store.factory.newNode("1");
        Node n2 = store.factory.newNode("2");
        Node n3 = store.factory.newNode("3");
        store.addNode(n1);
        store.addNode(n2);
        store.addNode(n3);
        Edge e0 = store.factory.newEdge("0", n1, n2, 0, 1.0, true);
        Edge e1 = store.factory.newEdge("1", n2, n3, 0, 1.0, false);
        Edge e2 = store.factory.newEdge("2", n2, n1, 0, 1.0, true);
        Edge e3 = store.factory.newEdge("3", n3, n2, 0, 1.0, false);

        store.addAllEdges(Arrays.asList(new Edge[] { e0, e1, e2, e3 }));
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2), new Edge[] { e2 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2, 0), new Edge[] { e2 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n1, n2, 1), new Edge[] {});

        testEdgeIterable(store.undirectedDecorator.getEdges(n2, n1), new Edge[] { e2 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n2, n1, 0), new Edge[] { e2 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n2, n1, 1), new Edge[] {});

        testEdgeIterable(store.undirectedDecorator.getEdges(n2, n3), new Edge[] { e1, e3 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n2, n3, 0), new Edge[] { e1, e3 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n2, n3, 1), new Edge[] {});

        testEdgeIterable(store.undirectedDecorator.getEdges(n3, n2), new Edge[] { e1, e3 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n3, n2, 0), new Edge[] { e1, e3 });
        testEdgeIterable(store.undirectedDecorator.getEdges(n3, n2, 1), new Edge[] {});
    }

    // UTILITY
    private void testEdgeIterable(EdgeIterable iterable, Edge[] edges) {
        Set<Edge> edgeSet = new HashSet<Edge>(iterable.toCollection());
        for (Edge n : edges) {
            Assert.assertTrue(edgeSet.remove(n));
        }
        Assert.assertEquals(edgeSet.size(), 0);
    }
}
