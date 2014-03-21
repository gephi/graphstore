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
public class GraphVersionTest {

    @Test
    public void testDefaultGraphVersion() {
        GraphVersion graphVersion = new GraphVersion(null);

        Assert.assertEquals(graphVersion.nodeVersion, Integer.MIN_VALUE + 1);
        Assert.assertEquals(graphVersion.edgeVersion, Integer.MIN_VALUE + 1);

        int nv = graphVersion.incrementAndGetNodeVersion();
        int ev = graphVersion.incrementAndGetEdgeVersion();

        Assert.assertEquals(nv, Integer.MIN_VALUE + 2);
        Assert.assertEquals(ev, Integer.MIN_VALUE + 2);
    }

    @Test
    public void testInfiniteLoop() {
        GraphVersion graphVersion = new GraphVersion(null);
        graphVersion.nodeVersion = Integer.MAX_VALUE - 1;
        graphVersion.edgeVersion = Integer.MAX_VALUE - 1;

        int nv = graphVersion.incrementAndGetNodeVersion();
        int ev = graphVersion.incrementAndGetEdgeVersion();

        Assert.assertEquals(nv, Integer.MIN_VALUE + 1);
        Assert.assertEquals(ev, Integer.MIN_VALUE + 1);
    }

    @Test
    public void testAddNode() {
        GraphStore graphStore = new GraphStore();
        NodeImpl[] nodes = GraphGenerator.generateNodeList(1);
        int nodeVersion = graphStore.version.nodeVersion;
        int edgeVersion = graphStore.version.edgeVersion;
        graphStore.addNode(nodes[0]);

        Assert.assertEquals(graphStore.version.nodeVersion, nodeVersion + 1);
        Assert.assertEquals(graphStore.version.edgeVersion, edgeVersion);
    }

    @Test
    public void testRemoveNode() {
        GraphStore graphStore = new GraphStore();
        NodeImpl[] nodes = GraphGenerator.generateNodeList(1);
        int nodeVersion = graphStore.version.nodeVersion;
        int edgeVersion = graphStore.version.edgeVersion;
        graphStore.addNode(nodes[0]);
        graphStore.removeNode(nodes[0]);

        Assert.assertEquals(graphStore.version.nodeVersion, nodeVersion + 2);
        Assert.assertEquals(graphStore.version.edgeVersion, edgeVersion);
    }

    @Test
    public void testClear() {
        GraphStore graphStore = new GraphStore();
        NodeImpl[] nodes = GraphGenerator.generateNodeList(1);
        int nodeVersion = graphStore.version.nodeVersion;
        int edgeVersion = graphStore.version.edgeVersion;
        graphStore.addNode(nodes[0]);
        graphStore.clear();

        Assert.assertEquals(graphStore.version.nodeVersion, nodeVersion + 2);
        Assert.assertEquals(graphStore.version.edgeVersion, edgeVersion);
    }

    @Test
    public void testClearWithEdges() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        int nodeVersion = graphStore.version.nodeVersion;
        int edgeVersion = graphStore.version.edgeVersion;
        graphStore.clear();

