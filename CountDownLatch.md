# CountDownLatch
 对 link java.util.concurrent.CountDownLatch 做了优化
 1、性能比他高很多
 2、可重用(调用reset(),计数器会复位)
 
### 性能测试结果
    8个线程同时减。开启JIT，这个CountDownLatch的性能是juc.CountDownLatch的2以上倍，
    关闭JIT，是juc.CountDownLatch的1.5倍以上。不同主机测试结果可能略有差异，具体性能
    情况可以自己拉代码测测
    
   
