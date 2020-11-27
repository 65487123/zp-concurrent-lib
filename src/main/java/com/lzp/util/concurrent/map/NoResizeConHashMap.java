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

package com.lzp.util.concurrent.map;

import sun.misc.Unsafe;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.*;



/**
 * Description:线程安全的Map
 * 比 {@link java.util.concurrent.ConcurrentHashMap}性能高(写元素、读元素、删元素、遍历元素,总内存占用等)
 * <p>
 * 主要区别:
 * 1、消除了树化以及树退化等操作:因为达到树化的概率很低，所以没必要为了这么低的概率去优化(增加红黑树结构，需要增加类型判断）。
 * 并且树化只是增加了查性能，写性能会降低
 * 2、没有扩容机制,所以要求使用者预估最大存储键值对数量(ConcurrentHashMap使用时也建议带参数初始化避免扩容,但是就算不去
 * 扩容，put和remove过程避免不了检查是否在扩容的操作）
 * 3、优化了很多细节，减少不必要的操作
 *
 * @author: Zeping Lu
 * @date: 2020/11/18 15:54
 */
public class NoResizeConHashMap<K, V> implements Map<K, V>, Serializable {
    public static final int MAXIMUM_CAPACITY = 2147483647;

    private static final Unsafe U;

    private final long BASE;

    private final int ASHIFT;

    private final transient Node<K, V>[] TABLE;

    private final int M;

    static class Node<K, V> implements Map.Entry<K, V> {
        volatile K key;
        volatile V val;
        volatile int h;
        volatile Node<K, V> next;

        Node(K key, V val, Node<K, V> next, int h) {
            this.key = key;
            this.val = val;
            this.next = next;
            this.h = h;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.val;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final String toString() {
            return key + "=" + val;
        }

    }

