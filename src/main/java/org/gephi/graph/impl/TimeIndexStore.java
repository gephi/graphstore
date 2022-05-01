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

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeIndex;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.graph.impl.utils.MapDeepEquals;

public abstract class TimeIndexStore<T extends Element, K, S extends TimeSet<K>, M extends TimeMap<K, ?>> {

    // Lock
    protected final TableLockImpl lock;
    // Element
    protected final Class<T> elementType;
    // Timestamp index management
    protected final Map<K, Integer> timeSortedMap;
    protected final IntSortedSet garbageQueue;
    protected int[] countMap;
    protected int length;
    // Index
    protected TimeIndexImpl mainIndex;
    protected final Map<GraphView, TimeIndexImpl> viewIndexes;

    protected TimeIndexStore(Class<T> type, TableLockImpl lock, boolean indexed, Map<K, Integer> sortedMap) {
        this.elementType = type;
        this.lock = lock;

        garbageQueue = new IntRBTreeSet();
        // Subclass
        timeSortedMap = sortedMap;
        countMap = new int[0];

        viewIndexes = indexed ? new Object2ObjectOpenHashMap<>() : null;
    }

    protected abstract void checkK(K k);

    protected abstract double getLow(K k);

    protected abstract TimeIndexImpl createIndex(boolean main);

    protected Integer add(K k) {
        checkK(k);

        lock();
        try {
            Integer id = timeSortedMap.get(k);
            if (id == null) {
                if (!garbageQueue.isEmpty()) {
                    id = garbageQueue.firstInt();
                    garbageQueue.remove(id);
                } else {
                    id = length++;
                }
                timeSortedMap.put(k, id);
                ensureArraySize(id);
                countMap[id] = 1;
            } else {
                countMap[id]++;
            }

            return id;
        } finally {
            unlock();
        }
    }

    public void add(K k, Element element) {
        lock();
        try {
            int timeIndex = add(k);

            if (mainIndex != null) {
                mainIndex.add(timeIndex, element);

                if (!viewIndexes.isEmpty()) {
                    for (Entry<GraphView, TimeIndexImpl> entry : viewIndexes.entrySet()) {
                        GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                        DirectedSubgraph graph = graphView.getDirectedGraph();
                        boolean node = element instanceof Node;
                        if (node ? graph.contains((Node) element) : graph.contains((Edge) element)) {
                            entry.getValue().add(timeIndex, element);
                        }
                    }

                }
            }
        } finally {
            unlock();
        }
    }

    public void add(TimeMap<K, ?> timeMap) {
        for (K timeKey : timeMap.toKeysArray()) {
            add(timeKey);
        }
    }

    public void add(TimeSet<K> timeSet, Element element) {
        for (K timeKey : timeSet.toArray()) {
            add(timeKey, element);
        }
    }

    protected Integer remove(K k) {
        lock();
        try {
            checkK(k);

            Integer id = timeSortedMap.get(k);
            if (id != null) {
                if (--countMap[id] == 0) {
                    garbageQueue.add(id);
                    timeSortedMap.remove(k);
                }
            }
            return id;
        } finally {
            unlock();
        }
    }

    public void remove(K k, Element element) {
        lock();
        try {
            Integer timeIndex = remove(k);
            if (timeIndex == null) {
                return;
            }

            if (mainIndex != null) {
                mainIndex.remove(timeIndex, element);

                if (!viewIndexes.isEmpty()) {
                    for (Entry<GraphView, TimeIndexImpl> entry : viewIndexes.entrySet()) {
                        GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                        DirectedSubgraph graph = graphView.getDirectedGraph();
                        if (element instanceof Node) {
                            if (graph.contains((Node) element)) {
                                entry.getValue().remove(timeIndex, element);
                            }
                        } else if (graph.contains((Edge) element)) {
                            entry.getValue().remove(timeIndex, element);
                        }
                    }
                }
            }

        } finally {
            unlock();
        }
    }

    public void remove(M timeMap) {
        for (K timeKey : timeMap.toKeysArray()) {
            remove(timeKey);
        }
    }

    public void remove(S timeSet, Element element) {
        for (K timeKey : timeSet.toArray()) {
            remove(timeKey, element);
        }
    }

    public boolean contains(K k) {
        checkK(k);

        lock();
        try {
            return timeSortedMap.containsKey(k);
        } finally {
            unlock();
        }
    }

