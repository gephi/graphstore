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
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedSubgraph;
import org.gephi.graph.impl.EdgeStore.EdgeInOutIterator;

public class GraphViewImpl implements GraphView {

    // Data
    protected final GraphStore graphStore;
    protected final boolean nodeView;
    protected final boolean edgeView;
    protected final GraphAttributesImpl attributes;
    protected BitSet nodeBitVector;
    protected BitSet edgeBitVector;
    protected int storeId;
    // Version
    protected final GraphVersion version;
    protected final List<GraphObserverImpl> observers;
    // Decorators
    protected final GraphViewDecorator directedDecorator;
    protected final GraphViewDecorator undirectedDecorator;
    // Stats
    protected int nodeCount;
    protected int edgeCount;
    protected int[] typeCounts;
    protected int[] mutualEdgeTypeCounts;
    protected int mutualEdgesCount;
    // Dynamic
    protected Interval interval;

    public GraphViewImpl(final GraphStore store, boolean nodes, boolean edges) {
        this.graphStore = store;
        this.nodeView = nodes;
        this.edgeView = edges;
        this.attributes = new GraphAttributesImpl();
        if (nodes) {
            this.nodeBitVector = new BitSet(store.nodeStore.maxStoreId());
        } else {
            this.nodeBitVector = null;
        }
        this.edgeBitVector = new BitSet(store.edgeStore.maxStoreId());
        this.typeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        this.mutualEdgeTypeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];

