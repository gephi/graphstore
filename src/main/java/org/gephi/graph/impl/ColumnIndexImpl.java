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

import org.gephi.graph.api.ColumnIndex;
import org.gephi.graph.api.Element;

public interface ColumnIndexImpl<K, T extends Element> extends ColumnIndex<K, T> {

    void destroy();

    void clear();

    K putValue(T element, K value);

    void removeValue(T element, K value);

    K replaceValue(T element, K oldValue, K newValue);
}
