import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.withlock.OptimizedArrBlockQueue;
import com.lzp.util.concurrent.latch.CountDownLatch;
import com.lzp.util.concurrent.threadpool.*;
import com.lzp.util.concurrent.threadpool.ThreadPoolExecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
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

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        //com.lzp.util.concurrent.threadpool.ThreadPoolExecutor executorService = new ThreadPoolExecutor(4, 4, 0, new ArrayBlockingQueue(10000000), new ThreadFactoryImpl(""));
        //BlockingQueue blockingQueue = new OptimizedArrBlockQueue(5);
        //com.lzp.util.concurrent.latch.CountDownLatch countDownLatch = new com.lzp.util.concurrent.latch.CountDownLatch(16000000);
        ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue(10);
        /*for (int i = 0; i < 1000; i++) {
            new AtomicInteger(1);
        }*/
        //new CountDownLatch(1).await();
        /*java.util.concurrent.CountDownLatch countDownLatch = new java.util.concurrent.CountDownLatch(16000000);
        long now = System.currentTimeMillis();

        countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);
        System.out.println(countDownLatch.getCount());*/
        //executorService.shutdown();

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
