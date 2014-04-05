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

import org.gephi.nanobench.NanoBench;
import org.testng.annotations.Test;

public class EdgeStoreBenchmarkTest {

    @Test
    public void testPushStore() {
        int[] n = {100, 1000, 5000};
        double[] p = {0.01, 0.1, 0.3};
        for (int nodes : n) {
            for (double prob : p) {
                int edges = (int) (nodes * (nodes - 1) * prob);
                NanoBench.create().measurements(2).measure("push edge store nodes=" + nodes + " edges=" + edges, new EdgeStoreBenchmark().pushEdgeStore(nodes, prob));
            }
        }
    }

    @Test
    public void testIterateStore() {
        int[] n = {100, 1000, 5000};
        double[] p = {0.01, 0.1, 0.3};
        for (int nodes : n) {
            for (double prob : p) {
                int edges = (int) (nodes * (nodes - 1) * prob);
                NanoBench.create().measurements(2).measure("iterate edge store nodes=" + nodes + " edges=" + edges, new EdgeStoreBenchmark().iterateEdgeStore(nodes, prob));
            }
        }
    }

    @Test
    public void testIterateOutNeighbors() {
        int[] n = {100, 1000, 5000};
        double[] p = {0.01, 0.1, 0.3};
        for (int nodes : n) {
            for (double prob : p) {
                int edges = (int) (nodes * (nodes - 1) * prob);
                NanoBench.create().measurements(2).measure("iterate neighbors list out nodes=" + nodes + " edges=" + edges, new EdgeStoreBenchmark().iterateEdgeStoreNeighborsOut(nodes, prob));
            }
        }
    }

    @Test
    public void testIterateInOutNeighbors() {
        int[] n = {100, 1000, 5000};
        double[] p = {0.01, 0.1, 0.3};
        for (int nodes : n) {
            for (double prob : p) {
                int edges = (int) (nodes * (nodes - 1) * prob);
                NanoBench.create().measurements(2).measure("iterate neighbors list in&out nodes=" + nodes + " edges=" + edges, new EdgeStoreBenchmark().iterateEdgeStoreNeighborsInOut(nodes, prob));
            }
        }
    }

    @Test
    public void testResetEdgeStore() {
        int[] n = {100, 1000, 5000};
        double[] p = {0.01, 0.1, 0.3};
        for (int nodes : n) {
            for (double prob : p) {
                int edges = (int) (nodes * (nodes - 1) * prob);
                NanoBench.create().measurements(2).measure("reset edge store nodes=" + nodes + " edges=" + edges, new EdgeStoreBenchmark().resetEdgeStore(nodes, prob));
            }
        }
    }
}
