import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.threadpool.RejectExecuHandler;
import com.lzp.util.concurrent.threadpool.ThreadFactoryImpl;
import com.lzp.util.concurrent.threadpool.ThreadPoolExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = new java.util.concurrent.ThreadPoolExecutor(4, 8, 2, TimeUnit.SECONDS, new ArrayBlockingQueue(45000), new ThreadFactoryImpl("test"), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor executor) {
                r.run();
            }
        });
        CountDownLatch countDownLatch = new CountDownLatch(100000);
        Runnable runnable = () -> {
            sum();
            countDownLatch.countDown();
        };
        long now = System.currentTimeMillis();
        for (int i = 0; i < 50000; i++) {
            executorService.execute(runnable);
        }
        System.out.println(((java.util.concurrent.ThreadPoolExecutor)executorService).getPoolSize());

        Thread.sleep(15000);

        System.out.println(((java.util.concurrent.ThreadPoolExecutor)executorService).getPoolSize());
        //now = System.currentTimeMillis();
        for (int i = 0; i < 50000; i++) {
            executorService.execute(runnable);
        }
        executorService.shutdown();
        System.out.println(((java.util.concurrent.ThreadPoolExecutor)executorService).getPoolSize());
    }




    static int sum() {
        int a = 0;
        for (int i = 0; i < 100000; i++) {
            a += i;
        }
        return a;
    }

    static void test() throws InterruptedException {

    }
}
