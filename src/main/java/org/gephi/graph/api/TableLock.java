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

public interface TableLock {

    /**
     * Acquires the lock. Acquires the lock if it is not held by another thread and
     * returns immediately, setting the lock hold count to one.
     */
    void lock();

    /**
     * Attempts to release this lock. If the current thread is the holder of this
     * lock then the hold count is decremented. If the hold count is now zero then
     * the lock is released. If the current thread is not the holder of this lock
     * then IllegalMonitorStateException is thrown.
     *
     * @throws IllegalMonitorStateException if the current thread does not hold this
     *         lock
     */
    void unlock();

    /**
     * Queries the number of holds on this lock by the current thread. A thread has
     * a hold on a lock for each lock action that is not matched by an unlock
     * action.
     *
     * @return the number of holds on this lock by the current thread, or zero if
     *         this lock is not held by the current thread
     */
    int getHoldCount();
}
