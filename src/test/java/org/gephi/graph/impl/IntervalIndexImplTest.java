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

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntervalIndexImplTest {

    @Test
    public void testGetMin() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timeStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timeStore.nodeIndexStore;
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), Double.NEGATIVE_INFINITY);

        store.add(new Interval(1.0, 3.0));
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 1.0);

        store.add(new Interval(2.0, 3.0));
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 1.0);

        store.remove(new Interval(1.0, 3.0));
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 2.0);
    }

    @Test
    public void testGetMinMaxWithView() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore(TimeRepresentation.INTERVAL);
        Node n1 = graphStore.getNode("1");
        Node n2 = graphStore.getNode("2");
        n1.addInterval(new Interval(1.0, 5.0));
        n2.addInterval(new Interval(2.0, 3.0));

        TimeStore timeStore = graphStore.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timeStore.nodeIndexStore;

        GraphViewImpl view = graphStore.viewStore.createView();
        Graph graph = graphStore.viewStore.getGraph(view);
        view.fill();
        TimeIndexImpl index = store.createViewIndex(graph);

        Assert.assertEquals(index.getMinTimestamp(), 1.0);
        Assert.assertEquals(index.getMaxTimestamp(), 5.0);
        graph.removeNode(n1);
        Assert.assertEquals(index.getMinTimestamp(), 2.0);
        Assert.assertEquals(index.getMaxTimestamp(), 3.0);
    }

    @Test
    public void testGetMax() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timeStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timeStore.nodeIndexStore;
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), Double.POSITIVE_INFINITY);

        store.add(new Interval(1.0, 3.0));
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 3.0);

        store.add(new Interval(1.0, 2.0));
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 3.0);

        store.remove(new Interval(1.0, 3.0));
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 2.0);
    }

    @Test
    public void testGetMinWithInfinite() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timeStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timeStore.nodeIndexStore;

        store.add(new Interval(Double.NEGATIVE_INFINITY, 3.0));
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 3.0);

        store.add(new Interval(2.0, 3.0));
        Assert.assertEquals(store.mainIndex.getMinTimestamp(), 2.0);
    }

    @Test
    public void testGetMaxWithInfinite() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timeStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timeStore.nodeIndexStore;

        store.add(new Interval(1.0, Double.POSITIVE_INFINITY));
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 1.0);

        store.add(new Interval(1.0, 2.0));
        Assert.assertEquals(store.mainIndex.getMaxTimestamp(), 2.0);
    }

    @Test
    public void testGetElements() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timeStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timeStore.nodeIndexStore;
        Interval i1 = new Interval(1.0, 5.0);
        Interval i2 = new Interval(-1.0, 1.0);
        Interval i3 = new Interval(4.0, 8.0);

        store.add(i1);
        store.add(i2);
        store.add(i3);

        NodeImpl n0 = new NodeImpl(0);
        NodeImpl n1 = new NodeImpl(1);
        NodeImpl n2 = new NodeImpl(2);
        NodeImpl n3 = new NodeImpl(3);

        store.add(i1, n0);
        store.add(i1, n1);
        store.add(i2, n2);
        store.add(i3, n3);

        ObjectSet r1 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(2.0, 3.0))));
        Assert.assertTrue(r1.contains(n0));
        Assert.assertTrue(r1.contains(n1));
        Assert.assertEquals(r1.size(), 2);

        ObjectSet r2 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(-1, 3.0))));
        Assert.assertTrue(r2.contains(n0));
        Assert.assertTrue(r2.contains(n1));
        Assert.assertTrue(r2.contains(n2));
        Assert.assertEquals(r2.size(), 3);

        ObjectSet r4 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(-1.0, -1.0))));
        Assert.assertTrue(r4.contains(n2));
        Assert.assertEquals(r4.size(), 1);

        ObjectSet r5 = new ObjectOpenHashSet(getArrayFromIterable(store.mainIndex.get(new Interval(-12.0, 10))));
        Assert.assertTrue(r5.contains(n2));
        Assert.assertTrue(r5.contains(n1));
        Assert.assertTrue(r5.contains(n0));
        Assert.assertTrue(r5.contains(n3));
        Assert.assertEquals(r5.size(), 4);
    }

    @Test
    public void testHasNodesEdgesEmpty() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timeStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timeStore.nodeIndexStore;
        Assert.assertFalse(store.mainIndex.hasElements());
    }

    @Test
    public void testHasNodes() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timeStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timeStore.nodeIndexStore;
        Assert.assertFalse(store.mainIndex.hasElements());

        store.add(new Interval(1.0, 2.0));
        store.add(new Interval(3.0, 4.0));

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(new Interval(1.0, 2.0), nodeImpl);
        store.add(new Interval(3.0, 4.0), nodeImpl);
        Assert.assertTrue(store.mainIndex.hasElements());

        store.remove(new Interval(1.0, 2.0), nodeImpl);
        Assert.assertTrue(store.mainIndex.hasElements());
        store.remove(new Interval(3.0, 4.0), nodeImpl);
        Assert.assertFalse(store.mainIndex.hasElements());
    }

    @Test
    public void testHasNodesClear() {
        Configuration config = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        GraphModelImpl graphModel = new GraphModelImpl(config);
        TimeStore timeStore = graphModel.store.timeStore;
        IntervalIndexStore store = (IntervalIndexStore) timeStore.nodeIndexStore;
        store.add(new Interval(1.0, 2.0));

        NodeImpl nodeImpl = new NodeImpl(0);

        store.add(new Interval(1.0, 2.0), nodeImpl);
        store.clear();
        Assert.assertFalse(store.mainIndex.hasElements());
    }

    // UTILITY
    private <T> Object[] getArrayFromIterable(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T t : iterable) {
            list.add(t);
        }
        return list.toArray();
    }
}
