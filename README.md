# zp-concurrent-lib
Some concurrency tools written by myself

#  功能介绍
    java并发库，使用方法和juc包下的类相同（和juc下的工具类实现同样的接口）

#  目前实现的类

## com.lzp.util.concurrent.blockingQueue

### nolock

#### NoLockBlockingQueue.java
    高性能阻塞队列，适用于多个生产者对一个消费者（线程),无锁设计，并且解决了伪共享问题。
    使用方法：消费者线程必须得设置为2的次方，不然性能反而比jdk自带的队列差 
##### 主要实现原理及优点
    底层是一个数组，通过cas实现无锁设计，放元素和取元素能同时进行，把不同生产线程生产的消息放到不同的块中并且读头指针和尾指针
    周边进行内存填充解决伪共享问题。由于每个数组块只被一个生产线程所占有，所以在放入元素时都无需CAS，进一步提高了性能 
    自测平均性能是ArrayBlockingQueue的2倍以上。(ArrayBlockingQueue已经是juc中最快的阻塞队列了）
##### 缺点
    比较占资源（CPU时间片、CPU缓存）。当调用take()时队列空或者调用put()时队列满，调用的线程并不会进入阻塞状态，所以当主机cpu逻辑处理器
    比较少或者对队列性能要求不是特别高的情况下，没必要用这个队列。
#### OneToOneBlockingQueue.java
    高性能阻塞队列，一个生产者对一个消费者（线程),同样无锁设计，同样解决了伪共享问题。
##### 优点
    当生产者和消费者都只有一个线程时，性能比NoLockBlockingQueue要高
##### 缺点
    比较占资源（CPU时间片、CPU缓存）

### withlock

## com.lzp.util.concurrent.threadpool

#### ThreadPoolExecutor.java
    线程池，功能和用法和java.util.concurrent.ThreadPoolExecutor完全一样
    
