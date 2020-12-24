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


 package com.lzp.util.concurrent.blockingQueue.lockless;


 import java.util.concurrent.atomic.AtomicInteger;

 /**
  * Description:高性能阻塞队列，适用于任意个数生产者对任意个数消费者（线程),无锁设计。
  * 特点是几乎没什么副作用(cpu时间片、高速缓存行浪费)
  *
  * @author: Lu ZePing
  * @date: 2020/7/20 12:19
  */
 public class NoSideEffectLocklessQueue<E> extends BlockingQueueAdapter<E> {

     private E[] array;

     private final int M;


     private final AtomicInteger totalSize = new AtomicInteger();

     private final AtomicInteger head = new AtomicInteger();

     private final AtomicInteger tail = new AtomicInteger();

     private final AtomicInteger puttingWaiterCount = new AtomicInteger();

     private final AtomicInteger takingWaiterCount = new AtomicInteger();

     public NoSideEffectLocklessQueue(int preferCapacity) {
         int minCapacity = 64;
         if (preferCapacity <= 0) {
             throw new IllegalArgumentException();
         } else if (preferCapacity < minCapacity) {
             array = (E[]) new Object[minCapacity];
             M = minCapacity - 1;
         } else {
             int capacity = tableSizeFor(preferCapacity);
             array = (E[]) new Object[capacity];
             M = capacity - 1;
         }
     }

     public NoSideEffectLocklessQueue() {
         int minCapacity = 64;
         array = (E[]) new Object[minCapacity];
         M = minCapacity - 1;
     }

     @Override
     public void put(E obj) throws InterruptedException {
         if (obj == null) {
             throw new NullPointerException();
         }
         //如果头指针已经倒追尾指针了，就自旋，防止两个或以上的线程数量同时等待同一个索引位置。直到有位置释放出来
         while (totalSize.incrementAndGet() > M) {
             totalSize.decrementAndGet();
             Thread.yield();
         }
         int p = head.getAndIncrement() & M;
         for (int i = 0; array[p] != null; i++) {
             //如果已经有put线程在阻塞等待了，说明取元素并不频繁，自旋没意义
             if (puttingWaiterCount.get() == 0 && i < 50) {
                 Thread.yield();
                 continue;
             }
             puttingWaiterCount.incrementAndGet();
             synchronized (head) {
                 if (array[p] == null) {
                     break;
                 }
                 head.wait();
                 while (array[p] != null) {
                     head.wait();
                 }
             }
             puttingWaiterCount.decrementAndGet();
             break;
         }
         array[p] = obj;
         if (takingWaiterCount.get() > 0) {
             //如果有取元素的线程在阻塞等待，则notify
             synchronized (this) {
                 this.notifyAll();
             }
         }
     }


     @Override
     public E take() throws InterruptedException {
         E e;
         //如果尾指针已经倒追头指针了，就自旋，防止两个或以上的线程数量同时等待同一个索引位置。直到有位置释放出来
         while (totalSize.decrementAndGet() < -M) {
             totalSize.incrementAndGet();
             Thread.yield();
         }
         int p = tail.getAndIncrement() & M;
         for (int i = 0; (e = array[p]) == null; i++) {
             //如果已经有take线程在阻塞等待了，说明取元素并不频繁，自旋没意义
             if (takingWaiterCount.get() == 0 && i < 50) {
                 Thread.yield();
                 continue;
             }
             takingWaiterCount.incrementAndGet();
             synchronized (this) {
                 if ((e = array[p]) != null) {
                     break;
                 }
                 this.wait();
                 while ((e = array[p]) == null) {
                     this.wait();
                 }
             }
             takingWaiterCount.decrementAndGet();
             break;
         }
         array[p] = null;
         if (puttingWaiterCount.get() > 0) {
             synchronized (head) {
                 head.notifyAll();
             }
         }
         return e;
     }

     @Override
     public int size() {
         int size = totalSize.get();
         if (size > this.array.length) {
             return array.length;
         } else {
             return Math.max(size, 0);
         }
     }
 }
