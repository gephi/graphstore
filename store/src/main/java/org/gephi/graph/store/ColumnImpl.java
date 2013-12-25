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

import org.gephi.attribute.api.AttributeUtils;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.gephi.attribute.api.Table;
import org.gephi.attribute.time.Estimator;
import org.gephi.attribute.time.TimestampValueSet;

/**
 *
 * @author mbastian
 */
public class ColumnImpl implements Column {

    //Attributes
    protected final TableImpl table;
    protected final String id;
    protected final Class typeClass;
    protected final String title;
    protected final Object defaultValue;
    protected final Origin origin;
    protected final boolean indexed;
    protected final boolean dynamic;
    protected final boolean readOnly;
    protected Estimator estimator;
    //Store Id
    protected int storeId = ColumnStore.NULL_ID;

    public ColumnImpl(TableImpl table, String id, Class typeClass, String title, Object defaultValue, Origin origin, boolean indexed, boolean readOnly) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("The column ID can't be null or empty");
        }
        if (typeClass == null) {
            throw new NullPointerException();
        }
        this.table = table;
        this.id = id;
        this.typeClass = typeClass;
        this.title = title;
        this.defaultValue = defaultValue;
        this.origin = origin;
        this.indexed = indexed;
        this.readOnly = readOnly;
        this.dynamic = TimestampValueSet.class.isAssignableFrom(typeClass);
    }

    public ColumnImpl(String id, Class typeClass, String title, Object defaultValue, Origin origin, boolean indexed, boolean readOnly) {
        this(null, id, typeClass, title, defaultValue, origin, indexed, readOnly);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getIndex() {
        return storeId;
    }

    @Override
    public Class getTypeClass() {
        return typeClass;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public boolean isIndexed() {
        return indexed;
    }

    @Override
    public boolean isArray() {
        return typeClass.isArray();
    }

    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean isNumber() {
        return AttributeUtils.isNumberType(typeClass);
    }

    @Override
    public boolean isProperty() {
        return origin.equals(Origin.PROPERTY);
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    @Override
    public String toString() {
        return title + " (" + typeClass.toString() + ")";
    }

    public Estimator getEstimator() {
        return estimator;
    }

    public void setEstimator(Estimator estimator) {
        this.estimator = estimator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Column) {
            ColumnImpl o = (ColumnImpl) obj;
            return id.equals(o.id) && o.typeClass == typeClass;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.typeClass != null ? this.typeClass.hashCode() : 0);
        return hash;
    }
}
