/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.graph.benchmark;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.awt.Color;
import java.util.Random;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
//import org.openide.util.lookup.ServiceProvider;
import org.gephi.graph.api.Node;
import org.gephi.graph.store.ColumnImpl;
import org.gephi.graph.store.ColumnStore;
import org.gephi.graph.store.NodeImpl;
//import org.gephi.graph.store.NodeStore;
import org.gephi.graph.store.EdgeStore;
import org.gephi.graph.store.EdgeImpl;
import org.gephi.graph.store.GraphLock;
import static org.gephi.graph.store.GraphStoreConfiguration.ENABLE_ELEMENT_LABEL;
import org.gephi.graph.store.GraphStore;
import org.gephi.graph.store.GraphStoreConfiguration;

import static org.gephi.graph.store.GraphStoreConfiguration.ENABLE_INDEX_NODES;



/**
 *
 * @author Mathieu Bastian
 */

//@ServiceProvider(service = Generator.class)
public class RandomGraph implements Generator {

    
    protected int numberOfNodes;
    protected double wiringProbability;
    private int edgeCount;  
    protected boolean cancel;
    private LongSet idSet;
    
    public GraphStore graphStore;
    protected final GraphLock lock;
    protected String columnName;
    protected final ColumnStore<Node> nodeColumnStore;
    
    
    public RandomGraph()
    {
       lock = new GraphLock();
       
       nodeColumnStore = new ColumnStore<Node>(Node.class, GraphStoreConfiguration.ENABLE_INDEX_NODES, GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? lock : null);
       numberOfNodes = 50;
       wiringProbability = 0.5;
       edgeCount = 0;  
       cancel = false;
       this.columnName = null;
       
       graphStore = new GraphStore();
       idSet = new LongOpenHashSet();
       generate(graphStore);
       
    }
    public RandomGraph(String col)
    {
       lock = new GraphLock();
       nodeColumnStore = new ColumnStore<Node>(Node.class, GraphStoreConfiguration.ENABLE_INDEX_NODES, GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? lock : null);
       numberOfNodes = 50;
       wiringProbability = 0.5;
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
       // Progress.start(progress, max);
        
        Random random = new Random();
        Column column = generateBasicColumn("id");
       
            for (int i = 0; i < numberOfNodes && !cancel; i++) {
                
                NodeImpl node = new NodeImpl(String.valueOf(i));
                node.setAttribute(column,i);
                graphStore.addNode(node);

            }
            if(idSet.isEmpty())
            System.out.println("id set is empty ");
           // LongSet idSet = new LongOpenHashSet();
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
                               //edgStore.add(edge);
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











