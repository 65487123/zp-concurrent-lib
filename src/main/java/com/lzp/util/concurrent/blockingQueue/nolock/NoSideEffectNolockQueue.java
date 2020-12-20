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
  * Description:高性能阻塞队列，适用于一个生产者对一个消费者（线程),无锁设计。
  *
  * @author: Lu ZePing
  * @date: 2020/7/20 12:19
  */
 public class NoSideEffectNolockQueue<E> extends BlockingQueueAdapter<E> {

     private E[] array;

     private final int M;

     private final int DOUBLE_M;

     private AtomicInteger head = new AtomicInteger();

     private AtomicInteger tail = new AtomicInteger();

     private AtomicInteger totalSize = new AtomicInteger();


     public NoSideEffectNolockQueue(int capacity) {
         if (capacity <= 0) {
             throw new IllegalArgumentException();
         }
         array = (E[]) new Object[capacity];
         //int占4个字节
         M = capacity - 1;
         DOUBLE_M = M << 1;
     }

     @Override
     public void put(E obj) throws InterruptedException {
         if (obj == null) {
             throw new NullPointerException();
         }
         if (totalSize.get() > M) {
             while (totalSize.get() > M) {
                 Thread.yield();
             }
         }
         int p = head.getAndIncrement() & M;
         while (array[p] != null) {
             synchronized (this) {
                 this.wait();
             }
         }
         array[p] = obj;
         if (totalSize.incrementAndGet() == 1) {
             synchronized (this) {
                 this.notifyAll();
             }
         }
     }


     @Override
     public E take() throws InterruptedException {
         E e;
         if (totalSize.get() == 0) {
             while (totalSize.get() == 0) {
                 Thread.yield();
             }
         }
         int p = tail.getAndIncrement() & M;
         while ((e = array[p]) == null) {
             synchronized (this) {
                 this.wait();
             }
         }
         array[p] = null;
         if (totalSize.get() == M) {
             synchronized (this) {
                 this.notifyAll();
             }
         }
         return e;
     }
 }
