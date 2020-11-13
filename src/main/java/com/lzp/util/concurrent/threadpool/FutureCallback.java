package com.lzp.util.concurrent.threadpool;

/**
 * Description:回调接口
 *
 * @author: Zeping Lu
 * @date: 2020/11/13 12:48
 */
public interface FutureCallback<R> {
    /**
     * @return
     * @Description 当future已经成功完成，执行的回调方法
     * @Param future返回的结果
     */
    void onSuccess(R r);

    /**
     * @return
     * @Description 当future失败时，执行的回调方法
     * @Param future抛出的异常或错误
     */
    void onFailure(Throwable t);
}
