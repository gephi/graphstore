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

import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampStringMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ColumnImplTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testColumnNullId() {
        new ColumnImpl(null, null, null, this, Origin.DATA, true, false);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testColumnEmptyId() {
        new ColumnImpl("", null, null, this, Origin.DATA, true, false);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testColumnNullType() {
        new ColumnImpl("foo", null, null, this, Origin.DATA, true, false);
    }

    @Test
    public void testColumnIsDynamic() {
        ColumnImpl col1 = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        Assert.assertFalse(col1.isDynamic());

        ColumnImpl col2 = new ColumnImpl("0", TimestampDoubleMap.class, null, null, Origin.DATA, false, false);
        Assert.assertTrue(col2.isDynamic());
    }

    @Test
    public void testColumnIsArray() {
        ColumnImpl col1 = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        Assert.assertFalse(col1.isArray());

        ColumnImpl col2 = new ColumnImpl("0", int[].class, null, null, Origin.DATA, false, false);
        Assert.assertTrue(col2.isArray());
    }

    @Test
    public void testColumnGetEstimatorStaticNull() {
        ColumnImpl col = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        Assert.assertNull(col.getEstimator());
    }

    @Test
    public void testColumnGetEstimatorDynamicDefaultNull() {
        ColumnImpl col = new ColumnImpl("0", TimestampStringMap.class, null, null, Origin.DATA, false, false);
        Assert.assertEquals(col.getEstimator(), Estimator.FIRST);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testColumnSetEstimatorStatic() {
        ColumnImpl col = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        col.setEstimator(Estimator.AVERAGE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testColumnSetEstimatorNotSupported() {
        ColumnImpl col = new ColumnImpl("0", TimestampStringMap.class, null, null, Origin.DATA, false, false);
        col.setEstimator(Estimator.AVERAGE);
    }

    @Test
    public void testColumnSetEstimator() {
        ColumnImpl col = new ColumnImpl("0", TimestampDoubleMap.class, null, null, Origin.DATA, false, false);
        col.setEstimator(Estimator.SUM);
        Assert.assertEquals(col.getEstimator(), Estimator.SUM);
    }

    @Test
    public void testColumnEquals() {
        ColumnImpl col1 = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        ColumnImpl col2 = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        ColumnImpl col3 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col4 = new ColumnImpl("1", String.class, null, null, Origin.DATA, false, false);
        Assert.assertTrue(col1.equals(col2));
        Assert.assertFalse(col1.equals(col3));
        Assert.assertFalse(col1.equals(col4));
    }

    @Test
    public void testColumnHashcode() {
        ColumnImpl col1 = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        ColumnImpl col2 = new ColumnImpl("0", String.class, null, null, Origin.DATA, false, false);
        ColumnImpl col3 = new ColumnImpl("0", Integer.class, null, null, Origin.DATA, false, false);
        ColumnImpl col4 = new ColumnImpl("1", String.class, null, null, Origin.DATA, false, false);
        Assert.assertEquals(col1.hashCode(), col2.hashCode());
        Assert.assertNotEquals(col1.hashCode(), col3.hashCode());
        Assert.assertNotEquals(col1.hashCode(), col4.hashCode());
    }

    @Test
    public void testColumnDeepEquals() {
        ColumnImpl col1 = new ColumnImpl("0", TimestampDoubleMap.class, null, null, Origin.DATA, false, false);
        ColumnImpl col2 = new ColumnImpl("0", TimestampDoubleMap.class, null, null, Origin.DATA, false, false);
        Assert.assertTrue(col1.deepEquals(col2));
        col2.setEstimator(Estimator.SUM);
        Assert.assertFalse(col1.deepEquals(col2));
    }

    @Test
    public void testColumnDeepHashcode() {
        ColumnImpl col1 = new ColumnImpl("0", TimestampDoubleMap.class, null, null, Origin.DATA, false, false);
        ColumnImpl col2 = new ColumnImpl("0", TimestampDoubleMap.class, null, null, Origin.DATA, false, false);
        Assert.assertEquals(col1.deepHashCode(), col2.deepHashCode());
        col2.setEstimator(Estimator.SUM);
        Assert.assertNotEquals(col1.deepHashCode(), col2.deepHashCode());
    }
}
