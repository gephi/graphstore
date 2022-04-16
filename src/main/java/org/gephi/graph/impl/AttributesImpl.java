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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;

public class AttributesImpl {

    // Attributes
    protected Object[] attributes;

    public AttributesImpl(ColumnStore columnStore) {
        if (columnStore != null) {
            int length = columnStore.length;
            final ColumnImpl[] cols = columnStore.columns;
            attributes = new Object[length];
            for (int i = 0; i < length; i++) {
                Column c = cols[i];
                if (c != null && !c.isProperty()) {
                    attributes[i] = c.getDefaultValue();
                }
            }
        } else {
            attributes = new Object[GraphStoreConfiguration.EDGE_DEFAULT_COLUMNS];
        }
    }

    public Object getId() {
        return attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX];
    }

    public void setId(Object id) {
        attributes[GraphStoreConfiguration.ELEMENT_ID_INDEX] = id;
    }

    public String getLabel() {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_LABEL && attributes.length > GraphStoreConfiguration.ELEMENT_LABEL_INDEX) {
            return (String) attributes[GraphStoreConfiguration.ELEMENT_LABEL_INDEX];
        }
        return null;
    }

    public Object getAttribute(Column column) {
        return getAttribute(column.getIndex());
    }

    public Object getAttribute(int index) {
        Object res = null;
        synchronized (this) {
            if (index < attributes.length) {
                res = attributes[index];
            }
        }

        return res;
    }

    protected Object getAttribute(Column column, Object timeObject, Estimator estimator) {
        int index = column.getIndex();
        synchronized (this) {
            TimeMap dynamicValue = null;
            if (index < attributes.length) {
                dynamicValue = (TimeMap) attributes[index];
            }
            if (dynamicValue != null && !dynamicValue.isEmpty()) {
                if (estimator == null) {
                    return dynamicValue.get(timeObject, column.getDefaultValue());
                } else {
                    return dynamicValue.get((Interval) timeObject, estimator);
                }
            }
        }
        return null;
    }

    protected Object removeTimeAttribute(Column column, Object timeObject) {
        int index = column.getIndex();
        Object oldValue = null;
        boolean res = false;
        synchronized (this) {
            TimeMap dynamicValue = (TimeMap) attributes[index];
            if (dynamicValue != null) {
                oldValue = dynamicValue.get(timeObject, null);

                res = dynamicValue.remove(timeObject);
            }
        }
        return oldValue;
    }

    protected Object setAttribute(Column column, Object value) {
        int index = column.getIndex();
        return setAttribute(index, value);
    }

    public Object setAttribute(int index, Object value) {
        Object oldValue = null;
        synchronized (this) {
            if (index >= attributes.length) {
                Object[] newArray = new Object[index + 1];
                System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                attributes = newArray;
            } else {
                oldValue = attributes[index];
            }
            attributes[index] = value;
        }
        return oldValue;
    }

    private Object ensureSize(int index) {
        if (index >= attributes.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(attributes, 0, newArray, 0, attributes.length);
            attributes = newArray;
        } else {
            return attributes[index];
        }
        return null;
    }

    protected Object setAttribute(Column column, Object value, Object timeObject) {
        int index = column.getIndex();
        Object oldValue = null;
        synchronized (this) {
            oldValue = ensureSize(index);
            TimeMap dynamicValue;
            if (oldValue == null) {
                try {
                    attributes[index] = dynamicValue = (TimeMap) column.getTypeClass().getDeclaredConstructor()
                            .newInstance();
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                        | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                dynamicValue = (TimeMap) oldValue;
            }

            dynamicValue.put(timeObject, value);
            return dynamicValue;
        }
    }

    protected boolean addTime(Object timeObject) {
        boolean res;
        synchronized (this) {
            TimeSet timeSet = getTimeSet();
            if (timeSet == null) {
                if (timeObject instanceof Interval) {
                    timeSet = new IntervalSet();
                } else {
                    timeSet = new TimestampSet();
                }
                int index = GraphStoreConfiguration.ELEMENT_TIMESET_INDEX;
                if (index >= attributes.length) {
                    Object[] newArray = new Object[index + 1];
                    System.arraycopy(attributes, 0, newArray, 0, attributes.length);
                    attributes = newArray;
                }
                attributes[index] = timeSet;
            }
            res = timeSet.add(timeObject);
        }
        return res;
    }

    protected boolean removeTime(Object timeObject) {
        boolean res = false;
        synchronized (this) {
            TimeSet timeSet = getTimeSet();
            if (timeSet != null) {
                res = timeSet.remove(timeObject);
            }
        }

        return res;
    }

    protected TimeSet getTimeSet() {
        if (GraphStoreConfiguration.ENABLE_ELEMENT_TIME_SET && GraphStoreConfiguration.ELEMENT_TIMESET_INDEX < attributes.length) {
            return (TimeSet) attributes[GraphStoreConfiguration.ELEMENT_TIMESET_INDEX];
        }
        return null;
    }

    protected boolean hasTime(Object timeObject) {
        synchronized (this) {
            TimeSet timeSet = getTimeSet();
            if (timeSet != null) {
                return timeSet.contains(timeObject);
            }
        }
        return false;
    }

    protected Iterable<Map.Entry> getAttributes(Column column) {
        int index = column.getIndex();
        TimeMap dynamicValue = null;
        synchronized (this) {
            if (index < attributes.length) {
                dynamicValue = (TimeMap) attributes[index];
            }
            if (dynamicValue != null) {
                Object[] values = dynamicValue.toValuesArray();
                if (dynamicValue instanceof TimestampMap) {
                    return new TimeAttributeIterable(((TimestampMap) dynamicValue).getTimestamps(), values);
                } else if (dynamicValue instanceof IntervalMap) {
                    return new TimeAttributeIterable(((IntervalMap) dynamicValue).toKeysArray(), values);
                }
            }

        }
        return TimeAttributeIterable.EMPTY_ITERABLE;
    }

    protected Object getTimeSetArray() {
        synchronized (this) {
            TimeSet timeSet = getTimeSet();
            if (timeSet != null) {
                return timeSet.toPrimitiveArray();
            }
        }
        return null;
    }

    public Object[] getBackingArray() {
        return attributes;
    }

    // Used by serialization
    protected void setBackingArray(Object[] attributes) {
        this.attributes = attributes;
    }
}
