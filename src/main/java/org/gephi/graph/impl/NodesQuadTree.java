package org.gephi.graph.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.ConcurrentModificationException;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;

/**
 * Adapted from https://bitbucket.org/C3/quadtree/wiki/Home
 *
 * @author Eduardo Ramos
 */
public class NodesQuadTree {

    protected final GraphLockImpl lock = new GraphLockImpl();

    private final QuadTreeNode quadTreeRoot;
    private final int maxLevels;
    private final int maxObjectsPerNode;
    private final GraphStore graphStore;
    private int modCount = 0;

    public NodesQuadTree(Rect2D rect) {
        this(null, rect);
    }

    public NodesQuadTree(GraphStore store, Rect2D rect) {
        this(store, rect, GraphStoreConfiguration.SPATIAL_INDEX_MAX_LEVELS,
                GraphStoreConfiguration.SPATIAL_INDEX_MAX_OBJECTS_PER_NODE);
    }

    public NodesQuadTree(GraphStore store, Rect2D rect, int maxLevels, int maxObjectsPerNode) {
        this.quadTreeRoot = new QuadTreeNode(rect);
        this.maxLevels = maxLevels;
        this.maxObjectsPerNode = maxObjectsPerNode;
        this.graphStore = store;
    }

    public Rect2D quadRect() {
        return quadTreeRoot.quadRect();
    }

    public NodeIterable getNodes(Rect2D searchRect) {
        return quadTreeRoot.getNodes(searchRect);
    }

    public NodeIterable getNodes(Rect2D searchRect, boolean approximate) {
        return quadTreeRoot.getNodes(searchRect, approximate);
    }

    public NodeIterable getNodes(Rect2D searchRect, boolean approximate, Predicate<? super Node> predicate) {
        return quadTreeRoot.getNodes(searchRect, approximate, predicate);
    }

    public NodeIterable getAllNodes() {
        return quadTreeRoot.getAllNodes();
    }

    public NodeIterable getAllNodes(Predicate<? super Node> predicate) {
        return quadTreeRoot.getAllNodes(predicate);
    }

    public EdgeIterable getEdges() {
        return quadTreeRoot.getAllEdges();
    }

    public EdgeIterable getEdges(Rect2D searchRect) {
        return quadTreeRoot.getEdges(searchRect);
    }

    public EdgeIterable getEdges(Rect2D searchRect, boolean approximate) {
        return quadTreeRoot.getEdges(searchRect, approximate);
    }

    public EdgeIterable getEdges(Rect2D searchRect, boolean approximate, Predicate<? super Edge> predicate) {
        return quadTreeRoot.getEdges(searchRect, approximate, predicate);
    }

    public boolean updateNode(NodeImpl item, float minX, float minY, float maxX, float maxY) {
        writeLock();
        try {
            final SpatialNodeDataImpl obj = item.getSpatialData();
            if (obj != null) {
                obj.updateBoundaries(minX, minY, maxX, maxY);
                quadTreeRoot.update(item);
                modCount++;
                return true;
            } else {
                return false;
            }
        } finally {
            writeUnlock();
        }
    }

    public boolean addNode(NodeImpl item) {
        writeLock();
        try {
            final float x = item.x();
            final float y = item.y();
            final float size = item.size();

            final float minX = x - size;
            final float minY = y - size;
            final float maxX = x + size;
            final float maxY = y + size;

            SpatialNodeDataImpl spatialData = item.getSpatialData();
            if (spatialData == null) {
                spatialData = new SpatialNodeDataImpl(minX, minY, maxX, maxY);
                item.setSpatialData(spatialData);
                quadTreeRoot.insert(item);
                modCount++;
                return true;
            } else {
                return false;
            }
        } finally {
            writeUnlock();
        }
    }

    public void clear() {
        writeLock();
        try {
            for (Node node : getAllNodes()) {
                SpatialNodeDataImpl spatialData = ((NodeImpl) node).getSpatialData();
                spatialData.clear();
            }
            quadTreeRoot.clear();
            modCount++;
        } finally {
            writeUnlock();
        }
    }

    public boolean removeNode(NodeImpl item) {
        writeLock();
        try {
            final SpatialNodeDataImpl spatialData = item.getSpatialData();
            if (spatialData != null && spatialData.quadTreeNode != null) {
                quadTreeRoot.delete(item, true);
                modCount++;
                return true;
            }
            return false;
        } finally {
            writeUnlock();
        }
    }

    public int getObjectCount() {
        readLock();
        int count = quadTreeRoot.objectCount();
        readUnlock();
        return count;
    }

    public void readLock() {
        if (lock != null) {
            lock.readLock();
        }
    }

    public void readUnlock() {
        if (lock != null) {
            lock.readUnlock();
        }
    }

    public void writeLock() {
        if (lock != null) {
            lock.writeLock();
        }
    }

