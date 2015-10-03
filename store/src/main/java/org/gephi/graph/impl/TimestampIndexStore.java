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

import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2IntSortedMap;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeIndex;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.impl.utils.MapDeepEquals;

public class TimestampIndexStore<T extends Element> implements TimeIndexStore<Double, TimestampSet, TimestampMap> {

    //Const
    public static final int NULL_INDEX = -1;
    //Lock
    protected final GraphLock graphLock;
    //Element
    protected final Class<T> elementType;
    //Timestamp index managament
    protected final Double2IntSortedMap timestampSortedMap;
    protected final IntSortedSet garbageQueue;
    protected int[] countMap;
    protected int length;
    //Index
    protected final TimestampIndexImpl mainIndex;
    protected final Map<GraphView, TimestampIndexImpl> viewIndexes;

    public TimestampIndexStore(Class<T> type, GraphLock lock, boolean indexed) {
        elementType = type;
        graphLock = lock;

        garbageQueue = new IntRBTreeSet();
        timestampSortedMap = new Double2IntRBTreeMap();
        timestampSortedMap.defaultReturnValue(NULL_INDEX);
        countMap = new int[0];

        mainIndex = indexed ? new TimestampIndexImpl<T>(this, true) : null;
        viewIndexes = indexed ? new Object2ObjectOpenHashMap<GraphView, TimestampIndexImpl>() : null;
    }

    @Override
    public int add(Double timestamp) {
        checkDouble(timestamp);

        int id = timestampSortedMap.get(timestamp.doubleValue());
        if (id == NULL_INDEX) {
            if (!garbageQueue.isEmpty()) {
                id = garbageQueue.firstInt();
                garbageQueue.remove(id);
            } else {
                id = length++;
            }
            timestampSortedMap.put(timestamp.doubleValue(), id);
            ensureArraySize(id);
            countMap[id] = 1;
        } else {
            countMap[id]++;
        }

        return id;
    }

