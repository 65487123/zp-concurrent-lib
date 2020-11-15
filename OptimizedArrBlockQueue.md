# OptimizedArrBlockQueue
 基于数组实现的阻塞队列，和ArrayBlockingQueue继承同样的类以及实现相同的接口
 并对其进行了优化
 
### 相比ArrayBlockingQueue优点
    性能高：
    1、ArrayBlockingQueue读和写用的是同一把锁，多线程同时读写时严重影响性能
    而我这个这个队列读和写锁的是不同的对象
    2、ArrayBlockingQueue用的是JUC里的Lock锁，而我这个用的是synchronized锁，
    经过实测，synchronized(jdk1.6以后)性能比JUC里的lock锁性能高
### 性能测试结果
    
### 一个生产线程对一个消费线程
#### ArrayBlockingQueue
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arro.png)
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arro2.png)
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arro3.png)
#### 这个队列
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oao.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oao2.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oao3.png)
### 四个生产线程对一个消费线程
#### ArrayBlockingQueue
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arr.png)
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arr2.png)
![arr](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/arr3.png)
#### 这个队列
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oa.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oa2.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/oa3.png)
#### 测试总结
    关闭JIT、内存足够的情况下，一个生产者对一个消费者，这个队列性能是ArrayBlockingQueue的2倍
    左右，四个生产者对一个消费者，这个队列性能是ArrayBlockingQueue的6倍以上。
    生产者消费者线程数量越多，性能差距越大
   