    public void writeUnlock() {
        if (lock != null) {
            lock.writeUnlock();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        quadTreeRoot.toString(sb);

        return sb.toString();
    }

    public int getDepth() {
        readLock();
        int depth = quadTreeRoot.getDepth();
        readUnlock();
        return depth;
    }

    public int getNodeCount(boolean keepOnlyWithObjects) {
        readLock();
        int count = quadTreeRoot.getNodeCount(keepOnlyWithObjects);
        readUnlock();
        return count;
    }

    public Rect2D getBoundaries() {
        return getBoundaries(null);
    }

    public Rect2D getBoundaries(Predicate<? super Node> predicate) {
        readLock();
        try {
            NodeIterable allNodes = predicate == null ? getAllNodes() : getAllNodes(predicate);

            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;

            boolean hasNodes = false;

            for (Node node : allNodes) {
                if (node == null) {
                    continue;
                }
                SpatialNodeDataImpl spatialData = ((NodeImpl) node).getSpatialData();
                if (spatialData != null) {
                    hasNodes = true;
                    if (spatialData.minX < minX) {
                        minX = spatialData.minX;
                    }
                    if (spatialData.minY < minY) {
                        minY = spatialData.minY;
                    }
                    if (spatialData.maxX > maxX) {
                        maxX = spatialData.maxX;
                    }
                    if (spatialData.maxY > maxY) {
                        maxY = spatialData.maxY;
                    }
                }
            }

            return hasNodes ? new Rect2D(minX, minY, maxX, maxY) : new Rect2D(Float.NEGATIVE_INFINITY,
                    Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        } finally {
            readUnlock();
        }
    }

    private int collectOverlapping(QuadTreeNode node, Rect2D searchRect, Set<QuadTreeNode> resultSet) {
        if (searchRect != null && !node.rect.intersects(searchRect)) {
            return 0;
        }

        // If this node has objects and intersects with search rect, add it
        int nodeCount = 0;
        if (node.objectCount > 0) {
            resultSet.add(node);
            nodeCount += node.objectCount;
        }

        // Recursively check children
        if (node.childTL != null) {
            nodeCount += collectOverlapping(node.childTL, searchRect, resultSet);
            nodeCount += collectOverlapping(node.childTR, searchRect, resultSet);
            nodeCount += collectOverlapping(node.childBL, searchRect, resultSet);
            nodeCount += collectOverlapping(node.childBR, searchRect, resultSet);
        }
        return nodeCount;
    }

    protected class QuadTreeNode {

        private NodeImpl[] objects = null; // Fixed-size array for objects
        private int objectCount = 0; // Number of objects currently in this node
        private final Rect2D rect; // The area this QuadTree represents

        private final QuadTreeNode parent; // The parent of this quad
        private final int level;
        private int size = 0; // Total number of objects in this node and its children

        private QuadTreeNode childTL = null; // Top Left Child
        private QuadTreeNode childTR = null; // Top Right Child
        private QuadTreeNode childBL = null; // Bottom Left Child
        private QuadTreeNode childBR = null; // Bottom Right Child

        public Rect2D quadRect() {
            return rect;
        }

        public QuadTreeNode topLeftChild() {
            return childTL;
        }

        public QuadTreeNode topRightChild() {
            return childTR;
        }

        public QuadTreeNode bottomLeftChild() {
            return childBL;
        }

        public QuadTreeNode bottomRightChild() {
            return childBR;
        }

        public QuadTreeNode parent() {
            return parent;
        }

        public int count() {
            return size;
        }

        public boolean isEmptyLeaf() {
            return size == 0 && childTL == null;
        }

        public QuadTreeNode(Rect2D rect) {
            this(null, 0, rect);
        }

        private QuadTreeNode(QuadTreeNode parent, int level, Rect2D rect) {
            this.level = level;
            this.rect = rect;
            this.parent = parent;
        }

        private void add(NodeImpl item) {
            if (objects == null) {
                objects = new NodeImpl[maxObjectsPerNode];
            }

            // Check if item is already in this node (avoid duplicates)
            SpatialNodeDataImpl spatialData = item.getSpatialData();
            if (spatialData.quadTreeNode == this && spatialData.arrayIndex >= 0) {
                return; // Already in this node
            }

            // Resize array if needed (can happen when at max depth or when objects don't
            // fit in children)
            if (objectCount >= objects.length) {
                NodeImpl[] newArray = new NodeImpl[objects.length * 2];
                System.arraycopy(objects, 0, newArray, 0, objects.length);
                objects = newArray;
            }

            // Add to array
            objects[objectCount] = item;
            spatialData.setQuadTreeNode(this);
            spatialData.setArrayIndex(objectCount);
            objectCount++;

            // Update size and edge size for this node and all parents
            QuadTreeNode node = this;
            while (node != null) {
                node.size++;
                node = node.parent;
            }
        }

        private void remove(NodeImpl item) {
            if (objects != null && objectCount > 0) {
                SpatialNodeDataImpl spatialData = item.getSpatialData();
                int index = spatialData.arrayIndex;

                if (index >= 0 && index < objectCount && objects[index] == item) {
                    // Swap with last element for O(1) removal
                    objectCount--;
                    NodeImpl lastItem = objects[objectCount];
                    objects[index] = lastItem;
                    objects[objectCount] = null;

                    // Update the moved item's index
                    if (lastItem != null && index < objectCount) {
                        lastItem.getSpatialData().setArrayIndex(index);
                    }

                    // Clear removed item's data
                    spatialData.clear();

                    // Update size
                    QuadTreeNode node = this;
                    while (node != null) {
                        node.size--;
                        node = node.parent;
                    }
                }
            }
        }

        private int objectCount() {
            return size;
        }

        private void subdivide() {
            // We've reached capacity, subdivide...
            final float minX = rect.minX;
            final float halfX = (rect.minX + rect.maxX) / 2;
            final float maxX = rect.maxX;

            final float minY = rect.minY;
            final float halfY = (rect.minY + rect.maxY) / 2;
            final float maxY = rect.maxY;

            childTL = new QuadTreeNode(this, level + 1, new Rect2D(minX, minY, halfX, halfY));
            childTR = new QuadTreeNode(this, level + 1, new Rect2D(halfX, minY, maxX, halfY));
            childBL = new QuadTreeNode(this, level + 1, new Rect2D(minX, halfY, halfX, maxY));
            childBR = new QuadTreeNode(this, level + 1, new Rect2D(halfX, halfY, maxX, maxY));

            // Keep track of objects that couldn't be moved
            NodeImpl[] remainingObjects = new NodeImpl[objectCount];
            int remainingCount = 0;

            // If they're completely contained by the quad, bump objects down
            for (int i = 0; i < objectCount; i++) {
                NodeImpl obj = objects[i];
                QuadTreeNode destTree = getDestinationTree(obj);
                if (destTree != this) {
                    // Insert to the appropriate tree
                    destTree.insert(obj);

                    // Update size
                    QuadTreeNode node = this;
                    while (node != null) {
                        node.size--;
                        node = node.parent;
                    }
                } else {
                    // Keep this object in the current node
                    remainingObjects[remainingCount] = obj;
                    obj.getSpatialData().setArrayIndex(remainingCount);
                    remainingCount++;
                }
            }

            // Update this node's object array
            for (int i = 0; i < remainingCount; i++) {
                objects[i] = remainingObjects[i];
            }
            for (int i = remainingCount; i < objectCount; i++) {
                objects[i] = null;
            }
            objectCount = remainingCount;
        }

        private QuadTreeNode getDestinationTree(NodeImpl item) {
            // If a child can't contain an object, it will live in this Quad
            final QuadTreeNode destTree;

            SpatialNodeDataImpl spatialData = item.getSpatialData();
            final float minX = spatialData.minX;
            final float minY = spatialData.minY;
            final float maxX = spatialData.maxX;
            final float maxY = spatialData.maxY;

            if (childTL.quadRect().contains(minX, minY, maxX, maxY)) {
                destTree = childTL;
            } else if (childTR.quadRect().contains(minX, minY, maxX, maxY)) {
                destTree = childTR;
            } else if (childBL.quadRect().contains(minX, minY, maxX, maxY)) {
                destTree = childBL;
            } else if (childBR.quadRect().contains(minX, minY, maxX, maxY)) {
                destTree = childBR;
            } else {
                destTree = this;
            }

            return destTree;
        }

        private void relocate(NodeImpl item) {
            SpatialNodeDataImpl spatialData = item.getSpatialData();

            // Are we still inside our parent?
            if (quadRect().contains(spatialData.minX, spatialData.minY, spatialData.maxX, spatialData.maxY)) {
                // Good, have we moved inside any of our children?
                if (childTL != null) {
                    QuadTreeNode dest = getDestinationTree(item);
                    if (spatialData.quadTreeNode != dest) {
                        // Delete the item from this quad and add it to our
                        // child
                        // Note: Do NOT clean during this call, it can
                        // potentially delete our destination quad
                        QuadTreeNode formerOwner = spatialData.quadTreeNode;
                        delete(item, false);
                        dest.insert(item);

                        // Clean up ourselves
                        formerOwner.cleanUpwards();
                    }
                }
            } else {
                // We don't fit here anymore, move up, if we can
                if (parent != null) {
                    parent.relocate(item);
                }
            }
        }

        private void cleanUpwards() {
            if (childTL != null) {
                // If all the children are empty leaves, delete all the children
                if (childTL.isEmptyLeaf() && childTR.isEmptyLeaf() && childBL.isEmptyLeaf() && childBR.isEmptyLeaf()) {
                    childTL = null;
                    childTR = null;
                    childBL = null;
                    childBR = null;

                    if (parent != null && count() == 0) {
                        parent.cleanUpwards();
                    }
                }
            } else {
                // I could be one of 4 empty leaves, tell my parent to clean up
                if (parent != null && count() == 0) {
                    parent.cleanUpwards();
                }
            }
        }

        private void clear() {
            // clear out the children, if we have any
            if (childTL != null) {
                childTL.clear();
                childTR.clear();
                childBL.clear();
                childBR.clear();
            }

            // clear any objects at this level
            if (objects != null) {
                // Clear spatial data references for all objects
                for (int i = 0; i < objectCount; i++) {
                    if (objects[i] != null) {
                        SpatialNodeDataImpl spatialData = objects[i].getSpatialData();
                        spatialData.clear();
                        objects[i] = null;
                    }
                }
                objects = null;
                objectCount = 0;
            }

            // Reset size and edge size
            size = 0;

            // Set the children to null
            childTL = null;
            childTR = null;
            childBL = null;
            childBR = null;
        }

        private void delete(NodeImpl node, boolean clean) {
            SpatialNodeDataImpl spatialData = node.getSpatialData();
            if (spatialData.quadTreeNode != null) {
                if (spatialData.quadTreeNode == this) {
                    remove(node);
                    if (clean) {
                        cleanUpwards();
                    }
                } else {
                    spatialData.quadTreeNode.delete(node, clean);
                }
            }
        }

        private void insert(NodeImpl item) {
            SpatialNodeDataImpl spatialData = item.getSpatialData();
            // If this quad doesn't contain the items rectangle, do nothing,
            // unless we are the root
            if (!rect.contains(spatialData.minX, spatialData.minY, spatialData.maxX, spatialData.maxY)) {
                if (parent == null) {
                    // This object is outside of the QuadTreeXNA bounds, we
                    // should add it at the root level
                    add(item);
                } else {
                    throw new IllegalStateException(
                            "We are not the root, and this object doesn't fit here. How did we get here?");
                }
            }

            if (objects == null || (childTL == null && (level >= maxLevels || objectCount + 1 <= maxObjectsPerNode))) {
                // If there's room to add the object, just add it
                add(item);
            } else {
                // No quads, create them and bump objects down where appropriate
                if (childTL == null) {
                    subdivide();
                }

                // Find out which tree this object should go in and add it there
                final QuadTreeNode destTree = getDestinationTree(item);
                if (destTree == this) {
                    add(item);
                } else {
                    destTree.insert(item);
                }
            }
        }

        private NodeIterable getNodes(Rect2D searchRect) {
            return new QuadTreeNodesIterable(searchRect);
        }

        private NodeIterable getNodes(Rect2D searchRect, boolean approximate) {
            return new QuadTreeNodesIterable(searchRect, approximate);
        }

        private NodeIterable getNodes(Rect2D searchRect, boolean approximate, Predicate<? super Node> predicate) {
            return new FilteredQuadTreeNodeIterable(searchRect, approximate, predicate);
        }

        private NodeIterable getAllNodes() {
            return new QuadTreeNodesIterable(null);
        }

        private NodeIterable getAllNodes(Predicate<? super Node> predicate) {
            return new FilteredQuadTreeNodeIterable(null, false, predicate);
        }

        private EdgeIterable getEdges(Rect2D searchRect) {
            return new QuadTreeEdgesIterable(searchRect);
        }

        private EdgeIterable getEdges(Rect2D searchRect, boolean approximate) {
            return new QuadTreeEdgesIterable(searchRect, approximate);
        }

        private EdgeIterable getEdges(Rect2D searchRect, boolean approximate, Predicate<? super Edge> predicate) {
            return new FilteredQuadTreeEdgeIterable(searchRect, approximate, predicate);
        }

        private EdgeIterable getAllEdges() {
            return new QuadTreeEdgesIterable(null);
        }

        private void update(NodeImpl item) {
            SpatialNodeDataImpl spatialData = item.getSpatialData();
            if (spatialData.quadTreeNode != null) {
                spatialData.quadTreeNode.relocate(item);
            } else {
                relocate(item);
            }
        }

        private int getDepth() {
            int maxLevel = level;
            if (childTL != null) {
                maxLevel = Math.max(maxLevel, childTL.getDepth());
                maxLevel = Math.max(maxLevel, childBR.getDepth());
                maxLevel = Math.max(maxLevel, childTR.getDepth());
                maxLevel = Math.max(maxLevel, childBL.getDepth());
            }
            return maxLevel;
        }

        private int getNodeCount(boolean withObjects) {
            int count = 1; // Count this node

            // If withObjects is true, only count nodes that have objects
            if (withObjects && (objects == null || objectCount == 0)) {
                count = 0;
            }

            // Recursively count children
            if (childTL != null) {
                count += childTL.getNodeCount(withObjects);
                count += childTR.getNodeCount(withObjects);
                count += childBL.getNodeCount(withObjects);
                count += childBR.getNodeCount(withObjects);
            }

            return count;
        }

        public void toString(StringBuilder sb) {
            for (int i = 0; i < level; i++) {
                sb.append("  ");
            }
            sb.append(rect.toString()).append('\n');

            if (objects != null) {
                for (int j = 0; j <= level; j++) {
                    sb.append("  ");
                }

                sb.append(objectCount).append(" objects \n");
            }

            if (childTL != null) {
                childTL.toString(sb);
                childTR.toString(sb);
                childBL.toString(sb);
                childBR.toString(sb);
            }
        }
    }

    private class FilteredQuadTreeNodeIterable extends QuadTreeNodesIterable {

        private final Predicate<? super Node> predicate;

        public FilteredQuadTreeNodeIterable(Rect2D searchRect, boolean approximate, Predicate<? super Node> predicate) {
            super(searchRect, approximate);
            this.predicate = predicate;
        }

        @Override
        public Iterator<Node> iterator() {
            return new QuadTreeNodesIterator(quadTreeRoot, searchRect, approximate, predicate);
        }

        @Override
        public Spliterator<Node> spliterator() {
            return new FilteredQuadTreeNodesSpliterator(quadTreeRoot, searchRect, approximate, predicate);
        }
    }

    private class QuadTreeNodesIterable implements NodeIterable {

        protected final Rect2D searchRect;
        protected final boolean approximate;

        public QuadTreeNodesIterable(Rect2D searchRect) {
            this(searchRect, GraphStoreConfiguration.SPATIAL_INDEX_APPROXIMATE_AREA_SEARCH);
        }

        public QuadTreeNodesIterable(Rect2D searchRect, boolean approximate) {
            this.searchRect = searchRect;
            this.approximate = approximate;
        }

        @Override
        public Iterator<Node> iterator() {
            return new QuadTreeNodesIterator(quadTreeRoot, searchRect, approximate);
        }

        @Override
        public Spliterator<Node> spliterator() {
            return new QuadTreeNodesSpliterator(quadTreeRoot, searchRect, approximate);
        }

        @Override
        public Node[] toArray() {
            readLock();
            int count = getObjectCount();
            Node[] array = new Node[count];
            for (Node node : this) {
                array[--count] = node;
            }
            readUnlock();
            return array;
        }

        @Override
        public Collection<Node> toCollection() {
            final List<Node> list = new ArrayList<>();

            for (Node node : this) {
                list.add(node);
            }

            return list;
        }

        @Override
        public Set<Node> toSet() {
            final Set<Node> set = new HashSet<>();

            for (Node node : this) {
                set.add(node);
            }

            return set;
        }

        @Override
        public void doBreak() {
            readUnlock();
        }
    }

    private class FilteredQuadTreeEdgeIterable extends QuadTreeEdgesIterable {

        private final Predicate<? super Edge> predicate;

        public FilteredQuadTreeEdgeIterable(Rect2D searchRect, boolean approximate, Predicate<? super Edge> predicate) {
            super(searchRect, approximate);
            this.predicate = predicate;
        }

        @Override
        public Iterator<Edge> iterator() {
            return new QuadTreeEdgesIterator(quadTreeRoot, searchRect, approximate, predicate);
        }

        @Override
        public Spliterator<Edge> spliterator() {
            HashSet<QuadTreeNode> overlappingNodes = new HashSet<>();
            int nodeCount = collectOverlapping(quadTreeRoot, searchRect, overlappingNodes);
            if (useDirectIterator(nodeCount)) {
                return new QuadTreeGlobalEdgesSpliterator(searchRect, approximate, overlappingNodes, predicate);
            }
            // Use local iterator
            return new FilteredQuadTreeEdgesSpliterator(quadTreeRoot, searchRect, approximate, predicate);
        }
    }

    private class QuadTreeEdgesIterable implements EdgeIterable {

        protected final Rect2D searchRect;
        protected final boolean approximate;

        public QuadTreeEdgesIterable(Rect2D searchRect) {
            this(searchRect, GraphStoreConfiguration.SPATIAL_INDEX_APPROXIMATE_AREA_SEARCH);
        }

        public QuadTreeEdgesIterable(Rect2D searchRect, boolean approximate) {
            this.searchRect = searchRect;
            this.approximate = approximate;
        }

        @Override
        public Iterator<Edge> iterator() {
            return new QuadTreeEdgesIterator(quadTreeRoot, searchRect, approximate);
        }

        protected boolean useDirectIterator(int nodeCount) {
            return (float) nodeCount / quadTreeRoot.size > GraphStoreConfiguration.SPATIAL_INDEX_LOCAL_ITERATOR_THRESHOLD;
        }

        @Override
        public Spliterator<Edge> spliterator() {
            if (searchRect == null) {
                // Special case: all edges
                return new QuadTreeGlobalEdgesSpliterator(null, approximate, null, null);
            }
            HashSet<QuadTreeNode> overlappingNodes = new HashSet<>();
            int nodeCount = collectOverlapping(quadTreeRoot, searchRect, overlappingNodes);
            if (approximate && nodeCount == quadTreeRoot.size) {
                // Optimisation: approximate search and all nodes overlapping, so just return
                // all edges
                return new QuadTreeGlobalEdgesSpliterator(null, true, null, null);
            } else if (useDirectIterator(nodeCount)) {
                return new QuadTreeGlobalEdgesSpliterator(searchRect, approximate, overlappingNodes, null);
            }
            // Use local iterator
            return new QuadTreeEdgesSpliterator(quadTreeRoot, searchRect, approximate);
        }

        @Override
        public Edge[] toArray() {
            return toCollection().toArray(new Edge[0]);
        }

        @Override
        public Collection<Edge> toCollection() {
            final List<Edge> list = new ArrayList<>();

            for (Edge edge : this) {
                list.add(edge);
            }

            return list;
        }

        @Override
        public Set<Edge> toSet() {
            final Set<Edge> set = new HashSet<>();

            for (Edge edge : this) {
                set.add(edge);
            }

            return set;
        }

        @Override
        public void doBreak() {
            readUnlock();
        }

    }

    private class QuadTreeEdgesIterator implements Iterator<Edge> {

        private final EdgeStore.EdgeInOutMultiIterator edgeIterator;
        private final Predicate<? super Edge> predicate;
        private boolean finished = false;
        private Edge next;

        public QuadTreeEdgesIterator(QuadTreeNode root, Rect2D searchRect, boolean approximate) {
            this(root, searchRect, approximate, null);
        }

        public QuadTreeEdgesIterator(QuadTreeNode root, Rect2D searchRect, boolean approximate, Predicate<? super Edge> predicate) {
            this.predicate = predicate;
            readLock();

            // Create a node iterator for the quad tree
            final QuadTreeNodesIterator nodeIterator = new QuadTreeNodesIterator(root, searchRect, approximate);

            // Create the edge iterator using the EdgeStore method
            this.edgeIterator = graphStore.edgeStore.edgeIterator(new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return nodeIterator.hasNext();
                }

                @Override
                public NodeImpl next() {
                    return nodeIterator.next();
                }
            }, true);
        }

        @Override
        public boolean hasNext() {
            if (finished) {
                return false;
            }

            if (next != null) {
                return true;
            }

            // Look for next edge that passes predicate
            while (edgeIterator != null && edgeIterator.hasNext()) {
                Edge edge = edgeIterator.next();
                if (predicate == null || predicate.test(edge)) {
                    next = edge;
                    return true;
                }
            }

            readUnlock();
            finished = true;
            return false;
        }

        @Override
        public Edge next() {
            if (next == null && !hasNext()) {
                throw new IllegalStateException("No next available!");
            }

            Edge result = next;
            next = null;
            return result;
        }
    }

