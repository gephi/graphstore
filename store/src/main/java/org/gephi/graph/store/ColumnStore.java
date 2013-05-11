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
package org.gephi.graph.store;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortRBTreeSet;
import it.unimi.dsi.fastutil.shorts.ShortSortedSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public class ColumnStore<T extends Element> implements Iterable<Column> {

    //Config
    protected final static int MAX_SIZE = 65534;
    //Const
    protected final static int NULL_ID = -1;
    protected final static short NULL_SHORT = Short.MIN_VALUE;
    //Element
    protected final Class<T> elementType;
    //Columns
    protected final Object2ShortMap<String> idMap;
    protected final ColumnImpl[] columns;
    protected final TimestampMap[] timestampMaps;
    protected final ShortSortedSet garbageQueue;
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
        this.garbageQueue = new ShortRBTreeSet();
        this.idMap = new Object2ShortOpenHashMap<String>(MAX_SIZE);
        this.columns = new ColumnImpl[MAX_SIZE];
        this.timestampMaps = new TimestampMap[MAX_SIZE];
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
                    id = garbageQueue.firstShort();
                    garbageQueue.remove(id);
                } else {
                    id = intToShort(length);
                    if (length >= MAX_SIZE) {
                        throw new RuntimeException("Maximum number of columns reached at " + MAX_SIZE);
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
            garbageQueue.add(id);

            int intId = shortToInt(id);
            columns[intId] = null;
            if (indexStore != null) {
                indexStore.removeColumn((ColumnImpl) column);
            }
            columnImpl.setStoreId(NULL_ID);
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

    @Override
    public Iterator<Column> iterator() {
        return new ColumnStoreIterator();
    }

    public Set<String> getColumnKeys() {
        readLock();
        try {
            return new ObjectOpenHashSet<String>(idMap.keySet());
        } finally {
            readUnlock();
        }
    }

    public void clear() {
        garbageQueue.clear();
        idMap.clear();
        Arrays.fill(columns, null);
        Arrays.fill(timestampMaps, null);
        if (indexStore != null) {
            indexStore.clear();
        }
    }

    public int size() {
        return length - garbageQueue.size();
    }

    public TimestampMap getTimestampMap(Column column) {
        int index = column.getIndex();
        TimestampMap timestampStore = timestampMaps[index];
        if (timestampStore == null) {
            timestampStore = new TimestampMap();
            timestampMaps[index] = timestampStore;
        }
        return timestampStore;
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

    private final class ColumnStoreIterator implements Iterator<Column> {

        private int index;
        private ColumnImpl pointer;

        @Override
        public boolean hasNext() {
            while (index < length && (pointer = columns[index++]) == null) {
            }
            if (pointer == null) {
                return false;
            }
            return true;
        }

        @Override
        public Column next() {
            ColumnImpl c = pointer;
            pointer = null;
            return c;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.elementType != null ? this.elementType.hashCode() : 0);
        Iterator<Column> itr = this.iterator();
        while (itr.hasNext()) {
            hash = 11 * hash + itr.next().hashCode();
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
        final ColumnStore<T> other = (ColumnStore<T>) obj;
        if (this.elementType != other.elementType && (this.elementType == null || !this.elementType.equals(other.elementType))) {
            return false;
        }
        Iterator<Column> itr1 = this.iterator();
        Iterator<Column> itr2 = other.iterator();
        while (itr1.hasNext()) {
            if (!itr2.hasNext()) {
                return false;
            }
            Column c1 = itr1.next();
            Column c2 = itr2.next();
            if (!c1.equals(c2)) {
                return false;
            }
            TimestampMap s1 = timestampMaps[c1.getIndex()];
            TimestampMap s2 = timestampMaps[c2.getIndex()];
            if ((s1 == null && s2 != null) || (s1 != null && s2 == null)) {
                return false;
            }
            if (s1 != null && !s1.equals(s2)) {
                return false;
            }
        }
        return true;
    }
}
