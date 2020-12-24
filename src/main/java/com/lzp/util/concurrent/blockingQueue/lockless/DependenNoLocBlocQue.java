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


/**
 * Description:高性能阻塞队列，适用于多个生产者对一个消费者（线程),无锁设计，并且解决了伪共享问题。
 * 使用方法：消费者线程必须得设置为2的次方
 *
 * 和 {@link NoLockBlockingQueue}适用场景的区别：
 * 这个队列适用于生产者生产下一个消息依赖与上一个消息被消费
 * 也就是说，一个生产者生产的前一个消息必须得被消费了，才会生产下一个消息
 *
 * @author: Lu ZePing
 * @date: 2019/7/20 12:19
 */
public class DependenNoLocBlocQue<E> extends BlockingQueueAdapter<E> {
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
    private E[][] array;
    /**
     * 4字节，加上对象头12字节，一共20字节，还差44字节
     */
    private final int m;

    private long padding1, padding2, padding3, padding4, padding5;
    private int padding6;

    private int[] head;
    private int[] tail;


    public DependenNoLocBlocQue(int preferCapacity, int threadSum) {
        int capacity = tableSizeFor(preferCapacity);
        int capacityPerSlot = capacity / threadSum;
        array = (E[][]) new Object[threadSum][capacityPerSlot];
        //int占4个字节
        head = new int[16 * threadSum];
        tail = new int[16 * threadSum];
        m = capacityPerSlot - 1;
    }

    @Override
    public void put(E obj, int threadId) throws InterruptedException {
        if (obj == null){
            throw new NullPointerException();
        }
        int p = head[16 * threadId]++ & m;
        while (array[threadId][p] != null) {
            //消费速度远跟不上生产速度。由于这队列用作生产者消费者有依赖关系的，所以基本不会出现这种情况
            Thread.yield();
        }
        array[threadId][p] = obj;
    }


    @Override
    public E take() throws InterruptedException {
        E r;
        //java中,while(true)和for(;;)编译后生成的字节码一模一样
        while (true) {
            for (int i = 0; i < tail.length; i += 16) {
                int p = tail[i] & this.m;
                if ((r = array[i / 16][p]) != null) {
                    array[i / 16][p] = null;
                    tail[i]++;
                    return r;
                }
            }
            Thread.yield();
        }
    }

}