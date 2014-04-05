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
package org.gephi.graph.benchmark;

import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LockingBenchmark {

    private final DataStruture struture = new DataStruture();
    private double number;
    private final int READS = 500;
    private final int WRITES = 100;
    private final int READER_THREADS = 4;
    private final int WRITER_THREADS = 4;

    public Runnable readWithoutLock() {
        return new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < READS; i++) {
                    struture.read();
                }
            }
        };
    }

    public Runnable readWithLock() {
        final ReadWriteLock lock = new ReentrantReadWriteLock(true);
        return new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < READS; i++) {
                    lock.readLock().lock();
                    struture.read();
                    lock.readLock().unlock();
                }
            }
        };
    }

    public Runnable fairReadOnly() {
        final ReadWriteLock lock = new ReentrantReadWriteLock(true);
        return new Runnable() {
            @Override
            public void run() {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < READS; i++) {
                            lock.readLock().lock();
                            struture.read();
                            lock.readLock().unlock();
                        }
                    }
                };
                Thread[] threads = new Thread[READER_THREADS];
                for (int i = 0; i < READER_THREADS; i++) {
                    Thread thread = new Thread(r);
                    thread.start();
                    threads[i] = thread;
                }
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LockingBenchmark.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
    }

    public Runnable unfairReadOnly() {
        final ReadWriteLock lock = new ReentrantReadWriteLock(false);
        return new Runnable() {
            @Override
            public void run() {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < READS; i++) {
                            lock.readLock().lock();
                            struture.read();
                            lock.readLock().unlock();
                        }
                    }
                };
                Thread[] threads = new Thread[READER_THREADS];
                for (int i = 0; i < READER_THREADS; i++) {
                    Thread thread = new Thread(r);
                    thread.start();
                    threads[i] = thread;
                }
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LockingBenchmark.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
    }

    public Runnable fairWriteOnly() {
        final ReadWriteLock lock = new ReentrantReadWriteLock(true);
        return new Runnable() {
            @Override
            public void run() {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < WRITES; i++) {
                            lock.writeLock().lock();
                            struture.write();
                            lock.writeLock().unlock();
                        }
                    }
                };
                Thread[] threads = new Thread[READER_THREADS];
                for (int i = 0; i < READER_THREADS; i++) {
                    Thread thread = new Thread(r);
                    thread.start();
                    threads[i] = thread;
                }
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LockingBenchmark.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
    }

    public Runnable unfairWriteOnly() {
        final ReadWriteLock lock = new ReentrantReadWriteLock(false);
        return new Runnable() {
            @Override
            public void run() {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < WRITES; i++) {
                            lock.writeLock().lock();
                            struture.write();
                            lock.writeLock().unlock();
                        }
                    }
                };
                Thread[] threads = new Thread[READER_THREADS];
                for (int i = 0; i < READER_THREADS; i++) {
                    Thread thread = new Thread(r);
                    thread.start();
                    threads[i] = thread;
                }
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LockingBenchmark.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
    }

    public Runnable fairReadWrites() {
        final ReadWriteLock lock = new ReentrantReadWriteLock(true);
        return new Runnable() {
            @Override
            public void run() {
                Runnable reader = new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < READS; i++) {
                            lock.readLock().lock();
                            struture.read();
                            lock.readLock().unlock();
                        }
                    }
                };
                Runnable writer = new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < WRITES; i++) {
                            lock.writeLock().lock();
                            struture.write();
                            lock.writeLock().unlock();
                        }
                    }
                };
                Thread[] threads = new Thread[READER_THREADS + WRITER_THREADS];
                for (int i = 0; i < READER_THREADS; i++) {
                    Thread thread = new Thread(reader);
                    threads[i] = thread;
                }
                for (int i = 0; i < WRITER_THREADS; i++) {
                    Thread thread = new Thread(writer);
                    threads[READER_THREADS + i] = thread;
                }
                for (Thread thread : threads) {
                    thread.start();
                }
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LockingBenchmark.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
    }

    public Runnable unfairReadWrites() {
        final ReadWriteLock lock = new ReentrantReadWriteLock(false);
        return new Runnable() {
            @Override
            public void run() {
                Runnable reader = new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < READS; i++) {
                            lock.readLock().lock();
                            struture.read();
                            lock.readLock().unlock();
                        }
                    }
                };
                Runnable writer = new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < WRITES; i++) {
                            lock.writeLock().lock();
                            struture.write();
                            lock.writeLock().unlock();
                        }
                    }
                };
                Thread[] threads = new Thread[READER_THREADS + WRITER_THREADS];
                for (int i = 0; i < READER_THREADS; i++) {
                    Thread thread = new Thread(reader);
                    threads[i] = thread;
                }
                for (int i = 0; i < WRITER_THREADS; i++) {
                    Thread thread = new Thread(writer);
                    threads[READER_THREADS + i] = thread;
                }
                for (Thread thread : threads) {
                    thread.start();
                }
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LockingBenchmark.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
    }

    private class DataStruture {

        private final int[] values = new int[10000];
        private final int readLoops = 10;
        private final int writeLoops = 5;

        public DataStruture() {
            Random rand = new Random(454);
            for (int i = 0; i < values.length; i++) {
                values[i] = rand.nextInt(values.length);
            }
        }

        public void write() {
            Random rand = new Random(45445);
            for (int i = 0; i < writeLoops; i++) {
                for (int j = 0; j < values.length; j++) {
                    values[j] = rand.nextInt(values.length);
                }
            }
        }

        public void read() {
            double avg = 0;
            for (int i = 0; i < readLoops; i++) {
                double sum = 0;
                for (int j = 0; j < values.length; j++) {
                    sum += values[j];
                }
                sum /= values.length;
                avg += sum;
            }
            avg /= readLoops;
            number = avg;
        }
    }
}
