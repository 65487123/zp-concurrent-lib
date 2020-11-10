# OneToOneBlockingQueue
 高性能阻塞队列，一个生产者对一个消费者（线程),同样无锁设计，同样解决了伪共享问题。
##### 优点
    当生产者和消费者都只有一个线程时，性能比NoLockBlockingQueue要高。
##### 缺点
    和NoLockBlockingQueue类似

    
