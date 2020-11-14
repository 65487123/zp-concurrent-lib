# ListenableFuture
 实现了java.util.concurrent.Future接口，并且增加了添加异步回调功能
 
# 功能
    Future接口定义的功能都实现了，新加了一个添加回调方法的功能
    /**
     * @param futureCallback 回调接口实现类
     * @description 添加回调任务。回调任务是可以添加多个的,不管添加几个,都会执行
     */
    public void addCallback(FutureCallback<R> futureCallback)
    
    
# 使用方法
    1、new一个com.lzp.util.concurrent.threadpool.ThreadPoolExecutor
    2、执行线程池的submit(Callable<T> task)方法，会返回一个Future
    3、用ListenableFuture接收
    4、可以通过get()阻塞获取执行结果，也可以通过addCallback添加异步回调方法
# 例子
![example](https://github.com/65487123/zp-concurrent-lib/raw/master/picture/FutureDemo.png)   
   
          

    
