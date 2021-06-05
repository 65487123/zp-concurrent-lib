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
 * 使用方法：消费者线程必须得设置为2的整数次方
 *
 * 和 {@link DependenNoLocBlocQue}适用场景的区别：
 * 这个队列适用于生产者生产消息与消费者消费消息无依赖关系
 * 也就是说，一个生产者生产的前一个消息不管有没有被消费，都会生产下一个消息
 *
 * @author: Lu ZePing
 * @date: 2019/7/20 12:19
 */
public class NoLockBlockingQueue<E> extends BlockingQueueAdapter<E> {
    /**
     * 指针压缩后4字节
     *
     * 这里可能会出现可见性问题，但是完全不会出任何问题(实际大量测试没有出现过问题)：
     *
     * 对生产者来说，先cas拿到将要存放元素的位置,然后判断当前位置的元素是否已经被消费(是否是null),
     * 如果为null，才放入元素，如果出现可见性问题，可能会出现位置已经为null了，但是生产者看起来
     * 还不是null。这样造成的后果只是空轮循几次而已，结果并不会出错。(这其实是面向实现编程而不是面向
     * 规范编程了,不是非常严谨,不加volatile,按照java规范,其实是有可能出现永远不可见的情况的,
     * Thread.yield()以及Thread.sleep(1)并没有保证重新取得cpu资源时会刷新寄存器,而且也不是
     * 所有CPU都保证支持缓存一致性协议。。只能说根据绝大部份底层实现,不会出现一直不可见的情况)
     *
     * 同样，对消费者来说。如果出现可见性问题，最多就是当前位置已经有元素了，而消费者没看到。造成的
     * 后果也是出现空轮询几次。
     *
     *
     * 由于每部份数组块只被同一个线程操作，所以写数据的时候也不需要进行cas（不可能会出现两个写线程
     * 同时在等一个位置被释放）
     */
    private final E[][] ARRAY;
    /**
     * 4字节，加上对象头12字节，一共20字节，还差44字节
     */
    private final int M;

    private long padding1, padding2, padding3, padding4, padding5;
    private int padding6;

    private final int[] HEAD;
    private final int[] TAIL;


    public NoLockBlockingQueue(int preferCapacity, int threadSum) {
        int capacity = tableSizeFor(preferCapacity);
        int capacityPerSlot = capacity / threadSum;
        ARRAY = (E[][]) new Object[threadSum][capacityPerSlot];
        //int占4个字节
        HEAD = new int[16 * threadSum];
        TAIL = new int[16 * threadSum];
        M = capacityPerSlot - 1;
    }

    @Override
    public void put(E obj, int threadId) throws InterruptedException {
        if (obj==null){
            throw new NullPointerException();
        }
        int p = HEAD[16 * threadId]++ & M;
        while (ARRAY[threadId][p] != null) {
            //这里sleep也有解决伪共享的效果，因为会给消费者1ms的时间取元素
            Thread.sleep(1);
        }
        ARRAY[threadId][p] = obj;
    }


    @Override
    public E take() throws InterruptedException {
        E r;
        while (true) {
            for (int i = 0; i < TAIL.length; i += 16) {
                int p = TAIL[i] & this.M;
                if ((r = ARRAY[i / 16][p]) != null) {
                    ARRAY[i / 16][p] = null;
                    TAIL[i]++;
                    return r;
                }
            }
            //这里sleep也有解决伪共享的效果，因为会给生产者1ms的时间去写
            Thread.sleep(1);
        }
    }

}