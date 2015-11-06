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

import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.TimeIndex;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;

public interface TimeIndexStore<K, S extends TimeSet, M extends TimeMap> {

    public int add(K timeObject);

    public int add(K timeObject, Element element);

    public void add(S timeSet);

    public void add(M timeMap);

    public int remove(K timeObject);

    public int remove(K timeObject, Element element);

    public void remove(M timeMap);

    public void remove(S timeSet);

    public void index(Element element);

    public void clear(Element element);

    public boolean contains(K timeObject);

    public void clear();

    public int size();

    public TimeIndex getIndex(Graph graph);

    public void indexInView(Element element, GraphView view);

    public void clearInView(Element element, GraphView view);

    public void clear(GraphView view);

    public void indexView(Graph graph);

    public void deleteViewIndex(Graph graph);

    public boolean hasIndex();

    public boolean deepEquals(TimeIndexStore obj);

    public int deepHashCode();
}
