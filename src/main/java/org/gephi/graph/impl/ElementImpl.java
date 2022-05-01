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

package org.gephi.graph.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnIterable;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalBooleanMap;
import org.gephi.graph.api.types.IntervalByteMap;
import org.gephi.graph.api.types.IntervalCharMap;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalFloatMap;
import org.gephi.graph.api.types.IntervalIntegerMap;
import org.gephi.graph.api.types.IntervalLongMap;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalShortMap;
import org.gephi.graph.api.types.IntervalStringMap;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.graph.api.types.TimestampBooleanMap;
import org.gephi.graph.api.types.TimestampByteMap;
import org.gephi.graph.api.types.TimestampCharMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampFloatMap;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.gephi.graph.api.types.TimestampLongMap;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampShortMap;
import org.gephi.graph.api.types.TimestampStringMap;

public abstract class ElementImpl implements Element {

    // Reference to store
    protected final GraphStore graphStore;
    // Attributes
    protected final AttributesImpl attributes;

    public ElementImpl(Object id, GraphStore graphStore) {
        if (id == null) {
            throw new NullPointerException();
        }
        this.graphStore = graphStore;
        this.attributes = new AttributesImpl(getColumnStore());
        this.attributes.setId(id);
    }

    abstract ColumnStore getColumnStore();

    abstract TimeIndexStore getTimeIndexStore();

    abstract boolean isValid();

    @Override
    public Object getId() {
        return attributes.getId();
    }

    @Override
    public String getLabel() {
        return attributes.getLabel();
    }

