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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.time.TimestampSet;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;

/**
 *
 * @author mbastian
 */
public class TimestampIndexStore<T extends Element> {

    //Element
    protected final Class<T> elementType;
    //Timestamp
    protected final TimestampStore timestampStore;
    protected final TimestampMap timestampMap;
    //Index
    protected final TimestampIndexImpl mainIndex;
    protected final Map<GraphView, TimestampIndexImpl> viewIndexes;

    public TimestampIndexStore(TimestampStore store, Class<T> type, TimestampMap map) {
        timestampStore = store;
        elementType = type;
        timestampMap = map;
        mainIndex = new TimestampIndexImpl<T>(this, true);
        viewIndexes = new Object2ObjectOpenHashMap<GraphView, TimestampIndexImpl>();
    }

    public TimestampIndexImpl getIndex(Graph graph) {
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

    public TimestampIndexImpl createViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't create a view index for the main view");
        }

        TimestampIndexImpl viewIndex = new TimestampIndexImpl(this, false);
        // TODO: Check view doesn't exist already
        viewIndexes.put(graph.getView(), viewIndex);

        indexView(graph);

        return viewIndex;
    }

    public void deleteViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't delete a view index for the main view");
        }
        TimestampIndexImpl index = viewIndexes.remove(graph.getView());
        if (index != null) {
            index.clear();
        }

    }

    public void clear() {
        mainIndex.clear();

        if (!viewIndexes.isEmpty()) {
            for (TimestampIndexImpl index : viewIndexes.values()) {
                index.clear();
            }
        }

    }

    protected void index(ElementImpl element) {
        TimestampSet set = element.getTimestampSet();
        if (set != null) {
            int[] ts = set.getTimestamps();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestamp = ts[i];
                mainIndex.add(timestamp, element);
            }
        }
    }

    protected void clear(ElementImpl element) {
        TimestampSet set = element.getTimestampSet();
        if (set != null) {
            int[] ts = set.getTimestamps();
            int tsLength = ts.length;
            for (int i = 0; i < tsLength; i++) {
                int timestamp = ts[i];
                mainIndex.remove(timestamp, element);

                if (mainIndex.timestamps[i] == null) {
                    timestampMap.removeTimestamp(timestampMap.indexMap[i]);
                }
            }

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    boolean node = element instanceof Node;
                    if (node ? graph.contains((Node) element) : graph.contains((Edge) element)) {
                        for (int i = 0; i < tsLength; i++) {
                            int timestamp = ts[i];
                            entry.getValue().remove(timestamp, element);
                        }
                    }
                }
            }
        }
    }

    public int add(double timestamp, ElementImpl element) {
        int timestampIndex = timestampMap.getTimestampIndex(timestamp);

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

        return timestampIndex;
    }

    public int remove(double timestamp, ElementImpl element) {
        int timestampIndex = timestampMap.getTimestampIndex(timestamp);
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

        if (mainIndex.timestamps[timestampIndex] == null) {
            timestampMap.removeTimestamp(timestamp);
        }

        return timestampIndex;
    }

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
                        ElementImpl element = (ElementImpl) iterator.next();
                        TimestampSet set = element.getTimestampSet();
                        if (set != null) {
                            int[] ts = set.getTimestamps();
                            int tsLength = ts.length;
                            for (int i = 0; i < tsLength; i++) {
                                int timestamp = ts[i];
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

    public void indexInView(T element, GraphView view) {
        ElementImpl elementImpl = (ElementImpl) element;
        TimestampIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            TimestampSet set = elementImpl.getTimestampSet();
            if (set != null) {
                int[] ts = set.getTimestamps();
                int tsLength = ts.length;
                for (int i = 0; i < tsLength; i++) {
                    int timestamp = ts[i];
                    viewIndex.add(timestamp, elementImpl);
                }
            }
        }
    }

    public void clearInView(T element, GraphView view) {
        ElementImpl elementImpl = (ElementImpl) element;
        TimestampIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            TimestampSet set = elementImpl.getTimestampSet();
            if (set != null) {
                int[] ts = set.getTimestamps();
                int tsLength = ts.length;
                for (int i = 0; i < tsLength; i++) {
                    int timestamp = ts[i];
                    viewIndex.remove(timestamp, elementImpl);
                }
            }
        }
    }

    public void clear(GraphView view) {
        TimestampIndexImpl viewIndex = viewIndexes.get(view);
        if (viewIndex != null) {
            viewIndex.clear();
        }
    }
}
