package org.gephi.graph.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    public NodeIterable getNodes(float minX, float minY, float maxX, float maxY) {
        return quadTreeRoot.getNodes(new Rect2D(minX, minY, maxX, maxY));
    }

    public NodeIterable getAllNodes() {
        return quadTreeRoot.getAllNodes();
    }

    public boolean updateNode(NodeImpl item, float minX, float minY, float maxX, float maxY) {
        writeLock();
        try {
            final SpatialNodeDataImpl obj = item.getSpatialData();
            if (obj != null) {
                obj.updateBoundaries(minX, minY, maxX, maxY);
                quadTreeRoot.update(item);
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

        private Set<NodeImpl> objects = null;
        private final Rect2D rect; // The area this QuadTree represents

        private final QuadTreeNode parent; // The parent of this quad
        private final int level;

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
            return objectCount();
        }

        public boolean isEmptyLeaf() {
            return count() == 0 && childTL == null;
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
                objects = new LinkedHashSet<>();
            }

            item.getSpatialData().setQuadTreeNode(this);
            objects.add(item);
        }

        private void remove(NodeImpl item) {
            if (objects != null) {
                objects.remove(item);
            }
        }

        private int objectCount() {
            int count = 0;

            // add the objects at this level
            if (objects != null) {
                count += objects.size();
            }

            // add the objects that are contained in the children
            if (childTL != null) {
                count += childTL.objectCount();
                count += childTR.objectCount();
                count += childBL.objectCount();
                count += childBR.objectCount();
            }

            return count;
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

            // If they're completely contained by the quad, bump objects down
            final Iterator<NodeImpl> iterator = objects.iterator();
            while (iterator.hasNext()) {
                NodeImpl obj = iterator.next();
                QuadTreeNode destTree = getDestinationTree(obj);
                if (destTree != this) {
                    // Insert to the appropriate tree, remove the object, and
                    // back up one in the loop
                    destTree.insert(obj);

                    iterator.remove();
                }
            }
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
                objects.clear();
                objects = null;
            }

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

            if (objects == null || (childTL == null && (level >= maxLevels || objects
                    .size() + 1 <= maxObjectsPerNode))) {
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

        public void toString(StringBuilder sb) {
            for (int i = 0; i < level; i++) {
                sb.append("  ");
            }
            sb.append(rect.toString()).append('\n');

            if (objects != null) {
                for (NodeImpl object : objects) {
                    for (int i = 0; i <= level; i++) {
                        sb.append("  ");
                    }

                    sb.append(object.getId()).append('\n');
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

        public QuadTreeNodesIterable(Rect2D searchRect) {
            this.searchRect = searchRect;
        }

        @Override
        public Iterator<Node> iterator() {
            return new QuadTreeNodesIterator(quadTreeRoot, searchRect);
        }

        @Override
        public Node[] toArray() {
            final Collection<Node> collection = toCollection();
            return collection.toArray(new Node[0]);
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
        private final Deque<QuadTreeNode> nodesStack = new ArrayDeque<>();
        private final Deque<Boolean> fullyContainedStack = new ArrayDeque<>();

        // Current:
        private Iterator<NodeImpl> currentIterator;
        private boolean currentFullyContained;
        private boolean finished = false;

        private NodeImpl next;

        public QuadTreeNodesIterator(QuadTreeNode root, Rect2D searchRect) {
            this.searchRect = searchRect;

            readLock();

            // Null rect means get all
            currentFullyContained = searchRect == null;

            // We always add the root and don't test for the root being fully
            // contained, to correctly handle the case of nodes out of the quad
            // tree bounds
            addChildrenToVisit(root, currentFullyContained);
            currentIterator = root.objects != null ? root.objects.iterator() : null;
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
                        final SpatialNodeDataImpl spatialData = elem.getSpatialData();

                        if (currentFullyContained || searchRect
                                .intersects(spatialData.minX, spatialData.minY, spatialData.maxX, spatialData.maxY)) {
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
                        currentIterator = pointer.objects != null ? pointer.objects.iterator() : null;
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
}
