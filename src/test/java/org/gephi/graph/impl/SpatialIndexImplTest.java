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

import java.util.Arrays;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SpatialIndexImplTest {

    private static final float BOUNDS = 1000f;
    private static final Rect2D BOUNDS_RECT = new Rect2D(-BOUNDS, -BOUNDS, BOUNDS, BOUNDS);

    @Test
    public void testDisabled() {
        GraphStore store = GraphGenerator.generateEmptyGraphStore();
        Assert.assertNull(store.spatialIndex);
    }

    @Test
    public void testGetEdgesEmpty() {
        SpatialIndexImpl spatialIndex = new GraphStore(null, getConfig()).spatialIndex;
        Assert.assertTrue(spatialIndex.getEdgesInArea(BOUNDS_RECT).toCollection().isEmpty());
    }

    @Test
    public void testGetElementsBothNodesVisible() {
        GraphStore store = GraphGenerator.generateTinyGraphStore(getConfig());

        NodeImpl n1 = store.getNode("1");
        NodeImpl n2 = store.getNode("2");
        EdgeImpl e = store.getEdge("0");

        SpatialIndexImpl spatialIndex = store.spatialIndex;
        assertSame(spatialIndex.getNodesInArea(BOUNDS_RECT), n1, n2);
        assertSame(spatialIndex.getEdgesInArea(BOUNDS_RECT), e, e);
    }

    @Test
    public void testGetElementsOneNodeVisible() {
        GraphStore store = GraphGenerator.generateTinyGraphStore(getConfig());

        NodeImpl n1 = store.getNode("1");
        n1.setPosition(300000f, 300000f);
        NodeImpl n2 = store.getNode("2");
        EdgeImpl e = store.getEdge("0");

        SpatialIndexImpl spatialIndex = store.spatialIndex;
        assertSame(spatialIndex.getNodesInArea(BOUNDS_RECT), n2);
        assertSame(spatialIndex.getEdgesInArea(BOUNDS_RECT), e);
    }

    @Test
    public void testGetElementsWithoutNodeVisible() {
        GraphStore store = GraphGenerator.generateTinyGraphStore(getConfig());

        NodeImpl n1 = store.getNode("1");
        n1.setPosition(300000f, 300000f);
        NodeImpl n2 = store.getNode("2");
        n2.setPosition(300001f, 300001f);
        EdgeImpl e = store.getEdge("0");

        SpatialIndexImpl spatialIndex = store.spatialIndex;
        Assert.assertTrue(spatialIndex.getNodesInArea(BOUNDS_RECT).toCollection().isEmpty());
        Assert.assertTrue(spatialIndex.getEdgesInArea(BOUNDS_RECT).toCollection().isEmpty());
    }

    @Test
    public void testGetElementsWithSelfLoop() {
        GraphStore store = GraphGenerator.generateTinyGraphStoreWithSelfLoop(getConfig());

        NodeImpl n1 = store.getNode("1");
        EdgeImpl e = store.getEdge("0");

        SpatialIndexImpl spatialIndex = store.spatialIndex;
        assertSame(spatialIndex.getNodesInArea(BOUNDS_RECT), n1);
        assertSame(spatialIndex.getEdgesInArea(BOUNDS_RECT), e);
    }

    @Test
    public void testClear() {
        GraphStore store = GraphGenerator.generateTinyGraphStore(getConfig());

        SpatialIndexImpl spatialIndex = store.spatialIndex;
        Assert.assertEquals(spatialIndex.getObjectCount(), store.getNodeCount());
        Assert.assertEquals(spatialIndex.getNodesInArea(new Rect2D(-1, -1, 1, 1)).toCollection().size(), store
                .getNodeCount());
        store.clear();
        Assert.assertEquals(spatialIndex.getObjectCount(), 0);
        Assert.assertTrue(spatialIndex.getNodesInArea(new Rect2D(-1, -1, 1, 1)).toCollection().isEmpty());
    }

    private void assertSame(NodeIterable iterable, Node... expected) {
        Assert.assertEquals(iterable.toCollection(), Arrays.asList(expected));
    }

    private void assertSame(EdgeIterable iterable, Edge... expected) {
        Assert.assertEquals(iterable.toCollection(), Arrays.asList(expected));
    }

    // Configuration with spatial index
    private Configuration getConfig() {
        return Configuration.builder().enableSpatialIndex(true).build();
    }
}