    private class QuadTreeNodesIterator implements Iterator<Node> {

        private final Rect2D searchRect;
        private final boolean approximate;
        private final Predicate<? super Node> predicate;
        private final Deque<QuadTreeNode> nodesStack = new ArrayDeque<>();
        private final Deque<Boolean> fullyContainedStack = new ArrayDeque<>();

        // Current:
        private Iterator<NodeImpl> currentIterator;
        private boolean currentFullyContained;
        private boolean finished = false;

        private NodeImpl next;

        public QuadTreeNodesIterator(QuadTreeNode root, Rect2D searchRect, boolean approximate) {
            this(root, searchRect, approximate, null);
        }

        public QuadTreeNodesIterator(QuadTreeNode root, Rect2D searchRect, boolean approximate, Predicate<? super Node> predicate) {
            this.searchRect = searchRect;
            this.approximate = approximate;
            this.predicate = predicate;

            readLock();

            // Null rect means get all
            currentFullyContained = searchRect == null;

            // We always add the root and don't test for the root being fully
            // contained, to correctly handle the case of nodes out of the quad
            // tree bounds
            addChildrenToVisit(root, currentFullyContained);
            currentIterator = root.objects != null ? new ArrayIterator(root.objects, root.objectCount) : null;
        }

        private void addChildrenToVisit(QuadTreeNode quadTreeNode, boolean fullyContained) {
            if (quadTreeNode.childTL != null) {
                nodesStack.push(quadTreeNode.childBR);
                nodesStack.push(quadTreeNode.childBL);
                nodesStack.push(quadTreeNode.childTR);
                nodesStack.push(quadTreeNode.childTL);

                fullyContainedStack.push(fullyContained);
                fullyContainedStack.push(fullyContained);
                fullyContainedStack.push(fullyContained);
                fullyContainedStack.push(fullyContained);
            }
        }

