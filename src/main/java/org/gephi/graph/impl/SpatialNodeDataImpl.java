package org.gephi.graph.impl;

public class SpatialNodeDataImpl {

    public float minX, minY, maxX, maxY;

    protected NodesQuadTree.QuadTreeNode quadTreeNode;
    protected int arrayIndex = -1; // Index in the quad tree node's array, -1 if not in a node

    public SpatialNodeDataImpl(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void updateBoundaries(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void setQuadTreeNode(NodesQuadTree.QuadTreeNode quadTreeNode) {
        this.quadTreeNode = quadTreeNode;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public void clear() {
        this.quadTreeNode = null;
        this.arrayIndex = -1;
    }
}
