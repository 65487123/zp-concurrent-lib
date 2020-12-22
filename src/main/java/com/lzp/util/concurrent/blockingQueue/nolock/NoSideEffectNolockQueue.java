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


 package com.lzp.util.concurrent.blockingQueue.nolock;


 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;

 /**
  * Description:高性能阻塞队列，适用于任意个数生产者对任意个数消费者（线程),无锁设计。
  * 特点是几乎没什么副作用(cpu时间片、高速缓存行浪费)
  * @author: Lu ZePing
  * @date: 2020/7/20 12:19
  */
 public class NoSideEffectNolockQueue<E> extends BlockingQueueAdapter<E> {

     private E[] array;

     private final int M;

     //private final int DOUBLE_M;

     //private  final AtomicInteger totalSize = new AtomicInteger();

     private final AtomicInteger head = new AtomicInteger();

     private final AtomicInteger tail = new AtomicInteger();

     private final AtomicInteger puttingWaiterCount = new AtomicInteger();

     private final AtomicInteger takingWaiterCount = new AtomicInteger();

     public NoSideEffectNolockQueue(int preferCapacity) {
         int minCapacity = 1048576;
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
         //DOUBLE_M = M << 1;
     }

     @Override
     public void put(E obj) throws InterruptedException {
         if (obj == null) {
             throw new NullPointerException();
         }
         int p = head.getAndIncrement() & M;
         for (int i = 0; array[p] != null; i++) {
             if (puttingWaiterCount.get() == 0 && i < 50) {
                 Thread.yield();
                 continue;
             }
             puttingWaiterCount.incrementAndGet();
             synchronized (this) {
                 this.wait();
                 while (array[p] != null) {
                     this.notify();
                     this.wait();
                 }
             }
             puttingWaiterCount.decrementAndGet();
             break;
         }
         array[p] = obj;
         if (takingWaiterCount.get() > 0) {
             synchronized (this) {
                 this.notify();
             }
         }
     }


     @Override
     public E take() throws InterruptedException {
         E e;
         int p = tail.getAndIncrement() & M;
         for (int i = 0; (e = array[p]) == null; i++) {
             if (takingWaiterCount.get() == 0 && i < 50) {
                 Thread.yield();
                 continue;
             }
             takingWaiterCount.incrementAndGet();
             synchronized (this) {
                 this.wait();
                 while ((e = array[p]) == null) {
                     this.notify();
                     this.wait();
                 }
             }
             takingWaiterCount.decrementAndGet();
             break;
         }
         array[p] = null;
         if (puttingWaiterCount.get() > 0) {
             synchronized (this) {
                 this.notify();
             }
         }
         return e;
     }
 }
