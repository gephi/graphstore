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
import java.util.Random;
import org.gephi.graph.store.EdgeImpl;
import org.gephi.graph.store.EdgeStore;
import org.gephi.graph.store.GraphStore;
import org.gephi.graph.store.NodeImpl;


/**
 * Generates a directed connected graph.
 *
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.117.7097&rep=rep1&type=pdf
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.83.381&rep=rep1&type=pdf
 *
 * n >= 2
 * p >= 1
 * p <= 2n - 2
 * q >= 0
 * q <= n^2 - p * (p + 3) / 2 - 1 for p < n
 * q <= (2n - p - 3) * (2n - p) / 2 + 1 for p >= n
 * r >= 0
 *
 * Ω(n^4 * q)
 *
 * @author Nitesh Bhargava
 */

public class Kleinberg extends GraphStore implements Generator  {
	
    
    
        private boolean cancel;
	private int n ;
	private int p ;
	private int q;
	private int r ;
        private LongSet idSet;
        private int edgeCount;
        private int nodeCount;
        private boolean torusBased ;
        EdgeStore edgeStore = super.edgeStore ;
        final GraphStore graphstore ;
        
        Kleinberg()
        {
         cancel = false;
	 n = 5;
	 p = 2;
	 q = 2;
	 r = 0;
         edgeCount=0;   
         nodeCount=0;
         torusBased = false;
         idSet = new LongOpenHashSet();
         
         graphstore = new GraphStore();
         generate(this.graphstore);
        }
    
        /**
         * User defined Kleinberg Graph 
         * no*no = number of nodes 
         * local = local contacts
         * Long = long range contacts
         */
        Kleinberg(int no,int local,int Long)
        {
         cancel = false;
	 n = no;
	 p = local;
	 q = Long;
	 r = 0;
         edgeCount=0;   
         nodeCount=0;
         torusBased = false;
         idSet = new LongOpenHashSet();
         
         graphstore = new GraphStore();
         generate(this.graphstore);
        }
	
        
	@Override
	public void generate(GraphStore graphStore) {
		Random random = new Random();
            
		// Creating lattice n x n
		NodeImpl[][] nodes = new NodeImpl[n][n];
		for (int i = 0; i < n ; ++i)
                    
			for (int j = 0; j < n; ++j) 
                        {
                                
				NodeImpl node = new NodeImpl(getNodeCount());
				nodes[i][j] = node;
				graphStore.addNode(node);
                                setNodeCount(getNodeCount() + 1);
				
			}
                
                 
		// Creating edges from each node to p local contacts
                
                 
                 
		for (int i = 0; i < n; ++i)
			for (int j = 0; j < n ; ++j)
				for (int k = i - p; k <= i + p ; ++k)
					for (int l = j - p; l <= j + p ; ++l) {
						if ((isTorusBased() || !isTorusBased() && k >= 0 && k < n && l >= 0 && l < n) &&
								d(i, j, k, l) <= p && nodes[i][j] != nodes[(k + n) % n][(l + n) % n]) {
							
                                                        EdgeImpl Edge = new EdgeImpl(String.valueOf(edgeCount),nodes[i][j],nodes[(k + n) % n][(l + n) % n],0,1.0,true);
                                                        edgeCount++;
                                                        graphStore.addEdge(Edge);
                                                        getIdSet().add(Edge.getLongId());
                                                        
							
							
						}
						
					}
               
		// Creating edges from each node to q long-range contacts
		for (int i = 0; i < n && !cancel; ++i)
			for (int j = 0; j < n && !cancel; ++j) {
				double sum = 0.0;
				for (int k = 0; k < n && !cancel; ++k)
					for (int l = 0; l < n && !cancel; ++l) {
						if (!isTorusBased() && d(i, j, k, l) > p)
							sum += Math.pow(d(i, j, k, l), -r);
						else if (isTorusBased() && dtb(i, j, k, l) > p)
							sum += Math.pow(dtb(i, j, k, l), -r);
						
					}
				for (int m = 0; m < q && !cancel; ++m) {
					double  b = random.nextDouble();
					boolean e = false;
					while (!e) {
						double pki = 0.0;
						for (int k = 0; k < n && !e && !cancel; ++k)
							for (int l = 0; l < n && !e && !cancel; ++l)
								if (!isTorusBased() && d(i, j, k, l) > p || isTorusBased() && dtb(i, j, k, l) > p) {
									pki += Math.pow(!isTorusBased() ? d(i, j, k, l) : dtb(i, j, k, l), -r) / sum;
                                                                        EdgeImpl Edge = new EdgeImpl(String.valueOf(edgeCount),nodes[i][j],nodes[k][l],0,1.0,true);
									if (b <= pki && !getIdSet().contains(Edge.getLongId())) {
                                                                            
                                                                                
                                                                                
                                                                                edgeCount++;									
										graphStore.addEdge(Edge);
                                                                                getIdSet().add(Edge.getLongId());
										e = true;
                                                                                
									}
								}
						b = random.nextDouble();
					}
					
				}
			}

		
	}

	public int d(int i, int j, int k, int l) {
		return Math.abs(k - i) + Math.abs(l - j);
	}

	public int dtb(int i, int j, int k, int l) {
		return Math.min(Math.abs(k - i), n - Math.abs(k - i)) + Math.min(Math.abs(l - j), n - Math.abs(l - j));
	}

	public int getn() {
		return n;
	}

	public int getp() {
		return p;
	}

	public int getq() {
		return q;
	}

	public int getr() {
		return r;
	}

	public boolean isTorusBased() {
		return torusBased;
	}

	public void setn(int n) {
		this.n = n;
	}

	public void setp(int p) {
		this.p = p;
	}

	public void setq(int q) {
		this.q = q;
	}

	public void setr(int r) {
		this.r = r;
	}

	public void setTorusBased(boolean torusBased) {
		this.torusBased = torusBased;
	}

	 public int getEdgeCount() {
        return edgeCount;
    }

        public void setEdgeCount(int edgeCount) {
        this.edgeCount = edgeCount;
        }
       

	@Override
	public boolean cancel() {
		cancel = true;
		return true;
	}

    /**
     * @return the nodeCount
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * @param nodeCount the nodeCount to set
     */
    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
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