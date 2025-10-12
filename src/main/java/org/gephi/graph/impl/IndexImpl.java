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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnIndex;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Index;

public class IndexImpl<T extends Element> implements Index<T> {

    protected final ColumnStore<T> columnStore;
    protected final Graph graph;
    protected ColumnIndexImpl[] columns;
    protected int columnsCount;

    public IndexImpl(ColumnStore<T> columnStore) {
        this(columnStore, columnStore.graphStore);
    }

    public IndexImpl(ColumnStore<T> columnStore, Graph graph) {
        this.columnStore = columnStore;
        this.graph = graph;
        this.columns = new ColumnIndexImpl[0];
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

        ColumnIndexImpl index = getIndex(column);
        if (index != null) {
            return index.count(value);
        }
        return 0;
    }

    public int count(String key, Object value) {
        checkNonNullObject(key);

        return count(columnStore.getColumn(key), value);
    }

    @Override
    public Iterable<T> get(Column column, Object value) {
        checkNonNullColumnObject(column);

        ColumnIndexImpl index = getIndex(column);
        if (index != null) {
            return index.get(value);
        }
        return Collections.EMPTY_LIST;
    }

    public Iterable<T> get(String key, Object value) {
        checkNonNullObject(key);

        return get(columnStore.getColumn(key), value);
    }

    @Override
    public boolean isSortable(Column column) {
        checkNonNullColumnObject(column);

        ColumnIndexImpl index = getIndex(column);
        if (index != null) {
            return index.isSortable();
        }
        return false;
    }

    @Override
    public Number getMinValue(Column column) {
        checkNonNullColumnObject(column);

        ColumnIndexImpl index = getIndex(column);
        if (index != null) {
            return index.getMinValue();
        }
        return null;
    }

    @Override
    public Number getMaxValue(Column column) {
        checkNonNullColumnObject(column);

        ColumnIndexImpl index = getIndex(column);
        if (index != null) {
            return index.getMaxValue();
        }
        return null;
    }

    public Iterable<Map.Entry<Object, Set<T>>> get(Column column) {
        checkNonNullColumnObject(column);

        return getIndex(column);
    }

