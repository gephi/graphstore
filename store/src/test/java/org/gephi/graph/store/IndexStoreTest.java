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

import java.util.ArrayList;
import java.util.List;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class IndexStoreTest {

    @Test
    public void testAddColumn() {
        ColumnStore<Node> store = generateEmptyNodeStore();
        IndexStore<Node> indexStore = store.indexStore;
        ColumnImpl col = generateIdColumn();
        store.addColumn(col);

        Assert.assertTrue(col.storeId != ColumnStore.NULL_ID);
        Assert.assertTrue(indexStore.hasColumn(col));
    }

    @Test
    public void testRemoveColumn() {
        ColumnStore<Node> store = generateEmptyNodeStore();
        IndexStore<Node> indexStore = store.indexStore;
        ColumnImpl col = generateIdColumn();
        store.addColumn(col);
        store.removeColumn(col);

        Assert.assertTrue(col.storeId == ColumnStore.NULL_ID);
        Assert.assertFalse(indexStore.hasColumn(col));
    }

    @Test
    public void testGetMainIndex() {
        GraphStore graphStore = new GraphStore();
        IndexStore<Node> indexStore = graphStore.nodePropertyStore.indexStore;

        Assert.assertNotNull(indexStore.getIndex(graphStore));
    }

    @Test
    public void testIndexNodeNull() {
        IndexStore<Node> indexStore = generateBasicNodeColumnStore().indexStore;
        NodeImpl n = new NodeImpl("0");
        indexStore.index(n);

        Assert.assertEquals(n.properties.length, indexStore.propertyStore.length);
    }

    @Test
    public void testIndexNode() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col1 = columnStore.getColumn("id");
        Column col2 = columnStore.getColumn("age");

        NodeImpl n = new NodeImpl("0");
        n.setProperty(col1, "A");
        n.setProperty(col2, 20);
        indexStore.index(n);

        Assert.assertEquals(mainIndex.count(col1, "A"), 1);
        Assert.assertEquals(mainIndex.count(col2, 20), 1);
        Assert.assertEquals(mainIndex.countElements(col1), 1);
        Assert.assertEquals(mainIndex.countElements(col2), 1);
        Assert.assertEquals(mainIndex.countValues(col1), 1);
        Assert.assertEquals(mainIndex.countValues(col2), 1);

        Assert.assertTrue(mainIndex.values(col1).contains("A"));
        Assert.assertTrue(mainIndex.values(col2).contains(20));

        Assert.assertSame(getIterable(mainIndex.get(col1, "A"))[0], n);
    }

    @Test
    public void testIndexNodeNullValue() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col1 = columnStore.getColumn("id");
        Column col2 = columnStore.getColumn("age");

        NodeImpl n = new NodeImpl("0");
        n.setProperty(col1, null);
        n.setProperty(col2, null);
        indexStore.index(n);

        Assert.assertEquals(mainIndex.count(col1, null), 1);
        Assert.assertEquals(mainIndex.count(col2, null), 1);
        Assert.assertEquals(mainIndex.countElements(col1), 1);
        Assert.assertEquals(mainIndex.countElements(col2), 1);
        Assert.assertEquals(mainIndex.countValues(col1), 1);
        Assert.assertEquals(mainIndex.countValues(col2), 1);

        Assert.assertTrue(mainIndex.values(col1).contains(null));
        Assert.assertTrue(mainIndex.values(col2).contains(null));

        Assert.assertSame(getIterable(mainIndex.get(col1, null))[0], n);
    }

    @Test
    public void testClearNode() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col1 = columnStore.getColumn("id");
        Column col2 = columnStore.getColumn("age");

        NodeImpl n = new NodeImpl("0");
        n.setProperty(col1, "A");
        n.setProperty(col2, 20);
        indexStore.index(n);
        indexStore.clear(n);

        Assert.assertEquals(mainIndex.count(col1, "A"), 0);
        Assert.assertEquals(mainIndex.count(col2, 20), 0);
        Assert.assertEquals(mainIndex.countElements(col1), 0);
        Assert.assertEquals(mainIndex.countElements(col2), 0);
        Assert.assertEquals(mainIndex.countValues(col1), 0);
        Assert.assertEquals(mainIndex.countValues(col2), 0);

        Assert.assertFalse(mainIndex.values(col1).contains("A"));
        Assert.assertFalse(mainIndex.values(col2).contains(20));
    }

    @Test
    public void testClearNodeNullValue() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col1 = columnStore.getColumn("id");
        Column col2 = columnStore.getColumn("age");

        NodeImpl n = new NodeImpl("0");
        n.setProperty(col1, null);
        n.setProperty(col2, null);
        indexStore.index(n);
        indexStore.clear(n);

        Assert.assertEquals(mainIndex.count(col1, null), 0);
        Assert.assertEquals(mainIndex.count(col2, null), 0);
        Assert.assertEquals(mainIndex.countElements(col1), 0);
        Assert.assertEquals(mainIndex.countElements(col2), 0);
        Assert.assertEquals(mainIndex.countValues(col1), 0);
        Assert.assertEquals(mainIndex.countValues(col2), 0);

        Assert.assertFalse(mainIndex.values(col1).contains(null));
        Assert.assertFalse(mainIndex.values(col2).contains(null));
    }

    @Test
    public void testIndexNodeUnique() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col1 = columnStore.getColumn("id");
        Column col2 = columnStore.getColumn("age");

        NodeImpl[] nodes = generateNodesWithUniqueAttributes(columnStore);
        for (NodeImpl n : nodes) {
            indexStore.index(n);
        }

        Assert.assertEquals(mainIndex.countElements(col1), nodes.length);
        Assert.assertEquals(mainIndex.countElements(col2), nodes.length);
        Assert.assertEquals(mainIndex.countValues(col1), nodes.length);
        Assert.assertEquals(mainIndex.countValues(col2), nodes.length);
        Assert.assertEquals(mainIndex.values(col1).size(), nodes.length);
        Assert.assertEquals(mainIndex.values(col2).size(), nodes.length);
    }

    @Test
    public void testMinMaxValue() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col = columnStore.getColumn("age");

        NodeImpl n1 = new NodeImpl("0");
        n1.setProperty(col, 1);
        NodeImpl n2 = new NodeImpl("1");
        n2.setProperty(col, 5);

        indexStore.index(n1);
        indexStore.index(n2);

        Assert.assertEquals(mainIndex.getMinValue(col), 1);
        Assert.assertEquals(mainIndex.getMaxValue(col), 5);
    }

    @Test
    public void testClear() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col = columnStore.getColumn("age");

        NodeImpl n1 = new NodeImpl("0");
        n1.setProperty(col, 1);
        NodeImpl n2 = new NodeImpl("1");
        n2.setProperty(col, 5);

        indexStore.index(n1);
        indexStore.index(n2);

        indexStore.clear();

        Assert.assertEquals(mainIndex.count(col, 1), 0);
        Assert.assertTrue(mainIndex.values(col).isEmpty());
    }

    //UTILITY
    private NodeImpl[] generateNodesWithUniqueAttributes(ColumnStore<Node> columnStore) {
        int count = 100;
        NodeImpl[] nodes = new NodeImpl[count];
        for (int i = 0; i < 100; i++) {
            NodeImpl n = new NodeImpl(i);
            nodes[i] = n;

            for (ColumnImpl col : columnStore) {
                if (col.typeClass.equals(String.class)) {
                    n.setProperty(col, "" + i);
                } else if (col.typeClass.equals(Integer.class)) {
                    n.setProperty(col, i);
                }
            }
        }
        return nodes;
    }

    private ColumnStore<Node> generateBasicNodeColumnStore() {
        GraphStore graphStore = new GraphStore();
        ColumnStore<Node> columnStore = graphStore.nodePropertyStore;
        columnStore.addColumn(new ColumnImpl("id", String.class, "ID", null, Origin.DATA, true));
        columnStore.addColumn(new ColumnImpl("age", Integer.class, "Age", null, Origin.DATA, true));
        return columnStore;
    }

    private ColumnStore<Node> generateEmptyNodeStore() {
        GraphStore graphStore = new GraphStore();
        ColumnStore<Node> columnStore = graphStore.nodePropertyStore;
        return columnStore;
    }

    private ColumnImpl generateIdColumn() {
        return new ColumnImpl("id", String.class, "ID", null, Origin.DATA, true);
    }

    private Node[] getIterable(Iterable<Node> itr) {
        List<Node> list = new ArrayList<Node>();
        for (Node n : itr) {
            list.add(n);
        }
        return list.toArray(new Node[0]);
    }
}
