# zp-concurrent-lib
 自己写的一些java并发工具类

#  功能介绍
    java并发库，使用方法和juc包下的类相同（和juc下的工具类实现同样的接口）
#  目前实现的类
## com.lzp.util.concurrent.threadpool

#### ThreadPoolExecutor.java
    线程池，功能和用法和java.util.concurrent.ThreadPoolExecutor完全一样
## com.lzp.util.concurrent.blockingQueue.nolock
#### NoLockBlockingQueue.java
    高性能阻塞队列，适用于多个生产者对一个消费者（线程),无锁设计，并且解决了伪共享问题。
    使用方法：消费者线程必须得设置为2的次方，不然性能反而比jdk自带的队列差 
##### 主要实现原理及优点
    底层是一个数组，通过cas实现无锁设计，放元素和取元素能同时进行，把不同生产线程生产的消息放到不同的块
    中并且读头指针和尾指针周边进行内存填充解决伪共享问题。由于每个数组块只被一个生产线程所占有，所以在放
    入元素时都无需CAS，进一步提高了性能 
    自测性能是ArrayBlockingQueue的4倍以上（内存充足时，ArrayBlockingQueue是jdk自带的阻塞队列中最快的)
##### 缺点
    比较占资源（CPU时间片、CPU缓存）。所以并不适合所有场景。
    比如下面这些场景:
    1、主机cpu逻辑处理器比较少：当队列满或者空时可能会空轮询，所以一个逻辑cpu的部份资源可能会被浪费。
    2、对性能要求不是特别高：大部分场景，jdk自带的队列就够了
    3、性能瓶颈不在进出队列上(cpu、内存操作),而在IO上。比如10万个元素从网络传输到本机，然后经过cpu计算
    处理后把结果返回给远端，这整个过程需要耗时一秒钟（不算进出队列的时间）。这个队列每秒钟能进出5000万个
    元素，100000个元素进出这个队列只需要花0.002秒，而用jdk自带的队列，算他5分之一的性能，进出队列也才需
    要0.01秒。这整体的tps也才相差790而已（99800.399-99009.9)，用这个队列只能提高百分之一左右的tps。
#### OneToOneBlockingQueue.java
    高性能阻塞队列，一个生产者对一个消费者（线程),同样无锁设计，同样解决了伪共享问题。
##### 优点
    当生产者和消费者都只有一个线程时，性能比NoLockBlockingQueue要高。
##### 缺点
    和NoLockBlockingQueue类似
#### DependenNoLocBlocQue.java
    和 NoLockBlockingQueue 适用场景不同，这个队列适用于一个生产者生产下一个消息依赖于这个生产者生产的上一个消息被消费
    而 NoLockBlockingQueue 适用于一个生产者生产下一个消息不依赖于这个生产者生产的上一个消息被消费
##### 实现上和NoLockBlockingQueue的区别
    和NoLockBlockingQueue一样，底层都是一个数组。区别是，NoLockBlockingQueue进行put的时候发现数组当前索引位置的元素
    没有被消费会sleep一毫秒，进行take的时候发现数组当前索引位置没有元素同样会sleep一毫秒，这样做的好处是1、进一步消除
    伪共享:当头指针倒追尾指针时，头指针会等尾指针跑一毫秒，当尾指针追上头指针时，尾指针会等头指针跑一毫秒。2、在队列已
    满或者队列空的时候，让出了大量cpu使用权，减少大量空轮询次数。而这个队列不会调用sleep(),而是调用Thread.yield()
##### 相比NoLockBlockingQueue，缺点：
    在队列空的时候，调用take()会占用一个cpu的大量使用权。(在cpu比较空闲时，基本会跑满一个逻辑cpu)
#### [DependenOneTOneBlocQue.java] (https://github.com/65487123/zp-concurrent-lib/blob/master/DepenOneTOneBlocQue.md)
    和 OneToOneBlockingQueue 适用场景不同，这个队列适用于生产者生产下一个消息依赖于生产者生产的上一个消息被消费
    而 OneToOneBlockingQueue 适用于生产者生产下一个消息不依赖于生产者生产的上一个消息被消费    
##### 缺点
    和DependenNoLocBlocQue类似。
## com.lzp.util.concurrent.blockingQueue.withlock

    
