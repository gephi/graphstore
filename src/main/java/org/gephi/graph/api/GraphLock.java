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

import java.util.concurrent.TimeUnit;

/**
 * Wrapper around <code>ReentrantReadWriteLock</code> that controls multi-thread access to the graph structure.
 */
public interface GraphLock {

    /**
     * Acquires the read lock. Acquires the read lock if the write lock is not held by another thread and returns
     * immediately.
     * <p>
     * This call waits without bound. A read hold blocks every writer, and once a writer is waiting, new readers wait
     * behind it as well, so a read hold that is never released stalls all graph operations. Do not hold the read lock
     * across a wait on another thread, and do not abandon an auto-locking iterator (see {@link NodeIterable} and
     * {@link EdgeIterable}) before it is exhausted or {@code doBreak()} has been called. Use
     * {@link #tryReadLock(long, TimeUnit)} when the caller cannot afford to wait indefinitely.
     */
    void readLock();

    /**
     * Attempts to release this lock. If the number of readers is now zero then the lock is made available for write
     * lock attempts. If the current thread does not hold this lock then IllegalMonitorStateException is thrown.
     *
     * @throws IllegalMonitorStateException if the current thread does not hold this lock
     */
    void readUnlock();

    /**
     * Release this lock by releasing all current read locks.
     */
    void readUnlockAll();

    /**
     * Acquires the write lock. Acquires the write lock if neither the read nor write lock are held by another thread
     * and returns immediately, setting the write lock hold count to one.
     *
     * @throws IllegalMonitorStateException if the current thread holds a read lock already
     * @see #tryWriteLock(long, TimeUnit)
     */
    void writeLock();

    /**
     * Attempts to release this lock. If the current thread is the holder of this lock then the hold count is
     * decremented. If the hold count is now zero then the lock is released. If the current thread is not the holder of
     * this lock then IllegalMonitorStateException is thrown.
     * <p>
     * throws @IllegalMonitorStateException if the current thread does not hold this lock
     */
    void writeUnlock();

    /**
     * Queries the number of reentrant read holds on this lock by the current thread. A reader thread has a hold on a
     * lock for each lock action that is not matched by an unlock action.
     *
     * @return the number of holds on the read lock by the current thread, or zero if the read lock is not held by the
     *         current thread
     */
    int getReadHoldCount();

    /**
     * Queries the number of reentrant write holds on this lock by the current thread. A writer thread has a hold on a
     * lock for each lock action that is not matched by an unlock action.
     *
     * @return the number of holds on the write lock by the current thread, or zero if the write lock is not held by the
     *         current thread
     */
    int getWriteHoldCount();

    /**
     * Acquires the read lock if the write lock is not held by another thread within the given waiting time.
     * <p>
     * Unlike {@link #readLock()}, the wait is bounded and interruptible. A caller that receives {@code false} has not
     * acquired the lock and must not call {@link #readUnlock()}.
     *
     * @param timeout the time to wait for the read lock
     * @param unit the time unit of the timeout argument
     * @return true if the read lock was acquired
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws UnsupportedOperationException if the implementation does not support timed acquisition
     */
    default boolean tryReadLock(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    /**
     * Acquires the write lock if neither the read nor write lock are held by another thread within the given waiting
     * time.
     * <p>
     * Unlike {@link #writeLock()}, the wait is bounded and interruptible. A caller that receives {@code false} has not
     * acquired the lock and must not call {@link #writeUnlock()}.
     *
     * @param timeout the time to wait for the write lock
     * @param unit the time unit of the timeout argument
     * @return true if the write lock was acquired
     * @throws IllegalMonitorStateException if the current thread holds a read lock already
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws UnsupportedOperationException if the implementation does not support timed acquisition
     */
    default boolean tryWriteLock(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    /**
     * Queries the number of read holds on this lock across all threads. This differs from {@link #getReadHoldCount()},
     * which counts only the current thread. A non-zero value while no thread is expected to be reading points at a read
     * hold that was never released.
     *
     * @return the total number of read holds, or zero if the read lock is not held
     * @throws UnsupportedOperationException if the implementation does not expose this
     */
    default int getReadLockCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * Queries whether the write lock is held by any thread.
     *
     * @return true if any thread holds the write lock
     * @throws UnsupportedOperationException if the implementation does not expose this
     */
    default boolean isWriteLocked() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an estimate of the number of threads waiting to acquire either the read or the write lock. The value is
     * an estimate because the number of threads may change while this method traverses internal data structures. It is
     * designed for monitoring, not for synchronization control.
     *
     * @return the estimated number of waiting threads
     * @throws UnsupportedOperationException if the implementation does not expose this
     */
    default int getQueueLength() {
        throw new UnsupportedOperationException();
    }
}
