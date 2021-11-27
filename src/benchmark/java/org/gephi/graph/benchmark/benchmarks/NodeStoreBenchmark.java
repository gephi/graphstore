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
package org.gephi.graph.benchmark.benchmarks;

import java.util.Iterator;
import java.util.List;

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.NodeImpl;
import org.gephi.graph.impl.NodeStore;

/**
 *
 * @author mbastian, niteshbhargv
 */
public class NodeStoreBenchmark {

    private Object object;

    public Runnable iterateStore(final int nodes) {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, 0, config).generate().commit();
        final NodeStore nodeStore = graph.getStore().getNodeStore();
        Runnable runnable = () -> {
            Iterator<Node> m = nodeStore.iterator();
            for (; m.hasNext();) {
                NodeImpl b = (NodeImpl) m.next();
                object = b;
            }
        };
        return runnable;
    }

    public Runnable resetNodeStore(final int nodes) {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, 0, config).generate().commit();
        final NodeStore nodeStore = graph.getStore().getNodeStore();
        final List<Node> nodeList = graph.getNodes();
        Runnable runnable = () -> {
            for (Node n : nodeList) {
                nodeStore.remove(n);
            }
            for (Node n : nodeList) {
                nodeStore.add(n);
            }
        };
        return runnable;
    }

    public Runnable pushStore(int nodes) {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, 0, config).generate();
        final NodeStore nodeStore = graph.getStore().getNodeStore();
        final List<Node> nodeList = graph.getNodes();

        Runnable runnable = () -> {
            nodeStore.clear();
            for (Node n : nodeList) {
                nodeStore.add(n);
            }
        };
        return runnable;
    }
}
