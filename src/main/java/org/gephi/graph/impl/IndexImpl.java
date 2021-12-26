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

import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.chars.Char2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.Element;

public class IndexImpl<T extends Element> implements Index<T> {

    protected final TableLockImpl lock;
    protected final ColumnStore<T> columnStore;
    protected ColumnIndex<?, T>[] columns;
    protected int columnsCount;

    public IndexImpl(ColumnStore<T> columnStore) {
        this.columnStore = columnStore;
        this.columns = new ColumnIndex[0];
        this.lock = columnStore.lock;
    }

    @Override
    public Class<T> getIndexClass() {
        return columnStore.elementType;
    }

    @Override
    public String getIndexName() {
        return "index_" + columnStore.elementType.getCanonicalName();
    }

    @Override
    public int count(Column column, Object value) {
        checkNonNullColumnObject(column);

        lock();
        try {
            ColumnIndex index = getIndex((ColumnImpl) column);
            return index.getCount(value);
        } finally {
            unlock();
        }
    }

    public int count(String key, Object value) {
        checkNonNullObject(key);

        ColumnIndex index = getIndex(key);
        return index.getCount(value);
    }

    public Iterable<T> get(String key, Object value) {
        checkNonNullObject(key);

        ColumnIndex index = getIndex(key);
        return index.getValueSet(value);
    }

    @Override
    public Iterable<T> get(Column column, Object value) {
        checkNonNullColumnObject(column);

        if (lock != null) {
            lock.lock();
            ColumnIndex index = getIndex((ColumnImpl) column);
            Set<T> valueSet = index.getValueSet(value);
            return valueSet == null ? null : new LockableIterable<>(index.getValueSet(value));
        }
        ColumnIndex index = getIndex((ColumnImpl) column);
        return index.getValueSet(value);
    }

    @Override
    public boolean isSortable(Column column) {
        checkNonNullColumnObject(column);

        lock();
        try {
            ColumnIndex index = getIndex((ColumnImpl) column);

            return index.isSortable();
        } finally {
            unlock();
        }
    }

    @Override
    public Number getMinValue(Column column) {
        checkNonNullColumnObject(column);

        lock();
        try {
            ColumnIndex index = getIndex((ColumnImpl) column);
            return index.getMinValue();
        } finally {
            unlock();
        }
    }

    @Override
    public Number getMaxValue(Column column) {
        checkNonNullColumnObject(column);
        lock();
        try {
            ColumnIndex index = getIndex((ColumnImpl) column);
            return index.getMaxValue();
        } finally {
            unlock();
        }
    }

    public Iterable<Map.Entry<Object, Set<T>>> get(Column column) {
        checkNonNullColumnObject(column);

        ColumnIndex index = getIndex((ColumnImpl) column);
        return index;
    }

    @Override
    public Collection values(Column column) {
        checkNonNullColumnObject(column);

        lock();
        try {
            ColumnIndex index = getIndex((ColumnImpl) column);
            return new ArrayList(index.values());
        } finally {
            unlock();
        }
    }

    @Override
    public int countValues(Column column) {
        checkNonNullColumnObject(column);
        lock();
        try {
            ColumnIndex index = getIndex((ColumnImpl) column);
            return index.countValues();
        } finally {
            unlock();
        }
    }

    @Override
    public int countElements(Column column) {
        checkNonNullColumnObject(column);
        lock();
        try {
            ColumnIndex index = getIndex((ColumnImpl) column);
            return index.elements;
        } finally {
            unlock();
        }
    }

    public Object put(String key, Object value, T element) {
        checkNonNullObject(key);

        ColumnIndex index = getIndex(key);
        return index.putValue(element, value);
    }

    public Object put(Column column, Object value, T element) {
        checkNonNullColumnObject(column);

        ColumnIndex index = getIndex((ColumnImpl) column);
        return index.putValue(element, value);
    }

    public void remove(String key, Object value, T element) {
        checkNonNullObject(key);

        ColumnIndex index = getIndex(key);
        index.removeValue(element, value);
    }

    public void remove(Column column, Object value, T element) {
        checkNonNullColumnObject(column);

        ColumnIndex index = getIndex((ColumnImpl) column);
        index.removeValue(element, value);
    }

    public Object set(String key, Object oldValue, Object value, T element) {
        checkNonNullObject(key);

        ColumnIndex index = getIndex(key);
        return index.replaceValue(element, oldValue, value);
    }

    public Object set(Column column, Object oldValue, Object value, T element) {
        checkNonNullColumnObject(column);

        ColumnIndex index = getIndex((ColumnImpl) column);
        return index.replaceValue(element, oldValue, value);
    }

    public void clear() {
        for (ColumnIndex ai : columns) {
            if (ai != null) {
                ai.clear();
            }
        }
    }

    protected void addColumn(ColumnImpl col) {
        if (col.isIndexed()) {
            ensureColumnSize(col.storeId);
            ColumnIndex index = createIndex(col);
            columns[col.storeId] = index;
            columnsCount++;
        }
    }

