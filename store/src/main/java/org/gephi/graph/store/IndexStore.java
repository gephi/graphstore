package org.gephi.graph.store;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
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

    protected void addColumn(ColumnImpl col) {
        mainIndex.addColumn(col);
        for (IndexImpl<T> index : viewIndexes.values()) {
            index.addColumn(col);
        }
    }

    protected void removeColumn(ColumnImpl col) {
        mainIndex.removeColumn(col);
        for (IndexImpl<T> index : viewIndexes.values()) {
            index.destroy();
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
        IndexImpl<T> viewIndex = viewIndexes.get(graph);
        if (viewIndex == null) {
            viewIndex = createViewIndex(graph);
        }
        return viewIndex;
    }

    protected IndexImpl createViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't create a view index for the main view");
        }
        IndexImpl viewIndex = new IndexImpl<T>(propertyStore);
        viewIndex.addAllColumns(propertyStore.columns);
        viewIndexes.put(graph, viewIndex);

        Iterator<T> iterator = null;
        if (propertyStore.elementType.equals(Node.class)) {
            iterator = (Iterator<T>) graph.getNodes().iterator();
        } else if (propertyStore.elementType.equals(Edge.class)) {
            iterator = (Iterator<T>) graph.getEdges().iterator();
        }

        if (iterator != null) {
            while (iterator.hasNext()) {
                ElementImpl element = (ElementImpl) iterator.next();

                final int length = propertyStore.length;
                final ColumnImpl[] cols = propertyStore.columns;
                for (int i = 0; i < length; i++) {
                    Column c = cols[i];
                    if (c != null && c.isIndexed()) {
                        Object value = element.properties[c.getIndex()];
                        viewIndex.put(c, value, element);
                    }
                }
            }
        }
        return viewIndex;
    }

    protected void deleteViewIndex(Graph graph) {
        if (graph.getView().isMainView()) {
            throw new IllegalArgumentException("Can't delete a view index for the main view");
        }
        IndexImpl<T> index = viewIndexes.remove(graph);
        if (index != null) {
            index.destroy();
        }
    }

    public Object set(Column column, Object oldValue, Object value, T element) {
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

    public void clear(T element) {
        ElementImpl elementImpl = (ElementImpl) element;

        final int length = propertyStore.length;
        final ColumnImpl[] cols = propertyStore.columns;
        for (int i = 0; i < length; i++) {
            Column c = cols[i];
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

    public void index(T element) {
        ElementImpl elementImpl = (ElementImpl) element;
        ensurePropertyArrayLength(elementImpl, propertyStore.length);

        final int length = propertyStore.length;
        final ColumnImpl[] cols = propertyStore.columns;
        for (int i = 0; i < length; i++) {
            Column c = cols[i];
            if (c != null && c.isIndexed()) {
                Object value = elementImpl.properties[c.getIndex()];
                value = mainIndex.put(c, value, element);
                elementImpl.properties[c.getIndex()] = value;
            }
        }
    }

    public void clear() {
        mainIndex.clear();
        for (IndexImpl index : viewIndexes.values()) {
            index.clear();
        }
    }

    private void ensurePropertyArrayLength(ElementImpl element, int size) {
        final Object[] properties = element.properties;
        if (size > properties.length) {
            Object[] newArray = new Object[size];
            System.arraycopy(properties, 0, newArray, 0, properties.length);
            element.properties = newArray;
        }
    }
}
