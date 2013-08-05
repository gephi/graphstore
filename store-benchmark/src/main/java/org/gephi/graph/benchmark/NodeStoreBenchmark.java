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
package org.gephi.graph.benchmark;

import java.util.Iterator;
import java.util.Random;
import org.gephi.graph.api.Node;
import org.gephi.graph.store.EdgeImpl;
import org.gephi.graph.store.EdgeStore;
import org.gephi.graph.store.NodeImpl;
import org.gephi.graph.store.NodeStore;




/**
 *
 * @author mbastian
 */
public class NodeStoreBenchmark {

    private static int NODES_READ = 500000;
    private static int NODES_WRITE = 50000;
    private Object object;

    public Runnable iterateStore() {
        final NodeStore nodeStore = new NodeStore();
        int nodes = NODES_READ;
        for (int i = 0; i < nodes; i++) {
            NodeImpl n = new NodeImpl(String.valueOf(i));
            nodeStore.add(n);
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                Iterator<Node> m = nodeStore.iterator();
                for (; m.hasNext();) {
                    NodeImpl b = (NodeImpl) m.next();
                    object = b;
                }
            }
        };
        return runnable;
    }

    public Runnable resetNodeStore() {
        int nodes = NODES_WRITE;
        final NodeStore nodeStore = new NodeStore();
        final NodeImpl[] nodeArray = new NodeImpl[nodes];
        for (int i = 0; i < nodes; i++) {
            NodeImpl n = new NodeImpl(String.valueOf(i));
            nodeStore.add(n);
            nodeArray[i] = n;
        }
        Runnable runnable = new Runnable() {
            public void run() {
                for (int i = 0; i < nodeArray.length; i++) {
                    NodeImpl n = nodeArray[i];
                    nodeStore.remove(n);
                }
                for (int i = 0; i < nodeArray.length; i++) {
                    NodeImpl n = nodeArray[i];
                    nodeStore.add(n);
                }
            }
        };
        return runnable;
    }

    public Runnable pushStore() {
        final NodeImpl[] nodeStock = new NodeImpl[NODES_WRITE];
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < NODES_WRITE; i++) {
                    nodeStock[i] = new NodeImpl(String.valueOf(i));
                }
                int nodes = NODES_WRITE;
                NodeStore nodeStore = new NodeStore();
                for (int i = 0; i < nodes; i++) {
                    NodeImpl n = nodeStock[i];
                    nodeStore.add(n);
                }
                object = nodeStore;
            }
        };
        return runnable;
    }
    
    public Runnable addNode() {
       //final NodeImpl[] nodeStock = new NodeImpl[NODES_WRITE];
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                RandomGraph randomGraph = new RandomGraph();
                NodeImpl newRNode = new NodeImpl(String.valueOf("newRandomNode"));
                randomGraph.nodeStore.add(newRNode);
                Random random = new Random();
                if(random.nextDouble() < randomGraph.wiringProbability)
                {
                    EdgeImpl Edge = new EdgeImpl(String.valueOf(randomGraph.numberOfNodes),newRNode,randomGraph.nodeStore.get(String.valueOf(random.nextInt() % randomGraph.numberOfNodes)),0,1.0,true);
                        randomGraph.edgeStore.add(Edge);
                }
                Kleinberg kleinbergGraph = new Kleinberg();
                NodeImpl newKNode = new NodeImpl(String.valueOf("newKleinbergNode"));
                kleinbergGraph.graphstore.addNode(newKNode);
                
                /// Add Edge implementation remaining
            }
        };
        return runnable;
        
    }
    public Runnable removeNode() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Random random = new Random();                
                RandomGraph randomGraph = new RandomGraph();
                randomGraph.nodeStore.remove(String.valueOf(random.nextInt() % randomGraph.numberOfNodes));
                
                Kleinberg kleinbergGraph = new Kleinberg();
                
            }
            
        };
        return runnable;
}
   public Runnable iterateNodes(){
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                RandomGraph randomGraph = new RandomGraph();
                Iterator<Node> m = randomGraph.nodeStore.iterator();
                for (; m.hasNext();) {
                    NodeImpl b = (NodeImpl) m.next();
                    object = b;
                }              
            }
        };
        
        return runnable;
       
   }
   
   public Runnable iterateNeighbors(){
       Runnable runnable = new Runnable() {

           @Override
           public void run() {
               RandomGraph randomGraph = new RandomGraph();
               Random random = new Random();
               NodeImpl node = randomGraph.nodeStore.get(random.nextInt()% randomGraph.numberOfNodes);
               Iterator<Node> Neighbors = randomGraph.edgeStore.neighborIterator(node);
               }
       };
       return runnable;
   }

}    