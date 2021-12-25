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
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TableLock;
import org.gephi.graph.api.TableObserver;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;

public class TableImpl<T extends Element> implements Collection<Column>, Table {

    // Store
    protected final ColumnStore<T> store;

    public TableImpl(Class<T> elementType, boolean indexed) {
        this(null, elementType, indexed);
    }

    public TableImpl(GraphStore graphStore, Class<T> elementType, boolean indexed) {
        store = new ColumnStore<>(graphStore, elementType, indexed);
    }

    @Override
    public Column addColumn(String id, Class type) {
        return addColumn(id, null, type, Origin.DATA, null, true);
    }

    @Override
    public Column addColumn(String id, Class type, Origin origin) {
        return addColumn(id, null, type, origin, null, true);
    }

    @Override
    public Column addColumn(String id, String title, Class type, Object defaultValue) {
        return addColumn(id, title, type, Origin.DATA, defaultValue, true);
    }

    @Override
    public Column addColumn(String id, String title, Class type, Origin origin, Object defaultValue, boolean indexed) {
        checkValidId(id);
        checkSupportedTypes(type);
        checkDefaultValue(defaultValue, type);

        type = AttributeUtils.getStandardizedType(type);
        if (defaultValue != null) {
            defaultValue = AttributeUtils.standardizeValue(defaultValue);
        }

        if (title == null || title.isEmpty()) {
            title = id;
        }

        id = id.toLowerCase();

        if (indexed && store.indexStore == null) {
            indexed = false;
        }

        ColumnImpl column = new ColumnImpl(this, id, type, title, defaultValue, origin, indexed, false);
        store.addColumn(column);

        return column;
    }

    @Override
    public boolean add(Column column) {
        store.checkNonNullColumnObject(column);
        store.addColumn(column);
        return true;
    }

    @Override
    public int countColumns() {
        return store.size();
    }

    @Override
    public int size() {
        return countColumns();
    }

    @Override
    public boolean isEmpty() {
        return countColumns() == 0;
    }

    @Override
    public Iterator<Column> iterator() {
        return store.iterator();
    }

    @Override
    public void doBreak() {
        store.doBreak();
    }

    @Override
    public Column[] toArray() {
        return store.toArray();
    }

    @Override
    public <K> K[] toArray(K[] array) {
        store.checkNonNullObject(array);

        ColumnImpl[] columns = store.toArray();

        if (array.length < size()) {
            array = (K[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size());
        }
        for (int i = 0; i < columns.length; i++) {
            array[i] = (K) columns[i];
        }
        return array;
    }

    @Override
    public List<Column> toList() {
        return store.toList();
    }

    @Override
    public Column getColumn(int index) {
        return store.getColumnByIndex(index);
    }

    @Override
    public Column getColumn(String id) {
        return store.getColumn(id.toLowerCase());
    }

    @Override
    public boolean hasColumn(String id) {
        return store.hasColumn(id.toLowerCase());
    }

    @Override
    public boolean contains(Object o) {
        store.checkNonNullColumnObject(o);

        ColumnImpl column = (ColumnImpl) o;
        return hasColumn(column.getId());
    }

    @Override
    public void removeColumn(Column column) {
        store.removeColumn(column);
    }

    @Override
    public void removeColumn(String id) {
        store.removeColumn(id.toLowerCase());
    }

    @Override
    public boolean remove(Object o) {
        store.checkNonNullColumnObject(o);
        removeColumn((ColumnImpl) o);
        return true;
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("This method from Collection isn't implemented");
    }

    @Override
    public boolean addAll(Collection<? extends Column> c) {
        throw new UnsupportedOperationException("This method from Collection isn't implemented");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("This method from Collection isn't implemented");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("This method from Collection isn't implemented");
    }

    @Override
    public TableObserver createTableObserver(boolean withDiff) {
        return store.createTableObserver(this, withDiff);
    }

    @Override
    public Class getElementClass() {
        return store.elementType;
    }

    @Override
    public Graph getGraph() {
        return store.graphStore;
    }

    @Override
    public boolean isNodeTable() {
        return Node.class.equals(store.elementType);
    }

    @Override
    public boolean isEdgeTable() {
        return Edge.class.equals(store.elementType);
    }

    @Override
    public TableLockImpl getLock() {
        return store.lock;
    }

    public void destroyTableObserver(TableObserver observer) {
        checkableTableObserver(observer);

        store.destroyTablesObserver((TableObserverImpl) observer);
    }

    public boolean deepEquals(TableImpl<T> obj) {
        if (obj == null) {
            return false;
        }
        return !(this.store != obj.store && (this.store == null || !this.store.deepEquals(obj.store)));
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 71 * hash + (this.store != null ? this.store.deepHashCode() : 0);
        return hash;
    }

    private void checkValidId(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("The column id can't be empty.");
        }
        if (store.hasColumn(id.toLowerCase())) {
            throw new IllegalArgumentException("The column '" + id + "' already existing in the table");
        }
    }

    private void checkSupportedTypes(Class type) {
        if (!AttributeUtils.isSupported(type)) {
            throw new IllegalArgumentException("Unknown type " + type.getName());
        }
    }

    private void checkCollection(final Collection<?> collection) {
        if (collection == this) {
            throw new IllegalArgumentException("Can't pass itself");
        }
    }

    private void checkDefaultValue(Object defaultValue, Class type) {
        if (defaultValue != null) {
            if (defaultValue.getClass() != type) {
                throw new IllegalArgumentException("The default value type cannot be cast to the type");
            }
        }
    }

    private void checkableTableObserver(TableObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        if (!(observer instanceof TableObserverImpl)) {
            throw new ClassCastException("The observer should be a TableObserverImpl instance");
        }
        if (((TableObserverImpl) observer).table != this) {
            throw new RuntimeException("The observer doesn't belong to this table");
        }
    }
}
