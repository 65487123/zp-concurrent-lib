# CountDownLatch
  线程安全的Map,
  带合理参数初始化,比java.util.concurrent.ConcurrentHashMap性能高(写元素、读元素、删元素、遍历元素、总内存占用等)
 # 与java.util.concurrent.ConcurrentHashMap主要区别:
    1、消除了树化以及树退化等操作:因为达到树化条件的概率很低，所以没必要为了这么低的概率去优化(增加红黑树结构，需要
    增加类型判断)。并且树化只是增加了查性能，写性能会降低
    2、没有扩容机制,所以要求使用者预估最大存储键值对数量(ConcurrentHashMap使用时也建议带参数初始化避免扩容,但是就算不去
    扩容，put、remove等过程避免不了检查是否在扩容的操作）
    3、优化了些细节，减少不必要的操作
 # 总结
  简单的其实是最好的，合适的场景选择合适的工具
   