        @Override
        public boolean hasNext() {
            if (finished) {
                return false;
            }

            if (next != null) {
                return true;
            }

            while (currentIterator != null || !nodesStack.isEmpty()) {
                if (currentIterator != null) {
                    while (currentIterator.hasNext()) {
                        final NodeImpl elem = currentIterator.next();

                        // First check spatial conditions
                        boolean spatialMatch;
                        if (approximate || currentFullyContained) {
                            // In approximate mode or when fully contained, include all objects
                            spatialMatch = true;
                        } else {
                            // In exact mode, check intersection
                            final SpatialNodeDataImpl spatialData = elem.getSpatialData();
                            spatialMatch = searchRect
                                    .intersects(spatialData.minX, spatialData.minY, spatialData.maxX, spatialData.maxY);
                        }

                        // If spatial conditions are met, check predicate
                        if (spatialMatch && (predicate == null || predicate.test(elem))) {
                            next = elem;
                            return true;
                        }
                    }

                    currentIterator = null;
                } else {
                    final QuadTreeNode pointer = nodesStack.pop();

                    currentFullyContained = fullyContainedStack.pop() || searchRect.contains(pointer.rect);

                    if (currentFullyContained || pointer.rect.intersects(searchRect)) {
                        addChildrenToVisit(pointer, currentFullyContained);
                        currentIterator = pointer.objects != null
                                ? new ArrayIterator(pointer.objects, pointer.objectCount) : null;
                    } else {
                        currentIterator = null;
                    }
                }
            }

            readUnlock();
            finished = true;
            return false;
        }

