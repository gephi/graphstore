package org.gephi.graph.benchmark;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.store.EdgeImpl;
import org.gephi.graph.store.EdgeStore;
import org.gephi.graph.store.GraphLock;
import org.gephi.graph.store.NodeImpl;
import org.gephi.graph.store.NodeStore;


/**
 *
 * @author mbastian, niteshbhargv
 */
public class EdgeStoreBenchmark {

    private static int NODES = 50000;
    private static int EDGES = 500000;
    private Object object;
    private EdgeImpl edgeObject;
    private NodeImpl nodeObject;

    public Runnable pushEdgeStore() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EdgeStore edgeStore = new EdgeStore();
                for (EdgeImpl e : generateEdgeList()) {
                    edgeStore.add(e);
                }
                object = edgeStore;
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStore() {
        final EdgeStore edgeStore = new EdgeStore();
        final LongSet edgeSet = new LongOpenHashSet();
        for (EdgeImpl e : generateEdgeList()) {
            edgeStore.add(e);
            edgeSet.add(e.getLongId());
        }
        object = edgeStore;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Iterator<Edge> itr = edgeStore.iterator();
                for (; itr.hasNext();) {
                    edgeObject = (EdgeImpl) itr.next();
                }
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStoreWithLocking() {
        final EdgeStore edgeStore = new EdgeStore(null, new GraphLock(), null, null);
        final LongSet edgeSet = new LongOpenHashSet();
        for (EdgeImpl e : generateEdgeList()) {
            edgeStore.add(e);
            edgeSet.add(e.getLongId());
        }
        object = edgeStore;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Iterator<Edge> itr = edgeStore.iterator();
                for (; itr.hasNext();) {
                    edgeObject = (EdgeImpl) itr.next();
                }
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStoreNeighborsOut() {
        final EdgeStore edgeStore = new EdgeStore();
        List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>(generateEdgeList());
        ObjectSet<NodeImpl> nodeSet = new ObjectOpenHashSet<NodeImpl>();
        for (EdgeImpl e : edgeList) {
            nodeSet.add(e.getSource());
            nodeSet.add(e.getTarget());
            edgeStore.add(e);
        }
        final NodeImpl[] nodes = nodeSet.toArray(new NodeImpl[0]);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (NodeImpl node : nodes) {
                    Iterator<Edge> itr = edgeStore.edgeOutIterator(node);
                    for (; itr.hasNext();) {
                        edgeObject = (EdgeImpl) itr.next();
                    }
                }
            }
        };
        return runnable;
    }

