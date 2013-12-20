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

import org.gephi.attribute.time.Interval;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampMapTest {

    @Test
    public void testEmpty() {
        TimestampMap map = new TimestampMap();
        Assert.assertTrue(map.size() == 0);
    }

    @Test
    public void testAddOne() {
        TimestampMap map = new TimestampMap();
        int a = map.addTimestamp(1.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(map.contains(1.0));
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.getTimestampIndex(1.0), 0);
    }

    @Test
    public void testAddOnGet() {
        TimestampMap map = new TimestampMap();
        int a = map.getTimestampIndex(1.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(map.contains(1.0));
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.getTimestampIndex(1.0), 0);
    }

    @Test
    public void testMultipleGet() {
        TimestampMap map = new TimestampMap();
        int a = map.getTimestampIndex(1.0);
        int b = map.getTimestampIndex(1.0);

        Assert.assertEquals(a, 0);
        Assert.assertEquals(b, 0);
        Assert.assertTrue(map.contains(1.0));
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.getTimestampIndex(1.0), 0);
    }

    @Test
    public void testRemove() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);
        map.removeTimestamp(1.0);

        Assert.assertTrue(map.size() == 0);
        Assert.assertFalse(map.contains(1.0));
    }

    @Test
    public void testNotContains() {
        TimestampMap map = new TimestampMap();
        Assert.assertFalse(map.contains(1.0));
    }

    @Test
    public void testAddAfterRemove() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);
        map.removeTimestamp(1.0);
        int a = map.addTimestamp(2.0);

        Assert.assertEquals(a, 0);
        Assert.assertTrue(map.contains(2.0));
    }

    @Test
    public void testGetIndicies() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);
        map.addTimestamp(2.0);

        int[] indicies = map.getTimestampIndices(new Interval(1.0, 2.0));
        Assert.assertEquals(indicies, new int[]{0, 1});

        int[] indicies2 = map.getTimestampIndices(new Interval(1.0, 1.0));
        Assert.assertEquals(indicies2, new int[]{0});
    }

    @Test
    public void testGetIndiciesOpen() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);
        map.addTimestamp(2.0);

        int[] indicies = map.getTimestampIndices(new Interval(1.0, 2.0, true, true));
        Assert.assertEquals(indicies, new int[]{});

        int[] indicies2 = map.getTimestampIndices(new Interval(1.0, 2.0, false, true));
        Assert.assertEquals(indicies2, new int[]{0});

        int[] indicies3 = map.getTimestampIndices(new Interval(1.0, 2.0, true, false));
        Assert.assertEquals(indicies3, new int[]{1});
    }

    @Test
    public void testGetIndiciesUnique() {
        TimestampMap map = new TimestampMap();
        map.addTimestamp(1.0);

        int[] indicies = map.getTimestampIndices(new Interval(1.0, 2.0));
        Assert.assertEquals(indicies, new int[]{0});
    }
    
    @Test
    public void testHasTimestampIndex() {
        TimestampMap map = new TimestampMap();
        
        Assert.assertFalse(map.hasTimestampIndex(1.0));
        
        map.addTimestamp(1.0);
        
        Assert.assertTrue(map.hasTimestampIndex(1.0));
        
        map.removeTimestamp(1.0);
        
        Assert.assertFalse(map.hasTimestampIndex(1.0));
    }
}
