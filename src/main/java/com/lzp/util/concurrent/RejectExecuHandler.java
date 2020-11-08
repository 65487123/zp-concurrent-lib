package com.lzp.util.concurrent;

/**
 * @author 86173
 */
public interface RejectExecuHandler {

    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);

}
