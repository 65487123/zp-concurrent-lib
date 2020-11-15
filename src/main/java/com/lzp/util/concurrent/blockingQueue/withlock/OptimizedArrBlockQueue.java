package com.lzp.util.concurrent.blockingQueue.withlock;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Description: {@link java.util.concurrent.ArrayBlockingQueue}读和写用的是同一把锁，
 * 这个队列读和写用的是不同的锁。用的是jdk自带的synchronized锁，读锁锁的是当前队列对象，写锁锁的是items。
 * 经过实测，jdk优化以后的synchronized锁性能比JUC下面的锁要好
 *
 * @author: Zeping Lu
 * @date: 2020/11/10 17:15
 */
public class OptimizedArrBlockQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable{

    /**
     * 存元素的容器，并且作为读锁。 写锁
     */
    private final Object[] items;

    private volatile int takeIndex;

    private volatile int putIndex;

    private boolean putThreadsIsWaiting;

    private boolean takeThreadsIsWaiting;

    private final Object putWatingLock = new Object();

    private final Object takeWatingLock = new Object() ;

    public OptimizedArrBlockQueue(int capacity){
        this.items = new Object[capacity];
    }



    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public int size() {
        int size;
        if ((size = takeIndex - putIndex) < 0) {
            size = items.length - size;
        }
        return size;
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

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
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
            return e;
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public int remainingCapacity() {
        return 0;
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
    public boolean offer(E e) {
        return false;
    }

    @Override
    public E poll() {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }
}
