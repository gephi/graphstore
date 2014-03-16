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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class EdgeTypeStoreTest {

    @Test
    public void testDefaultSize() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();

        Assert.assertEquals(edgeTypeStore.size(), 1);
    }

    @Test
    public void testAddType() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");

        Assert.assertEquals(type, 1);
        Assert.assertTrue(edgeTypeStore.contains("0"));

        type = edgeTypeStore.addType("0");
        Assert.assertEquals(type, 1);
        Assert.assertTrue(edgeTypeStore.contains("0"));
    }

    @Test
    public void testAddNullType() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType(null);

        Assert.assertEquals(type, 0);
        Assert.assertTrue(edgeTypeStore.contains(null));

        type = edgeTypeStore.addType(null);
        Assert.assertEquals(type, 0);
        Assert.assertTrue(edgeTypeStore.contains(null));
    }

    @Test
    public void testRemoveType() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");

        type = edgeTypeStore.removeType("0");

        Assert.assertEquals(type, 1);
        Assert.assertFalse(edgeTypeStore.contains("0"));

        Assert.assertEquals(edgeTypeStore.size(), 1);

        type = edgeTypeStore.removeType("0");
        Assert.assertEquals(type, EdgeTypeStore.NULL_TYPE);
    }

    @Test
    public void testRemoveNullType() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType(null);

        type = edgeTypeStore.removeType(null);

        Assert.assertEquals(type, 0);
        Assert.assertFalse(edgeTypeStore.contains(null));

        Assert.assertEquals(edgeTypeStore.size(), 0);

        type = edgeTypeStore.removeType(null);
        Assert.assertEquals(type, EdgeTypeStore.NULL_TYPE);
    }

    @Test
    public void testRemoveTypeFromType() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");

        Object label = edgeTypeStore.removeType(type);

        Assert.assertEquals(label, "0");
        Assert.assertFalse(edgeTypeStore.contains("0"));

        Assert.assertEquals(edgeTypeStore.size(), 1);

        label = edgeTypeStore.removeType(type);
        Assert.assertNull(label);
    }

    @Test
    public void testGetByLabel() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");

        Assert.assertEquals(edgeTypeStore.getId("0"), type);

        Assert.assertEquals(edgeTypeStore.getId("1"), EdgeTypeStore.NULL_TYPE);
    }

    @Test
    public void testGetById() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");

        Assert.assertEquals(edgeTypeStore.getLabel(type), "0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidId() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        edgeTypeStore.addType("0");

        edgeTypeStore.getLabel(-1);
    }
    
    @Test
    public void testNullId() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        Assert.assertNull(edgeTypeStore.getLabel(0));
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnknowdId() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        edgeTypeStore.getLabel(4);
    }

    @Test
    public void testContains() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");

        Assert.assertTrue(edgeTypeStore.contains(type));
    }
    
    @Test
    public void testClear() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        edgeTypeStore.addType("0");
        edgeTypeStore.addType("1");
        edgeTypeStore.removeType("0");
        edgeTypeStore.clear();
        
        Assert.assertEquals(edgeTypeStore.size(), 1);
        Assert.assertTrue(edgeTypeStore.contains(null));
        Assert.assertTrue(edgeTypeStore.garbageQueue.isEmpty());
    }

    @Test
    public void testMaximumLength() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        for (int i = 1; i < 65534; i++) {
            int type = edgeTypeStore.addType(String.valueOf(i));
            Assert.assertEquals(edgeTypeStore.getId(String.valueOf(i)), type);
        }

        Assert.assertEquals(edgeTypeStore.size(), EdgeTypeStore.MAX_SIZE);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testMaximumLengthException() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        for (int i = 0; i < 65534; i++) {
            edgeTypeStore.addType(String.valueOf(i));
        }
    }

    @Test
    public void testGarbage() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        edgeTypeStore.addType("1");
        edgeTypeStore.removeType(type);

        Assert.assertTrue(edgeTypeStore.contains("1"));
        Assert.assertFalse(edgeTypeStore.contains("0"));

        Assert.assertEquals(edgeTypeStore.garbageQueue.size(), 1);
        Assert.assertEquals(edgeTypeStore.size(), 2);

        type = edgeTypeStore.addType("3");

        Assert.assertTrue(edgeTypeStore.contains("3"));
        Assert.assertTrue(edgeTypeStore.contains("1"));

        Assert.assertEquals(edgeTypeStore.garbageQueue.size(), 0);
        Assert.assertEquals(edgeTypeStore.size(), 3);
        Assert.assertEquals(edgeTypeStore.length, 3);
    }

    @Test
    public void testEqualsEmpty() {
        EdgeTypeStore edgeTypeStore1 = new EdgeTypeStore();
        Assert.assertEquals(edgeTypeStore1, edgeTypeStore1);

        EdgeTypeStore edgeTypeStore2 = new EdgeTypeStore();
        Assert.assertEquals(edgeTypeStore1, edgeTypeStore2);
    }

    @Test
    public void testHashCodeEmpty() {
        EdgeTypeStore edgeTypeStore1 = new EdgeTypeStore();
        Assert.assertEquals(edgeTypeStore1.hashCode(), edgeTypeStore1.hashCode());

        EdgeTypeStore edgeTypeStore2 = new EdgeTypeStore();
        Assert.assertEquals(edgeTypeStore1.hashCode(), edgeTypeStore2.hashCode());
    }

    @Test
    public void testEquals() {
        EdgeTypeStore edgeTypeStore1 = new EdgeTypeStore();
        edgeTypeStore1.addType("Foo");
        edgeTypeStore1.addType("Bar");
        edgeTypeStore1.addType(9);
        edgeTypeStore1.removeType("Foo");

        EdgeTypeStore edgeTypeStore2 = new EdgeTypeStore();
        edgeTypeStore2.addType("Foo");
        edgeTypeStore2.addType("Bar");
        edgeTypeStore2.addType(9);
        edgeTypeStore2.removeType("Foo");

        EdgeTypeStore edgeTypeStore3 = new EdgeTypeStore();
        edgeTypeStore3.addType("Foo");

        Assert.assertEquals(edgeTypeStore1, edgeTypeStore2);
        Assert.assertNotEquals(edgeTypeStore1, edgeTypeStore3);
    }

    @Test
    public void testHashCode() {
        EdgeTypeStore edgeTypeStore1 = new EdgeTypeStore();
        edgeTypeStore1.addType("Foo");
        edgeTypeStore1.addType("Bar");
        edgeTypeStore1.addType(9);
        edgeTypeStore1.removeType("Foo");

        EdgeTypeStore edgeTypeStore2 = new EdgeTypeStore();
        edgeTypeStore2.addType("Foo");
        edgeTypeStore2.addType("Bar");
        edgeTypeStore2.addType(9);
        edgeTypeStore2.removeType("Foo");

        EdgeTypeStore edgeTypeStore3 = new EdgeTypeStore();
        edgeTypeStore3.addType("Foo");

        Assert.assertEquals(edgeTypeStore1.hashCode(), edgeTypeStore2.hashCode());
        Assert.assertNotEquals(edgeTypeStore1.hashCode(), edgeTypeStore3.hashCode());
    }
}
