
/* Copyright zeping lu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.lzp.util.concurrent.threadpool;

import java.util.concurrent.*;

/**
 * Description:实现了ScheduledFuture接口
 *
 * @author: Zeping Lu
 * @date: 2020/12/4 17:55
 */
public class ScheduledFutureImp<R> extends ListenableFuture<R> implements ScheduledFuture<R> {

    private BlockingQueue<ScheduledFutureImp<R>> taskQueue;

    private long deadLine;

    /**
     * 是否是周期性的任务
     */
    private boolean periodic = false;

    /**
     * 是否以固定频率运行(如果periodic为true，这个为false，说明是以固定延迟运行)
     */
    private boolean periodicAtFixedRate;

    /**
     * 周期任务时有用
     */
    private long time;

    public ScheduledFutureImp(Callable<R> callable, long delay, TimeUnit unit, BlockingQueue<ScheduledFutureImp<R>> taskQueue) {
        super(callable);
        this.deadLine = System.currentTimeMillis() + unit.toMillis(delay);
        this.taskQueue = taskQueue;
    }

    public ScheduledFutureImp(Runnable runnable, long delay, TimeUnit unit, BlockingQueue<ScheduledFutureImp<R>> taskQueue) {
        super(runnable);
        this.deadLine = System.currentTimeMillis() + unit.toMillis(delay);
        this.taskQueue = taskQueue;
    }

    public ScheduledFutureImp(Runnable runnable, long initialDelay, long time, TimeUnit unit, boolean periodicAtFixedRate,
                              BlockingQueue<ScheduledFutureImp<R>> taskQueue) {
        super(runnable);
        this.periodic = true;
        this.deadLine = System.currentTimeMillis() + unit.toMillis(initialDelay);
        this.periodicAtFixedRate = periodicAtFixedRate;
        this.time = unit.toMillis(time);
        this.taskQueue = taskQueue;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(deadLine - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return deadLine - ((ScheduledFutureImp) o).deadLine > 0 ? 1 : -1;
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() < this.deadLine) {
            this.taskQueue.offer(this);
        } else {
            if (periodic) {
                if (periodicAtFixedRate) {
                    this.deadLine += this.time;
                    try {
                        if (this.getState() == 0) {
                            executeCall();
                            //如果线程池已经被shutdown了，那么队列中会多这一个任务，没什么大影响
                            this.taskQueue.offer(this);
                        }
                    } catch (Throwable t) {
                        setThrowableIfNecessary(t);
                    }
                } else {
                    try {
                        if (this.getState() == 0) {
                            executeCall();
                            this.deadLine = System.currentTimeMillis() + this.time;
                            //如果线程池已经被shutdown了，那么队列中会多这一个任务，没什么大影响
                            this.taskQueue.offer(this);
                        }
                    } catch (Throwable t) {
                        setThrowableIfNecessary(t);
                    }
                }
            } else {
                super.run();
            }
        }
    }

    private void executeCall() throws Exception {
        this.setThread(Thread.currentThread());
        this.call();
        this.setThread(null);
        Thread.interrupted();
    }

    private void setThrowableIfNecessary (Throwable t) {
        if (this.getThrowable() instanceof CancellationException) {
            return;
        }
        this.setThrowable(t);
        this.setState(2);
        synchronized (this) {
            this.notifyAll();
        }
    }



    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!periodic) {
            return super.cancel(mayInterruptIfRunning);
        } else {
            if (this.getState() > 0) {
                return false;
            } else {
                synchronized (this) {
                    if (this.getState() > 0) {
                        return false;
                    } else {
                        this.setThrowable(new CancellationException());
                        this.setState(3);
                        if (mayInterruptIfRunning) {
                            if (this.getThread() != null) {
                                try {
                                    //如果在判断的时候还在执行，而刚判断完就执行完了，会抛空指针异常
                                    this.getThread().interrupt();
                                } catch (NullPointerException ignored) {
                                }
                            }
                        }
                        this.notifyAll();
                        return true;
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "delayed: " + getDelay(TimeUnit.MILLISECONDS) + "ms";
    }
}
