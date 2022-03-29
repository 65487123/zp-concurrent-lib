# ThreadPoolExecutor
 线程池，功能和用法和java.util.concurrent.ThreadPoolExecutor完全一样

    
# 相比JUC包下的ThreadPoolExecutor的优势
## 达到最大线程数时优化了细节
    JUC自带的线程池：当核心线程数满了，队列也满了，这时候还有大量任务进来。处理这些任务时发现当前
    线程数还没达到最大线程数，这些任务会争夺创建额外线程的权利，当没有抢到创建额外线程的权利，直接执行拒绝策略。
    我的这个线程池：当核心线程数满了，队列也满了，这时候还有大量任务进来，同样这些任务会争夺创建额外线程的权利，
    但是，如果没有抢到争夺额外线程的权利，会再次把任务丢进阻塞队列一次,如果队列还是满的，才会执行拒绝策略。(虽
    然第二次能加进去的概率很小很小...)
    这样做还会带来另一个好处：
    如果业务场景需要，想把线程池的执行顺序改成：执行一个任务时，当核心线程数没满就开核心线程，把这个任务当作第一个任务，
    当核心线程数满了，但是线程数没达到最大线程数，继续开线程，把这个任务当作额外线程的第一个任务，当线程数达到最大线程数了，才把任务加入到队列中。
    实现起来就是通过重写阻塞队列的offer方法，offer()的内容是判断当前线程池里的线程是否达到最大线程，如果没达到最大线程返回false，否则执行入队操作。
    伪代码示例如下:
    threadPoolExecutor = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>() {
            @Override
            public boolean offer(Runnable runnable) {
                if (threadPoolExecutor.getActiveCount() < 3) {
                    return false;
                } else {
                    return super.offer(runnable);
                }
            }
     });
     注意：threadPoolExecutor得是成员变量
     
     这样做虽说能大致实现上面所说的执行顺序，但是如果用的是juc提供的ThreadPoolExecutor,会有问题的：
     execute()一个任务时，线程数达到核心线程数但没达到最大线程数，阻塞队列返回false，于是开始创建额外线程,但在争抢创建额外线程时失败了，
     这时直接就会执行拒绝策略了。如果是默认的拒绝策略，就抛异常了.......
     而我这个线程池就没这个问题



## 性能高（除去执行任务的时间，额外耗时少）
    简单自测了下，execute()大量小任务，性能比java.util.concurrent.ThreadPoolExecutor要高。
    由于性能比jdk自带的线程池高(没改变执行顺序的情况下,这才是主要原因)，进一步降低了执行拒绝策略的概率
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
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/1f4769fcc8ca510a98b7ad474f7e90b.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/425f3042b55814a02b208e9dcabf1cd.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/58efe65e87b35ef04bb2714b24b484f.png)
![mine](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/70e3e47134d8c5bde4d4b3e47bc3420.png)
#### juc包下的线程池
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/93e14cc0299bf84b91f291e0c37c252.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/ca68cb92aa49c5aea6d64224dbded69.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/1c74679f7c13d3309c828e69ed476f0.png)
![juc](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/24355f76a90a795dee35b85e0210fe4.png)
#### 测试总结：
    都是开了4个线程的线程池，同一台主机(8个逻辑处理器)，同样的队列，同样jvm启动参数,都没产生gc。execute()500万次同样的任务，
    都关闭了JIT。我的这个线程池耗时基本在12000ms+，JUC的线程池耗时基本都在22000ms+  而真正执行任务(run方法里的)的时间可以忽略不计
    换句话说，执行任务额外耗时，JUC线程池比我这个线程池多差不多一倍。(实际上，这样测试也不是特别严谨，因为CountDownLatch.countDown()
    方法会因为线程之间激烈竞争而导致性能下降，也就是说，execute()方法，除了执行run()以外做的事情越少,countDown()竞争越激烈，整体耗时越长。 
    最好的测试方法是先记录当前时刻,execute()空任务,比如for循环execute()500万个空任务，然后调用线程池的shutdown(),接着调用线程池的
    awaitTermination()阻塞等待线程池关闭，最后记录时间。而且开启JIT测试能更符合实际场景,有兴趣可以自己拉代码测测)
    当然，如果真正执行的任务是很耗时的，比如有io操作，那么这点差距可以忽略。
    
