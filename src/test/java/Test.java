import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.threadpool.RejectExecuHandler;
import com.lzp.util.concurrent.threadpool.ThreadFactoryImpl;

import java.util.concurrent.*;

/**
 * Description:test
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        com.lzp.util.concurrent.threadpool.ThreadPoolExecutor executorService = new com.lzp.util.concurrent.threadpool.ThreadPoolExecutor(4, 4, 2, new LinkedBlockingQueue(),new ThreadFactoryImpl(""));
        //CountDownLatch countDownLatch = new CountDownLatch(5000000);
        long now = System.currentTimeMillis();
        for (int i = 0; i <5000000 ; i++) {
            
        }
        //countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);

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
