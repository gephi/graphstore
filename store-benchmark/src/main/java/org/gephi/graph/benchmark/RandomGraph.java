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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.awt.Color;
import java.util.Random;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.gephi.graph.api.Node;
import org.gephi.graph.store.ColumnImpl;
import org.gephi.graph.store.ColumnStore;
import org.gephi.graph.store.NodeImpl;
import org.gephi.graph.store.EdgeStore;
import org.gephi.graph.store.EdgeImpl;
import org.gephi.graph.store.GraphLock;
import static org.gephi.graph.store.GraphStoreConfiguration.ENABLE_ELEMENT_LABEL;
import org.gephi.graph.store.GraphStore;
import org.gephi.graph.store.GraphStoreConfiguration;
import static org.gephi.graph.store.GraphStoreConfiguration.ENABLE_INDEX_NODES;



/**
 * Generates directed connected random graph with wiring probability p
 * @author Mathieu Bastian, Nitesh Bhargava
 */


public class RandomGraph implements Generator {

    
    protected int numberOfNodes;
    protected double wiringProbability;
    private int edgeCount;  
    protected boolean cancel;   // cancel - to break the process anytime
    private LongSet idSet;
    
    public GraphStore graphStore;
    protected final GraphLock lock;
    protected String columnName;
    protected final ColumnStore<Node> nodeColumnStore;
    
    //Constructor
    public RandomGraph()
    {
       lock = new GraphLock();
       
       nodeColumnStore = new ColumnStore<Node>(Node.class, GraphStoreConfiguration.ENABLE_INDEX_NODES, GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? lock : null);
       numberOfNodes = 10;
       wiringProbability = 0.01;
       edgeCount = 0;  
       cancel = false;
       this.columnName = null;
       
       graphStore = new GraphStore();
       idSet = new LongOpenHashSet();
       generate(graphStore);
       
    }
    
    /* 
     * used for user defined number of nodes and wiring probbability
     * n = number of nodes, p = wiring probability
    */
    
    public RandomGraph(int n,double p)
    {
       lock = new GraphLock();
       
       nodeColumnStore = new ColumnStore<Node>(Node.class, GraphStoreConfiguration.ENABLE_INDEX_NODES, GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? lock : null);
       numberOfNodes = n;
       wiringProbability = p;
       edgeCount = 0;  
       cancel = false;
       this.columnName = null;
       
      graphStore = new GraphStore();
       idSet = new LongOpenHashSet();
       generate(graphStore);
       
    }
    /*
     * used for generating random graph with certain attributes
     */
    public RandomGraph(String col)
    {
       lock = new GraphLock();
       nodeColumnStore = new ColumnStore<Node>(Node.class, GraphStoreConfiguration.ENABLE_INDEX_NODES, GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? lock : null);
       numberOfNodes = 10;
       wiringProbability = 0.01;
       edgeCount = 0;  
       cancel = false; 
       this.columnName = col;
       
       graphStore = new GraphStore();
       idSet = new LongOpenHashSet();
       generate(graphStore);
       
    }
    
    /* 
     * used for user defined number of nodes and wiring probbability
     * n = number of nodes, p = wiring probability
     * "col" used for generating random graph with certain attributes
     * col = attribute id 
     * 
    */
    public RandomGraph(String col,int n ,double p)
    {
       lock = new GraphLock();
       nodeColumnStore = new ColumnStore<Node>(Node.class, GraphStoreConfiguration.ENABLE_INDEX_NODES, GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? lock : null);
       numberOfNodes = n;
       wiringProbability = p;
       edgeCount = 0;  
       cancel = false; 
       this.columnName = col;
       
       graphStore = new GraphStore();
       idSet = new LongOpenHashSet();
       generate(graphStore);
       
    }
    
    
    
    @Override
    public void generate(GraphStore container) {

        int max = numberOfNodes;
        if (wiringProbability > 0) {
            max += numberOfNodes - 1;
        }
       
        
        Random random = new Random();
        Column column = generateBasicColumn("id");  // node attribute added
       
        /*
         * Adding nodes into the graphstore
         */
            for (int i = 0; i < numberOfNodes && !cancel; i++) {
                
                NodeImpl node = new NodeImpl(String.valueOf(i));
                node.setAttribute(column,i);
                graphStore.addNode(node);

            }
            
        /*
         * Adding edges into the graphstore with the wiring probability p
         *
         */
            if (wiringProbability > 0)
            {
                for (int i = 0; i < numberOfNodes - 1 && !cancel; i++)
                {
                    NodeImpl source = graphStore.getNode(String.valueOf(i));
                    for (int j = i + 1; j < numberOfNodes && !cancel; j++)
                    {
                        NodeImpl target = graphStore.getNode(String.valueOf(j));


                            EdgeImpl edge = new EdgeImpl(String.valueOf(edgeCount), source, target, 0, 1.0, true);
                           if (random.nextDouble() < wiringProbability  && source != target) 
                           {
                               graphStore.addEdge(edge);                               
                               idSet.add(edge.getLongId());
                               edgeCount++;
                                
                           }
                    }
                 
                }
            }
         
        
    }


    
    public void setNumberOfNodes(int numberOfNodes) {
        if (numberOfNodes < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.numberOfNodes = numberOfNodes;
    }

    public void setWiringProbability(double wiringProbability) {
        if (wiringProbability < 0 || wiringProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.wiringProbability = wiringProbability;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    private Column generateBasicColumn(String column) {
        
        this.nodeColumnStore.addColumn(new ColumnImpl(column, Integer.class , "Label", null, Origin.DATA, true));
        return this.nodeColumnStore.getColumn(column);
    }
    public double getWiringProbability() {
        return wiringProbability;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    

    /**
     * @return the EdgeCount
     */
    public int getEdgeCount() {
        return edgeCount;
    }

    /**
     * @param EdgeCount the EdgeCount to set
     */
    public void setEdgeCount(int EdgeCount) {
        this.edgeCount = EdgeCount;
    }

    /**
     * @return the idSet
     */
    public LongSet getIdSet() {
        return idSet;
    }

    /**
     * @param idSet the idSet to set
     */
    public void setIdSet(LongSet idSet) {
        this.idSet = idSet;
    }
}











