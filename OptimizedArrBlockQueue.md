# OptimizedArrBlockQueue
 基于数组实现的阻塞队列，和ArrayBlockingQueue继承同样的类以及实现相同的接口
 并对其进行了优化
 
### 相比ArrayBlockingQueue优点
    性能高：
    1、ArrayBlockingQueue取元素和放元素用的是同一把锁，多线程同时读写时严重影响性能
    而我这个这个队列取元素和放元素锁的是不同的对象
    2、ArrayBlockingQueue用的是JUC里的Lock锁，而我这个用的是synchronized锁，
    经过实测，synchronized(jdk1.6以后)性能比JUC里的lock锁性能高
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
    关闭JIT、内存足够的情况下，一个生产者对一个消费者，这个队列性能是ArrayBlockingQueue的2倍
    左右，四个生产者对一个消费者，这个队列性能是ArrayBlockingQueue的7倍以上。(主机不同，结果也
    不同，实际我用另一台cpu主频比较高的主机，测试结果是将近十倍的差距）
    生产者消费者线程数量越多，性能差距越大
    注：开启JIT的情况下，在主方法写多线程放取测试代码，ArrayBlockingQueue是比这个队列要快的，
    同样的元素个数，放取线程越多，ArrayBlockingQueue耗时越少。这我估计是因为JIT把Lock锁给优化
    成无锁了(不然怎么解释竞争越大，耗时越少呢？不管几个线程，都是排队放取元素的，应该线程越多
    耗时越大)。实际有线程竞争的场景(比如涉及倒网络传输的代码，放取元素时机是完全不确定的)估计
    不会优化成这样。
   