    public Runnable iterateEdgeStoreNeighborsInOut() {
        final EdgeStore edgeStore = new EdgeStore();
        List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>(generateEdgeList());
        ObjectSet<NodeImpl> nodeSet = new ObjectOpenHashSet<NodeImpl>();
        for (EdgeImpl e : edgeList) {
            nodeSet.add(e.getSource());
            nodeSet.add(e.getTarget());
            edgeStore.add(e);
        }
        final NodeImpl[] nodes = nodeSet.toArray(new NodeImpl[0]);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (NodeImpl node : nodes) {
                    Iterator<Edge> itr = edgeStore.edgeIterator(node);
                    for (; itr.hasNext();) {
                        edgeObject = (EdgeImpl) itr.next();
                    }
                }
            }
        };
        return runnable;
    }

    public Runnable resetEdgeStore() {
        final EdgeStore edgeStore = new EdgeStore();
        final List<EdgeImpl> edgeList = new LinkedList<EdgeImpl>(generateEdgeList());
        for (EdgeImpl e : edgeList) {
            edgeStore.add(e);
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (EdgeImpl e : edgeList) {
                    edgeStore.remove(e);
                }
                for (EdgeImpl e : edgeList) {
                    edgeStore.add(e);
                }
            }
        };
        return runnable;
    }
    
    /**
     * addEdge - creates a random graph with given parameters and adds an edge
     */
    
        public Runnable addEdge(final int n, final double p){
           Runnable runnable = new Runnable(){

               @Override
               public void run() {
                  
                   RandomGraph randomGraph = new RandomGraph(n,p);
                   Random random = new Random();
                   boolean flag =true;
                   while(flag){


                       int sourceID = random.nextInt(randomGraph.numberOfNodes);                      
                       int DestID = random.nextInt(randomGraph.numberOfNodes);                     
                       NodeImpl source = randomGraph.graphStore.getNodeStore().get(String.valueOf(sourceID));
                       NodeImpl target = randomGraph.graphStore.getNodeStore().get(String.valueOf(DestID));
                       
                       if(!randomGraph.graphStore.getEdgeStore().contains(source, target,0))
                       {
                           randomGraph.setEdgeCount(randomGraph.getEdgeCount()+1);
                           EdgeImpl edge = new EdgeImpl(String.valueOf(randomGraph.getEdgeCount()), source, target, 0, 1.0, true);
                           randomGraph.graphStore.getEdgeStore().add(edge);
                           flag = false;
                       }
                       
                   }
               }

           };
           return runnable;
       }
        
    /**
     * removeEdge - creates a random graph with given parameters and removes a edge from the given set of edges 
     */
    
     public Runnable removeEdge(final int n, final double p){
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
              RandomGraph randomGraph = new RandomGraph(n,p);
              Random random = new Random();
              boolean flag = true;  // checking edge exists or not
              while(flag){
              
                 NodeImpl source =  randomGraph.graphStore.getNodeStore().get(String.valueOf(random.nextInt(randomGraph.numberOfNodes) ));
                 NodeImpl dest = randomGraph.graphStore.getNodeStore().get(String.valueOf(random.nextInt(randomGraph.numberOfNodes) ));
                  if(randomGraph.graphStore.getEdgeStore().contains(source, dest, 0)){
                      flag = false;
                      randomGraph.graphStore.getEdgeStore().remove(randomGraph.graphStore.getEdgeStore().get(source, dest,0));
                 }
                  
              }
              
            }
        };
        
        return runnable;
        
    }
     /**
     * iterateEdge - creates a random graph with given parameters and iterates throughout the given edges
     */
    public Runnable iterateEdge(final int n, final double p){
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                RandomGraph randomGraph = new RandomGraph(n,p);
                Iterator<Edge> itr = randomGraph.graphStore.getEdgeStore().iterator();
                for (; itr.hasNext();) {
                    edgeObject = (EdgeImpl) itr.next();
                }
                }
        };
        return runnable;
    }
    
   /**
    * iterateKleinbergEdge - creates a Kleinberg graph with given parameters and iterates throughout the given edges
    */
    public Runnable iterateKleinbergEdge(final int no,final int local,final int Long){
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                 Kleinberg graph = new Kleinberg(no,local,Long);
                Iterator<Edge> itr = graph.edgeStore.iterator();
                for (; itr.hasNext();) {
                    edgeObject = (EdgeImpl) itr.next();
                }
                }
        };
        return runnable;
    }
    /**
    * iterateKleinbergEdge - creates a Kleinberg graph with given parameters and iterate among neighbors
    */
    public Runnable iterateKleinbergNeighbors(final int no,final int local,final int Long){
       
       
       Runnable runnable = new Runnable() {
       
           

           @Override
           public void run() {
               final  Kleinberg graph = new Kleinberg(no,local,Long);
               Iterator<Edge> edgeIt =  graph.edgeStore.iterator();
               ObjectSet<NodeImpl> nodeSet = new ObjectOpenHashSet<NodeImpl>();
               for(;edgeIt.hasNext();)
               {
                   EdgeImpl e = (EdgeImpl) edgeIt.next();
                   nodeSet.add(e.getSource());
                   nodeSet.add(e.getTarget());
               }
              final NodeImpl[] nodes = nodeSet.toArray(new NodeImpl[0]);
               for (NodeImpl node : nodes) {
                    Iterator<Edge> itr = graph.edgeStore.edgeOutIterator(node);
                    for (; itr.hasNext();) {
                        edgeObject = (EdgeImpl) itr.next();
                    }
                }
           
              
              
               }
       };
       return runnable;
   }
    
  /**
    * iterateKleinbergEdge - creates a Kleinberg graph with given parameters and removes an edge randomly from the Kleinberg graph 
    */
    
    
    public Runnable removeKleinbergEdge(final int no,final int local,final int Long)
    {
        Runnable runnable = new Runnable() {

            @Override
            public void run() 
            {
                
              Kleinberg graph = new Kleinberg(no,local,Long);              
              boolean flag = true;  // checking edge exists or not
              while(flag)
              {
                  
                  for(Node n :graph.graphstore.getNodes())
                  {
                      for(Node m:graph.graphstore.getNodes())
                      {
                          NodeImpl source = graph.graphstore.getNode(n.getId());
                          NodeImpl dest = graph.graphstore.getNode(n.getId());
                          if(graph.edgeStore.contains(source, dest, 0))
                                {
                                        flag = false;
                                        graph.edgeStore.remove(graph.edgeStore.get(source, dest));
                                }
                      }
                      flag = false;
                      
                  }
                 
                  
                  
              }
                
            }
        };
        
        return runnable;
        
    }
    
    
    /**
    * iterateKleinbergEdge - creates a Kleinberg graph with given parameters and adds a edge within given set of nodes  
    */
    
    
    public Runnable addKleinbergEdge(final int no,final int local,final int Long){
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
              Kleinberg graph = new Kleinberg(no,local,Long);
              Random random = new Random();
              boolean flag = true;  // checking edge exists or not
              while(flag)
              {
                 
                  for(Node n :graph.graphstore.getNodes())
                  {
                      for(Node m:graph.graphstore.getNodes())
                      {
                          NodeImpl source = graph.graphstore.getNode(n.getId());
                          NodeImpl dest = graph.graphstore.getNode(n.getId());
                          if(!graph.edgeStore.contains(source, dest, 0))
                                {
                                        flag = false;
                                        graph.setEdgeCount(graph.getEdgeCount()+1);
                                        EdgeImpl edge = new EdgeImpl(String.valueOf(graph.getEdgeCount()), source, dest, 0, 1.0, true);
                                        graph.edgeStore.add(edge);
                                }
                      }
                      flag = false;
                      
                  }
                 
                  
                  
              }
                
            }
        };
        return runnable;
    }

    private List<EdgeImpl> generateEdgeList() {
        final NodeStore nodeStore = new NodeStore();
        for (int i = 0; i < NODES; i++) {
            NodeImpl n = new NodeImpl(String.valueOf(i));
            nodeStore.add(n);
        }
        final List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>();
        LongSet idSet = new LongOpenHashSet();
        Random r = new Random(123);
        int edgeCount = 0;
        while (edgeCount < EDGES) {
            int sourceId = r.nextInt(NODES);
            int targetId = r.nextInt(NODES);
            NodeImpl source = nodeStore.get(sourceId);
            NodeImpl target = nodeStore.get(targetId);
            EdgeImpl edge = new EdgeImpl(String.valueOf(edgeCount), source, target, 0, 1.0, true);
            if (source != target && !idSet.contains(edge.getLongId())) {
                edgeList.add(edge);
                edgeCount++;
                idSet.add(edge.getLongId());
            }
        }
        return edgeList;
    }
    
}
