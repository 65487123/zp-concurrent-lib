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
#### 我的这个线程池
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/17e46afea8b693c21f31c3bed30cb23.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/335ee85b1de0caa4b6e7ecb158fcba4.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/41d725ebbebf2da2b3a93898d2cd7c7.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/6c1c76b7fe5cc93106cff16071b950c.png)
#### juc包下的线程池
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/33d70612020ee6c861647eaad84f193.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/36d77a43dba6a0fc32022881447b557.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/bc457ce89625f89c3907c2df16d3867.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/f5fac0f42dec89700b7726eca828128.png)

#### 测试总结：
    都是开了8个线程的线程池，同一台主机(8个逻辑处理器)，都没产生gc。execute()一亿次同样的任务
    我的这个线程池耗时平均在9500ms左右，JUC的线程池耗时基本都在11500ms以上
    这个测试阻塞队列是LinkedBlockingQueue，实际上，换成ArrayBlockingQueue，核心线程数设小
    点(ArrayBlockingQueue的读和写用的是一把锁，所以消费者太多性能反而下降，变成性能瓶颈)，
    execute()大量小任务时，我的这个队列性能是JDK自带队列的两倍左右(耗时为二分之一)
    
