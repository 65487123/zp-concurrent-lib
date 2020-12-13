
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


import com.lzp.util.concurrent.blockingQueue.withlock.DelayQueue;

import java.util.List;
import java.util.concurrent.*;

/**
 * Description:实现了{@link ScheduledExecutorService}的线程池
 *
 * @author: Zeping Lu
 * @date: 2020/12/3 15:42
 */
public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor
        implements ScheduledExecutorService {


    public ScheduledThreadPoolExecutor(int coreNum) {
        super(coreNum, Integer.MAX_VALUE, 0, new DelayQueue());
    }

    public ScheduledThreadPoolExecutor(int coreNum, ThreadFactory threadFactory) {
        super(coreNum, Integer.MAX_VALUE, 0, new DelayQueue(), threadFactory);
    }

    public ScheduledThreadPoolExecutor(int coreNum, RejectExecuHandler rejectExecuHandler) {
        super(coreNum, Integer.MAX_VALUE, 0, new DelayQueue(), rejectExecuHandler);
    }

    public ScheduledThreadPoolExecutor(int coreNum, ThreadFactory threadFactory, RejectExecuHandler rejectExecuHandler) {
        super(coreNum, Integer.MAX_VALUE, 0, new DelayQueue(), threadFactory, rejectExecuHandler);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledFutureImp<?> scheduledFuture = new ScheduledFutureImp(command, delay, unit, this.getBlockingQueue());
        execute(scheduledFuture);
        return scheduledFuture;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        ScheduledFutureImp<V> scheduledFuture = new ScheduledFutureImp<>(callable, delay, unit, this.getBlockingQueue());
        execute(scheduledFuture);
        return scheduledFuture;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledFutureImp<?> scheduledFuture = new ScheduledFutureImp<>(command, initialDelay, period, unit, true, this.getBlockingQueue());
        execute(scheduledFuture);
        return scheduledFuture;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ScheduledFutureImp<?> scheduledFuture = new ScheduledFutureImp<>(command, initialDelay, delay, unit, false, this.getBlockingQueue());
        execute(scheduledFuture);
        return scheduledFuture;
    }

    @Override
    public List<Runnable> shutdownNow() {
        for (Object future : this.getBlockingQueue()) {
            ((ScheduledFuture) future).cancel(false);
        }
        return super.shutdownNow();
    }

    @Override
    public void shutdown() {
        ThreadPoolExecutor executorService = this;
        this.execute(new ScheduledFutureImp(executorService::stop, 0, TimeUnit.SECONDS, this.getBlockingQueue()));
        this.setShutdown();
        for (Object future : this.getBlockingQueue()) {
            ((ScheduledFuture) future).cancel(false);
        }
    }
}
