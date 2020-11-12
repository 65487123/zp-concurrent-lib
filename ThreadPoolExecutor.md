# ThreadPoolExecutor
 线程池，功能和用法和java.util.concurrent.ThreadPoolExecutor完全一样

# 特点：
    1、简洁，线程池核心功能都实现了，但是实现过程没有复杂的骚操作。
    2、对JDK的线程池做了些优化
    
# 相比JUC包下的ThreadPoolExecutor的优势
## 达到最大线程数时优化了细节
    JUC自带的线程池：当核心线程数满了，队列也满了，这时候还有大量任务进来。处理这些任务时发现当前
    线程数还没达到最大线程数，这些任务会争夺创建额外线程的权利，当没有抢到创建额外线程的权利，直接执行拒绝策略。
    我的这个线程池：当核心线程数满了，队列也满了，这时候还有大量任务进来，同样这些任务会争夺创建额外线程的权利，
    但是，如果没有抢到争夺额外线程的权利，会再次把任务丢进阻塞队列一次,如果队列还是满的，才会执行拒绝策略。(虽
    然第二次能加进去的概率很小很小...)

## 性能高（除去执行任务的时间，额外耗时少）
    简单自测了下，execute()大量小任务，性能比java.util.concurrent.ThreadPoolExecutor要高。
    由于性能比jdk自带的线程池高，进一步降低了执行拒绝策略的概率(这才是主要原因)
    经过实际测试，使用长度500万的有界队列，使用默认的拒绝策略(抛异常),核心线程数为4，最大线程数为8.执行1亿个小任务
    JDK自带的线程池，几乎百分百抛出拒绝策略的异常，而我这个线程池几乎百分百完成了所有任务而没有执行拒绝策略。(测试
    结果和机器有关，总的来说，当队列容量远小于总任务数量，核心线程数量又小于最大线程数时，执行拒绝策略的概率比JDK
    自带的线程池小很多)

## 重写了submit(),返回的Future可以增加异步回调方法
    JDK自带的线程池，执行submit()返回的是FutureTask对象，这个对象获取结果需要阻塞等待，而我这个返回的Future能添加
    异步回调方法，当任务执行结束，会执行回调方法。
    
# 和JUC包下的线程池性能测试对比：
### execute()500万次小任务
#### 我的这个线程池
![mine](https://github.com/65487123/zp-concurrent-lib/blob/master/picture/1c74679f7c13d3309c828e69ed476f0.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/1f4769fcc8ca510a98b7ad474f7e90b.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/24355f76a90a795dee35b85e0210fe4.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/425f3042b55814a02b208e9dcabf1cd.png)
#### juc包下的线程池
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/58efe65e87b35ef04bb2714b24b484f.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/70e3e47134d8c5bde4d4b3e47bc3420.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/93e14cc0299bf84b91f291e0c37c252.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/ca68cb92aa49c5aea6d64224dbded69.png)
### 500万次小任务
#### 测试总结：
    都是开了4个线程的线程池，同一台主机(8个逻辑处理器)，同样的队列，都没产生gc。execute()500万次同样的任务，都关闭了JIT
    我的这个线程池耗时基本在12000ms+，JUC的线程池耗时基本都在22000ms+  而真正执行任务(run方法里的)的时间可以忽略不计
    换句话说，执行任务额外耗时，JUC线程池比我这个线程池多差不多一倍
    
