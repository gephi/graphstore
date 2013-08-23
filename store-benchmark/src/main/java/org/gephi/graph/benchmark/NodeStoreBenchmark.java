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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Iterator;
import java.util.Random;
import org.gephi.graph.api.Edge;
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
                NodeImpl newRNode = new NodeImpl(String.valueOf(randomGraph.numberOfNodes + 1));
                randomGraph.nodeStore.add(newRNode);
                Random random = new Random();
                //if(random.nextDouble() < randomGraph.wiringProbability)
                {
                    randomGraph.setEdgeCount(randomGraph.getEdgeCount()+1);
                    int temp = random.nextInt(randomGraph.numberOfNodes);
                  //  System.out.println("temp:"+temp );
                    NodeImpl source = randomGraph.nodeStore.get(String.valueOf(temp));
                    
                    NodeImpl dest = randomGraph.nodeStore.get(String.valueOf(1));
                    EdgeImpl Edge = new EdgeImpl(String.valueOf(randomGraph.getEdgeCount()), source,newRNode, 0, 1.0, true);
                    if(Edge.getSource()== null)
                    {
                        System.out.println("source null");
                    }
                    if(Edge.getTarget()== null)
                    {
                        System.out.println("target null");
                    }
                    //randomGraph.nodeStore.get(String.valueOf(randomGraph.numberOfNodes+1))
                    if(source!=newRNode){
                        randomGraph.edgeStore.add(Edge);
                     //   System.out.println("not equal");
                    }
                    
                }
               
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
                int nodeID = random.nextInt(randomGraph.numberOfNodes);
                
                randomGraph.nodeStore.remove(randomGraph.nodeStore.get(nodeID));
                
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
               NodeImpl node = randomGraph.nodeStore.get(random.nextInt(randomGraph.numberOfNodes));
               Iterator<Node> Neighbors = randomGraph.edgeStore.neighborIterator(node);
               }
       };
       return runnable;
   }
   
   public Runnable addKleinbergNode()
   {
       Runnable runnable = new Runnable() {

           @Override
           public void run() {
               // Adding the Node
               Kleinberg graph = new Kleinberg();
               Random random = new Random();
               NodeImpl newNode = new NodeImpl(String.valueOf(graph.getNodeCount()+1));
               graph.setNodeCount(graph.getNodeCount()+1);
               int p = graph.getp();
               int q = graph.getq();
               int n = graph.getn();
               int r = graph.getr();
               int i = graph.getNodeCount();
             
               int temp = 0;
               
               NodeImpl nodes[][] =new NodeImpl[n][n];
               graph.graphstore.addNode(newNode);
               for(int it=0;it<n;it++)
                   for(int j=0;j<n;j++)
                   {
                       nodes[it][j] = graph.graphstore.getNode(temp);
                       
                       temp++;
                   }
                       
               
               
               

               // add edges that has this node as a source or destination and have atmost p local contacts
               for (int j = 0; j < n ; ++j)
               {
                 //  System.out.println("in the l loop");
				for (int k = i - p; k <= i + p ; ++k)
                                {
                                     //   System.out.println("break");
					for (int l = j - p; l <= j + p ; ++l)
                                        {
                                            
                                       //     System.out.println(l+" k ="+k+" n ="+n);
						if ((graph.isTorusBased() || !graph.isTorusBased() && k >= 0  && l >= 0 ) &&
                                                         graph.d(i, j, k, l) <= p )
                                                {
							//System.out.println("in local range");
                                                        Object id  = nodes[(k + n) % n][(l + n) % n].getId();
                                                        graph.setEdgeCount(graph.getEdgeCount()+1);
                                                        EdgeImpl Edge = new EdgeImpl(graph.getEdgeCount(),newNode,graph.graphstore.getNode(id),0,1.0,true);
                                                        
                                                        graph.graphstore.addEdge(Edge);
                                                        graph.getIdSet().add(Edge.getLongId());
						}
						
					}
              
                                }
               }
               
               // add edges that has this node as a source or destination and have atmost q local contacts
               
			for (int j = 0; j < n ; ++j)
                        {
                            
				double sum = 0.0;
				for (int k = 0; k < n ; ++k)
					for (int l = 0; l < n ; ++l) 
                                        {
						if ( !graph.isTorusBased() && graph.d(i, j, k, l) > p)
							sum += Math.pow(graph.d(i, j, k, l), -r);
						else if ( graph.isTorusBased() && graph.dtb(i, j, k, l) > p)
							sum += Math.pow(graph.dtb(i, j, k, l), -r);
						
					}
                             //   System.out.println("sum = "+sum);
				for (int m = 0; m < q ; ++m) 
                                {
                                    //    System.out.println("inside");
					double  b = random.nextDouble();
					boolean e = false;
					while (!e) 
                                        {
                                                System.out.println("inside");
						double pki = 0.0;
						for (int k = 0; k < n && !e ; ++k)
							for (int l = 0; l < n && !e; ++l)
								if (!graph.isTorusBased() && graph.d(i, j, k, l) > p || graph.isTorusBased() && graph.dtb(i, j, k, l) > p)
                                                                {
									
                                                                        pki += Math.pow(!graph.isTorusBased() ? graph.d(i, j, k, l) : graph.dtb(i, j, k, l), -r) / sum;
                                                                        Object id  = nodes[k][l].getId();
                                                                        EdgeImpl Edge = new EdgeImpl(graph.getEdgeCount()+1,newNode,graph.graphstore.getNode(id),0,1.0,true);
									if ( !graph.getIdSet().contains(Edge.getLongId())) 
                                                                        {    
                                                                                System.out.println("in long range");									
										graph.graphstore.addEdge(Edge);
                                                                                graph.getIdSet().add(Edge.getLongId());
										e = true;
									}
								}
						b = random.nextDouble();
					}
					
				}
			}

               
           }
       };
       
       return runnable;
   }
    
public Runnable iterateKleinbergNodes(){
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Kleinberg graph = new Kleinberg();
                NodeStore nodeStore = new NodeStore();
                nodeStore = (NodeStore) graph.graphstore.getNodes();
                Iterator<Node> m = nodeStore.iterator();
                for (; m.hasNext();) {
                    NodeImpl b = (NodeImpl) m.next();
                    object = b;
                }              
            }
        };
        
        return runnable;
       
   }
   
   
}