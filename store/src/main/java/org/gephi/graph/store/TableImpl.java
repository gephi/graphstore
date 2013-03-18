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

import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.gephi.attribute.api.Table;
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
    public Column addColumn(String id, String title, Class type, Origin origin, Object defaultValue, boolean indexed) {
        checkValidId(id);
        checkSupportedTypes(type);
        checkDefaultValue(defaultValue, type);

        if (title == null || title.isEmpty()) {
            title = id;
        }

        id = id.toLowerCase();

        if (indexed && store.indexStore == null) {
            indexed = false;
        }

        ColumnImpl column = new ColumnImpl(id, type, title, defaultValue, origin, indexed);
        store.addColumn(column);

        return column;
    }

    @Override
    public int countColumns() {
        return store.size();
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
        if (!(type.equals(Byte.class)
                || type.equals(Short.class)
                || type.equals(Integer.class)
                || type.equals(Long.class)
                || type.equals(Float.class)
                || type.equals(Double.class)
                || type.equals(Boolean.class)
                || type.equals(Character.class)
                || type.equals(String.class)
                || type.equals(byte[].class)
                || type.equals(short[].class)
                || type.equals(int[].class)
                || type.equals(long[].class)
                || type.equals(float[].class)
                || type.equals(double[].class)
                || type.equals(boolean[].class)
                || type.equals(char[].class)
                || type.equals(String[].class))) {
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
}
