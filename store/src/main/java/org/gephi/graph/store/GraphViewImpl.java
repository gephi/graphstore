package org.gephi.graph.store;

import cern.colt.bitvector.BitVector;
import java.util.Collection;
import java.util.Iterator;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedSubgraph;
import org.gephi.graph.store.EdgeStore.EdgeInOutIterator;

/**
 *
 * @author mbastian
 */
public final class GraphViewImpl implements GraphView {

    //Const
    public static final int DEFAULT_TYPE_COUNT = 1;
    //Data
    protected final GraphStore graphStore;
    protected final BitVector nodeBitVector;
    protected final BitVector edgeBitVector;
    protected int storeId;
    //Decorators
    private final GraphViewDecorator directedDecorator;
    private final GraphViewDecorator undirectedDecorator;
    //Stats
    protected int nodeCount;
    protected int edgeCount;
    private int[] typeCounts;
    private int[] mutualEdgeTypeCounts;
    private int mutualEdgesCount;

    public GraphViewImpl(final GraphStore store) {
        this.graphStore = store;
        this.nodeCount = store.getNodeCount();
        this.edgeCount = store.getEdgeCount();
        this.nodeBitVector = new BitVector(nodeCount);
        this.edgeBitVector = new BitVector(edgeCount);
        this.typeCounts = new int[DEFAULT_TYPE_COUNT];
        this.directedDecorator = new GraphViewDecorator(graphStore, this, false);
        this.undirectedDecorator = new GraphViewDecorator(graphStore, this, true);
    }

    protected DirectedSubgraph getDirectedGraph() {
        return directedDecorator;
    }

    protected UndirectedSubgraph getUndirectedGraph() {
        return undirectedDecorator;
    }

    public boolean addNode(final Node node) {
        checkNonNullNodeObject(node);

        NodeImpl nodeImpl = (NodeImpl) node;
        graphStore.nodeStore.checkNodeExists(nodeImpl);

        int id = nodeImpl.storeId;
        boolean isSet = nodeBitVector.get(id);
        if (!isSet) {
            nodeBitVector.set(id);
            nodeCount++;
            return true;
        }
        return false;
    }

