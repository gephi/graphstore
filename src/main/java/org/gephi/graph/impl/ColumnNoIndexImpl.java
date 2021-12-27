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

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

public class ColumnNoIndexImpl<K, T extends Element> implements ColumnIndexImpl<K, T> {

    // Data
    protected final ColumnImpl column;
    // Stores
    protected final Class<T> elementClass;
    // Graph
    protected final Graph graph;

    protected ColumnNoIndexImpl(ColumnImpl column, Graph graph, Class<T> elementClass) {
        this.column = column;
        this.elementClass = elementClass;
        this.graph = graph;
    }

    private Iterator<T> getElementIterator() {
        if (elementClass.equals(Node.class)) {
            return (Iterator<T>) graph.getNodes().iterator();
        } else if (elementClass.equals(Edge.class)) {
            return (Iterator<T>) graph.getEdges().iterator();
        }
        return null;
    }

    @Override
    public int count(K value) {
        Iterator<T> elementIterator = getElementIterator();
        int count = 0;
        if (elementIterator != null) {
            while (elementIterator.hasNext()) {
                ElementImpl element = (ElementImpl) elementIterator.next();
                K obj = (K) element.getAttribute(column);
                if (value == null && obj == null) {
                    count++;
                } else if (value != null && value.equals(obj)) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public Iterable<T> get(K value) {
        return new ElementWithValueIterable(getElementIterator(), value);
    }

    @Override
    public Collection<K> values() {
        Iterator<T> elementIterator = getElementIterator();
        Set<K> set = new ObjectOpenHashSet<>();
        if (elementIterator != null) {
            while (elementIterator.hasNext()) {
                ElementImpl element = (ElementImpl) elementIterator.next();
                K obj = (K) element.getAttribute(column);
                set.add(obj);
            }
        }
        return set;
    }

    @Override
    public int countValues() {
        return values().size();
    }

    @Override
    public int countElements() {
        if (elementClass.equals(Node.class)) {
            return graph.getNodeCount();
        } else if (elementClass.equals(Edge.class)) {
            return graph.getEdgeCount();
        }
        return 0;
    }

    @Override
    public boolean isSortable() {
        return Number.class.isAssignableFrom(column.getTypeClass());
    }

    @Override
    public Number getMinValue() {
        if (!isSortable()) {
            throw new UnsupportedOperationException("Only supported for sortable columns");
        }
        Number min = null;
        Iterator<T> elementIterator = getElementIterator();
        if (elementIterator != null) {
            double minN = Double.POSITIVE_INFINITY;
            while (elementIterator.hasNext()) {
                ElementImpl element = (ElementImpl) elementIterator.next();
                Number num = (Number) element.getAttribute(column);
                if (min == null || (num != null && num.doubleValue() < minN)) {
                    if (num != null) {
                        minN = num.doubleValue();
                    }
                    min = num;
                }
            }
        }
        return min;
    }

    @Override
    public Number getMaxValue() {
        if (!isSortable()) {
            throw new UnsupportedOperationException("Only supported for sortable columns");
        }
        Number max = null;
        Iterator<T> elementIterator = getElementIterator();
        if (elementIterator != null) {
            double maxN = Double.NEGATIVE_INFINITY;

            while (elementIterator.hasNext()) {
                ElementImpl element = (ElementImpl) elementIterator.next();
                Number num = (Number) element.getAttribute(column);
                if (max == null || (num != null && num.doubleValue() > maxN)) {
                    if (num != null) {
                        maxN = num.doubleValue();
                    }
                    max = num;
                }
            }
        }
        return max;
    }

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public Iterator<Map.Entry<K, ? extends Set<T>>> iterator() {
        // TODO
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void clear() {
        // Nothing to clear
    }

    @Override
    public void destroy() {
        // Nothing to destroy
    }

    @Override
    public K putValue(T element, K value) {
        return value;
    }

    @Override
    public K replaceValue(T element, K oldValue, K newValue) {
        return newValue;
    }

    @Override
    public void removeValue(T element, K value) {
        // Nothing to remove
    }

    private class ElementWithValueIterable implements Iterable<T> {

        private final Iterator<T> ite;
        private final K value;

        public ElementWithValueIterable(Iterator<T> ite, K value) {
            this.ite = ite;
            this.value = value;
        }

        @Override
        public Iterator<T> iterator() {
            return new ElementWithValueIterator(ite, value);
        }
    }

    private class ElementWithValueIterator implements Iterator<T> {

        private final Iterator<T> itr;
        private final K value;
        private T pointer;

        public ElementWithValueIterator(Iterator<T> itr, K value) {
            this.itr = itr;
            this.value = value;
        }

        @Override
        public boolean hasNext() {
            while (pointer == null && itr.hasNext()) {
                T element = itr.next();
                K val = (K) element.getAttribute(column);
                if ((value == null && val == null) || (val != null && val.equals(value))) {
                    pointer = element;
                }
            }
            return pointer != null;
        }

        @Override
        public T next() {
            T res = pointer;
            pointer = null;
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
