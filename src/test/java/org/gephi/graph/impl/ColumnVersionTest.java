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
package org.gephi.graph.impl;

import org.gephi.graph.api.Origin;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ColumnVersionTest {

    @Test
    public void testColumnVersion() {
        ColumnImpl col = new ColumnImpl("c1", int[].class, null, null, Origin.DATA, true, false);
        ColumnVersion columnVersion = new ColumnVersion(col);
        Assert.assertSame(columnVersion.column, col);
        Assert.assertEquals(columnVersion.version.get(), Integer.MIN_VALUE);
        Assert.assertEquals(columnVersion.incrementAndGetVersion(), Integer.MIN_VALUE + 1l);
        columnVersion.version.set(Integer.MAX_VALUE);
        Assert.assertEquals(columnVersion.incrementAndGetVersion(), Integer.MIN_VALUE);
    }
}
