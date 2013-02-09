package org.gephi.attribute.api;

import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.NodeIterable;

/**
 *
 * @author mbastian
 */
public interface TimestampIndex {

    public double getMinTimestamp();

    public double getMaxTimestamp();

    public NodeIterable getNodes(double timestamp);

    public NodeIterable getNodes(double from, double to);

    public EdgeIterable getEdges(double timestamp);

    public EdgeIterable getEdges(double from, double to);
}
