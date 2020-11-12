package com.lzp.util.concurrent.threadpool;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:线程池，和jdk的线程池用法一样
 * 核心线程从创建后就一直存在，直到线程池被关闭，额外线程空闲一段时间就会死亡
 *
 * 相比{@link java.util.concurrent.ThreadPoolExecutor}的优势
 *
 * 1、达到最大线程数时：
 * JUC自带的线程池：当核心线程数满了，队列也满了，这时候还有大量任务进来。处理这些任务时发现当前
 * 线程数还没达到最大线程数，这些任务会争夺创建额外线程的权利，当没有抢到创建额外线程的权利，直接执行拒绝策略。
 * 这个线程池：当核心线程数满了，队列也满了，这时候还有大量任务进来，同样这些任务会争夺创建额外线程的权利，
 * 但是，如果没有抢到争夺额外线程的权利，会再次把任务丢进阻塞队列一次,如果队列还是满的，才会执行拒绝策略。(虽
 * 然第二次能加进去的概率很小很小...)
 *
 * 2、性能高
 * 简单自测了下，execute()大量小任务，性能比{@link java.util.concurrent.ThreadPoolExecutor}要高很多。
 * 由于性能比jdk自带的线程池高，进一步降低了执行拒绝策略的概率(这才是主要原因）
 *
 * 经过实际测试，使用长度500万的有界队列，使用默认的拒绝策略(抛异常),核心线程数为4，最大线程数为8，
 * 执行1亿个小任务，JDK自带的线程池，几乎百分百抛出拒绝策略的异常，这个线程池几乎百分百完成了所有
 * 任务而没有执行拒绝策略。(测试结果和机器有关，总的来说，当队列容量远小于总任务数量，核心线程数量又小于
 * 最大线程数时，执行拒绝策略的概率比JDK自带的线程池小很多)
 *
 * 2、重写了submit(),返回的Future可以增加异步回调方法
 * JDK自带的线程池，执行submit()返回的是{@link FutureTask}对象，这个对象获取结果需要阻塞等待，
 * 而这个返回的是{@link ListenableFuture},这个future能添加异步回调方法，当任务执行结束，会执行回调方法。
 *
 * 2、性能高
 * 简单自测了下，execute()大量小任务，性能比{@link java.util.concurrent.ThreadPoolExecutor}要高很多。
 * 由于性能比jdk自带的线程池高，进一步降低了执行拒绝策略的概率
 *
 * @author: Lu ZePing
 * @date: 2019/6/2 15:19
 */
public class ThreadPoolExecutor extends ExecutorServiceAdapter {

    /**
     * 核心线程数量
     */
    private int coreNum;

    /**
     * 最大线程数量
     */
    private int maxNum;

    /**
     * 阻塞队列
     */
    private BlockingQueue<Runnable> blockingQueue;

    /**
     * 额外线程最大空闲时间
     */
    private int timeout;

    /**
     * 线程工厂
     */
    private ThreadFactory threadFactory;

    /**
     * 拒绝策略
     */
    private RejectExecuHandler rejectedExecutionHandler;

    /**
     * 当前的工作线程数量
     */
    private AtomicInteger workerSum = new AtomicInteger(0);
    /**
     * 当前的工作线程
     */

    private List<Worker> workerList = new CopyOnWriteArrayList<>();

    /**
     * 标志核心线程是否已满
     */
    private volatile boolean coreThreadMax = false;

    /**
     * 标志额外线程是否已满
     */
    private volatile boolean additionThreadMax = false;

    /**
     * 标志线程池是否已经被关闭(调用shutDown())
     */
    private volatile boolean shutdown;

    /**
     * 标志线程池是否已经被立即关闭(调用shutDownNow())
     */
    private volatile boolean shutdownNow;

    /**
     * Description:工作线程的封装
     */
    class Worker implements Runnable {
        /**
         * 工作线程的第一个任务
         */
        private Runnable firstTask;
        /**
         * 标志是否是额外线程
         */
        private boolean additional;
        /**
         * 工作线程的引用
         */
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
                    workerList.remove(this);
                    workerSum.decrementAndGet();
                    additionThreadMax = false;
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

    /**
     * 抛出{@code RejectedExecutionException}的拒绝策略
     */
    static class AbortPolicy implements RejectExecuHandler {

        /**
         * 总是会抛出 RejectedExecutionException.
         *
         * @param r        需要执行的任务
         * @param executor 试图执行这个任务的执行器
         * @throws RejectedExecutionException always
         */
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            throw new RejectedExecutionException("Task " + r.toString() +
                    " rejected from " +
                    executor.toString());
        }
    }

    /**
     * 线程池构造器，额外线程超时时间单位为秒
     *
     * @param coreNum                  核心线程数
     * @param maxNum                   最大线程数
     * @param timeout                  额外线程最大空闲时间
     * @param blockingQueue            阻塞队列
     * @param threadFactory            线程工厂
     * @param rejectedExecutionHandler 拒绝策略
     */
    public ThreadPoolExecutor(int coreNum, int maxNum, int timeout, BlockingQueue blockingQueue, ThreadFactory threadFactory, RejectExecuHandler rejectedExecutionHandler) {
        this.coreNum = coreNum;
        this.maxNum = maxNum;
        this.blockingQueue = blockingQueue;
        this.threadFactory = threadFactory;
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.timeout = timeout;
    }


    /**
     * 线程池构造器，额外线程超时时间单位为秒
     * 使用默认拒绝策略(抛异常)
     */
    public ThreadPoolExecutor(int coreNum, int maxNum, int timeout, BlockingQueue blockingQueue, ThreadFactory threadFactory) {
        this(coreNum, maxNum, timeout, blockingQueue, threadFactory, new AbortPolicy());
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


    @Override
    public List<Runnable> shutdownNow() {
        this.shutdown = true;
        this.shutdownNow = true;
        for (Worker worker : workerList) {
            worker.thread.interrupt();
        }
        while (true) {
            if (workerList.isEmpty()) {
                return new ArrayList(blockingQueue);
            }
        }
    }


    /**
     * Returns the current number of threads in the pool.
     *
     * @return the number of threads
     */
    public int getPoolSize() {
        return workerList.size();
    }


    @Override
    public void shutdown() {
        this.shutdown = true;

    }


    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdownNow || (shutdown && blockingQueue.isEmpty());
    }


}
