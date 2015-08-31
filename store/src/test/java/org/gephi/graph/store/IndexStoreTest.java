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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class IndexStoreTest {

    @Test
    public void testEmpty() {
        GraphStore graphStore = new GraphStore();
        ColumnStore<Node> columnStore = graphStore.nodeColumnStore;
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;
        Assert.assertEquals(mainIndex.size(), 0);
    }

    @Test
    public void testAddColumn() {
        ColumnStore<Node> store = generateEmptyNodeStore();
        IndexStore<Node> indexStore = store.indexStore;
        ColumnImpl col = generateFooColumn();
        store.addColumn(col);

        Assert.assertTrue(col.storeId != ColumnStore.NULL_ID);
        Assert.assertTrue(indexStore.hasColumn(col));
    }

    @Test
    public void testRemoveColumn() {
        ColumnStore<Node> store = generateEmptyNodeStore();
        IndexStore<Node> indexStore = store.indexStore;
        ColumnImpl col = generateFooColumn();
        store.addColumn(col);
        store.removeColumn(col);

        Assert.assertTrue(col.storeId == ColumnStore.NULL_ID);
        Assert.assertFalse(indexStore.hasColumn(col));
    }

    @Test
    public void testGetMainIndex() {
        GraphStore graphStore = new GraphStore();
        IndexStore<Node> indexStore = graphStore.nodeColumnStore.indexStore;

        Assert.assertNotNull(indexStore.getIndex(graphStore));
    }

    @Test
    public void testIndexNodeNull() {
        IndexStore<Node> indexStore = generateBasicNodeColumnStore().indexStore;
        NodeImpl n = new NodeImpl("0");
        indexStore.index(n);

        Assert.assertEquals(n.attributes.length, indexStore.columnStore.length);
    }

    @Test
    public void testIndexNode() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col1 = columnStore.getColumn("foo");
        Column col2 = columnStore.getColumn("age");

        NodeImpl n = new NodeImpl("0");
        n.setAttribute(col1, "A");
        n.setAttribute(col2, 20);
        indexStore.index(n);

        Assert.assertEquals(mainIndex.count(col1, "A"), 1);
        Assert.assertEquals(mainIndex.count("foo", "A"), 1);
        Assert.assertEquals(mainIndex.count(col2, 20), 1);
        Assert.assertEquals(mainIndex.count("age", 20), 1);
        Assert.assertEquals(mainIndex.countElements(col1), 1);
        Assert.assertEquals(mainIndex.countElements(col2), 1);
        Assert.assertEquals(mainIndex.countValues(col1), 1);
        Assert.assertEquals(mainIndex.countValues(col2), 1);

        Assert.assertTrue(mainIndex.values(col1).contains("A"));
        Assert.assertTrue(mainIndex.values(col2).contains(20));

        Assert.assertSame(getIterable(mainIndex.get(col1, "A"))[0], n);
        Assert.assertNull(mainIndex.get(col1, "B"));

        Assert.assertSame(getIterable(mainIndex.get("foo", "A"))[0], n);
        Assert.assertNull(mainIndex.get("foo", "B"));
    }

    @Test
    public void testIndexNodeNullValue() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col1 = columnStore.getColumn("foo");
        Column col2 = columnStore.getColumn("age");

        NodeImpl n = new NodeImpl("0");
        n.setAttribute(col1, null);
        n.setAttribute(col2, null);
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

        Column col1 = columnStore.getColumn("foo");
        Column col2 = columnStore.getColumn("age");

        NodeImpl n = new NodeImpl("0");
        n.setAttribute(col1, "A");
        n.setAttribute(col2, 20);
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

        Column col1 = columnStore.getColumn("foo");
        Column col2 = columnStore.getColumn("age");

        NodeImpl n = new NodeImpl("0");
        n.setAttribute(col1, null);
        n.setAttribute(col2, null);
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

        Column col1 = columnStore.getColumn("foo");
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
    public void testClear() {
        ColumnStore<Node> columnStore = generateBasicNodeColumnStore();
        IndexStore<Node> indexStore = columnStore.indexStore;
        IndexImpl<Node> mainIndex = indexStore.mainIndex;

        Column col = columnStore.getColumn("age");

        NodeImpl n1 = new NodeImpl("0");
        n1.setAttribute(col, 1);
        NodeImpl n2 = new NodeImpl("1");
        n2.setAttribute(col, 5);

        indexStore.index(n1);
        indexStore.index(n2);

        indexStore.clear();

        Assert.assertEquals(mainIndex.count(col, 1), 0);
        Assert.assertTrue(mainIndex.values(col).isEmpty());
    }

    @Test
    public void testCreateViewIndex() {
        GraphStore graphStore = generateBasicGraphStoreWithColumns();
        IndexStore<Node> indexStore = graphStore.nodeColumnStore.indexStore;
        GraphView view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        IndexImpl index = indexStore.createViewIndex(graph);
        Assert.assertSame(indexStore.getIndex(graph), index);
        for (ColumnImpl col : graphStore.nodeColumnStore.toArray()) {
            if (col.isIndexed()) {
                Assert.assertTrue(index.hasColumn(col));
            }
        }
    }

    @Test
    public void testGetIndexWithView() {
        GraphStore graphStore = generateBasicGraphStoreWithColumns();
        IndexStore<Node> indexStore = graphStore.nodeColumnStore.indexStore;
        GraphView view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        IndexImpl index = indexStore.getIndex(graph);
        Assert.assertNotNull(index);
        Assert.assertSame(indexStore.getIndex(graph), index);
        for (ColumnImpl col : graphStore.nodeColumnStore.toArray()) {
            if (col.isIndexed()) {
                Assert.assertTrue(index.hasColumn(col));
            }
        }
    }

    @Test
    public void testCreateViewIndexWithElements() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        IndexStore<Node> indexStore = graphStore.nodeColumnStore.indexStore;
        ColumnImpl column = new ColumnImpl("foo", String.class, "Foo", null, Origin.DATA, true, false);
        graphStore.nodeColumnStore.addColumn(column);
        NodeImpl n1 = graphStore.getNode("1");
        n1.setAttribute(column, "bar");

        GraphViewImpl view = graphStore.viewStore.createView();
        view.fill();
        Graph graph = graphStore.viewStore.getGraph(view);
        IndexImpl index = indexStore.createViewIndex(graph);
        Assert.assertEquals(index.countElements(column), 2);
        Assert.assertEquals(index.countValues(column), 2);
        Assert.assertEquals(index.count(column, "bar"), 1);
        Assert.assertEquals(index.count(column, null), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateViewIndexMainView() {
        GraphStore graphStore = new GraphStore();
        graphStore.nodeColumnStore.indexStore.createViewIndex(graphStore);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDeleteViewIndexMainView() {
        GraphStore graphStore = new GraphStore();
        graphStore.nodeColumnStore.indexStore.deleteViewIndex(graphStore);
    }

    @Test
    public void testDeleteViewIndex() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        IndexStore<Node> indexStore = graphStore.nodeColumnStore.indexStore;
        ColumnImpl column = new ColumnImpl("foo", String.class, "Foo", null, Origin.DATA, true, false);
        graphStore.nodeColumnStore.addColumn(column);
        NodeImpl n1 = graphStore.getNode("1");
        n1.setAttribute(column, "bar");

        GraphViewImpl view = graphStore.viewStore.createView();
        view.fill();
        Graph graph = graphStore.viewStore.getGraph(view);
        IndexImpl index = indexStore.createViewIndex(graph);
        indexStore.deleteViewIndex(graph);
        Assert.assertFalse(index.hasColumn(column));
    }

    @Test
    public void testAddColumnWithView() {
        GraphStore graphStore = new GraphStore();
        GraphView view = graphStore.viewStore.createView();
        IndexImpl index = graphStore.nodeColumnStore.indexStore.createViewIndex(graphStore.viewStore.getGraph(view));
        ColumnImpl column = new ColumnImpl("foo", String.class, "Foo", null, Origin.DATA, true, false);
        graphStore.nodeColumnStore.addColumn(column);
        Assert.assertTrue(index.hasColumn(column));
    }

    @Test
    public void testRemoveColumnWithView() {
        GraphStore graphStore = new GraphStore();
        GraphView view = graphStore.viewStore.createView();
        ColumnImpl column = new ColumnImpl("foo", String.class, "Foo", null, Origin.DATA, true, false);
        graphStore.nodeColumnStore.addColumn(column);

        IndexImpl index = graphStore.nodeColumnStore.indexStore.createViewIndex(graphStore.viewStore.getGraph(view));
        graphStore.nodeColumnStore.removeColumn(column);
        Assert.assertFalse(index.hasColumn(column));
    }

    @Test
    public void testSetWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        NodeImpl n1 = graphStore.getNode("1");
        NodeImpl n2 = graphStore.getNode("2");

        GraphView view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        graph.addNode(n1);

        IndexStore<Node> indexStore = graphStore.nodeColumnStore.indexStore;
        ColumnImpl column = new ColumnImpl("foo", String.class, "Foo", null, Origin.DATA, true, false);
        graphStore.nodeColumnStore.addColumn(column);

        IndexImpl index = indexStore.createViewIndex(graphStore.viewStore.getGraph(view));

        n1.setAttribute(column, "foo");
        n2.setAttribute(column, "bar");

        Assert.assertEquals(index.countElements(column), 1);
        Assert.assertEquals(index.countValues(column), 1);
        Assert.assertEquals(index.count(column, "foo"), 1);

        n1.removeAttribute(column);

        Assert.assertEquals(index.countElements(column), 1);
        Assert.assertEquals(index.countValues(column), 1);
        Assert.assertEquals(index.count(column, null), 1);
    }

    @Test
    public void testIndexWithView() {
        GraphStore graphStore = generateBasicGraphStoreWithColumns();
        GraphView view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);

        IndexStore<Node> indexStore = graphStore.nodeColumnStore.indexStore;
        IndexImpl index = indexStore.createViewIndex(graphStore.viewStore.getGraph(view));

        Column column = graphStore.nodeColumnStore.getColumn("foo");
        Node n1 = graphStore.factory.newNode("1");
        n1.setAttribute(column, "bar");
        graphStore.addNode(n1);
        graph.addNode(n1);

        Assert.assertEquals(index.countElements(column), 1);
        Assert.assertEquals(index.countValues(column), 1);
        Assert.assertEquals(index.count(column, "bar"), 1);
    }

    @Test
    public void testClearWithView() {
        GraphStore graphStore = generateBasicGraphStoreWithColumns();
        GraphView view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);

        Column column = graphStore.nodeColumnStore.getColumn("foo");
        Node n1 = graphStore.factory.newNode("1");
        n1.setAttribute(column, "bar");
        graphStore.addNode(n1);
        graph.addNode(n1);

        IndexStore<Node> indexStore = graphStore.nodeColumnStore.indexStore;
        IndexImpl index = indexStore.createViewIndex(graphStore.viewStore.getGraph(view));

        graphStore.removeNode(n1);

        Assert.assertEquals(index.countElements(column), 0);
        Assert.assertEquals(index.countValues(column), 0);
        Assert.assertEquals(index.count(column, "bar"), 0);
    }

    @Test
    public void testClearInView() {
        GraphStore graphStore = generateBasicGraphStoreWithColumns();
        GraphView view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);

        Column column = graphStore.nodeColumnStore.getColumn("foo");
        Node n1 = graphStore.factory.newNode("1");
        n1.setAttribute(column, "bar");
        graphStore.addNode(n1);
        graph.addNode(n1);

        IndexStore<Node> indexStore = graphStore.nodeColumnStore.indexStore;
        IndexImpl index = indexStore.createViewIndex(graphStore.viewStore.getGraph(view));

        graph.removeNode(n1);

        Assert.assertEquals(index.countElements(column), 0);
        Assert.assertEquals(index.countValues(column), 0);
        Assert.assertEquals(index.count(column, "bar"), 0);
    }

    //UTILITY
    private NodeImpl[] generateNodesWithUniqueAttributes(ColumnStore<Node> columnStore) {
        int count = 100;
        NodeImpl[] nodes = new NodeImpl[count];
        for (int i = 0; i < 100; i++) {
            NodeImpl n = new NodeImpl(i);
            nodes[i] = n;

            for (Column col : columnStore) {
                if (col.getTypeClass().equals(String.class)) {
                    n.setAttribute(col, "" + i);
                } else if (col.getTypeClass().equals(Integer.class)) {
                    n.setAttribute(col, i);
                }
            }
        }
        return nodes;
    }

    private GraphStore generateBasicGraphStoreWithColumns() {
        GraphStore graphStore = new GraphStore();
        ColumnStore<Node> columnStore = graphStore.nodeColumnStore;
        columnStore.addColumn(new ColumnImpl("foo", String.class, "Foo", null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("age", Integer.class, "Age", null, Origin.DATA, true, false));
        return graphStore;
    }

    private ColumnStore<Node> generateBasicNodeColumnStore() {
        GraphStore graphStore = new GraphStore();
        ColumnStore<Node> columnStore = graphStore.nodeColumnStore;
        columnStore.addColumn(new ColumnImpl("foo", String.class, "Foo", null, Origin.DATA, true, false));
        columnStore.addColumn(new ColumnImpl("age", Integer.class, "Age", null, Origin.DATA, true, false));
        return columnStore;
    }

    private ColumnStore<Node> generateEmptyNodeStore() {
        GraphStore graphStore = new GraphStore();
        ColumnStore<Node> columnStore = graphStore.nodeColumnStore;
        return columnStore;
    }

    private ColumnImpl generateFooColumn() {
        return new ColumnImpl("foo", String.class, "FOO", null, Origin.DATA, true, false);
    }

    private Node[] getIterable(Iterable<Node> itr) {
        List<Node> list = new ArrayList<Node>();
        for (Node n : itr) {
            list.add(n);
        }
        return list.toArray(new Node[0]);
    }
}
