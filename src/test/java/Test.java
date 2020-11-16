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
    static Object[] d = new Object[ThreadLocalRandom.current().nextInt(10000)];
    static int r = d.length;

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        com.lzp.util.concurrent.threadpool.ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 0, new OptimizedArrBlockQueue(10000000), new ThreadFactoryImpl(""));
        BlockingQueue blockingQueue = new OptimizedArrBlockQueue(5);
        CountDownLatch countDownLatch = new CountDownLatch(10000000);
        blockingQueue.put("1");
        System.out.println(blockingQueue.size());
        System.out.println(blockingQueue.remainingCapacity());
        blockingQueue.put("1");
        System.out.println(blockingQueue.size());
        System.out.println(blockingQueue.remainingCapacity());blockingQueue.put("1");
        System.out.println(blockingQueue.size());
        System.out.println(blockingQueue.remainingCapacity());
        blockingQueue.put("1");
        System.out.println(blockingQueue.size());
        System.out.println(blockingQueue.remainingCapacity());blockingQueue.put("1");
        System.out.println(blockingQueue.size());
        System.out.println(blockingQueue.remainingCapacity());
        blockingQueue.take();
        blockingQueue.take();
        blockingQueue.take();
        System.out.println(blockingQueue.size());
        System.out.println(blockingQueue.remainingCapacity());
        blockingQueue.put("1");
        System.out.println(blockingQueue.size());
        System.out.println(blockingQueue.remainingCapacity());
        blockingQueue.put("1");
        System.out.println(blockingQueue.size());
        System.out.println(blockingQueue.remainingCapacity());
        blockingQueue.put("1");
        System.out.println(blockingQueue.size());
        System.out.println(blockingQueue.remainingCapacity());
        /*for (int i = 0; i < 20000000; i++) {
            executorService.execute(() -> {
                sum();
                countDownLatch.countDown();
            });
        }*/
        countDownLatch.await();
        //System.out.println(System.currentTimeMillis() - now);
        executorService.shutdown();
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
