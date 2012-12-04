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
        
        Assert.assertEquals(edgeTypeStore.size(), 0);
    }
    
    @Test
    public void testAddType() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        Assert.assertEquals(type, 0);
        Assert.assertTrue(edgeTypeStore.contains("0"));
        
        type = edgeTypeStore.addType("0");
        Assert.assertEquals(type, 0);
        Assert.assertTrue(edgeTypeStore.contains("0"));
    }
    
    @Test
    public void testRemoveType() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        type = edgeTypeStore.removeType("0");
        
        Assert.assertEquals(type, 0);
        Assert.assertFalse(edgeTypeStore.contains("0"));
        
        Assert.assertEquals(edgeTypeStore.size(), 0);
        
        type = edgeTypeStore.removeType("0");
        Assert.assertEquals(type, EdgeTypeStore.NULL_TYPE);
    }
    
    @Test
    public void testRemoveTypeFromType() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        Object label = edgeTypeStore.removeType(type);
        
        Assert.assertEquals(label, "0");
        Assert.assertFalse(edgeTypeStore.contains("0"));
        
        Assert.assertEquals(edgeTypeStore.size(), 0);
        
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
        
        Assert.assertNull(edgeTypeStore.getLabel(1));
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidId() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        edgeTypeStore.getLabel(-1);
    }
    
    @Test
    public void testContains() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        Assert.assertTrue(edgeTypeStore.contains(type));
    }
    
    @Test
    public void testMaximumLength() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        for (int i = 0; i < 65534; i++) {
            int type = edgeTypeStore.addType(String.valueOf(i));
            Assert.assertEquals(edgeTypeStore.getId(String.valueOf(i)), type);
        }
        
        Assert.assertEquals(edgeTypeStore.size(), EdgeTypeStore.MAX_SIZE);
    }
    
    @Test(expectedExceptions = RuntimeException.class)
    public void testMaximumLengthException() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        for (int i = 0; i < 65535; i++) {
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
        Assert.assertEquals(edgeTypeStore.size(), 1);
        
        type = edgeTypeStore.addType("3");
        
        Assert.assertTrue(edgeTypeStore.contains("3"));
        Assert.assertTrue(edgeTypeStore.contains("1"));
        
        Assert.assertEquals(edgeTypeStore.garbageQueue.size(), 0);
        Assert.assertEquals(edgeTypeStore.size(), 2);
        Assert.assertEquals(edgeTypeStore.length, 2);
    }
    
    @Test
    public void testIncrement() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        int val = edgeTypeStore.increment(type);
        
        Assert.assertEquals(val, 1);
        
        val = edgeTypeStore.increment(type);
        
        Assert.assertEquals(val, 2);
    }
    
    @Test(expectedExceptions = RuntimeException.class)
    public void testIncrementUnknownId() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        
        edgeTypeStore.increment(0);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIncrementInvalidId() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        edgeTypeStore.increment(-1);
    }
    
    @Test
    public void testDecrement() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        int val = edgeTypeStore.increment(type);
        val = edgeTypeStore.decrement(type);
        
        Assert.assertEquals(val, 0);
    }
    
    @Test(expectedExceptions = RuntimeException.class)
    public void testDecrementUnknownId() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        
        edgeTypeStore.decrement(0);
    }
    
    @Test(expectedExceptions = RuntimeException.class)
    public void testDecrementZero() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        edgeTypeStore.decrement(type);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDecrementInvalidId() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        edgeTypeStore.decrement(-1);
    }
    
    @Test
    public void testGetCount() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        int count = edgeTypeStore.increment(type);
        Assert.assertEquals(edgeTypeStore.getCount(type), count);
        count = edgeTypeStore.decrement(type);
        Assert.assertEquals(edgeTypeStore.getCount(type), count);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetCountInvalid() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        int type = edgeTypeStore.addType("0");
        
        edgeTypeStore.getCount(-1);
    }
    
    @Test(expectedExceptions = RuntimeException.class)
    public void testGetCountUnknown() {
        EdgeTypeStore edgeTypeStore = new EdgeTypeStore();
        
        edgeTypeStore.getCount(0);
    }
}