        @Override
        public NodeImpl next() {
            if (next == null) {
                throw new IllegalStateException("No next available!");
            }

            final NodeImpl node = next;

            next = null;
            return node;
        }

    }

    // Helper class to iterate over array elements
    private static class ArrayIterator implements Iterator<NodeImpl> {
        private final NodeImpl[] array;
        private final int size;
        private final Predicate<? super NodeImpl> predicate;
        private int index = 0;
        private NodeImpl next;

        public ArrayIterator(NodeImpl[] array, int size) {
            this(array, size, null);
        }

        public ArrayIterator(NodeImpl[] array, int size, Predicate<? super NodeImpl> predicate) {
            this.array = array;
            this.size = size;
            this.predicate = predicate;
        }

        @Override
        public boolean hasNext() {
            if (next != null) {
                return true;
            }

            // Find next element that passes predicate
            while (index < size) {
                NodeImpl candidate = array[index++];
                if (predicate == null || predicate.test(candidate)) {
                    next = candidate;
                    return true;
                }
            }
            return false;
        }

        @Override
        public NodeImpl next() {
            if (next == null && !hasNext()) {
                throw new IllegalStateException("No more elements");
            }
            NodeImpl result = next;
            next = null;
            return result;
        }
    }

    private abstract class AbstractQuadTreeSpliterator<T> implements Spliterator<T> {
        protected final Rect2D searchRect;
        protected final boolean approximate;
        protected final Deque<QuadTreeNode> nodesStack = new ArrayDeque<>();
        protected final Deque<Boolean> fullyContainedStack = new ArrayDeque<>();

        protected final int expectedModCount;
        protected Iterator<?> currentIterator;
        protected boolean currentFullyContained;
        protected T next;
        protected int remainingSize;

