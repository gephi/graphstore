package org.gephi.graph.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Rect2D;

/**
 * Adapted from https://bitbucket.org/C3/quadtree/wiki/Home
 *
 * TODO: unit tests!!
 * TODO: almost the same as NodesQuadTree, maybe generate code with templating-maven-plugin
 * @author Eduardo Ramos
 */
public class EdgesQuadTree {

    private static final int MAX_OBJECTS_PER_NODE = 2;
    private static final int MAX_LEVELS = 16;

    private final GraphLock lock = new GraphLock();
    private Map<Edge, QuadTreeObject> wrappedDictionary = new LinkedHashMap<>();

    private QuadTreeNode quadTreeRoot;

    public EdgesQuadTree(Rect2D rect) {
        quadTreeRoot = new QuadTreeNode(rect);
    }

    public EdgesQuadTree(float dimensionMax) {
        this(-dimensionMax, -dimensionMax, dimensionMax, dimensionMax);
    }

    public EdgesQuadTree(float minX, float minY, float maxX, float maxY) {
        quadTreeRoot = new QuadTreeNode(new Rect2D(minX, minY, maxX, maxY));
    }

    public Rect2D quadRect() {
        return quadTreeRoot.quadRect();
    }

    public EdgeIterable getEdges(Rect2D rect) {
        return quadTreeRoot.getEdges(rect);
    }

    public void getEdges(Rect2D rect, Consumer<Edge> callback) {
        quadTreeRoot.getEdges(rect, callback);
    }

    public EdgeIterable getEdges(float minX, float minY, float maxX, float maxY) {
        return quadTreeRoot.getEdges(new Rect2D(minX, minY, maxX, maxY));
    }

    public void getEdges(float minX, float minY, float maxX, float maxY, Consumer<Edge> callback) {
        quadTreeRoot.getEdges(new Rect2D(minX, minY, maxX, maxY), callback);
    }

    public EdgeIterable getAllEdges() {
        return quadTreeRoot.getAllEdges();
    }

    public void getAllEdges(Consumer<Edge> callback) {
        quadTreeRoot.getAllEdges(callback);
    }

    public boolean updateEdge(Edge item, float minX, float minY, float maxX, float maxY) {
        writeLock();
        try {
            final QuadTreeObject obj = wrappedDictionary.get(item);
            if (obj != null) {
                obj.updateItemCoords(minX, minY, maxX, maxY);
                quadTreeRoot.update(obj);
                return true;
            } else {
                return false;
            }
        } finally {
            writeUnlock();
        }
    }