    protected void addAllColumns(ColumnImpl[] cols) {
        ensureColumnSize(cols.length);
        for (ColumnImpl col : cols) {
            if (col.isIndexed()) {
                ColumnIndex index = createIndex(col);
                columns[col.storeId] = index;
                columnsCount++;
            }
        }
    }

    protected void removeColumn(ColumnImpl col) {
        if (col.isIndexed()) {
            ColumnIndex index = columns[col.storeId];
            index.destroy();
            columns[col.storeId] = null;
            columnsCount--;
        }
    }

    protected boolean hasColumn(ColumnImpl col) {
        if (col.isIndexed()) {
            int id = col.storeId;
            if (id != ColumnStore.NULL_ID && columns.length > id && columns[id].column == col) {
                return true;
            }
        }
        return false;
    }

    protected ColumnIndex getIndex(ColumnImpl col) {
        if (col.isIndexed()) {
            int id = col.storeId;
            if (id != ColumnStore.NULL_ID && columns.length > id) {
                ColumnIndex index = columns[id];
                if (index != null && index.column == col) {
                    return index;
                }
            }
        }
        return null;
    }

    protected ColumnIndex getIndex(String key) {
        int id = columnStore.getColumnIndex(key);
        if (id != ColumnStore.NULL_ID && columns.length > id) {
            return columns[id];
        }
        return null;
    }

    protected void destroy() {
        for (ColumnIndex ai : columns) {
            if (ai != null) {
                ai.destroy();
            }
        }
        columns = new ColumnIndex[0];
        columnsCount = 0;
    }

    protected int size() {
        return columnsCount;
    }

    ColumnIndex createIndex(ColumnImpl column) {
        if (column.getTypeClass().equals(Byte.class)) {
            // Byte
            return new ColumnIndex.ByteIndex<T>(column);
        } else if (column.getTypeClass().equals(Short.class)) {
            // Short
            return new ColumnIndex.ShortIndex<T>(column);
        } else if (column.getTypeClass().equals(Integer.class)) {
            // Integer
            return new ColumnIndex.IntegerIndex<T>(column);
        } else if (column.getTypeClass().equals(Long.class)) {
            // Long
            return new ColumnIndex.LongIndex<T>(column);
        } else if (column.getTypeClass().equals(Float.class)) {
            // Float
            return new ColumnIndex.FloatIndex<T>(column);
        } else if (column.getTypeClass().equals(Double.class)) {
            // Double
            return new ColumnIndex.DoubleIndex<T>(column);
        } else if (Number.class.isAssignableFrom(column.getTypeClass())) {
            // Other numbers
            return new ColumnIndex.GenericNumberIndex<T>(column);
        } else if (column.getTypeClass().equals(Boolean.class)) {
            // Boolean
            return new ColumnIndex.BooleanIndex<T>(column);
        } else if (column.getTypeClass().equals(Character.class)) {
            // Char
            return new ColumnIndex.CharIndex<T>(column);
        } else if (column.getTypeClass().equals(String.class)) {
            // String
            return new ColumnIndex.DefaultIndex<T>(column);
        } else if (column.getTypeClass().equals(byte[].class)) {
            // Byte Array
            return new ColumnIndex.ByteArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(short[].class)) {
            // Short Array
            return new ColumnIndex.ShortArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(int[].class)) {
            // Integer Array
            return new ColumnIndex.IntegerArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(long[].class)) {
            // Long Array
            return new ColumnIndex.LongArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(float[].class)) {
            // Float array
            return new ColumnIndex.FloatArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(double[].class)) {
            // Double array
            return new ColumnIndex.DoubleArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(boolean[].class)) {
            // Boolean array
            return new ColumnIndex.BooleanArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(char[].class)) {
            // Char array
            return new ColumnIndex.CharArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(String[].class)) {
            // String array
            return new ColumnIndex.DefaultArrayIndex<T>(column);
        } else if (column.getTypeClass().isArray()) {
            // Default Array
            return new ColumnIndex.DefaultArrayIndex<T>(column);
        }
        return new ColumnIndex.DefaultIndex<T>(column);
    }

    private void ensureColumnSize(int index) {
        if (index >= columns.length) {
            ColumnIndex[] newArray = new ColumnIndex[index + 1];
            System.arraycopy(columns, 0, newArray, 0, columns.length);
            columns = newArray;
        }
    }

    void lock() {
        if (lock != null) {
            lock.lock();
        }
    }

    void unlock() {
        if (lock != null) {
            lock.unlock();
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

    private class LockableIterable<T> implements Iterable<T> {

        private final Iterable<T> ite;

        public LockableIterable(Iterable<T> ite) {
            this.ite = ite;
        }

        @Override
        public Iterator<T> iterator() {
            return new LockableIterator<>(ite.iterator());
        }
    }

    private class LockableIterator<T> implements Iterator<T> {

        private final Iterator<T> itr;

        public LockableIterator(Iterator<T> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            boolean n = itr.hasNext();
            if (!n) {
                lock.unlock();
            }
            return n;
        }

        @Override
        public T next() {
            return itr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
