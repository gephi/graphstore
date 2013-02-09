package org.gephi.graph.store;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.time.TimestampBooleanSet;
import org.gephi.attribute.time.TimestampByteSet;
import org.gephi.attribute.time.TimestampCharSet;
import org.gephi.attribute.time.TimestampDoubleSet;
import org.gephi.attribute.time.TimestampFloatSet;
import org.gephi.attribute.time.TimestampIntegerSet;
import org.gephi.attribute.time.TimestampLongSet;
import org.gephi.attribute.time.TimestampSet;
import org.gephi.attribute.time.TimestampShortSet;
import org.gephi.attribute.time.TimestampStringSet;
import org.gephi.attribute.time.TimestampValueSet;
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
    //Timestamp
    protected TimestampSet timestampSet;

    public ElementImpl(Object id, GraphStore graphStore) {
        if(id == null) {
            throw new NullPointerException();
        }
        this.id = id;
        this.graphStore = graphStore;
        this.properties = new Object[0];
    }

    abstract ColumnStore getPropertyStore();

    abstract boolean isValid();

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
        checkColumn(column);
        
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
        checkColumn(column);
        
        ColumnStore propertyStore = getPropertyStore();
        int index = column.getIndex();
        if (index < properties.length) {
            Object oldValue = properties[index];
            properties[index] = null;
            if (column.isIndexed() && propertyStore != null && isValid()) {
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
        checkType(column, value);
        checkColumn(column);

        int index = column.getIndex();
        ColumnStore propertyStore = getPropertyStore();
        Object oldValue = null;
        if (index >= properties.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(properties, 0, newArray, 0, properties.length);
            properties = newArray;
        } else {
            oldValue = properties[index];
        }

        if (column.isIndexed() && propertyStore != null && isValid()) {
            value = propertyStore.indexStore.set(column, oldValue, value, this);
        }
        properties[index] = value;
    }

    @Override
    public void setProperty(String key, Object value, double timestamp) {
        setProperty(getPropertyStore().getColumn(key), value, timestamp);
    }

    @Override
    public void setProperty(Column column, Object value, double timestamp) {
        checkType(column, value);
        checkDouble(timestamp);
        checkColumn(column);

        final TimestampStore timestampStore = getTimestampStore();
        if (timestampStore != null) {
            int index = column.getIndex();
            Object oldValue = null;
            if (index >= properties.length) {
                Object[] newArray = new Object[index + 1];
                System.arraycopy(properties, 0, newArray, 0, properties.length);
                properties = newArray;
            } else {
                oldValue = properties[index];
            }

            TimestampValueSet dynamicValue = null;
            if (oldValue == null) {
                try {
                    dynamicValue = (TimestampValueSet) column.getTypeClass().newInstance();
                } catch (InstantiationException ex) {
                    Logger.getLogger(ElementImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ElementImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                dynamicValue = (TimestampValueSet) oldValue;
            }


            int timestampIndex = timestampStore.getTimestampIndex(timestamp);
            dynamicValue.put(timestampIndex, value);
        } else {
            throw new RuntimeException("The timestamp store is not available");
        }
    }

    @Override
    public boolean addTimestamp(double timestamp) {
        checkDouble(timestamp);

        final TimestampStore timestampStore = getTimestampStore();
        if (timestampStore != null) {
            if (timestampSet == null) {
                timestampSet = new TimestampSet();
            }
            final int timestampIndex = timestampStore.addElement(timestamp, this);
            return timestampSet.add(timestampIndex);
        }
        return false;
    }

    @Override
    public boolean removeTimestamp(double timestamp) {
        checkDouble(timestamp);

        if (timestampSet != null) {
            final TimestampStore timestampStore = getTimestampStore();
            if (timestampStore != null) {
                final int timestampIndex = timestampStore.removeElement(timestamp, this);
                return timestampSet.remove(timestampIndex);
            }
        }
        return false;
    }

    @Override
    public double[] getTimestamps() {
        if (timestampSet != null) {
            final TimestampStore timestampStore = getTimestampStore();
            if (timestampStore != null) {
                final int[] indices = timestampSet.getTimestamps();
                return timestampStore.getTimestamps(indices);
            }
        }
        return new double[0];
    }

    protected void indexProperties() {
        ColumnStore propertyStore = getPropertyStore();
        if (propertyStore != null) {
            propertyStore.indexStore.index(this);
        }
        TimestampStore timestampStore = getTimestampStore();
        if (timestampStore != null) {
            timestampStore.index(this);
        }
    }

    @Override
    public void clearProperties() {
        if (isValid()) {
            ColumnStore propertyStore = getPropertyStore();
            if (propertyStore != null) {
                propertyStore.indexStore.clear(this);
            }
            TimestampStore timestampStore = getTimestampStore();
            if (timestampStore != null) {
                timestampStore.clear(this);
            }
        }

        properties = new Object[0];

        if (timestampSet != null) {
            timestampSet.clear();
        }
    }

    protected GraphStore getGraphStore() {
        return graphStore;
    }

    protected TimestampStore getTimestampStore() {
        if (graphStore != null) {
            return graphStore.timestampStore;
        }
        return null;
    }

    private void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can' be NaN or infinity");
        }
    }
    
    private void checkColumn(Column column) {
        if(column.getIndex() == ColumnStore.NULL_ID) {
            throw new IllegalArgumentException("The column does not exist");
        }
        ColumnStore columnStore = getPropertyStore();
        if(columnStore.getColumnByIndex(column.getIndex()) != column) {
            throw new IllegalArgumentException("The column does not belong to the right column store");
        }
    }

    private void checkType(Column column, Object value) {
        if (value != null) {
            Class typeClass = column.getTypeClass();
            if (TimestampValueSet.class.isAssignableFrom(typeClass)) {
                if ((value instanceof Double && !typeClass.equals(TimestampDoubleSet.class))
                        || (value instanceof Float && !typeClass.equals(TimestampFloatSet.class))
                        || (value instanceof Boolean && !typeClass.equals(TimestampBooleanSet.class))
                        || (value instanceof Integer && !typeClass.equals(TimestampIntegerSet.class))
                        || (value instanceof Long && !typeClass.equals(TimestampLongSet.class))
                        || (value instanceof Short && !typeClass.equals(TimestampShortSet.class))
                        || (value instanceof Byte && !typeClass.equals(TimestampByteSet.class))
                        || (value instanceof String && !typeClass.equals(TimestampStringSet.class))
                        || (value instanceof Character && !typeClass.equals(TimestampCharSet.class))) {
                    throw new IllegalArgumentException("The object class does not match with the dynamic type (" + typeClass.getName() + ")");
                } else {
                    throw new IllegalArgumentException("Unknown TimestampValueSet class");
                }
            } else if (!value.getClass().equals(typeClass)) {
                throw new IllegalArgumentException("The object class does not match with the column type (" + typeClass.getName() + ")");
            }
        }
    }
}