    public void index(Element element) {
        lock();
        try {
            S timeSet = getTimeSet(element);

            if (timeSet != null) {
                add(timeSet, element);
            }

            synchronized (element) {
                for (Object val : element.getAttributes()) {
                    if (val instanceof TimeMap) {
                        TimeMap dynamicValue = (TimeMap) val;
                        add(dynamicValue);
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public void clear(Element element) {
        lock();
        try {
            S timeSet = getTimeSet(element);

            if (timeSet != null) {
                remove(timeSet, element);
            }

            synchronized (element) {
                for (Object val : element.getAttributes()) {
                    if (val instanceof TimeMap) {
                        TimeMap dynamicValue = (TimeMap) val;
                        remove((M) dynamicValue);
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public void clear() {
        lock();
        try {
            timeSortedMap.clear();
            garbageQueue.clear();
            countMap = new int[0];
            length = 0;

            if (mainIndex != null) {
                mainIndex.clear();

                if (!viewIndexes.isEmpty()) {
                    for (TimeIndexImpl index : viewIndexes.values()) {
                        index.clear();
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public int size() {
        return timeSortedMap.size();
    }

    public TimeIndex getIndex(Graph graph) {
        GraphView view = graph.getView();
        if (view.isMainView()) {
            return mainIndex;
        }
        lock();
        try {
            TimeIndexImpl viewIndex = viewIndexes.get(graph.getView());
            if (viewIndex == null) {
                // TODO Make the auto-creation optional?
                viewIndex = createViewIndex(graph);
            }
            return viewIndex;
        } finally {
            unlock();
        }
    }

    protected TimeIndexImpl createViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't create a view index for the main view");
        }

        TimeIndexImpl viewIndex = createIndex(false);
        // TODO: Check view doesn't exist already
        viewIndexes.put(graph.getView(), viewIndex);

        indexView(graph);

        return viewIndex;
    }

    public void deleteViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't delete a view index for the main view");
        }
        lock();
        try {
            TimeIndexImpl index = viewIndexes.remove(graph.getView());
            if (index != null) {
                index.clear();
            }
        } finally {
            unlock();
        }
    }

    public void indexView(Graph graph) {
        TimeIndexImpl viewIndex = viewIndexes.get(graph.getView());
        if (viewIndex != null) {
            graph.readLock();
            lock();
            try {
                Iterator<T> iterator = null;

                if (elementType.equals(Node.class)) {
                    iterator = (Iterator<T>) graph.getNodes().iterator();
                } else if (elementType.equals(Edge.class)) {
                    iterator = (Iterator<T>) graph.getEdges().iterator();
                }

                if (iterator != null) {
                    while (iterator.hasNext()) {
                        Element element = iterator.next();
                        S set = getTimeSet(element);
                        if (set != null) {
                            K[] ts = set.toArray();
                            int tsLength = ts.length;
                            for (int i = 0; i < tsLength; i++) {
                                int timestamp = timeSortedMap.get(ts[i]);
                                viewIndex.add(timestamp, element);
                            }
                        }
                    }
                }
            } finally {
                graph.readUnlock();
                unlock();
            }
        }
    }

    public void indexInView(T element, GraphView view) {
        TimeIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            lock();
            try {
                S set = getTimeSet(element);
                if (set != null) {
                    K[] ts = set.toArray();
                    int tsLength = ts.length;
                    for (int i = 0; i < tsLength; i++) {
                        int timestampIndex = timeSortedMap.get(ts[i]);
                        viewIndex.add(timestampIndex, element);
                    }
                }
            } finally {
                unlock();
            }
        }
    }

    public void clearInView(T element, GraphView view) {
        TimeIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            lock();
            try {
                S set = getTimeSet(element);
                if (set != null) {
                    K[] ts = set.toArray();
                    int tsLength = ts.length;
                    for (int i = 0; i < tsLength; i++) {
                        int timestampIndex = timeSortedMap.get(ts[i]);
                        viewIndex.remove(timestampIndex, element);
                    }
                }
            } finally {
                unlock();
            }
        }
    }

    public void clear(GraphView view) {
        TimeIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            viewIndex.clear();
        }
    }

    public boolean hasIndex() {
        return mainIndex != null;
    }

    private S getTimeSet(Element element) {
        Object[] attributes = element.getAttributes();
        if (GraphStoreConfiguration.ENABLE_ELEMENT_TIME_SET && GraphStoreConfiguration.ELEMENT_TIMESET_INDEX < attributes.length) {
            return (S) attributes[GraphStoreConfiguration.ELEMENT_TIMESET_INDEX];
        }
        return null;
    }

    private void checkTimeIndex(Integer timeIndex) {
        if (timeIndex == null) {
            throw new IllegalArgumentException("Unknown time index");
        }
    }

    protected void ensureArraySize(int index) {
        if (index >= countMap.length) {
            int newSize = Math.min(Math
                    .max(index + 1, (int) (index * GraphStoreConfiguration.TIMESTAMP_STORE_GROWING_FACTOR)), Integer.MAX_VALUE);
            int[] newArray = new int[newSize];
            System.arraycopy(countMap, 0, newArray, 0, countMap.length);
            countMap = newArray;
        }
    }

    public int deepHashCode() {
        int hash = 3;
        hash = 29 * hash + elementType.hashCode();
        for (Map.Entry<K, Integer> entry : timeSortedMap.entrySet()) {
            hash = 29 * hash + entry.getKey().hashCode();
            hash = 29 * hash + entry.getValue().hashCode();
            hash = 29 * hash + countMap[entry.getValue()];
        }
        return hash;
    }

    public boolean deepEquals(TimeIndexStore obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(getClass())) {
            return false;
        }
        TimeIndexStore other = (TimeIndexStore) obj;
        if (!other.elementType.equals(elementType)) {
            return false;
        }
        if (!MapDeepEquals.mapDeepEquals(timeSortedMap, other.timeSortedMap)) {
            return false;
        }
        int[] otherCountMap = other.countMap;
        for (Integer k : timeSortedMap.values()) {
            if (otherCountMap[k] != countMap[k]) {
                return false;
            }
        }
        return true;
    }

    private void lock() {
        if (lock != null) {
            lock.lock();
        }
    }

    private void unlock() {
        if (lock != null) {
            lock.unlock();
        }
    }
}
