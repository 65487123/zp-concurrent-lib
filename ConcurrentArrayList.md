# ConcurrentArrayList
 线程安全的List，适合写多读少或者读写频率差不多
 
### JDK提供的线程安全List局限性
    1 java.util.Vector:所有操作全加锁，读和写不能同时进行
    2 Collections.synchronizedList 和Vector一样，所有操作加锁，大量线程时性能很低
    3 java.util.concurrent.CopyOnWriteArrayList：
    写时复制，大量写操作时，频繁new数组并复制，严重影响性能，数组元素多时很容易造成OOM
    适合读多写少(最好是几乎不用写,全都是读操作)

### 这个List实现原理及适用场景 
    1、写时加锁，读时无锁(通过Unsafe直接读内存值),适合写多读少或者读写频率差不多
    2、开始遍历时，保存一份当时的视图，然后只遍历这个试图。数据实时性可能没那么高，但是性能
    比其他List高(包括非线程安全的list)
    3、jdk原生序列化性能比ArrayList高
    
