package org.gephi.graph.store;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Collection;
import java.util.Iterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.NodeIterator;

/**
 *
 * @author mbastian
 */
public class NodeStore implements Collection<Node>, NodeIterable {

    public final static int NULL_ID = -1;
    public final static int BLOCK_SIZE = 5000;
    public final static int DEFAULT_BLOCKS = 10;
    public final static float DEFAULT_LOAD_FACTOR = .7f;
    //Store
    protected final EdgeStore edgeStore;
    //Locking (optional)
    protected final GraphLock lock;
    //Data
    protected int size;
    protected int garbageSize;
    protected int blocksCount;
    protected int currentBlockIndex;
    protected NodeBlock blocks[];
    protected NodeBlock currentBlock;
    protected Object2IntOpenHashMap dictionary;

    public NodeStore() {
        initStore();
        this.lock = null;
        this.edgeStore = null;
    }

    public NodeStore(final EdgeStore edgeStore, final GraphLock lock) {
        initStore();
        this.lock = lock;
        this.edgeStore = edgeStore;
    }

    private void initStore() {
        this.size = 0;
        this.garbageSize = 0;
        this.blocksCount = 1;
        this.currentBlockIndex = 0;
        this.blocks = new NodeBlock[DEFAULT_BLOCKS];
        this.blocks[0] = new NodeBlock(0);
        this.currentBlock = blocks[currentBlockIndex];
        this.dictionary = new Object2IntOpenHashMap(BLOCK_SIZE, DEFAULT_LOAD_FACTOR);
        this.dictionary.defaultReturnValue(NULL_ID);
    }

    private void ensureCapacity(final int capacity) {
        assert capacity > 0;

        int blockCapacity = currentBlock.getCapacity();
        while (capacity > blockCapacity) {
            if (currentBlockIndex == blocksCount - 1) {
                int blocksNeeded = (int) Math.ceil((capacity - blockCapacity) / (double) BLOCK_SIZE);
                for (int i = 0; i < blocksNeeded; i++) {
                    if (blocksCount == blocks.length) {
                        NodeBlock[] newBlocks = new NodeBlock[blocksCount + 1];
                        System.arraycopy(blocks, 0, newBlocks, 0, blocks.length);
                        blocks = newBlocks;
                    }
                    NodeBlock block = blocks[blocksCount];
                    if (block == null) {
                        block = new NodeBlock(blocksCount);
                        blocks[blocksCount] = block;
                    }
                    if (blockCapacity == 0 && i == 0) {
                        currentBlockIndex = blocksCount;
                        currentBlock = block;
                    }
                    blocksCount++;
                }
                break;
            } else {
                currentBlockIndex++;
                currentBlock = blocks[currentBlockIndex];
                blockCapacity = currentBlock.getCapacity();
            }
        }
    }

    private void trimDictionary() {
        dictionary.trim(Math.max(BLOCK_SIZE, size * 2));
    }

    public NodeImpl get(final int id) {
        checkValidId(id);

        return blocks[id / BLOCK_SIZE].get(id);
    }

    public NodeImpl get(final Object id) {
        int index = dictionary.getInt(id);
        if (index != NodeStore.NULL_ID) {
            return get(index);
        }
        return null;
    }

    @Override
    public void clear() {
        for (NodeStoreIterator itr = new NodeStoreIterator(); itr.hasNext();) {
            NodeImpl node = itr.next();
            node.setStoreId(NodeStore.NULL_ID);
        }
        initStore();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public NodeStoreIterator iterator() {
        return new NodeStoreIterator();
    }

    @Override
    public NodeImpl[] toArray() {
        readLock();

        NodeImpl[] array = new NodeImpl[size];
        if (garbageSize == 0) {
            for (int i = 0; i < blocksCount; i++) {
                NodeBlock block = blocks[i];
                System.arraycopy(block.backingArray, 0, array, block.offset, block.nodeLength);
            }
        } else {
            NodeStoreIterator itr = iterator();
            int offset = 0;
            while (itr.hasNext()) {
                NodeImpl n = itr.next();
                array[offset++] = n;
            }
        }

        readUnlock();

        return array;
    }

    @Override
    public <T> T[] toArray(T[] array) {
        checkNonNullObject(array);

        readLock();

        if (array.length < size()) {
            array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size());
        }
        if (garbageSize == 0) {
            for (int i = 0; i < blocksCount; i++) {
                NodeBlock block = blocks[i];
                System.arraycopy(block.backingArray, 0, array, block.offset, block.nodeLength);
            }
        } else {
            NodeStoreIterator itr = iterator();
            int offset = 0;
            while (itr.hasNext()) {
                NodeImpl n = itr.next();
                array[offset++] = (T) n;
            }
        }

        readUnlock();
        return array;
    }

