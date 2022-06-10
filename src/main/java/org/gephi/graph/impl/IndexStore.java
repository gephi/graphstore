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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;

public class IndexStore<T extends Element> {

    protected final ColumnStore<T> columnStore;
    protected final TableLockImpl lock;
    protected final IndexImpl<T> mainIndex;
    protected final Map<GraphView, IndexImpl<T>> viewIndexes;

    public IndexStore(ColumnStore<T> columnStore) {
        this.columnStore = columnStore;
        this.mainIndex = new IndexImpl<>(columnStore);
        this.viewIndexes = new Object2ObjectOpenHashMap<>();
        this.lock = columnStore.lock;
    }

    // Table locked
    protected void addColumn(ColumnImpl col) {
        mainIndex.addColumn(col);
        for (IndexImpl<T> index : viewIndexes.values()) {
            index.addColumn(col);
        }
    }

    // Table locked
    protected void removeColumn(ColumnImpl col) {
        mainIndex.removeColumn(col);
        for (IndexImpl<T> index : viewIndexes.values()) {
            index.removeColumn(col);
        }
    }

    protected boolean hasColumn(ColumnImpl col) {
        return mainIndex.hasColumn(col);
    }

    protected IndexImpl getIndex(Graph graph) {
        GraphView view = graph.getView();
        if (view.isMainView()) {
            return mainIndex;
        }
        synchronized (viewIndexes) {
            IndexImpl<T> viewIndex = viewIndexes.get(graph.getView());
            if (viewIndex == null) {
                viewIndex = createViewIndex(graph);
            }
            return viewIndex;
        }
    }

    protected IndexImpl createViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't create a view index for the main view");
        }
        lock();
        try {
            IndexImpl viewIndex = new IndexImpl<>(columnStore, graph);
            ColumnImpl[] columns = columnStore.toArray();
            viewIndex.addAllColumns(columns);
            viewIndexes.put(graph.getView(), viewIndex);

            indexView(graph);

            return viewIndex;
        } finally {
            unlock();
        }
    }

    protected void deleteViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't delete a view index for the main view");
        }
        lock();
        try {
            IndexImpl<T> index = viewIndexes.remove(graph.getView());
            if (index != null) {
                index.destroy();
            }
        } finally {
            unlock();
        }
    }

    public Object set(Column column, Object oldValue, Object value, T element) {
        value = mainIndex.set(column, oldValue, value, element);

        if (!viewIndexes.isEmpty()) {
            synchronized (viewIndexes) {
                for (Entry<GraphView, IndexImpl<T>> entry : viewIndexes.entrySet()) {
                    GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                    DirectedSubgraph graph = graphView.getDirectedGraph();
                    boolean inView = element instanceof Node ? graph.contains((Node) element)
                            : graph.contains((Edge) element);
                    if (inView) {
                        entry.getValue().set(column, oldValue, value, element);
                    }
                }
            }
        }

        return value;
    }

    public void clear(T element) {
        ElementImpl elementImpl = (ElementImpl) element;

        lock();
        try {
            final int length = columnStore.length;
            final ColumnImpl[] cols = columnStore.columns;
            for (int i = 0; i < length; i++) {
                Column c = cols[i];
                if (c != null && c.isIndexed()) {
                    Object value = elementImpl.getAttribute(c);
                    mainIndex.remove(c, value, element);
                    if (!viewIndexes.isEmpty()) {
                        synchronized (viewIndexes) {
                            for (Entry<GraphView, IndexImpl<T>> entry : viewIndexes.entrySet()) {
                                GraphViewImpl graphView = (GraphViewImpl) entry.getKey();
                                DirectedSubgraph graph = graphView.getDirectedGraph();
                                boolean inView = element instanceof Node ? graph.contains((Node) element)
                                        : graph.contains((Edge) element);
                                if (inView) {
                                    entry.getValue().remove(c, value, element);
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public void index(T element) {
        ElementImpl elementImpl = (ElementImpl) element;
        lock();
        try {

            final int length = columnStore.length;
            final ColumnImpl[] cols = columnStore.columns;
            for (int i = 0; i < length; i++) {
                Column c = cols[i];
                if (c != null && c.isIndexed()) {
                    Object value = elementImpl.getAttribute(c);
                    value = mainIndex.put(c, value, element);
                    elementImpl.setAttribute(c, value);
                }
            }
        } finally {
            unlock();
        }
    }

    public void indexView(Graph graph) {
        final IndexImpl viewIndex = viewIndexes.get(graph.getView());
        if (viewIndex != null) {
            graph.readLock();
            try {
                Iterator<T> iterator = null;
                if (columnStore.elementType.equals(Node.class)) {
                    iterator = (Iterator<T>) graph.getNodes().iterator();
                } else if (columnStore.elementType.equals(Edge.class)) {
                    iterator = (Iterator<T>) graph.getEdges().iterator();
                }

                if (iterator != null) {
                    while (iterator.hasNext()) {
                        ElementImpl element = (ElementImpl) iterator.next();

                        final ColumnImpl[] cols = columnStore.columns;
                        int length = columnStore.length;
                        for (int i = 0; i < length; i++) {
                            Column c = cols[i];
                            if (c != null && c.isIndexed()) {
                                Object value = element.getAttribute(c);
                                viewIndex.put(c, value, element);
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
        lock();
        try {
            IndexImpl<T> index = viewIndexes.get(view);
            if (index != null) {
                final int length = columnStore.length;
                final ColumnImpl[] cols = columnStore.columns;
                for (int i = 0; i < length; i++) {
                    Column c = cols[i];
                    if (c != null && c.isIndexed()) {
                        Object value = elementImpl.getAttribute(c);
                        index.put(c, value, element);
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public void clearInView(T element, GraphView view) {
        ElementImpl elementImpl = (ElementImpl) element;
        lock();
        try {
            IndexImpl<T> index = viewIndexes.get(view);
            if (index != null) {
                final int length = columnStore.length;
                final ColumnImpl[] cols = columnStore.columns;
                for (int i = 0; i < length; i++) {
                    Column c = cols[i];
                    if (c != null && c.isIndexed()) {
                        Object value = elementImpl.getAttribute(c);
                        index.remove(c, value, element);
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public void clear(GraphView view) {
        lock();
        try {
            IndexImpl<T> index = viewIndexes.get(view);
            if (index != null) {
                index.clear();
            }
        } finally {
            unlock();
        }
    }

    public void clear() {
        lock();
        try {
            mainIndex.clear();
            for (IndexImpl index : viewIndexes.values()) {
                index.clear();
            }
        } finally {
            unlock();
        }
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
