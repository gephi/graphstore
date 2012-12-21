package org.gephi.graph.store;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.gephi.attribute.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;

/**
 *
 * @author mbastian
 */
public class IndexStore<T extends Element> {

    protected final ColumnStore<T> propertyStore;
    protected final IndexImpl<T> mainIndex;
    protected final Map<Graph, IndexImpl<T>> viewIndexes;

    public IndexStore(ColumnStore<T> propertyStore) {
        this.propertyStore = propertyStore;
        this.mainIndex = new IndexImpl<T>(propertyStore);
        this.viewIndexes = new Object2ObjectOpenHashMap<Graph, IndexImpl<T>>();
    }

    protected synchronized void addColumn(ColumnImpl col) {
        mainIndex.addColumn(col);
        for (IndexImpl<T> index : viewIndexes.values()) {
            index.addColumn(col);
        }
    }

    protected synchronized void removeColumn(ColumnImpl col) {
        mainIndex.removeColumn(col);
        for (IndexImpl<T> index : viewIndexes.values()) {
            index.destroy();
            index.removeColumn(col);
        }
    }

    protected synchronized IndexImpl getIndex(Graph graph) {
        GraphView view = graph.getView();
        if (view.isMainView()) {
            return mainIndex;
        }
        IndexImpl<T> viewIndex = viewIndexes.get(graph);
        if (viewIndex == null) {
            viewIndex = createViewIndex(graph);
        }
        return viewIndex;
    }

    protected synchronized IndexImpl createViewIndex(Graph graph) {
        IndexImpl viewIndex = new IndexImpl<T>(propertyStore);
        viewIndex.addAllColumns(propertyStore.columns);
        viewIndexes.put(graph, viewIndex);

        if (propertyStore.elementType.equals(Node.class)) {
            for (Node n : graph.getNodes()) {
                NodeImpl node = (NodeImpl) n;

                for (Column c : propertyStore.columns) {
                    if (c != null && c.isIndexed()) {
                        Object value = node.properties[c.getIndex()];
                        viewIndex.put(c, value, (T) node);
                    }
                }
            }
        } else if (propertyStore.elementType.equals(Edge.class)) {
            for (Edge e : graph.getEdges()) {
                EdgeImpl edge = (EdgeImpl) e;

                for (Column c : propertyStore.columns) {
                    if (c != null && c.isIndexed()) {
                        Object value = edge.properties[c.getIndex()];
                        viewIndex.put(c, value, (T) edge);
                    }
                }
            }
        }
        return viewIndex;
    }

    protected synchronized void deleteViewIndex(Graph graph) {
        IndexImpl<T> index = viewIndexes.remove(graph);
        if (index != null) {
            index.destroy();
        }
    }

    public synchronized Object set(Column column, Object oldValue, Object value, T element) {
        value = mainIndex.set(column, oldValue, value, element);

        if (!viewIndexes.isEmpty()) {
            for (Entry<Graph, IndexImpl<T>> entry : viewIndexes.entrySet()) {
                Graph graph = entry.getKey();
                if (element instanceof Node) {
                    if (graph.contains((Node) element)) {
                        entry.getValue().set(column, oldValue, value, element);
                    }
                } else if (element instanceof Edge) {
                    if (graph.contains((Edge) element)) {
                        entry.getValue().set(column, oldValue, value, element);
                    }
                }
            }
        }

        return value;
    }

    public synchronized void clear(T element) {
        ElementImpl elementImpl = (ElementImpl) element;

        for (Column c : propertyStore.columns) {
            if (c != null && c.isIndexed()) {
                Object value = elementImpl.properties[c.getIndex()];
                mainIndex.remove(c, value, element);
                for (Entry<Graph, IndexImpl<T>> entry : viewIndexes.entrySet()) {
                    Graph graph = entry.getKey();
                    boolean inView = element instanceof Node ? graph.contains((Node) element) : graph.contains((Edge) element);
                    if (inView) {
                        entry.getValue().remove(c, value, element);
                    }
                }
            }
        }
    }

    public synchronized void index(T element) {
        ElementImpl elementImpl = (ElementImpl) element;

        for (Column c : propertyStore.columns) {
            if (c != null && c.isIndexed()) {
                Object value = elementImpl.properties[c.getIndex()];
                value = mainIndex.put(c, value, element);
                elementImpl.properties[c.getIndex()] = value;
            }
        }
    }
}
