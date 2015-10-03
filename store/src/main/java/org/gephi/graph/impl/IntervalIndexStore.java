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
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeIndex;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;

public class IntervalIndexStore<T extends Element> implements TimeIndexStore<Interval, IntervalSet, IntervalMap> {

    //Lock
    protected final GraphLock graphLock;
    //Element
    protected final Class<T> elementType;

    public IntervalIndexStore(Class<T> type, GraphLock lock, boolean indexed) {
        elementType = type;
        graphLock = lock;
    }

    @Override
    public int add(Interval interval) {
        //TODO
        return 0;
    }

    @Override
    public int add(Interval interval, Element element) {
        //TODO
        return 0;
    }

    @Override
    public void add(IntervalSet timeSet) {
        //TODO
    }

    @Override
    public void add(IntervalMap timeMap) {
        //TODO
    }

    @Override
    public int remove(Interval interval) {
        //TODO
        return 0;
    }

    @Override
    public int remove(Interval interval, Element element) {
        //TODO
        return 0;
    }

    @Override
    public void remove(IntervalMap timeMap) {
        //TODO
    }

    @Override
    public void remove(IntervalSet timeSet) {
        //TODO
    }

    @Override
    public void index(Element element) {
        //TODO
    }

    @Override
    public void clear(Element element) {
        //TODO
    }

    @Override
    public boolean contains(Interval interval) {
        //TODO
        return false;
    }

    @Override
    public void clear() {
        //TODO
    }

    @Override
    public int size() {
        //TODO
        return 0;
    }

    @Override
    public TimeIndex getIndex(Graph graph) {
        //TODO
        return null;
    }

    @Override
    public void indexInView(Element element, GraphView view) {
        //TODO
    }

    @Override
    public void clearInView(Element element, GraphView view) {
        //TODO
    }

    @Override
    public void clear(GraphView view) {
        //TODO
    }

    @Override
    public void indexView(Graph graph) {
        //TODO
    }

    @Override
    public void deleteViewIndex(Graph graph) {
        //TODO
    }

    @Override
    public boolean deepEquals(TimeIndexStore obj) {
        //TODO
        return false;
    }

    @Override
    public int deepHashCode() {
        //TODO
        return 0;
    }
}
