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

/**
 *
 * @author mbastian
 */
public class EdgeStoreBenchmarkTest {

    @Test
    public void testPushStore() {
        NanoBench.create().measurements(20).measure("push edge store", new EdgeStoreBenchmark().pushEdgeStore());
    }

    @Test
    public void testIterateStore() {
        NanoBench.create().cpuOnly().measurements(300).measure("iterate edge store", new EdgeStoreBenchmark().iterateEdgeStore());
    }

    public void testIterateStoreWithLocking() {
        NanoBench.create().cpuOnly().measurements(300).measure("iterate edge store with locking", new EdgeStoreBenchmark().iterateEdgeStoreWithLocking());
    }

    @Test
    public void testIterateOutNeighbors() {
        NanoBench.create().cpuOnly().measurements(100).measure("iterate neighbors list out", new EdgeStoreBenchmark().iterateEdgeStoreNeighborsOut());
    }

    @Test
    public void testIterateInOutNeighbors() {
        NanoBench.create().cpuOnly().measurements(100).measure("iterate neighbors list in & out", new EdgeStoreBenchmark().iterateEdgeStoreNeighborsInOut());
    }

    @Test
    public void testResetEdgeStore() {
        NanoBench.create().cpuOnly().measurements(20).measure("reset edge store", new EdgeStoreBenchmark().resetEdgeStore());
    }
}
