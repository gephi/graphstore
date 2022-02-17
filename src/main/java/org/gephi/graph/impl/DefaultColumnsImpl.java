package org.gephi.graph.impl;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Origin;

public class DefaultColumnsImpl implements GraphModel.DefaultColumns {

    protected final GraphStore store;

    // Extra columns (temporary solution, until they are fully added as normal
    // columns)
    protected final ColumnImpl degreeColumn;
    protected final ColumnImpl inDegreeColumn;
    protected final ColumnImpl outDegreeColumn;
    protected final ColumnImpl typeColumn;

    public DefaultColumnsImpl(GraphStore store) {
        this.store = store;

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
        return store.nodeTable.getColumn(GraphStoreConfiguration.ELEMENT_ID_INDEX);
    }

    @Override
    public Column edgeId() {
        return store.nodeTable.getColumn(GraphStoreConfiguration.ELEMENT_LABEL_INDEX);
    }

    @Override
    public Column nodeLabel() {
        return store.nodeTable.getColumn(GraphStoreConfiguration.ELEMENT_LABEL_INDEX);
    }

    @Override
    public Column edgeLabel() {
        return store.edgeTable.getColumn(GraphStoreConfiguration.ELEMENT_ID_INDEX);
    }

    @Override
    public Column nodeTimeSet() {
        return store.nodeTable.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
    }

    @Override
    public Column edgeTimeSet() {
        return store.edgeTable.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
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
}
