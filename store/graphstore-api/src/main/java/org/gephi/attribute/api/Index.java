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
package org.gephi.attribute.api;

import java.util.Collection;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public interface Index<T extends Element> {

    public int count(Column column, Object value);

    public Iterable<T> get(Column column, Object value);

    public Collection values(Column column);

    public int countValues(Column column);

    public int countElements(Column column);

    public Number getMinValue(Column column);

    public Number getMaxValue(Column column);

    public Class<T> getIndexClass();

    public String getIndexName();
}
