package org.gephi.graph.store;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
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
