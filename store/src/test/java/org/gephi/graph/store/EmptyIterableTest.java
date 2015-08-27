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
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.junit.Assert;
import org.testng.annotations.Test;

public class EmptyIterableTest {

    @Test
    public void testEdgeIterableHasNext() {
        Iterator<Edge> itr = EdgeIterable.EMPTY.iterator();
        Assert.assertFalse(itr.hasNext());
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testEdgeIterableNext() {
        EdgeIterable.EMPTY.iterator().next();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testEdgeIterableRemove() {
        EdgeIterable.EMPTY.iterator().remove();
    }

    @Test
    public void testEdgeIterableToArray() {
        EdgeIterable itr = EdgeIterable.EMPTY;
        Assert.assertArrayEquals(itr.toArray(), new Edge[0]);
    }

    @Test
    public void testEdgeIterableToCollection() {
        EdgeIterable itr = EdgeIterable.EMPTY;
        Assert.assertEquals(itr.toCollection(), new ArrayList<Edge>());
    }
    
    @Test
    public void testEdgeIterableDoBreak() {
        EdgeIterable.EMPTY.doBreak();
    }

    @Test
    public void testNodeIterableHasNext() {
        Iterator<Node> itr = NodeIterable.EMPTY.iterator();
        Assert.assertFalse(itr.hasNext());
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testNodeIterableNext() {
        NodeIterable.EMPTY.iterator().next();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testNodeIterableRemove() {
        NodeIterable.EMPTY.iterator().remove();
    }

    @Test
    public void testNodeIterableToArray() {
        NodeIterable itr = NodeIterable.EMPTY;
        Assert.assertArrayEquals(itr.toArray(), new Node[0]);
    }

    @Test
    public void testNodeIterableToCollection() {
        NodeIterable itr = NodeIterable.EMPTY;
        Assert.assertEquals(itr.toCollection(), new ArrayList<Node>());
    }
    
    @Test
    public void testNodeIterableDoBreak() {
        NodeIterable.EMPTY.doBreak();
    }
    
    @Test
    public void testElementIterableHasNext() {
        Iterator<Element> itr = ElementIterable.EMPTY.iterator();
        Assert.assertFalse(itr.hasNext());
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testElementIterableNext() {
        ElementIterable.EMPTY.iterator().next();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testElementIterableRemove() {
        ElementIterable.EMPTY.iterator().remove();
    }

    @Test
    public void testElementIterableToArray() {
        ElementIterable itr = ElementIterable.EMPTY;
        Assert.assertArrayEquals(itr.toArray(), new Element[0]);
    }

    @Test
    public void testElementIterableToCollection() {
        ElementIterable itr = ElementIterable.EMPTY;
        Assert.assertEquals(itr.toCollection(), new ArrayList<Element>());
    }
    
    @Test
    public void testElementIterableDoBreak() {
        ElementIterable.EMPTY.doBreak();
    }
}
