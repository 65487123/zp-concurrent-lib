import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.withlock.OptimizedArrBlockQueue;
import com.lzp.util.concurrent.threadpool.*;
import com.lzp.util.concurrent.threadpool.ThreadPoolExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Description:test
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    static volatile int a = ThreadLocalRandom.current().nextInt(10000);
    static volatile int b = ThreadLocalRandom.current().nextInt(10000);
    static AtomicInteger c = new AtomicInteger();
    static Object[]  d = new Object[ThreadLocalRandom.current().nextInt(10000)];
    static int r = d.length;
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        BlockingQueue blockingQueue = new OptimizedArrBlockQueue(100000);
        CountDownLatch countDownLatch = new CountDownLatch(2000000);
        long now = System.currentTimeMillis();
        new Thread(() -> put(blockingQueue)).start();
        new Thread(() -> {
            for (int i = 0; i <2000000 ; i++) {
                try {
                    blockingQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        }).start();
        countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);
    }
    static void put(BlockingQueue arrayBlockingQueue){
        for (int i = 0; i <2000000 ; i++) {
            try {
                arrayBlockingQueue.put(String.valueOf(i));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void put(OneToOneBlockingQueue arrayBlockingQueue){
        for (int i = 0; i <800000 ; i++) {
            try {
                arrayBlockingQueue.put(String.valueOf(i));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
