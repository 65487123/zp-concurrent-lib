# DelayQueue
延迟队列
   和java.util.concurrent.DelayQueue}区别
   1、java.util.concurrent.DelayQueue}用的是Lock锁，
   jdk优化后的synchronized锁性能是比lock锁要好的,所以这里我用
   synchronized锁再实现了一个延迟队列。
   2、由于延迟队列中的优先级队列只供这个延迟队列使用，
   对优先级队列进行了修改，减少了不必要的操作。
 
   
