package com.lzp.util.concurrent.threadpool;

import java.util.concurrent.RejectedExecutionException;

/**
 * Description:拒接策略接口
 *
 * @author: Lu ZePing
 * @date: 2019/7/20 12:19
 */
public interface RejectExecuHandler {

    /**
     * Method that may be invoked by a {@link ThreadPoolExecutor} when
     * {@link java.util.concurrent.ThreadPoolExecutor#execute execute} cannot accept a
     * task.  This may occur when no more threads or queue slots are
     * available because their bounds would be exceeded, or upon
     * shutdown of the Executor.
     *
     * <p>In the absence of other alternatives, the method may throw
     * an unchecked {@link RejectedExecutionException}, which will be
     * propagated to the caller of {@code execute}.
     *
     * @param r the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     * @throws RejectedExecutionException if there is no remedy
     */
    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);

}
