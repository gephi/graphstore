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
import java.util.Random;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.graph.store.EdgeImpl;
import org.gephi.graph.store.GraphModelImpl;
import org.gephi.graph.store.GraphStore;

/**
 * Generates a directed connected graph.
 *
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.117.7097&rep=rep1&type=pdf
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.83.381&rep=rep1&type=pdf
 *
 * n >= 2 p >= 1 p <= 2n - 2 q >= 0 q <= n^2 - p * (p + 3) / 2 - 1 for p < n q
 * <= (2n - p - 3) * (2n - p) / 2 + 1 for p >= n r >= 0
 *
 * Ω(n^4 * q)
 *
 * @author Nitesh Bhargava
 */
public class KleinbergGraph extends Generator {

    private int n = 5;
    private int p = 2;
    private int q = 2;
    private int r = 0;
    private boolean torusBased;

    /**
     * User defined Kleinberg Graph no*no = number of nodes local = local
     * contacts Long = long range contacts
     */
    KleinbergGraph(int no, int local, int longRange) {
        super();
        n = no;
        p = local;
        q = longRange;
        r = 0;
        torusBased = false;
    }

    @Override
    public KleinbergGraph generate() {
        Random random = new Random();

        // Creating lattice n x n
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                Node node = factory.newNode();
                nodes.add(node);
            }
        }
        LongOpenHashSet edgeSet = new LongOpenHashSet();

        // Creating edges from each node to p local contacts
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                for (int k = i - p; k <= i + p; ++k) {
                    for (int l = j - p; l <= j + p; ++l) {
                        if ((isTorusBased() || !isTorusBased() && k >= 0 && k < n && l >= 0 && l < n)
                                && d(i, j, k, l) <= p && nodes.get(i * n + j) != nodes.get(((k + n) % n) * n + ((l + n) % n))) {
                            Edge edge = factory.newEdge(nodes.get(i * n + j), nodes.get(((k + n) % n) * n + ((l + n) % n)), 0, true);
                            edges.add(edge);
                            edgeSet.add(((EdgeImpl) edge).getLongId());
                        }
                    }
                }
            }
        }

        // Creating edges from each node to q long-range contacts
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                double sum = 0.0;
                for (int k = 0; k < n; ++k) {
                    for (int l = 0; l < n; ++l) {
                        if (!isTorusBased() && d(i, j, k, l) > p) {
                            sum += Math.pow(d(i, j, k, l), -r);
                        } else if (isTorusBased() && dtb(i, j, k, l) > p) {
                            sum += Math.pow(dtb(i, j, k, l), -r);
                        }

                    }
                }
                for (int m = 0; m < q; ++m) {
                    double b = random.nextDouble();
                    boolean e = false;
                    while (!e) {
                        double pki = 0.0;
                        for (int k = 0; k < n && !e; ++k) {
                            for (int l = 0; l < n && !e; ++l) {
                                if (!isTorusBased() && d(i, j, k, l) > p || isTorusBased() && dtb(i, j, k, l) > p) {
                                    pki += Math.pow(!isTorusBased() ? d(i, j, k, l) : dtb(i, j, k, l), -r) / sum;
                                    Edge edge = factory.newEdge(nodes.get(i * n + j), nodes.get(k * n + l), 0, true);
                                    if (b <= pki && !edgeSet.contains(((EdgeImpl) edge).getLongId())) {
                                        edges.add(edge);
                                        edgeSet.add(((EdgeImpl) edge).getLongId());
                                        e = true;
                                    }
                                }
                            }
                        }
                        b = random.nextDouble();
                    }

                }
            }
        }
        return this;
    }

    @Override
    public KleinbergGraph commit() {
        commitInner();
        return this;
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
}
