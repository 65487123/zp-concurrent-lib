package com.lzp.util.concurrent.threadpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Description:可以添加异步回调方法的Future
 *
 * @author: Zeping Lu
 * @date: 2020/11/11 17:55
 */
public class ListenableFuture<R> implements Runnable, Future<R> {

    private static final int NEW = 0;
    private static final int IS_RUNNING = 1;
    private static final int IS_DONE = 2;
    private static final int CATCH_THROWABLE = 3;
    private static final int IS_CANCELED = 4;

    private final Callable<R> callable;

    private volatile R result;

    private volatile Throwable t;

    private volatile Thread thread;

    private boolean interrupted = false;

    private volatile int state = 0;

    private Map<FutureCallback<R>,Executor> futureCallbackAndExces = new HashMap<>();

    public ListenableFuture(Callable<R> runnable) {
        this.callable = runnable;
    }


    @Override
    public void run() {
        this.thread = Thread.currentThread();
        this.state = IS_RUNNING;
        try {
            synchronized (this) {
                if (this.state != IS_CANCELED) {
                    result = callable.call();
                    state = 1;
                    if (futureCallbackAndExces.size() != 0) {
                        for (Map.Entry<FutureCallback<R>, Executor> entry : futureCallbackAndExces.entrySet()) {
                            entry.getValue().execute(() -> entry.getKey().onSuccess(result));
                        }
                        futureCallbackAndExces.clear();
                    }
                }
                this.notifyAll();
            }
        } catch (Throwable t) {
            if (this.t instanceof CancellationException) {
                return;
            }
            this.t = t;
            synchronized (this) {
                state = 2;
                if (futureCallbackAndExces.size() != 0) {
                    for (Map.Entry<FutureCallback<R>, Executor> entry : futureCallbackAndExces.entrySet()) {
                        entry.getValue().execute(() -> entry.getKey().onFailure(t));
                    }
                    futureCallbackAndExces.clear();
                }
                this.notifyAll();
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        //todo 暂时有bug的，有时间再写
        if (this.state > IS_RUNNING) {
            return false;
        } else {
            this.state = IS_CANCELED;
            this.t = new CancellationException();
            if (mayInterruptIfRunning){
                synchronized (callable){
                    if (this.state == IS_RUNNING && !interrupted){
                        this.thread.interrupt();
                    }
                }
            }
            synchronized (this) {
                this.state = IS_CANCELED;
                if (futureCallbackAndExces.size() != 0) {
                    for (Map.Entry<FutureCallback<R>, Executor> entry : futureCallbackAndExces.entrySet()) {
                        entry.getValue().execute(() -> entry.getKey().onFailure(t));
                    }
                    futureCallbackAndExces.clear();
                }
            }
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return this.state == IS_CANCELED;
    }

    @Override
    public boolean isDone() {
        return this.state > IS_RUNNING;
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        if (this.state == IS_CANCELED){
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

    /**
     * @Description 添加回调方法
     */
    public void addCallback(Executor executor, FutureCallback<R> futureCallback) {
        if (executor == null || futureCallback == null) {
            throw new NullPointerException();
        }
        if (this.state > IS_RUNNING) {
            if (this.state == IS_DONE) {
                executor.execute(() -> futureCallback.onSuccess(result));
            } else {
                executor.execute(() -> futureCallback.onFailure(t));
            }
        } else {
            synchronized (this) {
                if (this.state > IS_RUNNING) {
                    if (this.state == IS_DONE) {
                        executor.execute(() -> futureCallback.onSuccess(result));
                    } else {
                        executor.execute(() -> futureCallback.onFailure(t));
                    }
                } else {
                    this.futureCallbackAndExces.put(futureCallback, executor);
                }
            }
        }
    }
}
