package com.lzp.util.concurrent.threadpool;

/**
 * Description:拒接策略接口
 *
 * @author: Lu ZePing
 * @date: 2019/7/20 12:19
 */
public interface RejectExecuHandler {

    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);

}
