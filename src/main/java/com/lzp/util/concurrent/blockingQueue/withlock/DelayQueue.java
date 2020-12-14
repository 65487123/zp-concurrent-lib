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


 import sun.misc.SharedSecrets;

 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Delayed;
 import java.util.concurrent.TimeUnit;



 /**
  * Description:延迟队列
  * 和{@link java.util.concurrent.DelayQueue}区别
  * 1、{@link java.util.concurrent.DelayQueue}用的是Lock锁，
  * jdk优化后的synchronized锁性能是比lock锁要好的,所以这里我用
  * synchronized锁再实现了一个延迟队列。
  * 2、由于延迟队列中的优先级队列只供这个延迟队列使用，
  * 对优先级队列进行了修改，减少了不必需要的操作。
  *
  * @author: Zeping Lu
  * @date: 2020/11/10 17:15
  */
 public class DelayQueue<E extends Delayed> extends AbstractQueue<E>
         implements BlockingQueue<E> {
     private static class PriorityQueue<E> extends AbstractQueue<E>
             implements java.io.Serializable {

         private static final long serialVersionUID = -7764885057309804111L;

         transient Object[] queue = new Object[10];

         private int size = 0;


         private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;


         private void grow(int minCapacity) {
             Object[] data = this.queue;
             int oldCapacity = data.length;
             int newCapacity = oldCapacity << 1;
             if (newCapacity < minCapacity) {
                 newCapacity = minCapacity;
             }
             if (newCapacity > MAX_ARRAY_SIZE) {
                 newCapacity = MAX_ARRAY_SIZE;
             }
             this.queue = Arrays.copyOf(data, newCapacity);
         }


         @Override
         public boolean offer(E e) {
             if (e == null) {
                 throw new NullPointerException();
             }
             int i = size;
             if (i >= queue.length) {
                 grow(i + 1);
             }
             size = i + 1;
             if (i == 0) {
                 queue[0] = e;
             } else {
                 siftUp(i, e);
             }
             return true;
         }

         @Override
         public E peek() {
             return (size == 0) ? null : (E) queue[0];
         }

         private int indexOf(Object o) {
             if (o != null) {
                 for (int i = 0; i < size; i++) {
                     if (o.equals(queue[i])) {
                         return i;
                     }
                 }
             }
             return -1;
         }


         @Override
         public boolean remove(Object o) {
             int i = indexOf(o);
             if (i == -1) {
                 return false;
             } else {
                 removeAt(i);
                 return true;
             }
         }


         @Override
         public boolean contains(Object o) {
             return indexOf(o) != -1;
         }


         @Override
         public Object[] toArray() {
             return Arrays.copyOf(queue, size);
         }


         @Override
         public <T> T[] toArray(T[] a) {
             final int size = this.size;
             if (a.length < size) {
                 return (T[]) Arrays.copyOf(queue, size, a.getClass());
             }
             System.arraycopy(queue, 0, a, 0, size);
             if (a.length > size) {
                 a[size] = null;
             }
             return a;
         }


         @Override
         public Iterator<E> iterator() {
             throw new UnsupportedOperationException();
         }

         @Override
         public int size() {
             return size;
         }

         @Override
         public void clear() {
             for (int i = 0; i < size; i++) {
                 queue[i] = null;
             }
             size = 0;
         }

         @Override
         public E poll() {
             if (size == 0) {
                 return null;
             }
             int s = --size;
             E result = (E) queue[0];
             E x = (E) queue[s];
             queue[s] = null;
             if (s != 0) {
                 siftDown(0, x);
             }
             return result;
         }

         private E removeAt(int i) {
             int s = --size;
             if (s == i)
             {
                 queue[i] = null;
             } else {
                 E moved = (E) queue[s];
                 queue[s] = null;
                 siftDown(i, moved);
                 if (queue[i] == moved) {
                     siftUp(i, moved);
                     if (queue[i] != moved) {
                         return moved;
                     }
                 }
             }
             return null;
         }


         private void siftUp(int k, E x) {
             Comparable<? super E> key = (Comparable<? super E>) x;
             while (k > 0) {
                 int parent = (k - 1) >>> 1;
                 Object e = queue[parent];
                 if (key.compareTo((E) e) >= 0) {
                     break;
                 }
                 queue[k] = e;
                 k = parent;
             }
             queue[k] = key;
         }


         private void siftDown(int k, E x) {
             Comparable<? super E> key = (Comparable<? super E>) x;
             int half = size >>> 1;
             while (k < half) {
                 int child = (k << 1) + 1;
                 Object c = queue[child];
                 int right = child + 1;
                 if (right < size &&
                         ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0) {
                     c = queue[child = right];
                 }
                 if (key.compareTo((E) c) <= 0) {
                     break;
                 }
                 queue[k] = c;
                 k = child;
             }
             queue[k] = key;
         }


         private void heapify() {
             for (int i = (size >>> 1) - 1; i >= 0; i--) {
                 siftDown(i, (E) queue[i]);
             }
         }


         private void writeObject(java.io.ObjectOutputStream s)
                 throws java.io.IOException {
             s.defaultWriteObject();

             s.writeInt(Math.max(2, size + 1));

             for (int i = 0; i < size; i++) {
                 s.writeObject(queue[i]);
             }
         }

         private void readObject(java.io.ObjectInputStream s)
                 throws java.io.IOException, ClassNotFoundException {
             s.defaultReadObject();

             s.readInt();

             SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, size);
             queue = new Object[size];

             for (int i = 0; i < size; i++) {
                 queue[i] = s.readObject();
             }

             heapify();
         }
     }

     private class Itr implements Iterator<E> {
         private Object[] array;
         private int index;

         Itr(Object[] array) {
             this.array = array;
         }

         @Override
         public boolean hasNext() {
             return index < array.length;
         }

         @Override
         public E next() {
             return (E) array[index++];
         }

         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }

     private final PriorityQueue<E> q = new PriorityQueue<E>();

     private Thread leader = null;

     @Override
     public Iterator<E> iterator() {
         return new Itr(q.toArray());
     }

     @Override
     public synchronized int size() {
         return q.size();
     }

     @Override
     public void put(E e) throws InterruptedException {
         offer(e);
     }

     @Override
     public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
         return offer(e);
     }

     @Override
     public synchronized E take() throws InterruptedException {
         try {
             //java中,while(true)和for(;;)编译后生成的字节码一模一样
             while (true) {
                 E first = q.peek();
                 if (first == null) {
                     this.wait();
                 } else {
                     long delay = first.getDelay(TimeUnit.MILLISECONDS);
                     if (delay <= 0) {
                         return q.poll();
                     }
                     first = null;
                     //等待这个对象到期的leader线程已存在
                     if (leader != null) {
                         this.wait();
                     } else {
                         leader = Thread.currentThread();
                         try {
                             this.wait(delay);
                         } finally {
                             //如果在等待这个元素过期期间，有新的元素加入到队列中,并且排序到队首,则leader会重新选举
                             if (leader == Thread.currentThread()) {
                                 //如果这个元素还是第一个元素,则清除leader信息
                                 leader = null;
                             }
                         }
                     }
                 }
             }
         } finally {
             //如果在等待过程中抛中断异常，那么到这时leader可能不为null
             if (leader == null && q.peek() != null) {
                 this.notify();
             }
         }
     }

     @Override
     public synchronized E poll(long timeout, TimeUnit unit) throws InterruptedException {
         long remainingTime = unit.toMillis(timeout);
         long deadline = System.currentTimeMillis() + remainingTime;
         try {
             //java中,while(true)和for(;;)编译后生成的字节码一模一样
             while (true) {
                 E first = q.peek();
                 if (first == null) {
                     if (remainingTime <= 0) {
                         return null;
                     } else {
                         this.wait(remainingTime);
                         remainingTime = deadline - System.currentTimeMillis();
                     }
                 } else {
                     long delay = first.getDelay(TimeUnit.MILLISECONDS);
                     if (delay <= 0) {
                         return q.poll();
                     }
                     if (remainingTime <= 0) {
                         return null;
                     }
                     first = null;
                     if (remainingTime < delay || leader != null) {
                         this.wait(remainingTime);
                         remainingTime = deadline - System.currentTimeMillis();
                     } else {
                         leader = Thread.currentThread();
                         try {
                             this.wait(delay);
                             remainingTime = deadline - System.currentTimeMillis();
                         } finally {
                             if (leader == Thread.currentThread()) {
                                 leader = null;
                             }
                         }
                     }
                 }
             }
         } finally {
             if (leader == null && q.peek() != null) {
                 this.notify();
             }
         }
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
     public synchronized boolean offer(E e) {
         q.offer(e);
         if (q.peek() == e) {
             /*
             如果新加的元素被排到队首,则随机唤醒一个等待中的线程，并且清除原
             有leader线程(如果已经有线程在等元素过期),把唤醒的线程设为新的
             leader(如果被唤醒的线程获取到这个元素时发现还没到期的话)
             */
             leader = null;
             this.notify();
         }
         return true;
     }

     @Override
     public synchronized E poll() {
         E first = q.peek();
         if (first == null || first.getDelay(TimeUnit.MILLISECONDS) > 0) {
             return null;
         } else {
             return q.poll();
         }
     }

     @Override
     public E peek() {
         throw new UnsupportedOperationException();
     }

     @Override
     public synchronized Object[] toArray() {
         return q.toArray();
     }

     @Override
     public synchronized void clear() {
         q.clear();
     }
 }
