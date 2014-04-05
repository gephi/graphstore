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

public class NodeStoreBenchmarkTest {

    @Test
    public void testPushStore() {
        int[] n = {100, 1000, 10000, 100000};
        for (int nodes : n) {
            NanoBench.create().measurements(10).measure("push node store " + nodes, new NodeStoreBenchmark().pushStore(nodes));
        }
    }

    @Test
    public void testIterateStore() {
        int[] n = {100, 1000, 10000, 100000};
        for (int nodes : n) {
            NanoBench.create().cpuOnly().measurements(10).measure("iterate node store " + nodes, new NodeStoreBenchmark().iterateStore(nodes));
        }
    }

    @Test
    public void testResetNodeStore() {
        int[] n = {100, 1000, 10000, 100000};
        for (int nodes : n) {
            NanoBench.create().measurements(10).measure("reset node store "+nodes, new NodeStoreBenchmark().resetNodeStore(nodes));
        }
    }
}
