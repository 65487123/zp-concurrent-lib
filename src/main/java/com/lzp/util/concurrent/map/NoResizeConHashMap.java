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
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.sun.xml.internal.fastinfoset.util.ValueArray.MAXIMUM_CAPACITY;

/**
 * Description:线程安全的Map
 * 比 {@link java.util.concurrent.ConcurrentHashMap} 性能高
 * <p>
 * 主要区别:
 * 1、消除了树化以及树退化等操作:因为达到树化的概率很低，所以没必要为了这么低的概率去优化(增加红黑树结构，需要增加类型判断）。
 * 并且树化只是增加了查性能，写性能会降低
 * 2、没有扩容机制,所以要求使用者预估最大存储键值对数量(ConcurrentHashMap使用时也建议带参数初始化避免扩容,但是就算不去
 * 扩容，put过程避免不了检查是否在扩容的操作）
 * 4、优化了很多细节，减少不必要的操作
 *
 * @author: Zeping Lu
 * @date: 2020/11/18 15:54
 */
public class NoResizeConHashMap<K, V> extends AbstractMap<K, V> implements Serializable {
    private Unsafe u;
    private final long BASE;
    private final long SCALE;
    private Node<K, V>[] table;
    private final int M;

    static class Node<K, V> implements Map.Entry<K, V> {
        volatile K key;
        volatile V val;
        volatile int h;
        volatile Node<K, V> next;

        Node(K key, V val, Node<K, V> next,int h) {
            this.key = key;
            this.val = val;
            this.next = next;
            this.h = h;
        }

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public V getValue() {
            return null;
        }

        @Override
        public V setValue(V value) {
            return null;
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
        try {
            Constructor<Unsafe> constructor = Unsafe.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            u = constructor.newInstance();
        } catch (Exception ignored) {
        }
        assert u != null;
        BASE = u.arrayBaseOffset(Object[].class);
        SCALE = u.arrayIndexScale(Object[].class);
        int cap = ((capacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                MAXIMUM_CAPACITY :
                tableSizeFor(capacity));
        table = new Node[cap];
        M = cap - 1;
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
        if (table[i = ((h = (hash(key.hashCode()))) & M)] == null) {
            Node newNode = new Node(key, value, null, h);
            if (!u.compareAndSwapObject(table, BASE + i * SCALE, null, newNode)) {
                Node<K, V> node;
                synchronized (node = table[i]) {
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
        } else {
            Node<K, V> node;
            synchronized (node = table[i]) {
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
                node.next = new Node<>(key, value, null, h);
                return null;
            }
        }
        return null;
    }

    @Override
    public V get(Object key) {
        Node<K, V> node;
        int h;
        if ((node = table[(h = hash(key.hashCode())) & M]) != null) {
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
        if (table[(i = (h = hash(key.hashCode())) & M)] != null) {
            Node<K, V> node, preNode;
            synchronized (node = table[i]) {
                if (key.equals(node.key)) {
                    V preV = node.val;
                    if (node.next == null) {
                        node.key = null;
                        node.val = null;
                        node.h = h;
                    } else {
                        table[i] = node.next;
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
            }
        }
        return null;
    }

    @Override
    public int size() {
        int sum = 0;
        Node node;
        for (Node<K, V> kvNode : table) {
            if ((node = kvNode) != null) {
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
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {

    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {

    }

    @Override
    public V putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public V replace(K key, V value) {
        return null;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return null;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return null;
    }

}
