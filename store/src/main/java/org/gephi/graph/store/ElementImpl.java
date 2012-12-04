package org.gephi.graph.store;

import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public abstract class ElementImpl implements Element {

    //Reference to store
    protected GraphStore graphStore;
    //Id
    protected final Object id;
    //Properties
    protected Object[] properties;

    public ElementImpl(Object id) {
        this.id = id;
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
        int index = column.getIndex();
        if (index < properties.length) {
            Object oldValue = properties[index];
            properties[index] = null;
            getPropertyStore().remove(column, oldValue, this);
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
        Object oldValue = null;
        if (index >= properties.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(properties, 0, newArray, 0, index);
            properties = newArray;
        } else {
            oldValue = properties[index];
        }
        properties[index] = value;
        getPropertyStore().set(column, oldValue, value, this);
    }

    @Override
    public void clearProperties() {
        PropertyStore propertyStore = getPropertyStore();
        for (int i = 0; i < properties.length; i++) {
            Object o = properties[i];
            Column c = propertyStore.getColumnByIndex(i);
            propertyStore.remove(c, o, this);
        }
    }

    public GraphStore getGraphStore() {
        return graphStore;
    }

    public void setGraphStore(GraphStore graphStore) {
        this.graphStore = graphStore;
    }
}
