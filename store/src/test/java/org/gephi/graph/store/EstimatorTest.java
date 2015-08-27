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
package org.gephi.graph.store;

import org.gephi.attribute.time.Estimator;
import org.junit.Assert;
import org.testng.annotations.Test;

public class EstimatorTest {

    @Test
    public void testEstimatorIs() {
        Estimator e = Estimator.AVERAGE;
        Assert.assertTrue(e.is(Estimator.AVERAGE));
        Assert.assertFalse(e.is(Estimator.MAX));
    }

    @Test
    public void testEstimatorIsAll() {
        Estimator e = Estimator.AVERAGE;
        Assert.assertTrue(e.is(Estimator.AVERAGE));
        Assert.assertTrue(e.is(Estimator.LAST, Estimator.AVERAGE));
        Assert.assertFalse(e.is(Estimator.MAX));
    }

    @Test
    public void testEstimatorIsAllNone() {
        Estimator e = Estimator.FIRST;
        Assert.assertFalse(e.is(Estimator.LAST, Estimator.AVERAGE));
    }
}
