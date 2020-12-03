
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
 * Description:实现了{@link ScheduledExecutorService}的线程池
 *
 * @author: Zeping Lu
 * @date: 2020/12/3 15:42
 */
public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor
        implements ScheduledExecutorService {


    public ScheduledThreadPoolExecutor(int coreNum, int maxNum, int timeout, BlockingQueue blockingQueue, ThreadFactory threadFactory, RejectExecuHandler rejectedExecutionHandler) {
        super(coreNum, maxNum, timeout, blockingQueue, threadFactory, rejectedExecutionHandler);
    }

    public ScheduledThreadPoolExecutor(int coreNum, int maxNum, int timeout, BlockingQueue blockingQueue, ThreadFactory threadFactory) {
        super(coreNum, maxNum, timeout, blockingQueue, threadFactory);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return null;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return null;
    }
}
