# ScheduledThreadPoolExecutor
 实现了ScheduledExecutorService的线程池
 
# 功能
    和juc.ScheduledThreadPoolExecutor功能一样，可以用来实现单次延迟任务、
    固定频率定时任务、固定间隔定时任务。
    
    
# 特点
    时间间隔稳定(jdk自带的也很稳定)
# 实现这个类的原因
    这个类倒是没有对JDK中提供的ScheduledThreadPoolExecutor做了明显的优化，
    实现这个类主要是让我这一套线程池功能更完善，并且通过实现这个类来更深层次
    了解这个类实现的接口的规范、定时任务、延迟队列主要实现原理等等。
   
          

    
