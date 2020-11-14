import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.threadpool.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Description:test
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        //创建线程池(com.lzp.util.concurrent.threadpool.ThreadPoolExecutor)
        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, new LinkedBlockingQueue(), new ThreadFactoryImpl(""));
        //执行submit()方法,我的这个线程池就会返回ListenableFuture对象
        ListenableFuture<String> listenableFuture = (ListenableFuture<String>) executorService.submit(() -> {
            Thread.sleep(5000);
            return "任务完成";
        });
        //添加异步回调方法
        listenableFuture.addCallback(executorService, new FutureCallback<String>() {
            @Override
            public void onSuccess(String s) {
                System.out.println(System.currentTimeMillis() + " " +s);
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println(t.toString());
            }
        });
        //执行主线程的其他事情
        System.out.println(System.currentTimeMillis() + ": 执行主线程的其他事情");



        /*System.out.println(((ThreadPoolExecutor)executorService).getPoolSize());
        Thread.sleep(15000);
        System.out.println(((ThreadPoolExecutor)executorService).getPoolSize());
        //now = System.currentTimeMillis();
        for (int i = 0; i < 50000; i++) {
            executorService.execute(runnable);
        }
        executorService.shutdown();
        System.out.println(((ThreadPoolExecutor)executorService).getPoolSize());*/
    }




    static int sum() {
        int a = 0;
        for (int i = 0; i < 1; i++) {
            a += i;
        }
        return a;
    }

    static void test() throws InterruptedException {

    }
}
