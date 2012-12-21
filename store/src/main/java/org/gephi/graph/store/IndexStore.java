package org.gephi.graph.store;

import org.gephi.attribute.api.Column;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public class IndexStore<T extends Element> {

    protected final ColumnStore<T> propertyStore;
    protected final IndexImpl<T> mainIndex;

    public IndexStore(ColumnStore<T> propertyStore) {
        this.propertyStore = propertyStore;
        this.mainIndex = new IndexImpl<T>(propertyStore);
    }

    protected synchronized void addColumn(ColumnImpl col) {
        mainIndex.addColumn(col);
    }

    protected synchronized void removeColumn(ColumnImpl col) {
        mainIndex.removeColumn(col);
    }

    public synchronized Object set(Column column, Object oldValue, Object value, T element) {
        value = mainIndex.set(column, oldValue, value, element);

        return value;
    }

    public synchronized void remove(Column column, Object value, T element) {
        mainIndex.remove(column, value, element);
    }

    public synchronized Object put(Column column, Object value, T element) {
        value = mainIndex.put(column, value, element);

        return value;
    }
}
