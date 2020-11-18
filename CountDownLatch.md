# CountDownLatch
 用法和java.util.concurrent.CountDownLatch完全一样，但是性能比其要高
 
### 性能测试结果
    8个线程同时减。开启JIT，这个CountDownLatch的性能是juc.CountDownLatch的2以上倍，
    关闭JIT，是juc.CountDownLatch的1.5倍以上。不同主机测试结果可能略有差异，具体性能
    情况可以自己拉代码测测
    
   
