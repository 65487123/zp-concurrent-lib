
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

package com.lzp.util.concurrent.lock;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * Description:不会进入阻塞状态的可重入锁
 * 适用场景：同步代码块中的执行代码很少,且竞争线程少,稍微自旋个几次就能拿到锁，没必要用户态切换到内核态。
 * 或用锁只为实现线程间通信(Condition的await()、signal()/signalAll());
 * (线程间通信有三种方式 wait/notify await/signal park/unpark,
 * 经过我的测试,如果运用得当,park/unpark性能最好。其次是wait/notify，await/signal最差)
 *
 * @author: Zeping Lu
 * @date: 2020/12/31 10:15
 */
public class NoBlocKReentrantLock implements Lock {

    private final Unsafe U;
    private final long VALUE_OFFSET;

    private volatile long threadId = -1;
    private final AtomicInteger entrCount = new AtomicInteger();
    private final Set<Thread> waitSet = new HashSet<>();


    class ConditionImp implements Condition{

        @Override
        public void await() throws InterruptedException {
            Thread thread = Thread.currentThread();
            waitSet.add(thread);
            unlock();
            LockSupport.park();
            lock();
        }

        @Override
        public void awaitUninterruptibly() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            Thread thread = Thread.currentThread();
            waitSet.add(thread);
            long deadLine = System.nanoTime() + nanosTimeout;
            parkAndGetLock(nanosTimeout);
            long remain = deadLine - System.nanoTime();
            if (remain < 0) {
                waitSet.remove(thread);
            }
            return remain;
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            Thread thread = Thread.currentThread();
            waitSet.add(thread);
            long deadLine = System.nanoTime() + (time = unit.toNanos(time));
            parkAndGetLock(time);
            long remain = deadLine - System.nanoTime();
            if (remain < 0) {
                waitSet.remove(thread);
            }
            return remain > 0;
        }

        private void parkAndGetLock(long time) {
            //释放锁
            unlock();
            LockSupport.park(time);
            lock();
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void signal() {
            if (waitSet.size() > 0) {
                Iterator<Thread> iterator;
                LockSupport.unpark((iterator = waitSet.iterator()).next());
                iterator.remove();
            }
        }

        @Override
        public void signalAll() {
            for (Thread thread : waitSet) {
                LockSupport.unpark(thread);
            }
            waitSet.clear();
        }
    }


    public NoBlocKReentrantLock() {
        try {
            Constructor<Unsafe> constructor = Unsafe.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            U = constructor.newInstance();
            VALUE_OFFSET = U.objectFieldOffset(NoBlocKReentrantLock.class.getDeclaredField("threadId"));
        } catch (Exception ignored) {
            throw new RuntimeException("Class initialization failed: Unsafe initialization failed");
        }
    }

    @Override
    public void lock() {
        Thread thread = Thread.currentThread();
        for (; ; ) {
            if (threadId == -1) {
                if (casThreadId(-1, thread.getId())) {
                    entrCount.getAndIncrement();
                    return;
                }
            } else if (this.threadId == (thread.getId())) {
                entrCount.getAndIncrement();
                return;
            }
            Thread.yield();
        }
    }


    @Override
    public void unlock() {
        if (Thread.currentThread().getId() != threadId) {
            throw new IllegalMonitorStateException();
        }
        //释放锁
        if (entrCount.getAndDecrement() == 1) {
            this.threadId = -1;
        }
    }



    @Override
    public Condition newCondition() {
        return new ConditionImp();
    }


    private boolean casThreadId(long val1, long val2) {
        return U.compareAndSwapLong(this, VALUE_OFFSET, val1, val2);
    }


    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
