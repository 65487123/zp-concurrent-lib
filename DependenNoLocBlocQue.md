# DependenNoLocBlocQue
 和[NoLockBlockingQueue](https://github.com/65487123/zp-concurrent-lib/blob/master/NoLockBlockingQueue.md)适用场景不同，这个队列适用于一个生产者生产下一个消息依赖于这个生产者生产的上一个消息被消费而 NoLockBlockingQueue 适用于一个生产者生产下一个消息不依赖于这个生产者生产的上一个消息被消费
##### 实现上和NoLockBlockingQueue的区别
    和NoLockBlockingQueue一样，底层都是一个数组。区别是，NoLockBlockingQueue进行put的时候发现数组当前索引位置的元素
    没有被消费会sleep一毫秒，进行take的时候发现数组当前索引位置没有元素同样会sleep一毫秒，这样做的好处是1、进一步消除
    伪共享:当头指针倒追尾指针时，头指针会等尾指针跑一毫秒，当尾指针追上头指针时，尾指针会等头指针跑一毫秒。2、在队列已
    满或者队列空的时候，让出了大量cpu使用权，减少大量空轮询次数。而这个队列不会调用sleep(),而是调用Thread.yield()
##### 相比NoLockBlockingQueue，缺点：
    在队列空的时候，调用take()会占用一个cpu的大量使用权。(在cpu比较空闲时，基本会跑满一个逻辑cpu)

    
