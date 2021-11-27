package org.gephi.graph.api;

import java.util.function.Consumer;

/**
 * Object to query the nodes and edges of the graph in a spatial context.
 * 
 * @author Eduardo Ramos
 */
public interface SpatialIndex {

    NodeIterable getNodesInArea(Rect2D rect);

    void getNodesInArea(Rect2D rect, Consumer<Node> callback);

    EdgeIterable getEdgesInArea(Rect2D rect);

    void getEdgesInArea(Rect2D rect, Consumer<Edge> callback);
}
