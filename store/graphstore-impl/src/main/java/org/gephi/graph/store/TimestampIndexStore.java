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
import java.util.Map;
import java.util.Map.Entry;
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

    //Timestamp
    protected final TimestampStore timestampStore;
    protected final TimestampMap timestampMap;
    //Index
    protected final TimestampIndexImpl mainIndex;
    protected final Map<GraphView, TimestampIndexImpl> viewIndexes;

    public TimestampIndexStore(TimestampStore store, TimestampMap map) {
        timestampStore = store;
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
        return viewIndex;
    }

    public TimestampIndexImpl createViewIndex(Graph graph, boolean indexNode, boolean indexEdge) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't create a view index for the main view");
        }
        TimestampIndexImpl viewIndex = new TimestampIndexImpl(this, false);
        viewIndexes.put(graph.getView(), viewIndex);

        if (indexNode) {
            for (Node node : graph.getNodes()) {
                index((NodeImpl) node);
            }
        }
        if (indexEdge) {
            for (Edge edge : graph.getEdges()) {
                index((EdgeImpl) edge);
            }
        }

        return viewIndex;
    }

    public void deleteViewIndex(Graph graph) {
        if (GraphStoreConfiguration.ENABLE_INDEX_TIMESTAMP) {
            if (graph.getView().isMainView()) {
                throw new IllegalArgumentException("Can't delete a view index for the main view");
            }
            TimestampIndexImpl index = viewIndexes.remove(graph.getView());
            if (index != null) {
                index.clear();
            }
        }
    }

    public void clear() {
        if (GraphStoreConfiguration.ENABLE_INDEX_TIMESTAMP) {
            mainIndex.clear();

            if (!viewIndexes.isEmpty()) {
                for (TimestampIndexImpl index : viewIndexes.values()) {
                    index.clear();
                }
            }
        }
    }

    protected void index(ElementImpl element) {
        if (GraphStoreConfiguration.ENABLE_INDEX_TIMESTAMP) {
            TimestampSet set = element.getTimestampSet();
            if (set != null) {
                int[] ts = set.getTimestamps();
                int tsLength = ts.length;
                for (int i = 0; i < tsLength; i++) {
                    int timestamp = ts[i];
                    mainIndex.add(timestamp, element);
                }

                if (!viewIndexes.isEmpty()) {
                    for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                        GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                        DirectedSubgraph graph = graphView.getDirectedGraph();
                        boolean node = element instanceof Node;
                        if (node ? graph.contains((Node) element) : graph.contains((Edge) element)) {
                            for (int i = 0; i < tsLength; i++) {
                                int timestamp = ts[i];
                                entry.getValue().add(timestamp, element);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void clear(ElementImpl element) {
        if (GraphStoreConfiguration.ENABLE_INDEX_TIMESTAMP) {
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
    }

    public int add(double timestamp, ElementImpl element) {
        int timestampIndex = timestampMap.getTimestampIndex(timestamp);
        if (GraphStoreConfiguration.ENABLE_INDEX_TIMESTAMP) {
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

    public int remove(double timestamp, ElementImpl element) {
        int timestampIndex = timestampMap.getTimestampIndex(timestamp);
        if (GraphStoreConfiguration.ENABLE_INDEX_TIMESTAMP) {
            mainIndex.remove(timestampIndex, element);

            if (!viewIndexes.isEmpty()) {
                for (Entry<GraphView, TimestampIndexImpl> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    if (element instanceof Node) {
                        if (graph.contains((Node) element)) {
                            entry.getValue().remove(timestampIndex, element);
                        }
                    } else {
                        if (graph.contains((Edge) element)) {
                            entry.getValue().remove(timestampIndex, element);
                        }
                    }
                }
            }

            if (mainIndex.timestamps[timestampIndex] == null) {
                timestampMap.removeTimestamp(timestamp);
            }
        }

        return timestampIndex;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.timestampMap != null ? this.timestampMap.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimestampIndexStore<T> other = (TimestampIndexStore<T>) obj;
        if (this.timestampMap != other.timestampMap && (this.timestampMap == null || !this.timestampMap.equals(other.timestampMap))) {
            return false;
        }
        return true;
    }
}