    @Override
    public Collection values(Column column) {
        checkNonNullColumnObject(column);

        ColumnIndexImpl index = getIndex(column);
        if (index != null) {
            return index.values();
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public int countValues(Column column) {
        checkNonNullColumnObject(column);
        ColumnIndexImpl index = getIndex(column);
        if (index != null) {
            return index.countValues();
        }
        return 0;
    }

    @Override
    public int countElements(Column column) {
        checkNonNullColumnObject(column);
        ColumnIndexImpl index = getIndex(column);
        if (index != null) {
            return index.countElements();
        }
        return 0;
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
        ensureColumnSize(col.storeId);
        ColumnIndexImpl index = createIndex(col);
        columns[col.storeId] = index;
        columnsCount++;
    }

    protected void addAllColumns(ColumnImpl[] cols) {
        ensureColumnSize(cols.length);
        for (ColumnImpl col : cols) {
            ColumnIndexImpl index = createIndex(col);
            ensureColumnSize(col.storeId);
            columns[col.storeId] = index;
            columnsCount++;
        }
    }

    protected void removeColumn(ColumnImpl col) {
        ColumnIndexImpl index = columns[col.storeId];
        index.destroy();
        columns[col.storeId] = null;
        columnsCount--;
    }

    protected boolean hasColumn(ColumnImpl col) {
        int id = col.storeId;
        return id != ColumnStore.NULL_ID && columns.length > id && columns[id].getColumn() == col;
    }

    protected ColumnIndexImpl getIndex(Column col) {
        int id = col.getIndex();
        if (id != ColumnStore.NULL_ID && columns.length > id) {
            ColumnIndexImpl index = columns[id];
            if (index != null && index.getColumn() == col) {
                return index;
            }
        }

        // TODO: Make this more robust
        if (col.isProperty()) {
            DefaultColumnsImpl defaultColumns = columnStore.graphStore.defaultColumns;
            if (col == defaultColumns.degreeColumn) {
                return new DegreeNoIndexImpl(graph, DegreeNoIndexImpl.DegreeType.DEGREE);
            } else if (col == defaultColumns.inDegreeColumn) {
                return new DegreeNoIndexImpl(graph, DegreeNoIndexImpl.DegreeType.IN_DEGREE);
            } else if (col == defaultColumns.outDegreeColumn) {
                return new DegreeNoIndexImpl(graph, DegreeNoIndexImpl.DegreeType.OUT_DEGREE);
            } else if (col == defaultColumns.typeColumn) {
                return new EdgeTypeNoIndexImpl(graph);
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

    ColumnIndexImpl createIndex(ColumnImpl col) {
        return col.isIndexed() && ColumnStandardIndexImpl.isSupportedType(col) ? createStandardIndex(col)
                : createNoIndex(col, graph);
    }

    ColumnNoIndexImpl createNoIndex(ColumnImpl column, Graph graph) {
        return new ColumnNoIndexImpl(column, graph, columnStore.elementType);
    }

    ColumnStandardIndexImpl createStandardIndex(ColumnImpl column) {
        if (column.getTypeClass().equals(Byte.class)) {
            // Byte
            return new ColumnStandardIndexImpl.ByteStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(Short.class)) {
            // Short
            return new ColumnStandardIndexImpl.ShortStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(Integer.class)) {
            // Integer
            return new ColumnStandardIndexImpl.IntegerStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(Long.class)) {
            // Long
            return new ColumnStandardIndexImpl.LongStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(Float.class)) {
            // Float
            return new ColumnStandardIndexImpl.FloatStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(Double.class)) {
            // Double
            return new ColumnStandardIndexImpl.DoubleStandardIndex<T>(column);
        } else if (Number.class.isAssignableFrom(column.getTypeClass())) {
            // Other numbers
            return new ColumnStandardIndexImpl.GenericNumberStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(Boolean.class)) {
            // Boolean
            return new ColumnStandardIndexImpl.BooleanStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(Character.class)) {
            // Char
            return new ColumnStandardIndexImpl.CharStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(String.class)) {
            // String
            return new ColumnStandardIndexImpl.DefaultStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(byte[].class)) {
            // Byte Array
            return new ColumnStandardIndexImpl.ByteArrayStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(short[].class)) {
            // Short Array
            return new ColumnStandardIndexImpl.ShortArrayStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(int[].class)) {
            // Integer Array
            return new ColumnStandardIndexImpl.IntegerArrayStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(long[].class)) {
            // Long Array
            return new ColumnStandardIndexImpl.LongArrayStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(float[].class)) {
            // Float array
            return new ColumnStandardIndexImpl.FloatArrayStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(double[].class)) {
            // Double array
            return new ColumnStandardIndexImpl.DoubleArrayStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(boolean[].class)) {
            // Boolean array
            return new ColumnStandardIndexImpl.BooleanArrayStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(char[].class)) {
            // Char array
            return new ColumnStandardIndexImpl.CharArrayStandardIndex<T>(column);
        } else if (column.getTypeClass().equals(String[].class)) {
            // String array
            return new ColumnStandardIndexImpl.DefaultArrayStandardIndex<T>(column);
        } else if (column.getTypeClass().isArray()) {
            // Default Array
            return new ColumnStandardIndexImpl.DefaultArrayStandardIndex<T>(column);
        }
        return new ColumnStandardIndexImpl.DefaultStandardIndex<T>(column);
    }

    private void ensureColumnSize(int index) {
        if (index >= columns.length) {
            ColumnIndexImpl[] newArray = new ColumnIndexImpl[index + 1];
            System.arraycopy(columns, 0, newArray, 0, columns.length);
            columns = newArray;
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
