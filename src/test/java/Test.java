import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.threadpool.ThreadFactoryImpl;

import java.util.concurrent.*;

/**
 * Description:
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService  = new ThreadPoolExecutor(8,8,0,TimeUnit.SECONDS,new LinkedBlockingQueue(),new ThreadFactoryImpl("test"));
        CountDownLatch countDownLatch = new CountDownLatch(100000000);
        Runnable runnable = () -> {
            sum();
            countDownLatch.countDown();
        };
        long now = System.currentTimeMillis();
        for (int i = 0 ;i <100000000 ;i++ ) {
            executorService.execute(runnable);
        }
        countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);
    }

    static int sum() {
        int a = 0;
        for (int i = 0; i < 100; i++) {
            a += i;
        }
        return a;
    }
}
