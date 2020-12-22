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


 import java.util.concurrent.atomic.AtomicInteger;

 /**
  * Description:高性能阻塞队列，适用于多个生产者对多个消费者（线程),无锁设计。
  * 特点是几乎没什么副作用
  * @author: Lu ZePing
  * @date: 2020/7/20 12:19
  */
 public class NoSideEffectNolockQueue<E> extends BlockingQueueAdapter<E> {

     private E[] array;

     private final int M;

     private AtomicInteger head = new AtomicInteger();

     private AtomicInteger tail = new AtomicInteger();

     private AtomicInteger puttingWaiterCount = new AtomicInteger();

     private AtomicInteger takingWaiterCount = new AtomicInteger();

     public NoSideEffectNolockQueue(int preferCapacity) {
         if (preferCapacity <= 0) {
             throw new IllegalArgumentException();
         }
         int capacity = tableSizeFor(preferCapacity);
         array = (E[]) new Object[capacity];
         M = capacity - 1;
     }

     @Override
     public void put(E obj) throws InterruptedException {
         if (obj == null) {
             throw new NullPointerException();
         }

         int p = head.getAndIncrement() & M;
         for (int i = 0 ;array[p] != null;i++) {
             if (i<100){
                 Thread.yield();
                 continue;
             }
             puttingWaiterCount.incrementAndGet();
             synchronized (this) {
                 this.wait();
             }
             puttingWaiterCount.decrementAndGet();
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
             if (i < 100) {
                 Thread.yield();
                 continue;
             }
             takingWaiterCount.incrementAndGet();
             synchronized (this) {
                 this.wait();
             }
             takingWaiterCount.decrementAndGet();
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
