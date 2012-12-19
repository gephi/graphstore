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

    abstract PropertyStore getPropertyStore();

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
        PropertyStore propertyStore = getPropertyStore();
        int index = column.getIndex();
        if (index < properties.length) {
            Object oldValue = properties[index];
            properties[index] = null;
            if (propertyStore != null) {
                propertyStore.remove(column, oldValue, this);
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
        PropertyStore propertyStore = getPropertyStore();
        Object oldValue = null;
        if (index >= properties.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(properties, 0, newArray, 0, index);
            properties = newArray;
        } else {
            oldValue = properties[index];
        }

        if (propertyStore != null) {
            value = propertyStore.set(column, oldValue, value, this);
        }
        properties[index] = value;
    }

    @Override
    public void clearProperties() {
        PropertyStore propertyStore = getPropertyStore();

        for (int index = 0; index < properties.length; index++) {
            Object value = properties[index];
            properties[index] = null;
            if (propertyStore != null) {
                Column column = propertyStore.getColumnByIndex(index);
                propertyStore.remove(column, value, this);
            }
        }
    }

    protected void indexProperties() {
        PropertyStore propertyStore = getPropertyStore();
        if (propertyStore != null) {
            for (int index = 0; index < properties.length; index++) {
                Object value = properties[index];
                Column column = propertyStore.getColumnByIndex(index);
                value = getPropertyStore().put(column, value, this);
                properties[index] = value;
            }
        }
    }

    protected GraphStore getGraphStore() {
        return graphStore;
    }
}
