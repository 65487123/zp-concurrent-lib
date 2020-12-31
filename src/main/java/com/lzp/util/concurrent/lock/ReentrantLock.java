
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * Description:实际上juc里的lock锁性能是比synchronized要差的，但是JIT对synchronized锁的优化
 * 力度不如lock大（或者说jit对synchronized根本没有优化作用)，导致开启jit，多线程高并发竞争时，
 * lock锁比synchronized要快，而关闭jit，lock锁性能和synchronized完全没法比。
 * 这个类就是借鉴jdk1.6以后的synchronized，用java实现一便并做了一些优化
 * @author: Zeping Lu
 * @date: 2020/12/31 10:15
 */
public class ReentrantLock implements Lock {
    private int count = 0;
    private volatile long threadId = -1;
    private final long VALUE_OFFSET;
    private volatile AtomicInteger lockHeldState = new AtomicInteger();
    private final Unsafe U;
    private final LinkedBlockingQueue<Thread> entryList = new LinkedBlockingQueue();
    private final LinkedBlockingQueue<Thread> waitSet = new LinkedBlockingQueue();

    public ReentrantLock() {
        try {
            Constructor<Unsafe> constructor = Unsafe.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            U = constructor.newInstance();
            VALUE_OFFSET = U.objectFieldOffset(ReentrantLock.class.getDeclaredField("threadId"));
        } catch (Exception ignored) {
            throw new RuntimeException("Class initialization failed: Unsafe initialization failed");
        }
    }

    @Override
    public void lock() {
        long thisThreadId;
        long threadId = this.threadId;
        if (threadId == (thisThreadId = Thread.currentThread().getId())) {
            count++;
        } else if (threadId == -1) {
            lockHeldState.getAndIncrement();
            if (casThreadIdFailed(-1, thisThreadId)) {
                lockHeldState.getAndDecrement();
                Thread.yield();
                acquireLock(thisThreadId);
            }
        } else {
            if (lockHeldState.getAndIncrement()==0){
                if (casThreadIdFailed(threadId,thisThreadId)){
                    lockHeldState.getAndDecrement();
                    acquireLock(thisThreadId);
                }
            }else {
                acquireLock(thisThreadId);
            }
        }
    }

    private void acquireLock(long thisThreadId) {
        long threadId = this.threadId;
        for (int i = 0; i < 10; i++) {
            if (this.lockHeldState.get() == 0) {
                lockHeldState.getAndIncrement();
                if (casThreadIdFailed(threadId, thisThreadId)) {
                    lockHeldState.getAndDecrement();
                    break;
                } else {
                    return;
                }
            }
            Thread.yield();
        }
        entryList.offer(Thread.currentThread());
        do {
            //如果在之前已经被unpark()过，会直接醒，然后清除标志
            LockSupport.park(Thread.currentThread());
        } while (this.threadId != thisThreadId);
    }


    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlock() {
        if (count == 0) {

        } else {
            count--;
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    private boolean casThreadIdFailed(long val1, long val2) {
        return !U.compareAndSwapLong(this, VALUE_OFFSET, val1, val2);
    }
}
