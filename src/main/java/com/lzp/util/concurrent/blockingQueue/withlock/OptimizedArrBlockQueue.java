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
 import java.util.concurrent.atomic.AtomicInteger;

 /**
  * Description:
  * 相比{@link java.util.concurrent.ArrayBlockingQueue}优势
  * 性能高：
  * 1、ArrayBlockingQueue取元素和放元素用的是同一把锁，多线程同时读写时严重影响性能
  * 而我这个这个队列取元素和放元素锁的是不同的对象
  * 2、ArrayBlockingQueue用的是JUC里的Lock锁，而我这个用的是synchronized锁，
  * 经过实测，synchronized(jdk1.6以后)性能比JUC里的lock锁性能高
  *
  * @author: Zeping Lu
  * @date: 2020/11/10 17:15
  */
 public class OptimizedArrBlockQueue<E> extends AbstractQueue<E>
         implements BlockingQueue<E>, java.io.Serializable {

     private class Itr implements Iterator<E> {

         private Object[] elementDataView;
         private int index = 0;

         {
             synchronized (this) {
                 synchronized (items) {
                     int temp;
                     if ((temp = takeIndex - putIndex) < 0) {
                         elementDataView = new Object[-temp];
                         System.arraycopy(items, takeIndex, elementDataView, 0, -temp);
                     } else if (temp == 0) {
                         if (items[putIndex] == null) {
                             elementDataView = new Object[0];
                         } else {
                             elementDataView = new Object[items.length];
                             System.arraycopy(items, takeIndex, elementDataView, 0, items.length - takeIndex);
                             System.arraycopy(items, 0, elementDataView, putIndex + 1, takeIndex);
                         }
                     } else {
                         elementDataView = new Object[items.length - temp];
                         System.arraycopy(items, takeIndex, elementDataView, 0, items.length - takeIndex);
                         System.arraycopy(items, 0, elementDataView, items.length - takeIndex, putIndex);
                     }
                 }
             }
         }

         @Override
         public boolean hasNext() {
             return index != elementDataView.length;
         }

         @Override
         public E next() {
             return (E) elementDataView[index++];
         }
     }

     private int takeIndex;
     /**
      * 存元素的容器，并且作为读锁。
      */
     private transient final Object[] items;

     private final Object putWatingLock = new Object();

     private final Object takeWatingLock = new Object();

     private final AtomicInteger size = new AtomicInteger();

     private int putIndex;

     public OptimizedArrBlockQueue(int capacity) {
         this.items = new Object[capacity];
     }


     @Override
     public Iterator<E> iterator() {
         return new Itr();
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
         Object[] items = this.items;
         //这个操作能保证元素放取的可见性
         if (size.get() == items.length) {
             //有自旋一次的意思
             Thread.yield();
             while (items[putIndex] != null) {
                 synchronized (putWatingLock) {
                     if (items[putIndex] == null) {
                         break;
                     }
                     putWatingLock.wait();
                 }
             }
         }
         enqueue(e, items);
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
                 if (items[putIndex] == null) {
                     break;
                 }
                 putWatingLock.wait(remainingTime);
                 remainingTime = deadline - System.currentTimeMillis();
             }
         }
         if (items[putIndex] != null) {
             return false;
         }
         enqueue(e, items);
         return true;
     }

     @Override
     public E take() throws InterruptedException {
         Object[] items = this.items;
         synchronized (items) {
             E e;
             //这个操作能保证元素放取的可见性
             if (size.get() == 0) {
                 //有自旋一次的意思
                 Thread.yield();
                 while ((e = (E) items[takeIndex]) == null) {
                     synchronized (takeWatingLock) {
                         if ((e = (E) items[takeIndex]) != null) {
                             break;
                         }
                         takeWatingLock.wait();
                     }
                 }
             } else {
                 e = (E) items[takeIndex];
             }
             dequeue(items);
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
                     if ((e = (E) items[takeIndex]) != null) {
                         break;
                     }
                     takeWatingLock.wait(remainingTime);
                     remainingTime = deadline - System.currentTimeMillis();
                 }
             }
             if (e == null) {
                 return null;
             }
             dequeue(items);
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
         enqueue(e, items);
         return true;
     }

     @Override
     public E poll() {
         synchronized (items) {
             E e;
             if ((e = (E) items[takeIndex]) == null) {
                 return null;
             }
             dequeue(items);
             return e;
         }
     }

     @Override
     public E peek() {
         throw new UnsupportedOperationException();
     }

     private void enqueue(E e, Object[] items) {
         items[putIndex++] = e;
         if (size.getAndIncrement() == 0) {
             synchronized (takeWatingLock) {
                 takeWatingLock.notify();
             }
         }
         if (putIndex == items.length) {
             putIndex = 0;
         }
     }

     private void dequeue(Object[] items) {
         items[takeIndex++] = null;
         if (size.getAndDecrement() == items.length) {
             synchronized (putWatingLock) {
                 putWatingLock.notify();
             }
         }
         if (takeIndex == items.length) {
             takeIndex = 0;
         }
     }
 }
