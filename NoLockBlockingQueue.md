# NoLockBlockingQueue
  高性能阻塞队列，适用于多个生产者对一个消费者（线程),无锁设计，并且解决了伪共享问题。
  使用方法：消费者线程必须得设置为2的次方
##### 主要实现原理及优点
    底层是一个数组，通过cas实现无锁设计，放元素和取元素能同时进行，把不同生产线程生产的消息放到不同的块
    中并且读头指针和尾指针周边进行内存填充解决伪共享问题。由于每个数组块只被一个生产线程所占有，所以在放
    入元素时都无需CAS，进一步提高了性能 
    自测性能是ArrayBlockingQueue的七八倍以上(四生产线程，1消费线程，关闭JIT）
    
##### 自测截图
##### 启动参数及测试方法调用的方法
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/param.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/method.png)
##### 这个队列
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/mine1.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/mine2.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/mine3.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/mine4.png)
##### ArrayBlockingQueue
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/jdk1.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/jdk2.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/jdk3.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/jdk4.png)


##### 缺点
    比较占资源（CPU时间片、CPU缓存）。所以并不适合所有场景。
    比如下面这些场景:
    1、主机cpu逻辑处理器比较少：当队列满或者空时可能会空轮询，所以一个逻辑cpu的部份资源可能会被浪费。
    2、对性能要求不是特别高：大部分场景，jdk自带的队列就够了
    3、性能瓶颈不在进出队列上(cpu、内存操作),而在IO上。比如10万个元素从网络传输到本机，然后经过cpu计算
    处理后把结果返回给远端，这整个过程需要耗时一秒钟（不算进出队列的时间）。这个队列每秒钟能进出5000万个
    元素，100000个元素进出这个队列只需要花0.002秒，而用jdk自带的队列，算他5分之一的性能，进出队列也才需
    要0.01秒。这整体的tps也才相差790而已（99800.399-99009.9)，用这个队列只能提高百分之一左右的tps。

    
