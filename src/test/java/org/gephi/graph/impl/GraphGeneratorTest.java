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

import org.gephi.graph.api.Edge;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphGeneratorTest {

    @Test
    public void testGenerateLargeGraphStoreEdgesReferenceRealNodes() {
        GraphStore graphStore = GraphGenerator.generateLargeGraphStore();

        // Edges must reference the same node objects registered in the store's own nodeStore, not just
        // objects that happen to carry a matching storeId - otherwise removeNode()'s cascade-edge-removal
        // (which walks the real node's own adjacency links) silently fails to find and remove them.
        for (Edge edge : graphStore.edgeStore) {
            EdgeImpl e = (EdgeImpl) edge;
            Assert.assertSame(graphStore.nodeStore.get(e.source.storeId), e.source);
            Assert.assertSame(graphStore.nodeStore.get(e.target.storeId), e.target);
        }
    }
}