        protected AbstractQuadTreeSpliterator(QuadTreeNode root, Rect2D searchRect, boolean approximate) {
            this.searchRect = searchRect;
            this.approximate = approximate;
            this.expectedModCount = modCount;

            // Null rect means get all
            currentFullyContained = searchRect == null;

            // Initialize with root
            addNode(root, currentFullyContained);
            currentIterator = createIteratorForNode(root);

            // For SIZED characteristic, we need exact count
            if (searchRect == null) {
                // Getting all elements, so we can use the maintained size
                remainingSize = root.size;
            } else if (approximate) {
                // In approximate mode, count all elements in intersecting quadrants
                remainingSize = countNodesInRectApproximate(root, searchRect);
            } else {
                // Need to count elements in the search rect
                remainingSize = countNodesInRect(root, searchRect);
            }
        }

        protected AbstractQuadTreeSpliterator(QuadTreeNode node, Rect2D searchRect, boolean approximate, int expectedModCount, boolean fullyContained, int size) {
            this.searchRect = searchRect;
            this.approximate = approximate;
            this.expectedModCount = expectedModCount;
            this.remainingSize = size;
            this.currentFullyContained = fullyContained;

            if (node != null) {
                addNode(node, fullyContained);
                currentIterator = createIteratorForNode(node);
            } else {
                currentIterator = null;
            }
        }

        protected void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        protected void addNode(QuadTreeNode node, boolean fullyContained) {
            if (node.childTL != null) {
                nodesStack.push(node.childBR);
                nodesStack.push(node.childBL);
                nodesStack.push(node.childTR);
                nodesStack.push(node.childTL);

                fullyContainedStack.push(fullyContained);
                fullyContainedStack.push(fullyContained);
                fullyContainedStack.push(fullyContained);
                fullyContainedStack.push(fullyContained);
            }
        }

        protected int countNodesInRect(QuadTreeNode node, Rect2D rect) {
            int count = 0;

            // Count objects at this level
            if (node.objects != null) {
                for (int i = 0; i < node.objectCount; i++) {
                    NodeImpl obj = node.objects[i];
                    SpatialNodeDataImpl spatialData = obj.getSpatialData();
                    if (rect.intersects(spatialData.minX, spatialData.minY, spatialData.maxX, spatialData.maxY)) {
                        count++;
                    }
                }
            }

            // Count in children if they intersect
            if (node.childTL != null) {
                if (rect.contains(node.childTL.rect)) {
                    count += node.childTL.size;
                } else if (node.childTL.rect.intersects(rect)) {
                    count += countNodesInRect(node.childTL, rect);
                }

                if (rect.contains(node.childTR.rect)) {
                    count += node.childTR.size;
                } else if (node.childTR.rect.intersects(rect)) {
                    count += countNodesInRect(node.childTR, rect);
                }

                if (rect.contains(node.childBL.rect)) {
                    count += node.childBL.size;
                } else if (node.childBL.rect.intersects(rect)) {
                    count += countNodesInRect(node.childBL, rect);
                }

                if (rect.contains(node.childBR.rect)) {
                    count += node.childBR.size;
                } else if (node.childBR.rect.intersects(rect)) {
                    count += countNodesInRect(node.childBR, rect);
                }
            }

            return count;
        }

        protected int countNodesInRectApproximate(QuadTreeNode node, Rect2D rect) {
            int count = 0;

            // Count objects at this level if the node intersects
            if (node.objects != null) {
                count += node.objectCount;
            }

            // Count in children if they intersect
            if (node.childTL != null) {
                if (rect.containsOrIntersects(node.childTL.rect)) {
                    count += node.childTL.size;
                }
                if (rect.containsOrIntersects(node.childTR.rect)) {
                    count += node.childTR.size;
                }
                if (rect.containsOrIntersects(node.childBL.rect)) {
                    count += node.childBL.size;
                }
                if (rect.containsOrIntersects(node.childBR.rect)) {
                    count += node.childBR.size;
                }
            }

            return count;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            checkForComodification();

            if (next != null || findNext()) {
                action.accept(next);
                next = null;
                remainingSize--;
                return true;
            }
            return false;
        }

        protected abstract boolean findNext();

        protected abstract Iterator<?> createIteratorForNode(QuadTreeNode node);

        protected abstract boolean checkElementSpatialMatch(Object element);

        protected abstract AbstractQuadTreeSpliterator<T> createSplitInstance(QuadTreeNode node, Rect2D searchRect, boolean approximate, int expectedModCount, boolean fullyContained, int size);

        @Override
        public Spliterator<T> trySplit() {
            checkForComodification();

            // Can only split if we have nodes on the stack
            if (!nodesStack.isEmpty() && remainingSize > 1) {
                // Take half of the remaining nodes from the stack
                int nodesToSplit = Math.min(nodesStack.size() / 2, remainingSize / 2);
                if (nodesToSplit > 0) {
                    Deque<QuadTreeNode> splitNodes = new ArrayDeque<>();
                    Deque<Boolean> splitContained = new ArrayDeque<>();

                    // Calculate size for the split
                    int splitSize = 0;

                    // Move nodes to split queues and calculate their size
                    for (int i = 0; i < nodesToSplit; i++) {
                        QuadTreeNode node = nodesStack.removeLast();
                        boolean contained = fullyContainedStack.removeLast();
                        splitNodes.addFirst(node);
                        splitContained.addFirst(contained);

                        if (searchRect == null || contained) {
                            splitSize += node.size;
                        } else if (approximate) {
                            splitSize += countNodesInRectApproximate(node, searchRect);
                        } else {
                            splitSize += countNodesInRect(node, searchRect);
                        }
                    }

                    // Update our remaining size
                    remainingSize -= splitSize;

                    // Create new spliterator for the split portion with empty initial state
                    AbstractQuadTreeSpliterator<T> split = createSplitInstance(null, searchRect, approximate, expectedModCount, false, splitSize);

                    // Add all split nodes to the new spliterator's stack
                    while (!splitNodes.isEmpty()) {
                        split.nodesStack.push(splitNodes.removeLast());
                        split.fullyContainedStack.push(splitContained.removeLast());
                    }

                    return split;
                }
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return remainingSize;
        }
    }

    private class QuadTreeNodesSpliterator extends AbstractQuadTreeSpliterator<Node> {

        public QuadTreeNodesSpliterator(QuadTreeNode root, Rect2D searchRect, boolean approximate) {
            super(root, searchRect, approximate);
        }

        private QuadTreeNodesSpliterator(QuadTreeNode node, Rect2D searchRect, boolean approximate, int expectedModCount, boolean fullyContained, int size) {
            super(node, searchRect, approximate, expectedModCount, fullyContained, size);
        }

