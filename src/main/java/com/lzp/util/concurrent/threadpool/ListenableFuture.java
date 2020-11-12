package com.lzp.util.concurrent.threadpool;

import java.util.concurrent.*;

/**
 * Description:
 *
 * @author: Zeping Lu
 * @date: 2020/11/11 17:55
 */
public class ListenableFuture<R> implements Runnable, Future<R> {

    private Callable<R> callable;

    private R result;

    private Exception e;


    private volatile int state = 0;

    private static final int NOT_DONE = 0;

    private static final int IS_DONE = 1;

    private static final int CATCH_EXCEPTION = 2;

    public ListenableFuture(Callable<R> runnable) {
        this.callable = runnable;
    }

    @Override
    public void run() {
        try {
            result = callable.call();
            synchronized (this) {
                state = 1;
                this.notifyAll();
            }
        } catch (Exception e) {
            this.e = e;
            synchronized (this) {
                state = 2;
                this.notifyAll();
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return this.state > NOT_DONE;
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        synchronized (this) {
            while (this.state < IS_DONE) {
                this.wait();
            }
        }
        if (this.state == CATCH_EXCEPTION) {
            throw new ExecutionException(e);
        } else {
            return result;
        }
    }

    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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
        } else if (this.state == CATCH_EXCEPTION) {
            throw new ExecutionException(e);
        } else {
            return result;
        }
    }
}
