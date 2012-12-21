package org.gephi.graph.store;

import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public abstract class ElementImpl implements Element {

    //Reference to store
    protected final GraphStore graphStore;
    //Id
    protected final Object id;
    //Properties
    protected Object[] properties;

    public ElementImpl(Object id, GraphStore graphStore) {
        this.id = id;
        this.graphStore = graphStore;
        this.properties = new Object[0];
    }

    abstract ColumnStore getPropertyStore();

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Object getProperty(String key) {
        return getProperty(getPropertyStore().getColumn(key));
    }

    @Override
    public Object getProperty(Column column) {
        int index = column.getIndex();
        Object res = null;
        if (index < properties.length) {
            res = properties[index];
        }
        if (res == null) {
            return column.getDefaultValue();
        }
        return res;
    }

    @Override
    public Object[] getProperties() {
        return properties;
    }

    @Override
    public Set<String> getPropertyKeys() {
        return getPropertyStore().getPropertyKeys();
    }

    @Override
    public Object removeProperty(String key) {
        return removeProperty(getPropertyStore().getColumn(key));
    }

    @Override
    public Object removeProperty(Column column) {
        ColumnStore propertyStore = getPropertyStore();
        int index = column.getIndex();
        if (index < properties.length) {
            Object oldValue = properties[index];
            properties[index] = null;
            if (column.isIndexed() && propertyStore != null) {
                propertyStore.indexStore.set(column, oldValue, column.getDefaultValue(), this);
            }
            return oldValue;
        }
        return null;
    }

    @Override
    public void setProperty(String key, Object value) {
        setProperty(getPropertyStore().getColumn(key), value);
    }

    @Override
    public void setProperty(Column column, Object value) {
        int index = column.getIndex();
        ColumnStore propertyStore = getPropertyStore();
        Object oldValue = null;
        if (index >= properties.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(properties, 0, newArray, 0, index);
            properties = newArray;
        } else {
            oldValue = properties[index];
        }

        if (column.isIndexed() && propertyStore != null) {
            value = propertyStore.indexStore.set(column, oldValue, value, this);
        }
        properties[index] = value;
    }

    @Override
    public void clearProperties() {
        ColumnStore propertyStore = getPropertyStore();
        if (propertyStore != null) {
            propertyStore.indexStore.clear(this);
        }
    }
    
    protected void indexProperties() {
        ColumnStore propertyStore = getPropertyStore();
        if (propertyStore != null) {
            propertyStore.indexStore.index(this);
        }
    }

    protected GraphStore getGraphStore() {
        return graphStore;
    }
}
