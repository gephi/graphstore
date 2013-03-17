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
package org.gephi.graph.store;

import org.gephi.graph.api.Graph;

/**
 *
 * @author mbastian
 */
public class GraphVersion {

    protected final Graph graph;
    protected int nodeVersion = Integer.MIN_VALUE + 1;
    protected int edgeVersion = Integer.MIN_VALUE + 1;

    public GraphVersion(Graph graph) {
        this.graph = graph;
    }

    public int incrementAndGetNodeVersion() {
        nodeVersion++;
        if (nodeVersion == Integer.MAX_VALUE) {
            nodeVersion = Integer.MIN_VALUE + 1;
            handleNodeReset();
        }
        return nodeVersion;
    }

    public int incrementAndGetEdgeVersion() {
        edgeVersion++;
        if (edgeVersion == Integer.MAX_VALUE) {
            edgeVersion = Integer.MIN_VALUE + 1;
            handleEdgeReset();
        }
        return edgeVersion;
    }

    private void handleNodeReset() {
        if (graph != null) {
            if (graph.getView().isMainView()) {
            } else {
            }
        }
    }

    private void handleEdgeReset() {
        if (graph != null) {
            if (graph.getView().isMainView()) {
            } else {
            }
        }
    }
}