    @Override
    public int add(Double timestamp, Element element) {
        int timestampIndex = add(timestamp);

        if (mainIndex != null) {
            mainIndex.add(timestampIndex, element);

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    boolean node = element instanceof Node;
                    if (node ? graph.contains((Node) element) : graph.contains((Edge) element)) {
                        entry.getValue().add(timestampIndex, element);
                    }
                }

            }
        }

        return timestampIndex;
    }

    @Override
    public void add(TimestampMap timeMap) {
        for (double timeKey : timeMap.getTimestamps()) {
            add(timeKey);
        }
    }

    @Override
    public void add(TimestampSet timeSet) {
        for (double timeKey : timeSet.toPrimitiveArray()) {
            add(timeKey);
        }
    }

    @Override
    public int remove(Double timestamp) {
        checkDouble(timestamp);

        int id = timestampSortedMap.get(timestamp.doubleValue());
        if (id != NULL_INDEX) {
            if (--countMap[id] == 0) {
                garbageQueue.add(id);
                timestampSortedMap.remove(timestamp.doubleValue());
            }
        }
        return id;
    }

    @Override
    public int remove(Double timestamp, Element element) {
        int timestampIndex = remove(timestamp);
        checkTimestampIndex(timestampIndex);

        if (mainIndex != null) {
            mainIndex.remove(timestampIndex, element);

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    if (element instanceof Node) {
                        if (graph.contains((Node) element)) {
                            entry.getValue().remove(timestampIndex, element);
                        }
                    } else if (graph.contains((Edge) element)) {
                        entry.getValue().remove(timestampIndex, element);
                    }
                }
            }
        }

        return timestampIndex;
    }

    @Override
    public void remove(TimestampMap timeMap) {
        for (double timeKey : timeMap.getTimestamps()) {
            remove(timeKey);
        }
    }

    @Override
    public void remove(TimestampSet timeSet) {
        for (double timeKey : timeSet.toPrimitiveArray()) {
            remove(timeKey);
        }
    }

    @Override
    public boolean contains(Double timestamp) {
        checkDouble(timestamp);

        return timestampSortedMap.containsKey(timestamp.doubleValue());
    }

    @Override
    public void index(Element element) {
        TimestampSet timeSet = getTimeSet(element);

        if (timeSet != null) {
            add(timeSet);
        }

        for (Object val : element.getAttributes()) {
            if (val != null && val instanceof TimestampMap) {
                TimestampMap dynamicValue = (TimestampMap) val;
                add(dynamicValue);
            }
        }

        if (timeSet != null && mainIndex != null) {
            double[] ts = timeSet.toPrimitiveArray();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestampIndex = getTimestampIndex(ts[i]);
                mainIndex.add(timestampIndex, element);
            }
        }
    }

    @Override
    public void clear(Element element) {
        TimestampSet timeSet = getTimeSet(element);

        if (timeSet != null && mainIndex != null) {
            double[] ts = timeSet.toPrimitiveArray();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestampIndex = getTimestampIndex(ts[i]);
                mainIndex.remove(timestampIndex, element);
            }

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    boolean node = element instanceof Node;
                    if (node ? graph.contains((Node) element) : graph.contains((Edge) element)) {
                        for (int i = 0; i < tsLength; i++) {
                            int timestampIndex = getTimestampIndex(ts[i]);
                            entry.getValue().remove(timestampIndex, element);
                        }
                    }
                }
            }
        }

        if (timeSet != null) {
            remove(timeSet);
        }

        for (Object val : element.getAttributes()) {
            if (val != null && val instanceof TimestampMap) {
                TimestampMap dynamicValue = (TimestampMap) val;
                remove(dynamicValue);
            }
        }
    }

    @Override
    public void clear() {
        timestampSortedMap.clear();
        garbageQueue.clear();
        countMap = new int[0];
        length = 0;

        if (mainIndex != null) {
            mainIndex.clear();

            if (!viewIndexes.isEmpty()) {
                for (TimestampIndexImpl index : viewIndexes.values()) {
                    index.clear();
                }
            }
        }
    }

    @Override
    public int size() {
        return timestampSortedMap.size();
    }

    protected int getTimestampIndex(double timestamp) {
        return timestampSortedMap.get(timestamp);
    }

    @Override
    public TimeIndex getIndex(Graph graph) {
        GraphView view = graph.getView();
        if (view.isMainView()) {
            return mainIndex;
        }
        TimestampIndexImpl viewIndex = viewIndexes.get(graph.getView());
        if (viewIndex == null) {
            // TODO Make the auto-creation optional?
            viewIndex = createViewIndex(graph);
            viewIndexes.put(graph.getView(), viewIndex);
        }
        return viewIndex;
    }

    protected TimestampIndexImpl createViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't create a view index for the main view");
        }

        TimestampIndexImpl viewIndex = new TimestampIndexImpl(this, false);
        // TODO: Check view doesn't exist already
        viewIndexes.put(graph.getView(), viewIndex);

        indexView(graph);

        return viewIndex;
    }

    @Override
    public void deleteViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't delete a view index for the main view");
        }
        TimestampIndexImpl index = viewIndexes.remove(graph.getView());
        if (index != null) {
            index.clear();
        }
    }

    @Override
    public void indexView(Graph graph) {
        TimestampIndexImpl viewIndex = viewIndexes.get(graph.getView());
        if (viewIndex != null) {
            graph.readLock();
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
                        TimestampSet set = getTimeSet(element);
                        if (set != null) {
                            double[] ts = set.toPrimitiveArray();
                            int tsLength = ts.length;
                            for (int i = 0; i < tsLength; i++) {
                                int timestamp = getTimestampIndex(ts[i]);
                                viewIndex.add(timestamp, element);
                            }
                        }
                    }
                }
            } finally {
                graph.readUnlock();
            }
        }
    }

    @Override
    public void indexInView(Element element, GraphView view) {
        TimestampIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            TimestampSet set = getTimeSet(element);
            if (set != null) {
                double[] ts = set.toPrimitiveArray();
                int tsLength = ts.length;
                for (int i = 0; i < tsLength; i++) {
                    int timestampIndex = getTimestampIndex(ts[i]);
                    viewIndex.add(timestampIndex, element);
                }
            }
        }
    }

    @Override
    public void clearInView(Element element, GraphView view) {
        ElementImpl elementImpl = (ElementImpl) element;
        TimestampIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            TimestampSet set = getTimeSet(element);
            if (set != null) {
                double[] ts = set.toPrimitiveArray();
                int tsLength = ts.length;
                for (int i = 0; i < tsLength; i++) {
                    int timestampIndex = getTimestampIndex(ts[i]);
                    viewIndex.remove(timestampIndex, elementImpl);
                }
            }
        }
    }

    @Override
    public void clear(GraphView view) {
        TimestampIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            viewIndex.clear();
        }
    }

    private TimestampSet getTimeSet(Element element) {
        Object[] attributes = element.getAttributes();
        if (GraphStoreConfiguration.ENABLE_ELEMENT_TIME_SET && GraphStoreConfiguration.ELEMENT_TIMESET_INDEX < attributes.length) {
            return (TimestampSet) attributes[GraphStoreConfiguration.ELEMENT_TIMESET_INDEX];
        }
        return null;
    }

    private void checkTimestampIndex(int timestampIndex) {
        if (timestampIndex == NULL_INDEX) {
            throw new IllegalArgumentException("Unknown timestamp index");
        }
    }

    protected void ensureArraySize(int index) {
        if (index >= countMap.length) {
            int newSize = Math.min(Math.max(index + 1, (int) (index * GraphStoreConfiguration.TIMESTAMP_STORE_GROWING_FACTOR)), Integer.MAX_VALUE);
            int[] newArray = new int[newSize];
            System.arraycopy(countMap, 0, newArray, 0, countMap.length);
            countMap = newArray;
        }
    }

    void checkDouble(double timestamp) {
        if (Double.isInfinite(timestamp) || Double.isNaN(timestamp)) {
            throw new IllegalArgumentException("Timestamp can't be NaN or infinity");
        }
    }

    @Override
    public int deepHashCode() {
        int hash = 3;
        hash = 29 * hash + elementType.hashCode();
        for (Double2IntMap.Entry entry : timestampSortedMap.double2IntEntrySet()) {
            hash = 29 * hash + entry.getKey().hashCode();
            hash = 29 * hash + entry.getValue().hashCode();
            hash = 29 * hash + countMap[entry.getValue()];
        }
        return hash;
    }

    @Override
    public boolean deepEquals(TimeIndexStore obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(getClass())) {
            return false;
        }
        TimestampIndexStore other = (TimestampIndexStore) obj;
        if (!other.elementType.equals(elementType)) {
            return false;
        }
        if (!MapDeepEquals.mapDeepEquals(timestampSortedMap, other.timestampSortedMap)) {
            return false;
        }
        int[] otherCountMap = other.countMap;
        for (Integer k : timestampSortedMap.values()) {
            if (otherCountMap[k] != countMap[k]) {
                return false;
            }
        }
        return true;
    }
}
