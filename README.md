# zp-concurrent-lib
 自己写的一些java高性能并发类库
#  功能介绍
    java并发库，使用方法和juc包下的类相同（和juc下的工具类实现同样的接口）,但是性能比其要高
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
