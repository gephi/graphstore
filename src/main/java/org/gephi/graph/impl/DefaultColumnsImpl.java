package org.gephi.graph.impl;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;

public class DefaultColumnsImpl implements GraphModel.DefaultColumns {

    protected final GraphStore store;

    // Default columns (initialised at store creation)
    protected final TableDefaultColumns<Node> nodeDefaultColumns;
    protected final TableDefaultColumns<Edge> edgeDefaultColumns;

    // Extra columns (temporary solution, until they are fully added as normal
    // columns)
    protected final ColumnImpl degreeColumn;
    protected final ColumnImpl inDegreeColumn;
    protected final ColumnImpl outDegreeColumn;
    protected final ColumnImpl typeColumn;

    public DefaultColumnsImpl(GraphStore store) {
        this.store = store;
        this.nodeDefaultColumns = new TableDefaultColumns<>(store.nodeTable);
        this.edgeDefaultColumns = new TableDefaultColumns<>(store.edgeTable);

        degreeColumn = new ColumnImpl(store.nodeTable, GraphStoreConfiguration.NODE_DEGREE_COLUMN_ID, Integer.class,
                "Degree", null, Origin.PROPERTY, false, true);
        inDegreeColumn = new ColumnImpl(store.nodeTable, GraphStoreConfiguration.NODE_IN_DEGREE_COLUMN_ID,
                Integer.class, "In-Degree", null, Origin.PROPERTY, false, true);
        outDegreeColumn = new ColumnImpl(store.nodeTable, GraphStoreConfiguration.NODE_OUT_DEGREE_COLUMN_ID,
                Integer.class, "Out-Degree", null, Origin.PROPERTY, false, true);
        typeColumn = new ColumnImpl(store.edgeTable, GraphStoreConfiguration.EDGE_TYPE_COLUMN_ID, Integer.class, "Type",
                null, Origin.PROPERTY, false, true);
    }

    @Override
    public Column nodeId() {
        return nodeDefaultColumns.id;
    }

    @Override
    public Column edgeId() {
        return edgeDefaultColumns.id;
    }

    public Column edgeWeight() {
        return store.edgeTable.getColumn(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
    }

    @Override
    public Column nodeLabel() {
        return nodeDefaultColumns.label;
    }

    @Override
    public Column edgeLabel() {
        return edgeDefaultColumns.label;
    }

    @Override
    public Column nodeTimeSet() {
        return nodeDefaultColumns.timeset;
    }

    @Override
    public Column edgeTimeSet() {
        return edgeDefaultColumns.timeset;
    }

    @Override
    public Column degree() {
        return degreeColumn;
    }

    @Override
    public Column inDegree() {
        return inDegreeColumn;
    }

    @Override
    public Column outDegree() {
        return outDegreeColumn;
    }

    @Override
    public Column edgeType() {
        return typeColumn;
    }

    protected static class TableDefaultColumns<T extends Element> {

        protected final ColumnImpl id;
        protected final ColumnImpl label;
        protected final ColumnImpl timeset;

        public TableDefaultColumns(TableImpl<T> table) {
            this.id = table.getColumn(GraphStoreConfiguration.ELEMENT_ID_INDEX);
            this.label = table.getColumn(GraphStoreConfiguration.ELEMENT_LABEL_INDEX);
            this.timeset = table.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
        }
    }
}
