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

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.gephi.graph.api.GraphLock;

public class GraphLockImpl implements GraphLock {

    protected final ReentrantReadWriteLock readWriteLock;
    protected final ReadLock readLock;
    protected final WriteLock writeLock;

    public GraphLockImpl() {
        readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    @Override
    public void readLock() {
        readLock.lock();
    }

    @Override
    public void readUnlock() {
        readLock.unlock();
    }

    @Override
    public void readUnlockAll() {
        final int nReadLocks = readWriteLock.getReadHoldCount();
        for (int n = 0; n < nReadLocks; n++) {
            readLock.unlock();
        }
    }

    @Override
    public void writeLock() {
        if (readWriteLock.getReadHoldCount() > 0 && !readWriteLock.isWriteLockedByCurrentThread()) {
            throw new IllegalMonitorStateException(
                    "Impossible to acquire a write lock when currently holding a read lock. Use toArray() methods on NodeIterable and EdgeIterable to avoid holding a readLock or wrap your loop with a write lock.");
        }
        writeLock.lock();
    }

    @Override
    public void writeUnlock() {
        writeLock.unlock();
    }

    @Override
    public int getReadHoldCount() {
        return readWriteLock.getReadHoldCount();
    }

    @Override
    public int getWriteHoldCount() {
        return readWriteLock.getWriteHoldCount();
    }

    public void checkHoldWriteLock() {
        if (!readWriteLock.isWriteLockedByCurrentThread()) {
            throw new IllegalMonitorStateException(
                    "Impossible to perform a write operation without lock. Wrap your code with a write lock to solve this.");
        }
    }
}
