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
 * An edge.
 */
public interface Edge extends Element, EdgeProperties {

    /**
     * Returns the egde's source.
     *
     * @return the source node
     */
    public Node getSource();

    /**
     * Returns the edge's target.
     *
     * @return the target node
     */
    public Node getTarget();

    /**
     * Returns the edge's weight.
     *
     * @return the weight
     */
    public double getWeight();

    /**
     * Returns the edge's weight at the given timestamp.
     *
     * @param timestamp the timestamp
     * @return the weight
     */
    public double getWeight(double timestamp);

    /**
     * Returns the edge's weight in the given graph view.
     * <p>
     * Views can configure a time interval and therefore the edge weight over
     * time may vary.
     *
     * @param view the graph view
     * @return the weight
     */
    public double getWeight(GraphView view);

    /**
     * Sets the edge's weight.
     *
     * @param weight the weight
     */
    public void setWeight(double weight);

    /**
     * Sets the edge's weight at the given timestamp.
     *
     * @param weight the weight
     * @param timestamp the timestamp
     */
    public void setWeight(double weight, double timestamp);

    /**
     * Returns true if this edge has a dynamic weight.
     *
     * @return true if the edge has a dynamic weight, false otherwise
     */
    public boolean hasDynamicWeight();

    /**
     * Returns the edge's type.
     *
     * @return the type
     */
    public int getType();

    /**
     * Returns the edge's type label.
     *
     * @return the type label.
     */
    public Object getTypeLabel();

    /**
     * Returns true if this edge is a self-loop.
     *
     * @return true if self-loop, false otherwise
     */
    public boolean isSelfLoop();

    /**
     * Returns true if this edge is directed.
     *
     * @return true if directed, false otherwise
     */
    public boolean isDirected();
}
