# OptimizedArrBlockQueue
 基于数组实现的阻塞队列，和ArrayBlockingQueue继承同样的类以及实现相同的接口
 并对其进行了优化
 
### 相比ArrayBlockingQueue优点
    性能高：
    1、ArrayBlockingQueue取元素和放元素用的是同一把锁，多线程同时读写时严重影响性能
    而我这个这个队列取元素和放元素锁的是不同的对象
    2、ArrayBlockingQueue用的是JUC里的Lock锁，而我这个用的是synchronized锁
    synchronized相比于Lock锁的优势
    (1)内存占用少：synchronized锁标记是在对象头里,所以任何对象都能当成锁,不需要额外new锁对象。
    (2)性能不比Lock锁差：经过实测,不管线程竞争是否激烈,关闭JIT的情况下，synchronized性能远远
    高于Lock锁,开启JIT的情况下,synchronized性能相比Lock也差不了多少。(JUC里的Lock锁由于是
    java代码实现的,所以能更大地享受到JIT的收益)
### 性能测试结果
    
### 一个生产线程对一个消费线程
#### ArrayBlockingQueue
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arro.png)
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arro1.png)
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arro2.png)
#### 这个队列
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oao.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oao1.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oao2.png)
### 四个生产线程对一个消费线程
#### ArrayBlockingQueue
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arr2.png)
#### 这个队列
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oa2.png)
#### 测试总结
    关闭JIT、内存足够的情况下，一个生产者对一个消费者，这个队列性能是ArrayBlockingQueue的2倍左右，四个生产者对一个消费者，这个队列性能是
    ArrayBlockingQueue的7倍以上。(物理主机不同，结果也不同，实际我用另一台cpu主频比较高的主机，测试结果是将近十倍的差距）生产者消费者线程
    数量越多，性能差距越大
    注：
    1、这种测试方法测试的结果是有一点点误差的，因为CountDownLatch.countDown()会因为多线程之间的竞争而性能下降，线程竞争越激烈CountDownLatch
    .countDown()耗时越长。所以把CountDownLatch.countDown()放到循环外面更合适，countDown的次数设为线程数量，或者用其他更准的方法测，比如包装
    成任务丢进线程池，然后关闭线程池，测算任务执行时间。由于线程竞争越激烈CountDownLatch.countDown()耗时越长，所以这个结果也能说明问题了(整体
    耗时少说明线程竞争更激烈，countDown耗时更多，队列存取耗时更少)，所以就不再用按上述说的测试然后上传截图了。
    2、开启JIT的情况下，在主方法写多线程放取测试代码，ArrayBlockingQueue是比这个队列要快的，同样的元素个数，放取线程越多，ArrayBlockingQueue
    耗时越少。这我估计是因为JIT把Lock锁给优化成无锁了(不然怎么解释竞争越大，耗时越少呢？不管几个线程，都是排队放取元素的，应该线程越多耗时越大)。
    实际有线程竞争的场景(比如涉及倒网络传输的代码，放取元素时机是完全不确定的)估计不会优化成这样。
    
   
