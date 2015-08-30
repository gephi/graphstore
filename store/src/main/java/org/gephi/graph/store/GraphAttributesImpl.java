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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.gephi.attribute.api.AttributeUtils;
import org.gephi.attribute.time.TimestampValueSet;
import org.gephi.graph.utils.MapDeepEquals;

/**
 *
 * @author mbastian
 */
public class GraphAttributesImpl {

    protected final TimestampMap timestampMap = new TimestampMap();
    protected final Map<String, Object> attributes = new HashMap<String, Object>();

    public synchronized Set<String> getKeys() {
        return attributes.keySet();
    }

    public synchronized void setValue(String key, Object value) {
        if (value != null) {
            checkSupportedTypes(value.getClass());
        }
        attributes.put(key, value);
    }

    public synchronized Object getValue(String key) {
        return attributes.get(key);
    }

    public synchronized Object getValue(String key, double timestamp) {
        TimestampValueSet valueSet = (TimestampValueSet) attributes.get(key);
        if (valueSet != null && timestampMap.hasTimestampIndex(timestamp)) {
            int index = timestampMap.getTimestampIndex(timestamp);
            return valueSet.get(index, null);
        }
        return null;
    }

    public synchronized void setValue(String key, Object value, double timestamp) {
        if (value == null) {
            throw new NullPointerException("The value can't be null for the key '" + key + "'");
        }
        checkSupportedDynamicTypes(value.getClass());

        AttributeUtils.getDynamicType(value.getClass());
        int index = timestampMap.getTimestampIndex(timestamp);

        TimestampValueSet valueSet = null;
        if (attributes.containsKey(key)) {
            valueSet = (TimestampValueSet) attributes.get(key);
        } else {
            try {
                valueSet = AttributeUtils.getDynamicType(value.getClass()).newInstance();
                attributes.put(key, valueSet);
            } catch (Exception ex) {
                throw new RuntimeException("The dynamic type can't be created", ex);
            }
        }
        if (!value.getClass().equals(valueSet.getTypeClass())) {
            throw new IllegalArgumentException("The value type " + value.getClass().getName() + " doesn't match with the expected type " + valueSet.getTypeClass().getName());
        }

        valueSet.put(index, value);
    }

    protected void setGraphAttributes(GraphAttributesImpl graphAttributes) {
        timestampMap.setTimestampMap(graphAttributes.timestampMap);
        attributes.putAll(graphAttributes.attributes);
    }

    private void checkSupportedTypes(Class type) {
        if (!AttributeUtils.isSupported(type)) {
            throw new IllegalArgumentException("Unknown type " + type.getName());
        }
    }

    private void checkSupportedDynamicTypes(Class type) {
        try {
            AttributeUtils.getDynamicType(type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported dynamic type " + type.getName());
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.attributes != null ? this.attributes.hashCode() : 0);
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
        final GraphAttributesImpl other = (GraphAttributesImpl) obj;
        if (!MapDeepEquals.mapDeepEquals(attributes, other.attributes)) {
            return false;
        }
        if (!timestampMap.equals(other.timestampMap)) {
            return false;
        }
        return true;
    }
}
