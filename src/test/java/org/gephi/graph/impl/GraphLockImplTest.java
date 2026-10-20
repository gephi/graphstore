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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    // --- Timed acquisition ---

    @Test
    public void testTryWriteLockAcquiresWhenFree() throws InterruptedException {
        GraphLockImpl lock = new GraphLockImpl();
        Assert.assertTrue(lock.tryWriteLock(1, TimeUnit.SECONDS));
        Assert.assertEquals(lock.getWriteHoldCount(), 1);
        lock.writeUnlock();
    }

    @Test
    public void testTryWriteLockTimesOutWhileAnotherThreadHoldsRead() throws InterruptedException {
        GraphLockImpl lock = new GraphLockImpl();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread reader = holdLockOnThread(lock, false, acquired, release);
        acquired.await();
        Assert.assertFalse(lock.tryWriteLock(100, TimeUnit.MILLISECONDS));
        Assert.assertEquals(lock.getWriteHoldCount(), 0);
        release.countDown();
        reader.join();
        Assert.assertTrue(lock.tryWriteLock(1, TimeUnit.SECONDS));
        lock.writeUnlock();
    }

    @Test(expectedExceptions = IllegalMonitorStateException.class)
    public void testTryWriteLockWhileHoldingReadLock() throws InterruptedException {
        GraphLockImpl lock = new GraphLockImpl();
        lock.readLock();
        lock.tryWriteLock(1, TimeUnit.SECONDS);
    }

    @Test
    public void testTryReadLockAcquiresWhenFree() throws InterruptedException {
        GraphLockImpl lock = new GraphLockImpl();
        Assert.assertTrue(lock.tryReadLock(1, TimeUnit.SECONDS));
        Assert.assertEquals(lock.getReadHoldCount(), 1);
        lock.readUnlock();
    }

    @Test
    public void testTryReadLockTimesOutWhileAnotherThreadHoldsWrite() throws InterruptedException {
        GraphLockImpl lock = new GraphLockImpl();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread writer = holdLockOnThread(lock, true, acquired, release);
        acquired.await();
        Assert.assertFalse(lock.tryReadLock(100, TimeUnit.MILLISECONDS));
        Assert.assertEquals(lock.getReadHoldCount(), 0);
        release.countDown();
        writer.join();
        Assert.assertTrue(lock.tryReadLock(1, TimeUnit.SECONDS));
        lock.readUnlock();
    }

    @Test
    public void testTryWriteLockPropagatesInterrupt() throws InterruptedException {
        GraphLockImpl lock = new GraphLockImpl();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread reader = holdLockOnThread(lock, false, acquired, release);
        acquired.await();
        AtomicBoolean interrupted = new AtomicBoolean(false);
        CountDownLatch waiting = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            try {
                waiting.countDown();
                lock.tryWriteLock(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        writer.start();
        waiting.await();
        writer.interrupt();
        writer.join(5000);
        Assert.assertFalse(writer.isAlive());
        Assert.assertTrue(interrupted.get());
        Assert.assertEquals(lock.getWriteHoldCount(), 0);
        release.countDown();
        reader.join();
    }

    // --- Diagnostics ---

    @Test
    public void testGetQueueLengthReportsQueuedWriter() throws InterruptedException {
        GraphLockImpl lock = new GraphLockImpl();
        lock.readLock();
        Assert.assertEquals(lock.getQueueLength(), 0);
        Thread writer = new Thread(() -> {
            lock.writeLock();
            lock.writeUnlock();
        });
        writer.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (lock.getQueueLength() == 0 && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
        Assert.assertEquals(lock.getQueueLength(), 1);
        lock.readUnlock();
        writer.join();
        Assert.assertEquals(lock.getQueueLength(), 0);
    }

    @Test
    public void testGetReadLockCountCountsHoldsAcrossThreads() throws InterruptedException {
        GraphLockImpl lock = new GraphLockImpl();
        Assert.assertEquals(lock.getReadLockCount(), 0);
        lock.readLock();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread reader = holdLockOnThread(lock, false, acquired, release);
        acquired.await();
        Assert.assertEquals(lock.getReadLockCount(), 2);
        Assert.assertEquals(lock.getReadHoldCount(), 1);
        release.countDown();
        reader.join();
        Assert.assertEquals(lock.getReadLockCount(), 1);
        lock.readUnlock();
        Assert.assertEquals(lock.getReadLockCount(), 0);
    }

    @Test
    public void testIsWriteLocked() {
        GraphLockImpl lock = new GraphLockImpl();
        Assert.assertFalse(lock.isWriteLocked());
        lock.writeLock();
        Assert.assertTrue(lock.isWriteLocked());
        lock.writeUnlock();
        Assert.assertFalse(lock.isWriteLocked());
    }

    // Holds the read (or write) lock on a background thread until released, so the test thread can observe contention.
    private static Thread holdLockOnThread(GraphLockImpl lock, boolean write, CountDownLatch acquired, CountDownLatch release) {
        Thread t = new Thread(() -> {
            if (write) {
                lock.writeLock();
            } else {
                lock.readLock();
            }
            acquired.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (write) {
                    lock.writeUnlock();
                } else {
                    lock.readUnlock();
                }
            }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }
}
