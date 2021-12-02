package org.gephi.graph.api;

import java.util.function.Consumer;

/**
 * Object to query the nodes and edges of the graph in a spatial context.
 * 
 * @author Eduardo Ramos
 */
public interface SpatialIndex {

    NodeIterable getNodesInArea(Rect2D rect);

    EdgeIterable getEdgesInArea(Rect2D rect);
}
