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
