
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

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Description:实现了ScheduledFuture接口
 *
 * @author: Zeping Lu
 * @date: 2020/12/4 17:55
 */
public class ScheduledFutureImp<R> extends ListenableFuture<R> implements ScheduledFuture<R> {

    private long remainingDelay;

    public ScheduledFutureImp(Callable<R> callable, long delay, TimeUnit unit) {
        super(callable);
        this.remainingDelay = unit.toMillis(delay);
    }

    public ScheduledFutureImp(Runnable runnable,long delay, TimeUnit unit) {
        super(runnable);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }

    @Override
    public void run() {
        super.run();
    }
}