    public boolean addAllNodes(final Collection<? extends Node> nodes) {
        if (!nodes.isEmpty()) {
            Iterator<? extends Node> nodeItr = nodes.iterator();
            boolean changed = false;
            while (nodeItr.hasNext()) {
                Node node = nodeItr.next();
                if (addNode(node)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean addEdge(final Edge edge) {
        checkNonNullEdgeObject(edge);

        EdgeImpl edgeImpl = (EdgeImpl) edge;
        graphStore.edgeStore.checkEdgeExists(edgeImpl);

        int id = edgeImpl.storeId;
        boolean isSet = edgeBitVector.get(id);
        if (!isSet) {
            edgeBitVector.set(id);
            edgeCount++;

            int type = edgeImpl.type;
            ensureTypeCountArrayCapacity(type);

            typeCounts[type]++;

            if (edgeImpl.isMutual() && edgeImpl.source.storeId > edgeImpl.target.storeId) {
                mutualEdgeTypeCounts[type]++;
                mutualEdgesCount++;
            }
            return true;
        }
        return false;
    }

    public boolean addAllEdges(final Collection<? extends Edge> edges) {
        if (!edges.isEmpty()) {
            Iterator<? extends Edge> edgeItr = edges.iterator();
            boolean changed = false;
            while (edgeItr.hasNext()) {
                Edge edge = edgeItr.next();
                if (addEdge(edge)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean removeNode(final Node node) {
        checkNonNullNodeObject(node);

        NodeImpl nodeImpl = (NodeImpl) node;
        graphStore.nodeStore.checkNodeExists(nodeImpl);

        int id = nodeImpl.storeId;
        boolean isSet = nodeBitVector.get(id);
        if (isSet) {
            nodeBitVector.clear(id);
            nodeCount--;

            //Remove edges
            EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                EdgeImpl edgeImpl = itr.next();

                int edgeId = edgeImpl.storeId;
                boolean edgeSet = edgeBitVector.get(edgeId);
                if (edgeSet) {
                    edgeBitVector.clear(edgeId);
                    edgeCount--;
                    typeCounts[edgeImpl.type]--;

                    if (edgeImpl.isMutual() && edgeImpl.source.storeId > edgeImpl.target.storeId) {
                        mutualEdgeTypeCounts[edgeImpl.type]--;
                        mutualEdgesCount--;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean removeNodeAll(final Collection<? extends Node> nodes) {
        if (!nodes.isEmpty()) {
            Iterator<? extends Node> nodeItr = nodes.iterator();
            boolean changed = false;
            while (nodeItr.hasNext()) {
                Node node = nodeItr.next();
                if (removeNode(node)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean removeEdge(final Edge edge) {
        checkNonNullEdgeObject(edge);

        EdgeImpl edgeImpl = (EdgeImpl) edge;
        graphStore.edgeStore.checkEdgeExists(edgeImpl);

        int id = edgeImpl.storeId;
        boolean isSet = edgeBitVector.get(id);
        if (isSet) {
            edgeBitVector.clear(id);
            edgeCount--;
            typeCounts[edgeImpl.type]--;

            if (edgeImpl.isMutual() && edgeImpl.source.storeId > edgeImpl.target.storeId) {
                mutualEdgeTypeCounts[edgeImpl.type]--;
                mutualEdgesCount--;
            }
            return true;
        }
        return false;
    }

    public boolean removeEdgeAll(final Collection<? extends Edge> edges) {
        if (!edges.isEmpty()) {
            Iterator<? extends Edge> edgeItr = edges.iterator();
            boolean changed = false;
            while (edgeItr.hasNext()) {
                Edge edge = edgeItr.next();
                if (removeEdge(edge)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public void clear() {
        nodeBitVector.clear();
        edgeBitVector.clear();
        nodeCount = 0;
        edgeCount = 0;
        typeCounts = new int[DEFAULT_TYPE_COUNT];
    }

    public void clearEdges() {
        edgeBitVector.clear();
        edgeCount = 0;
        typeCounts = new int[DEFAULT_TYPE_COUNT];
    }

    public boolean containsNode(final NodeImpl node) {
        return nodeBitVector.get(node.storeId);
    }

    public boolean containsEdge(final EdgeImpl edge) {
        return edgeBitVector.get(edge.storeId);
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getUndirectedEdgeCount() {
        return edgeCount - mutualEdgesCount;
    }

    public int getEdgeCount(int type) {
        if (type < 0 || type >= typeCounts.length) {
            throw new IllegalArgumentException("Incorrect type=" + type);
        }
        return typeCounts[type];
    }

    public int getUndirectedEdgeCount(int type) {
        if (type < 0 || type >= typeCounts.length) {
            throw new IllegalArgumentException("Incorrect type=" + type);
        }
        return typeCounts[type] - mutualEdgeTypeCounts[type];
    }
    
    @Override
    public GraphModelImpl getGraphModel() {
        return graphStore.graphModel;
    }

    private void ensureTypeCountArrayCapacity(int type) {
        if (type >= typeCounts.length) {
            int[] newArray = new int[type];
            System.arraycopy(typeCounts, 0, newArray, 0, typeCounts.length);
            typeCounts = newArray;

            int[] newMutualArray = new int[type];
            System.arraycopy(mutualEdgeTypeCounts, 0, newMutualArray, 0, mutualEdgeTypeCounts.length);
            mutualEdgeTypeCounts = newMutualArray;
        }
    }

    private void checkNonNullNodeObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof NodeImpl)) {
            throw new ClassCastException("Object must be a NodeImpl object");
        }
    }

    private void checkNonNullEdgeObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof EdgeImpl)) {
            throw new ClassCastException("Object must be a EdgeImpl object");
        }
    }
}
