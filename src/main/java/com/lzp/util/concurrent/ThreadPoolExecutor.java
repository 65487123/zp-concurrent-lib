package com.lzp.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:线程池
 *
 * @author: Lu ZePing
 * @date: 2019/6/2 15:19
 */
public class ThreadPoolExecutor implements Executor {
    private int coreNum;
    private int maxNum;
    private BlockingQueue<Runnable> blockingQueue;
    private int timeout;
    private ThreadFactory threadFactory;
    private RejectExecuHandler rejectedExecutionHandler;
    private AtomicInteger workerSum = new AtomicInteger(0);
    private List<Worker> workerList = new CopyOnWriteArrayList<Worker>();
    private volatile boolean coreThreadMax = false;
    private volatile boolean additionThreadMax = false;

    private volatile boolean shutdown;
    private volatile boolean shutdownNow;

    class Worker implements Runnable {
        private Runnable firstTask;
        private boolean additional;
        private Thread thread;

        Worker(Runnable firstTask, boolean additional) {
            this.firstTask = firstTask;
            this.additional = additional;
            thread = threadFactory.newThread(this);
            thread.start();
        }

        @Override
        public void run() {
            try {
                if (additional) {
                    do {
                        firstTask.run();
                        if (shutdownNow) {
                            workerList.remove(this);
                            return;
                        }
                    }
                    while ((firstTask = blockingQueue.poll(timeout, TimeUnit.SECONDS)) != null);
                } else {
                    while (true) {
                        firstTask.run();
                        if (shutdownNow) {
                            workerList.remove(this);
                            return;
                        }
                        firstTask = blockingQueue.take();
                    }
                }
            } catch (InterruptedException e) {
                //判断是否是调用了shutdownnow()方法而导致的线程中断，如果不是，这个线程也会消失
                if (shutdownNow) {
                    workerList.remove(this);
                    return;
                }
                workerList.remove(this);
                if (this.additional) {
                    workerSum.decrementAndGet();
                    additionThreadMax = false;
                } else {
                    Runnable firstTask;
                    while (!shutdown) {
                        try {
                            if ((firstTask = blockingQueue.poll(5, TimeUnit.SECONDS)) != null) {
                                workerList.add(new Worker(firstTask, false));
                                break;
                            }
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    public ThreadPoolExecutor(int coreNum, int maxNum, int timeout, BlockingQueue blockingQueue, ThreadFactory threadFactory, RejectExecuHandler rejectedExecutionHandler) {
        this.coreNum = coreNum;
        this.maxNum = maxNum;
        this.blockingQueue = blockingQueue;
        this.threadFactory = threadFactory;
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.timeout = timeout;
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown) {
            rejectedExecutionHandler.rejectedExecution(command, this);
            //判断线程数是否已经达到核心线程数
        } else if (coreThreadMax) {
            if (blockingQueue.offer(command)) {
            } else {
                //核心线程数满了,队列也满了,判断线程数是否已经达到最大线程数
                if (additionThreadMax) {
                    rejectedExecutionHandler.rejectedExecution(command, this);
                } else {
                    //没达到最大线程数,进行cas,然后看是否抢到创建额外线程的权利
                    if (workerSum.incrementAndGet() > maxNum) {
                        //没抢到,线程数量减回去
                        workerSum.decrementAndGet();
                        //执行拒绝策略前再入队一次，步成功就执行拒绝策略
                        if (!blockingQueue.offer(command)) {
                            rejectedExecutionHandler.rejectedExecution(command, this);
                        }
                    } else {
                        //抢到了创建额外线程权力
                        if (workerSum.intValue() == maxNum) {
                            this.additionThreadMax = true;
                        }
                        workerList.add(new Worker(command, true));
                    }
                }
            }
        } else {
            //线程数量没达到核心线程数，争抢创建核心线程机会
            if (workerSum.incrementAndGet() > coreNum) {
                //没抢到创建核心线程机会，入队
                if (blockingQueue.offer(command)) {
                    workerSum.decrementAndGet();
                } else {
                    //发现队列已满，查看是否抢到创建额外线程机会
                    if (workerSum.intValue() <= maxNum) {
                        workerList.add(new Worker(command, true));
                    } else {
                        workerSum.decrementAndGet();
                        rejectedExecutionHandler.rejectedExecution(command, this);
                    }
                }
            } else {
                //抢到创建核心线程机会
                if (workerSum.intValue() == coreNum) {
                    this.coreThreadMax = true;
                }
                workerList.add(new Worker(command, false));
            }
        }
    }


    public List<Runnable> shutdownNow() {
        this.shutdown = true;
        this.shutdownNow = true;
        for (Worker worker : workerList) {
            worker.thread.interrupt();
        }
        while (true){
            if (workerList.isEmpty()){
                return new ArrayList(blockingQueue);
            }
        }
    }

    public void shutdown() {
        this.shutdown = true;
    }
}
