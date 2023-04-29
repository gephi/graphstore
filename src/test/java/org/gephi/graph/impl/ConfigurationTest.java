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
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigurationTest {

    @Test
    public void testDefaultBuilder() {
        Configuration c = Configuration.builder().build();
        Assert.assertNotNull(c);
        Assert.assertNotNull(c.getNodeIdType());
        Assert.assertEquals(c, Configuration.builder().build());
        Assert.assertEquals(c.hashCode(), Configuration.builder().build().hashCode());
    }

    @Test
    public void testBuilderMultipleSet() {
        Configuration.Builder b = Configuration.builder();
        Assert.assertEquals(b.build().getNodeIdType(), String.class);
        b.nodeIdType(Integer.class);
        Assert.assertEquals(b.build().getNodeIdType(), Integer.class);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDefaultDeprecated() {
        Configuration c = new Configuration();
        Assert.assertNotNull(c.getNodeIdType());
        Assert.assertNotNull(c.getEdgeIdType());
        Assert.assertNotNull(c.getEdgeLabelType());
        Assert.assertNotNull(c.getEdgeWeightColumn());
    }

    @Test
    public void testSetNodeIdType() {
        Configuration c = Configuration.builder().nodeIdType(Float.class).build();
        Assert.assertEquals(c.getNodeIdType(), Float.class);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSetNodeIdTypeDeprecated() {
        Configuration c = new Configuration();
        c.setNodeIdType(Float.class);
        Assert.assertEquals(c.getNodeIdType(), Float.class);
    }

    @Test
    public void testSetEdgeIdType() {
        Configuration c = Configuration.builder().edgeIdType(Float.class).build();
        Assert.assertEquals(c.getEdgeIdType(), Float.class);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSetEdgeIdTypeDeprecated() {
        Configuration c = new Configuration();
        c.setEdgeIdType(Float.class);
        Assert.assertEquals(c.getEdgeIdType(), Float.class);
    }

    @Test
    public void testSetEdgeLabelType() {
        Configuration c = Configuration.builder().edgeLabelType(Float.class).build();
        Assert.assertEquals(c.getEdgeLabelType(), Float.class);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSetEdgeLabelTypeDeprecated() {
        Configuration c = new Configuration();
        c.setEdgeLabelType(Float.class);
        Assert.assertEquals(c.getEdgeLabelType(), Float.class);
    }

    @Test
    public void testSetEdgeWeightType() {
        Configuration c = Configuration.builder().edgeWeightType(IntervalDoubleMap.class).build();
        Assert.assertEquals(c.getEdgeWeightType(), IntervalDoubleMap.class);
        c = Configuration.builder().edgeWeightType(TimestampDoubleMap.class).build();
        Assert.assertEquals(c.getEdgeWeightType(), TimestampDoubleMap.class);
        c = Configuration.builder().edgeWeightType(Double.class).build();
        Assert.assertEquals(c.getEdgeWeightType(), Double.class);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSetEdgeWeightTypeDeprecated() {
        Configuration c = new Configuration();
        c.setEdgeWeightType(IntervalDoubleMap.class);
        Assert.assertEquals(c.getEdgeWeightType(), IntervalDoubleMap.class);
        c.setEdgeWeightType(TimestampDoubleMap.class);
        Assert.assertEquals(c.getEdgeWeightType(), TimestampDoubleMap.class);
        c.setEdgeWeightType(Double.class);
        Assert.assertEquals(c.getEdgeWeightType(), Double.class);
    }

    @Test
    public void testSetTimeRepresentation() {
        Configuration c = Configuration.builder().timeRepresentation(TimeRepresentation.INTERVAL).build();
        Assert.assertEquals(c.getTimeRepresentation(), TimeRepresentation.INTERVAL);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSetTimeRepresentationDeprecated() {
        Configuration c = new Configuration();
        c.setTimeRepresentation(TimeRepresentation.INTERVAL);
        Assert.assertEquals(c.getTimeRepresentation(), TimeRepresentation.INTERVAL);
    }

    @Test
    public void testSetEdgeWeightColumn() {
        Configuration c = Configuration.builder().edgeWeightColumn(false).build();
        Assert.assertEquals(c.getEdgeWeightColumn(), Boolean.FALSE);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSetEdgeWeightColumnDeprecated() {
        Configuration c = new Configuration();
        c.setEdgeWeightColumn(Boolean.FALSE);
        Assert.assertEquals(c.getEdgeWeightColumn(), Boolean.FALSE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetNodeIdTypeUnsupported() {
        Configuration.builder().nodeIdType(int[].class).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetNodeIdTypeUnsupportedDeprecated() {
        Configuration c = new Configuration();
        c.setNodeIdType(int[].class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetEdgeIdTypeUnsupported() {
        Configuration.builder().edgeIdType(int[].class).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("deprecation")
    public void testSetEdgeIdTypeUnsupportedDeprecated() {
        Configuration c = new Configuration();
        c.setEdgeIdType(int[].class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetEdgeWeightTypeFloatUnsupported() {
        Configuration.builder().edgeWeightType(Float.class).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("deprecation")
    public void testSetEdgeWeightTypeFloatUnsupportedDeprecated() {
        Configuration c = new Configuration();
        c.setEdgeWeightType(Float.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetEdgeWeightTypeNotNumberUnsupported() {
        Configuration.builder().edgeWeightType(String.class).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("deprecation")
    public void testSetEdgeWeightTypeNotNumberUnsupportedDeprecated() {
        Configuration c = new Configuration();
        c.setEdgeWeightType(String.class);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDefaultEqualsDeprecated() {
        Assert.assertEquals(new Configuration(), new Configuration());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDefaultHashCodeDeprecated() {
        Assert.assertEquals(new Configuration().hashCode(), new Configuration().hashCode());
    }

    @Test
    public void testEquals() {
        Configuration c1 = Configuration.builder().nodeIdType(Float.class).build();
        Configuration c2 = Configuration.builder().build();
        Assert.assertNotEquals(c2, c1);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testEqualsDeprecated() {
        Configuration c1 = new Configuration();
        Configuration c2 = new Configuration();
        Assert.assertEquals(c2, c1);
        c2.setNodeIdType(Float.class);
        Assert.assertNotEquals(c2, c1);
    }

    @Test
    public void testHashCode() {
        Configuration c1 = Configuration.builder().nodeIdType(Float.class).build();
        Configuration c2 = Configuration.builder().build();
        Assert.assertNotEquals(c2.hashCode(), c1.hashCode());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testHashCodeDeprecated() {
        Configuration c1 = new Configuration();
        Configuration c2 = new Configuration();
        Assert.assertEquals(c1.hashCode(), c2.hashCode());
        c2.setNodeIdType(Float.class);
        Assert.assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testCopy() {
        Configuration c1 = Configuration.builder().build();
        Configuration c2 = c1.copy();
        Assert.assertEquals(c2, c1);
        Assert.assertNotSame(c2, c1);

        Configuration c3 = Configuration.builder().nodeIdType(Float.class).build();
        Assert.assertNotEquals(c3, c1);
        Configuration c4 = c3.copy();
        Assert.assertEquals(c4, c3);
        Assert.assertEquals(Float.class, c4.getNodeIdType());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCopyDeprecated() {
        Configuration c1 = new Configuration();
        Configuration c2 = c1.copy();
        Assert.assertEquals(c2, c1);
        c1.setNodeIdType(Float.class);
        Assert.assertNotEquals(c2.getNodeIdType(), Float.class);
        Assert.assertNotEquals(c2, c1);
    }

    @Test
    public void testToConfiguration() {
        Configuration c1 = Configuration.builder().nodeIdType(Float.class).build();
        Configuration c2 = new ConfigurationImpl(c1).toConfiguration();
        Assert.assertEquals(c1, c2);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testExceptionSpatialIndexWithDisabledNodeProperties() {
        Configuration.builder().enableSpatialIndex(true).enableNodeProperties(false).build();
    }
}
