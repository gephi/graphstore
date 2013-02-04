package org.gephi.graph.store;

import org.gephi.graph.api.Node;

/**
 *
 * @author mbastian
 */
public class NodeImpl extends ElementImpl implements Node {

    protected int storeId = NodeStore.NULL_ID;
    protected EdgeImpl[] headOut = new EdgeImpl[EdgeStore.DEFAULT_TYPE_COUNT];
    protected EdgeImpl[] headIn = new EdgeImpl[EdgeStore.DEFAULT_TYPE_COUNT];
    //Degree
    protected int inDegree;
    protected int outDegree;
    protected int mutualDegree;

    public NodeImpl(Object id, GraphStore graphStore) {
        super(id, graphStore);
    }

    public NodeImpl(Object id) {
        super(id, null);
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int id) {
        this.storeId = id;
    }

    public int getDegree() {
        return inDegree + outDegree;
    }

    public int getInDegree() {
        return inDegree;
    }

    public int getOutDegree() {
        return outDegree;
    }

    public int getUndirectedDegree() {
        return inDegree + outDegree - mutualDegree;
    }

    @Override
    ColumnStore getPropertyStore() {
        if (graphStore != null) {
            return graphStore.nodePropertyStore;
        }
        return null;
    }

    @Override
    boolean isValid() {
        return storeId != NodeStore.NULL_ID;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
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
        final NodeImpl other = (NodeImpl) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
}
