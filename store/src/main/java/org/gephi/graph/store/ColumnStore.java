package org.gephi.graph.store;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortHeapPriorityQueue;
import it.unimi.dsi.fastutil.shorts.ShortPriorityQueue;
import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public class ColumnStore<T extends Element> {

    //Const
    public final static int NULL_ID = -1;
    public final static int MAX_SIZE = 65534;
    public final static short NULL_SHORT = Short.MIN_VALUE;
    //Element
    protected final Class<T> elementType;
    //Columns
    protected final Object2ShortMap<String> idMap;
    protected final ColumnImpl[] columns;
    protected final ShortPriorityQueue garbageQueue;
    //Index
    protected final IndexStore<T> indexStore;
    //Locking (optional)
    protected final GraphLock lock;
    //Variables
    protected int length;

    public ColumnStore(Class<T> elementType, boolean indexed) {
        this(elementType, indexed, null);
    }

    public ColumnStore(Class<T> elementType, boolean indexed, GraphLock lock) {
        if (MAX_SIZE >= Short.MAX_VALUE - Short.MIN_VALUE + 1) {
            throw new RuntimeException("Column Store size can't exceed 65534");
        }
        this.lock = lock;
        this.garbageQueue = new ShortHeapPriorityQueue(MAX_SIZE);
        this.idMap = new Object2ShortOpenHashMap<String>(MAX_SIZE);
        this.columns = new ColumnImpl[MAX_SIZE];
        this.elementType = elementType;
        this.indexStore = indexed ? new IndexStore<T>(this) : null;
        idMap.defaultReturnValue(NULL_SHORT);
    }

    public void addColumn(final Column column) {
        checkNonNullColumnObject(column);
        checkIndexStatus(column);

        writeLock();
        try {
            final ColumnImpl columnImpl = (ColumnImpl) column;
            short id = idMap.getShort(columnImpl.getId());
            if (id == NULL_SHORT) {
                if (!garbageQueue.isEmpty()) {
                    id = garbageQueue.dequeueShort();
                } else {
                    id = intToShort(length);
                    if (length >= MAX_SIZE) {
                        throw new RuntimeException("Maximum number of edge types reached at " + MAX_SIZE);
                    }
                    length++;
                }
                idMap.put(column.getId(), id);
                int intIndex = shortToInt(id);
                columnImpl.setStoreId(intIndex);
                columns[intIndex] = columnImpl;
                if (indexStore != null) {
                    indexStore.addColumn(columnImpl);
                }
            } else {
                throw new IllegalArgumentException("The column already exist");
            }
        } finally {
            writeUnlock();
        }
    }

    public void removeColumn(final Column column) {
        checkNonNullColumnObject(column);

        writeLock();
        try {
            final ColumnImpl columnImpl = (ColumnImpl) column;
            short id = idMap.removeShort(column.getId());
            if (id == NULL_SHORT) {
                throw new IllegalArgumentException("The column doesnt exist");
            }
            garbageQueue.enqueue(id);
            columnImpl.setStoreId(NULL_ID);

            int intId = shortToInt(id);
            columns[intId] = null;
            if (indexStore != null) {
                indexStore.removeColumn((ColumnImpl) column);
            }
        } finally {
            writeUnlock();
        }
    }

    public void removeColumn(final String key) {
        checkNonNullObject(key);
        readLock();
        try {
            removeColumn(getColumn(key));
        } finally {
            readUnlock();
        }
    }

    public int getColumnIndex(final String key) {
        checkNonNullObject(key);
        readLock();
        try {
            short id = idMap.getShort(key);
            if (id == NULL_SHORT) {
                throw new IllegalArgumentException("The column doesnt exist");
            }
            return shortToInt(id);
        } finally {
            readUnlock();
        }
    }

    public Column getColumnByIndex(final int index) {
        readLock();
        try {
            if (index < 0 || index >= columns.length) {
                throw new IllegalArgumentException("The column doesnt exist");
            }
            ColumnImpl a = columns[index];
            if (a == null) {
                throw new IllegalArgumentException("The column doesnt exist");
            }
            return a;
        } finally {
            readUnlock();
        }
    }

    public Column getColumn(final String key) {
        checkNonNullObject(key);
        readLock();
        try {
            short id = idMap.getShort(key);
            if (id == NULL_SHORT) {
                throw new IllegalArgumentException("The column doesnt exist");
            }
            return columns[shortToInt(id)];
        } finally {
            readUnlock();
        }
    }

    public boolean hasColumn(String key) {
        checkNonNullObject(key);
        readLock();
        try {
            return idMap.containsKey(key);
        } finally {
            readUnlock();
        }
    }

    public Set<String> getPropertyKeys() {
        readLock();
        try {
            return new ObjectOpenHashSet<String>(idMap.keySet());
        } finally {
            readUnlock();
        }
    }

    public int size() {
        return length - garbageQueue.size();
    }

    short intToShort(final int id) {
        return (short) (id + Short.MIN_VALUE + 1);
    }

    int shortToInt(final short id) {
        return id - Short.MIN_VALUE - 1;
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

    void writeLock() {
        if (lock != null) {
            lock.writeLock();
        }
    }

    void writeUnlock() {
        if (lock != null) {
            lock.writeUnlock();
        }
    }

    void checkNonNullObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }

    void checkNonNullColumnObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof ColumnImpl)) {
            throw new ClassCastException("Must be ColumnImpl object");
        }
    }

    void checkIndexStatus(final Column column) {
        if (indexStore == null && column.isIndexed()) {
            throw new IllegalArgumentException("Can't add an indexed column to a non indexed store");
        }
    }
}
