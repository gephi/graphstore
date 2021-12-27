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

import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IndexImplTest {

    @Test
    public void testIndexName() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        Assert.assertEquals(index.getIndexClass(), Node.class);
        Assert.assertEquals(index.getIndexName(), "index_" + Node.class.getCanonicalName());
    }

    @Test
    public void testAddColumn() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        ColumnImpl col = new ColumnImpl("foo", String.class, "foo", null, Origin.DATA, true, false);
        col.setStoreId(0);

        Assert.assertEquals(index.size(), GraphStoreConfiguration.NODE_DEFAULT_COLUMNS);
        index.addColumn(col);
        Assert.assertEquals(index.size(), 1 + GraphStoreConfiguration.NODE_DEFAULT_COLUMNS);
        Assert.assertSame(index.getIndex(col).getColumn(), col);
    }

    @Test
    public void testHasColumn() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        ColumnImpl col1 = new ColumnImpl("foo", String.class, "foo", null, Origin.DATA, true, false);
        ColumnImpl col2 = new ColumnImpl("bar", String.class, "bar", null, Origin.DATA, false, false);
        col1.setStoreId(0);
        col2.setStoreId(1);

        Assert.assertFalse(index.hasColumn(col1));
        index.addColumn(col1);
        index.addColumn(col2);
        Assert.assertTrue(index.hasColumn(col1));
        Assert.assertTrue(index.hasColumn(col2));
    }

    @Test
    public void testHasColumnDifferentIndex() {
        ColumnStore<Node> columnStore1 = generateEmptyNodeStore();
        IndexImpl<Node> index1 = columnStore1.indexStore.mainIndex;

        ColumnStore<Node> columnStore2 = generateEmptyNodeStore();
        IndexImpl<Node> index2 = columnStore2.indexStore.mainIndex;

        ColumnImpl col1 = new ColumnImpl("foo", String.class, "foo", null, Origin.DATA, true, false);
        ColumnImpl col2 = new ColumnImpl("bar", String.class, "bar", null, Origin.DATA, true, false);
        col1.setStoreId(0);
        col2.setStoreId(0);

        index1.addColumn(col1);
        index2.addColumn(col2);
        Assert.assertFalse(index1.hasColumn(col2));
        Assert.assertFalse(index2.hasColumn(col1));
    }

    @Test
    public void testAddAllColumns() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        ColumnImpl col1 = new ColumnImpl("1", String.class, "1", null, Origin.DATA, true, false);
        ColumnImpl col2 = new ColumnImpl("2", String.class, "2", null, Origin.DATA, false, false);
        ColumnImpl col3 = new ColumnImpl("3", String.class, "3", null, Origin.DATA, true, false);
        col1.setStoreId(0);
        col2.setStoreId(1);
        col3.setStoreId(2);

        index.addAllColumns(new ColumnImpl[] { col1, col2, col3 });
        Assert.assertEquals(index.size(), 3 + GraphStoreConfiguration.NODE_DEFAULT_COLUMNS);
    }

    @Test
    public void testDestroy() {
        ColumnStore<Node> columnStore = generateEmptyNodeStore();
        IndexImpl<Node> index = columnStore.indexStore.mainIndex;
        ColumnImpl col1 = new ColumnImpl("1", String.class, "1", null, Origin.DATA, true, false);
        ColumnImpl col2 = new ColumnImpl("2", String.class, "2", null, Origin.DATA, false, false);
        col1.setStoreId(0);
        col2.setStoreId(1);

        index.addAllColumns(new ColumnImpl[] { col1, col2 });
        index.destroy();
        Assert.assertEquals(index.size(), 0);
        Assert.assertNull(index.getIndex(col1));
        Assert.assertNull(index.getIndex(col2));
    }

    private ColumnStore<Node> generateEmptyNodeStore() {
        GraphStore graphStore = new GraphStore();
        ColumnStore<Node> columnStore = graphStore.nodeTable.store;
        return columnStore;
    }
}
