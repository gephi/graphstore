package org.gephi.graph.store;

import org.gephi.graph.api.Edge;

/**
 *
 * @author mbastian
 */
public class EdgeImpl extends ElementImpl implements Edge {

    //Bytes
    public static final byte DIRECTED_BYTE = 1;
    public static final byte MUTUAL_BYTE = 1 << 1;
    //Final Data
    protected final NodeImpl source;
    protected final NodeImpl target;
    protected final int type;
    //Pointers
    protected int storeId = EdgeStore.NULL_ID;
    protected int nextOutEdge = EdgeStore.NULL_ID;
    protected int nextInEdge = EdgeStore.NULL_ID;
    protected int previousOutEdge = EdgeStore.NULL_ID;
    protected int previousInEdge = EdgeStore.NULL_ID;
    //Flags
    protected byte flags;
    //Other fields

    public EdgeImpl(Object id, NodeImpl source, NodeImpl target, int type, boolean directed) {
        super(id);
        this.source = source;
        this.target = target;
        this.flags = (byte) (directed ? 1 : 0);
        this.type = type;
    }

    @Override
    public NodeImpl getSource() {
        return source;
    }

    @Override
    public NodeImpl getTarget() {
        return target;
    }

//    public int getSourceID() {
//        return (int) ((id >> NODE_BITS) & (1l << NODE_BITS) - 1);
//    }
//
//    public int getTargetID() {
//        return (int) (id & (1l << NODE_BITS) - 1);
//    }
//
//    public boolean isDirected() {
//        return ((int) (id >> (NODE_BITS * 2)) & 1) == 1;
//    }
    @Override
    public int getType() {
        return type;
    }

    public int getNextOutEdge() {
        return nextOutEdge;
    }

    public int getNextInEdge() {
        return nextInEdge;
    }

    public int getPreviousOutEdge() {
        return previousOutEdge;
    }

    public int getPreviousInEdge() {
        return previousInEdge;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int id) {
        this.storeId = id;
    }

    public long getLongId() {
        return EdgeStore.getLongId(source, target, isDirected());
    }

    @Override
    public boolean isDirected() {
        return (flags & DIRECTED_BYTE) == 1;
    }

    protected void setMutual(boolean mutual) {
        if (mutual) {
            flags |= MUTUAL_BYTE;
        } else {
            flags &= ~MUTUAL_BYTE;
        }
    }

    protected boolean isMutual() {
        return (flags & MUTUAL_BYTE) == MUTUAL_BYTE;
    }

    @Override
    public boolean isSelfLoop() {
        return source == target;
    }

    @Override
    PropertyStore getPropertyStore() {
        return graphStore.edgePropertyStore;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
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
        final EdgeImpl other = (EdgeImpl) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
}
