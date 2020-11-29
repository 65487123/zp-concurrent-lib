
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


package com.lzp.util.concurrent.list;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Description:线程安全的ArrayList
 * jdk提供的线程安全List：
 * 1、{@link java.util.Vector}:所有操作全加锁，读和写不能同时进行
 * 2、Collections.synchronizedList 和Vector一样，所有操作加锁，大量线程时性能很低
 * 3、{@link java.util.concurrent.CopyOnWriteArrayList}：
 * 写时复制，大量写操作时，频繁new数组并复制，严重影响性能，数组元素多时很容易造成OOM
 * 适合读多写少(最好是几乎不用写,全都是读操作)
 *
 * 这个list的特点：
 * 写时加锁，读时无锁(通过Unsafe直接读内存值),适合写多读少或者读写频率差不多
 *
 * @author: Zeping Lu
 * @date: 2020/11/25 14:45
 */

public class ConcurrentArrayList<E> implements List<E> {
    private final long BASE;

    private final int ASHIFT;

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private static final Unsafe U;

    private Object[] elementData;

    private int size;

    static {
        try {
            Constructor<Unsafe> constructor = Unsafe.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            U = constructor.newInstance();
        } catch (Exception ignored) {
            throw new RuntimeException("Class initialization failed: Unsafe initialization failed");
        }
    }
    public ConcurrentArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " +
                    initialCapacity);
        }
        BASE = U.arrayBaseOffset(Object[].class);
        /*通过首元素地址加上索引乘以scale(每个元素引用占位)可以得到元素位置，由于每个元素
        引用大小肯定是2的次方(4或8)，所以乘操作可以用位移操作来代替，aShift就是要左移的位数*/
        ASHIFT = 31 - Integer.numberOfLeadingZeros(U.arrayIndexScale(Object[].class));
        this.elementData = new Object[initialCapacity];
    }

    public ConcurrentArrayList() {
        BASE = U.arrayBaseOffset(Object[].class);
        /*通过首元素地址加上索引乘以scale(每个元素引用占位)可以得到元素位置，由于每个元素
        引用大小肯定是2的次方(4或8)，所以乘操作可以用位移操作来代替，aShift就是要左移的位数*/
        ASHIFT = 31 - Integer.numberOfLeadingZeros(U.arrayIndexScale(Object[].class));
        this.elementData = new Object[10];
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return null;
    }

    @Override
    public synchronized boolean add(E e) {
        ensureCapacityInternal(size + 1);
        elementData[size++] = e;
        return true;
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(minCapacity);
    }


    private void ensureExplicitCapacity(int minCapacity) {
        if (minCapacity - elementData.length > 0) {
            grow(minCapacity);
        }
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    private void grow(int minCapacity) {
        Object[] data = this.elementData;
        int oldCapacity = data.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        if (newCapacity > MAX_ARRAY_SIZE) {
            newCapacity = MAX_ARRAY_SIZE;
        }
        this.elementData = Arrays.copyOf(data, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
        {
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(Collection c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public E get(int index) {

        return tabAt(index);
    }


    private E tabAt(int i) {
        return (E) U.getObjectVolatile(elementData, ((long) i << ASHIFT) + BASE);
    }

    @Override
    public synchronized E remove(int index) {
        rangeCheck(index);

        E oldValue = (E) elementData[index];

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index,
                    numMoved);
        }
        elementData[--size] = null;
        return oldValue;
    }

    private void rangeCheck(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<E> listIterator() {
        return null;
    }

    @Override
    public ListIterator<E> listIterator(int i) {
        return null;
    }

    @Override
    public List<E> subList(int i, int i1) {
        return null;
    }

    @Override
    public E set(int index, Object element) {
        return null;
    }

    @Override
    public void add(int index, Object element) {

    }


}

