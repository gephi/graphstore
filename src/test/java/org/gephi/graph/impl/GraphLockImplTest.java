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

import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphLockImplTest {

    @Test
    public void testReadUnlockAll() {
        GraphLockImpl lock = new GraphLockImpl();
        lock.readLock();
        lock.readLock();
        Assert.assertEquals(lock.readWriteLock.getReadHoldCount(), 2);
        lock.readUnlockAll();
        Assert.assertEquals(lock.readWriteLock.getReadLockCount(), 0);
    }

    @Test
    public void testWriteLockBeforeReadLock() {
        GraphLockImpl lock = new GraphLockImpl();
        lock.writeLock();
        lock.readLock();
        lock.readLock();
    }

    @Test(expectedExceptions = IllegalMonitorStateException.class)
    public void testWriteLockAfterReadLock() {
        GraphLockImpl lock = new GraphLockImpl();
        lock.readLock();
        lock.writeLock();
    }

    @Test
    public void testCheckHoldWriteLock() {
        GraphLockImpl lock = new GraphLockImpl();
        lock.writeLock();
        lock.checkHoldWriteLock();
    }

    @Test(expectedExceptions = IllegalMonitorStateException.class)
    public void testCheckHoldWriteLockFail() {
        GraphLockImpl lock = new GraphLockImpl();
        lock.checkHoldWriteLock();
    }

    @Test
    public void testHoldersCount() {
        GraphLockImpl lock = new GraphLockImpl();
        lock.readLock();
        Assert.assertEquals(lock.getReadHoldCount(), 1);
        lock.readUnlock();
        Assert.assertEquals(lock.getReadHoldCount(), 0);
        lock.writeLock();
        Assert.assertEquals(lock.getWriteHoldCount(), 1);
        lock.writeUnlock();
        Assert.assertEquals(lock.getWriteHoldCount(), 0);

    }
}
