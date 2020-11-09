package com.lzp.util.concurrent.blockingQueue.nolock;


import java.util.concurrent.TimeUnit;


/**
 * Description:高性能阻塞队列，适用于一个生产者对一个消费者（线程),无锁设计，并且解决了伪共享问题。
 *
 * @author: Lu ZePing
 * @date: 2019/7/20 12:19
 */
public class OneToOneBlockingQueue<E> extends BlockingQueueAdapter<E> {

    /**
     * 指针压缩后4字节
     *
     * 这里可能会出现可见性问题，但是完全不会出任何问题：
     *
     * 对生产者来说，先cas拿到将要存放元素的位置,然后判断当前位置的元素是否已经被消费(是否是null),
     * 如果为null，才放入元素，如果出现可见性问题，可能会出现位置已经为null了，但是生产者看起来
     * 还是null。这样造成的后果只是空轮循几次而已，结果并不会出错。
     *
     * 同样，对消费者来说。如果出现可见性问题，最多就是当前位置已经有元素了，而消费者没看到。造成的
     * 后果也是出现空轮询几次。
     *
     *
     * 由于每部份数组块只被同一个线程操作，所以写数据的时候也不需要进行cas（不可能会出现两个写线程
     * 同时在等一个位置被释放）
     */
    private E[] array;
    /**
     * 4字节，加上对象头12字节，一共20字节，还差44字节
     */
    private final int m;

    private int[] head = new int[27];

    private int[] tail = new int[16];


    public OneToOneBlockingQueue(int preferCapacity) {
        int capacity = tableSizeFor(preferCapacity);
        array = (E[]) new Object[capacity];
        m = capacity - 1;
    }

    @Override
    public void put(E obj) throws InterruptedException {

        int p = head[11]++ & m;
        while (array[p] != null) {
            Thread.yield();
        }
        array[p] = obj;
    }


    @Override
    public E take() throws InterruptedException {
        E e;
        int p = tail[0]++ & m;
        while ((e = array[p]) == null) {
            Thread.yield();
        }
        array[p] = null;
        return  e;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long now = 0;
        long time = unit.toMillis(timeout);
        E e;
        int p = tail[0]++ & m;
        while ((e = array[p]) == null) {
            if (now == 0) {
                now = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - now > time) {
                tail[0]--;
                throw new InterruptedException();
            } else {
                Thread.yield();
            }
        }
        array[p] = null;
        return e;
    }
}