        @Override
        protected Iterator<?> createIteratorForNode(QuadTreeNode node) {
            return node.objects != null ? new ArrayIterator(node.objects, node.objectCount) : null;
        }

        @Override
        protected boolean checkElementSpatialMatch(Object element) {
            NodeImpl elem = (NodeImpl) element;
            if (approximate || currentFullyContained || searchRect == null) {
                return true;
            } else {
                final SpatialNodeDataImpl spatialData = elem.getSpatialData();
                return searchRect.intersects(spatialData.minX, spatialData.minY, spatialData.maxX, spatialData.maxY);
            }
        }

        @Override
        protected AbstractQuadTreeSpliterator<Node> createSplitInstance(QuadTreeNode node, Rect2D searchRect, boolean approximate, int expectedModCount, boolean fullyContained, int size) {
            return new QuadTreeNodesSpliterator(node, searchRect, approximate, expectedModCount, fullyContained, size);
        }

        @Override
        protected boolean findNext() {
            while (currentIterator != null || !nodesStack.isEmpty()) {
                if (currentIterator != null) {
                    while (currentIterator.hasNext()) {
                        final NodeImpl elem = (NodeImpl) currentIterator.next();

                        if (checkElementSpatialMatch(elem)) {
                            next = elem;
                            return true;
                        }
                    }
                    currentIterator = null;
                } else {
                    final QuadTreeNode pointer = nodesStack.pop();
                    currentFullyContained = fullyContainedStack
                            .pop() || (searchRect != null && searchRect.contains(pointer.rect));

                    if (currentFullyContained || searchRect == null || pointer.rect.intersects(searchRect)) {
                        addNode(pointer, currentFullyContained);
                        currentIterator = createIteratorForNode(pointer);
                    }
                }
            }
            return false;
        }

        @Override
        public int characteristics() {
            return DISTINCT | SIZED | SUBSIZED | NONNULL;
        }
    }

    private abstract static class FilteredSpliterator<T, S extends Spliterator<T>, P extends Spliterator<T>> implements Spliterator<T> {
        protected final S parentSpliterator;
        protected final Predicate<? super T> predicate;
        protected final Object[] holder = new Object[1];

        protected FilteredSpliterator(S parentSpliterator, Predicate<? super T> predicate) {
            this.parentSpliterator = parentSpliterator;
            this.predicate = predicate;
        }

        protected abstract P createSplitInstance(S splitParent, Predicate<? super T> predicate);

