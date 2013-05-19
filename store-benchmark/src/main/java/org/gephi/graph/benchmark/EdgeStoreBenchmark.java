package org.gephi.graph.benchmark;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.gephi.graph.api.Edge;
import org.gephi.graph.store.EdgeImpl;
import org.gephi.graph.store.EdgeStore;
import org.gephi.graph.store.GraphLock;
import org.gephi.graph.store.NodeImpl;
import org.gephi.graph.store.NodeStore;

/**
 *
 * @author mbastian
 */
public class EdgeStoreBenchmark {

    private static int NODES = 50000;
    private static int EDGES = 500000;
    private Object object;
    private EdgeImpl edgeObject;
    private NodeImpl nodeObject;

    public Runnable pushEdgeStore() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EdgeStore edgeStore = new EdgeStore();
                for (EdgeImpl e : generateEdgeList()) {
                    edgeStore.add(e);
                }
                object = edgeStore;
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStore() {
        final EdgeStore edgeStore = new EdgeStore();
        final LongSet edgeSet = new LongOpenHashSet();
        for (EdgeImpl e : generateEdgeList()) {
            edgeStore.add(e);
            edgeSet.add(e.getLongId());
        }
        object = edgeStore;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Iterator<Edge> itr = edgeStore.iterator();
                for (; itr.hasNext();) {
                    edgeObject = (EdgeImpl) itr.next();
                }
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStoreWithLocking() {
        final EdgeStore edgeStore = new EdgeStore(null, new GraphLock(), null, null);
        final LongSet edgeSet = new LongOpenHashSet();
        for (EdgeImpl e : generateEdgeList()) {
            edgeStore.add(e);
            edgeSet.add(e.getLongId());
        }
        object = edgeStore;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Iterator<Edge> itr = edgeStore.iterator();
                for (; itr.hasNext();) {
                    edgeObject = (EdgeImpl) itr.next();
                }
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStoreNeighborsOut() {
        final EdgeStore edgeStore = new EdgeStore();
        List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>(generateEdgeList());
        ObjectSet<NodeImpl> nodeSet = new ObjectOpenHashSet<NodeImpl>();
        for (EdgeImpl e : edgeList) {
            nodeSet.add(e.getSource());
            nodeSet.add(e.getTarget());
            edgeStore.add(e);
        }
        final NodeImpl[] nodes = nodeSet.toArray(new NodeImpl[0]);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (NodeImpl node : nodes) {
                    Iterator<Edge> itr = edgeStore.edgeOutIterator(node);
                    for (; itr.hasNext();) {
                        edgeObject = (EdgeImpl) itr.next();
                    }
                }
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStoreNeighborsInOut() {
        final EdgeStore edgeStore = new EdgeStore();
        List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>(generateEdgeList());
        ObjectSet<NodeImpl> nodeSet = new ObjectOpenHashSet<NodeImpl>();
        for (EdgeImpl e : edgeList) {
            nodeSet.add(e.getSource());
            nodeSet.add(e.getTarget());
            edgeStore.add(e);
        }
        final NodeImpl[] nodes = nodeSet.toArray(new NodeImpl[0]);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (NodeImpl node : nodes) {
                    Iterator<Edge> itr = edgeStore.edgeIterator(node);
                    for (; itr.hasNext();) {
                        edgeObject = (EdgeImpl) itr.next();
                    }
                }
            }
        };
        return runnable;
    }

    public Runnable resetEdgeStore() {
        final EdgeStore edgeStore = new EdgeStore();
        final List<EdgeImpl> edgeList = new LinkedList<EdgeImpl>(generateEdgeList());
        for (EdgeImpl e : edgeList) {
            edgeStore.add(e);
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (EdgeImpl e : edgeList) {
                    edgeStore.remove(e);
                }
                for (EdgeImpl e : edgeList) {
                    edgeStore.add(e);
                }
            }
        };
        return runnable;
    }

    private List<EdgeImpl> generateEdgeList() {
        final NodeStore nodeStore = new NodeStore();
        for (int i = 0; i < NODES; i++) {
            NodeImpl n = new NodeImpl(String.valueOf(i));
            nodeStore.add(n);
        }
        final List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>();
        LongSet idSet = new LongOpenHashSet();
        Random r = new Random(123);
        int edgeCount = 0;
        while (edgeCount < EDGES) {
            int sourceId = r.nextInt(NODES);
            int targetId = r.nextInt(NODES);
            NodeImpl source = nodeStore.get(sourceId);
            NodeImpl target = nodeStore.get(targetId);
            EdgeImpl edge = new EdgeImpl(String.valueOf(edgeCount), source, target, 0, 1.0, true);
            if (source != target && !idSet.contains(edge.getLongId())) {
                edgeList.add(edge);
                edgeCount++;
                idSet.add(edge.getLongId());
            }
        }
        return edgeList;
    }
}
