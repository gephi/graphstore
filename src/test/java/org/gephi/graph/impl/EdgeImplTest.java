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

import java.util.Iterator;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EdgeImplTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testWrongIdType() {
        new EdgeImpl(Boolean.TRUE, new GraphStore(), null, null, 0, 1.0, true);
    }

    @Test
    public void testSetGetWeight() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        Assert.assertEquals(e.getWeight(), 1.0);
        e.setWeight(42.0);
        Assert.assertEquals(e.getWeight(), 42.0);
    }

    @Test
    public void testZeroWeight() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.setWeight(0.0);
        Assert.assertEquals(e.getWeight(), 0.0);
    }

    @Test
    public void testGetDefaultTimestampWeight() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, 1.0);
        Assert.assertEquals(e.getWeight(2.0), GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING);
    }

    @Test
    public void testGetDefaultTimestampWeightWhenNotSet() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        Assert.assertEquals(e.getWeight(2.0), GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING);
    }

    @Test
    public void testGetDefaultIntervalWeight() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(IntervalDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, new Interval(1.0, 2.0));
        Assert.assertEquals(e
                .getWeight(new Interval(2.1, 4.0)), GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING);
    }

    @Test
    public void testGetDefaultIntervalWeightWhenNotSet() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(IntervalDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        Assert.assertEquals(e
                .getWeight(new Interval(2.0, 4.0)), GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING);
    }

    @Test
    public void testGetWeightInterval() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(IntervalDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, new Interval(1.0, 2.0));
        Assert.assertEquals(e.getWeight(new Interval(2.0, 4.0)), 42.0);
    }

    @Test
    public void testGetWeightIntervalMax() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(IntervalDoubleMap.class).build();

        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Column col = graphStore.edgeTable.store.getColumnByIndex(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
        col.setEstimator(Estimator.MAX);

        Edge e = graphStore.getEdge("0");
        e.setWeight(10.0, new Interval(1.0, 2.0));
        e.setWeight(20.0, new Interval(2.0, 3.0));
        Assert.assertEquals(e.getWeight(new Interval(2.0, 2.0)), 20.0);
    }

    @Test
    public void testSetTimestampWeight() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, 1.0);
        Assert.assertEquals(e.getWeight(1.0), 42.0);
        e.setWeight(10.0, 2.0);
        Assert.assertEquals(e.getWeight(1.0), 42.0);
        Assert.assertEquals(e.getWeight(2.0), 10.0);
    }

    @Test
    public void testSetIntervalWeight() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(IntervalDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        Interval i1 = new Interval(1.0, 2.0);
        Interval i2 = new Interval(3.0, 4.0);
        e.setWeight(42.0, i1);
        Assert.assertEquals(e.getWeight(i1), 42.0);
        e.setWeight(10.0, i2);
        Assert.assertEquals(e.getWeight(i1), 42.0);
        Assert.assertEquals(e.getWeight(i2), 10.0);
    }

    @Test
    public void testIntervalWeightUsesFirstValueInOverlappingIntervalsEstimator() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(IntervalDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, new Interval(1.0, 2.0));
        Assert.assertEquals(e.getWeight(new Interval(2.0, 4.0)), 42.0);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetWeightTimestampError() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        e.getWeight(1.0);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetWeightIntervalError() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        Edge e = graphStore.getEdge("0");
        e.getWeight(new Interval(1.0, 2.0));
    }

    @Test
    public void testGetWeightStaticError() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(1.0, 1.0);
        e.setWeight(1.5, 2.0);
        e.setWeight(2.0, 4.0);

        Assert.assertEquals(e.getWeight(), 1.0);
    }

    @Test
    public void testHasDynamicWeightDouble() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        Assert.assertFalse(e.hasDynamicWeight());
        e.setWeight(3.0);
        Assert.assertFalse(e.hasDynamicWeight());
    }

    @Test
    public void testHasDynamicWeightTimestamp() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        Assert.assertTrue(e.hasDynamicWeight());
        e.setWeight(1.0, 1.0);
        Assert.assertTrue(e.hasDynamicWeight());
    }

    @Test
    public void testHasDynamicWeightInterval() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(IntervalDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        Assert.assertTrue(e.hasDynamicWeight());
        e.setWeight(1.0, new Interval(1.0, 2.0));
        Assert.assertTrue(e.hasDynamicWeight());
    }

    @Test
    public void testGetDefaultWeightByGraphView() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        Assert.assertEquals(e
                .getWeight(graphStore.mainGraphView), GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING);
    }

    @Test
    public void testGetTimestampWeightMainGraphView() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0, 1.0);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 42.0);
        e.setWeight(10.0, 1.0);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 10.0);
    }

    @Test
    public void testGetWeightGraphViewMax() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(IntervalDoubleMap.class).build();

        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Column col = graphStore.edgeTable.store.getColumnByIndex(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
        col.setEstimator(Estimator.MAX);

        Edge e = graphStore.getEdge("0");
        Interval i1 = new Interval(1.0, 2.0);
        Interval i2 = new Interval(3.0, 4.0);
        e.setWeight(10.0, i1);
        e.setWeight(20.0, i2);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 20.0);
    }

    @Test
    public void testGetWeightNoValue() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setAttribute("weight", null);
        Assert.assertEquals(e
                .getWeight(graphStore.getView()), GraphStoreConfiguration.DEFAULT_DYNAMIC_EDGE_WEIGHT_WHEN_MISSING);
    }

    @Test
    public void testGetWeightDefaultEstimator() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(10.0, 1.0);
        e.setWeight(20.0, 2.0);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 10.0);
    }

    @Test
    public void testGetWeightAverageEstimator() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        Column col = graphStore.edgeTable.store.getColumnByIndex(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);
        col.setEstimator(Estimator.AVERAGE);
        e.setWeight(10.0, 1.0);
        e.setWeight(20.0, 2.0);
        Assert.assertEquals(e.getWeight(graphStore.getView()), 15.0);
    }

    @Test
    public void testGetWeightWithView() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(10.0, 1.0);
        e.setWeight(20.0, 2.0);
        e.setWeight(30.0, 3.0);
        GraphViewImpl view = graphStore.viewStore.createView();
        view.setTimeInterval(new Interval(2.0, 2.0));

        Assert.assertEquals(e.getWeight(view), 20.0);
    }

    @Test
    public void testGetWeightWithViewStatic() {
        Configuration config = Configuration.builder().edgeWeightType(Double.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(10.0);
        GraphViewImpl view = graphStore.viewStore.createView();

        Assert.assertEquals(e.getWeight(view), 10.0);
    }

    @Test
    public void testGetWeightsTimestamp() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(10.0, 3.0);
        e.setWeight(20.0, 1.0);

        Iterator<Map.Entry> itr = e.getWeights().iterator();
        Assert.assertNotNull(itr);
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Double> n1 = itr.next();
        Assert.assertEquals(n1.getKey(), 1.0);
        Assert.assertEquals(n1.getValue(), 20.0);
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Double, Double> n2 = itr.next();
        Assert.assertEquals(n2.getKey(), 3.0);
        Assert.assertEquals(n2.getValue(), 10.0);
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testGetWeightsInterval() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL)
                .edgeWeightType(IntervalDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        e.setWeight(10.0, new Interval(3.0, 4.0));
        e.setWeight(20.0, new Interval(1.0, 2.0));

        Iterator<Map.Entry> itr = e.getWeights().iterator();
        Assert.assertNotNull(itr);
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Interval, Double> n1 = itr.next();
        Assert.assertEquals(n1.getKey(), new Interval(1.0, 2.0));
        Assert.assertEquals(n1.getValue(), 20.0);
        Assert.assertTrue(itr.hasNext());
        Map.Entry<Interval, Double> n2 = itr.next();
        Assert.assertEquals(n2.getKey(), new Interval(3.0, 4.0));
        Assert.assertEquals(n2.getValue(), 10.0);
        Assert.assertFalse(itr.hasNext());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetWeightsStatic() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        Edge e = graphStore.getEdge("0");
        e.setWeight(42.0);
        e.getWeights();
    }

    @Test
    public void testSetAttributeWeightTimestamp() {
        Configuration config = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(config);
        Edge e = graphStore.getEdge("0");
        Column col = graphStore.edgeTable.getColumn(GraphStoreConfiguration.EDGE_WEIGHT_INDEX);

        TimestampDoubleMap wm = new TimestampDoubleMap();
        wm.put(1.0, 10.0);
        wm.put(2.0, 20.0);
        e.setAttribute(col, wm);

        Assert.assertEquals(e.getWeight(1.0), 10.0);
        Assert.assertEquals(e.getWeight(2.0), 20.0);
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
        GraphStore graphStore = new GraphStore();
        EdgeTypeStore edgeTypeStore = graphStore.edgeTypeStore;
        int typeId = edgeTypeStore.addType("foo");

        NodeImpl n1 = new NodeImpl("1", graphStore);
        NodeImpl n2 = new NodeImpl("2", graphStore);
        EdgeImpl e = new EdgeImpl("0", graphStore, n1, n2, typeId, 1.0, true);
        Assert.assertEquals(e.getType(), 1);
        Assert.assertEquals(e.getTypeLabel(), "foo");
    }

    @Test
    public void testEdgeFields() {
        NodeImpl source = new NodeImpl("0");
        NodeImpl target = new NodeImpl("1");
        double weight = 2.0;
        EdgeImpl edge = new EdgeImpl("0", source, target, 0, weight, true);

        Assert.assertTrue(edge.isDirected());
        Assert.assertFalse(edge.isSelfLoop());
        Assert.assertFalse(edge.isMutual());
        Assert.assertFalse(edge.isValid());

        edge.setMutual(true);

        Assert.assertTrue(edge.isMutual());

        edge = new EdgeImpl("0", source, source, 0, weight, true);

        Assert.assertTrue(edge.isSelfLoop());

        edge = new EdgeImpl("0", source, target, 0, weight, false);

        Assert.assertFalse(edge.isDirected());

        edge.setMutual(true);

        Assert.assertFalse(edge.isMutual());
    }

    @Test
    public void testGetTable() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        for (Edge e : graphStore.getEdges()) {
            Assert.assertSame(e.getTable(), graphStore.getModel().getEdgeTable());
        }
    }

    @Test
    public void testProperties() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Edge e = graphStore.getEdge("0");
        Assert.assertNotNull(e.getTextProperties());
        Assert.assertNotNull(e.getColor());
        Assert.assertEquals(e.alpha(), 1f);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPropertiesDisabled() {
        GraphStore graphStore = GraphGenerator
                .generateTinyGraphStore(Configuration.builder().enableEdgeProperties(false).build());
        Edge e = graphStore.getEdge("0");
        Assert.assertNull(e.getColor());
    }
}
