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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.benchmark.util.ReporterHandler;
import org.gephi.nanobench.NanoBench;
import org.testng.Reporter;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ControlBenchmarkTest {

    @BeforeSuite
    public void setUp() {
        Logger logger = NanoBench.getLogger();
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.INFO);
        logger.addHandler(new ReporterHandler());
        Reporter.setEscapeHtml(false);
    }

    @Test
    public void testControl() {
        Runnable runnable = new Runnable() {
            final int[] array = new int[10000000];
            int m = 0;

            @Override
            public void run() {
                int dummy = 0;
                for (int doNotIgnoreMe : array) {
                    dummy += doNotIgnoreMe;
                }
                m += dummy;
            }
        };
        NanoBench.create().measure("control", runnable);
    }
}