        this.directedDecorator = new GraphViewDecorator(graphStore, this, false);
        this.undirectedDecorator = new GraphViewDecorator(graphStore, this, true);
        this.version = graphStore.version != null ? new GraphVersion(directedDecorator) : null;
        this.observers = graphStore.version != null ? new ArrayList<>() : null;
        this.interval = Interval.INFINITY_INTERVAL;
    }

    public GraphViewImpl(final GraphViewImpl view, boolean nodes, boolean edges) {
        this.graphStore = view.graphStore;
        this.nodeView = nodes;
        this.edgeView = edges;
        this.attributes = new GraphAttributesImpl();
        if (nodes) {
            this.nodeBitVector = (BitSet) view.nodeBitVector.clone();
            this.nodeCount = view.nodeCount;
        } else {
            this.nodeBitVector = null;
        }
        this.edgeCount = view.edgeCount;
        this.edgeBitVector = (BitSet) view.edgeBitVector.clone();
        this.typeCounts = new int[view.typeCounts.length];
        System.arraycopy(view.typeCounts, 0, typeCounts, 0, view.typeCounts.length);
        this.mutualEdgeTypeCounts = new int[view.mutualEdgeTypeCounts.length];
        System.arraycopy(view.mutualEdgeTypeCounts, 0, mutualEdgeTypeCounts, 0, view.mutualEdgeTypeCounts.length);
        this.mutualEdgesCount = view.mutualEdgesCount;
        this.directedDecorator = new GraphViewDecorator(graphStore, this, false);
        this.undirectedDecorator = new GraphViewDecorator(graphStore, this, true);
        this.version = graphStore.version != null ? new GraphVersion(directedDecorator) : null;
        this.observers = graphStore.version != null ? new ArrayList<>() : null;
        this.interval = view.interval;
    }

    protected DirectedSubgraph getDirectedGraph() {
        return directedDecorator;
    }

    protected UndirectedSubgraph getUndirectedGraph() {
        return undirectedDecorator;
    }

    public boolean addNode(final Node node) {
        checkNodeView();

        NodeImpl nodeImpl = (NodeImpl) node;
        graphStore.nodeStore.checkNodeExists(nodeImpl);

        int id = nodeImpl.storeId;
        boolean isSet = nodeBitVector.get(id);
        if (!isSet) {
            nodeBitVector.set(id);
            nodeCount++;
            incrementNodeVersion();

            IndexStore<Node> indexStore = graphStore.nodeTable.store.indexStore;
            if (indexStore != null) {
                indexStore.indexInView(nodeImpl, this);
            }
            TimeIndexStore timeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (timeIndexStore != null) {
                timeIndexStore.indexInView(nodeImpl, this);
            }

            if (nodeView && !edgeView) {
                // Add edges
                EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node, false);
                while (itr.hasNext()) {
                    EdgeImpl edge = itr.next();
                    NodeImpl opposite = edge.source == nodeImpl ? edge.target : edge.source;
                    if (nodeBitVector.get(opposite.getStoreId())) {
                        // Add edge
                        int edgeid = edge.storeId;
                        boolean edgeisSet = edgeBitVector.get(edgeid);
                        if (!edgeisSet) {
                            addEdge(edge);
                        }
                        // End
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean addAllNodes(final Collection<? extends Node> nodes) {
        checkNodeView();

        if (!nodes.isEmpty()) {
            Iterator<? extends Node> nodeItr = nodes.iterator();
            boolean changed = false;
            while (nodeItr.hasNext()) {
                Node node = nodeItr.next();
                checkValidNodeObject(node);
                if (addNode(node)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean addEdge(final Edge edge) {
        checkEdgeView();

        EdgeImpl edgeImpl = (EdgeImpl) edge;
        graphStore.edgeStore.checkEdgeExists(edgeImpl);

        int id = edgeImpl.storeId;
        boolean isSet = edgeBitVector.get(id);
        if (!isSet) {
            checkIncidentNodesExists(edgeImpl);

            addEdge(edgeImpl);
            return true;
        }
        return false;
    }

    public boolean addAllEdges(final Collection<? extends Edge> edges) {
        checkEdgeView();

        if (!edges.isEmpty()) {
            Iterator<? extends Edge> edgeItr = edges.iterator();
            boolean changed = false;
            while (edgeItr.hasNext()) {
                Edge edge = edgeItr.next();
                checkValidEdgeObject(edge);
                if (addEdge(edge)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean removeNode(final Node node) {
        checkNodeView();

        NodeImpl nodeImpl = (NodeImpl) node;
        graphStore.nodeStore.checkNodeExists(nodeImpl);

        int id = nodeImpl.storeId;
        boolean isSet = nodeBitVector.get(id);
        if (isSet) {
            nodeBitVector.clear(id);
            nodeCount--;
            incrementNodeVersion();

            IndexStore<Node> indexStore = graphStore.nodeTable.store.indexStore;
            if (indexStore != null) {
                indexStore.clearInView(nodeImpl, this);
            }
            TimeIndexStore timeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (timeIndexStore != null) {
                timeIndexStore.clearInView(nodeImpl, this);
            }

            // Remove edges
            EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node, false);
            while (itr.hasNext()) {
                EdgeImpl edgeImpl = itr.next();

                int edgeId = edgeImpl.storeId;
                boolean edgeSet = edgeBitVector.get(edgeId);
                if (edgeSet) {
                    removeEdge(edgeImpl);
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
                checkValidNodeObject(node);
                if (removeNode(node)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean retainNodes(final Collection<? extends Node> c) {
        if (nodeView) {
            if (!c.isEmpty()) {
                // Build BitSet of nodes to retain
                BitSet retainSet = new BitSet(graphStore.nodeStore.maxStoreId());
                for (Node o : c) {
                    checkValidNodeObject(o);
                    retainSet.set(o.getStoreId());
                }

                // Find nodes to remove: nodes in this view but NOT in retain set
                // This is equivalent to: nodeBitVector AND NOT retainSet
                BitSet nodesToRemove = (BitSet) nodeBitVector.clone();
                nodesToRemove.andNot(retainSet);

                if (nodesToRemove.isEmpty()) {
                    return false;
                }

                // Bulk remove nodes
                bulkRemoveNodes(nodesToRemove);
                return true;
            } else if (nodeCount != 0) {
                clear();
                return true;
            }
        }
        return false;
    }

    public boolean retainEdges(final Collection<? extends Edge> c) {
        if (edgeView) {
            if (!c.isEmpty()) {
                // Build BitSet of edges to retain
                BitSet retainSet = new BitSet(graphStore.edgeStore.maxStoreId());
                for (Edge o : c) {
                    checkValidEdgeObject(o);
                    retainSet.set(o.getStoreId());
                }

                // Find edges to remove: edges in this view but NOT in retain set
                // This is equivalent to: edgeBitVector AND NOT retainSet
                BitSet edgesToRemove = (BitSet) edgeBitVector.clone();
                edgesToRemove.andNot(retainSet);

                if (edgesToRemove.isEmpty()) {
                    return false;
                }

                // Bulk remove edges
                bulkRemoveEdges(edgesToRemove);
                return true;
            } else if (edgeCount != 0) {
                clearEdges();
                return true;
            }
        }
        return false;
    }

    public boolean removeEdge(final Edge edge) {
        checkEdgeView();

        EdgeImpl edgeImpl = (EdgeImpl) edge;
        graphStore.edgeStore.checkEdgeExists(edgeImpl);

        int id = edgeImpl.storeId;
        boolean isSet = edgeBitVector.get(id);
        if (isSet) {
            removeEdge(edgeImpl);

            return true;
        }
        return false;
    }

    public boolean removeEdgeAll(final Collection<? extends Edge> edges) {
        checkEdgeView();

        if (!edges.isEmpty()) {
            Iterator<? extends Edge> edgeItr = edges.iterator();
            boolean changed = false;
            while (edgeItr.hasNext()) {
                Edge edge = edgeItr.next();
                checkValidEdgeObject(edge);
                if (removeEdge(edge)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public void clear() {
        if (nodeCount > 0) {
            incrementNodeVersion();
        }
        if (edgeCount > 0) {
            incrementEdgeVersion();
        }
        if (nodeView) {
            nodeBitVector.clear();
        }
        edgeBitVector.clear();
        nodeCount = 0;
        edgeCount = 0;
        typeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        mutualEdgeTypeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        mutualEdgesCount = 0;

        if (nodeView) {
            IndexStore<Node> nodeIndexStore = graphStore.nodeTable.store.indexStore;
            if (nodeIndexStore != null) {
                nodeIndexStore.clear(this);
            }
            TimeIndexStore nodeTimeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (nodeTimeIndexStore != null) {
                nodeTimeIndexStore.clear(this);
            }
        }
        IndexStore<Edge> edgeIndexStore = graphStore.edgeTable.store.indexStore;
        if (edgeIndexStore != null) {
            edgeIndexStore.clear(this);
        }
        TimeIndexStore edgeTimeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (edgeTimeIndexStore != null) {
            edgeTimeIndexStore.clear(this);
        }
    }

    public void clearEdges() {
        if (edgeCount > 0) {
            incrementEdgeVersion();
        }
        edgeBitVector.clear();
        edgeCount = 0;
        typeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        mutualEdgeTypeCounts = new int[GraphStoreConfiguration.VIEW_DEFAULT_TYPE_COUNT];
        mutualEdgesCount = 0;

        IndexStore<Edge> edgeIndexStore = graphStore.edgeTable.store.indexStore;
        if (edgeIndexStore != null) {
            edgeIndexStore.clear(this);
        }
        TimeIndexStore edgeTimeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (edgeTimeIndexStore != null) {
            edgeTimeIndexStore.clear(this);
        }
    }

    public void fill() {
        if (nodeView) {
            nodeBitVector.set(0, graphStore.nodeStore.maxStoreId(), true);
            this.nodeCount = graphStore.nodeStore.size();
        }
        edgeBitVector.set(0, graphStore.edgeStore.maxStoreId());

        this.edgeCount = graphStore.edgeStore.size();
        int typeLength = graphStore.edgeStore.longDictionary.length;
        this.typeCounts = new int[typeLength];
        for (int i = 0; i < typeLength; i++) {
            int count = graphStore.edgeStore.longDictionary[i].size();
            this.typeCounts[i] = count;
        }
        this.mutualEdgeTypeCounts = new int[graphStore.edgeStore.mutualEdgesTypeSize.length];
        System.arraycopy(graphStore.edgeStore.mutualEdgesTypeSize, 0, this.mutualEdgeTypeCounts, 0, this.mutualEdgeTypeCounts.length);
        this.mutualEdgesCount = graphStore.edgeStore.mutualEdgesSize;

        if (edgeCount > 0) {
            incrementEdgeVersion();
        }
        if (nodeCount > 0) {
            incrementNodeVersion();
        }

        if (nodeView) {
            IndexStore<Node> nodeIndexStore = graphStore.nodeTable.store.indexStore;
            if (nodeIndexStore != null) {
                nodeIndexStore.indexView(directedDecorator);
            }
            TimeIndexStore nodeTimeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (nodeTimeIndexStore != null) {
                nodeTimeIndexStore.indexView(directedDecorator);
            }
        }
        IndexStore<Edge> edgeIndexStore = graphStore.edgeTable.store.indexStore;
        if (edgeIndexStore != null) {
            edgeIndexStore.indexView(directedDecorator);
        }
        TimeIndexStore edgeTimeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (edgeTimeIndexStore != null) {
            edgeTimeIndexStore.indexView(directedDecorator);
        }
    }

    public boolean containsNode(final Node node) {
        if (!nodeView) {
            return true;
        }
        return nodeBitVector.get(node.getStoreId());
    }

    public boolean containsEdge(final Edge edge) {
        return edgeBitVector.get(edge.getStoreId());
    }

    public void intersection(final GraphViewImpl otherView) {
        BitSet nodeOtherBitVector = otherView.nodeBitVector;
        BitSet edgeOtherBitVector = otherView.edgeBitVector;

        if (nodeView) {
            // Find nodes to remove: nodes in this view but NOT in other view
            BitSet nodesToRemove = (BitSet) nodeBitVector.clone();
            nodesToRemove.andNot(nodeOtherBitVector);

            if (!nodesToRemove.isEmpty()) {
                // Bulk remove nodes
                bulkRemoveNodes(nodesToRemove);
            }
        }

        if (edgeView) {
            // Find edges to remove: edges in this view but NOT in other view
            BitSet edgesToRemove = (BitSet) edgeBitVector.clone();
            edgesToRemove.andNot(edgeOtherBitVector);

            if (!edgesToRemove.isEmpty()) {
                // Bulk remove edges
                bulkRemoveEdges(edgesToRemove);
            }
        }
    }

    public void union(final GraphViewImpl otherView) {
        BitSet nodeOtherBitVector = otherView.nodeBitVector;
        BitSet edgeOtherBitVector = otherView.edgeBitVector;

        if (nodeView) {
            // Find nodes to add: nodes in other view but NOT in this view
            BitSet nodesToAdd = (BitSet) nodeOtherBitVector.clone();
            nodesToAdd.andNot(nodeBitVector);

            if (!nodesToAdd.isEmpty()) {
                // Bulk add nodes
                bulkAddNodes(nodesToAdd);
            }
        }

        if (edgeView) {
            // Find edges to add: edges in other view but NOT in this view
            BitSet edgesToAdd = (BitSet) edgeOtherBitVector.clone();
            edgesToAdd.andNot(edgeBitVector);

            if (!edgesToAdd.isEmpty()) {
                // Bulk add edges
                bulkAddEdges(edgesToAdd);
            }
        }
    }

    public void not() {
        // Flip node bits if this is a node view
        if (nodeView) {
            nodeBitVector.flip(0, graphStore.nodeStore.maxStoreId());
            this.nodeCount = graphStore.nodeStore.size() - this.nodeCount;
            incrementNodeVersion();
        }

        // Flip edge bits
        edgeBitVector.flip(0, graphStore.edgeStore.maxStoreId());

        // Update edge counts by subtracting from totals
        this.edgeCount = graphStore.edgeStore.size() - this.edgeCount;

        // Ensure type count arrays are sized to match the store
        int storeTypeLength = graphStore.edgeStore.longDictionary.length;
        if (typeCounts.length < storeTypeLength) {
            int[] newTypeCounts = new int[storeTypeLength];
            System.arraycopy(typeCounts, 0, newTypeCounts, 0, typeCounts.length);
            typeCounts = newTypeCounts;

            int[] newMutualCounts = new int[storeTypeLength];
            System.arraycopy(mutualEdgeTypeCounts, 0, newMutualCounts, 0, mutualEdgeTypeCounts.length);
            mutualEdgeTypeCounts = newMutualCounts;
        }

        // Invert all type counts (including types that weren't in the view before)
        for (int i = 0; i < storeTypeLength; i++) {
            this.typeCounts[i] = graphStore.edgeStore.longDictionary[i].size() - this.typeCounts[i];
        }
        for (int i = 0; i < graphStore.edgeStore.mutualEdgesTypeSize.length; i++) {
            this.mutualEdgeTypeCounts[i] = graphStore.edgeStore.mutualEdgesTypeSize[i] - this.mutualEdgeTypeCounts[i];
        }
        this.mutualEdgesCount = graphStore.edgeStore.mutualEdgesSize - this.mutualEdgesCount;

        incrementEdgeVersion();

        // If node view is enabled, remove edges with invalid endpoints
        // Optimization: Only iterate through edges that are NOW in the view (after
        // flip)
        // instead of all edges in the store
        if (nodeView) {
            BitSet edgesToRemove = new BitSet();

            // Iterate only over edges that are set in the view (much faster for sparse
            // views)
            for (int edgeId = edgeBitVector.nextSetBit(0); edgeId >= 0; edgeId = edgeBitVector.nextSetBit(edgeId + 1)) {
                EdgeImpl edge = getEdge(edgeId);
                // Check if both endpoints are in the node view
                if (!nodeBitVector.get(edge.source.storeId) || !nodeBitVector.get(edge.target.storeId)) {
                    edgesToRemove.set(edgeId);
                }
            }

            // Bulk remove invalid edges
            if (!edgesToRemove.isEmpty()) {
                bulkRemoveEdgesForNot(edgesToRemove);
            }
        }

        // Rebuild indexes (necessary for NOT operation as the view content has
        // completely changed)
        if (nodeView) {
            IndexStore<Node> nodeIndexStore = graphStore.nodeTable.store.indexStore;
            if (nodeIndexStore != null) {
                nodeIndexStore.clear(directedDecorator.view);
                nodeIndexStore.indexView(directedDecorator);
            }
            TimeIndexStore nodeTimeIndexStore = graphStore.timeStore.nodeIndexStore;
            if (nodeTimeIndexStore != null) {
                nodeTimeIndexStore.clear(directedDecorator.view);
                nodeTimeIndexStore.indexView(directedDecorator);
            }
        }
        IndexStore<Edge> edgeIndexStore = graphStore.edgeTable.store.indexStore;
        if (edgeIndexStore != null) {
            edgeIndexStore.clear(directedDecorator.view);
            edgeIndexStore.indexView(directedDecorator);
        }
        TimeIndexStore edgeTimeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (edgeTimeIndexStore != null) {
            edgeTimeIndexStore.clear(directedDecorator.view);
            edgeTimeIndexStore.indexView(directedDecorator);
        }
    }

    public void addEdgeInNodeView(EdgeImpl edge) {
        if (nodeBitVector.get(edge.source.getStoreId()) && nodeBitVector.get(edge.target.getStoreId())) {
            incrementEdgeVersion();

            addEdge(edge);
        }
    }

    /**
     * Bulk remove nodes from the view. This is more efficient than removing nodes
     * one by one as it batches index updates and increments version only once.
     */
    private void bulkRemoveNodes(BitSet nodesToRemove) {
        // First pass: collect all edges to remove (incident to removed nodes)
        BitSet edgesToRemove = new BitSet();
        for (int nodeId = nodesToRemove.nextSetBit(0); nodeId >= 0; nodeId = nodesToRemove.nextSetBit(nodeId + 1)) {
            NodeImpl node = getNode(nodeId);
            EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node, false);
            while (itr.hasNext()) {
                EdgeImpl edge = itr.next();
                int edgeId = edge.storeId;
                if (edgeBitVector.get(edgeId)) {
                    edgesToRemove.set(edgeId);
                }
            }
        }

        // Remove edges in bulk
        if (!edgesToRemove.isEmpty()) {
            bulkRemoveEdges(edgesToRemove);
        }

        // Update node bit vector
        int removedCount = nodesToRemove.cardinality();
        nodeBitVector.andNot(nodesToRemove);
        nodeCount -= removedCount;
        incrementNodeVersion();

        // Bulk update indexes
        IndexStore<Node> indexStore = graphStore.nodeTable.store.indexStore;
        TimeIndexStore timeIndexStore = graphStore.timeStore.nodeIndexStore;

        if (indexStore != null || timeIndexStore != null) {
            for (int i = nodesToRemove.nextSetBit(0); i >= 0; i = nodesToRemove.nextSetBit(i + 1)) {
                NodeImpl node = getNode(i);
                if (indexStore != null) {
                    indexStore.clearInView(node, this);
                }
                if (timeIndexStore != null) {
                    timeIndexStore.clearInView(node, this);
                }
            }
        }
    }

    /**
     * Bulk add nodes to the view. This is more efficient than adding nodes one by
     * one as it batches index updates and increments version only once.
     */
    private void bulkAddNodes(BitSet nodesToAdd) {
        // Update node bit vector
        int addedCount = nodesToAdd.cardinality();
        nodeBitVector.or(nodesToAdd);
        nodeCount += addedCount;
        incrementNodeVersion();

        // Bulk update indexes
        IndexStore<Node> indexStore = graphStore.nodeTable.store.indexStore;
        TimeIndexStore timeIndexStore = graphStore.timeStore.nodeIndexStore;

        if (indexStore != null || timeIndexStore != null) {
            for (int i = nodesToAdd.nextSetBit(0); i >= 0; i = nodesToAdd.nextSetBit(i + 1)) {
                NodeImpl node = getNode(i);
                if (indexStore != null) {
                    indexStore.indexInView(node, this);
                }
                if (timeIndexStore != null) {
                    timeIndexStore.indexInView(node, this);
                }
            }
        }

        // If nodeView && !edgeView, add edges between newly added nodes and existing
        // nodes
        if (nodeView && !edgeView) {
            BitSet edgesToAdd = new BitSet();
            for (int nodeId = nodesToAdd.nextSetBit(0); nodeId >= 0; nodeId = nodesToAdd.nextSetBit(nodeId + 1)) {
                NodeImpl node = getNode(nodeId);
                EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node, false);
                while (itr.hasNext()) {
                    EdgeImpl edge = itr.next();
                    NodeImpl opposite = edge.source == node ? edge.target : edge.source;
                    // Check if opposite node is in view and edge is not already in view
                    if (nodeBitVector.get(opposite.storeId) && !edgeBitVector.get(edge.storeId)) {
                        edgesToAdd.set(edge.storeId);
                    }
                }
            }

            if (!edgesToAdd.isEmpty()) {
                bulkAddEdges(edgesToAdd);
            }
        }
    }

    /**
     * Bulk remove edges from the view. This is more efficient than removing edges
     * one by one as it updates stats in bulk and increments version only once.
     */
    private void bulkRemoveEdges(BitSet edgesToRemove) {
        // Update edge bit vector
        int removedCount = edgesToRemove.cardinality();
        edgeBitVector.andNot(edgesToRemove);
        edgeCount -= removedCount;

        // Update type counts and mutual edge counts
        for (int i = edgesToRemove.nextSetBit(0); i >= 0; i = edgesToRemove.nextSetBit(i + 1)) {
            EdgeImpl edge = getEdge(i);
            int type = edge.type;
            ensureTypeCountArrayCapacity(type);
            typeCounts[type]--;

            if (edge.isMutual() && !edge.isSelfLoop()) {
                EdgeImpl reverseEdge = graphStore.edgeStore.get(edge.target, edge.source, edge.type, false);
                if (reverseEdge != null && containsEdge(reverseEdge)) {
                    mutualEdgeTypeCounts[type]--;
                    mutualEdgesCount--;
                }
            }
        }

        incrementEdgeVersion();

        // Bulk update indexes
        IndexStore<Edge> indexStore = graphStore.edgeTable.store.indexStore;
        TimeIndexStore timeIndexStore = graphStore.timeStore.edgeIndexStore;

        if (indexStore != null || timeIndexStore != null) {
            for (int i = edgesToRemove.nextSetBit(0); i >= 0; i = edgesToRemove.nextSetBit(i + 1)) {
                EdgeImpl edge = getEdge(i);
                if (indexStore != null) {
                    indexStore.clearInView(edge, this);
                }
                if (timeIndexStore != null) {
                    timeIndexStore.clearInView(edge, this);
                }
            }
        }
    }

    /**
     * Bulk add edges to the view. This is more efficient than adding edges one by
     * one as it updates stats in bulk and increments version only once.
     */
    private void bulkAddEdges(BitSet edgesToAdd) {
        // Update edge bit vector
        int addedCount = edgesToAdd.cardinality();
        edgeBitVector.or(edgesToAdd);
        edgeCount += addedCount;

        // Update type counts and mutual edge counts
        for (int i = edgesToAdd.nextSetBit(0); i >= 0; i = edgesToAdd.nextSetBit(i + 1)) {
            EdgeImpl edge = getEdge(i);
            int type = edge.type;
            ensureTypeCountArrayCapacity(type);
            typeCounts[type]++;

            if (edge.isMutual() && !edge.isSelfLoop()) {
                EdgeImpl reverseEdge = graphStore.edgeStore.get(edge.target, edge.source, edge.type, false);
                if (reverseEdge != null && containsEdge(reverseEdge)) {
                    mutualEdgeTypeCounts[type]++;
                    mutualEdgesCount++;
                }
            }
        }

        incrementEdgeVersion();

        // Bulk update indexes
        IndexStore<Edge> indexStore = graphStore.edgeTable.store.indexStore;
        TimeIndexStore timeIndexStore = graphStore.timeStore.edgeIndexStore;

        if (indexStore != null || timeIndexStore != null) {
            for (int i = edgesToAdd.nextSetBit(0); i >= 0; i = edgesToAdd.nextSetBit(i + 1)) {
                EdgeImpl edge = getEdge(i);
                if (indexStore != null) {
                    indexStore.indexInView(edge, this);
                }
                if (timeIndexStore != null) {
                    timeIndexStore.indexInView(edge, this);
                }
            }
        }
    }

    /**
     * Special bulk remove for the not() operation. This updates the bit vector and
     * stats but does NOT increment version or update indexes (those are handled
     * separately in not()).
     */
    private void bulkRemoveEdgesForNot(BitSet edgesToRemove) {
        // Update edge bit vector
        int removedCount = edgesToRemove.cardinality();
        edgeBitVector.andNot(edgesToRemove);
        edgeCount -= removedCount;

        // Update type counts and mutual edge counts
        for (int i = edgesToRemove.nextSetBit(0); i >= 0; i = edgesToRemove.nextSetBit(i + 1)) {
            EdgeImpl edge = getEdge(i);
            int type = edge.type;
            ensureTypeCountArrayCapacity(type);
            typeCounts[type]--;

            if (edge.isMutual() && !edge.isSelfLoop()) {
                EdgeImpl reverseEdge = graphStore.edgeStore.get(edge.target, edge.source, edge.type, false);
                if (reverseEdge != null && containsEdge(reverseEdge)) {
                    mutualEdgeTypeCounts[type]--;
                    mutualEdgesCount--;
                }
            }
        }
        // Note: Version increment and index updates are handled by the caller (not()
        // method)
    }

    public int getNodeCount() {
        if (nodeView) {
            return nodeCount;
        }
        return graphStore.nodeStore.size();
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

    @Override
    public boolean isMainView() {
        return false;
    }

    @Override
    public boolean isNodeView() {
        return nodeView;
    }

    @Override
    public boolean isEdgeView() {
        return edgeView;
    }

    public void setTimeInterval(Interval interval) {
        if (interval == null) {
            interval = Interval.INFINITY_INTERVAL;
        }
        this.interval = interval;
    }

    @Override
    public Interval getTimeInterval() {
        return interval;
    }

    @Override
    public boolean isDestroyed() {
        return storeId == GraphViewStore.NULL_VIEW;
    }

    protected int getVersion() {
        return Objects.hash(version.nodeVersion, version.edgeVersion);
    }

    protected GraphObserverImpl createGraphObserver(Graph graph, boolean withDiff) {
        if (observers != null) {
            GraphObserverImpl observer = new GraphObserverImpl(graphStore, version, graph, withDiff);
            observers.add(observer);

            return observer;
        }
        return null;
    }

    protected void destroyGraphObserver(GraphObserverImpl observer) {
        if (observers != null) {
            observers.remove(observer);
            observer.destroyObserver();
        }
    }

    protected void destroyAllObservers() {
        if (observers != null) {
            for (GraphObserverImpl graphObserverImpl : observers) {
                graphObserverImpl.destroyObserver();
            }
            observers.clear();
        }
    }

    protected void ensureNodeVectorSize(NodeImpl node) {
        // BitSet automatically grows as needed, no manual resizing required
    }

    protected void ensureEdgeVectorSize(EdgeImpl edge) {
        // BitSet automatically grows as needed, no manual resizing required
    }

    protected void setEdgeType(EdgeImpl edgeImpl, int oldType, boolean wasMutual) {
        ensureTypeCountArrayCapacity(edgeImpl.type);
        typeCounts[oldType]--;
        typeCounts[edgeImpl.type]++;

        if (!edgeImpl.isSelfLoop()) {
            if (wasMutual && containsEdge(graphStore.edgeStore.get(edgeImpl.target, edgeImpl.source, oldType, false))) {
                mutualEdgeTypeCounts[oldType]--;
                mutualEdgesCount--;
            }

            if (edgeImpl.isMutual() && containsEdge(graphStore.edgeStore
                    .get(edgeImpl.target, edgeImpl.source, edgeImpl.type, false))) {
                mutualEdgeTypeCounts[edgeImpl.type]++;
                mutualEdgesCount++;
            }
        }
    }

    private void addEdge(EdgeImpl edgeImpl) {
        incrementEdgeVersion();

        edgeBitVector.set(edgeImpl.storeId);
        edgeCount++;

        int type = edgeImpl.type;
        ensureTypeCountArrayCapacity(type);

        typeCounts[type]++;

        if (edgeImpl.isMutual() && !edgeImpl.isSelfLoop() && containsEdge(graphStore.edgeStore
                .get(edgeImpl.target, edgeImpl.source, edgeImpl.type, false))) {
            mutualEdgeTypeCounts[type]++;
            mutualEdgesCount++;
        }

        IndexStore<Edge> indexStore = graphStore.edgeTable.store.indexStore;
        if (indexStore != null) {
            indexStore.indexInView(edgeImpl, this);
        }
        TimeIndexStore timeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (timeIndexStore != null) {
            timeIndexStore.indexInView(edgeImpl, this);
        }
    }

    private void removeEdge(EdgeImpl edgeImpl) {
        incrementEdgeVersion();

        edgeBitVector.clear(edgeImpl.storeId);
        edgeCount--;
        typeCounts[edgeImpl.type]--;

        if (edgeImpl.isMutual() && !edgeImpl.isSelfLoop() && containsEdge(graphStore.edgeStore
                .get(edgeImpl.target, edgeImpl.source, edgeImpl.type, false))) {
            mutualEdgeTypeCounts[edgeImpl.type]--;
            mutualEdgesCount--;
        }

        IndexStore<Edge> indexStore = graphStore.edgeTable.store.indexStore;
        if (indexStore != null) {
            indexStore.clearInView(edgeImpl, this);
        }
        TimeIndexStore timeIndexStore = graphStore.timeStore.edgeIndexStore;
        if (timeIndexStore != null) {
            timeIndexStore.clearInView(edgeImpl, this);
        }
    }

    private NodeImpl getNode(int id) {
        return graphStore.nodeStore.get(id);
    }

    private EdgeImpl getEdge(int id) {
        return graphStore.edgeStore.get(id);
    }

    private void ensureTypeCountArrayCapacity(int type) {
        if (type >= typeCounts.length) {
            int[] newArray = new int[type + 1];
            System.arraycopy(typeCounts, 0, newArray, 0, typeCounts.length);
            typeCounts = newArray;

            int[] newMutualArray = new int[type + 1];
            System.arraycopy(mutualEdgeTypeCounts, 0, newMutualArray, 0, mutualEdgeTypeCounts.length);
            mutualEdgeTypeCounts = newMutualArray;
        }
    }

    public int deepHashCode() {
        int hash = 5;
        hash = 17 * hash + (this.nodeView ? 1 : 0);
        hash = 17 * hash + (this.edgeView ? 1 : 0);
        hash = 11 * hash + (this.nodeBitVector != null ? this.nodeBitVector.hashCode() : 0);
        hash = 11 * hash + (this.edgeBitVector != null ? this.edgeBitVector.hashCode() : 0);
        hash = 11 * hash + this.nodeCount;
        hash = 11 * hash + this.edgeCount;
        hash = 11 * hash + Arrays.hashCode(this.typeCounts);
        hash = 11 * hash + Arrays.hashCode(this.mutualEdgeTypeCounts);
        hash = 11 * hash + this.mutualEdgesCount;
        hash = 11 * hash + (this.interval != null ? this.interval.hashCode() : 0);
        return hash;
    }

    public boolean deepEquals(GraphViewImpl obj) {
        if (obj == null) {
            return false;
        }
        if (this.nodeBitVector != obj.nodeBitVector && (this.nodeBitVector == null || !this.nodeBitVector
                .equals(obj.nodeBitVector))) {
            return false;
        }
        if (this.edgeBitVector != obj.edgeBitVector && (this.edgeBitVector == null || !this.edgeBitVector
                .equals(obj.edgeBitVector))) {
            return false;
        }
        if (this.nodeCount != obj.nodeCount) {
            return false;
        }
        if (this.edgeCount != obj.edgeCount) {
            return false;
        }
        if (this.nodeView != obj.nodeView) {
            return false;
        }
        if (this.edgeView != obj.edgeView) {
            return false;
        }
        if (!Arrays.equals(this.typeCounts, obj.typeCounts)) {
            return false;
        }
        if (!Arrays.equals(this.mutualEdgeTypeCounts, obj.mutualEdgeTypeCounts)) {
            return false;
        }
        if (this.mutualEdgesCount != obj.mutualEdgesCount) {
            return false;
        }
        if (this.interval != obj.interval && (this.interval == null || !this.interval.equals(obj.interval))) {
            return false;
        }
        return true;
    }

    private int incrementNodeVersion() {
        if (version != null) {
            return version.incrementAndGetNodeVersion();
        }
        return 0;
    }

    private int incrementEdgeVersion() {
        if (version != null) {
            return version.incrementAndGetEdgeVersion();
        }
        return 0;
    }

    private void checkNodeView() {
        if (!nodeView) {
            throw new RuntimeException("This method should only be used on a view with nodes enabled");
        }
    }

    private void checkEdgeView() {
        if (!edgeView) {
            throw new RuntimeException("This method should only be used on a view with edges enabled");
        }
    }

    private void checkIncidentNodesExists(final EdgeImpl e) {
        if (nodeView) {
            if (!nodeBitVector.get(e.source.storeId) || !nodeBitVector.get(e.target.storeId)) {
                throw new RuntimeException("Both source and target nodes need to be in the view");
            }
        }
    }

    private void checkValidEdgeObject(final Edge n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (!(n instanceof EdgeImpl)) {
            throw new ClassCastException("Object must be a EdgeImpl object");
        }
        if (((EdgeImpl) n).storeId == EdgeStore.NULL_ID) {
            throw new IllegalArgumentException("Edge should belong to a store");
        }
    }

    private void checkValidNodeObject(final Node n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (!(n instanceof NodeImpl)) {
            throw new ClassCastException("Object must be a NodeImpl object");
        }
        if (n.getStoreId() == NodeStore.NULL_ID) {
            throw new IllegalArgumentException("Node should belong to a store");
        }
    }
}
