/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.graph.benchmark;

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
 * @author Cezary Bartosiak
 */

public class Kleinberg implements Generator {
	private boolean cancel = false;
	//private ProgressTicket progressTicket;

	private int n = 10;
	private int p = 2;
	private int q = 2;
	private int r = 0;

	private boolean torusBased = false;

	@Override
	public void generate(GraphStore graphStore) {
		//Progress.start(progressTicket, n * n + n * n * (2 * p + 1) * (2 * p + 1) +
		//		(int)Math.pow(n, 4) + n * n * q);
		Random random = new Random();

		// Timestamps
		int vt = 0;
		int et = 1;

		// Creating lattice n x n
		NodeImpl[][] nodes = new NodeImpl[n][n];
		for (int i = 0; i < n && !cancel; ++i)
			for (int j = 0; j < n && !cancel; ++j) {
				NodeImpl node = new NodeImpl(String.valueOf("Node " + i + " " + j));
				//node.setLabel("Node " + i + " " + j);)
				//node.addTimeInterval(vt + "", 2 * n * n + "");
				nodes[i][j] = node;
				graphStore.addNode(node);
				//Progress.progress(progressTicket);
			}

		// Creating edges from each node to p local contacts
		for (int i = 0; i < n && !cancel; ++i)
			for (int j = 0; j < n && !cancel; ++j, ++et)
				for (int k = i - p; k <= i + p && !cancel; ++k)
					for (int l = j - p; l <= j + p && !cancel; ++l) {
						if ((torusBased || !torusBased && k >= 0 && k < n && l >= 0 && l < n) &&
								d(i, j, k, l) <= p && nodes[i][j] != nodes[(k + n) % n][(l + n) % n]) {
							EdgeStore edgeStore = new EdgeStore();
                                                        EdgeImpl Edge = new EdgeImpl(String.valueOf(j),nodes[i][j],nodes[(k + n) % n][(l + n) % n],0,1.0,true);
                                                        edgeStore.add(Edge);
                                                        graphStore.addEdge(Edge);
							
							
	
						}
						//Progress.progress(progressTicket);
					}

		// Creating edges from each node to q long-range contacts
		for (int i = 0; i < n && !cancel; ++i)
			for (int j = 0; j < n && !cancel; ++j, ++et) {
				double sum = 0.0;
				for (int k = 0; k < n && !cancel; ++k)
					for (int l = 0; l < n && !cancel; ++l) {
						if (!torusBased && d(i, j, k, l) > p)
							sum += Math.pow(d(i, j, k, l), -r);
						else if (torusBased && dtb(i, j, k, l) > p)
							sum += Math.pow(dtb(i, j, k, l), -r);
						//Progress.progress(progressTicket);
					}
				for (int m = 0; m < q && !cancel; ++m) {
					double  b = random.nextDouble();
					boolean e = false;
					while (!e && !cancel) {
						double pki = 0.0;
						for (int k = 0; k < n && !e && !cancel; ++k)
							for (int l = 0; l < n && !e && !cancel; ++l)
								if (!torusBased && d(i, j, k, l) > p || torusBased && dtb(i, j, k, l) > p) {
									pki += Math.pow(!torusBased ? d(i, j, k, l) : dtb(i, j, k, l), -r) / sum;

									if (b <= pki && !edgeStore.) {
										EdgeDraft edge = container.factory().newEdgeDraft();
										edge.setSource(nodes[i][j]);
										edge.setTarget(nodes[k][l]);
										//edge.addTimeInterval(et + "", 2 * n * n + "");
										container.addEdge(edge);

										e = true;
									}
								}
						b = random.nextDouble();
					}
					//Progress.progress(progressTicket);
				}
			}

		//Progress.finish(progressTicket);
		//progressTicket = null;
	}

	private int d(int i, int j, int k, int l) {
		return Math.abs(k - i) + Math.abs(l - j);
	}

	private int dtb(int i, int j, int k, int l) {
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

	

	@Override
	public boolean cancel() {
		cancel = true;
		return true;
	}

	
}