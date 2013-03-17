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

import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TableImplTest {

    @Test
    public void testTable() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Assert.assertEquals(table.countColumns(), 0);
    }

    @Test
    public void testAddColumnDefault() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Column col = table.addColumn("0", Integer.class);

        Assert.assertEquals(table.countColumns(), 1);
        Assert.assertEquals(table.getColumn("0"), col);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnknownType() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        table.addColumn("0", Node.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDefaultValueWrongType() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Float defaultValue = 25f;

        table.addColumn("0", null, Integer.class, Origin.DATA, defaultValue, false);
    }
}
