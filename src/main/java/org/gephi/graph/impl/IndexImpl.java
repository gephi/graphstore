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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnIndex;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Index;

public class IndexImpl<T extends Element> implements Index<T> {

    protected final TableLockImpl lock;
    protected final ColumnStore<T> columnStore;
    protected ColumnIndexImpl[] columns;
    protected int columnsCount;

    public IndexImpl(ColumnStore<T> columnStore) {
        this.columnStore = columnStore;
        this.columns = new ColumnIndexImpl[0];
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
    public ColumnIndex getColumnIndex(Column column) {
        return getIndex(column);
    }

    @Override
    public int count(Column column, Object value) {
        checkNonNullColumnObject(column);

        lock();
        try {
            return getIndex(column).getCount(value);
        } finally {
            unlock();
        }
    }

    public int count(String key, Object value) {
        checkNonNullObject(key);

        return count(columnStore.getColumn(key), value);
    }

    @Override
    public Iterable<T> get(Column column, Object value) {
        checkNonNullColumnObject(column);

        lock();
        try {
            return getIndex(column).get(value);
        } finally {
            unlock();
        }
    }

    public Iterable<T> get(String key, Object value) {
        checkNonNullObject(key);

        return get(columnStore.getColumn(key), value);
    }

    @Override
    public boolean isSortable(Column column) {
        checkNonNullColumnObject(column);

        lock();
        try {
            return getIndex(column).isSortable();
        } finally {
            unlock();
        }
    }

    @Override
    public Number getMinValue(Column column) {
        checkNonNullColumnObject(column);

        lock();
        try {
            return getIndex(column).getMinValue();
        } finally {
            unlock();
        }
    }

    @Override
    public Number getMaxValue(Column column) {
        checkNonNullColumnObject(column);
        lock();
        try {
            return getIndex(column).getMaxValue();
        } finally {
            unlock();
        }
    }

    public Iterable<Map.Entry<Object, Set<T>>> get(Column column) {
        checkNonNullColumnObject(column);

        return getIndex(column);
    }

    @Override
    public Collection values(Column column) {
        checkNonNullColumnObject(column);

        lock();
        try {
            return getIndex(column).values();
        } finally {
            unlock();
        }
    }

    @Override
    public int countValues(Column column) {
        checkNonNullColumnObject(column);
        lock();
        try {
            return getIndex(column).countValues();
        } finally {
            unlock();
        }
    }

    @Override
    public int countElements(Column column) {
        checkNonNullColumnObject(column);
        lock();
        try {
            return getIndex(column).elements;
        } finally {
            unlock();
        }
    }

    public Object put(String key, Object value, T element) {
        checkNonNullObject(key);

        return put(columnStore.getColumn(key), value, element);
    }

    public Object put(Column column, Object value, T element) {
        checkNonNullColumnObject(column);

        return getIndex(column).putValue(element, value);
    }

    public void remove(String key, Object value, T element) {
        checkNonNullObject(key);

        remove(columnStore.getColumn(key), value, element);
    }

    public void remove(Column column, Object value, T element) {
        checkNonNullColumnObject(column);

        getIndex(column).removeValue(element, value);
    }

    public Object set(String key, Object oldValue, Object value, T element) {
        checkNonNullObject(key);

        return set(columnStore.getColumn(key), oldValue, value, element);
    }

    public Object set(Column column, Object oldValue, Object value, T element) {
        checkNonNullColumnObject(column);

        return getIndex(column).replaceValue(element, oldValue, value);
    }

    public void clear() {
        for (ColumnIndexImpl ai : columns) {
            if (ai != null) {
                ai.clear();
            }
        }
    }

    protected void addColumn(ColumnImpl col) {
        if (col.isIndexed()) {
            ensureColumnSize(col.storeId);
            ColumnIndexImpl index = createIndex(col);
            columns[col.storeId] = index;
            columnsCount++;
        }
    }

    protected void addAllColumns(ColumnImpl[] cols) {
        ensureColumnSize(cols.length);
        for (ColumnImpl col : cols) {
            if (col.isIndexed()) {
                ColumnIndexImpl index = createIndex(col);
                columns[col.storeId] = index;
                columnsCount++;
            }
        }
    }

    protected void removeColumn(ColumnImpl col) {
        if (col.isIndexed()) {
            ColumnIndexImpl index = columns[col.storeId];
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

    protected ColumnIndexImpl getIndex(Column col) {
        if (col.isIndexed()) {
            int id = col.getIndex();
            if (id != ColumnStore.NULL_ID && columns.length > id) {
                ColumnIndexImpl index = columns[id];
                if (index != null && index.column == col) {
                    return index;
                }
            }
        }
        return null;
    }

    protected ColumnIndexImpl getIndex(String key) {
        return getIndex(columnStore.getColumn(key));
    }

    protected void destroy() {
        for (ColumnIndexImpl ai : columns) {
            if (ai != null) {
                ai.destroy();
            }
        }
        columns = new ColumnIndexImpl[0];
        columnsCount = 0;
    }

    protected int size() {
        return columnsCount;
    }

    ColumnIndexImpl createIndex(ColumnImpl column) {
        if (column.getTypeClass().equals(Byte.class)) {
            // Byte
            return new ColumnIndexImpl.ByteIndex<T>(column);
        } else if (column.getTypeClass().equals(Short.class)) {
            // Short
            return new ColumnIndexImpl.ShortIndex<T>(column);
        } else if (column.getTypeClass().equals(Integer.class)) {
            // Integer
            return new ColumnIndexImpl.IntegerIndex<T>(column);
        } else if (column.getTypeClass().equals(Long.class)) {
            // Long
            return new ColumnIndexImpl.LongIndex<T>(column);
        } else if (column.getTypeClass().equals(Float.class)) {
            // Float
            return new ColumnIndexImpl.FloatIndex<T>(column);
        } else if (column.getTypeClass().equals(Double.class)) {
            // Double
            return new ColumnIndexImpl.DoubleIndex<T>(column);
        } else if (Number.class.isAssignableFrom(column.getTypeClass())) {
            // Other numbers
            return new ColumnIndexImpl.GenericNumberIndex<T>(column);
        } else if (column.getTypeClass().equals(Boolean.class)) {
            // Boolean
            return new ColumnIndexImpl.BooleanIndex<T>(column);
        } else if (column.getTypeClass().equals(Character.class)) {
            // Char
            return new ColumnIndexImpl.CharIndex<T>(column);
        } else if (column.getTypeClass().equals(String.class)) {
            // String
            return new ColumnIndexImpl.DefaultIndex<T>(column);
        } else if (column.getTypeClass().equals(byte[].class)) {
            // Byte Array
            return new ColumnIndexImpl.ByteArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(short[].class)) {
            // Short Array
            return new ColumnIndexImpl.ShortArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(int[].class)) {
            // Integer Array
            return new ColumnIndexImpl.IntegerArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(long[].class)) {
            // Long Array
            return new ColumnIndexImpl.LongArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(float[].class)) {
            // Float array
            return new ColumnIndexImpl.FloatArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(double[].class)) {
            // Double array
            return new ColumnIndexImpl.DoubleArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(boolean[].class)) {
            // Boolean array
            return new ColumnIndexImpl.BooleanArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(char[].class)) {
            // Char array
            return new ColumnIndexImpl.CharArrayIndex<T>(column);
        } else if (column.getTypeClass().equals(String[].class)) {
            // String array
            return new ColumnIndexImpl.DefaultArrayIndex<T>(column);
        } else if (column.getTypeClass().isArray()) {
            // Default Array
            return new ColumnIndexImpl.DefaultArrayIndex<T>(column);
        }
        return new ColumnIndexImpl.DefaultIndex<T>(column);
    }

    private void ensureColumnSize(int index) {
        if (index >= columns.length) {
            ColumnIndexImpl[] newArray = new ColumnIndexImpl[index + 1];
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

}
