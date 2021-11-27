package org.gephi.graph.impl;

public class SpatialNodeDataImpl {

    public float minX, minY, maxX, maxY;

    protected NodesQuadTree.QuadTreeNode quadTreeNode;

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
}
