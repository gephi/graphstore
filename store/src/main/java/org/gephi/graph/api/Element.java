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
package org.gephi.graph.api;

import java.util.Set;
import org.gephi.attribute.api.Column;

/**
 *
 * @author mbastian
 */
public interface Element extends ElementProperties {

    public Object getId();

    public String getLabel();

    public Object getAttribute(String key);

    public Object getAttribute(Column column);

    public Object getAttribute(String key, double timestamp);

    public Object getAttribute(Column column, double timestamp);

    public Object getAttribute(String key, GraphView view);

    public Object getAttribute(Column column, GraphView view);

    public Object[] getAttributes();

    public Set<String> getAttributeKeys();

    public int getStoreId();

    public Object removeAttribute(String key);

    public Object removeAttribute(Column column);

    public void setLabel(String label);

    public void setAttribute(String key, Object value);

    public void setAttribute(Column column, Object value);

    public void setAttribute(String key, Object value, double timestamp);

    public void setAttribute(Column column, Object value, double timestamp);

    public boolean addTimestamp(double timestamp);

    public boolean removeTimestamp(double timestamp);
    
    public boolean hasTimestamp(double timestamp);

    public double[] getTimestamps();

    public void clearAttributes();
}
