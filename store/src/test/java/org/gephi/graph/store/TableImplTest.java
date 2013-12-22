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

import java.awt.Color;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.gephi.attribute.time.Estimator;
import org.gephi.attribute.time.TimestampByteSet;
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

    @Test
    public void testIsIndexed() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, true));
        Column col1 = table.addColumn("0", null, Integer.class, Origin.DATA, null, false);
        Column col2 = table.addColumn("1", null, Integer.class, Origin.DATA, null, true);

        Assert.assertFalse(col1.isIndexed());
        Assert.assertTrue(col2.isIndexed());
    }

    @Test
    public void testGetColumnId() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Column col = table.addColumn("0", Integer.class);

        Column c = table.getColumn("0");
        Assert.assertSame(col, c);
    }

    @Test
    public void testGetColumnBadId() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Assert.assertNull(table.getColumn("0"));
    }

    @Test
    public void testGetColumnIndex() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Column col = table.addColumn("0", Integer.class);

        Column c = table.getColumn(0);
        Assert.assertSame(col, c);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetColumnBadIndex() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        table.getColumn(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetEstimatorStaticType() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Column col = table.addColumn("0", Integer.class);
        Estimator est = Estimator.AVERAGE;
        table.setEstimator(col, est);
    }

    @Test
    public void testSetEstimator() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Column col = table.addColumn("0", TimestampByteSet.class);
        Estimator est = Estimator.AVERAGE;
        table.setEstimator(col, est);

        Assert.assertEquals(table.getEstimator(col), est);
    }

    @Test
    public void testTitleBackFill() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Column col = table.addColumn("0", Integer.class);
        Assert.assertEquals(col.getTitle(), "0");
    }

    @Test
    public void testIdLowercase() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Column col = table.addColumn("A", Integer.class);
        Assert.assertEquals(col.getId(), "a");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNonStandardType() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        table.addColumn("0", Color.class);
    }

    @Test
    public void testStandardizePrimitiveType() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Column col = table.addColumn("0", int.class);
        Assert.assertEquals(col.getTypeClass(), Integer.class);
    }

    @Test
    public void testStandardizeArrayType() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Column col = table.addColumn("0", Integer[].class);
        Assert.assertEquals(col.getTypeClass(), int[].class);
    }

    @Test
    public void testStandardizeArrayDefaultValue() {
        TableImpl<Node> table = new TableImpl<Node>(new ColumnStore<Node>(Node.class, false));
        Integer[] t = new Integer[]{1, 2};

        Column col = table.addColumn("0", null, Integer[].class, Origin.DATA, t, false);
        Object d = col.getDefaultValue();
        Assert.assertEquals(d.getClass(), int[].class);
        Assert.assertEquals(d, new int[]{1, 2});
    }
}
