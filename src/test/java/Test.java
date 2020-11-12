import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
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
    public static void main(String[] args) throws InterruptedException{
        ExecutorService executorService  = new ThreadPoolExecutor(4,4,0,new LinkedBlockingQueue(),new ThreadFactoryImpl("test"));
        CountDownLatch countDownLatch = new CountDownLatch(5000000);
        Runnable runnable = () -> {
            sum();
            countDownLatch.countDown();
        };
        long now = System.currentTimeMillis();
        for (int i = 0 ;i <5000000 ;i++ ) {
            executorService.execute(runnable);
        }
        countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);
    }
    static int sum() {
        int a = 0;
        for (int i = 0; i < 10; i++) {
            a += i;
        }
        return a;
    }

    static void test() throws InterruptedException {

    }
}
