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
public class NodeStoreBenchmarkTest {

    @Test
    public void testPushStore() {
        NanoBench.create().measurements(100).measure("push node store", new NodeStoreBenchmark().pushStore());
    }

    @Test
    public void testIterateStore() {
        NanoBench.create().cpuOnly().measurements(100).measure("iterate node store", new NodeStoreBenchmark().iterateStore());
    }

    @Test
    public void testResetNodeStore() {
        NanoBench.create().measurements(100).measure("reset node store", new NodeStoreBenchmark().resetNodeStore());
    }
}
