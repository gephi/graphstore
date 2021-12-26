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
package org.gephi.graph.api;

/**
 * Wrapper around <code>ReentrantReadWriteLock</code> that controls multi-thread
 * access to the graph structure.
 */
public interface GraphLock {

    /**
     * Acquires the read lock. Acquires the read lock if the write lock is not held
     * by another thread and returns immediately.
     */
    void readLock();

    /**
     * Attempts to release this lock. If the number of readers is now zero then the
     * lock is made available for write lock attempts. If the current thread does
     * not hold this lock then IllegalMonitorStateException is thrown.
     *
     * @throws IllegalMonitorStateException if the current thread does not hold this
     *         lock
     */
    void readUnlock();

    /**
     * Release this lock by releasing all current read locks.
     */
    void readUnlockAll();

    /**
     * Acquires the write lock. Acquires the write lock if neither the read nor
     * write lock are held by another thread and returns immediately, setting the
     * write lock hold count to one.
     *
     * @throws IllegalMonitorStateException if the current thread holds a read lock
     *         already
     */
    void writeLock();

    /**
     * Attempts to release this lock. If the current thread is the holder of this
     * lock then the hold count is decremented. If the hold count is now zero then
     * the lock is released. If the current thread is not the holder of this lock
     * then IllegalMonitorStateException is thrown.
     * <p>
     * throws @IllegalMonitorStateException if the current thread does not hold this
     * lock
     */
    void writeUnlock();

    /**
     * Queries the number of reentrant read holds on this lock by the current
     * thread. A reader thread has a hold on a lock for each lock action that is not
     * matched by an unlock action.
     *
     * @return the number of holds on the read lock by the current thread, or zero
     *         if the read lock is not held by the current thread
     */
    int getReadHoldCount();

    /**
     * Queries the number of reentrant write holds on this lock by the current
     * thread. A writer thread has a hold on a lock for each lock action that is not
     * matched by an unlock action.
     *
     * @return the number of holds on the write lock by the current thread, or zero
     *         if the write lock is not held by the current thread
     */
    int getWriteHoldCount();
}
