/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gephi.graph.api;

/**
 * Query the (quadtree-based) index based on the given rectangle area.
 * <p>
 * The spatial index is not enabled by default. To enable it, set the
 * appropriate configuration:
 * <code>@{@link Configuration.Builder#enableSpatialIndex(boolean)}</code>.
 * <p>
 * When nodes are moved, added or removed, the spatial index is automatically
 * updated. Edges are not indexed, but they are queried based on whether their
 * source or target nodes are in the given area.
 * <p>
 * The Z position is not taken into account when querying the spatial index,
 * only X/Y are supported.
 * </p>
 *
 * @author Eduardo Ramos
 */
public interface SpatialIndex {

    /**
     * Returns the nodes in the given area.
     *
     * @param rect area to query
     * @return nodes in the area
     */
    NodeIterable getNodesInArea(Rect2D rect);

    /**
     * Returns the nodes in the given area using a faster, but approximate method.
     * <p>
     * All nodes in the provided area are guaranteed to be returned, but some nodes
     * outside the area may also be returned.
     *
     * @param rect area to query
     * @return nodes in the area
     */
    NodeIterable getApproximateNodesInArea(Rect2D rect);

    /**
     * Returns the edges in the given area. Edges may be returned twice.
     *
     * @param rect area to query
     * @return edges in the area
     */
    EdgeIterable getEdgesInArea(Rect2D rect);

    /**
     * Returns the edges in the given area using a faster, but approximate method.
     * <p>
     * All edges in the provided area are guaranteed to be returned, but some edges
     * outside the area may also be returned. Edges may also be returned twice.
     *
     * @param rect area to query
     * @return edges in the area
     */
    EdgeIterable getApproximateEdgesInArea(Rect2D rect);

    /**
     * Returns the bounding rectangle that contains all nodes in the graph. The
     * boundaries are calculated based on each node's position and size.
     *
     * @return the bounding rectangle, or null if there are no nodes
     */
    Rect2D getBoundaries();

    /**
     * Acquires a read lock on the spatial index. This is recommended when using the
     * query functions in a stream context, to avoid the spatial index being
     * modified while being queried.
     * <p>
     * Every call to this method must be matched with a call to
     * {@link #readUnlock()}.
     */
    void readLock();

    /**
     * Releases a read lock on the spatial index. This must be called after a call
     * to {@link #readLock()}.
     */
    void readUnlock();
}
