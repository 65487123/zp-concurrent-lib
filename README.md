# zp-concurrent-lib
 自己写的一些java高性能并发类库
#  功能介绍
    java并发库，使用方法和juc包下的类相同（和juc下的工具类实现同样的接口）,但是性能比其要高
#  使用方法
    1、拉取代码到本地
    2、mvn install代码
    3、项目中加入依赖
    <groupId>com.lzp</groupId>
    <artifactId>zp-concurrent-lib</artifactId>
    <version>1.0</version>
#  目前实现的类
## com.lzp.util.concurrent.threadpool
[ThreadPoolExecutor.java](https://github.com/65487123/zp-concurrent-lib/blob/master/ThreadPoolExecutor.md)

[ListenableFuture.java](https://github.com/65487123/zp-concurrent-lib/blob/master/ListenableFuture.md)
## com.lzp.util.concurrent.blockingQueue.withlock
[OptimizedArrBlockQueue.java](https://github.com/65487123/zp-concurrent-lib/blob/master/OptimizedArrBlockQueue.md)

## com.lzp.util.concurrent.blockingQueue.nolock
[NoLockBlockingQueue.java](https://github.com/65487123/zp-concurrent-lib/blob/master/NoLockBlockingQueue.md)

[OneToOneBlockingQueue.java](https://github.com/65487123/zp-concurrent-lib/blob/master/OneToOneBlockingQueue.md)

[DependenNoLocBlocQue.java](https://github.com/65487123/zp-concurrent-lib/blob/master/DependenNoLocBlocQue.md)
 
[DependenOneTOneBlocQue.java](https://github.com/65487123/zp-concurrent-lib/blob/master/DependenOneTOneBlocQue.md)
       
## com.lzp.util.concurrent.latch
[CountDownLatch.java](https://github.com/65487123/zp-concurrent-lib/blob/master/CountDownLatch.md)
## com.lzp.util.concurrent.map
[NoResizeConHashMap.java](https://github.com/65487123/zp-concurrent-lib/blob/master/NoResizeConHashMap.md)
#  任何场景性能稳定比JUC好并且无任何负作用的类(推荐使用的)
    1、ThreadPoolExecutor.java(结合ListenableFuture.java使用):
    并行执行任务额外性能开销小，并且支持返回可添加回调的Future
    2、CountDownLatch.java：
    性能比juc包下的同名类高很多(不管是否开启JIT,实际开启JIT性能差距更大)
#  特定场景推荐使用类
其他