    @Override
    public void setLabel(String label) {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_LABEL) {
            setAttribute(getColumnStore().getColumnByIndex(GraphStoreConfiguration.ELEMENT_LABEL_INDEX), label);
        }
    }

    @Override
    public Object getAttribute(String key) {
        return getAttribute(checkColumnExists(key));
    }

    @Override
    public Object getAttribute(Column column) {
        checkColumn(column);

        return attributes.getAttribute(column);
    }

    @Override
    public Object getAttribute(String key, double timestamp) {
        return getAttribute(checkColumnExists(key), timestamp);
    }

    @Override
    public Object getAttribute(Column column, double timestamp) {
        checkTimeRepresentationTimestamp();
        checkDouble(timestamp);
        checkColumn(column);
        checkColumnDynamic(column);
        return attributes.getAttribute(column, timestamp, null);
    }

    @Override
    public Object getAttribute(String key, Interval interval) {
        return getAttribute(checkColumnExists(key), interval);
    }

    @Override
    public Object getAttribute(Column column, Interval interval) {
        checkTimeRepresentationInterval();
        checkColumn(column);
        checkColumnDynamic(column);
        return attributes.getAttribute(column, interval, null);
    }

    @Override
    public Object getAttribute(String key, GraphView view) {
        return getAttribute(checkColumnExists(key), view);
    }

    @Override
    public Object getAttribute(Column column, GraphView view) {
        checkColumn(column);

        if (!column.isDynamic()) {
            return getAttribute(column);
        } else {
            Interval interval = view.getTimeInterval();
            checkViewExist(view);
            return attributes.getAttribute(column, interval, getEstimator(column));
        }
    }

    @Override
    public Object[] getAttributes() {
        return attributes.getBackingArray();
    }

    @Override
    public Set<String> getAttributeKeys() {
        return getColumnStore().getColumnKeys();
    }

    @Override
    public ColumnIterable getAttributeColumns() {
        return getColumnStore();
    }

    @Override
    public Object removeAttribute(String key) {
        return removeAttribute(checkColumnExists(key));
    }

    @Override
    public Object removeAttribute(Column column) {
        checkColumn(column);
        checkReadOnlyColumn(column);

        Object oldValue = attributes.setAttribute(column, column.getDefaultValue());
        updateIndex(column, oldValue, column.getDefaultValue());

        return oldValue;
    }

    @Override
    public Object removeAttribute(Column column, double timestamp) {
        checkTimeRepresentationTimestamp();
        checkDouble(timestamp);
        return removeTimeAttribute(column, timestamp);
    }

    @Override
    public Object removeAttribute(String key, double timestamp) {
        return removeAttribute(checkColumnExists(key), timestamp);
    }

    @Override
    public Object removeAttribute(Column column, Interval interval) {
        checkTimeRepresentationInterval();
        return removeTimeAttribute(column, interval);
    }

    @Override
    public Object removeAttribute(String key, Interval interval) {
        return removeAttribute(checkColumnExists(key), interval);
    }

    private Object removeTimeAttribute(Column column, Object timeObject) {
        checkColumn(column);
        checkColumnDynamic(column);
        checkReadOnlyColumn(column);

        Object oldValue = attributes.removeTimeAttribute(column, timeObject);

        // TODO
        if (oldValue != null && isValid()) {
            TimeIndexStore timeIndexStore = getTimeIndexStore();
            if (timeIndexStore != null) {
                timeIndexStore.remove(timeObject);
            }
            ((ColumnImpl) column).incrementVersion(this);
        }
        return oldValue;
    }

    @Override
    public void setAttribute(String key, Object value) {
        setAttribute(checkColumnExists(key), value);
    }

    @Override
    public void setAttribute(Column column, Object value) {
        checkColumn(column);
        checkReadOnlyColumn(column);

        value = AttributeUtils.standardizeValue(value);
        checkType(column, value);

        Object oldValue = attributes.setAttribute(column, value);
        updateIndex(column, oldValue, value);
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        setAttribute(checkColumnExists(key), value, timestamp);
    }

    @Override
    public void setAttribute(Column column, Object value, double timestamp) {
        checkTimeRepresentationTimestamp();
        checkDouble(timestamp);
        setTimeAttribute(column, value, timestamp);
    }

    @Override
    public void setAttribute(String key, Object value, Interval interval) {
        setAttribute(checkColumnExists(key), value, interval);
    }

    @Override
    public void setAttribute(Column column, Object value, Interval interval) {
        checkTimeRepresentationInterval();
        setTimeAttribute(column, value, interval);
    }

    private void setTimeAttribute(Column column, Object value, Object timeObject) {
        checkColumn(column);
        checkColumnDynamic(column);
        checkReadOnlyColumn(column);
        checkType(column, value);

        Object newValue = attributes.setAttribute(column, value, timeObject);
        updateIndex(column, null, newValue);
    }

    private void updateIndex(Column column, Object oldValue, Object newValue) {
        // Update index
        if (isValid()) {
            ColumnStore columnStore = getColumnStore();
            ColumnImpl columnImpl = (ColumnImpl) column;
            if (columnImpl.isDynamic()) {
                TimeIndexStore timeIndexStore = getTimeIndexStore();
                if (timeIndexStore != null) {
                    if (TimeMap.class.isAssignableFrom(columnImpl.getTypeClass())) {
                        if (oldValue instanceof TimeMap) {
                            timeIndexStore.remove((TimeMap) oldValue);
                        } else if (oldValue != null) {
                            timeIndexStore.remove(oldValue);
                        }
                        if (newValue instanceof TimeMap) {
                            timeIndexStore.add((TimeMap) newValue);
                        } else if (newValue != null) {
                            timeIndexStore.add(newValue);
                        }
                    } else if (TimeSet.class.isAssignableFrom(columnImpl.getTypeClass())) {
                        if (oldValue instanceof TimeSet) {
                            timeIndexStore.remove((TimeSet) oldValue, this);
                        } else if (oldValue != null) {
                            timeIndexStore.remove(oldValue, this);
                        }
                        if (newValue instanceof TimeSet) {
                            timeIndexStore.add((TimeSet) newValue, this);
                        } else if (newValue != null) {
                            timeIndexStore.add(newValue, this);
                        }
                    }
                }
            }
            if (columnStore != null) {
                columnStore.indexStore.set(column, oldValue, newValue, this);
            }
            columnImpl.incrementVersion(this);
        }
    }

    @Override
    public boolean addTimestamp(double timestamp) {
        checkDouble(timestamp);
        checkTimeRepresentationTimestamp();
        return addTime(timestamp);
    }

    @Override
    public boolean addInterval(Interval interval) {
        checkTimeRepresentationInterval();
        return addTime(interval);
    }

    private boolean addTime(Object timeObject) {
        checkEnabledTimeSet();

        boolean res = attributes.addTime(timeObject);
        if (res) {
            Column column = getColumnStore().getColumnByIndex(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
            updateIndex(column, null, timeObject);
        }
        return res;
    }

    @Override
    public boolean removeTimestamp(double timestamp) {
        checkDouble(timestamp);
        checkTimeRepresentationTimestamp();
        return removeTime(timestamp);
    }

    @Override
    public boolean removeInterval(Interval interval) {
        checkTimeRepresentationInterval();
        return removeTime(interval);
    }

    private boolean removeTime(Object timeObject) {
        checkEnabledTimeSet();

        boolean res = attributes.removeTime(timeObject);
        if (res) {
            Column column = getColumnStore().getColumnByIndex(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
            updateIndex(column, timeObject, null);
        }
        return res;
    }

    @Override
    public double[] getTimestamps() {
        checkTimeRepresentationTimestamp();
        checkEnabledTimeSet();
        Object res = attributes.getTimeSetArray();
        if (res == null) {
            return new double[0];
        }
        return (double[]) res;
    }

    @Override
    public Interval[] getIntervals() {
        checkTimeRepresentationInterval();
        checkEnabledTimeSet();
        Object res = attributes.getTimeSetArray();
        if (res == null) {
            return new Interval[0];
        }
        return (Interval[]) res;
    }

    @Override
    public Interval getTimeBounds() {
        checkEnabledTimeSet();
        TimeSet timeSet = attributes.getTimeSet();
        if (timeSet != null) {
            Double min = timeSet.getMinDouble();
            Double max = timeSet.getMaxDouble();
            if (min != null) {
                return new Interval(min, max);
            }
        }
        return null;
    }

    @Override
    public boolean hasTimestamp(double timestamp) {
        checkTimeRepresentationTimestamp();
        checkEnabledTimeSet();
        return attributes.hasTime(timestamp);
    }

    @Override
    public boolean hasInterval(Interval interval) {
        checkTimeRepresentationInterval();
        checkEnabledTimeSet();
        return attributes.hasTime(interval);
    }

    @Override
    public Iterable<Map.Entry> getAttributes(Column column) {
        checkColumn(column);
        checkColumnDynamic(column);

        return attributes.getAttributes(column);
    }

    // Called when elements are added
    // TODO
    protected void indexAttributes() {
        synchronized (this) {
            ColumnStore columnStore = getColumnStore();
            if (columnStore != null) {
                columnStore.indexStore.index(this);
            }

            TimeIndexStore timeIndexStore = getTimeIndexStore();
            if (timeIndexStore != null) {
                timeIndexStore.index(this);
            }
        }
    }

    @Override
    public void clearAttributes() {
        synchronized (this) {
            ColumnStore columnStore = getColumnStore();
            if (columnStore != null) {
                final int length = columnStore.length;
                final ColumnImpl[] cols = columnStore.columns;
                for (int i = 0; i < length; i++) {
                    Column c = cols[i];
                    if (!c.isProperty() && !c.isReadOnly()) {
                        removeAttribute(c);
                    }
                }
            }
        }
    }

    protected void destroyAttributes() {
        synchronized (this) {
            ColumnStore columnStore = getColumnStore();
            if (columnStore != null) {
                columnStore.indexStore.clear(this);
            }

            TimeIndexStore timeIndexStore = getTimeIndexStore();
            if (timeIndexStore != null) {
                timeIndexStore.clear(this);
            }
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
        return this.getId().equals(other.getId());
    }

    protected Estimator getEstimator(Column column) {
        Estimator estimator = column.getEstimator();
        if (estimator == null) {
            return GraphStoreConfiguration.DEFAULT_ESTIMATOR;
        }
        return estimator;
    }

    protected GraphStore getGraphStore() {
        return graphStore;
    }

    protected void checkTimeRepresentationTimestamp() {
        if (!getTimeRepresentation().equals(TimeRepresentation.TIMESTAMP)) {
            throw new RuntimeException(
                    "Can't use timestamps as the configuration is set to " + getTimeRepresentation());
        }
    }

    protected void checkTimeRepresentationInterval() {
        if (!getTimeRepresentation().equals(TimeRepresentation.INTERVAL)) {
            throw new RuntimeException("Can't use intervals as the configuration is set to " + getTimeRepresentation());
        }
    }

    void checkEnabledTimeSet() {
        if (!GraphStoreConfiguration.ENABLE_ELEMENT_TIME_SET) {
            throw new RuntimeException("Can't call timestamp or intervals methods if they are disabled");
        }
    }

    void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can't be NaN or infinity");
        }
    }

    Column checkColumnExists(String key) {
        Column col = getColumnStore().getColumn(key);
        if (col == null) {
            throw new IllegalArgumentException("The column '" + key + "' is not found");
        }
        return col;
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
            if (TimestampMap.class.isAssignableFrom(typeClass)) {
                checkTimeRepresentationTimestamp();
                if ((value instanceof Double && (!typeClass
                        .equals(TimestampDoubleMap.class))) || (value instanceof Float && !typeClass
                                .equals(TimestampFloatMap.class)) || (value instanceof Boolean && !typeClass
                                        .equals(TimestampBooleanMap.class)) || (value instanceof Integer && !typeClass
                                                .equals(TimestampIntegerMap.class)) || (value instanceof Long && !typeClass
                                                        .equals(TimestampLongMap.class)) || (value instanceof Short && !typeClass
                                                                .equals(TimestampShortMap.class)) || (value instanceof Byte && !typeClass
                                                                        .equals(TimestampByteMap.class)) || (value instanceof String && !typeClass
                                                                                .equals(TimestampStringMap.class)) || (value instanceof Character && !typeClass
                                                                                        .equals(TimestampCharMap.class))) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the dynamic type (" + typeClass.getName() + ")");
                }
            } else if (IntervalMap.class.isAssignableFrom(typeClass)) {
                checkTimeRepresentationInterval();
                if ((value instanceof Double && (!typeClass
                        .equals(IntervalDoubleMap.class))) || (value instanceof Float && !typeClass
                                .equals(IntervalFloatMap.class)) || (value instanceof Boolean && !typeClass
                                        .equals(IntervalBooleanMap.class)) || (value instanceof Integer && !typeClass
                                                .equals(IntervalIntegerMap.class)) || (value instanceof Long && !typeClass
                                                        .equals(IntervalLongMap.class)) || (value instanceof Short && !typeClass
                                                                .equals(IntervalShortMap.class)) || (value instanceof Byte && !typeClass
                                                                        .equals(IntervalByteMap.class)) || (value instanceof String && !typeClass
                                                                                .equals(IntervalStringMap.class)) || (value instanceof Character && !typeClass
                                                                                        .equals(IntervalCharMap.class))) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the dynamic type (" + typeClass.getName() + ")");
                }
            } else if (List.class.isAssignableFrom(typeClass)) {
                if (!(value instanceof List)) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the list type (" + typeClass.getName() + ")");
                }
            } else if (Set.class.isAssignableFrom(typeClass)) {
                if (!(value instanceof Set)) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the set type (" + typeClass.getName() + ")");
                }
            } else if (Map.class.isAssignableFrom(typeClass)) {
                if (!(value instanceof Map)) {
                    throw new IllegalArgumentException(
                            "The object class does not match with the map type (" + typeClass.getName() + ")");
                }
            } else if (!value.getClass().equals(typeClass)) {
                throw new IllegalArgumentException(
                        "The object class does not match with the column type (" + typeClass.getName() + ")");
            }
        }
    }

    void checkViewExist(final GraphView view) {
        graphStore.viewStore.checkNonNullViewObject(view);
        if (!view.isMainView()) {
            graphStore.viewStore.checkViewExist((GraphViewImpl) view);
        }
    }

    TimeRepresentation getTimeRepresentation() {
        if (graphStore != null) {
            return graphStore.configuration.getTimeRepresentation();
        }
        return GraphStoreConfiguration.DEFAULT_TIME_REPRESENTATION;
    }
}
