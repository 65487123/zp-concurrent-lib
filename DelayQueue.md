# DelayQueue
延迟队列
   和java.util.concurrent.DelayQueue区别  
   1、java.util.concurrent.DelayQueue用的是Lock锁,jdk优化后的 
   synchronized锁性能不比Lock锁差多少,而且不额外占用内存, 所以这里
   我用synchronized锁再实现了一个延迟队列。  
   2、由于延迟队列中的优先级队列只供这个延迟队列使用，
   对优先级队列进行了修改，减少了不必要的操作。
 
   
