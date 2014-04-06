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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.time.Estimator;
import org.gephi.attribute.time.Interval;
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
import org.gephi.graph.api.GraphView;

/**
 *
 * @author mbastian
 */
public abstract class ElementImpl implements Element {

    //Reference to store
    protected final GraphStore graphStore;
    //Attributes
    protected Object[] attributes;

    public ElementImpl(Object id, GraphStore graphStore) {
        if (id == null) {
            throw new NullPointerException();
        }
        this.graphStore = graphStore;
    }

    abstract ColumnStore getColumnStore();

    abstract TimestampMap getTimestampMap();

    abstract TimestampIndexStore getTimestampIndexStore();

    abstract boolean isValid();

    @Override
    public Object getId() {
        return attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX];
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
        synchronized (this) {
            if (index < attributes.length) {
                res = attributes[index];
            }
        }

        if (res == null) {
            return column.getDefaultValue();
        }
        return res;
    }

    @Override
    public Object getAttribute(String key, double timestamp) {
        return getAttribute(getColumnStore().getColumn(key), timestamp);
    }

    @Override
    public Object getAttribute(Column column, double timestamp) {
        checkEnabledTimestampSet();
        checkDouble(timestamp);
        checkColumn(column);
        checkColumnDynamic(column);

        Object res = null;
        final TimestampMap timestampMap = getTimestampMap();
        if (timestampMap != null) {

            int index = column.getIndex();
            TimestampValueSet dynamicValue = null;
            synchronized (this) {
                if (index < attributes.length) {
                    dynamicValue = (TimestampValueSet) attributes[index];
                }
                if (dynamicValue != null) {
                    int timestampIndex = timestampMap.getTimestampIndex(timestamp);
                    res = dynamicValue.get(timestampIndex, column.getDefaultValue());
                }
            }
        } else {
            throw new RuntimeException("The timestamp store is not available");
        }
        return res;
    }

    @Override
    public Object getAttribute(String key, GraphView view) {
        return getAttribute(getColumnStore().getColumn(key), view);
    }

    @Override
    public Object getAttribute(Column column, GraphView view) {
        checkColumn(column);

        if (!column.isDynamic()) {
            return getAttribute(column);
        } else {
            Interval interval = view.getTimeInterval();
            checkEnabledTimestampSet();
            checkViewExist((GraphViewImpl) view);
            final ColumnStore columnStore = getColumnStore();
            final TimestampMap timestampMap = columnStore.getTimestampMap(column);
            if (timestampMap != null) {
                int index = column.getIndex();
                TimestampValueSet dynamicValue = null;
                synchronized (this) {
                    if (index < attributes.length) {
                        dynamicValue = (TimestampValueSet) attributes[index];
                    }
                    if (dynamicValue != null && !dynamicValue.isEmpty()) {
                        int[] timestampIndices = timestampMap.getTimestampIndices(interval);
                        Estimator estimator = columnStore.getEstimator(column);
                        if (estimator == null) {
                            estimator = Estimator.FIRST;
                        }
                        return dynamicValue.get(null, timestampIndices, estimator);
                    }
                }
            } else {
                throw new RuntimeException("The timestamp store is not available");
            }
        }
        return null;
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
        checkReadOnlyColumn(column);

        ColumnStore columnStore = getColumnStore();
        int index = column.getIndex();
        synchronized (this) {
            if (index < attributes.length) {
                Object oldValue = attributes[index];
                attributes[index] = null;
                if (column.isIndexed() && columnStore != null && isValid()) {
                    columnStore.indexStore.set(column, oldValue, column.getDefaultValue(), this);
                }
                ((ColumnImpl)column).incrementVersion();
                return oldValue;
            }
        }
        return null;
    }

    @Override
    public void setLabel(String label) {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_LABEL) {
            int index = GraphStoreConfiguration.ELEMENT_LABEL_INDEX;
            synchronized (this) {
                if (index >= attributes.length) {
                    Object[] newArray = new Object[index + 1];
                    System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                    attributes = newArray;
                }
                attributes[index] = label;
            }
        }
    }

    @Override
    public void setAttribute(String key, Object value) {
        setAttribute(getColumnStore().getColumn(key), value);
    }

    @Override
    public void setAttribute(Column column, Object value) {
        checkColumn(column);
        checkReadOnlyColumn(column);
        checkType(column, value);

        int index = column.getIndex();
        ColumnStore columnStore = getColumnStore();
        Object oldValue = null;

        synchronized (this) {
            if (index >= attributes.length) {
                Object[] newArray = new Object[index + 1];
                System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                attributes = newArray;
            } else {
                oldValue = attributes[index];
            }

            if (column.isIndexed() && columnStore != null && isValid()) {
                value = columnStore.indexStore.set(column, oldValue, value, this);
            }
            attributes[index] = value;
            ((ColumnImpl)column).incrementVersion();
        }
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        setAttribute(getColumnStore().getColumn(key), value, timestamp);
    }

    @Override
    public void setAttribute(Column column, Object value, double timestamp) {
        checkEnabledTimestampSet();
        checkColumn(column);
        checkColumnDynamic(column);
        checkReadOnlyColumn(column);
        checkType(column, value);
        checkDouble(timestamp);

        final TimestampMap timestampMap = getColumnStore().getTimestampMap(column);
        if (timestampMap != null) {
            int index = column.getIndex();
            Object oldValue = null;
            synchronized (this) {
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
                        attributes[index] = dynamicValue = (TimestampValueSet) column.getTypeClass().newInstance();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    dynamicValue = (TimestampValueSet) oldValue;
                }

                int timestampIndex = timestampMap.getTimestampIndex(timestamp);
                dynamicValue.put(timestampIndex, value);
                ((ColumnImpl)column).incrementVersion();
            }
        } else {
            throw new RuntimeException("The timestamp store is not available");
        }
    }

    @Override
    public boolean addTimestamp(double timestamp) {
        checkEnabledTimestampSet();
        checkDouble(timestamp);

        final TimestampIndexStore timestampStore = getTimestampIndexStore();
        if (timestampStore != null) {
            synchronized (this) {
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
                final int timestampIndex = timestampStore.add(timestamp, this);
                return timestampSet.add(timestampIndex);
            }
        }
        return false;
    }

    @Override
    public boolean removeTimestamp(double timestamp) {
        checkEnabledTimestampSet();
        checkDouble(timestamp);

        synchronized (this) {
            TimestampSet timestampSet = getTimestampSet();
            if (timestampSet != null) {
                final TimestampIndexStore timestampStore = getTimestampIndexStore();
                if (timestampStore != null) {
                    final int timestampIndex = timestampStore.remove(timestamp, this);
                    return timestampSet.remove(timestampIndex);
                }
            }
        }
        return false;
    }

    @Override
    public double[] getTimestamps() {
        checkEnabledTimestampSet();

        synchronized (this) {
            TimestampSet timestampSet = getTimestampSet();
            if (timestampSet != null) {
                final TimestampMap timestampMap = getTimestampMap();
                if (timestampMap != null) {
                    final int[] indices = timestampSet.getTimestamps();
                    return timestampMap.getTimestamps(indices);
                }
            }
        }
        return new double[0];
    }

    @Override
    public boolean hasTimestamp(double timestamp) {
        checkEnabledTimestampSet();

        synchronized (this) {
            TimestampSet timestampSet = getTimestampSet();
            if (timestampSet != null) {
                final TimestampMap timestampMap = getTimestampMap();
                if (timestampMap != null) {
                    if (timestampMap.hasTimestampIndex(timestamp)) {
                        return timestampSet.contains(timestampMap.getTimestampIndex(timestamp));
                    }
                }
            }
        }
        return false;
    }

    public Iterable<Map.Entry<Double, Object>> getAttributes(Column column) {
        checkEnabledTimestampSet();
        checkColumn(column);
        checkColumnDynamic(column);

        Object res = null;
        final TimestampMap timestampMap = getColumnStore().getTimestampMap(column);
        if (timestampMap != null) {

            int index = column.getIndex();
            TimestampValueSet dynamicValue = null;
            synchronized (this) {
                if (index < attributes.length) {
                    dynamicValue = (TimestampValueSet) attributes[index];
                }
                if (dynamicValue != null) {
                    Object[] values = dynamicValue.toArray();
                    double[] timestamps = timestampMap.getTimestamps(dynamicValue.getTimestamps());
                    return new DynamicValueIterable(timestamps, values);
                }
            }
        }
        return DynamicValueIterable.EMPTY_ITERABLE;
    }

    protected TimestampSet getTimestampSet() {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_TIMESTAMP_SET && GraphStoreConfiguration.ELEMENT_TIMESTAMP_INDEX < attributes.length) {
            return (TimestampSet) attributes[GraphStoreConfiguration.ELEMENT_TIMESTAMP_INDEX];
        }
        return null;
    }

    protected void indexAttributes() {
        synchronized (this) {
            ColumnStore columnStore = getColumnStore();
            if (columnStore != null) {
                columnStore.indexStore.index(this);
            }
            final TimestampIndexStore timestampStore = getTimestampIndexStore();
            if (timestampStore != null) {
                timestampStore.index(this);
            }
        }
    }

    @Override
    public void clearAttributes() {
        synchronized (this) {
            if (isValid()) {
                ColumnStore columnStore = getColumnStore();
                if (columnStore != null) {
                    columnStore.indexStore.clear(this);
                }
                final TimestampIndexStore timestampStore = getTimestampIndexStore();
                if (timestampStore != null) {
                    timestampStore.clear(this);
                }
            }

            TimestampSet timestampSet = getTimestampSet();
            if (timestampSet != null) {
                timestampSet.clear();
            }

            Object[] newAttributes = new Object[GraphStoreConfiguration.ELEMENT_ID_INDEX + 1];
            newAttributes[GraphStoreConfiguration.ELEMENT_ID_INDEX] = attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX];
            attributes = newAttributes;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.getId().hashCode();
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
        final ElementImpl other = (ElementImpl) obj;
        if (!this.getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    protected GraphStore getGraphStore() {
        return graphStore;
    }

    void checkEnabledTimestampSet() {
        if (!GraphStoreConfiguration.ENABLE_ELEMENT_TIMESTAMP_SET) {
            throw new RuntimeException("Can't call timestamp methods if they are disabled");
        }
    }

    void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can't be NaN or infinity");
        }
    }

    void checkColumn(Column column) {
        if (column.getIndex() == ColumnStore.NULL_ID) {
            throw new IllegalArgumentException("The column does not exist");
        }
        ColumnStore columnStore = getColumnStore();
        if (columnStore != null && columnStore.getColumnByIndex(column.getIndex()) != column) {
            throw new IllegalArgumentException("The column does not belong to the right column store");
        }
    }

    void checkReadOnlyColumn(Column column) {
        if (column.isReadOnly()) {
            throw new RuntimeException("Can't modify the read-only '" + column.getId() + "' column");
        }
    }

    void checkColumnDynamic(Column column) {
        if (!((ColumnImpl) column).isDynamic()) {
            throw new IllegalArgumentException("The column is not dynamic");
        }
    }

    void checkType(Column column, Object value) {
        if (value != null) {
            Class typeClass = column.getTypeClass();
            if (TimestampValueSet.class.isAssignableFrom(typeClass)) {
                if ((value instanceof Double && (!typeClass.equals(TimestampDoubleSet.class)))
                        || (value instanceof Float && !typeClass.equals(TimestampFloatSet.class))
                        || (value instanceof Boolean && !typeClass.equals(TimestampBooleanSet.class))
                        || (value instanceof Integer && !typeClass.equals(TimestampIntegerSet.class))
                        || (value instanceof Long && !typeClass.equals(TimestampLongSet.class))
                        || (value instanceof Short && !typeClass.equals(TimestampShortSet.class))
                        || (value instanceof Byte && !typeClass.equals(TimestampByteSet.class))
                        || (value instanceof String && !typeClass.equals(TimestampStringSet.class))
                        || (value instanceof Character && !typeClass.equals(TimestampCharSet.class))) {
                    throw new IllegalArgumentException("The object class does not match with the dynamic type (" + typeClass.getName() + ")");
                }
            } else if (!value.getClass().equals(typeClass)) {
                throw new IllegalArgumentException("The object class does not match with the column type (" + typeClass.getName() + ")");
            }
        }
    }

    void checkViewExist(final GraphViewImpl view) {
        graphStore.viewStore.checkNonNullViewObject(view);
        graphStore.viewStore.checkViewExist(view);
    }

    private static class DynamicValueIterable implements Iterable<Map.Entry<Double, Object>> {

        private static Iterable<Map.Entry<Double, Object>> EMPTY_ITERABLE = new Iterable<Map.Entry<Double, Object>>() {

            @Override
            public Iterator<Map.Entry<Double, Object>> iterator() {
                return Collections.emptyIterator();
            }
        };
        private final double[] timestamps;
        private final Object[] values;

        public DynamicValueIterable(double[] timestamps, Object[] values) {
            this.timestamps = timestamps;
            this.values = values;
        }

        @Override
        public Iterator<Map.Entry<Double, Object>> iterator() {
            return new DynamicValueIterator(timestamps, values);
        }
    }

    private static class DynamicValueIterator implements Iterator<Map.Entry<Double, Object>> {

        private final Entry entry = new Entry();
        private final double[] timestamps;
        private final Object[] values;
        private int index;

        public DynamicValueIterator(double[] timestamps, Object[] values) {
            this.timestamps = timestamps;
            this.values = values;
        }

        @Override
        public boolean hasNext() {
            return index < timestamps.length;
        }

        @Override
        public Map.Entry<Double, Object> next() {
            entry.timestamp = timestamps[index];
            entry.value = values[index++];
            return entry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        private static class Entry implements Map.Entry<Double, Object> {

            private double timestamp;
            private Object value;

            @Override
            public Double getKey() {
                return timestamp;
            }

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public Object setValue(Object value) {
                throw new UnsupportedOperationException("Not supported");
            }

        }
    }
}
