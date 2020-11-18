# CountDownLatch
 用法和java.util.concurrent.CountDownLatch完全一样，但是性能比其要高
 
### 性能测试结果
    8个线程同时减。开启JIT，这个CountDownLatch的性能是juc.CountDownLatch的1.5-2倍，
    关闭JIT，是juc.CountDownLatch的1.2倍左右。这个可以自己拉代码测测
    
   
