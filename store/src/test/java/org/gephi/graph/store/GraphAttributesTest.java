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

import java.awt.Color;
import java.awt.Transparency;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class GraphAttributesTest {

    @Test
    public void testEmpty() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        Assert.assertTrue(atts.getKeys().isEmpty());
    }

    @Test
    public void testSetOne() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", "bar");

        Assert.assertEquals(atts.getValue("foo"), "bar");
    }

    @Test
    public void testGetNull() {
        GraphAttributesImpl atts = new GraphAttributesImpl();

        Assert.assertNull(atts.getValue("foo"));
    }

    @Test
    public void testReplace() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", "bar");
        atts.setValue("foo", "bar2");

        Assert.assertEquals(atts.getValue("foo"), "bar2");
    }

    @Test
    public void testSetNull() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", null);

        Assert.assertNull(atts.getValue("foo"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnsupportedType() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", Color.BLACK);
    }

    @Test
    public void testGeyKeys() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", "bar");

        Set<String> keys = atts.getKeys();
        Assert.assertEquals(keys.size(), 1);
        Assert.assertTrue(keys.contains("foo"));
    }

    @Test
    public void testSetWithTimestamp() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", "bar", 1.0);

        Assert.assertEquals(atts.getValue("foo", 1.0), "bar");
    }

    @Test
    public void testSetMultipleTimestamp() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", "bar1", 1.0);
        atts.setValue("foo", "bar2", 2.0);
        atts.setValue("foo", "bar3", 3.0);

        Assert.assertEquals(atts.getValue("foo", 1.0), "bar1");
        Assert.assertEquals(atts.getValue("foo", 2.0), "bar2");
        Assert.assertEquals(atts.getValue("foo", 3.0), "bar3");
    }

    @Test
    public void testReplaceWithTimestamp() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", "bar", 1.0);
        atts.setValue("foo", "bar2", 1.0);

        Assert.assertEquals(atts.getValue("foo", 1.0), "bar2");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testSetNullWithTimestamp() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", null, 1.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnsupportedTypeWithTimestamp() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", int[].class, 1.0);
    }

    @Test
    public void testGeyKeysWithTimestamp() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", "bar", 1.0);

        Set<String> keys = atts.getKeys();
        Assert.assertEquals(keys.size(), 1);
        Assert.assertTrue(keys.contains("foo"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testWrongDynmaicType() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", "bar", 1.0);
        atts.setValue("foo", 1, 2.0);
    }

    @Test
    public void testGetNullWithTimestamp() {
        GraphAttributesImpl atts = new GraphAttributesImpl();
        atts.setValue("foo", "bar", 1.0);

        Assert.assertNull(atts.getValue("bar", 1.0));
        Assert.assertNull(atts.getValue("foo", 2.0));
    }

    @Test
    public void testEquals() {
        GraphAttributesImpl atts1 = new GraphAttributesImpl();
        GraphAttributesImpl atts2 = new GraphAttributesImpl();
        
        Assert.assertTrue(atts1.equals(atts2));
        Assert.assertTrue(atts1.hashCode() == atts2.hashCode());
        
        atts1.setValue("foo", "bar", 1.0);
        atts2.setValue("foo", "bar", 1.0);
        
        Assert.assertTrue(atts1.equals(atts2));
        Assert.assertTrue(atts1.hashCode() == atts2.hashCode());
    }
}
