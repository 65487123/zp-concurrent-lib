import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.threadpool.*;
import com.lzp.util.concurrent.threadpool.ThreadPoolExecutor;

import java.util.concurrent.*;


/**
 * Description:test
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        //创建线程池(com.lzp.util.concurrent.threadpool.ThreadPoolExecutor)
        ExecutorService executorService = new ThreadPoolExecutor(2, 2, 0, new LinkedBlockingQueue(), new ThreadFactoryImpl(""));
        long now = System.currentTimeMillis();
        Future future = executorService.submit(() -> {
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(1000);
        future.cancel(true);
        executorService.shutdown();
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
