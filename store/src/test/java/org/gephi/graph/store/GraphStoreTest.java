package org.gephi.graph.store;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.store.BasicGraphStore.BasicEdge;
import org.gephi.graph.store.BasicGraphStore.BasicNode;
import org.testng.Assert;

/**
 *
 * @author mbastian
 */
public class GraphStoreTest {

    private void testBasicStoreEquals(GraphStore graphStore, BasicGraphStore basicGraphStore) {
        BasicGraphStore.BasicEdgeStore basicEdgeStore = basicGraphStore.edgeStore;
        BasicGraphStore.BasicNodeStore basicNodeStore = basicGraphStore.nodeStore;
        EdgeStore edgeStore = graphStore.edgeStore;
        NodeStore nodeStore = graphStore.nodeStore;

        Assert.assertEquals(nodeStore.size(), basicNodeStore.size());
        int size = basicNodeStore.size();
        for (Node n : nodeStore) {
            Assert.assertTrue(basicNodeStore.containsId(n.getId()));
            size--;
        }
        Assert.assertEquals(size, 0);

        Assert.assertEquals(edgeStore.size(), basicEdgeStore.size());
        size = basicEdgeStore.size();
        for (Edge e : edgeStore) {
            Assert.assertTrue(basicEdgeStore.containsId(e.getId()));
            size--;
        }
        Assert.assertEquals(size, 0);

        for (Int2IntMap.Entry typeCountEntry : basicEdgeStore.typeCountMap.int2IntEntrySet()) {
            int type = typeCountEntry.getIntKey();
            int count = typeCountEntry.getIntValue();
            Assert.assertEquals(edgeStore.size(type), count);
        }

        Object2ObjectMap<Object, BasicNode> basicNodeMap = new Object2ObjectOpenHashMap<Object, BasicNode>();
        Object2ObjectMap<Object, NodeImpl> nodeMap = new Object2ObjectOpenHashMap<Object, NodeImpl>();
        Int2IntMap typeCounts = new Int2IntOpenHashMap();
        for (Edge basicEdge : basicEdgeStore) {
            BasicGraphStore.BasicNode source = (BasicGraphStore.BasicNode) basicEdge.getSource();
            BasicGraphStore.BasicNode target = (BasicGraphStore.BasicNode) basicEdge.getTarget();
            basicNodeMap.put(source.getId(), source);
            basicNodeMap.put(target.getId(), target);

            EdgeImpl edge = edgeStore.get(basicEdge.getId());
            nodeMap.put(edge.getSource().getId(), edge.getSource());
            nodeMap.put(edge.getTarget().getId(), edge.getTarget());

            Assert.assertNotNull(edge);
            Assert.assertEquals(edge.getId(), basicEdge.getId());
            Assert.assertEquals(edge.getType(), basicEdge.getType());

            typeCounts.put(basicEdge.getType(), typeCounts.get(basicEdge.getType()) + 1);
        }

        Assert.assertEquals(nodeMap.size(), basicNodeMap.size());

        for (BasicNode basicNode : basicNodeMap.values()) {
            NodeImpl node = nodeMap.get(basicNode.getId());
            for (Int2ObjectMap.Entry<Object2ObjectMap<Object, BasicGraphStore.BasicEdge>> entry : basicNode.outEdges.int2ObjectEntrySet()) {
                int type = entry.getIntKey();
                Object2ObjectMap<Object, BasicEdge> edges = entry.getValue();
                EdgeStore.EdgeTypeOutIterator itr = edgeStore.edgeOutIterator(node, type);
                int i = 0;
                for (; itr.hasNext(); i++) {
                    EdgeImpl edge = itr.next();
                    Assert.assertNotNull(edge);
                    Assert.assertEquals(edge.getId(), edges.get(edge.target.getId()).getId());
                }
                Assert.assertEquals(i, edges.size());
            }
            for (Int2ObjectMap.Entry<Object2ObjectMap<Object, BasicGraphStore.BasicEdge>> entry : basicNode.inEdges.int2ObjectEntrySet()) {
                int type = entry.getIntKey();
                Object2ObjectMap<Object, BasicEdge> edges = entry.getValue();
                EdgeStore.EdgeTypeInIterator itr = edgeStore.edgeInIterator(node, type);
                int i = 0;
                for (; itr.hasNext(); i++) {
                    EdgeImpl edge = itr.next();
                    Assert.assertNotNull(edge);
                    Assert.assertEquals(edge.getId(), edges.get(edge.source.getId()).getId());
                }
                Assert.assertEquals(i, edges.size());
            }
        }

        for (Int2IntMap.Entry typeEntry : typeCounts.int2IntEntrySet()) {
            Assert.assertEquals(edgeStore.size(typeEntry.getIntKey()), typeEntry.getIntValue());
        }
    }
}
