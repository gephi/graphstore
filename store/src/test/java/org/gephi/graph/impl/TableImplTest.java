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

import java.awt.Color;
import java.util.Arrays;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TableImplTest {

    @Test
    public void testTable() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Assert.assertEquals(table.countColumns(), 0);
    }

    @Test
    public void testAddColumnDefault() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", Integer.class);

        Assert.assertEquals(table.countColumns(), 1);
        Assert.assertEquals(table.getColumn("Id"), col);
        Assert.assertEquals(table.getColumn("id"), col);
    }

    @Test
    public void testAddColumnWithOrigin() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", Integer.class, Origin.PROPERTY);
        Assert.assertEquals(col.getOrigin(), Origin.PROPERTY);
        Assert.assertTrue(col.isProperty());
    }

    @Test
    public void testAddColumnWithTitleAndDefaultValue() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", "Foo", Integer.class, 42);
        Assert.assertEquals(col.getTitle(), "Foo");
        Assert.assertEquals(col.getDefaultValue(), 42);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnknownType() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        table.addColumn("Id", Node.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDefaultValueWrongType() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Float defaultValue = 25f;

        table.addColumn("Id", null, Integer.class, Origin.DATA, defaultValue, false);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testOriginCantBeNull() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);

        table.addColumn("Id", null, Integer.class, null, 0, false);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testOriginCantBeNull2() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);

        table.addColumn("Id", Integer.class, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testTypeClassCantBeNull() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);

        table.addColumn("Id", null, null, Origin.DATA, 0, false);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testTypeClassCantBeNull2() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);

        table.addColumn("Id", null);
    }

    @Test
    public void testIsIndexed() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, true);
        Column col1 = table.addColumn("Id", null, Integer.class, Origin.DATA, null, false);
        Column col2 = table.addColumn("1", null, Integer.class, Origin.DATA, null, true);

        Assert.assertFalse(col1.isIndexed());
        Assert.assertTrue(col2.isIndexed());
    }

    @Test
    public void testGetColumnId() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", Integer.class);

        Column c1 = table.getColumn("Id");
        Assert.assertSame(col, c1);
        Column c2 = table.getColumn("ID");
        Assert.assertSame(col, c2);
    }

    @Test
    public void testGetColumnBadId() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Assert.assertNull(table.getColumn("Id"));
    }

    @Test
    public void testGetColumnIndex() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", Integer.class);

        Column c = table.getColumn(0);
        Assert.assertSame(col, c);
    }

    @Test
    public void testHasColumn() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        table.addColumn("Id", Integer.class);

        Assert.assertTrue(table.hasColumn("Id"));
        Assert.assertTrue(table.hasColumn("ID"));
        Assert.assertTrue(table.hasColumn("id"));
        Assert.assertTrue(table.hasColumn("iD"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetColumnBadIndex() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        table.getColumn(0);
    }

    @Test
    public void testTitleBackFill() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", Integer.class);
        Assert.assertEquals(col.getTitle(), "Id");
    }

    @Test
    public void testIdLowercase() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("A", Integer.class);
        Assert.assertEquals(col.getId(), "a");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNonStandardType() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        table.addColumn("Id", Color.class);
    }

    @Test
    public void testStandardizePrimitiveType() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", int.class);
        Assert.assertEquals(col.getTypeClass(), Integer.class);
    }

    @Test
    public void testStandardizeArrayType() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", Integer[].class);
        Assert.assertEquals(col.getTypeClass(), int[].class);
    }

    @Test
    public void testStandardizeArrayDefaultValue() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Integer[] t = new Integer[] { 1, 2 };

        Column col = table.addColumn("Id", null, Integer[].class, Origin.DATA, t, false);
        Object d = col.getDefaultValue();
        Assert.assertEquals(d.getClass(), int[].class);
        Assert.assertEquals(d, new int[] { 1, 2 });
    }

    @Test
    public void testRemoveColumn() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", Integer.class);

        table.removeColumn(col);
        Assert.assertFalse(table.hasColumn("Id"));
        Assert.assertFalse(table.hasColumn("id"));
    }

    @Test
    public void testRemoveColumnString() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        table.addColumn("Id", Integer.class);

        table.removeColumn("Id");
        Assert.assertFalse(table.hasColumn("Id"));

        // Test case insensitive
        table.addColumn("Id", Integer.class);
        table.removeColumn("id");
        Assert.assertFalse(table.hasColumn("Id"));
    }

    @Test
    public void testCountColumns() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        table.addColumn("Id", Integer.class);

        table.removeColumn("Id");
        table.addColumn("Id", Integer.class);
        Assert.assertEquals(table.countColumns(), 1);
    }

    @Test
    public void testGetElementClass() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Assert.assertEquals(table.getElementClass(), Node.class);
    }

    @Test
    public void testToArray() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", Integer.class);
        Assert.assertEquals(table.toArray(), new Column[] { col });
    }

    @Test
    public void testToList() {
        TableImpl<Node> table = new TableImpl<Node>(Node.class, false);
        Column col = table.addColumn("Id", Integer.class);
        Assert.assertEquals(table.toList(), Arrays.asList(new Column[] { col }));
    }

    @Test
    public void testDeepEquals() {
        TableImpl<Node> table1 = new TableImpl<Node>(Node.class, false);
        table1.addColumn("Id", Integer.class);

        TableImpl<Node> table2 = new TableImpl<Node>(Node.class, false);
        table2.addColumn("Id", Integer.class);

        Assert.assertTrue(table1.deepEquals(table2));
    }

    @Test
    public void testDeepHashCode() {
        TableImpl<Node> table1 = new TableImpl<Node>(Node.class, false);
        table1.addColumn("Id", Integer.class);

        TableImpl<Node> table2 = new TableImpl<Node>(Node.class, false);
        table2.addColumn("Id", Integer.class);

        Assert.assertEquals(table1.deepHashCode(), table2.deepHashCode());
    }
}