    public boolean addEdge(Edge item, float minX, float minY, float maxX, float maxY) {
        writeLock();
        try {
            if (!containsEdge(item)) {
                final QuadTreeObject wrappedObject = new QuadTreeObject(item, minX, minY, maxX, maxY);
                wrappedDictionary.put(item, wrappedObject);
                quadTreeRoot.insert(wrappedObject);
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
            wrappedDictionary.clear();
            quadTreeRoot.clear();
        } finally {
            writeUnlock();
        }
    }

    public boolean containsEdge(Edge item) {
        readLock();
        try {
            return wrappedDictionary.containsKey(item);
        } finally {
            readUnlock();
        }
    }

    public int count() {
        readLock();
        try {
            return wrappedDictionary.size();
        } finally {
            readUnlock();
        }
    }

    public boolean removeEdge(Edge item) {
        writeLock();
        try {
            final QuadTreeObject obj = wrappedDictionary.get(item);
            if (obj != null) {
                quadTreeRoot.delete(obj, true);
                wrappedDictionary.remove(item);
                return true;
            } else {
                return false;
            }
        } finally {
            writeUnlock();
        }
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

    private class QuadTreeObject {

        private final Edge data;
        private float minX, minY, maxX, maxY;

        private QuadTreeNode owner;

        public QuadTreeObject(Edge data, float minX, float minY, float maxX, float maxY) {
            this.data = data;
            updateItemCoords(minX, minY, maxX, maxY);
        }

        private void updateItemCoords(float minX, float minY, float maxX, float maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    private class QuadTreeNode {

        private Set<QuadTreeObject> objects = null;
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

        private void add(QuadTreeObject item) {
            if (objects == null) {
                objects = new LinkedHashSet<>();
            }

            item.owner = this;
            objects.add(item);
        }

        private void remove(QuadTreeObject item) {
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
            final Iterator<QuadTreeObject> iterator = objects.iterator();
            while (iterator.hasNext()) {
                QuadTreeObject obj = iterator.next();
                QuadTreeNode destTree = getDestinationTree(obj);
                if (destTree != this) {
                    // Insert to the appropriate tree, remove the object, and
                    // back up one in the loop
                    destTree.insert(obj);

                    iterator.remove();
                }
            }
        }

        private QuadTreeNode getDestinationTree(QuadTreeObject item) {
            // If a child can't contain an object, it will live in this Quad
            final QuadTreeNode destTree;

            final float minX = item.minX;
            final float minY = item.minY;
            final float maxX = item.maxX;
            final float maxY = item.maxY;

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

        private void relocate(QuadTreeObject item) {
            // Are we still inside our parent?
            if (quadRect().contains(item.minX, item.minY, item.maxX, item.maxY)) {
                // Good, have we moved inside any of our children?
                if (childTL != null) {
                    QuadTreeNode dest = getDestinationTree(item);
                    if (item.owner != dest) {
                        // Delete the item from this quad and add it to our
                        // child
                        // Note: Do NOT clean during this call, it can
                        // potentially delete our destination quad
                        QuadTreeNode formerOwner = item.owner;
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

        private void delete(QuadTreeObject item, boolean clean) {
            if (item.owner != null) {
                if (item.owner == this) {
                    remove(item);
                    if (clean) {
                        cleanUpwards();
                    }
                } else {
                    item.owner.delete(item, clean);
                }
            }
        }

        private void insert(QuadTreeObject item) {
            // If this quad doesn't contain the items rectangle, do nothing,
            // unless we are the root
            if (!rect.contains(item.minX, item.minY, item.maxX, item.maxY)) {
                if (parent == null) {
                    // This object is outside of the QuadTreeXNA bounds, we
                    // should add it at the root level
                    add(item);
                } else {
                    throw new IllegalStateException(
                            "We are not the root, and this object doesn't fit here. How did we get here?");
                }
            }

            if (objects == null || (childTL == null && (level >= MAX_LEVELS || objects.size() + 1 <= MAX_OBJECTS_PER_NODE))) {
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

        private EdgeIterable getEdges(Rect2D searchRect) {
            return new QuadTreeEdgesIterable(searchRect);
        }

        private EdgeIterable getAllEdges() {
            return new QuadTreeEdgesIterable(null);
        }

        private void getEdges(Rect2D searchRect, Consumer<Edge> callback) {
            if (searchRect.contains(this.rect)) {
                this.getAllEdges(callback);
            } else if (searchRect.intersects(this.rect)) {
                if (objects != null && !objects.isEmpty()) {
                    for (QuadTreeObject obj : objects) {
                        if (searchRect.intersects(obj.minX, obj.minY, obj.maxX, obj.maxY)) {
                            callback.accept(obj.data);
                        }
                    }
                }

                if (childTL != null) {
                    childTL.getEdges(searchRect, callback);
                    childTR.getEdges(searchRect, callback);
                    childBL.getEdges(searchRect, callback);
                    childBR.getEdges(searchRect, callback);
                }
            }
        }

        private void getAllEdges(Consumer<Edge> callback) {
            if (objects != null && !objects.isEmpty()) {
                for (QuadTreeObject obj : objects) {
                    callback.accept(obj.data);
                }
            }

            if (childTL != null) {
                childTL.getAllEdges(callback);
                childTR.getAllEdges(callback);
                childBL.getAllEdges(callback);
                childBR.getAllEdges(callback);
            }
        }

        private void update(QuadTreeObject item) {
            if (item.owner != null) {
                item.owner.relocate(item);
            } else {
                relocate(item);
            }
        }

        public void toString(StringBuilder sb) {
            for (int i = 0; i < level; i++) {
                sb.append("  ");
            }
            sb.append(rect.toString()).append('\n');

            if (objects != null) {
                for (QuadTreeObject object : objects) {
                    for (int i = 0; i <= level; i++) {
                        sb.append("  ");
                    }

                    sb.append(object.data.getId()).append('\n');
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

    private class QuadTreeEdgesIterable implements EdgeIterable {

        private final Rect2D searchRect;

        public QuadTreeEdgesIterable(Rect2D searchRect) {
            this.searchRect = searchRect;
        }

        @Override
        public Iterator<Edge> iterator() {
            return new QuadTreeEdgesIterator(quadTreeRoot, searchRect);
        }

        @Override
        public Edge[] toArray() {
            final Collection<Edge> collection = toCollection();
            return collection.toArray(new Edge[collection.size()]);
        }

        @Override
        public Collection<Edge> toCollection() {
            final List<Edge> list = new ArrayList<>();

            final Iterator<Edge> iterator = iterator();
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }

            return list;
        }

        @Override
        public void doBreak() {
            readUnlock();
        }

    }

    private class QuadTreeEdgesIterator implements Iterator<Edge> {

        private final Rect2D searchRect;
        private final Deque<QuadTreeNode> nodesStack = new ArrayDeque<>();
        private final Deque<Boolean> fullyContainedStack = new ArrayDeque<>();

        // Current:
        private Iterator<QuadTreeObject> currentIterator;
        private boolean currentFullyContained = false;
        private boolean finished = false;

        private QuadTreeObject next;

        public QuadTreeEdgesIterator(QuadTreeNode root, Rect2D searchRect) {
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
                        final QuadTreeObject elem = currentIterator.next();

                        if (currentFullyContained || searchRect.intersects(elem.minX, elem.minY, elem.maxX, elem.maxY)) {
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
        public Edge next() {
            if (next == null) {
                throw new IllegalStateException("No next available!");
            }

            final Edge edge = next.data;

            next = null;

            return edge;
        }

    }
}