        Assert.assertEquals(graphStore.version.nodeVersion, nodeVersion + 1);
        Assert.assertEquals(graphStore.version.edgeVersion, edgeVersion + 1);
    }

    @Test
    public void testAddEdge() {
        GraphStore graphStore = new GraphStore();
        NodeImpl[] nodes = GraphGenerator.generateNodeList(2);
        EdgeImpl edge = new EdgeImpl("0", graphStore, nodes[0], nodes[1], 0, 1.0, true);
        graphStore.addNode(nodes[0]);
        graphStore.addNode(nodes[1]);
        int edgeVersion = graphStore.version.edgeVersion;
        int nodeVersion = graphStore.version.nodeVersion;

        graphStore.addEdge(edge);

        Assert.assertEquals(graphStore.version.edgeVersion, edgeVersion + 1);
        Assert.assertEquals(graphStore.version.nodeVersion, nodeVersion);
    }

    @Test
    public void testRemoveEdge() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        int edgeVersion = graphStore.version.edgeVersion;
        int nodeVersion = graphStore.version.nodeVersion;
        graphStore.removeEdge(graphStore.getEdges().toArray()[0]);

        Assert.assertEquals(graphStore.version.edgeVersion, edgeVersion + 1);
        Assert.assertEquals(graphStore.version.nodeVersion, nodeVersion);
    }

    @Test
    public void testClearEdges() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        int edgeVersion = graphStore.version.edgeVersion;
        int nodeVersion = graphStore.version.nodeVersion;
        graphStore.clearEdges();

        Assert.assertEquals(graphStore.version.edgeVersion, edgeVersion + 1);
        Assert.assertEquals(graphStore.version.nodeVersion, nodeVersion);
    }

    @Test
    public void testDefaultViewVersion() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewImpl view = graphStore.viewStore.createView();

        Assert.assertNotNull(view.version);
        Assert.assertNotSame(view.version, graphStore.version);
        Assert.assertEquals(view.version.nodeVersion, Integer.MIN_VALUE + 1);
        Assert.assertEquals(view.version.edgeVersion, Integer.MIN_VALUE + 1);
    }

    @Test
    public void testViewAddNode() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        int edgeGraphVersion = graphStore.version.edgeVersion;
        int nodeGraphVersion = graphStore.version.nodeVersion;
        Node[] nodes = graphStore.getNodes().toArray();

        GraphViewImpl view = graphStore.viewStore.createView();
        int edgeVersion = view.version.edgeVersion;
        int nodeVersion = view.version.nodeVersion;

        view.addNode(nodes[0]);

        Assert.assertEquals(view.version.edgeVersion, edgeVersion);
        Assert.assertEquals(view.version.nodeVersion, nodeVersion + 1);

        Assert.assertEquals(graphStore.version.edgeVersion, edgeGraphVersion);
        Assert.assertEquals(graphStore.version.nodeVersion, nodeGraphVersion);
    }

    @Test
    public void testViewRemoveNode() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        int edgeGraphVersion = graphStore.version.edgeVersion;
        int nodeGraphVersion = graphStore.version.nodeVersion;
        Node[] nodes = graphStore.getNodes().toArray();

        GraphViewImpl view = graphStore.viewStore.createView();
        int edgeVersion = view.version.edgeVersion;
        int nodeVersion = view.version.nodeVersion;

        view.addNode(nodes[0]);
        view.removeNode(nodes[0]);

        Assert.assertEquals(view.version.edgeVersion, edgeVersion);
        Assert.assertEquals(view.version.nodeVersion, nodeVersion + 2);

        Assert.assertEquals(graphStore.version.edgeVersion, edgeGraphVersion);
        Assert.assertEquals(graphStore.version.nodeVersion, nodeGraphVersion);
    }

    @Test
    public void testViewAddEdge() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        int edgeGraphVersion = graphStore.version.edgeVersion;
        int nodeGraphVersion = graphStore.version.nodeVersion;
        Edge[] edges = graphStore.getEdges().toArray();

        GraphViewImpl view = graphStore.viewStore.createView();
        int edgeVersion = view.version.edgeVersion;
        int nodeVersion = view.version.nodeVersion;

        view.addNode(edges[0].getSource());
        view.addNode(edges[0].getTarget());
        view.addEdge(edges[0]);

        Assert.assertEquals(view.version.edgeVersion, edgeVersion + 1);
        Assert.assertEquals(view.version.nodeVersion, nodeVersion + 2);

        Assert.assertEquals(graphStore.version.edgeVersion, edgeGraphVersion);
        Assert.assertEquals(graphStore.version.nodeVersion, nodeGraphVersion);
    }

    @Test
    public void testViewRemoveEdge() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        int edgeGraphVersion = graphStore.version.edgeVersion;
        int nodeGraphVersion = graphStore.version.nodeVersion;
        Edge[] edges = graphStore.getEdges().toArray();

        GraphViewImpl view = graphStore.viewStore.createView();
        view.fill();

        int edgeVersion = view.version.edgeVersion;
        int nodeVersion = view.version.nodeVersion;

        view.removeEdge(edges[0]);

        Assert.assertEquals(view.version.edgeVersion, edgeVersion + 1);
        Assert.assertEquals(view.version.nodeVersion, nodeVersion);

        Assert.assertEquals(graphStore.version.edgeVersion, edgeGraphVersion);
        Assert.assertEquals(graphStore.version.nodeVersion, nodeGraphVersion);
    }

    @Test
    public void testViewFillClear() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();

        GraphViewImpl view = graphStore.viewStore.createView();
        int edgeVersion = view.version.edgeVersion;
        int nodeVersion = view.version.nodeVersion;
        view.clear();

        Assert.assertEquals(view.version.edgeVersion, edgeVersion);
        Assert.assertEquals(view.version.nodeVersion, nodeVersion);

        view.fill();

        Assert.assertEquals(view.version.edgeVersion, edgeVersion + 1);
        Assert.assertEquals(view.version.nodeVersion, nodeVersion + 1);

        edgeVersion = view.version.edgeVersion;
        nodeVersion = view.version.nodeVersion;

        view.clear();

        Assert.assertEquals(view.version.edgeVersion, edgeVersion + 1);
        Assert.assertEquals(view.version.nodeVersion, nodeVersion + 1);
    }

    @Test
    public void testViewRemoveNodeMain() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewImpl view = graphStore.viewStore.createView();
        view.fill();

        int edgeVersion = view.version.edgeVersion;
        int nodeVersion = view.version.nodeVersion;

        Node node = graphStore.getNodes().toArray()[0];
        int degree = graphStore.getDegree(node);
        graphStore.removeNode(node);

        Assert.assertEquals(view.version.edgeVersion, edgeVersion + degree);
        Assert.assertEquals(view.version.nodeVersion, nodeVersion + 1);
    }

    @Test
    public void testViewRemoveEdgeMain() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        GraphViewImpl view = graphStore.viewStore.createView();
        view.fill();

        int edgeVersion = view.version.edgeVersion;
        int nodeVersion = view.version.nodeVersion;

        Edge edge = graphStore.getEdges().toArray()[0];
        graphStore.removeEdge(edge);

        Assert.assertEquals(view.version.edgeVersion, edgeVersion + 1);
        Assert.assertEquals(view.version.nodeVersion, nodeVersion);
    }
}
