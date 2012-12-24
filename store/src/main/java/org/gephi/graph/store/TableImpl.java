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
    }

    private void checkDefaultValue(Object defaultValue, Class type) {
        if (defaultValue != null) {
            if (defaultValue.getClass() != type) {
                throw new IllegalArgumentException("The default value type cannot be cast to the type");
            }
        }
    }
}