    private class EnteySet implements Set<Map.Entry<K, V>> {


        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EnteyIterator();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(Entry<K, V> kvEntry) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    private class KeySet implements Set<K> {


        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(K k) {
            throw new UnsupportedOperationException();
        }


        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException();
        }


        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    private class EnteyIterator implements Iterator<Entry<K, V>> {
        private Node<K, V> thisNode = new Node<>(null, null, null, 0);
        private int index = 0;

        @Override
        public boolean hasNext() {
            if (thisNode.next != null) {
                return true;
            } else {
                Node<K, V> node;
                Node[] tab = TABLE;
                int index = this.index;
                while (index < tab.length) {
                    if ((node = tabAt(tab, index++)) != null) {
                        if (node.key != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public Entry<K, V> next() {
            if (thisNode.next != null) {
                return (thisNode = thisNode.next);
            } else {
                Node[] tab = TABLE;
                while (index < tab.length) {
                    if ((thisNode = tabAt(tab,index++)) != null) {
                        if (thisNode.key != null) {
                            return thisNode;
                        }
                    }
                }
            }
            throw new NoSuchElementException();
        }
    }

    private class KeyIterator implements Iterator<K> {
        private Node<K, V> thisNode = new Node<>(null, null, null, 0);
        private int index = 0;

        @Override
        public boolean hasNext() {
            if (thisNode.next != null) {
                return true;
            } else {
                Node<K, V> node;
                Node[] tab = TABLE;
                int index = this.index;
                while (index < tab.length) {
                    if ((node = tabAt(tab,index++)) != null) {
                        if (node.key != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public K next() {
            if (thisNode.next != null) {
                return (thisNode = thisNode.next).key;
            } else {
                Node[] tab = TABLE;
                while (index < tab.length) {
                    if ((thisNode = tabAt(tab,index++)) != null) {
                        if (thisNode.key != null) {
                            return thisNode.key;
                        }
                    }
                }
            }
            throw new NoSuchElementException();
        }
    }

    static {
        try {
            Constructor<Unsafe> constructor = Unsafe.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            U = constructor.newInstance();
        } catch (Exception ignored) {
            throw new RuntimeException("Class initialization failed: Unsafe initialization failed");
        }
    }


    /**
     * 构造方法，必须指定容量。需要使用者根据业务场景预估最大键值对数量,设置合理参数以达到最好性能
     *
     * @param capacity 数组容量
     */
    public NoResizeConHashMap(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        BASE = U.arrayBaseOffset(Object[].class);
        /*通过首元素地址加上索引乘以scale(每个元素引用占位)可以得到元素位置，由于每个元素
        引用大小肯定是2的次方(4或8)，所以乘操作可以用位移操作来代替，aShift就是要左移的位数*/
        ASHIFT = 31 - Integer.numberOfLeadingZeros(U.arrayIndexScale(Object[].class));
        TABLE = new Node[(M = (capacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                MAXIMUM_CAPACITY :
                tableSizeFor(capacity) - 1) + 1];
    }

    /**
     * Returns a power of two table size for the given desired capacity.
     * See Hackers Delight, sec 3.2
     */
    private int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    private int hash(int h) {
        return h ^ (h >>> 16);
    }

    @Override
    public V put(K key, V value) {
        int i, h;
        Node[] tab = this.TABLE;
        if (tabAt(tab, i = (h = (hash(key.hashCode()))) & M) == null) {
            Node<K, V> newNode = new Node<>(key, value, null, h);
            if (!U.compareAndSwapObject(TABLE, BASE + ((long) i << ASHIFT), null, newNode)) {
                Node<K, V> node;
                while (true) {
                    synchronized (node = tabAt(tab, i)) {
                        if (node == (node = tabAt(tab, i))) {
                            if (node.key == null) {
                                node.key = key;
                                node.val = value;
                                return null;
                            } else if (node.key.equals(key)) {
                                V old = node.val;
                                node.val = value;
                                return old;
                            }
                            while (node.next != null) {
                                if ((node = node.next).key.equals(key)) {
                                    V old = node.val;
                                    node.val = value;
                                    return old;
                                }
                            }
                            node.next = newNode;
                        }
                    }
                }
            }
        } else {
            Node<K, V> node;
            while (true) {
                synchronized (node = tabAt(tab, i)) {
                    if (node == (node = tabAt(tab, i))) {
                        if (node.key == null) {
                            node.key = key;
                            node.val = value;
                            node.h = h;
                            return null;
                        } else if (node.key.equals(key)) {
                            V old = node.val;
                            node.val = value;
                            return old;
                        }
                        while (node.next != null) {
                            if ((node = node.next).key.equals(key)) {
                                V old = node.val;
                                node.val = value;
                                return old;
                            }
                        }
                        node.next = new Node<>(key, value, null, h);
                        return null;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public V get(Object key) {
        Node<K, V> node;
        int h;
        if ((node = tabAt(TABLE,(h = hash(key.hashCode())) & M)) != null) {
            do {
                if (node.h == h && (key.equals(node.key))) {
                    return node.val;
                }
            } while ((node = node.next) != null);
        }
        return null;
    }


    @Override
    public V remove(Object key) {
        int i, h;
        Node<K, V> node;
        Node[] tab = TABLE;
        if ((node = tabAt(tab,(i = (h = hash(key.hashCode())) & M))) != null) {
            Node<K, V> preNode;
            while (true) {
                synchronized (node) {
                    if (node == (node = tabAt(tab,i))) {
                        if (node.h == h && key.equals(node.key)) {
                            V preV = node.val;
                            if (node.next == null) {
                                node.key = null;
                                node.val = null;
                            } else {
                                TABLE[i] = node.next;
                            }
                            return preV;
                        }
                        while (node.next != null) {
                            preNode = node;
                            if ((node = node.next).h == h && node.key.equals(key)) {
                                preNode.next = node.next;
                                return node.val;
                            }
                        }
                        return null;
                    }
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        Arrays.fill(TABLE, null);
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    /**
     * 感觉这个方法实际场景中用得不多，所以为了不影响读写元素性能，牺牲了这个方法的性能
     */
    @Override
    public int size() {
        int sum = 0;
        Node<K, V> node;
        Node[] tab = TABLE;
        for (int i = 0; i < TABLE.length; i++) {
            if ((node = tabAt(tab,i)) != null) {
                if (node.key != null) {
                    sum++;
                }
                while ((node = node.next) != null) {
                    sum++;
                }
            }
        }
        return sum;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Node<K, V> node;
        for (Node<K, V> kvNode : TABLE) {
            if ((node = kvNode) != null) {
                do {
                    if (value.equals(node.val)) {
                        return true;
                    }
                }
                while ((node = node.next) != null);
            }
        }
        return false;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EnteySet();
    }

    private Node<K, V> tabAt(Node[] tab,int i) {
        return (Node<K, V>) U.getObjectVolatile(tab, ((long) i << ASHIFT) + BASE);
    }

    /**
     * 不建议使用java原生序列化，性能不行
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        for (Entry<K, V> node : this.entrySet()) {
            s.writeObject(node.getKey());
            s.writeObject(node.getValue());
        }
    }

    /**
     * 不建议使用java原生序列化，性能不行
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        Map<K, V> map = new HashMap<>();
        for (; ; ) {
            try {
                K k = (K) s.readObject();
                V v = (V) s.readObject();
                map.put(k, v);
            } catch (Exception e) {
                break;
            }
        }
        if (map.size() != 0) {
            Class cls = this.getClass();
            try {
                Field base = cls.getDeclaredField("BASE");
                base.setAccessible(true);
                base.set(this, U.arrayBaseOffset(Object[].class));
                Field aSft= cls.getDeclaredField("ASHIFT");
                aSft.setAccessible(true);
                aSft.set(this, 31 - Integer.numberOfLeadingZeros(U.arrayIndexScale(Object[].class)));
                Field m = cls.getDeclaredField("M");
                m.setAccessible(true);
                m.set(this, tableSizeFor(map.size()) - 1);
                Field table = cls.getDeclaredField("TABLE");
                table.setAccessible(true);
                table.set(this, new Node[this.M + 1]);
            } catch (Exception e) {
                throw new RemoteException("Deserialization failed");
            }
            for (Map.Entry<K, V> entry : map.entrySet()) {
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(TABLE.length << 2).append("{");
        Node<K, V> node;
        Node[] tab = this.TABLE;
        for (int i = 0; i < tab.length; i++) {
            if ((node = tabAt(tab, i)) != null) {
                /*if (node.key != null) {
                    do {
                        stringBuilder.append(node.key + "=" + node.val + ",");
                    } while ((node = node.next) != null);
                }*/
                if (node.key !=null){
                    stringBuilder.append(node.key + "=" + node.val + ",");
                }else {
                    continue;
                }
                while (node.next!=null){
                    node = node.next;
                    stringBuilder.append(node.key + "=" + node.val + ",");
                }
            }
        }
        stringBuilder.setCharAt(stringBuilder.length() - 1, '}');
        return stringBuilder.toString();
    }

    /**
     * 返回最大链表长度，用来判断hash碰撞程度
     */
    public int maxLenth() {
        int maxLength = 1;
        Node node;
        Node[] table = TABLE;
        for (int i = 0; i < TABLE.length; i++) {
            node = tabAt(table, i);
            if (node == null) {
                continue;
            }
            int length = 1;
            while ((node = node.next) != null) {
                length++;
            }
            if (length > maxLength) {
                maxLength = length;
            }
        }
        return maxLength;
    }

    /**
     * 返回空桶个数，用来判断hash碰撞程度
     */
    public int emptyBucketSum() {
        int emptyBucketSum = 0;
        Node node;
        Node[] table = TABLE;
        for (int i = 0; i < TABLE.length; i++) {
            node = tabAt(table, i);
            if (node == null) {
                emptyBucketSum++;
            }
        }
        return emptyBucketSum;
    }
}
