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
public class TimestsampStoreTest {
    
    @Test
    public void testEmpty() {
        TimestampStore store = new TimestampStore(null, null);
        Assert.assertTrue(store.isEmpty());
    }
    
    @Test
    public void testEqualsEmpty() {
        TimestampStore store1 = new TimestampStore(null, null);
        TimestampStore store2 = new TimestampStore(null, null);
        
        Assert.assertTrue(store1.equals(store2));
        Assert.assertTrue(store1.hashCode() == store2.hashCode());
    }
    
    @Test
    public void testGetMinNull() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = new TimestampStore(graphStore, null);
        Assert.assertEquals(store.getMin(graphStore), Double.NEGATIVE_INFINITY);
    }
    
    @Test
    public void testGetMaxNull() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = new TimestampStore(graphStore, null);
        Assert.assertEquals(store.getMax(graphStore), Double.POSITIVE_INFINITY);
    }
    
    @Test
    public void testGetMin() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = new TimestampStore(graphStore, null);
        store.nodeMap.addTimestamp(1.0);
        store.nodeMap.addTimestamp(2.0);
        Assert.assertEquals(store.getMin(graphStore), 1.0);
    }
    
    @Test
    public void testGetMax() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = new TimestampStore(graphStore, null);
        store.nodeMap.addTimestamp(1.0);
        store.nodeMap.addTimestamp(2.0);
        Assert.assertEquals(store.getMax(graphStore), 2.0);
    }
    
    @Test
    public void testClearEdges() {
        TimestampStore store = new TimestampStore(null, null);
        store.nodeMap.addTimestamp(1.0);
        store.edgeMap.addTimestamp(2.0);
        store.clearEdges();
        Assert.assertEquals(store.edgeMap.size(), 0);
        Assert.assertEquals(store.nodeMap.size(), 1);
        Assert.assertFalse(store.isEmpty());
    }
    
    @Test
    public void testClear() {
        TimestampStore store = new TimestampStore(null, null);
        store.nodeMap.addTimestamp(1.0);
        store.edgeMap.addTimestamp(2.0);
        store.clear();
        Assert.assertEquals(store.edgeMap.size(), 0);
        Assert.assertEquals(store.nodeMap.size(), 0);
        Assert.assertTrue(store.isEmpty());
    }
    
    @Test
    public void testEquals() {
        TimestampStore store1 = new TimestampStore(null, null);
        TimestampStore store2 = new TimestampStore(null, null);
        store1.nodeMap.addTimestamp(1.0);
        store1.edgeMap.addTimestamp(2.0);
        store2.nodeMap.addTimestamp(1.0);
        store2.edgeMap.addTimestamp(2.0);
        
        Assert.assertTrue(store1.equals(store2));
        Assert.assertTrue(store1.hashCode() == store2.hashCode());
    }
}
