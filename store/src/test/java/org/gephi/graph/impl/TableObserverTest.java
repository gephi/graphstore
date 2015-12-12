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

import java.util.Arrays;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TableDiff;
import org.gephi.graph.api.types.TimestampIntegerMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TableObserverTest {

    @Test
    public void testDefaultObserver() {
        TableImpl table = new TableImpl(Node.class, false);
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(false);

        Assert.assertFalse(tableObserver.destroyed);
        Assert.assertEquals(table.deepHashCode(), tableObserver.tableHash);
        Assert.assertTrue(table.store.observers.contains(tableObserver));

        Assert.assertFalse(tableObserver.hasTableChanged());
    }

    @Test
    public void testObserverAddColumn() {
        TableImpl table = new TableImpl(Node.class, false);
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(false);

        table.addColumn("0", Integer.class);

        Assert.assertTrue(tableObserver.hasTableChanged());
        Assert.assertFalse(tableObserver.hasTableChanged());
    }

    @Test
    public void testObserverRemoveColumn() {
        TableImpl table = new TableImpl(Node.class, false);
        table.addColumn("0", Integer.class);
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(false);
        table.removeColumn("0");

        Assert.assertTrue(tableObserver.hasTableChanged());
        Assert.assertFalse(tableObserver.hasTableChanged());
    }

    @Test
    public void testObserverModifyColumn() {
        TableImpl table = new TableImpl(Node.class, false);
        Column col = table.addColumn("0", TimestampIntegerMap.class);
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(false);
        col.setEstimator(Estimator.MAX);

        Assert.assertTrue(tableObserver.hasTableChanged());
        Assert.assertFalse(tableObserver.hasTableChanged());
    }

    @Test
    public void testDestroyObserver() {
        TableImpl table = new TableImpl(Node.class, false);
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(false);

        tableObserver.destroy();

        Assert.assertTrue(tableObserver.destroyed);
        Assert.assertFalse(table.store.observers.contains(tableObserver));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetDiffWithoutSetting() {
        TableImpl table = new TableImpl(Node.class, false);
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(false);
        tableObserver.getDiff();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetDiffWithoutHasGraphChanged() {
        TableImpl table = new TableImpl(Node.class, false);
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(true);
        tableObserver.getDiff();
    }

    @Test
    public void testDiffRemoveColumn() {
        TableImpl table = new TableImpl(Node.class, false);
        table.addColumn("0", Integer.class);
        Column[] columns = table.toArray();
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(true);
        table.removeColumn("0");

        Assert.assertTrue(tableObserver.hasTableChanged());
        final TableDiff tableDiff = tableObserver.getDiff();
        Assert.assertNotNull(tableDiff);

        Assert.assertEquals(Arrays.asList(columns), tableDiff.getRemovedColumns());
        Assert.assertTrue(tableDiff.getAddedColumns().isEmpty());
        Assert.assertTrue(tableDiff.getModifiedColumns().isEmpty());
    }

    @Test
    public void testDiffAddColumn() {
        TableImpl table = new TableImpl(Node.class, false);
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(true);
        table.addColumn("0", Integer.class);
        Column[] columns = table.toArray();

        Assert.assertTrue(tableObserver.hasTableChanged());
        final TableDiff tableDiff = tableObserver.getDiff();
        Assert.assertNotNull(tableDiff);

        Assert.assertEquals(Arrays.asList(columns), tableDiff.getAddedColumns());
        Assert.assertTrue(tableDiff.getRemovedColumns().isEmpty());
        Assert.assertTrue(tableDiff.getModifiedColumns().isEmpty());
    }

    @Test
    public void testDiffModifyColumn() {
        TableImpl table = new TableImpl(Node.class, false);
        Column col = table.addColumn("0", TimestampIntegerMap.class);
        TableObserverImpl tableObserver = (TableObserverImpl) table.createTableObserver(true);
        col.setEstimator(Estimator.AVERAGE);

        Assert.assertTrue(tableObserver.hasTableChanged());
        final TableDiff tableDiff = tableObserver.getDiff();
        Assert.assertNotNull(tableDiff);

        Assert.assertEquals(Arrays.asList(new Column[]{col}), tableDiff.getModifiedColumns());
        Assert.assertTrue(tableDiff.getRemovedColumns().isEmpty());
        Assert.assertTrue(tableDiff.getAddedColumns().isEmpty());
    }
}
