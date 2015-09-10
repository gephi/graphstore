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

import org.gephi.graph.api.Configuration;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigurationTest {

    @Test
    public void testDefault() {
        Configuration c = new Configuration();
        Assert.assertNotNull(c.getNodeIdType());
        Assert.assertNotNull(c.getEdgeIdType());
    }

    @Test
    public void testSetNodeIdType() {
        Configuration c = new Configuration();
        c.setNodeIdType(Float.class);
        Assert.assertEquals(c.getNodeIdType(), Float.class);
    }

    @Test
    public void testSetEdgeIdType() {
        Configuration c = new Configuration();
        c.setEdgeIdType(Float.class);
        Assert.assertEquals(c.getEdgeIdType(), Float.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetNodeIdTypeUnsupported() {
        Configuration c = new Configuration();
        c.setNodeIdType(int[].class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetEdgeIdTypeUnsupported() {
        Configuration c = new Configuration();
        c.setEdgeIdType(int[].class);
    }

    @Test
    public void testDefaultEquals() {
        Assert.assertTrue(new Configuration().equals(new Configuration()));
    }

    @Test
    public void testDefaultHashCode() {
        Assert.assertEquals(new Configuration().hashCode(), new Configuration().hashCode());
    }

    @Test
    public void testEquals() {
        Configuration c1 = new Configuration();
        Configuration c2 = new Configuration();
        c2.setNodeIdType(Float.class);
        Assert.assertFalse(c1.equals(c2));
    }

    @Test
    public void testHashCode() {
        Configuration c1 = new Configuration();
        Configuration c2 = new Configuration();
        c2.setNodeIdType(Float.class);
        Assert.assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testCopy() {
        Configuration c1 = new Configuration();
        Configuration c2 = c1.copy();
        Assert.assertTrue(c1.equals(c2));
        c1.setNodeIdType(Float.class);
        Assert.assertNotEquals(c2.getNodeIdType(), Float.class);
        Assert.assertFalse(c1.equals(c2));
    }
}
