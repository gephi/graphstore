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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.TextProperties;
import org.gephi.graph.spi.LayoutData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class GraphStoreTest {

    @Test
    public void testEmpty() {
        GraphStore graphStore = new GraphStore();
        BasicGraphStore basicStore = new BasicGraphStore();

        testBasicStoreEquals(graphStore, basicStore);
    }

    @Test
    public void testFullDirected() {
        GraphStore graphStore = new GraphStore();
        BasicGraphStore basicStore = new BasicGraphStore();

        graphStore.edgeTypeStore.addType("0");

        NodeImpl[] nodes = GraphGenerator.generateLargeNodeList();
        BasicGraphStore.BasicNode[] basicNodes = GraphGenerator.generateLargeBasicNodeList();

        graphStore.addAllNodes(Arrays.asList(nodes));
        basicStore.addAllNodes(Arrays.asList(basicNodes));

        EdgeImpl[] edges = GraphGenerator.generateEdgeList(graphStore.nodeStore, 20000, 0, true, true);
        BasicGraphStore.BasicEdge[] basicEdges = GraphGenerator.generateBasicEdgeList(basicStore.nodeStore, 20000, 0, true, true);

        graphStore.addAllEdges(Arrays.asList(edges));
        basicStore.addAllEdges(Arrays.asList(basicEdges));

        testBasicStoreEquals(graphStore, basicStore);
    }

    @Test
    public void testFullUndirected() {
        GraphStore graphStore = new GraphStore();
        BasicGraphStore basicStore = new BasicGraphStore();

        graphStore.edgeTypeStore.addType("0");

        NodeImpl[] nodes = GraphGenerator.generateLargeNodeList();
        BasicGraphStore.BasicNode[] basicNodes = GraphGenerator.generateLargeBasicNodeList();

        graphStore.addAllNodes(Arrays.asList(nodes));
        basicStore.addAllNodes(Arrays.asList(basicNodes));

        EdgeImpl[] edges = GraphGenerator.generateEdgeList(graphStore.nodeStore, 20000, 0, false, true);
        BasicGraphStore.BasicEdge[] basicEdges = GraphGenerator.generateBasicEdgeList(basicStore.nodeStore, 20000, 0, false, true);

        graphStore.addAllEdges(Arrays.asList(edges));
        basicStore.addAllEdges(Arrays.asList(basicEdges));

        testBasicStoreEquals(graphStore, basicStore);
    }

    @Test
    public void testFullMixed() {
        GraphStore graphStore = new GraphStore();
        BasicGraphStore basicStore = new BasicGraphStore();

        graphStore.edgeTypeStore.addType("0");

        NodeImpl[] nodes = GraphGenerator.generateLargeNodeList();
        BasicGraphStore.BasicNode[] basicNodes = GraphGenerator.generateLargeBasicNodeList();

        graphStore.addAllNodes(Arrays.asList(nodes));
        basicStore.addAllNodes(Arrays.asList(basicNodes));

        EdgeImpl[] edges = GraphGenerator.generateMixedEdgeList(graphStore.nodeStore, 20000, 0, true);
        BasicGraphStore.BasicEdge[] basicEdges = GraphGenerator.generateBasicMixedEdgeList(basicStore.nodeStore, 20000, 0, true);

        graphStore.addAllEdges(Arrays.asList(edges));
        basicStore.addAllEdges(Arrays.asList(basicEdges));

        testBasicStoreEquals(graphStore, basicStore);
    }

    @Test
    public void testAddNode() {
        GraphStore graphStore = new GraphStore();
        NodeImpl[] nodes = GraphGenerator.generateNodeList(1);

        boolean a = graphStore.addNode(nodes[0]);
        boolean b = graphStore.addNode(nodes[0]);

        Assert.assertTrue(a);
        Assert.assertFalse(b);

        boolean c = graphStore.contains(nodes[0]);

        Assert.assertTrue(c);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testAddNodeClass() {
        GraphStore graphStore = new GraphStore();

        graphStore.addNode(new Node() {
            @Override
            public Object getId() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getAttribute(String key) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getAttribute(Column column) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getAttribute(Column column, double timestamp) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getAttribute(String key, double timestamp) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object[] getAttributes() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Set<String> getAttributeKeys() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object removeAttribute(String key) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object removeAttribute(Column column) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setAttribute(String key, Object value) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setAttribute(Column column, Object value) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void clearAttributes() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setAttribute(String key, Object value, double timestamp) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setAttribute(Column column, Object value, double timestamp) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean addTimestamp(double timestamp) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean removeTimestamp(double timestamp) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public double[] getTimestamps() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float x() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float y() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float z() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float r() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float g() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float b() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getRGBA() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Color getColor() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float alpha() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float size() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setX(float x) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setY(float y) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setZ(float z) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setPosition(float x, float y) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setPosition(float x, float y, float z) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setR(float r) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setG(float g) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setB(float b) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setAlpha(float a) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setColor(Color color) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setSize(float size) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isFixed() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public <T extends LayoutData> T getLayoutData() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setFixed(boolean fixed) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setLayoutData(LayoutData layoutData) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public TextProperties getTextProperties() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getLabel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setLabel(String label) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getStoreId() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getAttribute(String key, GraphView view) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getAttribute(Column column, GraphView view) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean hasTimestamp(double timestamp) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    @Test
    public void testAddEdge() {
        GraphStore graphStore = new GraphStore();
        NodeImpl[] nodes = GraphGenerator.generateNodeList(2);
        graphStore.addAllNodes(Arrays.asList(nodes));

        EdgeImpl edge = new EdgeImpl("0", nodes[0], nodes[1], 0, 1.0, true);
        boolean a = graphStore.addEdge(edge);
        boolean b = graphStore.addEdge(edge);

        Assert.assertTrue(a);
        Assert.assertFalse(b);

        boolean c = graphStore.contains(edge);

        Assert.assertTrue(c);
    }

    @Test
    public void testRemoveNodeWithEdges() {
        GraphStore graphStore = new GraphStore();
        NodeImpl[] nodes = GraphGenerator.generateSmallNodeList();
        graphStore.addAllNodes(Arrays.asList(nodes));

        EdgeImpl[] edges = GraphGenerator.generateEdgeList(graphStore.nodeStore, 100, 0, true, true);
        graphStore.addAllEdges(Arrays.asList(edges));

        int edgeCount = graphStore.getEdgeCount();

        graphStore.writeLock();
        Iterator<Node> nodeIterator = graphStore.getNodes().iterator();
        for (; nodeIterator.hasNext();) {
            NodeImpl n = (NodeImpl) nodeIterator.next();
            int degree = n.getDegree();

            boolean hasSelfLoop = graphStore.getEdge(n, n, 0) != null;
            if (hasSelfLoop) {
                degree--;
            }

            nodeIterator.remove();

            Assert.assertEquals(graphStore.getEdgeCount(), edgeCount - degree);
            edgeCount -= degree;
        }
        graphStore.writeUnlock();

        Assert.assertEquals(edgeCount, 0);
        Assert.assertEquals(graphStore.getNodeCount(), 0);
    }

    @Test
    public void testRemoveEdges() {
        GraphStore graphStore = GraphGenerator.generateSmallGraphStore();
        Edge[] edges = graphStore.getEdges().toArray();

        int edgeCount = edges.length;
        for (Edge e : edges) {
            boolean b = graphStore.removeEdge(e);
            Assert.assertTrue(b);

            Assert.assertEquals(graphStore.getEdgeCount(), --edgeCount);
        }
        Assert.assertEquals(graphStore.getEdgeCount(), 0);
    }

    private void testBasicStoreEquals(GraphStore graphStore, BasicGraphStore basicGraphStore) {
        BasicGraphStore.BasicEdgeStore basicEdgeStore = basicGraphStore.edgeStore;
        BasicGraphStore.BasicNodeStore basicNodeStore = basicGraphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeStore nodeStore = graphStore.nodeStore;

        //Nodes
        Assert.assertEquals(nodeStore.size(), basicNodeStore.size());
        int size = basicNodeStore.size();
        for (Node n : nodeStore) {
            Assert.assertTrue(basicNodeStore.containsId(n.getId()));
            size--;
        }
        Assert.assertEquals(size, 0);

        //Edges
        Assert.assertEquals(edgeStore.size(), basicEdgeStore.size());
        size = basicEdgeStore.size();
        for (Edge e : edgeStore) {
            Assert.assertTrue(basicEdgeStore.containsId(e.getId()));
            size--;
        }
        Assert.assertEquals(size, 0);

        //Type counts
        for (Int2IntMap.Entry typeCountEntry : basicEdgeStore.typeCountMap.int2IntEntrySet()) {
            int type = typeCountEntry.getIntKey();
            int count = typeCountEntry.getIntValue();
            Assert.assertEquals(edgeStore.size(type), count);
        }

        //Edges
        IntSet typeSet = new IntOpenHashSet();
        int edgeCount = 0;
        for (Edge basicEdge : basicEdgeStore) {
            Edge edge = edgeStore.get(basicEdge.getId());

            Assert.assertNotNull(edge);
            Assert.assertEquals(edge.getId(), basicEdge.getId());
            Assert.assertEquals(edge.getType(), basicEdge.getType());
            Assert.assertEquals(edge.getSource().getId(), basicEdge.getSource().getId());
            Assert.assertEquals(edge.getTarget().getId(), basicEdge.getTarget().getId());
            edgeCount++;

            typeSet.add(edge.getType());
        }

        Assert.assertEquals(edgeStore.size(), edgeCount);

        //Node and neighbors
        int nodeCount = 0;
        for (Node basicNode : basicNodeStore) {
            Node node = nodeStore.get(basicNode.getId());

            Assert.assertNotNull(node);

            testEdgeSets(basicGraphStore.getOutEdges(basicNode), graphStore.getOutEdges(node));
            testEdgeSets(basicGraphStore.getInEdges(basicNode), graphStore.getInEdges(node));
            testEdgeSets(basicGraphStore.getEdges(basicNode), graphStore.getEdges(node));

            for (int type : typeSet) {
                testEdgeSets(basicGraphStore.getOutEdges(basicNode, type), graphStore.getOutEdges(node, type));
                testEdgeSets(basicGraphStore.getInEdges(basicNode, type), graphStore.getInEdges(node, type));
                testEdgeSets(basicGraphStore.getEdges(basicNode, type), graphStore.getEdges(node, type));
            }

            testNodeSets(basicGraphStore.getNeighbors(basicNode), graphStore.getNeighbors(node));
            testNodeSets(basicGraphStore.getPredecessors(basicNode), graphStore.getPredecessors(node));
            testNodeSets(basicGraphStore.getSuccessors(basicNode), graphStore.getSuccessors(node));

            for (int type : typeSet) {
                testNodeSets(basicGraphStore.getNeighbors(basicNode, type), graphStore.getNeighbors(node, type));
                testNodeSets(basicGraphStore.getPredecessors(basicNode, type), graphStore.getPredecessors(node, type));
                testNodeSets(basicGraphStore.getSuccessors(basicNode), graphStore.getSuccessors(node, type));
            }

            nodeCount++;
        }
    }

    private void testEdgeSets(EdgeIterable e1, EdgeIterable e2) {
        Set s1 = new ObjectOpenHashSet();
        Set s2 = new ObjectOpenHashSet();

        for (Edge e : e1) {
            Assert.assertTrue(s1.add(e.getId()));
        }

        for (Edge e : e2) {
            Assert.assertTrue(s2.add(e.getId()));
        }

        Assert.assertEquals(s1.size(), s2.size());

        for (Object o : s1) {
            Assert.assertTrue(s2.remove(o));
        }
        Assert.assertEquals(s2.size(), 0);
    }

    private void testNodeSets(NodeIterable n1, NodeIterable n2) {
        Set s1 = new ObjectOpenHashSet();
        Set s2 = new ObjectOpenHashSet();

        for (Node e : n1) {
            Assert.assertTrue(s1.add(e.getId()));
        }

        for (Node e : n2) {
            Assert.assertTrue(s2.add(e.getId()));
        }

        Assert.assertEquals(s1.size(), s2.size());

        for (Object o : s1) {
            Assert.assertTrue(s2.remove(o));
        }
        Assert.assertEquals(s2.size(), 0);
    }
}
