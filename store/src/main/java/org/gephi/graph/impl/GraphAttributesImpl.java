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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.impl.utils.MapDeepEquals;

public class GraphAttributesImpl {

    protected final Map<String, Object> attributes = new HashMap<String, Object>();

    public synchronized Set<String> getKeys() {
        return attributes.keySet();
    }

    public synchronized void setValue(String key, Object value) {
        if (value != null) {
            checkSupportedTypes(value.getClass());
        }
        attributes.put(key, AttributeUtils.standardizeValue(value));
    }

    public synchronized Object getValue(String key) {
        return attributes.get(key);
    }

    public synchronized Object getValue(String key, double timestamp) {
        TimestampMap valueSet = (TimestampMap) attributes.get(key);
        if (valueSet != null) {
            return valueSet.get(timestamp, null);
        }
        return null;
    }

    public synchronized void setValue(String key, Object value, double timestamp) {
        if (value == null) {
            throw new NullPointerException("The value can't be null for the key '" + key + "'");
        }
        checkSupportedDynamicTypes(value.getClass());

        TimestampMap valueSet = null;
        if (attributes.containsKey(key)) {
            valueSet = (TimestampMap) attributes.get(key);
        } else {
            try {
                valueSet = AttributeUtils.getTimestampMapType(value.getClass()).newInstance();
                attributes.put(key, valueSet);
            } catch (Exception ex) {
                throw new RuntimeException("The dynamic type can't be created", ex);
            }
        }
        if (!value.getClass().equals(valueSet.getTypeClass())) {
            throw new IllegalArgumentException("The value type " + value.getClass().getName() + " doesn't match with the expected type " + valueSet.getTypeClass().getName());
        }

        valueSet.put(timestamp, value);
    }

    protected void setGraphAttributes(GraphAttributesImpl graphAttributes) {
        attributes.putAll(graphAttributes.attributes);
    }

    private void checkSupportedTypes(Class type) {
        if (!AttributeUtils.isSupported(type)) {
            throw new IllegalArgumentException("Unknown type " + type.getName());
        }
    }

    private void checkSupportedDynamicTypes(Class type) {
        try {
            AttributeUtils.getTimestampMapType(type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported dynamic type " + type.getName());
        }
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 47 * hash + (this.attributes != null ? this.attributes.hashCode() : 0);
        return hash;
    }

    public boolean deepEquals(GraphAttributesImpl obj) {
        if (obj == null) {
            return false;
        }
        if (!MapDeepEquals.mapDeepEquals(attributes, obj.attributes)) {
            return false;
        }
        return true;
    }
}
