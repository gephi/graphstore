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
    //Attributes
    protected Object[] attributes;

    public ElementImpl(Object id, GraphStore graphStore) {
        if (id == null) {
            throw new NullPointerException();
        }
        this.id = id;
        this.graphStore = graphStore;
        this.attributes = new Object[0];
    }

    abstract ColumnStore getColumnStore();

    abstract boolean isValid();

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public String getLabel() {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_LABEL && attributes.length > GraphStoreConfiguration.ELEMENT_LABEL_INDEX) {
            return (String) attributes[GraphStoreConfiguration.ELEMENT_LABEL_INDEX];
        }
        return null;
    }

    @Override
    public Object getAttribute(String key) {
        return getAttribute(getColumnStore().getColumn(key));
    }

    @Override
    public Object getAttribute(Column column) {
        checkColumn(column);

        int index = column.getIndex();
        Object res = null;
        if (index < attributes.length) {
            res = attributes[index];
        }
        if (res == null) {
            return column.getDefaultValue();
        }
        return res;
    }

    @Override
    public Object[] getAttributes() {
        return attributes;
    }

    @Override
    public Set<String> getAttributeKeys() {
        return getColumnStore().getColumnKeys();
    }

    @Override
    public Object removeAttribute(String key) {
        return removeAttribute(getColumnStore().getColumn(key));
    }

    @Override
    public Object removeAttribute(Column column) {
        checkColumn(column);

        ColumnStore columnStore = getColumnStore();
        int index = column.getIndex();
        if (index < attributes.length) {
            Object oldValue = attributes[index];
            attributes[index] = null;
            if (column.isIndexed() && columnStore != null && isValid()) {
                writeLock();
                try {
                    columnStore.indexStore.set(column, oldValue, column.getDefaultValue(), this);
                } finally {
                    writeUnlock();
                }
            }
            return oldValue;
        }
        return null;
    }

    @Override
    public void setLabel(String label) {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_LABEL) {
            int index = GraphStoreConfiguration.ELEMENT_LABEL_INDEX;
            if (index >= attributes.length) {
                Object[] newArray = new Object[index + 1];
                System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                attributes = newArray;
            }
            attributes[index] = label;
        }
    }

    @Override
    public void setAttribute(String key, Object value) {
        setAttribute(getColumnStore().getColumn(key), value);
    }

    @Override
    public void setAttribute(Column column, Object value) {
        checkType(column, value);
        checkColumn(column);

        int index = column.getIndex();
        ColumnStore columnStore = getColumnStore();
        Object oldValue = null;
        if (index >= attributes.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(attributes, 0, newArray, 0, attributes.length);
            attributes = newArray;
        } else {
            oldValue = attributes[index];
        }

        if (column.isIndexed() && columnStore != null && isValid()) {
            writeLock();
            try {
                value = columnStore.indexStore.set(column, oldValue, value, this);
            } finally {
                writeUnlock();
            }
        }
        attributes[index] = value;
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        setAttribute(getColumnStore().getColumn(key), value, timestamp);
    }

    @Override
    public void setAttribute(Column column, Object value, double timestamp) {
        checkEnabledTimestampSet();
        checkType(column, value);
        checkDouble(timestamp);
        checkColumn(column);

        final TimestampStore timestampStore = getTimestampStore();
        if (timestampStore != null) {
            writeLock();
            try {
                int index = column.getIndex();
                Object oldValue = null;
                if (index >= attributes.length) {
                    Object[] newArray = new Object[index + 1];
                    System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                    attributes = newArray;
                } else {
                    oldValue = attributes[index];
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
            } finally {
                writeUnlock();
            }
        } else {
            throw new RuntimeException("The timestamp store is not available");
        }
    }

    @Override
    public boolean addTimestamp(double timestamp) {
        checkEnabledTimestampSet();
        checkDouble(timestamp);

        final TimestampStore timestampStore = getTimestampStore();
        if (timestampStore != null) {
            writeLock();
            try {
                TimestampSet timestampSet = getTimestampSet();
                if (timestampSet == null) {
                    timestampSet = new TimestampSet();
                    int index = GraphStoreConfiguration.ELEMENT_TIMESTAMP_INDEX;
                    if (index >= attributes.length) {
                        Object[] newArray = new Object[index + 1];
                        System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                        attributes = newArray;
                    }
                    attributes[index] = timestampSet;
                }
                final int timestampIndex = timestampStore.addElement(timestamp, this);
                return timestampSet.add(timestampIndex);
            } finally {
                writeUnlock();
            }
        }
        return false;
    }

    @Override
    public boolean removeTimestamp(double timestamp) {
        checkEnabledTimestampSet();
        checkDouble(timestamp);

        TimestampSet timestampSet = getTimestampSet();
        if (timestampSet != null) {
            writeLock();
            try {
                final TimestampStore timestampStore = getTimestampStore();
                if (timestampStore != null) {
                    final int timestampIndex = timestampStore.removeElement(timestamp, this);
                    return timestampSet.remove(timestampIndex);
                }
            } finally {
                writeUnlock();
            }
        }
        return false;
    }

    @Override
    public double[] getTimestamps() {
        checkEnabledTimestampSet();

        TimestampSet timestampSet = getTimestampSet();
        if (timestampSet != null) {
            final TimestampStore timestampStore = getTimestampStore();
            if (timestampStore != null) {
                readLock();
                try {
                    final int[] indices = timestampSet.getTimestamps();
                    return timestampStore.getTimestamps(indices);
                } finally {
                    readUnlock();
                }
            }
        }
        return new double[0];
    }

    protected TimestampSet getTimestampSet() {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_TIMESTAMP_SET && GraphStoreConfiguration.ELEMENT_TIMESTAMP_INDEX < attributes.length) {
            return (TimestampSet) attributes[GraphStoreConfiguration.ELEMENT_TIMESTAMP_INDEX];
        }
        return null;
    }

    protected void indexAttributes() {
        ColumnStore columnStore = getColumnStore();
        if (columnStore != null) {
            columnStore.indexStore.index(this);
        }
        TimestampStore timestampStore = getTimestampStore();
        if (timestampStore != null) {
            timestampStore.index(this);
        }
    }

    @Override
    public void clearAttributes() {
        writeLock();
        try {
            if (isValid()) {
                ColumnStore columnStore = getColumnStore();
                if (columnStore != null) {
                    columnStore.indexStore.clear(this);
                }
                TimestampStore timestampStore = getTimestampStore();
                if (timestampStore != null) {
                    timestampStore.clear(this);
                }
            }


            TimestampSet timestampSet = getTimestampSet();
            if (timestampSet != null) {
                timestampSet.clear();
            }

            attributes = new Object[0];
        } finally {
            writeUnlock();
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

    private void checkEnabledTimestampSet() {
        if (!GraphStoreConfiguration.ENABLE_ELEMENT_TIMESTAMP_SET) {
            throw new RuntimeException("Can't call timestamp methods if they are disabled");
        }
    }

    private void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can' be NaN or infinity");
        }
    }

    private void checkColumn(Column column) {
        if (column.getIndex() == ColumnStore.NULL_ID) {
            throw new IllegalArgumentException("The column does not exist");
        }
        ColumnStore columnStore = getColumnStore();
        if (columnStore != null && columnStore.getColumnByIndex(column.getIndex()) != column) {
            throw new IllegalArgumentException("The column does not belong to the right column store");
        }
    }

    private void readLock() {
        if (graphStore != null) {
            graphStore.autoReadLock();
        }
    }

    private void readUnlock() {
        if (graphStore != null) {
            graphStore.autoReadUnlock();
        }
    }

    private void writeLock() {
        if (graphStore != null) {
            graphStore.writeLock();
        }
    }

    private void writeUnlock() {
        if (graphStore != null) {
            graphStore.writeUnlock();
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
