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

import java.util.Iterator;
import org.gephi.attribute.api.AttributeUtils;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.gephi.attribute.api.Table;
import org.gephi.attribute.api.TableObserver;
import org.gephi.attribute.time.Estimator;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public class TableImpl<T extends Element> implements Table {

    //Store
    protected final ColumnStore<T> store;

    public TableImpl(ColumnStore<T> store) {
        this.store = store;
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
    public int countColumns() {
        return store.size();
    }

    @Override
    public Iterator<Column> iterator() {
        return store.iterator();
    }

    @Override
    public Column getColumn(int index) {
        return store.getColumnByIndex(index);
    }

    @Override
    public Column getColumn(String id) {
        return store.getColumn(id);
    }

    @Override
    public Column[] getColumns() {
        return store.toArray();
    }

    @Override
    public boolean hasColumn(String id) {
        return store.hasColumn(id);
    }

    @Override
    public void removeColumn(Column column) {
        store.removeColumn(column);
    }

    @Override
    public void removeColumn(String id) {
        store.removeColumn(id);
    }

    @Override
    public Estimator getEstimator(Column column) {
        return store.getEstimator(column);
    }

    @Override
    public void setEstimator(Column column, Estimator estimator) {
        store.setEstimator(column, estimator);
    }

    @Override
    public TableObserver getTableObserver() {
        return store.createTableObserver(this);
    }

    @Override
    public Class getElementClass() {
        return store.elementType;
    }

    public void destroyTableObserver(TableObserver observer) {
        checkableTableObserver(observer);

        store.destroyGraphObserver((TableObserverImpl) observer);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.store != null ? this.store.hashCode() : 0);
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
        final TableImpl<T> other = (TableImpl<T>) obj;
        if (this.store != other.store && (this.store == null || !this.store.equals(other.store))) {
            return false;
        }
        return true;
    }

    private void checkValidId(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("The column id can' be empty.");
        }
        if (store.hasColumn(id.toLowerCase())) {
            throw new IllegalArgumentException("The column already existing in the table");
        }
    }

    private void checkSupportedTypes(Class type) {
        if (!AttributeUtils.isSupported(type)) {
            throw new IllegalArgumentException("Unknown type " + type.getName());
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
