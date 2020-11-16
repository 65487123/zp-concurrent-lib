package com.lzp.util.concurrent.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Description:可以添加异步回调方法的Future
 *
 * @author: Zeping Lu
 * @date: 2020/11/11 17:55
 */
public class ListenableFuture<R> implements Runnable, Future<R> {

    /**
     * future的状态
     */
    private static final int NEW = 0;
    private static final int IS_DONE = 1;
    private static final int CATCH_THROWABLE = 2;
    private static final int IS_CANCELED = 3;

    private final Callable<R> callable;

    private volatile R result;

    private volatile Throwable t;

    private volatile Thread thread;

    private volatile int state = 0;

    /**
     * 回调任务
     */
    private List<FutureCallback<R>> futureCallbacks = new ArrayList<>();

    public ListenableFuture(Callable<R> runnable) {
        this.callable = runnable;
    }


    @Override
    public void run() {
        this.thread = Thread.currentThread();
        try {
            if (this.state != IS_CANCELED) {
                result = callable.call();
                //有可能在这上下两行中间被意外中断(cancel()方法里的逻辑)，所以下面第二行需要清除一次中断标记
                this.thread = null;
                Thread.interrupted();
                state = IS_DONE;
                synchronized (this) {
                    this.notifyAll();
                    if (futureCallbacks.size() != 0) {
                        for (FutureCallback<R> futureCallback : futureCallbacks) {
                            try {
                                futureCallback.onSuccess(result);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                        futureCallbacks.clear();
                    }
                }
            }
        } catch (Throwable t) {
            if (this.t instanceof CancellationException) {
                return;
            }
            this.t = t;
            this.thread = null;
            Thread.interrupted();
            state = CATCH_THROWABLE;
            synchronized (this) {
                this.notifyAll();
                if (futureCallbacks.size() != 0) {
                    for (FutureCallback<R> futureCallback : futureCallbacks) {
                        try {
                            futureCallback.onFailure(t);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    futureCallbacks.clear();
                }
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        //如果发现已经完成了，直接返回false
        if (this.state > NEW) {
            return false;
        } else {
            //加锁，防止多个线程同时cancel()出问题
            synchronized (callable) {
                //如果已经完成或者被cancel了，直接返回false
                if (this.state > NEW) {
                    return false;
                } else {
                    //走到这里，有可能已经成功完成了(包括抛异常)，但是不管，直接把状态改为canceled,异常信息也改为cancel异常
                    this.t = new CancellationException();
                    this.state = IS_CANCELED;
                    //有可能刚把状态设为canceled,那边任务才刚跑完，又把状态设为正常结束了，任务结束，会唤醒阻塞get()结果的线程，或者把回调方法丢进执行器
                    if (mayInterruptIfRunning) {
                        //判断是否还在执行，如果任务还在执行就中断
                        if (this.thread != null) {
                            try {
                                //如果在判断的时候还在执行，而刚判断完就执行完了，会抛空指针异常
                                this.thread.interrupt();
                            } catch (NullPointerException ignored) {
                            }
                        }
                    }
                }
                synchronized (this) {
                    this.state = IS_CANCELED;
                    if (futureCallbacks.size() != 0) {
                        for (FutureCallback<R> futureCallback : futureCallbacks) {
                            try {
                                futureCallback.onFailure(t);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                        futureCallbacks.clear();
                    }
                    return true;
                }
            }
        }
    }


    @Override
    public boolean isCancelled() {
        return this.state == IS_CANCELED;
    }

    @Override
    public boolean isDone() {
        return this.state > NEW;
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        if (this.state == IS_CANCELED) {
            throw (CancellationException) t;
        }
        synchronized (this) {
            while (this.state < IS_DONE) {
                this.wait();
            }
        }
        if (this.state == CATCH_THROWABLE) {
            throw new ExecutionException(t);
        } else {
            return result;
        }
    }

    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (this.state == IS_CANCELED) {
            throw (CancellationException) t;
        }
        long remainingTime = unit.toMillis(timeout);
        long deadLine = System.currentTimeMillis() + remainingTime;
        synchronized (this) {
            while (this.state < IS_DONE && remainingTime > 0) {
                this.wait(remainingTime);
                remainingTime = deadLine - System.currentTimeMillis();
            }
            if (this.state < IS_DONE) {
                throw new TimeoutException();
            } else if (this.state == CATCH_THROWABLE) {
                throw new ExecutionException(t);
            } else if (this.state == IS_CANCELED) {
                throw new CancellationException();
            } else {
                return result;
            }
        }
    }

    /**
     * @param futureCallback 回调接口实现类
     * @description 添加回调任务。回调任务是可以添加多个的,不管添加几个,都会执行
     */
    public void addCallback(FutureCallback<R> futureCallback) {
        if (futureCallback == null) {
            throw new NullPointerException();
        }
        if (this.state > NEW) {
            if (this.state == IS_DONE) {
                //有可能刚成功又被cancel了，但是不影响，这次就当成功好了
                futureCallback.onSuccess(result);
            } else {
                futureCallback.onFailure(t);
            }
        } else {
            synchronized (this) {
                this.futureCallbacks.add(futureCallback);
            }
        }
    }
}
