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

import org.gephi.attribute.time.Estimator;

/**
 *
 * @author mbastian
 */
public interface Table extends Iterable<Column> {

    public Column addColumn(String id, Class type);

    public Column addColumn(String id, Class type, Origin origin);
    
    public Column addColumn(String id, String title, Class type, Object defaultValue);

    public Column addColumn(String id, String title, Class type, Origin origin, Object defaultValue, boolean indexed);

    public Column getColumn(int index);

    public Column getColumn(String id);

    public Column[] getColumns();

    public Estimator getEstimator(Column column);

    public void setEstimator(Column column, Estimator estimator);

    public boolean hasColumn(String id);

    public void removeColumn(Column column);

    public void removeColumn(String id);

    public int countColumns();

    public Class getElementClass();

    public TableObserver getTableObserver();
}
