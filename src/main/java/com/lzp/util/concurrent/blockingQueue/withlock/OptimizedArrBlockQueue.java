 /* Copyright zeping lu
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */

package com.lzp.util.concurrent.blockingQueue.withlock;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Description: {@link java.util.concurrent.ArrayBlockingQueue}读和写用的是同一把锁，
 * 这个队列读和写用的是不同的锁。用的是jdk自带的synchronized锁，读锁锁的是当前队列对象，写锁锁的是items。
 * 经过实测，jdk优化以后的synchronized锁性能比JUC下面的锁要好
 *
 * @author: Zeping Lu
 * @date: 2020/11/10 17:15
 */
public class OptimizedArrBlockQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {

    /**
     * 存元素的容器，并且作为读锁。
     */
    private final Object[] items;

    private int takeIndex;

    private int putIndex;

    private boolean putThreadsIsWaiting;

    private boolean takeThreadsIsWaiting;

    private final Object putWatingLock = new Object();

    private final Object takeWatingLock = new Object();

    public OptimizedArrBlockQueue(int capacity) {
        this.items = new Object[capacity];
    }


    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public synchronized int size() {
        synchronized (items) {
            int temp;
            if ((temp = takeIndex - putIndex) < 0) {
                return -temp;
            } else if (temp == 0) {
                return items[putIndex] == null ? 0 : items.length;
            }
            return items.length - temp;
        }
    }

    @Override
    public synchronized void put(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        while (items[putIndex] != null) {
            synchronized (putWatingLock) {
                putThreadsIsWaiting = true;
                putWatingLock.wait();
            }
        }
        enqueue(e);
    }

    @Override
    public synchronized boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        long remainingTime = unit.toMillis(timeout);
        long deadline = System.currentTimeMillis() + remainingTime;
        while (items[putIndex] != null && remainingTime > 0) {
            synchronized (putWatingLock) {
                putThreadsIsWaiting = true;
                putWatingLock.wait(remainingTime);
                remainingTime = deadline - System.currentTimeMillis();
            }
        }
        if (items[putIndex] != null) {
            putThreadsIsWaiting = false;
            return false;
        }
        enqueue(e);
        return true;
    }

    @Override
    public E take() throws InterruptedException {
        synchronized (items) {
            E e;
            while ((e = (E) items[takeIndex]) == null) {
                synchronized (takeWatingLock) {
                    takeThreadsIsWaiting = true;
                    takeWatingLock.wait();
                }
            }
            dequeue();
            return e;
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        synchronized (items) {
            long remainingTime = unit.toMillis(timeout);
            long deadline = System.currentTimeMillis() + remainingTime;
            E e;
            while ((e = (E) items[takeIndex]) == null && remainingTime > 0) {
                synchronized (takeWatingLock) {
                    takeThreadsIsWaiting = true;
                    takeWatingLock.wait(remainingTime);
                    remainingTime = deadline - System.currentTimeMillis();
                }
            }
            if (e == null) {
                return null;
            }
            dequeue();
            return e;
        }
    }

    @Override
    public synchronized int remainingCapacity() {
        synchronized (items) {
            int temp;
            if ((temp = putIndex - takeIndex) > 0) {
                return items.length - temp;
            } else if (temp == 0) {
                return items[putIndex] == null ? items.length : 0;
            } else {
                return -temp;
            }
        }
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }

    @Override
    public synchronized boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        if (items[putIndex] != null) {
            return false;
        }
        enqueue(e);
        return true;
    }

    @Override
    public E poll() {
        synchronized (items) {
            E e;
            if ((e = (E) items[takeIndex]) == null) {
                return null;
            }
            dequeue();
            return e;
        }
    }

    @Override
    public E peek() {
        return null;
    }

    private void enqueue(E e) {
        items[putIndex++] = e;
        synchronized (takeWatingLock) {
            if (takeThreadsIsWaiting) {
                takeThreadsIsWaiting = false;
                takeWatingLock.notify();
            }
        }
        if (putIndex == items.length) {
            putIndex = 0;
        }
    }

    private void dequeue() {
        items[takeIndex++] = null;
        synchronized (putWatingLock) {
            if (putThreadsIsWaiting) {
                putThreadsIsWaiting = false;
                putWatingLock.notify();
            }
        }
        if (takeIndex == items.length) {
            takeIndex = 0;
        }
    }
}
