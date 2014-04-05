/**
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
package org.gephi.graph.benchmark;

import java.util.Random;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.store.NodeImpl;

/**
 * Generates directed connected random graph with wiring probability p
 *
 * @author Mathieu Bastian, Nitesh Bhargava
 */
public class RandomGraph extends Generator {

    protected final int numberOfNodes;
    protected final int numberOfEdges;
    protected final double wiringProbability;
    
    public RandomGraph(int n, double p) {
        super();
        numberOfNodes = n;
        numberOfEdges = (int)(n*(n-1)*p);
        wiringProbability = p;
    }
    
    public RandomGraph(int nodes, int edges) {
        this(nodes, ((double)edges)/(nodes*(nodes-1)));
    }

    @Override
    public RandomGraph generate() {
        Random random = new Random();

        for (int i = 0; i < numberOfNodes; i++) {
            Node node = factory.newNode(i);
            nodes.add(node);
        }

        if (wiringProbability > 0) {
            for (int i = 0; i < numberOfNodes - 1; i++) {
                NodeImpl source = graphStore.getNode(i);
                for (int j = i + 1; j < numberOfNodes; j++) {
                    NodeImpl target = graphStore.getNode(j);

                    if (random.nextDouble() < wiringProbability && source != target) {
                        Edge edge = factory.newEdge(source, target, 0, true);
                        edges.add(edge);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public RandomGraph commit() {
        commitInner();
        return this;
    }
}
