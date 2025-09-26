package org.gephi.graph.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.ConcurrentModificationException;
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
    private int modCount = 0;

    public NodesQuadTree(Rect2D rect) {
        this(rect, GraphStoreConfiguration.SPATIAL_INDEX_MAX_LEVELS,
                GraphStoreConfiguration.SPATIAL_INDEX_MAX_OBJECTS_PER_NODE);
    }

    public NodesQuadTree(Rect2D rect, int maxLevels, int maxObjectsPerNode) {
        this.quadTreeRoot = new QuadTreeNode(rect);
        this.maxLevels = maxLevels;
        this.maxObjectsPerNode = maxObjectsPerNode;
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

    public NodeIterable getNodes(float minX, float minY, float maxX, float maxY) {
        return quadTreeRoot.getNodes(new Rect2D(minX, minY, maxX, maxY));
    }

    public NodeIterable getNodes(float minX, float minY, float maxX, float maxY, boolean approximate) {
        return quadTreeRoot.getNodes(new Rect2D(minX, minY, maxX, maxY), approximate);
    }

    public NodeIterable getAllNodes() {
        return quadTreeRoot.getAllNodes();
    }

    public Spliterator<Node> spliterator() {
        return new QuadTreeNodesSpliterator(quadTreeRoot, null);
    }

    public Spliterator<Node> spliterator(Rect2D searchRect) {
        return new QuadTreeNodesSpliterator(quadTreeRoot, searchRect);
    }

    public Spliterator<Node> spliterator(Rect2D searchRect, boolean approximate) {
        return new QuadTreeNodesSpliterator(quadTreeRoot, searchRect, approximate);
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
                spatialData.setQuadTreeNode(null);
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
        readLock();
        try {
            NodeIterable allNodes = getAllNodes();

            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;

            boolean hasNodes = false;

            for (Node node : allNodes) {
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

            // Update size for this node and all parents
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
                    spatialData.setArrayIndex(-1);
                    spatialData.setQuadTreeNode(null);

                    // Update size for this node and all parents
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

                    // Update size for this node and all parents to compensate for the object moving
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
                        spatialData.setArrayIndex(-1);
                        spatialData.setQuadTreeNode(null);
                        objects[i] = null;
                    }
                }
                objects = null;
                objectCount = 0;
            }

            // Reset size
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

        private NodeIterable getAllNodes() {
            return new QuadTreeNodesIterable(null);
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
                for (int i = 0; i < objectCount; i++) {
                    for (int j = 0; j <= level; j++) {
                        sb.append("  ");
                    }

                    sb.append(objects[i].getId()).append('\n');
                }
            }

            if (childTL != null) {
                childTL.toString(sb);
                childTR.toString(sb);
                childBL.toString(sb);
                childBR.toString(sb);
            }
        }
    }

    private class QuadTreeNodesIterable implements NodeIterable {

        private final Rect2D searchRect;
        private final boolean approximate;

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

    private class QuadTreeNodesIterator implements Iterator<Node> {

        private final Rect2D searchRect;
        private final boolean approximate;
        private final Deque<QuadTreeNode> nodesStack = new ArrayDeque<>();
        private final Deque<Boolean> fullyContainedStack = new ArrayDeque<>();

        // Current:
        private Iterator<NodeImpl> currentIterator;
        private boolean currentFullyContained;
        private boolean finished = false;

        private NodeImpl next;

        public QuadTreeNodesIterator(QuadTreeNode root, Rect2D searchRect) {
            this(root, searchRect, false);
        }

        public QuadTreeNodesIterator(QuadTreeNode root, Rect2D searchRect, boolean approximate) {
            this.searchRect = searchRect;
            this.approximate = approximate;

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

                        if (approximate || currentFullyContained) {
                            // In approximate mode or when fully contained, include all objects
                            next = elem;
                            return true;
                        } else {
                            // In exact mode, check intersection
                            final SpatialNodeDataImpl spatialData = elem.getSpatialData();
                            if (searchRect
                                    .intersects(spatialData.minX, spatialData.minY, spatialData.maxX, spatialData.maxY)) {
                                next = elem;
                                return true;
                            }
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
        private int index = 0;

        public ArrayIterator(NodeImpl[] array, int size) {
            this.array = array;
            this.size = size;
        }

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public NodeImpl next() {
            if (index >= size) {
                throw new IllegalStateException("No more elements");
            }
            return array[index++];
        }
    }

    private class QuadTreeNodesSpliterator implements Spliterator<Node> {
        private final Rect2D searchRect;
        private final boolean approximate;
        private final Deque<QuadTreeNode> nodesStack = new ArrayDeque<>();
        private final Deque<Boolean> fullyContainedStack = new ArrayDeque<>();

        private final int expectedModCount;
        private Iterator<NodeImpl> currentIterator;
        private boolean currentFullyContained;
        private NodeImpl next;
        private int remainingSize;

        public QuadTreeNodesSpliterator(QuadTreeNode root, Rect2D searchRect) {
            this(root, searchRect, false);
        }

        public QuadTreeNodesSpliterator(QuadTreeNode root, Rect2D searchRect, boolean approximate) {
            this.searchRect = searchRect;
            this.approximate = approximate;
            this.expectedModCount = modCount;

            // Null rect means get all
            currentFullyContained = searchRect == null;

            // Initialize with root
            addNode(root, currentFullyContained);
            currentIterator = root.objects != null ? new ArrayIterator(root.objects, root.objectCount) : null;

            // For SIZED characteristic, we need exact count
            if (searchRect == null) {
                // Getting all nodes, so we can use the maintained size
                remainingSize = root.size;
            } else if (approximate) {
                // In approximate mode, count all nodes in intersecting quadrants
                remainingSize = countNodesInRectApproximate(root, searchRect);
            } else {
                // Need to count nodes in the search rect
                remainingSize = countNodesInRect(root, searchRect);
            }
        }

        private QuadTreeNodesSpliterator(QuadTreeNode node, Rect2D searchRect, boolean approximate, int expectedModCount, boolean fullyContained, int size) {
            this.searchRect = searchRect;
            this.approximate = approximate;
            this.expectedModCount = expectedModCount;
            this.remainingSize = size;
            this.currentFullyContained = fullyContained;

            if (node != null) {
                addNode(node, fullyContained);
                currentIterator = node.objects != null ? new ArrayIterator(node.objects, node.objectCount) : null;
            } else {
                currentIterator = null;
            }
        }

        private void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        private void addNode(QuadTreeNode node, boolean fullyContained) {
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

        private int countNodesInRect(QuadTreeNode node, Rect2D rect) {
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

        private int countNodesInRectApproximate(QuadTreeNode node, Rect2D rect) {
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
        public boolean tryAdvance(Consumer<? super Node> action) {
            checkForComodification();

            if (next != null || findNext()) {
                action.accept(next);
                next = null;
                remainingSize--;
                return true;
            }
            return false;
        }

        private boolean findNext() {
            while (currentIterator != null || !nodesStack.isEmpty()) {
                if (currentIterator != null) {
                    while (currentIterator.hasNext()) {
                        final NodeImpl elem = currentIterator.next();

                        if (approximate || currentFullyContained || searchRect == null) {
                            // In approximate mode, when fully contained, or getting all nodes
                            next = elem;
                            return true;
                        } else {
                            // In exact mode, check intersection
                            final SpatialNodeDataImpl spatialData = elem.getSpatialData();
                            if (searchRect
                                    .intersects(spatialData.minX, spatialData.minY, spatialData.maxX, spatialData.maxY)) {
                                next = elem;
                                return true;
                            }
                        }
                    }
                    currentIterator = null;
                } else {
                    final QuadTreeNode pointer = nodesStack.pop();
                    currentFullyContained = fullyContainedStack
                            .pop() || (searchRect != null && searchRect.contains(pointer.rect));

                    if (currentFullyContained || searchRect == null || pointer.rect.intersects(searchRect)) {
                        addNode(pointer, currentFullyContained);
                        currentIterator = pointer.objects != null
                                ? new ArrayIterator(pointer.objects, pointer.objectCount) : null;
                    }
                }
            }
            return false;
        }

        @Override
        public Spliterator<Node> trySplit() {
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
                    QuadTreeNodesSpliterator split = new QuadTreeNodesSpliterator(null, searchRect, approximate,
                            expectedModCount, false, splitSize);

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

        @Override
        public int characteristics() {
            return DISTINCT | SIZED | SUBSIZED | NONNULL;
        }
    }
}