    @Override
    public boolean add(final Node n) {
        checkNonNullNodeObject(n);

        NodeImpl node = (NodeImpl) n;
        if (node.storeId == NodeStore.NULL_ID) {
            checkIdDoesntExist(n.getId());

            if (garbageSize > 0) {
                for (int i = 0; i < blocksCount; i++) {
                    NodeBlock nodeBlock = blocks[i];
                    if (nodeBlock.hasGarbage()) {
                        nodeBlock.set(node);
                        garbageSize--;
                        dictionary.put(node.getId(), node.storeId);
                        break;
                    }
                }
            } else {
                ensureCapacity(1);
                currentBlock.add(node);
                dictionary.put(node.getId(), node.storeId);
            }
            size++;
            return true;
        } else if (isValidIndex(node.storeId) && get(node.storeId) == node) {
            return false;
        } else {
            throw new IllegalArgumentException("The node already belongs to another store");
        }
    }

    @Override
    public boolean remove(final Object o) {
        checkNonNullNodeObject(o);

        NodeImpl node = (NodeImpl) o;
        int id = node.storeId;
        if (id != NodeStore.NULL_ID) {
            checkNodeExists(node);

            int storeIndex = id / BLOCK_SIZE;
            NodeBlock block = blocks[storeIndex];
            block.remove(node);
            size--;
            garbageSize++;
            dictionary.remove(node.getId());
            trimDictionary();
            for (int i = storeIndex; i == (blocksCount - 1) && block.garbageLength == block.nodeLength && i >= 0;) {
                if (i != 0) {
                    blocks[i] = null;
                    blocksCount--;
                    garbageSize -= block.nodeLength;
                    block = blocks[--i];
                    currentBlock = block;
                    currentBlockIndex--;
                } else {
                    currentBlock.clear();
                    garbageSize = 0;
                    break;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(final Object o) {
        checkNonNullNodeObject(o);

        NodeImpl node = (NodeImpl) o;
        int id = node.getStoreId();
        if (id != NodeStore.NULL_ID) {
            if (get(id) == node) {
                return true;
            }
        }

        return false;
    }

    public boolean containsId(final Object id) {
        checkNonNullObject(id);

        return dictionary.containsKey(id);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            int found = 0;
            for (Object o : c) {
                if (contains((NodeImpl) o)) {
                    found++;
                }
            }
            return found == c.size();
        }
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends Node> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            int capacityNeeded = c.size() - garbageSize;
            if (capacityNeeded > 0) {
                ensureCapacity(capacityNeeded);
            }
            boolean changed = false;
            Iterator<? extends Node> itr = c.iterator();
            while (itr.hasNext()) {
                Node e = itr.next();
                if (add(e)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            boolean changed = false;
            Iterator itr = c.iterator();
            while (itr.hasNext()) {
                Object o = itr.next();
                if (remove(o)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        checkCollection(c);

        if (!c.isEmpty()) {
            ObjectSet<NodeImpl> set = new ObjectOpenHashSet(c.size());
            for (Object o : c) {
                checkNonNullObject(o);
                checkNodeExists((NodeImpl) o);
                set.add((NodeImpl) o);
            }

            boolean changed = false;
            NodeStoreIterator itr = iterator();
            while (itr.hasNext()) {
                NodeImpl e = itr.next();
                if (!set.contains(e)) {
                    itr.remove();
                    changed = true;
                }
            }
            return changed;
        } else {
            clear();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.size;
        NodeStoreIterator itr = this.iterator();
        while (itr.hasNext()) {
            hash = 67 * hash + itr.next().hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NodeStore other = (NodeStore) obj;
        if (this.size != other.size) {
            return false;
        }
        NodeStoreIterator itr1 = this.iterator();
        NodeStoreIterator itr2 = other.iterator();
        while (itr1.hasNext()) {
            if (!itr2.hasNext()) {
                return false;
            }
            if (!itr1.next().equals(itr2.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void doBreak() {
        readUnlock();
    }

    void readLock() {
        if (lock != null) {
            lock.readLock();
        }
    }

    void readUnlock() {
        if (lock != null) {
            lock.readUnlock();
        }
    }

    void checkWriteLock() {
        if (lock != null) {
            lock.checkHoldWriteLock();
        }
    }

    private void checkIdDoesntExist(Object id) {
        if (dictionary.containsKey(id)) {
            throw new IllegalArgumentException("The node id already exist");
        }
    }

    private boolean isValidIndex(int id) {
        if (id < 0 || id >= currentBlock.offset + currentBlock.nodeLength) {
            return false;
        }
        return true;
    }

    void checkNonNullObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }

    void checkNonNullNodeObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof NodeImpl)) {
            throw new ClassCastException("Object must be a NodeImpl object");
        }
    }

    void checkNodeExists(final NodeImpl node) {
        if (get(node.storeId) != node) {
            throw new IllegalArgumentException("The node is invalid");
        }
    }

    void checkValidId(final int id) {
        if (id < 0 || !isValidIndex(id)) {
            throw new IllegalArgumentException("Node id=" + id + " is invalid");
        }
    }

    void checkCollection(final Collection<?> collection) {
        if (collection == this) {
            throw new IllegalArgumentException("Can't pass itself");
        }
    }

    protected static class NodeBlock {

        protected final int offset;
        protected final short[] garbageArray;
        protected final NodeImpl[] backingArray;
        protected int nodeLength;
        protected int garbageLength;

        public NodeBlock(int index) {
            this.offset = index * BLOCK_SIZE;
            if (BLOCK_SIZE >= Short.MAX_VALUE - Short.MIN_VALUE) {
                throw new RuntimeException("BLOCK SIZE can't exceed 65535");
            }
            this.garbageArray = new short[BLOCK_SIZE];
            this.backingArray = new NodeImpl[BLOCK_SIZE];
        }

        public boolean hasGarbage() {
            return garbageLength > 0;
        }

        public int getCapacity() {
            return BLOCK_SIZE - nodeLength - garbageLength;
        }

        public void add(NodeImpl k) {
            int i = nodeLength++;
            backingArray[i] = k;
            k.setStoreId(i + offset);
        }

        public void set(NodeImpl k) {
            int i = garbageArray[--garbageLength] - Short.MIN_VALUE;
            backingArray[i] = k;
            k.setStoreId(i + offset);
        }

        public NodeImpl get(int id) {
            return backingArray[id - offset];
        }

        public void remove(NodeImpl k) {
            int i = k.getStoreId() - offset;
            backingArray[i] = null;
            garbageArray[garbageLength++] = (short) (i + Short.MIN_VALUE);
            k.setStoreId(NULL_ID);
        }

        public void clear() {
            nodeLength = 0;
            garbageLength = 0;
        }
    }

    protected final class NodeStoreIterator implements Iterator<Node>, NodeIterator {

        protected int blockIndex;
        protected NodeImpl[] backingArray;
        protected int blockLength;
        protected int cursor;
        protected NodeImpl pointer;

        public NodeStoreIterator() {
            this.backingArray = blocks[blockIndex].backingArray;
            this.blockLength = blocks[blockIndex].nodeLength;
            readLock();
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (cursor == blockLength || ((pointer = backingArray[cursor++]) == null)) {
                if (cursor == blockLength) {
                    if (++blockIndex < blocksCount) {
                        backingArray = blocks[blockIndex].backingArray;
                        blockLength = blocks[blockIndex].nodeLength;
                        cursor = 0;
                    } else {
                        break;
                    }
                }
            }
            if (pointer == null) {
                readUnlock();
                return false;
            }
            return true;
        }

        @Override
        public NodeImpl next() {
            return pointer;
        }

        @Override
        public void remove() {
            checkWriteLock();
            if (edgeStore != null) {
                for (EdgeStore.EdgeInOutIterator edgeIterator = edgeStore.edgeIterator(pointer); edgeIterator.hasNext();) {
                    edgeIterator.next();
                    edgeIterator.remove();
                }
            }
            NodeStore.this.remove(pointer);
        }
    }
}
