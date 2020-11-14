package com.lzp.util.concurrent.threadpool;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Description:适配器类
 *
 * @author: Zeping Lu
 * @date: 2020/11/9 14:09
 */
public abstract class ExecutorServiceAdapter implements ExecutorService {


    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

}
