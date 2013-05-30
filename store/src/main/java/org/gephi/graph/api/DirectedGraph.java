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
 *
 * @author mbastian
 */
public interface DirectedGraph extends Graph {

    @Override
    public Edge getEdge(Node source, Node target, int type);

    @Override
    public boolean isAdjacent(Node source, Node target);

    @Override
    public boolean isAdjacent(Node source, Node target, int type);

    public NodeIterable getPredecessors(Node node);

    public NodeIterable getPredecessors(Node node, int type);

    public NodeIterable getSuccessors(Node node);

    public NodeIterable getSuccessors(Node node, int type);

    public EdgeIterable getInEdges(Node node);

    public EdgeIterable getInEdges(Node node, int type);

    public EdgeIterable getOutEdges(Node node);

    public EdgeIterable getOutEdges(Node node, int type);

    public Edge getMutualEdge(Edge edge);

    public int getInDegree(Node node);

    public int getOutDegree(Node node);
}
