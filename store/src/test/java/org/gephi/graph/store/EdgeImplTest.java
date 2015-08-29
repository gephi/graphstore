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
import org.gephi.attribute.time.Interval;
import org.gephi.graph.api.Edge;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EdgeImplTest {

    @Test
    public void testSetGetWeight() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        Assert.assertEquals(e.getWeight(), 1.0);
        e.setWeight(42.0);
        Assert.assertEquals(e.getWeight(), 42.0);
    }

    @Test
    public void testGetDefaultDynamicWeight() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, 1.0);
        Assert.assertEquals(e.getWeight(2.0), 0.0);
    }

    @Test
    public void testSetDynamicWeight() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, 1.0);
        Assert.assertEquals(e.getWeight(1.0), 42.0);
        e.setWeight(10.0, 2.0);
        Assert.assertEquals(e.getWeight(1.0), 42.0);
        Assert.assertEquals(e.getWeight(2.0), 10.0);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetWeightDynamicError() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.getWeight(1.0);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetWeightStaticError() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.setWeight(1.0, 1.0);
        e.getWeight();
    }

    @Test
    public void testHasDynamicWeight() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        Assert.assertFalse(e.hasDynamicWeight());
        e.setWeight(1.0, 1.0);
        Assert.assertTrue(e.hasDynamicWeight());
        e.setWeight(1.0);
        Assert.assertFalse(e.hasDynamicWeight());
    }

    @Test
    public void testGetDefaultWeightByGraphView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        Assert.assertEquals(e.getWeight(graphStore.mainGraphView), 1.0);
    }

    @Test
    public void testGetWeightMainGraphView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, 1.0);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 42.0);
        e.setWeight(10.0, 1.0);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 10.0);
    }

    @Test
    public void testGetWeightMainGraphViewReset() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, 1.0);
        e.setWeight(4.0);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 4.0);
        e.setWeight(5.0, 2.0);
        TimestampMap timestampMap = graphStore.edgeColumnStore.getTimestampMap(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
        Assert.assertEquals(timestampMap.size(), 1);
        Assert.assertFalse(timestampMap.contains(1.0));
    }

    @Test
    public void testGetWeightDefaultEstimator() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.setWeight(10.0, 1.0);
        e.setWeight(20.0, 2.0);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 10.0);
    }

    @Test
    public void testGetWeightAverageEstimator() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        graphStore.edgeColumnStore.setEstimator(graphStore.edgeColumnStore.getColumnByIndex(GraphStoreConfiguration.EDGE_WEIGHT_INDEX), Estimator.AVERAGE);
        e.setWeight(10.0, 1.0);
        e.setWeight(20.0, 2.0);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 15.0);
    }

    @Test
    public void testGetWeightWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.setWeight(10.0, 1.0);
        e.setWeight(20.0, 2.0);
        e.setWeight(30.0, 3.0);
        GraphViewImpl view = graphStore.viewStore.createView();
        view.setTimeInterval(new Interval(2.0, 2.0));

        Assert.assertEquals(e.getWeight(view), 20.0);
    }

    @Test
    public void testGetTypeLabelDefault() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        Assert.assertEquals(e.getType(), 0);
        Assert.assertNull(e.getTypeLabel());
    }

    @Test
    public void testGetTypeLabelCustom() {
        GraphStore graphStore = new GraphStore(null);
        EdgeTypeStore edgeTypeStore = graphStore.edgeTypeStore;
        int typeId = edgeTypeStore.addType("foo");

        NodeImpl n1 = new NodeImpl("1", graphStore);
        NodeImpl n2 = new NodeImpl("2", graphStore);
        EdgeImpl e = new EdgeImpl("0", graphStore, n1, n2, typeId, 1.0, true);
        Assert.assertEquals(e.getType(), 1);
        Assert.assertEquals(e.getTypeLabel(), "foo");
    }
}
