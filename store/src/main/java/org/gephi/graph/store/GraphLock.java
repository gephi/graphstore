package org.gephi.graph.store;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 *
 * @author mbastian
 */
public class GraphLock {

    protected final ReentrantReadWriteLock readWriteLock;
    protected final ReadLock readLock;
    protected final WriteLock writeLock;

    public GraphLock() {
        readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    //Locking
    public void readLock() {
        readLock.lock();
    }

    public void readUnlock() {
        readLock.unlock();
    }

    public void readUnlockAll() {
        final int nReadLocks = readWriteLock.getReadHoldCount();
        for (int n = 0; n < nReadLocks; n++) {
            readLock.unlock();
        }
    }

    public void writeLock() {
        if (readWriteLock.getReadHoldCount() > 0) {
            throw new IllegalMonitorStateException("Impossible to acquire a write lock when currently holding a read lock. Use toArray() methods on NodeIterable and EdgeIterable to avoid holding a readLock or wrap your loop with a write lock.");
        }
        writeLock.lock();
    }

    public void writeUnlock() {
        writeLock.unlock();
    }
    
    public void checkHoldWriteLock() {
        if(!readWriteLock.isWriteLockedByCurrentThread()) {
            throw new IllegalArgumentException("Impossible to perform a write operation while holding only a read lock. Wrap your code with a write loop to solve this.");
        }
    }
}