        protected abstract boolean testPredicate(T element);

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            while (true) {
                boolean advanced = parentSpliterator.tryAdvance(e -> holder[0] = e);
                if (!advanced)
                    return false;
                @SuppressWarnings("unchecked")
                T t = (T) holder[0];
                if (testPredicate(t)) {
                    action.accept(t);
                    return true;
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Spliterator<T> trySplit() {
            S splitParent = (S) parentSpliterator.trySplit();
            if (splitParent != null) {
                return createSplitInstance(splitParent, predicate);
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return parentSpliterator.estimateSize();
        }

        @Override
        public int characteristics() {
            return parentSpliterator.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }
    }

    private class FilteredQuadTreeNodesSpliterator extends FilteredSpliterator<Node, QuadTreeNodesSpliterator, FilteredQuadTreeNodesSpliterator> {

        public FilteredQuadTreeNodesSpliterator(QuadTreeNode root, Rect2D searchRect, boolean approximate, Predicate<? super Node> predicate) {
            super(new QuadTreeNodesSpliterator(root, searchRect, approximate), predicate);
        }

        private FilteredQuadTreeNodesSpliterator(QuadTreeNodesSpliterator parentSpliterator, Predicate<? super Node> predicate) {
            super(parentSpliterator, predicate);
        }

        @Override
        protected FilteredQuadTreeNodesSpliterator createSplitInstance(QuadTreeNodesSpliterator splitParent, Predicate<? super Node> predicate) {
            return new FilteredQuadTreeNodesSpliterator(splitParent, predicate);
        }

        @Override
        protected boolean testPredicate(Node element) {
            return predicate == null || predicate.test(element);
        }
    }

    private class QuadTreeEdgesSpliterator extends AbstractQuadTreeSpliterator<Edge> {

        public QuadTreeEdgesSpliterator(QuadTreeNode root, Rect2D searchRect) {
            this(root, searchRect, false);
        }

        public QuadTreeEdgesSpliterator(QuadTreeNode root, Rect2D searchRect, boolean approximate) {
            super(root, searchRect, approximate);
        }

        private QuadTreeEdgesSpliterator(QuadTreeNode node, Rect2D searchRect, boolean approximate, int expectedModCount, boolean fullyContained, int size) {
            super(node, searchRect, approximate, expectedModCount, fullyContained, size);
        }

        @Override
        protected Iterator<?> createIteratorForNode(QuadTreeNode node) {
            if (node.objects == null) {
                return Collections.emptyIterator();
            }
            return graphStore.edgeStore.edgeIterator(new ArrayIterator(node.objects, node.objectCount), false);
        }

        @Override
        protected boolean checkElementSpatialMatch(Object element) {
            Edge edge = (Edge) element;
            if (approximate || currentFullyContained || searchRect == null) {
                return true;
            } else {
                // In exact mode, check if edge endpoints intersect with search rect
                Node source = edge.getSource();
                Node target = edge.getTarget();
                SpatialNodeDataImpl sourceSpatialData = ((NodeImpl) source).getSpatialData();
                SpatialNodeDataImpl targetSpatialData = ((NodeImpl) target).getSpatialData();

                return (sourceSpatialData != null && searchRect
                        .intersects(sourceSpatialData.minX, sourceSpatialData.minY, sourceSpatialData.maxX, sourceSpatialData.maxY)) || (targetSpatialData != null && searchRect
                                .intersects(targetSpatialData.minX, targetSpatialData.minY, targetSpatialData.maxX, targetSpatialData.maxY));
            }
        }

        @Override
        protected AbstractQuadTreeSpliterator<Edge> createSplitInstance(QuadTreeNode node, Rect2D searchRect, boolean approximate, int expectedModCount, boolean fullyContained, int size) {
            return new QuadTreeEdgesSpliterator(node, searchRect, approximate, expectedModCount, fullyContained, size);
        }

        @Override
        protected boolean findNext() {
            while (currentIterator != null || !nodesStack.isEmpty()) {
                if (currentIterator != null) {
                    if (currentIterator.hasNext()) {
                        Edge edge = (Edge) currentIterator.next();

                        if (checkElementSpatialMatch(edge)) {
                            next = edge;
                            return true;
                        }
                    } else {
                        currentIterator = null;
                    }
                } else {
                    final QuadTreeNode pointer = nodesStack.pop();
                    currentFullyContained = fullyContainedStack
                            .pop() || (searchRect != null && searchRect.contains(pointer.rect));

                    if (currentFullyContained || searchRect == null || pointer.rect.intersects(searchRect)) {
                        addNode(pointer, currentFullyContained);
                        currentIterator = createIteratorForNode(pointer);
                    }
                }
            }
            return false;
        }

        @Override
        public int characteristics() {
            return DISTINCT | NONNULL;
        }
    }

    private class FilteredQuadTreeEdgesSpliterator extends FilteredSpliterator<Edge, QuadTreeEdgesSpliterator, FilteredQuadTreeEdgesSpliterator> {

        public FilteredQuadTreeEdgesSpliterator(QuadTreeNode root, Rect2D searchRect, boolean approximate, Predicate<? super Edge> predicate) {
            super(new QuadTreeEdgesSpliterator(root, searchRect, approximate), predicate);
        }

        private FilteredQuadTreeEdgesSpliterator(QuadTreeEdgesSpliterator parentSpliterator, Predicate<? super Edge> predicate) {
            super(parentSpliterator, predicate);
        }

        @Override
        protected FilteredQuadTreeEdgesSpliterator createSplitInstance(QuadTreeEdgesSpliterator splitParent, Predicate<? super Edge> predicate) {
            return new FilteredQuadTreeEdgesSpliterator(splitParent, predicate);
        }

        @Override
        protected boolean testPredicate(Edge element) {
            return predicate == null || predicate.test(element);
        }
    }

    /**
     * A spliterator that iterates through all edges in the EdgeStore and filters
     * them based on whether their nodes belong to quad tree nodes that overlap with
     * a search rectangle. This approach iterates edges directly rather than
     * iterating nodes first.
     */
    protected class QuadTreeGlobalEdgesSpliterator implements Spliterator<Edge> {

        private final Rect2D searchRect;
        private final boolean approximate;
        private final Set<QuadTreeNode> overlappingQuadNodes;
        private final Spliterator<Edge> baseSpliterator;
        private final int expectedVersion;
        private final Predicate<? super Edge> additionalPredicate;

        public QuadTreeGlobalEdgesSpliterator(Rect2D searchRect, boolean approximate, Set<QuadTreeNode> overlappingQuadNodes, Predicate<? super Edge> additionalPredicate) {
            this.searchRect = searchRect;
            this.approximate = approximate;
            this.additionalPredicate = additionalPredicate;
            this.expectedVersion = modCount;
            this.overlappingQuadNodes = overlappingQuadNodes;

            // Create the base spliterator from EdgeStore with our predicate
            if (additionalPredicate == null) {
                if (searchRect == null) {
                    // No filtering needed, use the full spliterator
                    this.baseSpliterator = graphStore.edgeStore.spliterator();
                } else {
                    // Only spatial filtering
                    this.baseSpliterator = graphStore.edgeStore.newFilteredSpliterator(this::shouldIncludeEdge);
                }
            } else {
                if (searchRect == null) {
                    this.baseSpliterator = graphStore.edgeStore
                            .newFilteredSpliterator(this::shouldIncludeEdgeAllWithPredicate);
                } else {
                    this.baseSpliterator = graphStore.edgeStore.newFilteredSpliterator(this::shouldIncludeEdge);
                }
            }
        }

        private QuadTreeGlobalEdgesSpliterator(Rect2D searchRect, boolean approximate, Set<QuadTreeNode> overlappingQuadNodes, Spliterator<Edge> baseSpliterator, int expectedVersion, Predicate<? super Edge> additionalPredicate) {
            this.searchRect = searchRect;
            this.approximate = approximate;
            this.overlappingQuadNodes = overlappingQuadNodes;
            this.baseSpliterator = baseSpliterator;
            this.expectedVersion = expectedVersion;
            this.additionalPredicate = additionalPredicate;
        }

        private boolean shouldIncludeEdgeAllWithPredicate(EdgeImpl edge) {
            checkForComodification();

            return additionalPredicate.test(edge);
        }

        /**
         * Determines if an edge should be included based on spatial filtering criteria
         */
        private boolean shouldIncludeEdge(EdgeImpl edge) {
            checkForComodification();

            boolean spatialMatch = false;

            SpatialNodeDataImpl sourceSpatialData = edge.source.getSpatialData();
            SpatialNodeDataImpl targetSpatialData = edge.target.getSpatialData();

            if (sourceSpatialData != null && sourceSpatialData.quadTreeNode != null) {
                spatialMatch = overlappingQuadNodes.contains(sourceSpatialData.quadTreeNode);
            }

            if (!spatialMatch && targetSpatialData != null && targetSpatialData.quadTreeNode != null) {
                // Only check target if source wasn't already overlapping
                spatialMatch = overlappingQuadNodes.contains(targetSpatialData.quadTreeNode);
            }

            // Apply additional predicate if provided
            if (spatialMatch && (additionalPredicate == null || additionalPredicate.test(edge))) {
                if (approximate) {
                    return true;
                } else {
                    // In exact mode, check if edge endpoints intersect with search rect
                    boolean sourceIntersects = sourceSpatialData != null && searchRect
                            .intersects(sourceSpatialData.minX, sourceSpatialData.minY, sourceSpatialData.maxX, sourceSpatialData.maxY);
                    boolean targetIntersects = targetSpatialData != null && searchRect
                            .intersects(targetSpatialData.minX, targetSpatialData.minY, targetSpatialData.maxX, targetSpatialData.maxY);
                    return sourceIntersects || targetIntersects;
                }
            }
            return false;
        }

        private void checkForComodification() {
            if (expectedVersion != modCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super Edge> action) {
            return baseSpliterator.tryAdvance(action);
        }

        @Override
        public Spliterator<Edge> trySplit() {
            Spliterator<Edge> splitBase = baseSpliterator.trySplit();
            if (splitBase == null) {
                return null;
            }

            return new QuadTreeGlobalEdgesSpliterator(searchRect, approximate, overlappingQuadNodes, splitBase,
                    expectedVersion, additionalPredicate);
        }

        @Override
        public long estimateSize() {
            return baseSpliterator.estimateSize();
        }

        @Override
        public int characteristics() {
            return baseSpliterator.characteristics();
        }
    }
}
