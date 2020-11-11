# ThreadPoolExecutor
 线程池，功能和用法和java.util.concurrent.ThreadPoolExecutor完全一样

# 特点：
    1、简洁，线程池核心功能都实现了，但是没有其他复杂的骚操作。
    2、简单自测了下，execute()大量小任务，性能比java.util.concurrent.ThreadPoolExecutor要高。
    
# 和JUC包下的线程池性能对比：
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
    我的这个线程池耗时基本都在9500-10000ms之间，JUC的线程池耗时基本都在11500左右
    
